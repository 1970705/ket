package com.wordland.data.repository

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.domain.constants.DomainConstants
import com.wordland.domain.model.IslandMastery
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for island mastery operations
 */
interface IslandMasteryRepository {
    suspend fun getIslandMastery(
        userId: String,
        islandId: String,
    ): IslandMastery?

    fun getIslandMasteryFlow(
        userId: String,
        islandId: String,
    ): Flow<IslandMastery?>

    suspend fun getAllIslandMastery(userId: String): List<IslandMastery>

    suspend fun getUnlockedIslands(userId: String): List<IslandMastery>

    suspend fun getMasteredIslands(
        userId: String,
        minPercentage: Double,
    ): List<IslandMastery>

    suspend fun insertIslandMastery(mastery: IslandMastery)

    suspend fun updateIslandMastery(mastery: IslandMastery)

    suspend fun calculateMastery(
        userId: String,
        islandId: String,
        masteredWords: Int,
        completedLevels: Int,
        crossSceneScore: Double,
    ): Boolean

    suspend fun getUnlockedIslandCount(userId: String): Int

    suspend fun getMasteredIslandCount(userId: String): Int
}

/**
 * Implementation of IslandMasteryRepository using Room database
 */
class IslandMasteryRepositoryImpl(
    private val islandMasteryDao: IslandMasteryDao,
    private val progressDao: com.wordland.data.dao.ProgressDao,
) : IslandMasteryRepository {
    override suspend fun getIslandMastery(
        userId: String,
        islandId: String,
    ): IslandMastery? {
        return islandMasteryDao.getIslandMastery(userId, islandId)
    }

    override fun getIslandMasteryFlow(
        userId: String,
        islandId: String,
    ): Flow<IslandMastery?> {
        return islandMasteryDao.getIslandMasteryFlow(userId, islandId)
    }

    override suspend fun getAllIslandMastery(userId: String): List<IslandMastery> {
        return islandMasteryDao.getAllIslandMastery(userId)
    }

    override suspend fun getUnlockedIslands(userId: String): List<IslandMastery> {
        return islandMasteryDao.getUnlockedIslands(userId)
    }

    override suspend fun getMasteredIslands(
        userId: String,
        minPercentage: Double,
    ): List<IslandMastery> {
        return islandMasteryDao.getMasteredIslands(userId, minPercentage)
    }

    override suspend fun insertIslandMastery(mastery: IslandMastery) {
        islandMasteryDao.insertIslandMastery(mastery)
    }

    override suspend fun updateIslandMastery(mastery: IslandMastery) {
        islandMasteryDao.updateIslandMastery(mastery)
    }

    override suspend fun calculateMastery(
        userId: String,
        islandId: String,
        masteredWords: Int,
        completedLevels: Int,
        crossSceneScore: Double,
    ): Boolean {
        val existing = getIslandMastery(userId, islandId)
        val now = System.currentTimeMillis()

        if (existing == null) {
            // Create new mastery record
            val mastery =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = masteredWords, // Initial value, will update
                    masteredWords = masteredWords,
                    totalLevels = completedLevels, // Initial value
                    completedLevels = completedLevels,
                    crossSceneScore = crossSceneScore,
                    unlockedAt = now,
                    masteredAt = null,
                    createdAt = now,
                    updatedAt = now,
                )
            insertIslandMastery(mastery)
            return false
        }

        // Calculate mastery percentage
        val wordMastery = (masteredWords.toDouble() / existing.totalWords) * 0.7
        val crossSceneMastery = crossSceneScore * 0.3
        val masteryPercentage = (wordMastery + crossSceneMastery) * 100

        // Check if should unlock next island
        val isUnlocked = masteryPercentage >= DomainConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND
        val wasUnlocked = existing.isNextIslandUnlocked

        // Check if newly mastered
        val masteredAt = if (masteryPercentage >= 100) now else existing.masteredAt

        // Update database
        islandMasteryDao.updateMasteryProgress(
            userId = userId,
            islandId = islandId,
            masteredWords = masteredWords,
            completedLevels = completedLevels,
            crossSceneScore = crossSceneScore,
            masteryPercentage = masteryPercentage,
            isUnlocked = isUnlocked,
            masteredAt = masteredAt ?: 0L,
            updatedAt = now,
        )

        // Return true if newly unlocked
        return isUnlocked && !wasUnlocked
    }

    override suspend fun getUnlockedIslandCount(userId: String): Int {
        return islandMasteryDao.getUnlockedIslandCount(userId)
    }

    override suspend fun getMasteredIslandCount(userId: String): Int {
        return islandMasteryDao.getMasteredIslandCount(userId)
    }
}
