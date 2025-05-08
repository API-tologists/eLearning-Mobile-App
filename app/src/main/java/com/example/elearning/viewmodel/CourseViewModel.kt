package com.example.elearning.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elearning.model.Course
import com.example.elearning.model.CourseEnrollment
import com.example.elearning.model.CourseSection
import com.example.elearning.model.User
import com.example.elearning.model.Lesson
import com.example.elearning.repository.CourseRepository
import com.example.elearning.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CourseViewModel"
    private val repository = CourseRepository()
    private val userRepository = UserRepository()
    private val db = FirebaseFirestore.getInstance()
    private val coursesCollection = db.collection("courses")
    private val enrollmentsCollection = db.collection("enrollments")
    private val context = application.applicationContext
    
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses
    
    private val _enrolledCourses = MutableStateFlow<List<Course>>(emptyList())
    val enrolledCourses: StateFlow<List<Course>> = _enrolledCourses.asStateFlow()
    
    private val _instructorCourses = MutableStateFlow<List<Course>>(emptyList())
    val instructorCourses: StateFlow<List<Course>> = _instructorCourses
    
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()
    
    private val _courseProgress = MutableStateFlow<Int>(0)
    val courseProgress: StateFlow<Int> = _courseProgress.asStateFlow()
    
    private val _enrolledStudents = MutableStateFlow<List<User>>(emptyList())
    val enrolledStudents: StateFlow<List<User>> = _enrolledStudents.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getAllCourses().collectLatest { courses ->
                Log.d(TAG, "Loaded ${courses.size} courses")
                courses.forEach { course ->
                    Log.d(TAG, "Course: ${course.title}")
                }
                _courses.value = courses
            }
        }
    }
    
    fun loadEnrolledCourses(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading enrolled courses for user: $userId")
            repository.getEnrolledCourses(userId).collectLatest { courses ->
                Log.d(TAG, "Loaded ${courses.size} enrolled courses for user $userId")
                courses.forEach { course ->
                    Log.d(TAG, "Enrolled course: ${course.title} (ID: ${course.id})")
                }
                _enrolledCourses.value = courses
            }
        }
    }
    
    fun loadInstructorCourses(instructorId: String) {
        viewModelScope.launch {
            try {
                val snapshot = coursesCollection.whereEqualTo("instructor", instructorId).get().await()
                _instructorCourses.value = snapshot.toObjects(Course::class.java)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun loadCourseById(courseId: String) {
        viewModelScope.launch {
            repository.getCourseById(courseId).collectLatest { course ->
                Log.d(TAG, "Loaded course: ${course?.title}")
                _selectedCourse.value = course
            }
        }
    }
    
    fun enrollInCourse(studentId: String, courseId: String) {
        viewModelScope.launch {
            try {
                repository.enrollInCourse(studentId, courseId)
                // Immediately reload enrolled courses for the student so the UI updates
                loadEnrolledCourses(studentId)
            } catch (e: Exception) {
                Log.e(TAG, "Error enrolling in course", e)
            }
        }
    }
    
    fun updateCourseProgress(userId: String, courseId: String, lessonId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting progress update for user $userId in course $courseId for lesson $lessonId")
                
                // Get the enrollment document
                val enrollmentQuery = enrollmentsCollection
                    .whereEqualTo("studentId", userId)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .await()
                
                if (enrollmentQuery.isEmpty) {
                    Log.e(TAG, "No enrollment found for user $userId in course $courseId")
                    return@launch
                }
                
                val enrollmentDoc = enrollmentQuery.documents[0]
                val enrollment = enrollmentDoc.toObject(CourseEnrollment::class.java)
                
                if (enrollment == null) {
                    Log.e(TAG, "Failed to parse enrollment document")
                    return@launch
                }
                
                Log.d(TAG, "Current enrollment state: completedLessons=${enrollment.completedLessons}, progress=${enrollment.progress}")
                
                // Get the course to calculate total lessons
                val courseRef = coursesCollection.document(courseId)
                val course = courseRef.get().await().toObject(Course::class.java)
                val totalLessons = course?.sections?.sumOf { it.lessons.size } ?: 0
                
                if (totalLessons > 0 && course != null) {
                    // Add the new lesson to completed lessons if not already completed
                    val updatedCompletedLessons = if (!enrollment.completedLessons.contains(lessonId)) {
                        enrollment.completedLessons + lessonId
                    } else {
                        enrollment.completedLessons
                    }
                    
                    // Calculate progress based on completed lessons vs total lessons
                    val progress = (updatedCompletedLessons.size * 100) / totalLessons
                    
                    val updatedEnrollment = enrollment.copy(
                        completedLessons = updatedCompletedLessons,
                        progress = progress
                    )
                    
                    // Update the enrollment document
                    enrollmentDoc.reference.set(updatedEnrollment).await()
                    
                    // Update the course's lesson completion status
                    val updatedSections = course.sections.map { section ->
                        section.copy(
                            lessons = section.lessons.map { lesson ->
                                if (lesson.id == lessonId) {
                                    lesson.copy(completed = true)
                                } else lesson
                            }
                        )
                    }
                    val updatedCourse = course.copy(sections = updatedSections)
                    
                    // Update the course document
                    courseRef.set(updatedCourse).await()
                    
                    // Update the local state
                    _selectedCourse.value = updatedCourse
                    
                    Log.d(TAG, "Progress updated successfully: ${updatedCompletedLessons.size}/$totalLessons lessons completed (${progress}%)")
                    Log.d(TAG, "Course document updated with lesson completion status")
                } else {
                    Log.e(TAG, "Invalid course or total lessons count")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating course progress", e)
            }
        }
    }
    
    fun loadCourseProgress(userId: String, courseId: String) {
        viewModelScope.launch {
            repository.getCourseProgress(userId, courseId).collectLatest { progress ->
                Log.d(TAG, "Loaded progress for user $userId in course $courseId: $progress%")
                _courseProgress.value = progress
            }
        }
    }
    
    fun createCourse(
        title: String,
        description: String,
        instructor: String,
        imageUrl: String,
        sections: List<CourseSection>,
        instructorId: String
    ) {
        viewModelScope.launch {
            try {
                // Always generate a unique course ID!
                val courseId = java.util.UUID.randomUUID().toString()
                val course = Course(
                    id = courseId,
                    title = title,
                    description = description,
                    instructor = instructorId,
                    imageUrl = imageUrl,
                    sections = sections
                )
                repository.createCourse(course)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating course", e)
            }
        }
    }
    
    fun loadEnrolledStudents(courseId: String) {
        viewModelScope.launch {
            repository.getEnrolledStudents(courseId).collectLatest { students ->
                _enrolledStudents.value = students
            }
        }
    }
    
    fun addSectionToCourse(courseId: String, sectionTitle: String) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val newSection = CourseSection(
                    id = UUID.randomUUID().toString(),
                    title = sectionTitle,
                    lessons = emptyList()
                )
                val updatedSections = course.sections + newSection
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Section added successfully: $sectionTitle")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding section", e)
            }
        }
    }
    
    fun addLessonToSection(
        courseId: String,
        sectionId: String,
        lessonTitle: String,
        lessonDescription: String,
        lessonVideoUrl: String,
        lessonDuration: String,
        lessonPdfUrl: String,
        lessonImageUrl: String
    ) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        val newLesson = Lesson(
                            id = UUID.randomUUID().toString(),
                            title = lessonTitle,
                            description = lessonDescription,
                            videoUrl = lessonVideoUrl,
                            duration = lessonDuration,
                            pdfUrl = lessonPdfUrl,
                            imageUrl = lessonImageUrl
                        )
                        section.copy(lessons = section.lessons + newLesson)
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Lesson added successfully: $lessonTitle")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding lesson", e)
            }
        }
    }
    
    fun loadCourses() {
        viewModelScope.launch {
            try {
                val snapshot = coursesCollection.get().await()
                _courses.value = snapshot.toObjects(Course::class.java)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun selectCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val document = coursesCollection.document(courseId).get().await()
                val course = document.toObject(Course::class.java)
                _selectedCourse.value = course
                Log.d(TAG, "Course loaded: ${course?.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading course", e)
            }
        }
    }
    
    fun addLessonWithFiles(
        courseId: String,
        sectionId: String,
        lessonTitle: String,
        lessonDescription: String,
        lessonDuration: String,
        videoUri: Uri?,
        pdfUri: Uri?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                // Check if user is authenticated
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User must be authenticated to upload files")
                    return@launch
                }

                // Generate unique file names with extensions
                val videoPath = videoUri?.let { 
                    val extension = getFileExtension(it)
                    "lessons/videos/${UUID.randomUUID()}$extension"
                }
                val pdfPath = pdfUri?.let { 
                    val extension = getFileExtension(it)
                    "lessons/pdfs/${UUID.randomUUID()}$extension"
                }
                val imagePath = imageUri?.let { 
                    val extension = getFileExtension(it)
                    "lessons/images/${UUID.randomUUID()}$extension"
                }

                // Upload files with proper error handling
                val videoUrl = videoUri?.let { 
                    try {
                        repository.uploadFileToStorage(it, videoPath!!)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading video: ${e.message}", e)
                        ""
                    }
                } ?: ""

                val pdfUrl = pdfUri?.let { 
                    try {
                        repository.uploadFileToStorage(it, pdfPath!!)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading PDF: ${e.message}", e)
                        ""
                    }
                } ?: ""

                val imageUrl = imageUri?.let { 
                    try {
                        repository.uploadFileToStorage(it, imagePath!!)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading image: ${e.message}", e)
                        ""
                    }
                } ?: ""

                // Add the lesson with the uploaded file URLs
                addLessonToSection(
                    courseId = courseId,
                    sectionId = sectionId,
                    lessonTitle = lessonTitle,
                    lessonDescription = lessonDescription,
                    lessonVideoUrl = videoUrl,
                    lessonDuration = lessonDuration,
                    lessonPdfUrl = pdfUrl,
                    lessonImageUrl = imageUrl
                )

                Log.d(TAG, "Lesson added successfully with files")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading files", e)
            }
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "application/pdf" -> ".pdf"
            "video/mp4" -> ".mp4"
            else -> ""
        }
    }
} 