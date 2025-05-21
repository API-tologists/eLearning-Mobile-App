package com.example.elearning.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.Note
import com.example.elearning.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val repository = NoteRepository()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentLessonId: String? = null
    private var currentUserId: String? = null

    fun loadNotes(userId: String, lessonId: String) {
        if (currentLessonId == lessonId && currentUserId == userId) {
            return // Already loaded for this lesson
        }
        
        currentLessonId = lessonId
        currentUserId = userId
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getNotesForLesson(userId, lessonId).collect { notesList ->
                    _notes.value = notesList
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.saveNote(note).fold(
                    onSuccess = { savedNote ->
                        val currentNotes = _notes.value.toMutableList()
                        val index = currentNotes.indexOfFirst { it.id == savedNote.id }
                        if (index != -1) {
                            currentNotes[index] = savedNote
                        } else {
                            currentNotes.add(0, savedNote)
                        }
                        _notes.value = currentNotes
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteNote(noteId).fold(
                    onSuccess = {
                        _notes.value = _notes.value.filter { it.id != noteId }
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 