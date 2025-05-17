package com.example.elearning.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.elearning.model.AuthState
import com.example.elearning.model.UserRole
import com.example.elearning.ui.screens.*
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user

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
            if (user != null) {
                if (user.role == UserRole.INSTRUCTOR) {
                    InstructorDashboardScreen(
                        navController = navController,
                        user = user,
                        courseViewModel = courseViewModel,
                        authViewModel = authViewModel
                    )
                } else {
                    HomeScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        courseViewModel = courseViewModel
                    )
                }
            }
        }
        composable(Screen.MyCourses.route) {
            MyCoursesScreen(
                navController = navController,
                authViewModel = authViewModel,
                courseViewModel = courseViewModel
            )
        }
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(navController = navController)
        }
        composable(Screen.Assignments.route) {
            AssignmentsScreen(navController = navController)
        }
        composable(Screen.Quizzes.route) {
            QuizzesScreen(navController = navController)
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController = navController)
        }
        composable(Screen.Discussions.route) {
            DiscussionsScreen(navController = navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = Screen.CourseDetail.route,
            arguments = Screen.CourseDetail.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseDetailScreen(
                navController = navController,
                authViewModel = authViewModel,
                courseViewModel = courseViewModel,
                courseId = courseId
            )
        }
        composable(
            route = Screen.Lesson.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("sectionId") { type = NavType.StringType },
                navArgument("lessonIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: return@composable
            val lessonIndex = backStackEntry.arguments?.getInt("lessonIndex") ?: return@composable
            LessonScreen(
                navController = navController,
                courseId = courseId,
                sectionId = sectionId,
                lessonIndex = lessonIndex,
                courseViewModel = courseViewModel,
                authViewModel = authViewModel
            )
        }
        composable(
            route = Screen.CourseEditor.route,
            arguments = Screen.CourseEditor.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseEditorScreen(
                navController = navController,
                courseId = courseId,
                courseViewModel = courseViewModel
            )
        }
        composable(
            route = Screen.LessonDetails.route,
            arguments = Screen.LessonDetails.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: return@composable
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            LessonDetailsScreen(
                navController = navController,
                courseId = courseId,
                sectionId = sectionId,
                lessonId = lessonId,
                courseViewModel = courseViewModel
            )
        }
        composable(
            route = Screen.QuizEditor.route,
            arguments = Screen.QuizEditor.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: return@composable
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            QuizEditorScreen(
                navController = navController,
                courseId = courseId,
                sectionId = sectionId,
                quizId = quizId,
                courseViewModel = courseViewModel
            )
        }
        composable(
            route = Screen.QuizTaking.route,
            arguments = Screen.QuizTaking.arguments
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: return@composable
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            QuizTakingScreen(
                navController = navController,
                courseId = courseId,
                sectionId = sectionId,
                quizId = quizId,
                courseViewModel = courseViewModel
            )
        }
    }
}
