package com.example.elearning

import android.app.Application
import com.google.firebase.FirebaseApp

class ELearningApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
