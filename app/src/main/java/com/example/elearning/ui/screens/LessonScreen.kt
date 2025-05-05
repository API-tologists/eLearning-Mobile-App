package com.example.elearning.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.model.Lesson
import com.example.elearning.navigation.Screen
import com.example.elearning.viewmodel.CourseViewModel
import com.example.elearning.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    courseId: String,
    sectionId: String,
    lessonIndex: Int,
    navController: NavController,
    courseViewModel: CourseViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    val currentSection = course?.sections?.find { it.id == sectionId }
    val currentLesson = currentSection?.lessons?.getOrNull(lessonIndex)
    val hasNextLesson = currentSection?.lessons?.size?.let { it > lessonIndex + 1 } ?: false
    val hasNextSection = course?.sections?.indexOf(currentSection)?.let { it < (course?.sections?.size ?: 0) - 1 } ?: false

    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        courseViewModel.loadCourseById(courseId)
    }

    LaunchedEffect(course, currentLesson) {
        currentLesson?.let { lesson ->
            completed = lesson.completed
            Log.d("LessonScreen", "Lesson completion status updated: "+lesson.completed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentLesson?.title ?: "Lesson") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add to bookmarks */ }) {
                        Icon(Icons.Outlined.Star, contentDescription = "Bookmark")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Video player placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lesson content
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentLesson?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress and completion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${currentLesson?.duration ?: ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (completed) "Completed" else "Mark as Complete",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { 
                            completed = it
                            if (it) {
                                currentLesson?.id?.let { lessonId ->
                                    val authState = authViewModel.authState.value
                                    if (authState is com.example.elearning.model.AuthState.Authenticated) {
                                        courseViewModel.updateCourseProgress(
                                            userId = authState.user.id,
                                            courseId = courseId,
                                            lessonId = lessonId
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { 
                        if (lessonIndex > 0) {
                            navController.navigate(
                                Screen.Lesson.createRoute(
                                    courseId = courseId,
                                    sectionId = sectionId,
                                    lessonIndex = lessonIndex - 1
                                )
                            )
                        }
                    },
                    enabled = lessonIndex > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }

                Button(
                    onClick = { 
                        // Mark current lesson as completed and update progress
                        if (!completed) {
                            completed = true
                            currentLesson?.id?.let { lessonId ->
                                val authState = authViewModel.authState.value
                                if (authState is com.example.elearning.model.AuthState.Authenticated) {
                                    courseViewModel.updateCourseProgress(
                                        userId = authState.user.id,
                                        courseId = courseId,
                                        lessonId = lessonId
                                    )
                                }
                            }
                        }

                        // Navigate to next lesson
                        if (hasNextLesson) {
                            // Navigate to next lesson in current section
                            navController.navigate(
                                Screen.Lesson.createRoute(
                                    courseId = courseId,
                                    sectionId = sectionId,
                                    lessonIndex = lessonIndex + 1
                                )
                            )
                        } else if (hasNextSection) {
                            // Navigate to first lesson of next section
                            val nextSectionIndex = course?.sections?.indexOf(currentSection)?.plus(1) ?: 0
                            val nextSection = course?.sections?.getOrNull(nextSectionIndex)
                            nextSection?.let {
                                navController.navigate(
                                    Screen.Lesson.createRoute(
                                        courseId = courseId,
                                        sectionId = it.id,
                                        lessonIndex = 0
                                    )
                                )
                            }
                        }
                    },
                    enabled = hasNextLesson || hasNextSection
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
} 