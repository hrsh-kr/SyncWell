package com.example.syncwell.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.syncwell.data.sensors.FitnessDataManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "AuthManager"
        const val RC_GOOGLE_SIGN_IN = 9001
    }

    // Google Sign-In client
    private val googleSignInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // TODO: Replace with your web client ID from Google Developer Console
            .requestEmail()
            .build()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // Check if user is already signed in
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    // Get sign-in intent for activity result
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Process sign-in result
    fun handleSignInResult(data: Intent?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account, onSuccess, onFailure)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            onFailure(e)
        }
    }

    // Sign out from both Firebase and Google
    fun signOut(onComplete: () -> Unit = {}) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    // Check if we have fitness permissions
    fun hasFitnessPermission(fitnessOptions: FitnessOptions): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    // Get fitness account
    fun getAccountForFitness(fitnessOptions: FitnessOptions): GoogleSignInAccount? {
        return try {
            GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting account for fitness", e)
            null
        }
    }

    // Auth with Firebase using Google credentials
    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    onSuccess()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    onFailure(task.exception ?: Exception("Unknown error during authentication"))
                }
            }
    }
} 