package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for FeedbackType and related functions
 * Part of P0 Phase 1: Visual Feedback Enhancements
 */
class FeedbackTypeTest {
    @Test
    fun `FeedbackType PerfectThreeStar returns 3 stars`() {
        assertEquals(3, FeedbackType.PerfectThreeStar.stars)
    }

    @Test
    fun `FeedbackType GoodTwoStar returns 2 stars`() {
        assertEquals(2, FeedbackType.GoodTwoStar.stars)
    }

    @Test
    fun `FeedbackType CorrectOneStar returns 1 star`() {
        assertEquals(1, FeedbackType.CorrectOneStar.stars)
    }

    @Test
    fun `FeedbackType Incorrect returns 0 stars`() {
        assertEquals(0, FeedbackType.Incorrect.stars)
    }

    @Test
    fun `determineFeedbackType returns Incorrect for wrong answer`() {
        val result =
            determineFeedbackType(
                isCorrect = false,
                attempts = 1,
                hintsUsed = 0,
            )
        assertEquals(FeedbackType.Incorrect, result)
    }

    @Test
    fun `determineFeedbackType returns Perfect for first attempt correct without hints`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 1,
                hintsUsed = 0,
                // For 5-letter word: need at least 3500ms for adequate thinking
                responseTimeMs = 4000L,
                wordLength = 5,
            )
        assertEquals(FeedbackType.PerfectThreeStar, result)
    }

    @Test
    fun `determineFeedbackType returns Good for correct with 1 hint`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 1,
                hintsUsed = 1,
                responseTimeMs = 4000L,
                wordLength = 5,
            )
        assertEquals(FeedbackType.GoodTwoStar, result)
    }

    @Test
    fun `determineFeedbackType returns Good for correct on second attempt`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 2,
                hintsUsed = 0,
                responseTimeMs = 4000L,
                wordLength = 5,
            )
        assertEquals(FeedbackType.GoodTwoStar, result)
    }

    @Test
    fun `determineFeedbackType returns Correct for correct after multiple attempts`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 3,
                hintsUsed = 2,
                // Use very short response time to trigger guessing detection
                responseTimeMs = 100L,
                wordLength = 5,
            )
        // With guessing detection and many attempts/hints, should be CorrectOneStar
        assertEquals(FeedbackType.CorrectOneStar, result)
    }

    @Test
    fun `determineFeedbackType detects guessing - too fast response`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 1,
                hintsUsed = 0,
                responseTimeMs = 200L, // Too fast for 5 letter word
                wordLength = 5,
            )
        // Fast correct answer should not get Perfect rating
        assertNotEquals(FeedbackType.PerfectThreeStar, result)
    }

    @Test
    fun `determineFeedbackType allows fast response for short words`() {
        val result =
            determineFeedbackType(
                isCorrect = true,
                attempts = 1,
                hintsUsed = 0,
                // For 2-letter word: need at least 2000ms (1000 + 2*500)
                responseTimeMs = 2500L,
                wordLength = 2,
            )
        assertEquals(FeedbackType.PerfectThreeStar, result)
    }

    @Test
    fun `FeedbackType getMessage returns correct messages`() {
        assertEquals("完美！", FeedbackType.PerfectThreeStar.getMessage(true))
        assertEquals("很好！", FeedbackType.GoodTwoStar.getMessage(true))
        assertEquals("正确！", FeedbackType.CorrectOneStar.getMessage(true))
        assertEquals("再试一次！", FeedbackType.Incorrect.getMessage(false))
    }
}

/**
 * Unit tests for ComboState extension functions
 */
class ComboStateExtensionTest {
    @Test
    fun `ComboState isFireStreak extension returns true for 5+ consecutive correct`() {
        val state = ComboState(consecutiveCorrect = 5)
        assertTrue(state.isFireStreak)
    }

    @Test
    fun `ComboState isFireStreak extension returns false for less than 5 consecutive correct`() {
        val state = ComboState(consecutiveCorrect = 4)
        assertFalse(state.isFireStreak)
    }

    @Test
    fun `ComboState getMotivationalMessage returns fire message for hot streak`() {
        val state = ComboState(consecutiveCorrect = 5)
        assertTrue(state.getMotivationalMessage(false).contains("🔥"))
    }

    @Test
    fun `ComboState getMotivationalMessage returns last word message`() {
        val state = ComboState(consecutiveCorrect = 3)
        assertEquals("最后一个词！", state.getMotivationalMessage(true))
    }

