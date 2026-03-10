package com.wordland.domain.model.statistics

/**
 * Domain model for aggregated level statistics
 * Contains cumulative statistics for a specific level across all plays
 *
 * @property userId User identifier
 * @property levelId Level identifier
 * @property totalGames Total games played
 * @property completedGames Games completed (not abandoned)
 * @property perfectGames Games with 3-star rating
 * @property highestScore Highest score achieved
 * @property lowestScore Lowest score achieved
 * @property averageScore Average score across all games
 * @property totalScore Cumulative score
 * @property bestTime Best completion time (ms), null if no completed games
 * @property worstTime Worst completion time (ms), null if no completed games
 * @property averageTime Average completion time (ms)
 * @property totalTime Total time spent on this level (ms)
 * @property totalCorrect Total correct answers across all games
 * @property totalQuestions Total questions across all games
 * @property overallAccuracy Overall accuracy rate (0.0-1.0)
 * @property bestCombo Highest combo achieved
 * @property firstPlayedAt First play timestamp (ms)
 * @property lastPlayedAt Last play timestamp (ms)
 * @property lastUpdatedAt Last update timestamp (ms)
 */
data class LevelStatistics(
    val userId: String,
    val levelId: String,
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val perfectGames: Int = 0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val averageScore: Float = 0f,
    val totalScore: Int = 0,
    val bestTime: Long? = null,
    val worstTime: Long? = null,
    val averageTime: Long = 0,
    val totalTime: Long = 0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0,
    val overallAccuracy: Float = 0f,
    val bestCombo: Int = 0,
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,
    val lastUpdatedAt: Long,
) {
    /**
     * Calculate completion rate (0.0-1.0)
     */
    val completionRate: Float
        get() =
            if (totalGames > 0) {
                completedGames.toFloat() / totalGames.toFloat()
            } else {
                0f
            }

    /**
     * Calculate perfect game rate (0.0-1.0)
     */
    val perfectRate: Float
        get() =
            if (totalGames > 0) {
                perfectGames.toFloat() / totalGames.toFloat()
            } else {
                0f
            }

    /**
     * Check if level is mastered (high completion and accuracy)
     */
    val isMastered: Boolean
        get() =
            totalGames >= 3 &&
                completionRate >= 0.8f &&
                overallAccuracy >= 0.8f &&
                perfectGames >= 1

    /**
     * Get performance tier based on statistics
     */
    val performanceTier: PerformanceTier
        get() =
            when {
                isMastered && perfectGames >= totalGames * 0.5 -> PerformanceTier.EXPERT
                isMastered -> PerformanceTier.ADVANCED
                totalGames >= 2 && completionRate >= 0.5 -> PerformanceTier.INTERMEDIATE
                totalGames >= 1 -> PerformanceTier.BEGINNER
                else -> PerformanceTier.NOT_STARTED
            }
}

/**
 * Performance tier for a level
 */
enum class PerformanceTier {
    /** Level not yet played */
    NOT_STARTED,

    /** Just started, few plays */
    BEGINNER,

    /** Some progress, moderate performance */
    INTERMEDIATE,

    /** Good performance, mastered */
    ADVANCED,

    /** Excellent performance, expert level */
    EXPERT,
    ;

    /**
     * Get display name for the tier
     */
    fun getDisplayName(): String =
        when (this) {
            NOT_STARTED -> "未开始"
            BEGINNER -> "初学者"
            INTERMEDIATE -> "进阶中"
            ADVANCED -> "精通"
            EXPERT -> "专家"
        }

    /**
     * Get color for UI representation
     */
    fun getColor(): Long =
        when (this) {
            NOT_STARTED -> 0xFF9E9E9E // Gray
            BEGINNER -> 0xFFFFB74D // Orange
            INTERMEDIATE -> 0xFF4FC3F7 // Light Blue
            ADVANCED -> 0xFF66BB6A // Green
            EXPERT -> 0xFFAB47BC // Purple
        }
}
