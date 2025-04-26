package com.example.elearning.model

data class CourseSection(
    val id: String,
    val title: String,
    val lessons: List<Lesson>
) 