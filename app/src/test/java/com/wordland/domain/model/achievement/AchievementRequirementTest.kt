package com.wordland.domain.model.achievement

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for AchievementRequirement sealed class
 */
class AchievementRequirementTest {
    @Test
    fun `CompleteLevels getProgressText returns correct format`() {
        val requirement = AchievementRequirement.CompleteLevels(count = 5, minStars = 2)

        assertEquals("0/5 levels", requirement.getProgressText(0))
        assertEquals("3/5 levels", requirement.getProgressText(3))
        assertEquals("5/5 levels", requirement.getProgressText(5))
    }

    @Test
    fun `MasterWords getProgressText returns correct format`() {
        val requirement = AchievementRequirement.MasterWords(count = 30, memoryStrengthThreshold = 80)

        assertEquals("0/30 words", requirement.getProgressText(0))
        assertEquals("15/30 words", requirement.getProgressText(15))
        assertEquals("30/30 words", requirement.getProgressText(30))
    }

    @Test
    fun `ComboMilestone getProgressText shows reached when target met`() {
        val requirement = AchievementRequirement.ComboMilestone(comboCount = 5)

        assertEquals("Best: 0/5", requirement.getProgressText(0))
        assertEquals("Best: 3/5", requirement.getProgressText(3))
        assertEquals("Reached!", requirement.getProgressText(5))
        assertEquals("Reached!", requirement.getProgressText(10))
    }

    @Test
    fun `StreakDays getProgressText returns correct format`() {
        val requirement = AchievementRequirement.StreakDays(days = 7, minWordsPerDay = 1)

        assertEquals("0/7 days", requirement.getProgressText(0))
        assertEquals("3/7 days", requirement.getProgressText(3))
        assertEquals("7/7 days", requirement.getProgressText(7))
    }

    @Test
    fun `PerfectLevel getProgressText returns correct format`() {
        val requirement = AchievementRequirement.PerfectLevel(count = 3)

        assertEquals("0/3 levels", requirement.getProgressText(0))
        assertEquals("1/3 levels", requirement.getProgressText(1))
        assertEquals("3/3 levels", requirement.getProgressText(3))
    }

    @Test
    fun `NoHintsLevel getProgressText returns correct format`() {
        val requirement = AchievementRequirement.NoHintsLevel(wordCount = 6)

        assertEquals("0/6 words", requirement.getProgressText(0))
        assertEquals("3/6 words", requirement.getProgressText(3))
        assertEquals("6/6 words", requirement.getProgressText(6))
    }

    @Test
    fun `MaxMemoryStrength getProgressText returns correct format`() {
        val requirement = AchievementRequirement.MaxMemoryStrength(count = 10, strength = 100)

        assertEquals("0/10 words", requirement.getProgressText(0))
        assertEquals("5/10 words", requirement.getProgressText(5))
        assertEquals("10/10 words", requirement.getProgressText(10))
    }

    @Test
    fun `CompleteIsland getProgressText returns correct format`() {
        val requirement = AchievementRequirement.CompleteIsland(islandId = "look_island", levelCount = 5)

        assertEquals("0/5 levels", requirement.getProgressText(0))
        assertEquals("3/5 levels", requirement.getProgressText(3))
        assertEquals("5/5 levels", requirement.getProgressText(5))
    }

    @Test
    fun `UnlockIslands getProgressText returns correct format`() {
        val requirement = AchievementRequirement.UnlockIslands(count = 3)

        assertEquals("0/3 islands", requirement.getProgressText(0))
        assertEquals("1/3 islands", requirement.getProgressText(1))
        assertEquals("3/3 islands", requirement.getProgressText(3))
    }

    @Test
    fun `all requirements have correct targetValue`() {
        val requirements =
            listOf(
                AchievementRequirement.CompleteLevels(count = 5, minStars = 1),
                AchievementRequirement.MasterWords(count = 30, memoryStrengthThreshold = 80),
                AchievementRequirement.ComboMilestone(comboCount = 5),
                AchievementRequirement.StreakDays(days = 7),
                AchievementRequirement.PerfectLevel(count = 1),
                AchievementRequirement.NoHintsLevel(wordCount = 6),
                AchievementRequirement.MaxMemoryStrength(count = 10, strength = 100),
                AchievementRequirement.CompleteIsland(islandId = "look_island", levelCount = 5),
                AchievementRequirement.UnlockIslands(count = 3),
            )

        val expectedValues = listOf(5, 30, 5, 7, 1, 6, 10, 5, 3)

        requirements.zip(expectedValues).forEach { (requirement, expected) ->
            assertEquals(expected, requirement.targetValue)
        }
    }
}
