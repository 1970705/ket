package com.wordland.ui.viewmodel.match

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.MatchGameState
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
 * Tests for MatchGameViewModel state transitions and game completion.
 * Covers game completion, statistics calculation, and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MatchGameViewModelStateTest {
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

    // === Game Completion Tests ===

    @Test
    fun `selectBubble transitions to Completed when all bubbles matched`() =
        runTest {
            // Given - Create a mock use case that returns only 1 pair for easier testing
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - match the only pair
            val bubbles = playingState.bubbles
            viewModel.selectBubble(bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(bubbles[1].id)
            advanceUntilIdle()

            // Then
            val finalState = viewModel.gameState.value
            assertTrue(finalState is MatchGameState.Completed)
            assertEquals(MatchGameUiState.Completed, viewModel.uiState.value)
        }

    @Test
    fun `Completed state contains correct statistics`() =
        runTest {
            // Given - Create a mock use case that returns only 1 pair for easier testing
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When
            val bubbles = playingState.bubbles
            viewModel.selectBubble(bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(bubbles[1].id)
            advanceUntilIdle()

            // Then
            val completedState = viewModel.gameState.value as MatchGameState.Completed
            assertEquals(1, completedState.pairs)
            assertTrue(completedState.elapsedTime >= 0)
            assertTrue(completedState.accuracy > 0f)
        }

    // === Statistics and Accuracy Tests ===

    @Test
    fun `accuracy calculation with all correct matches is 100 percent`() =
        runTest {
            // Given - Single pair
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - match correctly
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[1].id)
            advanceUntilIdle()

            // Then - accuracy should be 1.0 (100%)
            val completedState = viewModel.gameState.value as MatchGameState.Completed
            assertEquals(1.0f, completedState.accuracy, 0.001f)
        }

    @Test
    fun `elapsedTime is non-negative on game completion`() =
        runTest {
            // Given - Single pair
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[1].id)
            advanceUntilIdle()

            // Then - elapsedTime should be >= 0
            val completedState = viewModel.gameState.value as MatchGameState.Completed
            assertTrue(completedState.elapsedTime >= 0)
        }

    // === Exit Dialog State Tests ===

    @Test
    fun `showExitConfirmation sets showExitDialog to true`() {
        // Given
        viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

        // When
        viewModel.showExitConfirmation()

        // Then
        assertTrue(viewModel.showExitDialog)
    }

    @Test
    fun `hideExitConfirmation sets showExitDialog to false`() {
        // Given
        viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)
        viewModel.showExitConfirmation()

        // When
        viewModel.hideExitConfirmation()

        // Then
        assertFalse(viewModel.showExitDialog)
    }

    @Test
    fun `showExitConfirmation can be called multiple times`() {
        // Given
        viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)

        // When - call multiple times
        viewModel.showExitConfirmation()
        viewModel.showExitConfirmation()
        viewModel.showExitConfirmation()

        // Then - should remain true
        assertTrue(viewModel.showExitDialog)
    }

    @Test
    fun `hideExitConfirmation can be called multiple times`() {
        // Given
        viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)
        viewModel.showExitConfirmation()

        // When - call multiple times
        viewModel.hideExitConfirmation()
        viewModel.hideExitConfirmation()
        viewModel.hideExitConfirmation()

        // Then - should remain false
        assertFalse(viewModel.showExitDialog)
    }

    @Test
    fun `exit dialog state is independent of game state`() =
        runTest {
            // Given
            setupReadyGame()

            // When - show exit dialog
            viewModel.showExitConfirmation()

            // Then - game state should still be Ready
            assertTrue(viewModel.gameState.value is MatchGameState.Ready)
            assertTrue(viewModel.showExitDialog)
        }

    // === Helper Methods ===

    private fun setupReadyGame() =
        runTest {
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()
        }

    private fun setupSinglePairGame() =
        runTest {
            // Create a mock use case that returns only 1 pair
            val singlePairBubbles = MatchGameViewModelTestHelper.createSinglePairBubbles()
            val mockGetWordPairsUseCase: GetWordPairsUseCase = mockk(relaxed = true)
            coEvery { mockGetWordPairsUseCase(any()) } returns
                com.wordland.domain.model.Result.Success(singlePairBubbles)

            viewModel = MatchGameViewModel(mockGetWordPairsUseCase, checkMatchUseCase)
            viewModel.initializeGame(
                MatchGameViewModelTestHelper.TEST_LEVEL_ID,
                MatchGameViewModelTestHelper.TEST_ISLAND_ID,
            )
            advanceUntilIdle()
            viewModel.startGame()
        }
}
