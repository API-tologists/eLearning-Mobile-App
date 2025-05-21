package com.example.elearning.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val category: String,
    val downloadDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_sections")
data class SectionEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val order: Int
)

@Entity(tableName = "downloaded_lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val sectionId: String,
    val courseId: String,
    val title: String,
    val description: String,
    val duration: String,
    val order: Int,
    var localVideoPath: String? = null,
    var localPdfPath: String? = null,
    var localImagePath: String? = null
) 