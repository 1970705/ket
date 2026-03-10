package com.wordland.domain.algorithm

/**
 * Dynamic Star Rating Algorithm
 *
 * Calculates star rating (1-3 stars) based on multiple performance factors.
 * This algorithm replaces the fixed 3-star rating with a dynamic system that
 * rewards good performance while being child-friendly.
 *
 * **Scoring Formula:**
 * ```
 * Base Score = Accuracy × 60% - Hint Penalty + Time Bonus - Error Penalty + Combo Bonus - Guessing Penalty
 * Stars = clamp(1 to 3, round(Base Score))
 * ```
 *
 * **Factors:**
 * 1. **Accuracy (60% weight)**: Correct answers / Total attempts
 *    - 100% accuracy = 1.8 points (60% of 3)
 *    - 80% accuracy = 1.44 points
 *    - 50% accuracy = 0.9 points
 *
 * 2. **Hint Penalty** (P0-BUG-011 fix: Re-enabled)
 *    - Per-word: Using hint caps that word at 2 stars instead of 3
 *    - Level-level: -0.15 per hint (capped at -0.6)
 *    - Dual-penalty prevents excessive hint usage
 *
 * 3. **Time Bonus**: +0.3 stars for fast completion
 *    - Fast = under 5 seconds per word average
 *    - No fast bonus when guessing is detected (P0-BUG-011 fix)
 *
 * 4. **Error Penalty**: -0.1 stars per error (capped at -0.3)
 *    - 1 error = -0.1 points
 *    - 2 errors = -0.2 points
 *    - 3+ errors = -0.3 points (capped)
 *
 * 5. **Combo Bonus**: Rewards consecutive correct answers
 *    - 5+ combo = +0.5 stars
 *    - 10+ combo = +1.0 stars
 *
 * 6. **Guessing Penalty**: -0.6 stars when guessing detected
 *    - Pattern-based detection (via GuessingDetector)
 *    - No time bonus when guessing flag is set
 *
 * **Star Thresholds:**
 * - 3 Stars: Score >= 2.5 (excellent performance)
 * - 2 Stars: Score >= 1.5 (good performance)
 * - 1 Star: Score >= 0.5 (passed minimum)
 *
 * **Child-Friendly Design:**
 * - Minimum 1 star if at least one word is correct
 * - Encourages improvement without harsh penalties
 * - Fast time bonus rewards quick thinkers
 * - But prevents gaming by detecting guessing (handled separately)
 *
 * @author Wordland Team
 * @since 1.2
 * @since 1.3 P0-BUG-011 fixes (hint penalty re-enabled, guessing bonus fix)
 */
object StarRatingCalculator {
    /**
     * Scoring weights and thresholds
     *
     * Formula: (correct / total) × MAX_STARS - hintPenalty + timeBonus - errorPenalty
     * - Accuracy gives base stars (0-3)
     * - Hint penalty reduces stars (capped at -0.5)
     * - Time bonus adds small boost (+0.3)
     * - Error penalty reduces stars (capped at -0.3)
     */
    private const val MAX_STARS = 3 // Maximum stars
    private const val MIN_STARS = 1 // Minimum stars (if passed)
    private const val STAR_THRESHOLD_3 = 2.5f // Score needed for 3 stars (FIXED P0-BUG-010)
    private const val STAR_THRESHOLD_2 = 1.5f // Score needed for 2 stars
    private const val STAR_THRESHOLD_1 = 0.5f // Score needed for 1 star

    // Adjusted threshold for anti-guessing: perfect score with guessing penalty = 2 stars
    // 6/6 correct = 3.0, guessing penalty = -0.6, result = 2.4 < 2.5 = 2 stars

    // Hint penalty: Re-enabled at level-level (P0-BUG-011 fix)
    // Dual-penalty system:
    // - Per-word: Max 2 stars per word if hint used (prevents gaming individual words)
    // - Level-level: Reduces total score based on total hints (prevents excessive hint usage)
    private const val HINT_PENALTY_PER_HINT = 0.15f // Penalty per hint (P0-BUG-011 fix)
    private const val MAX_HINT_PENALTY = 0.6f // Max hint penalty (P0-BUG-011 fix)

    private const val TIME_BONUS_FAST = 0.3f // Bonus for fast completion (+0.3 stars)
    private const val FAST_TIME_PER_WORD_MS = 5000L // 5 seconds per word = fast

