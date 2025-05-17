package com.example.elearning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.model.Course
import com.example.elearning.model.CourseSection
import com.example.elearning.model.Lesson
import com.example.elearning.viewmodel.CourseViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.elearning.navigation.Screen
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.MoreVert
import com.example.elearning.model.Quiz
import com.example.elearning.model.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditorScreen(
    navController: NavController,
    courseId: String,
    courseViewModel: CourseViewModel
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showAddLessonDialog by remember { mutableStateOf<Pair<Boolean, Int>>(false to -1) }
    var showAddQuizDialog by remember { mutableStateOf<Pair<Boolean, Int>>(false to -1) }
    var isLoading by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf(course?.category ?: "") }

    // Load course details
    LaunchedEffect(courseId) {
        isLoading = true
        courseViewModel.selectCourse(courseId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Course", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
                Text("Loading course data...")
            } else if (course == null) {
                Text(
                    "Course not found or failed to load. Course ID: $courseId",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { courseViewModel.selectCourse(courseId) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Reload Course")
                }
            } else {
                course?.let { c ->
                    Text(
                        text = c.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = c.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Sections",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { showAddSectionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Section")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Section")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (c.sections.isEmpty()) {
                        Text(
                            "No sections yet. Click 'Add Section' to get started.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(c.sections) { section ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
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

                                    // Lessons section
                                    Text(
                                        "Lessons",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Button(
                                        onClick = { showAddLessonDialog = true to c.sections.indexOf(section) },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Lesson")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Lesson")
                                    }

                                    // Display lessons
                                    section.lessons.forEach { lesson ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    navController.navigate(
                                                        Screen.LessonDetails.createRoute(
                                                            courseId = courseId,
                                                            sectionId = section.id,
                                                            lessonId = lesson.id
                                                        )
                                                    )
                                                }
                                        ) {
                                            Icon(
                                                Icons.Filled.PlayArrow,
                                                contentDescription = "Lesson",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    lesson.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (lesson.duration.isNotBlank()) {
                                                    Text(
                                                        lesson.duration,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Quizzes section
                                    Text(
                                        "Quizzes",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Button(
                                        onClick = { showAddQuizDialog = true to c.sections.indexOf(section) },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Quiz")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Quiz")
                                    }

                                    // Display quizzes
                                    section.quizzes.forEach { quiz ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = quiz.title,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = "${quiz.questions.size} questions",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        navController.navigate(
                                                            Screen.QuizEditor.createRoute(
                                                                courseId = courseId,
                                                                sectionId = section.id,
                                                                quizId = quiz.id
                                                            )
                                                        )
                                                    }
                                                ) {
                                                    Icon(Icons.Default.MoreVert, contentDescription = "Edit Quiz")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Section Dialog
    if (showAddSectionDialog && course != null) {
        var sectionTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("Add Section") },
            text = {
                OutlinedTextField(
                    value = sectionTitle,
                    onValueChange = { sectionTitle = it },
                    label = { Text("Section Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (sectionTitle.isNotBlank()) {
                        course?.let { currentCourse ->
                            courseViewModel.addSectionToCourse(currentCourse.id, sectionTitle)
                        }
                        showAddSectionDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Lesson Dialog
    if (showAddLessonDialog.first && course != null && showAddLessonDialog.second >= 0) {
        var lessonTitle by remember { mutableStateOf("") }
        var lessonDescription by remember { mutableStateOf("") }
        var lessonDuration by remember { mutableStateOf("") }

        // File picker state
        val videoUri = remember { mutableStateOf<Uri?>(null) }
        val pdfUri = remember { mutableStateOf<Uri?>(null) }
        val imageUri = remember { mutableStateOf<Uri?>(null) }

        // File pickers
        val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            videoUri.value = uri
        }
        val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            pdfUri.value = uri
        }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri.value = uri
        }

        AlertDialog(
            onDismissRequest = { showAddLessonDialog = false to -1 },
            title = { Text("Add Lesson") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lessonTitle,
                        onValueChange = { lessonTitle = it },
                        label = { Text("Lesson Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lessonDescription,
                        onValueChange = { lessonDescription = it },
                        label = { Text("Lesson Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lessonDuration,
                        onValueChange = { lessonDuration = it },
                        label = { Text("Duration") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { videoPicker.launch("video/*") }) {
                        Text(if (videoUri.value != null) "Video Selected" else "Select Video")
                    }
                    Button(onClick = { pdfPicker.launch("application/pdf") }) {
                        Text(if (pdfUri.value != null) "PDF Selected" else "Select PDF")
                    }
                    Button(onClick = { imagePicker.launch("image/*") }) {
                        Text(if (imageUri.value != null) "Image Selected" else "Select Image")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (lessonTitle.isNotBlank()) {
                        course?.let { currentCourse ->
                            val sectionId = currentCourse.sections[showAddLessonDialog.second].id
                            courseViewModel.addLessonWithFiles(
                                courseId = currentCourse.id,
                                sectionId = sectionId,
                                lessonTitle = lessonTitle,
                                lessonDescription = lessonDescription,
                                lessonDuration = lessonDuration,
                                videoUri = videoUri.value,
                                pdfUri = pdfUri.value,
                                imageUri = imageUri.value
                            )
                        }
                        showAddLessonDialog = false to -1
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddLessonDialog = false to -1 }) { Text("Cancel") }
            }
        )
    }

    // Add Quiz Dialog
    if (showAddQuizDialog.first && course != null && showAddQuizDialog.second >= 0) {
        var quizTitle by remember { mutableStateOf("") }
        var quizDescription by remember { mutableStateOf("") }
        var passingScore by remember { mutableStateOf("70") }
        var timeLimit by remember { mutableStateOf("0") }
        var attemptsAllowed by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showAddQuizDialog = false to -1 },
            title = { Text("Add Quiz") },
            text = {
                Column {
                    OutlinedTextField(
                        value = quizTitle,
                        onValueChange = { quizTitle = it },
                        label = { Text("Quiz Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quizDescription,
                        onValueChange = { quizDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passingScore,
                        onValueChange = { passingScore = it },
                        label = { Text("Passing Score (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeLimit,
                        onValueChange = { timeLimit = it },
                        label = { Text("Time Limit (minutes, 0 for no limit)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = attemptsAllowed,
                        onValueChange = { attemptsAllowed = it },
                        label = { Text("Attempts Allowed") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (quizTitle.isNotBlank()) {
                            course?.let { currentCourse ->
                                val sectionIndex = showAddQuizDialog.second
                                if (sectionIndex >= 0 && sectionIndex < currentCourse.sections.size) {
                                    val section = currentCourse.sections[sectionIndex]
                                    courseViewModel.addQuizToSection(
                                        courseId = currentCourse.id,
                                        sectionId = section.id,
                                        quizTitle = quizTitle,
                                        quizDescription = quizDescription,
                                        passingScore = passingScore.toIntOrNull() ?: 70,
                                        timeLimit = timeLimit.toIntOrNull() ?: 0,
                                        attemptsAllowed = attemptsAllowed.toIntOrNull() ?: 1
                                    )
                                }
                            }
                            showAddQuizDialog = false to -1
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuizDialog = false to -1 }) {
                    Text("Cancel")
                }
            }
        )
    }
} 