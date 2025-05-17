package com.example.elearning.model

data class QuizAttempt(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val sectionId: String = "",
    val quizId: String = "",
    val score: Int = 0,
    val hasPassed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) 