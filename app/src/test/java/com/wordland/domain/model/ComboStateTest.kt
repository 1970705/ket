package com.wordland.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ComboState
 * Tests all combo logic including anti-guessing and multiplier calculation
 */
class ComboStateTest {
    // ========== Basic State Tests ==========

    @Test
    fun `initial combo state has zero values`() {
        val state = ComboState()
        assertEquals(0, state.consecutiveCorrect)
        assertEquals(0L, state.averageResponseTime)
        assertEquals(1.0f, state.multiplier)
    }

    @Test
    fun `combo is not active when count is zero`() {
        val state = ComboState()
        assertFalse(state.isActive)
    }

    @Test
    fun `combo is active when count is greater than zero`() {
        val state = ComboState(consecutiveCorrect = 1)
        assertTrue(state.isActive)
    }

    // ========== Minimum Think Time Calculation ==========

    @Test
    fun `minimum think time for 3-letter word is 2500ms`() {
        val state = ComboState()
        assertEquals(2500L, state.calculateMinimumThinkTime(3))
    }

    @Test
    fun `minimum think time for 5-letter word is 3500ms`() {
        val state = ComboState()
        assertEquals(3500L, state.calculateMinimumThinkTime(5))
    }

    @Test
    fun `minimum think time for 6-letter word is 4000ms`() {
        val state = ComboState()
        assertEquals(4000L, state.calculateMinimumThinkTime(6))
    }

    @Test
    fun `minimum think time for 8-letter word is 5000ms`() {
        val state = ComboState()
        assertEquals(5000L, state.calculateMinimumThinkTime(8))
    }

    // ========== Adequate Thinking Time Detection ==========

    @Test
    fun `fast answer is considered guessing`() {
        val state = ComboState()
        // 3-letter word requires 2500ms minimum
        val isAdequate = state.isAdequateThinkingTime(1500L, 3)
        assertFalse("Fast answer should not be adequate thinking time", isAdequate)
    }

    @Test
    fun `adequate time answer is not guessing`() {
        val state = ComboState()
        // 3-letter word requires 2500ms minimum
        val isAdequate = state.isAdequateThinkingTime(3000L, 3)
        assertTrue("Adequate time answer should pass", isAdequate)
    }

    @Test
    fun `exactly minimum time is considered adequate`() {
        val state = ComboState()
        // 5-letter word requires 3500ms minimum
        val isAdequate = state.isAdequateThinkingTime(3500L, 5)
        assertTrue("Exactly minimum time should be adequate", isAdequate)
    }

    // ========== Combo Increment Logic ==========

    @Test
    fun `correct answer with adequate time increments combo`() {
        val state = ComboState(consecutiveCorrect = 0)
        val newState = state.afterCorrectAnswer(3500L, 5) // 5-letter word, 3500ms min
        assertEquals(1, newState.consecutiveCorrect)
        assertEquals(1.0f, newState.multiplier) // Still below 3
    }

    @Test
    fun `correct answer with inadequate time does NOT increment combo`() {
        val state = ComboState(consecutiveCorrect = 0)
        val newState = state.afterCorrectAnswer(1000L, 5) // 5-letter word, too fast
        assertEquals(0, newState.consecutiveCorrect) // Should not increment
        assertEquals(1.0f, newState.multiplier)
    }

    @Test
    fun `combo reaches 3-combo milestone`() {
        val state = ComboState(consecutiveCorrect = 2)
        val newState = state.afterCorrectAnswer(3000L, 3)
        assertEquals(3, newState.consecutiveCorrect)
        assertEquals(1.2f, newState.multiplier)
        assertTrue(newState.isAtMilestone)
    }

    @Test
    fun `combo reaches 5-combo milestone`() {
        val state = ComboState(consecutiveCorrect = 4)
        val newState = state.afterCorrectAnswer(4000L, 5)
        assertEquals(5, newState.consecutiveCorrect)
        assertEquals(1.5f, newState.multiplier)
        assertTrue(newState.isAtMilestone)
        assertTrue(newState.isHighCombo)
    }

    // ========== Combo Reset Logic ==========

    @Test
    fun `reset returns combo to initial state`() {
        val state = ComboState(consecutiveCorrect = 5, averageResponseTime = 4000L)
        val resetState = state.reset()
        assertEquals(0, resetState.consecutiveCorrect)
        assertEquals(0L, resetState.averageResponseTime)
        assertEquals(1.0f, resetState.multiplier)
    }

    // ========== Average Response Time Calculation ==========

    @Test
    fun `average response time is calculated correctly`() {
        val state = ComboState(consecutiveCorrect = 2, averageResponseTime = 3000L)
        // New answer with 4000ms should update average to (3000*2 + 4000)/3 = 3333.33
        val newState = state.afterCorrectAnswer(4000L, 5)
        assertEquals(3, newState.consecutiveCorrect)
        assertEquals(3333L, newState.averageResponseTime) // Rounded
    }

