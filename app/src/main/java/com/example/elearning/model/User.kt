package com.example.elearning.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String,
    val enrolledCourses: List<String> = emptyList(),
    val completedLessons: List<String> = emptyList()
)

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
} 