package com.wordland.data.dao

import androidx.room.*
import com.wordland.domain.model.BehaviorTracking

/**
 * Data Access Object for behavior tracking
 */
@Dao
interface TrackingDao {
    @Query("SELECT * FROM behavior_tracking WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTracking(
        userId: String,
        limit: Int = 100,
    ): List<BehaviorTracking>

    @Query(
        """
        SELECT * FROM behavior_tracking
        WHERE userId = :userId
        AND wordId = :wordId
        ORDER BY timestamp DESC
        LIMIT :limit
    """,
    )
    suspend fun getTrackingByWord(
        userId: String,
        wordId: String,
        limit: Int = 20,
    ): List<BehaviorTracking>

    @Query(
        """
        SELECT * FROM behavior_tracking
        WHERE userId = :userId
        AND sceneId = :sceneId
        AND action = :action
        ORDER BY timestamp DESC
    """,
    )
    suspend fun getCrossSceneAttempts(
        userId: String,
        sceneId: String,
        action: String = "cross_scene_answer",
    ): List<BehaviorTracking>

    @Query(
        """
        SELECT * FROM behavior_tracking
        WHERE userId = :userId
        AND timestamp >= :startTime
        AND timestamp <= :endTime
        ORDER BY timestamp ASC
    """,
    )
    suspend fun getTrackingByTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<BehaviorTracking>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracking(tracking: BehaviorTracking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingList(trackingList: List<BehaviorTracking>)

    @Update
    suspend fun updateTracking(tracking: BehaviorTracking)

    @Delete
    suspend fun deleteTracking(tracking: BehaviorTracking)

    @Query("DELETE FROM behavior_tracking WHERE timestamp < :beforeTime")
    suspend fun deleteOldTracking(beforeTime: Long): Int

    @Query("DELETE FROM behavior_tracking WHERE userId = :userId")
    suspend fun deleteAllTrackingForUser(userId: String): Int

    // === Statistics ===

    @Query(
        """
        SELECT COUNT(*)
        FROM behavior_tracking
        WHERE userId = :userId
        AND action = 'cross_scene_answer'
        AND isCorrect = 1
    """,
    )
    suspend fun getCorrectCrossSceneCount(userId: String): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM behavior_tracking
        WHERE userId = :userId
        AND action = 'cross_scene_answer'
    """,
    )
    suspend fun getTotalCrossSceneCount(userId: String): Int

    @Query(
        """
        SELECT AVG(responseTime)
        FROM behavior_tracking
        WHERE userId = :userId
        AND responseTime IS NOT NULL
    """,
    )
    suspend fun getAverageResponseTime(userId: String): Double?

    @Query(
        """
        SELECT AVG(responseTime)
        FROM behavior_tracking
        WHERE userId = :userId
        AND isCorrect = 1
        AND responseTime IS NOT NULL
    """,
    )
    suspend fun getAverageCorrectResponseTime(userId: String): Double?
}
