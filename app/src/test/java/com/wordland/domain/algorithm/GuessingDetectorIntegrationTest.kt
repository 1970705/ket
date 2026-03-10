package com.wordland.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration test to verify unified guessing detection strategy
 *
 * Verifies that all guessing detection implementations use consistent
 * thresholds from GuessingDetector.
 */
class GuessingDetectorIntegrationTest {
    @Test
    fun `GuessingDetector constants are defined and non-zero`() {
        // Verify all unified constants exist and have reasonable values
        assertTrue(
            "LEVEL_PENALTY_THRESHOLD_MS should be positive",
            GuessingDetector.LEVEL_PENALTY_THRESHOLD_MS > 0,
        )
        assertTrue(
            "WORD_DETECTION_THRESHOLD_MS should be positive",
            GuessingDetector.WORD_DETECTION_THRESHOLD_MS > 0,
        )
        assertTrue(
            "RAPID_INCORRECT_THRESHOLD_MS should be positive",
            GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS > 0,
        )
        assertTrue(
            "LEVEL_PENALTY_STARS should be positive",
            GuessingDetector.LEVEL_PENALTY_STARS > 0,
        )
    }

    @Test
    fun `Level penalty threshold is 1500ms`() {
        assertEquals(
            "Level penalty threshold should be 1500ms",
            1500L,
            GuessingDetector.LEVEL_PENALTY_THRESHOLD_MS,
        )
    }

    @Test
    fun `Word detection threshold is 2000ms`() {
        assertEquals(
            "Word detection threshold should be 2000ms",
            2000L,
            GuessingDetector.WORD_DETECTION_THRESHOLD_MS,
        )
    }

    @Test
    fun `Rapid incorrect threshold is 1500ms`() {
        assertEquals(
            "Rapid incorrect threshold should be 1500ms",
            1500L,
            GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS,
        )
    }

    @Test
    fun `Level penalty stars is 0_6`() {
        assertEquals(
            "Level penalty should be 0.6 stars",
            0.6f,
            GuessingDetector.LEVEL_PENALTY_STARS,
            0.001f,
        )
    }

    @Test
    fun `StarRatingCalculator uses unified constants`() {
        // Verify StarRatingCalculator produces expected results with unified thresholds

        // Test 1: Guessing penalty (avg < 1500ms)
        val guessingData =
            StarRatingCalculator.PerformanceData(
                totalWords = 6,
                correctAnswers = 6,
                hintsUsed = 0,
                totalTimeMs = 6000L, // 1000ms per word (guessing!)
                wrongAnswers = 0,
                maxCombo = 0,
            )

        val guessingStars = StarRatingCalculator.calculateStars(guessingData)
        assertEquals(
            "Guessing should result in 2 stars (penalty applied)",
            2,
            guessingStars,
        )

        // Test 2: Normal time (avg > 1500ms)
        val normalData =
            StarRatingCalculator.PerformanceData(
                totalWords = 6,
                correctAnswers = 6,
                hintsUsed = 0,
                totalTimeMs = 24000L, // 4000ms per word (normal)
                wrongAnswers = 0,
                maxCombo = 0,
            )

        val normalStars = StarRatingCalculator.calculateStars(normalData)
        assertEquals(
            "Normal time should result in 3 stars (no penalty)",
            3,
            normalStars,
        )

        // Test 3: Boundary (exactly 1500ms per word)
        val boundaryData =
            StarRatingCalculator.PerformanceData(
                totalWords = 6,
                correctAnswers = 6,
                hintsUsed = 0,
                totalTimeMs = 9000L, // Exactly 1500ms per word
                wrongAnswers = 0,
                maxCombo = 0,
            )

        val boundaryStars = StarRatingCalculator.calculateStars(boundaryData)
        assertEquals(
            "Exactly at threshold (1500ms) should get fast bonus, not penalty",
            3,
            boundaryStars,
        )
    }

    @Test
    fun `GuessingDetector detectGuessing uses WORD_DETECTION_THRESHOLD_MS`() {
        // Create response patterns at exactly WORD_DETECTION_THRESHOLD_MS
        val patternsAtThreshold =
            listOf(
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis(),
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS, // Exactly 2000ms
                    isCorrect = true,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 1000,
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS, // Exactly 2000ms
                    isCorrect = true,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 2000,
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS, // Exactly 2000ms
                    isCorrect = true,
                    hintUsed = false,
                ),
            )

        // At threshold (2000ms) should NOT trigger detection (uses < not <=)
        val resultAtThreshold = GuessingDetector.detectGuessing(patternsAtThreshold)
        assertEquals(
            "Exactly at WORD_DETECTION_THRESHOLD_MS should not trigger guessing",
            false,
            resultAtThreshold,
        )

        // Just below threshold should trigger detection
        val patternsBelowThreshold =
            listOf(
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis(),
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS - 1, // 1999ms
                    isCorrect = true,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 1000,
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS - 1, // 1999ms
                    isCorrect = true,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 2000,
                    responseTime = GuessingDetector.WORD_DETECTION_THRESHOLD_MS - 1, // 1999ms
                    isCorrect = true,
                    hintUsed = false,
                ),
            )

        val resultBelowThreshold = GuessingDetector.detectGuessing(patternsBelowThreshold)
        assertEquals(
            "Just below WORD_DETECTION_THRESHOLD_MS should trigger guessing",
            true,
            resultBelowThreshold,
        )
    }

    @Test
    fun `Rapid incorrect detection uses RAPID_INCORRECT_THRESHOLD_MS`() {
        // Create patterns with rapid incorrect answers at exactly RAPID_INCORRECT_THRESHOLD_MS
        val patternsAtThreshold =
            listOf(
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis(),
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS, // Exactly 1500ms
                    isCorrect = false,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 1000,
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS, // Exactly 1500ms
                    isCorrect = false,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 2000,
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS, // Exactly 1500ms
                    isCorrect = true, // Mix in one correct to avoid random pattern detection
                    hintUsed = false,
                ),
            )

        // At threshold should NOT trigger rapid incorrect (uses < not <=)
        val resultAtThreshold = GuessingDetector.detectGuessing(patternsAtThreshold)
        assertEquals(
            "Exactly at RAPID_INCORRECT_THRESHOLD_MS should not trigger rapid incorrect detection",
            false,
            resultAtThreshold,
        )

        // Just below threshold should trigger
        val patternsBelowThreshold =
            listOf(
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis(),
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS - 1, // 1499ms
                    isCorrect = false,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 1000,
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS - 1, // 1499ms
                    isCorrect = false,
                    hintUsed = false,
                ),
                GuessingDetector.ResponsePattern(
                    timestamp = System.currentTimeMillis() + 2000,
                    responseTime = GuessingDetector.RAPID_INCORRECT_THRESHOLD_MS - 1, // 1499ms
                    isCorrect = false,
                    hintUsed = false,
                ),
            )

        val resultBelowThreshold = GuessingDetector.detectGuessing(patternsBelowThreshold)
        assertEquals(
            "Just below RAPID_INCORRECT_THRESHOLD_MS should trigger rapid incorrect detection",
            true,
            resultBelowThreshold,
        )
    }
}
