package com.example.syncwell.ui.tasks

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.data.local.entities.Task
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val taskState by taskViewModel.taskState.collectAsState()
    
    // Refresh tasks when screen loads
    LaunchedEffect(Unit) {
        taskViewModel.refreshTasks()
    }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = "Tasks",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { taskViewModel.refreshTasks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        when (taskState) {
            is TaskViewModel.TaskState.Loading -> {
                FullScreenLoading()
            }
            is TaskViewModel.TaskState.Success -> {
                val tasks = (taskState as TaskViewModel.TaskState.Success).tasks
                if (tasks.isEmpty()) {
                    EmptyTaskList(padding = innerPadding)
                } else {
                    TaskList(
                        padding = innerPadding,
                        tasks = tasks,
                        onTaskChecked = { task, checked ->
                            taskViewModel.toggleTaskCompleted(task)
                        },
                        onEditTask = { 
                            taskViewModel.selectTask(it)
                            onNavigateToEditTask(it.id)
                        },
                        onDeleteTask = { taskViewModel.deleteTask(it) }
                    )
                }
            }
            is TaskViewModel.TaskState.Error -> {
                ErrorView(
                    message = (taskState as TaskViewModel.TaskState.Error).message,
                    padding = innerPadding,
                    onRetry = { taskViewModel.refreshTasks() }
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    padding: PaddingValues,
    tasks: List<Task>,
    onTaskChecked: (Task, Boolean) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onTaskChecked = { checked -> onTaskChecked(task, checked) },
                onEditTask = { onEditTask(task) },
                onDeleteTask = { onDeleteTask(task) }
            )
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onTaskChecked: (Boolean) -> Unit,
    onEditTask: () -> Unit,
    onDeleteTask: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (task.completed) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = onTaskChecked,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.completed) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    task.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (task.completed) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Format and show deadline if exists
                    task.deadlineMillis?.let { deadline ->
                        if (deadline > 0) {
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                            val formattedDate = dateFormat.format(Date(deadline))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (task.completed) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                                    else 
                                        MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (task.completed) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Task actions
                Row {
                    IconButton(
                        onClick = onEditTask,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDeleteTask,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Show priority and category if exists
            if (task.importance) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityChip(isImportant = true)
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(isImportant: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isImportant) 
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f) 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isImportant) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = if (isImportant) "Important" else "Normal",
                style = MaterialTheme.typography.labelSmall,
                color = if (isImportant) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTaskList(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap the + button to add your first task",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    padding: PaddingValues,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorMessage(message = message)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IconButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// Helper functions for date formatting
private fun formatDate(timeMillis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timeMillis))
} 