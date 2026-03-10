package com.wordland.data.repository

import com.wordland.data.dao.StatisticsDao
import com.wordland.data.entity.GlobalStatisticsEntity
import com.wordland.data.entity.LevelStatisticsEntity
import com.wordland.data.entity.toDomainModel
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GlobalStatistics
import com.wordland.domain.model.statistics.LevelStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension functions need explicit import
private fun GlobalStatistics.toEntity(): GlobalStatisticsEntity =
    GlobalStatisticsEntity(
        userId = userId,
        totalGames = totalGames,
        totalScore = totalScore,
        totalPerfectGames = totalPerfectGames,
        totalStudyTime = totalStudyTime,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastStudyDate = lastStudyDate,
        totalLevelsCompleted = totalLevelsCompleted,
        totalLevelsPerfected = totalLevelsPerfected,
        totalWordsMastered = totalWordsMastered,
        totalCorrectAnswers = totalCorrectAnswers,
        totalPracticeSessions = totalPracticeSessions,
        firstUsedAt = firstUsedAt,
        lastUpdatedAt = lastUpdatedAt,
    )

private fun LevelStatistics.toEntity(): LevelStatisticsEntity =
    LevelStatisticsEntity(
        userId = userId,
        levelId = levelId,
        totalGames = totalGames,
        completedGames = completedGames,
        perfectGames = perfectGames,
        highestScore = highestScore,
        lowestScore = lowestScore,
        averageScore = averageScore,
        totalScore = totalScore,
        bestTime = bestTime,
        worstTime = worstTime,
        averageTime = averageTime,
        totalTime = totalTime,
        totalCorrect = totalCorrect,
        totalQuestions = totalQuestions,
        overallAccuracy = overallAccuracy,
        bestCombo = bestCombo,
        firstPlayedAt = firstPlayedAt,
        lastPlayedAt = lastPlayedAt,
        lastUpdatedAt = lastUpdatedAt,
    )

/**
 * Repository for statistics operations
 *
 * Handles database operations for aggregated statistics
 * (level and global) and provides Flow-based data streams
 */
