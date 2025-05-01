package com.example.elearning.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.example.elearning.model.AuthState
import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.navigation.Screen
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel,
    courseId: String
) {
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val course by courseViewModel.selectedCourse.collectAsState()
    val courseProgress by courseViewModel.courseProgress.collectAsState()
    
    var isEnrolled by remember { mutableStateOf(false) }
    
    LaunchedEffect(courseId) {
        courseViewModel.loadCourseById(courseId)
        user?.id?.let { userId ->
            courseViewModel.loadCourseProgress(userId, courseId)
        }
    }
    
    LaunchedEffect(course, user) {
        if (course != null && user != null) {
            courseViewModel.loadEnrolledCourses(user.id)
            isEnrolled = courseViewModel.enrolledCourses.value.any { it.id == courseId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        course?.let { currentCourse ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AsyncImage(
                        model = currentCourse.imageUrl,
                        contentDescription = "Course Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentCourse.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Instructor: ${currentCourse.instructor}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rating: ${currentCourse.rating}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Duration: ${currentCourse.duration}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentCourse.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (user != null) {
                        if (isEnrolled) {
                            Text(
                                text = "Progress: $courseProgress%",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinearProgressIndicator(
                                progress = { courseProgress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Button(
                                onClick = {
                                    user.id?.let { userId ->
                                        courseViewModel.enrollInCourse(userId, currentCourse.id)
                                        isEnrolled = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enroll Now")
                            }
                        }
                    }
                }
                
                items(currentCourse.sections) { section ->
                    CourseSectionItem(section = section)
                }
            }
        }
    }
}

@Composable
private fun CourseSectionItem(section: CourseSection) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            section.lessons.forEach { lesson ->
                LessonItem(lesson = lesson)
            }
        }
    }
}

@Composable
private fun LessonItem(lesson: Lesson) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
            contentDescription = if (lesson.isCompleted) "Completed" else "Not Completed",
            tint = if (lesson.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = lesson.duration,
                style = MaterialTheme.typography.bodyMedium
            )
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