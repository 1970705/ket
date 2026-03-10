package com.wordland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Learning Screen transition animations
 *
 * Enhanced transitions specifically designed for the learning flow:
 * - Question transitions (word switching)
 * - Feedback reveal animations
 * - Level completion transitions
 *
 * Performance: 60fps target (16.67ms per frame)
 *
 * @since 1.6 (Epic #8.3)
 */

/**
 * Question-to-feedback transition
 *
 * Animates from Ready state to Feedback state with smooth slide
 *
 * @param isFeedback Whether showing feedback (true) or question (false)
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun QuestionToFeedbackTransition(
    isFeedback: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = isFeedback,
        modifier = modifier,
        enter =
            slideInVertically(
                initialOffsetY = { it / 4 }, // Slide from 1/4 screen height
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.QUESTION_FEEDBACK_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        exit =
            slideOutVertically(
                targetOffsetY = { -it / 4 },
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.QUESTION_FEEDBACK_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
            ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        content = content,
    )
}

/**
 * Word switch transition (question to next question)
 *
 * Quick transition between words during gameplay
 *
 * @param wordKey Unique key for current word
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun <T> WordSwitchTransition(
    wordKey: T,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.animation.AnimatedContent(
        targetState = wordKey,
        modifier = modifier,
        transitionSpec = {
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.WORD_SWITCH_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ) togetherWith slideOutVertically(
                    targetOffsetY = { -it / 3 },
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.WORD_SWITCH_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                )
        },
        label = "word_switch",
    ) {
        Box {
            content()
        }
    }
}

/**
 * Level completion reveal transition
 *
 * Grand reveal animation for level completion screen
 *
 * @param isVisible Whether completion screen is visible
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun LevelCompleteReveal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter =
            scaleIn(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.LEVEL_REVEAL_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                initialScale = 0.8f,
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        exit =
            scaleOut(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.LEVEL_REVEAL_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                targetScale = 0.9f,
            ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        content = content,
    )
}

/**
 * Milestone celebration transition
 *
 * Special transition for combo milestones (3, 5, 10 combo)
 *
 * @param showMilestone Whether to show milestone celebration
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun MilestoneCelebrationTransition(
    showMilestone: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = showMilestone,
        modifier = modifier,
        enter =
            scaleIn(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.MILESTONE_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                initialScale = 0f,
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.MILESTONE_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        exit =
            scaleOut(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.MILESTONE_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                targetScale = 1.5f,
            ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.MILESTONE_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        content = content,
    )
}

/**
 * Hint card expand/collapse transition
 *
 * Smooth expansion when hint is shown
 *
 * @param isExpanded Whether hint is expanded
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun HintExpandTransition(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = isExpanded,
        modifier = modifier,
        enter =
            expandVertically(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.HINT_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                expandFrom = Alignment.Top,
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.HINT_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        exit =
            shrinkVertically(
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.HINT_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
                shrinkTowards = Alignment.Top,
            ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.HINT_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        content = content,
    )
}

/**
 * Star breakdown transition
 *
 * Transition from level complete to star breakdown
 *
 * @param showBreakdown Whether to show breakdown
 * @param modifier Compose modifier
 * @param content Content to display
 */
@Composable
fun StarBreakdownTransition(
    showBreakdown: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = showBreakdown,
        modifier = modifier,
        enter =
            slideInVertically(
                initialOffsetY = { it },
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.BREAKDOWN_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
            ) +
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.BREAKDOWN_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        exit =
            slideOutVertically(
                targetOffsetY = { -it },
                animationSpec =
                    tween(
                        durationMillis = TransitionSpecs.BREAKDOWN_DURATION_MS,
                        easing = FastOutSlowInEasing,
                    ),
            ) +
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis = TransitionSpecs.BREAKDOWN_FADE_DURATION_MS,
                            easing = FastOutSlowInEasing,
                        ),
                ),
        content = content,
    )
}

/**
 * Animation specifications
 * Optimized for 60fps performance
 */
private object TransitionSpecs {
    // Question to Feedback: 300ms
    const val QUESTION_FEEDBACK_DURATION_MS = 300

    // Word Switch: 200ms (quick for gameplay)
    const val WORD_SWITCH_DURATION_MS = 200

    // Level Complete Reveal: 500ms (emphasized)
    const val LEVEL_REVEAL_DURATION_MS = 500

    // Milestone: 400ms (celebratory)
    const val MILESTONE_DURATION_MS = 400

    // Hint Expand: 250ms (quick feedback)
    const val HINT_DURATION_MS = 250

    // Star Breakdown: 400ms (smooth)
    const val BREAKDOWN_DURATION_MS = 400

    // Fade durations
    const val FADE_DURATION_MS = 200
    const val MILESTONE_FADE_DURATION_MS = 150
    const val HINT_FADE_DURATION_MS = 200
    const val BREAKDOWN_FADE_DURATION_MS = 300
}
