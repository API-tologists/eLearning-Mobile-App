package com.example.elearning.data

import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson

object CourseRepository {
    val courses = listOf(
        Course(
            id = "1",
            title = "Android Development with Jetpack Compose",
            description = "Learn how to build modern Android applications using Jetpack Compose, Android's modern toolkit for building native UI.",
            imageUrl = "https://example.com/android.jpg",
            instructor = "John Doe",
            rating = 4.8f,
            duration = "8 hours",
            category = "Mobile Development",
            progress = 65,
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Getting Started",
                    lessons = listOf(
                        Lesson("1-1", "Introduction to Android Development", "30 min", "https://example.com/video1", false),
                        Lesson("1-2", "Setting Up Your Development Environment", "45 min", "https://example.com/video2", true),
                        Lesson("1-3", "Your First Android App", "60 min", "https://example.com/video3", false)
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Jetpack Compose Basics",
                    lessons = listOf(
                        Lesson("2-1", "Introduction to Composables", "40 min", "https://example.com/video4", false),
                        Lesson("2-2", "State Management", "50 min", "https://example.com/video5", true),
                        Lesson("2-3", "Layouts and UI Elements", "45 min", "https://example.com/video6", false)
                    )
                )
            )
        ),
        Course(
            id = "2",
            title = "Web Development Fundamentals",
            description = "Master the fundamentals of web development including HTML, CSS, and JavaScript.",
            imageUrl = "https://example.com/web.jpg",
            instructor = "Jane Smith",
            rating = 4.5f,
            duration = "10 hours",
            category = "Web Development",
            progress = 30,
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "HTML & CSS",
                    lessons = listOf(
                        Lesson("2-1", "HTML Structure and Elements", "40 min", "https://example.com/video4", false),
                        Lesson("2-2", "CSS Styling and Layout", "50 min", "https://example.com/video5", true),
                        Lesson("2-3", "Responsive Design", "60 min", "https://example.com/video6", false)
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "JavaScript Basics",
                    lessons = listOf(
                        Lesson("2-4", "Variables and Data Types", "45 min", "https://example.com/video7", false),
                        Lesson("2-5", "Functions and Control Flow", "50 min", "https://example.com/video8", false),
                        Lesson("2-6", "DOM Manipulation", "55 min", "https://example.com/video9", false)
                    )
                )
            )
        ),
        Course(
            id = "3",
            title = "Machine Learning with Python",
            description = "Learn the fundamentals of machine learning and implement algorithms using Python.",
            imageUrl = "https://example.com/ml.jpg",
            instructor = "Alex Johnson",
            rating = 4.7f,
            duration = "12 hours",
            category = "Data Science",
            progress = 0,
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Python for Data Science",
                    lessons = listOf(
                        Lesson("3-1", "NumPy and Pandas", "45 min", "https://example.com/video7", false),
                        Lesson("3-2", "Data Visualization", "55 min", "https://example.com/video8", false),
                        Lesson("3-3", "Data Preprocessing", "50 min", "https://example.com/video9", false)
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Machine Learning Algorithms",
                    lessons = listOf(
                        Lesson("3-4", "Linear Regression", "60 min", "https://example.com/video10", false),
                        Lesson("3-5", "Classification Models", "65 min", "https://example.com/video11", false),
                        Lesson("3-6", "Neural Networks", "70 min", "https://example.com/video12", false)
                    )
                )
            )
        ),
        Course(
            id = "4",
            title = "iOS Development with SwiftUI",
            description = "Build beautiful iOS applications using SwiftUI and modern Apple frameworks.",
            imageUrl = "https://example.com/ios.jpg",
            instructor = "Sarah Wilson",
            rating = 4.9f,
            duration = "9 hours",
            category = "Mobile Development",
            progress = 80,
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "SwiftUI Basics",
                    lessons = listOf(
                        Lesson("4-1", "Views and Modifiers", "40 min", "https://example.com/video10", true),
                        Lesson("4-2", "State Management", "50 min", "https://example.com/video11", true),
                        Lesson("4-3", "Navigation", "45 min", "https://example.com/video12", false)
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Advanced SwiftUI",
                    lessons = listOf(
                        Lesson("4-4", "Custom Views", "55 min", "https://example.com/video13", false),
                        Lesson("4-5", "Animations", "50 min", "https://example.com/video14", false),
                        Lesson("4-6", "App Architecture", "60 min", "https://example.com/video15", false)
                    )
                )
            )
        ),
        Course(
            id = "5",
            title = "Cloud Computing with AWS",
            description = "Learn how to build and deploy applications on Amazon Web Services.",
            imageUrl = "https://example.com/aws.jpg",
            instructor = "Michael Brown",
            rating = 4.6f,
            duration = "15 hours",
            category = "Cloud Computing",
            progress = 45,
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "AWS Fundamentals",
                    lessons = listOf(
                        Lesson("5-1", "Cloud Computing Basics", "50 min", "https://example.com/video13", true),
                        Lesson("5-2", "AWS Core Services", "60 min", "https://example.com/video14", false),
                        Lesson("5-3", "Security and Compliance", "55 min", "https://example.com/video15", false)
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Application Deployment",
                    lessons = listOf(
                        Lesson("5-4", "EC2 and ECS", "65 min", "https://example.com/video16", false),
                        Lesson("5-5", "S3 and CloudFront", "55 min", "https://example.com/video17", false),
                        Lesson("5-6", "Serverless with Lambda", "60 min", "https://example.com/video18", false)
                    )
                )
            )
        )
    )

    fun getCourseById(id: String): Course? = courses.find { it.id == id }
    
    fun getFeaturedCourses(): List<Course> = courses.take(3)
    
    fun getInProgressCourses(): List<Course> = courses.filter { it.progress > 0 && it.progress < 100 }
    
    fun getCompletedCourses(): List<Course> = courses.filter { it.progress == 100 }
    
    fun getCoursesByCategory(category: String): List<Course> = 
        courses.filter { it.category.equals(category, ignoreCase = true) }
    
    fun getTopRatedCourses(): List<Course> = 
        courses.sortedByDescending { it.rating }.take(3)
} 