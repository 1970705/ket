package com.wordland.domain.combo

import com.wordland.domain.model.ComboState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ComboManager
 * Tests anti-guessing logic, combo calculation, and multiplier application
 */
class ComboManagerTest {
    // ComboManager is an object (singleton), no need to instantiate

    // ========== isGuessing Tests ==========

    @Test
    fun `answer under minimum time is guessing`() {
        // 3-letter word: minimum is 2500ms
        val isGuessing = ComboManager.isGuessing(2000L, 3)
        assertTrue("Answer under minimum time should be guessing", isGuessing)
    }

    @Test
    fun `answer at minimum time is not guessing`() {
        // 3-letter word: minimum is 2500ms
        val isGuessing = ComboManager.isGuessing(2500L, 3)
        assertFalse("Answer at minimum time should not be guessing", isGuessing)
    }

    @Test
    fun `answer over minimum time is not guessing`() {
        // 5-letter word: minimum is 3500ms
        val isGuessing = ComboManager.isGuessing(4000L, 5)
        assertFalse("Answer over minimum time should not be guessing", isGuessing)
    }

    @Test
    fun `very fast answer on long word is guessing`() {
        // 8-letter word: minimum is 5000ms, answered in 2000ms
        val isGuessing = ComboManager.isGuessing(2000L, 8)
        assertTrue("Very fast answer on long word should be guessing", isGuessing)
    }

    // ========== calculateMinimumThinkTime Tests ==========

    @Test
    fun `minimum think time formula is correct`() {
        // Formula: 1000ms + (wordLength * 500ms)
        assertEquals(1500L, ComboManager.calculateMinimumThinkTime(1))
        assertEquals(2000L, ComboManager.calculateMinimumThinkTime(2))
        assertEquals(2500L, ComboManager.calculateMinimumThinkTime(3))
        assertEquals(3000L, ComboManager.calculateMinimumThinkTime(4))
        assertEquals(3500L, ComboManager.calculateMinimumThinkTime(5))
        assertEquals(4000L, ComboManager.calculateMinimumThinkTime(6))
        assertEquals(5000L, ComboManager.calculateMinimumThinkTime(8))
        assertEquals(6000L, ComboManager.calculateMinimumThinkTime(10))
    }

    // ========== getMultiplier Tests ==========

    @Test
    fun `multiplier is 10 for 0-2 combo`() {
        assertEquals(1.0f, ComboManager.getMultiplier(0))
        assertEquals(1.0f, ComboManager.getMultiplier(1))
        assertEquals(1.0f, ComboManager.getMultiplier(2))
    }

    @Test
    fun `multiplier is 12 for 3-4 combo`() {
        assertEquals(1.2f, ComboManager.getMultiplier(3))
        assertEquals(1.2f, ComboManager.getMultiplier(4))
    }

    @Test
    fun `multiplier is 15 for 5+ combo`() {
        assertEquals(1.5f, ComboManager.getMultiplier(5))
        assertEquals(1.5f, ComboManager.getMultiplier(10))
        assertEquals(1.5f, ComboManager.getMultiplier(100))
    }

    // ========== calculateCombo Tests ==========

    @Test
    fun `correct answer with adequate time increments combo`() {
        val currentState = ComboState(consecutiveCorrect = 0)
        val newState =
            ComboManager.calculateCombo(
                currentState = currentState,
                isCorrect = true,
                responseTime = 3500L, // 5-letter word needs >=3500ms
                wordLength = 5,
            )
        assertEquals(1, newState.consecutiveCorrect)
        assertEquals(1.0f, newState.multiplier)
    }

    @Test
    fun `correct answer with inadequate time does not increment combo`() {
        val currentState = ComboState(consecutiveCorrect = 0)
        val newState =
            ComboManager.calculateCombo(
                currentState = currentState,
                isCorrect = true,
                responseTime = 1000L, // Too fast
                wordLength = 5,
            )
        assertEquals(0, newState.consecutiveCorrect) // Should not increment
        assertEquals(1.0f, newState.multiplier)
    }

    @Test
    fun `wrong answer resets combo`() {
        val currentState = ComboState(consecutiveCorrect = 5)
        val newState =
            ComboManager.calculateCombo(
                currentState = currentState,
                isCorrect = false,
                responseTime = 0L,
                wordLength = 0,
            )
        assertEquals(0, newState.consecutiveCorrect)
        assertEquals(1.0f, newState.multiplier)
    }

