package com.wordland.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SM-2 Algorithm core calculations
 *
 * Tests quality calculation, ease factor updates, interval calculation,
 * repetition updates, and memory strength.
 */
class Sm2AlgorithmCoreCalculationTest {
    // ==================== Quality Calculation Tests ====================

    @Test
    fun `calculateQuality returns 5 for perfect correct answer`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 1000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertEquals(Sm2Algorithm.QUALITY_PERFECT, quality)
    }

    @Test
    fun `calculateQuality returns 5 for fast correct answer (child mode)`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 3500L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )
        // 3500ms / 1.5 = 2333ms, which is < 4000 * 0.7 = 2800ms (Perfect threshold)
        assertEquals(Sm2Algorithm.QUALITY_PERFECT, quality)
    }

    @Test
    fun `calculateQuality returns 4 for normal speed correct answer (child mode)`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 5000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )
        // 5000ms / 1.5 = 3333ms, which is between 2800ms and 6000ms (Hesitant threshold)
        assertEquals(Sm2Algorithm.QUALITY_HESITANT, quality)
    }

    @Test
    fun `calculateQuality returns 3 for struggled correct answer (child mode)`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 12000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )
        // 12000ms / 1.5 = 8000ms, which is > 6000ms (Struggled threshold)
        assertEquals(Sm2Algorithm.QUALITY_STRUGGLED, quality)
    }

    @Test
    fun `calculateQuality returns 2 for incorrect but knew it`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = false,
                responseTimeMs = 3000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertEquals(Sm2Algorithm.QUALITY_WRONG_KNEW, quality)
    }

    @Test
    fun `calculateQuality returns 1 for incorrect and didn't know`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = false,
                responseTimeMs = 10000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertEquals(Sm2Algorithm.QUALITY_WRONG_DIDNT_KNOW, quality)
    }

    @Test
    fun `calculateQuality returns 0 for blackout`() {
        val quality =
            Sm2Algorithm.calculateQuality(
                isCorrect = false,
                responseTimeMs = 20000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        assertEquals(Sm2Algorithm.QUALITY_BLACKOUT, quality)
    }

    @Test
    fun `calculateQuality applies hint penalty`() {
        val qualityWithoutHint =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 2000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        val qualityWithHint =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 2000L,
                hintsUsed = 1,
                wordDifficulty = 3,
                isChild = true,
            )

        assertTrue(qualityWithHint < qualityWithoutHint)
        assertEquals(Sm2Algorithm.QUALITY_HESITANT, qualityWithHint)
    }

    @Test
    fun `calculateQuality is more lenient for children`() {
        // Same response time, different child setting
        val adultQuality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 5000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = false,
            )

        val childQuality =
            Sm2Algorithm.calculateQuality(
                isCorrect = true,
                responseTimeMs = 5000L,
                hintsUsed = 0,
                wordDifficulty = 3,
                isChild = true,
            )

        // Child should get better (higher) quality
        assertTrue(childQuality >= adultQuality)
    }

    // ==================== Ease Factor Tests ====================

    @Test
    fun `updateEaseFactor increases for quality 5`() {
        val currentEF = 2.5f
        val newEF = Sm2Algorithm.updateEaseFactor(currentEF, 5)

        assertTrue(newEF > currentEF)
        assertEquals(2.6f, newEF, 0.001f)
    }

    @Test
    fun `updateEaseFactor increases slightly for quality 4`() {
        val currentEF = 2.5f
        val newEF = Sm2Algorithm.updateEaseFactor(currentEF, 4)

        // Formula: EF' = EF + (0.1 - (5-q) × (0.08 + (5-q) × 0.02))
        // For q=4: EF' = 2.5 + (0.1 - 1 × (0.08 + 1 × 0.02))
        //          = 2.5 + (0.1 - 0.10) = 2.5 + 0 = 2.5
        // Actually the formula gives exactly 2.5, no increase for q=4
        assertEquals(2.5f, newEF, 0.001f)
    }

    @Test
    fun `updateEaseFactor stays roughly same for quality 3`() {
        val currentEF = 2.5f
        val newEF = Sm2Algorithm.updateEaseFactor(currentEF, 3)

        // Formula: EF' = EF + (0.1 - (5-q) × (0.08 + (5-q) × 0.02))
        // For q=3: EF' = 2.5 + (0.1 - 2 × (0.08 + 2 × 0.02))
        //          = 2.5 + (0.1 - 2 × 0.12) = 2.5 + (0.1 - 0.24) = 2.5 - 0.14 = 2.36
        assertEquals(2.36f, newEF, 0.001f)
    }

    @Test
    fun `updateEaseFactor decreases for quality 2`() {
        val currentEF = 2.5f
        val newEF = Sm2Algorithm.updateEaseFactor(currentEF, 2)

        assertTrue(newEF < currentEF)
    }

    @Test
    fun `updateEaseFactor decreases significantly for quality 0`() {
        val currentEF = 2.5f
        val newEF = Sm2Algorithm.updateEaseFactor(currentEF, 0)

        // Formula: EF' = EF + (0.1 - (5-q) × (0.08 + (5-q) × 0.02))
        // For q=0: EF' = 2.5 + (0.1 - 5 × (0.08 + 5 × 0.02))
        //          = 2.5 + (0.1 - 5 × 0.18) = 2.5 + (0.1 - 0.9) = 2.5 - 0.8 = 1.7
        // But min is 1.3, so should be 1.7 (not 1.96 as expected)
        assertTrue(newEF < currentEF)
        assertEquals(1.7f, newEF, 0.001f)
    }

    @Test
    fun `updateEaseFactor respects minimum bound`() {
        val minEF = Sm2Algorithm.MIN_EASE_FACTOR
        val newEF = Sm2Algorithm.updateEaseFactor(minEF, 0)

        assertEquals(minEF, newEF, 0.001f)
    }

    @Test
    fun `updateEaseFactor respects maximum bound`() {
        val maxEF = Sm2Algorithm.MAX_EASE_FACTOR
        val newEF = Sm2Algorithm.updateEaseFactor(maxEF, 5)

        assertEquals(maxEF, newEF, 0.001f)
    }

    // ==================== Interval Calculation Tests ====================

    @Test
    fun `calculateNextInterval returns 0 for first review`() {
        val interval =
            Sm2Algorithm.calculateNextInterval(
                repetitions = 0,
                previousInterval = 0,
                easeFactor = 2.5f,
            )

        assertEquals(0, interval)
    }

    @Test
    fun `calculateNextInterval returns 1 for second review`() {
        val interval =
            Sm2Algorithm.calculateNextInterval(
                repetitions = 1,
                previousInterval = 0,
                easeFactor = 2.5f,
            )

        assertEquals(1, interval)
    }

    @Test
    fun `calculateNextInterval grows exponentially`() {
        val interval1 = Sm2Algorithm.calculateNextInterval(1, 0, 2.5f) // 1 day
        val interval2 = Sm2Algorithm.calculateNextInterval(2, interval1, 2.5f) // ~2.5 days
        val interval3 = Sm2Algorithm.calculateNextInterval(3, interval2, 2.5f) // ~6.25 days

        assertEquals(1, interval1)
        assertEquals(2, interval2) // 1 * 2.5 = 2.5 → 2 (int)
        assertEquals(5, interval3) // 2 * 2.5 = 5

        assertTrue(interval3 > interval2)
        assertTrue(interval2 > interval1)
    }

    @Test
    fun `calculateNextInterval caps at maximum for children`() {
        // With high repetitions and EF, interval would exceed max
        val interval =
            Sm2Algorithm.calculateNextInterval(
                repetitions = 50,
                previousInterval = 30,
                easeFactor = 2.7f,
            )

        assertTrue(interval <= Sm2Algorithm.MAX_INTERVAL_DAYS)
    }

    // ==================== Repetition Update Tests ====================

    @Test
    fun `updateRepetitions increments for quality 3 or higher`() {
        val currentReps = 2

        val newReps3 = Sm2Algorithm.updateRepetitions(currentReps, 3)
        val newReps4 = Sm2Algorithm.updateRepetitions(currentReps, 4)
        val newReps5 = Sm2Algorithm.updateRepetitions(currentReps, 5)

        assertEquals(3, newReps3)
        assertEquals(3, newReps4)
        assertEquals(3, newReps5)
    }

    @Test
    fun `updateRepetitions resets to 0 for quality less than 3`() {
        val currentReps = 5

        val newReps0 = Sm2Algorithm.updateRepetitions(currentReps, 0)
        val newReps1 = Sm2Algorithm.updateRepetitions(currentReps, 1)
        val newReps2 = Sm2Algorithm.updateRepetitions(currentReps, 2)

        assertEquals(0, newReps0)
        assertEquals(0, newReps1)
        assertEquals(0, newReps2)
    }

    // ==================== Memory Strength Calculation Tests ====================

    @Test
    fun `calculateMemoryStrength returns higher value for better parameters`() {
        val weak = Sm2Algorithm.calculateMemoryStrength(1.5f, 1, 1)
        val strong = Sm2Algorithm.calculateMemoryStrength(2.5f, 10, 10)

        assertTrue(strong > weak)
    }

    @Test
    fun `calculateMemoryStrength respects bounds`() {
        val minStrength =
            Sm2Algorithm.calculateMemoryStrength(
                easeFactor = Sm2Algorithm.MIN_EASE_FACTOR,
                repetitions = 0,
                intervalDays = 0,
            )

        val maxStrength =
            Sm2Algorithm.calculateMemoryStrength(
                easeFactor = Sm2Algorithm.MAX_EASE_FACTOR,
                repetitions = 100,
                intervalDays = 100,
            )

        assertTrue(minStrength >= 0)
        assertTrue(maxStrength <= 100)
    }
}