    private const val ERROR_PENALTY_PER_ERROR = 0.1f // Penalty per error (P0-BUG-010 fix: was 0.25f)
    private const val MAX_ERROR_PENALTY = 0.3f // Max error penalty (3 errors, P0-BUG-010 fix: was 0.75f)

    // Combo bonus thresholds (P0-BUG-010 fix: restored to original design)
    private const val COMBO_BONUS_THRESHOLD_HIGH = 10 // 10+ combo gives +1 star
    private const val COMBO_BONUS_THRESHOLD_LOW = 5 // 5+ combo gives +0.5 star
    private const val COMBO_BONUS_HIGH = 1.0f // +1 star for 10+ combo
    private const val COMBO_BONUS_LOW = 0.5f // +0.5 star for 5+ combo

    // Time-based bonuses (no penalty - guessing handled by isGuessing flag)
    private const val MAX_TIME_PER_WORD_MS = 15000L // 15s maximum per word
    private const val SLOW_PENALTY = 0.2f // Penalty for being too slow
    private const val GUESSING_PENALTY = GuessingDetector.LEVEL_PENALTY_STARS // Penalty for guessing (-0.6 stars)

    /**
     * Performance data for star rating calculation
     *
     * **Unified Guessing Detection**:
     * The `isGuessing` flag should be set by calling `GuessingDetector.detectGuessing()`
     * with response patterns at the level level. This provides a single source of truth
     * for guessing detection, replacing the previous time-based threshold approach.
     *
     * @property totalWords Total number of words in the level
     * @property correctAnswers Number of correctly answered words
     * @property hintsUsed Total number of hints used
     * @property totalTimeMs Total time taken to complete the level (ms)
     * @property wrongAnswers Number of wrong answer attempts
     * @property maxCombo Maximum combo achieved during the level
     * @property isGuessing Whether guessing behavior was detected (computed by GuessingDetector)
     */
    data class PerformanceData(
        val totalWords: Int,
        val correctAnswers: Int,
        val hintsUsed: Int,
        val totalTimeMs: Long,
        val wrongAnswers: Int,
        val maxCombo: Int = 0,
        val isGuessing: Boolean = false,
    )

    /**
     * Calculate star rating based on performance data
     *
     * Algorithm factors:
     * 1. Accuracy (60% weight) - Primary factor
     * 2. Hint penalty - Reduces stars for using hints
     * 3. Time bonus/punishment - Rewards good pace
     * 4. Error penalty - Reduces stars for wrong answers
     * 5. Combo bonus - Rewards consecutive correct answers
     * 6. Guessing penalty - Applied if isGuessing flag is true (computed by GuessingDetector)
     *
     * @param data Performance data from the level
     * @return Star rating (1-3), or 0 if no correct answers
     */
    fun calculateStars(data: PerformanceData): Int {
        // Edge case: No correct answers = 0 stars
        if (data.correctAnswers == 0) return 0

        // 1. Calculate accuracy score (base stars from accuracy)
        val accuracyScore =
            calculateAccuracyScore(
                data.correctAnswers,
                data.totalWords,
            )

        // 2. Calculate hint penalty
        val hintPenalty = calculateHintPenalty(data.hintsUsed)

        // 3. Calculate time bonus (fast/slow)
        // P0-BUG-011 fix: No fast bonus when guessing is detected
        // P0-BUG-011 fix: No fast bonus when hints are used (hints indicate unfamiliarity)
        // Fast completion with guessing flag indicates random clicking, not skill
        // Fast completion with hints indicates using hints to rush, not mastery
        val timeBonus =
            if (data.isGuessing || data.hintsUsed > 0) {
                // When guessing or using hints, no time bonus
                // Fast completion with hints is suspicious - may indicate rushing through hints
                0f
            } else {
                calculateTimeBonusWithPenalty(
                    data.totalWords,
                    data.totalTimeMs,
                )
            }

        // 4. Calculate error penalty
        val errorPenalty = calculateErrorPenalty(data.wrongAnswers)

        // 5. Calculate combo bonus
        // P0-BUG-011 fix: No combo bonus when hints are used
        // Using hints indicates unfamiliarity with words, should not reward consecutive answers
        val comboBonus =
            if (data.hintsUsed > 0) {
                0f // No combo bonus when hints used
            } else {
                calculateComboBonus(data.maxCombo)
            }

        // 6. Calculate guessing penalty (from unified detector)
        val guessingPenalty = if (data.isGuessing) GUESSING_PENALTY else 0f

        // 7. Combine all factors
        val totalScore = accuracyScore - hintPenalty + timeBonus - errorPenalty + comboBonus - guessingPenalty

        // Debug: Log detailed breakdown
        android.util.Log.d(
            "StarRatingCalculator",
            "DEBUG: accuracy=$accuracyScore, hintPenalty=$hintPenalty, timeBonus=$timeBonus, " +
                "errorPenalty=$errorPenalty, comboBonus=$comboBonus, guessingPenalty=$guessingPenalty, " +
                "total=$totalScore",
        )

        // 8. Convert to star rating
        // Child-friendly: At least 1 star if any correct answer
        val stars =
            when {
                totalScore >= STAR_THRESHOLD_3 -> MAX_STARS
                totalScore >= STAR_THRESHOLD_2 -> 2
                totalScore > 0 || data.correctAnswers > 0 -> MIN_STARS // Any correct = at least 1 star
                else -> 0
            }

        // Log for debugging
        android.util.Log.d(
            "StarRatingCalculator",
            "calculateStars: correct=${data.correctAnswers}/${data.totalWords}, " +
                "hints=${data.hintsUsed}, errors=${data.wrongAnswers}, " +
                "combo=${data.maxCombo}, time=${data.totalTimeMs}ms, " +
                "guessing=${data.isGuessing}, " +
                "score=${"%.2f".format(java.util.Locale.US, totalScore)} → $stars stars",
        )

        return stars
    }

