package com.wordland.data.repository

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.domain.model.IslandMastery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for IslandMasteryRepository query and delegation methods.
 * Tests cover get*, insert*, update*, and count methods.
 */
class IslandMasteryRepositoryQueryTest {
    private lateinit var repository: IslandMasteryRepositoryImpl
    private lateinit var islandMasteryDao: IslandMasteryDao
    private lateinit var progressDao: ProgressDao

    private val userId = "user_001"
    private val islandId = "look_island"

    @Before
    fun setup() {
        islandMasteryDao = mockk(relaxed = true)
        progressDao = mockk(relaxed = true)
        repository = IslandMasteryRepositoryImpl(islandMasteryDao, progressDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== getIslandMastery Tests ====================

    @Test
    fun `getIslandMastery returns mastery from dao`() =
        runTest {
            // Given
            val expected =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = 30,
                    masteredWords = 15,
                    totalLevels = 5,
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
    fun `getIslandMastery delegates correctly for different island`() =
        runTest {
            val otherIslandId = "make_lake"
            val expected =
                IslandMastery(
                    islandId = otherIslandId,
                    userId = userId,
                    totalWords = 30,
                    masteredWords = 10,
                    totalLevels = 5,
                    completedLevels = 1,
                    crossSceneScore = 0.3,
                )
            coEvery { islandMasteryDao.getIslandMastery(userId, otherIslandId) } returns expected

            val result = repository.getIslandMastery(userId, otherIslandId)

            assertEquals(expected, result)
        }

    // ==================== insertIslandMastery Tests ====================

    @Test
    fun `insertIslandMastery delegates to dao`() =
        runTest {
            // Given
            val mastery =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = 30,
                    masteredWords = 0,
                    totalLevels = 5,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                )
            coEvery { islandMasteryDao.insertIslandMastery(mastery) } returns Unit

            // When
            repository.insertIslandMastery(mastery)

            // Then
            coVerify { islandMasteryDao.insertIslandMastery(mastery) }
        }

    // ==================== updateIslandMastery Tests ====================

    @Test
    fun `updateIslandMastery delegates to dao`() =
        runTest {
            // Given
            val mastery =
                IslandMastery(
                    islandId = islandId,
                    userId = userId,
                    totalWords = 30,
                    masteredWords = 15,
                    totalLevels = 5,
                    completedLevels = 2,
                    crossSceneScore = 0.5,
                )
            coEvery { islandMasteryDao.updateIslandMastery(mastery) } returns Unit

            // When
            repository.updateIslandMastery(mastery)

            // Then
            coVerify { islandMasteryDao.updateIslandMastery(mastery) }
        }

    // ==================== getAllIslandMastery Tests ====================

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
    fun `getAllIslandMastery returns empty list when none exist`() =
        runTest {
            coEvery { islandMasteryDao.getAllIslandMastery(userId) } returns emptyList()

            val result = repository.getAllIslandMastery(userId)

            assertTrue(result.isEmpty())
        }

    // ==================== getUnlockedIslands Tests ====================

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
    fun `getUnlockedIslands returns empty list when none unlocked`() =
        runTest {
            coEvery { islandMasteryDao.getUnlockedIslands(userId) } returns emptyList()

            val result = repository.getUnlockedIslands(userId)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getUnlockedIslands returns multiple unlocked islands`() =
        runTest {
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
                    IslandMastery(
                        "make_lake",
                        userId,
                        30,
                        25,
                        8,
                        6,
                        0.85,
                        isNextIslandUnlocked = true,
                    ),
                )
            coEvery { islandMasteryDao.getUnlockedIslands(userId) } returns expected

            val result = repository.getUnlockedIslands(userId)

            assertEquals(2, result.size)
        }

    // ==================== getMasteredIslands Tests ====================

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
    fun `getMasteredIslands returns empty list when none mastered`() =
        runTest {
            val minPercentage = 90.0
            coEvery { islandMasteryDao.getMasteredIslands(userId, minPercentage) } returns emptyList()

            val result = repository.getMasteredIslands(userId, minPercentage)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getMasteredIslands respects minimum percentage`() =
        runTest {
            val minPercentage = 75.0
            val expected =
                listOf(
                    IslandMastery(
                        "look_island",
                        userId,
                        30,
                        28,
                        5,
                        5,
                        0.95,
                        masteryPercentage = 95.0,
                    ),
                )
            coEvery { islandMasteryDao.getMasteredIslands(userId, minPercentage) } returns expected

            val result = repository.getMasteredIslands(userId, minPercentage)

            assertEquals(expected, result)
        }

    // ==================== getUnlockedIslandCount Tests ====================

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
    fun `getUnlockedIslandCount returns zero when none unlocked`() =
        runTest {
            coEvery { islandMasteryDao.getUnlockedIslandCount(userId) } returns 0

            val result = repository.getUnlockedIslandCount(userId)

            assertEquals(0, result)
        }

    // ==================== getMasteredIslandCount Tests ====================

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

    @Test
    fun `getMasteredIslandCount returns zero when none mastered`() =
        runTest {
            coEvery { islandMasteryDao.getMasteredIslandCount(userId) } returns 0

            val result = repository.getMasteredIslandCount(userId)

            assertEquals(0, result)
        }
}
