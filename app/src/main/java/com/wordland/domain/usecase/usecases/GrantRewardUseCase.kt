package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.achievement.AchievementReward
import javax.inject.Inject

/**
 * Grant achievement rewards to user
 *
 * This use case handles:
 * - Adding stars to user balance
 * - Unlocking titles, badges, pets
 * - Logging reward grants for analytics
 */
class GrantRewardUseCase
    @Inject
    constructor(
        // Will inject UserRepository when implemented for star balance
    ) {
        suspend operator fun invoke(
            userId: String,
            reward: AchievementReward,
        ): GrantResult {
            return try {
                when (reward) {
                    is AchievementReward.Stars -> {
                        // Add stars to user balance
                        // TODO: Implement when UserRepository is ready
                        GrantResult.Success(
                            rewardType = "stars",
                            amount = reward.amount,
                        )
                    }
                    is AchievementReward.Title -> {
                        // Grant title to user
                        // TODO: Implement when UserProfile is ready
                        GrantResult.Success(
                            rewardType = "title",
                            displayName = reward.displayName,
                        )
                    }
                    is AchievementReward.Badge -> {
                        // Grant badge to user
                        // TODO: Implement when UserProfile is ready
                        GrantResult.Success(
                            rewardType = "badge",
                            displayName = reward.displayName,
                        )
                    }
                    is AchievementReward.PetUnlock -> {
                        // Unlock pet for user
                        // TODO: Implement when PetRepository is enhanced
                        GrantResult.Success(
                            rewardType = "pet",
                            displayName = reward.petName,
                        )
                    }
                    is AchievementReward.Multiple -> {
                        // Grant all rewards
                        val results =
                            reward.rewards.map { r ->
                                invoke(userId, r)
                            }
                        if (results.all { it is GrantResult.Success }) {
                            GrantResult.Success(
                                rewardType = "multiple",
                                displayName = "${results.size} rewards",
                            )
                        } else {
                            GrantResult.Partial(
                                granted = results.count { it is GrantResult.Success },
                                total = results.size,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                GrantResult.Error(e)
            }
        }
    }

/**
 * Result of granting a reward
 */
sealed class GrantResult {
    data class Success(
        val rewardType: String,
        val amount: Int = 0,
        val displayName: String = "",
    ) : GrantResult()

    data class Partial(
        val granted: Int,
        val total: Int,
    ) : GrantResult()

    data class Error(val exception: Throwable) : GrantResult()
}
