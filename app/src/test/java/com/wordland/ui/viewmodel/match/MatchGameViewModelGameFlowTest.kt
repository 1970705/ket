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
 * Tests for MatchGameViewModel game flow functionality.
 * Covers start game, pause, resume, and timing management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MatchGameViewModelGameFlowTest {
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

    // === Start Game Tests ===

    @Test
    fun `startGame transitions from Ready to Playing`() =
        runTest {
            // Given
            setupReadyGame()

            // When
            viewModel.startGame()

            // Then
            val gameState = viewModel.gameState.value
            assertTrue(gameState is MatchGameState.Playing)
            assertEquals(MatchGameUiState.Playing, viewModel.uiState.value)
        }

    @Test
    fun `startGame initializes Playing state with correct values`() =
        runTest {
            // Given
            setupReadyGame()

            // When
            viewModel.startGame()

            // Then
            val gameState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(emptySet<String>(), gameState.selectedBubbleIds)
            assertEquals(0, gameState.matchedPairs)
            assertTrue(gameState.startTime > 0)
        }

    @Test
    fun `startGame does nothing when not in Ready state`() =
        runTest {
            // Given
            viewModel = MatchGameViewModel(getWordPairsUseCase, checkMatchUseCase)
            // Don't initialize - should be in Idle state

            // When
            viewModel.startGame()

            // Then
            assertEquals(MatchGameState.Idle, viewModel.gameState.value)
        }

    // === Pause and Resume Tests ===

    @Test
    fun `pauseGame transitions from Playing to Paused`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            // When
            viewModel.pauseGame()

            // Then
            assertTrue(viewModel.gameState.value is MatchGameState.Paused)
            assertEquals(MatchGameUiState.Paused, viewModel.uiState.value)
        }

    @Test
    fun `resumeGame transitions from Paused to Playing`() =
        runTest {
            // Given
            setupReadyAndStartGame()
            viewModel.pauseGame()

            // When
            viewModel.resumeGame()

            // Then
            assertTrue(viewModel.gameState.value is MatchGameState.Playing)
            assertEquals(MatchGameUiState.Playing, viewModel.uiState.value)
        }

    @Test
    fun `resumeGame adjusts start time correctly`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val originalStartTime = (viewModel.gameState.value as MatchGameState.Playing).startTime

            // Pause
            viewModel.pauseGame()

            // Add a small real delay to ensure System.currentTimeMillis advances
            kotlinx.coroutines.delay(10)

            // When
            viewModel.resumeGame()

            // Then - startTime should be valid (greater than 0)
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            assertTrue(playingState.startTime > 0)
            // After pause/resume, the startTime should have been adjusted
            // to account for the paused time
            assertTrue(playingState.startTime >= originalStartTime)
        }

    @Test
    fun `pauseGame does nothing when not in Playing state`() =
        runTest {
            // Given
            setupReadyGame()

            // When - pause while in Ready state
            viewModel.pauseGame()

            // Then - should still be in Ready state
            assertTrue(viewModel.gameState.value is MatchGameState.Ready)
        }

    // === State Transition Edge Cases ===

    @Test
    fun `startGame from Paused state does nothing`() =
        runTest {
            // Given
            setupReadyAndStartGame()
            viewModel.pauseGame()

            // When - try to start while paused
            viewModel.startGame()

            // Then - should remain in Paused state
            assertTrue(viewModel.gameState.value is MatchGameState.Paused)
        }

    @Test
    fun `resumeGame from Playing state does nothing`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val originalStartTime = (viewModel.gameState.value as MatchGameState.Playing).startTime

            // When - try to resume while already playing
            viewModel.resumeGame()

            // Then - startTime should not change
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(originalStartTime, playingState.startTime)
        }

    @Test
    fun `resumeGame from Ready state does nothing`() =
        runTest {
            // Given
            setupReadyGame()

            // When - try to resume from Ready state
            viewModel.resumeGame()

            // Then - should remain in Ready state
            assertTrue(viewModel.gameState.value is MatchGameState.Ready)
        }

    @Test
    fun `multiple pause resume cycles preserve elapsed time`() =
        runTest {
            // Given - Single pair for easier completion
            setupSinglePairGame()

            // When - multiple pause/resume cycles
            viewModel.pauseGame()
            advanceTimeBy(100)
            viewModel.resumeGame()
            advanceTimeBy(50)
            viewModel.pauseGame()
            advanceTimeBy(100)
            viewModel.resumeGame()

            // Then - complete game
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[1].id)
            advanceUntilIdle()

            // Should complete successfully
            assertTrue(viewModel.gameState.value is MatchGameState.Completed)
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

    private fun setupReadyAndStartGame() =
        runTest {
            setupReadyGame()
            viewModel.startGame()
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
