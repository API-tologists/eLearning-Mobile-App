package com.example.elearning.repository

import com.example.elearning.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NoteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")

    suspend fun saveNote(note: Note): Result<Note> = try {
        val noteRef = if (note.id.isEmpty()) {
            notesCollection.document()
        } else {
            notesCollection.document(note.id)
        }
        
        val noteWithId = note.copy(id = noteRef.id)
        noteRef.set(noteWithId).await()
        Result.success(noteWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getNotesForLesson(userId: String, lessonId: String): Flow<List<Note>> = flow {
        try {
            val snapshot = notesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("lessonId", lessonId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val notes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Note::class.java)
            }
            emit(notes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = try {
        notesCollection.document(noteId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 