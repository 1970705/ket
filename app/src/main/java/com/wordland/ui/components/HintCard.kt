package com.wordland.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced Hint Card component displaying progressive hints with visual feedback
 *
 * Features:
 * - Shows hint text with fade-in animation
 * - Displays hint level indicator (1/2/3 dots)
 * - Shows remaining hints count
 * - Warns about star penalty when hints are used
 * - Child-friendly design with large buttons and emoji icons
 *
 * @param hintText The hint text to display (null if no hint shown yet)
 * @param hintLevel Current hint level (0-3)
 * @param hintsRemaining Number of hints remaining (0-3)
 * @param hintPenaltyApplied Whether using hints will reduce stars
 * @param onUseHint Callback when user requests hint
 * @param modifier Modifier for the card
 */
@Composable
fun HintCard(
    hintText: String?,
    hintLevel: Int,
    hintsRemaining: Int,
    hintPenaltyApplied: Boolean,
    onUseHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animate card appearance when hint is shown
    val scale by animateFloatAsState(
        targetValue = if (hintText != null) 1f else 0.95f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "hint_card_scale",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (hintLevel) {
                        1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        3 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
            ),
        shape = RoundedCornerShape(16.dp),
        border =
            BorderStroke(
                width = 2.dp,
                color =
                    when (hintLevel) {
                        1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        2 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                        3 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    },
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .scale(scale),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side: Icon and hint content
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hint icon with level indicator
                HintIconWithLevel(hintLevel = hintLevel)

                Spacer(modifier = Modifier.width(12.dp))

                // Hint text or button
                if (hintText != null) {
                    HintTextContent(
                        hintText = hintText,
                        hintLevel = hintLevel,
                    )
                } else {
                    HintPlaceholder()
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right side: Hint button or status
            HintActionButton(
                hintText = hintText,
                hintsRemaining = hintsRemaining,
                hintPenaltyApplied = hintPenaltyApplied,
                onUseHint = onUseHint,
            )
        }

        // Penalty warning (shown below hint content)
        AnimatedVisibility(
            visible = hintPenaltyApplied && hintText != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically(),
        ) {
            PenaltyWarning()
        }
    }
}

/**
 * Hint icon with level indicator dots
 */
@Composable
private fun HintIconWithLevel(hintLevel: Int) {
    // Cap hint level at 3 for display (safety check)
    val displayLevel = minOf(hintLevel, 3)

    Box {
        // Main icon
        Text(
            text =
                when (displayLevel) {
                    0 -> "\uD83D\uDEA7" // 💡
                    1 -> "\uD83D\uDD11" // 🔑 (key - first letter)
                    2 -> "\uD83D\uDD0E" // 🔎 (magnifying glass - searching)
                    3 -> "\u2600\uFE0F" // ☀️ (sun - full reveal)
                    else -> "\uD83D\uDEA7" // 💡
                },
            fontSize = 32.sp,
            modifier = Modifier.padding(4.dp),
        )

        // Level indicator dots
        if (displayLevel > 0) {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                repeat(3) { index ->
                    val level = index + 1
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .background(
                                    color =
                                        if (level <= displayLevel) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        },
                                    shape = RoundedCornerShape(3.dp),
                                ),
                    )
                }
            }
        }
    }
}

/**
 * Hint text content with level-specific styling
 */
@Composable
private fun HintTextContent(
    hintText: String,
    hintLevel: Int,
) {
    // Cap hint level at 3 for display (safety check)
    val displayLevel = minOf(hintLevel, 3)

    Column {
        Text(
            text = "提示 $displayLevel/3",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = hintText,
            style =
                when (displayLevel) {
                    3 -> MaterialTheme.typography.titleMedium
                    2 -> MaterialTheme.typography.bodyLarge
                    else -> MaterialTheme.typography.bodyMedium
                },
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight =
                when (displayLevel) {
                    3 -> FontWeight.Bold
                    else -> FontWeight.Normal
                },
        )
    }
}

/**
 * Placeholder shown when no hint is active yet
 */
@Composable
private fun HintPlaceholder() {
    Text(
        text = "点击提示获取帮助",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    )
}

/**
 * Hint action button with different states
 */
@Composable
private fun HintActionButton(
    hintText: String?,
    hintsRemaining: Int,
    hintPenaltyApplied: Boolean,
    onUseHint: () -> Unit,
) {
    when {
        // No hints remaining
        hintsRemaining == 0 -> {
            OutlinedButton(
                onClick = { /* Disabled */ },
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(64.dp),
            ) {
                Text(
                    text = "\uD83D\uDEAB", // 🚫
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        // Hint already shown - show remaining count
        hintText != null -> {
            Button(
                onClick = onUseHint,
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            when {
                                hintsRemaining == 1 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                hintsRemaining == 2 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            },
                    ),
                modifier = Modifier.height(64.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "\uD83D\uDEA7", // 💡
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "再提示",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = "($hintsRemaining)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        // Initial hint button
        else -> {
            Button(
                onClick = onUseHint,
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (hintPenaltyApplied) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    ),
                modifier = Modifier.height(64.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "\uD83D\uDEA7", // 💡
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "提示",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "($hintsRemaining)",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

/**
 * Penalty warning message
 */
@Composable
private fun PenaltyWarning() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\u26A0\uFE0F", // ⚠️
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = "使用提示将扣除1星",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Compact hint button for use in action rows
 * Simpler version of HintCard for tight spaces
 */
@Composable
fun CompactHintButton(
    hintsRemaining: Int,
    hintPenaltyApplied: Boolean = false,
    onUseHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onUseHint,
        enabled = hintsRemaining > 0,
        shape = RoundedCornerShape(12.dp),
        border =
            BorderStroke(
                width = 2.dp,
                color =
                    when {
                        hintsRemaining == 0 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        hintPenaltyApplied -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.primary
                    },
            ),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor =
                    when {
                        hintsRemaining == 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        hintPenaltyApplied -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
            ),
        modifier = modifier.height(48.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (hintsRemaining == 0) "\uD83D\uDEAB" else "\uD83D\uDEA7", // 🚫 / 💡
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (hintsRemaining == 0) "已用完" else "提示 ($hintsRemaining)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
