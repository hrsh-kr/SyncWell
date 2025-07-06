package com.example.syncwell.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncwell.data.local.entities.Task
import com.example.syncwell.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    // UI state for task operations
    sealed class TaskState {
        object Loading : TaskState()
        data class Success(val tasks: List<Task>) : TaskState()
        data class Error(val message: String) : TaskState()
    }

    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        object Success : FormState()
        data class Error(val message: String) : FormState()
    }

    // Task classification for Eisenhower matrix
    sealed class TaskCategory {
        object UrgentImportant : TaskCategory() // Red - Do first
        object UrgentNotImportant : TaskCategory() // Yellow - Schedule
        object NotUrgentImportant : TaskCategory() // Green - Delegate
        object NotUrgentNotImportant : TaskCategory() // Grey - Eliminate
    }

    // State flows
    private val _taskState = MutableStateFlow<TaskState>(TaskState.Loading)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    // Selected task for editing
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask.asStateFlow()

    init {
        loadTasks()
    }

    // Load all tasks for the current user
    private fun loadTasks() {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading
                repository.tasks
                    .catch { error ->
                        _taskState.value = TaskState.Error(error.message ?: "Failed to load tasks")
                    }
                    .collect { tasks ->
                        _taskState.value = TaskState.Success(tasks)
                    }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    // Refresh tasks from remote
    fun refreshTasks() {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading
                repository.refreshTasks()
                // The updated data will flow through the tasks flow
            } catch (e: Exception) {
                _taskState.value = TaskState.Error(e.message ?: "Failed to refresh tasks")
            }
        }
    }

    // Add a new task with all the new fields
    fun addTask(
        title: String, 
        description: String?, 
        deadlineMillis: Long,
        importance: Boolean,
        reminderEnabled: Boolean,
        reminderDaysBeforeDeadline: Int,
        reminderType: String,
        notes: String?
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val task = Task(
                    id = UUID.randomUUID().toString(),
                    userId = "",  // Will be set by repository
                    title = title,
                    description = description,
                    priority = if (importance) "HIGH" else "LOW", // For backward compatibility
                    deadlineMillis = deadlineMillis,
                    importance = importance,
                    reminderEnabled = reminderEnabled,
                    reminderDaysBeforeDeadline = reminderDaysBeforeDeadline,
                    reminderType = reminderType,
                    notes = notes,
                    lastModified = System.currentTimeMillis()
                )
                repository.upsertTask(task)
                _formState.value = FormState.Success
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to add task")
            }
        }
    }

    // Update an existing task with all fields
    fun updateTask(
        id: String, 
        title: String, 
        description: String?, 
        deadlineMillis: Long,
        importance: Boolean,
        reminderEnabled: Boolean,
        reminderDaysBeforeDeadline: Int,
        reminderType: String,
        notes: String?,
        completed: Boolean
    ) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            try {
                val current = _selectedTask.value
                if (current != null) {
                    val updatedTask = current.copy(
                        title = title,
                        description = description,
                        priority = if (importance) "HIGH" else "LOW", // For backward compatibility
                        deadlineMillis = deadlineMillis,
                        importance = importance,
                        reminderEnabled = reminderEnabled,
                        reminderDaysBeforeDeadline = reminderDaysBeforeDeadline,
                        reminderType = reminderType,
                        notes = notes,
                        completed = completed,
                        lastModified = System.currentTimeMillis()
                    )
                    repository.upsertTask(updatedTask)
                    _formState.value = FormState.Success
                } else {
                    _formState.value = FormState.Error("No task selected for update")
                }
            } catch (e: Exception) {
                _formState.value = FormState.Error(e.message ?: "Failed to update task")
            }
        }
    }

    // Classify task according to Eisenhower Matrix
    fun classifyTask(task: Task): TaskCategory {
        val now = System.currentTimeMillis()
        val isUrgent = task.deadlineMillis > 0 && 
            (task.deadlineMillis - now) <= URGENT_THRESHOLD
        
        return when {
            isUrgent && task.importance -> TaskCategory.UrgentImportant
            isUrgent && !task.importance -> TaskCategory.UrgentNotImportant
            !isUrgent && task.importance -> TaskCategory.NotUrgentImportant
            else -> TaskCategory.NotUrgentNotImportant
        }
    }

    // Delete a task
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
                // Success will be reflected in the flow
            } catch (e: Exception) {
                _taskState.value = TaskState.Error(e.message ?: "Failed to delete task")
            }
        }
    }

    // Toggle task completed status
    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            try {
                repository.completeTask(task, !task.completed)
                // The update will flow through the tasks flow
            } catch (e: Exception) {
                _taskState.value = TaskState.Error(e.message ?: "Failed to update task")
            }
        }
    }

    // Select a task for editing
    fun selectTask(task: Task) {
        _selectedTask.value = task
    }

    // Clear the selected task
    fun clearSelectedTask() {
        _selectedTask.value = null
    }

    // Reset form state
    fun resetFormState() {
        _formState.value = FormState.Idle
    }

    companion object {
        // 4 days in milliseconds - Tasks with deadline within 4 days are considered urgent
        private const val URGENT_THRESHOLD = 4 * 24 * 60 * 60 * 1000L
    }
}
