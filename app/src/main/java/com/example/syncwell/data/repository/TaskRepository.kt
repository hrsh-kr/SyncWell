package com.example.syncwell.data.repository

import com.example.syncwell.data.local.dao.TaskDao
import com.example.syncwell.data.local.entities.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get current user ID or null if not logged in
    private val userId: String?
        get() = auth.currentUser?.uid
    
    private var firestoreListener: ListenerRegistration? = null

    // Expose a Flow of tasks from Room (local cache)
    val tasks: Flow<List<Task>> = taskDao.getTasksForUser(userId ?: "")
        .flowOn(Dispatchers.IO)

    init {
        // Set up an auth state listener to respond to sign-in/sign-out events
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, attach the Firestore listener
                setupFirestoreListener(user)
            } else {
                // User is signed out, remove the listener
                firestoreListener?.remove()
                firestoreListener = null
            }
        }
    }
    
    private fun setupFirestoreListener(user: FirebaseUser) {
        // Remove any existing listener
        firestoreListener?.remove()
        
        // Create new listener
        firestore.collection("tasks")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error (log, etc.)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (doc in snapshots.documentChanges) {
                        try {
                            val task = doc.document.toObject(Task::class.java)
                            if (task != null) {
                                when (doc.type) {
                                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            taskDao.insertTask(task)
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            taskDao.deleteTask(task)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Handle deserialization errors
                        }
                    }
                }
            }
    }

    // Add or update a task: write to Room then to Firestore
    suspend fun upsertTask(task: Task) {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        val updatedTask = task.copy(lastModified = System.currentTimeMillis(), userId = uid)
        taskDao.insertTask(updatedTask)  // write to local DB first
        try {
            firestore.collection("tasks")
                .document(updatedTask.id)
                .set(updatedTask)
                .await()  // KTX suspend
        } catch (e: Exception) {
            // Handle network errors (e.g. by logging or retry logic)
        }
    }

    // Delete a task: remove from Room and Firestore
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        try {
            firestore.collection("tasks")
                .document(task.id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle errors
        }
    }

    // Sync local database with Firestore (fetch all tasks)
    suspend fun syncWithFirestore() {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        try {
            val querySnapshot = firestore.collection("tasks")
                .whereEqualTo("userId", uid)
                .get()
                .await()
                
            // Process documents individually to handle errors gracefully
            for (doc in querySnapshot.documents) {
                try {
                    val task = doc.toObject(Task::class.java)
                    if (task != null) {
                        taskDao.insertTask(task)
                    }
                } catch (e: Exception) {
                    // Log error but continue processing other documents
                }
            }
        } catch (e: Exception) {
            // Handle general sync errors
        }
    }

    // Force refresh from remote
    suspend fun refreshTasks() {
        syncWithFirestore()
    }

    // Mark task as completed
    suspend fun completeTask(task: Task, isCompleted: Boolean) {
        val updatedTask = task.copy(completed = isCompleted, lastModified = System.currentTimeMillis())
        upsertTask(updatedTask)
    }

    // Clear user data (e.g. on logout)
    suspend fun clearUserData() {
        val uid = userId ?: return
        taskDao.deleteAllTasksForUser(uid)
    }
}