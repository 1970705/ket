package com.wordland.domain.model.fillblank

import com.wordland.domain.model.FillBlankQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FillBlankQuestion constructor validation.
 * Tests cover constructor parameters, defaults, and edge cases.
 */
class FillBlankQuestionConstructorTest {
    // ==================== Constructor Validation Tests ====================

    @Test
    fun `constructor should accept valid question with all parameters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
                difficulty = 1,
                emoji = "\uD83C\uDF4E",
            )

        assertEquals("word_001", question.wordId)
        assertEquals("苹果", question.translation)
        assertEquals("apple", question.targetWord)
        assertEquals("a", question.firstLetterHint)
        assertEquals(5, question.letterPool.size)
        assertEquals(1, question.difficulty)
        assertEquals("\uD83C\uDF4E", question.emoji)
    }

    @Test
    fun `constructor should use default difficulty of 1`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertEquals(1, question.difficulty)
    }

    @Test
    fun `constructor should accept null emoji`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
                emoji = null,
            )

        assertEquals(null, question.emoji)
    }

    @Test
    fun `constructor should accept question with scrambled letter pool`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('p', 'e', 'a', 'l', 'p'), // Scrambled
            )

        assertEquals(5, question.letterPool.size)
        assertTrue(question.letterPool.contains('a'))
        assertTrue(question.letterPool.contains('p'))
        assertTrue(question.letterPool.contains('l'))
        assertTrue(question.letterPool.contains('e'))
    }

    @Test
    fun `constructor should accept question with extra letters in pool`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e', 'x', 'y'), // Extra letters
            )

        assertEquals(7, question.letterPool.size)
    }

    @Test
    fun `constructor should accept question with uppercase letters in pool`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('A', 'P', 'P', 'L', 'E'),
            )

        assertEquals(5, question.letterPool.size)
    }

    @Test
    fun `constructor should handle question with uppercase target word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "APPLE",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertEquals("APPLE", question.targetWord)
        assertEquals(5, question.letterPool.size)
    }

    @Test
    fun `constructor should handle question with mixed case target word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "ApPlE",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertEquals("ApPlE", question.targetWord)
        assertEquals(5, question.letterPool.size)
    }

    @Test
    fun `constructor should handle word with apostrophe`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "不能",
                targetWord = "can't",
                firstLetterHint = "c",
                letterPool = listOf('c', 'a', 'n', '\'', 't'),
            )

        assertEquals("can't", question.targetWord)
        assertEquals(5, question.letterPool.size)
    }

    @Test
    fun `constructor should handle word with hyphen`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "阿姨",
                targetWord = "aunt-like",
                firstLetterHint = "a",
                letterPool = listOf('a', 'u', 'n', 't', '-', 'l', 'i', 'k', 'e'),
            )

        assertEquals("aunt-like", question.targetWord)
        assertEquals(9, question.letterPool.size)
    }

    @Test
    fun `constructor should handle letterPool with mixed case letters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('A', 'P', 'p', 'L', 'e'),
            )

        assertEquals(5, question.letterPool.size)
    }

    @Test
    fun `constructor should handle all valid difficulty levels`() {
        for (difficulty in 1..5) {
            val question =
                FillBlankQuestion(
                    wordId = "word_001",
                    translation = "苹果",
                    targetWord = "apple",
                    firstLetterHint = "a",
                    letterPool = listOf('a', 'p', 'p', 'l', 'e'),
                    difficulty = difficulty,
                )

            assertEquals(difficulty, question.difficulty)
        }
    }
}
