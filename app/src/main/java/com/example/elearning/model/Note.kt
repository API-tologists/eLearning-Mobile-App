package com.example.elearning.model

data class Note(
    val id: String = "",
    val lessonId: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 