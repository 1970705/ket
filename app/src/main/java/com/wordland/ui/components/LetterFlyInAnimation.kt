package com.wordland.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

// Public animation constants for use in components
const val LETTER_FLY_IN_DELAY_MS = 100L
const val COMPACT_LETTER_DELAY_MS = 75L
const val COMPACT_LETTER_DURATION_MS = 150

/**
 * Letter fly-in animation component for Spell Battle game
 *
 * Features:
 * - Letters fly in sequentially with configurable delay
 * - Spring animation for bouncy, child-friendly effect
 * - Scale + rotation + alpha animations
 * - Optimized for 60fps performance
 *
 * @param targetWord The complete word to spell
 * @param userAnswer Current user input (for showing revealed letters)
 * @param onAnimationComplete Callback when animation completes
 * @param modifier Compose modifier
 *
 * Animation Specification (per VISUAL_FEEDBACK_DESIGN.md):
 * - Duration per letter: 150ms
 * - Delay between letters: 100ms
 * - Easing: FastOutSlowIn (spring)
 * - Target: 60fps
 */
@Composable
fun LetterFlyInAnimation(
    targetWord: String,
    userAnswer: String,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Track how many letters should be animated
    var animatedCount by remember(targetWord, userAnswer) {
        mutableIntStateOf(0)
    }

    // Animate letters sequentially when targetWord changes
    LaunchedEffect(targetWord) {
        animatedCount = 0

        // Staggered animation - reveal each letter with delay
        targetWord.forEachIndexed { index, _ ->
            animatedCount = index + 1
            // Delay before next letter: 100ms per design spec
            kotlinx.coroutines.delay(LETTER_FLY_IN_DELAY_MS)
        }

        onAnimationComplete()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        targetWord.forEachIndexed { index, letter ->
            val isRevealed = index < animatedCount
            val isFilled = index < userAnswer.length
            val userLetter = if (isFilled) userAnswer[index].toString() else ""

            AnimatedLetterBox(
                letter = letter,
                userLetter = userLetter,
                isRevealed = isRevealed,
                isFilled = isFilled,
                index = index,
            )
        }
    }
}

/**
 * Individual animated letter box
 *
 * Animation sequence:
 * 1. Scale: 0.3 → 1.0 (spring bounce)
 * 2. Rotation: -180° → 0° (spin effect)
 * 3. Alpha: 0 → 1 (fade in)
 */
@Composable
private fun AnimatedLetterBox(
    letter: Char,
    userLetter: String,
    isRevealed: Boolean,
    isFilled: Boolean,
    index: Int,
) {
    // Scale animation - spring for bouncy effect
    val scale = remember { Animatable(0.3f) }

    // Rotation animation - spin effect
    val rotation = remember { Animatable(-180f) }

    // Alpha animation
    val alpha = remember { Animatable(0f) }

    // Start animations when revealed
    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            // Run all animations in parallel for performance
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = LETTER_SPRING_SPEC,
            )
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = LETTER_ROTATION_SPEC,
            )
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = LETTER_ALPHA_SPEC,
            )
        }
    }

    // Determine styling based on state
    val backgroundColor =
        when {
            isFilled && userLetter == letter.toString() -> MaterialTheme.colorScheme.primaryContainer
            isRevealed -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        }

    val borderColor =
        when {
            isFilled && userLetter == letter.toString() -> MaterialTheme.colorScheme.primary
            isRevealed -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.outlineVariant
        }

    val textColor =
        when {
            isFilled -> MaterialTheme.colorScheme.onPrimaryContainer
            isRevealed -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> Color.Transparent
        }

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .scale(scale.value)
                .rotate(rotation.value)
                .alpha(alpha.value)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = MaterialTheme.shapes.small,
                )
                .background(
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.small,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isRevealed) letter.uppercaseChar().toString() else "",
            fontSize = 24.sp,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

/**
 * Compact letter fly-in animation (for smaller spaces)
 * Simplified animation for better performance
 */
