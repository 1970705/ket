package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GlobalStatistics
import com.wordland.domain.model.statistics.UserLevel
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetGlobalStatisticsUseCase
 * Tests getting global statistics and derived metrics
 */
class GetGlobalStatisticsUseCaseTest {
    private lateinit var getGlobalStatisticsUseCase: GetGlobalStatisticsUseCase
    private lateinit var statisticsRepository: StatisticsRepository

    private val testUserId = "test_user"

    private lateinit var testGlobalStats: GlobalStatistics

    @Before
    fun setup() {
        statisticsRepository = mockk(relaxed = true)

        getGlobalStatisticsUseCase = GetGlobalStatisticsUseCase(statisticsRepository)

        testGlobalStats =
            GlobalStatistics(
                userId = testUserId,
                totalGames = 50,
                totalScore = 5000,
                totalPerfectGames = 15,
                totalStudyTime = 3600000L, // 1 hour
                currentStreak = 7,
                longestStreak = 14,
                lastStudyDate = System.currentTimeMillis(),
                totalLevelsCompleted = 10,
                totalLevelsPerfected = 5,
                totalWordsMastered = 75,
                totalCorrectAnswers = 200,
                totalPracticeSessions = 25,
                firstUsedAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                lastUpdatedAt = System.currentTimeMillis(),
            )

        coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
            Result.Success(
                testGlobalStats,
            )
        every { statisticsRepository.getGlobalStatisticsFlow(testUserId) } returns
            flowOf(
                testGlobalStats,
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns global statistics successfully`() =
        runTest {
            val result = getGlobalStatisticsUseCase(testUserId)

            assertTrue(result is Result.Success)
            val stats = (result as Result.Success).data
            assertEquals(testUserId, stats.userId)
            assertEquals(50, stats.totalGames)
            assertEquals(5000, stats.totalScore)
        }

    @Test
    fun `invoke returns Error when repository fails`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Database error"),
                )

            val result = getGlobalStatisticsUseCase(testUserId)

            assertTrue(result is Result.Error)
        }

    @Test
    fun `getFlow returns statistics flow`() =
        runTest {
            val flow = getGlobalStatisticsUseCase.getFlow(testUserId)

            // Collect the flow
            val collected = mutableListOf<GlobalStatistics?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(testGlobalStats, collected[0])
        }

    @Test
    fun `getCurrentStreak returns correct streak`() =
        runTest {
            val streak = getGlobalStatisticsUseCase.getCurrentStreak(testUserId)

            assertEquals(7, streak)
        }

    @Test
    fun `getCurrentStreak returns 0 when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val streak = getGlobalStatisticsUseCase.getCurrentStreak(testUserId)

            assertEquals(0, streak)
        }

    @Test
    fun `getLongestStreak returns correct longest streak`() =
        runTest {
            val longestStreak = getGlobalStatisticsUseCase.getLongestStreak(testUserId)

            assertEquals(14, longestStreak)
        }

    @Test
    fun `getLongestStreak returns 0 when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val longestStreak = getGlobalStatisticsUseCase.getLongestStreak(testUserId)

            assertEquals(0, longestStreak)
        }

    @Test
    fun `getTotalGames returns correct total games`() =
        runTest {
            val totalGames = getGlobalStatisticsUseCase.getTotalGames(testUserId)

            assertEquals(50, totalGames)
        }

    @Test
    fun `getTotalGames returns 0 when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val totalGames = getGlobalStatisticsUseCase.getTotalGames(testUserId)

            assertEquals(0, totalGames)
        }

    @Test
    fun `getAverageScore calculates correctly`() =
        runTest {
            val avgScore = getGlobalStatisticsUseCase.getAverageScore(testUserId)

            // 5000 / 50 = 100
            assertEquals(100f, avgScore, 0.01f)
        }

    @Test
    fun `getAverageScore returns 0 when no games`() =
        runTest {
            val zeroGamesStats = testGlobalStats.copy(totalGames = 0, totalScore = 0)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    zeroGamesStats,
                )

            val avgScore = getGlobalStatisticsUseCase.getAverageScore(testUserId)

