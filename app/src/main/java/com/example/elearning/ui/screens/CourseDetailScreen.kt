package com.example.elearning.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // In a real app, this would come from a ViewModel
    val course = remember {
        Course(
            id = courseId,
            title = "Android Development with Jetpack Compose",
            instructor = "John Doe",
            thumbnail = "https://example.com/android.jpg",
            progress = 0.65f,
            description = "Learn how to build modern Android applications using Jetpack Compose, Android's modern toolkit for building native UI.",
            lessons = listOf(
                Lesson(
                    "1",
                    "Introduction to Kotlin",
                    "30 min",
                    "https://example.com/video1",
                    false
                ),
                Lesson("2", "Variables and Types", "45 min", "https://example.com/video2", false),
                Lesson("3", "Control Flow", "40 min", "https://example.com/video3", false)
            ),
            sections = TODO(),
            rating = TODO(),
            duration = TODO(),
            category = TODO()
        )
    }

    var isBookmarked by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    model = course.thumbnail,
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
                        text = course.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Instructor: ${course.instructor}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = course.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(course.progress * 100).toInt()}% Complete",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            items(course.sections) { section ->
                CourseSection(
                    section = section,
                    onLessonClick = { lessonIndex ->
                        navController.navigate(
                            Screen.Lesson.route + "/${course.id}/${section.id}/$lessonIndex"
                        )
                    }
                )
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
                            headlineContent = { Text(lesson) },
                            leadingContent = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            modifier = Modifier.clickable { onLessonClick(index) }
                        )
                    }
                }
            }
        }
    }
} 