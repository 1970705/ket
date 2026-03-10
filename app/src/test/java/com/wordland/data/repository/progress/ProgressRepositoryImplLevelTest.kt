package com.wordland.data.repository.progress

import com.wordland.data.dao.ProgressDao
import com.wordland.data.repository.ProgressRepositoryImpl
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProgressRepositoryImpl - Level Progress and Statistics
 * Tests level progress CRUD operations and statistics queries
 */
class ProgressRepositoryImplLevelTest {
    private lateinit var repository: ProgressRepositoryImpl
    private lateinit var progressDao: ProgressDao

    private val testUserId = "test_user"
    private val testLevelId = "test_level"
    private val testIslandId = "test_island"

    @Before
    fun setup() {
        progressDao = mockk(relaxed = true)
        repository = ProgressRepositoryImpl(progressDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Level Progress Tests ===

    @Test
    fun `getLevelProgress returns null when not found`() =
        runTest {
            coEvery { progressDao.getLevelProgress(any(), any()) } returns null

            val result = repository.getLevelProgress(testUserId, testLevelId)

            assertNull(result)
        }

    @Test
    fun `getLevelProgress returns progress when exists`() =
        runTest {
            val expectedProgress =
                LevelProgress(
                    levelId = testLevelId,
                    userId = testUserId,
                    islandId = testIslandId,
                    status = LevelStatus.COMPLETED,
                    stars = 2,
                    score = 150,
                    totalTime = 5000L,
                    difficulty = "normal",
                )
            coEvery { progressDao.getLevelProgress(testUserId, testLevelId) } returns expectedProgress

            val result = repository.getLevelProgress(testUserId, testLevelId)

            assertNotNull(result)
            assertEquals(expectedProgress.levelId, result!!.levelId)
            assertEquals(2, result.stars)
        }

    @Test
    fun `getLevelProgressFlow returns flow from DAO`() =
        runTest {
            val expectedProgress =
                LevelProgress(
                    levelId = testLevelId,
                    userId = testUserId,
                    islandId = testIslandId,
                    status = LevelStatus.COMPLETED,
                    stars = 3,
                    score = 200,
                    totalTime = 3000L,
                    difficulty = "normal",
                )
            coEvery { progressDao.getLevelProgressFlow(testUserId, testLevelId) } returns flowOf(expectedProgress)

            val result = repository.getLevelProgressFlow(testUserId, testLevelId).first()

            assertNotNull(result)
            assertEquals(LevelStatus.COMPLETED, result!!.status)
        }

    @Test
    fun `getAllLevelProgress returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = testIslandId,
                        status = LevelStatus.COMPLETED,
                        stars = 3,
                        score = 200,
                        totalTime = 3000L,
                        difficulty = "normal",
                    ),
                )
            coEvery { progressDao.getAllLevelProgress(testUserId) } returns expectedList

            val result = repository.getAllLevelProgress(testUserId)

            assertEquals(1, result.size)
            assertEquals("level1", result[0].levelId)
        }

    @Test
    fun `getUnlockedLevels returns unlocked levels from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = testIslandId,
                        status = LevelStatus.UNLOCKED,
                        stars = 0,
                        score = 0,
                        totalTime = 0L,
                        difficulty = "normal",
                    ),
                )
            coEvery { progressDao.getUnlockedLevels(testUserId) } returns expectedList

            val result = repository.getUnlockedLevels(testUserId)

            assertEquals(1, result.size)
            assertEquals(LevelStatus.UNLOCKED, result[0].status)
        }

    @Test
    fun `getLevelsByIsland returns levels for island from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    LevelProgress(
                        levelId = "level1",
                        userId = testUserId,
                        islandId = testIslandId,
                        status = LevelStatus.COMPLETED,
                        stars = 3,
                        score = 200,
                        totalTime = 3000L,
                        difficulty = "normal",
                    ),
                )
            coEvery { progressDao.getLevelsByIsland(testUserId, testIslandId) } returns expectedList

            val result = repository.getLevelsByIsland(testUserId, testIslandId)

            assertEquals(1, result.size)
            assertEquals(testIslandId, result[0].islandId)
        }

    @Test
    fun `insertLevelProgress calls DAO`() =
        runTest {
            val progress =
                LevelProgress(
                    levelId = testLevelId,
                    userId = testUserId,
                    islandId = testIslandId,
                    status = LevelStatus.LOCKED,
                    stars = 0,
                    score = 0,
                    totalTime = 0L,
                    difficulty = "normal",
                )
            coEvery { progressDao.insertLevelProgress(any()) } just Runs

            repository.insertLevelProgress(progress)

            coVerify { progressDao.insertLevelProgress(progress) }
        }

    @Test
    fun `updateLevelProgress calls DAO`() =
        runTest {
            val progress =
                LevelProgress(
                    levelId = testLevelId,
                    userId = testUserId,
                    islandId = testIslandId,
                    status = LevelStatus.IN_PROGRESS,
                    stars = 1,
                    score = 100,
                    totalTime = 2000L,
                    difficulty = "normal",
                )
            coEvery { progressDao.updateLevelProgress(any()) } just Runs

            repository.updateLevelProgress(progress)

            coVerify { progressDao.updateLevelProgress(progress) }
        }

    @Test
    fun `recordLevelAttempt calls DAO`() =
        runTest {
            coEvery { progressDao.updateLevelAttempt(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs

            repository.recordLevelAttempt(
                userId = testUserId,
                levelId = testLevelId,
                status = LevelStatus.COMPLETED,
                stars = 3,
                score = 250,
                timeSpent = 4000L,
            )

            coVerify {
                progressDao.updateLevelAttempt(
                    userId = testUserId,
                    levelId = testLevelId,
                    status = LevelStatus.COMPLETED,
                    stars = 3,
                    score = 250,
                    timeSpent = 4000L,
                    lastPlayTime = any(),
                    updatedAt = any(),
                )
            }
        }

    // === Statistics Tests ===

    @Test
    fun `getMasteredWordCount returns count from DAO`() =
        runTest {
            coEvery { progressDao.getMasteredWordCount(testUserId) } returns 15

            val result = repository.getMasteredWordCount(testUserId)

            assertEquals(15, result)
        }

    @Test
    fun `getLearningWordCount returns count from DAO`() =
        runTest {
            coEvery { progressDao.getLearningWordCount(testUserId) } returns 25

            val result = repository.getLearningWordCount(testUserId)

            assertEquals(25, result)
        }

    @Test
    fun `getAverageMemoryStrength returns average from DAO`() =
        runTest {
            coEvery { progressDao.getAverageMemoryStrength(testUserId) } returns 65.5

            val result = repository.getAverageMemoryStrength(testUserId)

            assertEquals(65.5, result!!, 0.01)
        }

    @Test
    fun `getAverageMemoryStrength returns null when no progress`() =
        runTest {
            coEvery { progressDao.getAverageMemoryStrength(testUserId) } returns null

            val result = repository.getAverageMemoryStrength(testUserId)

            assertNull(result)
        }

    @Test
    fun `getTotalPracticeCount returns total from DAO`() =
        runTest {
            coEvery { progressDao.getTotalPracticeCount(testUserId) } returns 150

            val result = repository.getTotalPracticeCount(testUserId)

            assertEquals(150, result)
        }
}