            assertEquals(0f, avgScore, 0.01f)
        }

    @Test
    fun `getStudyTimeHours calculates correctly`() =
        runTest {
            val hours = getGlobalStatisticsUseCase.getStudyTimeHours(testUserId)

            // 3600000ms = 1 hour
            assertEquals(1f, hours, 0.01f)
        }

    @Test
    fun `getStudyTimeHours converts milliseconds to hours`() =
        runTest {
            val twoHoursStats = testGlobalStats.copy(totalStudyTime = 7200000L) // 2 hours
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    twoHoursStats,
                )

            val hours = getGlobalStatisticsUseCase.getStudyTimeHours(testUserId)

            assertEquals(2f, hours, 0.01f)
        }

    @Test
    fun `isOnStreak returns true when current streak is positive`() =
        runTest {
            val onStreak = getGlobalStatisticsUseCase.isOnStreak(testUserId)

            assertTrue(onStreak)
        }

    @Test
    fun `isOnStreak returns false when current streak is 0`() =
        runTest {
            val noStreakStats = testGlobalStats.copy(currentStreak = 0)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    noStreakStats,
                )

            val onStreak = getGlobalStatisticsUseCase.isOnStreak(testUserId)

            assertFalse(onStreak)
        }

    @Test
    fun `isOnStreak returns false when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val onStreak = getGlobalStatisticsUseCase.isOnStreak(testUserId)

            assertFalse(onStreak)
        }

    @Test
    fun `getUserLevel returns NEWCOMER for less than 5 words`() =
        runTest {
            val newcomerStats = testGlobalStats.copy(totalWordsMastered = 3)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    newcomerStats,
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.NEWCOMER, level)
        }

    @Test
    fun `getUserLevel returns BEGINNER for 5-19 words`() =
        runTest {
            val beginnerStats = testGlobalStats.copy(totalWordsMastered = 10)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    beginnerStats,
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.BEGINNER, level)
        }

    @Test
    fun `getUserLevel returns INTERMEDIATE for 20-49 words`() =
        runTest {
            val intermediateStats = testGlobalStats.copy(totalWordsMastered = 35)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    intermediateStats,
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.INTERMEDIATE, level)
        }

    @Test
    fun `getUserLevel returns ADVANCED for 50-99 words`() =
        runTest {
            val advancedStats = testGlobalStats.copy(totalWordsMastered = 75)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    advancedStats,
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.ADVANCED, level)
        }

    @Test
    fun `getUserLevel returns EXPERT for 100+ words`() =
        runTest {
            val expertStats = testGlobalStats.copy(totalWordsMastered = 120)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    expertStats,
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.EXPERT, level)
        }

    @Test
    fun `getUserLevel returns NEWCOMER when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val level = getGlobalStatisticsUseCase.getUserLevel(testUserId)

            assertEquals(UserLevel.NEWCOMER, level)
        }

    @Test
    fun `getProgressToNextLevel calculates for NEWCOMER`() =
        runTest {
            val newcomerStats = testGlobalStats.copy(totalWordsMastered = 2)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    newcomerStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            // 2 / 5 = 0.4
            assertEquals(0.4f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getProgressToNextLevel calculates for BEGINNER`() =
        runTest {
            val beginnerStats = testGlobalStats.copy(totalWordsMastered = 12)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    beginnerStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            // (12 - 5) / (20 - 5) = 7 / 15 ≈ 0.47
            assertEquals(0.47f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getProgressToNextLevel calculates for INTERMEDIATE`() =
        runTest {
            val intermediateStats = testGlobalStats.copy(totalWordsMastered = 35)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    intermediateStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            // (35 - 20) / (50 - 20) = 15 / 30 = 0.5
            assertEquals(0.5f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getProgressToNextLevel calculates for ADVANCED`() =
        runTest {
            val advancedStats = testGlobalStats.copy(totalWordsMastered = 75)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    advancedStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            // (75 - 50) / (100 - 50) = 25 / 50 = 0.5
            assertEquals(0.5f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getProgressToNextLevel returns null for EXPERT`() =
        runTest {
            val expertStats = testGlobalStats.copy(totalWordsMastered = 120)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    expertStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            assertNull(progress)
        }

    @Test
    fun `getProgressToNextLevel returns 0 when Error`() =
        runTest {
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Error(
                    RuntimeException("Error"),
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            assertEquals(0f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getProgressToNextLevel at level boundary`() =
        runTest {
            val boundaryStats = testGlobalStats.copy(totalWordsMastered = 5)
            coEvery { statisticsRepository.getGlobalStatistics(testUserId) } returns
                Result.Success(
                    boundaryStats,
                )

            val progress = getGlobalStatisticsUseCase.getProgressToNextLevel(testUserId)

            // At exactly 5, should start at 0 for BEGINNER
            assertEquals(0f, progress ?: 0f, 0.01f)
        }

    @Test
    fun `getFlow returns null when statistics not exist`() =
        runTest {
            every { statisticsRepository.getGlobalStatisticsFlow(testUserId) } returns flowOf(null)

            val flow = getGlobalStatisticsUseCase.getFlow(testUserId)

            val collected = mutableListOf<GlobalStatistics?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNull(collected[0])
        }
}