    /**
     * Calculate base accuracy score in stars
     *
     * Formula: (correct / total) × MAX_STARS
     *
     * Examples:
     * - 6/6 correct = 3.0 points (base)
     * - 5/6 correct = 2.5 points (base)
     * - 4/6 correct = 2.0 points (base)
     * - 3/6 correct = 1.5 points (base)
     * - 2/6 correct = 1.0 point (base)
     * - 1/6 correct = 0.5 points (base)
     */
    private fun calculateAccuracyScore(
        correct: Int,
        total: Int,
    ): Float {
        if (total == 0) return 0f
        val accuracy = correct.toFloat() / total.toFloat()
        return accuracy * MAX_STARS
    }

    /**
     * Calculate hint penalty
     *
     * FIXED P0-BUG-011: Re-enabled level-level hint penalty.
     *
     * The per-word penalty (in SubmitAnswerUseCase) caps each word at 2 stars if hint used.
     * However, the level-level penalty provides ADDITIONAL disincentive for excessive hints.
     *
     * Dual-penalty system:
     * - Per-word: Max 2 stars per word if hint used (prevents gaming individual words)
     * - Level-level: Reduces total score based on total hints (prevents excessive hint usage)
     *
     * Formula: min(hintsUsed × 0.15, 0.6)
     * - 0 hints = 0.0 points (no penalty)
     * - 2 hints = -0.3 points
     * - 4 hints = -0.6 points (capped)
     * - 6 hints (all words) = -0.6 points (capped)
     */
    private fun calculateHintPenalty(hintsUsed: Int): Float {
        if (hintsUsed == 0) return 0f
        // P0-BUG-011 fix: Re-enabled to prevent excessive hint usage
        return minOf(hintsUsed * HINT_PENALTY_PER_HINT, MAX_HINT_PENALTY)
    }

    /**
     * Calculate time bonus (no penalty - guessing handled separately)
     *
     * Awards bonus for good pacing:
     * - Fast (< 5s/word): +0.3 points (bonus)
     * - Normal (5-15s/word): 0 points
     * - Slow (> 15s/word): -0.2 points (penalty)
     *
     * **Note**: Guessing penalty is applied via the `isGuessing` flag in
     * PerformanceData, which is computed by GuessingDetector.detectGuessing().
     * This provides a single source of truth for guessing detection.
     */
    private fun calculateTimeBonusWithPenalty(
        totalWords: Int,
        totalTimeMs: Long,
    ): Float {
        if (totalWords == 0) return 0f

        val avgTimePerWord = totalTimeMs.toFloat() / totalWords.toFloat()
        return when {
            // Fast completion: rewards quick thinkers
            avgTimePerWord < FAST_TIME_PER_WORD_MS -> TIME_BONUS_FAST
            // Normal pace: no bonus or penalty
            avgTimePerWord <= MAX_TIME_PER_WORD_MS -> 0f
            // Too slow: small penalty (keeps engagement)
            else -> -SLOW_PENALTY
        }
    }

