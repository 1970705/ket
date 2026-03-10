package com.wordland.ui.viewmodel

import com.wordland.domain.model.Result
import com.wordland.domain.model.UserStats
import com.wordland.domain.usecase.usecases.GetIslandsUseCase
import com.wordland.domain.usecase.usecases.GetUserStatsUseCase
import com.wordland.ui.uistate.HomeUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for HomeViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var viewModel: HomeViewModel
    private lateinit var getUserStats: GetUserStatsUseCase
    private lateinit var getIslands: GetIslandsUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getUserStats = mockk()
        getIslands = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls loadHomeData and updates to Success`() =
        runTest {
            // Given
            val mockStats =
                UserStats(
                    totalLevels = 10,
                    completedLevels = 5,
                    masteredIslands = 2,
                    totalWords = 100,
                    masteredWords = 50,
                    completionRate = 0.5f,
                )
            coEvery { getUserStats("user_001") } returns Result.Success(mockStats)

            // When
            viewModel = HomeViewModel(getUserStats, getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is HomeUiState.Success)
            val successState = state as HomeUiState.Success
            assertEquals("Player", successState.data.userName)
            assertEquals(5, successState.data.totalProgress)
        }

    @Test
    fun `refresh reloads home data`() =
        runTest {
            // Given
            val mockStats1 =
                UserStats(
                    totalLevels = 10,
                    completedLevels = 5,
                    masteredIslands = 2,
                    totalWords = 100,
                    masteredWords = 50,
                    completionRate = 0.5f,
                )
            val mockStats2 =
                UserStats(
                    totalLevels = 10,
                    completedLevels = 10,
                    masteredIslands = 2,
                    totalWords = 100,
                    masteredWords = 50,
                    completionRate = 1.0f,
                )

            coEvery { getUserStats("user_001") } returnsMany
                listOf(
                    Result.Success(mockStats1),
                    Result.Success(mockStats2),
                )

            viewModel = HomeViewModel(getUserStats, getIslands)
            advanceUntilIdle()

            val initialState = viewModel.uiState.value as HomeUiState.Success
            assertEquals(5, initialState.data.totalProgress)

            // When
            viewModel.refresh()
            advanceUntilIdle()

            // Then
            val newState = viewModel.uiState.value as HomeUiState.Success
            assertEquals(10, newState.data.totalProgress)
        }

    @Test
    fun `init updates uiState to Error when loading fails`() =
        runTest {
            // Given
            val exception = RuntimeException("Failed to load user stats")
            coEvery { getUserStats("user_001") } returns Result.Error(exception)

            // When
            viewModel = HomeViewModel(getUserStats, getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is HomeUiState.Error)
            assertEquals("Failed to load user stats", (state as HomeUiState.Error).message)
        }

    @Test
    fun `Success state contains correct user data`() =
        runTest {
            // Given
            val mockStats =
                UserStats(
                    totalLevels = 20,
                    completedLevels = 8,
                    masteredIslands = 3,
                    totalWords = 200,
                    masteredWords = 80,
                    completionRate = 0.4f,
                )
            coEvery { getUserStats("user_001") } returns Result.Success(mockStats)

            // When
            viewModel = HomeViewModel(getUserStats, getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as HomeUiState.Success
            assertNotNull(state.data)
            assertEquals("Player", state.data.userName)
            assertEquals(8, state.data.totalProgress)
        }

    @Test
    fun `Error state contains exception message`() =
        runTest {
            // Given
            val exception = IllegalStateException("Network error")
            coEvery { getUserStats("user_001") } returns Result.Error(exception)

            // When
            viewModel = HomeViewModel(getUserStats, getIslands)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as HomeUiState.Error
            assertEquals("Network error", state.message)
        }
}
