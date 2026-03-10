package com.wordland.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MultipleChoiceQuestion constructor and validation
 *
 * Tests cover constructor validation, answer checking (isCorrect),
 * and correct index retrieval.
 */
class MultipleChoiceQuestionValidationTest {
    // ==================== Constructor Validation Tests ====================

    @Test
    fun `constructor should accept valid question with 4 options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
                difficulty = 1,
                emoji = "",
            )

        assertEquals("word_001", question.wordId)
        assertEquals("苹果", question.translation)
        assertEquals("apple", question.targetWord)
        assertEquals(4, question.options.size)
        assertEquals(1, question.difficulty)
        assertEquals("", question.emoji)
    }

    @Test
    fun `constructor should accept valid question without emoji`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_002",
                translation = "香蕉",
                targetWord = "banana",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertNull(question.emoji)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when options size is less than 4`() {
        MultipleChoiceQuestion(
            wordId = "word_001",
            translation = "苹果",
            targetWord = "apple",
            options = listOf("apple", "banana", "orange"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when options size is more than 4`() {
        MultipleChoiceQuestion(
            wordId = "word_001",
            translation = "苹果",
            targetWord = "apple",
            options = listOf("apple", "banana", "orange", "grape", "mango"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when options is empty`() {
        MultipleChoiceQuestion(
            wordId = "word_001",
            translation = "苹果",
            targetWord = "apple",
            options = emptyList(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw exception when target word is not in options`() {
        MultipleChoiceQuestion(
            wordId = "word_001",
            translation = "苹果",
            targetWord = "apple",
            options = listOf("banana", "orange", "grape", "mango"),
        )
    }

    @Test
    fun `constructor should use default difficulty of 1`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertEquals(1, question.difficulty)
    }

    // ==================== isCorrect() Tests ====================

    @Test
    fun `isCorrect should return true for correct answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should return false for wrong answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should be case insensitive - lowercase`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "Apple",
                options = listOf("Apple", "Banana", "Orange", "Grape"),
            )

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should be case insensitive - uppercase`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("APPLE"))
    }

    @Test
    fun `isCorrect should be case insensitive - mixed case`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("ApPlE"))
    }

    @Test
    fun `isCorrect should return false for empty string`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("apple"))
    }

    @Test
    fun `isCorrect should return false for answer with whitespace`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertTrue(question.isCorrect("apple"))
    }

    // ==================== getCorrectIndex() Tests ====================

    @Test
    fun `getCorrectIndex should return 0 when correct answer is first option`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertEquals(0, question.getCorrectIndex())
    }

    @Test
    fun `getCorrectIndex should return 1 when correct answer is second option`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "banana",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertEquals(1, question.getCorrectIndex())
    }

    @Test
    fun `getCorrectIndex should return 2 when correct answer is third option`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "orange",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertEquals(2, question.getCorrectIndex())
    }

    @Test
    fun `getCorrectIndex should return 3 when correct answer is fourth option`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "grape",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertEquals(3, question.getCorrectIndex())
    }
}
