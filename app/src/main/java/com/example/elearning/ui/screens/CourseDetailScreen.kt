package com.example.elearning.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.navigation.Screen
import com.example.elearning.repository.CourseRepository
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.model.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    // In a real app, this would come from a ViewModel
    val course = remember { CourseRepository.getCourseById(courseId) }
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val progress = remember { CourseRepository.getCourseProgress(user?.id ?: "", courseId) }
    var isBookmarked by remember { mutableStateOf(false) }

    course?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(it.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isBookmarked = !isBookmarked }) {
                            Icon(
                                if (isBookmarked) Icons.Filled.Phone else Icons.Filled.ThumbUp,
                                contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    AsyncImage(
                        model = it.imageUrl,
                        contentDescription = "Course Thumbnail",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Instructor: ${it.instructor}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "$progress% Complete",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                items(it.sections) { section ->
                    CourseSection(
                        section = section,
                        onLessonClick = { lessonIndex ->
                            navController.navigate(
                                Screen.Lesson.createRoute(it.id, section.id, lessonIndex)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseSection(
    section: CourseSection,
    onLessonClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { Text(section.title) },
                trailingContent = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.Phone else Icons.Default.Favorite,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    section.lessons.forEachIndexed { index, lesson ->
                        ListItem(
                            headlineContent = { Text(text = lesson.title) },
                            supportingContent = { Text(text = lesson.duration) },
                            leadingContent = {
                                Icon(
                                    if (lesson.isCompleted) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = if (lesson.isCompleted) "Completed" else "Locked"
                                )
                            },
                            modifier = Modifier.clickable { onLessonClick(index) }
                        )
                    }
                }
            }
        }
    }
} 