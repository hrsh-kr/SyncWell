package com.example.syncwell.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.syncwell.data.local.dao.MedicineDao
import com.example.syncwell.data.local.dao.TaskDao
import com.example.syncwell.data.local.dao.WellnessDao
import com.example.syncwell.data.local.entities.Task
import com.example.syncwell.data.local.entities.WellnessEntry
import com.example.syncwell.data.local.entities.Medicine

@Database(entities = [Task::class, Medicine::class, WellnessEntry::class], version = 2)
abstract class SyncWellDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun medicineDao(): MedicineDao
    abstract fun wellnessDao(): WellnessDao
    
    companion object {
        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add missing columns to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN deadlineMillis INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN importance INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderType TEXT NOT NULL DEFAULT 'ONCE'")
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderDaysBeforeDeadline INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}