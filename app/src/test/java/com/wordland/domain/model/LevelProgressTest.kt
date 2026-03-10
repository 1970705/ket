package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LevelProgress
 */
class LevelProgressTest {
    @Test
    fun `getCompletionPercentage returns 100 when review completed`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.COMPLETED,
                stars = 3,
                isReviewCompleted = true,
                difficulty = "normal",
            )

        // When
        val result = progress.getCompletionPercentage()

        // Then
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `getCompletionPercentage returns 0 when no stars and review not completed`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.UNLOCKED,
                stars = 0,
                isReviewCompleted = false,
                difficulty = "normal",
            )

        // When
        val result = progress.getCompletionPercentage()

        // Then
        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun `getCompletionPercentage returns 60 when 3 stars and review not completed`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.IN_PROGRESS,
                stars = 3,
                isReviewCompleted = false,
                difficulty = "normal",
            )

        // When
        val result = progress.getCompletionPercentage()

        // Then - 3/5 * 100 = 60%
        assertEquals(60.0, result, 0.01)
    }

    @Test
    fun `getCompletionPercentage returns 20 when 1 star and review not completed`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.IN_PROGRESS,
                stars = 1,
                isReviewCompleted = false,
                difficulty = "normal",
            )

        // When
        val result = progress.getCompletionPercentage()

        // Then - 1/5 * 100 = 20%
        assertEquals(20.0, result, 0.01)
    }

    @Test
    fun `getCompletionPercentage returns 40 when 2 stars and review not completed`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.IN_PROGRESS,
                stars = 2,
                isReviewCompleted = false,
                difficulty = "normal",
            )

        // When
        val result = progress.getCompletionPercentage()

        // Then - 2/5 * 100 = 40%
        assertEquals(40.0, result, 0.01)
    }

    @Test
    fun `LevelProgress creates with correct default values`() {
        // Given
        val progress =
            LevelProgress(
                levelId = "level_01",
                userId = "user_001",
                islandId = "island_1",
                status = LevelStatus.LOCKED,
                difficulty = "easy",
            )

        // Then
        assertEquals(0, progress.stars)
        assertEquals(0, progress.totalTime)
        assertEquals(0, progress.attempts)
        assertEquals(0, progress.practiceCount)
        assertFalse(progress.isNewWordsCompleted)
        assertFalse(progress.isReviewCompleted)
        assertEquals(0, progress.score)
        assertNull(progress.masteredAt)
        assertNotNull(progress.createdAt)
        assertNotNull(progress.updatedAt)
    }

    @Test
    fun `LevelProgress stores all custom values correctly`() {
        // Given
        val testTime = System.currentTimeMillis()
        val progress =
            LevelProgress(
                levelId = "level_02",
                userId = "user_002",
                islandId = "island_2",
                status = LevelStatus.PERFECT,
                stars = 3,
                totalTime = 300000L,
                bestTime = 250000L,
                lastPlayTime = testTime,
                attempts = 5,
                firstPlayTime = testTime - 100000,
                practiceCount = 3,
                isNewWordsCompleted = true,
                isReviewCompleted = true,
                score = 500,
                difficulty = "hard",
                masteredAt = testTime,
                createdAt = testTime,
                updatedAt = testTime,
            )

        // Then
        assertEquals("level_02", progress.levelId)
        assertEquals("user_002", progress.userId)
        assertEquals("island_2", progress.islandId)
        assertEquals(LevelStatus.PERFECT, progress.status)
        assertEquals(3, progress.stars)
        assertEquals(300000L, progress.totalTime)
        assertEquals(250000L, progress.bestTime)
        assertEquals(testTime, progress.lastPlayTime)
        assertEquals(5, progress.attempts)
        assertEquals(testTime - 100000, progress.firstPlayTime)
        assertEquals(3, progress.practiceCount)
        assertTrue(progress.isNewWordsCompleted)
        assertTrue(progress.isReviewCompleted)
        assertEquals(500, progress.score)
        assertEquals("hard", progress.difficulty)
        assertEquals(testTime, progress.masteredAt)
        assertEquals(testTime, progress.createdAt)
        assertEquals(testTime, progress.updatedAt)
    }

    @Test
    fun `LevelStatus enum has correct values`() {
        // Then
        assertEquals(5, LevelStatus.values().size)
        assertTrue(LevelStatus.values().contains(LevelStatus.LOCKED))
        assertTrue(LevelStatus.values().contains(LevelStatus.UNLOCKED))
        assertTrue(LevelStatus.values().contains(LevelStatus.IN_PROGRESS))
        assertTrue(LevelStatus.values().contains(LevelStatus.COMPLETED))
        assertTrue(LevelStatus.values().contains(LevelStatus.PERFECT))
    }
}
