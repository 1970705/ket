package com.wordland.data.entity

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for AchievementEntity data class
 *
 * Tests entity structure and data integrity
 */
@RunWith(JUnit4::class)
class AchievementEntityTest {
    @Test
    fun achievementEntity_creationWithAllFields() {
        val entity =
            AchievementEntity(
                id = "test_achievement_001",
                name = "Test Achievement",
                description = "Test Description",
                icon = "test_icon",
                category = "LEARNING",
                tier = "BRONZE",
                requirementType = "CompleteLevels",
                requirementData = "{\"count\":5}",
                rewardType = "Stars",
                rewardData = "{\"amount\":100}",
                isHidden = false,
                parentId = null,
            )

        assertEquals("test_achievement_001", entity.id)
        assertEquals("Test Achievement", entity.name)
        assertEquals("Test Description", entity.description)
        assertEquals("test_icon", entity.icon)
        assertEquals("LEARNING", entity.category)
        assertEquals("BRONZE", entity.tier)
        assertEquals("CompleteLevels", entity.requirementType)
        assertEquals("{\"count\":5}", entity.requirementData)
        assertEquals("Stars", entity.rewardType)
        assertEquals("{\"amount\":100}", entity.rewardData)
        assertFalse(entity.isHidden)
        assertNull(entity.parentId)
    }

    @Test
    fun achievementEntity_creationWithDefaults() {
        val entity =
            AchievementEntity(
                id = "test_002",
                name = "Test",
                description = "Desc",
                icon = "icon",
                category = "LEARNING",
                tier = "SILVER",
                requirementType = "None",
                requirementData = "{}",
                rewardType = "None",
                rewardData = "{}",
            )

        // Default values
        assertFalse(entity.isHidden)
        assertNull(entity.parentId)
    }

    @Test
    fun achievementEntity_hiddenAchievement() {
        val entity =
            AchievementEntity(
                id = "hidden_001",
                name = "Hidden Achievement",
                description = "Complete a secret task",
                icon = "secret",
                category = "SPECIAL",
                tier = "GOLD",
                requirementType = "Secret",
                requirementData = "{}",
                rewardType = "Badge",
                rewardData = "{}",
                isHidden = true,
            )

        assertTrue(entity.isHidden)
    }

    @Test
    fun achievementEntity_withParent() {
        val entity =
            AchievementEntity(
                id = "child_001",
                name = "Child Achievement",
                description = "Sub achievement",
                icon = "child",
                category = "LEARNING",
                tier = "SILVER",
                requirementType = "None",
                requirementData = "{}",
                rewardType = "None",
                rewardData = "{}",
                parentId = "parent_001",
            )

        assertEquals("parent_001", entity.parentId)
    }

    @Test
    fun requirementData_creationWithAllFields() {
        val data =
            RequirementData(
                type = "CompleteLevels",
                count = 5,
                minStars = 3,
                memoryStrengthThreshold = 80,
                comboCount = 10,
                days = 7,
                minWordsPerDay = 5,
                wordCount = 6,
                strength = 100,
                islandId = "look_island",
                levelCount = 5,
            )

        assertEquals("CompleteLevels", data.type)
        assertEquals(5, data.count)
        assertEquals(3, data.minStars)
        assertEquals(80, data.memoryStrengthThreshold)
        assertEquals(10, data.comboCount)
        assertEquals(7, data.days)
        assertEquals(5, data.minWordsPerDay)
        assertEquals(6, data.wordCount)
        assertEquals(100, data.strength)
        assertEquals("look_island", data.islandId)
        assertEquals(5, data.levelCount)
    }

    @Test
    fun requirementData_creationWithOnlyRequiredFields() {
        val data =
            RequirementData(
                type = "PerfectLevel",
            )

        assertEquals("PerfectLevel", data.type)
        assertNull(data.count)
        assertNull(data.minStars)
    }

    @Test
    fun rewardData_creationWithAllFields() {
        val data =
            RewardData(
                type = "Badge",
                amount = 100,
                titleId = "title_001",
                displayName = "Master",
                badgeId = "badge_001",
                iconName = "icon_star",
                petId = "pet_001",
                petName = "Buddy",
                petIcon = "pet_icon",
            )

        assertEquals("Badge", data.type)
        assertEquals(100, data.amount)
        assertEquals("title_001", data.titleId)
        assertEquals("Master", data.displayName)
        assertEquals("badge_001", data.badgeId)
        assertEquals("icon_star", data.iconName)
        assertEquals("pet_001", data.petId)
        assertEquals("Buddy", data.petName)
        assertEquals("pet_icon", data.petIcon)
    }

    @Test
    fun rewardData_creationForStars() {
        val data =
            RewardData(
                type = "Stars",
                amount = 500,
            )

        assertEquals("Stars", data.type)
        assertEquals(500, data.amount)
        assertNull(data.titleId)
        assertNull(data.badgeId)
    }
}
