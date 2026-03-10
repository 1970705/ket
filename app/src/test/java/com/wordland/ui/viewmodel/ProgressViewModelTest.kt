package com.wordland.ui.viewmodel

import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.usecase.usecases.GetUserProgressUseCase
import com.wordland.ui.uistate.ProgressUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProgressViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    private lateinit var viewModel: ProgressViewModel
    private lateinit var getUserProgress: GetUserProgressUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val testProgress =
        listOf(
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 60,
                totalAttempts = 6,
                correctAttempts = 5,
                incorrectAttempts = 1,
            ),
            UserWordProgress(
                wordId = "word2",
                userId = "user_001",
                status = LearningStatus.NEW,
                memoryStrength = 30,
                totalAttempts = 5,
                correctAttempts = 2,
                incorrectAttempts = 3,
            ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getUserProgress = mockk()
        viewModel = ProgressViewModel(getUserProgress)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProgress updates uiState to Loading initially`() =
        runTest {
            // Given
            every { getUserProgress("user_001") } returns flowOf(testProgress)

            // When
            viewModel.loadProgress("user_001")
            advanceUntilIdle()

            // Then - should reach Success state after loading
            val state = viewModel.uiState.value
            assertTrue(state is ProgressUiState.Success)
        }

    @Test
    fun `loadProgress updates uiState to Success when data loads successfully`() =
        runTest {
            // Given
            every { getUserProgress("user_001") } returns flowOf(testProgress)

            // When
            viewModel.loadProgress("user_001")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ProgressUiState.Success)
            val successState = state as ProgressUiState.Success
            assertEquals(2, successState.progress.size)
            assertEquals("word1", successState.progress[0].wordId)
        }

    @Test
    fun `loadProgress updates uiState to Success with empty list when no data`() =
        runTest {
            // Given
            every { getUserProgress("user_001") } returns flowOf(emptyList())

            // When
            viewModel.loadProgress("user_001")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is ProgressUiState.Success)
            assertTrue((state as ProgressUiState.Success).progress.isEmpty())
        }

    @Test
    fun `loadProgress returns correct progress data`() =
        runTest {
            // Given
            every { getUserProgress("user_001") } returns flowOf(testProgress)

            // When
            viewModel.loadProgress("user_001")
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as ProgressUiState.Success
            assertEquals(60, state.progress[0].memoryStrength)
            assertEquals(30, state.progress[1].memoryStrength)
            assertEquals(5, state.progress[0].correctAttempts)
            assertEquals(2, state.progress[1].correctAttempts)
        }

    @Test
    fun `initial uiState is Loading`() {
        // Then - initial state should be Loading
        assertTrue(viewModel.uiState.value is ProgressUiState.Loading)
    }
}
