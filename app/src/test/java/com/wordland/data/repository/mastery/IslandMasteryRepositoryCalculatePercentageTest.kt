package com.wordland.data.repository.mastery

import com.wordland.domain.model.IslandMastery
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Tests for mastery percentage calculation
 *
 * Tests the calculateMastery() method's percentage computation logic:
 * - Correct percentage calculation (70% words + 30% cross-scene)
 * - Zero mastery handling
 * - Perfect scores (100% mastery)
 */
class IslandMasteryRepositoryCalculatePercentageTest : IslandMasteryRepositoryCalculateTestHelper() {
    @Test
    fun `calculateMastery creates new record when none exists`() =
        runTest {
            // Given
            setupForNewRecord()

            // When
            val result =
                repository.calculateMastery(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 5,
                    completedLevels = 1,
                    crossSceneScore = 0.8,
                )

            // Then
            assertFalse("Should return false for new record (not newly unlocked)", result)
            coVerify { islandMasteryDao.insertIslandMastery(any()) }

            // Verify the inserted mastery has correct values
            val insertSlot = slot<IslandMastery>()
            coVerify { islandMasteryDao.insertIslandMastery(capture(insertSlot)) }

            val inserted = insertSlot.captured
            assertEquals(userId, inserted.userId)
            assertEquals(islandId, inserted.islandId)
            assertEquals(5, inserted.masteredWords)
            assertEquals(1, inserted.completedLevels)
            assertEquals(0.8, inserted.crossSceneScore, 0.01)
        }

    @Test
    fun `calculateMastery initializes with zero progress`() =
        runTest {
            // Given
            setupForNewRecord()

            // When
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 0,
                completedLevels = 0,
                crossSceneScore = 0.0,
            )

            // Then
            val insertSlot = slot<IslandMastery>()
            coVerify { islandMasteryDao.insertIslandMastery(capture(insertSlot)) }

            val inserted = insertSlot.captured
            assertEquals(0, inserted.masteredWords)
            assertEquals(0, inserted.completedLevels)
            assertEquals(0.0, inserted.crossSceneScore, 0.01)
            assertEquals(0.0, inserted.masteryPercentage, 0.01)
        }

    @Test
    fun `calculateMastery calculates correct mastery percentage`() =
        runTest {
            // Given - Existing mastery with totalWords set
            val existing = createDefaultMastery()
            setupForUpdate(existing)

            // When - Mastered 15 out of 30 words (50%), with 0.8 cross scene score
            val result =
                repository.calculateMastery(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 15,
                    completedLevels = 2,
                    crossSceneScore = 0.8,
                )

            // Then - Mastery = 50% * 0.7 + 80% * 0.3 = 35% + 24% = 59%
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 15,
                    completedLevels = 2,
                    crossSceneScore = 0.8,
                    masteryPercentage = 59.0,
                    isUnlocked = false, // Below 60% threshold
                    masteredAt = any(),
                    updatedAt = any(),
                )
            }
        }

    @Test
    fun `calculateMastery handles zero mastered words`() =
        runTest {
            // Given
            val existing = createDefaultMastery()
            setupForUpdate(existing)

            // When
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 0,
                completedLevels = 0,
                crossSceneScore = 0.0,
            )

            // Then - 0% * 0.7 + 0% * 0.3 = 0%
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 0,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = 0.0,
                    isUnlocked = false,
                    masteredAt = any(),
                    updatedAt = any(),
                )
            }
        }

    @Test
    fun `calculateMastery handles perfect scores`() =
        runTest {
            // Given
            val existing =
                createMasteryWithProgress(
                    masteredWords = 28,
                    completedLevels = 4,
                    crossSceneScore = 0.9,
                    masteryPercentage = 87.0,
                    isUnlocked = true,
                )
            setupForUpdate(existing)

            // When - Perfect scores
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 30,
                completedLevels = 5,
                crossSceneScore = 1.0,
            )

            // Then - 100% * 0.7 + 100% * 0.3 = 100%
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isUnlocked = true,
                    masteredAt = any(), // Should be set
                    updatedAt = any(),
                )
            }
        }
}
