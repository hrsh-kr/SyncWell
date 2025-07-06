package com.example.syncwell.data.sensors

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.syncwell.data.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitnessDataManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager
) {
    companion object {
        private const val TAG = "FitnessDataManager"
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
        private const val PREFS_NAME = "fitness_prefs"
        private const val KEY_PERMISSION_REQUESTED = "fitness_permission_requested"
    }

    // Define the FitnessOptions needed for the app
    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    
    // Shared Preferences for tracking if permissions have been requested
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Check if the user has granted the necessary permissions
    fun hasOAuthPermission(): Boolean {
        return authManager.hasFitnessPermission(fitnessOptions)
    }
    
    // Check if we've already requested permissions before
    fun hasRequestedPermission(): Boolean {
        return prefs.getBoolean(KEY_PERMISSION_REQUESTED, false)
    }
    
    // Mark that we've requested permissions
    fun markPermissionRequested() {
        prefs.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
    }
    
    // Reset permission requested flag (useful when user signs out)
    fun resetPermissionRequested() {
        prefs.edit().putBoolean(KEY_PERMISSION_REQUESTED, false).apply()
    }

    // Get step count for today
    suspend fun getTodayStepCount(): Int {
        if (!hasOAuthPermission()) {
            Log.w(TAG, "No OAuth permission for fitness data")
            return 0
        }

        try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Past 24 hours

            val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build()

            val account = authManager.getAccountForFitness(fitnessOptions) ?: return 0
            
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()

            var totalSteps = 0
            if (response.buckets.isNotEmpty() && response.buckets[0].dataSets.isNotEmpty()) {
                val dataSet = response.buckets[0].dataSets[0]
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        totalSteps += dataPoint.getValue(field).asInt()
                    }
                }
            }
            
            Log.d(TAG, "Total steps: $totalSteps")
            return totalSteps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting step data", e)
            return 0
        }
    }
} 