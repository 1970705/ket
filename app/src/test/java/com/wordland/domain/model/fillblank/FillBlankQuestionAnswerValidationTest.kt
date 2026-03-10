package com.wordland.domain.model.fillblank

import com.wordland.domain.model.FillBlankQuestion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FillBlankQuestion answer validation methods.
 * Tests cover isCorrect() and isLetterCorrect() methods.
 */
class FillBlankQuestionAnswerValidationTest {
    // ==================== isCorrect() Tests ====================

    @Test
    fun `isCorrect should return true for correct answer`() {
        val question = createValidQuestion()

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should return false for wrong answer`() {
        val question = createValidQuestion()

        assertFalse(question.isCorrect("banana"))
        assertFalse(question.isCorrect("appl"))
        assertFalse(question.isCorrect("applepie"))
    }

    @Test
    fun `isCorrect should be case insensitive - lowercase`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "Apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should be case insensitive - uppercase`() {
        val question = createValidQuestion()

        assertTrue(question.isCorrect("APPLE"))
    }

    @Test
    fun `isCorrect should be case insensitive - mixed case`() {
        val question = createValidQuestion()

        assertTrue(question.isCorrect("ApPlE"))
        assertTrue(question.isCorrect("aPpLe"))
    }

    @Test
    fun `isCorrect should return false for empty string`() {
        val question = createValidQuestion()

        assertFalse(question.isCorrect(""))
    }

    @Test
    fun `isCorrect should return false for answer with leading or trailing whitespace`() {
        val question = createValidQuestion()

        assertFalse(question.isCorrect(" apple"))
        assertFalse(question.isCorrect("apple "))
        assertFalse(question.isCorrect(" apple "))
    }

    @Test
    fun `isCorrect should return false for partial answer`() {
        val question = createValidQuestion()

        assertFalse(question.isCorrect("app"))
        assertFalse(question.isCorrect("appl"))
    }

    @Test
    fun `isCorrect should handle single letter word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "A",
                targetWord = "a",
                firstLetterHint = "a",
                letterPool = listOf('a'),
            )

        assertTrue(question.isCorrect("a"))
        assertTrue(question.isCorrect("A"))
        assertFalse(question.isCorrect("b"))
    }

    @Test
    fun `isCorrect should handle long word`() {
        val word = "accomplishment"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "成就",
                targetWord = word,
                firstLetterHint = "a",
                letterPool = word.toList(),
            )

        assertTrue(question.isCorrect("accomplishment"))
    }

    @Test
    fun `isCorrect should handle word with repeated letters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "香蕉",
                targetWord = "banana",
                firstLetterHint = "b",
                letterPool = listOf('b', 'a', 'n', 'a', 'n', 'a'),
            )

