package com.example.elearning.repository

import com.example.elearning.model.User
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val courseRepository = CourseRepository()

    fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>()
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        usersRef.child(userId).addValueEventListener(listener)
        awaitClose { usersRef.child(userId).removeEventListener(listener) }
    }

    suspend fun createUser(user: User) {
        usersRef.child(user.id).setValue(user).await()
    }

    suspend fun updateUser(user: User) {
        usersRef.child(user.id).setValue(user).await()
    }

    suspend fun addUser(user: User) {
        if (usersRef.child(user.id).get().await() == null) {
            usersRef.child(user.id).setValue(user).await()
        }
    }

    suspend fun enrollUserInCourse(userId: String, courseId: String) {
        val userRef = usersRef.child(userId)
        userRef.child("enrolledCourses").child(courseId).setValue(true).await()
        courseRepository.enrollInCourse(userId, courseId)
    }

    fun getCourseProgress(userId: String, courseId: String): Flow<Int> {
        return courseRepository.getCourseProgress(userId, courseId)
    }
} 