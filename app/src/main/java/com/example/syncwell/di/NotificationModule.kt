package com.example.syncwell.di

import android.content.Context
import androidx.work.WorkManager
import com.example.syncwell.notifications.MedicineAlarmScheduler
import com.example.syncwell.notifications.WellnessReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing notification and reminder related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Singleton
    @Provides
    fun provideMedicineAlarmScheduler(@ApplicationContext context: Context): MedicineAlarmScheduler {
        return MedicineAlarmScheduler(context)
    }
    
    @Singleton
    @Provides
    fun provideWellnessReminderScheduler(
        @ApplicationContext context: Context, 
        workManager: WorkManager
    ): WellnessReminderScheduler {
        return WellnessReminderScheduler(context, workManager)
    }
} 