package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.GameHistoryRepository
import com.wordland.data.repository.StatisticsRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GameMode
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RecordGameHistoryUseCase
 * Tests recording game history and updating statistics
 */
class RecordGameHistoryUseCaseTest {
    private lateinit var recordGameHistoryUseCase: RecordGameHistoryUseCase
    private lateinit var gameHistoryRepository: GameHistoryRepository
    private lateinit var statisticsRepository: StatisticsRepository

    private val testUserId = "test_user"
    private val testLevelId = "look_island_level_1"
    private val testIslandId = "look_island"
    private val testGameMode = GameMode.SPELL_BATTLE

    @Before
    fun setup() {
        gameHistoryRepository = mockk(relaxed = true)
        statisticsRepository = mockk(relaxed = true)

        recordGameHistoryUseCase =
            RecordGameHistoryUseCase(
                gameHistoryRepository,
                statisticsRepository,
            )

        // Default successful behaviors
        coEvery { gameHistoryRepository.insertGameHistory(any()) } returns Result.Success(Unit)
        coEvery {
            statisticsRepository.updateGlobalStatistics(
                any(),
                any(),
                any(),
                any(),
            )
        } returns
            Result.Success(
                com.wordland.domain.model.statistics.GlobalStatistics(
                    userId = testUserId,
                    totalGames = 1,
                    totalScore = 100,
                    totalPerfectGames = 0,
                    totalStudyTime = 60000L,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastStudyDate = System.currentTimeMillis(),
                    totalLevelsCompleted = 0,
                    totalLevelsPerfected = 0,
                    totalWordsMastered = 0,
                    totalCorrectAnswers = 5,
                    totalPracticeSessions = 1,
                    firstUsedAt = System.currentTimeMillis(),
                    lastUpdatedAt = System.currentTimeMillis(),
                ),
            )
        coEvery {
            statisticsRepository.updateLevelStatistics(
                any(),
                any(),
                any(),
            )
        } returns
            Result.Success(
                com.wordland.domain.model.statistics.LevelStatistics(
                    userId = testUserId,
                    levelId = testLevelId,
                    totalGames = 1,
                    completedGames = 1,
                    perfectGames = 0,
                    highestScore = 100,
                    lowestScore = 100,
                    averageScore = 100f,
                    totalScore = 100,
                    bestTime = 60000L,
                    worstTime = 60000L,
                    averageTime = 60000,
                    totalTime = 60000L,
                    totalCorrect = 5,
                    totalQuestions = 6,
                    overallAccuracy = 0.833f,
                    bestCombo = 3,
                    firstPlayedAt = System.currentTimeMillis() - 60000L,
                    lastPlayedAt = System.currentTimeMillis(),
                    lastUpdatedAt = System.currentTimeMillis(),
                ),
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke records game history successfully`() =
        runTest {
            val startTime = System.currentTimeMillis() - 60000L
            val endTime = System.currentTimeMillis()

            val result =
                recordGameHistoryUseCase(
                    userId = testUserId,
                    levelId = testLevelId,
                    islandId = testIslandId,
                    gameMode = testGameMode,
                    startTime = startTime,
                    endTime = endTime,
                    score = 100,
                    stars = 2,
                    correctAnswers = 5,
                    totalQuestions = 6,
                    maxCombo = 3,
                )

            assertTrue(result is Result.Success)
            val gameId = (result as Result.Success).data
            assertTrue(gameId.isNotEmpty())

            coVerify { gameHistoryRepository.insertGameHistory(any()) }
        }

    @Test
    fun `invoke calculates duration correctly`() =
        runTest {
            val startTime = System.currentTimeMillis() - 120000L // 2 minutes ago
            val endTime = System.currentTimeMillis()

            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = startTime,
                endTime = endTime,
                score = 100,
                stars = 2,
                correctAnswers = 5,
                totalQuestions = 6,
                maxCombo = 3,
            )

            coVerify {
                gameHistoryRepository.insertGameHistory(
                    match { it.duration >= 119000 && it.duration <= 121000 },
                )
            }
        }

    @Test
    fun `invoke calculates accuracy correctly`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 100,
                stars = 2,
                correctAnswers = 5,
                totalQuestions = 10,
                maxCombo = 3,
            )

            coVerify {
                gameHistoryRepository.insertGameHistory(
                    match { it.accuracy == 0.5f },
                )
            }
        }

    @Test
    fun `invoke returns zero accuracy when no questions`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 0,
                stars = 0,
                correctAnswers = 0,
                totalQuestions = 0,
                maxCombo = 0,
            )

            coVerify {
                gameHistoryRepository.insertGameHistory(
                    match { it.accuracy == 0f },
                )
            }
        }

    @Test
    fun `invoke updates global statistics`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 150,
                stars = 3,
                correctAnswers = 6,
                totalQuestions = 6,
                maxCombo = 6,
            )

            coVerify {
                statisticsRepository.updateGlobalStatistics(
                    userId = testUserId,
                    gameScore = 150,
                    gamePerfect = true, // 3 stars = perfect
                    studyTime = any(),
                )
            }
        }

    @Test
    fun `invoke marks game as perfect when stars is 3`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 100,
                stars = 3,
                correctAnswers = 6,
                totalQuestions = 6,
                maxCombo = 6,
            )

            coVerify {
                statisticsRepository.updateGlobalStatistics(
                    any(),
                    any(),
                    gamePerfect = true,
                    any(),
                )
            }
        }

    @Test
    fun `invoke marks game as not perfect when stars less than 3`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 100,
                stars = 2,
                correctAnswers = 5,
                totalQuestions = 6,
                maxCombo = 3,
            )

            coVerify {
                statisticsRepository.updateGlobalStatistics(
                    any(),
                    any(),
                    gamePerfect = false,
                    any(),
                )
            }
        }

    @Test
    fun `invoke updates level statistics when levelId provided`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 100,
                stars = 2,
                correctAnswers = 5,
                totalQuestions = 6,
                maxCombo = 3,
            )

            coVerify {
                statisticsRepository.updateLevelStatistics(
                    userId = testUserId,
                    levelId = testLevelId,
                    gameHistory = any(),
                )
            }
        }

    @Test
    fun `invoke updates level statistics always`() =
        runTest {
            recordGameHistoryUseCase(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                startTime = System.currentTimeMillis() - 60000L,
                endTime = System.currentTimeMillis(),
                score = 100,
                stars = 2,
                correctAnswers = 5,
                totalQuestions = 6,
                maxCombo = 3,
            )

            coVerify(atLeast = 1) {
                statisticsRepository.updateLevelStatistics(any(), any(), any())
            }
        }

    @Test
    fun `invoke returns Error when game history insert fails`() =
        runTest {
            coEvery { gameHistoryRepository.insertGameHistory(any()) } returns
                Result.Error(
                    RuntimeException("Database error"),
                )

            val result =
                recordGameHistoryUseCase(
                    userId = testUserId,
                    levelId = testLevelId,
                    islandId = testIslandId,
                    gameMode = testGameMode,
                    startTime = System.currentTimeMillis() - 60000L,
                    endTime = System.currentTimeMillis(),
                    score = 100,
                    stars = 2,
                    correctAnswers = 5,
                    totalQuestions = 6,
                    maxCombo = 3,
                )

            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns Error when global statistics update fails`() =
        runTest {
            coEvery {
                statisticsRepository.updateGlobalStatistics(
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns Result.Error(RuntimeException("Statistics error"))

            val result =
                recordGameHistoryUseCase(
                    userId = testUserId,
                    levelId = testLevelId,
                    islandId = testIslandId,
                    gameMode = testGameMode,
                    startTime = System.currentTimeMillis() - 60000L,
                    endTime = System.currentTimeMillis(),
                    score = 100,
                    stars = 2,
                    correctAnswers = 5,
                    totalQuestions = 6,
                    maxCombo = 3,
                )

            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns Error when level statistics update fails`() =
        runTest {
            coEvery {
                statisticsRepository.updateLevelStatistics(
                    any(),
                    any(),
                    any(),
                )
            } returns Result.Error(RuntimeException("Level statistics error"))

            val result =
                recordGameHistoryUseCase(
                    userId = testUserId,
                    levelId = testLevelId,
                    islandId = testIslandId,
                    gameMode = testGameMode,
                    startTime = System.currentTimeMillis() - 60000L,
                    endTime = System.currentTimeMillis(),
                    score = 100,
                    stars = 2,
                    correctAnswers = 5,
                    totalQuestions = 6,
                    maxCombo = 3,
                )

            assertTrue(result is Result.Error)
        }

    @Test
    fun `recordLevelGame creates game history with calculated timestamps`() =
        runTest {
            val duration = 60000L // 1 minute

            val result =
                recordGameHistoryUseCase.recordLevelGame(
                    userId = testUserId,
                    levelId = testLevelId,
                    islandId = testIslandId,
                    gameMode = testGameMode,
                    duration = duration,
                    score = 100,
                    stars = 2,
                    correctAnswers = 5,
                    totalQuestions = 6,
                    maxCombo = 3,
                )

            assertTrue(result is Result.Success)

            coVerify {
                gameHistoryRepository.insertGameHistory(
                    match {
                        it.duration == duration &&
                            it.endTime > it.startTime &&
                            it.endTime - it.startTime == duration
                    },
                )
            }
        }

    @Test
    fun `recordLevelGame passes correct parameters`() =
        runTest {
            recordGameHistoryUseCase.recordLevelGame(
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = testGameMode,
                duration = 45000L,
                score = 200,
                stars = 3,
                correctAnswers = 10,
                totalQuestions = 10,
                maxCombo = 10,
            )

            coVerify {
                gameHistoryRepository.insertGameHistory(
                    match {
                        it.userId == testUserId &&
                            it.levelId == testLevelId &&
                            it.gameMode == testGameMode &&
                            it.score == 200 &&
                            it.stars == 3 &&
                            it.correctAnswers == 10 &&
                            it.totalQuestions == 10 &&
                            it.maxCombo == 10 &&
                            it.accuracy == 1.0f
                    },
                )
            }
        }
}
