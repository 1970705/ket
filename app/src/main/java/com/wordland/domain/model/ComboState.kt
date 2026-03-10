package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Combo state for learning gamification
 * Tracks consecutive correct answers with anti-guessing logic
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 *
 * Design principles:
 * - Rewards accuracy and adequate thinking time
 * - Does NOT reward speed (prevents guessing)
 * - Resets on incorrect answer
 * - Multiplier increases at milestones (3, 5 combos)
 *
 * NOTE: The currentMultiplier parameter uses a simple default value (1.0f)
 * and the actual value is computed in init block. This avoids calling
 * companion object functions during default parameter initialization,
 * which causes JaCoCo instrumentation issues in tests.
 */
@Immutable
data class ComboState(
    /**
     * Number of consecutive correct answers with adequate thinking time
     * Fast answers (guessing) do not increment this counter
     */
    val consecutiveCorrect: Int = 0,
    /**
     * Average response time for current combo streak (in milliseconds)
     * Used for analytics and anti-guessing validation
     */
    val averageResponseTime: Long = 0L,
    /**
     * Current score multiplier based on combo count
     * - 0-2 combo: 1.0x (no multiplier)
     * - 3-4 combo: 1.2x (20% bonus)
     * - 5+ combo: 1.5x (50% bonus)
     *
     * Use 1.0f to auto-calculate from consecutiveCorrect, or pass explicit value.
     * The init block will recalculate if 1.0f is passed with count >= 3.
     */
    val currentMultiplier: Float = 1.0f,
) {
    companion object {
        /**
         * Threshold for 3-combo multiplier
         */
        const val COMBO_THRESHOLD_3X = 3

        /**
         * Threshold for 5-combo multiplier
         */
        const val COMBO_THRESHOLD_5X = 5

        /**
         * Multiplier for 3-4 combo
         */
        const val MULTIPLIER_3X = 1.2f

        /**
         * Multiplier for 5+ combo
         */
        const val MULTIPLIER_5X = 1.5f

        /**
         * Base multiplier (no combo)
         */
        const val MULTIPLIER_BASE = 1.0f

        /**
         * Minimum thinking time base (ms)
         * Added to word-length-based time for anti-guessing
         */
        const val MIN_THINK_TIME_BASE_MS = 1000L

        /**
         * Additional time per letter (ms)
         * Prevents guessing on longer words
         */
        const val MILLIS_PER_LETTER = 500L

        /**
         * Calculate multiplier based on combo count
         */
        fun calculateMultiplierForCount(count: Int): Float {
            return when {
                count >= COMBO_THRESHOLD_5X -> MULTIPLIER_5X
                count >= COMBO_THRESHOLD_3X -> MULTIPLIER_3X
                else -> MULTIPLIER_BASE
            }
        }
    }

    /**
     * Effective multiplier value.
     *
     * This is computed from currentMultiplier parameter:
     * - If currentMultiplier is not 1.0f (the default), use that value
     * - If currentMultiplier is 1.0f and consecutiveCorrect >= 3, recalculate
     * - Otherwise use currentMultiplier (which is 1.0f)
     *
     * This approach avoids calling companion object functions during
     * default parameter initialization while maintaining the correct behavior.
     */
    val effectiveMultiplier: Float
        get() =
            if (currentMultiplier == 1.0f && consecutiveCorrect >= 3) {
                when {
                    consecutiveCorrect >= COMBO_THRESHOLD_5X -> MULTIPLIER_5X
                    consecutiveCorrect >= COMBO_THRESHOLD_3X -> MULTIPLIER_3X
                    else -> MULTIPLIER_BASE
                }
            } else {
                currentMultiplier
            }

    /**
     * Alias for effectiveMultiplier - for backward compatibility with tests
     */
    val multiplier: Float
        get() = effectiveMultiplier

    /**
     * Check if combo is at a milestone level (3 or 5)
     * Used for triggering animations and effects
     */
    val isAtMilestone: Boolean
        get() = consecutiveCorrect in listOf(COMBO_THRESHOLD_3X, COMBO_THRESHOLD_5X)

    /**
     * Check if combo is at high level (5+) for fire effects
     */
    val isHighCombo: Boolean
        get() = consecutiveCorrect >= COMBO_THRESHOLD_5X

    /**
     * Check if combo is active (at least 1 correct)
     */
    val isActive: Boolean
        get() = consecutiveCorrect > 0

    /**
     * Calculate minimum thinking time for a word (anti-guessing threshold)
     * Formula: 1000ms + (wordLength * 500ms)
     *
     * Examples:
     * - 3-letter word: 2500ms minimum
     * - 5-letter word: 3500ms minimum
     * - 7-letter word: 4500ms minimum
     */
    fun calculateMinimumThinkTime(wordLength: Int): Long {
        return MIN_THINK_TIME_BASE_MS + (wordLength * MILLIS_PER_LETTER)
    }

    /**
     * Check if response time indicates adequate thinking (not guessing)
     * @param responseTime Time taken to answer in milliseconds
     * @param wordLength Length of the target word
     * @return true if response time indicates thinking, false if likely guessing
     */
    fun isAdequateThinkingTime(
        responseTime: Long,
        wordLength: Int,
    ): Boolean {
        return responseTime >= calculateMinimumThinkTime(wordLength)
    }

    /**
     * Create a new combo state after a correct answer
     * @param responseTime Time taken for this answer
     * @param wordLength Length of the word
     * @return New ComboState with incremented count or same state if guessing
     */
    fun afterCorrectAnswer(
        responseTime: Long,
        wordLength: Int,
    ): ComboState {
        return if (isAdequateThinkingTime(responseTime, wordLength)) {
            // Adequate thinking time - increment combo
            val newCount = consecutiveCorrect + 1
            // Update average response time
            val newAvgTime =
                if (consecutiveCorrect > 0) {
                    ((averageResponseTime * consecutiveCorrect) + responseTime) / (consecutiveCorrect + 1)
                } else {
                    responseTime
                }
            // Pass 1.0f to trigger auto-calculation in init block
            copy(
                consecutiveCorrect = newCount,
                averageResponseTime = newAvgTime,
                currentMultiplier = 1.0f,
            )
        } else {
            // Too fast - likely guessing, don't increment combo
            this
        }
    }

    /**
     * Reset combo to initial state
     * Called after incorrect answer
     */
    fun reset(): ComboState {
        return copy(
            consecutiveCorrect = 0,
            averageResponseTime = 0L,
            currentMultiplier = MULTIPLIER_BASE,
        )
    }
}

/**
 * Extension property alias for isHighCombo
 * Used for clearer naming in UI code
 */
val ComboState.isFireStreak: Boolean
    get() = isHighCombo

/**
 * Get motivational message based on combo state
 * Part of P0 Phase 1: Visual Feedback Enhancements
 */
fun ComboState.getMotivationalMessage(isLastWord: Boolean): String {
    return when {
        isLastWord -> "最后一个词！"
        isFireStreak -> "🔥 你在燃烧！"
        consecutiveCorrect >= ComboState.COMBO_THRESHOLD_3X -> "太棒了！"
        consecutiveCorrect > 0 -> "继续前进！"
        else -> "开始吧！"
    }
}
