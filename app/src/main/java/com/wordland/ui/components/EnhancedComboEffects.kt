package com.wordland.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Enhanced combo visual effects component
 *
 * Story #1.3: Combo Visual Effects
 *
 * Features:
 * - Progressive fire effects (3/5/10+ combo tiers)
 * - Screen shake for high combos
 * - Pulse animation on combo increment
 * - Dynamic color transitions
 * - Multiplier badges
 *
 * Per VISUAL_FEEDBACK_DESIGN.md Section 5:
 * - 3 combo: Small fire + gentle pulse
 * - 5 combo: Medium fire + strong pulse
 * - 10+ combo: Large fire + screen shake
 */
@Composable
fun EnhancedComboIndicator(
    combo: Int,
    maxCombo: Int = combo,
    onComboReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (combo <= 0) return

    // Get combo tier configuration
    val tier = getComboTier(combo)

    // Track for animation triggers
    var previousCombo by remember { mutableIntStateOf(0) }

    // Update previous combo after animation checks
    LaunchedEffect(combo) {
        if (combo > previousCombo) {
            previousCombo = combo
        }
    }

    // Scale animation for combo changes
    val scale = remember { Animatable(1f) }

    LaunchedEffect(combo) {
        if (combo > previousCombo) {
            // Pop effect
            scale.animateTo(
                targetValue = 1.4f,
                animationSpec =
                    spring(
                        dampingRatio = 0.4f,
                        stiffness = 300f,
                    ),
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.6f,
                        stiffness = 250f,
                    ),
            )
        }
    }

    // Pulse animation for active combo
    val pulseScale by animateFloatAsState(
        targetValue = if (combo > previousCombo) 1.1f else 1f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 200f,
            ),
        label = "pulse_scale",
    )

    // Rotation animation for fire emojis (high combo)
    val rotation by animateFloatAsState(
        targetValue = if (tier >= 2) 10f else 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "fire_rotation",
    )

    // Color animation based on tier
    val containerColor by animateColorAsState(
        targetValue = getTierColor(tier),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "container_color",
    )

    val contentColor by animateColorAsState(
        targetValue = getTierContentColor(tier),
        animationSpec = tween(300),
        label = "content_color",
    )

    Box(
        modifier =
            modifier
                .scale(scale.value * pulseScale)
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(20.dp),
                )
                .then(
                    // Add glowing border for high combos
                    if (tier >= 2) {
                        Modifier.border(
                            width = 2.dp,
                            color = getTierGlowColor(tier),
                            shape = RoundedCornerShape(20.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Combo count
            Text(
                text = "${combo}x",
                color = contentColor,
                fontSize =
                    when (tier) {
                        3 -> 24.sp
                        2 -> 22.sp
                        else -> 18.sp
                    },
                fontWeight =
                    when (tier) {
                        3 -> FontWeight.ExtraBold
                        2 -> FontWeight.Bold
                        else -> FontWeight.SemiBold
                    },
            )

            // Fire emojis based on tier
            val fireCount =
                when (tier) {
                    3 -> 3
                    2 -> 2
                    1 -> 1
                    else -> 0
                }

            repeat(fireCount) { index ->
                val fireScale = 1f + (index * 0.1f)
                Text(
                    text = "🔥",
                    modifier =
                        Modifier
                            .scale(fireScale)
                            .rotate(rotation * (index + 1)),
                    fontSize = 18.sp,
                )
            }

            // Multiplier badge for high combos
            if (tier >= 1) {
                val multiplierText =
                    when (tier) {
                        3 -> "×2.0"
                        2 -> "×1.5"
                        1 -> "×1.2"
                        else -> ""
                    }

                if (multiplierText.isNotEmpty()) {
                    Text(
                        text = multiplierText,
                        modifier =
                            Modifier
                                .background(
                                    color = contentColor.copy(alpha = 0.25f),
                                    shape = CircleShape,
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * Screen shake effect for high combos
 */
@Composable
fun ComboShakeEffect(
    enabled: Boolean,
    intensity: Float = 1f,
    content: @Composable () -> Unit,
) {
    if (!enabled || intensity <= 0f) {
        content()
        return
    }

    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(enabled) {
        if (enabled) {
            // Quick shake sequence
            val shakeAmount = 8f * intensity
            repeat(3) {
                offsetX.animateTo(
                    targetValue = shakeAmount,
                    animationSpec = tween(50),
                )
                offsetX.animateTo(
                    targetValue = -shakeAmount,
                    animationSpec = tween(50),
                )
            }
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(100),
            )
        }
    }

    Box(
        modifier =
            Modifier
                .offset(x = offsetX.value.dp),
    ) {
        content()
    }
}

/**
 * Combo milestone celebration popup
 */
@Composable
fun ComboMilestonePopup(
    combo: Int,
    onDismiss: () -> Unit = {},
) {
    val tier = getComboTier(combo)

    val scale = remember { Animatable(0f) }

    LaunchedEffect(combo) {
        // Entrance animation
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec =
                spring(
                    dampingRatio = 0.5f,
                    stiffness = 200f,
                ),
        )

        // Hold and exit
        delay(1500)
        scale.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(200),
        )
        onDismiss()
    }

    val message =
        when (tier) {
            3 -> "UNSTOPPABLE! 🔥🔥🔥"
            2 -> "ON FIRE! 🔥🔥"
            1 -> "GREAT STREAK! 🔥"
            else -> ""
        }

    val color =
        when (tier) {
            3 -> Color(0xFFFF0000)
            2 -> Color(0xFFFF6B35)
            1 -> Color(0xFFFFB347)
            else -> MaterialTheme.colorScheme.primary
        }

    if (message.isNotEmpty()) {
        Box(
            modifier =
                Modifier
                    .scale(scale.value)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize =
                    when (tier) {
                        3 -> 28.sp
                        2 -> 24.sp
                        else -> 20.sp
                    },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Combo tier enumeration
 */
enum class ComboTier {
    LOW, // 1-2 combo (tier 0)
    MEDIUM, // 3-4 combo (tier 1)
    HIGH, // 5-9 combo (tier 2)
    EXTREME, // 10+ combo (tier 3)
}

/**
 * Get combo tier based on count
 */
private fun getComboTier(combo: Int): Int {
    return when {
        combo >= 10 -> 3 // EXTREME
        combo >= 5 -> 2 // HIGH
        combo >= 3 -> 1 // MEDIUM
        else -> 0 // LOW
    }
}

/**
 * Get configuration for combo tier
 */
@Composable
private fun getTierColor(tier: Int): Color {
    return when (tier) {
        3 -> Color(0xFFFF0000) // Red-Orange
        2 -> Color(0xFFFF6B35) // Fire Orange
        1 -> Color(0xFFFFB347) // Warm Orange
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun getTierContentColor(tier: Int): Color {
    return when (tier) {
        3, 2, 1 -> Color.White
        else -> MaterialTheme.colorScheme.onPrimary
    }
}

@Composable
private fun getTierGlowColor(tier: Int): Color {
    return when (tier) {
        3 -> Color(0xFFFF4500)
        2 -> Color(0xFFFF8C00)
        1 -> Color(0xFFFFD93D)
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Combo animation state for coordinating multiple effects
 */
data class ComboAnimationState(
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val isPulseActive: Boolean = false,
    val isShakeActive: Boolean = false,
    val pulseIntensity: Float = 0f,
    val shakeIntensity: Float = 0f,
) {
    val tier: Int
        get() =
            when {
                combo >= 10 -> 3
                combo >= 5 -> 2
                combo >= 3 -> 1
                else -> 0
            }

    val shouldShowFire: Boolean
        get() = combo >= 3

    val shouldShowShake: Boolean
        get() = combo >= 10

    val shouldShowPulse: Boolean
        get() = combo >= 3

    val multiplier: Float
        get() =
            when {
                combo >= 10 -> 2.0f
                combo >= 5 -> 1.5f
                combo >= 3 -> 1.2f
                else -> 1.0f
            }
}
