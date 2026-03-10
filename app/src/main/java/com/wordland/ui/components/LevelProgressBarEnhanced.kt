package com.wordland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.model.ComboState

/**
 * Enhanced level progress bar with motivational messages
 *
 * Part of P0 Phase 1: Visual Feedback Enhancements
 *
 * Features:
 * - Motivational messages based on progress and combo state
 * - Animated progress bar
 * - Visual combo indicator
 *
 * Messages:
 * - "🔥 你在燃烧！" for 5+ streak (hot streak)
 * - "最后一个词！" for last word
 * - "开始吧！" for first word
 * - "继续前进！" default
 */
@Composable
fun LevelProgressBarEnhanced(
    currentWord: Int,
    totalWords: Int,
    comboState: ComboState = ComboState(),
    modifier: Modifier = Modifier,
    showCombo: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
) {
    val progress =
        if (totalWords > 0) {
            currentWord.toFloat() / totalWords.toFloat()
        } else {
            0f
        }

    val isLastWord = currentWord == totalWords
    val isFirstWord = currentWord == 0

    val isHotStreak = comboState.isHighCombo
    val motivationalMessage =
        when {
            isLastWord -> "最后一个词！"
            isHotStreak -> "🔥 你在燃烧！"
            comboState.consecutiveCorrect >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
            comboState.consecutiveCorrect > 0 -> "继续前进！"
            else -> "开始吧！"
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header row with progress text and combo indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Progress counter
            Text(
                text = "$currentWord / $totalWords",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isHotStreak) FontWeight.Bold else FontWeight.Normal,
            )

            // Combo indicator
            if (showCombo && comboState.consecutiveCorrect > 0) {
                ComboIndicator(
                    comboCount = comboState.consecutiveCorrect,
                    isHotStreak = isHotStreak,
                )
            }
        }

        // Progress bar
        AnimatedProgressBar(
            progress = progress,
            backgroundColor = backgroundColor,
            progressColor =
                if (isHotStreak) {
                    Color(0xFFFF6B35) // Fire orange for hot streak
                } else {
                    progressColor
                },
        )

        // Motivational message
        AnimatedMotivationalMessage(
            message = motivationalMessage,
            isHotStreak = isHotStreak,
        )
    }
}

/**
 * Animated progress bar with smooth transitions
 */
@Composable
private fun AnimatedProgressBar(
    progress: Float,
    backgroundColor: Color,
    progressColor: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec =
            tween(
                durationMillis = 500,
                easing = EaseOutCubic,
            ),
        label = "progress_animation",
    )

    LinearProgressIndicator(
        progress = animatedProgress,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = backgroundColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                ),
        color = progressColor,
        trackColor = Color.Transparent,
        strokeCap = StrokeCap.Round,
    )
}

/**
 * Animated motivational message with fade-in effect
 */
@Composable
private fun AnimatedMotivationalMessage(
    message: String,
    isHotStreak: Boolean,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = EaseIn,
            ),
        label = "message_alpha",
    )

    val scale by animateFloatAsState(
        targetValue = if (visible && isHotStreak) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 300f,
            ),
        label = "message_scale",
    )

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color =
            if (isHotStreak) {
                Color(0xFFFF6B35)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        fontWeight = if (isHotStreak) FontWeight.Bold else FontWeight.Normal,
        modifier =
            Modifier
                .alpha(alpha)
                .scale(scale),
    )
}

/**
 * Combo indicator showing consecutive correct answers
 */
@Composable
private fun ComboIndicator(
    comboCount: Int,
    isHotStreak: Boolean,
) {
    val scale by animateFloatAsState(
        targetValue = if (isHotStreak) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.4f,
                stiffness = 400f,
            ),
        label = "combo_scale",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.scale(scale),
    ) {
        if (isHotStreak) {
            Text(
                text = "🔥",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Text(
            text = "x$comboCount",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color =
                if (isHotStreak) {
                    Color(0xFFFF6B35)
                } else {
                    MaterialTheme.colorScheme.primary
                },
        )
    }
}

/**
 * Simplified progress bar for minimal UI
 */
@Composable
fun CompactLevelProgressBar(
    currentWord: Int,
    totalWords: Int,
    modifier: Modifier = Modifier,
) {
    val progress =
        if (totalWords > 0) {
            currentWord.toFloat() / totalWords.toFloat()
        } else {
            0f
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$currentWord / $totalWords",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier =
                Modifier
                    .width(100.dp)
                    .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}
