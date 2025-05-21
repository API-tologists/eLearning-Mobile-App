package com.example.elearning.local

import androidx.room.*

@Dao
interface CourseDao {
    // Course operations
    @Query("SELECT * FROM downloaded_courses")
    suspend fun getAllDownloadedCourses(): List<CourseEntity>

    @Query("SELECT * FROM downloaded_courses WHERE id = :courseId")
    suspend fun getDownloadedCourse(courseId: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM downloaded_courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String)

    // Section operations
    @Query("SELECT * FROM downloaded_sections WHERE courseId = :courseId ORDER BY `order` ASC")
    suspend fun getSectionsForCourse(courseId: String): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Query("DELETE FROM downloaded_sections WHERE courseId = :courseId")
    suspend fun deleteSectionsForCourse(courseId: String)

    // Lesson operations
    @Query("SELECT * FROM downloaded_lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    suspend fun getLessonsForCourse(courseId: String): List<LessonEntity>

    @Query("SELECT * FROM downloaded_lessons WHERE sectionId = :sectionId ORDER BY `order` ASC")
    suspend fun getLessonsForSection(sectionId: String): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM downloaded_lessons WHERE courseId = :courseId")
    suspend fun deleteLessonsForCourse(courseId: String)

    @Query("UPDATE downloaded_lessons SET localVideoPath = :path WHERE id = :lessonId")
    suspend fun updateLessonVideoPath(lessonId: String, path: String?)

    @Query("UPDATE downloaded_lessons SET localPdfPath = :path WHERE id = :lessonId")
    suspend fun updateLessonPdfPath(lessonId: String, path: String?)

    @Query("UPDATE downloaded_lessons SET localImagePath = :path WHERE id = :lessonId")
    suspend fun updateLessonImagePath(lessonId: String, path: String?)
} 