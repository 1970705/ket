package com.wordland.domain.algorithm

/**
 * Enhanced Spaced Repetition Algorithm based on SM-2 (SuperMemo-2)
 *
 * This algorithm improves upon the fixed-interval approach by:
 * 1. Tracking ease factor per word (individual difficulty)
 * 2. Using exponential interval scaling
 * 3. Adjusting based on quality of response
 * 4. Implementing priority-based review scheduling
 *
 * Reference: Piotr Woźniak, "Optimization of repetition spacing"
 */
object Sm2Algorithm {
    // Constants for SM-2 algorithm
    const val MIN_EASE_FACTOR = 1.3f
    const val MAX_EASE_FACTOR = 2.7f
    const val DEFAULT_EASE_FACTOR = 2.5f

    const val MIN_INTERVAL_DAYS = 1
    const val MAX_INTERVAL_DAYS = 30 // Child-friendly: cap at 30 days

    const val QUALITY_PERFECT = 5
    const val QUALITY_HESITANT = 4
    const val QUALITY_STRUGGLED = 3
    const val QUALITY_WRONG_KNEW = 2
    const val QUALITY_WRONG_DIDNT_KNOW = 1
    const val QUALITY_BLACKOUT = 0

    /**
     * Calculate quality score (0-5) based on response
     *
     * Quality scale:
     * 5 - Perfect response (< 70% of expected time)
     * 4 - Correct response with hesitation (< 150% of expected time)
     * 3 - Correct response but difficult (> 150% of expected time)
     * 2 - Incorrect but remembered after seeing answer
     * 1 - Incorrect, didn't remember but seemed familiar
     * 0 - Complete blackout
     *
     * @param isCorrect Whether the answer was correct
     * @param responseTimeMs Time taken to respond
     * @param hintsUsed Number of hints used
     * @param wordDifficulty Word difficulty (1-5)
     * @param isChild Whether to use child-friendly thresholds (more lenient)
     * @return Quality score (0-5)
     */
    fun calculateQuality(
        isCorrect: Boolean,
        responseTimeMs: Long,
        hintsUsed: Int,
        wordDifficulty: Int,
        isChild: Boolean = true,
    ): Int {
        // Expected time based on difficulty (child-friendly by default)
        val baseTime =
            when (wordDifficulty) {
                1 -> 2000L
                2 -> 3000L
                3 -> 4000L
                4 -> 5000L
                5 -> 6000L
                else -> 4000L
            }

        // Child-friendly: more lenient time thresholds
        val timeMultiplier = if (isChild) 1.5f else 1.0f
        val adjustedTime = responseTimeMs / timeMultiplier

        // Calculate base quality
        val baseQuality =
            when {
                !isCorrect -> {
                    // For incorrect answers, determine level of forgetting
                    when {
                        responseTimeMs > 15000 -> QUALITY_BLACKOUT // Gave up after long time
                        responseTimeMs > 8000 -> QUALITY_WRONG_DIDNT_KNOW // Took time, didn't know
                        else -> QUALITY_WRONG_KNEW // Quick but wrong - knew it but forgot
                    }
                }
                adjustedTime < baseTime * 0.7 -> QUALITY_PERFECT // Very fast, confident
                adjustedTime < baseTime * 1.5 -> QUALITY_HESITANT // Normal speed
                else -> QUALITY_STRUGGLED // Slow but correct
            }

        // Apply hint penalty (using hints reduces quality by 1 level, minimum 2)
        return if (hintsUsed > 0 && baseQuality >= QUALITY_STRUGGLED) {
            maxOf(QUALITY_WRONG_KNEW, baseQuality - hintsUsed)
        } else {
            baseQuality
        }
    }

