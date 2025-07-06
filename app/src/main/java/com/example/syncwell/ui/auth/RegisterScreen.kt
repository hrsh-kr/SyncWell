package com.example.syncwell.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.PasswordTextField
import com.example.syncwell.ui.components.SyncWellButton
import com.example.syncwell.ui.components.SyncWellTextField
import com.example.syncwell.ui.components.SyncWellTextButton
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    userViewModel: UserViewModel
) {
    val authState by userViewModel.authState.collectAsState()
    val formState by userViewModel.formState.collectAsState()
    
    // Form state
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    
    // Validation function
    fun validateForm(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "Name is required"
            isValid = false
        } else {
            nameError = ""
        }
        
        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email address"
            isValid = false
        } else {
            emailError = ""
        }
        
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = ""
        }
        
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirm password is required"
            isValid = false
        } else if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = ""
        }
        
        return isValid
    }
    
    // Handle registration
    fun handleRegister() {
        if (validateForm()) {
            userViewModel.createAccount(email, password, name)
        }
    }
    
    // Reset form state when navigating away
    LaunchedEffect(Unit) {
        userViewModel.resetFormState()
    }
    
    // Handle successful registration
    LaunchedEffect(formState) {
        if (formState is UserViewModel.FormState.Success) {
            onNavigateToLogin()
        }
    }
    
    when (authState) {
        is UserViewModel.AuthState.Loading -> {
            FullScreenLoading()
        }
        is UserViewModel.AuthState.SignedOut -> {
            Scaffold(
                topBar = {
                    SyncWellTopAppBar(
                        title = "Create Account",
                        onBackClick = onNavigateToLogin
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    VerticalSpacer(height = 16)
                    
                    Text(
                        text = "Join SyncWell",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    VerticalSpacer(height = 8)
                    
                    Text(
                        text = "Create an account to start your wellness journey",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    VerticalSpacer(height = 24)
                    
                    // Form error message
                    if (formState is UserViewModel.FormState.Error) {
                        ErrorMessage(
                            message = (formState as UserViewModel.FormState.Error).message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Name field
                    SyncWellTextField(
                        value = name,
                        onValueChange = { name = it; nameError = "" },
                        label = "Full Name",
                        isError = nameError.isNotEmpty(),
                        errorText = nameError,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 16)
                    
                    // Email field
                    SyncWellTextField(
                        value = email,
                        onValueChange = { email = it; emailError = "" },
                        label = "Email",
                        isError = emailError.isNotEmpty(),
                        errorText = emailError,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 16)
                    
                    // Password field
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = "" },
                        label = "Password",
                        isError = passwordError.isNotEmpty(),
                        errorText = passwordError,
                        imeAction = ImeAction.Next,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 16)
                    
                    // Confirm password field
                    PasswordTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                        label = "Confirm Password",
                        isError = confirmPasswordError.isNotEmpty(),
                        errorText = confirmPasswordError,
                        imeAction = ImeAction.Done,
                        onImeAction = { handleRegister() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 32)
                    
                    // Register button
                    SyncWellButton(
                        onClick = { handleRegister() },
                        text = "Create Account",
                        isLoading = formState is UserViewModel.FormState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 16)
                    
                    // Login link
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SyncWellTextButton(
                            onClick = onNavigateToLogin,
                            text = "Already have an account? Login",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        else -> {
            // Handled by navigation
        }
    }
} 