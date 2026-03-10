package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.LevelStatistics
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetLevelStatisticsUseCase metrics calculation methods.
 * Tests cover getAverageScore(), getBestTime(), and getCompletionRate() methods.
 */
class GetLevelStatisticsUseCaseMetricsTest {
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

    // ==================== getAverageScore() Tests ====================

    @Test
    fun `getAverageScore calculates correctly`() =
        runTest {
            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            // 1500 / 10 = 150
            assertEquals(150f, avgScore ?: 0f, 0.01f)
        }

    @Test
    fun `getAverageScore returns null when no games played`() =
        runTest {
            val noGamesStats = testLevelStats.copy(totalGames = 0, totalScore = 0)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(noGamesStats)

            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            assertNull(avgScore)
        }

    @Test
    fun `getAverageScore returns null when no statistics`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            assertNull(avgScore)
        }

    @Test
    fun `getAverageScore returns null on Error`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Error"))

            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            assertNull(avgScore)
        }

    @Test
    fun `getAverageScore handles high scores`() =
        runTest {
            val highScoreStats =
                testLevelStats.copy(
                    totalGames = 5,
                    totalScore = 1000,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(highScoreStats)

            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            assertEquals(200f, avgScore ?: 0f, 0.01f)
        }

    @Test
    fun `getAverageScore handles single game`() =
        runTest {
            val singleGameStats =
                testLevelStats.copy(
                    totalGames = 1,
                    totalScore = 180,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(singleGameStats)

            val avgScore = getLevelStatisticsUseCase.getAverageScore(testUserId, testLevelId)

            assertEquals(180f, avgScore ?: 0f, 0.01f)
        }

    // ==================== getBestTime() Tests ====================

    @Test
    fun `getBestTime returns best time`() =
        runTest {
            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertEquals(45000L, bestTime)
        }

    @Test
    fun `getBestTime returns null when not completed`() =
        runTest {
            val notCompletedStats = testLevelStats.copy(bestTime = null)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(notCompletedStats)

            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertNull(bestTime)
        }

    @Test
    fun `getBestTime returns null when no statistics`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertNull(bestTime)
        }

    @Test
    fun `getBestTime returns null on Error`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Error"))

            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertNull(bestTime)
        }

    @Test
    fun `getBestTime handles very fast completion`() =
        runTest {
            val fastStats = testLevelStats.copy(bestTime = 30000L)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(fastStats)

            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertEquals(30000L, bestTime)
        }

    @Test
    fun `getBestTime handles slow completion`() =
        runTest {
            val slowStats = testLevelStats.copy(bestTime = 180000L)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(slowStats)

            val bestTime = getLevelStatisticsUseCase.getBestTime(testUserId, testLevelId)

            assertEquals(180000L, bestTime)
        }

    // ==================== getCompletionRate() Tests ====================

    @Test
    fun `getCompletionRate calculates correctly`() =
        runTest {
            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            // 8 / 10 = 0.8
            assertEquals(0.8f, completionRate ?: 0f, 0.01f)
        }

    @Test
    fun `getCompletionRate returns null when no games`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            assertNull(completionRate)
        }

    @Test
    fun `getCompletionRate returns null on Error`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Error"))

            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            assertNull(completionRate)
        }

    @Test
    fun `getCompletionRate handles perfect completion`() =
        runTest {
            val perfectCompletionStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 10,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(perfectCompletionStats)

            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            assertEquals(1.0f, completionRate ?: 0f, 0.01f)
        }

    @Test
    fun `getCompletionRate handles low completion`() =
        runTest {
            val lowCompletionStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 3,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(lowCompletionStats)

            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            assertEquals(0.3f, completionRate ?: 0f, 0.01f)
        }

    @Test
    fun `getCompletionRate handles zero completed games`() =
        runTest {
            val noCompletionStats =
                testLevelStats.copy(
                    totalGames = 5,
                    completedGames = 0,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(noCompletionStats)

            val completionRate =
                getLevelStatisticsUseCase.getCompletionRate(testUserId, testLevelId)

            assertEquals(0.0f, completionRate ?: 0f, 0.01f)
        }
}
