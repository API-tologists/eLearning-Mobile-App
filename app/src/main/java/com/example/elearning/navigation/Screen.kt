package com.example.elearning.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object MyCourses : Screen("my_courses")
    object Bookmarks : Screen("bookmarks")
    object Assignments : Screen("assignments")
    object Quizzes : Screen("quizzes")
    object Schedule : Screen("schedule")
    object Discussions : Screen("discussions")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object Subscription : Screen("subscription/{courseId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String) = "subscription/$courseId"
    }
    object PaymentMethod : Screen("payment_method/{courseId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String) = "payment_method/$courseId"
    }
    object CourseDetail : Screen("course_detail/{courseId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }
    object Lesson : Screen("lesson/{courseId}/{sectionId}/{lessonIndex}") {
        fun createRoute(courseId: String, sectionId: String, lessonIndex: Int) =
            "lesson/$courseId/$sectionId/$lessonIndex"
    }
    object InstructorDashboard : Screen("instructor_dashboard")
    object CourseEditor : Screen("course_editor/{courseId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String) = "course_editor/$courseId"
    }
    object LessonDetails : Screen("lesson_details/{courseId}/{sectionId}/{lessonId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("sectionId") { type = NavType.StringType },
            navArgument("lessonId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String, sectionId: String, lessonId: String) =
            "lesson_details/$courseId/$sectionId/$lessonId"
    }
    object QuizEditor : Screen("quiz_editor/{courseId}/{sectionId}/{quizId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("sectionId") { type = NavType.StringType },
            navArgument("quizId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String, sectionId: String, quizId: String) =
            "quiz_editor/$courseId/$sectionId/$quizId"
    }
    object QuizTaking : Screen("quiz_taking/{courseId}/{sectionId}/{quizId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType },
            navArgument("sectionId") { type = NavType.StringType },
            navArgument("quizId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String, sectionId: String, quizId: String) =
            "quiz_taking/$courseId/$sectionId/$quizId"
    }
    object Certificate : Screen("certificate/{certificateId}") {
        fun createRoute(certificateId: String) = "certificate/$certificateId"
    }
    object AddCreditCard : Screen("add_credit_card")
    object CreditCards : Screen("credit_cards")
    object Profile : Screen("profile")
    object SubscriptionSuccess : Screen("subscription_success/{courseId}") {
        val arguments = listOf(
            navArgument("courseId") { type = NavType.StringType }
        )
        fun createRoute(courseId: String) = "subscription_success/$courseId"
    }
    object Downloads : Screen("downloads")
} 