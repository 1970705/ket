package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.AchievementGalleryData
import com.wordland.data.repository.AchievementRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.domain.model.achievement.CompletionStats

/**
 * Get achievements for display
 * Returns grouped achievements with progress
 *
 * This use case:
 * - Fetches all achievements with user progress
 * - Groups them by status (unlocked, in progress, locked)
 * - Includes completion statistics
 */
class GetAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
) {
    suspend operator fun invoke(
        userId: String,
        category: AchievementCategory? = null,
    ): Result<AchievementGalleryData> {
        return try {
            val data =
                if (category != null) {
                    // Filter by category
                    val allData = achievementRepository.getAllAchievementsWithProgress(userId)
                    allData.copy(
                        unlocked = allData.unlocked.filter { it.category == category },
                        inProgress = allData.inProgress.filter { it.achievement.category == category },
                        locked = allData.locked.filter { it.category == category },
                    )
                } else {
                    achievementRepository.getAllAchievementsWithProgress(userId)
                }

            Result.Success(data)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get only unlocked achievements
     */
    suspend fun getUnlockedOnly(userId: String): Result<List<Achievement>> {
        return try {
            val achievements = achievementRepository.getUnlockedAchievements(userId)
            Result.Success(achievements)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get completion statistics
     */
    suspend fun getCompletionStats(userId: String): Result<CompletionStats> {
        return try {
            val stats = achievementRepository.getCompletionStats(userId)
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Get achievements with progress as Flow for reactive updates
     */
    fun getAchievementsFlow(userId: String) = achievementRepository.getUserAchievementsFlow(userId)
}
