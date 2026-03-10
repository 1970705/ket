package com.wordland.data.seed

import com.wordland.data.repository.AchievementRepository
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.domain.model.achievement.AchievementRequirement
import com.wordland.domain.model.achievement.AchievementReward
import com.wordland.domain.model.achievement.AchievementTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Seeder for achievement definitions
 * Creates 12 achievements across all categories
 */
class AchievementSeeder(
    private val achievementRepository: AchievementRepository,
) {
    /**
     * Seed all achievements to database
     * Call this during app initialization
     */
    suspend fun seedAchievements() =
        withContext(Dispatchers.IO) {
            val achievements = getAllAchievements()
            achievementRepository.insertAchievements(achievements)
        }

    /**
     * Get all 12 achievement definitions
     */
    fun getAllAchievements(): List<Achievement> =
        listOf(
            // === PROGRESS ACHIEVEMENTS (5) ===
            Achievement(
                id = "first_steps",
                name = "First Steps",
                description = "Complete your first level",
                icon = "🌟",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.BRONZE,
                requirement = AchievementRequirement.CompleteLevels(count = 1, minStars = 1),
                reward = AchievementReward.Stars(amount = 10),
            ),
            Achievement(
                id = "page_turner",
                name = "Page Turner",
                description = "Complete 5 levels",
                icon = "📚",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.SILVER,
                requirement = AchievementRequirement.CompleteLevels(count = 5, minStars = 2),
                reward = AchievementReward.Stars(amount = 25),
            ),
            Achievement(
                id = "word_scholar",
                name = "Word Scholar",
                description = "Learn 30 words",
                icon = "🎓",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.GOLD,
                requirement = AchievementRequirement.MasterWords(count = 30, memoryStrengthThreshold = 80),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 50),
                                AchievementReward.Title(titleId = "scholar", displayName = "Scholar"),
                            ),
                    ),
            ),
            Achievement(
                id = "word_hunter",
                name = "Word Hunter",
                description = "Learn 60 words",
                icon = "🏆",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.PLATINUM,
                requirement = AchievementRequirement.MasterWords(count = 60, memoryStrengthThreshold = 80),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 100),
                                AchievementReward.Badge(
                                    badgeId = "word_hunter",
                                    iconName = "🏆",
                                    displayName = "Word Hunter",
                                ),
                            ),
                    ),
            ),
            Achievement(
                id = "island_master",
                name = "Island Master",
                description = "Complete an island with 100% mastery",
                icon = "🏝️",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.PLATINUM,
                requirement = AchievementRequirement.CompleteIsland(islandId = "look_island", levelCount = 5),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 150),
                                AchievementReward.PetUnlock(
                                    petId = "island_master_pet",
                                    petName = "Island Guardian",
                                    petIcon = "🦜",
                                ),
                            ),
                    ),
            ),
            // === PERFORMANCE ACHIEVEMENTS (3) ===
            Achievement(
                id = "perfectionist",
                name = "Perfectionist",
                description = "Complete a level with all 3-star words",
                icon = "💎",
                category = AchievementCategory.PERFORMANCE,
                tier = AchievementTier.GOLD,
                requirement = AchievementRequirement.PerfectLevel(count = 1),
                reward = AchievementReward.Stars(amount = 30),
            ),
            Achievement(
                id = "memory_master",
                name = "Memory Master",
                description = "Get 10 words to memory strength 100",
                icon = "🧠",
                category = AchievementCategory.PERFORMANCE,
                tier = AchievementTier.PLATINUM,
                requirement = AchievementRequirement.MaxMemoryStrength(count = 10, strength = 100),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 40),
                                AchievementReward.Title(
                                    titleId = "memory_master",
                                    displayName = "Memory Master",
                                ),
                            ),
                    ),
            ),
            Achievement(
                id = "no_hints_hero",
                name = "No Hints Hero",
                description = "Complete a level without using any hints",
                icon = "🚀",
                category = AchievementCategory.PERFORMANCE,
                tier = AchievementTier.GOLD,
                requirement = AchievementRequirement.NoHintsLevel(wordCount = 6),
                reward = AchievementReward.Stars(amount = 30),
            ),
            // === COMBO ACHIEVEMENTS (2) ===
            Achievement(
                id = "combo_master",
                name = "Combo Master",
                description = "Reach a 5x combo",
                icon = "🔥",
                category = AchievementCategory.COMBO,
                tier = AchievementTier.SILVER,
                requirement = AchievementRequirement.ComboMilestone(comboCount = 5),
                reward = AchievementReward.Stars(amount = 25),
            ),
            Achievement(
                id = "unstoppable",
                name = "Unstoppable",
                description = "Reach a 10x combo",
                icon = "⚡",
                category = AchievementCategory.COMBO,
                tier = AchievementTier.PLATINUM,
                requirement = AchievementRequirement.ComboMilestone(comboCount = 10),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 50),
                                AchievementReward.Title(
                                    titleId = "unstoppable",
                                    displayName = "Unstoppable",
                                ),
                            ),
                    ),
            ),
            // === STREAK ACHIEVEMENTS (2) ===
            Achievement(
                id = "dedicated_student",
                name = "Dedicated Student",
                description = "Practice for 7 days in a row",
                icon = "📅",
                category = AchievementCategory.STREAK,
                tier = AchievementTier.GOLD,
                requirement = AchievementRequirement.StreakDays(days = 7, minWordsPerDay = 1),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 35),
                                AchievementReward.Badge(
                                    badgeId = "dedicated",
                                    iconName = "📅",
                                    displayName = "Dedicated",
                                ),
                            ),
                    ),
            ),
            Achievement(
                id = "week_warrior",
                name = "Week Warrior",
                description = "Practice for 30 days in a row",
                icon = "🔥",
                category = AchievementCategory.STREAK,
                tier = AchievementTier.PLATINUM,
                requirement = AchievementRequirement.StreakDays(days = 30, minWordsPerDay = 1),
                reward =
                    AchievementReward.Multiple(
                        rewards =
                            listOf(
                                AchievementReward.Stars(amount = 100),
                                AchievementReward.Title(
                                    titleId = "week_warrior",
                                    displayName = "Week Warrior",
                                ),
                                AchievementReward.PetUnlock(
                                    petId = "week_warrior_pet",
                                    petName = "Flame Phoenix",
                                    petIcon = "🔥",
                                ),
                            ),
                    ),
            ),
        )

    companion object {
        /**
         * Total number of achievements
         */
        const val TOTAL_ACHIEVEMENTS = 12
    }
}
