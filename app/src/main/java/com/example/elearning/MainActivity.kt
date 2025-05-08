package com.example.elearning

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.elearning.navigation.NavGraph
import com.example.elearning.ui.theme.ELearningTheme
import com.example.elearning.viewmodel.AuthViewModel
import com.example.elearning.viewmodel.CourseViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ELearningTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val application = LocalContext.current.applicationContext as Application
                    val navController = rememberNavController()
                    val authViewModel = remember { AuthViewModel() }
                    val courseViewModel = remember { CourseViewModel(application) }

                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        courseViewModel = courseViewModel
                    )
                }
            }
        }
    }
}