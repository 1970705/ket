package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wordland.data.entity.GameHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for GameHistory operations
 *
 * Provides database operations for game history records
 */
@Dao
interface GameHistoryDao {
    /**
     * Insert a new game history record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(history: GameHistoryEntity)

    /**
     * Insert multiple game history records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistoryList(historyList: List<GameHistoryEntity>)

    /**
     * Get game history for a user with pagination
     *
     * @param userId User identifier
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return Flow of game history list
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE userId = :userId
        ORDER BY startTime DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun getGameHistory(
        userId: String,
        limit: Int = 20,
        offset: Int = 0,
    ): Flow<List<GameHistoryEntity>>

    /**
     * Get game history for a specific level
     *
     * @param userId User identifier
     * @param levelId Level identifier
     * @param limit Maximum number of records to return
     * @return Flow of game history list
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE userId = :userId AND levelId = :levelId
        ORDER BY startTime DESC
        LIMIT :limit
        """,
    )
    fun getGameHistoryByLevel(
        userId: String,
        levelId: String,
        limit: Int = 20,
    ): Flow<List<GameHistoryEntity>>

    /**
     * Get game history for a specific game mode
     *
     * @param userId User identifier
     * @param gameMode Game mode (SPELL_BATTLE, QUICK_JUDGE, etc.)
     * @param limit Maximum number of records to return
     * @return Flow of game history list
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE userId = :userId AND gameMode = :gameMode
        ORDER BY startTime DESC
        LIMIT :limit
        """,
    )
    fun getGameHistoryByMode(
        userId: String,
        gameMode: String,
        limit: Int = 20,
    ): Flow<List<GameHistoryEntity>>

    /**
     * Get recent games within a date range
     *
     * @param userId User identifier
     * @param startTime Start timestamp (ms)
     * @param endTime End timestamp (ms)
     * @param limit Maximum number of records to return
     * @return Flow of game history list
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE userId = :userId
        AND startTime >= :startTime
        AND startTime <= :endTime
        ORDER BY startTime DESC
        LIMIT :limit
        """,
    )
    fun getGameHistoryByDateRange(
        userId: String,
        startTime: Long,
        endTime: Long,
        limit: Int = 100,
    ): Flow<List<GameHistoryEntity>>

    /**
     * Get games from the last N days
     *
     * @param userId User identifier
     * @param daysAgo Number of days to look back
     * @return Flow of game history list
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE userId = :userId
        AND startTime >= :startTime
        ORDER BY startTime DESC
        """,
    )
    fun getRecentGames(
        userId: String,
        startTime: Long, // calculated as System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000)
    ): Flow<List<GameHistoryEntity>>

    /**
     * Get a specific game history record
     *
     * @param gameId Game identifier
     * @return Flow of game history or null if not found
     */
    @Query(
        """
        SELECT * FROM game_history
        WHERE gameId = :gameId
        LIMIT 1
        """,
    )
    fun getGameById(gameId: String): Flow<GameHistoryEntity?>

    /**
     * Get count of games for a user
     *
     * @param userId User identifier
     * @return Number of games played
     */
    @Query(
        """
        SELECT COUNT(*) FROM game_history
        WHERE userId = :userId
        """,
    )
    fun getGameCount(userId: String): Flow<Int>

    /**
     * Get count of perfect games (3 stars) for a user
     *
     * @param userId User identifier
     * @return Number of perfect games
     */
    @Query(
        """
        SELECT COUNT(*) FROM game_history
        WHERE userId = :userId AND stars = 3
        """,
    )
    fun getPerfectGameCount(userId: String): Flow<Int>

    /**
     * Get total score for a user
     *
     * @param userId User identifier
     * @return Total score across all games
     */
    @Query(
        """
        SELECT COALESCE(SUM(score), 0) FROM game_history
        WHERE userId = :userId
        """,
    )
    fun getTotalScore(userId: String): Flow<Int>

    /**
     * Delete old game history (for cleanup)
     *
     * @param beforeTimestamp Delete records older than this timestamp
     * @return Number of records deleted
     */
    @Query(
        """
        DELETE FROM game_history
        WHERE startTime < :beforeTimestamp
        """,
    )
    suspend fun deleteOldHistory(beforeTimestamp: Long): Int

    /**
     * Delete all game history for a user
     *
     * @param userId User identifier
     * @return Number of records deleted
     */
    @Query(
        """
        DELETE FROM game_history
        WHERE userId = :userId
        """,
    )
    suspend fun deleteAllHistoryForUser(userId: String): Int
}
