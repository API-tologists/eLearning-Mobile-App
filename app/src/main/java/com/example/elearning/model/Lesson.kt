package com.example.elearning.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val duration: String = "",
    val videoUrl: String = "",
    val isCompleted: Boolean = false
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "duration" to duration,
            "videoUrl" to videoUrl,
            "isCompleted" to isCompleted
        )
    }
} 