@Singleton
class StatisticsRepositoryImpl
    @Inject
    constructor(
        private val statisticsDao: StatisticsDao,
    ) : StatisticsRepository {
        // ==================== Global Statistics ====================

        override suspend fun getGlobalStatistics(userId: String): Result<GlobalStatistics> {
            return try {
                val entity = statisticsDao.getGlobalStatisticsSync(userId)
                if (entity != null) {
                    Result.Success(entity.toDomainModel())
                } else {
                    // Initialize with default statistics
                    val newStatsEntity = createDefaultGlobalStatistics(userId)
                    statisticsDao.insertGlobalStatistics(newStatsEntity)
                    Result.Success(newStatsEntity.toDomainModel())
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override fun getGlobalStatisticsFlow(userId: String): Flow<GlobalStatistics?> {
            return statisticsDao.getGlobalStatistics(userId)
                .map { entity -> entity?.toDomainModel() }
        }

        override suspend fun insertGlobalStatistics(stats: GlobalStatistics): Result<Unit> {
            return try {
                statisticsDao.insertGlobalStatistics(stats.toEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override suspend fun updateGlobalStatistics(
            userId: String,
            gameScore: Int,
            gamePerfect: Boolean,
            studyTime: Long,
        ): Result<GlobalStatistics> {
            return try {
                val current = statisticsDao.getGlobalStatisticsSync(userId)

                val updated =
                    if (current != null) {
                        current.copy(
                            totalGames = current.totalGames + 1,
                            totalScore = current.totalScore + gameScore,
                            totalPerfectGames = current.totalPerfectGames + if (gamePerfect) 1 else 0,
                            totalStudyTime = current.totalStudyTime + studyTime,
                            lastUpdatedAt = System.currentTimeMillis(),
                        ).toDomainModel()
                    } else {
                        createDefaultGlobalStatistics(userId).copy(
                            totalGames = 1,
                            totalScore = gameScore,
                            totalPerfectGames = if (gamePerfect) 1 else 0,
                            totalStudyTime = studyTime,
                            lastUpdatedAt = System.currentTimeMillis(),
                        ).toDomainModel()
                    }

                // Update streak
                val withUpdatedStreak = updateStreak(updated.toEntity())
                statisticsDao.insertGlobalStatistics(withUpdatedStreak)

                Result.Success(withUpdatedStreak.toDomainModel())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        // ==================== Level Statistics ====================

        override fun getLevelStatistics(
            userId: String,
            levelId: String,
        ): Flow<LevelStatistics?> {
            return statisticsDao.getLevelStatistics(userId, levelId)
                .map { entity -> entity?.toDomainModel() }
        }

        override suspend fun getLevelStatisticsSync(
            userId: String,
            levelId: String,
        ): Result<LevelStatistics?> {
            return try {
                val entity = statisticsDao.getLevelStatisticsSync(userId, levelId)
                Result.Success(entity?.toDomainModel())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override fun getAllLevelStatistics(userId: String): Flow<List<LevelStatistics>> {
            return statisticsDao.getAllLevelStatistics(userId)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getLevelStatisticsByIsland(
            userId: String,
            islandId: String,
        ): Flow<List<LevelStatistics>> {
            return statisticsDao.getLevelStatisticsByIsland(userId, islandId)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override suspend fun insertLevelStatistics(stats: LevelStatistics): Result<Unit> {
            return try {
                statisticsDao.insertLevelStatistics(stats.toEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override suspend fun updateLevelStatistics(
            userId: String,
            levelId: String,
            gameHistory: com.wordland.domain.model.statistics.GameHistory,
        ): Result<LevelStatistics> {
            return try {
                // Initialize if not exists
                statisticsDao.initializeLevelStatistics(userId, levelId)

                // Update statistics
                statisticsDao.updateLevelStatistics(
                    userId = userId,
                    levelId = levelId,
                    score = gameHistory.score,
                    stars = gameHistory.stars,
                    duration = gameHistory.duration,
                    isCompleted = gameHistory.isCompleted,
                    correctAnswers = gameHistory.correctAnswers,
                    totalQuestions = gameHistory.totalQuestions,
                    maxCombo = gameHistory.maxCombo,
                    currentTime = System.currentTimeMillis(),
                )

                // Return updated statistics
                val updated = statisticsDao.getLevelStatisticsSync(userId, levelId)
                Result.Success(updated?.toDomainModel() ?: createDefaultLevelStatistics(userId, levelId))
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override fun getCompletedLevelsCount(userId: String): Flow<Int> {
            return statisticsDao.getCompletedLevelsCount(userId)
        }

        override fun getPerfectedLevelsCount(userId: String): Flow<Int> {
            return statisticsDao.getPerfectedLevelsCount(userId)
        }

        override fun getBestPerformingLevels(
            userId: String,
            limit: Int,
        ): Flow<List<LevelStatistics>> {
            return statisticsDao.getBestPerformingLevels(userId, limit)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getLevelsNeedingPractice(
            userId: String,
            limit: Int,
        ): Flow<List<LevelStatistics>> {
            return statisticsDao.getLevelsNeedingPractice(userId, limit)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        // ==================== Aggregate Queries ====================

        override suspend fun getAggregateStatistics(userId: String): Result<AggregateStats> {
            return try {
                val agg = statisticsDao.getAggregateStatistics(userId)
                if (agg != null) {
                    Result.Success(
                        AggregateStats(
                            totalLevels = agg.totalLevels,
                            totalGames = agg.totalGames,
                            completedGames = agg.completedGames,
                            perfectGames = agg.perfectGames,
                            totalScore = agg.totalScore,
                            totalTime = agg.totalTime,
                            totalCorrect = agg.totalCorrect,
                            totalQuestions = agg.totalQuestions,
                        ),
                    )
                } else {
                    Result.Success(
                        AggregateStats(
                            totalLevels = 0,
                            totalGames = 0,
                            completedGames = 0,
                            perfectGames = 0,
                            totalScore = 0,
                            totalTime = 0,
                            totalCorrect = 0,
                            totalQuestions = 0,
                        ),
                    )
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        // ==================== Helper Methods ====================

        private fun createDefaultGlobalStatistics(userId: String): GlobalStatisticsEntity {
            val now = System.currentTimeMillis()
            return GlobalStatisticsEntity(
                userId = userId,
                totalGames = 0,
                totalScore = 0,
                totalPerfectGames = 0,
                totalStudyTime = 0,
                currentStreak = 0,
                longestStreak = 0,
                lastStudyDate = null,
                totalLevelsCompleted = 0,
                totalLevelsPerfected = 0,
                totalWordsMastered = 0,
                totalCorrectAnswers = 0,
                totalPracticeSessions = 0,
                firstUsedAt = now,
                lastUpdatedAt = now,
            )
        }

        private fun createDefaultLevelStatistics(
            userId: String,
            levelId: String,
        ): LevelStatistics {
            val now = System.currentTimeMillis()
            return LevelStatistics(
                userId = userId,
                levelId = levelId,
                totalGames = 0,
                completedGames = 0,
                perfectGames = 0,
                highestScore = 0,
                lowestScore = 0,
                averageScore = 0f,
                totalScore = 0,
                bestTime = null,
                worstTime = null,
                averageTime = 0,
                totalTime = 0,
                totalCorrect = 0,
                totalQuestions = 0,
                overallAccuracy = 0f,
                bestCombo = 0,
                firstPlayedAt = null,
                lastPlayedAt = null,
                lastUpdatedAt = now,
            )
        }

        private fun updateStreak(stats: GlobalStatisticsEntity): GlobalStatisticsEntity {
            val now = System.currentTimeMillis()
            val todayMidnight = getTodayMidnight(now)

            // Check if last study was yesterday
            val yesterdayMidnight = todayMidnight - 24 * 60 * 60 * 1000L

            val newCurrentStreak =
                when {
                    stats.lastStudyDate == null -> 1
                    stats.lastStudyDate >= todayMidnight -> stats.currentStreak + 1
                    stats.lastStudyDate >= yesterdayMidnight -> stats.currentStreak // Still in streak
                    else -> 1 // Reset streak
                }

            val newLongestStreak = maxOf(stats.longestStreak, newCurrentStreak)

            return stats.copy(
                currentStreak = newCurrentStreak,
                longestStreak = newLongestStreak,
                lastStudyDate = now,
                lastUpdatedAt = now,
            )
        }

        private fun getTodayMidnight(now: Long): Long {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }

/**
 * Interface for statistics repository operations
 */
interface StatisticsRepository {
    // ==================== Global Statistics ====================

    /**
     * Get global statistics for a user (suspend)
     */
    suspend fun getGlobalStatistics(userId: String): Result<GlobalStatistics>

    /**
     * Get global statistics for a user (Flow)
     */
    fun getGlobalStatisticsFlow(userId: String): Flow<GlobalStatistics?>

    /**
     * Insert or update global statistics
     */
    suspend fun insertGlobalStatistics(stats: GlobalStatistics): Result<Unit>

    /**
     * Update global statistics after a game
     *
     * @param userId User identifier
     * @param gameScore Score from the game
     * @param gamePerfect Whether the game was perfect (3 stars)
     * @param studyTime Time spent in the game (ms)
     * @return Updated global statistics
     */
    suspend fun updateGlobalStatistics(
        userId: String,
        gameScore: Int,
        gamePerfect: Boolean,
        studyTime: Long,
    ): Result<GlobalStatistics>

    // ==================== Level Statistics ====================

    /**
     * Get level statistics (Flow)
     */
    fun getLevelStatistics(
        userId: String,
        levelId: String,
    ): Flow<LevelStatistics?>

    /**
     * Get level statistics (suspend)
     */
    suspend fun getLevelStatisticsSync(
        userId: String,
        levelId: String,
    ): Result<LevelStatistics?>

    /**
     * Get all level statistics for a user
     */
    fun getAllLevelStatistics(userId: String): Flow<List<LevelStatistics>>

    /**
     * Get level statistics for an island
     */
    fun getLevelStatisticsByIsland(
        userId: String,
        islandId: String,
    ): Flow<List<LevelStatistics>>

    /**
     * Insert or update level statistics
     */
    suspend fun insertLevelStatistics(stats: LevelStatistics): Result<Unit>

    /**
     * Update level statistics after a game
     *
     * @param userId User identifier
     * @param levelId Level identifier
     * @param gameHistory Completed game data
     * @return Updated level statistics
     */
    suspend fun updateLevelStatistics(
        userId: String,
        levelId: String,
        gameHistory: com.wordland.domain.model.statistics.GameHistory,
    ): Result<LevelStatistics>

    /**
     * Get count of completed levels
     */
    fun getCompletedLevelsCount(userId: String): Flow<Int>

    /**
     * Get count of perfected levels
     */
    fun getPerfectedLevelsCount(userId: String): Flow<Int>

    /**
     * Get best performing levels
     */
    fun getBestPerformingLevels(
        userId: String,
        limit: Int = 5,
    ): Flow<List<LevelStatistics>>

    /**
     * Get levels needing practice
     */
    fun getLevelsNeedingPractice(
        userId: String,
        limit: Int = 5,
    ): Flow<List<LevelStatistics>>

    // ==================== Aggregate Queries ====================

    /**
     * Get aggregate statistics across all levels
     */
    suspend fun getAggregateStatistics(userId: String): Result<AggregateStats>
}

/**
 * Aggregate statistics data class
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

    /**
     * Calculate completion rate
     */
    val completionRate: Float
        get() =
            if (totalGames > 0) {
                completedGames.toFloat() / totalGames.toFloat()
            } else {
                0f
            }
}
