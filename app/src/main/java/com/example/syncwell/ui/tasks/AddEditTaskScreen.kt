package com.example.syncwell.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.data.local.entities.Task
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.SyncWellButton
import com.example.syncwell.ui.components.SyncWellTextField
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.TaskViewModel
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: String?,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val selectedTask by viewModel.selectedTask.collectAsState()
    
    // Form state
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var deadlineDate by remember { mutableStateOf(Calendar.getInstance()) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderDays by remember { mutableStateOf(1) }
    var reminderType by remember { mutableStateOf("ONCE") }
    var notes by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Load existing task data
    LaunchedEffect(selectedTask) {
        selectedTask?.let { task ->
            taskTitle = task.title
            taskDescription = task.description ?: ""
            isImportant = task.importance
            isCompleted = task.completed
            notes = task.notes ?: ""
            
            if (task.deadlineMillis > 0) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = task.deadlineMillis
                deadlineDate = cal
            }
            
            reminderEnabled = task.reminderEnabled
            reminderDays = task.reminderDaysBeforeDeadline
            reminderType = task.reminderType
        }
    }
    
    // Load task by ID
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.selectTask(Task(id = taskId))
        } else {
            viewModel.clearSelectedTask()
        }
    }
    
    // Handle form submission result
    LaunchedEffect(formState) {
        if (formState is TaskViewModel.FormState.Success) {
            viewModel.resetFormState()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = if (taskId == null) "Add Task" else "Edit Task",
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
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                placeholder = { Text("Enter task title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text("Description") },
                placeholder = { Text("Enter task description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // Deadline date and time picker
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Deadline: ${deadlineDate.time.toFormattedString()}")
            }
            
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
                Text("Time: ${formatTime(deadlineDate)}")
            }
            
            // Importance switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mark as Important",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Switch(
                    checked = isImportant,
                    onCheckedChange = { isImportant = it }
                )
            }
            
            // Reminder settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Enable Reminders",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }
                    
                    if (reminderEnabled) {
                        // Days before deadline
                        Text(
                            text = "Remind me ${reminderDays} days before deadline",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Slider(
                            value = reminderDays.toFloat(),
                            onValueChange = { reminderDays = it.toInt() },
                            valueRange = 1f..7f,
                            steps = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Reminder type
                        Text(
                            text = "Reminder frequency",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderTypeChip(
                                text = "Once",
                                selected = reminderType == "ONCE",
                                onClick = { reminderType = "ONCE" }
                            )
                            
                            ReminderTypeChip(
                                text = "Daily",
                                selected = reminderType == "DAILY",
                                onClick = { reminderType = "DAILY" }
                            )
                        }
                    }
                }
            }
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                placeholder = { Text("Additional information") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            // Completed checkbox (for edit mode)
            if (taskId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Mark as completed",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button
            Button(
                onClick = {
                    if (taskId == null) {
                        // Add new task
                        viewModel.addTask(
                            title = taskTitle,
                            description = taskDescription,
                            deadlineMillis = deadlineDate.timeInMillis,
                            importance = isImportant,
                            reminderEnabled = reminderEnabled,
                            reminderDaysBeforeDeadline = reminderDays,
                            reminderType = reminderType,
                            notes = notes
                        )
                    } else {
                        // Update existing task
                        viewModel.updateTask(
                            id = taskId,
                            title = taskTitle,
                            description = taskDescription,
                            deadlineMillis = deadlineDate.timeInMillis,
                            importance = isImportant,
                            reminderEnabled = reminderEnabled,
                            reminderDaysBeforeDeadline = reminderDays,
                            reminderType = reminderType,
                            notes = notes,
                            completed = isCompleted
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = taskTitle.isNotBlank() && formState !is TaskViewModel.FormState.Loading
            ) {
                Text(if (taskId == null) "Add Task" else "Update Task")
            }
            
            // Error message
            if (formState is TaskViewModel.FormState.Error) {
                Text(
                    text = (formState as TaskViewModel.FormState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Loading indicator
            if (formState is TaskViewModel.FormState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Show date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onConfirm = { year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                // Preserve time
                cal.set(Calendar.HOUR_OF_DAY, deadlineDate.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, deadlineDate.get(Calendar.MINUTE))
                deadlineDate = cal
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialYear = deadlineDate.get(Calendar.YEAR),
            initialMonth = deadlineDate.get(Calendar.MONTH),
            initialDay = deadlineDate.get(Calendar.DAY_OF_MONTH)
        )
    }
    
    // Show time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, deadlineDate.get(Calendar.YEAR))
                cal.set(Calendar.MONTH, deadlineDate.get(Calendar.MONTH))
                cal.set(Calendar.DAY_OF_MONTH, deadlineDate.get(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                deadlineDate = cal
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialHour = deadlineDate.get(Calendar.HOUR_OF_DAY),
            initialMinute = deadlineDate.get(Calendar.MINUTE)
        )
    }
}

@Composable
private fun ReminderTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// Helper function to format time
private fun formatTime(calendar: Calendar): String {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val minuteStr = if (minute < 10) "0$minute" else "$minute"
    return "$hour12:$minuteStr $amPm"
}

// Helper function to format Date as string
private fun Date.toFormattedString(): String {
    val formatter = java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.getDefault())
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