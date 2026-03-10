package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.AchievementRepository
import com.wordland.domain.achievement.AchievementTracker
import com.wordland.domain.model.Result
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.GameEvent

/**
 * Check achievements after a game event
 * Returns list of newly unlocked achievements
 *
 * This use case coordinates:
 * - AchievementTracker to evaluate progress
 * - GrantRewardUseCase to award rewards
 * - Error handling for the entire flow
 */
class CheckAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
    private val achievementTracker: AchievementTracker,
    private val grantRewardUseCase: GrantRewardUseCase,
) {
    suspend operator fun invoke(
        userId: String,
        event: GameEvent,
    ): Result<List<Achievement>> {
        return try {
            val newlyUnlocked =
                achievementTracker.checkAchievements(
                    userId = userId,
                    event = event,
                    achievementRepository = achievementRepository,
                )

            // Grant rewards for newly unlocked achievements
            for (achievement in newlyUnlocked) {
                grantRewardUseCase(userId, achievement.reward)
            }

            Result.Success(newlyUnlocked)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
