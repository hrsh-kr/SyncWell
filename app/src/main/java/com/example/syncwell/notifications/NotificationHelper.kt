package com.example.syncwell.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.syncwell.MainActivity
import com.example.syncwell.R

/**
 * Helper class for creating and showing notifications
 */
object NotificationHelper {
    const val CHANNEL_ID_MEDICINE = "medicine_channel"
    const val CHANNEL_ID_WELLNESS = "wellness_channel"
    
    private const val REQUEST_CODE_OPEN_APP = 0
    
    /**
     * Create notification channels for Android O and above
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Medicine channel (high importance for exact timing)
            val medicineName = context.getString(R.string.notification_channel_medicine_name)
            val medicineDescription = context.getString(R.string.notification_channel_medicine_description)
            val medicineChannel = NotificationChannel(
                CHANNEL_ID_MEDICINE,
                medicineName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = medicineDescription
            }
            
            // Wellness channel (default importance)
            val wellnessName = context.getString(R.string.notification_channel_wellness_name)
            val wellnessDescription = context.getString(R.string.notification_channel_wellness_description)
            val wellnessChannel = NotificationChannel(
                CHANNEL_ID_WELLNESS,
                wellnessName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = wellnessDescription
            }
            
            // Register the channels with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as NotificationManager
            notificationManager.createNotificationChannel(medicineChannel)
            notificationManager.createNotificationChannel(wellnessChannel)
        }
    }
    
    /**
     * Show a medicine reminder notification
     */
    fun showMedicineNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        medicineId: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_TYPE", "MEDICINE")
            if (medicineId != null) {
                putExtra("MEDICINE_ID", medicineId)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MEDICINE)
            .setSmallIcon(R.drawable.ic_notification_medicine)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handle notification permission denied (Android 13+)
        }
    }
    
    /**
     * Show a wellness reminder notification
     */
    fun showWellnessNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_TYPE", "WELLNESS")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WELLNESS)
            .setSmallIcon(R.drawable.ic_notification_wellness)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handle notification permission denied (Android 13+)
        }
    }
} 