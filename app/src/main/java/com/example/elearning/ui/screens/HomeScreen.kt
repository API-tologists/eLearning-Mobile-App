package com.example.elearning.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.data.CourseRepository
import com.example.elearning.model.AuthState
import com.example.elearning.navigation.Screen
import com.example.elearning.ui.components.CourseCard
import com.example.elearning.ui.components.NavigationDrawer
import com.example.elearning.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var showDrawer by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    val allCourses = remember { CourseRepository.courses }
    val filteredCourses = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allCourses
        } else {
            allCourses.filter { course ->
                course.title.contains(searchQuery, ignoreCase = true) ||
                course.instructor.contains(searchQuery, ignoreCase = true) ||
                course.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (user != null) {
        NavigationDrawer(
            user = user,
            navController = navController,
            onCloseDrawer = { showDrawer = false }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { 
                            if (showSearchBar) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search courses...") },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            } else {
                                Text("eLearning")
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { showDrawer = true }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSearchBar = !showSearchBar }) {
                                Icon(
                                    if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (showSearchBar) "Close Search" else "Search"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchQuery.isNotBlank()) {
                        item {
                            Text(
                                text = "Search Results",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(filteredCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "Welcome, ${user.name}!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Continue your learning journey",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        item {
                            Text(
                                text = "Featured Courses",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(CourseRepository.getFeaturedCourses()) { course ->
                                    CourseCard(
                                        course = course,
                                        onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            QuickActionsGrid(navController)
                        }

                        item {
                            Text(
                                text = "Continue Learning",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(CourseRepository.getInProgressCourses()) { course ->
                            CourseProgressCard(
                                course = course,
                                onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                text = "Assignments",
                onClick = { navController.navigate(Screen.Assignments.route) }
            )
            QuickActionButton(
                text = "Quizzes",
                onClick = { navController.navigate(Screen.Quizzes.route) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(
                text = "Schedule",
                onClick = { navController.navigate(Screen.Schedule.route) }
            )
            QuickActionButton(
                text = "Progress",
                onClick = { navController.navigate(Screen.Progress.route) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(156.dp)
            .height(80.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun CourseProgressCard(
    course: com.example.elearning.model.Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Instructor: ${course.instructor}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = course.progress,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${(course.progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
} 