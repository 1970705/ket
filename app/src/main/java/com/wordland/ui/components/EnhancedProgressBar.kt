package com.wordland.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced progress bar with visual feedback
 *
 * Story #1.4: Progress Bar Enhancement
 *
 * Features:
 * - Smooth transition animation (300ms per VISUAL_FEEDBACK_DESIGN.md)
 * - Segmented display (words/total)
 * - Combo bonus visual feedback
 * - Critical warning (< 30% remaining)
 * - Star-based progress indicators
 * - Performance optimized (60fps target)
 *
 * Per VISUAL_FEEDBACK_DESIGN.md Section 2:
 * - PROGRESS_FILL = 350ms for smooth transitions
 * - Linear easing for predictable progress
 *
 * @param currentWord Current word index (1-based)
 * @param totalWords Total number of words in level
 * @param earnedStars Stars earned for each completed word (0-3)
 * @param combo Current combo count
 * @param modifier Compose modifier
 */
@Composable
fun EnhancedProgressBar(
    currentWord: Int,
    totalWords: Int,
    earnedStars: List<Int> = emptyList(), // Stars for each completed word
    combo: Int = 0,
    modifier: Modifier = Modifier,
) {
    val progress =
        if (totalWords > 0) {
            currentWord.toFloat() / totalWords.toFloat()
        } else {
            0f
        }

    // Calculate remaining percentage
    val remainingPercent = 1f - progress
    val isCritical = remainingPercent < 0.3f && currentWord < totalWords

    // Combo tier for visual feedback
    val comboTier =
        remember(combo) {
            when {
                combo >= 10 -> 3
                combo >= 5 -> 2
                combo >= 3 -> 1
                else -> 0
            }
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header row with progress text and combo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Progress counter with critical warning
            ProgressCounter(
                currentWord = currentWord,
                totalWords = totalWords,
                isCritical = isCritical,
            )

            // Combo indicator
            if (combo > 0) {
                ProgressComboIndicator(
                    combo = combo,
                    tier = comboTier,
                )
            }
        }

        // Progress bar with smooth animation
        SmoothProgressBar(
            progress = progress,
            isCritical = isCritical,
            comboTier = comboTier,
        )

        // Star indicators for completed words
        if (earnedStars.isNotEmpty()) {
            StarProgressRow(
                totalWords = totalWords,
                earnedStars = earnedStars,
                currentWord = currentWord,
            )
        }

        // Motivational message
        ProgressMessage(
            progress = progress,
            isCritical = isCritical,
            combo = combo,
        )
    }
}

/**
 * Progress counter with critical warning
 */
@Composable
private fun ProgressCounter(
    currentWord: Int,
    totalWords: Int,
    isCritical: Boolean,
) {
    val scale by animateFloatAsState(
        targetValue = if (isCritical) 1.05f else 1f,
        animationSpec = PROGRESS_PULSE_SPEC,
        label = "counter_scale",
    )

    val color =
        when {
            isCritical -> Color(0xFFF44336) // Red for critical
            currentWord == totalWords -> Color(0xFF4CAF50) // Green for complete
            else -> MaterialTheme.colorScheme.onSurface
        }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCritical) {
            Text(
                text = "⚠️",
                modifier = Modifier.scale(scale),
            )
        }

        Text(
            text = "$currentWord / $totalWords",
            fontSize = 16.sp,
            fontWeight = if (isCritical) FontWeight.Bold else FontWeight.SemiBold,
            color = color,
            modifier = Modifier.scale(scale),
        )
    }
}

/**
 * Combo indicator in progress bar
 */
@Composable
private fun ProgressComboIndicator(
    combo: Int,
    tier: Int,
) {
    val scale by animateFloatAsState(
        targetValue = if (tier >= 2) 1.1f else 1f,
        animationSpec = PROGRESS_PULSE_SPEC,
        label = "combo_scale",
    )

    val color =
        when (tier) {
            3 -> Color(0xFFFF0000)
            2 -> Color(0xFFFF6B35)
            1 -> Color(0xFFFFB347)
            else -> MaterialTheme.colorScheme.primary
        }

    Surface(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border =
            if (tier >= 1) {
                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = color.copy(alpha = 0.5f),
                )
            } else {
                null
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Fire emojis based on tier
            val fireCount =
                when (tier) {
                    3 -> 3
                    2 -> 2
                    1 -> 1
                    else -> 0
                }

            repeat(fireCount) {
                Text(
                    text = "🔥",
                    fontSize = 12.sp,
                )
            }

            Text(
                text = "${combo}x",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

/**
 * Smooth progress bar with gradient and animation
 */
@Composable
private fun SmoothProgressBar(
    progress: Float,
    isCritical: Boolean,
    comboTier: Int,
) {
    // Animated progress value (300ms per spec)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec =
            tween(
                durationMillis = PROGRESS_FILL_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "progress_fill",
    )

    // Gradient brush based on state
    val brush =
        when {
            isCritical ->
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            Color(0xFFF44336),
                            Color(0xFFFF9800),
                        ),
                )
            comboTier >= 2 ->
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFFB347),
                        ),
                )
            comboTier >= 1 ->
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFFFFB347),
                        ),
                )
            else ->
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                )
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                )
                .clip(RoundedCornerShape(6.dp)),
    ) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            color = Color.Transparent, // We use custom drawing
            trackColor = Color.Transparent,
        )

        // Custom progress fill with gradient
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(12.dp)
                    .background(brush, RoundedCornerShape(6.dp)),
        )

        // Critical pulse effect
        if (isCritical) {
            val pulseAlpha by animateFloatAsState(
                targetValue = 0.3f,
                animationSpec =
                    tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing,
                    ),
                label = "critical_pulse",
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(12.dp)
                        .background(
                            color = Color(0xFFF44336).copy(alpha = pulseAlpha),
                            shape = RoundedCornerShape(6.dp),
                        ),
            )
        }
    }
}

