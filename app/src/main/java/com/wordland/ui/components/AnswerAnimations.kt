package com.wordland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animation state for answer feedback
 */
enum class FeedbackAnimationState {
    Idle,
    Showing,
    Completed,
}

/**
 * Enhanced correct answer animation component
 * Shows checkmark with scale, rotation, and glow effects
 *
 * Features:
 * - Bouncy scale animation (0 -> 1.3 -> 1.0)
 * - Rotation effect
 * - Pulsing glow background
 * - Smooth fade in
 */
@Composable
fun CorrectAnswerAnimation(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
) {
    var animationState by remember { mutableStateOf(FeedbackAnimationState.Idle) }

    // Multi-stage scale animation
    val scale by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0f
                FeedbackAnimationState.Showing -> 1.3f
                FeedbackAnimationState.Completed -> 1f
            },
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 350f,
            ),
        label = "correct_scale",
    )

    // Alpha animation with delay for dramatic effect
    val alpha by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0f
                FeedbackAnimationState.Showing -> 1f
                FeedbackAnimationState.Completed -> 1f
            },
        animationSpec = tween(durationMillis = 300, easing = EaseOutCubic),
        label = "correct_alpha",
    )

    // Subtle rotation for dynamic effect
    val rotation by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> -15f
                FeedbackAnimationState.Showing -> 0f
                FeedbackAnimationState.Completed -> 0f
            },
        animationSpec =
            tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing,
            ),
        label = "correct_rotation",
    )

    // Pulsing glow effect
    val glowScale by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0.5f
                FeedbackAnimationState.Showing -> 1.2f
                FeedbackAnimationState.Completed -> 1f
            },
        animationSpec =
            tween(
                durationMillis = 600,
                easing = EaseInOutCubic,
            ),
        label = "glow_scale",
    )

    // Glow alpha
    val glowAlpha by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0f
                FeedbackAnimationState.Showing -> 0.6f
                FeedbackAnimationState.Completed -> 0.3f
            },
        animationSpec =
            tween(
                durationMillis = 500,
                easing = EaseOutCubic,
            ),
        label = "glow_alpha",
    )

    LaunchedEffect(Unit) {
        animationState = FeedbackAnimationState.Showing
        // Auto transition to completed after bounce
        delay(400)
        animationState = FeedbackAnimationState.Completed
        delay(200)
        onAnimationComplete()
    }

    Box(
        modifier =
            modifier
                .size(140.dp)
                .scale(scale)
                .alpha(alpha),
        contentAlignment = Alignment.Center,
    ) {
        // Outer glow ring
        Box(
            modifier =
                Modifier
                    .size(130.dp)
                    .scale(glowScale)
                    .alpha(glowAlpha)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
        )

        // Main background circle with gradient
        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .rotate(rotation)
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Correct",
                tint = Color.White,
                modifier = Modifier.size(60.dp),
            )
        }

        // Sparkle particles
        if (animationState != FeedbackAnimationState.Idle) {
            SparkleParticles(
                isActive = animationState == FeedbackAnimationState.Showing,
                color = Color.White,
            )
        }
    }
}

/**
 * Enhanced incorrect answer animation component
 * Shows X with multi-stage shake animation and red glow
 *
 * Features:
 * - Multi-direction shake (left-right-left-right)
 * - Red glow effect
 * - Fade in with slight delay
 */