        assertTrue(question.isCorrect("banana"))
    }

    @Test
    fun `isCorrect should handle word with all unique letters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "春天",
                targetWord = "spring",
                firstLetterHint = "s",
                letterPool = listOf('s', 'p', 'r', 'i', 'n', 'g'),
            )

        assertTrue(question.isCorrect("spring"))
    }

    @Test
    fun `isCorrect should handle uppercase target word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "APPLE",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertTrue(question.isCorrect("apple"))
        assertTrue(question.isCorrect("APPLE"))
    }

    @Test
    fun `isCorrect should handle mixed case target word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "ApPlE",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertTrue(question.isCorrect("apple"))
        assertTrue(question.isCorrect("APPLE"))
        assertTrue(question.isCorrect("ApPlE"))
    }

    @Test
    fun `isCorrect should handle word with apostrophe`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "不能",
                targetWord = "can't",
                firstLetterHint = "c",
                letterPool = listOf('c', 'a', 'n', '\'', 't'),
            )

        assertTrue(question.isCorrect("can't"))
    }

    @Test
    fun `isCorrect should handle word with hyphen`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "阿姨",
                targetWord = "aunt-like",
                firstLetterHint = "a",
                letterPool = listOf('a', 'u', 'n', 't', '-', 'l', 'i', 'k', 'e'),
            )

        assertTrue(question.isCorrect("aunt-like"))
    }

    // ==================== isLetterCorrect() Tests ====================

    @Test
    fun `isLetterCorrect should return true for correct letter at position 0`() {
        val question = createValidQuestion()

        assertTrue(question.isLetterCorrect('a', 0))
        assertTrue(question.isLetterCorrect('A', 0))
    }

    @Test
    fun `isLetterCorrect should return true for correct letter at middle positions`() {
        val question = createValidQuestion()

        assertTrue(question.isLetterCorrect('p', 1))
        assertTrue(question.isLetterCorrect('P', 2))
        assertTrue(question.isLetterCorrect('l', 3))
    }

    @Test
    fun `isLetterCorrect should return true for correct letter at last position`() {
        val question = createValidQuestion()

        assertTrue(question.isLetterCorrect('e', 4))
        assertTrue(question.isLetterCorrect('E', 4))
    }

    @Test
    fun `isLetterCorrect should return false for wrong letter at valid position`() {
        val question = createValidQuestion()

        assertFalse(question.isLetterCorrect('b', 0))
        assertFalse(question.isLetterCorrect('x', 2))
        assertFalse(question.isLetterCorrect('z', 4))
    }

    @Test
    fun `isLetterCorrect should return false for negative position`() {
        val question = createValidQuestion()

        assertFalse(question.isLetterCorrect('a', -1))
        assertFalse(question.isLetterCorrect('a', -10))
    }

    @Test
    fun `isLetterCorrect should return false for position beyond word length`() {
        val question = createValidQuestion()

        assertFalse(question.isLetterCorrect('a', 5))
        assertFalse(question.isLetterCorrect('a', 10))
    }

    @Test
    fun `isLetterCorrect should be case insensitive`() {
        val question = createValidQuestion()

        assertTrue(question.isLetterCorrect('A', 0))
        assertTrue(question.isLetterCorrect('P', 1))
        assertTrue(question.isLetterCorrect('a', 0))
        assertTrue(question.isLetterCorrect('p', 1))
    }

    @Test
    fun `isLetterCorrect should handle all positions for word`() {
        val word = "hello"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "你好",
                targetWord = word,
                firstLetterHint = "h",
                letterPool = word.toList(),
            )

        assertTrue(question.isLetterCorrect('h', 0))
        assertTrue(question.isLetterCorrect('e', 1))
        assertTrue(question.isLetterCorrect('l', 2))
        assertTrue(question.isLetterCorrect('l', 3))
        assertTrue(question.isLetterCorrect('o', 4))
    }

    @Test
    fun `isLetterCorrect should handle single letter word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "I",
                targetWord = "i",
                firstLetterHint = "i",
                letterPool = listOf('i'),
            )

        assertTrue(question.isLetterCorrect('i', 0))
    }

    @Test
    fun `isLetterCorrect should handle long word`() {
        val word = "accomplishment"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "成就",
                targetWord = word,
                firstLetterHint = "a",
                letterPool = word.toList(),
            )

        assertTrue(question.isLetterCorrect('a', 0))
        assertTrue(question.isLetterCorrect('t', 13))
        assertFalse(question.isLetterCorrect('z', 5))
    }

    @Test
    fun `isLetterCorrect should handle word with hyphen`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "阿姨",
                targetWord = "aunt-like",
                firstLetterHint = "a",
                letterPool = listOf('a', 'u', 'n', 't', '-', 'l', 'i', 'k', 'e'),
            )

        assertTrue(question.isLetterCorrect('-', 4))
    }

    // ==================== Helper Methods ====================

    private fun createValidQuestion(): FillBlankQuestion {
        return FillBlankQuestion(
            wordId = "word_001",
            translation = "苹果",
            targetWord = "apple",
            firstLetterHint = "a",
            letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            difficulty = 1,
        )
    }
}
