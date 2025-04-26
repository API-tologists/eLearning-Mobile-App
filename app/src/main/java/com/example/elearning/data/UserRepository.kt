package com.example.elearning.data

import com.example.elearning.model.User
import com.example.elearning.model.Course
import com.example.elearning.model.Lesson

object UserRepository {
    private val users = mutableListOf(
        User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            profileImage = "https://example.com/profile1.jpg",
            enrolledCourses = listOf("1", "2"),
            completedLessons = listOf("1", "2", "4")
        ),
        User(
            id = "2",
            name = "Jane Smith",
            email = "jane@example.com",
            profileImage = "https://example.com/profile2.jpg",
            enrolledCourses = listOf("1", "3"),
            completedLessons = listOf("1", "7", "8")
        ),
        User(
            id = "3",
            name = "Mike Johnson",
            email = "mike@example.com",
            profileImage = "https://example.com/profile3.jpg",
            enrolledCourses = listOf("2", "3"),
            completedLessons = listOf("4", "5", "6")
        )
    )

    fun getUserById(id: String): User? = users.find { it.id == id }

    fun getUserByEmail(email: String): User? = users.find { it.email == email }

    fun updateUserProgress(userId: String, courseId: String, lessonId: String) {
        val user = users.find { it.id == userId }
        user?.let {
            val updatedUser = it.copy(
                completedLessons = it.completedLessons + lessonId
            )
            users[users.indexOf(it)] = updatedUser
        }
    }

    fun enrollUserInCourse(userId: String, courseId: String) {
        val user = users.find { it.id == userId }
        user?.let {
            if (!it.enrolledCourses.contains(courseId)) {
                val updatedUser = it.copy(
                    enrolledCourses = it.enrolledCourses + courseId
                )
                users[users.indexOf(it)] = updatedUser
            }
        }
    }

    fun getCourseProgress(userId: String, courseId: String): Float {
        val user = users.find { it.id == userId }
        val course = CourseRepository.courses.find { it.id == courseId }
        
        return if (user != null && course != null) {
            val completedLessons = user.completedLessons.count { lessonId ->
                course.lessons.any { it.id == lessonId }
            }
            completedLessons.toFloat() / course.lessons.size
        } else {
            0f
        }
    }
} 