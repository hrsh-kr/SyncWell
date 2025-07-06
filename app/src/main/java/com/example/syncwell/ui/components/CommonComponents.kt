package com.example.syncwell.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.syncwell.R
import com.example.syncwell.ui.theme.CardBackground
import com.example.syncwell.ui.theme.DarkPrimary
import com.example.syncwell.ui.theme.DarkSurface
import com.example.syncwell.ui.theme.Secondary
import com.example.syncwell.ui.theme.Success
import com.example.syncwell.ui.theme.Warning

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .padding(vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncWellTopAppBar(
    title: String,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
    actions: @Composable () -> Unit = {},
    contentDescription: String? = null
) {
    Surface(
        color = DarkSurface,
        shadowElevation = 0.dp // Remove elevation for a flatter design
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = if (contentDescription != null) {
                            Modifier.semantics { 
                                this.contentDescription = contentDescription
                            }
                        } else {
                            Modifier.semantics { 
                                this.contentDescription = "Navigate back"
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null, // Content description is now on the button
                            tint = DarkPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface,
                titleContentColor = Color.White,
                actionIconContentColor = DarkPrimary
            )
        )
    }
}

@Composable
fun SyncWellTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    errorText: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onImeAction: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() },
                onGo = { onImeAction() }
            ),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedLabelColor = DarkPrimary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = DarkPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )
        )
        
        if (isError && errorText.isNotEmpty()) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String = "",
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    SyncWellTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        isError = isError,
        errorText = errorText,
        onImeAction = onImeAction,
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(
                        id = if (passwordVisible) R.drawable.ic_visibility 
                        else R.drawable.ic_visibility_off
                    ),
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    )
}

@Composable
fun SyncWellButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonType: ButtonType = ButtonType.FILLED
) {
    when (buttonType) {
        ButtonType.FILLED -> {
            Button(
                onClick = onClick,
                enabled = enabled && !isLoading,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = DarkPrimary.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                ButtonContent(text, isLoading)
            }
        }
        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled && !isLoading,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkPrimary
                )
            ) {
                ButtonContent(text, isLoading)
            }
        }
        ButtonType.TONAL -> {
            FilledTonalButton(
                onClick = onClick,
                enabled = enabled && !isLoading,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DarkPrimary.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                ButtonContent(text, isLoading)
            }
        }
        ButtonType.ELEVATED -> {
            ElevatedButton(
                onClick = onClick,
                enabled = enabled && !isLoading,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = CardBackground,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                ButtonContent(text, isLoading)
            }
        }
        ButtonType.GRADIENT -> {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    DarkPrimary,
                                    Secondary
                                )
                            )
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ButtonContent(text, isLoading)
                }
            }
        }
    }
}

@Composable
private fun ButtonContent(text: String, isLoading: Boolean) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
            color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class ButtonType {
    FILLED,  // Default primary button
    OUTLINED,  // Outlined button with border
    TONAL,  // Filled tonal button (secondary)
    ELEVATED,  // Elevated button with shadow
    GRADIENT   // Gradient background button
}

@Composable
fun SyncWellCard(
    onClick: () -> Unit,
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    backgroundColor: Color = CardBackground
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 0.dp, // Flatter design
            pressedElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        color = DarkPrimary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                    modifier = Modifier.size(32.dp),
                    tint = DarkPrimary
            )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SyncWellCardWithPainter(
    onClick: () -> Unit,
    title: String,
    iconResId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.3f),
                                backgroundColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                    modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SyncWellTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "completed", "done", "active" -> Pair(Success.copy(alpha = 0.2f), Success)
        "pending", "in progress" -> Pair(Warning.copy(alpha = 0.2f), Warning)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VerticalSpacer(height: Int) {
    Spacer(modifier = Modifier.height(height.dp))
}

@Composable
fun HorizontalSpacer(width: Int) {
    Spacer(modifier = Modifier.width(width.dp))
} 