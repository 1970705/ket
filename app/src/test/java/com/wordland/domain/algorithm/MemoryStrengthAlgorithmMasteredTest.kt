package com.wordland.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the optimized guessing detection algorithm (Plan C - Hybrid)
 *
 * Tests the hybrid guessing detection where:
 * - Mastered words (strength >= 80 AND historical accuracy >= 90%) skip guessing penalty
 * - Other words use standard guessing detection
 */
class MemoryStrengthAlgorithmMasteredTest {
    // ==================== Mastered Word Tests ====================

    @Test
    fun `mastered word with fast answer is NOT penalized`() {
        // Word is mastered: strength >= 80 AND accuracy >= 90%
        val currentStrength = 85
        val isCorrect = true
        val isGuessing = true // Detected as guessing by pattern analysis
        val wordDifficulty = 3
        val historicalAccuracy = 0.92f // 92% - above 90% threshold

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = isCorrect,
                isGuessing = isGuessing,
                wordDifficulty = wordDifficulty,
                historicalAccuracy = historicalAccuracy,
            )

        // Should NOT penalize - mastered words get normal strength increase
        // Base change for strength 85 with correct answer = +5
        // Adjusted by difficulty 3.0 multiplier = +5
        assertEquals(90, newStrength)
    }

    @Test
    fun `mastered word with exact threshold values skips penalty`() {
        // Edge case: exactly at mastery threshold
        val currentStrength = 80 // Exactly 80
        val historicalAccuracy = 0.9f // Exactly 90%

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true, // Detected as guessing
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should NOT penalize - meets mastery criteria exactly
        // Base change for strength 80 with correct answer = +5
        assertEquals(85, newStrength)
    }

    @Test
    fun `mastered word with wrong answer still decreases strength`() {
        // Mastery only skips guessing penalty, not wrong answer penalty
        val currentStrength = 85
        val historicalAccuracy = 0.95f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = false,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should decrease - wrong answer penalty applies regardless of mastery
        // Base change for strength > 80 with wrong answer = -20
        assertEquals(65, newStrength)
    }

    @Test
    fun `mastered word strength capped at 100`() {
        val currentStrength = 98
        val historicalAccuracy = 0.95f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should cap at 100
        assertEquals(100, newStrength)
    }

    // ==================== Non-Mastered Word Tests ====================

    @Test
    fun `non-mastered word with high strength but low accuracy IS penalized for guessing`() {
        // High strength but low accuracy - NOT mastered
        val currentStrength = 85 // >= 80
        val historicalAccuracy = 0.75f // < 90% - not mastered

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should penalize - guessing applies when not mastered
        assertEquals(55, newStrength) // 85 - 30 penalty
    }

    @Test
    fun `non-mastered word with low strength but high accuracy IS penalized for guessing`() {
        // High accuracy but low strength - NOT mastered
        val currentStrength = 70 // < 80
        val historicalAccuracy = 0.95f // >= 90%

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should penalize - guessing applies when not mastered
        assertEquals(40, newStrength) // 70 - 30 penalty
    }

    @Test
    fun `non-mastered word with guessing penalty cannot go below zero`() {
        val currentStrength = 20
        val historicalAccuracy = 0.5f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should cap at 0
        assertEquals(0, newStrength)
    }

    @Test
    fun `non-mastered word without guessing gets normal increase`() {
        val currentStrength = 50
        val historicalAccuracy = 0.7f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false, // Not guessing
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should increase normally - base change for strength 50-60 = +15
        assertEquals(65, newStrength)
    }

    // ==================== Null Historical Accuracy Tests ====================

    @Test
    fun `new word with null historical accuracy IS penalized for guessing`() {
        // New word - no historical accuracy (null)
        val currentStrength = 30
        val historicalAccuracy = null // No history yet

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should penalize - null accuracy means NOT mastered
        assertEquals(0, newStrength) // 30 - 30 penalty
    }

    @Test
    fun `new word without guessing gets normal increase`() {
        val currentStrength = 30 // In 30-60 range, gets +15 base change
        val historicalAccuracy = null

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength 30-60 with correct answer = +15
        // Difficulty 3 = 1.0 multiplier
        assertEquals(45, newStrength)
    }

    // ==================== Difficulty Multiplier Tests ====================

    @Test
    fun `mastered word with easy difficulty gets reduced boost`() {
        val currentStrength = 85
        val historicalAccuracy = 0.92f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true, // Would be penalized, but mastery skips it
                wordDifficulty = 1, // Easy - 0.8 multiplier
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength 80+ with correct answer = +5
        // Adjusted by difficulty 0.8 multiplier = +4
        assertEquals(89, newStrength)
    }

    @Test
    fun `mastered word with hard difficulty gets increased boost`() {
        val currentStrength = 85
        val historicalAccuracy = 0.92f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true, // Would be penalized, but mastery skips it
                wordDifficulty = 5, // Hard - 1.2 multiplier
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength 80+ with correct answer = +5
        // Adjusted by difficulty 1.2 multiplier = +6
        assertEquals(91, newStrength)
    }

    // ==================== Wrong Answer Tests ====================

    @Test
    fun `mastered word wrong answer is not affected by guessing flag`() {
        // Wrong answers should ignore the guessing flag entirely
        val currentStrength = 85
        val historicalAccuracy = 0.92f

        // Wrong answer with guessing flag
        val newStrength1 =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = false,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Wrong answer without guessing flag
        val newStrength2 =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = false,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Both should be the same - guessing doesn't affect wrong answers
        assertEquals(newStrength2, newStrength1)
        // Base change for strength > 80 with wrong answer = -20
        assertEquals(65, newStrength1)
    }

    // ==================== Strength Range Tests ====================

    @Test
    fun `all strength levels with mastered status skip guessing penalty`() {
        val strengths = listOf(80, 85, 90, 95, 100)

        for (strength in strengths) {
            val newStrength =
                MemoryStrengthAlgorithm.calculateNewStrength(
                    currentStrength = strength,
                    isCorrect = true,
                    isGuessing = true,
                    wordDifficulty = 3,
                    historicalAccuracy = 0.92f,
                )

            // Should always increase, never penalized
            assertTrue("Strength $strength should increase or stay same", newStrength >= strength)
        }
    }

    @Test
    fun `low strength words get maximum increase for correct answer`() {
        val currentStrength = 10
        val historicalAccuracy = null

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength < 30 with correct answer = +20
        assertEquals(30, newStrength)
    }

    @Test
    fun `medium strength words get moderate increase`() {
        val currentStrength = 45
        val historicalAccuracy = 0.6f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength 30-60 with correct answer = +15
        assertEquals(60, newStrength)
    }

    @Test
    fun `high medium strength words get lower increase`() {
        val currentStrength = 70
        val historicalAccuracy = 0.8f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength 60-80 with correct answer = +10
        assertEquals(80, newStrength)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `zero strength with guessing stays at zero`() {
        val currentStrength = 0
        val historicalAccuracy = 0.0f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should stay at 0 (penalty can't go negative)
        assertEquals(0, newStrength)
    }

    @Test
    fun `zero strength without guessing increases`() {
        val currentStrength = 0
        val historicalAccuracy = 0.0f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = false,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Base change for strength < 30 = +20
        assertEquals(20, newStrength)
    }

    @Test
    fun `accuracy just below mastery threshold with high strength still gets penalized`() {
        // 89.9% accuracy - just below 90% threshold
        val currentStrength = 85
        val historicalAccuracy = 0.899f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should penalize - accuracy below 90%
        assertEquals(55, newStrength) // 85 - 30 penalty
    }

    @Test
    fun `strength just below mastery threshold with high accuracy still gets penalized`() {
        // 79 strength - just below 80 threshold
        val currentStrength = 79
        val historicalAccuracy = 0.95f

        val newStrength =
            MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = true,
                isGuessing = true,
                wordDifficulty = 3,
                historicalAccuracy = historicalAccuracy,
            )

        // Should penalize - strength below 80
        assertEquals(49, newStrength) // 79 - 30 penalty
    }

    // ==================== Wrong Answer Penalty Tests ====================

    @Test
    fun `wrong answer penalty scales with strength`() {
        val testCases =
            mapOf(
                100 to -20, // > 80: -20
                85 to -20, // > 80: -20
                70 to -15, // 60-80: -15
                50 to -10, // 30-60: -10
                20 to -5, // < 30: -5
            )

        for ((strength, expectedChange) in testCases) {
            val newStrength =
                MemoryStrengthAlgorithm.calculateNewStrength(
                    currentStrength = strength,
                    isCorrect = false,
                    isGuessing = false,
                    wordDifficulty = 3,
                    historicalAccuracy = null,
                )

            val actualChange = newStrength - strength
            assertEquals("Strength $strength", expectedChange.toLong(), actualChange.toLong())
        }
    }
}
