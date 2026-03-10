package com.wordland.domain.achievement

import com.wordland.data.repository.AchievementRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.domain.model.achievement.AchievementRequirement
import com.wordland.domain.model.achievement.AchievementWithProgress
import com.wordland.domain.model.achievement.GameEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Core achievement tracking logic
 * Evaluates achievements against game events
 *
 * This class contains the business logic for:
 * - Determining which achievements are affected by game events
 * - Calculating progress updates
 * - Checking if unlock conditions are met
 */
class AchievementTracker(
    private val progressRepository: ProgressRepository,
) {
    /**
     * Check all achievements for a game event
     * @return List of newly unlocked achievements
     */
    suspend fun checkAchievements(
        userId: String,
        event: GameEvent,
        achievementRepository: AchievementRepository,
    ): List<Achievement> =
        withContext(Dispatchers.Default) {
            val newlyUnlocked = mutableListOf<Achievement>()

            // Get locked/in-progress achievements
            val allData = achievementRepository.getAllAchievementsWithProgress(userId)
            val lockedAchievements =
                allData.inProgress +
                    allData.locked.map { locked ->
                        AchievementWithProgress(
                            achievement = locked,
                            progress = 0,
                            target = locked.requirement.targetValue,
                            isUnlocked = false,
                        )
                    }

            // Filter achievements relevant to this event
            val relevantAchievements =
                lockedAchievements.filter { achievementWithProgress ->
                    isEventRelevant(event, achievementWithProgress.achievement)
                }

            // Check each relevant achievement
            for (achievementWithProgress in relevantAchievements) {
                val achievement = achievementWithProgress.achievement
                val currentProgress = achievementWithProgress.progress

                // Calculate new progress
                val newProgress =
                    calculateProgress(
                        event = event,
                        achievement = achievement,
                        currentProgress = currentProgress,
                        progressRepository = progressRepository,
                        achievementRepository = achievementRepository,
                    )

                // Update progress in database
                achievementRepository.updateProgress(userId, achievement.id, newProgress)

                // Check if unlocked
                if (newProgress >= achievement.requirement.targetValue) {
                    achievementRepository.unlockAchievement(userId, achievement.id, newProgress)
                    newlyUnlocked.add(achievement)
                }
            }

            newlyUnlocked
        }

    /**
     * Check if an event is relevant to an achievement
     * Optimizes by skipping unrelated achievements
     */
    private fun isEventRelevant(
        event: GameEvent,
        achievement: Achievement,
    ): Boolean {
        return when (event) {
            is GameEvent.WordMastered -> {
                achievement.category == AchievementCategory.PROGRESS ||
                    achievement.category == AchievementCategory.PERFORMANCE ||
                    achievement.requirement is AchievementRequirement.MasterWords ||
                    achievement.requirement is AchievementRequirement.MaxMemoryStrength
            }
            is GameEvent.LevelComplete -> {
                achievement.requirement is AchievementRequirement.CompleteLevels ||
                    achievement.requirement is AchievementRequirement.PerfectLevel ||
                    achievement.requirement is AchievementRequirement.NoHintsLevel ||
                    achievement.requirement is AchievementRequirement.CompleteIsland
            }
            is GameEvent.ComboAchieved -> {
                achievement.requirement is AchievementRequirement.ComboMilestone
            }
            is GameEvent.StreakUpdate -> {
                achievement.requirement is AchievementRequirement.StreakDays
            }
            is GameEvent.IslandComplete -> {
                achievement.requirement is AchievementRequirement.CompleteIsland ||
                    achievement.requirement is AchievementRequirement.UnlockIslands
            }
            is GameEvent.AnswerSubmitted -> {
                // Used for accumulator tracking only
                false
            }
        }
    }

    /**
     * Calculate new progress for an achievement based on event
     */
    private suspend fun calculateProgress(
        event: GameEvent,
        achievement: Achievement,
        currentProgress: Int,
        progressRepository: ProgressRepository,
        achievementRepository: AchievementRepository,
    ): Int {
        return when (achievement.requirement) {
            // === Word counting achievements ===
            is AchievementRequirement.MasterWords -> {
                achievementRepository.getMasteredWordCount(
                    userId = event.userId,
                    minStrength = (achievement.requirement as AchievementRequirement.MasterWords).memoryStrengthThreshold,
                )
            }

            is AchievementRequirement.MaxMemoryStrength -> {
                achievementRepository.getMaxStrengthWordCount(
                    userId = event.userId,
                    strength = (achievement.requirement as AchievementRequirement.MaxMemoryStrength).strength,
                )
            }

            // === Level counting achievements ===
            is AchievementRequirement.CompleteLevels -> {
                val minStars = (achievement.requirement as AchievementRequirement.CompleteLevels).minStars
                achievementRepository.getCompletedLevelCount(event.userId, minStars)
            }

            is AchievementRequirement.PerfectLevel -> {
                // Special case: track perfect levels separately
                when (event) {
                    is GameEvent.LevelComplete -> {
                        if (event.allThreeStar) currentProgress + 1 else currentProgress
                    }
                    else -> currentProgress
                }
            }

            is AchievementRequirement.NoHintsLevel -> {
                when (event) {
                    is GameEvent.LevelComplete -> {
                        if (event.hintsUsed == 0 && event.stars > 0) {
                            event.wordCount // Complete in one go
                        } else {
                            currentProgress
                        }
                    }
                    else -> currentProgress
                }
            }

            // === Island achievements ===
            is AchievementRequirement.CompleteIsland -> {
                val islandId = (achievement.requirement as AchievementRequirement.CompleteIsland).islandId
                achievementRepository.getCompletedLevelCountInIsland(
                    userId = event.userId,
                    islandId = islandId,
                )
            }

            is AchievementRequirement.UnlockIslands -> {
                achievementRepository.getUnlockedIslandCount(event.userId)
            }

            // === Combo achievements ===
            is AchievementRequirement.ComboMilestone -> {
                when (event) {
                    is GameEvent.ComboAchieved -> {
                        event.comboCount
                    }
                    else -> currentProgress
                }
            }

            // === Streak achievements ===
            is AchievementRequirement.StreakDays -> {
                when (event) {
                    is GameEvent.StreakUpdate -> {
                        event.streakDays
                    }
                    else -> currentProgress
                }
            }
        }
    }
}
