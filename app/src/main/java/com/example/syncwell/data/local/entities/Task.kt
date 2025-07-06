package com.example.syncwell.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = "",            // Unique ID (UUID)
    val userId: String = "",                    // Firebase UID of the owner
    val title: String = "",                     // Task title
    val description: String? = null,            // Task details
    val priority: String? = null,               // Legacy field
    val deadlineMillis: Long = 0,               // Deadline date and time in milliseconds
    val importance: Boolean = false,            // High/Low importance for Eisenhower matrix
    val reminderEnabled: Boolean = false,       // Whether to show reminders
    val reminderDaysBeforeDeadline: Int = 0,    // How many days before deadline to start reminders
    val reminderType: String = "ONCE",          // ONCE, DAILY, etc.
    val notes: String? = null,                  // Additional notes
    val completed: Boolean = false,             // Completion flag
    val lastModified: Long = 0                  // Timestamp for sync/merge
)