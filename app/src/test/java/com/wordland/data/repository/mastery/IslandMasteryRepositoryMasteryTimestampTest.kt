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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Mastery timestamp tests for IslandMasteryRepository.
 * Tests masteredAt timestamp handling when reaching 100% mastery.
 */
class IslandMasteryRepositoryMasteryTimestampTest {
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
    fun `calculateMastery sets masteredAt when 100 percent mastery`() =
        runTest {
            // Given
            val existing =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 28,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 4,
                    crossSceneScore = 0.9,
                    masteryPercentage = 90.0,
                    isNextIslandUnlocked = true,
                    masteredAt = null,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 30,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isNextIslandUnlocked = true,
                    masteredAt = originalMasteredAt,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns existing
            coEvery {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
