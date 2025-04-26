package com.example.elearning.data

import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson

object CourseRepository {
    val courses = listOf(
        Course(
            id = "1",
            title = "Android Development with Jetpack Compose",
            instructor = "John Doe",
            thumbnail = "https://example.com/android.jpg",
            progress = 0.65f,
            description = "Learn how to build modern Android applications using Jetpack Compose, Android's modern toolkit for building native UI.",
            rating = 4.8f,
            duration = "8 hours",
            category = "Mobile Development",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Getting Started",
                    lessons = listOf(
                        "Introduction to Android Development",
                        "Setting Up Your Development Environment",
                        "Your First Android App"
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Jetpack Compose Basics",
                    lessons = listOf(
                        "Introduction to Composables",
                        "State Management",
                        "Layouts and UI Elements"
                    )
                ),
                CourseSection(
                    id = "3",
                    title = "Advanced UI Development",
                    lessons = listOf(
                        "Custom Composables",
                        "Animation and Transitions",
                        "Material Design 3"
                    )
                )
            ),
            lessons = listOf(
                Lesson("1-1", "Introduction to Android Development", "30 min", "https://example.com/video1", false),
                Lesson("1-2", "Setting Up Your Development Environment", "45 min", "https://example.com/video2", true),
                Lesson("1-3", "Your First Android App", "60 min", "https://example.com/video3", false)
            )
        ),
        Course(
            id = "2",
            title = "Web Development Fundamentals",
            instructor = "Jane Smith",
            thumbnail = "https://example.com/web.jpg",
            progress = 0.3f,
            description = "Master the fundamentals of web development including HTML, CSS, and JavaScript.",
            rating = 4.5f,
            duration = "10 hours",
            category = "Web Development",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "HTML & CSS",
                    lessons = listOf(
                        "HTML Structure and Elements",
                        "CSS Styling and Layout",
                        "Responsive Design"
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "JavaScript Basics",
                    lessons = listOf(
                        "Variables and Data Types",
                        "Functions and Control Flow",
                        "DOM Manipulation"
                    )
                )
            ),
            lessons = listOf(
                Lesson("2-1", "HTML Structure and Elements", "40 min", "https://example.com/video4", false),
                Lesson("2-2", "CSS Styling and Layout", "50 min", "https://example.com/video5", true),
                Lesson("2-3", "Responsive Design", "60 min", "https://example.com/video6", false)
            )
        ),
        Course(
            id = "3",
            title = "Machine Learning with Python",
            instructor = "Alex Johnson",
            thumbnail = "https://example.com/ml.jpg",
            progress = 0.0f,
            description = "Learn the fundamentals of machine learning and implement algorithms using Python.",
            rating = 4.7f,
            duration = "12 hours",
            category = "Data Science",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "Python for Data Science",
                    lessons = listOf(
                        "NumPy and Pandas",
                        "Data Visualization",
                        "Data Preprocessing"
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Machine Learning Algorithms",
                    lessons = listOf(
                        "Linear Regression",
                        "Classification Models",
                        "Neural Networks"
                    )
                )
            ),
            lessons = listOf(
                Lesson("3-1", "NumPy and Pandas", "45 min", "https://example.com/video7", false),
                Lesson("3-2", "Data Visualization", "55 min", "https://example.com/video8", false),
                Lesson("3-3", "Data Preprocessing", "50 min", "https://example.com/video9", false)
            )
        ),
        Course(
            id = "4",
            title = "iOS Development with SwiftUI",
            instructor = "Sarah Wilson",
            thumbnail = "https://example.com/ios.jpg",
            progress = 0.8f,
            description = "Build beautiful iOS applications using SwiftUI and modern Apple frameworks.",
            rating = 4.9f,
            duration = "9 hours",
            category = "Mobile Development",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "SwiftUI Basics",
                    lessons = listOf(
                        "Views and Modifiers",
                        "State Management",
                        "Navigation"
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Advanced SwiftUI",
                    lessons = listOf(
                        "Custom Views",
                        "Animations",
                        "App Architecture"
                    )
                )
            ),
            lessons = listOf(
                Lesson("4-1", "Views and Modifiers", "40 min", "https://example.com/video10", true),
                Lesson("4-2", "State Management", "50 min", "https://example.com/video11", true),
                Lesson("4-3", "Navigation", "45 min", "https://example.com/video12", false)
            )
        ),
        Course(
            id = "5",
            title = "Cloud Computing with AWS",
            instructor = "Michael Brown",
            thumbnail = "https://example.com/aws.jpg",
            progress = 0.45f,
            description = "Learn how to build and deploy applications on Amazon Web Services.",
            rating = 4.6f,
            duration = "15 hours",
            category = "Cloud Computing",
            sections = listOf(
                CourseSection(
                    id = "1",
                    title = "AWS Fundamentals",
                    lessons = listOf(
                        "Cloud Computing Basics",
                        "AWS Core Services",
                        "Security and Compliance"
                    )
                ),
                CourseSection(
                    id = "2",
                    title = "Application Deployment",
                    lessons = listOf(
                        "EC2 and ECS",
                        "S3 and CloudFront",
                        "Serverless with Lambda"
                    )
                )
            ),
            lessons = listOf(
                Lesson("5-1", "Cloud Computing Basics", "50 min", "https://example.com/video13", true),
                Lesson("5-2", "AWS Core Services", "60 min", "https://example.com/video14", false),
                Lesson("5-3", "Security and Compliance", "55 min", "https://example.com/video15", false)
            )
        )
    )

    fun getCourseById(id: String): Course? = courses.find { it.id == id }
    
    fun getFeaturedCourses(): List<Course> = courses.take(3)
    
    fun getInProgressCourses(): List<Course> = courses.filter { it.progress > 0f && it.progress < 1f }
    
    fun getCompletedCourses(): List<Course> = courses.filter { it.progress >= 1f }
    
    fun getCoursesByCategory(category: String): List<Course> = 
        courses.filter { it.category.equals(category, ignoreCase = true) }
    
    fun getTopRatedCourses(): List<Course> = 
        courses.sortedByDescending { it.rating }.take(3)
} 