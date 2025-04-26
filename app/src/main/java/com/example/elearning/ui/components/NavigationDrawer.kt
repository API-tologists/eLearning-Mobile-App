package com.example.elearning.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.model.User
import com.example.elearning.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    user: User,
    navController: NavController,
    isDrawerOpen: Boolean,
    onDrawerStateChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed)
    
    // Update drawer state when isDrawerOpen changes
    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // User Profile Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Navigation Items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Home.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "My Courses") },
                    label = { Text("My Courses") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.MyCourses.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Bookmarks.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Assignments") },
                    label = { Text("Assignments") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Assignments.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Quizzes") },
                    label = { Text("Quizzes") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Quizzes.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Schedule") },
                    label = { Text("Schedule") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Schedule.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Discussions") },
                    label = { Text("Discussions") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Discussions.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Progress.route)
                        onDrawerStateChange(false)
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Settings.route)
                        onDrawerStateChange(false)
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        onDrawerStateChange(false)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.3f),
        content = content
    )
} 