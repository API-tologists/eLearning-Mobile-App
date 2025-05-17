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
import com.example.elearning.model.Quiz
import com.example.elearning.model.Question
import com.example.elearning.model.QuestionType
import com.example.elearning.model.QuizAttempt
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.flow
import com.example.elearning.service.GeminiService
import android.content.Context
import com.example.elearning.R

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CourseViewModel"
    private val repository = CourseRepository()
    private val userRepository = UserRepository()
    private val db = FirebaseFirestore.getInstance()
    private val coursesCollection = db.collection("courses")
    private val enrollmentsCollection = db.collection("enrollments")
    private val context = application.applicationContext
    private val geminiService = GeminiService(context.getString(R.string.gemini_api_key)).apply {
        checkModelAvailability()
    }
    
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
    
    private val _externalResources = MutableStateFlow<List<String>>(emptyList())
    val externalResources: StateFlow<List<String>> = _externalResources.asStateFlow()
    
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
                    
                    // Update the local state
                    _selectedCourse.value = course
                    
                    Log.d(TAG, "Progress updated successfully: ${updatedCompletedLessons.size}/$totalLessons lessons completed (${progress}%)")
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
    
    fun updateCourseRating(courseId: String, rating: Float) {
        viewModelScope.launch {
            try {
                repository.updateCourseRating(courseId, rating)
                // Reload the course to update the UI
                loadCourseById(courseId)
                Log.d(TAG, "Course rating updated to $rating")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating course rating", e)
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

    fun getLessonCompletionStatus(userId: String, courseId: String, lessonId: String): Flow<Boolean> = callbackFlow {
        try {
            val enrollmentQuery = enrollmentsCollection
                .whereEqualTo("studentId", userId)
                .whereEqualTo("courseId", courseId)
                .get()
                .await()

            if (enrollmentQuery.isEmpty) {
                trySend(false)
                return@callbackFlow
            }

            val enrollment = enrollmentQuery.documents[0].toObject(CourseEnrollment::class.java)
            trySend(enrollment?.completedLessons?.contains(lessonId) ?: false)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lesson completion status", e)
            trySend(false)
        }
        
        // Add awaitClose to properly clean up the flow
        awaitClose()
    }

    fun updateLesson(
        courseId: String,
        sectionId: String,
        lessonId: String,
        title: String,
        description: String,
        duration: String
    ) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        val updatedLessons = section.lessons.map { lesson ->
                            if (lesson.id == lessonId) {
                                lesson.copy(
                                    title = title,
                                    description = description,
                                    duration = duration
                                )
                            } else lesson
                        }
                        section.copy(lessons = updatedLessons)
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Lesson updated successfully: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lesson", e)
            }
        }
    }

    fun deleteLesson(courseId: String, sectionId: String, lessonId: String) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(lessons = section.lessons.filter { it.id != lessonId })
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Lesson deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting lesson", e)
            }
        }
    }

    fun updateLessonMedia(
        courseId: String,
        sectionId: String,
        lessonId: String,
        mediaType: String, // "image", "video", "pdf"
        uri: Uri
    ) {
        viewModelScope.launch {
            // 1. Upload file to Firebase Storage, get downloadUrl
            val downloadUrl = uploadFileToFirebase(uri, mediaType) // You must implement this!
            // 2. Update lesson's media URL in Firestore
            val course = _selectedCourse.value ?: return@launch
            val updatedSections = course.sections.map { section ->
                if (section.id == sectionId) {
                    val updatedLessons = section.lessons.map { lesson ->
                        if (lesson.id == lessonId) {
                            when (mediaType) {
                                "image" -> lesson.copy(imageUrl = downloadUrl)
                                "video" -> lesson.copy(videoUrl = downloadUrl)
                                "pdf" -> lesson.copy(pdfUrl = downloadUrl)
                                else -> lesson
                            }
                        } else lesson
                    }
                    section.copy(lessons = updatedLessons)
                } else section
            }
            val updatedCourse = course.copy(sections = updatedSections)
            coursesCollection.document(courseId).set(updatedCourse).await()
            _selectedCourse.value = updatedCourse
        }
    }

    fun deleteLessonMedia(
        courseId: String,
        sectionId: String,
        lessonId: String,
        mediaType: String // "image", "video", "pdf"
    ) {
        viewModelScope.launch {
            val course = _selectedCourse.value ?: return@launch
            val updatedSections = course.sections.map { section ->
                if (section.id == sectionId) {
                    val updatedLessons = section.lessons.map { lesson ->
                        if (lesson.id == lessonId) {
                            when (mediaType) {
                                "image" -> lesson.copy(imageUrl = "")
                                "video" -> lesson.copy(videoUrl = "")
                                "pdf" -> lesson.copy(pdfUrl = "")
                                else -> lesson
                            }
                        } else lesson
                    }
                    section.copy(lessons = updatedLessons)
                } else section
            }
            val updatedCourse = course.copy(sections = updatedSections)
            coursesCollection.document(courseId).set(updatedCourse).await()
            _selectedCourse.value = updatedCourse
        }
    }

    // You must implement this function to upload to Firebase Storage and return the download URL
    suspend fun uploadFileToFirebase(uri: Uri, mediaType: String): String {
        // ... your upload logic here ...
        return "https://your_download_url"
    }

    fun addQuizToSection(
        courseId: String,
        sectionId: String,
        quizTitle: String,
        quizDescription: String,
        passingScore: Int,
        timeLimit: Int,
        attemptsAllowed: Int
    ) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val newQuiz = Quiz(
                    id = UUID.randomUUID().toString(),
                    title = quizTitle,
                    description = quizDescription,
                    passingScore = passingScore,
                    timeLimit = timeLimit,
                    attemptsAllowed = attemptsAllowed
                )
                
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(quizzes = section.quizzes + newQuiz)
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Quiz added successfully: $quizTitle")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding quiz", e)
            }
        }
    }

    fun addQuestionToQuiz(
        courseId: String,
        sectionId: String,
        quizId: String,
        questionText: String,
        questionType: QuestionType,
        options: List<String>,
        correctAnswer: String,
        points: Int
    ) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val newQuestion = Question(
                    id = UUID.randomUUID().toString(),
                    text = questionText,
                    type = questionType,
                    options = options,
                    correctAnswer = correctAnswer,
                    points = points
                )
                
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        val updatedQuizzes = section.quizzes.map { quiz ->
                            if (quiz.id == quizId) {
                                quiz.copy(questions = quiz.questions + newQuestion)
                            } else quiz
                        }
                        section.copy(quizzes = updatedQuizzes)
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Question added successfully to quiz")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding question to quiz", e)
            }
        }
    }

    fun deleteQuiz(courseId: String, sectionId: String, quizId: String) {
        viewModelScope.launch {
            try {
                val course = _selectedCourse.value ?: return@launch
                val updatedSections = course.sections.map { section ->
                    if (section.id == sectionId) {
                        section.copy(quizzes = section.quizzes.filter { it.id != quizId })
                    } else section
                }
                val updatedCourse = course.copy(sections = updatedSections)
                
                // Update the course in Firestore
                coursesCollection.document(courseId).set(updatedCourse).await()
                _selectedCourse.value = updatedCourse
                
                Log.d(TAG, "Quiz deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting quiz", e)
            }
        }
    }

    fun saveQuizAttempt(
        courseId: String,
        sectionId: String,
        quizId: String,
        score: Int,
        hasPassed: Boolean
    ) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User must be authenticated to save quiz attempt")
                    return@launch
                }

                val attempt = QuizAttempt(
                    id = UUID.randomUUID().toString(),
                    studentId = currentUser.uid,
                    courseId = courseId,
                    sectionId = sectionId,
                    quizId = quizId,
                    score = score,
                    hasPassed = hasPassed,
                    timestamp = System.currentTimeMillis()
                )

                // Save the attempt
                db.collection("quiz_attempts").document(attempt.id).set(attempt).await()

                // If passed, mark the section as completed
                if (hasPassed) {
                    val enrollmentQuery = enrollmentsCollection
                        .whereEqualTo("studentId", currentUser.uid)
                        .whereEqualTo("courseId", courseId)
                        .get()
                        .await()

                    if (!enrollmentQuery.isEmpty) {
                        val enrollmentDoc = enrollmentQuery.documents[0]
                        val enrollment = enrollmentDoc.toObject(CourseEnrollment::class.java)
                        
                        if (enrollment != null) {
                            val updatedCompletedLessons = enrollment.completedLessons + sectionId
                            val updatedEnrollment = enrollment.copy(
                                completedLessons = updatedCompletedLessons,
                                progress = calculateProgress(courseId, updatedCompletedLessons)
                            )
                            enrollmentDoc.reference.set(updatedEnrollment).await()
                        }
                    }
                }

                Log.d(TAG, "Quiz attempt saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving quiz attempt", e)
            }
        }
    }

    fun canTakeQuiz(courseId: String, sectionId: String): Flow<Boolean> = flow {
        var canTake = false
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // First check if user is enrolled
                val enrollmentQuery = enrollmentsCollection
                    .whereEqualTo("studentId", currentUser.uid)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .await()

                if (!enrollmentQuery.isEmpty) {
                    // Get the enrollment
                    val enrollment = enrollmentQuery.documents[0].toObject(CourseEnrollment::class.java)
                    if (enrollment != null) {
                        // Get the course and section
                        val course = coursesCollection.document(courseId).get().await().toObject(Course::class.java)
                        val section = course?.sections?.find { it.id == sectionId }
                        
                        if (section != null) {
                            // Check if all lessons in the section are completed
                            val completedLessons = enrollment.completedLessons
                            canTake = section.lessons.all { lesson ->
                                completedLessons.contains(lesson.id)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking quiz eligibility", e)
        }
        emit(canTake)
    }

    private suspend fun calculateProgress(courseId: String, completedLessons: List<String>): Int {
        val course = coursesCollection.document(courseId).get().await().toObject(Course::class.java)
        val totalLessons = course?.sections?.sumOf { it.lessons.size } ?: 0
        return if (totalLessons > 0) {
            (completedLessons.size * 100) / totalLessons
        } else 0
    }

    fun getExternalResources(quizTitle: String, quizDescription: String, failedQuestions: List<Question>) {
        viewModelScope.launch {
            try {
                val questionTexts = failedQuestions.map { it.text }
                val resources = geminiService.generateLearningResources(
                    quizTitle = quizTitle,
                    quizDescription = quizDescription,
                    failedQuestions = questionTexts
                )
                _externalResources.value = resources
            } catch (e: Exception) {
                Log.e(TAG, "Error getting external resources", e)
                _externalResources.value = emptyList()
            }
        }
    }
} 