package com.wordland.ui.viewmodel

import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.usecase.usecases.GetLevelsUseCase
import com.wordland.ui.uistate.LevelSelectUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LevelSelectViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LevelSelectViewModelTest {
    private lateinit var viewModel: LevelSelectViewModel
    private lateinit var getLevels: GetLevelsUseCase

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
                status = LevelStatus.UNLOCKED,
                stars = 0,
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
        getLevels = mockk()
        viewModel = LevelSelectViewModel(getLevels)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadLevels updates uiState to Loading initially`() {
        // Given
        every { getLevels("look_island", "user_001") } returns flowOf(testLevels)

        // When
        viewModel.loadLevels("look_island")

        // Then - should be in Loading state initially
        val state = viewModel.uiState.value
        assertTrue(state is LevelSelectUiState.Loading)
    }

    @Test
    fun `loadLevels updates uiState to Success when data loads successfully`() =
        runTest {
            // Given
            every { getLevels("look_island", "user_001") } returns flowOf(testLevels)

            // When
            viewModel.loadLevels("look_island")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LevelSelectUiState.Success)
            val successState = state as LevelSelectUiState.Success
            assertEquals(3, successState.levels.size)
            assertEquals("look_island_level_01", successState.levels[0].levelId)
        }

    @Test
    fun `loadLevels updates uiState to Success with empty list when no levels`() =
        runTest {
            // Given
            every { getLevels("look_island", "user_001") } returns flowOf(emptyList())

            // When
            viewModel.loadLevels("look_island")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is LevelSelectUiState.Success)
            assertTrue((state as LevelSelectUiState.Success).levels.isEmpty())
        }

    @Test
    fun `loadLevels returns correct level data`() =
        runTest {
            // Given
            every { getLevels("look_island", "user_001") } returns flowOf(testLevels)

            // When
            viewModel.loadLevels("look_island")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LevelSelectUiState.Success
            assertEquals(3, state.levels[0].stars)
            assertEquals(LevelStatus.COMPLETED, state.levels[0].status)
            assertEquals(LevelStatus.UNLOCKED, state.levels[1].status)
            assertEquals(LevelStatus.LOCKED, state.levels[2].status)
        }

    @Test
    fun `loadLevels handles different islands correctly`() =
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
                        difficulty = "easy",
                    ),
                )

            every { getLevels("move_valley", "user_001") } returns flowOf(moveValleyLevels)

            // When
            viewModel.loadLevels("move_valley")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LevelSelectUiState.Success
            assertEquals(1, state.levels.size)
            assertEquals("move_valley", state.levels[0].islandId)
        }

    @Test
    fun `loadLevels handles multiple island loads correctly`() =
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
                        difficulty = "easy",
                    ),
                )

            every { getLevels("move_valley", "user_001") } returns flowOf(moveValleyLevels)

            // When
            viewModel.loadLevels("move_valley")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LevelSelectUiState.Success
            assertEquals("move_valley", state.levels[0].islandId)
        }

    @Test
    fun `refresh reloads level data`() =
        runTest {
            // Given
            val updatedLevels =
                listOf(
                    LevelProgress(
                        levelId = "look_island_level_01",
                        userId = "user_001",
                        islandId = "look_island",
                        status = LevelStatus.PERFECT,
                        stars = 3,
                        difficulty = "easy",
                    ),
                )

            every { getLevels("look_island", "user_001") } returns flowOf(updatedLevels)

            viewModel.loadLevels("look_island")
            advanceUntilIdle()

            // When
            viewModel.refresh("look_island")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LevelSelectUiState.Success
            assertEquals(LevelStatus.PERFECT, state.levels[0].status)
        }

    @Test
    fun `initial uiState is Loading`() {
        // Then - initial state should be Loading
        assertTrue(viewModel.uiState.value is LevelSelectUiState.Loading)
    }

    @Test
    fun `loadLevels preserves level metadata`() =
        runTest {
            // Given
            every { getLevels("look_island", "user_001") } returns flowOf(testLevels)

            // When
            viewModel.loadLevels("look_island")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as LevelSelectUiState.Success
            assertEquals("easy", state.levels[0].difficulty)
            assertEquals("normal", state.levels[1].difficulty)
            assertEquals("hard", state.levels[2].difficulty)
        }
}