    /**
     * Update ease factor based on quality score
     *
     * Formula: EF' = EF + (0.1 - (5 - q) × (0.08 + (5 - q) × 0.02))
     *
     * - q = 5: EF increases by 0.1 (word is getting easier)
     * - q = 4: EF increases by ~0.06
     * - q = 3: EF stays roughly the same
     * - q = 2: EF decreases by ~0.14
     * - q = 1: EF decreases by ~0.32
     * - q = 0: EF decreases by 0.54 (significant struggle)
     *
     * @param currentEaseFactor Current ease factor (1.3 - 2.7)
     * @param quality Quality score (0-5)
     * @return Updated ease factor, constrained to [MIN_EASE_FACTOR, MAX_EASE_FACTOR]
     */
    fun updateEaseFactor(
        currentEaseFactor: Float,
        quality: Int,
    ): Float {
        val q = quality.coerceIn(0, 5)
        val delta = 0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)
        val newEF = currentEaseFactor + delta
        return newEF.coerceIn(MIN_EASE_FACTOR, MAX_EASE_FACTOR)
    }

    /**
     * Calculate next review interval in days
     *
     * Rules:
     * - First review (n=0): review again same day (0 days)
     * - Second review (n=1): review in 1 day
     * - Subsequent reviews (n≥2): I = I_prev × EF
     *
     * @param repetitions Number of successful repetitions
     * @param previousInterval Previous interval in days
     * @param easeFactor Current ease factor
     * @return Next interval in days
     */
    fun calculateNextInterval(
        repetitions: Int,
        previousInterval: Int,
        easeFactor: Float,
    ): Int {
        val interval =
            when (repetitions) {
                0 -> 0 // First time - review same day
                1 -> MIN_INTERVAL_DAYS // Second review - 1 day
                else -> {
                    // Exponential growth: I = I_prev × EF
                    val days = (previousInterval * easeFactor).toInt()
                    maxOf(MIN_INTERVAL_DAYS, days)
                }
            }

        // Child-friendly: cap maximum interval
        return interval.coerceAtMost(MAX_INTERVAL_DAYS)
    }

    /**
     * Update repetition count based on quality
     *
     * - Quality ≥ 3: Successful recall, increment repetitions
     * - Quality < 3: Failed recall, reset to 0
     *
     * @param currentRepetitions Current repetition count
     * @param quality Quality score (0-5)
     * @return Updated repetition count
     */
    fun updateRepetitions(
        currentRepetitions: Int,
        quality: Int,
    ): Int {
        return if (quality >= QUALITY_STRUGGLED) {
            currentRepetitions + 1
        } else {
            0 // Reset on failure
        }
    }

    /**
     * Calculate memory strength (0-100) from SM-2 parameters
     *
     * Maps SM-2 parameters to the legacy memory strength scale:
     * - Higher ease factor → higher strength
     * - More repetitions → higher strength
     * - Longer intervals → higher strength
     *
     * @param easeFactor Current ease factor
     * @param repetitions Number of successful repetitions
     * @param intervalDays Current interval in days
     * @return Memory strength (0-100)
     */
    fun calculateMemoryStrength(
        easeFactor: Float,
        repetitions: Int,
        intervalDays: Int,
    ): Int {
        // Base strength from ease factor (1.3 → 20, 2.7 → 60)
        val efStrength = ((easeFactor - MIN_EASE_FACTOR) / (MAX_EASE_FACTOR - MIN_EASE_FACTOR)) * 40f + 20f

        // Bonus for repetitions (max 30 points)
        val repBonus = minOf(30f, repetitions * 5f)

        // Bonus for interval (max 10 points)
        val intervalBonus = minOf(10f, intervalDays / 3f)

        return (efStrength + repBonus + intervalBonus).toInt().coerceIn(0, 100)
    }

    /**
     * Calculate next review timestamp
     *
     * @param intervalDays Interval in days
     * @param baseTime Base timestamp (defaults to current time)
     * @return Next review timestamp in milliseconds
     */
    fun calculateNextReviewTime(
        intervalDays: Int,
        baseTime: Long = System.currentTimeMillis(),
    ): Long {
        val intervalMs = intervalDays * 24 * 60 * 60 * 1000L
        return baseTime + intervalMs
    }

    /**
     * Process a review and calculate all updated parameters
     *
     * This is the main entry point for the SM-2 algorithm.
     *
     * @param currentProgress Current user word progress
     * @param isCorrect Whether the answer was correct
     * @param responseTimeMs Time taken to respond
     * @param hintsUsed Number of hints used
     * @param wordDifficulty Word difficulty (1-5)
     * @param isChild Whether to use child-friendly thresholds
     * @return Sm2Result with all updated parameters
     */
    fun processReview(
        currentProgress: Sm2Progress,
        isCorrect: Boolean,
        responseTimeMs: Long,
        hintsUsed: Int,
        wordDifficulty: Int,
        isChild: Boolean = true,
    ): Sm2Result {
        // 1. Calculate quality score
        val quality = calculateQuality(isCorrect, responseTimeMs, hintsUsed, wordDifficulty, isChild)

        // 2. Update ease factor
        val newEaseFactor = updateEaseFactor(currentProgress.easeFactor, quality)

        // 3. Update repetitions
        val newRepetitions = updateRepetitions(currentProgress.repetitions, quality)

        // 4. Calculate next interval
        val newInterval = calculateNextInterval(newRepetitions, currentProgress.intervalDays, newEaseFactor)

        // 5. Calculate memory strength
        val newMemoryStrength = calculateMemoryStrength(newEaseFactor, newRepetitions, newInterval)

        // 6. Calculate next review time
        val nextReviewTime = calculateNextReviewTime(newInterval)

        // 7. Update review counts
        val newTotalReviews = currentProgress.totalReviews + 1
        val newSuccessfulReviews = currentProgress.successfulReviews + if (quality >= QUALITY_STRUGGLED) 1 else 0

        // 8. Update forgetting rate estimate
        val newForgettingRate =
            estimateForgettingRate(
                currentProgress.forgettingRate,
                quality,
                currentProgress.totalReviews,
            )

        return Sm2Result(
            quality = quality,
            newEaseFactor = newEaseFactor,
            newRepetitions = newRepetitions,
            newIntervalDays = newInterval,
            newMemoryStrength = newMemoryStrength,
            nextReviewTime = nextReviewTime,
            newTotalReviews = newTotalReviews,
            newSuccessfulReviews = newSuccessfulReviews,
            newForgettingRate = newForgettingRate,
        )
    }

    /**
     * Estimate individual forgetting rate
     *
     * Uses exponential moving average to track how quickly user forgets
     *
     * @param currentRate Current forgetting rate estimate (0-1)
     * @param quality Most recent quality score
     * @param reviewCount Number of reviews so far
     * @return Updated forgetting rate
     */
    private fun estimateForgettingRate(
        currentRate: Float,
        quality: Int,
        reviewCount: Int,
    ): Float {
        // Convert quality to "forgetting indicator" (0 = remembered, 1 = forgotten)
        val forgotIndicator =
            when {
                quality >= QUALITY_STRUGGLED -> 0.0f // Remembered
                quality == QUALITY_WRONG_KNEW -> 0.3f // Knew but forgot
                quality == QUALITY_WRONG_DIDNT_KNOW -> 0.6f // Didn't know
                else -> 1.0f // Complete blackout
            }

        // Adaptive learning rate (slower with more data)
        val alpha = 0.3f / (1 + reviewCount * 0.1f)

        // Exponential moving average
        val newRate = currentRate * (1 - alpha) + forgotIndicator * alpha

        return newRate.coerceIn(0f, 1f)
    }

    /**
     * Get human-readable quality description
     */
    fun getQualityDescription(quality: Int): String {
        return when (quality) {
            QUALITY_PERFECT -> "完美"
            QUALITY_HESITANT -> "正确"
            QUALITY_STRUGGLED -> "困难"
            QUALITY_WRONG_KNEW -> "知道但忘了"
            QUALITY_WRONG_DIDNT_KNOW -> "不知道"
            QUALITY_BLACKOUT -> "完全忘记"
            else -> "未知"
        }
    }
}

