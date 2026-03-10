package com.wordland.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Hint level enum
 */
enum class HintLevel {
    None, // No hint available
    Level1, // First letter
    Level2, // Half word (3 letters)
    Level3, // Full word (all letters)
}

/**
 * Hint system for learning screen
 * Provides hints based on current hint level
 */
@Composable
fun HintCard(
    text: String,
    content: String,
    icon: (@Composable () -> Unit)? = null,
    alpha: Float,
    scale: Float,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .requiredSize(
                    width = (200.dp * scale).coerceIn(150.dp, 250.dp),
                    height = (100.dp * scale).coerceIn(80.dp, 120.dp),
                ),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Hint",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(48.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (icon != null) {
                icon()
            }
        }
    }
}
