package com.wordland.domain.algorithm.bugs

import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.algorithm.helper.StarRatingTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bug fix validation tests for StarRatingCalculator.
 * Tests specific bug fixes and regression prevention.
 */
class StarRatingBugsTest {
    @Test
    fun `Unified - perfect score with guessing flag earns 2 stars`() {
        // Given: Perfect accuracy BUT with guessing flag set by GuessingDetector
        val dataWithGuessing =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 36000L, // 6s per word (normal, no fast bonus)
                isGuessing = true,
            )
        val dataWithoutGuessing =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 36000L,
                isGuessing = false,
            )

        // When
        val starsWithGuessing = StarRatingCalculator.calculateStars(dataWithGuessing)
        val starsWithoutGuessing = StarRatingCalculator.calculateStars(dataWithoutGuessing)

        // Then: Guessing penalty applies -0.6 points
        assertEquals("Perfect with guessing flag should earn 2 stars", 2, starsWithGuessing)
        assertEquals("Perfect without guessing flag should earn 3 stars", 3, starsWithoutGuessing)
    }

    @Test
    fun `Unified - 5 correct with guessing flag earns 2 stars`() {
        // Given: 5/6 correct with guessing flag
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = 25000L,
                wrongAnswers = 1,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 5/6 = 2.5 - 0.1 (error) - 0.6 (guessing) = 1.8 → 2 stars
        assertEquals("5/6 with guessing should earn 2 stars", 2, stars)
    }

    @Test
    fun `Unified - 4 correct with guessing flag earns 1 star`() {
        // Given: 4/6 correct with guessing flag
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 30000L,
                wrongAnswers = 2,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 4/6 = 2.0 - 0.2 (errors) - 0.6 (guessing) = 1.2 → 1 star
        assertEquals("4/6 with guessing should earn 1 star", 1, stars)
    }

    @Test
    fun `Unified - guessing penalty does not reduce to 0 stars if any correct`() {
        // Given: Only 1 correct with guessing flag
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 1,
                totalTimeMs = 30000L,
                wrongAnswers = 5,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Child-friendly - at least 1 star if any correct
        assertEquals("1 correct with guessing should still earn 1 star", 1, stars)
    }

    @Test
    fun `Unified - combo bonus can overcome guessing penalty`() {
        // Given: Fast completion with high combo BUT guessing flag set
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 20000L, // Fast time bonus
                maxCombo = 10,
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3.0 + 0.3 (fast) + 1.0 (combo) - 0.6 (guessing) = 3.7 → 3 stars
        assertEquals("Perfect with combo can overcome guessing penalty", 3, stars)
    }

    @Test
    fun `Unified - time bonus no longer detects guessing`() {
        // Given: Very fast completion WITHOUT guessing flag
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 6000L, // 1s per word (previously would trigger guessing)
                isGuessing = false,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Fast time bonus applies, no guessing penalty
        assertEquals("Fast time without guessing flag should get fast bonus", 3, stars)
    }

    @Test
    fun `Unified - getScoringBreakdown includes guessing detection`() {
        // Given
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 24000L,
                isGuessing = true,
            )

        // When
        val breakdown = StarRatingCalculator.getScoringBreakdown(data)

        // Then: Should mention guessing detection
        assertTrue("Breakdown should mention guessing", breakdown.contains("Guessing Detected"))
        assertTrue("Breakdown should show guessing penalty", breakdown.contains("-0.60"))
    }

    @Test
    fun `Boundary - isGuessing flag applies penalty regardless of time`() {
        // Given: Normal time BUT guessing flag set
        val dataWithFlag =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 30000L, // Exactly 5s per word (normal)
                isGuessing = true,
            )
        val dataWithoutFlag =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 30000L,
                isGuessing = false,
            )

        // When
        val starsWithFlag = StarRatingCalculator.calculateStars(dataWithFlag)
        val starsWithoutFlag = StarRatingCalculator.calculateStars(dataWithoutFlag)

        // Then: isGuessing flag determines penalty, not time
        assertEquals("With guessing flag at normal time should get 2 stars", 2, starsWithFlag)
        assertEquals("Without guessing flag at normal time should get 3 stars", 3, starsWithoutFlag)
    }

    // ========== P0-BUG-011 DIAGNOSTIC TESTS ==========

    @Test
    fun `BUG-011 Scenario 2 - All words with hints earns 2 stars not 3`() {
        // Given: Every word used a hint (6 hints for 6 words)
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                hintsUsed = 6,
                totalTimeMs = 36000L,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Should earn 2 stars, NOT 3 stars
        assertEquals("All words with hints should earn 2 stars (P0-BUG-011)", 2, stars)
    }

    @Test
    fun `BUG-011 Scenario 3 - 4 correct with moderate errors earns 2 stars not 1`() {
        // Given: 4/6 correct with moderate errors
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 36000L,
                wrongAnswers = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Should earn 2 stars, NOT 1 star
        assertEquals("4/6 correct with 2 errors should earn 2 stars (P0-BUG-011)", 2, stars)
    }

    @Test
    fun `BUG-011 Scenario 4 - Fast perfect completion with guessing earns 1 star`() {
        // Given: Fast perfect completion BUT with guessing flag
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 6,
                totalTimeMs = 4800L, // 800ms per word (very fast, indicates guessing)
                isGuessing = true,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Should earn 1 or 2 stars, NOT 3 stars
        assertTrue("Fast perfect with guessing should NOT earn 3 stars", stars != 3)
        assertTrue("Fast perfect with guessing should earn 1 or 2 stars", stars == 1 || stars == 2)
    }
}
