package com.rdrgraphics.editor

import android.app.Application
import android.util.Log
import com.rdrgraphics.editor.blackbox.BlackBoxManager

class RDRApplication : Application() {
    companion object {
        private const val TAG = "RDRApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "RDR Graphics Editor starting...")
        
        // Initialize BlackBox
        try {
            val initialized = BlackBoxManager.initialize(this)
            if (initialized) {
                Log.i(TAG, "BlackBox initialized successfully")
            } else {
                Log.w(TAG, "BlackBox initialization failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BlackBox", e)
        }
    }
}
