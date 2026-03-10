package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Feedback type for word completion based on performance
 *
 * This determines the visual feedback animation and intensity.
 * Part of P0 Phase 1: Visual Feedback Enhancements
 *
 * Design Philosophy: 70% learning + 30% entertainment balance
 * - Feedback enhances the experience without changing learning mechanics
 * - Performance-aware to maintain 60fps on target devices
 */
@Immutable
sealed class FeedbackType {
    /**
     * Perfect answer - 3 stars performance
     * Animation: Simplified confetti/celebration
     * Trigger: First attempt correct, no hints, reasonable response time
     */
    @Immutable
    data object PerfectThreeStar : FeedbackType()

    /**
     * Good answer - 2 stars performance
     * Animation: Flash/shimmer effect
     * Trigger: Correct with 1-2 attempts or with hint
     */
    @Immutable
    data object GoodTwoStar : FeedbackType()

    /**
     * Correct answer - 1 star performance
     * Animation: Simple checkmark
     * Trigger: Correct after multiple attempts or with multiple hints
     */
    @Immutable
    data object CorrectOneStar : FeedbackType()

    /**
     * Incorrect answer
     * Animation: Shake effect
     * Trigger: Wrong answer
     */
    @Immutable
    data object Incorrect : FeedbackType()

    /**
     * Get the star rating associated with this feedback type
     */
    val stars: Int
        get() =
            when (this) {
                is PerfectThreeStar -> 3
                is GoodTwoStar -> 2
                is CorrectOneStar -> 1
                is Incorrect -> 0
            }

    /**
     * Get the display message for this feedback type
     */
    fun getMessage(isCorrect: Boolean): String =
        when (this) {
            is PerfectThreeStar -> "完美！"
            is GoodTwoStar -> "很好！"
            is CorrectOneStar -> "正确！"
            is Incorrect -> "再试一次！"
        }
}

/**
 * Determine feedback type based on answer performance
 *
 * @param isCorrect Whether the answer was correct
 * @param attempts Number of attempts taken (1 = first try)
 * @param hintsUsed Number of hints used
 * @param responseTimeMs Time taken to answer in milliseconds
 * @param wordLength Length of the target word (for guessing detection)
 * @return Appropriate FeedbackType
 */
fun determineFeedbackType(
    isCorrect: Boolean,
    attempts: Int = 1,
    hintsUsed: Int = 0,
    responseTimeMs: Long = 0L,
    wordLength: Int = 0,
): FeedbackType {
    if (!isCorrect) {
        return FeedbackType.Incorrect
    }

    // Anti-guessing check: if answered too quickly, it might be a guess
    val minThinkTime = 1000L + wordLength * 500L
    val isLikelyGuessing = responseTimeMs < minThinkTime && wordLength > 0

    // If detected as guessing, downgrade to CorrectOneStar
    if (isLikelyGuessing) {
        return FeedbackType.CorrectOneStar
    }

    return when {
        // Perfect: First attempt, no hints
        attempts == 1 && hintsUsed == 0 -> FeedbackType.PerfectThreeStar

        // Good: 1-2 attempts with 0-1 hints, or 1 attempt with 2 hints
        (attempts <= 2 && hintsUsed <= 1) || (attempts == 1 && hintsUsed == 2) -> FeedbackType.GoodTwoStar

        // Correct: Everything else
        else -> FeedbackType.CorrectOneStar
    }
}
