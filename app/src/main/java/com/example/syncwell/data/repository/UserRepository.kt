package com.example.syncwell.data.repository

import android.content.Intent
import com.example.syncwell.data.auth.AuthManager
import com.example.syncwell.data.sensors.FitnessDataManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val authManager: AuthManager,
    private val fitnessDataManager: FitnessDataManager
) {
    // Current Firebase user
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // User authentication state as a Flow
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        // Initial value
        trySend(auth.currentUser)
        // Remove the listener when the flow is canceled
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get sign-in intent for Google Sign-In
    fun getGoogleSignInIntent(): Intent {
        return authManager.getSignInIntent()
    }
    
    // Handle Google Sign-In result
    fun handleGoogleSignInResult(data: Intent?): Result<Unit> {
        return try {
            var result: Result<Unit> = Result.failure(Exception("Sign in not completed"))
            
            authManager.handleSignInResult(
                data,
                onSuccess = {
                    // Success case handled in callback
                    result = Result.success(Unit)
                },
                onFailure = { e ->
                    result = Result.failure(e)
                }
            )
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create a new account
    suspend fun createAccount(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            // Create the account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!

            // Update display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val userData = hashMapOf(
                "email" to email,
                "displayName" to displayName,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.uid).set(userData).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        authManager.signOut {
            // Reset fitness permission requested flag
            fitnessDataManager.resetPermissionRequested()
        }
    }

    // Update user profile
    suspend fun updateProfile(displayName: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("User not logged in"))
        
        return try {
            // Update Firebase Auth profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
            
            // Update in Firestore
            firestore.collection("users").document(user.uid)
                .update("displayName", displayName).await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reset password
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user data from Firestore
    suspend fun getUserData(): Result<Map<String, Any>> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("User not logged in"))
        
        return try {
            val document = firestore.collection("users").document(user.uid).get().await()
            if (document.exists()) {
                Result.success(document.data ?: emptyMap())
            } else {
                Result.failure(NoSuchElementException("User document not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 