    /**
     * Calculate combo bonus for consecutive correct answers
     *
     * Based on anti-cheating principles: only rewards combos with
     * adequate thinking time (handled by time penalty above)
     *
     * - 10+ combo: +1 star
     * - 5+ combo: +0.5 star
     * - < 5 combo: 0
     */
    private fun calculateComboBonus(maxCombo: Int): Float {
        return when {
            maxCombo >= COMBO_BONUS_THRESHOLD_HIGH -> COMBO_BONUS_HIGH
            maxCombo >= COMBO_BONUS_THRESHOLD_LOW -> COMBO_BONUS_LOW
            else -> 0f
        }
    }

    /**
     * Calculate error penalty
     *
     * Each wrong answer costs points, but capped to prevent excessive penalty
     * - 1 error = -0.15 points
     * - 2 errors = -0.3 points
     * - 3+ errors = -0.45 points (capped)
     */
    private fun calculateErrorPenalty(wrongAnswers: Int): Float {
        return minOf(
            wrongAnswers * ERROR_PENALTY_PER_ERROR,
            MAX_ERROR_PENALTY,
        )
    }

    /**
     * Calculate minimum words needed for a target star rating
     *
     * Assumes no hints, normal time, no errors
     *
     * @param targetStars Target star rating (1-3)
     * @param totalWords Total words in level
     * @return Minimum correct answers needed
     */
    fun calculateMinCorrectForStars(
        targetStars: Int,
        totalWords: Int,
    ): Int {
        val targetScore =
            when (targetStars) {
                3 -> STAR_THRESHOLD_3
                2 -> STAR_THRESHOLD_2
                1 -> STAR_THRESHOLD_1
                else -> return 0
            }

        // Reverse the accuracy formula: score = (correct / total) × 3
        // correct = (score / 3) × total
        val minCorrect = ((targetScore / MAX_STARS) * totalWords).toInt()

        // At least 1 correct for any stars
        return maxOf(minCorrect, 1)
    }

    /**
     * Get a breakdown of the scoring for debugging/display
     *
     * @param data Performance data
     * @return String describing how the score was calculated
     */
    fun getScoringBreakdown(data: PerformanceData): String {
        val accuracyScore = calculateAccuracyScore(data.correctAnswers, data.totalWords)
        val hintPenalty = calculateHintPenalty(data.hintsUsed)
        val timeBonus = calculateTimeBonusWithPenalty(data.totalWords, data.totalTimeMs)
        val errorPenalty = calculateErrorPenalty(data.wrongAnswers)
        val comboBonus = calculateComboBonus(data.maxCombo)
        val guessingPenalty = if (data.isGuessing) GUESSING_PENALTY else 0f
        val totalScore = accuracyScore - hintPenalty + timeBonus - errorPenalty + comboBonus - guessingPenalty

        val avgTimePerWord = if (data.totalWords > 0) data.totalTimeMs / data.totalWords.toFloat() else 0f

        return """
            |
            |Star Rating Breakdown:
            |======================
            |Accuracy: ${data.correctAnswers}/${data.totalWords} = ${"%.1f".format(
            java.util.Locale.US,
            data.correctAnswers.toFloat() / data.totalWords * 100,
        )}% → +${"%.2f".format(java.util.Locale.US, accuracyScore)} points
            |Hints Used: ${data.hintsUsed} → -${"%.2f".format(java.util.Locale.US, hintPenalty)} points
            |Time: ${data.totalTimeMs / 1000}s (${"%.1f".format(java.util.Locale.US, avgTimePerWord)}s/word) → ${"%.2f".format(
            java.util.Locale.US,
            timeBonus,
        )} points
            |Errors: ${data.wrongAnswers} → -${"%.2f".format(java.util.Locale.US, errorPenalty)} points
            |Max Combo: ${data.maxCombo} → +${"%.2f".format(java.util.Locale.US, comboBonus)} points
            |Guessing Detected: ${data.isGuessing} → -${"%.2f".format(java.util.Locale.US, guessingPenalty)} points
            |--------------------------------
            |Total Score: ${"%.2f".format(java.util.Locale.US, totalScore)} → ${calculateStars(data)} stars
            |======================
            """.trimMargin()
    }
}