    @Test
    fun `first answer sets average to its time`() {
        val state = ComboState()
        val newState = state.afterCorrectAnswer(3500L, 5)
        assertEquals(1, newState.consecutiveCorrect)
        assertEquals(3500L, newState.averageResponseTime)
    }

    // ========== Milestone Detection ==========

    @Test
    fun `combo of 3 is a milestone`() {
        val state = ComboState(consecutiveCorrect = 3)
        assertTrue(state.isAtMilestone)
    }

    @Test
    fun `combo of 5 is a milestone`() {
        val state = ComboState(consecutiveCorrect = 5)
        assertTrue(state.isAtMilestone)
    }

    @Test
    fun `combo of 4 is not a milestone`() {
        val state = ComboState(consecutiveCorrect = 4)
        assertFalse(state.isAtMilestone)
    }

    @Test
    fun `combo of 6 is not a milestone`() {
        val state = ComboState(consecutiveCorrect = 6)
        assertFalse(state.isAtMilestone)
    }

    // ========== High Combo Detection ==========

    @Test
    fun `combo of 4 is not high combo`() {
        val state = ComboState(consecutiveCorrect = 4)
        assertFalse(state.isHighCombo)
    }

    @Test
    fun `combo of 5 is high combo`() {
        val state = ComboState(consecutiveCorrect = 5)
        assertTrue(state.isHighCombo)
    }

    @Test
    fun `combo of 10 is high combo`() {
        val state = ComboState(consecutiveCorrect = 10)
        assertTrue(state.isHighCombo)
    }

    // ========== Multiplier Calculation ==========

    @Test
    fun `combo of 0-2 has 1x multiplier`() {
        assertEquals(1.0f, ComboState(consecutiveCorrect = 0).multiplier)
        assertEquals(1.0f, ComboState(consecutiveCorrect = 1).multiplier)
        assertEquals(1.0f, ComboState(consecutiveCorrect = 2).multiplier)
    }

    @Test
    fun `combo of 3-4 has 12x multiplier`() {
        assertEquals(1.2f, ComboState(consecutiveCorrect = 3).multiplier)
        assertEquals(1.2f, ComboState(consecutiveCorrect = 4).multiplier)
    }

    @Test
    fun `combo of 5 or more has 15x multiplier`() {
        assertEquals(1.5f, ComboState(consecutiveCorrect = 5).multiplier)
        assertEquals(1.5f, ComboState(consecutiveCorrect = 10).multiplier)
        assertEquals(1.5f, ComboState(consecutiveCorrect = 100).multiplier)
    }

    // ========== Real-World Scenarios ==========

    @Test
    fun `scenario - building combo slowly and correctly`() {
        var state = ComboState()

        // First word: "cat" (3 letters), answered in 3 seconds
        state = state.afterCorrectAnswer(3000L, 3)
        assertEquals(1, state.consecutiveCorrect)
        assertEquals(1.0f, state.multiplier)

        // Second word: "run" (3 letters), answered in 2.8 seconds
        state = state.afterCorrectAnswer(2800L, 3)
        assertEquals(2, state.consecutiveCorrect)
        assertEquals(1.0f, state.multiplier)

        // Third word: "jump" (4 letters), answered in 3.5 seconds
        state = state.afterCorrectAnswer(3500L, 4)
        assertEquals(3, state.consecutiveCorrect)
        assertEquals(1.2f, state.multiplier)
    }

    @Test
    fun `scenario - fast answers don't build combo`() {
        var state = ComboState()

        // All answers are too fast (guessing)
        state = state.afterCorrectAnswer(1000L, 3) // Too fast for 3-letter
        assertEquals(0, state.consecutiveCorrect) // No increment

        state = state.afterCorrectAnswer(1200L, 4) // Too fast for 4-letter
        assertEquals(0, state.consecutiveCorrect) // Still no increment
    }

    @Test
    fun `scenario - mix of fast and slow answers`() {
        var state = ComboState()

        // First answer: adequate time
        state = state.afterCorrectAnswer(3000L, 4)
        assertEquals(1, state.consecutiveCorrect)

        // Second answer: too fast (guessing)
        state = state.afterCorrectAnswer(1500L, 5)
        assertEquals(1, state.consecutiveCorrect) // No increment

        // Third answer: adequate time
        state = state.afterCorrectAnswer(4000L, 5)
        assertEquals(2, state.consecutiveCorrect)
    }

    @Test
    fun `scenario - reaching fire streak (5 combo)`() {
        var state = ComboState()

        // Build up to 5 combo with adequate times
        repeat(5) { i ->
            state = state.afterCorrectAnswer(3000L + (i * 100), 4)
        }

        assertEquals(5, state.consecutiveCorrect)
        assertEquals(1.5f, state.multiplier)
        assertTrue(state.isHighCombo)
    }
}
