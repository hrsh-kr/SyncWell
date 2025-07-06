package com.example.syncwell.initialization

import android.content.Context
import android.util.Log
import com.example.syncwell.notifications.WellnessReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles initialization tasks for the app
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wellnessReminderScheduler: WellnessReminderScheduler
) {
    
    private val TAG = "AppInitializer"
    
    /**
     * Initialize app components
     */
    fun initialize() {
        initializeReminders()
    }
    
    /**
     * Initialize reminders for the app
     */
    private fun initializeReminders() {
        try {
            Log.d(TAG, "Scheduling wellness reminders")
            wellnessReminderScheduler.scheduleDailyReminder()
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling wellness reminders", e)
        }
    }
} 