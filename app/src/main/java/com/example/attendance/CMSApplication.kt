package com.example.attendance

import android.app.Application
import com.google.firebase.FirebaseApp

class CMSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
