package com.example.elearning

import android.app.Application
import com.example.elearning.viewmodel.CourseViewModel

class eLearningApplication : Application() {
    lateinit var courseViewModel: CourseViewModel
        private set

    override fun onCreate() {
        super.onCreate()
        courseViewModel = CourseViewModel(this)
    }
}
