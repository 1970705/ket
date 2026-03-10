package com.wordland.domain.model.statistics

/**
 * Domain model for global learning statistics
 * Contains overall user progress and performance metrics
 *
 * @property userId User identifier
 * @property totalGames Total games played
 * @property totalScore Cumulative score across all games
 * @property totalPerfectGames Total 3-star games
 * @property totalStudyTime Total study time (ms)
 * @property currentStreak Current consecutive study days
 * @property longestStreak Longest consecutive study days achieved
 * @property lastStudyDate Last study date timestamp (ms)
 * @property totalLevelsCompleted Total levels completed
 * @property totalLevelsPerfected Total levels perfected (3-star all games)
 * @property totalWordsMastered Total words mastered (memory strength >= 80)
 * @property totalCorrectAnswers Total correct answers across all games
 * @property totalPracticeSessions Total practice sessions
 * @property firstUsedAt First app use timestamp (ms)
 * @property lastUpdatedAt Last update timestamp (ms)
 */
data class GlobalStatistics(
    val userId: String,
    val totalGames: Int = 0,
    val totalScore: Int = 0,
    val totalPerfectGames: Int = 0,
    val totalStudyTime: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: Long? = null,
    val totalLevelsCompleted: Int = 0,
    val totalLevelsPerfected: Int = 0,
    val totalWordsMastered: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalPracticeSessions: Int = 0,
    val firstUsedAt: Long,
    val lastUpdatedAt: Long,
) {
    /**
     * Calculate overall accuracy (0.0-1.0)
     * Based on correct answers vs total questions
     */
    val overallAccuracy: Float
        get() = 0f // Requires total questions count, to be calculated from game history

    /**
     * Calculate average score per game
     */
    val averageScore: Float
        get() =
            if (totalGames > 0) {
                totalScore.toFloat() / totalGames.toFloat()
            } else {
                0f
            }

    /**
     * Calculate perfect game rate (0.0-1.0)
     */
    val perfectGameRate: Float
        get() =
            if (totalGames > 0) {
                totalPerfectGames.toFloat() / totalGames.toFloat()
            } else {
                0f
            }

    /**
     * Check if user is currently on a streak
     */
    val hasActiveStreak: Boolean
        get() = currentStreak > 0

    /**
     * Get study time in hours
     */
    val studyTimeHours: Float
        get() = totalStudyTime / (1000f * 60f * 60f)

    /**
     * Get study time in minutes
     */
    val studyTimeMinutes: Float
        get() = totalStudyTime / (1000f * 60f)

    /**
     * Calculate retention rate (days since first use)
     */
    val daysSinceFirstUse: Int
        get() =
            ((System.currentTimeMillis() - firstUsedAt) / (1000 * 60 * 60 * 24)).toInt()

    /**
     * Get user level based on progress
     */
    val userLevel: UserLevel
        get() =
            when {
                totalWordsMastered >= 100 -> UserLevel.EXPERT
                totalWordsMastered >= 50 -> UserLevel.ADVANCED
                totalWordsMastered >= 20 -> UserLevel.INTERMEDIATE
                totalWordsMastered >= 5 -> UserLevel.BEGINNER
                else -> UserLevel.NEWCOMER
            }
}

/**
 * User proficiency level based on words mastered
 */
enum class UserLevel {
    /** Just started, less than 5 words */
    NEWCOMER,

    /** Learning basics, 5-19 words */
    BEGINNER,

    /** Making progress, 20-49 words */
    INTERMEDIATE,

    /** Good vocabulary, 50-99 words */
    ADVANCED,

    /** Large vocabulary, 100+ words */
    EXPERT,
    ;

    /**
     * Get display name for the level
     */
    fun getDisplayName(): String =
        when (this) {
            NEWCOMER -> "初来乍到"
            BEGINNER -> "初学者"
            INTERMEDIATE -> "进阶者"
            ADVANCED -> "高手"
            EXPERT -> "大师"
        }

    /**
     * Get XP range for this level
     */
    fun getXpRange(): IntRange =
        when (this) {
            NEWCOMER -> 0 until 5
            BEGINNER -> 5 until 20
            INTERMEDIATE -> 20 until 50
            ADVANCED -> 50 until 100
            EXPERT -> 100 until Int.MAX_VALUE
        }

    /**
     * Get required XP to reach this level
     */
    fun getRequiredXp(): Int =
        when (this) {
            NEWCOMER -> 0
            BEGINNER -> 5
            INTERMEDIATE -> 20
            ADVANCED -> 50
            EXPERT -> 100
        }
}
