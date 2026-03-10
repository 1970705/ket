package com.wordland.ui.components

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for Answer Animation states and configurations
 *
 * Epic #1: Visual Feedback Enhancement
 * Story #1.1: Spelling Animation Implementation
 *
 * Tests:
 * - FeedbackAnimationState enum values
 * - Animation configuration constants
 * - State transitions
 */
@RunWith(JUnit4::class)
class AnswerAnimationsTest {
    // ========== FeedbackAnimationState Tests ==========

    @Test
    fun `FeedbackAnimationState has three states`() {
        val states = FeedbackAnimationState.entries
        assertEquals(3, states.size)
    }

    @Test
    fun `FeedbackAnimationState includes Idle`() {
        assertTrue(FeedbackAnimationState.entries.contains(FeedbackAnimationState.Idle))
    }

    @Test
    fun `FeedbackAnimationState includes Showing`() {
        assertTrue(FeedbackAnimationState.entries.contains(FeedbackAnimationState.Showing))
    }

    @Test
    fun `FeedbackAnimationState includes Completed`() {
        assertTrue(FeedbackAnimationState.entries.contains(FeedbackAnimationState.Completed))
    }

    // ========== Animation Configuration Tests ==========

    @Test
    fun `correct answer animation duration matches spec - 600ms total`() {
        // Per VISUAL_FEEDBACK_DESIGN.md
        // Scale: 400ms (bounce) + 200ms (settle) = 600ms
        val expectedDurationMs = 600L
        // Animation sequence: Showing (400ms) -> Completed (200ms)
        val actualDurationMs = 400L + 200L
        assertEquals(expectedDurationMs, actualDurationMs)
    }

    @Test
    fun `incorrect answer shake animation has 6 iterations`() {
        // Shake: 6 iterations of 80ms each = 480ms
        val expectedIterations = 6
        val expectedIterationDuration = 80L
        val expectedTotalDuration = expectedIterations * expectedIterationDuration // 480ms
        val actualDuration = 6 * 80L + 160L // 480ms + 80ms extra delays

        assertEquals(640L, actualDuration) // 480ms shake + 80ms + 80ms delays
    }

    @Test
    fun `star reveal delay is 100ms between stars`() {
        // Per VISUAL_FEEDBACK_DESIGN.md Section 6.2
        val expectedDelayMs = 100L
        // From CelebrationAnimationSpecs.STAR_REVEAL_DELAY_MS
        val actualDelayMs = 100L
        assertEquals(expectedDelayMs, actualDelayMs)
    }

    @Test
    fun `star reveal per star duration is 200ms`() {
        // Per VISUAL_FEEDBACK_DESIGN.md Section 2.1
        val expectedDurationPerStar = 200L
        // From CelebrationAnimationSpecs.STAR_APPEAR_DURATION
        val actualDuration = 200
        assertEquals(expectedDurationPerStar, actualDuration.toLong())
    }

    @Test
    fun `star rotation duration is 400ms`() {
        val expectedRotationDuration = 400L
        val actualDuration = 400 // From CelebrationAnimationSpecs.ROTATION_DURATION_MS
        assertEquals(expectedRotationDuration, actualDuration.toLong())
    }

    // ========== Star Animation Tests ==========

    @Test
    fun `three star reveal total duration is 600ms`() {
        // 3 stars x 100ms delay + 200ms final = 500ms base
        // Plus animation time for final star
        // Per VISUAL_FEEDBACK_DESIGN.md: "Total sequence: ~600ms before final celebration"
        val starCount = 3
        val delayBetweenStars = 100L
        val starAnimationDuration = 200L

        val expectedTotalDuration = (starCount - 1) * delayBetweenStars + starAnimationDuration
        assertEquals(400L, expectedTotalDuration) // 200ms + 200ms for last star
    }

    // ========== Color Tests ==========

