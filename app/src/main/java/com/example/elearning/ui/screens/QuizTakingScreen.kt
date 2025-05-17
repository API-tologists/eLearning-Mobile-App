package com.example.elearning.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var failedQuestions by remember { mutableStateOf(listOf<Question>()) }
    val externalResources by courseViewModel.externalResources.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (hasPassed) "Congratulations! You passed!" else "Try again!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Your score: $score%",
                    style = MaterialTheme.typography.titleLarge
                )

                // Only show recommended resources if the user failed
                if (!hasPassed && externalResources.isNotEmpty()) {
                    Text(
                        text = "Recommended Resources:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    RecommendedResourcesSection(resources = externalResources)
                }

                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth()
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
                            
                            // Get failed questions
                            failedQuestions = currentQuiz?.questions?.filter { question ->
                                answers[question.id] != question.correctAnswer
                            } ?: emptyList()
                            
                            // Get external resources if failed
                            if (!hasPassed) {
                                currentQuiz?.let { quiz ->
                                    courseViewModel.getExternalResources(
                                        quizTitle = quiz.title,
                                        quizDescription = quiz.description,
                                        failedQuestions = failedQuestions
                                    )
                                }
                            }
                            
                            showResults = true
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
fun RecommendedResourcesSection(resources: List<String>) {
    val context = LocalContext.current
    // Fixed regex: removed [ and ] from inside the character class
    val urlRegex = "(https?://[\\w\\-._~:/?@!$&'()*+,;=%]+)".toRegex()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        resources.forEach { resource ->
            val urls = urlRegex.findAll(resource).map { it.value }.toList()
            if (urls.isNotEmpty()) {
                // Show the text before the first URL, if any
                val firstUrlIndex = resource.indexOf(urls.first())
                if (firstUrlIndex > 0) {
                    Text(text = resource.substring(0, firstUrlIndex))
                }
                urls.forEach { url ->
                    Text(
                        text = url,
                        color = Color.Blue,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
                // Show the text after the last URL, if any
                val lastUrl = urls.last()
                val lastUrlEnd = resource.indexOf(lastUrl) + lastUrl.length
                if (lastUrlEnd < resource.length) {
                    Text(text = resource.substring(lastUrlEnd))
                }
            } else {
                Text(text = resource)
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