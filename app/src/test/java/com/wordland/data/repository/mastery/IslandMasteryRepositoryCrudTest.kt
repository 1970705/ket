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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * CRUD operation tests for IslandMasteryRepository.
 * Tests basic repository methods: get, insert, update, and query operations.
 */
class IslandMasteryRepositoryCrudTest {
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
    fun `getIslandMastery returns mastery from dao`() =
        runTest {
            // Given
            val expected =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 15,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 2,
                    crossSceneScore = 0.5,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns expected

            // When
            val result = repository.getIslandMastery(userId, islandId)

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `getIslandMastery returns null when not found`() =
        runTest {
            // Given
            coEvery { islandMasteryDao.getIslandMastery(userId, islandId) } returns null

            // When
            val result = repository.getIslandMastery(userId, islandId)

            // Then
            assertNull(result)
        }

    @Test
    fun `insertIslandMastery delegates to dao`() =
        runTest {
            // Given
            val mastery =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 0,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                )
            coEvery { islandMasteryDao.insertIslandMastery(mastery) } just Runs

            // When
            repository.insertIslandMastery(mastery)

            // Then
            coVerify { islandMasteryDao.insertIslandMastery(mastery) }
        }

    @Test
    fun `updateIslandMastery delegates to dao`() =
        runTest {
            // Given
            val mastery =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_WORDS,
                    masteredWords = 15,
                    totalLevels = IslandMasteryRepositoryTestHelper.LOOK_ISLAND_TOTAL_LEVELS,
                    completedLevels = 2,
                    crossSceneScore = 0.5,
                )
            coEvery { islandMasteryDao.updateIslandMastery(mastery) } just Runs

            // When
            repository.updateIslandMastery(mastery)

            // Then
            coVerify { islandMasteryDao.updateIslandMastery(mastery) }
        }

    @Test
    fun `getAllIslandMastery delegates to dao`() =
        runTest {
            // Given
            val expected =
                listOf(
                    IslandMastery("look_island", userId, 30, 15, 5, 2, 0.5),
                    IslandMastery("make_lake", userId, 30, 5, 8, 1, 0.17),
                )
            coEvery { islandMasteryDao.getAllIslandMastery(userId) } returns expected

            // When
            val result = repository.getAllIslandMastery(userId)

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `getUnlockedIslands delegates to dao`() =
        runTest {
            // Given
            val expected =
                listOf(
                    IslandMastery(
                        "look_island",
                        userId,
                        30,
                        30,
                        5,
                        5,
                        1.0,
                        isNextIslandUnlocked = true,
                    ),
                )
            coEvery { islandMasteryDao.getUnlockedIslands(userId) } returns expected

            // When
            val result = repository.getUnlockedIslands(userId)

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `getMasteredIslands delegates to dao`() =
        runTest {
            // Given
            val minPercentage = 80.0
            val expected =
                listOf(
                    IslandMastery(
                        "look_island",
                        userId,
                        30,
                        30,
                        5,
                        5,
                        1.0,
                        masteryPercentage = 100.0,
                    ),
                )
            coEvery { islandMasteryDao.getMasteredIslands(userId, minPercentage) } returns expected

            // When
            val result = repository.getMasteredIslands(userId, minPercentage)

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `getUnlockedIslandCount delegates to dao`() =
        runTest {
            // Given
            val expected = 2
            coEvery { islandMasteryDao.getUnlockedIslandCount(userId) } returns expected

            // When
            val result = repository.getUnlockedIslandCount(userId)

            // Then
            assertEquals(expected, result)
        }

    @Test
    fun `getMasteredIslandCount delegates to dao`() =
        runTest {
            // Given
            val expected = 1
            coEvery { islandMasteryDao.getMasteredIslandCount(userId) } returns expected

            // When
            val result = repository.getMasteredIslandCount(userId)

            // Then
            assertEquals(expected, result)
        }
}
