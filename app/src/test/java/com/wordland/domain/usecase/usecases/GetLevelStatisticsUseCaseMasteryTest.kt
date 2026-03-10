package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.LevelStatistics
import com.wordland.domain.model.statistics.PerformanceTier
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetLevelStatisticsUseCase mastery detection methods.
 * Tests cover isMastered() and getPerformanceTier() methods.
 */
class GetLevelStatisticsUseCaseMasteryTest {
    private lateinit var getLevelStatisticsUseCase: GetLevelStatisticsUseCase
    private lateinit var statisticsRepository: StatisticsRepository

    private val testUserId = "test_user"
    private val testLevelId = "look_island_level_1"

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
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== isMastered() Tests ====================

    @Test
    fun `isMastered returns true when level is mastered`() =
        runTest {
            val masteredStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 8,
                    overallAccuracy = 0.9f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(masteredStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertTrue(isMastered)
        }

    @Test
    fun `isMastered returns false when level not mastered`() =
        runTest {
            val notMasteredStats = testLevelStats.copy(overallAccuracy = 0.5f)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(notMasteredStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered returns false when no statistics`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered returns false when Error`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Error"))

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered requires minimum 3 games played`() =
        runTest {
            val lowGamesStats =
                testLevelStats.copy(
                    totalGames = 2,
                    completedGames = 2,
                    overallAccuracy = 1.0f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(lowGamesStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered requires minimum completion rate`() =
        runTest {
            val lowCompletionStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 5, // 50% completion
                    overallAccuracy = 0.9f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(lowCompletionStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered requires at least one perfect game`() =
        runTest {
            val noPerfectStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 8,
                    perfectGames = 0,
                    overallAccuracy = 0.9f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(noPerfectStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertFalse(isMastered)
        }

    @Test
    fun `isMastered returns true with perfect stats`() =
        runTest {
            val perfectStats =
                testLevelStats.copy(
                    totalGames = 5,
                    completedGames = 5,
                    perfectGames = 5,
                    overallAccuracy = 1.0f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(perfectStats)

            val isMastered = getLevelStatisticsUseCase.isMastered(testUserId, testLevelId)

            assertTrue(isMastered)
        }

    // ==================== getPerformanceTier() Tests ====================

    @Test
    fun `getPerformanceTier returns EXPERT for mastered level with high perfect rate`() =
        runTest {
            val expertStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 8,
                    perfectGames = 6,
                    overallAccuracy = 0.9f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(expertStats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.EXPERT, tier)
        }

    @Test
    fun `getPerformanceTier returns ADVANCED for mastered level`() =
        runTest {
            val advancedStats =
                testLevelStats.copy(
                    totalGames = 5,
                    completedGames = 4,
                    perfectGames = 1,
                    overallAccuracy = 0.85f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(advancedStats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.ADVANCED, tier)
        }

    @Test
    fun `getPerformanceTier returns INTERMEDIATE for moderate performance`() =
        runTest {
            val intermediateStats =
                testLevelStats.copy(
                    totalGames = 3,
                    completedGames = 2,
                    overallAccuracy = 0.6f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(intermediateStats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.INTERMEDIATE, tier)
        }

    @Test
    fun `getPerformanceTier returns BEGINNER for few plays`() =
        runTest {
            val beginnerStats =
                testLevelStats.copy(
                    totalGames = 1,
                    completedGames = 1,
                    overallAccuracy = 0.7f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(beginnerStats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.BEGINNER, tier)
        }

    @Test
    fun `getPerformanceTier returns NOT_STARTED for no games`() =
        runTest {
            val noGamesStats = testLevelStats.copy(totalGames = 0)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(noGamesStats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.NOT_STARTED, tier)
        }

    @Test
    fun `getPerformanceTier returns null when no statistics`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertNull(tier)
        }

    @Test
    fun `getPerformanceTier returns EXPERT with 50 percent perfect rate`() =
        runTest {
            val stats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 8,
                    perfectGames = 5,
                    overallAccuracy = 0.85f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(stats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.EXPERT, tier)
        }

    @Test
    fun `getPerformanceTier returns ADVANCED below 50 percent perfect rate`() =
        runTest {
            val stats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 8,
                    perfectGames = 3,
                    overallAccuracy = 0.85f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(stats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.ADVANCED, tier)
        }

    @Test
    fun `getPerformanceTier handles high accuracy low completion`() =
        runTest {
            val stats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 5,
                    overallAccuracy = 0.9f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(stats)

            val tier = getLevelStatisticsUseCase.getPerformanceTier(testUserId, testLevelId)

            assertEquals(PerformanceTier.INTERMEDIATE, tier)
        }
}
