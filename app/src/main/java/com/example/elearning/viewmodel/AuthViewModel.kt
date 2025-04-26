package com.example.elearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.AuthState
import com.example.elearning.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Simulate loading user data
        viewModelScope.launch {
            delay(1000) // Simulate network delay
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(1000) // Simulate network delay
            
            // Dummy authentication logic
            if (email.isNotBlank() && password.isNotBlank()) {
                _authState.value = AuthState.Authenticated(
                    User(
                        id = "1",
                        name = "John Doe",
                        email = email,
                        profileImage = "https://example.com/profile.jpg",
                        enrolledCourses = listOf("1", "2"),
                        completedLessons = listOf("1", "2")
                    )
                )
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }
} 