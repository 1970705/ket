package com.wordland.domain.model.achievement

import androidx.compose.runtime.Immutable

/**
 * Game events that trigger achievement checks
 * Sealed class for type-safe event handling
 */
@Immutable
sealed class GameEvent {
    /**
     * User ID for this event
     */
    abstract val userId: String

    /**
     * Timestamp when event occurred
     */
    abstract val timestamp: Long

    /**
     * Word mastered (memory strength >= threshold)
     */
    data class WordMastered(
        override val userId: String,
        val wordId: String,
        val memoryStrength: Int,
        val stars: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()

    /**
     * Level completed
     */
    data class LevelComplete(
        override val userId: String,
        val levelId: String,
        val islandId: String,
        val levelNumber: Int,
        val stars: Int,
        val allThreeStar: Boolean,
        val hintsUsed: Int,
        val wordCount: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()

    /**
     * Combo milestone reached
     */
    data class ComboAchieved(
        override val userId: String,
        val comboCount: Int,
        val averageResponseTime: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()

    /**
     * Daily streak updated
     */
    data class StreakUpdate(
        override val userId: String,
        val streakDays: Int,
        val freezeDaysUsed: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()

    /**
     * Answer submitted (for tracking)
     */
    data class AnswerSubmitted(
        override val userId: String,
        val wordId: String,
        val isCorrect: Boolean,
        val isGuessing: Boolean,
        val responseTime: Long,
        val hintUsed: Boolean,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()

    /**
     * Island completed
     */
    data class IslandComplete(
        override val userId: String,
        val islandId: String,
        val levelsCompleted: Int,
        val totalLevels: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : GameEvent()
}
