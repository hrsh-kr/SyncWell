package com.example.syncwell

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration as WorkConfiguration
import com.example.syncwell.data.auth.AuthManager
import com.example.syncwell.data.sensors.FitnessDataManager
import com.example.syncwell.initialization.AppInitializer
import com.example.syncwell.notifications.NotificationHelper
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class SyncWellApp : Application(), WorkConfiguration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var appInitializer: AppInitializer
    
    @Inject
    lateinit var authManager: AuthManager
    
    @Inject
    lateinit var fitnessDataManager: FitnessDataManager
    
    companion object {
        private const val PREF_LANGUAGE = "pref_language"
        private const val DEFAULT_LANGUAGE = "en"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ThreeTenABP for date/time operations
        AndroidThreeTen.init(this)
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Initialize app components
        appInitializer.initialize()
        
        // Apply saved language preference
        applySavedLanguage()
    }
    
    // Apply the saved language preference
    private fun applySavedLanguage() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        
        // Create locale from language code
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        
        // Set the app locale programmatically using both methods for compatibility
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(savedLanguage)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // Apply configuration directly to resources
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    // Override attachBaseContext to apply locale before views are created
    override fun attachBaseContext(base: Context) {
        // Get saved language
        val prefs = base.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString(PREF_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        
        // Create locale from language code
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        
        // Create configuration with locale
        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        // Create context with new configuration
        val context = base.createConfigurationContext(config)
        
        super.attachBaseContext(context)
    }
    
    // Configure WorkManager with Hilt integration
    override val workManagerConfiguration: WorkConfiguration
        get() = WorkConfiguration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
} 