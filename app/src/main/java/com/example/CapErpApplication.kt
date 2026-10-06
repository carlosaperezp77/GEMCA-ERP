package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class CapErpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            val apiKey = BuildConfig.FIREBASE_API_KEY
            val appId = BuildConfig.FIREBASE_APPLICATION_ID
            val projectId = BuildConfig.FIREBASE_PROJECT_ID

            if (apiKey.isEmpty() || apiKey.startsWith("FIREBASE_API") ||
                appId.isEmpty() || appId.startsWith("FIREBASE_APP") ||
                projectId.isEmpty() || projectId.startsWith("FIREBASE_PROJECT")) {
                
                Log.w("CapErpApplication", "Firebase configuration is not fully configured. Using fallback sandbox options.")
                
                // Initialize with safe fallback options to allow Firebase SDK calls (Auth & Firestore) 
                // to compile and load without throwing direct initialization exceptions in preview mode.
                val fallbackOptions = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyFallbackPlaceholderForPreviewModeOnly")
                    .setApplicationId("1:000000000000:android:0000000000000000000000")
                    .setProjectId("cap-erp-sandbox")
                    .build()
                FirebaseApp.initializeApp(this, fallbackOptions)
                Log.i("CapErpApplication", "Firebase sandbox environment initialized successfully.")
            } else {
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.i("CapErpApplication", "Firebase custom environment initialized successfully.")
            }
        } catch (e: Exception) {
            Log.e("CapErpApplication", "Error during Firebase initialization", e)
        }
    }
}
