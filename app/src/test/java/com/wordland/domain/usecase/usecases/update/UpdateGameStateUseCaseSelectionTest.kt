package com.wordland.domain.usecase.usecases.update

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
 * Unit tests for UpdateGameStateUseCase - Bubble Selection and Matching
 * Tests bubble selection logic, match detection, and completion detection
 */
class UpdateGameStateUseCaseSelectionTest {
    private lateinit var updateGameStateUseCase: UpdateGameStateUseCase
    private lateinit var checkMatchUseCase: CheckMatchUseCase

    // Test bubbles
    private val testBubbles =
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

    @Before
    fun setup() {
        checkMatchUseCase = CheckMatchUseCase()
        updateGameStateUseCase = UpdateGameStateUseCase(checkMatchUseCase)
    }

    // === SelectBubble Action Tests ===

    @Test
    fun `handleSelectBubble adds bubble to selected when less than 2 selected`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_en"))

        // Then
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(setOf("pair_1_en"), newPlayingState.selectedBubbleIds)
        assertTrue(newPlayingState.bubbles[0].isSelected)
    }

    @Test
    fun `handleSelectBubble removes bubble when already selected`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_en"))

        // Then
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(emptySet<String>(), newPlayingState.selectedBubbleIds)
        assertFalse(newPlayingState.bubbles[0].isSelected)
    }

    @Test
    fun `handleSelectBubble does not add third bubble - triggers match instead`() {
        // When 2 matching bubbles are already selected, clicking a third one
        // triggers the match check instead of adding the third bubble
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en", "pair_1_zh"), // Already selected matching pair
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When - click a third bubble while 2 are already selected
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_2_en"))

        // Then - the original 2 bubbles are matched and selection is cleared
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(1, newPlayingState.matchedPairs) // First pair matched
        assertEquals(emptySet<String>(), newPlayingState.selectedBubbleIds) // Selection cleared
        // The third bubble was not added to selection
        assertEquals(0, newPlayingState.selectedBubbleIds.size)
    }

    @Test
    fun `handleSelectBubble marks matching bubbles as matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When - select the matching bubble
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_zh"))

        // Then - should auto-match and clear selection
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(1, newPlayingState.matchedPairs)
        assertEquals(emptySet<String>(), newPlayingState.selectedBubbleIds)
        assertTrue(newPlayingState.bubbles[0].isMatched)
        assertTrue(newPlayingState.bubbles[1].isMatched)
        assertFalse(newPlayingState.bubbles[0].isSelected)
        assertFalse(newPlayingState.bubbles[1].isSelected)
    }

    @Test
    fun `handleSelectBubble clears selection for non-matching bubbles`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When - select a non-matching bubble
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_2_en"))

        // Then - should clear selection
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(0, newPlayingState.matchedPairs)
        assertEquals(emptySet<String>(), newPlayingState.selectedBubbleIds)
        assertFalse(newPlayingState.bubbles[0].isSelected)
        assertFalse(newPlayingState.bubbles[2].isSelected)
    }

    @Test
    fun `handleSelectBubble returns same state when not Playing`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        // When
        val result = updateGameStateUseCase(readyState, GameAction.SelectBubble("pair_1_en"))

        // Then
        assertSame(readyState, result)
    }

    @Test
    fun `handleSelectBubble returns same state when bubble cannot be selected`() {
        val matchedBubbles =
            listOf(
                BubbleState(
                    id = "pair_1_en",
                    word = "apple",
                    pairId = "pair_1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
            )
        val playingState =
            MatchGameState.Playing(
                bubbles = matchedBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_en"))

        // Then
        assertSame(playingState, result)
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
}
