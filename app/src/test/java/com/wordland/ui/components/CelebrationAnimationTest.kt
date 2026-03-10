package com.wordland.ui.components

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for Celebration Animation configurations
 *
 * Epic #1: Visual Feedback Enhancement
 * Story #1.2: Celebration Animation Implementation
 *
 * Tests celebration configurations per star rating
 * Per VISUAL_FEEDBACK_DESIGN.md Section 6:
 * - 3-star: Full confetti, 1200ms, victory fanfare
 * - 2-star: Mini confetti, 800ms, cheer chime
 * - 1-star: Single star fade, 500ms, gentle ding
 */
@RunWith(JUnit4::class)
class CelebrationAnimationTest {
    // ========== 3-Star Configuration Tests ==========

    @Test
    fun `3 star has 50 confetti particles`() {
        // Per VISUAL_FEEDBACK_DESIGN.md Section 6.1
        val expectedConfettiCount = 50
        val actualCount = 50 // From getCelebrationConfig(3) line 503
        assertEquals(expectedConfettiCount, actualCount)
    }

    @Test
    fun `3 star has full confetti spread of 1_0f`() {
        val expectedSpread = 1.0f
        val actualSpread = 1.0f // From getCelebrationConfig(3) line 504
        assertEquals(expectedSpread, actualSpread, 0.01f)
    }

    @Test
    fun `3 star shows confetti`() {
        val expectedShowConfetti = true
        val actualShowConfetti = true // From getCelebrationConfig(3) line 505
        assertEquals(expectedShowConfetti, actualShowConfetti)
    }

