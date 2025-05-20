package com.example.elearning.ui.screens

import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.model.AuthState
import com.example.elearning.model.Lesson
import com.example.elearning.model.User
import com.example.elearning.navigation.Screen
import com.example.elearning.viewmodel.CourseViewModel
import com.example.elearning.viewmodel.AuthViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.elearning.ui.components.NotesBottomSheet
import com.example.elearning.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    courseId: String,
    sectionId: String,
    lessonIndex: Int,
    navController: NavController,
    courseViewModel: CourseViewModel,
    authViewModel: AuthViewModel,
    noteViewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    val currentSection = course?.sections?.find { it.id == sectionId }
    val currentLesson = currentSection?.lessons?.getOrNull(lessonIndex)
    val hasNextLesson = currentSection?.lessons?.size?.let { it > lessonIndex + 1 } ?: false
    val hasNextSection = course?.sections?.indexOf(currentSection)?.let { it < (course?.sections?.size ?: 0) - 1 } ?: false
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    var isCompleted by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Check if lesson is completed
    LaunchedEffect(currentLesson?.id, user?.id) {
        if (currentLesson?.id != null && user?.id != null) {
            courseViewModel.getLessonCompletionStatus(user.id, courseId, currentLesson.id).collect { completed ->
                isCompleted = completed
            }
        }
    }

    LaunchedEffect(Unit) {
        courseViewModel.loadCourseById(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentLesson?.title ?: "Lesson") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Box(modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video player
                if (!currentLesson?.videoUrl.isNullOrEmpty()) {
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            try {
                                val mediaItem = MediaItem.fromUri(currentLesson?.videoUrl ?: "")
                                setMediaItem(mediaItem)
                                prepare()
                            } catch (e: Exception) {
                                Log.e("LessonScreen", "Error preparing video: ${e.message}")
                            }
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            try {
                                exoPlayer.release()
                            } catch (e: Exception) {
                                Log.e("LessonScreen", "Error releasing player: ${e.message}")
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        AndroidView(
                            factory = { context ->
                                PlayerView(context).apply {
                                    player = exoPlayer
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    useController = true
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PDF viewer
                if (!currentLesson?.pdfUrl.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    webViewClient = WebViewClient()
                                    settings.javaScriptEnabled = true
                                    loadUrl("https://docs.google.com/viewer?url=${currentLesson?.pdfUrl}&embedded=true")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image viewer
                if (!currentLesson?.imageUrl.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = currentLesson?.imageUrl,
                            contentDescription = "Lesson image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Fit
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

                // Duration and completion status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duration: ${currentLesson?.duration ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Mark as completed button
                    Button(
                        onClick = {
                            currentLesson?.id?.let { lessonId ->
                                user?.id?.let { userId ->
                                    courseViewModel.updateCourseProgress(
                                        userId = userId,
                                        courseId = courseId,
                                        lessonId = lessonId
                                    )
                                    isCompleted = true
                                }
                            }
                        },
                        enabled = !isCompleted
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                            contentDescription = if (isCompleted) "Completed" else "Mark as Completed"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isCompleted) "Completed" else "Mark as Completed")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }

                    Button(
                        onClick = { 
                            // Navigate to next lesson
                            if (hasNextLesson) {
                                navController.navigate(
                                    Screen.Lesson.createRoute(
                                        courseId = courseId,
                                        sectionId = sectionId,
                                        lessonIndex = lessonIndex + 1
                                    )
                                )
                            } else if (hasNextSection) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
            // Bouton flottant positionné manuellement juste au-dessus du bouton Next
            var showNotesSheet by remember { mutableStateOf(false) }
            FloatingActionButton(
                onClick = { showNotesSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp) // Ajuste bottom pour placer au-dessus de Next
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Take Notes")
            }
            if (showNotesSheet) {
                NotesBottomSheet(
                    onDismiss = { showNotesSheet = false },
                    lessonId = currentLesson?.id ?: "",
                    userId = user?.id ?: "",
                    noteViewModel = noteViewModel
                )
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