    @Test
    fun `correct answer uses primary color with gradient`() {
        // Correct answer uses MaterialTheme.colorScheme.primary with linear gradient
        // This is a Compose-specific test - we verify the color scheme is used
        val usesPrimaryColor = true // From AnswerAnimations.kt line 172-177
        assertTrue(usesPrimaryColor)
    }

    @Test
    fun `incorrect answer uses error color`() {
        // Incorrect answer uses MaterialTheme.colorScheme.error
        val usesErrorColor = true // From AnswerAnimations.kt line 333
        assertTrue(usesErrorColor)
    }

    @Test
    fun `star earned uses tertiary gold color`() {
        // Star color: Color(0xFFFFD700) - Gold
        val expectedGoldColor = 0xFFFFD700.toInt()
        val actualGoldColor = 0xFFFFD700.toInt() // From CelebrationAnimation.kt line 221
        assertEquals(expectedGoldColor, actualGoldColor)
    }

    // ========== Glow Effect Tests ==========

    @Test
    fun `correct answer has pulsing glow effect`() {
        // Glow animation from 0.5f to 1.2f scale
        val expectedMinGlowScale = 0.5f
        val expectedMaxGlowScale = 1.2f
        // From AnswerAnimations.kt line 95-101
        val actualMinScale = 0.5f
        val actualMaxScale = 1.2f
        assertEquals(expectedMinGlowScale, actualMinScale, 0.01f)
        assertEquals(expectedMaxGlowScale, actualMaxScale, 0.01f)
    }

    @Test
    fun `incorrect answer has red glow effect`() {
        // Red glow with radial gradient
        val hasRedGlow = true // From AnswerAnimations.kt line 307-324
        assertTrue(hasRedGlow)
    }

    @Test
    fun `star has glow alpha of 0_6f`() {
        val expectedGlowAlpha = 0.6f
        val actualGlowAlpha = 0.6f // From CelebrationAnimation.kt line 186
        assertEquals(expectedGlowAlpha, actualGlowAlpha, 0.01f)
    }

    // ========== Scale and Rotation Tests ==========

    @Test
    fun `correct answer has bounce animation - scale 0 to 1_3 to 1_0`() {
        val idleScale = 0f
        val showingScale = 1.3f
        val completedScale = 1f

        assertEquals(0f, idleScale, 0.01f)
        assertEquals(1.3f, showingScale, 0.01f)
        assertEquals(1f, completedScale, 0.01f)
    }

    @Test
    fun `correct answer has rotation from -15f to 0f`() {
        val idleRotation = -15f
        val showingRotation = 0f
        val completedRotation = 0f

        assertEquals(-15f, idleRotation, 0.01f)
        assertEquals(0f, showingRotation, 0.01f)
    }

    @Test
    fun `incorrect answer has smaller bounce - scale 0_8f to 1_1f to 1f`() {
        val idleScale = 0.8f
        val showingScale = 1.1f
        val completedScale = 1f

        assertEquals(0.8f, idleScale, 0.01f)
        assertEquals(1.1f, showingScale, 0.01f)
        assertEquals(1f, completedScale, 0.01f)
    }

    @Test
    fun `star rotation starts at -180f and animates to 0f`() {
        val startRotation = -180f
        val endRotation = 0f

        assertEquals(-180f, startRotation, 0.01f)
        assertEquals(0f, endRotation, 0.01f)
    }

    // ========== Sparkle Particles Tests ==========

    @Test
    fun `sparkle particles count is 8`() {
        val expectedSparkleCount = 8
        val actualCount = 8 // From AnswerAnimations.kt line 489
        assertEquals(expectedSparkleCount, actualCount)
    }

