package com.example.elearning

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.navigation.NavGraph
import com.example.elearning.ui.theme.ELearningTheme
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get database reference
        val database = Firebase.database.reference
        
        // Create sample courses
        val course1 = Course(
            id = "course1",
            title = "Introduction to Android Development",
            description = "Learn the fundamentals of Android app development with Kotlin and Jetpack Compose.",
            instructor = "John Smith",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/elearning-app-12345.appspot.com/o/course1.jpg",
            sections = listOf(
                CourseSection(
                    id = "section1",
                    title = "Getting Started",
                    lessons = listOf(
                        Lesson(
                            id = "lesson1",
                            title = "Introduction to Android",
                            duration = "10:00",
                            videoUrl = "https://example.com/video1.mp4",
                            isCompleted = false
                        ),
                        Lesson(
                            id = "lesson2",
                            title = "Setting Up Your Environment",
                            duration = "15:00",
                            videoUrl = "https://example.com/video2.mp4",
                            isCompleted = false
                        )
                    )
                ),
                CourseSection(
                    id = "section2",
                    title = "Kotlin Basics",
                    lessons = listOf(
                        Lesson(
                            id = "lesson3",
                            title = "Variables and Data Types",
                            duration = "12:00",
                            videoUrl = "https://example.com/video3.mp4",
                            isCompleted = false
                        )
                    )
                )
            )
        )

        val course2 = Course(
            id = "course2",
            title = "Advanced UI Design with Jetpack Compose",
            description = "Master the art of creating beautiful and responsive UIs using Jetpack Compose.",
            instructor = "Sarah Johnson",
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/elearning-app-12345.appspot.com/o/course2.jpg",
            sections = listOf(
                CourseSection(
                    id = "section1",
                    title = "Compose Fundamentals",
                    lessons = listOf(
                        Lesson(
                            id = "lesson1",
                            title = "Composable Functions",
                            duration = "20:00",
                            videoUrl = "https://example.com/video4.mp4",
                            isCompleted = false
                        ),
                        Lesson(
                            id = "lesson2",
                            title = "State Management",
                            duration = "25:00",
                            videoUrl = "https://example.com/video5.mp4",
                            isCompleted = false
                        )
                    )
                ),
                CourseSection(
                    id = "section2",
                    title = "Advanced UI Components",
                    lessons = listOf(
                        Lesson(
                            id = "lesson3",
                            title = "Custom Composables",
                            duration = "18:00",
                            videoUrl = "https://example.com/video6.mp4",
                            isCompleted = false
                        )
                    )
                )
            )
        )

        // Add courses to database
        database.child("courses").child(course1.id).setValue(course1)
            .addOnSuccessListener {
                Log.d(TAG, "Course 1 added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding course 1", e)
            }

        database.child("courses").child(course2.id).setValue(course2)
            .addOnSuccessListener {
                Log.d(TAG, "Course 2 added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding course 2", e)
            }

        // Verify courses in database
        database.child("courses").get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "Courses in database: ${snapshot.childrenCount}")
                snapshot.children.forEach { child ->
                    Log.d(TAG, "Course: ${child.key}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading courses", e)
            }

        setContent {
            ELearningTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel = remember { AuthViewModel() }
                    val courseViewModel = remember { CourseViewModel() }
                    
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        courseViewModel = courseViewModel
                    )
                }
            }
        }
    }
}