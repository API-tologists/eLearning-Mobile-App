package com.example.elearning.model

// import com.google.firebase.firestore.Exclude // Removed
// import com.google.firebase.firestore.IgnoreExtraProperties // Removed

data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val duration: String = "",
    val videoUrl: String = "",
    var completed: Boolean = false
) 