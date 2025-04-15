package com.example.androcode // Use your package name

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // Marks application class for Hilt code generation
class AndroCodeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize other things here if needed
    }
}
