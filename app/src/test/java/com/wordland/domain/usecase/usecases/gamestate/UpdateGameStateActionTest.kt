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
 * Game action tests for UpdateGameStateUseCase.
 * Tests StartGame, PauseGame, ResumeGame, ExitGame, ResetGame, RestartGame actions.
 */
class UpdateGameStateActionTest {
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

    // === StartGame Action Tests ===

    @Test
    fun `handleStartGame returns Playing state when current state is Ready`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        // When
        val result = updateGameStateUseCase(readyState, GameAction.StartGame)

        // Then
        assertTrue(result is MatchGameState.Playing)
        assertEquals(testBubbles, (result as MatchGameState.Playing).bubbles)
        assertEquals(emptySet<String>(), result.selectedBubbleIds)
        assertEquals(0, result.matchedPairs)
    }

    @Test
    fun `handleStartGame sets start time correctly`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)
        val beforeTime = System.currentTimeMillis()

        // When
        val result = updateGameStateUseCase(readyState, GameAction.StartGame)

        // Then
        val playingState = result as MatchGameState.Playing
        assertTrue(playingState.startTime >= beforeTime)
        assertTrue(playingState.startTime <= System.currentTimeMillis())
    }

    @Test
    fun `handleStartGame returns same state when not in Ready state`() {
        val idleState = MatchGameState.Idle

        // When
        val result = updateGameStateUseCase(idleState, GameAction.StartGame)

        // Then
        assertSame(idleState, result)
    }

    // === PauseGame Action Tests ===

    @Test
    fun `handlePauseGame returns Paused state when current state is Playing`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 0,
                elapsedTime = 500L,
                startTime = System.currentTimeMillis() - 1000,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.PauseGame)

        // Then
        assertTrue(result is MatchGameState.Paused)
        val pausedState = result as MatchGameState.Paused
        assertEquals(testBubbles, pausedState.previousState.bubbles)
        assertEquals(setOf("pair_1_en"), pausedState.previousState.selectedBubbleIds)
        assertEquals(0, pausedState.previousState.matchedPairs)
        assertTrue(pausedState.previousState.elapsedTime >= 500L)
    }

    @Test
    fun `handlePauseGame updates elapsed time correctly`() {
        val startTime = System.currentTimeMillis() - 2000
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 500L,
                startTime = startTime,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.PauseGame)

        // Then
        val pausedState = result as MatchGameState.Paused
        assertTrue(pausedState.previousState.elapsedTime >= 500L)
    }

    @Test
    fun `handlePauseGame returns same state when not Playing`() {
        val idleState = MatchGameState.Idle

        // When
        val result = updateGameStateUseCase(idleState, GameAction.PauseGame)

        // Then
        assertSame(idleState, result)
    }

    // === ResumeGame Action Tests ===

    @Test
    fun `handleResumeGame returns Playing state when current state is Paused`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )
        val pausedState = MatchGameState.Paused(previousState = playingState)

        // When
        val result = updateGameStateUseCase(pausedState, GameAction.ResumeGame)

        // Then
        assertTrue(result is MatchGameState.Playing)
        val resumedState = result as MatchGameState.Playing
        assertEquals(1, resumedState.matchedPairs)
        assertTrue(resumedState.startTime > 0)
    }

    @Test
    fun `handleResumeGame returns same state when not Paused`() {
        val idleState = MatchGameState.Idle

        // When
        val result = updateGameStateUseCase(idleState, GameAction.ResumeGame)

        // Then
        assertSame(idleState, result)
    }

    // === ExitGame Action Tests ===

    @Test
    fun `handleExitGame returns GameOver state`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.ExitGame)

        // Then
        assertSame(MatchGameState.GameOver, result)
    }

    @Test
    fun `handleExitGame returns GameOver from any state`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        // When
        val result = updateGameStateUseCase(readyState, GameAction.ExitGame)

        // Then
        assertSame(MatchGameState.GameOver, result)
    }

    // === ResetGame Action Tests ===

    @Test
    fun `handleResetGame returns Idle state`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.ResetGame)

        // Then
        assertSame(MatchGameState.Idle, result)
    }

    @Test
    fun `handleResetGame returns Idle from any valid state`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        // When
        val result = updateGameStateUseCase(readyState, GameAction.ResetGame)

        // Then
        assertSame(MatchGameState.Idle, result)
    }

    // === RestartGame Action Tests ===

    @Test
    fun `handleRestartGame returns Idle from Ready state`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        // When
        val result = updateGameStateUseCase(readyState, GameAction.RestartGame)

        // Then
        assertSame(MatchGameState.Idle, result)
    }

    @Test
    fun `handleRestartGame returns Idle from Playing state`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )

        // When
        val result = updateGameStateUseCase(playingState, GameAction.RestartGame)

        // Then
        assertSame(MatchGameState.Idle, result)
    }

    @Test
    fun `handleRestartGame returns Idle from Paused state`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )
        val pausedState = MatchGameState.Paused(previousState = playingState)

        // When
        val result = updateGameStateUseCase(pausedState, GameAction.RestartGame)

        // Then
        assertSame(MatchGameState.Idle, result)
    }

    @Test
    fun `handleRestartGame returns same state from Idle`() {
        val idleState = MatchGameState.Idle

        // When
        val result = updateGameStateUseCase(idleState, GameAction.RestartGame)

        // Then
        assertSame(idleState, result)
    }

    @Test
    fun `handleRestartGame returns same state from Completed`() {
        val completedState =
            MatchGameState.Completed(
                elapsedTime = 5000L,
                pairs = 2,
                accuracy = 1.0f,
            )

        // When
        val result = updateGameStateUseCase(completedState, GameAction.RestartGame)

        // Then
        assertSame(completedState, result)
    }
}
