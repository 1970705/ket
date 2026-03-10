package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.LevelStatistics
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetLevelStatisticsUseCase basic invoke and flow operations.
 * Tests cover invoke(), getFlow(), getByIsland(), and count methods.
 */
class GetLevelStatisticsUseCaseBasicTest {
    private lateinit var getLevelStatisticsUseCase: GetLevelStatisticsUseCase
    private lateinit var statisticsRepository: StatisticsRepository

    private val testUserId = "test_user"
    private val testLevelId = "look_island_level_1"
    private val testIslandId = "look_island"

    private lateinit var testLevelStats: LevelStatistics

    @Before
    fun setup() {
        statisticsRepository = mockk(relaxed = true)

        getLevelStatisticsUseCase = GetLevelStatisticsUseCase(statisticsRepository)

        val now = System.currentTimeMillis()
        testLevelStats =
            LevelStatistics(
                userId = testUserId,
                levelId = testLevelId,
                totalGames = 10,
                completedGames = 8,
                perfectGames = 3,
                highestScore = 200,
                lowestScore = 50,
                averageScore = 150f,
                totalScore = 1500,
                bestTime = 45000L,
                worstTime = 120000L,
                averageTime = 75000,
                totalTime = 750000L,
                totalCorrect = 45,
                totalQuestions = 60,
                overallAccuracy = 0.75f,
                bestCombo = 8,
                firstPlayedAt = now - 7 * 24 * 60 * 60 * 1000L,
                lastPlayedAt = now,
                lastUpdatedAt = now,
            )

        coEvery {
            statisticsRepository.getLevelStatisticsSync(
                testUserId,
                testLevelId,
            )
        } returns Result.Success(testLevelStats)
        every {
            statisticsRepository.getLevelStatistics(
                testUserId,
                testLevelId,
            )
        } returns flowOf(testLevelStats)
        every {
            statisticsRepository.getLevelStatisticsByIsland(
                testUserId,
                testIslandId,
            )
        } returns flowOf(listOf(testLevelStats))
        every { statisticsRepository.getCompletedLevelsCount(testUserId) } returns flowOf(8)
        every { statisticsRepository.getPerfectedLevelsCount(testUserId) } returns flowOf(3)
        every {
            statisticsRepository.getBestPerformingLevels(
                testUserId,
                any(),
            )
        } returns flowOf(listOf(testLevelStats))
        every {
            statisticsRepository.getLevelsNeedingPractice(
                testUserId,
                any(),
            )
        } returns flowOf(listOf(testLevelStats))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== invoke() Tests ====================

    @Test
    fun `invoke returns level statistics successfully`() =
        runTest {
            val result = getLevelStatisticsUseCase(testUserId, testLevelId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(testLevelStats, stats)
        }

    @Test
    fun `invoke returns Error when repository fails`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Database error"))

            val result = getLevelStatisticsUseCase(testUserId, testLevelId)

            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns Success with null when statistics not exist`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val result = getLevelStatisticsUseCase(testUserId, testLevelId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(null, stats)
        }

    // ==================== getFlow() Tests ====================

    @Test
    fun `getFlow returns statistics flow`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getFlow(testUserId, testLevelId)

            val collected = mutableListOf<LevelStatistics?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(testLevelStats, collected[0])
        }

    @Test
    fun `getFlow emits null when statistics not exist`() =
        runTest {
            every {
                statisticsRepository.getLevelStatistics(
                    testUserId,
                    testLevelId,
                )
            } returns flowOf(null)

            val flow = getLevelStatisticsUseCase.getFlow(testUserId, testLevelId)

            val collected = mutableListOf<LevelStatistics?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNull(collected[0])
        }

    @Test
    fun `getFlow emits multiple values when repository updates`() =
        runTest {
            val updatedStats = testLevelStats.copy(totalGames = 15)
            every {
                statisticsRepository.getLevelStatistics(
                    testUserId,
                    testLevelId,
                )
            } returns flowOf(testLevelStats, updatedStats)

            val flow = getLevelStatisticsUseCase.getFlow(testUserId, testLevelId)

            val collected = mutableListOf<LevelStatistics?>()
            flow.collect { collected.add(it) }

            assertEquals(2, collected.size)
            assertEquals(testLevelStats, collected[0])
            assertEquals(updatedStats, collected[1])
        }

    // ==================== getByIsland() Tests ====================

    @Test
    fun `getByIsland returns island level statistics`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getByIsland(testUserId, testIslandId)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)
            assertEquals(testLevelStats, collected[0][0])
        }

    @Test
    fun `getByIsland returns empty list when no levels`() =
        runTest {
            every {
                statisticsRepository.getLevelStatisticsByIsland(
                    testUserId,
                    testIslandId,
                )
            } returns flowOf(emptyList())

            val flow = getLevelStatisticsUseCase.getByIsland(testUserId, testIslandId)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0].size)
        }

    @Test
    fun `getByIsland returns multiple levels`() =
        runTest {
            val multipleLevels =
                listOf(
                    testLevelStats,
                    testLevelStats.copy(levelId = "level_2"),
                    testLevelStats.copy(levelId = "level_3"),
                )
            every {
                statisticsRepository.getLevelStatisticsByIsland(
                    testUserId,
                    testIslandId,
                )
            } returns flowOf(multipleLevels)

            val flow = getLevelStatisticsUseCase.getByIsland(testUserId, testIslandId)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(3, collected[0].size)
        }

    // ==================== getCompletedCount() Tests ====================

    @Test
    fun `getCompletedCount returns completed levels count`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getCompletedCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(8, collected[0])
        }

    @Test
    fun `getCompletedCount returns zero when none completed`() =
        runTest {
            every { statisticsRepository.getCompletedLevelsCount(testUserId) } returns flowOf(0)

            val flow = getLevelStatisticsUseCase.getCompletedCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0])
        }

    // ==================== getPerfectedCount() Tests ====================

    @Test
    fun `getPerfectedCount returns perfected levels count`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getPerfectedCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(3, collected[0])
        }

    @Test
    fun `getPerfectedCount returns zero when none perfected`() =
        runTest {
            every { statisticsRepository.getPerfectedLevelsCount(testUserId) } returns flowOf(0)

            val flow = getLevelStatisticsUseCase.getPerfectedCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0])
        }

    // ==================== getBestPerforming() Tests ====================

    @Test
    fun `getBestPerforming returns best levels`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getBestPerforming(testUserId, limit = 5)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)
        }

    @Test
    fun `getBestPerforming respects limit parameter`() =
        runTest {
            val multipleLevels =
                listOf(
                    testLevelStats,
                    testLevelStats.copy(levelId = "level_2"),
                    testLevelStats.copy(levelId = "level_3"),
                )
            every {
                statisticsRepository.getBestPerformingLevels(
                    testUserId,
                    2,
                )
            } returns flowOf(multipleLevels.take(2))

            val flow = getLevelStatisticsUseCase.getBestPerforming(testUserId, limit = 2)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(2, collected[0].size)
        }

    @Test
    fun `getBestPerforming returns empty list when no levels`() =
        runTest {
            every {
                statisticsRepository.getBestPerformingLevels(
                    testUserId,
                    any(),
                )
            } returns flowOf(emptyList())

            val flow = getLevelStatisticsUseCase.getBestPerforming(testUserId, limit = 5)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0].size)
        }

    // ==================== getLevelsNeedingPractice() Tests ====================

    @Test
    fun `getLevelsNeedingPractice returns levels needing practice`() =
        runTest {
            val flow = getLevelStatisticsUseCase.getLevelsNeedingPractice(testUserId, limit = 5)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)
        }

    @Test
    fun `getLevelsNeedingPractice respects limit parameter`() =
        runTest {
            val multipleLevels =
                listOf(
                    testLevelStats,
                    testLevelStats.copy(levelId = "level_2"),
                    testLevelStats.copy(levelId = "level_3"),
                )
            every {
                statisticsRepository.getLevelsNeedingPractice(
                    testUserId,
                    3,
                )
            } returns flowOf(multipleLevels.take(3))

            val flow = getLevelStatisticsUseCase.getLevelsNeedingPractice(testUserId, limit = 3)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(3, collected[0].size)
        }

    @Test
    fun `getLevelsNeedingPractice returns empty list when no levels`() =
        runTest {
            every {
                statisticsRepository.getLevelsNeedingPractice(
                    testUserId,
                    any(),
                )
            } returns flowOf(emptyList())

            val flow = getLevelStatisticsUseCase.getLevelsNeedingPractice(testUserId, limit = 5)

            val collected = mutableListOf<List<LevelStatistics>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0].size)
        }
}