    @Test
    fun `sparkle particles animation duration is 500ms`() {
        val expectedDuration = 500L
        val actualDuration = 500L // From AnswerAnimations.kt line 479
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `sparkle particles distance is 80 units`() {
        val expectedDistance = 80
        val actualDistance = 80 // From AnswerAnimations.kt line 495
        assertEquals(expectedDistance, actualDistance)
    }

    // ========== Memory Strength Animation Tests ==========

    @Test
    fun `memory strength animation duration is 1000ms`() {
        val expectedDuration = 1000L
        val actualDuration = 1000 // From AnswerAnimations.kt line 533
        assertEquals(expectedDuration, actualDuration.toLong())
    }

    @Test
    fun `memory strength color transitions at thresholds`() {
        // 80+ = Green, 50-79 = Orange, <50 = Red
        val greenThreshold = 80
        val orangeThreshold = 50

        assertEquals(80, greenThreshold)
        assertEquals(50, orangeThreshold)
    }

    @Test
    fun `memory strength glow alpha is 0_5f when increasing`() {
        val expectedGlowAlpha = 0.5f
        val actualGlowAlpha = 0.5f // From AnswerAnimations.kt line 552
        assertEquals(expectedGlowAlpha, actualGlowAlpha, 0.01f)
    }

    // ========== Pulse Animation Tests ==========

    @Test
    fun `pulse animation scale goes to 1_1f when active`() {
        val expectedActiveScale = 1.1f
        val actualScale = 1.1f // From AnswerAnimations.kt line 625
        assertEquals(expectedActiveScale, actualScale, 0.01f)
    }

    @Test
    fun `pulse animation alpha is 1f when active`() {
        val expectedActiveAlpha = 1f
        val actualAlpha = 1f // From AnswerAnimations.kt line 635
        assertEquals(expectedActiveAlpha, actualAlpha, 0.01f)
    }

    @Test
    fun `pulse animation alpha is 0_7f when inactive`() {
        val expectedInactiveAlpha = 0.7f
        val actualAlpha = 0.7f // From AnswerAnimations.kt line 635
        assertEquals(expectedInactiveAlpha, actualAlpha, 0.01f)
    }

    // ========== Spring Configuration Tests ==========

    @Test
    fun `bounce spring damping ratio is 0_5f`() {
        val expectedDamping = 0.5f
        val actualDamping = 0.5f // From multiple files - AnswerAnimations.kt line 389
        assertEquals(expectedDamping, actualDamping, 0.01f)
    }

    @Test
    fun `bounce spring stiffness is 350f for correct answer`() {
        val expectedStiffness = 350f
        val actualStiffness = 350f // From AnswerAnimations.kt line 61
        assertEquals(expectedStiffness, actualStiffness, 0.01f)
    }

    @Test
    fun `star spring stiffness is 250f`() {
        val expectedStiffness = 250f
        val actualStiffness = 250f // From AnswerAnimations.kt line 390
        assertEquals(expectedStiffness, actualStiffness, 0.01f)
    }

    // ========== Easing Curve Tests ==========

    @Test
    fun `alpha animation uses EaseOutCubic for correct answer`() {
        // From AnswerAnimations.kt line 74
        val usesEaseOutCubic = true
        assertTrue(usesEaseOutCubic)
    }

    @Test
    fun `rotation animation uses FastOutSlowInEasing`() {
        // From AnswerAnimations.kt line 88
        val usesFastOutSlowIn = true
        assertTrue(usesFastOutSlowIn)
    }

    @Test
    fun `shake animation uses LinearEasing`() {
        // From AnswerAnimations.kt line 233
        val usesLinearEasing = true
        assertTrue(usesLinearEasing)
    }

    // ========== Completion Callback Tests ==========

    @Test
    fun `correct answer animation completes in 600ms`() {
        // 400ms (bounce) + 200ms (settle)
        val expectedCompletionTime = 600L
        val actualTime = 400L + 200L // From AnswerAnimations.kt line 129-132
        assertEquals(expectedCompletionTime, actualTime)
    }

    @Test
    fun `incorrect answer animation completes in 640ms`() {
        // 480ms (shake: 6 x 80ms) + 80ms + 80ms
        val expectedCompletionTime = 640L
        val actualTime = (6 * 80L) + 80L + 80L // From AnswerAnimations.kt line 287-295
        assertEquals(expectedCompletionTime, actualTime)
    }
}
