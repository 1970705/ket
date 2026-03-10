package com.wordland.domain.algorithm.combo

import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.algorithm.helper.StarRatingTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Combo system tests for StarRatingCalculator.
 * Tests combo bonus mechanics and threshold behaviors.
 */
class StarRatingComboTest {
    @Test
    fun `Scenario 6a - High combo (10+) earns bonus star`() {
        // Given: Good performance with very high combo
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 0,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 10,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 + 1.0 combo = 2.8 → 3 stars!
        assertEquals("4/6 with 10+ combo should earn 3 stars", 3, stars)
    }

    @Test
    fun `Scenario 6b - Medium combo (5-9) earns partial bonus`() {
        // Given: Good performance with medium combo
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 0,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 5,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 + 0.5 combo = 2.3 → 2 stars
        assertEquals("4/6 with 5 combo should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 6c - Low combo under 5 earns no bonus`() {
        // Given: Performance with low combo
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 0,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 4,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 = 1.8 → 2 stars (no combo bonus)
        assertEquals("4/6 with <5 combo should earn 2 stars (no bonus)", 2, stars)
    }

    @Test
    fun `Boundary - combo threshold exactly at 5 earns bonus`() {
        // Given: Combo exactly at threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 3,
                maxCombo = 5,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3/6 = 1.5 + 0.5 combo = 2.0 → 2 stars
        assertEquals("Combo of 5 should earn bonus", 2, stars)
    }

    @Test
    fun `Boundary - combo just below 5 earns no bonus`() {
        // Given: Combo just below threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 4,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 + 0 combo = 1.8 → 2 stars
        assertEquals("Combo of 4 should not earn bonus", 2, stars)
    }

    @Test
    fun `Boundary - combo exactly at 10 earns high bonus`() {
        // Given: Combo exactly at high threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 10,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 + 1.0 combo = 2.8 → 3 stars!
        assertEquals("Combo of 10 should earn high bonus", 3, stars)
    }

    @Test
    fun `Boundary - combo just below 10 earns low bonus`() {
        // Given: Combo just below high threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 9,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 + 0.5 combo = 2.3 → 2 stars
        assertEquals("Combo of 9 should earn low bonus only", 2, stars)
    }

    @Test
    fun `Combined - 4 correct with combo and fast bonus earns 3 stars`() {
        // Given: 4/6 correct but with bonuses
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 20000L, // Fast
                wrongAnswers = 2,
                maxCombo = 10,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 2.0 + 0.3 + 1.0 - 0.2 = 3.1 → 3 stars
        assertEquals("4/6 with fast and combo should earn 3 stars", 3, stars)
    }

    @Test
    fun `Unified - combo bonus can overcome guessing penalty`() {
        // Given: Guessing flag set but with high combo
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 24000L, // Normal time
                maxCombo = 10,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3.0 - 0.6 + 1.0 = 3.4 → 3 stars (combo helps!)
        assertEquals("Guessing with high combo can still earn 3 stars", 3, stars)
    }

    @Test
    fun `Scenario 7c - Perfect with high combo overcomes guessing penalty`() {
        // Given: Perfect completion with high combo BUT guessing flag set
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 24000L, // Normal time
                maxCombo = 10,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 6/6 = 3.0 - 0.6 guessing penalty + 1.0 combo bonus = 3.4 → 3 stars
        assertEquals("Perfect with high combo overcomes guessing penalty", 3, stars)
    }

    @Test
    fun `getScoringBreakdown includes combo info`() {
        // Given
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 1,
                totalTimeMs = 24000L,
                wrongAnswers = 1,
                maxCombo = 7,
            )

        // When
        val breakdown = StarRatingCalculator.getScoringBreakdown(data)

        // Then: Should contain combo information
        assertTrue("Breakdown should mention combo", breakdown.contains("Combo"))
    }
}
