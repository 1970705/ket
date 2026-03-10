package com.wordland.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wordland.domain.model.AnimationQuality
import com.wordland.domain.model.FeedbackType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Word completion feedback component with tiered animations
 *
 * Part of P0 Phase 1: Visual Feedback Enhancements
 *
 * Animations based on FeedbackType:
 * - Perfect (3★): Simplified confetti + star burst
 * - Good (2★): Flash/shimmer effect
 * - Correct (1★): Simple checkmark animation
 * - Incorrect: Shake effect
 *
 * Performance-aware: Respects AnimationQuality settings
 */
@Composable
fun WordCompletionFeedback(
    feedbackType: FeedbackType,
    quality: AnimationQuality = AnimationQuality.Medium,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var animationPlayed by remember { mutableStateOf(false) }

    // Trigger animation on first composition
    LaunchedEffect(feedbackType) {
        animationPlayed = true
    }

    when (feedbackType) {
        is FeedbackType.PerfectThreeStar -> {
            PerfectFeedbackAnimation(
                quality = quality,
                onAnimationComplete = onAnimationComplete,
                modifier = modifier,
            )
        }
        is FeedbackType.GoodTwoStar -> {
            GoodFeedbackAnimation(
                quality = quality,
                onAnimationComplete = onAnimationComplete,
                modifier = modifier,
            )
        }
        is FeedbackType.CorrectOneStar -> {
            CorrectFeedbackAnimation(
                quality = quality,
                onAnimationComplete = onAnimationComplete,
                modifier = modifier,
            )
        }
        is FeedbackType.Incorrect -> {
            IncorrectFeedbackAnimation(
                quality = quality,
                onAnimationComplete = onAnimationComplete,
                modifier = modifier,
            )
        }
    }
}

/**
 * Perfect feedback - Simplified confetti with star burst
 * Performance-optimized particle system
 */
