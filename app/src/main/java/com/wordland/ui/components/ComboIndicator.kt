package com.wordland.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordland.domain.model.ComboState
import kotlinx.coroutines.delay

/**
 * Combo indicator component for learning gamification
 * Displays current combo count with visual feedback
 *
 * Features:
 * - Shows combo count (e.g., "3 Combo!")
 * - Displays multiplier badge for 3+ and 5+ combos
 * - Fire emoji for 5+ combo
 * - Animated scale effect on combo change
 * - Color transitions based on combo level
 */
@Composable
fun ComboIndicator(
    comboState: ComboState,
    modifier: Modifier = Modifier,
) {
    if (!comboState.isActive) {
        // Don't show anything if combo is not active
        return
    }

    val scale = remember { Animatable(1f) }

    // Animate scale when combo changes
    LaunchedEffect(comboState.consecutiveCorrect) {
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec =
                spring(
                    dampingRatio = 0.5f,
                    stiffness = 200f,
                ),
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = 0.7f,
                    stiffness = 300f,
                ),
        )
    }

    // Calculate color based on combo level
    val containerColor by animateColorAsState(
        targetValue =
            when {
                comboState.consecutiveCorrect >= 5 -> Color(0xFFFF6B35) // Fire orange
                comboState.consecutiveCorrect >= 3 -> Color(0xFFFFB347) // Warm orange
                else -> MaterialTheme.colorScheme.primary
            },
        animationSpec = tween(durationMillis = 300),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            when {
                comboState.consecutiveCorrect >= 5 -> Color.White
                else -> MaterialTheme.colorScheme.onPrimary
            },
        animationSpec = tween(durationMillis = 300),
        label = "contentColor",
    )

    // Calculate multiplier badge
    val multiplierBadge =
        when {
            comboState.consecutiveCorrect >= 5 -> "×1.5"
            comboState.consecutiveCorrect >= 3 -> "×1.2"
            else -> null
        }

    // Fire emoji for high combo
    val fireEmoji = if (comboState.isHighCombo) "\uD83D\uDD25" else null

    Box(
        modifier =
            modifier
                .scale(scale.value)
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Combo count
            Text(
                text = "${comboState.consecutiveCorrect} Combo!",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = if (comboState.isHighCombo) FontWeight.Bold else FontWeight.SemiBold,
            )

            // Fire emoji for high combo
            if (fireEmoji != null) {
                Text(
                    text = fireEmoji,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 16.sp,
                )
            }

            // Multiplier badge
            if (multiplierBadge != null) {
                Text(
                    text = multiplierBadge,
                    modifier =
                        Modifier
                            .padding(start = 8.dp)
                            .background(
                                color = contentColor.copy(alpha = 0.2f),
                                shape = CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * Compact combo indicator for smaller spaces
 * Shows only the count with optional fire emoji
 */
@Composable
fun CompactComboIndicator(
    comboState: ComboState,
    modifier: Modifier = Modifier,
) {
    if (!comboState.isActive) {
        return
    }

    val scale by animateFloatAsState(
        targetValue = if (comboState.isAtMilestone) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (comboState.isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha",
    )

    val textColor =
        when {
            comboState.consecutiveCorrect >= 5 -> Color(0xFFFF6B35)
            comboState.consecutiveCorrect >= 3 -> Color(0xFFFFB347)
            else -> MaterialTheme.colorScheme.primary
        }

    Row(
        modifier = modifier.alpha(alpha).scale(scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${comboState.consecutiveCorrect}",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        if (comboState.isHighCombo) {
            Text(
                text = " \uD83D\uDD25",
                fontSize = 18.sp,
            )
        }
    }
}

/**
 * Combo milestone celebration animation
 * Shows when reaching 3, 5, or 10 combo
 * Auto-dismisses after 2 seconds
 */
@Composable
fun ComboMilestoneCelebration(
    comboCount: Int,
    modifier: Modifier = Modifier,
) {
    if (comboCount !in listOf(3, 5, 10)) {
        return
    }

    val scale = remember { Animatable(0f) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(comboCount) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = 0.5f,
                    stiffness = 150f,
                ),
        )
        // Auto-dismiss after 2 seconds
        delay(2000)
        isVisible = false
    }

    if (!isVisible) return

    val (message, emoji, backgroundColor) =
        when (comboCount) {
            3 -> Triple("Nice Streak!", "✨", Color(0xFFFFB347))
            5 -> Triple("On Fire!", "\uD83D\uDD25", Color(0xFFFF6B35))
            10 -> Triple("Unstoppable!", "\uD83D\uDD25\uD83D\uDD25", Color(0xFFFFD700))
            else -> Triple("", "", MaterialTheme.colorScheme.primary)
        }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha",
    )

    Box(
        modifier =
            modifier
                .scale(scale.value)
                .alpha(alpha)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(
            text = "$message $emoji",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
