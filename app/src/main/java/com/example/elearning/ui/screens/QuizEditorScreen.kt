package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.model.Quiz
import com.example.elearning.model.Question
import com.example.elearning.model.QuestionType
import com.example.elearning.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizEditorScreen(
    navController: NavController,
    courseId: String,
    sectionId: String,
    quizId: String,
    courseViewModel: CourseViewModel
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var editableDescription by remember { mutableStateOf("") }
    var showDeleteQuizDialog by remember { mutableStateOf(false) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }
    
    val currentQuiz = course?.sections?.find { it.id == sectionId }?.quizzes?.find { it.id == quizId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentQuiz?.title ?: "Quiz Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = editableDescription,
                    onValueChange = { editableDescription = it },
                    label = { Text("Quiz Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        courseViewModel.updateQuizDescription(courseId, sectionId, quizId, editableDescription)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save") }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAddQuestionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Question")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Question")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(currentQuiz?.questions ?: emptyList()) { question ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = question.text,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { questionToDelete = question }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Type: ${question.type.name}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (question.type == QuestionType.MULTIPLE_CHOICE) {
                            question.options.forEach { option ->
                                Text(
                                    text = "• $option",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Correct Answer: ${question.correctAnswer}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Points: ${question.points}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showDeleteQuizDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Quiz")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Quiz")
                }
            }
        }
        // Delete Question Dialog
        if (questionToDelete != null) {
            AlertDialog(
                onDismissRequest = { questionToDelete = null },
                title = { Text("Delete Question") },
                text = { Text("Are you sure you want to delete this question?") },
                confirmButton = {
                    TextButton(onClick = {
                        courseViewModel.deleteQuestionFromQuiz(courseId, sectionId, quizId, questionToDelete!!.id)
                        questionToDelete = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { questionToDelete = null }) { Text("Cancel") }
                }
            )
        }
        // Delete Quiz Dialog
        if (showDeleteQuizDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteQuizDialog = false },
                title = { Text("Delete Quiz") },
                text = { Text("Are you sure you want to delete this quiz? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        courseViewModel.deleteQuiz(courseId, sectionId, quizId)
                        showDeleteQuizDialog = false
                        navController.popBackStack()
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteQuizDialog = false }) { Text("Cancel") }
                }
            )
        }
    }

    if (showAddQuestionDialog) {
        var questionText by remember { mutableStateOf("") }
        var selectedQuestionType by remember { mutableStateOf(QuestionType.MULTIPLE_CHOICE) }
        var options by remember { mutableStateOf("") }
        var correctAnswer by remember { mutableStateOf("") }
        var points by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            title = { Text("Add Question") },
            text = {
                Column {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Question Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = selectedQuestionType.name,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Question Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            QuestionType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = { selectedQuestionType = type }
                                )
                            }
                        }
                    }
                    
                    if (selectedQuestionType == QuestionType.MULTIPLE_CHOICE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = options,
                            onValueChange = { options = it },
                            label = { Text("Options (one per line)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correctAnswer,
                        onValueChange = { correctAnswer = it },
                        label = { Text("Correct Answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it },
                        label = { Text("Points") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (questionText.isNotBlank() && correctAnswer.isNotBlank()) {
                            val optionsList = if (selectedQuestionType == QuestionType.MULTIPLE_CHOICE) {
                                options.split("\n").filter { it.isNotBlank() }
                            } else {
                                emptyList()
                            }
                            
                            courseViewModel.addQuestionToQuiz(
                                courseId = courseId,
                                sectionId = sectionId,
                                quizId = quizId,
                                questionText = questionText,
                                questionType = selectedQuestionType,
                                options = optionsList,
                                correctAnswer = correctAnswer,
                                points = points.toIntOrNull() ?: 1
                            )
                            showAddQuestionDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 