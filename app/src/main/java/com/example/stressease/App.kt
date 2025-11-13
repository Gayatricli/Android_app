package com.example.stressease

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.FirebaseApp

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase (in case it's not auto-initialized)
        FirebaseApp.initializeApp(this)

        //
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = settings

        println("✅ Firestore offline persistence enabled successfully.")
    }
}