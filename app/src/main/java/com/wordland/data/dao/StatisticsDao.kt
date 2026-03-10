package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wordland.data.entity.GlobalStatisticsEntity
import com.wordland.data.entity.LevelStatisticsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for statistics operations
 *
 * Provides database operations for level and global statistics
 */
@Dao
interface StatisticsDao {
    // ==================== Level Statistics ====================

    /**
     * Insert or update level statistics
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevelStatistics(stats: LevelStatisticsEntity)

    /**
     * Get level statistics for a specific level
     *
     * @param userId User identifier
     * @param levelId Level identifier
     * @return Flow of level statistics
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId AND levelId = :levelId
        LIMIT 1
        """,
    )
    fun getLevelStatistics(
        userId: String,
        levelId: String,
    ): Flow<LevelStatisticsEntity?>

    /**
     * Get all level statistics for a user
     *
     * @param userId User identifier
     * @return Flow of all level statistics
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId
        ORDER BY lastPlayedAt DESC
        """,
    )
    fun getAllLevelStatistics(userId: String): Flow<List<LevelStatisticsEntity>>

    /**
     * Get level statistics for an island
     * Filters levels by island ID prefix
     *
     * @param userId User identifier
     * @param islandIdPrefix Island ID prefix (e.g., "look_island")
     * @return Flow of level statistics for the island
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId AND levelId LIKE :islandIdPrefix || '%'
        ORDER BY levelId ASC
        """,
    )
    fun getLevelStatisticsByIsland(
        userId: String,
        islandIdPrefix: String,
    ): Flow<List<LevelStatisticsEntity>>

    /**
     * Get completed levels count
     *
     * @param userId User identifier
     * @return Number of levels with at least one completed game
     */
    @Query(
        """
        SELECT COUNT(*) FROM level_statistics
        WHERE userId = :userId AND completedGames > 0
        """,
    )
    fun getCompletedLevelsCount(userId: String): Flow<Int>

    /**
     * Get perfected levels count
     *
     * @param userId User identifier
     * @return Number of levels with at least one perfect game
     */
    @Query(
        """
        SELECT COUNT(*) FROM level_statistics
        WHERE userId = :userId AND perfectGames > 0
        """,
    )
    fun getPerfectedLevelsCount(userId: String): Flow<Int>

    /**
     * Get best performing levels
     * Returns levels with highest accuracy
     *
     * @param userId User identifier
     * @param limit Maximum number of levels to return
     * @return Flow of level statistics ordered by accuracy
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId AND totalGames > 0
        ORDER BY overallAccuracy DESC, totalGames DESC
        LIMIT :limit
        """,
    )
    fun getBestPerformingLevels(
        userId: String,
        limit: Int = 5,
    ): Flow<List<LevelStatisticsEntity>>

    /**
     * Get levels needing practice
     * Returns levels with low accuracy or completion rate
     *
     * @param userId User identifier
     * @param limit Maximum number of levels to return
     * @return Flow of level statistics ordered by accuracy (ascending)
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId AND totalGames >= 2
        ORDER BY overallAccuracy ASC,
                 CAST(completedGames AS REAL) / NULLIF(totalGames, 0) ASC
        LIMIT :limit
        """,
    )
    fun getLevelsNeedingPractice(
        userId: String,
        limit: Int = 5,
    ): Flow<List<LevelStatisticsEntity>>

    // ==================== Global Statistics ====================

    /**
     * Insert or update global statistics
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlobalStatistics(stats: GlobalStatisticsEntity)

    /**
     * Get global statistics for a user
     *
     * @param userId User identifier
     * @return Flow of global statistics
     */
    @Query(
        """
        SELECT * FROM global_statistics
        WHERE userId = :userId
        LIMIT 1
        """,
    )
    fun getGlobalStatistics(userId: String): Flow<GlobalStatisticsEntity?>

    /**
     * Check if global statistics exists for a user
     *
     * @param userId User identifier
     * @return Flow indicating if statistics exist
     */
    @Query(
        """
        SELECT COUNT(*) > 0 FROM global_statistics
        WHERE userId = :userId
        """,
    )
    fun hasGlobalStatistics(userId: String): Flow<Boolean>

    // ==================== Aggregation Queries ====================

