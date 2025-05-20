package com.example.elearning.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.AuthState
import com.example.elearning.model.User
import com.example.elearning.model.UserRole
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    init {
        // Check if user is already logged in
        auth.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val userDoc = db.collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()
                    
                    if (userDoc.exists()) {
                        val user = userDoc.toObject(User::class.java)
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated(user!!)
                    } else {
                        signOut()
                    }
                } catch (e: Exception) {
                    signOut()
                }
            }
        }
    }
    
    suspend fun signUp(name: String, email: String, password: String, role: UserRole) {
        try {
            _authState.value = AuthState.Loading
            
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            // Create user document in Firestore
            val user = User(
                id = authResult.user?.uid ?: throw Exception("User creation failed"),
                name = name,
                email = email,
                role = role
            )
            
            db.collection("users")
                .document(user.id)
                .set(user)
                .await()
            
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            throw e
        }
    }
    
    suspend fun signIn(email: String, password: String) {
        try {
            _authState.value = AuthState.Loading
            
            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            
            // Get user data from Firestore
            val userDoc = db.collection("users")
                .document(authResult.user?.uid ?: throw Exception("Sign in failed"))
                .get()
                .await()
            
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user!!)
            } else {
                throw Exception("User data not found")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            throw e
        }
    }
    
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Reauthenticate user
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
                    currentUser.reauthenticate(credential).await()
                    
                    // Delete user data from Firestore
                    db.collection("users").document(currentUser.uid).delete().await()
                    
                    // Delete user from Firebase Auth
                    currentUser.delete().await()
                    
                    // Sign out and navigate to login
                    signOut()
                }
            } catch (e: Exception) {
                throw Exception("Error deleting account", e)

            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Update email in Firebase Auth if it has changed
                    if (email != currentUser.email) {
                        currentUser.updateEmail(email).await()
                    }
                    
                    // Update user document in Firestore
                    val userRef = db.collection("users").document(currentUser.uid)
                    userRef.update(
                        mapOf(
                            "name" to name,
                            "email" to email
                        )
                    ).await()
                    
                    // Update local state
                    _currentUser.value = _currentUser.value?.copy(
                        name = name,
                        email = email
                    )
                    
                    // Update auth state
                    _authState.value = AuthState.Authenticated(_currentUser.value!!)
                }
            } catch (e: Exception) {
                throw Exception("Error updating profile", e)

            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Reauthenticate user with current password
                    val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                    currentUser.reauthenticate(credential).await()
                    
                    // Update password
                    currentUser.updatePassword(newPassword).await()
                }
            } catch (e: Exception) {
                throw Exception("Error changing password", e)
            }
        }
    }
} 