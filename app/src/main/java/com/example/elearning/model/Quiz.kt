package com.example.elearning.model

data class Quiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val questions: List<Question> = emptyList(),
    val passingScore: Int = 70,
    val timeLimit: Int = 0, // in minutes, 0 means no time limit
    val attemptsAllowed: Int = 1
)

data class Question(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val points: Int = 1
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SHORT_ANSWER
} 