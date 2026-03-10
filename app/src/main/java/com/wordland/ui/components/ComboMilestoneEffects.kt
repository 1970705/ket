package com.wordland.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Combo milestone celebration effects
 *
 * Features:
 * - Glowing ring animation for milestones (3, 5, 10 combo)
 * - Pulse animation (scale + alpha)
 * - Rotating border effect
 * - Fire emoji intensification
 *
 * Per VISUAL_FEEDBACK_DESIGN.md Section 5:
 * - 3 combo: Small glow + gentle pulse
 * - 5 combo: Medium glow + strong pulse + ring
 * - 10+ combo: Large glow + screen shake + rotating ring
 *
 * @param combo Current combo count
 * @param previousCombo Previous combo count (for animation triggers)
 * @param modifier Compose modifier
 * @param reduceMotion Whether to reduce motion
 *
 * @since 1.6 (Epic #8.3)
 */
@Composable
fun ComboMilestoneGlow(
    combo: Int,
    previousCombo: Int = combo,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    if (reduceMotion || combo < 3) {
        // No effect for low combo or reduced motion
        return
    }

    val tier = getMilestoneTier(combo)
    val previousTier = getMilestoneTier(previousCombo)
    val isMilestoneReached = tier > previousTier

    // Pulse animation (always active for tier 2+)
    val pulseScale by animateFloatAsState(
        targetValue = if (tier >= 2) 1.1f else 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 800,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "combo_pulse_scale",
    )

    // Glow alpha
    val glowAlpha by animateFloatAsState(
        targetValue =
            when {
                tier >= 3 -> 0.6f
                tier >= 2 -> 0.4f
                else -> 0.2f
            },
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 1000,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "combo_glow_alpha",
    )

    // Rotation for tier 3
    val rotation by animateFloatAsState(
        targetValue = if (tier >= 3) 360f else 0f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 3000,
                        easing = FastOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "combo_rotation",
    )

    // Milestone burst animation
    var burstScale by remember { mutableStateOf(0f) }

    LaunchedEffect(isMilestoneReached) {
        if (isMilestoneReached) {
            // Animate burst effect
            burstScale = 1.5f
            delay(100)
            burstScale = 1f
        }
    }

    val finalScale = pulseScale * burstScale

    Box(
        modifier =
            modifier
                .scale(finalScale)
                .rotate(rotation),
    ) {
        // Outer glow ring
        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .alpha(glowAlpha)
                    .background(
                        color =
                            getTierColor(tier).copy(
                                alpha = 0.15f,
                            ),
                        shape = CircleShape,
                    ),
        )

        // Rotating border ring (tier 3 only)
        if (tier >= 3) {
            Box(
                modifier =
                    Modifier
                        .size(76.dp)
                        .scale(pulseScale)
                        .alpha(glowAlpha)
                        .rotate(rotation)
                        .background(
                            brush =
                                Brush.sweepGradient(
                                    colors =
                                        listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFF6B35),
                                            Color(0xFFFF4500),
                                            Color(0xFFFFD700),
                                        ),
                                ),
                            shape = CircleShape,
                        ),
            )
        }
    }
}

/**
 * Combo milestone popup notification
 *
 * Shows a celebratory popup when reaching combo milestones
 *
 * @param combo Current combo count
 * @param onDismiss Callback to dismiss the popup
 * @param modifier Compose modifier
 */
@Composable
fun ComboMilestonePopup(
    combo: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tier = getMilestoneTier(combo)

    // Check if milestone is reached (3, 5, 10)
    val isMilestone =
        when (combo) {
            3, 5, 10, 15, 20, 25, 30 -> true
            else -> false
        }

    if (!isMilestone) {
        onDismiss()
        return
    }

    var isVisible by remember { mutableStateOf(true) }
    var animationPhase by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue =
            when (animationPhase) {
                0 -> 0.5f
                1 -> 1.2f
                else -> 1f
            },
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 300f,
            ),
        label = "popup_scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "popup_alpha",
    )

    LaunchedEffect(Unit) {
        // Entrance animation
        animationPhase = 1
        delay(100)
        animationPhase = 2

        // Hold and dismiss
        delay(2000)
        isVisible = false
        delay(300)
        onDismiss()
    }

    if (isVisible) {
        val message =
            when (combo) {
                3 -> "3 Combo! 🔥"
                5 -> "5 Combo! 🔥🔥"
                10 -> "10 Combo! UNSTOPPABLE! 🔥🔥🔥"
                15 -> "15 Combo! LEGENDARY! 🌟"
                20 -> "20 Combo! INCREDIBLE! 💫"
                25 -> "25 Combo! EPIC! ⚡"
                30 -> "30 Combo! GODLIKE! 👑"
                else -> "$combo Combo!"
            }

        val color =
            when {
                combo >= 20 -> Color(0xFFFFD700) // Gold
                combo >= 10 -> Color(0xFFFF4500) // Red-Orange
                combo >= 5 -> Color(0xFFFF6B35) // Orange
                else -> Color(0xFFFFB347) // Yellow-Orange
            }

        Box(
            modifier =
                modifier
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        color = color.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize =
                    when {
                        combo >= 10 -> 24.sp
                        combo >= 5 -> 20.sp
                        else -> 18.sp
                    },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Enhanced combo counter with milestone effects
 *
 * @param combo Current combo count
 * @param modifier Compose modifier
 * @param reduceMotion Whether to reduce motion
 */
@Composable
fun EnhancedComboCounter(
    combo: Int,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
) {
    if (combo <= 0) return

    val tier = getMilestoneTier(combo)

    // Scale animation on combo change
    var displayedCombo by remember { mutableIntStateOf(0) }

    LaunchedEffect(combo) {
        displayedCombo = combo
    }

    val scale by animateFloatAsState(
        targetValue = if (displayedCombo > 0) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = 0.5f,
                stiffness = 300f,
            ),
        label = "counter_scale",
    )

    // Rotation for fire emojis at high combo
    val rotation by animateFloatAsState(
        targetValue = if (tier >= 3 && !reduceMotion) 15f else 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "fire_rotation",
    )

    val color = getTierColor(tier)

    Box(
        modifier =
            modifier
                .scale(scale)
                .background(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                )
                .then(
                    if (tier >= 2) {
                        Modifier.border(
                            width = 2.dp,
                            color = color.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
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

            repeat(fireCount) { index ->
                val fireRotation = rotation * (index + 1)
                Text(
                    text = "🔥",
                    modifier =
                        Modifier
                            .rotate(fireRotation)
                            .size(20.dp),
                    fontSize = 16.sp,
                )
            }

            Text(
                text = "${combo}x",
                color = color,
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
        }
    }
}

/**
 * Get milestone tier based on combo count
 */
private fun getMilestoneTier(combo: Int): Int {
    return when {
        combo >= 10 -> 3
        combo >= 5 -> 2
        combo >= 3 -> 1
        else -> 0
    }
}

/**
 * Get color based on milestone tier
 */
@Composable
private fun getTierColor(tier: Int): Color {
    return when (tier) {
        3 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFFF6B35) // Orange
        1 -> Color(0xFFFFB347) // Yellow-Orange
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Remember mutable float state
 */
@Composable
private fun rememberMutableFloatStateOf(initialValue: Float): androidx.compose.runtime.MutableState<Float> =
    remember { androidx.compose.runtime.mutableStateOf(initialValue) }