@Composable
private fun PerfectFeedbackAnimation(
    quality: AnimationQuality,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!quality.animationEnabled) {
        StaticFeedback(
            icon = { PerfectIcon() },
            message = "完美！",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = modifier,
        )
        return
    }

    val duration = (800 * quality.durationMultiplier).toInt()
    var hasCompleted by remember { mutableStateOf(false) }

    // Animation progress
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            tween(
                durationMillis = duration,
                easing = EaseOutCubic,
            ),
        label = "perfect_progress",
    )

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (progress > 0) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 300f,
            ),
        label = "perfect_scale",
    )

    LaunchedEffect(progress) {
        if (progress >= 1f && !hasCompleted) {
            hasCompleted = true
            onAnimationComplete()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Confetti particles (simplified)
        if (quality.particleCount > 0) {
            SimplifiedConfetti(
                particleCount = quality.particleCount,
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Star burst icon
            Box(
                modifier =
                    Modifier
                        .size(120.dp)
                        .scale(scale),
                contentAlignment = Alignment.Center,
            ) {
                PerfectIcon()
            }

            // Message
            Text(
                text = "完美！",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

/**
 * Good feedback - Flash/shimmer effect
 */
@Composable
private fun GoodFeedbackAnimation(
    quality: AnimationQuality,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = (500 * quality.durationMultiplier).toInt()
    var hasCompleted by remember { mutableStateOf(false) }

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 350f,
            ),
        label = "good_scale",
    )

    // Shimmer alpha
    val shimmerAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing,
            ),
        label = "good_shimmer",
    )

    LaunchedEffect(shimmerAlpha) {
        if (shimmerAlpha >= 1f && !hasCompleted) {
            hasCompleted = true
            onAnimationComplete()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(100.dp)
                        .scale(scale)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * shimmerAlpha),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f * shimmerAlpha),
                                            Color.Transparent,
                                        ),
                                ),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                GoodIcon()
            }

            Text(
                text = "很好！",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Correct feedback - Simple checkmark
 */
@Composable
private fun CorrectFeedbackAnimation(
    quality: AnimationQuality,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = (400 * quality.durationMultiplier).toInt()
    var hasCompleted by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = 0.7f,
                stiffness = 400f,
            ),
        label = "correct_scale",
    )

    LaunchedEffect(scale) {
        if (scale >= 1f && !hasCompleted) {
            hasCompleted = true
            onAnimationComplete()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .scale(scale),
                contentAlignment = Alignment.Center,
            ) {
                CorrectIcon()
            }

            Text(
                text = "正确！",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Incorrect feedback - Shake effect
 */
@Composable
private fun IncorrectFeedbackAnimation(
    quality: AnimationQuality,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = (300 * quality.durationMultiplier).toInt()
    var hasCompleted by remember { mutableStateOf(false) }

    // One-time shake using Offset animation
    var shakeCount by remember { mutableIntStateOf(0) }

    val offsetX by animateIntAsState(
        targetValue = if (shakeCount > 0) 0 else 10,
        animationSpec =
            spring(
                dampingRatio = 0.3f,
                stiffness = 400f,
            ),
        label = "shake_offset",
        finishedListener = {
            if (shakeCount == 1) {
                shakeCount = 2
            } else if (shakeCount == 2) {
                shakeCount = 3
                hasCompleted = true
                onAnimationComplete()
            }
        },
    )

    LaunchedEffect(Unit) {
        if (quality.animationEnabled) {
            shakeCount = 1
        } else {
            hasCompleted = true
            onAnimationComplete()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp)
                .offset(x = if (shakeCount in 1..2) offsetX.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                IncorrectIcon()
            }

            Text(
                text = "再试一次！",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Static feedback for low quality mode
 */
@Composable
private fun StaticFeedback(
    icon: @Composable () -> Unit,
    message: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = color,
            )
        }
    }
}

/**
 * Simplified confetti particle system
 * Performance-optimized: Uses Canvas instead of individual composables
 */
@Composable
private fun SimplifiedConfetti(
    particleCount: Int,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val confettiColors =
        listOf(
            Color(0xFFFF6B6B), // Red
            Color(0xFF4ECDC4), // Teal
            Color(0xFFFFD93D), // Yellow
            Color(0xFF6BCB77), // Green
            Color(0xFF4D96FF), // Blue
            Color(0xFF9B59B6), // Purple
        )

    // Pre-generate particles with random properties
    val particles =
        remember(particleCount) {
            List(particleCount) {
                BurstConfettiParticle(
                    color = confettiColors.random(),
                    startX = Random.nextFloat(),
                    startY = Random.nextFloat(),
                    angle = Random.nextFloat() * 360f,
                    speed = Random.nextFloat() * 0.5f + 0.5f,
                    size = Random.nextFloat() * 8f + 4f,
                )
            }
        }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val animatedProgress = (progress * particle.speed).coerceIn(0f, 1f)
            val distance = animatedProgress * height * 0.6f
            val angleRad = particle.angle * Math.PI / 180

            val x = particle.startX * width + cos(angleRad).toFloat() * distance
            val y = particle.startY * height + sin(angleRad).toFloat() * distance + animatedProgress * height * 0.3f

            val alpha = ((1f - animatedProgress) * 0.8f).coerceIn(0f, 1f)
            val size = particle.size * (1f - animatedProgress * 0.3f)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = size / 2,
                center = Offset(x, y),
            )
        }
    }
}

/**
 * Confetti particle data
 */
private data class BurstConfettiParticle(
    val color: Color,
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
)

/**
 * Perfect icon - 3 stars
 */
@Composable
private fun PerfectIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/**
 * Good icon - 2 stars
 */
@Composable
private fun GoodIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/**
 * Correct icon - 1 star/check
 */
@Composable
private fun CorrectIcon() {
    Box(
        modifier =
            Modifier
                .size(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
    }
}

/**
 * Incorrect icon - X
 */
@Composable
private fun IncorrectIcon() {
    Box(
        modifier =
            Modifier
                .size(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "✕",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
