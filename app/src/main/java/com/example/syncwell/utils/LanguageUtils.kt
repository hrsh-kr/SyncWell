package com.example.syncwell.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.syncwell.MainActivity
import java.util.Locale

/**
 * Helper functions for language management in the app.
 */

private const val PREF_LANGUAGE = "pref_language"

/**
 * Sets the application language.
 * 
 * @param context The application context
 * @param languageCode The ISO code of the language to set (e.g., "en", "es")
 */
fun setAppLanguage(context: Context, languageCode: String) {
    // Save language preference
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    prefs.edit().putString(PREF_LANGUAGE, languageCode).apply()
    
    // Create locale from language code
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    // Set the app locale programmatically using both methods for compatibility
    val localeList = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(localeList)
    
    // Apply configuration directly to resources
    val config = context.resources.configuration
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        config.setLocales(localeList)
    } else {
        @Suppress("DEPRECATION")
        config.locale = locale
    }
    
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    
    // Restart the main activity for changes to take effect
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    
    // Finish current activity if it's an activity
    if (context is Activity) {
        context.finish()
    }
}

/**
 * Gets the current application language.
 * 
 * @param context The application context
 * @return The ISO code of the current language (e.g., "en", "es")
 */
fun getCurrentLanguage(context: Context): String {
    // First check if we have a saved preference
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val savedLanguage = prefs.getString(PREF_LANGUAGE, null)
    
    // If we have a saved preference, return it
    if (!savedLanguage.isNullOrEmpty()) {
        return savedLanguage
    }
    
    // Otherwise, get current locale from resources
    val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale
    }
    
    // Return the language code
    return currentLocale.language
}

/**
 * Checks if the app is using right-to-left layout direction.
 * 
 * @param context The application context
 * @return True if the app is using RTL layout, false otherwise
 */
fun isRtlLayout(context: Context): Boolean {
    val config = context.resources.configuration
    return config.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL
} 