    /**
     * Get aggregate statistics across all levels
     * Useful for dashboard display
     *
     * @param userId User identifier
     * @return Flow containing total games, total score, and total accuracy
     */
    @Query(
        """
        SELECT
            COUNT(*) as totalLevels,
            SUM(totalGames) as totalGames,
            SUM(completedGames) as completedGames,
            SUM(perfectGames) as perfectGames,
            SUM(totalScore) as totalScore,
            SUM(totalTime) as totalTime,
            SUM(totalCorrect) as totalCorrect,
            SUM(totalQuestions) as totalQuestions
        FROM level_statistics
        WHERE userId = :userId
        """,
    )
    suspend fun getAggregateStatistics(userId: String): AggregateStats?

    /**
     * Update level statistics after a game
     * This is a complex update that increments multiple fields
     *
     * @param userId User identifier
     * @param levelId Level identifier
     * @param score Game score
     * @param stars Game stars
     * @param duration Game duration
     * @param isCompleted Whether the game was completed
     * @param correctAnswers Number of correct answers
     * @param totalQuestions Number of total questions
     * @param maxCombo Maximum combo
     */
    @Query(
        """
        UPDATE level_statistics
        SET
            totalGames = totalGames + 1,
            completedGames = completedGames + CASE WHEN :isCompleted THEN 1 ELSE 0 END,
            perfectGames = perfectGames + CASE WHEN :stars >= 3 THEN 1 ELSE 0 END,
            highestScore = MAX(highestScore, :score),
            lowestScore = CASE WHEN lowestScore = 0 THEN :score ELSE MIN(lowestScore, :score) END,
            totalScore = totalScore + :score,
            bestTime = CASE WHEN :isCompleted AND (bestTime IS NULL OR :duration < bestTime) THEN :duration ELSE bestTime END,
            worstTime = CASE WHEN :isCompleted AND (worstTime IS NULL OR :duration > worstTime) THEN :duration ELSE worstTime END,
            totalTime = totalTime + :duration,
            totalCorrect = totalCorrect + :correctAnswers,
            totalQuestions = totalQuestions + :totalQuestions,
            overallAccuracy = CAST((totalCorrect + :correctAnswers) AS REAL) / CAST((totalQuestions + :totalQuestions) AS REAL),
            bestCombo = MAX(bestCombo, :maxCombo),
            lastPlayedAt = :currentTime,
            lastUpdatedAt = :currentTime
        WHERE userId = :userId AND levelId = :levelId
        """,
    )
    suspend fun updateLevelStatistics(
        userId: String,
        levelId: String,
        score: Int,
        stars: Int,
        duration: Long,
        isCompleted: Boolean,
        correctAnswers: Int,
        totalQuestions: Int,
        maxCombo: Int,
        currentTime: Long = System.currentTimeMillis(),
    )

    /**
     * Initialize level statistics for a new level
     * Inserts only if not exists
     */
    @Query(
        """
        INSERT INTO level_statistics (userId, levelId, firstPlayedAt, lastPlayedAt, lastUpdatedAt)
        SELECT :userId, :levelId, :currentTime, :currentTime, :currentTime
        WHERE NOT EXISTS (SELECT 1 FROM level_statistics WHERE userId = :userId AND levelId = :levelId)
        """,
    )
    suspend fun initializeLevelStatistics(
        userId: String,
        levelId: String,
        currentTime: Long = System.currentTimeMillis(),
    )

    // ==================== Sync Methods (for Repository) ====================

    /**
     * Get global statistics synchronously
     */
    @Query(
        """
        SELECT * FROM global_statistics
        WHERE userId = :userId
        LIMIT 1
        """,
    )
    suspend fun getGlobalStatisticsSync(userId: String): GlobalStatisticsEntity?

    /**
     * Get level statistics synchronously
     */
    @Query(
        """
        SELECT * FROM level_statistics
        WHERE userId = :userId AND levelId = :levelId
        LIMIT 1
        """,
    )
    suspend fun getLevelStatisticsSync(
        userId: String,
        levelId: String,
    ): LevelStatisticsEntity?
}

/**
 * Aggregate statistics data class
 * Used for complex aggregation queries
 */
data class AggregateStats(
    val totalLevels: Int,
    val totalGames: Int,
    val completedGames: Int,
    val perfectGames: Int,
    val totalScore: Int,
    val totalTime: Long,
    val totalCorrect: Int,
    val totalQuestions: Int,
) {
    /**
     * Calculate overall accuracy
     */
    val overallAccuracy: Float
        get() =
            if (totalQuestions > 0) {
                totalCorrect.toFloat() / totalQuestions.toFloat()
            } else {
                0f
            }
}
