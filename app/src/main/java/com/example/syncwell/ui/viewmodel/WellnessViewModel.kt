package com.example.syncwell.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncwell.data.local.entities.WellnessEntry
import com.example.syncwell.data.repository.WellnessRepository
import com.example.syncwell.data.sensors.FitnessDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WellnessViewModel @Inject constructor(
    private val repository: WellnessRepository,
    private val fitnessDataManager: FitnessDataManager
) : ViewModel() {

    // UI state for wellness operations
    sealed class WellnessState {
        object Loading : WellnessState()
        data class Success(val entries: List<WellnessEntry>) : WellnessState()
        data class Error(val message: String) : WellnessState()
    }

    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        object Success : FormState()
        data class Error(val message: String) : FormState()
    }

    sealed class SummaryState {
        object Loading : SummaryState()
        data class Success(val summary: WellnessRepository.WellnessSummary) : SummaryState()
        data class Error(val message: String) : SummaryState()
    }

    // Wellness recommendations
    data class WellnessRecommendation(
        val area: String, 
        val message: String, 
        val actionItem: String
    )

    // Achievement types
    enum class AchievementType {
        WATER_STREAK,
        SLEEP_STREAK,
        STEPS_STREAK,
        WATER_GOAL_100,
        SLEEP_GOAL_100,
        STEPS_GOAL_100
    }

    // State flows
    private val _wellnessState = MutableStateFlow<WellnessState>(WellnessState.Success(emptyList()))
    val wellnessState: StateFlow<WellnessState> = _wellnessState.asStateFlow()

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Success(WellnessRepository.WellnessSummary(0f, 0f, 0f, 0)))
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val _recommendations = MutableStateFlow<List<WellnessRecommendation>>(emptyList())
    val recommendations: StateFlow<List<WellnessRecommendation>> = _recommendations.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementType>>(emptyList())
    val achievements: StateFlow<List<AchievementType>> = _achievements.asStateFlow()

    // Selected entry for editing
    private val _selectedEntry = MutableStateFlow<WellnessEntry?>(null)
    val selectedEntry: StateFlow<WellnessEntry?> = _selectedEntry.asStateFlow()

    // Today's entry (if exists)
    private val _todayEntry = MutableStateFlow<WellnessEntry?>(null)
    val todayEntry: StateFlow<WellnessEntry?> = _todayEntry.asStateFlow()

    init {
        loadEntries()
        loadSummary(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis()) // Last 7 days
        viewModelScope.launch {
            loadTodayEntry()
            // Try to fetch step data from Google Fit if available
            if (fitnessDataManager.hasOAuthPermission()) {
                updateStepCountFromGoogleFit()
            }
        }
    }

    // Load today's wellness entry if exists
    private suspend fun loadTodayEntry() {
        try {
            val today = LocalDate.now()
            val entry = repository.getEntryForDate(today)
            if (entry != null) {
                _todayEntry.value = entry
            } else {
                // Create a default entry with appropriate targets
                _todayEntry.value = WellnessEntry(
                    date = today,
                    waterGoalOz = 64,
                    sleepGoalHours = 8f
                )
            }
        } catch (e: Exception) {
            // Create a default entry on error
            _todayEntry.value = WellnessEntry(
                date = LocalDate.now(),
                waterGoalOz = 64,
                sleepGoalHours = 8f
            )
        }
    }

    // Load all wellness entries for the current user
    private fun loadEntries() {
        viewModelScope.launch {
            try {
                _wellnessState.value = WellnessState.Loading
                repository.entries
                    .catch { error ->
                        _wellnessState.value = WellnessState.Error(error.message ?: "Failed to load wellness data")
                    }
                    .collect { entries ->
                        _wellnessState.value = WellnessState.Success(entries)
                        generateRecommendations(entries)
                        checkAchievements(entries)
                    }
            } catch (e: Exception) {
                _wellnessState.value = WellnessState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    // Generate wellness recommendations based on recent data
    private fun generateRecommendations(entries: List<WellnessEntry>) {
        if (entries.isEmpty()) return
        
        val recentEntries = entries.sortedByDescending { it.timestamp }.take(7)
        val recommendations = mutableListOf<WellnessRecommendation>()
        
        // Check water intake
        val avgWaterPercentage = recentEntries.map { 
            it.waterIntakeOz.toFloat() / it.waterGoalOz.toFloat() 
        }.average()
        
        if (avgWaterPercentage < 0.8f) {
            recommendations.add(
                WellnessRecommendation(
                    area = "Hydration",
                    message = "You're not meeting your water intake goals.",
                    actionItem = "Try to drink one more glass of water each day."
                )
            )
        }
        
        // Check sleep
        val avgSleepPercentage = recentEntries.map { 
            it.sleepHours / it.sleepGoalHours 
        }.average()
        
        if (avgSleepPercentage < 0.8f) {
            recommendations.add(
                WellnessRecommendation(
                    area = "Sleep",
                    message = "You're not getting enough sleep.",
                    actionItem = "Try going to bed 30 minutes earlier tonight."
                )
            )
        }
        
        // Check steps
        val avgStepPercentage = recentEntries.map { 
            it.stepCount.toFloat() / it.stepGoal.toFloat() 
        }.average()
        
        if (avgStepPercentage < 0.7f) {
            recommendations.add(
                WellnessRecommendation(
                    area = "Activity",
                    message = "Your step count is below your target.",
                    actionItem = "Try taking a 10-minute walk each day."
                )
            )
        }
        
        _recommendations.value = recommendations
    }

    // Check for achievements
    private fun checkAchievements(entries: List<WellnessEntry>) {
        if (entries.isEmpty()) return
        
        val achievements = mutableListOf<AchievementType>()
        val sortedEntries = entries.sortedBy { it.date }
        
        // Check for 7-day streaks
        var waterStreak = 0
        var sleepStreak = 0
        var stepStreak = 0
        
        for (entry in sortedEntries) {
            // Water streak
            if (entry.waterIntakeOz >= entry.waterGoalOz) {
                waterStreak++
                if (waterStreak >= 7 && AchievementType.WATER_STREAK !in achievements) {
                    achievements.add(AchievementType.WATER_STREAK)
                }
            } else {
                waterStreak = 0
            }
            
            // Sleep streak
            if (entry.sleepHours >= entry.sleepGoalHours) {
                sleepStreak++
                if (sleepStreak >= 7 && AchievementType.SLEEP_STREAK !in achievements) {
                    achievements.add(AchievementType.SLEEP_STREAK)
                }
            } else {
                sleepStreak = 0
            }
            
            // Steps streak
            if (entry.stepCount >= entry.stepGoal) {
                stepStreak++
                if (stepStreak >= 7 && AchievementType.STEPS_STREAK !in achievements) {
                    achievements.add(AchievementType.STEPS_STREAK)
                }
            } else {
                stepStreak = 0
            }
        }
        
        // Check for 100-day achievements (total counts)
        if (sortedEntries.count { it.waterIntakeOz >= it.waterGoalOz } >= 100) {
            achievements.add(AchievementType.WATER_GOAL_100)
        }
        
        if (sortedEntries.count { it.sleepHours >= it.sleepGoalHours } >= 100) {
            achievements.add(AchievementType.SLEEP_GOAL_100)
        }
        
        if (sortedEntries.count { it.stepCount >= it.stepGoal } >= 100) {
            achievements.add(AchievementType.STEPS_GOAL_100)
        }
        
        _achievements.value = achievements
    }

    // Refresh entries from remote
    fun refreshEntries() {
        viewModelScope.launch {
            try {
                _wellnessState.value = WellnessState.Loading
                repository.refreshEntries()
                // The updated data will flow through the entries flow
            } catch (e: Exception) {
                _wellnessState.value = WellnessState.Error(e.message ?: "Failed to refresh wellness data")
            }
        }
    }

    // Load wellness summary for a period
    fun loadSummary(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            try {
                _summaryState.value = SummaryState.Loading
                val summary = repository.getWellnessSummary(startTime, endTime)
                _summaryState.value = SummaryState.Success(summary)
            } catch (e: Exception) {
                _summaryState.value = SummaryState.Error(e.message ?: "Failed to load wellness summary")
            }
        }
    }

    // Add a new wellness entry with all fields
    fun addEntry(
        date: LocalDate,
        waterIntakeOz: Int,
        waterGoalOz: Int,
        sleepHours: Float,
        sleepGoalHours: Float,
        bedTimeMillis: Long,
        wakeupTimeMillis: Long,
        stepCount: Int,
        stepGoal: Int,
        moodRating: Int,
        energyLevel: Int,
        notes: String
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val entry = WellnessEntry(
                    id = UUID.randomUUID().toString(),
                    userId = "",  // Will be set by repository
                    date = date,
                    timestamp = System.currentTimeMillis(),
                    waterIntakeOz = waterIntakeOz,
                    waterGoalOz = waterGoalOz,
                    sleepHours = sleepHours,
                    sleepGoalHours = sleepGoalHours,
                    bedTimeMillis = bedTimeMillis,
                    wakeupTimeMillis = wakeupTimeMillis,
                    stepCount = stepCount,
                    stepGoal = stepGoal,
                    moodRating = moodRating,
                    energyLevel = energyLevel,
                    notes = notes,
                    lastModified = System.currentTimeMillis()
                )
                repository.upsertEntry(entry)
                if (date == LocalDate.now()) {
                    _todayEntry.value = entry
                }
                _formState.value = FormState.Success
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to add wellness entry")
            }
        }
    }

    // Update an existing wellness entry with all fields
    fun updateEntry(
        id: String,
        date: LocalDate,
        waterIntakeOz: Int,
        waterGoalOz: Int,
        sleepHours: Float,
        sleepGoalHours: Float,
        bedTimeMillis: Long,
        wakeupTimeMillis: Long,
        stepCount: Int,
        stepGoal: Int,
        moodRating: Int,
        energyLevel: Int,
        notes: String
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val current = _selectedEntry.value
                if (current != null) {
                    val updatedEntry = current.copy(
                        date = date,
                        waterIntakeOz = waterIntakeOz,
                        waterGoalOz = waterGoalOz,
                        sleepHours = sleepHours,
                        sleepGoalHours = sleepGoalHours,
                        bedTimeMillis = bedTimeMillis,
                        wakeupTimeMillis = wakeupTimeMillis,
                        stepCount = stepCount,
                        stepGoal = stepGoal,
                        moodRating = moodRating,
                        energyLevel = energyLevel,
                        notes = notes,
                        lastModified = System.currentTimeMillis()
                    )
                    repository.upsertEntry(updatedEntry)
                    if (date == LocalDate.now()) {
                        _todayEntry.value = updatedEntry
                    }
                    _formState.value = FormState.Success
                } else {
                    _formState.value = FormState.Error("No entry selected for update")
                }
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to update wellness entry")
            }
        }
    }

    // Update water intake
    fun updateWaterIntake(waterIntakeOz: Int) {
        viewModelScope.launch {
            try {
                val todayDate = LocalDate.now()
                val current = _todayEntry.value ?: repository.getEntryForDate(todayDate) ?: WellnessEntry(date = todayDate)
                
                val updatedEntry = current.copy(
                    waterIntakeOz = waterIntakeOz,
                    lastModified = System.currentTimeMillis()
                )
                
                repository.upsertEntry(updatedEntry)
                _todayEntry.value = updatedEntry
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    // Update step count for today
    fun updateStepCount(stepCount: Int) {
        viewModelScope.launch {
            try {
                val todayDate = LocalDate.now()
                val current = _todayEntry.value ?: repository.getEntryForDate(todayDate) ?: WellnessEntry(date = todayDate)
                
                val updatedEntry = current.copy(
                    stepCount = stepCount,
                    lastModified = System.currentTimeMillis()
                )
                
                repository.upsertEntry(updatedEntry)
                _todayEntry.value = updatedEntry
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    // Update sleep data for today
    fun updateSleepData(sleepHours: Float, bedTimeMillis: Long, wakeupTimeMillis: Long) {
        viewModelScope.launch {
            try {
                val todayDate = LocalDate.now()
                val current = _todayEntry.value ?: repository.getEntryForDate(todayDate) ?: WellnessEntry(date = todayDate)
                
                val updatedEntry = current.copy(
                    sleepHours = sleepHours,
                    bedTimeMillis = bedTimeMillis,
                    wakeupTimeMillis = wakeupTimeMillis,
                    lastModified = System.currentTimeMillis()
                )
                
                repository.upsertEntry(updatedEntry)
                _todayEntry.value = updatedEntry
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    // Delete a wellness entry
    fun deleteEntry(entry: WellnessEntry) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(entry)
                if (entry.id == _todayEntry.value?.id) {
                    _todayEntry.value = null
                }
                // Success will be reflected in the flow
            } catch (e: Exception) {
                _wellnessState.value = WellnessState.Error(e.message ?: "Failed to delete wellness entry")
            }
        }
    }

    // Get entries for a specific date range
    fun getEntriesForDateRange(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            try {
                _wellnessState.value = WellnessState.Loading
                repository.getEntriesForDateRange(startTime, endTime)
                    .catch { error ->
                        _wellnessState.value = WellnessState.Error(error.message ?: "Failed to load wellness data")
                    }
                    .collect { entries ->
                        _wellnessState.value = WellnessState.Success(entries)
                        generateRecommendations(entries)
                        checkAchievements(entries)
                    }
            } catch (e: Exception) {
                _wellnessState.value = WellnessState.Error(e.message ?: "Failed to load wellness data for date range")
            }
        }
    }

    // Select an entry for editing
    fun selectEntry(entry: WellnessEntry) {
        _selectedEntry.value = entry
    }

    // Clear the selected entry
    fun clearSelectedEntry() {
        _selectedEntry.value = null
    }

    // Reset form state
    fun resetFormState() {
        _formState.value = FormState.Idle
    }

    // Update step count with data from Google Fit
    private suspend fun updateStepCountFromGoogleFit() {
        try {
            // Get step count from Google Fit
            val stepCount = fitnessDataManager.getTodayStepCount()
            if (stepCount > 0) {
                updateStepCount(stepCount)
            }
        } catch (e: Exception) {
            // Handle silently - just won't update step count automatically
        }
    }
    
    // Refresh step data from Google Fit
    fun refreshStepData() {
        viewModelScope.launch {
            if (fitnessDataManager.hasOAuthPermission()) {
                updateStepCountFromGoogleFit()
            }
        }
    }

    // Update water intake goal
    fun updateWaterTarget(waterGoalOz: Int) {
        viewModelScope.launch {
            try {
                val todayDate = LocalDate.now()
                val current = _todayEntry.value ?: repository.getEntryForDate(todayDate) ?: WellnessEntry(date = todayDate)
                
                val updatedEntry = current.copy(
                    waterGoalOz = waterGoalOz,
                    lastModified = System.currentTimeMillis()
                )
                
                repository.upsertEntry(updatedEntry)
                _todayEntry.value = updatedEntry
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    // Update sleep goal
    fun updateSleepTarget(sleepGoalHours: Float) {
        viewModelScope.launch {
            try {
                val todayDate = LocalDate.now()
                val current = _todayEntry.value ?: repository.getEntryForDate(todayDate) ?: WellnessEntry(date = todayDate)
                
                val updatedEntry = current.copy(
                    sleepGoalHours = sleepGoalHours,
                    lastModified = System.currentTimeMillis()
                )
                
                repository.upsertEntry(updatedEntry)
                _todayEntry.value = updatedEntry
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }
} 