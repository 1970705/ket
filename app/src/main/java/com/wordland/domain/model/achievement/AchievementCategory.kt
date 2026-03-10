package com.wordland.domain.model.achievement

/**
 * Achievement categories for filtering and display
 */
enum class AchievementCategory {
    /**
     * Learning milestones (levels, words mastered)
     */
    PROGRESS,

    /**
     * Excellence (perfect levels, high accuracy)
     */
    PERFORMANCE,

    /**
     * Combo achievements
     */
    COMBO,

    /**
     * Consistency (daily practice)
     */
    STREAK,

    /**
     * Unique challenges
     */
    SPECIAL,
}
