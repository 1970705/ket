package com.wordland.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wordland.domain.model.MapViewMode
import com.wordland.ui.viewmodel.ViewTransitionState

/**
 * Crossfade transition between view modes with slide animation
 *
 * Provides a smooth 500ms transition between island view and world view
 * with fade and horizontal slide effects.
 *
 * @param viewMode Current active view mode
 * @param transitionState Current transition animation state
 * @param islandContent Content to show for island view
 * @param worldContent Content to show for world view
 * @param modifier Modifier for the container
 */
@Composable
fun ViewModeTransition(
    viewMode: MapViewMode,
    transitionState: ViewTransitionState,
    islandContent: @Composable () -> Unit,
    worldContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val slideOffset = with(density) { 100.dp.toPx() }

    Box(modifier = modifier) {
        // Island View (exits to the left when switching to world view)
        AnimatedVisibility(
            visible = viewMode == MapViewMode.ISLAND_VIEW || transitionState.isTransitioning,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                ) +
                    slideInHorizontally(
                        initialOffsetX = { -slideOffset.toInt() },
                        animationSpec =
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                    ),
            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                ) +
                    slideOutHorizontally(
                        targetOffsetX = { -slideOffset.toInt() },
                        animationSpec =
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                    ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                islandContent()
            }
        }

        // World View (enters from the right when switching from island view)
        AnimatedVisibility(
            visible = viewMode == MapViewMode.WORLD_VIEW || transitionState.isTransitioning,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                ) +
                    slideInHorizontally(
                        initialOffsetX = { slideOffset.toInt() },
                        animationSpec =
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                    ),
            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing,
                        ),
                ) +
                    slideOutHorizontally(
                        targetOffsetX = { slideOffset.toInt() },
                        animationSpec =
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                    ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                worldContent()
            }
        }
    }
}

/**
 * Simple fade-scale transition (legacy, 300ms)
 * Used as fallback when slide animation is not desired
 */
@Composable
fun ViewModeTransitionSimple(
    viewMode: MapViewMode,
    islandContent: @Composable () -> Unit,
    worldContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = viewMode == MapViewMode.ISLAND_VIEW,
        enter =
            androidx.compose.animation.fadeIn(
                animationSpec = tween(300),
            ) +
                androidx.compose.animation.scaleIn(
                    animationSpec = tween(300, easing = { it * it }),
                ),
        exit =
            androidx.compose.animation.fadeOut(
                animationSpec = tween(300),
            ) +
                androidx.compose.animation.scaleOut(
                    animationSpec = tween(300, easing = { it * it }),
                ),
        modifier = modifier,
    ) {
        islandContent()
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = viewMode == MapViewMode.WORLD_VIEW,
        enter =
            androidx.compose.animation.fadeIn(
                animationSpec = tween(300),
            ) +
                androidx.compose.animation.scaleIn(
                    animationSpec = tween(300, easing = { it * it }),
                ),
        exit =
            androidx.compose.animation.fadeOut(
                animationSpec = tween(300),
            ) +
                androidx.compose.animation.scaleOut(
                    animationSpec = tween(300, easing = { it * it }),
                ),
        modifier = modifier,
    ) {
        worldContent()
    }
}
