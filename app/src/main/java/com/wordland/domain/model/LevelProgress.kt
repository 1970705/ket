package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User's progress for a specific level
 * Tracks completion status, stars, and attempts
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey
    val levelId: String, // "look_island_level_01"
    val userId: String, // "user_001" (MVP: single user)
    val islandId: String, // "look_island"
    // Level Status
    val status: LevelStatus,
    val stars: Int = 0, // 0-3
    // Time Tracking
    val totalTime: Long = 0, // Total time spent (ms)
    val bestTime: Long? = null, // Best completion time (ms)
    val lastPlayTime: Long? = null, // Last play time (ms)
    // Attempts
    val attempts: Int = 0, // Total attempts
    val firstPlayTime: Long? = null, // First play time (ms)
    var practiceCount: Int = 0, // Practice count
    // Content Completion
    val isNewWordsCompleted: Boolean = false, // Are all new words completed?
    val isReviewCompleted: Boolean = false, // Is review completed?
    // Score
    val score: Int = 0, // Highest score
    // Difficulty
    val difficulty: String, // "easy", "normal", "hard"
    // Mastery tracking
    var masteredAt: Long? = null, // When level was mastered
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /**
     * Calculate completion percentage
     */
    fun getCompletionPercentage(): Double {
        val totalTasks = 5 // TODO: Define based on level design
        val completedTasks =
            when (isReviewCompleted) {
                true -> totalTasks
                false -> stars
            }
        return (completedTasks.toDouble() / totalTasks) * 100
    }
}

/**
 * Level Status enum
 */
enum class LevelStatus {
    LOCKED, // Locked, not yet available
    UNLOCKED, // Unlocked, not yet played
    IN_PROGRESS, // Currently playing
    COMPLETED, // Completed (not necessarily perfected)
    PERFECT, // Perfected (3 stars)
}
