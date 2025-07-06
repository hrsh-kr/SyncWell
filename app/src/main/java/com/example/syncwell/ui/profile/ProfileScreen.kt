package com.example.syncwell.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncwell.R
import com.example.syncwell.ui.components.ErrorMessage
import com.example.syncwell.ui.components.FullScreenLoading
import com.example.syncwell.ui.components.SyncWellButton
import com.example.syncwell.ui.components.SyncWellTextField
import com.example.syncwell.ui.components.SyncWellTopAppBar
import com.example.syncwell.ui.components.VerticalSpacer
import com.example.syncwell.ui.viewmodel.UserViewModel
import com.example.syncwell.utils.getCurrentLanguage
import com.example.syncwell.utils.setAppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val authState by userViewModel.authState.collectAsState()
    val formState by userViewModel.formState.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    
    // Pre-fetch string resources to avoid @Composable context issues
    val profileNameError = stringResource(R.string.profile_name_error)
    val saveString = stringResource(R.string.save)
    val cancelString = stringResource(R.string.cancel)
    val profileSignOutString = stringResource(R.string.profile_sign_out)
    val profileConfirmSignOutString = stringResource(R.string.profile_confirm_sign_out)
    val aboutTitleString = stringResource(R.string.about_title)
    val aboutDescriptionString = stringResource(R.string.about_description)
    val aboutFeaturesString = stringResource(R.string.about_features)
    val aboutFeature1String = stringResource(R.string.about_feature_1)
    val aboutFeature2String = stringResource(R.string.about_feature_2)
    val aboutFeature3String = stringResource(R.string.about_feature_3)
    val aboutFeature4String = stringResource(R.string.about_feature_4)
    val aboutFeature5String = stringResource(R.string.about_feature_5)
    val aboutPrivacyString = stringResource(R.string.about_privacy)
    val aboutDeveloperString = stringResource(R.string.about_developer)
    val accessibilityProfilePicture = stringResource(R.string.accessibility_profile_picture)
    val accessibilityEditName = stringResource(R.string.accessibility_edit_name)
    val accessibilityBack = stringResource(R.string.accessibility_back)
    val accessibilitySignOut = stringResource(R.string.accessibility_sign_out)
    val accessibilityAbout = stringResource(R.string.accessibility_about)
    
    // Get the current context for language setting
    val context = LocalContext.current
    
    // Edit profile state
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    
    // Reset form state when screen is dismissed
    LaunchedEffect(Unit) {
        userViewModel.resetFormState()
    }
    
    when (authState) {
        is UserViewModel.AuthState.Loading -> {
            FullScreenLoading()
        }
        is UserViewModel.AuthState.SignedIn -> {
            val user = (authState as UserViewModel.AuthState.SignedIn).user
            
            Scaffold(
                topBar = {
                    SyncWellTopAppBar(
                        title = stringResource(R.string.profile_title),
                        onBackClick = onNavigateBack,
                        contentDescription = accessibilityBack
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VerticalSpacer(height = 24)
                    
                    // Profile header
                    ProfileHeader(
                        displayName = user.displayName ?: "User",
                        email = user.email ?: "",
                        onEditName = {
                            editedName = user.displayName ?: ""
                            showEditNameDialog = true
                        }
                    )
                    
                    VerticalSpacer(height = 24)
                    
                    // Profile options
                    ProfileOption(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.profile_app_info),
                        subtitle = stringResource(R.string.profile_about_syncwell),
                        onClick = { showAboutDialog = true },
                        contentDescription = accessibilityAbout
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Accessibility options
                    ProfileOption(
                        icon = Icons.Outlined.Accessibility,
                        title = stringResource(R.string.profile_accessibility),
                        subtitle = "TalkBack and screen reader support",
                        onClick = { /* Open system accessibility settings */ }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Language options
                    ProfileOption(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.profile_language),
                        subtitle = "English / Español",
                        onClick = { showLanguageDialog = true }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileOption(
                        icon = Icons.Default.ExitToApp,
                        title = profileSignOutString,
                        subtitle = stringResource(R.string.profile_sign_out_subtitle),
                        onClick = { showSignOutDialog = true },
                        contentDescription = accessibilitySignOut
                    )
                    
                    VerticalSpacer(height = 32)
                    
                    // Version info
                    Text(
                        text = stringResource(R.string.profile_version),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Language selection dialog
            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text("Select Language / Seleccionar Idioma") },
                    text = { 
                        Column {
                            Text(
                                "Choose your preferred language. The app will restart to apply changes.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            VerticalSpacer(height = 16)
                            
                            // Current language indicator
                            val currentLanguage = getCurrentLanguage(context)
                            
                            // English option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Set app language to English
                                        setAppLanguage(context, "en")
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "English",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (currentLanguage == "en") FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Check mark for current language
                                if (currentLanguage == "en") {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Divider()
                            
                            // Spanish option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Set app language to Spanish
                                        setAppLanguage(context, "es")
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Español",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (currentLanguage == "es") FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentLanguage == "es") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Check mark for current language
                                if (currentLanguage == "es") {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showLanguageDialog = false }
                        ) {
                            Text(cancelString)
                        }
                    }
                )
            }
            
            // Edit name dialog
            if (showEditNameDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showEditNameDialog = false
                        nameError = ""
                        userViewModel.resetFormState()
                    },
                    title = { Text(stringResource(R.string.profile_edit_name)) },
                    text = {
                        Column {
                            if (formState is UserViewModel.FormState.Error) {
                                ErrorMessage(
                                    message = (formState as UserViewModel.FormState.Error).message
                                )
                                VerticalSpacer(height = 8)
                            }
                            
                            SyncWellTextField(
                                value = editedName,
                                onValueChange = { editedName = it; nameError = "" },
                                label = "Display Name",
                                isError = nameError.isNotEmpty(),
                                errorText = nameError,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Name"
                                    )
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (editedName.isBlank()) {
                                    nameError = profileNameError
                                } else {
                                    userViewModel.updateProfile(editedName)
                                    // Close dialog on success
                                    if (formState !is UserViewModel.FormState.Error) {
                                        showEditNameDialog = false
                                    }
                                }
                            },
                            enabled = formState !is UserViewModel.FormState.Loading
                        ) {
                            Text(saveString)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showEditNameDialog = false
                                nameError = ""
                                userViewModel.resetFormState()
                            }
                        ) {
                            Text(cancelString)
                        }
                    }
                )
            }
            
            // Sign out confirmation dialog
            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    title = { Text(profileSignOutString) },
                    text = { Text(profileConfirmSignOutString) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSignOutDialog = false
                                onSignOut()
                            }
                        ) {
                            Text(profileSignOutString)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showSignOutDialog = false }
                        ) {
                            Text(cancelString)
                        }
                    }
                )
            }
            
            // About dialog
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text(aboutTitleString) },
                    text = { 
                        Column {
                            Text(
                                text = aboutDescriptionString,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            VerticalSpacer(height = 16)
                            
                            Text(
                                text = aboutFeaturesString,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            VerticalSpacer(height = 8)
                            
                            Text(text = aboutFeature1String)
                            Text(text = aboutFeature2String)
                            Text(text = aboutFeature3String)
                            Text(text = aboutFeature4String)
                            Text(text = aboutFeature5String)
                            
                            VerticalSpacer(height = 16)
                            
                            Text(
                                text = aboutPrivacyString,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            VerticalSpacer(height = 16)
                            
                            Text(
                                text = aboutDeveloperString,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showAboutDialog = false }
                        ) {
                            Text(cancelString)
                        }
                    }
                )
            }
        }
        else -> {
            // Handled by navigation
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    onEditName: () -> Unit
) {
    // Cache string resource to avoid @Composable invocation context issues
    val accessibilityProfilePicture = stringResource(R.string.accessibility_profile_picture)
    val accessibilityEditName = stringResource(R.string.accessibility_edit_name)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile avatar
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .semantics { contentDescription = accessibilityProfilePicture },
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(60.dp)
                )
            }
            
            VerticalSpacer(height = 16)
            
            // Display name with edit button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                IconButton(
                    onClick = onEditName,
                    modifier = Modifier.semantics { 
                        contentDescription = accessibilityEditName
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ProfileOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    contentDescription: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { 
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
} 