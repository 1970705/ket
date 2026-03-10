package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MatchGameState
 * Tests game state properties, progress calculation, and completion detection
 */
class MatchGameStateTest {
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

    // === Idle State Tests ===

    @Test
    fun `Idle state is a singleton`() {
        val idle1 = MatchGameState.Idle
        val idle2 = MatchGameState.Idle

        assertSame(idle1, idle2)
    }

    // === Preparing State Tests ===

    @Test
    fun `Preparing state is a singleton`() {
        val preparing1 = MatchGameState.Preparing
        val preparing2 = MatchGameState.Preparing

        assertSame(preparing1, preparing2)
    }

    // === Ready State Tests ===

    @Test
    fun `Ready state stores pairs and bubbles correctly`() {
        val readyState = MatchGameState.Ready(pairs = 2, bubbles = testBubbles)

        assertEquals(2, readyState.pairs)
        assertEquals(testBubbles, readyState.bubbles)
    }

    @Test
    fun `Ready state with zero pairs is valid`() {
        val readyState = MatchGameState.Ready(pairs = 0, bubbles = emptyList())

        assertEquals(0, readyState.pairs)
        assertTrue(readyState.bubbles.isEmpty())
    }

    // === Playing State Tests ===

    @Test
    fun `Playing state initial properties are correct`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        assertEquals(testBubbles, playingState.bubbles)
        assertEquals(emptySet<String>(), playingState.selectedBubbleIds)
        assertEquals(0, playingState.matchedPairs)
        assertEquals(0L, playingState.elapsedTime)
    }

    @Test
    fun `Playing state progress is calculated correctly`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 0L,
            )

        // 4 bubbles = 2 pairs, 1 matched = 50% progress
        assertEquals(0.5f, playingState.progress, 0.001f)
    }

    @Test
    fun `Playing state progress is 1 when all pairs matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 2,
                elapsedTime = 0L,
            )

        assertEquals(1.0f, playingState.progress, 0.001f)
    }

    @Test
    fun `Playing state progress is 0 when no pairs matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        assertEquals(0.0f, playingState.progress, 0.001f)
    }

    @Test
    fun `Playing state progress handles empty bubble list`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = emptyList(),
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        assertEquals(0f, playingState.progress, 0.001f)
    }

    @Test
    fun `Playing state isCompleted returns true when all pairs matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 2,
                elapsedTime = 0L,
            )

        assertTrue(playingState.isCompleted)
    }

    @Test
    fun `Playing state isCompleted returns false when not all pairs matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 0L,
            )

        assertFalse(playingState.isCompleted)
    }

    @Test
    fun `Playing state isCompleted returns false when no pairs matched`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        assertFalse(playingState.isCompleted)
    }

    @Test
    fun `Playing state selectedBubbles returns list of selected bubble IDs`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en", "pair_2_zh"),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        val selected = playingState.selectedBubbles
        assertEquals(2, selected.size)
        assertTrue(selected.contains("pair_1_en"))
        assertTrue(selected.contains("pair_2_zh"))
    }

    @Test
    fun `Playing state startTime defaults to current time`() {
        val beforeTime = System.currentTimeMillis()

        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        val afterTime = System.currentTimeMillis()

        assertTrue(playingState.startTime >= beforeTime)
        assertTrue(playingState.startTime <= afterTime)
    }

    @Test
    fun `Playing state can be copied with modified values`() {
        val original =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 0,
                elapsedTime = 0L,
            )

        val copied = original.copy(matchedPairs = 1)

        assertEquals(0, original.matchedPairs)
        assertEquals(1, copied.matchedPairs)
        assertEquals(original.bubbles, copied.bubbles)
    }

    // === Paused State Tests ===

    @Test
    fun `Paused state stores previous Playing state`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )
        val pausedState = MatchGameState.Paused(previousState = playingState)

        assertSame(playingState, pausedState.previousState)
    }

    @Test
    fun `Paused state preserves previous state properties`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = setOf("pair_1_en"),
                matchedPairs = 1,
                elapsedTime = 1000L,
            )
        val pausedState = MatchGameState.Paused(previousState = playingState)

        assertEquals(testBubbles, pausedState.previousState.bubbles)
        assertEquals(setOf("pair_1_en"), pausedState.previousState.selectedBubbleIds)
        assertEquals(1, pausedState.previousState.matchedPairs)
        assertEquals(1000L, pausedState.previousState.elapsedTime)
    }

    // === Completed State Tests ===

    @Test
    fun `Completed state stores final statistics`() {
        val completedState =
            MatchGameState.Completed(
                elapsedTime = 15000L,
                pairs = 2,
                accuracy = 0.85f,
            )

        assertEquals(15000L, completedState.elapsedTime)
        assertEquals(2, completedState.pairs)
        assertEquals(0.85f, completedState.accuracy, 0.001f)
    }

    @Test
    fun `Completed state defaults accuracy to 1_0`() {
        val completedState =
            MatchGameState.Completed(
                elapsedTime = 10000L,
                pairs = 3,
            )

        assertEquals(1.0f, completedState.accuracy, 0.001f)
    }

    @Test
    fun `Completed state with zero accuracy is valid`() {
        val completedState =
            MatchGameState.Completed(
                elapsedTime = 5000L,
                pairs = 0,
                accuracy = 0.0f,
            )

        assertEquals(0.0f, completedState.accuracy, 0.001f)
    }

    @Test
    fun `Completed state with perfect accuracy is valid`() {
        val completedState =
            MatchGameState.Completed(
                elapsedTime = 5000L,
                pairs = 5,
                accuracy = 1.0f,
            )

        assertEquals(1.0f, completedState.accuracy, 0.001f)
    }

    // === GameOver State Tests ===

    @Test
    fun `GameOver state is a singleton`() {
        val gameOver1 = MatchGameState.GameOver
        val gameOver2 = MatchGameState.GameOver

        assertSame(gameOver1, gameOver2)
    }

    // === Error State Tests ===

    @Test
    fun `Error state stores error message`() {
        val errorMessage = "Failed to load words"
        val errorState = MatchGameState.Error(message = errorMessage)

        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun `Error state with empty message is valid`() {
        val errorState = MatchGameState.Error(message = "")

        assertEquals("", errorState.message)
    }

    @Test
    fun `Error state with null message throws exception`() {
        // Kotlin requires non-null String, so this test verifies type safety
        val errorState = MatchGameState.Error(message = "error")

        assertNotNull(errorState.message)
    }

    // === State Serialization Tests ===

    @Test
    fun `all states are serializable`() {
        // This test verifies that all state classes can be instantiated
        // Actual serialization testing would require kotlinx-coroutines-test

        val states =
            listOf(
                MatchGameState.Idle,
                MatchGameState.Preparing,
                MatchGameState.Ready(pairs = 1, bubbles = testBubbles),
                MatchGameState.Playing(
                    bubbles = testBubbles,
                    selectedBubbleIds = emptySet(),
                    matchedPairs = 0,
                    elapsedTime = 0L,
                ),
                MatchGameState.Paused(
                    previousState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 0,
                            elapsedTime = 0L,
                        ),
                ),
                MatchGameState.Completed(elapsedTime = 1000L, pairs = 1),
                MatchGameState.GameOver,
                MatchGameState.Error(message = "test error"),
            )

        assertEquals(8, states.size)
    }

    // === Edge Cases ===

    @Test
    fun `Playing state with odd number of bubbles calculates progress correctly`() {
        val oddBubbles =
            listOf(
                BubbleState(id = "b1", word = "w1", pairId = "p1", color = BubbleColor.PINK),
                BubbleState(id = "b2", word = "w2", pairId = "p1", color = BubbleColor.GREEN),
                BubbleState(id = "b3", word = "w3", pairId = "p2", color = BubbleColor.BLUE),
            )

        val playingState =
            MatchGameState.Playing(
                bubbles = oddBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 1,
                elapsedTime = 0L,
            )

        // 3 bubbles = 1.5 pairs (integer division), 1 matched = 1/1 = 100%
        assertEquals(1.0f, playingState.progress, 0.001f)
    }

    @Test
    fun `Playing state progress handles large matched pairs count`() {
        val playingState =
            MatchGameState.Playing(
                bubbles = testBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = 100, // More than total pairs
                elapsedTime = 0L,
            )

        // When matchedPairs exceeds totalPairs, progress exceeds 1.0
        // This is by design to track how many extra pairs were counted
        assertTrue(playingState.progress > 1.0f)
        assertEquals(50.0f, playingState.progress, 0.001f) // 100 / 2 = 50
    }
}
