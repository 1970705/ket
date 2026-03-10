package com.wordland.domain.algorithm.helper

import com.wordland.domain.algorithm.StarRatingCalculator

/**
 * Test helper class providing shared test data and utilities for StarRatingCalculator tests.
 * Reduces code duplication across split test files.
 */
object StarRatingTestHelper {
    // Standard level configuration
    const val STANDARD_WORDS = 6
    const val STANDARD_TIME_MS = 30000L // 30 seconds for 6 words = 5s each

    /**
     * Creates a standard PerformanceData for testing.
     */
    fun createStandardPerformanceData(
        totalWords: Int = STANDARD_WORDS,
        correctAnswers: Int = STANDARD_WORDS,
        hintsUsed: Int = 0,
        totalTimeMs: Long = STANDARD_TIME_MS,
        wrongAnswers: Int = 0,
        maxCombo: Int = 0,
        isGuessing: Boolean = false,
    ) = StarRatingCalculator.PerformanceData(
        totalWords = totalWords,
        correctAnswers = correctAnswers,
        hintsUsed = hintsUsed,
        totalTimeMs = totalTimeMs,
        wrongAnswers = wrongAnswers,
        maxCombo = maxCombo,
        isGuessing = isGuessing,
    )

    /**
     * Creates perfect performance data.
     */
    fun createPerfectData(
        totalWords: Int = STANDARD_WORDS,
        totalTimeMs: Long = STANDARD_TIME_MS,
    ) = createStandardPerformanceData(
        totalWords = totalWords,
        correctAnswers = totalWords,
        hintsUsed = 0,
        totalTimeMs = totalTimeMs,
        wrongAnswers = 0,
        maxCombo = totalWords,
        isGuessing = false,
    )

    /**
     * Creates performance data with all words using hints.
     */
    fun createAllHintsData(totalWords: Int = STANDARD_WORDS) =
        createStandardPerformanceData(
            totalWords = totalWords,
            correctAnswers = totalWords,
            hintsUsed = totalWords,
            totalTimeMs = STANDARD_TIME_MS,
            wrongAnswers = 0,
            maxCombo = 0,
            isGuessing = false,
        )
}
