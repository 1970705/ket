package com.wordland.domain.algorithm.boundary

import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.algorithm.helper.StarRatingTestHelper
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Boundary and threshold tests for StarRatingCalculator.
 * Tests exact threshold values and boundary conditions.
 */
class StarRatingBoundaryTest {
    @Test
    fun `Boundary - Exactly 2_5 score earns 3 stars`() {
        // Given: Score exactly at 3-star threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = 30000L,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 5/6 = 2.5 → 3 stars (>= threshold)
        assertEquals("Exactly 2.5 score should earn 3 stars", 3, stars)
    }

    @Test
    fun `Boundary - Just below 2_5 score earns 2 stars`() {
        // Given: Score just below 3-star threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = 30000L,
                wrongAnswers = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 2.5 - 0.1 = 2.4 → 2 stars (< threshold)
        assertEquals("2.4 score should earn 2 stars", 2, stars)
    }

    @Test
    fun `Boundary - Exactly 1_5 score earns 2 stars`() {
        // Given: Score exactly at 2-star threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = 30000L,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 3/6 = 1.5 → 2 stars (>= threshold)
        assertEquals("Exactly 1.5 score should earn 2 stars", 2, stars)
    }

    @Test
    fun `Boundary - Just below 1_5 score earns 1 star`() {
        // Given: Score just below 2-star threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = 100000L, // Very slow
                wrongAnswers = 3,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: 1.5 - 0.2 = 1.3 → 1 star (< threshold)
        assertEquals("1.3 score should earn 1 star", 1, stars)
    }

    @Test
    fun `Boundary - fast time threshold exactly at 5s per word`() {
        // Given: Time exactly at fast threshold (5s/word)
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = 30000L, // Exactly 5s per word
                wrongAnswers = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: At fast threshold boundary (< 5s is fast)
        assertEquals("Exactly 5s per word should not get fast bonus", 2, stars)
    }

    @Test
    fun `Boundary - just below fast time threshold gets bonus`() {
        // Given: Time just below fast threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                totalTimeMs = 29999L, // 4.9998s per word
                wrongAnswers = 1,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Just below threshold gets fast bonus
        assertEquals("Just below 5s per word should get fast bonus", 3, stars)
    }

    @Test
    fun `Boundary - slow time threshold exactly at 15s per word`() {
        // Given: Time exactly at slow threshold (15s/word)
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 90000L, // Exactly 15s per word
                wrongAnswers = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: At slow threshold (<= 15s means no penalty)
        assertEquals("Exactly 15s per word should not get slow penalty", 2, stars)
    }

    @Test
    fun `Boundary - just above slow time threshold gets penalty`() {
        // Given: Time just above slow threshold
        val data =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 4,
                totalTimeMs = 90001L, // 15.00017s per word
                wrongAnswers = 2,
            )

        // When
        val stars = StarRatingCalculator.calculateStars(data)

        // Then: Just above threshold gets slow penalty
        assertEquals("Just above 15s per word should get slow penalty", 2, stars)
    }

    @Test
    fun `Boundary - hint penalty disabled at level-level`() {
        // Given: Varying hints (penalty disabled at level-level)
        val data1 =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 2,
                totalTimeMs = 30000L,
                wrongAnswers = 1,
            )
        val data2 =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 5,
                hintsUsed = 3,
                totalTimeMs = 30000L,
                wrongAnswers = 1,
            )

        // When
        val stars1 = StarRatingCalculator.calculateStars(data1)
        val stars2 = StarRatingCalculator.calculateStars(data2)

        // Then: Both should be same (hint penalty disabled at level-level)
        assertEquals("2 hints should earn 2 stars", 2, stars1)
        assertEquals("3 hints should also earn 2 stars (penalty disabled)", 2, stars2)
    }

    @Test
    fun `Boundary - error penalty cap at 3 errors`() {
        // Given: 3 errors (at cap) and 4 errors (above cap)
        val data1 =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 3,
                totalTimeMs = 30000L,
                wrongAnswers = 3,
            )
        val data2 =
            StarRatingTestHelper.createStandardPerformanceData(
                correctAnswers = 2,
                totalTimeMs = 30000L,
                wrongAnswers = 4,
            )

        // When
        val stars1 = StarRatingCalculator.calculateStars(data1)
        val stars2 = StarRatingCalculator.calculateStars(data2)

        // Then: Error penalty capped
        assertEquals("3 errors should earn 1 star", 1, stars1)
        assertEquals("4 errors (capped) should earn 1 star", 1, stars2)
    }
}
