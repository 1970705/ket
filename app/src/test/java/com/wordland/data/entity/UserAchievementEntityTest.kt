package com.wordland.data.entity

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for UserAchievementEntity data class
 *
 * Tests user achievement progress tracking
 */
@RunWith(JUnit4::class)
class UserAchievementEntityTest {
    @Test
    fun userAchievementEntity_creationWithAllFields() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_001",
                isUnlocked = false,
                progress = 3,
                target = 5,
                lastUpdated = 1234567890L,
            )

        assertEquals("user_001", entity.userId)
        assertEquals("achievement_001", entity.achievementId)
        assertFalse(entity.isUnlocked)
        assertEquals(3, entity.progress)
        assertEquals(5, entity.target)
        assertEquals(1234567890L, entity.lastUpdated)
    }

    @Test
    fun userAchievementEntity_unlockedAchievement() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_002",
                isUnlocked = true,
                progress = 5,
                target = 5,
                lastUpdated = System.currentTimeMillis(),
            )

        assertTrue(entity.isUnlocked)
        assertEquals(entity.target, entity.progress)
    }

    @Test
    fun userAchievementEntity_zeroProgress() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_003",
                isUnlocked = false,
                progress = 0,
                target = 10,
                lastUpdated = 0L,
            )

        assertEquals(0, entity.progress)
        assertEquals(10, entity.target)
        assertFalse(entity.isUnlocked)
    }

    @Test
    fun userAchievementEntity_progressCalculation() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_004",
                isUnlocked = false,
                progress = 7,
                target = 10,
                lastUpdated = 0L,
            )

        val percentage = (entity.progress.toFloat() / entity.target * 100).toInt()
        assertEquals(70, percentage)
    }

    @Test
    fun userAchievementEntity_isComplete_whenProgressEqualsTarget() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_005",
                isUnlocked = true,
                progress = 10,
                target = 10,
                lastUpdated = 0L,
            )

        val isComplete = entity.progress >= entity.target
        assertTrue(isComplete)
    }

    @Test
    fun userAchievementEntity_isNotComplete_whenProgressLessThanTarget() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_006",
                isUnlocked = false,
                progress = 3,
                target = 10,
                lastUpdated = 0L,
            )

        val isComplete = entity.progress >= entity.target
        assertFalse(isComplete)
    }

    @Test
    fun userAchievementEntity_remainingProgress() {
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_007",
                isUnlocked = false,
                progress = 3,
                target = 10,
                lastUpdated = 0L,
            )

        val remaining = entity.target - entity.progress
        assertEquals(7, remaining)
    }

    @Test
    fun userAchievementEntity_timeSinceUpdate() {
        val pastTime = System.currentTimeMillis() - 3600000L // 1 hour ago
        val entity =
            UserAchievementEntity(
                userId = "user_001",
                achievementId = "achievement_008",
                isUnlocked = false,
                progress = 5,
                target = 10,
                lastUpdated = pastTime,
            )

        val timeSinceUpdate = System.currentTimeMillis() - entity.lastUpdated
        assertTrue(timeSinceUpdate >= 3600000L)
    }
}
