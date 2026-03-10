package com.wordland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset

/**
 * Screen transition types for navigation animations
 */
enum class ScreenTransitionType {
    FADE,
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    SCALE,
    NONE,
}

/**
 * Enter transition for fade effect
 */
val fadeIn: EnterTransition =
    fadeIn(
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    )

/**
 * Exit transition for fade effect
 */
val fadeOut: ExitTransition =
    fadeOut(
        animationSpec =
            tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing,
            ),
    )

/**
 * Horizontal slide enter transition (from right)
 */
val slideInRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) + fadeIn()

/**
 * Horizontal slide exit transition (to left)
 */
val slideOutLeft: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec =
            tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing,
            ),
    ) + fadeOut()

/**
 * Horizontal slide enter transition (from left)
 */
val slideInLeft: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) + fadeIn()

/**
 * Horizontal slide exit transition (to right)
 */
val slideOutRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec =
            tween(
                durationMillis = 250,
                easing = FastOutSlowInEasing,
            ),
    ) + fadeOut()

/**
 * Scale + Fade enter transition
 */
val scaleIn: EnterTransition =
    scaleIn(
        animationSpec =
            spring(
                dampingRatio = 0.7f,
                stiffness = 300f,
            ),
        initialScale = 0.9f,
    ) + fadeIn()

/**
 * Scale + Fade exit transition
 */
val scaleOut: ExitTransition =
    scaleOut(
        animationSpec =
            spring(
                dampingRatio = 0.7f,
                stiffness = 300f,
            ),
        targetScale = 0.9f,
    ) + fadeOut()

/**
 * Animated content wrapper with specified transition type
 *
 * @param targetState The state that triggers content change
 * @param transitionType Type of transition animation
 * @param modifier Modifier for the container
 * @param content The content to display based on state
 */
@Composable
fun <T> AnimatedScreenTransition(
    targetState: T,
    transitionType: ScreenTransitionType = ScreenTransitionType.FADE,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val transitionSpec =
        when (transitionType) {
            ScreenTransitionType.FADE -> {
                fadeIn togetherWith fadeOut
            }
            ScreenTransitionType.SLIDE_HORIZONTAL -> {
                slideInRight togetherWith slideOutLeft
            }
            ScreenTransitionType.SLIDE_VERTICAL -> {
                slideInVertically(
                    animationSpec =
                        tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing,
                        ),
                ) togetherWith
                    slideOutVertically(
                        animationSpec =
                            tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing,
                            ),
                    )
            }
            ScreenTransitionType.SCALE -> {
                scaleIn togetherWith scaleOut
            }
            ScreenTransitionType.NONE -> {
                EnterTransition.None togetherWith ExitTransition.None
            }
        }

    androidx.compose.animation.AnimatedContent(
        modifier = modifier,
        targetState = targetState,
        transitionSpec = { transitionSpec },
        label = "screen_transition",
    ) { state ->
        content(state)
    }
}

/**
 * Fade in animation for content appearance
 */
@Composable
fun FadeIn(
    modifier: Modifier = Modifier,
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val alpha: Float by
        animateFloatAsState(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = durationMillis,
                    delayMillis = delayMillis,
                    easing = FastOutSlowInEasing,
                ),
            label = "fade_in",
        )

    Box(modifier = modifier.alpha(alpha)) {
        content()
    }
}

/**
 * Scale in animation for content appearance
 */
@Composable
fun ScaleIn(
    modifier: Modifier = Modifier,
    fromScale: Float = 0.8f,
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val scale: Float by
        animateFloatAsState(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio = 0.7f,
                    stiffness = 300f,
                ),
            label = "scale_in",
        )

    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

/**
 * Slide in from bottom animation
 */
@Composable
fun SlideInFromBottom(
    modifier: Modifier = Modifier,
    offsetY: Float = 100f,
    durationMillis: Int = 300,
    content: @Composable () -> Unit,
) {
    val currentOffsetY: Int by
        animateIntAsState(
            targetValue = 0,
            animationSpec =
                spring(
                    dampingRatio = 0.75f,
                    stiffness = 350f,
                ),
            label = "slide_in_bottom",
        )

    Box(
        modifier = modifier.offset { IntOffset(0, currentOffsetY) },
    ) {
        content()
    }
}
