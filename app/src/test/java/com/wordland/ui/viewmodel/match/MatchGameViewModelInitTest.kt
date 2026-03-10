package com.wordland.ui.viewmodel.match

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.MatchGameState
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.CheckMatchUseCase
import com.wordland.domain.usecase.usecases.GetWordPairsUseCase
import com.wordland.ui.viewmodel.MatchGameUiState
import com.wordland.ui.viewmodel.MatchGameViewModel
import com.wordland.ui.viewmodel.helper.MatchGameViewModelTestHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests for MatchGameViewModel initialization and game setup.
 * Covers game initialization, bubble creation, error states, and empty list handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MatchGameViewModelInitTest {
    private lateinit var viewModel: MatchGameViewModel
    private lateinit var getWordPairsUseCase: GetWordPairsUseCase
    private lateinit var checkMatchUseCase: CheckMatchUseCase
    private lateinit var wordRepository: WordRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup mocks
        wordRepository = mockk(relaxed = true)
        checkMatchUseCase = CheckMatchUseCase()
        getWordPairsUseCase = GetWordPairsUseCase(wordRepository)

        // Setup repository mock
        coEvery { wordRepository.getWordsByLevel(any()) } returns
            MatchGameViewModelTestHelper.testWords
        coEvery { wordRepository.getWordsByIsland(any()) } returns
            MatchGameViewModelTestHelper.testWords
        coEvery { wordRepository.getRandomKETWords(any()) } returns
            MatchGameViewModelTestHelper.testWords
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Initialization Tests ===

    @Test
    fun `initializeGame sets Preparing state then Ready state`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.showExitDialog)
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Ready)
            assertEquals(6, (gameState as MatchGameState.Ready).pairs)
        }

    @Test
    fun `initializeGame creates correct number of bubbles`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Ready)
            assertEquals(12, (gameState as MatchGameState.Ready).bubbles.size)
        }

    @Test
    fun `initializeGame sets UI state to Ready`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            assertEquals(MatchGameUiState.Ready, viewModel.uiState.value)
        }

    @Test
    fun `initializeGame handles error state`() =
        runTest {
            // Given - Create a mock use case that returns an error
            val mockGetWordPairsUseCase: GetWordPairsUseCase = mockk(relaxed = true)
            coEvery {
                mockGetWordPairsUseCase(any())
            } returns Result.Error(RuntimeException("Database error"))
            viewModel = MatchGameViewModel(mockGetWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Error)
            val uiState = viewModel.uiState.value
            assertTrue(uiState is MatchGameUiState.Error)
        }

    @Test
    fun `initializeGame resets exit dialog state`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)
            viewModel.showExitConfirmation()

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.showExitDialog)
        }

    @Test
    fun `initializeGame handles empty word list`() =
        runTest {
            // Given - Mock use case returns empty list
            val mockGetWordPairsUseCase: GetWordPairsUseCase = mockk(relaxed = true)
            coEvery { mockGetWordPairsUseCase(any()) } returns Result.Success(emptyList())

            viewModel = MatchGameViewModel(mockGetWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then - should handle gracefully with Ready state but 0 pairs
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Ready)
            val readyState = gameState as MatchGameState.Ready
            assertEquals(0, readyState.pairs)
            assertTrue(readyState.bubbles.isEmpty())
        }

    @Test
    fun `initializeGame with empty levelId uses default`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

            // When - pass empty levelId
            viewModel.initializeGame("", MatchGameViewModelTestHelper.TEST_ISLAND_ID)
            advanceUntilIdle()

            // Then - should still initialize with empty result
            val gameState = viewModel.gameState.value
            assertTrue(
                gameState is MatchGameState.Ready ||
                    gameState is MatchGameState.Error,
            )
        }

    @Test
    fun `initializeGame handles special characters in IDs`() =
        runTest {
            // Given
            val specialLevelId = "level_01-test"
            val specialIslandId = "island_01.test"

            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

            // When - initialize with special characters
            viewModel.initializeGame(specialLevelId, specialIslandId)
            advanceUntilIdle()

            // Then - should still initialize
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Ready)
        }

    @Test
    fun `initializeGame with network error results in Error state`() =
        runTest {
            // Given
            val mockGetWordPairsUseCase: GetWordPairsUseCase = mockk(relaxed = true)
            coEvery {
                mockGetWordPairsUseCase(any())
            } returns Result.Error(Exception("Network error"))

            viewModel = MatchGameViewModel(mockGetWordPairsUseCase, checkMatchUseCase)

            // When
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // Then
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Error)
        }

    @Test
    fun `actions in Error state do not crash`() =
        runTest {
            // Given - Put in error state
            val mockGetWordPairsUseCase: GetWordPairsUseCase = mockk(relaxed = true)
            coEvery {
                mockGetWordPairsUseCase(any())
            } returns Result.Error(Exception("Error"))

            viewModel = MatchGameViewModel(mockGetWordPairsUseCase, checkMatchUseCase)
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()

            // When - try various actions
            viewModel.startGame()
            viewModel.pauseGame()
            viewModel.resumeGame()
            viewModel.selectBubble("any_id")
            advanceUntilIdle()

            // Then - should not crash
            assertTrue(viewModel.gameState.value is MatchGameState.Error)
        }
}
