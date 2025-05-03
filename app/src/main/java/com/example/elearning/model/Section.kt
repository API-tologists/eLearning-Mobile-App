package com.example.elearning.model
 
data class Section(
    val id: String,
    val title: String,
    val lessons: List<Lesson>
) 