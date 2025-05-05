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
    var isLoading by remember { mutableStateOf(true) }

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
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            section.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        IconButton(onClick = {
                                            val idx = c.sections.indexOf(section)
                                            showAddLessonDialog = true to idx
                                        }) {
                                            Icon(Icons.Filled.Add, contentDescription = "Add Lesson")
                                        }
                                    }
                                    if (section.lessons.isEmpty()) {
                                        Text(
                                            "No lessons yet.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        section.lessons.forEach { lesson ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 4.dp)
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
} 