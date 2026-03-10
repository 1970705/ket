package com.wordland.domain.usecase.usecases.gamehistory

import com.wordland.data.repository.GameHistoryRepository
import com.wordland.domain.model.statistics.GameHistory
import com.wordland.domain.model.statistics.GameMode
import com.wordland.domain.usecase.usecases.GetGameHistoryUseCase
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for GetGameHistoryUseCase - Query Operations
 * Tests getting game history with various filters
 */
class GetGameHistoryUseCaseQueryTest {
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
            gameHistoryRepository.getGameHistory(
                testUserId,
                any(),
                any(),
            )
        } returns flowOf(listOf(testGameHistory))
        every {
            gameHistoryRepository.getGameHistoryByLevel(
                testUserId,
                testLevelId,
                any(),
            )
        } returns flowOf(listOf(testGameHistory))
        every {
            gameHistoryRepository.getGameHistoryByMode(
                testUserId,
                any(),
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
        every { gameHistoryRepository.getRecentGames(testUserId, any()) } returns
            flowOf(
                listOf(testGameHistory),
            )
        every { gameHistoryRepository.getGameById(testGameId) } returns flowOf(testGameHistory)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns paginated game history`() =
        runTest {
            val flow = getGameHistoryUseCase(testUserId, limit = 20, offset = 0)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)
            assertEquals(testGameHistory, collected[0][0])

            coVerify { gameHistoryRepository.getGameHistory(testUserId, 20, 0) }
        }

    @Test
    fun `invoke uses default limit and offset`() =
        runTest {
            val flow = getGameHistoryUseCase(testUserId)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)

            coVerify { gameHistoryRepository.getGameHistory(testUserId, 20, 0) }
        }

    @Test
    fun `getByLevel returns games for specific level`() =
        runTest {
            val flow = getGameHistoryUseCase.getByLevel(testUserId, testLevelId)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(1, collected[0].size)

            coVerify { gameHistoryRepository.getGameHistoryByLevel(testUserId, testLevelId, 20) }
        }

    @Test
    fun `getByLevel uses custom limit`() =
        runTest {
            val flow = getGameHistoryUseCase.getByLevel(testUserId, testLevelId, limit = 50)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            coVerify { gameHistoryRepository.getGameHistoryByLevel(testUserId, testLevelId, 50) }
        }

    @Test
    fun `getByMode returns games for specific mode`() =
        runTest {
            val flow = getGameHistoryUseCase.getByMode(testUserId, GameMode.QUICK_JUDGE)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)

            coVerify {
                gameHistoryRepository.getGameHistoryByMode(
                    testUserId,
                    GameMode.QUICK_JUDGE,
                    20,
                )
            }
        }

    @Test
    fun `getByDateRange returns games in date range`() =
        runTest {
            val startTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            val endTime = System.currentTimeMillis()

            val flow = getGameHistoryUseCase.getByDateRange(testUserId, startTime, endTime)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)

            coVerify {
                gameHistoryRepository.getGameHistoryByDateRange(
                    testUserId,
                    startTime,
                    endTime,
                    100,
                )
            }
        }

    @Test
    fun `getByDateRange uses custom limit`() =
        runTest {
            val startTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            val endTime = System.currentTimeMillis()

            val flow = getGameHistoryUseCase.getByDateRange(testUserId, startTime, endTime, limit = 50)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            coVerify {
                gameHistoryRepository.getGameHistoryByDateRange(
                    testUserId,
                    startTime,
                    endTime,
                    50,
                )
            }
        }

    @Test
    fun `getRecent returns recent games`() =
        runTest {
            val flow = getGameHistoryUseCase.getRecent(testUserId, days = 7)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)

            coVerify { gameHistoryRepository.getRecentGames(testUserId, 7) }
        }

    @Test
    fun `getRecent uses default days`() =
        runTest {
            val flow = getGameHistoryUseCase.getRecent(testUserId)

            val collected = mutableListOf<List<GameHistory>>()
            flow.collect { collected.add(it) }

            coVerify { gameHistoryRepository.getRecentGames(testUserId, 7) }
        }

    @Test
    fun `getById returns game by ID`() =
        runTest {
            val flow = getGameHistoryUseCase.getById(testGameId)

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertEquals(testGameHistory, collected[0])

            coVerify { gameHistoryRepository.getGameById(testGameId) }
        }

    @Test
    fun `getById returns null for non-existent game`() =
        runTest {
            every { gameHistoryRepository.getGameById(any()) } returns flowOf(null)

            val flow = getGameHistoryUseCase.getById("non_existent_id")

            val collected = mutableListOf<GameHistory?>()
            flow.collect { collected.add(it) }

            assertEquals(1, collected.size)
            assertNull(collected[0])
        }
}