@Composable
fun CompactLetterFlyInAnimation(
    targetWord: String,
    userAnswer: String,
    modifier: Modifier = Modifier,
) {
    var animatedCount by remember(targetWord) { mutableIntStateOf(0) }

    LaunchedEffect(targetWord) {
        animatedCount = 0
        targetWord.forEachIndexed { index, _ ->
            animatedCount = index + 1
            kotlinx.coroutines.delay(COMPACT_LETTER_DELAY_MS)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        targetWord.forEachIndexed { index, letter ->
            val isRevealed = index < animatedCount
            val isFilled = index < userAnswer.length

            val scale by animateFloatAsState(
                targetValue = if (isRevealed) 1f else 0f,
                animationSpec =
                    tween(
                        durationMillis = COMPACT_LETTER_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                label = "compact_letter_scale_$index",
            )

            val alpha by animateFloatAsState(
                targetValue = if (isRevealed) 1f else 0f,
                animationSpec =
                    tween(
                        durationMillis = COMPACT_LETTER_DURATION_MS,
                        easing = LinearEasing,
                    ),
                label = "compact_letter_alpha_$index",
            )

            Surface(
                modifier =
                    Modifier
                        .size(40.dp)
                        .scale(scale)
                        .alpha(alpha),
                shape = MaterialTheme.shapes.small,
                border =
                    BorderStroke(
                        width = 2.dp,
                        color =
                            if (isFilled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                    ),
                color =
                    if (isFilled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isRevealed) {
                        Text(
                            text = letter.uppercaseChar().toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (isFilled) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animation specification constants
 * Per VISUAL_FEEDBACK_DESIGN.md Section 2
 */
private object LetterAnimationSpecs {
    // Spring damping and stiffness for bouncy effect
    const val SPRING_DAMPING_RATIO = 0.5f
    const val SPRING_STIFFNESS = 300f

    // Rotation duration
    const val ROTATION_DURATION_MS = 400

    // Alpha fade duration
    const val ALPHA_DURATION_MS = 100
}

// Re-export specs for use in components
private val LETTER_SPRING_SPEC: SpringSpec<Float> =
    spring(
        dampingRatio = LetterAnimationSpecs.SPRING_DAMPING_RATIO,
        stiffness = LetterAnimationSpecs.SPRING_STIFFNESS,
    )

private val LETTER_ROTATION_SPEC: AnimationSpec<Float> =
    TweenSpec<Float>(
        durationMillis = LetterAnimationSpecs.ROTATION_DURATION_MS,
        easing = FastOutSlowInEasing,
    )

private val LETTER_ALPHA_SPEC: AnimationSpec<Float> =
    TweenSpec<Float>(
        durationMillis = LetterAnimationSpecs.ALPHA_DURATION_MS,
        easing = LinearEasing,
    )

/**
 * Animation quality setting for performance control
 * Per VISUAL_FEEDBACK_DESIGN.md Section 8
 */
enum class AnimationQuality {
    HIGH, // Full animations, all effects
    MEDIUM, // Simplified animations, reduced effects
    LOW, // Minimal animations, static where possible
}

/**
 * Configuration for letter fly-in animation
 */
data class LetterAnimationConfig(
    val quality: AnimationQuality = AnimationQuality.HIGH,
    val letterDelay: Long = LETTER_FLY_IN_DELAY_MS,
    val enableRotation: Boolean = true,
    val enableBounce: Boolean = true,
) {
    companion object {
        val Default = LetterAnimationConfig()

        val Performance =
            LetterAnimationConfig(
                quality = AnimationQuality.MEDIUM,
                letterDelay = 50L, // Faster delay
                enableRotation = false, // Skip rotation for performance
            )

        val Minimal =
            LetterAnimationConfig(
                quality = AnimationQuality.LOW,
                letterDelay = 0L, // No delay
                enableRotation = false,
                enableBounce = false,
            )
    }
}
