package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Quick Judge Game Question
 *
 * User sees a word with a translation and must quickly decide if the translation is correct.
 * This is a speed-based game mode that tests vocabulary recognition.
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class QuickJudgeQuestion(
    val wordId: String,
    val word: String, // The English word
    val translation: String, // The Chinese translation shown to user
    val isCorrect: Boolean, // Whether this translation is actually correct
    val timeLimit: Int = 5, // Time limit in seconds (default 5)
    val difficulty: Int = 1, // 1-5
) {
    /**
     * Check if user's judgment is correct
     * @param userSaidTrue True if user clicked "Correct", False if user clicked "Incorrect"
     * @return True if user's judgment matches the actual correctness
     */
    fun isJudgmentCorrect(userSaidTrue: Boolean): Boolean {
        return userSaidTrue == isCorrect
    }

    /**
     * Calculate time bonus based on remaining time
     * @param timeTakenMs Time taken to answer in milliseconds
     * @return Time bonus score (0-100)
     */
    fun calculateTimeBonus(timeTakenMs: Long): Int {
        val timeLimitMs = timeLimit * 1000L
        if (timeTakenMs > timeLimitMs) return 0

        val remainingMs = timeLimitMs - timeTakenMs
        val remainingRatio = remainingMs.toFloat() / timeLimitMs

        // Bonus: up to 100 points based on speed
        return (remainingRatio * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Get the actual correct translation for this word
     * This is used when showing the correct answer after a wrong judgment
     */
    fun getActualCorrectTranslation(): String {
        return translation.takeIf { isCorrect } ?: "[Correct translation hidden]"
    }
}

/**
 * Quick Judge Result
 * Contains the result of a single quick judge question
 */
@Immutable
data class QuickJudgeResult(
    val question: QuickJudgeQuestion,
    val userSaidTrue: Boolean,
    val isCorrect: Boolean,
    val timeTakenMs: Long,
    val timeBonus: Int,
    val baseScore: Int,
    val totalScore: Int,
) {
    /**
     * Check if answered within time limit
     */
    val isWithinTimeLimit: Boolean
        get() = timeTakenMs <= question.timeLimit * 1000L

    /**
     * Get the speed rating (1-3, where 3 is fastest)
     */
    fun getSpeedRating(): Int {
        val timeLimitMs = question.timeLimit * 1000L
        val ratio = timeTakenMs.toFloat() / timeLimitMs
        return when {
            ratio <= 0.3f -> 3 // Very fast
            ratio <= 0.6f -> 2 // Fast
            else -> 1 // Normal
        }
    }
}
