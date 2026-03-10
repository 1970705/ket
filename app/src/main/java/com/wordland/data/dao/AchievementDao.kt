package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wordland.data.entity.AchievementEntity
import com.wordland.data.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for achievement operations
 */
@Dao
interface AchievementDao {
    // === Achievement definitions ===

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE category = :category")
    suspend fun getAchievementsByCategory(category: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    // === User progress ===

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    suspend fun getUserAchievements(userId: String): List<UserAchievementEntity>

    @Query("SELECT * FROM user_achievements WHERE userId = :userId AND achievementId = :achievementId")
    suspend fun getUserAchievement(
        userId: String,
        achievementId: String,
    ): UserAchievementEntity?

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    fun getUserAchievementsFlow(userId: String): Flow<List<UserAchievementEntity>>

    @Query("SELECT * FROM user_achievements WHERE userId = :userId AND achievementId = :achievementId")
    fun getUserAchievementFlow(
        userId: String,
        achievementId: String,
    ): Flow<UserAchievementEntity?>

    // === Unlocked achievements ===

    @Query(
        """
        SELECT achievements.* FROM achievements
        INNER JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        WHERE user_achievements.userId = :userId
        AND user_achievements.isUnlocked = 1
        ORDER BY user_achievements.unlockedAt DESC
    """,
    )
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity>

    @Query(
        """
        SELECT achievements.* FROM achievements
        INNER JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        WHERE user_achievements.userId = :userId
        AND user_achievements.isUnlocked = 1
        ORDER BY user_achievements.unlockedAt DESC
    """,
    )
    fun getUnlockedAchievementsFlow(userId: String): Flow<List<AchievementEntity>>

    // === Progress operations ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserAchievementEntity)

    @Query(
        """
        UPDATE user_achievements
        SET isUnlocked = 1,
            unlockedAt = :timestamp,
            lastUpdated = :timestamp
        WHERE userId = :userId
        AND achievementId = :achievementId
    """,
    )
    suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        timestamp: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE user_achievements
        SET progress = :progress,
            lastUpdated = :timestamp
        WHERE userId = :userId
        AND achievementId = :achievementId
    """,
    )
    suspend fun updateProgress(
        userId: String,
        achievementId: String,
        progress: Int,
        timestamp: Long = System.currentTimeMillis(),
    )

    // === Batch operations ===

    @Transaction
    suspend fun unlockWithProgress(
        userId: String,
        achievementId: String,
        finalProgress: Int,
    ) {
        insertOrUpdateProgress(
            UserAchievementEntity(
                userId = userId,
                achievementId = achievementId,
                isUnlocked = true,
                progress = finalProgress,
                target = finalProgress,
                unlockedAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis(),
            ),
        )
    }

    // === Stats queries ===

    @Query("SELECT COUNT(*) FROM user_achievements WHERE userId = :userId AND isUnlocked = 1")
    suspend fun getUnlockedCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM user_achievements
        WHERE userId = :userId
        AND achievementId = :achievementId
        AND isUnlocked = 1
    """,
    )
    suspend fun isUnlocked(
        userId: String,
        achievementId: String,
    ): Int
}
