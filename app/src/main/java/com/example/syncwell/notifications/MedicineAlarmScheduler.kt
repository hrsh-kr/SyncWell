package com.example.syncwell.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.syncwell.data.local.entities.Medicine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling medicine alarms
 */
@Singleton
class MedicineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * Schedule an alarm for a medicine
     */
    fun scheduleMedicineAlarm(medicine: Medicine) {
        val intent = createAlarmIntent(medicine)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // On Android 12+ without permission, use a less precise alarm
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                medicine.timeMillis,
                pendingIntent
            )
        } else {
            // Use exact alarm if possible
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                medicine.timeMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancel an alarm for a medicine
     */
    fun cancelMedicineAlarm(medicine: Medicine) {
        val intent = createAlarmIntent(medicine)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel the alarm if it exists
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    
    /**
     * Reschedule an alarm for a medicine (cancel and re-schedule)
     */
    fun rescheduleMedicineAlarm(medicine: Medicine) {
        cancelMedicineAlarm(medicine)
        scheduleMedicineAlarm(medicine)
    }
    
    /**
     * Create the intent for the alarm
     */
    private fun createAlarmIntent(medicine: Medicine): Intent {
        return Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("MED_ID", medicine.id)
            putExtra("MED_NAME", medicine.name)
        }
    }
} 