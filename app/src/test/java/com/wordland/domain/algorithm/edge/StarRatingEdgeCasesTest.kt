package com.wordland.domain.algorithm.edge

import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.algorithm.helper.StarRatingTestHelper
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Edge case tests for StarRatingCalculator.
 * Tests non-standard level sizes, unusual scenarios, and combined effects.
 */
class StarRatingEdgeCasesTest {
    @Test
    fun `Scenario 5a - Fast completion with 4 correct earns 2 stars`() {
        // Given: Fast time bonus helps reach 2 stars
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 18000L, // 3s per word (fast!)
                wrongAnswers = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 + 0.3 - 0.2 = 2.1 → 2 stars
        assertEquals("4/6 fast should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 5b - Hint penalty disabled at level-level`() {
        // Given: Multiple hints used
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 3,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Hint penalty disabled at level-level
        assertEquals("5/6 with 3 hints should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 5c - Error penalty capped at 3 errors`() {
        // Given: Multiple wrong answers
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 3,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Error penalty capped at -0.3
        assertEquals("3/6 with 3 errors should earn 1 star", 1, stars)
    }

    @Test
    fun `Scenario 5d - 5 correct with 0 errors is borderline for 3 stars`() {
        // Given: Good accuracy, no errors
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 5/6 = 2.5 → exactly at 3-star threshold
        assertEquals("5/6 correct with no errors should earn 3 stars at boundary", 3, stars)
    }

    @Test
    fun `Scenario 5e - Boundary condition for 1 to 2 stars`() {
        // Given: Right at the 1-2 star boundary
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 0,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3/6 = 1.5 → exactly at 2-star threshold
        assertEquals("3/6 exactly at 1-2 star boundary should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 5f - 1 correct with 1 error earns 1 star`() {
        // Given: Minimal correct answers
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 1,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 5,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Child-friendly minimum
        assertEquals("1/6 correct should earn 1 star (child-friendly)", 1, stars)
    }

    @Test
    fun `Scenario 7b - Slow completion applies small penalty`() {
        // Given: 4/6 correct but very slow (>15s per word)
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 100000L, // ~16.7s per word (very slow)
                wrongAnswers = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 slow penalty = 1.8 → 2 stars (still passes)
        assertEquals("4/6 very slow should earn 2 stars (small penalty)", 2, stars)
    }

    @Test
    fun `Scenario 8 - 4 correct with hints and combo earns 2 stars`() {
        // Given: Balanced performance with both penalties and bonuses
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                hintsUsed = 2,
                totalTimeMs = StarRatingTestHelper.STANDARD_TIME_MS,
                wrongAnswers = 2,
                maxCombo = 5,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.5 hint + 0.5 combo = 2.0 → 2 stars
        assertEquals("4/6 with hints and 5 combo should earn 2 stars", 2, stars)
    }

    @Test
    fun `Scenario 8b - Worst case scenario still gets 1 star`() {
        // Given: Multiple penalties but at least some correct
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 2,
                hintsUsed = 5,
                totalTimeMs = 8000L,
                wrongAnswers = 4,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Child-friendly minimum 1 star
        assertEquals("Worst case with 2 correct should earn 1 star", 1, stars)
    }

    @Test
    fun `Combined - perfect with all bonuses still capped at 3 stars`() {
        // Given: Best possible performance
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 20000L, // Fast (3.33s per word)
                maxCombo = 15,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Score would be 3 + 0.3 + 1.0 = 4.3, but capped at 3
        assertEquals("Perfect with all bonuses still capped at 3 stars", 3, stars)
    }

    @Test
    fun `Combined - guessing penalty applies but combo can overcome it`() {
        // Given: Guessing flag set but with high combo
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 24000L,
                maxCombo = 10,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3.0 - 0.6 + 1.0 = 3.4 → 3 stars (combo helps!)
        assertEquals("Guessing with high combo can still earn 3 stars", 3, stars)
    }

    @Test
    fun `Combined - all penalties still get 1 star (child-friendly)`() {
        // Given: Worst performance with some correct
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 1,
                hintsUsed = 5,
                totalTimeMs = 6000L,
                wrongAnswers = 5,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Child-friendly - at least 1 star if any correct
        assertEquals("Worst case with 1 correct should earn 1 star", 1, stars)
    }

    @Test
    fun `Combined - 5 correct with all penalties earns 1 star`() {
        // Given: Good accuracy but all penalties
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 5,
                totalTimeMs = 24000L,
                wrongAnswers = 1,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 2.5 - 0.6 - 0.0 - 0.1 - 0.6 = 1.2 → 1 star
        assertEquals("5/6 with all penalties should earn 1 star", 1, stars)
    }

    @Test
    fun `Combined - borderline 3 star with hint penalty`() {
        // Given: Performance that would be 3 stars without hints
        // Note: P0-BUG-011 fix changed behavior - using hints disables time bonus and combo bonus
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 2,
                totalTimeMs = 20000L,
                maxCombo = 10,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: With P0-BUG-011 fix, using hints disables time and combo bonuses
        // Calculation: 2.5 - 0.3 (hint) + 0.0 (no fast bonus with hints) + 0.0 (no combo with hints) = 2.2 → 2 stars
        assertEquals("5/6 with hints (no fast/combo bonuses per P0-BUG-011) should earn 2 stars", 2, stars)
    }

    @Test
    fun `Edge case - 10 word level calculations`() {
        // Given: Non-standard level size
        val data =
            StarRatingCalculator.PerformanceData(
                totalWords = 10,
                correctAnswers = 8,
                hintsUsed = 0,
                totalTimeMs = 40000L, // 4s per word
                wrongAnswers = 2,
                maxCombo = 8,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 8/10 = 2.4 - 0.2 + 0.5 = 2.7 → 3 stars
        assertEquals("8/10 correct should earn 3 stars", 3, stars)
    }

    @Test
    fun `Edge case - 3 word level (mini level)`() {
        // Given: Small level size
        val data =
            StarRatingCalculator.PerformanceData(
                totalWords = 3,
                correctAnswers = 2,
                hintsUsed = 0,
                totalTimeMs = 10000L,
                wrongAnswers = 1,
                maxCombo = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 2/3 = 2.0 - 0.1 = 1.9 → 2 stars
        assertEquals("2/3 correct should earn 2 stars", 2, stars)
    }

    @Test
    fun `Edge case - 1 correct in 2 word level earns 2 stars`() {
        // Given: Tiny level
        val data =
            StarRatingCalculator.PerformanceData(
                totalWords = 2,
                correctAnswers = 1,
                hintsUsed = 0,
                totalTimeMs = 8000L,
                wrongAnswers = 0,
                maxCombo = 0,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 1/2 = 1.5 → 2 stars (exactly at threshold)
        assertEquals("1/2 correct should earn 2 stars", 2, stars)
    }
}
