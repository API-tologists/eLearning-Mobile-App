package com.example.elearning.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elearning.model.User
import com.example.elearning.navigation.Screen
import com.example.elearning.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    user: User,
    navController: NavController,
    authViewModel: AuthViewModel,
    isDrawerOpen: Boolean,
    onDrawerStateChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = (screenWidth * 0.65f) // Navbar Takes 50% of screen width
    
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
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .background(MaterialTheme.colorScheme.surface)
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
                            .shadow(4.dp, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Navigation Items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Home.route)
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Star, contentDescription = "My Courses") },
                    label = { Text("My Courses") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.MyCourses.route)
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Certificate") },
                    label = { Text("Certificate") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Progress.route)
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Downloads") },
                    label = { Text("Downloads") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Downloads.route)
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screen.Settings.route)
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        onDrawerStateChange(false)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        gesturesEnabled = false,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        content = content
    )
}