package com.example.syncwell.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.syncwell.data.local.DateConverters
import com.google.firebase.firestore.DocumentId
import org.threeten.bp.LocalDate
import java.util.UUID

@Entity(tableName = "wellness_entries")
@TypeConverters(DateConverters::class)
data class WellnessEntry(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: LocalDate = LocalDate.now(),
    val timestamp: Long = System.currentTimeMillis(),  // Added for time-based queries
    
    // Water tracking
    val waterIntakeOz: Int = 0,
    val waterGoalOz: Int = 64,                        // Default 8 glasses (8oz each)
    
    // Sleep tracking
    val sleepHours: Float = 0f,
    val sleepGoalHours: Float = 8f,                   // Default 8 hours
    val bedTimeMillis: Long = 0,                      // Bedtime in millis
    val wakeupTimeMillis: Long = 0,                   // Wakeup time in millis
    
    // Step tracking
    val stepCount: Int = 0,
    val stepGoal: Int = 10000,                        // Default 10k steps
    
    // Other health metrics
    val moodRating: Int = 0,                          // Added for mood queries
    val energyLevel: Int = 0,                         // Added for energy queries
    val notes: String = "",                           // Optional notes field
    
    val streakCounter: Int = 0,                       // For tracking streaks
    val lastModified: Long = System.currentTimeMillis()
)

