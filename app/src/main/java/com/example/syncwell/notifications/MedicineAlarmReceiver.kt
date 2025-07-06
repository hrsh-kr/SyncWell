package com.example.syncwell.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.syncwell.R

/**
 * BroadcastReceiver for medicine alarms
 */
class MedicineAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // Get medicine information from the intent
        val medicineId = intent.getStringExtra("MED_ID") ?: return
        val medicineName = intent.getStringExtra("MED_NAME") ?: context.getString(R.string.app_name)
        
        // Show notification
        val title = context.getString(R.string.medicine_notification_title)
        val content = context.getString(R.string.medicine_notification_content, medicineName)
        
        // Use medicine ID hash code as notification ID to ensure uniqueness but consistency for the same medicine
        val notificationId = medicineId.hashCode()
        
        NotificationHelper.showMedicineNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            content = content,
            medicineId = medicineId
        )
    }
} 