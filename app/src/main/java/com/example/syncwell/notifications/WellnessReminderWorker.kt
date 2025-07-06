package com.example.syncwell.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.syncwell.R
import com.example.syncwell.data.repository.WellnessRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.threeten.bp.LocalDate

/**
 * WorkManager worker for scheduling wellness reminders
 */
@HiltWorker
class WellnessReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val wellnessRepository: WellnessRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WELLNESS_REMINDER_WORK_NAME = "wellness_reminder_work"
        const val WELLNESS_NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        // Check if user has already logged wellness data today
        val userId = wellnessRepository.getCurrentUserId() ?: return Result.success()
        
        // Get today's date
        val today = LocalDate.now()
        
        // Check if there's an entry for today already
        val hasEntryForToday = wellnessRepository.hasEntryForDate(today)
        
        // Only show reminder if no entry exists for today
        if (!hasEntryForToday) {
            val title = applicationContext.getString(R.string.wellness_notification_title)
            val content = applicationContext.getString(R.string.wellness_notification_content)
            
            NotificationHelper.showWellnessNotification(
                context = applicationContext,
                notificationId = WELLNESS_NOTIFICATION_ID,
                title = title,
                content = content
            )
        }
        
        return Result.success()
    }
} 