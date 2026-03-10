package com.wordland.domain.algorithm.basic

import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.algorithm.helper.StarRatingTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Basic scenario tests for StarRatingCalculator.
 * Tests standard scoring scenarios without edge cases or boundary conditions.
 */
class StarRatingBasicScenariosTest {
    @Test
    fun `Scenario 1 - Perfect performance earns 3 stars`() {
        // Given: All correct, no hints, fast time, no errors
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 24000L, // 4s per word (fast!)
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("Perfect performance should earn 3 stars", 3, stars)
    }

    @Test
    fun `Scenario 1b - 5 correct with 1 error earns 2 stars`() {
        // Given: Good performance with minor errors
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("5/6 with 1 error should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 2 - Good performance with hints earns 2 stars`() {
        // Given: Mostly correct with some hints
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 2,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("5/6 with hints should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 2b - 4 correct with no hints earns 2 stars`() {
        // Given: 4/6 correct without hints
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 0,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("4/6 correct should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 3 - Passing performance with errors earns 1 star`() {
        // Given: Barely passing
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 3,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("3/6 correct should earn 1 star", 1, stars)
    }

    @Test
    fun `Scenario 3b - Minimum 2 correct earns 1 star`() {
        // Given: Minimum passing
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 2,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 4,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Child-friendly minimum
        assertEquals("2/6 correct should earn 1 star (child-friendly)", 1, stars)
    }

    @Test
    fun `Scenario 4 - Failing performance earns 0 stars`() {
        // Given: No correct answers
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 0,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 6,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("0/6 correct should earn 0 stars", 0, stars)
    }

    @Test
    fun `Edge case - Single word level`() {
        // Given: Non-standard level size
        val data =
            StarRatingCalculator.PerformanceData(
                totalWords = 1,
                correctAnswers = 1,
                hintsUsed = 0,
                totalTimeMs = 5000L,
                wrongAnswers = 0,
                maxCombo = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Single correct = 3 stars
        assertEquals("1/1 correct should earn 3 stars", 3, stars)
    }

    @Test
    fun `Edge case - Zero total words`() {
        // Given: Empty level
        val data =
            StarRatingCalculator.PerformanceData(
                totalWords = 0,
                correctAnswers = 0,
                hintsUsed = 0,
                totalTimeMs = 0L,
                wrongAnswers = 0,
                maxCombo = 0,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then
        assertEquals("Empty level should earn 0 stars", 0, stars)
    }

    @Test
    fun `calculateMinCorrectForStars returns valid values`() {
        // Given: Standard 6-word level
        val totalWords = 6

        // When/Then: Verify minimum correct for each star level
        // 3 stars: need >= 5 correct (2.5+ threshold)
        val minFor3Stars = StarRatingCalculator.calculateMinCorrectForStars(3, totalWords)
        assertEquals("Min correct for 3 stars should be 5", 5, minFor3Stars)

        // 2 stars: need >= 3 correct (1.5+ threshold)
        val minFor2Stars = StarRatingCalculator.calculateMinCorrectForStars(2, totalWords)
        assertEquals("Min correct for 2 stars should be 3", 3, minFor2Stars)

        // 1 star: need >= 1 correct (child-friendly)
        val minFor1Star = StarRatingCalculator.calculateMinCorrectForStars(1, totalWords)
        assertEquals("Min correct for 1 star should be 1", 1, minFor1Star)
    }

    @Test
    fun `getScoringBreakdown returns valid breakdown string`() {
        // Given
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 1,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 3,
            )

        // When
        val breakdown = StarRatingCalculator.getScoringBreakdown(data)

        // Then: Should contain key information
        assertTrue("Breakdown should not be empty", breakdown.isNotEmpty())
        assertTrue("Breakdown should mention accuracy", breakdown.contains("Accuracy"))
        assertTrue("Breakdown should mention stars", breakdown.contains("stars"))
    }
}
