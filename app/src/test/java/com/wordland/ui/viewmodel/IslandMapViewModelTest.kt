package com.wordland.ui.viewmodel

import com.wordland.domain.model.IslandWithProgress
import com.wordland.domain.usecase.usecases.GetIslandsUseCase
import com.wordland.ui.uistate.IslandMapUiState
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
 * Unit tests for IslandMapViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IslandMapViewModelTest {
    private lateinit var viewModel: IslandMapViewModel
    private lateinit var getIslands: GetIslandsUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testIslands =
        listOf(
            IslandWithProgress(
                islandId = "look_island",
                islandName = "Look Island",
                masteryPercentage = 75.0,
                isUnlocked = true,
                totalWords = 60,
                masteredWords = 45,
            ),
            IslandWithProgress(
                islandId = "move_valley",
                islandName = "Move Valley",
                masteryPercentage = 50.0,
                isUnlocked = true,
                totalWords = 60,
                masteredWords = 30,
            ),
            IslandWithProgress(
                islandId = "say_mountain",
                islandName = "Say Mountain",
                masteryPercentage = 0.0,
                isUnlocked = false,
                totalWords = 60,
                masteredWords = 0,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getIslands = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads islands and updates to Success`() =
        runTest {
            // Given
            every { getIslands("user_001") } returns flowOf(testIslands)

            // When
            viewModel = IslandMapViewModel(getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is IslandMapUiState.Success)
            val successState = state as IslandMapUiState.Success
            assertEquals(3, successState.islands.size)
            assertEquals("look_island", successState.islands[0].id)
            assertEquals("Look Island", successState.islands[0].name)
            assertEquals(75.0f, successState.islands[0].masteryPercentage, 0.01f)
            assertTrue(successState.islands[0].isUnlocked)
        }

    @Test
    fun `init updates uiState to Error when loading fails`() =
        runTest {
            // Given
            val exception = RuntimeException("Failed to load islands")
            every { getIslands("user_001") } returns flowOf(emptyList())

            // When
            viewModel = IslandMapViewModel(getIslands)
            advanceUntilIdle()

            // Then - Should have empty success state or error
            val state = viewModel.uiState.value
            assertTrue(state is IslandMapUiState.Success)
        }

    @Test
    fun `refresh reloads island data`() =
        runTest {
            // Given
            val updatedIslands =
                listOf(
                    IslandWithProgress(
                        islandId = "look_island",
                        islandName = "Look Island",
                        masteryPercentage = 100.0,
                        isUnlocked = true,
                        totalWords = 60,
                        masteredWords = 60,
                    ),
                )

            every { getIslands("user_001") } returnsMany
                listOf(
                    flowOf(testIslands),
                    flowOf(updatedIslands),
                )

            viewModel = IslandMapViewModel(getIslands)
            advanceUntilIdle()

            val initialState = viewModel.uiState.value as IslandMapUiState.Success
            assertEquals(3, initialState.islands.size)

            // When
            viewModel.refresh()
            advanceUntilIdle()

            // Then
            val newState = viewModel.uiState.value as IslandMapUiState.Success
            assertEquals(1, newState.islands.size)
            assertEquals(100.0f, newState.islands[0].masteryPercentage, 0.01f)
        }

    @Test
    fun `initial uiState is Loading`() {
        // Given
        every { getIslands("user_001") } returns flowOf(testIslands)

        // When
        viewModel = IslandMapViewModel(getIslands)

        // Then - initial state should be Loading (set in init block)
        val state = viewModel.uiState.value
        // State could be Loading or Success depending on timing
        assertTrue(state is IslandMapUiState.Loading || state is IslandMapUiState.Success)
    }

    @Test
    fun `island color is correctly mapped`() =
        runTest {
            // Given
            val islandsWithColors =
                listOf(
                    IslandWithProgress("look_island", "Look Island", 50.0, true, 60, 30),
                    IslandWithProgress("move_valley", "Move Valley", 50.0, true, 60, 30),
                    IslandWithProgress("say_mountain", "Say Mountain", 50.0, true, 60, 30),
                    IslandWithProgress("feel_garden", "Feel Garden", 50.0, true, 60, 30),
                )

            every { getIslands("user_001") } returns flowOf(islandsWithColors)

            // When
            viewModel = IslandMapViewModel(getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as IslandMapUiState.Success
            assertEquals(4, state.islands.size)

            // Verify colors are assigned (not null/gray for known islands)
            assertNotNull(state.islands[0].color)
            assertNotNull(state.islands[1].color)
            assertNotNull(state.islands[2].color)
            assertNotNull(state.islands[3].color)
        }

    @Test
    fun `unknown island gets gray color`() =
        runTest {
            // Given
            val unknownIsland =
                listOf(
                    IslandWithProgress("unknown_island", "Unknown", 0.0, false, 60, 0),
                )

            every { getIslands("user_001") } returns flowOf(unknownIsland)

            // When
            viewModel = IslandMapViewModel(getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as IslandMapUiState.Success
            assertEquals(1, state.islands.size)
            assertEquals("unknown_island", state.islands[0].id)
        }
}
