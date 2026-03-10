package com.wordland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Enhanced animated star rating display
 *
 * Features:
 * - Sequential star appearance animation
 * - Glow effect on earned stars
 * - Bouncy spring animation
 * - Optional celebration particles
 *
 * @param stars Number of filled stars (0-3)
 * @param maxStars Maximum number of stars to display (default 3)
 * @param starSize Size of each star icon
 * @param animated Whether to animate the stars (default true)
 * @param showGlow Whether to show glow effect (default true)
 * @param modifier Modifier for the component
 */
@Composable
fun StarRatingDisplay(
    stars: Int,
    maxStars: Int = 3,
    starSize: Dp = 32.dp,
    animated: Boolean = true,
    showGlow: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var animationTrigger by remember { mutableIntStateOf(0) }
    var revealedCount by remember { mutableIntStateOf(0) }

    // Trigger animation when stars change
    LaunchedEffect(stars) {
        if (animated) {
            animationTrigger++
            revealedCount = 0
            // Reveal stars sequentially
            for (i in 1..stars) {
                revealedCount = i
                delay(120)
            }
        } else {
            revealedCount = stars
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(starSize * 0.25f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(maxStars) { index ->
            val isEarned = index < stars.coerceAtMost(maxStars)
            val isRevealed = index < revealedCount

            EnhancedStar(
                isEarned = isEarned,
                isRevealed = isRevealed,
                starSize = starSize,
                showGlow = showGlow && isEarned && isRevealed,
                animationDelay = index * 120L,
                animationTrigger = animationTrigger,
            )
        }
    }
}

/**
 * Individual enhanced star with animation
 */
@Composable
private fun EnhancedStar(
    isEarned: Boolean,
    isRevealed: Boolean,
    starSize: Dp,
    showGlow: Boolean,
    animationDelay: Long,
    animationTrigger: Int,
) {
    var hasAnimated by remember { mutableStateOf(false) }

    // Scale animation with spring
    val scale by animateFloatAsState(
        targetValue =
            when {
                !isRevealed && !isEarned -> 1f
                isRevealed -> 1f
                else -> 0f
            },
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 200f,
            ),
        label = "star_scale",
    )

    // Rotation for entrance effect
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 0f else -180f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 250f,
            ),
        label = "star_rotation",
    )

    // Glow alpha animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (showGlow) 0.8f else 0f,
        animationSpec =
            tween(
                durationMillis = 400,
                easing = EaseOutCubic,
            ),
        label = "star_glow",
    )

    // Pulse effect for earned stars
    val pulseScale by animateFloatAsState(
        targetValue = if (isEarned && hasAnimated) 1f else 1.2f,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = EaseOutCubic,
            ),
        label = "star_pulse",
    )

    LaunchedEffect(animationTrigger) {
        if (isEarned) {
            delay(animationDelay)
            hasAnimated = true
        }
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        // Glow effect
        if (showGlow) {
            Box(
                modifier =
                    Modifier
                        .size(starSize * 1.4f)
                        .scale(pulseScale)
                        .alpha(glowAlpha * 0.5f)
                        .background(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            Color.Transparent,
                                        ),
                                ),
                            shape = CircleShape,
                        ),
            )
        }

        // Star icon
        val targetScale = if (isRevealed) scale * pulseScale else scale
        Icon(
            imageVector = if (isEarned) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = if (isEarned) "Star earned" else "Star not earned",
            tint =
                if (isEarned) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            modifier =
                Modifier
                    .size(starSize)
                    .scale(targetScale.coerceIn(0f, 1.5f))
                    .rotate(rotation),
        )
    }
}

/**
 * Compact star rating for smaller spaces
 * Simpler animation, no glow
 */
@Composable
fun CompactStarRating(
    stars: Int,
    maxStars: Int = 3,
    starSize: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
        label = "compact_stars",
    )

    Row(
        modifier = modifier.scale(scale),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(maxStars) { index ->
            val isEarned = index < stars.coerceAtMost(maxStars)
            val starScale by animateFloatAsState(
                targetValue = if (isEarned) 1f else 0.8f,
                animationSpec =
                    spring(
                        dampingRatio = 0.7f,
                        stiffness = 400f,
                    ),
                label = "compact_star_$index",
            )

            Icon(
                imageVector = if (isEarned) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint =
                    if (isEarned) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                modifier = Modifier.size(starSize).scale(starScale),
            )
        }
    }
}

/**
 * Level complete star celebration
 * Full screen star animation with particles
 */
@Composable
fun LevelCompleteStarAnimation(
    stars: Int,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
) {
    var revealedStars by remember { mutableIntStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(stars) {
        revealedStars = 0
        // Reveal stars one by one
        for (i in 1..stars) {
            revealedStars = i
            delay(200)
        }
        // Show confetti after all stars revealed
        if (stars == 3) {
            showConfetti = true
            delay(2000)
            showConfetti = false
        }
        delay(300)
        onAnimationComplete()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Stars
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                val isEarned = index < stars
                val isRevealed = index < revealedStars

                val scale by animateFloatAsState(
                    targetValue =
                        when {
                            !isEarned -> 0.8f
                            isRevealed -> 1f
                            else -> 0f
                        },
                    animationSpec =
                        spring(
                            dampingRatio = 0.4f,
                            stiffness = 180f,
                        ),
                    label = "level_star_$index",
                )

                val rotation by animateFloatAsState(
                    targetValue = if (isRevealed) 0f else -360f,
                    animationSpec =
                        tween(
                            durationMillis = 600,
                            easing = FastOutSlowInEasing,
                        ),
                    label = "level_star_rot_$index",
                )

                val glowAlpha by animateFloatAsState(
                    targetValue = if (isEarned && isRevealed) 1f else 0f,
                    animationSpec =
                        tween(
                            durationMillis = 300,
                            easing = EaseOutCubic,
                        ),
                    label = "level_star_glow_$index",
                )

                Box {
                    // Multi-layered glow
                    if (isEarned && isRevealed) {
                        listOf(1.5f, 1.3f, 1.1f).forEach { scaleMultiplier ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(56.dp * scaleMultiplier)
                                        .alpha(glowAlpha * 0.3f / scaleMultiplier)
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
                    }

                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint =
                            if (isEarned) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        modifier =
                            Modifier
                                .size(56.dp)
                                .scale(scale)
                                .rotate(rotation),
                    )
                }
            }
        }

        // Celebration message
        if (revealedStars == stars && stars > 0) {
            val messageAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec =
                    tween(
                        durationMillis = 500,
                        delayMillis = 200,
                        easing = EaseOutCubic,
                    ),
                label = "message_alpha",
            )

            val messageScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.6f,
                        stiffness = 250f,
                    ),
                label = "message_scale",
            )

            androidx.compose.material3.Text(
                text =
                    when (stars) {
                        3 -> "完美！"
                        2 -> "很棒！"
                        1 -> "不错！"
                        else -> ""
                    },
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.tertiary,
                    ),
                modifier =
                    Modifier
                        .alpha(messageAlpha)
                        .scale(messageScale)
                        .offset(y = 80.dp),
            )
        }
    }
}
