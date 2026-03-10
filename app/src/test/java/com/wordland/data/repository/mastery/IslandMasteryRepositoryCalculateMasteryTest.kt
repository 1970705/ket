package com.wordland.data.repository.mastery

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.data.repository.IslandMasteryRepositoryImpl
import com.wordland.domain.model.IslandMastery
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Core calculateMastery() tests for IslandMasteryRepository.
 * Tests mastery percentage calculation and island unlock logic.
 */
class IslandMasteryRepositoryCalculateMasteryTest {
    private lateinit var repository: IslandMasteryRepositoryImpl
    private lateinit var islandMasteryDao: IslandMasteryDao
    private lateinit var progressDao: ProgressDao

    private val userId = IslandMasteryRepositoryTestHelper.TEST_USER_ID
    private val islandId = IslandMasteryRepositoryTestHelper.TEST_ISLAND_ID

    @Before
    fun setup() {
        islandMasteryDao = mockk(relaxed = true)
        progressDao = mockk(relaxed = true)
        repository =
            IslandMasteryRepositoryTestHelper.createRepository(
                islandMasteryDao,
                progressDao,
            )
    }

    @Test
    fun `calculateMastery creates new record when none exists`() =
        runTest {
            // Given
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns null
            coEvery { islandMasteryDao.insertIslandMastery(any()) } just Runs

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
    fun `calculateMastery calculates correct mastery percentage`() =
        runTest {
            // Given - Existing mastery with totalWords set
            val existing =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS, // Look Island has 30 words
                    masteredWords = 0,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = 0.0,
                    isNextIslandUnlocked = false,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 15,
                    completedLevels = 2,
                    crossSceneScore = 0.8,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
    fun `calculateMastery returns true when newly unlocked`() =
        runTest {
            // Given - Existing mastery below unlock threshold
            val existing =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 10,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 1,
                    crossSceneScore = 0.5,
                    masteryPercentage = 30.0,
                    isNextIslandUnlocked = false,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 26,
                    completedLevels = 5,
                    crossSceneScore = 0.9,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 20,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 3,
                    crossSceneScore = 0.7,
                    masteryPercentage = 70.0,
                    isNextIslandUnlocked = true, // Already unlocked
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 22,
                    completedLevels = 4,
                    crossSceneScore = 0.75,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
            val existing =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 0,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = 0.0,
                    isNextIslandUnlocked = false,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = any(),
                    completedLevels = any(),
                    crossSceneScore = any(),
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
}