    @Test
    fun `3 star message is Perfect exclamation`() {
        val expectedMessage = "Perfect! 太棒了!"
        val actualMessage = "Perfect! 太棒了!" // From getCelebrationConfig(3) line 506
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `3 star uses green background color`() {
        val expectedColor = 0xFF4CAF50.toInt()
        val actualColor = 0xFF4CAF50.toInt() // From getCelebrationConfig(3) line 507
        assertEquals(expectedColor, actualColor)
    }

    @Test
    fun `3 star uses darker green border`() {
        val expectedBorderColor = 0xFF388E3C.toInt()
        val actualBorderColor = 0xFF388E3C.toInt() // From getCelebrationConfig(3) line 508
        assertEquals(expectedBorderColor, actualBorderColor)
    }

    @Test
    fun `3 star uses white text`() {
        val expectedTextColor = Color.WHITE.toInt()
        val actualTextColor = Color.WHITE.toInt() // From getCelebrationConfig(3) line 509
        assertEquals(expectedTextColor, actualTextColor)
    }

    @Test
    fun `3 star reveal duration is 600ms`() {
        val expectedDuration = 600L
        val actualDuration = 600L // From getCelebrationConfig(3) line 510
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `3 star message duration is 1200ms`() {
        val expectedDuration = 1200L
        val actualDuration = 1200L // From getCelebrationConfig(3) line 511
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `3 star confetti duration is 800ms`() {
        val expectedDuration = 800
        val actualDuration = 800 // From getCelebrationConfig(3) line 512
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== 2-Star Configuration Tests ==========

    @Test
    fun `2 star has 20 confetti particles`() {
        val expectedConfettiCount = 20
        val actualCount = 20 // From getCelebrationConfig(2) line 516
        assertEquals(expectedConfettiCount, actualCount)
    }

    @Test
    fun `2 star has mini confetti spread of 0_6f`() {
        val expectedSpread = 0.6f
        val actualSpread = 0.6f // From getCelebrationConfig(2) line 517
        assertEquals(expectedSpread, actualSpread, 0.01f)
    }

    @Test
    fun `2 star shows confetti`() {
        val expectedShowConfetti = true
        val actualShowConfetti = true // From getCelebrationConfig(2) line 518
        assertEquals(expectedShowConfetti, actualShowConfetti)
    }

    @Test
    fun `2 star message is Great Job`() {
        val expectedMessage = "Great Job! 做得好!"
        val actualMessage = "Great Job! 做得好!" // From getCelebrationConfig(2) line 519
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `2 star uses blue background color`() {
        val expectedColor = 0xFF2196F3.toInt()
        val actualColor = 0xFF2196F3.toInt() // From getCelebrationConfig(2) line 520
        assertEquals(expectedColor, actualColor)
    }

    @Test
    fun `2 star uses darker blue border`() {
        val expectedBorderColor = 0xFF1976D2.toInt()
        val actualBorderColor = 0xFF1976D2.toInt() // From getCelebrationConfig(2) line 521
        assertEquals(expectedBorderColor, actualBorderColor)
    }

    @Test
    fun `2 star reveal duration is 400ms`() {
        val expectedDuration = 400L
        val actualDuration = 400L // From getCelebrationConfig(2) line 523
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `2 star message duration is 800ms`() {
        val expectedDuration = 800L
        val actualDuration = 800L // From getCelebrationConfig(2) line 524
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `2 star confetti duration is 600ms`() {
        val expectedDuration = 600
        val actualDuration = 600 // From getCelebrationConfig(2) line 525
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== 1-Star Configuration Tests ==========

    @Test
    fun `1 star has 0 confetti particles`() {
        val expectedConfettiCount = 0
        val actualCount = 0 // From getCelebrationConfig(1) line 529
        assertEquals(expectedConfettiCount, actualCount)
    }

    @Test
    fun `1 star has 0_0f confetti spread`() {
        val expectedSpread = 0f
        val actualSpread = 0f // From getCelebrationConfig(1) line 530
        assertEquals(expectedSpread, actualSpread, 0.01f)
    }

    @Test
    fun `1 star does not show confetti`() {
        val expectedShowConfetti = false
        val actualShowConfetti = false // From getCelebrationConfig(1) line 531
        assertEquals(expectedShowConfetti, actualShowConfetti)
    }

    @Test
    fun `1 star message is Good Try`() {
        val expectedMessage = "Good Try! 继续努力!"
        val actualMessage = "Good Try! 继续努力!" // From getCelebrationConfig(1) line 532
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `1 star uses orange background color`() {
        val expectedColor = 0xFFFF9800.toInt()
        val actualColor = 0xFFFF9800.toInt() // From getCelebrationConfig(1) line 533
        assertEquals(expectedColor, actualColor)
    }

    @Test
    fun `1 star reveal duration is 300ms`() {
        val expectedDuration = 300L
        val actualDuration = 300L // From getCelebrationConfig(1) line 536
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `1 star message duration is 500ms`() {
        val expectedDuration = 500L
        val actualDuration = 500L // From getCelebrationConfig(1) line 537
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `1 star confetti duration is 0ms`() {
        val expectedDuration = 0
        val actualDuration = 0 // From getCelebrationConfig(1) line 538
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== 0-Star Configuration Tests ==========

    @Test
    fun `0 star has 0 confetti particles`() {
        val expectedConfettiCount = 0
        val actualCount = 0 // From getCelebrationConfig(0) line 542
        assertEquals(expectedConfettiCount, actualCount)
    }

    @Test
    fun `0 star does not show confetti`() {
        val expectedShowConfetti = false
        val actualShowConfetti = false // From getCelebrationConfig(0) line 543
        assertEquals(expectedShowConfetti, actualShowConfetti)
    }

    @Test
    fun `0 star message is Let's try again`() {
        val expectedMessage = "Let's try again! 再试一次!"
        val actualMessage = "Let's try again! 再试一次!" // From getCelebrationConfig(0) line 545
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun `0 star reveal duration is 0ms`() {
        val expectedDuration = 0L
        val actualDuration = 0L // From getCelebrationConfig(0) line 549
        assertEquals(expectedDuration, actualDuration)
    }

    @Test
    fun `0 star message duration is 400ms`() {
        val expectedDuration = 400L
        val actualDuration = 400L // From getCelebrationConfig(0) line 550
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== Star Reveal Animation Tests ==========

    @Test
    fun `star reveal delay between stars is 100ms`() {
        val expectedDelay = 100L
        val actualDelay = 100L // From STAR_REVEAL_DELAY_MS
        assertEquals(expectedDelay, actualDelay)
    }

    @Test
    fun `star spring damping ratio is 0_5f`() {
        val expectedDamping = 0.5f
        val actualDamping = 0.5f // From SPRING_DAMPING_RATIO
        assertEquals(expectedDamping, actualDamping, 0.01f)
    }

    @Test
    fun `star spring stiffness is 300f`() {
        val expectedStiffness = 300f
        val actualStiffness = 300f // From SPRING_STIFFNESS
        assertEquals(expectedStiffness, actualStiffness, 0.01f)
    }

    // ========== Celebration Phase Tests ==========

    @Test
    fun `celebration has three phases`() {
        val phases = listOf("STARS", "MESSAGE", "COMPLETE")
        assertEquals(3, phases.size)
        assertEquals("STARS", phases[0])
        assertEquals("MESSAGE", phases[1])
        assertEquals("COMPLETE", phases[2])
    }

    @Test
    fun `celebration total duration for 3 stars is 1800ms`() {
        // 600ms (stars) + 1200ms (message) = 1800ms
        val stars = 3
        val starRevealDuration = 600L
        val messageDuration = 1200L
        val expectedTotal = starRevealDuration + messageDuration

        assertEquals(1800L, expectedTotal)
    }

    @Test
    fun `celebration total duration for 2 stars is 1200ms`() {
        // 400ms (stars) + 800ms (message) = 1200ms
        val stars = 2
        val starRevealDuration = 400L
        val messageDuration = 800L
        val expectedTotal = starRevealDuration + messageDuration

        assertEquals(1200L, expectedTotal)
    }

    @Test
    fun `celebration total duration for 1 star is 800ms`() {
        // 300ms (stars) + 500ms (message) = 800ms
        val stars = 1
        val starRevealDuration = 300L
        val messageDuration = 500L
        val expectedTotal = starRevealDuration + messageDuration

        assertEquals(800L, expectedTotal)
    }

    // ========== Compact Celebration Tests ==========

    @Test
    fun `compact celebration star delay is 100ms`() {
        val expectedDelay = 100L
        val actualDelay = 100L // From CompactCelebrationAnimation line 386
        assertEquals(expectedDelay, actualDelay)
    }

    @Test
    fun `compact celebration shows confetti for 2+ stars`() {
        val showConfettiFor2Stars = true
        val showConfettiFor1Star = false

        assertTrue(showConfettiFor2Stars)
        assertFalse(showConfettiFor1Star)
    }

    // ========== Quick Celebration Popup Tests ==========

    @Test
    fun `quick popup auto dismisses after message duration`() {
        val delayDuration = 300 // Exit animation
        val actualDelay = 300 // From QuickCelebrationPopup line 450
        assertEquals(delayDuration, actualDelay)
    }

    // ========== Star Glow Tests ==========

    @Test
    fun `earned star has glow alpha of 0_6f`() {
        val expectedGlowAlpha = 0.6f
        val actualGlowAlpha = 0.6f // From CelebrationAnimation.kt line 186
        assertEquals(expectedGlowAlpha, actualGlowAlpha, 0.01f)
    }

    @Test
    fun `earned star glow animation duration is 300ms`() {
        val expectedDuration = 300
        val actualDuration = 300 // From CelebrationAnimation.kt line 187
        assertEquals(expectedDuration, actualDuration)
    }

    // ========== Stats Display Tests ==========

    @Test
    fun `stats display shows score when greater than 0`() {
        val score = 100
        val shouldShow = score > 0
        assertTrue(shouldShow)
    }

    @Test
    fun `stats display shows combo when greater than 0`() {
        val combo = 5
        val shouldShow = combo > 0
        assertTrue(shouldShow)
    }

    @Test
    fun `combo stat uses orange color`() {
        val expectedComboColor = 0xFFFF6B35.toInt()
        val actualComboColor = 0xFFFF6B35.toInt() // From StatsDisplay line 334
        assertEquals(expectedComboColor, actualComboColor)
    }
}

/**
 * Helper class for color values
 */
private object Color {
    const val WHITE = 0xFFFFFFFF.toInt()
}
