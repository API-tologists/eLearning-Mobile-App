package com.example.elearning.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val instructor: String = "",
    val rating: Float = 0f,
    val duration: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val enrolledStudents: Int = 0,
    val sections: List<CourseSection> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "instructor" to instructor,
            "rating" to rating,
            "duration" to duration,
            "category" to category,
            "price" to price,
            "enrolledStudents" to enrolledStudents,
            "sections" to sections.map { it.toMap() },
            "createdAt" to createdAt
        )
    }
}

@IgnoreExtraProperties
data class CourseSection(
    val id: String = "",
    val title: String = "",
    val lessons: List<Lesson> = emptyList()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "lessons" to lessons.map { it.toMap() }
        )
    }
}

data class CourseEnrollment(
    val courseId: String = "",
    val studentId: String = "",
    val progress: Int = 0,
    val enrolledDate: Long = System.currentTimeMillis(),
    val completedLessons: List<String> = emptyList()
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "courseId" to courseId,
            "studentId" to studentId,
            "progress" to progress,
            "enrolledDate" to enrolledDate,
            "completedLessons" to completedLessons
        )
    }
} 