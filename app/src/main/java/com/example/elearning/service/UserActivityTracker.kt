package com.example.elearning.service

import android.content.Context
import android.content.SharedPreferences
import com.example.elearning.model.Course
import com.example.elearning.model.Lesson
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

class UserActivityTracker(
    private val context: Context,
    private val notificationService: NotificationService
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_activity", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance()
    
    private val _lastActiveCourse = MutableStateFlow<Course?>(null)
    val lastActiveCourse: StateFlow<Course?> = _lastActiveCourse

    fun trackLessonCompletion(userId: String, courseId: String, lessonId: String) {
        // Save the last completed lesson
        prefs.edit().apply {
            putString("last_course_id", courseId)
            putString("last_lesson_id", lessonId)
            putLong("last_activity_time", System.currentTimeMillis())
            apply()
        }

        // Check if this was the last lesson in a module
        checkModuleCompletion(userId, courseId, lessonId)
    }

    fun trackQuizCompletion(userId: String, courseId: String, quizId: String) {
        prefs.edit().apply {
            putString("last_quiz_id", quizId)
            putLong("last_activity_time", System.currentTimeMillis())
            apply()
        }
    }

    fun checkInactiveUsers() {
        val lastActivityTime = prefs.getLong("last_activity_time", 0)
        val currentTime = System.currentTimeMillis()
        val daysSinceLastActivity = TimeUnit.MILLISECONDS.toDays(currentTime - lastActivityTime)

        if (daysSinceLastActivity >= 3) {
            notificationService.sendReengagementNotification()
        }
    }

    private fun checkModuleCompletion(userId: String, courseId: String, lessonId: String) {
        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { document ->
                val course = document.toObject(Course::class.java)
                if (course != null) {
                    // Find the current section
                    val currentSection = course.sections.find { section ->
                        section.lessons.any { it.id == lessonId }
                    }

                    if (currentSection != null) {
                        // Check if this was the last lesson in the section
                        val isLastLesson = currentSection.lessons.last().id == lessonId
                        if (isLastLesson) {
                            notificationService.sendCompleteModuleNotification(
                                course = course,
                                moduleName = currentSection.title
                            )
                        }
                    }
                }
            }
    }

    fun checkIncompleteQuizzes(userId: String) {
        db.collection("courses")
            .whereArrayContains("enrolledStudents", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val course = document.toObject(Course::class.java)
                    course.sections.forEach { section ->
                        section.quizzes.forEach { quiz ->
                            // Check if user hasn't completed this quiz
                            db.collection("quiz_attempts")
                                .whereEqualTo("studentId", userId)
                                .whereEqualTo("quizId", quiz.id)
                                .get()
                                .addOnSuccessListener { attempts ->
                                    if (attempts.isEmpty) {
                                        notificationService.sendQuizReminderNotification(
                                            course = course,
                                            quizName = quiz.title
                                        )
                                    }
                                }
                        }
                    }
                }
            }
    }

    fun trackCourseProgress(userId: String, courseId: String) {
        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { document ->
                val course = document.toObject(Course::class.java)
                if (course != null) {
                    // Get the last completed lesson
                    val lastLessonId = prefs.getString("last_lesson_id", null)
                    if (lastLessonId != null) {
                        val lastLesson = course.sections
                            .flatMap { it.lessons }
                            .find { it.id == lastLessonId }

                        if (lastLesson != null) {
                            _lastActiveCourse.value = course
                            notificationService.sendResumeCourseNotification(
                                course = course,
                                lastLesson = lastLesson
                            )
                        }
                    }
                }
            }
    }
} 