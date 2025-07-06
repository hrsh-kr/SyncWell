package com.example.syncwell.ui.auth

import androidx.compose.foundation.layout.Arrangement
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
import com.example.syncwell.ui.components.SyncWellButton
import com.example.syncwell.ui.components.SyncWellTextField
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel
) {
    val formState by userViewModel.formState.collectAsState()
    
    // Form state
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var isResetSent by remember { mutableStateOf(false) }
    
    // Validation function
    fun validateForm(): Boolean {
        if (email.isBlank()) {
            emailError = "Email is required"
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email address"
            return false
        }
        
        emailError = ""
        return true
    }
    
    // Handle password reset
    fun handlePasswordReset() {
        if (validateForm()) {
            userViewModel.resetPassword(email)
        }
    }
    
    // Reset form state when navigating away
    LaunchedEffect(Unit) {
        userViewModel.resetFormState()
        isResetSent = false
    }
    
    // Handle success
    LaunchedEffect(formState) {
        if (formState is UserViewModel.FormState.Success) {
            isResetSent = true
        }
    }
    
    Scaffold(
        topBar = {
            SyncWellTopAppBar(
                title = "Forgot Password",
                onBackClick = onNavigateBack
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
            VerticalSpacer(height = 32)
            
            Text(
                text = if (isResetSent) "Password Reset Email Sent" else "Reset Your Password",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            
            VerticalSpacer(height = 16)
            
            Text(
                text = if (isResetSent) 
                    "Check your email for instructions to reset your password." 
                else 
                    "Enter your email address and we'll send you instructions to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            VerticalSpacer(height = 32)
            
            if (!isResetSent) {
                // Form error message
                if (formState is UserViewModel.FormState.Error) {
                    ErrorMessage(
                        message = (formState as UserViewModel.FormState.Error).message,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    VerticalSpacer(height = 16)
                }
                
                // Email field
                SyncWellTextField(
                    value = email,
                    onValueChange = { email = it; emailError = "" },
                    label = "Email",
                    isError = emailError.isNotEmpty(),
                    errorText = emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    onImeAction = { handlePasswordReset() },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                VerticalSpacer(height = 32)
                
                // Reset button
                SyncWellButton(
                    onClick = { handlePasswordReset() },
                    text = "Send Reset Instructions",
                    isLoading = formState is UserViewModel.FormState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Return to login button
                SyncWellButton(
                    onClick = onNavigateBack,
                    text = "Return to Login",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
} 