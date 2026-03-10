package com.wordland.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Celebration animation component for level completion
 *
 * Features:
 * - Differential feedback by star rating (3/2/1 star)
 * - Staggered star reveal animation
 * - Confetti particle effects
 * - Celebration messages
 * - Performance-optimized (60fps target)
 *
 * Per VISUAL_FEEDBACK_DESIGN.md Section 6:
 * - 3-star: Full confetti, 1200ms, victory fanfare
 * - 2-star: Mini confetti, 800ms, cheer chime
 * - 1-star: Single star fade, 500ms, gentle ding
 *
 * @param stars Number of stars earned (0-3)
 * @param score Final score
 * @param combo Max combo achieved
 * @param onAnimationComplete Callback when animation completes
 * @param modifier Compose modifier
 */
@Composable
fun CelebrationAnimation(
    stars: Int,
    score: Int = 0,
    combo: Int = 0,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Get celebration config based on stars
    val config = getCelebrationConfig(stars)

    // Track star reveal animation
    val visibleStars = remember { mutableStateListOf(false, false, false) }
    var animationPhase by remember { mutableStateOf(CelebrationPhase.STARS) }

    // Start animation sequence
    LaunchedEffect(stars) {
        // Reset
        repeat(3) { visibleStars[it] = false }
        animationPhase = CelebrationPhase.STARS

        // Phase 1: Star reveal (staggered)
        repeat(stars) { index ->
            delay(STAR_REVEAL_DELAY_MS)
            visibleStars[index] = true
        }

        // Wait for star animation to complete
        delay(config.starRevealDuration)

        // Phase 2: Message and confetti
        if (stars > 0) {
            animationPhase = CelebrationPhase.MESSAGE
            delay(config.messageDuration)
        }

        onAnimationComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            // Stars display
            StarsDisplay(
                visibleStars = visibleStars,
                stars = stars,
                animationPhase = animationPhase,
            )

            // Celebration message
            CelebrationMessage(
                stars = stars,
                score = score,
                combo = combo,
                animationPhase = animationPhase,
                config = config,
            )
        }

        // Confetti overlay (only for 2+ stars)
        if (animationPhase == CelebrationPhase.MESSAGE && config.showConfetti) {
            ConfettiEffect(
                particleCount = config.confettiCount,
                durationMillis = config.confettiDuration,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Stars display with staggered reveal animation
 */
@Composable
private fun StarsDisplay(
    visibleStars: List<Boolean>,
    stars: Int,
    animationPhase: CelebrationPhase,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        repeat(3) { index ->
            val isRevealed = visibleStars[index]
            val isEarned = index < stars

            val scale = remember { Animatable(0f) }
            val rotation = remember { Animatable(-180f) }

            LaunchedEffect(isRevealed) {
                if (isRevealed) {
                    // Parallel animation for performance
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = STAR_SPRING_SPEC,
                    )
                    rotation.animateTo(
                        targetValue = 0f,
                        animationSpec = STAR_ROTATION_SPEC,
                    )
                }
            }

            // Glow effect for earned stars
            val glowAlpha by animateFloatAsState(
                targetValue = if (isRevealed && isEarned) 0.6f else 0f,
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing,
                    ),
                label = "star_glow_$index",
            )

            Box(
                contentAlignment = Alignment.Center,
            ) {
                // Glow effect
                if (isEarned && isRevealed) {
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .scale(scale.value)
                                .alpha(glowAlpha)
                                .background(
                                    brush =
                                        Brush.radialGradient(
                                            colors =
                                                listOf(
                                                    Color(0xFFFFD700).copy(alpha = 0.5f),
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
                            Color(0xFFFFD700) // Gold
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    modifier =
                        Modifier
                            .size(48.dp)
                            .scale(scale.value)
                            .rotate(rotation.value),
                )
            }
        }
    }
}

/**
 * Celebration message with animation
 */
@Composable
private fun CelebrationMessage(
    stars: Int,
    score: Int,
    combo: Int,
    animationPhase: CelebrationPhase,
    config: CelebrationConfig,
) {
    val alpha by animateFloatAsState(
        targetValue = if (animationPhase == CelebrationPhase.MESSAGE) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
        label = "message_alpha",
    )

    val scale by animateFloatAsState(
        targetValue = if (animationPhase == CelebrationPhase.MESSAGE) 1f else 0.8f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 200f,
            ),
        label = "message_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            Modifier
                .scale(scale)
                .alpha(alpha),
    ) {
        // Main message
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = config.messageBackgroundColor,
            border =
                BorderStroke(
                    width = 2.dp,
                    color = config.messageBorderColor,
                ),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = config.message,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = config.messageTextColor,
                    textAlign = TextAlign.Center,
                )

                // Emoji for 3-star
                if (stars == 3) {
                    Text(
                        text = "🎉🎉🎉",
                        fontSize = 32.sp,
                    )
                }
            }
        }

        // Stats display (score, combo)
        if (score > 0 || combo > 0) {
            StatsDisplay(score = score, combo = combo)
        }
    }
}

/**
 * Stats display for score and combo
 */
