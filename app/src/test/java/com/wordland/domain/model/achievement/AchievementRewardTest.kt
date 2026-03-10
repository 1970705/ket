package com.wordland.domain.model.achievement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for AchievementReward sealed class
 */
class AchievementRewardTest {
    @Test
    fun `Stars getDisplayText returns correct format`() {
        val reward = AchievementReward.Stars(amount = 50)

        assertEquals("50 stars ⭐", reward.getDisplayText())
    }

    @Test
    fun `Title getDisplayText returns correct format`() {
        val reward =
            AchievementReward.Title(
                titleId = "scholar",
                displayName = "Scholar",
            )

        assertEquals("Title: Scholar", reward.getDisplayText())
    }

    @Test
    fun `Badge getDisplayText returns correct format`() {
        val reward =
            AchievementReward.Badge(
                badgeId = "word_hunter",
                iconName = "🏆",
                displayName = "Word Hunter",
            )

        assertEquals("Badge: Word Hunter 🏆", reward.getDisplayText())
    }

    @Test
    fun `PetUnlock getDisplayText returns correct format`() {
        val reward =
            AchievementReward.PetUnlock(
                petId = "flame_phoenix",
                petName = "Flame Phoenix",
                petIcon = "🔥",
            )

        assertEquals("Pet: Flame Phoenix 🔥", reward.getDisplayText())
    }

    @Test
    fun `Multiple getDisplayText combines all rewards`() {
        val reward =
            AchievementReward.Multiple(
                rewards =
                    listOf(
                        AchievementReward.Stars(amount = 50),
                        AchievementReward.Title(titleId = "master", displayName = "Master"),
                        AchievementReward.Badge(badgeId = "elite", iconName = "⭐", displayName = "Elite"),
                    ),
            )

        val displayText = reward.getDisplayText()
        assertTrue(displayText.contains("50 stars ⭐"))
        assertTrue(displayText.contains("Title: Master"))
        assertTrue(displayText.contains("Badge: Elite ⭐"))
    }

    @Test
    fun `Multiple with empty rewards returns empty string`() {
        val reward = AchievementReward.Multiple(rewards = emptyList())

        assertEquals("", reward.getDisplayText())
    }

    @Test
    fun `Multiple with single reward displays that reward`() {
        val reward =
            AchievementReward.Multiple(
                rewards =
                    listOf(
                        AchievementReward.Stars(amount = 100),
                    ),
            )

        assertEquals("100 stars ⭐", reward.getDisplayText())
    }
}
