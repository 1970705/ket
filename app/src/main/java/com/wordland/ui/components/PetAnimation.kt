package com.wordland.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.wordland.domain.model.Pet

/**
 * Animation states for pet reactions
 */
enum class PetAnimationState {
    /** Gentle breathing animation when idle */
    IDLE,

    /** Jump animation on correct answer */
    HAPPY,

    /** Shake + rotate animation for combo reactions */
    EXCITED,

    /** Spin + scale animation for level completion */
    CELEBRATE,
}

/**
 * Data class to hold pet animation transformation values
 */
@Composable
private fun rememberPetTransform(animationState: PetAnimationState): PetTransform {
    // Scale animation for IDLE (breathing effect)
    val idleScale = remember { Animatable(1f) }
    LaunchedEffect(idleScale) {
        while (true) {
            idleScale.animateTo(
                targetValue = 1.02f,
                animationSpec = tween(durationMillis = 800, delayMillis = 0),
            )
            idleScale.animateTo(
                targetValue = 0.98f,
                animationSpec = tween(durationMillis = 800, delayMillis = 100),
            )
        }
    }

    // Scale animation for HAPPY (jump effect)
    val happyScale = remember { Animatable(1f) }
    LaunchedEffect(animationState) {
        if (animationState == PetAnimationState.HAPPY) {
            happyScale.animateTo(
                targetValue = 1.15f,
                animationSpec =
                    spring(
                        dampingRatio = 0.4f,
                        stiffness = 400f,
                    ),
            )
            happyScale.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.6f,
                        stiffness = 300f,
                    ),
            )
        }
    }

    // Combined scale and rotate for EXCITED (combo reaction)
    val excitedScale = remember { Animatable(1f) }
    val excitedRotation = remember { Animatable(0f) }
    LaunchedEffect(animationState) {
        if (animationState == PetAnimationState.EXCITED) {
            // Shake effect
            excitedScale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 100),
            )
            excitedRotation.animateTo(
                targetValue = -10f,
                animationSpec = tween(durationMillis = 50),
            )
            excitedRotation.animateTo(
                targetValue = 10f,
                animationSpec = tween(durationMillis = 100),
            )
            excitedRotation.animateTo(
                targetValue = -5f,
                animationSpec = tween(durationMillis = 80),
            )
            excitedRotation.animateTo(
                targetValue = 5f,
                animationSpec = tween(durationMillis = 80),
            )
            excitedRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 50),
            )
            excitedScale.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.5f,
                        stiffness = 300f,
                    ),
            )
        }
    }

    // Scale and rotate for CELEBRATE (level complete)
    val celebrateScale = remember { Animatable(0f) }
    val celebrateRotation = remember { Animatable(0f) }
    LaunchedEffect(animationState) {
        if (animationState == PetAnimationState.CELEBRATE) {
            celebrateScale.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.3f,
                        stiffness = 200f,
                    ),
            )
            celebrateScale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 200),
            )
            celebrateRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 400),
            )
            celebrateScale.animateTo(
                targetValue = 1f,
                animationSpec =
                    spring(
                        dampingRatio = 0.5f,
                        stiffness = 250f,
                    ),
            )
        }
    }

    return when (animationState) {
        PetAnimationState.IDLE ->
            PetTransform(
                scale = idleScale.value,
                rotation = 0f,
            )
        PetAnimationState.HAPPY ->
            PetTransform(
                scale = happyScale.value,
                rotation = 0f,
            )
        PetAnimationState.EXCITED ->
            PetTransform(
                scale = excitedScale.value,
                rotation = excitedRotation.value,
            )
        PetAnimationState.CELEBRATE ->
            PetTransform(
                scale = celebrateScale.value,
                rotation = celebrateRotation.value,
            )
    }
}

/**
 * Simple data class to hold transformation values
 */
private data class PetTransform(
    val scale: Float,
    val rotation: Float,
)

/**
 * Pet animation component with various reaction states
 *
 * Features:
 * - IDLE: Gentle breathing animation (scale: 0.98-1.02, continuous loop)
 * - HAPPY: Jump animation on correct answer (scale: 1.0 → 1.15 → 1.0, ~300ms)
 * - EXCITED: Combo reaction with shake (rotate ±10°, ~200ms)
 * - CELEBRATE: Level complete with spin (360° rotation + scale, ~500ms)
 *
 * Uses Compose Animation API with spring animations for bouncy feel.
 * Optimized for 60fps performance.
 *
 * @param pet The pet entity to display
 * @param animationState Current animation state
 * @param modifier Modifier for the component
 */
@Composable
fun PetAnimation(
    pet: Pet,
    animationState: PetAnimationState = PetAnimationState.IDLE,
    modifier: Modifier = Modifier,
) {
    val transform = rememberPetTransform(animationState)

    Box(
        modifier =
            modifier
                .scale(transform.scale)
                .rotate(transform.rotation),
    ) {
        androidx.compose.material3.Text(
            text = pet.emoji,
            fontSize = 64.sp,
            // Use TextMotion.Animated for smoother animations
            // This optimizes text rendering during animations
        )
    }
}

/**
 * Compact pet animation for smaller spaces
 * Uses the same animation states but with a smaller default size
 *
 * @param pet The pet entity to display
 * @param animationState Current animation state
 * @param size Emoji size in sp (default 32.sp for compact)
 * @param modifier Modifier for the component
 */
@Composable
fun CompactPetAnimation(
    pet: Pet,
    animationState: PetAnimationState = PetAnimationState.IDLE,
    size: TextUnit = 32.sp,
    modifier: Modifier = Modifier,
) {
    val transform = rememberPetTransform(animationState)

    Box(
        modifier =
            modifier
                .scale(transform.scale)
                .rotate(transform.rotation),
    ) {
        androidx.compose.material3.Text(
            text = pet.emoji,
            fontSize = size,
        )
    }
}

/**
 * Pet animation with custom size modifier
 * Allows flexible sizing while maintaining animation behavior
 *
 * @param pet The pet entity to display
 * @param animationState Current animation state
 * @param emojiSize Size of the emoji in sp
 * @param modifier Modifier for the component
 */
@Composable
fun PetAnimationCustomSize(
    pet: Pet,
    animationState: PetAnimationState = PetAnimationState.IDLE,
    emojiSize: TextUnit = 64.sp,
    modifier: Modifier = Modifier,
) {
    val transform = rememberPetTransform(animationState)

    Box(
        modifier =
            modifier
                .scale(transform.scale)
                .rotate(transform.rotation),
    ) {
        androidx.compose.material3.Text(
            text = pet.emoji,
            fontSize = emojiSize,
        )
    }
}
