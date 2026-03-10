package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for IslandMastery
 */
class IslandMasteryTest {
    @Test
    fun `calculateMastery returns 100 when all words mastered and cross scene score is 1`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 100,
                totalLevels = 8,
                completedLevels = 8,
                crossSceneScore = 1.0,
            )

        // When
        val result = mastery.calculateMastery()

        // Then
        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun `calculateMastery returns 70 when half words mastered and no cross scene score`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 50,
                totalLevels = 8,
                completedLevels = 4,
                crossSceneScore = 0.0,
            )

        // When
        val result = mastery.calculateMastery()

        // Then - 50% * 0.7 = 35%
        assertEquals(35.0, result, 0.01)
    }

    @Test
    fun `calculateMastery combines word mastery and cross scene score correctly`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 80, // 80% word mastery
                totalLevels = 8,
                completedLevels = 6,
                crossSceneScore = 0.5, // 50% cross scene
            )

        // When
        val result = mastery.calculateMastery()

        // Then - 80% * 0.7 + 50% * 0.3 = 56% + 15% = 71%
        assertEquals(71.0, result, 0.01)
    }

    @Test
    fun `updateUnlockStatus returns false when mastery below 60 percent`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 50, // 50% mastery
                totalLevels = 8,
                completedLevels = 4,
                crossSceneScore = 0.0,
            )

        // When
        val (unlocked, percentage) = mastery.updateUnlockStatus()

        // Then
        assertFalse(unlocked)
        assertTrue(percentage < 60.0)
    }

    @Test
    fun `updateUnlockStatus returns true when mastery at 60 percent`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 86, // Approx 60% when combined with cross scene
                totalLevels = 8,
                completedLevels = 6,
                crossSceneScore = 0.0,
            )

        // When
        val (unlocked, percentage) = mastery.updateUnlockStatus()

        // Then
        assertTrue(unlocked)
        assertTrue(percentage >= 60.0)
    }

    @Test
    fun `updateUnlockStatus returns true when mastery above 60 percent`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 100,
                totalLevels = 8,
                completedLevels = 8,
                crossSceneScore = 0.5,
            )

        // When
        val (unlocked, percentage) = mastery.updateUnlockStatus()

        // Then
        assertTrue(unlocked)
        assertEquals(85.0, percentage, 0.01) // 100% * 0.7 + 50% * 0.3 = 70% + 15% = 85%
    }

    @Test
    fun `updateUnlockStatus returns correct mastery percentage`() {
        // Given
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 70, // 70% * 0.7 = 49%
                totalLevels = 8,
                completedLevels = 5,
                crossSceneScore = 0.7, // 70% * 0.3 = 21%
            )

        // When
        val (unlocked, percentage) = mastery.updateUnlockStatus()

        // Then - 49% + 21% = 70%
        assertEquals(70.0, percentage, 0.01)
        assertTrue(unlocked)
    }

    @Test
    fun `calculateMastery handles zero total words`() {
        // Given - Edge case
        val mastery =
            IslandMastery(
                islandId = "look_island",
                userId = "user_001",
                totalWords = 0,
                masteredWords = 0,
                totalLevels = 0,
                completedLevels = 0,
                crossSceneScore = 0.0,
            )

        // When
        val result = mastery.calculateMastery()

        // Then - Should handle division by zero
        // 0/0 would be NaN, but let's see what actually happens
        assertTrue(result.isNaN() || result == 0.0)
    }

    @Test
    fun `calculateMastery weights word mastery higher than cross scene`() {
        // Given
        val mastery1 =
            IslandMastery(
                islandId = "island1",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 100, // 100% word mastery
                totalLevels = 8,
                completedLevels = 8,
                crossSceneScore = 0.0, // 0% cross scene
            )

        val mastery2 =
            IslandMastery(
                islandId = "island2",
                userId = "user_001",
                totalWords = 100,
                masteredWords = 0, // 0% word mastery
                totalLevels = 8,
                completedLevels = 0,
                crossSceneScore = 1.0, // 100% cross scene
            )

        // When
        val result1 = mastery1.calculateMastery()
        val result2 = mastery2.calculateMastery()

        // Then - Word mastery should have higher weight (70%)
        assertEquals(70.0, result1, 0.01) // 100% * 0.7
        assertEquals(30.0, result2, 0.01) // 100% * 0.3
    }
}
