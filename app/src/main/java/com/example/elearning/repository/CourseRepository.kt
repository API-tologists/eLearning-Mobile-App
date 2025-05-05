package com.example.elearning.repository

import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.model.CourseEnrollment
import com.example.elearning.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CourseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val coursesCollection = db.collection("courses")
    private val enrollmentsCollection = db.collection("enrollments")
    private val usersCollection = db.collection("users")

    fun getAllCourses(): Flow<List<Course>> = callbackFlow {
        val listener = coursesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val courses = snapshot?.toObjects(Course::class.java) ?: emptyList()
            trySend(courses)
        }
        awaitClose { listener.remove() }
    }

    fun getInstructorCourses(instructorId: String): Flow<List<Course>> = callbackFlow {
        val listener = coursesCollection.whereEqualTo("instructor", instructorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val courses = snapshot?.toObjects(Course::class.java) ?: emptyList()
                trySend(courses)
            }
        awaitClose { listener.remove() }
    }

    fun getCourseById(courseId: String): Flow<Course?> = callbackFlow {
        val listener = coursesCollection.document(courseId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val course = snapshot?.toObject(Course::class.java)
            trySend(course)
        }
        awaitClose { listener.remove() }
    }

    fun getEnrolledCourses(userId: String): Flow<List<Course>> = callbackFlow {
        val listener = enrollmentsCollection.whereEqualTo("studentId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val enrollments = snapshot?.toObjects(CourseEnrollment::class.java) ?: emptyList()
                val courseIds = enrollments.map { it.courseId }
                if (courseIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    coursesCollection.whereIn("id", courseIds).get().addOnSuccessListener { coursesSnapshot ->
                        val courses = coursesSnapshot.toObjects(Course::class.java)
                        trySend(courses)
                    }.addOnFailureListener { close(it) }
                }
            }
        awaitClose { listener.remove() }
    }

    fun getEnrolledStudents(courseId: String): Flow<List<User>> = callbackFlow {
        val listener = enrollmentsCollection.whereEqualTo("courseId", courseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val enrollments = snapshot?.toObjects(CourseEnrollment::class.java) ?: emptyList()
                val studentIds = enrollments.map { it.studentId }
                if (studentIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    usersCollection.whereIn("id", studentIds).get().addOnSuccessListener { usersSnapshot ->
                        val students = usersSnapshot.toObjects(User::class.java)
                        trySend(students)
                    }.addOnFailureListener { close(it) }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createCourse(course: Course) {
        coursesCollection.document(course.id).set(course).await()
    }

    suspend fun enrollInCourse(userId: String, courseId: String) {
        val enrollment = CourseEnrollment(
            courseId = courseId,
            studentId = userId,
            progress = 0,
            enrolledDate = System.currentTimeMillis()
        )
        enrollmentsCollection.document("${userId}_${courseId}").set(enrollment).await()
        // Update enrolled students count
        val courseRef = coursesCollection.document(courseId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(courseRef)
            val course = snapshot.toObject(Course::class.java)
            if (course != null) {
                val updatedCourse = course.copy(enrolledStudents = course.enrolledStudents + 1)
                transaction.set(courseRef, updatedCourse)
            }
        }.await()
    }

    suspend fun updateCourseProgress(userId: String, courseId: String, lessonId: String) {
        val enrollmentRef = enrollmentsCollection.document("${userId}_${courseId}")
        db.runTransaction { transaction ->
            val snapshot = transaction.get(enrollmentRef)
            val enrollment = snapshot.toObject(CourseEnrollment::class.java)
            if (enrollment != null) {
                val updatedCompletedLessons = enrollment.completedLessons + lessonId
                val updatedProgress = calculateProgress(updatedCompletedLessons.size, getTotalLessons(courseId))
                val updatedEnrollment = enrollment.copy(
                    completedLessons = updatedCompletedLessons,
                    progress = updatedProgress
                )
                transaction.set(enrollmentRef, updatedEnrollment)
            }
        }.await()
    }

    fun getCourseProgress(userId: String, courseId: String): Flow<Int> = callbackFlow {
        val listener = enrollmentsCollection.document("${userId}_${courseId}")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val enrollment = snapshot?.toObject(CourseEnrollment::class.java)
                trySend(enrollment?.progress ?: 0)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSectionToCourse(courseId: String, sectionTitle: String) {
        val section = CourseSection(
            id = "${courseId}_${System.currentTimeMillis()}",
            title = sectionTitle,
            lessons = emptyList()
        )
        val courseRef = coursesCollection.document(courseId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(courseRef)
            val course = snapshot.toObject(Course::class.java)
            if (course != null) {
                val updatedSections = course.sections + section
                val updatedCourse = course.copy(sections = updatedSections)
                transaction.set(courseRef, updatedCourse)
            }
        }.await()
    }

    suspend fun addLessonToSection(courseId: String, sectionId: String, lessonTitle: String, lessonDescription: String = "", lessonVideoUrl: String = "", lessonDuration: String = "") {
        val lesson = Lesson(
            id = "${sectionId}_${System.currentTimeMillis()}",
            title = lessonTitle,
            description = lessonDescription,
            videoUrl = lessonVideoUrl,
            duration = lessonDuration,
            completed = false
        )
        val courseRef = coursesCollection.document(courseId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(courseRef)
            val course = snapshot.toObject(Course::class.java)
            if (course != null) {
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(lessons = section.lessons + lesson)
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                transaction.set(courseRef, updatedCourse)
            }
        }.await()
    }

    private fun calculateProgress(completedLessons: Int, totalLessons: Int): Int {
        return if (totalLessons > 0) {
            (completedLessons * 100) / totalLessons
        } else {
            0
        }
    }

    private fun getTotalLessons(courseId: String): Int {
        // This should be implemented to fetch the course and count all lessons in all sections
        // For now, return 0
        return 0
    }
} 