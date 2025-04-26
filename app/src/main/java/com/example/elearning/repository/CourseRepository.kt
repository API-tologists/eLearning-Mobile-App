package com.example.elearning.repository

import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.model.CourseEnrollment

object CourseRepository {
    private val enrolledCourses = listOf(
        Course(
            id = "1",
            title = "Android Development Basics",
            description = "Learn the fundamentals of Android development with Kotlin",
            imageUrl = "https://example.com/android.jpg",
            instructor = "John Doe",
            rating = 4.5f,
            duration = "8 hours",
            category = "Mobile Development",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Introduction to Android",
                    lessons = listOf(
                        Lesson(
                            id = "1",
                            title = "Android Studio Setup",
                            duration = "30 min",
                            videoUrl = "https://example.com/video1",
                            isCompleted = true
                        ),
                        Lesson(
                            id = "2",
                            title = "Basic UI Components",
                            duration = "45 min",
                            videoUrl = "https://example.com/video2",
                            isCompleted = true
                        )
                    )
                )
            )
        ),
        Course(
            id = "2",
            title = "Kotlin Programming",
            description = "Master Kotlin programming language",
            imageUrl = "https://example.com/kotlin.jpg",
            instructor = "Jane Smith",
            rating = 4.8f,
            duration = "10 hours",
            category = "Programming",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Kotlin Basics",
                    lessons = listOf(
                        Lesson(
                            id = "1",
                            title = "Variables and Types",
                            duration = "40 min",
                            videoUrl = "https://example.com/video3",
                            isCompleted = true
                        ),
                        Lesson(
                            id = "2",
                            title = "Control Flow",
                            duration = "50 min",
                            videoUrl = "https://example.com/video4",
                            isCompleted = true
                        )
                    )
                )
            )
        ),
        Course(
            id = "3",
            title = "Jetpack Compose",
            description = "Modern UI development with Jetpack Compose",
            imageUrl = "https://example.com/compose.jpg",
            instructor = "Mike Johnson",
            rating = 4.7f,
            duration = "12 hours",
            category = "UI Development",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Compose Basics",
                    lessons = listOf(
                        Lesson(
                            id = "1",
                            title = "Introduction to Compose",
                            duration = "35 min",
                            videoUrl = "https://example.com/video5",
                            isCompleted = false
                        ),
                        Lesson(
                            id = "2",
                            title = "Layouts and Modifiers",
                            duration = "45 min",
                            videoUrl = "https://example.com/video6",
                            isCompleted = false
                        )
                    )
                )
            )
        )
    )

    private val courseEnrollments = mutableListOf(
        CourseEnrollment(
            courseId = "1",
            studentId = "1", // John Doe's ID
            progress = 30
        )
    )

    fun getEnrolledCourses(): List<Course> = enrolledCourses

    fun getCourseById(id: String): Course? = enrolledCourses.find { it.id == id }

    fun getCourseProgress(studentId: String, courseId: String): Int {
        return courseEnrollments.find { it.studentId == studentId && it.courseId == courseId }?.progress ?: 0
    }

    fun enrollStudentInCourse(studentId: String, courseId: String) {
        if (courseEnrollments.none { it.studentId == studentId && it.courseId == courseId }) {
            courseEnrollments.add(CourseEnrollment(courseId, studentId, 0))
        }
    }

    fun updateCourseProgress(studentId: String, courseId: String, progress: Int) {
        val enrollment = courseEnrollments.find { it.studentId == studentId && it.courseId == courseId }
        enrollment?.let {
            courseEnrollments[courseEnrollments.indexOf(it)] = it.copy(progress = progress)
        }
    }
} 