    @Test
    fun `combo reaches 3-combo milestone with multiplier`() {
        val currentState = ComboState(consecutiveCorrect = 2)
        val newState =
            ComboManager.calculateCombo(
                currentState = currentState,
                isCorrect = true,
                responseTime = 3000L, // 4-letter word needs >=3000ms (exactly at threshold)
                wordLength = 4,
            )
        assertEquals(3, newState.consecutiveCorrect)
        assertEquals(1.2f, newState.multiplier)
    }

    @Test
    fun `combo reaches 5-combo milestone with multiplier`() {
        val currentState = ComboState(consecutiveCorrect = 4)
        val newState =
            ComboManager.calculateCombo(
                currentState = currentState,
                isCorrect = true,
                responseTime = 4000L,
                wordLength = 5,
            )
        assertEquals(5, newState.consecutiveCorrect)
        assertEquals(1.5f, newState.multiplier)
    }

    // ========== getMilestoneReached Tests ==========

    @Test
    fun `milestone detected when reaching 3 combo`() {
        val milestone = ComboManager.getMilestoneReached(2, 3)
        assertEquals(3, milestone)
    }

    @Test
    fun `milestone detected when reaching 5 combo`() {
        val milestone = ComboManager.getMilestoneReached(4, 5)
        assertEquals(5, milestone)
    }

    @Test
    fun `no milestone when staying at same combo`() {
        val milestone = ComboManager.getMilestoneReached(3, 3)
        assertNull(milestone)
    }

    @Test
    fun `no milestone when incrementing within same tier`() {
        val milestone = ComboManager.getMilestoneReached(3, 4)
        assertNull(milestone)
    }

    @Test
    fun `no milestone when reset to zero`() {
        val milestone = ComboManager.getMilestoneReached(5, 0)
        assertNull(milestone)
    }

    // ========== applyMultiplier Tests ==========

    @Test
    fun `multiplier applied correctly to base score`() {
        assertEquals(10, ComboManager.applyMultiplier(10, 1.0f))
        assertEquals(12, ComboManager.applyMultiplier(10, 1.2f))
        assertEquals(15, ComboManager.applyMultiplier(10, 1.5f))

        assertEquals(30, ComboManager.applyMultiplier(25, 1.2f))
        assertEquals(37, ComboManager.applyMultiplier(25, 1.5f)) // 37.5 truncated
    }

    // ========== getComboMessage Tests ==========

    @Test
    fun `combo message is null for low combos`() {
        assertNull(ComboManager.getComboMessage(0))
        assertNull(ComboManager.getComboMessage(1))
        assertNull(ComboManager.getComboMessage(2))
    }

    @Test
    fun `combo message appears at 3 combo`() {
        assertEquals("Nice streak!", ComboManager.getComboMessage(3))
    }

    @Test
    fun `combo message changes at higher combos`() {
        assertEquals("Keep it up!", ComboManager.getComboMessage(4))
        assertEquals("On fire! \uD83D\uDD25", ComboManager.getComboMessage(5))
        assertEquals("Unstoppable!", ComboManager.getComboMessage(6))
    }

    @Test
    fun `special message at 10 combo`() {
        assertEquals("Godlike! \uD83D\uDC51", ComboManager.getComboMessage(10))
    }

    @Test
    fun `fire emoji appears every 5 combos after 5`() {
        val message15 = ComboManager.getComboMessage(15)
        assertTrue("Message at 15 should contain fire emoji", message15?.contains("\uD83D\uDD25") == true)

        val message20 = ComboManager.getComboMessage(20)
        assertTrue("Message at 20 should contain fire emoji", message20?.contains("\uD83D\uDD25") == true)
    }

    // ========== shouldTriggerMilestoneAnimation Tests ==========

    @Test
    fun `animation triggers at 3 combo`() {
        assertTrue(ComboManager.shouldTriggerMilestoneAnimation(3))
    }

    @Test
    fun `animation triggers at 5 combo`() {
        assertTrue(ComboManager.shouldTriggerMilestoneAnimation(5))
    }

    @Test
    fun `animation does not trigger at 2 combo`() {
        assertFalse(ComboManager.shouldTriggerMilestoneAnimation(2))
    }

