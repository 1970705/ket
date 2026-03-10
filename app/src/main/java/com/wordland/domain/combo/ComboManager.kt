package com.wordland.domain.combo

import com.wordland.domain.model.ComboState

/**
 * Domain logic for managing combo system
 * Implements anti-guessing detection and combo calculation
 *
 * Design principles:
 * - Encourages thoughtful answers over fast answers
 * - Prevents gaming the system via rapid guessing
 * - Rewards consistent correct answers with adequate thinking time
 */
object ComboManager {
    /**
     * Calculate new combo state after an answer submission
     *
     * @param currentState Current combo state before this answer
     * @param isCorrect Whether the answer was correct
     * @param responseTime Time taken to answer (milliseconds)
     * @param wordLength Length of the target word
     * @return New combo state
     */
    fun calculateCombo(
        currentState: ComboState,
        isCorrect: Boolean,
        responseTime: Long,
        wordLength: Int,
    ): ComboState {
        return if (isCorrect) {
            currentState.afterCorrectAnswer(responseTime, wordLength)
        } else {
            currentState.reset()
        }
    }

    /**
     * Check if a response time indicates guessing behavior
     *
     * Anti-guessing threshold: 1000ms + (wordLength * 500ms)
     *
     * Examples:
     * - "cat" (3 letters): 2500ms minimum
     * - "apple" (5 letters): 3500ms minimum
     * - "banana" (6 letters): 4000ms minimum
     * - "computer" (8 letters): 5000ms minimum
     *
     * @param responseTime Time taken to answer
     * @param wordLength Length of the word
     * @return true if likely guessing (too fast), false if adequate thinking
     */
    fun isGuessing(
        responseTime: Long,
        wordLength: Int,
    ): Boolean {
        val minimumThinkTime = calculateMinimumThinkTime(wordLength)
        return responseTime < minimumThinkTime
    }

    /**
     * Calculate minimum thinking time for a word
     * @param wordLength Length of the word
     * @return Minimum time in milliseconds
     */
    fun calculateMinimumThinkTime(wordLength: Int): Long {
        return ComboState.MIN_THINK_TIME_BASE_MS + (wordLength * ComboState.MILLIS_PER_LETTER)
    }

    /**
     * Get the score multiplier for a given combo count
     *
     * Multiplier tiers:
     * - 0-2 combo: 1.0x (no bonus)
     * - 3-4 combo: 1.2x (20% bonus)
     * - 5+ combo: 1.5x (50% bonus)
     *
     * @param comboCount Current consecutive correct count
     * @return Score multiplier
     */
    fun getMultiplier(comboCount: Int): Float {
        return when {
            comboCount >= ComboState.COMBO_THRESHOLD_5X -> ComboState.MULTIPLIER_5X
            comboCount >= ComboState.COMBO_THRESHOLD_3X -> ComboState.MULTIPLIER_3X
            else -> ComboState.MULTIPLIER_BASE
        }
    }

    /**
     * Check if combo reached a new milestone
     * Used for triggering milestone celebrations
     *
     * @param previousCount Previous combo count
     * @param newCount New combo count
     * @return Milestone reached (3 or 5), or null if no milestone
     */
    fun getMilestoneReached(
        previousCount: Int,
        newCount: Int,
    ): Int? {
        // Only return milestone if we just reached it (not already at or above it)
        return when {
            newCount == ComboState.COMBO_THRESHOLD_3X && previousCount < ComboState.COMBO_THRESHOLD_3X -> ComboState.COMBO_THRESHOLD_3X
            newCount == ComboState.COMBO_THRESHOLD_5X && previousCount < ComboState.COMBO_THRESHOLD_5X -> ComboState.COMBO_THRESHOLD_5X
            else -> null
        }
    }

    /**
     * Calculate bonus score from combo multiplier
     * @param baseScore Base score before multiplier
     * @param multiplier Current combo multiplier
     * @return Total score with bonus
     */
    fun applyMultiplier(
        baseScore: Int,
        multiplier: Float,
    ): Int {
        return (baseScore * multiplier).toInt()
    }

    /**
     * Get combo feedback message for UI display
     * @param comboCount Current combo count
     * @return Feedback message or null if no message
     */
    fun getComboMessage(comboCount: Int): String? {
        return when (comboCount) {
            0 -> null
            1, 2 -> null
            3 -> "Nice streak!"
            4 -> "Keep it up!"
            5 -> "On fire! \uD83D\uDD25"
            6 -> "Unstoppable!"
            7 -> "Incredible!"
            8 -> "Amazing!"
            9 -> "Legendary!"
            10 -> "Godlike! \uD83D\uDC51"
            else -> if (comboCount % 5 == 0) "Still on fire! \uD83D\uDD25" else "Dominating!"
        }
    }

    /**
     * Check if animation should be triggered for combo milestone
     * @param comboCount Current combo count
     * @return true if milestone animation should play
     */
    fun shouldTriggerMilestoneAnimation(comboCount: Int): Boolean {
        return comboCount in
            listOf(
                ComboState.COMBO_THRESHOLD_3X,
                ComboState.COMBO_THRESHOLD_5X,
            ) || (comboCount > ComboState.COMBO_THRESHOLD_5X && comboCount % 5 == 0)
    }
}