@Composable
fun IncorrectAnswerAnimation(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
) {
    var animationState by remember { mutableStateOf(FeedbackAnimationState.Idle) }
    var shakeIteration by remember { mutableIntStateOf(0) }

    // Multi-stage shake animation
    val shakeOffset by animateFloatAsState(
        targetValue =
            when (shakeIteration) {
                0 -> 0f
                1 -> -30f // Left
                2 -> 30f // Right
                3 -> -20f // Left (smaller)
                4 -> 20f // Right (smaller)
                5 -> -10f // Left (smallest)
                6 -> 10f // Right (smallest)
                else -> 0f // Center
            },
        animationSpec =
            tween(
                durationMillis = 80,
                easing = LinearEasing,
            ),
        label = "shake_offset",
    )

    // Scale with bounce on error
    val scale by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0.8f
                FeedbackAnimationState.Showing -> 1.1f
                FeedbackAnimationState.Completed -> 1f
            },
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 400f,
            ),
        label = "incorrect_scale",
    )

    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0f
                FeedbackAnimationState.Showing -> 1f
                FeedbackAnimationState.Completed -> 1f
            },
        animationSpec = tween(durationMillis = 200),
        label = "incorrect_alpha",
    )

    // Red glow pulse
    val glowAlpha by animateFloatAsState(
        targetValue =
            when (animationState) {
                FeedbackAnimationState.Idle -> 0f
                FeedbackAnimationState.Showing -> 0.5f
                FeedbackAnimationState.Completed -> 0.2f
            },
        animationSpec =
            tween(
                durationMillis = 400,
                easing = EaseOutCubic,
            ),
        label = "error_glow",
    )

    LaunchedEffect(Unit) {
        animationState = FeedbackAnimationState.Showing

        // Animate shake iterations
        for (i in 1..6) {
            delay(80)
            shakeIteration = i
        }
        delay(80)
        shakeIteration = 0
        animationState = FeedbackAnimationState.Completed
        delay(100)
        onAnimationComplete()
    }

    Box(
        modifier =
            modifier
                .size(140.dp)
                .offset(x = shakeOffset.dp)
                .scale(scale)
                .alpha(alpha),
        contentAlignment = Alignment.Center,
    ) {
        // Error glow
        Box(
            modifier =
                Modifier
                    .size(130.dp)
                    .alpha(glowAlpha)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        Color.Transparent,
                                    ),
                            ),
                        shape = CircleShape,
                    ),
        )

        // Main background
        Box(
            modifier =
                Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Incorrect",
                tint = Color.White,
                modifier = Modifier.size(60.dp),
            )
        }
    }
}

/**
 * Enhanced star earned animation
 * Animated stars appearing with sequential delay and glow effect
 *
 * Features:
 * - Sequential star appearance (100ms delay between each)
 * - Scale + rotation animation
 * - Glow effect on earned stars
 */
@Composable
fun StarEarnedAnimation(
    stars: Int,
    modifier: Modifier = Modifier,
) {
    var animationPlayed by remember { mutableStateOf(false) }
    var currentStar by remember { mutableIntStateOf(0) }

    // Animate stars sequentially
    LaunchedEffect(stars) {
        animationPlayed = true
        currentStar = 0
        for (i in 1..stars) {
            currentStar = i
            delay(150) // Delay between stars
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val isEarned = index < stars
            val isRevealed = index < currentStar
            val starProgress = (currentStar - index).coerceAtLeast(0).toFloat()

            val scale by animateFloatAsState(
                targetValue = if (isRevealed) 1f else 0f,
                animationSpec =
                    spring(
                        dampingRatio = 0.5f,
                        stiffness = 250f,
                    ),
                label = "star_scale_$index",
            )

            val rotation by animateFloatAsState(
                targetValue = if (isRevealed) 0f else -180f,
                animationSpec =
                    spring(
                        dampingRatio = 0.6f,
                        stiffness = 300f,
                    ),
                label = "star_rotation_$index",
            )

            val glowAlpha by animateFloatAsState(
                targetValue =
                    when {
                        isEarned && isRevealed -> 0.6f
                        else -> 0f
                    },
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = EaseOutCubic,
                    ),
                label = "star_glow_$index",
            )

            Box(
                contentAlignment = Alignment.Center,
            ) {
                // Glow effect
                if (isEarned) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .alpha(glowAlpha)
                                .background(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                                    Color.Transparent,
                                                ),
                                        ),
                                    shape = CircleShape,
                                ),
                    )
                }

                // Star icon
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = if (isEarned) "Star earned" else "Star not earned",
                    tint =
                        if (isEarned) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    modifier =
                        Modifier
                            .size(48.dp)
                            .scale(scale)
                            .rotate(rotation),
                )
            }
        }
    }
}