    @Test
    fun `animation does not trigger at 4 combo`() {
        assertFalse(ComboManager.shouldTriggerMilestoneAnimation(4))
    }

    @Test
    fun `animation triggers every 5 combos after milestone`() {
        assertTrue(ComboManager.shouldTriggerMilestoneAnimation(10))
        assertTrue(ComboManager.shouldTriggerMilestoneAnimation(15))
        assertFalse(ComboManager.shouldTriggerMilestoneAnimation(12))
    }

    // ========== Real-World Scenarios ==========

    @Test
    fun `scenario - honest player builds combo slowly`() {
        var state = ComboState()

        // Player thinks carefully about each answer
        state = ComboManager.calculateCombo(state, true, 3000L, 4) // Exactly minimum for 4 letters
        assertEquals(1, state.consecutiveCorrect)

        state = ComboManager.calculateCombo(state, true, 3200L, 4) // Above minimum
        assertEquals(2, state.consecutiveCorrect)

        state = ComboManager.calculateCombo(state, true, 3500L, 4) // Above minimum
        assertEquals(3, state.consecutiveCorrect)
        assertEquals(1.2f, state.multiplier)
    }

    @Test
    fun `scenario - guesser never builds combo`() {
        var state = ComboState()

        // Player answers too quickly every time
        state = ComboManager.calculateCombo(state, true, 500L, 4)
        assertEquals(0, state.consecutiveCorrect)

        state = ComboManager.calculateCombo(state, true, 800L, 5)
        assertEquals(0, state.consecutiveCorrect)

        state = ComboManager.calculateCombo(state, true, 600L, 3)
        assertEquals(0, state.consecutiveCorrect)

        // Even after 3 "correct" answers, combo is still 0
        assertEquals(1.0f, state.multiplier)
    }

    @Test
    fun `scenario - combo resets on single wrong answer`() {
        var state = ComboState()

        // Build up to 5 combo
        repeat(5) {
            state = ComboManager.calculateCombo(state, true, 3500L, 5)
        }
        assertEquals(5, state.consecutiveCorrect)
        assertEquals(1.5f, state.multiplier)

        // One wrong answer
        state = ComboManager.calculateCombo(state, false, 0L, 0)
        assertEquals(0, state.consecutiveCorrect)
        assertEquals(1.0f, state.multiplier)
    }

    @Test
    fun `scenario - mixed speed and accuracy`() {
        var state = ComboState()

        // Slow correct answer
        state = ComboManager.calculateCombo(state, true, 4000L, 5)
        assertEquals(1, state.consecutiveCorrect)

        // Fast correct answer (guessing) - no increment
        state = ComboManager.calculateCombo(state, true, 1000L, 5)
        assertEquals(1, state.consecutiveCorrect)

        // Another slow correct answer
        state = ComboManager.calculateCombo(state, true, 3500L, 4)
        assertEquals(2, state.consecutiveCorrect)

        // Wrong answer - reset
        state = ComboManager.calculateCombo(state, false, 0L, 0)
        assertEquals(0, state.consecutiveCorrect)
    }

    @Test
    fun `scenario - building to fire streak`() {
        var state = ComboState()

        // Build up step by step with adequate thinking time
        // Each word gets appropriate time based on its length
        val testCases =
            listOf(
                4 to 3000L, // 4 letters needs >=3000ms
                5 to 3500L, // 5 letters needs >=3500ms
                4 to 3100L, // 4 letters needs >=3000ms
                6 to 4000L, // 6 letters needs >=4000ms
                5 to 3600L, // 5 letters needs >=3500ms
            )
        testCases.forEach { (length, time) ->
            state = ComboManager.calculateCombo(state, true, time, length)
        }

        assertEquals(5, state.consecutiveCorrect)
        assertEquals(1.5f, state.multiplier)
        assertNotNull(ComboManager.getComboMessage(5))
    }

    @Test
    fun `scenario - short words still require thinking time`() {
        var state = ComboState()

        // Even short words (3 letters) require 2500ms minimum
        state = ComboManager.calculateCombo(state, true, 2000L, 3)
        assertEquals(0, state.consecutiveCorrect) // Too fast

        state = ComboManager.calculateCombo(state, true, 2600L, 3)
        assertEquals(1, state.consecutiveCorrect) // Adequate time
    }
}
