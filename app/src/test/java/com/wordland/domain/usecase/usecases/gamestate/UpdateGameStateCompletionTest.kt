package com.wordland.domain.usecase.usecases.gamestate

import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.GameAction
import com.wordland.domain.model.MatchGameState
import com.wordland.domain.usecase.usecases.CheckMatchUseCase
import com.wordland.domain.usecase.usecases.UpdateGameStateUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Completion detection tests for UpdateGameStateUseCase.
 * Tests game completion and transition to Completed state.
 */
class UpdateGameStateCompletionTest {
    private lateinit var updateGameStateUseCase: UpdateGameStateUseCase
    private lateinit var checkMatchUseCase: CheckMatchUseCase

    @Before
    fun setup() {
        checkMatchUseCase = CheckMatchUseCase()
        updateGameStateUseCase = UpdateGameStateUseCase(checkMatchUseCase)
    }

    // === Completion Detection Tests ===

    @Test
    fun `checkMatchAndUpdate returns Completed when all bubbles matched`() {
        // Use 4 bubbles (2 pairs) - first pair already matched, second pair being matched now
        val testBubblesForMatch =
            listOf(
                // First pair - already matched
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
                // Second pair - being matched now
                BubbleState(
                    id = "pair_2_en",
                    word = "banana",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "pair_2_zh",
                    word = "香蕉",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )
        val stateBeforeMatch =
            MatchGameState.Playing(
                bubbles = testBubblesForMatch,
                selectedBubbleIds = setOf("pair_2_en"), // Only first bubble of final pair selected
                matchedPairs = 1, // First pair already matched
                elapsedTime = 1000L,
            )

        // When - select the second bubble of the final pair (triggering completion)
        val result = updateGameStateUseCase(stateBeforeMatch, GameAction.SelectBubble("pair_2_zh"))

        // Then
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(1000L, completedState.elapsedTime)
        assertEquals(2, completedState.pairs)
    }

    @Test
    fun `checkMatchAndUpdate returns Playing state when more bubbles remain`() {
        val manyBubbles =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    color = BubbleColor.GREEN,
                ),
                BubbleState(
                    id = "pair_2_en",
                    word = "banana",
                    pairId = "pair_2",
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "pair_2_zh",
                    word = "香蕉",
                    pairId = "pair_2",
                    color = BubbleColor.ORANGE,
                ),
            )
        val playingState =
            MatchGameState.Playing(
                bubbles = manyBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_zh"))

        // Then
        assertTrue(result is MatchGameState.Playing)
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(1, newPlayingState.matchedPairs)
    }

    @Test
    fun `completion detection works for single pair game`() {
        // Game with only 2 bubbles (1 pair)
        val singlePairBubbles =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    color = BubbleColor.GREEN,
                ),
            )
        val playingState =
            MatchGameState.Playing(
                bubbles = singlePairBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 500L,
            )

        // When - select the second bubble (only pair in game)
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_zh"))

        // Then - should complete immediately
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(500L, completedState.elapsedTime)
        assertEquals(1, completedState.pairs)
    }

    @Test
    fun `completion detection works for large game`() {
        // Game with many pairs (6 pairs = 12 bubbles)
        // First 5 pairs are matched, last pair is not matched
        val largeGameBubbles = mutableListOf<BubbleState>()
        repeat(5) { i ->
            largeGameBubbles.add(
                BubbleState(
                    id = "pair_${i}_en",
                    word = "word_$i",
                    pairId = "pair_$i",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
            )
            largeGameBubbles.add(
                BubbleState(
                    id = "pair_${i}_zh",
                    word = "词_$i",
                    pairId = "pair_$i",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
            )
        }
        // Last pair (index 5) not matched
        largeGameBubbles.add(
            BubbleState(
                id = "pair_5_en",
                word = "word_5",
                pairId = "pair_5",
                isMatched = false,
                color = BubbleColor.PINK,
            ),
        )
        largeGameBubbles.add(
            BubbleState(
                id = "pair_5_zh",
                word = "词_5",
                pairId = "pair_5",
                isMatched = false,
                color = BubbleColor.GREEN,
            ),
        )

        val playingState =
            MatchGameState.Playing(
                bubbles = largeGameBubbles,
                selectedBubbleIds = setOf(largeGameBubbles[largeGameBubbles.size - 2].id),
                matchedPairs = 5, // 5 pairs already matched
                elapsedTime = 5000L,
            )

        // When - select the final bubble
        val finalBubbleId = largeGameBubbles.last().id
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble(finalBubbleId))

        // Then - should complete
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(6, completedState.pairs)
    }

    @Test
    fun `completed state preserves elapsed time`() {
        val testBubblesForMatch =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
                BubbleState(
                    id = "pair_2_en",
                    word = "banana",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "pair_2_zh",
                    word = "香蕉",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )
        val stateBeforeMatch =
            MatchGameState.Playing(
                bubbles = testBubblesForMatch,
                selectedBubbleIds = setOf("pair_2_zh"),
                matchedPairs = 1,
                elapsedTime = 7777L,
            )

        // When
        val result = updateGameStateUseCase(stateBeforeMatch, GameAction.SelectBubble("pair_2_en"))

        // Then
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(7777L, completedState.elapsedTime)
    }

    @Test
    fun `completed state preserves pair count`() {
        val testBubblesForMatch =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
                BubbleState(
                    id = "pair_2_en",
                    word = "banana",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "pair_2_zh",
                    word = "香蕉",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )
        val stateBeforeMatch =
            MatchGameState.Playing(
                bubbles = testBubblesForMatch,
                selectedBubbleIds = setOf("pair_2_zh"),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )

        // When
        val result = updateGameStateUseCase(stateBeforeMatch, GameAction.SelectBubble("pair_2_en"))

        // Then
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(2, completedState.pairs)
    }

    @Test
    fun `completed state calculates accuracy correctly`() {
        val testBubblesForMatch =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "pair_1_zh",
                    word = "苹果",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
                BubbleState(
                    id = "pair_2_en",
                    word = "banana",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "pair_2_zh",
                    word = "香蕉",
                    pairId = "pair_2",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )
        val stateBeforeMatch =
            MatchGameState.Playing(
                bubbles = testBubblesForMatch,
                selectedBubbleIds = setOf("pair_2_zh"),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )

        // When
        val result = updateGameStateUseCase(stateBeforeMatch, GameAction.SelectBubble("pair_2_en"))

        // Then - should have perfect accuracy (2/2 pairs matched)
        assertTrue(result is MatchGameState.Completed)
        val completedState = result as MatchGameState.Completed
        assertEquals(1.0f, completedState.accuracy)
    }
}
