package com.wordland.data.repository

import com.wordland.data.dao.GameHistoryDao
import com.wordland.data.entity.GameHistoryEntity
import com.wordland.data.entity.toDomainModel
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension function for converting domain model to entity
private fun GameHistory.toEntity(): GameHistoryEntity =
    GameHistoryEntity(
        gameId = gameId,
        userId = userId,
        levelId = levelId,
        islandId = islandId,
        gameMode = gameMode.name,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        score = score,
        stars = stars,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        accuracy = accuracy,
        maxCombo = maxCombo,
        hintsUsed = hintsUsed,
        wrongAnswers = wrongAnswers,
        avgResponseTime = avgResponseTime,
        fastestAnswer = fastestAnswer,
        slowestAnswer = slowestAnswer,
        difficulty = difficulty,
        createdAt = createdAt,
    )

/**
 * Repository for game history operations
 *
 * Handles database operations for game history records
 * and provides Flow-based data streams for UI observation
 */
@Singleton
class GameHistoryRepositoryImpl
    @Inject
    constructor(
        private val gameHistoryDao: GameHistoryDao,
    ) : GameHistoryRepository {
        override suspend fun insertGameHistory(history: GameHistory): Result<Unit> {
            return try {
                gameHistoryDao.insertGameHistory(history.toEntity())
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override suspend fun insertGameHistoryList(historyList: List<GameHistory>): Result<Unit> {
            return try {
                gameHistoryDao.insertGameHistoryList(historyList.map { it.toEntity() })
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        override fun getGameHistory(
            userId: String,
            limit: Int,
            offset: Int,
        ): Flow<List<GameHistory>> {
            return gameHistoryDao.getGameHistory(userId, limit, offset)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getGameHistoryByLevel(
            userId: String,
            levelId: String,
            limit: Int,
        ): Flow<List<GameHistory>> {
            return gameHistoryDao.getGameHistoryByLevel(userId, levelId, limit)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getGameHistoryByMode(
            userId: String,
            gameMode: GameMode,
            limit: Int,
        ): Flow<List<GameHistory>> {
            return gameHistoryDao.getGameHistoryByMode(userId, gameMode.name, limit)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getGameHistoryByDateRange(
            userId: String,
            startTime: Long,
            endTime: Long,
            limit: Int,
        ): Flow<List<GameHistory>> {
            return gameHistoryDao.getGameHistoryByDateRange(userId, startTime, endTime, limit)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getRecentGames(
            userId: String,
            days: Int,
        ): Flow<List<GameHistory>> {
            val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            return gameHistoryDao.getRecentGames(userId, startTime)
                .map { entities -> entities.map { it.toDomainModel() } }
        }

        override fun getGameById(gameId: String): Flow<GameHistory?> {
            return gameHistoryDao.getGameById(gameId)
                .map { entity -> entity?.toDomainModel() }
        }

        override fun getGameCount(userId: String): Flow<Int> {
            return gameHistoryDao.getGameCount(userId)
        }

        override fun getPerfectGameCount(userId: String): Flow<Int> {
            return gameHistoryDao.getPerfectGameCount(userId)
        }

        override fun getTotalScore(userId: String): Flow<Int> {
            return gameHistoryDao.getTotalScore(userId)
        }

        override suspend fun deleteOldHistory(beforeTimestamp: Long): Result<Int> {
            return try {
                val deleted = gameHistoryDao.deleteOldHistory(beforeTimestamp)
                Result.Success(deleted)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

/**
 * Interface for game history repository operations
 */
interface GameHistoryRepository {
    /**
     * Insert a single game history record
     */
    suspend fun insertGameHistory(history: GameHistory): Result<Unit>

    /**
     * Insert multiple game history records
     */
    suspend fun insertGameHistoryList(historyList: List<GameHistory>): Result<Unit>

    /**
     * Get game history with pagination
     *
     * @param userId User identifier
     * @param limit Maximum number of records
     * @param offset Number of records to skip
     * @return Flow of game history
     */
    fun getGameHistory(
        userId: String,
        limit: Int = 20,
        offset: Int = 0,
    ): Flow<List<GameHistory>>

    /**
     * Get game history for a specific level
     */
    fun getGameHistoryByLevel(
        userId: String,
        levelId: String,
        limit: Int = 20,
    ): Flow<List<GameHistory>>

    /**
     * Get game history for a specific game mode
     */
    fun getGameHistoryByMode(
        userId: String,
        gameMode: GameMode,
        limit: Int = 20,
    ): Flow<List<GameHistory>>

    /**
     * Get game history within a date range
     */
    fun getGameHistoryByDateRange(
        userId: String,
        startTime: Long,
        endTime: Long,
        limit: Int = 100,
    ): Flow<List<GameHistory>>

    /**
     * Get recent games from the last N days
     *
     * @param userId User identifier
     * @param days Number of days to look back
     * @return Flow of recent games
     */
    fun getRecentGames(
        userId: String,
        days: Int = 7,
    ): Flow<List<GameHistory>>

    /**
     * Get a specific game by ID
     */
    fun getGameById(gameId: String): Flow<GameHistory?>

    /**
     * Get total number of games for a user
     */
    fun getGameCount(userId: String): Flow<Int>

    /**
     * Get number of perfect games (3 stars) for a user
     */
    fun getPerfectGameCount(userId: String): Flow<Int>

    /**
     * Get total score across all games for a user
     */
    fun getTotalScore(userId: String): Flow<Int>

    /**
     * Delete old game history records
     *
     * @param beforeTimestamp Delete records older than this timestamp
     * @return Number of records deleted
     */
    suspend fun deleteOldHistory(beforeTimestamp: Long): Result<Int>
}
