package com.wordland.domain.usecase.usecases.gamehistory

import com.wordland.data.repository.GameHistoryRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode
import com.wordland.domain.usecase.usecases.GetGameHistoryUseCase
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for GetGameHistoryUseCase - Statistics and Maintenance
 * Tests statistics calculation and data maintenance operations
 */
class GetGameHistoryUseCaseStatisticsTest {
    private lateinit var getGameHistoryUseCase: GetGameHistoryUseCase
    private lateinit var gameHistoryRepository: GameHistoryRepository

    private val testUserId = "test_user"
    private val testLevelId = "look_island_level_1"
    private val testIslandId = "look_island"
    private val testGameId = UUID.randomUUID().toString()

    private lateinit var testGameHistory: GameHistory

    @Before
    fun setup() {
        gameHistoryRepository = mockk(relaxed = true)

        getGameHistoryUseCase = GetGameHistoryUseCase(gameHistoryRepository)

        val now = System.currentTimeMillis()
        testGameHistory =
            GameHistory(
                gameId = testGameId,
                userId = testUserId,
                levelId = testLevelId,
                islandId = testIslandId,
                gameMode = GameMode.SPELL_BATTLE,
                startTime = now - 60000L,
                endTime = now,
                duration = 60000L,
                score = 100,
                stars = 2,
                totalQuestions = 6,
                correctAnswers = 5,
                accuracy = 0.833f,
                maxCombo = 3,
                hintsUsed = 0,
                wrongAnswers = 1,
                avgResponseTime = 10000L,
                fastestAnswer = 5000L,
                slowestAnswer = 15000L,
                difficulty = "normal",
                createdAt = now,
            )

        // Default mock behaviors
        every {
            gameHistoryRepository.getGameHistoryByLevel(
                testUserId,
                testLevelId,
                any(),
            )
        } returns flowOf(listOf(testGameHistory))
        every {
            gameHistoryRepository.getGameHistoryByDateRange(
                testUserId,
                any(),
                any(),
                any(),
            )
        } returns flowOf(listOf(testGameHistory))
        every { gameHistoryRepository.getGameCount(testUserId) } returns flowOf(1)
        every { gameHistoryRepository.getPerfectGameCount(testUserId) } returns flowOf(0)
        every { gameHistoryRepository.getTotalScore(testUserId) } returns flowOf(100)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCount returns game count`() =
        runTest {
            val flow = getGameHistoryUseCase.getCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0])

            coVerify { gameHistoryRepository.getGameCount(testUserId) }
        }

    @Test
    fun `getPerfectCount returns perfect game count`() =
        runTest {
            val flow = getGameHistoryUseCase.getPerfectCount(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(0, collected[0])

            coVerify { gameHistoryRepository.getPerfectGameCount(testUserId) }
        }

    @Test
    fun `getTotalScore returns total score`() =
        runTest {
            val flow = getGameHistoryUseCase.getTotalScore(testUserId)

            val collected = mutableListOf<Int>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(100, collected[0])

            coVerify { gameHistoryRepository.getTotalScore(testUserId) }
        }

    @Test
    fun `getAverageScore returns score as float`() =
        runTest {
            every { gameHistoryRepository.getTotalScore(testUserId) } returns flowOf(500)

            val flow = getGameHistoryUseCase.getAverageScore(testUserId)

            val collected = mutableListOf<Float>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(500f, collected[0], 0.01f)
        }

    @Test
    fun `getTodayGames returns today's games`() =
        runTest {
            val todayStart = getStartOfDay()
            val todayEnd = todayStart + 24 * 60 * 60 * 1000L

            every {
                gameHistoryRepository.getGameHistoryByDateRange(
                    testUserId,
                    todayStart,
                    todayEnd,
                    100,
                )
            } returns flowOf(listOf(testGameHistory))

            val flow = getGameHistoryUseCase.getTodayGames(testUserId)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
        }

    @Test
    fun `getThisWeekGames returns this week's games`() =
        runTest {
            val weekStart = getStartOfWeek()
            val now = System.currentTimeMillis()

            every {
                gameHistoryRepository.getGameHistoryByDateRange(
                    testUserId,
                    weekStart,
                    now,
                    100,
                )
            } returns flowOf(listOf(testGameHistory))

            val flow = getGameHistoryUseCase.getThisWeekGames(testUserId)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
        }

    @Test
    fun `getByModeAndRecent filters by mode and date range`() =
        runTest {
            val startTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

            val mixedGames =
                listOf(
                    testGameHistory,
                    testGameHistory.copy(
                        gameId = UUID.randomUUID().toString(),
                        gameMode = GameMode.QUICK_JUDGE,
                    ),
                )
            every {
                gameHistoryRepository.getGameHistoryByDateRange(
                    testUserId,
                    any(),
                    any(),
                    any(),
                )
            } returns flowOf(mixedGames)

            val flow =
                getGameHistoryUseCase.getByModeAndRecent(
                    testUserId,
                    GameMode.QUICK_JUDGE,
                    days = 7,
                )

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)
            assertEquals(GameMode.QUICK_JUDGE, collected[0][0].gameMode)
        }

