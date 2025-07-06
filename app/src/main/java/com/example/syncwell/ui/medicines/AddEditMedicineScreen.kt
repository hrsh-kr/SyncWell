package com.example.syncwell.ui.medicines

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.R
import com.example.syncwell.data.local.entities.Medicine
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellButton
import com.example.syncwell.ui.components.SyncWellTextField
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    medicineId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val selectedMedicine by viewModel.selectedMedicine.collectAsState()
    
    // Form state
    var medicineName by remember { mutableStateOf("") }
    var medicineDosage by remember { mutableStateOf("") }
    var medicineTime by remember { mutableStateOf(LocalTime.now()) }
    var withFood by remember { mutableStateOf(false) }
    var durationDays by remember { mutableStateOf(30) } // Default 30 days
    var startDate by remember { mutableStateOf(Calendar.getInstance()) }
    var notes by remember { mutableStateOf("") }
    
    // Effect to load existing medicine data
    LaunchedEffect(selectedMedicine) {
        selectedMedicine?.let { medicine ->
            medicineName = medicine.name
            medicineDosage = medicine.dosage
            
            // Convert timestamp to LocalTime
            val localTime = LocalTime.ofNanoOfDay((medicine.timeMillis % (24 * 60 * 60 * 1000)) * 1000000)
            medicineTime = localTime
            
            withFood = medicine.withFood
            durationDays = medicine.durationDays
            
            if (medicine.startDate > 0) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = medicine.startDate
                startDate = cal
            }
            
            notes = medicine.notes
        }
    }
    
    // Effect to load medicine by ID
    LaunchedEffect(medicineId) {
        if (medicineId != null) {
            viewModel.selectMedicine(Medicine(id = medicineId))
        } else {
            viewModel.clearSelectedMedicine()
        }
    }
    
    // Effect to handle form submission result
    LaunchedEffect(formState) {
        if (formState is MedicineViewModel.FormState.Success) {
            // Reset form state and navigate back
            viewModel.resetFormState()
            onNavigateBack()
        }
    }
    
    // Time picker state
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = if (medicineId == null) "Add Medicine" else "Edit Medicine",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form fields
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = { Text("Medicine Name") },
                placeholder = { Text("Enter medicine name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = medicineDosage,
                onValueChange = { medicineDosage = it },
                label = { Text("Dosage") },
                placeholder = { Text("E.g., 1 tablet, 5ml") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Time picker button
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Time: ${medicineTime.format(DateTimeFormatter.ofPattern("h:mm a"))}")
            }
            
            // Start date picker button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Start Date: ${startDate.time.toFormattedString()}")
            }
            
            // Duration in days
            Text(
                text = "Duration",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = durationDays.toFloat(),
                    onValueChange = { durationDays = it.toInt() },
                    valueRange = 1f..90f,
                    steps = 89,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = if (durationDays == 1) "1 day" else "$durationDays days",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // With food switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Take ${if (withFood) "after" else "before"} food",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Switch(
                    checked = withFood,
                    onCheckedChange = { withFood = it }
                )
            }
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Additional information") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button
            Button(
                onClick = {
                    val timeMillis = medicineTime.toNanoOfDay() / 1_000_000
                    
                    if (medicineId == null) {
                        // Add new medicine
                        viewModel.addMedicine(
                            name = medicineName,
                            dosage = medicineDosage,
                            timeMillis = timeMillis,
                            withFood = withFood,
                            durationDays = durationDays,
                            startDate = startDate.timeInMillis,
                            notes = notes
                        )
                    } else {
                        // Update existing medicine
                        viewModel.updateMedicine(
                            id = medicineId,
                            name = medicineName,
                            dosage = medicineDosage,
                            timeMillis = timeMillis,
                            withFood = withFood,
                            durationDays = durationDays,
                            startDate = startDate.timeInMillis,
                            notes = notes
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = medicineName.isNotBlank() && medicineDosage.isNotBlank() &&
                         formState !is MedicineViewModel.FormState.Loading
            ) {
                Text(if (medicineId == null) "Add Medicine" else "Update Medicine")
            }
            
            // Error message
            if (formState is MedicineViewModel.FormState.Error) {
                Text(
                    text = (formState as MedicineViewModel.FormState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Loading indicator
            if (formState is MedicineViewModel.FormState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Show time picker dialog

    if (showTimePicker) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                medicineTime = LocalTime.of(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialHour = medicineTime.hour,
            initialMinute = medicineTime.minute
        )
    }
    
    // Show date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onConfirm = { year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                startDate = cal
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialYear = startDate.get(Calendar.YEAR),
            initialMonth = startDate.get(Calendar.MONTH),
            initialDay = startDate.get(Calendar.DAY_OF_MONTH)
        )
    }
}

// Helper function to format Date as string
private fun Date.toFormattedString(): String {
    val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
    return formatter.format(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    // 1) remember your time state
    val timePickerState = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute
    )

    // 2) wrap the TimePicker in an AlertDialog
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton    = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        // this slot is typically called "text" in M3 AlertDialog
        text             = {
            // here’s your actual wheel/time‐picker
            TimePicker(state = timePickerState)
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onConfirm: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            set(initialYear, initialMonth, initialDay)
        }.timeInMillis
    )
    
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply { 
                        timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    }
                    onConfirm(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
} 