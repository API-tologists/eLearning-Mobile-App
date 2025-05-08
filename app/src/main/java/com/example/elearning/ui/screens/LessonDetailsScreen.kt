package com.example.elearning.ui.screens

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.viewmodel.CourseViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailsScreen(
    navController: NavController,
    courseId: String,
    sectionId: String,
    lessonId: String,
    courseViewModel: CourseViewModel
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    val currentSection = course?.sections?.find { it.id == sectionId }
    val currentLesson = currentSection?.lessons?.find { it.id == lessonId }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // File pickers
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { courseViewModel.updateLessonMedia(courseId, sectionId, lessonId, "image", it) }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { courseViewModel.updateLessonMedia(courseId, sectionId, lessonId, "video", it) }
    }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { courseViewModel.updateLessonMedia(courseId, sectionId, lessonId, "pdf", it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            currentLesson?.let { lesson ->
                // Lesson Title
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Lesson Duration
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lesson.duration,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Lesson Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = lesson.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // IMAGE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Image", style = MaterialTheme.typography.titleMedium)
                    if (lesson.imageUrl.isNotEmpty()) {
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Update Image")
                        }
                        IconButton(onClick = { courseViewModel.deleteLessonMedia(courseId, sectionId, lessonId, "image") }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Image")
                        }
                    } else {
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image")
                        }
                    }
                }
                if (lesson.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = lesson.imageUrl,
                        contentDescription = "Lesson image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Fit
                    )
                }

                // VIDEO
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Video", style = MaterialTheme.typography.titleMedium)
                    if (lesson.videoUrl.isNotEmpty()) {
                        IconButton(onClick = { videoPicker.launch("video/*") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Update Video")
                        }
                        IconButton(onClick = { courseViewModel.deleteLessonMedia(courseId, sectionId, lessonId, "video") }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Video")
                        }
                    } else {
                        IconButton(onClick = { videoPicker.launch("video/*") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Video")
                        }
                    }
                }
                if (lesson.videoUrl.isNotEmpty()) {
                    val context = LocalContext.current
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(lesson.videoUrl))
                            prepare()
                        }
                    }
                    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply { player = exoPlayer }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                    )
                }

                // PDF
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("PDF", style = MaterialTheme.typography.titleMedium)
                    if (lesson.pdfUrl.isNotEmpty()) {
                        IconButton(onClick = { pdfPicker.launch("application/pdf") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Update PDF")
                        }
                        IconButton(onClick = { courseViewModel.deleteLessonMedia(courseId, sectionId, lessonId, "pdf") }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete PDF")
                        }
                    } else {
                        IconButton(onClick = { pdfPicker.launch("application/pdf") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add PDF")
                        }
                    }
                }
                if (lesson.pdfUrl.isNotEmpty()) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl("https://docs.google.com/viewer?url=${lesson.pdfUrl}&embedded=true")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Lesson") },
            text = { Text("Are you sure you want to delete this lesson?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentLesson?.let { lesson ->
                            courseViewModel.deleteLesson(courseId, sectionId, lesson.id)
                            navController.navigateUp()
                        }
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog) {
        var title by remember { mutableStateOf(currentLesson?.title ?: "") }
        var description by remember { mutableStateOf(currentLesson?.description ?: "") }
        var duration by remember { mutableStateOf(currentLesson?.duration ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Lesson") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentLesson?.let { lesson ->
                            courseViewModel.updateLesson(
                                courseId = courseId,
                                sectionId = sectionId,
                                lessonId = lesson.id,
                                title = title,
                                description = description,
                                duration = duration
                            )
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 