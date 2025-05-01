package com.example.elearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.AuthState
import com.example.elearning.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Check if user is already signed in
        auth.currentUser?.let { firebaseUser ->
            _authState.value = AuthState.Authenticated(
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    profileImage = firebaseUser.photoUrl?.toString() ?: "",
                    enrolledCourses = listOf(),
                    completedLessons = listOf()
                )
            )
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading
            // Create user with email and password
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            
            // Update user profile with display name
            result.user?.let { firebaseUser ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Update auth state
                _authState.value = AuthState.Authenticated(
                    User(
                        id = firebaseUser.uid,
                        name = name,
                        email = firebaseUser.email ?: "",
                        profileImage = firebaseUser.photoUrl?.toString() ?: "",
                        enrolledCourses = listOf(),
                        completedLessons = listOf()
                    )
                )
                Result.success(Unit)
            } ?: Result.failure(Exception("User creation failed"))
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    _authState.value = AuthState.Authenticated(
                        User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            profileImage = firebaseUser.photoUrl?.toString() ?: "",
                            enrolledCourses = listOf(),
                            completedLessons = listOf()
                        )
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Unauthenticated
                // Handle error (you might want to add error handling in your AuthState)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
} 