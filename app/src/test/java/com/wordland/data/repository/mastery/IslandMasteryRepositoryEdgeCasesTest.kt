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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Edge case tests for IslandMasteryRepository calculateMastery().
 * Tests boundary conditions and extreme scenarios.
 */
class IslandMasteryRepositoryEdgeCasesTest {
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
    fun `calculateMastery handles zero mastered words`() =
        runTest {
            // Given
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
                    masteredWords = 0,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = any(),
                    isUnlocked = any(),
                    masteredAt = any(),
                    updatedAt = any(),
                )
            } just Runs

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
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 28,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 4,
                    crossSceneScore = 0.9,
                    masteryPercentage = 87.0,
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

    @Test
    fun `calculateMastery handles perfect scores with already mastered`() =
        runTest {
            // Given - Already mastered at 100%
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

            // When - Still perfect scores
            repository.calculateMastery(
                userId = userId,
                islandId = islandId,
                masteredWords = 30,
                completedLevels = 5,
                crossSceneScore = 1.0,
            )

            // Then - Should update masteredAt to current time
            coVerify {
                islandMasteryDao.updateMasteryProgress(
                    userId = userId,
                    islandId = islandId,
                    masteredWords = 30,
                    completedLevels = 5,
                    crossSceneScore = 1.0,
                    masteryPercentage = 100.0,
                    isUnlocked = true,
                    masteredAt = any(), // Updated to current time
                    updatedAt = any(),
                )
            }
        }
}
