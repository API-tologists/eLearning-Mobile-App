package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.elearning.model.Question
import com.example.elearning.model.QuestionType
import com.example.elearning.viewmodel.CourseViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(
    navController: NavController,
    courseId: String,
    sectionId: String,
    quizId: String,
    courseViewModel: CourseViewModel
) {
    val course by courseViewModel.selectedCourse.collectAsState()
    val currentQuiz = course?.sections?.find { it.id == sectionId }?.quizzes?.find { it.id == quizId }
    var answers by remember { mutableStateOf(mapOf<String, String>()) }
    var timeRemaining by remember { mutableStateOf(currentQuiz?.timeLimit ?: 0) }
    var showResults by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var hasPassed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Timer for quiz
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            kotlinx.coroutines.delay(60000) // 1 minute
            timeRemaining--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentQuiz?.title ?: "Quiz") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (showResults) {
            // Show results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (hasPassed) "Congratulations! You passed!" else "Try again!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your score: $score%",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigateUp() }
                ) {
                    Text("Back to Course")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    // Quiz info and timer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentQuiz?.description ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (currentQuiz?.timeLimit ?: 0 > 0) {
                                Text(
                                    text = "Time remaining: ${timeRemaining} minutes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = "Passing score: ${currentQuiz?.passingScore ?: 70}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Questions
                items(currentQuiz?.questions ?: emptyList()) { question ->
                    QuestionCard(
                        question = question,
                        answer = answers[question.id] ?: "",
                        onAnswerChange = { answer ->
                            answers = answers + (question.id to answer)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Calculate score
                            val totalPoints = currentQuiz?.questions?.sumOf { it.points } ?: 0
                            val earnedPoints = currentQuiz?.questions?.sumOf { question ->
                                if (answers[question.id] == question.correctAnswer) question.points else 0
                            } ?: 0
                            score = if (totalPoints > 0) (earnedPoints * 100) / totalPoints else 0
                            hasPassed = score >= (currentQuiz?.passingScore ?: 70)
                            showResults = true

                            // Save quiz attempt
                            scope.launch {
                                courseViewModel.saveQuizAttempt(
                                    courseId = courseId,
                                    sectionId = sectionId,
                                    quizId = quizId,
                                    score = score,
                                    hasPassed = hasPassed
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = answers.size == currentQuiz?.questions?.size
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Submit")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Quiz")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: Question,
    answer: String,
    onAnswerChange: (String) -> Unit
) {
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
                text = question.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (question.type) {
                QuestionType.MULTIPLE_CHOICE -> {
                    question.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == option,
                                onClick = { onAnswerChange(option) }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == "True",
                                onClick = { onAnswerChange("True") }
                            )
                            Text("True")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == "False",
                                onClick = { onAnswerChange("False") }
                            )
                            Text("False")
                        }
                    }
                }
                QuestionType.SHORT_ANSWER -> {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = onAnswerChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your answer") }
                    )
                }
            }
        }
    }
} 