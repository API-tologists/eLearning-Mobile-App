package com.example.elearning.model

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val instructor: String,
    val rating: Float,
    val duration: String,
    val category: String,
    val progress: Int,
    val sections: List<CourseSection>
)

data class CourseSection(
    val id: String,
    val title: String,
    val lessons: List<Lesson>
)

data class Lesson(
    val id: String,
    val title: String,
    val duration: String,
    val videoUrl: String,
    val isCompleted: Boolean
) 