package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CheckMatchUseCase
 * Tests bubble matching logic, selection validation, and game completion detection
 */
class CheckMatchUseCaseTest {
    private lateinit var checkMatchUseCase: CheckMatchUseCase

    // Test bubbles
    private val bubbleEn =
        BubbleState(
            id = "pair_apple_en",
            word = "apple",
            pairId = "pair_apple",
            color = BubbleColor.PINK,
        )

    private val bubbleZh =
        BubbleState(
            id = "pair_apple_zh",
            word = "苹果",
            pairId = "pair_apple",
            color = BubbleColor.GREEN,
        )

    private val bubbleDifferentPair =
        BubbleState(
            id = "pair_banana_en",
            word = "banana",
            pairId = "pair_banana",
            color = BubbleColor.BLUE,
        )

    private val bubbleMatched =
        BubbleState(
            id = "pair_orange_en",
            word = "orange",
            pairId = "pair_orange",
            isMatched = true,
            color = BubbleColor.ORANGE,
        )

    @Before
    fun setup() {
        checkMatchUseCase = CheckMatchUseCase()
    }

    // === Match Result Tests ===

    @Test
    fun `invoke returns Success when bubbles have matching pairId`() {
        // When
        val result = checkMatchUseCase(bubbleEn, bubbleZh)

        // Then
        assertTrue(result is MatchResult.Success)
    }

    @Test
    fun `invoke returns Failed when bubbles have different pairId`() {
        // When
        val result = checkMatchUseCase(bubbleEn, bubbleDifferentPair)

        // Then
        assertTrue(result is MatchResult.Failed)
    }

    @Test
    fun `invoke returns Invalid when first bubble is null`() {
        // When
        val result = checkMatchUseCase(null, bubbleZh)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when second bubble is null`() {
        // When
        val result = checkMatchUseCase(bubbleEn, null)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when both bubbles are null`() {
        // When
        val result = checkMatchUseCase(null, null)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when bubbles have same id`() {
        // When
        val result = checkMatchUseCase(bubbleEn, bubbleEn)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when first bubble is already matched`() {
        // When
        val result = checkMatchUseCase(bubbleMatched, bubbleZh)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when second bubble is already matched`() {
        // When
        val result = checkMatchUseCase(bubbleEn, bubbleMatched)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    @Test
    fun `invoke returns Invalid when both bubbles are already matched`() {
        val matchedBubble1 =
            BubbleState(
                id = "pair_1_en",
                word = "word1",
                pairId = "pair_1",
                isMatched = true,
                color = BubbleColor.PINK,
            )
        val matchedBubble2 =
            BubbleState(
                id = "pair_1_zh",
                word = "单词1",
                pairId = "pair_1",
                isMatched = true,
                color = BubbleColor.GREEN,
            )

        // When
        val result = checkMatchUseCase(matchedBubble1, matchedBubble2)

        // Then
        assertTrue(result is MatchResult.Invalid)
    }

    // === canSelectBubble Tests ===

    @Test
    fun `canSelectBubble returns true for valid bubble`() {
        // When
        val result = checkMatchUseCase.canSelectBubble(bubbleEn)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canSelectBubble returns false when bubble is null`() {
        // When
        val result = checkMatchUseCase.canSelectBubble(null)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canSelectBubble returns false when bubble is already matched`() {
        // When
        val result = checkMatchUseCase.canSelectBubble(bubbleMatched)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canSelectBubble returns false when bubble is selected`() {
        val selectedBubble =
            BubbleState(
                id = "pair_test_en",
                word = "test",
                pairId = "pair_test",
                isSelected = true,
                color = BubbleColor.PINK,
            )

        // When
        val result = checkMatchUseCase.canSelectBubble(selectedBubble)

        // Then
        assertFalse(result)
    }

    @Test
    fun `canSelectBubble returns false when bubble is matched and selected`() {
        val matchedAndSelected =
            BubbleState(
                id = "pair_test_en",
                word = "test",
                pairId = "pair_test",
                isSelected = true,
                isMatched = true,
                color = BubbleColor.PINK,
            )

        // When
        val result = checkMatchUseCase.canSelectBubble(matchedAndSelected)

        // Then
        assertFalse(result)
    }

    // === isGameCompleted Tests ===

    @Test
    fun `isGameCompleted returns true when all bubbles are matched`() {
        val bubbles =
            listOf(
                bubbleMatched,
                BubbleState(
                    id = "pair_orange_zh",
                    word = "橙子",
                    pairId = "pair_orange",
                    isMatched = true,
                    color = BubbleColor.ORANGE,
                ),
            )

        // When
        val result = checkMatchUseCase.isGameCompleted(bubbles)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isGameCompleted returns false when some bubbles are not matched`() {
        val bubbles =
            listOf(
                bubbleMatched,
                BubbleState(
                    id = "pair_orange_zh",
                    word = "橙子",
                    pairId = "pair_orange",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )

        // When
        val result = checkMatchUseCase.isGameCompleted(bubbles)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isGameCompleted returns true for empty bubble list`() {
        // When
        val result = checkMatchUseCase.isGameCompleted(emptyList())

        // Then
        assertTrue(result)
    }

    @Test
    fun `isGameCompleted returns false when no bubbles are matched`() {
        val bubbles = listOf(bubbleEn, bubbleZh, bubbleDifferentPair)

        // When
        val result = checkMatchUseCase.isGameCompleted(bubbles)

        // Then
        assertFalse(result)
    }

    // === Edge Case Tests ===

    @Test
    fun `invoke handles bubbles with same pairId but different ids correctly`() {
        val bubble1 =
            BubbleState(
                id = "pair_test_en",
                word = "test",
                pairId = "pair_test",
                color = BubbleColor.PINK,
            )
        val bubble2 =
            BubbleState(
                id = "pair_test_zh",
                word = "测试",
                pairId = "pair_test",
                color = BubbleColor.GREEN,
            )

        // When
        val result = checkMatchUseCase(bubble1, bubble2)

        // Then
        assertTrue(result is MatchResult.Success)
    }

    @Test
    fun `invoke matches are case-sensitive for pairId`() {
        val bubble1 =
            BubbleState(
                id = "bubble1",
                word = "word",
                pairId = "Pair_Test",
                color = BubbleColor.PINK,
            )
        val bubble2 =
            BubbleState(
                id = "bubble2",
                word = "单词",
                pairId = "pair_test",
                color = BubbleColor.GREEN,
            )

        // When
        val result = checkMatchUseCase(bubble1, bubble2)

        // Then
        assertTrue(result is MatchResult.Failed)
    }

    @Test
    fun `isGameCompleted handles bubbles with mixed states correctly`() {
        val bubbles =
            listOf(
                BubbleState(
                    id = "b1",
                    word = "w1",
                    pairId = "p1",
                    isMatched = true,
                    color = BubbleColor.PINK,
                ),
                BubbleState(
                    id = "b2",
                    word = "w2",
                    pairId = "p1",
                    isMatched = true,
                    color = BubbleColor.GREEN,
                ),
                BubbleState(
                    id = "b3",
                    word = "w3",
                    pairId = "p2",
                    isMatched = true,
                    color = BubbleColor.BLUE,
                ),
                BubbleState(
                    id = "b4",
                    word = "w4",
                    pairId = "p2",
                    isMatched = false,
                    color = BubbleColor.ORANGE,
                ),
            )

        // When
        val result = checkMatchUseCase.isGameCompleted(bubbles)

        // Then
        assertFalse(result)
    }
}
