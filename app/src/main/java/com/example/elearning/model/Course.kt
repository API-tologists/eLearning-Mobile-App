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

data class CourseEnrollment(
    val courseId: String,
    val studentId: String,
    val progress: Int,
    val enrolledDate: Long = System.currentTimeMillis()
) 