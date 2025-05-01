package com.example.elearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.AuthState
import com.example.elearning.model.User
import com.example.elearning.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
} 