/**
 * Star progress row showing stars for each completed word
 */
@Composable
private fun StarProgressRow(
    totalWords: Int,
    earnedStars: List<Int>,
    currentWord: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalWords) { index ->
            val isCompleted = index < currentWord
            val starsForWord = if (index < earnedStars.size) earnedStars[index] else 0
            val isCurrent = index == currentWord - 1

            WordStarIndicator(
                isCompleted = isCompleted,
                stars = starsForWord,
                isCurrent = isCurrent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Individual star indicator for a word
 */
@Composable
private fun WordStarIndicator(
    isCompleted: Boolean,
    stars: Int,
    isCurrent: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue =
            when {
                isCurrent -> 1.1f
                isCompleted -> 1f
                else -> 0.9f
            },
        animationSpec = PROGRESS_SPRING_SPEC,
        label = "star_indicator_scale",
    )

    val backgroundColor =
        when {
            isCurrent -> MaterialTheme.colorScheme.primaryContainer
            isCompleted -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        }

    val borderColor =
        when {
            isCurrent -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.outlineVariant
        }

    Box(
        modifier =
            modifier
                .height(20.dp)
                .scale(scale)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(4.dp),
                )
                .border(
                    width = if (isCurrent || isCompleted) 1.dp else 0.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (isCompleted) {
            StarsDisplay(count = stars)
        }
    }
}

/**
 * Small stars display (1-3 stars)
 */
@Composable
private fun StarsDisplay(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(10.dp),
            )
        }
        repeat(3 - count) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}

/**
 * Motivational message based on progress state
 */
@Composable
private fun ProgressMessage(
    progress: Float,
    isCritical: Boolean,
    combo: Int,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(progress, isCritical, combo) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
        label = "message_alpha",
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = PROGRESS_SPRING_SPEC,
        label = "message_scale",
    )

    val message =
        when {
            progress >= 1f -> "🎉 完成! Complete!"
            isCritical -> "💪 最后冲刺! Final push!"
            combo >= 10 -> "🔥🔥🔥 势不可挡! Unstoppable!"
            combo >= 5 -> "🔥🔥 燃烧中! On fire!"
            combo >= 3 -> "🔥 连击中! Combo!"
            progress >= 0.5f -> "👍 过半了! Halfway there!"
            else -> "🚀 继续前进! Keep going!"
        }

    Text(
        text = message,
        fontSize = 14.sp,
        fontWeight = if (isCritical || combo >= 5) FontWeight.Bold else FontWeight.Normal,
        color =
            when {
                isCritical -> Color(0xFFF44336)
                combo >= 5 -> Color(0xFFFF6B35)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        modifier =
            Modifier
                .alpha(alpha)
                .scale(scale),
    )
}

/**
 * Compact progress bar for minimal UI
 */
@Composable
fun CompactEnhancedProgressBar(
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

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec =
            tween(
                durationMillis = COMPACT_PROGRESS_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "compact_progress",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$currentWord/$totalWords",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.width(80.dp).height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}

/**
 * Animation specification constants
 * Per VISUAL_FEEDBACK_DESIGN.md Section 2
 */
private object ProgressAnimationSpecs {
    const val PROGRESS_FILL_DURATION_MS = 300 // 300ms for smooth progress
    const val COMPACT_PROGRESS_DURATION_MS = 200
    const val SPRING_DAMPING_RATIO = 0.5f
    const val SPRING_STIFFNESS = 300f
}

// Re-export specs
private val PROGRESS_SPRING_SPEC: SpringSpec<Float> =
    spring(
        dampingRatio = ProgressAnimationSpecs.SPRING_DAMPING_RATIO,
        stiffness = ProgressAnimationSpecs.SPRING_STIFFNESS,
    )

private val PROGRESS_PULSE_SPEC: AnimationSpec<Float> =
    tween(
        durationMillis = 250,
        easing = FastOutSlowInEasing,
    )

private val PROGRESS_FILL_DURATION_MS = ProgressAnimationSpecs.PROGRESS_FILL_DURATION_MS
private val COMPACT_PROGRESS_DURATION_MS = ProgressAnimationSpecs.COMPACT_PROGRESS_DURATION_MS

/**
 * Progress state for tracking animations
 */
data class ProgressState(
    val currentWord: Int = 0,
    val totalWords: Int = 6,
    val progress: Float = 0f,
    val isCritical: Boolean = false,
    val combo: Int = 0,
    val earnedStars: List<Int> = emptyList(),
) {
    val remainingWords: Int get() = totalWords - currentWord
    val remainingPercent: Float get() = 1f - progress
    val isComplete: Boolean get() = currentWord >= totalWords

    companion object {
        fun initial(totalWords: Int = 6) =
            ProgressState(
                currentWord = 0,
                totalWords = totalWords,
                progress = 0f,
                isCritical = false,
                combo = 0,
                earnedStars = emptyList(),
            )
    }

    fun updateProgress(newWord: Int): ProgressState {
        val newProgress =
            if (totalWords > 0) {
                newWord.toFloat() / totalWords.toFloat()
            } else {
                0f
            }

        return copy(
            currentWord = newWord,
            progress = newProgress,
            isCritical = newWord < totalWords && (1f - newProgress) < 0.3f,
        )
    }

    fun addStar(stars: Int): ProgressState {
        val newStars =
            earnedStars.toMutableList().apply {
                if (currentWord <= size) {
                    add(currentWord - 1, stars)
                }
            }
        return copy(earnedStars = newStars)
    }

    fun updateCombo(newCombo: Int): ProgressState {
        return copy(combo = newCombo)
    }
}
