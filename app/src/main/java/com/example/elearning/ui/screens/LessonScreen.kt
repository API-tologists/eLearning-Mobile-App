package com.example.elearning.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    courseId: String,
    sectionId: String,
    lessonIndex: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // In a real app, this would come from a ViewModel
    val lesson = remember {
        Lesson(
            id = "$courseId-$sectionId-$lessonIndex",
            title = "Introduction to Android Development",
            duration = "15:00",
            videoUrl = "https://example.com/video.mp4",
            isCompleted = false
        )
    }

    var isCompleted by remember { mutableStateOf(lesson.isCompleted) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add to bookmarks */ }) {
                        Icon(Icons.Default.AccountBox, contentDescription = "Bookmark")
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
                        Icons.Default.AccountBox,
                        contentDescription = "Play",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${lesson.duration}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCompleted) "Completed" else "Mark as Complete",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* Navigate to previous lesson */ },
                    enabled = lessonIndex > 0
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Previous")
                }

                Button(
                    onClick = { /* Navigate to next lesson */ }
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resources section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Resources",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ResourceItem(
                        icon = Icons.Default.DateRange,
                        title = "Lesson Notes",
                        onClick = { /* Download or view notes */ }
                    )
                    ResourceItem(
                        icon = Icons.Default.AccountBox,
                        title = "Exercise Files",
                        onClick = { /* Download exercise files */ }
                    )
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