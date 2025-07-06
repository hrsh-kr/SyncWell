package com.example.syncwell.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling wellness reminders
 */
@Singleton
class WellnessReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    /**
     * Schedule a daily wellness reminder
     */
    fun scheduleDailyReminder() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
            
        val reminderRequest = PeriodicWorkRequestBuilder<WellnessReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()
            
        workManager.enqueueUniquePeriodicWork(
            WellnessReminderWorker.WELLNESS_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
    
    /**
     * Cancel the daily wellness reminder
     */
    fun cancelDailyReminder() {
        workManager.cancelUniqueWork(WellnessReminderWorker.WELLNESS_REMINDER_WORK_NAME)
    }
} 