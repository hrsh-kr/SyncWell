package com.example.syncwell.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncwell.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // UI state for authentication
    sealed class AuthState {
        object Loading : AuthState()
        object SignedOut : AuthState()
        data class SignedIn(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // UI state for form operations
    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        object Success : FormState()
        data class Error(val message: String) : FormState()
    }
    
    // UI state for Google Sign-In
    sealed class GoogleSignInState {
        object Idle : GoogleSignInState()
        object Loading : GoogleSignInState()
        object Success : GoogleSignInState()
        data class Error(val message: String) : GoogleSignInState()
    }

    // States
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    private val _googleSignInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: StateFlow<GoogleSignInState> = _googleSignInState.asStateFlow()

    private val _userData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val userData: StateFlow<Map<String, Any>> = _userData.asStateFlow()

    init {
        // Observe authentication state changes
        viewModelScope.launch {
            userRepository.authState.collect { user ->
                if (user != null) {
                    _authState.value = AuthState.SignedIn(user)
                    // Fetch user data when signed in
                    fetchUserData()
                } else {
                    _authState.value = AuthState.SignedOut
                    _userData.value = emptyMap()
                }
            }
        }
    }

    // Sign in with email and password
    fun signIn(email: String, password: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = userRepository.signIn(email, password)
            result.fold(
                onSuccess = { _formState.value = FormState.Success },
                onFailure = { _formState.value = FormState.Error(it.message ?: "Sign in failed") }
            )
        }
    }
    
    // Get sign-in intent for Google Sign-In
    fun getGoogleSignInIntent(): Intent {
        return userRepository.getGoogleSignInIntent()
    }
    
    // Handle Google Sign-In result
    fun handleGoogleSignInResult(data: Intent?) {
        _googleSignInState.value = GoogleSignInState.Loading
        viewModelScope.launch {
            val result = userRepository.handleGoogleSignInResult(data)
            result.fold(
                onSuccess = { _googleSignInState.value = GoogleSignInState.Success },
                onFailure = { 
                    _googleSignInState.value = GoogleSignInState.Error(it.message ?: "Google sign-in failed") 
                }
            )
        }
    }

    // Create a new user account
    fun createAccount(email: String, password: String, displayName: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = userRepository.createAccount(email, password, displayName)
            result.fold(
                onSuccess = { _formState.value = FormState.Success },
                onFailure = { _formState.value = FormState.Error(it.message ?: "Account creation failed") }
            )
        }
    }

    // Sign out
    fun signOut() {
        userRepository.signOut()
        // Auth state will automatically update via the flow
    }

    // Update user profile
    fun updateProfile(displayName: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = userRepository.updateProfile(displayName)
            result.fold(
                onSuccess = {
                    _formState.value = FormState.Success
                    fetchUserData() // Refresh user data
                },
                onFailure = { 
                    _formState.value = FormState.Error(it.message ?: "Profile update failed") 
                }
            )
        }
    }

    // Reset password
    fun resetPassword(email: String) {
        _formState.value = FormState.Loading
        viewModelScope.launch {
            val result = userRepository.resetPassword(email)
            result.fold(
                onSuccess = { _formState.value = FormState.Success },
                onFailure = { _formState.value = FormState.Error(it.message ?: "Password reset failed") }
            )
        }
    }

    // Fetch user data from Firestore
    private fun fetchUserData() {
        viewModelScope.launch {
            val result = userRepository.getUserData()
            result.fold(
                onSuccess = { _userData.value = it },
                onFailure = { /* Silently fail, not critical */ }
            )
        }
    }

    // Reset form state
    fun resetFormState() {
        _formState.value = FormState.Idle
    }
    
    // Reset Google Sign-In state
    fun resetGoogleSignInState() {
        _googleSignInState.value = GoogleSignInState.Idle
    }
} 