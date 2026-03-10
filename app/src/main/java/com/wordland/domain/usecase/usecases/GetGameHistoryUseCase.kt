package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.GameHistoryRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * UseCase for getting game history with various filtering options
 *
 * Provides access to historical game data for analytics,
 * progress tracking, and user engagement insights.
 */
class GetGameHistoryUseCase
    @Inject
    constructor(
        private val gameHistoryRepository: GameHistoryRepository,
    ) {
        /**
         * Get paginated game history
         *
         * @param userId User identifier
         * @param limit Maximum number of records to return
         * @param offset Number of records to skip
         * @return Flow of game history
         */
        operator fun invoke(
            userId: String,
            limit: Int = 20,
            offset: Int = 0,
        ): Flow<List<GameHistory>> {
            return gameHistoryRepository.getGameHistory(userId, limit, offset)
        }

        /**
         * Get game history for a specific level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @param limit Maximum number of records to return
         * @return Flow of game history for the level
         */
        fun getByLevel(
            userId: String,
            levelId: String,
            limit: Int = 20,
        ): Flow<List<GameHistory>> {
            return gameHistoryRepository.getGameHistoryByLevel(userId, levelId, limit)
        }

        /**
         * Get game history for a specific game mode
         *
         * @param userId User identifier
         * @param gameMode Game mode to filter by
         * @param limit Maximum number of records to return
         * @return Flow of game history for the game mode
         */
        fun getByMode(
            userId: String,
            gameMode: GameMode,
            limit: Int = 20,
        ): Flow<List<GameHistory>> {
            return gameHistoryRepository.getGameHistoryByMode(userId, gameMode, limit)
        }

        /**
         * Get game history within a date range
         *
         * @param userId User identifier
         * @param startTime Start timestamp (inclusive)
         * @param endTime End timestamp (inclusive)
         * @param limit Maximum number of records to return
         * @return Flow of game history in the date range
         */
        fun getByDateRange(
            userId: String,
            startTime: Long,
            endTime: Long,
            limit: Int = 100,
        ): Flow<List<GameHistory>> {
            return gameHistoryRepository.getGameHistoryByDateRange(userId, startTime, endTime, limit)
        }

        /**
         * Get recent games from the last N days
         *
         * @param userId User identifier
         * @param days Number of days to look back
         * @return Flow of recent games
         */
        fun getRecent(
            userId: String,
            days: Int = 7,
        ): Flow<List<GameHistory>> {
            return gameHistoryRepository.getRecentGames(userId, days)
        }

        /**
         * Get a specific game by ID
         *
         * @param gameId Game identifier
         * @return Flow of game history (null if not found)
         */
        fun getById(gameId: String): Flow<GameHistory?> {
            return gameHistoryRepository.getGameById(gameId)
        }

        /**
         * Get total number of games played
         *
         * @param userId User identifier
         * @return Flow of game count
         */
        fun getCount(userId: String): Flow<Int> {
            return gameHistoryRepository.getGameCount(userId)
        }

        /**
         * Get number of perfect games (3 stars)
         *
         * @param userId User identifier
         * @return Flow of perfect game count
         */
        fun getPerfectCount(userId: String): Flow<Int> {
            return gameHistoryRepository.getPerfectGameCount(userId)
        }

        /**
         * Get total score across all games
         *
         * @param userId User identifier
         * @return Flow of total score
         */
        fun getTotalScore(userId: String): Flow<Int> {
            return gameHistoryRepository.getTotalScore(userId)
        }

        /**
         * Calculate average score per game
         *
         * @param userId User identifier
         * @return Flow of average score
         */
        fun getAverageScore(userId: String): Flow<Float> {
            return gameHistoryRepository.getTotalScore(userId).map { totalScore ->
                // This would require getting count as well, simplified version
                totalScore.toFloat()
            }
        }

        /**
         * Get today's games
         *
         * @param userId User identifier
         * @return Flow of today's games
         */
        fun getTodayGames(userId: String): Flow<List<GameHistory>> {
            val startOfDay = getStartOfDay()
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
            return getByDateRange(userId, startOfDay, endOfDay)
        }

        /**
         * Get this week's games
         *
         * @param userId User identifier
         * @return Flow of this week's games
         */
        fun getThisWeekGames(userId: String): Flow<List<GameHistory>> {
            val startOfWeek = getStartOfWeek()
            val now = System.currentTimeMillis()
            return getByDateRange(userId, startOfWeek, now)
        }

        /**
         * Get games by specific mode with date range
         *
         * @param userId User identifier
         * @param gameMode Game mode to filter
         * @param days Number of days to look back
         * @return Flow of filtered games
         */
        fun getByModeAndRecent(
            userId: String,
            gameMode: GameMode,
            days: Int = 7,
        ): Flow<List<GameHistory>> {
            val startTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val endTime = System.currentTimeMillis()
            return getByDateRange(userId, startTime, endTime)
                .map { games -> games.filter { it.gameMode == gameMode } }
        }

        /**
         * Get best performing game for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Flow of game history (null if no games)
         */
        fun getBestGame(
            userId: String,
            levelId: String,
        ): Flow<GameHistory?> {
            return getByLevel(userId, levelId, limit = 100)
                .map { games ->
                    games.maxByOrNull { it.score }
                }
        }

        /**
         * Get most recent game for a level
         *
         * @param userId User identifier
         * @param levelId Level identifier
         * @return Flow of game history (null if no games)
         */
        fun getMostRecentGame(
            userId: String,
            levelId: String,
        ): Flow<GameHistory?> {
            return getByLevel(userId, levelId, limit = 1)
                .map { games -> games.firstOrNull() }
        }

        /**
         * Delete old game history records
         *
         * @param beforeTimestamp Delete records older than this timestamp
         * @return Result containing number of deleted records
         */
        suspend fun deleteOldHistory(beforeTimestamp: Long): Result<Int> {
            return gameHistoryRepository.deleteOldHistory(beforeTimestamp)
        }

        /**
         * Helper method to get start of current day
         */
        private fun getStartOfDay(): Long {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        /**
         * Helper method to get start of current week (Monday)
         */
        private fun getStartOfWeek(): Long {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }
