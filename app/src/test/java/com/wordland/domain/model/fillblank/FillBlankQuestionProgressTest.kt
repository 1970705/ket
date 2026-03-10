package com.wordland.domain.model.fillblank

import com.wordland.domain.model.FillBlankQuestion
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for FillBlankQuestion progress tracking methods.
 * Tests cover getAnswerProgress() and getHint() methods.
 */
class FillBlankQuestionProgressTest {
    // ==================== getAnswerProgress() Tests ====================

    @Test
    fun `getAnswerProgress should return 0,0 for empty answer`() {
        val question = createValidQuestion()

        val progress = question.getAnswerProgress("")

        assertEquals(Pair(0, 5), progress)
    }

    @Test
    fun `getAnswerProgress should return correct count for partial correct answer`() {
        val question = createValidQuestion()

        assertEquals(Pair(1, 5), question.getAnswerProgress("a"))
        assertEquals(Pair(2, 5), question.getAnswerProgress("ap"))
        assertEquals(Pair(3, 5), question.getAnswerProgress("app"))
        assertEquals(Pair(4, 5), question.getAnswerProgress("appl"))
    }

    @Test
    fun `getAnswerProgress should return full count for complete correct answer`() {
        val question = createValidQuestion()

        assertEquals(Pair(5, 5), question.getAnswerProgress("apple"))
    }

    @Test
    fun `getAnswerProgress should count only correct letters in wrong answer`() {
        val question = createValidQuestion()

        assertEquals(Pair(1, 5), question.getAnswerProgress("axxxx")) // Only 'a' correct
        assertEquals(Pair(2, 5), question.getAnswerProgress("apxxx")) // 'a' and 'p' correct
        assertEquals(Pair(0, 5), question.getAnswerProgress("bnana")) // No correct letters
    }

    @Test
    fun `getAnswerProgress should be case insensitive`() {
        val question = createValidQuestion()

        assertEquals(Pair(5, 5), question.getAnswerProgress("APPLE"))
        assertEquals(Pair(3, 5), question.getAnswerProgress("APP"))
        assertEquals(Pair(2, 5), question.getAnswerProgress("aP"))
    }

    @Test
    fun `getAnswerProgress should handle answer longer than target word`() {
        val question = createValidQuestion()

        // Should only count up to target word length
        assertEquals(Pair(5, 5), question.getAnswerProgress("applepie"))
    }

    @Test
    fun `getAnswerProgress should handle single letter word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "A",
                targetWord = "a",
                firstLetterHint = "a",
                letterPool = listOf('a'),
            )

        assertEquals(Pair(0, 1), question.getAnswerProgress(""))
        assertEquals(Pair(1, 1), question.getAnswerProgress("a"))
        assertEquals(Pair(0, 1), question.getAnswerProgress("b"))
    }

    @Test
    fun `getAnswerProgress should handle long word`() {
        val word = "elephant"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "大象",
                targetWord = word,
                firstLetterHint = "e",
                letterPool = word.toList(),
            )

        assertEquals(Pair(0, 8), question.getAnswerProgress(""))
        assertEquals(Pair(4, 8), question.getAnswerProgress("elep"))
        assertEquals(Pair(8, 8), question.getAnswerProgress("elephant"))
    }

    @Test
    fun `getAnswerProgress should handle single letter`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "I",
                targetWord = "i",
                firstLetterHint = "i",
                letterPool = listOf('i'),
            )

        assertEquals(Pair(1, 1), question.getAnswerProgress("i"))
    }

    @Test
    fun `getAnswerProgress should handle long word completely correct`() {
        val word = "accomplishment"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "成就",
                targetWord = word,
                firstLetterHint = "a",
                letterPool = word.toList(),
            )

        assertEquals(Pair(14, 14), question.getAnswerProgress("accomplishment"))
    }

    @Test
    fun `getAnswerProgress should handle word with apostrophe`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "不能",
                targetWord = "can't",
                firstLetterHint = "c",
                letterPool = listOf('c', 'a', 'n', '\'', 't'),
            )

        assertEquals(Pair(5, 5), question.getAnswerProgress("can't"))
    }

    @Test
    fun `getAnswerProgress should handle word with hyphen`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "阿姨",
                targetWord = "aunt-like",
                firstLetterHint = "a",
                letterPool = listOf('a', 'u', 'n', 't', '-', 'l', 'i', 'k', 'e'),
            )

        assertEquals(Pair(9, 9), question.getAnswerProgress("aunt-like"))
    }

    @Test
    fun `getAnswerProgress should handle word with all unique letters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "春天",
                targetWord = "spring",
                firstLetterHint = "s",
                letterPool = listOf('s', 'p', 'r', 'i', 'n', 'g'),
            )

        assertEquals(Pair(6, 6), question.getAnswerProgress("spring"))
    }

    @Test
    fun `getAnswerProgress should handle word with repeated letters`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "香蕉",
                targetWord = "banana",
                firstLetterHint = "b",
                letterPool = listOf('b', 'a', 'n', 'a', 'n', 'a'),
            )

        assertEquals(Pair(6, 6), question.getAnswerProgress("banana"))
    }

    // ==================== getHint() Tests ====================

    @Test
    fun `getHint should return first letter hint correctly`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "a",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertEquals("首字母: a", question.getHint())
    }

    @Test
    fun `getHint should work with uppercase hint`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                firstLetterHint = "A",
                letterPool = listOf('a', 'p', 'p', 'l', 'e'),
            )

        assertEquals("首字母: A", question.getHint())
    }

    @Test
    fun `getHint should work for single letter word`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "A",
                targetWord = "a",
                firstLetterHint = "a",
                letterPool = listOf('a'),
            )

        assertEquals("首字母: a", question.getHint())
    }

    @Test
    fun `getHint should work for single letter`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "I",
                targetWord = "i",
                firstLetterHint = "i",
                letterPool = listOf('i'),
            )

        assertEquals("首字母: i", question.getHint())
    }

    @Test
    fun `getHint should work for long word`() {
        val word = "accomplishment"
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "成就",
                targetWord = word,
                firstLetterHint = "a",
                letterPool = word.toList(),
            )

        assertEquals("首字母: a", question.getHint())
    }

    @Test
    fun `getHint should work for word with hyphen`() {
        val question =
            FillBlankQuestion(
                wordId = "word_001",
                translation = "阿姨",
                targetWord = "aunt-like",
                firstLetterHint = "a",
                letterPool = listOf('a', 'u', 'n', 't', '-', 'l', 'i', 'k', 'e'),
            )

        assertEquals("首字母: a", question.getHint())
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
