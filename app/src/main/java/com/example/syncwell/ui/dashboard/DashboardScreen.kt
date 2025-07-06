package com.example.syncwell.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncwell.R
import com.example.syncwell.data.local.entities.Medicine
import com.example.syncwell.data.local.entities.Task
import com.example.syncwell.data.local.entities.WellnessEntry
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellCard
import com.example.syncwell.ui.components.SyncWellCardWithPainter
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.theme.AccentBlue
import com.example.syncwell.ui.theme.CardBackground
import com.example.syncwell.ui.theme.DarkBackground
import com.example.syncwell.ui.theme.DarkOnSurface
import com.example.syncwell.ui.theme.DarkPrimary
import com.example.syncwell.ui.theme.Divider
import com.example.syncwell.ui.theme.Primary
import com.example.syncwell.ui.theme.Secondary
import com.example.syncwell.ui.viewmodel.MedicineViewModel
import com.example.syncwell.ui.viewmodel.TaskViewModel
import com.example.syncwell.ui.viewmodel.UserViewModel
import com.example.syncwell.ui.viewmodel.WellnessViewModel
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToMedicines: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWellness: () -> Unit,
    onNavigateToProfile: () -> Unit,
    userViewModel: UserViewModel,
    taskViewModel: TaskViewModel,
    medicineViewModel: MedicineViewModel,
    wellnessViewModel: WellnessViewModel
) {
    val authState by userViewModel.authState.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    
    // State for FAB menu
    var showFabMenu by remember { mutableStateOf(false) }
    
    when (authState) {
        is UserViewModel.AuthState.Loading -> {
            FullScreenLoading()
        }
        is UserViewModel.AuthState.SignedIn -> {
            val user = (authState as UserViewModel.AuthState.SignedIn).user
            
            Scaffold(
                containerColor = DarkBackground,
                topBar = {
                    // Clean empty top bar with just background color for status bar area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(DarkBackground)
                    )
                },
                floatingActionButton = {
                    if (showFabMenu) {
                        ModernFabMenu(
                            onAddMedicine = {
                                showFabMenu = false
                                onNavigateToMedicines()
                            },
                            onAddTask = {
                                showFabMenu = false
                                onNavigateToTasks()
                            },
                            onAddWellness = {
                                showFabMenu = false
                                onNavigateToWellness()
                            },
                            onClose = { showFabMenu = false }
                        )
                    } else {
                        FloatingActionButton(
                            onClick = { showFabMenu = true },
                            containerColor = DarkPrimary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                DashboardContent(
                    padding = innerPadding,
                    displayName = user.displayName ?: "User",
                    onNavigateToMedicines = onNavigateToMedicines,
                    onNavigateToTasks = onNavigateToTasks,
                    onNavigateToWellness = onNavigateToWellness,
                    onNavigateToProfile = onNavigateToProfile,
                    taskViewModel = taskViewModel,
                    medicineViewModel = medicineViewModel,
                    wellnessViewModel = wellnessViewModel
                )
            }
        }
        else -> {
            // Handled by navigation
        }
    }
}

@Composable
private fun DashboardContent(
    padding: PaddingValues,
    displayName: String,
    onNavigateToMedicines: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWellness: () -> Unit,
    onNavigateToProfile: () -> Unit,
    taskViewModel: TaskViewModel,
    medicineViewModel: MedicineViewModel,
    wellnessViewModel: WellnessViewModel
) {
    // State for the currently selected tab
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // State from view models
    val taskState by taskViewModel.taskState.collectAsState()
    val medicineState by medicineViewModel.medicineState.collectAsState()
    val wellnessState by wellnessViewModel.wellnessState.collectAsState()
    val todayEntry by wellnessViewModel.todayEntry.collectAsState()
    
    // Get tasks and medicines from state
    val tasks = if (taskState is TaskViewModel.TaskState.Success) {
        (taskState as TaskViewModel.TaskState.Success).tasks
    } else {
        emptyList()
    }
    
    val medicines = if (medicineState is MedicineViewModel.MedicineState.Success) {
        (medicineState as MedicineViewModel.MedicineState.Success).medicines
    } else {
        emptyList()
    }
    
    // For demo purposes, calculate completion percentages
    val tasksCompletedPercentage = if (tasks.isNotEmpty()) {
        (tasks.count { it.completed } / tasks.size.toFloat()) * 100
    } else 0f
    
    val medicinesCompletedPercentage = if (medicines.isNotEmpty()) {
        // For demo, just show a random percentage
        63f
    } else 0f
    
    val waterProgress = (todayEntry?.waterIntakeOz ?: 0) / (todayEntry?.waterGoalOz?.toFloat() ?: 64f)
    val sleepProgress = (todayEntry?.sleepHours ?: 0f) / (todayEntry?.sleepGoalHours?.toFloat() ?: 8f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // User greeting
            UserHeader(
                displayName = displayName,
                onProfileClick = onNavigateToProfile
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main progress metrics in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Sleep circular progress
                CircularProgressMetric(
                    value = sleepProgress,
                    maxValue = 1f,
                    title = stringResource(R.string.dashboard_sleep_title),
                    mainText = stringResource(R.string.dashboard_sleep_hours, todayEntry?.sleepHours ?: 0),
                    subtitle = stringResource(R.string.dashboard_sleep_goal, todayEntry?.sleepGoalHours ?: 8),
                    progressColor = Secondary
                )
                
                // Water circular progress
                CircularProgressMetric(
                    value = waterProgress,
                    maxValue = 1f,
                    title = stringResource(R.string.dashboard_water_title),
                    mainText = stringResource(R.string.dashboard_water_amount, todayEntry?.waterIntakeOz ?: 0),
                    subtitle = stringResource(R.string.dashboard_water_goal, todayEntry?.waterGoalOz ?: 64),
                    progressColor = AccentBlue
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Today's activities card
            DashboardCard(
                title = stringResource(R.string.dashboard_activities),
                content = {
                    Column {
                        TodayActivityItem(
                            title = stringResource(R.string.dashboard_tasks),
                            icon = Icons.Default.Task,
                            progress = tasksCompletedPercentage / 100f,
                            progressText = "${tasks.count { it.completed }}/${tasks.size}",
                            progressColor = DarkPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TodayActivityItem(
                            title = stringResource(R.string.dashboard_medications),
                            icon = Icons.Default.Medication,
                            progress = medicinesCompletedPercentage / 100f,
                            progressText = "${(medicinesCompletedPercentage * medicines.size / 100f).toInt()}/${medicines.size}",
                            progressColor = Secondary
                        )
                    }
                },
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.Task,
                    text = stringResource(R.string.dashboard_tasks),
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f),
                    color = DarkPrimary
                )
                
                ActionButton(
                    icon = Icons.Default.Medication,
                    text = stringResource(R.string.dashboard_medications),
                    onClick = onNavigateToMedicines,
                    modifier = Modifier.weight(1f),
                    color = Secondary
                )
                
                ActionButton(
                    icon = Icons.Default.Favorite,
                    text = stringResource(R.string.dashboard_wellness),
                    onClick = onNavigateToWellness,
                    modifier = Modifier.weight(1f),
                    color = AccentBlue
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Upcoming tasks/medications
            DashboardCard(
                title = stringResource(R.string.dashboard_coming_up),
                content = {
                    Column {
                        // Show next few tasks or medications
                        val upcomingTasks = tasks.filter { !it.completed }.take(3)
                        val upcomingMeds = medicines.take(2)
                        
                        if (upcomingTasks.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.dashboard_tasks),
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkOnSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            upcomingTasks.forEach { task ->
                                UpcomingItem(
                                    title = task.title,
                                    time = "Today",
                                    icon = Icons.Default.Task,
                                    color = DarkPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        if (upcomingMeds.isNotEmpty()) {
                            if (upcomingTasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Divider.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            Text(
                                text = stringResource(R.string.dashboard_medications),
                                style = MaterialTheme.typography.titleSmall,
                                color = DarkOnSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            upcomingMeds.forEach { med ->
                                UpcomingItem(
                                    title = med.name,
                                    time = formatTime(med.timeMillis),
                                    icon = Icons.Default.Medication,
                                    color = Secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                },
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserHeader(
    displayName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_greeting, displayName),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(
                    R.string.dashboard_today, 
                    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF4D2B56))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = DarkPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun CircularProgressMetric(
    value: Float,
    maxValue: Float,
    title: String,
    mainText: String,
    subtitle: String,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clickable { /* Future enhancement could navigate to detailed view */ }
        ) {
            // Track (background)
            Canvas(modifier = Modifier.size(120.dp)) {
                drawArc(
                    color = Color.White.copy(alpha = 0.1f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }
            
            // Progress
            Canvas(modifier = Modifier.size(120.dp)) {
                drawArc(
                    color = progressColor,
                    startAngle = 270f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }
            
            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                // Display percentage
                Text(
                    text = stringResource(R.string.dashboard_percentage, (progress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    content: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun TodayActivityItem(
    title: String,
    icon: ImageVector,
    progress: Float,
    progressText: String,
    progressColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(progressColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = progressColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Custom progress bar
                Box(
                    modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                    Box(
                        modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressColor)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = progressText,
            color = progressColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
        Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = color,
            modifier = Modifier.size(24.dp)
        )
            }
        
            Spacer(modifier = Modifier.height(8.dp))
        
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WeeklyProgressChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1f
    
        Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val barColor = when (index) {
                    data.size - 1 -> DarkPrimary // Today
                    else -> Secondary.copy(alpha = 0.5f + (value / maxValue) * 0.5f)
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar
            Box(
                modifier = Modifier
                            .width(24.dp)
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        // Background bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        
                        // Value bar
                        Box(
            modifier = Modifier
                .fillMaxWidth()
                                .fillMaxHeight(value / maxValue)
                                .clip(RoundedCornerShape(12.dp))
                                .background(barColor)
                                .align(Alignment.BottomCenter)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Label
                Text(
                        text = labels[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index == data.size - 1) DarkPrimary else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingItem(
    title: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
                        Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
                        Text(
                text = time,
                color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View details",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ModernFabMenu(
    onAddMedicine: () -> Unit,
    onAddTask: () -> Unit,
    onAddWellness: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Close button
        FloatingActionButton(
            onClick = onClose,
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Task button
        ModernFabAction(
            icon = Icons.Default.Task,
            label = "Add Task",
            color = Secondary,
            onClick = onAddTask
        )
        
        // Medicine button
        ModernFabAction(
            icon = Icons.Default.Medication,
            label = "Add Medicine",
            color = AccentBlue,
            onClick = onAddMedicine
        )
        
        // Wellness button
        ModernFabAction(
            icon = Icons.Default.Favorite,
            label = "Add Wellness",
            color = DarkPrimary,
            onClick = onAddWellness
        )
    }
}

@Composable
private fun ModernFabAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(CardBackground)
            .padding(8.dp, 8.dp, 16.dp, 8.dp)
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
            
            Text(
            text = label,
            color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
    }
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
            // here's your actual wheel/time‐picker
            TimePicker(state = timePickerState)
        }
    )
}

/**
 * Formats a timestamp into a readable time string (e.g., "8:30 AM")
 */
private fun formatTime(timeMillis: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}
