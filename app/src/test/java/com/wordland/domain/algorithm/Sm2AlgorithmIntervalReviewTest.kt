package com.wordland.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SM-2 Algorithm interval and review processing
 *
 * Tests full review processing, integration scenarios, quality descriptions,
 * and next review time calculations.
 */
class Sm2AlgorithmIntervalReviewTest {
    // ==================== Full Review Processing Tests ====================

    @Test
    fun `processReview returns successful result for correct answer`() {
        val progress =
            Sm2Progress(
                easeFactor = 2.5f,
                repetitions = 1,
                intervalDays = 1,
                totalReviews = 1,
                successfulReviews = 1,
                forgettingRate = 0.5f,
            )

        val result =
            Sm2Algorithm.processReview(
                currentProgress = progress,
                isCorrect = true,
                responseTimeMs = 3000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertTrue(result.isSuccessful)
        assertTrue(result.newRepetitions > progress.repetitions)
        assertTrue(result.newSuccessfulReviews > progress.successfulReviews)
    }

    @Test
    fun `processReview resets repetitions for incorrect answer`() {
        val progress =
            Sm2Progress(
                easeFactor = 2.5f,
                repetitions = 5,
                intervalDays = 10,
                totalReviews = 5,
                successfulReviews = 5,
                forgettingRate = 0.3f,
            )

        val result =
            Sm2Algorithm.processReview(
                currentProgress = progress,
                isCorrect = false,
                responseTimeMs = 5000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertFalse(result.isSuccessful)
        assertEquals(0, result.newRepetitions)
        assertTrue(result.newIntervalDays < progress.intervalDays)
    }

    @Test
    fun `processReview increases ease factor for perfect answer`() {
        val progress = Sm2Progress(easeFactor = 2.5f)

        val result =
            Sm2Algorithm.processReview(
                currentProgress = progress,
                isCorrect = true,
                responseTimeMs = 1000L, // Fast
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertTrue(result.newEaseFactor > progress.easeFactor)
    }

    @Test
    fun `processReview decreases ease factor for wrong answer`() {
        val progress = Sm2Progress(easeFactor = 2.5f)

        val result =
            Sm2Algorithm.processReview(
                currentProgress = progress,
                isCorrect = false,
                responseTimeMs = 10000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertTrue(result.newEaseFactor < progress.easeFactor)
    }

    @Test
    fun `processReview tracks review counts correctly`() {
        val progress =
            Sm2Progress(
                totalReviews = 10,
                successfulReviews = 8,
            )

        val result =
            Sm2Algorithm.processReview(
                currentProgress = progress,
                isCorrect = true,
                responseTimeMs = 3000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertEquals(11, result.newTotalReviews)
        assertEquals(9, result.newSuccessfulReviews)
    }

    // ==================== Integration Tests ====================

    @Test
    fun `full learning cycle from new to mastered`() {
        // Simulate a learning cycle: 10 successful reviews
        var progress = Sm2Progress()

        repeat(10) { i ->
            val result =
                Sm2Algorithm.processReview(
                    currentProgress = progress,
                    isCorrect = true,
                    responseTimeMs = 2000L,
                    hintsUsed = 0,
                    wordDifficulty = 3,
                    isChild = true,
                )

            progress =
                Sm2Progress(
                    easeFactor = result.newEaseFactor,
                    repetitions = result.newRepetitions,
                    intervalDays = result.newIntervalDays,
                    totalReviews = result.newTotalReviews,
                    successfulReviews = result.newSuccessfulReviews,
                    forgettingRate = result.newForgettingRate,
                )

            println(
                "Review ${i + 1}: EF=${result.newEaseFactor}, " +
                    "reps=${result.newRepetitions}, " +
                    "interval=${result.newIntervalDays}d, " +
                    "strength=${result.newMemoryStrength}",
            )
        }

        // After 10 successful reviews:
        // - Ease factor should have increased
        assertTrue(progress.easeFactor > 2.5f)
        // - Repetitions should be 10
        assertEquals(10, progress.repetitions)
        // - Interval should be significant (> 10 days)
        assertTrue(progress.intervalDays > 10)
        // - Forgetting rate should be low (good retention)
        assertTrue(progress.forgettingRate < 0.5f)
    }

    @Test
    fun `struggling word has lower ease factor and shorter intervals`() {
        // Simulate a struggling learner (mixed success)
        var progress = Sm2Progress()
        val correctResponses = booleanArrayOf(true, false, true, false, true)

        correctResponses.forEach { isCorrect ->
            val result =
                Sm2Algorithm.processReview(
                    currentProgress = progress,
                    isCorrect = isCorrect,
                    responseTimeMs = if (isCorrect) 5000L else 10000L,
                    hintsUsed = 0,
                    wordDifficulty = 3,
                    isChild = true,
                )

            progress =
                Sm2Progress(
                    easeFactor = result.newEaseFactor,
                    repetitions = result.newRepetitions,
                    intervalDays = result.newIntervalDays,
                    totalReviews = result.newTotalReviews,
                    successfulReviews = result.newSuccessfulReviews,
                    forgettingRate = result.newForgettingRate,
                )
        }

        // After struggling:
        // - Ease factor should be lower
        assertTrue(progress.easeFactor < 2.5f)
        // - Repetitions should be low (resets on failure)
        assertTrue(progress.repetitions < 3)
        // - Interval should be short
        assertTrue(progress.intervalDays <= 2)
    }

    // ==================== Quality Description Tests ====================

    @Test
    fun `getQualityDescription returns correct Chinese labels`() {
        assertEquals("完美", Sm2Algorithm.getQualityDescription(5))
        assertEquals("正确", Sm2Algorithm.getQualityDescription(4))
        assertEquals("困难", Sm2Algorithm.getQualityDescription(3))
        assertEquals("知道但忘了", Sm2Algorithm.getQualityDescription(2))
        assertEquals("不知道", Sm2Algorithm.getQualityDescription(1))
        assertEquals("完全忘记", Sm2Algorithm.getQualityDescription(0))
    }

    // ==================== Next Review Time Tests ====================

    @Test
    fun `calculateNextReviewTime returns correct timestamp`() {
        val baseTime = 1000000L
        val reviewTime = Sm2Algorithm.calculateNextReviewTime(intervalDays = 7, baseTime = baseTime)

        val expectedTime = baseTime + (7 * 24 * 60 * 60 * 1000L)
        assertEquals(expectedTime, reviewTime)
    }

    @Test
    fun `calculateNextReviewTime defaults to current time`() {
        val before = System.currentTimeMillis()
        val reviewTime = Sm2Algorithm.calculateNextReviewTime(intervalDays = 0)
        val after = System.currentTimeMillis()

        assertTrue(reviewTime >= before)
        assertTrue(reviewTime <= after)
    }
}
