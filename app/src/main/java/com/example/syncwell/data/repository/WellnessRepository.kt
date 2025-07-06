package com.example.syncwell.data.repository

import com.example.syncwell.data.local.dao.WellnessDao
import com.example.syncwell.data.local.entities.WellnessEntry
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
import org.threeten.bp.LocalDate
import javax.inject.Inject

class WellnessRepository @Inject constructor(
    private val wellnessDao: WellnessDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Get current user ID or null if not logged in
    private val userId: String?
        get() = auth.currentUser?.uid
    
    private var firestoreListener: ListenerRegistration? = null

    // Safe version that returns empty list if user is not authenticated
    val entries: Flow<List<WellnessEntry>> = wellnessDao.getEntriesForUser(userId ?: "")
        .flowOn(Dispatchers.IO)

    // For use by workers and other non-suspend contexts
    fun getCurrentUserId(): String? {
        return userId
    }
    
    // Check if an entry exists for a specific date
    suspend fun hasEntryForDate(date: LocalDate): Boolean {
        val uid = userId ?: return false
        
        // Convert LocalDate to timestamp range for that day (midnight to midnight)
        val startOfDay = date.atStartOfDay(org.threeten.bp.ZoneOffset.UTC).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(org.threeten.bp.ZoneOffset.UTC).toInstant().toEpochMilli() - 1
        
        return wellnessDao.getEntryCountForDay(uid, startOfDay, endOfDay) > 0
    }

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
        firestoreListener = firestore.collection("wellnessEntries")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error (log, etc.)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (doc in snapshots.documentChanges) {
                        try {
                            val entry = doc.document.toObject(WellnessEntry::class.java)
                            if (entry != null) {
                                when (doc.type) {
                                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            wellnessDao.insertEntry(entry)
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            wellnessDao.deleteEntry(entry)
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

    suspend fun upsertEntry(entry: WellnessEntry) {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        val updatedEntry = entry.copy(lastModified = System.currentTimeMillis(), userId = uid)
        wellnessDao.insertEntry(updatedEntry)
        try {
            firestore.collection("wellnessEntries")
                .document(updatedEntry.id)
                .set(updatedEntry)
                .await()
        } catch (e: Exception) {
            // Handle errors
        }
    }

    suspend fun deleteEntry(entry: WellnessEntry) {
        wellnessDao.deleteEntry(entry)
        try {
            firestore.collection("wellnessEntries")
                .document(entry.id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle errors
        }
    }

    // Get a specific entry by ID
    suspend fun getEntryById(entryId: String): WellnessEntry? {
        return try {
            firestore.collection("wellnessEntries")
                .document(entryId)
                .get()
                .await()
                .toObject(WellnessEntry::class.java)
        } catch (e: Exception) {
            // If network fails, try to get from local database
            null
        }
    }

    // Get an entry for a specific date
    suspend fun getEntryForDate(date: LocalDate): WellnessEntry? {
        val uid = userId ?: return null
        
        // Convert LocalDate to timestamp range for that day (midnight to midnight)
        val startOfDay = date.atStartOfDay(org.threeten.bp.ZoneOffset.UTC).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(org.threeten.bp.ZoneOffset.UTC).toInstant().toEpochMilli() - 1
        
        return wellnessDao.getEntryForDay(uid, startOfDay, endOfDay)
    }

    // Get entries for a specific date range
    fun getEntriesForDateRange(startTime: Long, endTime: Long): Flow<List<WellnessEntry>> {
        val uid = userId ?: return wellnessDao.getEntriesForDateRange("", 0, 0)
        return wellnessDao.getEntriesForDateRange(uid, startTime, endTime)
            .flowOn(Dispatchers.IO)
    }

    // Get entries by mood rating
    fun getEntriesByMoodRating(rating: Int): Flow<List<WellnessEntry>> {
        val uid = userId ?: return wellnessDao.getEntriesByMoodRating("", 0)
        return wellnessDao.getEntriesByMoodRating(uid, rating)
            .flowOn(Dispatchers.IO)
    }

    // Sync local database with Firestore
    suspend fun syncWithFirestore() {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        try {
            val querySnapshot = firestore.collection("wellnessEntries")
                .whereEqualTo("userId", uid)
                .get()
                .await()
                
            // Process documents individually to handle errors gracefully
            for (doc in querySnapshot.documents) {
                try {
                    val entry = doc.toObject(WellnessEntry::class.java)
                    if (entry != null) {
                        wellnessDao.insertEntry(entry)
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
    suspend fun refreshEntries() {
        syncWithFirestore()
    }

    // Get average mood rating for a time period
    suspend fun getAverageMoodForPeriod(startTime: Long, endTime: Long): Float {
        val uid = userId ?: return 0f
        return wellnessDao.getAverageMoodForPeriod(uid, startTime, endTime) ?: 0f
    }

    // Get wellness summary statistics
    suspend fun getWellnessSummary(startTime: Long, endTime: Long): WellnessSummary {
        val uid = userId ?: return WellnessSummary(0f, 0f, 0f, 0)
        
        val avgMood = wellnessDao.getAverageMoodForPeriod(uid, startTime, endTime) ?: 0f
        val avgSleep = wellnessDao.getAverageSleepForPeriod(uid, startTime, endTime) ?: 0f
        val avgEnergy = wellnessDao.getAverageEnergyForPeriod(uid, startTime, endTime) ?: 0f
        val entryCount = wellnessDao.getEntryCountForPeriod(uid, startTime, endTime)

        return WellnessSummary(
            averageMood = avgMood,
            averageSleep = avgSleep,
            averageEnergy = avgEnergy,
            entryCount = entryCount
        )
    }

    // Clear user data (e.g. on logout)
    suspend fun clearUserData() {
        val uid = userId ?: return
        wellnessDao.deleteAllEntriesForUser(uid)
    }

    // Data class for wellness summary
    data class WellnessSummary(
        val averageMood: Float,
        val averageSleep: Float,
        val averageEnergy: Float,
        val entryCount: Int
    )
}