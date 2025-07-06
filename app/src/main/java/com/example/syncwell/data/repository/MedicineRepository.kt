package com.example.syncwell.data.repository

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
import com.example.syncwell.data.local.dao.MedicineDao
import com.example.syncwell.data.local.entities.Medicine

class MedicineRepository @Inject constructor(
    private val medicineDao: MedicineDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    private var firestoreListener: ListenerRegistration? = null

    // Safe version that returns empty list if user is not authenticated
    val medicines: Flow<List<Medicine>> = medicineDao.getMedicinesForUser(userId ?: "")
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
        firestoreListener = firestore.collection("medicines")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error (log, etc.)
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    for (doc in snapshots.documentChanges) {
                        try {
                            val med = doc.document.toObject(Medicine::class.java)
                            if (med != null) {
                                when (doc.type) {
                                    DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            medicineDao.insertMedicine(med)
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            medicineDao.deleteMedicine(med)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Log the error but continue processing other documents
                            // This prevents a single bad document from crashing the app
                        }
                    }
                }
            }
    }

    suspend fun upsertMedicine(medicine: Medicine) {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        val updatedMed = medicine.copy(lastModified = System.currentTimeMillis(), userId = uid)
        medicineDao.insertMedicine(updatedMed)
        try {
            firestore.collection("medicines")
                .document(updatedMed.id)
                .set(updatedMed)
                .await()
        } catch (e: Exception) {
            // Handle errors
        }
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        medicineDao.deleteMedicine(medicine)
        try {
            firestore.collection("medicines")
                .document(medicine.id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle errors
        }
    }

    // Get a specific medicine by ID
    suspend fun getMedicineById(medicineId: String): Medicine? {
        return try {
            firestore.collection("medicines")
                .document(medicineId)
                .get()
                .await()
                .toObject(Medicine::class.java)
        } catch (e: Exception) {
            // If network fails, try to get from local database
            null
        }
    }

    // Sync local database with Firestore (fetch all medicines)
    suspend fun syncWithFirestore() {
        val uid = userId ?: return // Don't proceed if not authenticated
        
        try {
            val querySnapshot = firestore.collection("medicines")
                .whereEqualTo("userId", uid)
                .get()
                .await()
                
            // Process documents individually to handle errors gracefully
            for (doc in querySnapshot.documents) {
                try {
                    val medicine = doc.toObject(Medicine::class.java)
                    if (medicine != null) {
                        medicineDao.insertMedicine(medicine)
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
    suspend fun refreshMedicines() {
        syncWithFirestore()
    }

    // Mark medicine as taken
    suspend fun markMedicineAsTaken(medicine: Medicine, taken: Boolean) {
        val updatedMedicine = medicine.copy(
            lastTaken = if (taken) System.currentTimeMillis() else medicine.lastTaken,
            lastModified = System.currentTimeMillis()
        )
        upsertMedicine(updatedMedicine)
    }

    // Update medicine dosage
    suspend fun updateDosage(medicine: Medicine, newDosage: String) {
        val updatedMedicine = medicine.copy(
            dosage = newDosage,
            lastModified = System.currentTimeMillis()
        )
        upsertMedicine(updatedMedicine)
    }

    // Get medicines that need to be taken soon (within the next hour)
    fun getMedicinesDueWithinHour(): Flow<List<Medicine>> {
        val uid = userId ?: return medicineDao.getMedicinesDueBefore("", 0) // Empty flow if not signed in
        
        val hourFromNow = System.currentTimeMillis() + 3600000 // 1 hour in milliseconds
        return medicineDao.getMedicinesDueBefore(uid, hourFromNow)
    }

    // Clear user data (e.g. on logout)
    suspend fun clearUserData() {
        val uid = userId ?: return
        medicineDao.deleteAllMedicinesForUser(uid)
    }
}