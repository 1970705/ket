package com.wordland.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

/**
 * Animated word switch component for smooth word transitions
 *
 * Features:
 * - Slide-in/slide-out animation when switching words
 * - Scale + alpha animation for smooth transition
 * - Performance optimized (60fps target)
 * - Respects reduce motion preference
 *
 * @param wordKey Unique key for the current word (triggers animation)
 * @param modifier Compose modifier
 * @param reduceMotion Whether to reduce motion (accessibility)
 * @param content The word content to display
 *
 * @since 1.6 (Epic #8.3)
 */
@Composable
fun <T> AnimatedWordSwitch(
    wordKey: T,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (reduceMotion) {
        // No animation for reduce motion preference
        Box(modifier = modifier) {
            content()
        }
        return
    }

    // Scale animation: shrink → expand
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            tween(
                durationMillis = WORD_SWITCH_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "word_scale_${wordKey.hashCode()}",
    )

    // Alpha animation: fade in
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
            tween(
                durationMillis = WORD_SWITCH_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "word_alpha_${wordKey.hashCode()}",
    )

    Box(
        modifier =
            modifier
                .scale(scale)
                .alpha(alpha),
    ) {
        content()
    }
}

/**
 * Animated slide-out transition for leaving word
 *
 * @param isVisible Whether the content is visible
 * @param modifier Compose modifier
 * @param reduceMotion Whether to reduce motion
 * @param content The content to display
 */
@Composable
fun AnimatedSlideOut(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (reduceMotion || !isVisible) {
        Box(modifier = modifier.alpha(if (isVisible) 1f else 0f)) {
            if (isVisible) {
                content()
            }
        }
        return
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec =
            tween(
                durationMillis = SLIDE_OUT_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "slide_out_scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = SLIDE_OUT_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "slide_out_alpha",
    )

    Box(
        modifier =
            modifier
                .scale(scale)
                .alpha(alpha),
    ) {
        if (isVisible) {
            content()
        }
    }
}

/**
 * Animated slide-in transition for new word
 *
 * @param isVisible Whether the content is visible
 * @param modifier Compose modifier
 * @param reduceMotion Whether to reduce motion
 * @param content The content to display
 */
@Composable
fun AnimatedSlideIn(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (reduceMotion || !isVisible) {
        Box(modifier = modifier.alpha(if (isVisible) 1f else 0f)) {
            if (isVisible) {
                content()
            }
        }
        return
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec =
            tween(
                durationMillis = SLIDE_IN_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "slide_in_scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = SLIDE_IN_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        label = "slide_in_alpha",
    )

    Box(
        modifier =
            modifier
                .scale(scale)
                .alpha(alpha),
    ) {
        if (isVisible) {
            content()
        }
    }
}

/**
 * Animation specification constants
 * Per VISUAL_FEEDBACK_DESIGN.md Section 2
 */
private object WordSwitchAnimationSpecs {
    const val WORD_SWITCH_DURATION_MS = 250 // Quick word transition
    const val SLIDE_OUT_DURATION_MS = 200
    const val SLIDE_IN_DURATION_MS = 250
}

// Re-export specs
private val WORD_SWITCH_DURATION_MS = WordSwitchAnimationSpecs.WORD_SWITCH_DURATION_MS
private val SLIDE_OUT_DURATION_MS = WordSwitchAnimationSpecs.SLIDE_OUT_DURATION_MS
private val SLIDE_IN_DURATION_MS = WordSwitchAnimationSpecs.SLIDE_IN_DURATION_MS
