package com.example.elearning.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elearning.model.AuthState
import com.example.elearning.model.Course
import com.example.elearning.navigation.Screen
import com.example.elearning.ui.components.CourseCard
import com.example.elearning.ui.components.NavigationDrawer
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel = viewModel()
) {
    var showDrawer by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    
    val courses by courseViewModel.courses.collectAsState(emptyList())
    val enrolledCourses by courseViewModel.enrolledCourses.collectAsState(emptyList())
    
    // Get unique categories
    val categories = remember(courses) { courses.map { it.category }.filter { it.isNotBlank() }.distinct() }

    val filteredCourses = remember(searchQuery, selectedCategory, courses) {
        courses.filter { course ->
            (searchQuery.isBlank() || course.title.contains(searchQuery, ignoreCase = true) ||
                course.instructor.contains(searchQuery, ignoreCase = true) ||
                course.category.contains(searchQuery, ignoreCase = true)) &&
            (selectedCategory.isBlank() || course.category == selectedCategory)
        }
    }

    // Load enrolled courses when user is authenticated
    LaunchedEffect(user?.id) {
        user?.id?.let { userId ->
            courseViewModel.loadEnrolledCourses(userId)
        }
    }

    if (user != null) {
        NavigationDrawer(
            user = user,
            navController = navController,
            authViewModel = authViewModel,
            isDrawerOpen = showDrawer,
            onDrawerStateChange = { showDrawer = it }
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
                                Text(
                                    "eLearning",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { showDrawer = !showDrawer },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = if (showDrawer) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = if (showDrawer) "Close" else "Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showSearchBar = !showSearchBar },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (showSearchBar) "Close Search" else "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showCategoryDialog = true },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Filter by Category",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.primary,
                            actionIconContentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.shadow(4.dp)
                    )
                }
            ) { padding ->
                // Category filter dialog
                if (showCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        title = { Text("Select Category") },
                        text = {
                            Column {
                                categories.forEach { category ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCategory = category
                                                showCategoryDialog = false
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedCategory == category,
                                            onClick = {
                                                selectedCategory = category
                                                showCategoryDialog = false
                                            }
                                        )
                                        Text(text = category, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                                if (selectedCategory.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = {
                                        selectedCategory = ""
                                        showCategoryDialog = false
                                    }) {
                                        Text("Clear Filter")
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showCategoryDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
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
                                text = "Available Courses",
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

                        if (enrolledCourses.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Continue Learning",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            items(enrolledCourses) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) }
                                )
                            }
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