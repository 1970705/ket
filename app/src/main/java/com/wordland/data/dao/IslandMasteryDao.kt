package com.wordland.data.dao

import androidx.room.*
import com.wordland.domain.model.IslandMastery
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for island mastery tracking
 */
@Dao
interface IslandMasteryDao {
    @Query("SELECT * FROM island_mastery WHERE userId = :userId AND islandId = :islandId")
    suspend fun getIslandMastery(
        userId: String,
        islandId: String,
    ): IslandMastery?

    @Query("SELECT * FROM island_mastery WHERE userId = :userId AND islandId = :islandId")
    fun getIslandMasteryFlow(
        userId: String,
        islandId: String,
    ): Flow<IslandMastery?>

    @Query("SELECT * FROM island_mastery WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getAllIslandMastery(userId: String): List<IslandMastery>

    @Query(
        """
        SELECT * FROM island_mastery
        WHERE userId = :userId
        AND isNextIslandUnlocked = 1
        ORDER BY createdAt ASC
    """,
    )
    suspend fun getUnlockedIslands(userId: String): List<IslandMastery>

    @Query(
        """
        SELECT * FROM island_mastery
        WHERE userId = :userId
        AND masteryPercentage >= :minPercentage
        ORDER BY masteryPercentage DESC
    """,
    )
    suspend fun getMasteredIslands(
        userId: String,
        minPercentage: Double = 60.0,
    ): List<IslandMastery>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIslandMastery(mastery: IslandMastery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIslandMasteryList(masteryList: List<IslandMastery>)

    @Update
    suspend fun updateIslandMastery(mastery: IslandMastery)

    @Query(
        """
        UPDATE island_mastery
        SET masteredWords = :masteredWords,
            completedLevels = :completedLevels,
            crossSceneScore = :crossSceneScore,
            masteryPercentage = :masteryPercentage,
            isNextIslandUnlocked = :isUnlocked,
            masteredAt = CASE WHEN :masteredAt > 0 THEN :masteredAt ELSE masteredAt END,
            updatedAt = :updatedAt
        WHERE userId = :userId AND islandId = :islandId
    """,
    )
    suspend fun updateMasteryProgress(
        userId: String,
        islandId: String,
        masteredWords: Int,
        completedLevels: Int,
        crossSceneScore: Double,
        masteryPercentage: Double,
        isUnlocked: Boolean,
        masteredAt: Long,
        updatedAt: Long,
    )

    @Delete
    suspend fun deleteIslandMastery(mastery: IslandMastery)

    @Query(
        """
        SELECT COUNT(*)
        FROM island_mastery
        WHERE userId = :userId AND isNextIslandUnlocked = 1
    """,
    )
    suspend fun getUnlockedIslandCount(userId: String): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM island_mastery
        WHERE userId = :userId AND masteredAt IS NOT NULL
    """,
    )
    suspend fun getMasteredIslandCount(userId: String): Int
}
