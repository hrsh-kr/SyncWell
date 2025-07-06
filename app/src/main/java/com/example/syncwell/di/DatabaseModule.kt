package com.example.syncwell.di

import android.content.Context
import com.example.syncwell.data.local.SyncWellDatabase
import com.example.syncwell.data.local.dao.MedicineDao
import com.example.syncwell.data.local.dao.WellnessDao
import com.example.syncwell.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import android.util.Log
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val TAG = "DatabaseModule"
    private const val DB_NAME = "syncwell_db"

    /**
     * Deletes the app's database file if it exists
     */
    private fun deleteDatabase(context: Context) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (dbFile.exists()) {
                val deleted = context.deleteDatabase(DB_NAME)
                Log.d(TAG, "Database deleted: $deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting database", e)
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SyncWellDatabase {
        // If you need to delete the database (for testing or migration issues)
        // Uncomment this line to delete the database on next app start - THEN COMMENT IT AGAIN
        deleteDatabase(context)
        
        // Build the Room database
        return Room.databaseBuilder(context, SyncWellDatabase::class.java, DB_NAME)
            .addMigrations(SyncWellDatabase.MIGRATION_1_2) // Use defined migration strategy
            .fallbackToDestructiveMigration() // As a last resort if migration fails
            .build()
    }

    @Provides fun provideTaskDao(db: SyncWellDatabase): TaskDao = db.taskDao()
    @Provides fun provideMedicineDao(db: SyncWellDatabase): MedicineDao = db.medicineDao()
    @Provides fun provideWellnessDao(db: SyncWellDatabase): WellnessDao = db.wellnessDao()
}