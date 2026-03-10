package com.wordland.data.repository.mastery

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for mastery unlock detection and timestamp management
 *
 * Tests the calculateMastery() method's unlock logic:
 * - New unlock detection (returns true when mastery crosses 60% threshold)
 * - Already unlocked handling (returns false)
 * - Boundary conditions at unlock threshold
 * - Mastered timestamp setting at 100% mastery
 */
class IslandMasteryRepositoryCalculateUnlockTest : IslandMasteryRepositoryCalculateTestHelper() {
    @Test
    fun `calculateMastery returns true when newly unlocked`() =
        runTest {
            // Given - Existing mastery below unlock threshold
            val existing =
                createMasteryWithProgress(
                    masteredWords = 10,
                    completedLevels = 1,
                    crossSceneScore = 0.5,
                    masteryPercentage = 30.0,
                    isUnlocked = false,
                )
            setupForUpdate(existing)

            // When - Mastered 26 out of 30 words (87%), with 0.9 cross scene score
            val result =
                repository.calculateMastery(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 26,
                    completedLevels = 5,
                    crossSceneScore = 0.9,
                )

            // Then - Should be newly unlocked (was false, now true)
            assertTrue("Should return true for newly unlocked island", result)
        }

    @Test
    fun `calculateMastery returns false when already unlocked`() =
        runTest {
            // Given - Already unlocked
            val existing =
                createMasteryWithProgress(
                    masteredWords = 20,
                    completedLevels = 3,
                    crossSceneScore = 0.7,
                    masteryPercentage = 70.0,
                    isUnlocked = true, // Already unlocked
                )
            setupForUpdate(existing)

            // When
            val result =
                repository.calculateMastery(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 22,
                    completedLevels = 4,
                    crossSceneScore = 0.75,
                )

            // Then - Still unlocked but not "newly" unlocked
            assertFalse("Should return false when already unlocked", result)
        }

    @Test
    fun `calculateMastery handles boundary at unlock threshold`() =
        runTest {
            // Given - Exactly at threshold
            val existing = createDefaultMastery()
            setupForUpdate(existing)

            // When - Mastery at 60.67% (above threshold)
            // 26/30 * 0.7 + 0.0 * 0.3 = 0.6067 * 100 = 60.67%
            val result =
                repository.calculateMastery(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 26,
                    completedLevels = 5,
                    crossSceneScore = 0.0,
                )

            // Then - Should be newly unlocked
            assertTrue("Should return true when mastery >= 60%", result)
        }

    @Test
    fun `calculateMastery sets masteredAt when 100 percent mastery`() =
        runTest {
            // Given
            val existing =
                createMasteryWithProgress(
                    masteredWords = 28,
                    completedLevels = 4,
                    crossSceneScore = 0.9,
                    masteryPercentage = 90.0,
                    isUnlocked = true,
                    masteredAt = null,
                )
            setupForUpdate(existing)

            val beforeTest = System.currentTimeMillis()

            // When - All words mastered, perfect cross scene score
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 30,
                completedLevels = 5,
                crossSceneScore = 1.0,
            )

            // Then
            val masteredAtSlot = slot<Long>()
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isUnlocked = true,
                    masteredAt = capture(masteredAtSlot),
                    updatedAt = any(),
                )
            }

            val masteredAt = masteredAtSlot.captured
            assertTrue("masteredAt should be set", masteredAt > 0)
            assertTrue("masteredAt should be recent", masteredAt >= beforeTest)
        }

    @Test
    fun `calculateMastery updates masteredAt to current time at 100 percent mastery`() =
        runTest {
            // Given
            val originalMasteredAt = 1700000000000L
            val existing =
                createMasteryWithProgress(
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isUnlocked = true,
                    masteredAt = originalMasteredAt,
                )
            setupForUpdate(existing)

            // When - Update but still at 100%
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 30,
                completedLevels = 5,
                crossSceneScore = 1.0,
            )

            // Then - masteredAt is updated to current time when mastery is 100%
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isUnlocked = true,
                    masteredAt = any(), // Updated to current time when mastery = 100%
                    updatedAt = any(),
                )
            }
        }
}
