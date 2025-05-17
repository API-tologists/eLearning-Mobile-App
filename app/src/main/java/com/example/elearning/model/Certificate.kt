package com.example.elearning.model

data class Certificate(
    val id: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val instructorId: String = "",
    val instructorName: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val certificateNumber: String = "" // Format: CERT-YYYY-XXXXX where X is a random number
) 