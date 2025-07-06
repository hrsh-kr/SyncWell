package com.example.syncwell.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncwell.data.local.entities.Medicine
import com.example.syncwell.data.repository.MedicineRepository
import com.example.syncwell.notifications.MedicineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.threeten.bp.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val medicineAlarmScheduler: MedicineAlarmScheduler
) : ViewModel() {

    // UI state for medicine operations
    sealed class MedicineState {
        object Loading : MedicineState()
        data class Success(val medicines: List<Medicine>) : MedicineState()
        data class Error(val message: String) : MedicineState()
    }

    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        object Success : FormState()
        data class Error(val message: String) : FormState()
    }

    // Medicine categories for display
    enum class MedicineCategory(val displayName: String) {
        MORNING("Morning"),
        AFTERNOON("Afternoon"),
        EVENING("Night"),
        UNSPECIFIED("Unspecified")
    }

    // State flows
    private val _medicineState = MutableStateFlow<MedicineState>(MedicineState.Loading)
    val medicineState: StateFlow<MedicineState> = _medicineState.asStateFlow()

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    // Selected medicine for editing
    private val _selectedMedicine = MutableStateFlow<Medicine?>(null)
    val selectedMedicine: StateFlow<Medicine?> = _selectedMedicine.asStateFlow()

    init {
        loadMedicines()
    }

    // Load all medicines for the current user
    private fun loadMedicines() {
        viewModelScope.launch {
            try {
                _medicineState.value = MedicineState.Loading
                medicineRepository.medicines
                    .catch { error ->
                        _medicineState.value = MedicineState.Error(error.message ?: "Failed to load medicines")
                    }
                    .collect { medicines ->
                        _medicineState.value = MedicineState.Success(medicines)
                    }
            } catch (e: Exception) {
                _medicineState.value = MedicineState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    // Refresh medicines from remote
    fun refreshMedicines() {
        viewModelScope.launch {
            try {
                _medicineState.value = MedicineState.Loading
                medicineRepository.refreshMedicines()
                // The updated data will flow through the medicines flow
            } catch (e: Exception) {
                _medicineState.value = MedicineState.Error(e.message ?: "Failed to refresh medicines")
            }
        }
    }

    // Determine medicine category based on time
    private fun getCategoryFromTime(timeMillis: Long): String {
        val hour = LocalTime.ofNanoOfDay((timeMillis % (24 * 60 * 60 * 1000)) * 1000000).hour
        return when {
            hour in 5..11 -> MedicineCategory.MORNING.name
            hour in 12..17 -> MedicineCategory.AFTERNOON.name
            else -> MedicineCategory.EVENING.name
        }
    }

    // Add a new medicine with all fields
    fun addMedicine(
        name: String, 
        dosage: String, 
        timeMillis: Long,
        withFood: Boolean,
        durationDays: Int,
        startDate: Long,
        notes: String
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val category = getCategoryFromTime(timeMillis)
                val medicine = Medicine(
                    id = UUID.randomUUID().toString(),
                    userId = "",  // Will be set by repository
                    name = name,
                    dosage = dosage,
                    timeMillis = timeMillis,
                    withFood = withFood,
                    durationDays = durationDays,
                    startDate = startDate,
                    notes = notes,
                    category = category,
                    lastModified = System.currentTimeMillis()
                )
                medicineRepository.upsertMedicine(medicine)
                // Schedule an alarm for the new medicine
                medicineAlarmScheduler.scheduleMedicineAlarm(medicine)
                _formState.value = FormState.Success
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to add medicine")
            }
        }
    }

    // Update an existing medicine with all fields
    fun updateMedicine(
        id: String, 
        name: String, 
        dosage: String, 
        timeMillis: Long,
        withFood: Boolean,
        durationDays: Int,
        startDate: Long,
        notes: String
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val current = _selectedMedicine.value
                if (current != null) {
                    val category = getCategoryFromTime(timeMillis)
                    val updatedMedicine = current.copy(
                        name = name,
                        dosage = dosage,
                        timeMillis = timeMillis,
                        withFood = withFood,
                        durationDays = durationDays,
                        startDate = startDate,
                        notes = notes,
                        category = category,
                        lastModified = System.currentTimeMillis()
                    )
                    medicineRepository.upsertMedicine(updatedMedicine)
                    
                    // Reschedule the alarm with updated time
                    medicineAlarmScheduler.rescheduleMedicineAlarm(updatedMedicine)
                    
                    _formState.value = FormState.Success
                } else {
                    _formState.value = FormState.Error("No medicine selected for update")
                }
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to update medicine")
            }
        }
    }

    // Filter medicines by category
    fun getMedicinesByCategory(category: MedicineCategory): List<Medicine> {
        val currentState = _medicineState.value
        if (currentState is MedicineState.Success) {
            return currentState.medicines.filter { it.category == category.name }
        }
        return emptyList()
    }

    // Check if a medicine is active (within duration period)
    fun isMedicineActive(medicine: Medicine): Boolean {
        val now = System.currentTimeMillis()
        val endDate = medicine.startDate + (medicine.durationDays * 24 * 60 * 60 * 1000L)
        return medicine.startDate <= now && (medicine.durationDays == 0 || now <= endDate)
    }

    // Delete a medicine
    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                // Cancel the alarm for this medicine
                medicineAlarmScheduler.cancelMedicineAlarm(medicine)
                
                // Delete from repository
                medicineRepository.deleteMedicine(medicine)
                // Success will be reflected in the flow
            } catch (e: Exception) {
                // Error will be propagated through the flow
                _medicineState.value = MedicineState.Error(e.message ?: "Failed to delete medicine")
            }
        }
    }

    // Mark a medicine as taken
    fun takeMedicine(medicine: Medicine) {
        viewModelScope.launch {
            try {
                val updatedMedicine = medicine.copy(
                    lastTaken = System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis()
                )
                medicineRepository.upsertMedicine(updatedMedicine)
            } catch (e: Exception) {
                // Error will be propagated through the flow
                _medicineState.value = MedicineState.Error(e.message ?: "Failed to update medicine")
            }
        }
    }

    // Select a medicine for editing
    fun selectMedicine(medicine: Medicine) {
        _selectedMedicine.value = medicine
    }

    // Clear the selected medicine
    fun clearSelectedMedicine() {
        _selectedMedicine.value = null
    }

    // Reset form state
    fun resetFormState() {
        _formState.value = FormState.Idle
    }
} 