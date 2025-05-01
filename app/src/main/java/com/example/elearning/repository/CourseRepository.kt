package com.example.elearning.repository

import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.model.CourseEnrollment
import com.example.elearning.model.User
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import com.google.firebase.database.DataSnapshot

class CourseRepository {
    private val database = FirebaseDatabase.getInstance()
    private val coursesRef = database.getReference("courses")
    private val enrollmentsRef = database.getReference("enrollments")
    private val usersRef = database.getReference("users")

    fun getAllCourses(): Flow<List<Course>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = snapshot.children.mapNotNull { it.getValue<Course>() }
                trySend(courses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        coursesRef.addValueEventListener(listener)
        awaitClose { coursesRef.removeEventListener(listener) }
    }

    fun getInstructorCourses(instructorId: String): Flow<List<Course>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = snapshot.children
                    .mapNotNull { it.getValue<Course>() }
                    .filter { it.instructor == instructorId }
                trySend(courses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        coursesRef.addValueEventListener(listener)
        awaitClose { coursesRef.removeEventListener(listener) }
    }

    fun getCourseById(courseId: String): Flow<Course?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val course = snapshot.getValue<Course>()
                trySend(course)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        coursesRef.child(courseId).addValueEventListener(listener)
        awaitClose { coursesRef.child(courseId).removeEventListener(listener) }
    }

    fun getEnrolledCourses(userId: String): Flow<List<Course>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enrollments = snapshot.children
                    .mapNotNull { it.getValue<CourseEnrollment>() }
                    .filter { it.studentId == userId }

                val courseIds = enrollments.map { it.courseId }
                coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(coursesSnapshot: DataSnapshot) {
                        val courses = coursesSnapshot.children
                            .mapNotNull { it.getValue<Course>() }
                            .filter { it.id in courseIds }
                        trySend(courses)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        enrollmentsRef.addValueEventListener(listener)
        awaitClose { enrollmentsRef.removeEventListener(listener) }
    }

    fun getEnrolledStudents(courseId: String): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enrollments = snapshot.children
                    .mapNotNull { it.getValue<CourseEnrollment>() }
                    .filter { it.courseId == courseId }

                val studentIds = enrollments.map { it.studentId }
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(usersSnapshot: DataSnapshot) {
                        val students = usersSnapshot.children
                            .mapNotNull { it.getValue<User>() }
                            .filter { it.id in studentIds }
                        trySend(students)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        enrollmentsRef.addValueEventListener(listener)
        awaitClose { enrollmentsRef.removeEventListener(listener) }
    }

    suspend fun createCourse(course: Course) {
        coursesRef.child(course.id).setValue(course).await()
    }

    suspend fun enrollInCourse(userId: String, courseId: String) {
        val enrollment = CourseEnrollment(
            courseId = courseId,
            studentId = userId,
            progress = 0,
            enrolledDate = System.currentTimeMillis()
        )
        enrollmentsRef.child("${userId}_${courseId}").setValue(enrollment).await()
        
        // Update enrolled students count
        coursesRef.child(courseId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val course = currentData.getValue<Course>() ?: return Transaction.abort()
                val updatedCourse = course.copy(enrolledStudents = course.enrolledStudents + 1)
                currentData.value = updatedCourse
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Transaction completed
            }
        })
    }

    suspend fun updateCourseProgress(userId: String, courseId: String, lessonId: String) {
        enrollmentsRef.child("${userId}_${courseId}").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val enrollment = currentData.getValue<CourseEnrollment>() ?: return Transaction.abort()
                val updatedEnrollment = enrollment.copy(
                    completedLessons = enrollment.completedLessons + lessonId,
                    progress = calculateProgress(enrollment.completedLessons.size + 1, getTotalLessons(courseId))
                )
                currentData.value = updatedEnrollment
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Transaction completed
            }
        })
    }

    fun getCourseProgress(userId: String, courseId: String): Flow<Int> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enrollment = snapshot.getValue<CourseEnrollment>()
                trySend(enrollment?.progress ?: 0)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        enrollmentsRef.child("${userId}_${courseId}").addValueEventListener(listener)
        awaitClose { enrollmentsRef.child("${userId}_${courseId}").removeEventListener(listener) }
    }

    suspend fun addSectionToCourse(courseId: String, sectionTitle: String) {
        val section = CourseSection(
            id = "${courseId}_${System.currentTimeMillis()}",
            title = sectionTitle,
            lessons = emptyList()
        )
        
        coursesRef.child(courseId).child("sections").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val sections = currentData.getValue<List<CourseSection>>() ?: emptyList()
                val updatedSections = sections + section
                currentData.value = updatedSections
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Transaction completed
            }
        })
    }

    suspend fun addLessonToSection(courseId: String, sectionId: String, lessonTitle: String, videoUrl: String) {
        val lesson = Lesson(
            id = "${sectionId}_${System.currentTimeMillis()}",
            title = lessonTitle,
            duration = "00:00", // You might want to calculate this based on video length
            videoUrl = videoUrl,
            isCompleted = false
        )
        
        coursesRef.child(courseId).child("sections").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val sections = currentData.getValue<List<CourseSection>>() ?: return Transaction.abort()
                val updatedSections = sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(lessons = section.lessons + lesson)
                    } else {
                        section
                    }
                }
                currentData.value = updatedSections
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                // Transaction completed
            }
        })
    }

    private fun calculateProgress(completedLessons: Int, totalLessons: Int): Int {
        return if (totalLessons > 0) {
            (completedLessons * 100) / totalLessons
        } else {
            0
        }
    }

    private fun getTotalLessons(courseId: String): Int {
        return 0 // We'll need to implement this properly
    }
} 