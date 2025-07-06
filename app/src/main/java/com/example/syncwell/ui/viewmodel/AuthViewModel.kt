package com.example.syncwell.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Represents the UI state of an authentication request.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val exception: Exception) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    // 1) Expose the raw FirebaseUser? (initially whatever FirebaseAuth has)
    private val _user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    // 2) Expose an AuthState to drive UI (loading / success / error)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // 3) Keep `_user` in sync whenever Auth state changes behind the scenes
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
    }

    /**
     * Attempt to sign in.
     */
    fun loginEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                // Await the Firebase Task
                auth.signInWithEmailAndPassword(email, password)
                    .await()

                // On success, currentUser is non-null
                auth.currentUser?.let { user ->
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    // This really shouldn't happen, but we guard it
                    _authState.value =
                        AuthState.Error(IllegalStateException("User was null after login"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e)
            }
        }
    }

    /**
     * Attempt to create a new account.
     */
    fun signUpEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .await()

                auth.currentUser?.let { user ->
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    _authState.value =
                        AuthState.Error(IllegalStateException("User was null after signup"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e)
            }
        }
    }

    /**
     * Sign out immediately.
     */
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _user.value = null
    }
}
