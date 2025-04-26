package com.example.elearning.model

data class Course(
    val id: String,
    val title: String,
    val instructor: String,
    val thumbnail: String,
    val progress: Float,
    val description: String = "",
    val sections: List<CourseSection> = emptyList(),
    val rating: Float = 0f,
    val duration: String = "",
    val category: String = "",
    val lessons: List<Lesson> = emptyList()
)

data class Lesson(
    val id: String,
    val title: String,
    val duration: String,
    val videoUrl: String,
    val isCompleted: Boolean = false
) 