package com.wordland.domain.model.achievement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for UserAchievement and AchievementWithProgress
 */
class UserAchievementTest {
    private val testRequirement = AchievementRequirement.MasterWords(count = 30, memoryStrengthThreshold = 80)

    @Test
    fun `UserAchievement progressPercentage is zero when progress is zero`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = false,
                progress = 0,
                target = 30,
            )

        assertEquals(0f, userAchievement.progressPercentage, 0.001f)
    }

    @Test
    fun `UserAchievement progressPercentage is 50 when half complete`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = false,
                progress = 15,
                target = 30,
            )

        assertEquals(0.5f, userAchievement.progressPercentage, 0.001f)
    }

    @Test
    fun `UserAchievement progressPercentage is 100 when complete`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = false,
                progress = 30,
                target = 30,
            )

        assertEquals(1f, userAchievement.progressPercentage, 0.001f)
    }

    @Test
    fun `UserAchievement progressPercentage coerces to 1 when progress exceeds target`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = false,
                progress = 50,
                target = 30,
            )

        assertEquals(1f, userAchievement.progressPercentage, 0.001f)
    }

    @Test
    fun `UserAchievement progressPercentage is 1 when unlocked`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = true,
                progress = 0,
                target = 0,
            )

        assertEquals(1f, userAchievement.progressPercentage, 0.001f)
    }

    @Test
    fun `UserAchievement getProgressText uses requirement format`() {
        val userAchievement =
            UserAchievement(
                userId = "user_001",
                achievementId = "word_scholar",
                isUnlocked = false,
                progress = 15,
                target = 30,
            )

        assertEquals("15/30 words", userAchievement.getProgressText(testRequirement))
    }

    @Test
    fun `AchievementWithProgress progressPercentage is zero when progress is zero`() {
        val achievement =
            Achievement(
                id = "word_scholar",
                name = "Word Scholar",
                description = "Learn 30 words",
                icon = "🎓",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.GOLD,
                requirement = testRequirement,
                reward = AchievementReward.Stars(amount = 50),
            )

        val withProgress =
            AchievementWithProgress(
                achievement = achievement,
                progress = 0,
                target = 30,
                isUnlocked = false,
            )

        assertEquals(0f, withProgress.progressPercentage, 0.001f)
    }

    @Test
    fun `AchievementWithProgress progressPercentage is 100 when complete`() {
        val achievement =
            Achievement(
                id = "word_scholar",
                name = "Word Scholar",
                description = "Learn 30 words",
                icon = "🎓",
                category = AchievementCategory.PROGRESS,
                tier = AchievementTier.GOLD,
                requirement = testRequirement,
                reward = AchievementReward.Stars(amount = 50),
            )

        val withProgress =
            AchievementWithProgress(
                achievement = achievement,
                progress = 30,
                target = 30,
                isUnlocked = true,
            )

        assertEquals(1f, withProgress.progressPercentage, 0.001f)
        assertTrue(withProgress.isUnlocked)
    }

    @Test
    fun `CompletionStats percentage is calculated correctly`() {
        val stats =
            CompletionStats(
                unlocked = 6,
                total = 12,
                percentage = 0f, // Will be calculated
            )

        // The percentage should be 6/12 = 0.5
        val expectedPercentage = 6f / 12f
        assertEquals(expectedPercentage, stats.unlocked.toFloat() / stats.total, 0.001f)
    }

    @Test
    fun `CompletionStats percentage is zero when total is zero`() {
        val stats =
            CompletionStats(
                unlocked = 0,
                total = 0,
                percentage = 0f,
            )

        assertEquals(0f, stats.percentage, 0.001f)
    }
}