/**
 * Current SM-2 progress state
 */
data class Sm2Progress(
    val easeFactor: Float = Sm2Algorithm.DEFAULT_EASE_FACTOR,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val totalReviews: Int = 0,
    val successfulReviews: Int = 0,
    val forgettingRate: Float = 0.5f,
)

/**
 * Result of processing a review with SM-2 algorithm
 */
data class Sm2Result(
    val quality: Int, // Quality score (0-5)
    val newEaseFactor: Float, // Updated ease factor
    val newRepetitions: Int, // Updated repetition count
    val newIntervalDays: Int, // Next interval in days
    val newMemoryStrength: Int, // Memory strength (0-100)
    val nextReviewTime: Long, // Next review timestamp
    val newTotalReviews: Int, // Total review count
    val newSuccessfulReviews: Int, // Successful review count
    val newForgettingRate: Float, // Updated forgetting rate
) {
    /**
     * Check if this review was successful (quality ≥ 3)
     */
    val isSuccessful: Boolean
        get() = quality >= Sm2Algorithm.QUALITY_STRUGGLED

    /**
     * Get quality description
     */
    fun getQualityDescription(): String = Sm2Algorithm.getQualityDescription(quality)

    /**
     * Get days until next review
     */
    fun getDaysUntilReview(): Int {
        val now = System.currentTimeMillis()
        return ((nextReviewTime - now) / (24 * 60 * 60 * 1000f)).toInt().coerceAtLeast(0)
    }
}
