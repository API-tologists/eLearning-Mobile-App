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
import androidx.compose.ui.text.font.FontWeight
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
import android.content.Intent
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import androidx.compose.ui.graphics.asImageBitmap
import android.os.Environment
import java.io.IOException

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            currentLesson?.let { lesson ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Lesson Title
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        // Lesson Duration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (lesson.duration.isNotBlank()) "Duration: ${lesson.duration}" else "No duration specified",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        // Description
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (lesson.description.isNotBlank()) lesson.description else "No description provided.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Divider()
                        Spacer(Modifier.height(12.dp))
                        // IMAGE
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Image", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
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
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = lesson.imageUrl,
                                contentDescription = "Lesson image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Divider()
                        Spacer(Modifier.height(12.dp))
                        // VIDEO
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
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
                            Spacer(Modifier.height(8.dp))
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
                                    .padding(bottom = 8.dp)
                            )
                        }
                        Divider()
                        Spacer(Modifier.height(12.dp))
                        // PDF
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("PDF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
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
                            Spacer(Modifier.height(8.dp))
                            PdfPreview(lesson.pdfUrl)
                        }
                    }
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

@Composable
fun PdfPreview(pdfUrl: String) {
    val context = LocalContext.current
    var localFile by remember { mutableStateOf<File?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Download PDF for preview
    LaunchedEffect(pdfUrl) {
        isLoading = true
        error = null
        try {
            withContext(Dispatchers.IO) {
                val tempFile = File(context.cacheDir, "temp_lesson.pdf")
                URL(pdfUrl).openStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                localFile = tempFile
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    // Render PDF page
    LaunchedEffect(localFile, currentPage) {
        if (localFile != null) {
            isLoading = true
            error = null
            try {
                withContext(Dispatchers.IO) {
                    val fileDescriptor = ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fileDescriptor)
                    pageCount = renderer.pageCount
                    val page = renderer.openPage(currentPage)
                    val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    fileDescriptor.close()
                    bitmap = bmp
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            }
            error != null -> {
                Text("Error loading PDF: $error", color = MaterialTheme.colorScheme.error)
            }
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "PDF Page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) { Text("Previous") }
                    Text("Page ${currentPage + 1} of $pageCount", modifier = Modifier.align(Alignment.CenterVertically))
                    Button(
                        onClick = { if (currentPage < pageCount - 1) currentPage++ },
                        enabled = currentPage < pageCount - 1
                    ) { Text("Next") }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isDownloading = true
                            var fileName = ""
                            try {
                                withContext(Dispatchers.IO) {
                                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    fileName = "lesson_${System.currentTimeMillis()}.pdf"
                                    val destFile = File(downloadsDir, fileName)
                                    URL(pdfUrl).openStream().use { input ->
                                        FileOutputStream(destFile).use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                }
                                Toast.makeText(context, "PDF downloaded to Downloads/$fileName", Toast.LENGTH_LONG).show()
                            } catch (e: IOException) {
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isDownloading = false
                            }
                        }
                    },
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Download PDF")
                }
            }
        }
    }
} 