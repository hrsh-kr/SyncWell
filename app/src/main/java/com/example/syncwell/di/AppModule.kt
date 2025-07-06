package com.example.syncwell.di

import android.content.Context
import com.example.syncwell.initialization.AppInitializer
import com.example.syncwell.notifications.WellnessReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideAppInitializer(
        @ApplicationContext context: Context,
        wellnessReminderScheduler: WellnessReminderScheduler
    ): AppInitializer {
        return AppInitializer(context, wellnessReminderScheduler)
    }
} 