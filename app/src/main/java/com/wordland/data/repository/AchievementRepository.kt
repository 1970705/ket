package com.wordland.data.repository

import com.wordland.data.dao.AchievementDao
import com.wordland.data.entity.UserAchievementEntity
import com.wordland.data.entity.toDomainModel
import com.wordland.data.entity.toEntity
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.domain.model.achievement.AchievementWithProgress
import com.wordland.domain.model.achievement.CompletionStats
import com.wordland.domain.model.achievement.UserAchievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for achievement operations
 * Bridges data layer with domain layer
 */
class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val progressRepository: ProgressRepository,
) {
    // === Achievement definitions ===

    /**
     * Get all achievement definitions
     */
    suspend fun getAllAchievements(): List<Achievement> {
        return achievementDao.getAllAchievements().map { it.toDomainModel() }
    }

    /**
     * Get achievement by ID
     */
    suspend fun getAchievement(achievementId: String): Achievement? {
        return achievementDao.getAchievement(achievementId)?.toDomainModel()
    }

    /**
     * Get achievements by category
     */
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return achievementDao.getAchievementsByCategory(category.name)
            .map { it.toDomainModel() }
    }

    /**
     * Insert achievement definitions (from seeder)
     */
    suspend fun insertAchievements(achievements: List<Achievement>) {
        achievementDao.insertAchievements(achievements.map { it.toEntity() })
    }

    // === User progress ===

    /**
     * Get user's achievement progress
     */
    suspend fun getUserAchievements(userId: String): List<UserAchievement> {
        return achievementDao.getUserAchievements(userId).map { it.toDomainModel() }
    }

    /**
     * Get user's achievement progress as Flow
     */
    fun getUserAchievementsFlow(userId: String): Flow<List<UserAchievement>> {
        return achievementDao.getUserAchievementsFlow(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    /**
     * Get user's progress for a specific achievement
     */
    suspend fun getUserAchievement(
        userId: String,
        achievementId: String,
    ): UserAchievement? {
        return achievementDao.getUserAchievement(userId, achievementId)?.toDomainModel()
    }

    /**
     * Get user's progress for a specific achievement as Flow
     */
    fun getUserAchievementFlow(
        userId: String,
        achievementId: String,
    ): Flow<UserAchievement?> {
        return achievementDao.getUserAchievementFlow(userId, achievementId)
            .map { it?.toDomainModel() }
    }

    // === Unlocked achievements ===

    /**
     * Get unlocked achievements
     */
    suspend fun getUnlockedAchievements(userId: String): List<Achievement> {
        return achievementDao.getUnlockedAchievements(userId).map { it.toDomainModel() }
    }

    /**
     * Get unlocked achievements as Flow
     */
    fun getUnlockedAchievementsFlow(userId: String): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievementsFlow(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    /**
     * Get all achievements with user progress (grouped for UI)
     */
    suspend fun getAllAchievementsWithProgress(userId: String): AchievementGalleryData {
        val allAchievements = getAllAchievements()
        val userProgressMap = getUserAchievements(userId).associateBy { it.achievementId }

        val unlocked = mutableListOf<Achievement>()
        val inProgress = mutableListOf<AchievementWithProgress>()
        val locked = mutableListOf<Achievement>()

        for (achievement in allAchievements) {
            val userProgress = userProgressMap[achievement.id]
            when {
                userProgress?.isUnlocked == true -> unlocked.add(achievement)
                userProgress != null ->
                    inProgress.add(
                        AchievementWithProgress(
                            achievement = achievement,
                            progress = userProgress.progress,
                            target = userProgress.target,
                            isUnlocked = false,
                        ),
                    )
                else -> locked.add(achievement)
            }
        }

        return AchievementGalleryData(
            unlocked = unlocked,
            inProgress = inProgress,
            locked = locked,
            completionStats = getCompletionStats(userId),
        )
    }

    // === Progress operations ===

    /**
     * Update progress for an achievement
     */
    suspend fun updateProgress(
        userId: String,
        achievementId: String,
        progress: Int,
    ) {
        val existing = achievementDao.getUserAchievement(userId, achievementId)

        if (existing != null) {
            achievementDao.updateProgress(userId, achievementId, progress)
        } else {
            val achievementEntity = achievementDao.getAchievement(achievementId)
            achievementDao.insertOrUpdateProgress(
                UserAchievementEntity(
                    userId = userId,
                    achievementId = achievementId,
                    isUnlocked = false,
                    progress = progress,
                    target =
                        achievementEntity?.let {
                            it.toDomainModel().requirement.targetValue
                        } ?: 1,
                    lastUpdated = System.currentTimeMillis(),
                ),
            )
        }
    }

    /**
     * Unlock an achievement
     */
    suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        finalProgress: Int,
    ) {
        achievementDao.unlockWithProgress(userId, achievementId, finalProgress)
    }

    /**
     * Check if achievement is unlocked
     */
    suspend fun isUnlocked(
        userId: String,
        achievementId: String,
    ): Boolean {
        return achievementDao.isUnlocked(userId, achievementId) > 0
    }

    // === Stats ===

    /**
     * Get achievement completion stats
     */
    suspend fun getCompletionStats(userId: String): CompletionStats {
        val unlocked = achievementDao.getUnlockedCount(userId)
        val total = achievementDao.getTotalAchievementCount()
        return CompletionStats(
            unlocked = unlocked,
            total = total,
            percentage = if (total > 0) unlocked.toFloat() / total else 0f,
        )
    }

    // === Progress queries for achievement tracking ===

    /**
     * Get mastered word count (for achievements)
     */
    suspend fun getMasteredWordCount(
        userId: String,
        minStrength: Int = 80,
    ): Int {
        return progressRepository.getMasteredWordCount(userId)
    }

    /**
     * Get max strength word count
     */
    suspend fun getMaxStrengthWordCount(
        userId: String,
        strength: Int = 100,
    ): Int {
        // For now, return mastered count
        return progressRepository.getMasteredWordCount(userId)
    }

    /**
     * Get completed level count
     */
    suspend fun getCompletedLevelCount(
        userId: String,
        minStars: Int = 1,
    ): Int {
        return progressRepository.getAllLevelProgress(userId)
            .count { it.stars >= minStars && it.status.name == "COMPLETED" }
    }

    /**
     * Get completed level count in island
     */
    suspend fun getCompletedLevelCountInIsland(
        userId: String,
        islandId: String,
    ): Int {
        return progressRepository.getLevelsByIsland(userId, islandId)
            .count { it.status.name == "COMPLETED" }
    }

    /**
     * Get unlocked island count
     */
    suspend fun getUnlockedIslandCount(userId: String): Int {
        return progressRepository.getLevelsByIsland(userId, "look_island")
            .count { it.status != LevelStatus.LOCKED }
    }

    /**
     * Get user streak data
     */
    suspend fun getUserStreak(userId: String): StreakData {
        // For now, return default streak data
        // This would be enhanced with proper streak tracking
        return StreakData(
            currentStreak = 1,
            longestStreak = 1,
            lastActiveDate = System.currentTimeMillis(),
            freezeDaysUsed = 0,
            freezeDaysAvailable = 2,
        )
    }
}

/**
 * Achievement gallery data for UI
 */
data class AchievementGalleryData(
    val unlocked: List<Achievement>,
    val inProgress: List<AchievementWithProgress>,
    val locked: List<Achievement>,
    val completionStats: CompletionStats,
)

/**
 * Streak data for achievements
 */
data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val freezeDaysUsed: Int,
    val freezeDaysAvailable: Int = 2,
)
