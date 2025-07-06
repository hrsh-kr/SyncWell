package com.example.syncwell.ui.wellness

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.R
import com.example.syncwell.data.auth.AuthManager
import com.example.syncwell.data.local.entities.WellnessEntry
import com.example.syncwell.data.repository.WellnessRepository
import com.example.syncwell.data.sensors.FitnessDataManager
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.UserViewModel
import com.example.syncwell.ui.viewmodel.WellnessViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEntry: () -> Unit = {},
    onNavigateToEditEntry: (String) -> Unit = {},
    wellnessViewModel: WellnessViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Get the activity and dependencies
    val context = LocalContext.current
    
    // Get the auth state
    val authState by userViewModel.authState.collectAsState()
    
    // Inject these dependencies
    val authManager = remember { (context.applicationContext as com.example.syncwell.SyncWellApp).authManager }
    val fitnessDataManager = remember { (context.applicationContext as com.example.syncwell.SyncWellApp).fitnessDataManager }
    
    // Check if we need to request fitness permissions
    val hasFitnessPermission = remember { mutableStateOf(fitnessDataManager.hasOAuthPermission()) }
    
    // Get states from ViewModel
    val entriesState by wellnessViewModel.wellnessState.collectAsState()
    val summaryState by wellnessViewModel.summaryState.collectAsState()
    val todayEntry by wellnessViewModel.todayEntry.collectAsState()
    
    // Dialog states
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showWaterTargetDialog by remember { mutableStateOf(false) }
    var showSleepTargetDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showSleepLogDialog by remember { mutableStateOf(false) }
    
    // Target values
    var waterTargetOz by remember { mutableIntStateOf(todayEntry?.waterGoalOz ?: 64) }
    var sleepTargetHours by remember { mutableFloatStateOf(todayEntry?.sleepGoalHours ?: 8f) }
    var waterReminderEnabled by remember { mutableStateOf(false) }
    
    // Sleep log values
    var sleepHours by remember { mutableFloatStateOf(todayEntry?.sleepHours ?: 0f) }
    var bedTime by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = todayEntry?.bedTimeMillis ?: 0 }) }
    var wakeTime by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = todayEntry?.wakeupTimeMillis ?: 0 }) }
    
    LaunchedEffect(authState) {
        // Remove fitness permission dialog and related code
        // Only refresh data when auth state changes to signed in
        if (authState is UserViewModel.AuthState.SignedIn) {
            wellnessViewModel.refreshEntries()
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val now = System.currentTimeMillis()
            wellnessViewModel.loadSummary(weekAgo, now)
        }
    }
    
    // Comment out permission request dialog
    /* 
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPermissionDialog = false
                fitnessDataManager.markPermissionRequested() // Mark as requested
            },
            title = { Text("Fitness Data Access") },
            text = { Text("Allow SyncWell to access your step count data to enable automatic tracking?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Request Google Fit permissions
                        try {
                            val account = authManager.getAccountForFitness(fitnessDataManager.fitnessOptions)
                            if (account != null) {
                                val activity = context as Activity
                                GoogleSignIn.requestPermissions(
                                    activity,
                                    FitnessDataManager.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                                    account,
                                    fitnessDataManager.fitnessOptions
                                )
                                fitnessDataManager.markPermissionRequested()
                            }
                        } catch (e: Exception) {
                            // Ignore errors
                        }
                        showPermissionDialog = false
                    }
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPermissionDialog = false
                        fitnessDataManager.markPermissionRequested() // Mark as requested even if denied
                    }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
    */
    
    // Water Target Dialog
    if (showWaterTargetDialog) {
        AlertDialog(
            onDismissRequest = { showWaterTargetDialog = false },
            title = { Text("Set Daily Water Target") },
            text = { 
                Column {
                    Text("Set your daily water intake target (oz):")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = waterTargetOz.toFloat(),
                        onValueChange = { waterTargetOz = it.toInt() },
                        valueRange = 32f..128f,
                        steps = 6
                    )
                    
                    Text(
                        text = "$waterTargetOz oz",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "≈ ${(waterTargetOz / 8)} cups",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update water target
                        wellnessViewModel.updateWaterTarget(waterTargetOz)
                        showWaterTargetDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Sleep Target Dialog
    if (showSleepTargetDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTargetDialog = false },
            title = { Text("Set Daily Sleep Target") },
            text = { 
                Column {
                    Text("Set your daily sleep hours target:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = sleepTargetHours,
                        onValueChange = { sleepTargetHours = it },
                        valueRange = 5f..10f,
                        steps = 10
                    )
                    
                    Text(
                        text = "$sleepTargetHours hours",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update sleep target
                        wellnessViewModel.updateSleepTarget(sleepTargetHours)
                        showSleepTargetDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Water Reminder Dialog
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Water Reminder Settings") },
            text = { 
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enable water reminders",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Switch(
                            checked = waterReminderEnabled,
                            onCheckedChange = { waterReminderEnabled = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Remind me to drink water every 3 hours",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Reminders will be sent between 8:00 AM and 8:00 PM",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Set up water reminder
                        if (waterReminderEnabled) {
                            // In a real app, this would configure the system's WorkManager
                            // or AlarmManager to schedule water reminders
                        } else {
                            // Cancel reminders
                        }
                        showReminderDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Sleep Log Dialog
    if (showSleepLogDialog) {
        AlertDialog(
            onDismissRequest = { showSleepLogDialog = false },
            title = { Text("Log Your Sleep") },
            text = { 
                Column {
                    Text("How many hours did you sleep?")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 0f..12f,
                        steps = 24
                    )
                    
                    Text(
                        text = "$sleepHours hours",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update sleep data
                        wellnessViewModel.updateSleepData(
                            sleepHours = sleepHours,
                            bedTimeMillis = bedTime.timeInMillis,
                            wakeupTimeMillis = wakeTime.timeInMillis
                        )
                        showSleepLogDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepLogDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = stringResource(R.string.wellness_notification_title),
                onBackClick = onNavigateBack,
                contentDescription = stringResource(R.string.accessibility_back),
                actions = {
                    IconButton(
                        onClick = { wellnessViewModel.refreshEntries() },
                        modifier = Modifier.semantics {
                            this.contentDescription = "Refresh wellness data"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics {
                    this.contentDescription = "Add new wellness entry"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        // Display content regardless of loading state to prevent the infinite loading screen
        val entries = if (entriesState is WellnessViewModel.WellnessState.Success) {
            (entriesState as WellnessViewModel.WellnessState.Success).entries
        } else {
            emptyList()
        }
        
        val summary = if (summaryState is WellnessViewModel.SummaryState.Success) {
                        (summaryState as WellnessViewModel.SummaryState.Success).summary
                    } else {
                        // Provide a default summary if not loaded yet
                        WellnessRepository.WellnessSummary(0f, 0f, 0f, 0)
        }
        
        SimplifiedWellnessContent(
            padding = innerPadding,
            todayEntry = todayEntry,
            onSetWaterTarget = { showWaterTargetDialog = true },
            onSetSleepTarget = { showSleepTargetDialog = true },
            onConfigureReminders = { showReminderDialog = true }
        )
        
        // Show error message if there's an error
        if (entriesState is WellnessViewModel.WellnessState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (entriesState as WellnessViewModel.WellnessState.Error).message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        IconButton(onClick = { wellnessViewModel.refreshEntries() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimplifiedWellnessContent(
    padding: PaddingValues,
    todayEntry: WellnessEntry?,
    onSetWaterTarget: () -> Unit,
    onSetSleepTarget: () -> Unit,
    onConfigureReminders: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Daily Targets Section
            DailyTargetsCard(
                todayEntry = todayEntry,
                onSetWaterTarget = onSetWaterTarget,
                onSetSleepTarget = onSetSleepTarget,
                onConfigureReminders = onConfigureReminders
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Text(
            text = "Tap + to add today's wellness data",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        }
    }
}

@Composable
private fun DailyTargetsCard(
    todayEntry: WellnessEntry?,
    onSetWaterTarget: () -> Unit,
    onSetSleepTarget: () -> Unit,
    onConfigureReminders: () -> Unit
) {
    val wellnessViewModel: WellnessViewModel = hiltViewModel()
    var showSleepLogDialog by remember { mutableStateOf(false) }
    
    // Sleep log values
    var sleepHours by remember { mutableFloatStateOf(todayEntry?.sleepHours ?: 0f) }
    var bedTime by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = todayEntry?.bedTimeMillis ?: 0 }) }
    var wakeTime by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = todayEntry?.wakeupTimeMillis ?: 0 }) }

    // Sleep Log Dialog
    if (showSleepLogDialog) {
        AlertDialog(
            onDismissRequest = { showSleepLogDialog = false },
            title = { Text("Log Your Sleep") },
            text = { 
                Column {
                    Text("How many hours did you sleep?")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 0f..12f,
                        steps = 24
                    )
                    
                    Text(
                        text = "$sleepHours hours",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Update sleep data
                        wellnessViewModel.updateSleepData(
                            sleepHours = sleepHours,
                            bedTimeMillis = bedTime.timeInMillis,
                            wakeupTimeMillis = wakeTime.timeInMillis
                        )
                        showSleepLogDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepLogDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Daily Targets & Reminders",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            VerticalSpacer(height = 16)
            
            // Water Target and Tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
        Text(
                        text = "Water Target",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
        )
        
        Text(
                        text = "${todayEntry?.waterIntakeOz ?: 0}/${todayEntry?.waterGoalOz ?: 64} oz",
            style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (todayEntry?.waterIntakeOz?.toFloat() ?: 0f) / (todayEntry?.waterGoalOz?.toFloat() ?: 64f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onSetWaterTarget) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit water target",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Quick water input
            Row(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(start = 36.dp, top = 8.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WaterQuickAddButton(
                    ounces = 8,
                    onClick = { 
                        val currentValue = todayEntry?.waterIntakeOz ?: 0
                        wellnessViewModel.updateWaterIntake(currentValue + 8) 
                    },
                    wellnessViewModel = wellnessViewModel
                )
                WaterQuickAddButton(
                    ounces = 16,
                    onClick = { 
                        val currentValue = todayEntry?.waterIntakeOz ?: 0
                        wellnessViewModel.updateWaterIntake(currentValue + 16) 
                    },
                    wellnessViewModel = wellnessViewModel
                )
                WaterQuickAddButton(
                    ounces = 24,
                    onClick = { 
                        val currentValue = todayEntry?.waterIntakeOz ?: 0
                        wellnessViewModel.updateWaterIntake(currentValue + 24) 
                    },
                    wellnessViewModel = wellnessViewModel
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
            )
            
            // Sleep Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sleep Target",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${todayEntry?.sleepHours ?: 0}/${todayEntry?.sleepGoalHours ?: 8} hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (todayEntry?.sleepHours ?: 0f) / (todayEntry?.sleepGoalHours ?: 8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row {
                    IconButton(onClick = { showSleepLogDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Log sleep",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onSetSleepTarget) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit sleep target",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
            )
            
            // Reminders
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = "Water Reminders",
                    style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                )
                    
                Text(
                        text = "Remind every 3 hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(onClick = onConfigureReminders) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure reminders",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterQuickAddButton(
    ounces: Int,
    onClick: () -> Unit,
    wellnessViewModel: WellnessViewModel
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "+$ounces oz",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

// Helper functions for date/time formatting
private fun formatDate(timeMillis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timeMillis))
} 