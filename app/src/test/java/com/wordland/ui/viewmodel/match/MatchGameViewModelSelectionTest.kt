package com.wordland.ui.viewmodel.match

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.MatchGameState
import com.wordland.domain.usecase.usecases.CheckMatchUseCase
import com.wordland.domain.usecase.usecases.GetWordPairsUseCase
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
 * Tests for MatchGameViewModel bubble selection functionality.
 * Covers single selection, deselection, two-bubble limit, and matching detection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MatchGameViewModelSelectionTest {
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

    // === Bubble Selection Tests ===

    @Test
    fun `selectBubble adds first bubble to selection`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val firstBubbleId = playingState.bubbles[0].id

            // When
            viewModel.selectBubble(firstBubbleId)
            advanceUntilIdle()

            // Then
            val newPlayingState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(1, newPlayingState.selectedBubbleIds.size)
            assertTrue(newPlayingState.selectedBubbleIds.contains(firstBubbleId))
            assertTrue(newPlayingState.bubbles[0].isSelected)
        }

    @Test
    fun `selectBubble deselects already selected bubble`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val firstBubbleId = playingState.bubbles[0].id

            // When - select twice
            viewModel.selectBubble(firstBubbleId)
            advanceUntilIdle()
            viewModel.selectBubble(firstBubbleId)
            advanceUntilIdle()

            // Then
            val newPlayingState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(0, newPlayingState.selectedBubbleIds.size)
            assertFalse(
                newPlayingState.bubbles.find { it.id == firstBubbleId }?.isSelected == true,
            )
        }

    @Test
    fun `selectBubble limits selection to 2 bubbles`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - try to select 3 bubbles
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[2].id) // Different pair
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[1].id) // Try third
            advanceUntilIdle()

            // Then - after match check completes, should have 0 or 2 selected
            val newPlayingState = viewModel.gameState.value as MatchGameState.Playing
            assertTrue(newPlayingState.selectedBubbleIds.size <= 2)
        }

    @Test
    fun `selectBubble does nothing when not in Playing state`() =
        runTest {
            // Given
            setupReadyGame()

            // When - try to select while in Ready state
            val readyState = viewModel.gameState.value as MatchGameState.Ready
            viewModel.selectBubble(readyState.bubbles[0].id)
            advanceUntilIdle()

            // Then - state should still be Ready
            assertTrue(viewModel.gameState.value is MatchGameState.Ready)
        }

    @Test
    fun `selectBubble marks matching bubbles as matched`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            // Get bubbles from Playing state
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val bubbleEn = playingState.bubbles.find { it.word == "apple" }!!
            val bubbleZh = playingState.bubbles.find { it.word == "苹果" }!!

            // When - select matching pair
            viewModel.selectBubble(bubbleEn.id)
            advanceUntilIdle()
            viewModel.selectBubble(bubbleZh.id)
            advanceUntilIdle()

            // Then - after delay, bubbles should be matched
            val newState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(1, newState.matchedPairs)
            assertTrue(newState.bubbles.find { it.id == bubbleEn.id }?.isMatched == true)
            assertTrue(newState.bubbles.find { it.id == bubbleZh.id }?.isMatched == true)
        }

    @Test
    fun `selectBubble clears non-matching selection`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            // Get bubbles from Playing state
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val bubbleApple = playingState.bubbles.find { it.word == "apple" }!!
            val bubbleBanana = playingState.bubbles.find { it.word == "banana" }!!

            // When - select non-matching pair
            viewModel.selectBubble(bubbleApple.id)
            advanceUntilIdle()
            viewModel.selectBubble(bubbleBanana.id)
            advanceUntilIdle()

            // After delay for non-match
            advanceTimeBy(600)

            // Then - selection should be cleared
            val newState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(0, newState.selectedBubbleIds.size)
            assertEquals(0, newState.matchedPairs)
        }

    @Test
    fun `selectBubble handles bubble not found in list gracefully`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            // When - select non-existent bubble
            viewModel.selectBubble("non_existent_bubble_id")
            advanceUntilIdle()

            // Then - should not crash
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(0, playingState.selectedBubbleIds.size)
        }

    @Test
    fun `selectBubble does not update state when selection unchanged`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val initialState = viewModel.gameState.value

            // When - select the same bubble twice (should toggle, then toggle back)
            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val bubbleId = playingState.bubbles[0].id

            viewModel.selectBubble(bubbleId)
            advanceUntilIdle()

            val stateAfterFirst = viewModel.gameState.value

            viewModel.selectBubble(bubbleId)
            advanceUntilIdle()

            val stateAfterSecond = viewModel.gameState.value

            // Then - state changed after first select
            assertNotEquals(initialState, stateAfterFirst)
            // After deselecting, selectedBubbleIds should be empty
            val finalState = stateAfterSecond as MatchGameState.Playing
            assertEquals(emptySet<String>(), finalState.selectedBubbleIds)
        }

    @Test
    fun `selectBubble handles same bubble selected twice in quick succession`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val bubbleId = playingState.bubbles[0].id

            // When - select same bubble twice rapidly
            viewModel.selectBubble(bubbleId)
            viewModel.selectBubble(bubbleId)
            advanceUntilIdle()

            // Then - should be deselected (toggle behavior)
            val newState = viewModel.gameState.value as MatchGameState.Playing
            assertFalse(newState.selectedBubbleIds.contains(bubbleId))
        }

    @Test
    fun `matching pair increments matchedPairs correctly`() =
        runTest {
            // Given - Single pair for easier testing
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - match the pair
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[1].id)
            advanceUntilIdle()

            // Then - game should be completed with 1 matched pair
            val finalState = viewModel.gameState.value
            assertTrue(
                finalState is MatchGameState.Completed ||
                    finalState is MatchGameState.Playing,
            )
            if (finalState is MatchGameState.Completed) {
                assertEquals(1, finalState.pairs)
            } else {
                assertEquals(1, (finalState as MatchGameState.Playing).matchedPairs)
            }
        }

    @Test
    fun `nonMatching pair does not increment matchedPairs`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - select non-matching pair
            viewModel.selectBubble(playingState.bubbles[0].id)
            advanceUntilIdle()
            viewModel.selectBubble(playingState.bubbles[2].id) // Different pair
            advanceUntilIdle()

            // Wait for non-match timeout
            advanceTimeBy(600)

            // Then - matchedPairs should still be 0
            val newState = viewModel.gameState.value as MatchGameState.Playing
            assertEquals(0, newState.matchedPairs)
        }

    @Test
    fun `rapid bubble selection handles correctly`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing

            // When - select multiple bubbles rapidly
            viewModel.selectBubble(playingState.bubbles[0].id)
            viewModel.selectBubble(playingState.bubbles[1].id)
            viewModel.selectBubble(playingState.bubbles[2].id)
            advanceUntilIdle()

            // Then - should handle gracefully (either 0, 1, or 2 selected depending on timing)
            val newState = viewModel.gameState.value as MatchGameState.Playing
            assertTrue(newState.selectedBubbleIds.size <= 2)
        }

    @Test
    fun `selecting matched bubble does not affect state`() =
        runTest {
            // Given - Single pair
            setupSinglePairGame()

            val playingState = viewModel.gameState.value as MatchGameState.Playing
            val bubbleEn = playingState.bubbles[0].id
            val bubbleZh = playingState.bubbles[1].id

            // Match the pair
            viewModel.selectBubble(bubbleEn)
            advanceUntilIdle()
            viewModel.selectBubble(bubbleZh)
            advanceUntilIdle()

            // After matching the only pair, game is completed
            val afterMatchState = viewModel.gameState.value
            val matchedCount =
                when (afterMatchState) {
                    is MatchGameState.Playing -> afterMatchState.matchedPairs
                    is MatchGameState.Completed -> afterMatchState.pairs
                    else -> 0
                }

            // When - try to select a matched bubble
            viewModel.selectBubble(bubbleEn)
            advanceUntilIdle()

            // Then - matchedPairs should not change
            val finalState = viewModel.gameState.value
            val finalMatchedCount =
                when (finalState) {
                    is MatchGameState.Playing -> finalState.matchedPairs
                    is MatchGameState.Completed -> finalState.pairs
                    else -> matchedCount
                }
            assertEquals(matchedCount, finalMatchedCount)
        }

    @Test
    fun `selectBubble with empty string ID does not crash`() =
        runTest {
            // Given
            setupReadyAndStartGame()

            // When - select with empty string ID
            viewModel.selectBubble("")
            advanceUntilIdle()

            // Then - should not crash
            assertTrue(viewModel.gameState.value is MatchGameState.Playing)
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
