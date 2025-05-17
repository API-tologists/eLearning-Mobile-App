package com.example.elearning.repository

import com.example.elearning.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
// import com.google.firebase.database.* // Removed
// import kotlinx.coroutines.channels.awaitClose
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.callbackFlow

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // TODO: Implement Firestore-based user management if needed
} 