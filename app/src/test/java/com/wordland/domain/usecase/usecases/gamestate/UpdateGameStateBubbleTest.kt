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
 * Bubble selection tests for UpdateGameStateUseCase.
 * Tests SelectBubble action, matching logic, and state transitions.
 */
class UpdateGameStateBubbleTest {
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

    @Test
    fun `handleSelectBubble selects bubble from matching pair`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When - select first bubble
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_en"))

        // Then
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(setOf("pair_1_en"), newPlayingState.selectedBubbleIds)
        assertTrue(newPlayingState.bubbles[0].isSelected)
    }

    @Test
    fun `handleSelectBubble toggles selection on same bubble`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        // When - select same bubble twice
        val result1 = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_1_en"))
        val result2 = updateGameStateUseCase(result1, GameAction.SelectBubble("pair_1_en"))

        // Then
        val newPlayingState = result2 as MatchGameState.Playing
        assertEquals(emptySet<String>(), newPlayingState.selectedBubbleIds)
        assertFalse(newPlayingState.bubbles[0].isSelected)
    }

    @Test
    fun `handleSelectBubble preserves matched bubbles state`() {
        // First pair already matched
        val bubblesWithMatched =
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
                testBubbles[2], // pair_2_en
                testBubbles[3], // pair_2_zh
            )
        val playingState =
            MatchGameState.Playing(
                bubbles = bubblesWithMatched,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 0L,
            )

        // When - select from remaining unmatched pair
        val result = updateGameStateUseCase(playingState, GameAction.SelectBubble("pair_2_en"))

        // Then - matched bubbles should stay matched
        val newPlayingState = result as MatchGameState.Playing
        assertEquals(1, newPlayingState.matchedPairs)
        assertTrue(newPlayingState.bubbles[0].isMatched)
        assertTrue(newPlayingState.bubbles[1].isMatched)
    }
}
