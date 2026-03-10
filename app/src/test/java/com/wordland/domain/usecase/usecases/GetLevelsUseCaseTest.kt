package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetLevelsUseCase
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetLevelsUseCaseTest {
    private lateinit var getLevelsUseCase: GetLevelsUseCase
    private lateinit var progressRepository: ProgressRepository

    private val testDispatcher = StandardTestDispatcher()

    private val testLevels =
        listOf(
            LevelProgress(
                levelId = "look_island_level_01",
                userId = "user_001",
                islandId = "look_island",
                status = LevelStatus.COMPLETED,
                stars = 3,
                difficulty = "easy",
            ),
            LevelProgress(
                levelId = "look_island_level_02",
                userId = "user_001",
                islandId = "look_island",
                status = LevelStatus.IN_PROGRESS,
                stars = 1,
                difficulty = "normal",
            ),
            LevelProgress(
                levelId = "look_island_level_03",
                userId = "user_001",
                islandId = "look_island",
                status = LevelStatus.LOCKED,
                stars = 0,
                difficulty = "hard",
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        progressRepository = mockk()
        getLevelsUseCase = GetLevelsUseCase(progressRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns levels for specified island`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "look_island") } returns testLevels

            // When
            val result = getLevelsUseCase("look_island", "user_001").first()

            // Then
            assertEquals(3, result.size)
            assertEquals("look_island_level_01", result[0].levelId)
            assertEquals("look_island", result[0].islandId)
        }

    @Test
    fun `invoke returns empty list when no levels found`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "unknown_island") } returns emptyList()

            // When
            val result = getLevelsUseCase("unknown_island", "user_001").first()

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun `invoke preserves level status`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "look_island") } returns testLevels

            // When
            val result = getLevelsUseCase("look_island", "user_001").first()

            // Then
            assertEquals(LevelStatus.COMPLETED, result[0].status)
            assertEquals(LevelStatus.IN_PROGRESS, result[1].status)
            assertEquals(LevelStatus.LOCKED, result[2].status)
        }

    @Test
    fun `invoke preserves difficulty levels`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "look_island") } returns testLevels

            // When
            val result = getLevelsUseCase("look_island", "user_001").first()

            // Then
            assertEquals("easy", result[0].difficulty)
            assertEquals("normal", result[1].difficulty)
            assertEquals("hard", result[2].difficulty)
        }

    @Test
    fun `invoke preserves star counts`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "look_island") } returns testLevels

            // When
            val result = getLevelsUseCase("look_island", "user_001").first()

            // Then
            assertEquals(3, result[0].stars)
            assertEquals(1, result[1].stars)
            assertEquals(0, result[2].stars)
        }

    @Test
    fun `invoke handles different islands`() =
        runTest {
            // Given
            val moveValleyLevels =
                listOf(
                    LevelProgress(
                        levelId = "move_valley_level_01",
                        userId = "user_001",
                        islandId = "move_valley",
                        status = LevelStatus.UNLOCKED,
                        stars = 0,
                        difficulty = "normal",
                    ),
                )

            coEvery { progressRepository.getLevelsByIsland("user_001", "move_valley") } returns moveValleyLevels

            // When
            val result = getLevelsUseCase("move_valley", "user_001").first()

            // Then
            assertEquals(1, result.size)
            assertEquals("move_valley", result[0].islandId)
            assertEquals("move_valley_level_01", result[0].levelId)
        }

    @Test
    fun `invoke returns levels in correct order`() =
        runTest {
            // Given
            coEvery { progressRepository.getLevelsByIsland("user_001", "look_island") } returns testLevels

            // When
            val result = getLevelsUseCase("look_island", "user_001").first()

            // Then
            assertEquals("look_island_level_01", result[0].levelId)
            assertEquals("look_island_level_02", result[1].levelId)
            assertEquals("look_island_level_03", result[2].levelId)
        }
}