@Composable
private fun StatsDisplay(
    score: Int,
    combo: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        if (score > 0) {
            StatItem(
                label = "Score",
                value = score.toString(),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (combo > 0) {
            StatItem(
                label = "Combo",
                value = "${combo}x",
                color = Color(0xFFFF6B35), // Orange
            )
        }
    }
}

/**
 * Individual stat item
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/**
 * Compact celebration animation (for smaller spaces)
 * Simplified version for performance
 */
@Composable
fun CompactCelebrationAnimation(
    stars: Int,
    modifier: Modifier = Modifier,
) {
    var currentStar by remember { mutableIntStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }

    val config = getCelebrationConfig(stars)

    LaunchedEffect(stars) {
        currentStar = 0
        showConfetti = false

        repeat(stars) { index ->
            delay(100)
            currentStar = index + 1
        }

        if (stars >= 2) {
            showConfetti = true
            delay(config.messageDuration)
            showConfetti = false
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

            val scale by animateFloatAsState(
                targetValue = if (isRevealed) 1f else 0f,
                animationSpec =
                    spring(
                        dampingRatio = 0.5f,
                        stiffness = 250f,
                    ),
                label = "compact_star_$index",
            )

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (isEarned) Color(0xFFFFD700) else MaterialTheme.colorScheme.surfaceVariant,
                modifier =
                    Modifier
                        .size(32.dp)
                        .scale(scale),
            )
        }
    }

    // Compact confetti overlay
    if (showConfetti && config.showConfetti) {
        CelebrationBurst(
            particleCount = 20,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Quick celebration popup (inline feedback)
 * For immediate feedback during gameplay
 */
@Composable
fun QuickCelebrationPopup(
    stars: Int,
    onDismiss: () -> Unit = {},
) {
    val config = getCelebrationConfig(stars)
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(config.messageDuration)
        visible = false
        delay(300) // Exit animation
        onDismiss()
    }

    if (visible) {
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "popup_alpha",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
                    .background(
                        color = config.messageBackgroundColor.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = config.message,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = config.messageTextColor,
                )

                if (stars == 3) {
                    Text(text = "⭐", fontSize = 20.sp)
                }
            }
        }
    }
}

/**
 * Get celebration configuration based on star rating
 * Per VISUAL_FEEDBACK_DESIGN.md Section 6.1
 */
@Composable
private fun getCelebrationConfig(stars: Int): CelebrationConfig {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return when (stars) {
        3 ->
            CelebrationConfig(
                confettiCount = 50,
                confettiSpread = 1.0f,
                showConfetti = true,
                message = "Perfect! 太棒了!",
                messageBackgroundColor = Color(0xFF4CAF50),
                messageBorderColor = Color(0xFF388E3C),
                messageTextColor = Color.White,
                starRevealDuration = 600L,
                messageDuration = 1200L,
                confettiDuration = 800,
            )

        2 ->
            CelebrationConfig(
                confettiCount = 20,
                confettiSpread = 0.6f,
                showConfetti = true,
                message = "Great Job! 做得好!",
                messageBackgroundColor = Color(0xFF2196F3),
                messageBorderColor = Color(0xFF1976D2),
                messageTextColor = Color.White,
                starRevealDuration = 400L,
                messageDuration = 800L,
                confettiDuration = 600,
            )

        1 ->
            CelebrationConfig(
                confettiCount = 0,
                confettiSpread = 0f,
                showConfetti = false,
                message = "Good Try! 继续努力!",
                messageBackgroundColor = Color(0xFFFF9800),
                messageBorderColor = Color(0xFFF57C00),
                messageTextColor = Color.White,
                starRevealDuration = 300L,
                messageDuration = 500L,
                confettiDuration = 0,
            )

        else ->
            CelebrationConfig(
                confettiCount = 0,
                confettiSpread = 0f,
                showConfetti = false,
                message = "Let's try again! 再试一次!",
                messageBackgroundColor = surfaceVariant,
                messageBorderColor = outline,
                messageTextColor = onSurfaceVariant,
                starRevealDuration = 0L,
                messageDuration = 400L,
                confettiDuration = 0,
            )
    }
}

/**
 * Celebration phase for animation sequencing
 */
private enum class CelebrationPhase {
    STARS, // Star reveal phase
    MESSAGE, // Message and confetti phase
    COMPLETE, // Animation complete
}

/**
 * Celebration configuration data class
 */
private data class CelebrationConfig(
    val confettiCount: Int,
    val confettiSpread: Float,
    val showConfetti: Boolean,
    val message: String,
    val messageBackgroundColor: Color,
    val messageBorderColor: Color,
    val messageTextColor: Color,
    val starRevealDuration: Long,
    val messageDuration: Long,
    val confettiDuration: Int,
)

/**
 * Animation specification constants
 * Per VISUAL_FEEDBACK_DESIGN.md Section 2
 */
private object CelebrationAnimationSpecs {
    const val STAR_REVEAL_DELAY_MS: Long = 100L // Delay between stars
    const val STAR_APPEAR_DURATION: Int = 200
    const val ROTATION_DURATION_MS: Int = 400
    const val SPRING_DAMPING_RATIO = 0.5f
    const val SPRING_STIFFNESS = 300f
}

// Animation specs
private val STAR_SPRING_SPEC: SpringSpec<Float> =
    spring(
        dampingRatio = CelebrationAnimationSpecs.SPRING_DAMPING_RATIO,
        stiffness = CelebrationAnimationSpecs.SPRING_STIFFNESS,
    )

private val STAR_ROTATION_SPEC: AnimationSpec<Float> =
    TweenSpec<Float>(
        durationMillis = CelebrationAnimationSpecs.ROTATION_DURATION_MS,
        easing = FastOutSlowInEasing,
    )

// Re-export delay for use in components
private val STAR_REVEAL_DELAY_MS: Long = CelebrationAnimationSpecs.STAR_REVEAL_DELAY_MS