    @Test
    fun `getBestGame returns highest scoring game`() =
        runTest {
            val games =
                listOf(
                    testGameHistory.copy(score = 100),
                    testGameHistory.copy(
                        gameId = UUID.randomUUID().toString(),
                        score = 200,
                    ),
                    testGameHistory.copy(
                        gameId = UUID.randomUUID().toString(),
                        score = 150,
                    ),
                )
            every {
                gameHistoryRepository.getGameHistoryByLevel(
                    testUserId,
                    testLevelId,
                    100,
                )
            } returns flowOf(games)

            val flow = getGameHistoryUseCase.getBestGame(testUserId, testLevelId)

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNotNull(collected[0])
            assertEquals(200, collected[0]!!.score)
        }

    @Test
    fun `getBestGame returns null when no games`() =
        runTest {
            every {
                gameHistoryRepository.getGameHistoryByLevel(
                    testUserId,
                    testLevelId,
                    100,
                )
            } returns flowOf(emptyList())

            val flow = getGameHistoryUseCase.getBestGame(testUserId, testLevelId)

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNull(collected[0])
        }

    @Test
    fun `getMostRecentGame returns first game`() =
        runTest {
            val games =
                listOf(
                    testGameHistory.copy(endTime = System.currentTimeMillis()),
                    testGameHistory.copy(
                        gameId = UUID.randomUUID().toString(),
                        endTime = System.currentTimeMillis() - 60000L,
                    ),
                )
            every {
                gameHistoryRepository.getGameHistoryByLevel(
                    testUserId,
                    testLevelId,
                    1,
                )
            } returns flowOf(games)

            val flow = getGameHistoryUseCase.getMostRecentGame(testUserId, testLevelId)

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNotNull(collected[0])
            assertEquals(games[0].gameId, collected[0]!!.gameId)
        }

    @Test
    fun `getMostRecentGame returns null when no games`() =
        runTest {
            every {
                gameHistoryRepository.getGameHistoryByLevel(
                    testUserId,
                    testLevelId,
                    1,
                )
            } returns flowOf(emptyList())

            val flow = getGameHistoryUseCase.getMostRecentGame(testUserId, testLevelId)

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNull(collected[0])
        }

    @Test
    fun `deleteOldHistory deletes records`() =
        runTest {
            val beforeTimestamp = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            coEvery { gameHistoryRepository.deleteOldHistory(any()) } returns Result.Success(5)

            val result = getGameHistoryUseCase.deleteOldHistory(beforeTimestamp)

            assertTrue(result is Result.Success)
            assertEquals(5, (result as Result.Success).data)

            coVerify { gameHistoryRepository.deleteOldHistory(beforeTimestamp) }
        }

    @Test
    fun `deleteOldHistory returns Error on failure`() =
        runTest {
            coEvery { gameHistoryRepository.deleteOldHistory(any()) } returns
                Result.Error(
                    RuntimeException("Delete error"),
                )

            val result =
                getGameHistoryUseCase.deleteOldHistory(
                    System.currentTimeMillis(),
                )

            assertTrue(result is Result.Error)
        }

    // Helper methods for date calculations
    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
