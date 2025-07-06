package com.example.syncwell.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * UI utilities for consistent design across the application
 */
object UiUtils {
    // Card shapes
    val cardShape = RoundedCornerShape(16.dp)
    val cardShapeSmall = RoundedCornerShape(12.dp)
    val buttonShape = RoundedCornerShape(12.dp)
    val textFieldShape = RoundedCornerShape(12.dp)
    val chipShape = RoundedCornerShape(16.dp)
    
    // Elevations
    val cardElevation = 4.dp
    val buttonElevation = 4.dp
    val pressedButtonElevation = 8.dp
    
    // Padding
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    
    // Spacings
    val spacingSmall = 8.dp
    val spacingMedium = 16.dp
    val spacingLarge = 24.dp
    
    // Icon sizes
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 36.dp
    val iconSizeExtraLarge = 48.dp
    
    // Color functions for status indicators
    @Composable
    fun getStatusColors(status: String): Pair<Color, Color> {
        return when (status.lowercase()) {
            "completed", "done", "active" -> Pair(Success.copy(alpha = 0.2f), Success)
            "pending", "in progress" -> Pair(Warning.copy(alpha = 0.2f), Warning)
            "cancelled", "failed" -> Pair(Error.copy(alpha = 0.2f), Error)
            else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    
    // Consistent button settings
    @Composable
    fun getElevatedButtonColors() = ButtonDefaults.elevatedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    )
    
    @Composable
    fun getPrimaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    )
    
    @Composable
    fun getSecondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    )
    
    // Card settings
    @Composable
    fun getElevatedCardColors(backgroundColor: Color = MaterialTheme.colorScheme.surface) = 
        CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    
    @Composable
    fun getCardElevation() = CardDefaults.cardElevation(
        defaultElevation = cardElevation
    )
    
    @Composable
    fun getElevatedCardElevation() = CardDefaults.elevatedCardElevation(
        defaultElevation = cardElevation,
        pressedElevation = pressedButtonElevation
    )
    
    // Function to get gradient colors for backgrounds
    @Composable
    fun getGradientColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    }
    
    // Function to get accent gradient colors
    @Composable
    fun getAccentGradientColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer
        )
    }
} 