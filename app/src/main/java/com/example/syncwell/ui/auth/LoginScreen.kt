package com.example.syncwell.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syncwell.MainActivity
import com.example.syncwell.R
import com.example.syncwell.ui.components.ButtonType
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellTextButton
import com.example.syncwell.ui.theme.DarkBackground
import com.example.syncwell.ui.theme.DarkPrimary
import com.example.syncwell.ui.theme.DarkSurface
import com.example.syncwell.ui.theme.DarkSurfaceVariant
import com.example.syncwell.ui.theme.Primary
import com.example.syncwell.ui.theme.Secondary
import com.example.syncwell.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val authState by userViewModel.authState.collectAsState()
    val formState by userViewModel.formState.collectAsState()
    val googleSignInState by userViewModel.googleSignInState.collectAsState()
    
    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            userViewModel.handleGoogleSignInResult(result.data)
        } else {
            Log.w("SyncWell", "Google Sign-In canceled by user")
        }
    }
    
    // Validation function
    fun validateForm(): Boolean {
        var isValid = true
        
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
        
        return isValid
    }
    
    // Handle login
    fun handleLogin() {
        if (validateForm()) {
            userViewModel.signIn(email, password)
        }
    }
    
    // Handle Google Sign-In
    fun handleGoogleSignIn() {
        try {
            val mainActivity = context as? MainActivity
            if (mainActivity != null) {
                mainActivity.signInWithGoogle()
            } else {
                // Fallback if not in MainActivity
                val signInIntent = userViewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e("SyncWell", "Error launching Google Sign-In", e)
        }
    }
    
    // Reset form state when navigating away
    LaunchedEffect(Unit) {
        userViewModel.resetFormState()
        userViewModel.resetGoogleSignInState()
    }
    
    when (authState) {
        is UserViewModel.AuthState.Loading -> {
            FullScreenLoading()
        }
        is UserViewModel.AuthState.SignedOut -> {
            // Full screen gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DarkBackground,
                                Color(0xFF0D2E2B) // Darker teal for gradient bottom
                            )
                        )
                    )
            ) {
                // Main content card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.Center)
                        .padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Login Header
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Username/Email field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Username",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                placeholder = { Text("Type your username", color = Color.Gray) },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Username Icon",
                                        tint = DarkPrimary
                                    )
                                },
                                singleLine = true,
                                isError = emailError.isNotEmpty(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = DarkPrimary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                            
                            if (emailError.isNotEmpty()) {
                                Text(
                                    text = emailError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Password field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                placeholder = { Text("Type your password", color = Color.Gray) },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Password Icon",
                                        tint = DarkPrimary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (passwordVisible) R.drawable.ic_visibility 
                                                else R.drawable.ic_visibility_off
                                            ),
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                singleLine = true,
                                isError = passwordError.isNotEmpty(),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkPrimary,
                                    unfocusedBorderColor = Color.Gray,
                                    cursorColor = DarkPrimary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                            
                            if (passwordError.isNotEmpty()) {
                                Text(
                                    text = passwordError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                        
                        // Forgot password link
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TextButton(
                                onClick = onNavigateToForgotPassword,
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    color = DarkPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Login button with gradient
                        Button(
                            onClick = { handleLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Primary,
                                                Secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (formState is UserViewModel.FormState.Loading) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "LOGIN",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                    if (formState is UserViewModel.FormState.Error) {
                            Spacer(modifier = Modifier.height(16.dp))
                        ErrorMessage(
                            message = (formState as UserViewModel.FormState.Error).message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Or sign up section
                        Text(
                            text = "Or Sign Up Using",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Social login buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Facebook button
                            IconButton(
                                onClick = { /* Facebook login */ },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3B5998), CircleShape)
                            ) {
                                Text(
                                    text = "f",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Twitter button
                            IconButton(
                                onClick = { /* Twitter login */ },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF1DA1F2), CircleShape)
                            ) {
                                Text(
                                    text = "t",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Google button
                            IconButton(
                                onClick = { handleGoogleSignIn() },
                        modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFDB4437), CircleShape)
                            ) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Sign up section
                        Text(
                            text = "Or Sign Up Using",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        TextButton(
                            onClick = onNavigateToRegister
                        ) {
                            Text(
                                text = "SIGN UP",
                                color = DarkPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
        else -> {
            // Handled by navigation
        }
    }
} 