    @Test
    fun `ComboState getMotivationalMessage returns start message for zero combo`() {
        val state = ComboState()
        assertEquals("开始吧！", state.getMotivationalMessage(false))
    }

    @Test
    fun `ComboState getMotivationalMessage returns continue message for moderate combo`() {
        val state = ComboState(consecutiveCorrect = 2)
        assertEquals("继续前进！", state.getMotivationalMessage(false))
    }

    @Test
    fun `ComboState getMotivationalMessage returns great message for 3+ combo`() {
        val state = ComboState(consecutiveCorrect = 3)
        assertEquals("太棒了！", state.getMotivationalMessage(false))
    }

    @Test
    fun `ComboState afterCorrectAnswer increments combo with adequate thinking time`() {
        val state = ComboState()
        // For 5-letter word: 1000ms + (5 * 500ms) = 3500ms threshold
        // Use 4000ms to ensure adequate thinking time
        val newState = state.afterCorrectAnswer(responseTime = 4000L, wordLength = 5)
        assertEquals(1, newState.consecutiveCorrect)
        assertEquals(1.0f, newState.multiplier)
    }

    @Test
    fun `ComboState afterCorrectAnswer does not increment combo for guessing`() {
        val state = ComboState()
        val newState = state.afterCorrectAnswer(responseTime = 200L, wordLength = 5)
        assertEquals(0, newState.consecutiveCorrect) // No increment due to guessing
    }

    @Test
    fun `ComboState afterCorrectAnswer sets multiplier to 1_2 after 3 correct`() {
        var state = ComboState()
        repeat(3) {
            // For 5-letter word: need at least 3500ms for adequate thinking
            state = state.afterCorrectAnswer(responseTime = 4000L, wordLength = 5)
        }
        assertEquals(3, state.consecutiveCorrect)
        assertEquals(1.2f, state.multiplier)
    }

    @Test
    fun `ComboState reset clears all progress`() {
        var state = ComboState()
        repeat(3) {
            // For 5-letter word: need at least 3500ms for adequate thinking
            state = state.afterCorrectAnswer(responseTime = 4000L, wordLength = 5)
        }
        state = state.reset()
        assertEquals(0, state.consecutiveCorrect)
        assertEquals(1.0f, state.multiplier)
        assertFalse(state.isHighCombo)
    }
}

/**
 * Unit tests for AnimationQuality
 */
class AnimationQualityTest {
    @Test
    fun `AnimationQuality High has 100 particles`() {
        assertEquals(100, AnimationQuality.High.particleCount)
    }

    @Test
    fun `AnimationQuality Medium has 40 particles`() {
        assertEquals(40, AnimationQuality.Medium.particleCount)
    }

    @Test
    fun `AnimationQuality Low has 0 particles`() {
        assertEquals(0, AnimationQuality.Low.particleCount)
    }

    @Test
    fun `AnimationQuality High has animations enabled`() {
        assertTrue(AnimationQuality.High.animationEnabled)
    }

    @Test
    fun `AnimationQuality Medium has animations enabled`() {
        assertTrue(AnimationQuality.Medium.animationEnabled)
    }

    @Test
    fun `AnimationQuality Low has animations disabled`() {
        assertFalse(AnimationQuality.Low.animationEnabled)
    }

    @Test
    fun `AnimationQuality High targets 60fps`() {
        assertEquals(60, AnimationQuality.High.targetFps)
    }

    @Test
    fun `AnimationQuality Medium targets 30fps`() {
        assertEquals(30, AnimationQuality.Medium.targetFps)
    }

    @Test
    fun `AnimationQuality Low targets 30fps`() {
        assertEquals(30, AnimationQuality.Low.targetFps)
    }

    @Test
    fun `getAnimationQuality returns High for HIGH tier`() {
        assertEquals(AnimationQuality.High, getAnimationQuality(PerformanceTier.HIGH))
    }

    @Test
    fun `getAnimationQuality returns Medium for MEDIUM tier`() {
        assertEquals(AnimationQuality.Medium, getAnimationQuality(PerformanceTier.MEDIUM))
    }

    @Test
    fun `getAnimationQuality returns Low for LOW tier`() {
        assertEquals(AnimationQuality.Low, getAnimationQuality(PerformanceTier.LOW))
    }
}