/**
 * Sparkle particles for celebration effect
 */
@Composable
private fun SparkleParticles(
    isActive: Boolean,
    color: Color = Color.White,
) {
    if (!isActive) return

    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animationProgress = 0f
        // Animate over 500ms
        val startTime = System.currentTimeMillis()
        while (animationProgress < 1f) {
            animationProgress = (System.currentTimeMillis() - startTime) / 500f
            delay(16)
        }
    }

    val alpha = ((1f - animationProgress) * 0.8f).coerceIn(0f, 1f)

    // Create 8 sparkle positions in a circle
    val sparkleCount = 8
    val radius = 60.dp

    Box(modifier = Modifier.size(140.dp)) {
        repeat(sparkleCount) { index ->
            val angle = (index * 360f / sparkleCount) * (Math.PI / 180f)
            val distance = animationProgress * 80

            val offsetX = (kotlin.math.cos(angle) * distance).dp
            val offsetY = (kotlin.math.sin(angle) * distance).dp

            val scale = ((1f - animationProgress) * 1.5f).coerceIn(0f, 1f)

            Box(
                modifier =
                    Modifier
                        .offset(x = offsetX, y = offsetY)
                        .size(8.dp)
                        .scale(scale)
                        .alpha(alpha)
                        .background(
                            color = color,
                            shape = CircleShape,
                        ),
            )
        }
    }
}

/**
 * Enhanced memory strength change animation
 * Shows animated progress bar with color transition
 */
@Composable
fun MemoryStrengthChangeAnimation(
    oldStrength: Int,
    newStrength: Int,
    modifier: Modifier = Modifier,
) {
    var currentStrength by remember { mutableIntStateOf(oldStrength) }
    var hasAnimated by remember { mutableStateOf(false) }

    val animatedStrength by animateIntAsState(
        targetValue = newStrength,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "strength_animation",
    )

    // Progress color based on strength level
    val progressColor by animateColorAsState(
        targetValue =
            when {
                animatedStrength >= 80 -> Color(0xFF4CAF50)
                animatedStrength >= 50 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
        animationSpec = tween(durationMillis = 500),
        label = "strength_color",
    )

    // Glow effect when strength increases
    val showGlow = remember(newStrength, oldStrength) { newStrength > oldStrength }
    val glowAlpha by animateFloatAsState(
        targetValue = if (showGlow && !hasAnimated) 0.5f else 0f,
        animationSpec =
            tween(
                durationMillis = 800,
                easing = EaseOutCubic,
            ),
        label = "strength_glow",
    )

    LaunchedEffect(newStrength) {
        currentStrength = newStrength
        if (showGlow) {
            delay(500)
            hasAnimated = true
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "记忆强度",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "$animatedStrength%",
                style = MaterialTheme.typography.titleMedium,
                color = progressColor,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Glow background
            if (showGlow) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .alpha(glowAlpha)
                            .background(
                                color = progressColor.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small,
                            ),
                )
            }

            LinearProgressIndicator(
                progress = animatedStrength.toFloat() / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
            )
        }
    }
}

/**
 * Quick feedback pulse animation for interactive elements
 */
@Composable
fun PulseAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (scale: Float, alpha: Float) -> Unit,
) {
    var pulseCount by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
        label = "pulse_scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.7f,
        animationSpec =
            tween(
                durationMillis = 200,
                easing = EaseOutCubic,
            ),
        label = "pulse_alpha",
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            pulseCount++
        }
    }

    Box(modifier = modifier) {
        content(scale, alpha)
    }
}
