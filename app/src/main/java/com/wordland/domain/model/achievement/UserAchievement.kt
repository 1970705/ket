package com.wordland.domain.model.achievement

import androidx.compose.runtime.Immutable

/**
 * User's progress on an achievement
 * This is the domain model - separate from the data layer entity
 */
@Immutable
data class UserAchievement(
    val userId: String,
    val achievementId: String,
    /**
     * Whether achievement is unlocked
     */
    val isUnlocked: Boolean = false,
    /**
     * Current progress value (toward targetValue)
     */
    val progress: Int = 0,
    /**
     * Target value from AchievementRequirement.targetValue
     */
    val target: Int = 1,
    /**
     * When achievement was unlocked (null if locked)
     */
    val unlockedAt: Long? = null,
    /**
     * Last time progress was updated
     */
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    /**
     * Calculate progress percentage (0-100)
     */
    val progressPercentage: Float
        get() =
            if (target > 0) {
                (progress.toFloat() / target.toFloat()).coerceAtMost(1f)
            } else {
                if (isUnlocked) 1f else 0f
            }

    /**
     * Get progress display text
     */
    fun getProgressText(requirement: AchievementRequirement): String {
        return requirement.getProgressText(progress)
    }
}

/**
 * Achievement with user progress info
 */
data class AchievementWithProgress(
    val achievement: Achievement,
    val progress: Int,
    val target: Int,
    val isUnlocked: Boolean,
) {
    /**
     * Calculate progress percentage (0-100)
     */
    val progressPercentage: Float
        get() =
            if (target > 0) {
                (progress.toFloat() / target.toFloat()).coerceAtMost(1f)
            } else {
                if (isUnlocked) 1f else 0f
            }
}

/**
 * Completion statistics for achievements
 */
data class CompletionStats(
    val unlocked: Int,
    val total: Int,
    val percentage: Float,
)
