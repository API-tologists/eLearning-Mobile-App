package com.example.elearning.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val profileImage: String = "",
    val enrolledCourses: List<String> = emptyList(),
    val completedLessons: List<String> = emptyList()
)

enum class UserRole {
    STUDENT,
    INSTRUCTOR
} 