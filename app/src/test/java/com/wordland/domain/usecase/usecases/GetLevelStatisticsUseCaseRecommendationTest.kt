package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.LevelStatistics
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetLevelStatisticsUseCase recommendation logic.
 * Tests cover getRecommendation() method with all recommendation types.
 */
class GetLevelStatisticsUseCaseRecommendationTest {
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

    // ==================== MASTERED Recommendation Tests ====================

    @Test
    fun `getRecommendation returns MASTERED for mastered level`() =
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

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.MASTERED, recommendation)
        }

    @Test
    fun `getRecommendation returns MASTERED for perfect level`() =
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

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.MASTERED, recommendation)
        }

    // ==================== START_PLAYING Recommendation Tests ====================

    @Test
    fun `getRecommendation returns START_PLAYING for no games`() =
        runTest {
            val noGamesStats = testLevelStats.copy(totalGames = 0)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(noGamesStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.START_PLAYING, recommendation)
        }

    @Test
    fun `getRecommendation returns START_PLAYING for null statistics`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(null)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.START_PLAYING, recommendation)
        }

    @Test
    fun `getRecommendation returns START_PLAYING on Error`() =
        runTest {
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Error(Exception("Error"))

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.START_PLAYING, recommendation)
        }

    // ==================== GOOD_PROGRESS Recommendation Tests ====================

    @Test
    fun `getRecommendation returns GOOD_PROGRESS for good performance`() =
        runTest {
            val goodStats =
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
            } returns Result.Success(goodStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            // This is mastered (completion 0.8, accuracy 0.85, games >= 3, perfect >= 1)
            assertEquals(LevelRecommendation.MASTERED, recommendation)
        }

    @Test
    fun `getRecommendation returns GOOD_PROGRESS for moderate performance`() =
        runTest {
            val moderateStats =
                testLevelStats.copy(
                    totalGames = 5,
                    completedGames = 3,
                    perfectGames = 0,
                    overallAccuracy = 0.75f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(moderateStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.GOOD_PROGRESS, recommendation)
        }

    // ==================== NEEDS_REVIEW Recommendation Tests ====================

    @Test
    fun `getRecommendation returns NEEDS_REVIEW for low accuracy`() =
        runTest {
            val lowAccuracyStats =
                testLevelStats.copy(overallAccuracy = 0.4f, totalGames = 3)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(lowAccuracyStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.NEEDS_REVIEW, recommendation)
        }

    @Test
    fun `getRecommendation returns NEEDS_REVIEW for very low accuracy`() =
        runTest {
            val veryLowAccuracyStats =
                testLevelStats.copy(overallAccuracy = 0.2f, totalGames = 5)
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(veryLowAccuracyStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.NEEDS_REVIEW, recommendation)
        }

    @Test
    fun `getRecommendation returns NEEDS_REVIEW for poor completion`() =
        runTest {
            val poorCompletionStats =
                testLevelStats.copy(
                    totalGames = 10,
                    completedGames = 3,
                    overallAccuracy = 0.5f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(poorCompletionStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.NEEDS_REVIEW, recommendation)
        }

    // ==================== PLAY_MORE Recommendation Tests ====================

    @Test
    fun `getRecommendation returns PLAY_MORE for few games`() =
        runTest {
            val fewGamesStats =
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
            } returns Result.Success(fewGamesStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.PLAY_MORE, recommendation)
        }

    @Test
    fun `getRecommendation returns PLAY_MORE for two games`() =
        runTest {
            val twoGamesStats =
                testLevelStats.copy(
                    totalGames = 2,
                    completedGames = 2,
                    overallAccuracy = 0.8f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(twoGamesStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.PLAY_MORE, recommendation)
        }

    // ==================== IMPROVING Recommendation Tests ====================

    @Test
    fun `getRecommendation returns IMPROVING for moderate stats`() =
        runTest {
            val moderateStats =
                testLevelStats.copy(
                    totalGames = 5,
                    completedGames = 3,
                    overallAccuracy = 0.65f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(moderateStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.IMPROVING, recommendation)
        }

    @Test
    fun `getRecommendation returns IMPROVING for mid range accuracy`() =
        runTest {
            val midRangeStats =
                testLevelStats.copy(
                    totalGames = 8,
                    completedGames = 5,
                    overallAccuracy = 0.6f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(midRangeStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.IMPROVING, recommendation)
        }

    @Test
    fun `getRecommendation returns IMPROVING for decent but not mastered`() =
        runTest {
            val decentStats =
                testLevelStats.copy(
                    totalGames = 6,
                    completedGames = 4,
                    perfectGames = 0,
                    overallAccuracy = 0.7f,
                )
            coEvery {
                statisticsRepository.getLevelStatisticsSync(
                    testUserId,
                    testLevelId,
                )
            } returns Result.Success(decentStats)

            val recommendation =
                getLevelStatisticsUseCase.getRecommendation(testUserId, testLevelId)

            assertEquals(LevelRecommendation.IMPROVING, recommendation)
        }
}
