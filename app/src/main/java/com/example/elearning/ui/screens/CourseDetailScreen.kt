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

import androidx.compose.material.icons.outlined.PlayArrow
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
import com.example.elearning.ui.components.RatingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel,
    courseId: String
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val enrolledCourses by courseViewModel.enrolledCourses.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }
    var courseProgress by remember { mutableStateOf(0) }

    // Calculate if user is enrolled
    val isEnrolled = remember(course, enrolledCourses) {
        course?.id?.let { courseId ->
            enrolledCourses.any { it.id == courseId }
        } ?: false
    }

    // Load course details and progress
    LaunchedEffect(courseId) {
        courseViewModel.loadCourseById(courseId)
        user?.id?.let { userId ->
            courseViewModel.loadCourseProgress(userId, courseId)
            courseViewModel.loadEnrolledCourses(userId)
        }
    }

    // Collect course progress
    LaunchedEffect(user?.id) {
        if (user?.id != null) {
            courseViewModel.courseProgress.collect { progress ->
                courseProgress = progress
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.title ?: "Course Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            course?.let { currentCourse ->
                // Course image
                item {
                    AsyncImage(
                        model = currentCourse.imageUrl,
                        contentDescription = currentCourse.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Course info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentCourse.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentCourse.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Course stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CourseStat(
                                icon = Icons.Default.Person,
                                label = "Students",
                                value = currentCourse.enrolledStudents.toString()
                            )
                            CourseStat(
                                icon = Icons.Default.Star,
                                label = "Rating",
                                value = String.format("%.1f", currentCourse.rating)
                            )
                            CourseStat(
                                icon = Icons.Outlined.PlayArrow,
                                label = "Duration",
                                value = currentCourse.duration
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress indicator for enrolled students
                        if (isEnrolled) {
                            LinearProgressIndicator(
                                progress = { courseProgress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Progress: $courseProgress%",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Enroll button or Rate button
                        if (!isEnrolled) {
                            Button(
                                onClick = {
                                    navController.navigate(Screen.Subscription.createRoute(courseId))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enroll Now")
                            }
                        } else if (courseProgress == 100) {
                            Button(
                                onClick = { showRatingDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rate this Course")
                            }
                        }
                    }
                }

                // Course sections
                items(currentCourse.sections) { section ->
                    CourseSection(
                        section = section,
                        onLessonClick = { lessonIndex ->
                            navController.navigate(
                                Screen.Lesson.createRoute(
                                    courseId = courseId,
                                    sectionId = section.id,
                                    lessonIndex = lessonIndex
                                )
                            )
                        },
                        courseViewModel = courseViewModel,
                        courseId = courseId,
                        navController = navController
                    )
                }
            }
        }
    }

    // Show rating dialog when course is completed
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onRatingSubmit = { rating ->
                course?.id?.let { courseId ->
                    courseViewModel.updateCourseRating(courseId, rating)
                }
            }
        )
    }
}

@Composable
private fun CourseStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
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
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Lesson",
            tint = MaterialTheme.colorScheme.onSurface
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
    onLessonClick: (Int) -> Unit,
    courseViewModel: CourseViewModel,
    courseId: String,
    navController: NavController
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
                            imageVector = if (expanded) Icons.Default.Info else Icons.Default.Info,
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
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Lesson"
                                )
                            },
                            modifier = Modifier.clickable { onLessonClick(index) }
                        )
                    }

                    // Display quizzes
                    section.quizzes.forEach { quiz ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = quiz.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${quiz.questions.size} questions",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                val canTakeQuiz by courseViewModel.canTakeQuiz(courseId, section.id)
                                    .collectAsState(initial = false)
                                
                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Screen.QuizTaking.createRoute(
                                                courseId = courseId,
                                                sectionId = section.id,
                                                quizId = quiz.id
                                            )
                                        )
                                    },
                                    enabled = canTakeQuiz,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (canTakeQuiz) "Take Quiz" else "Complete Lessons First")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 