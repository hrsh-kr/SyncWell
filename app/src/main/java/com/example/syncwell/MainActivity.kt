package com.example.syncwell

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.data.auth.AuthManager
import com.example.syncwell.data.sensors.FitnessDataManager
import com.example.syncwell.notifications.PermissionUtil
import com.example.syncwell.ui.AppNavigation
import com.example.syncwell.ui.theme.SyncWellTheme
import com.example.syncwell.ui.theme.DarkBackground
import com.example.syncwell.ui.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var fitnessDataManager: FitnessDataManager
    
    @Inject
    lateinit var authManager: AuthManager
    
    // Adding the missing googleSignInClient property
    private lateinit var googleSignInClient: GoogleSignInClient
    
    // Permission launcher for notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // No action needed here, but you could handle denied permissions
    }
    
    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("SyncWell", "Google Sign-In result: ${result.resultCode}")
        authManager.handleSignInResult(
            result.data,
            onSuccess = {
                Log.d("SyncWell", "Google Sign-In successful!")
                // Check for Google Fit permissions after successful sign-in
                checkGoogleFitPermissions()
            },
            onFailure = { e ->
                Log.e("SyncWell", "Google Sign-In failed", e)
            }
        )
    }
    
    // Apply the saved language preferences
    private fun applyLanguage() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("pref_language", "en") ?: "en"
        
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        
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
        
        // Also set via AppCompatDelegate for Compose
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(savedLanguage)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    override fun attachBaseContext(newBase: Context) {
        // Apply language before attaching the base context
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("pref_language", "en") ?: "en"
        
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)
        
        val config = Configuration(newBase.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        // Keep the splash screen visible for a little longer
        splashScreen.setKeepOnScreenCondition { true }
        
        super.onCreate(savedInstanceState)
        
        // Apply language preferences
        applyLanguage()
        
        // Set app to dark mode by default to match the fitness app design
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        // Set up Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Request notification permission for Android 13+
        PermissionUtil.requestNotificationPermissionIfNeeded(
            activity = this,
            permissionLauncher = requestPermissionLauncher
        )
        
        // Check for Google Fit permissions if already signed in
        if (authManager.isUserSignedIn()) {
            checkGoogleFitPermissions()
        }
        
        // Remove splash screen after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            splashScreen.setKeepOnScreenCondition { false }
        }, 1500) // 1.5 seconds delay
        
        enableEdgeToEdge()
        setContent {
            SyncWellTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userViewModel: UserViewModel = hiltViewModel()
                    AppNavigation(userViewModel = userViewModel)
                }
            }
        }
    }
    
    // Handle configuration changes, including locale changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguage()
    }
    
    private fun checkGoogleFitPermissions() {
        if (!fitnessDataManager.hasOAuthPermission() && !fitnessDataManager.hasRequestedPermission()) {
            try {
                // Get the account and request fitness permissions
                val account = authManager.getAccountForFitness(fitnessDataManager.fitnessOptions)
                if (account != null) {
                    GoogleSignIn.requestPermissions(
                        this,
                        FitnessDataManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                        account,
                        fitnessDataManager.fitnessOptions
                    )
                    fitnessDataManager.markPermissionRequested()
                }
            } catch (e: Exception) {
                // Handle the error silently - app will work without fitness data
                Log.e("SyncWell", "Error requesting fitness permissions", e)
            }
        }
    }
    
    // Trigger Google Sign-In
    fun signInWithGoogle() {
        try {
            googleSignInLauncher.launch(authManager.getSignInIntent())
        } catch (e: Exception) {
            Log.e("SyncWell", "Error launching Google Sign-In", e)
        }
    }
    
    // Handle the result from fitness permission request
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == FitnessDataManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // The user granted permission, try to get fitness data
                Log.d("SyncWell", "Google Fit permissions granted!")
            } else {
                // The user denied permission
                Log.d("SyncWell", "Google Fit permissions denied!")
            }
        }
    }
}