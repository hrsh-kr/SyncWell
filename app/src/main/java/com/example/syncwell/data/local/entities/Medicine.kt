package com.example.syncwell.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey val id: String = "",            // Unique ID
    val userId: String = "",                    // Owner UID
    val name: String = "",                      // Medicine name
    val dosage: String = "",                    // Dosage instructions
    val timeMillis: Long = 0,                   // Scheduled time (epoch millis)
    val withFood: Boolean = false,              // True = after food, False = before food
    val durationDays: Int = 0,                  // Number of days to take
    val startDate: Long = 0,                    // When to start taking (epoch millis)
    val notes: String = "",                     // Additional notes
    val category: String = "UNSPECIFIED",       // Category: MORNING, AFTERNOON, EVENING
    val lastModified: Long = 0,                 // Timestamp for sync
    val lastTaken: Long = 0                     // Last taken time (epoch millis)
)