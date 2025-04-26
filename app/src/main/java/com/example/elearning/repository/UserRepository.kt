package com.example.elearning.repository

import com.example.elearning.model.User

object UserRepository {
    private val users = mutableListOf(
        User(
            id = "1",
            name = "John Doe",
            email = "john@example.com",
            profileImage = "https://example.com/profile1.jpg",
            enrolledCourses = listOf("1") // Android Development Basics course
        )
    )

    fun addUser(user: User) {
        if (users.none { it.id == user.id }) {
            users.add(user)
        }
    }

    fun getUserById(id: String): User? = users.find { it.id == id }

    fun updateUser(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
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
                CourseRepository.enrollStudentInCourse(userId, courseId)
            }
        }
    }

    fun getCourseProgress(userId: String, courseId: String): Float {
        return CourseRepository.getCourseProgress(userId, courseId).toFloat()
    }
} 