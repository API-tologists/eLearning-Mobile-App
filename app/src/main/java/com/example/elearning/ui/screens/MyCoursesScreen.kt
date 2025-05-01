package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.elearning.model.AuthState
import com.example.elearning.model.Course
import com.example.elearning.navigation.Screen
import com.example.elearning.ui.components.CourseCard
import com.example.elearning.ui.components.NavigationDrawer
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel = viewModel()
) {
    var showDrawer by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val user = (authState as? AuthState.Authenticated)?.user
    val enrolledCourses by courseViewModel.enrolledCourses.collectAsState(emptyList())

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
                        title = { Text("My Courses") },
                        navigationIcon = {
                            IconButton(onClick = { showDrawer = !showDrawer }) {
                                Icon(
                                    imageVector = if (showDrawer) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = if (showDrawer) "Close" else "Menu"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                    items(enrolledCourses) { course ->
                        CourseCard(
                            course = course,
                            onClick = {
                                navController.navigate(Screen.CourseDetail.createRoute(course.id))
                            }
                        )
                    }
                }
            }
        }
    }
} 