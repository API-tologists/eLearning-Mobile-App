package com.example.elearning.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.elearning.model.AuthState
import com.example.elearning.ui.screens.*
import com.example.elearning.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> Screen.Home.route
            else -> Screen.Login.route
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            CourseDetailScreen(
                courseId = backStackEntry.arguments?.getString("courseId") ?: "",
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.MyCourses.route) {
            MyCoursesScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(navController)
        }
        composable(Screen.Assignments.route) {
            AssignmentsScreen(navController)
        }
        composable(Screen.Quizzes.route) {
            QuizzesScreen(navController)
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController)
        }
        composable(Screen.Discussions.route) {
            DiscussionsScreen(navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("sectionId") { type = NavType.StringType },
                navArgument("lessonIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            LessonScreen(
                courseId = backStackEntry.arguments?.getString("courseId") ?: "",
                sectionId = backStackEntry.arguments?.getString("sectionId") ?: "",
                lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: 0,
                navController = navController
            )
        }
    }
}
