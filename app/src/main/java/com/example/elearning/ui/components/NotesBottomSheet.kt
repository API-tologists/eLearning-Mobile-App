package com.example.elearning.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import com.example.elearning.model.Note
import com.example.elearning.viewmodel.NoteViewModel
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesBottomSheet(
    onDismiss: () -> Unit,
    lessonId: String,
    userId: String,
    noteViewModel: NoteViewModel
) {
    var noteText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var showNameDialog by remember { mutableStateOf(false) }
    var noteName by remember { mutableStateOf("") }
    
    val notes by noteViewModel.notes.collectAsState()
    val isLoading by noteViewModel.isLoading.collectAsState()
    val error by noteViewModel.error.collectAsState()

    LaunchedEffect(lessonId, userId) {
        noteViewModel.loadNotes(userId, lessonId)
    }

    val configuration = LocalConfiguration.current
    val halfScreenHeight = (configuration.screenHeightDp * 0.5f).dp

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { 
                Text(
                    text = "Name your note",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                ) 
            },
            text = {
                OutlinedTextField(
                    value = noteName,
                    onValueChange = { noteName = it },
                    label = { Text("Note name", color = Color.White) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Color(0xFF39393B),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteName.isNotBlank()) {
                            val note = Note(
                                lessonId = lessonId,
                                userId = userId,
                                title = noteName,
                                content = noteText
                            )
                            noteViewModel.saveNote(note)
                            noteText = ""
                            noteName = ""
                            showNameDialog = false
                            selectedTab = 1
                        }
                    },
                    enabled = noteName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF2C2C2E)
        )
    }

    error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { noteViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { noteViewModel.clearError() }) {
                    Text("OK")
                }
            },
            containerColor = Color(0xFF2C2C2E)
        )
    }

    if (true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(onClick = onDismiss)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(halfScreenHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF232526), Color(0xFF414345)),
                            startY = 0f,
                            endY = 600f
                        ),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x332C2C2E)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabPill(
                            text = "Note",
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        TabPill(
                            text = "All notes",
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                    ) {
                        Box(modifier = Modifier.padding(18.dp)) {
                            when (selectedTab) {
                                0 -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        OutlinedTextField(
                                            value = noteText,
                                            onValueChange = { noteText = it },
                                            label = { Text("Write your note...", color = Color.White) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            maxLines = Int.MAX_VALUE,
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                containerColor = Color(0xFF39393B),
                                                focusedBorderColor = Color.White,
                                                unfocusedBorderColor = Color.Gray,
                                                cursorColor = Color.White,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Button(
                                            onClick = { showNameDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = noteText.isNotBlank()
                                        ) {
                                            Text("Save note")
                                        }
                                    }
                                }
                                1 -> {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = Color.White
                                        )
                                    } else if (notes.isEmpty()) {
                                        Text("No notes yet.", color = Color.Gray)
                                    } else {
                                        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                                            items(notes) { note ->
                                                Card(
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF39393B))
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = note.title,
                                                                style = MaterialTheme.typography.titleMedium.copy(
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            )
                                                            IconButton(
                                                                onClick = { noteViewModel.deleteNote(note.id) }
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Delete,
                                                                    contentDescription = "Delete note",
                                                                    tint = Color.Red
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = note.content,
                                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
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
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color.White else Color.Transparent
    val fg = if (selected) Color(0xFF232526) else Color.White
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text(text, color = fg, fontWeight = FontWeight.SemiBold)
    }
} 