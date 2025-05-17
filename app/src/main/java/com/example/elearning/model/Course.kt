package com.example.elearning.model

// import com.google.firebase.database.Exclude // Removed
// import com.google.firebase.database.IgnoreExtraProperties // Removed

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
)

data class CourseSection(
    val id: String = "",
    val title: String = "",
    val lessons: List<Lesson> = emptyList(),
    val quizzes: List<Quiz> = emptyList()
)

data class CourseEnrollment(
    val courseId: String = "",
    val studentId: String = "",
    val progress: Int = 0,
    val enrolledDate: Long = System.currentTimeMillis(),
    val completedLessons: List<String> = emptyList()
)



