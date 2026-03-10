package com.wordland.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for MultipleChoiceQuestion feedback and edge cases
 *
 * Tests cover getAnswerFeedback(), edge cases, and
 * AnswerFeedback data class properties.
 */
class MultipleChoiceQuestionFeedbackTest {
    // ==================== getAnswerFeedback() Tests ====================

    @Test
    fun `getAnswerFeedback should return correct feedback for correct answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("apple")

        assertTrue(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertEquals(0, feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should return incorrect feedback for wrong answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("banana")

        assertFalse(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertEquals(1, feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should handle answer not in options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("mango")

        assertFalse(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertNull(feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should handle empty answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("")

        assertFalse(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertNull(feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should be case insensitive for correct answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("APPLE")

        assertTrue(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertEquals(0, feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should be case insensitive for wrong answer`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        val feedback = question.getAnswerFeedback("BANANA")

        assertFalse(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertEquals(1, feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }

    @Test
    fun `getAnswerFeedback should work when correct answer is at different positions`() {
        // Correct answer at index 1
        val question1 =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "banana",
                options = listOf("apple", "banana", "orange", "grape"),
            )
        val feedback1 = question1.getAnswerFeedback("banana")
        assertTrue(feedback1.isCorrect)
        assertEquals(1, feedback1.correctIndex)
        assertEquals(1, feedback1.selectedIndex)

        // Correct answer at index 2
        val question2 =
            MultipleChoiceQuestion(
                wordId = "word_002",
                translation = "香蕉",
                targetWord = "orange",
                options = listOf("apple", "banana", "orange", "grape"),
            )
        val feedback2 = question2.getAnswerFeedback("orange")
        assertTrue(feedback2.isCorrect)
        assertEquals(2, feedback2.correctIndex)
        assertEquals(2, feedback2.selectedIndex)

        // Correct answer at index 3
        val question3 =
            MultipleChoiceQuestion(
                wordId = "word_003",
                translation = "橙子",
                targetWord = "grape",
                options = listOf("apple", "banana", "orange", "grape"),
            )
        val feedback3 = question3.getAnswerFeedback("grape")
        assertTrue(feedback3.isCorrect)
        assertEquals(3, feedback3.correctIndex)
        assertEquals(3, feedback3.selectedIndex)
    }

    // ==================== Edge Cases Tests ====================

    @Test
    fun `should handle question with special characters in options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "不能",
                targetWord = "can't",
                options = listOf("can't", "cant", "cannot", "can not"),
            )

        assertTrue(question.isCorrect("can't"))
        assertEquals(0, question.getCorrectIndex())
    }

    @Test
    fun `should handle question with numbers in options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "一个",
                targetWord = "one",
                options = listOf("one", "two", "three", "four"),
            )

        assertTrue(question.isCorrect("one"))
    }

    @Test
    fun `should handle single letter options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "A",
                targetWord = "a",
                options = listOf("a", "b", "c", "d"),
            )

        assertTrue(question.isCorrect("a"))
        assertTrue(question.isCorrect("A"))
    }

    @Test
    fun `should handle long word options`() {
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "多才多艺",
                targetWord = "accomplishment",
                options = listOf("accomplishment", "achievement", "development", "improvement"),
            )

        assertTrue(question.isCorrect("accomplishment"))
    }

    @Test
    fun `constructor should accept question with duplicate options (currently allowed)`() {
        // Note: The current implementation doesn't validate for duplicate options
        // This test documents the current behavior
        val question =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "apple", "banana", "orange"),
            )

        // Currently accepts duplicates - could be improved in future
        assertEquals(4, question.options.size)
    }

    @Test
    fun `should handle all valid difficulty levels`() {
        for (difficulty in 1..5) {
            val question =
                MultipleChoiceQuestion(
                    wordId = "word_001",
                    translation = "苹果",
                    targetWord = "apple",
                    options = listOf("apple", "banana", "orange", "grape"),
                    difficulty = difficulty,
                )
            assertEquals(difficulty, question.difficulty)
        }
    }

    @Test
    fun `should handle emoji parameter correctly`() {
        val questionWithEmoji =
            MultipleChoiceQuestion(
                wordId = "word_001",
                translation = "苹果",
                targetWord = "apple",
                options = listOf("apple", "banana", "orange", "grape"),
                emoji = "",
            )

        assertEquals("", questionWithEmoji.emoji)

        val questionWithoutEmoji =
            MultipleChoiceQuestion(
                wordId = "word_002",
                translation = "香蕉",
                targetWord = "banana",
                options = listOf("apple", "banana", "orange", "grape"),
            )

        assertNull(questionWithoutEmoji.emoji)
    }

    // ==================== AnswerFeedback Data Class Tests ====================

    @Test
    fun `AnswerFeedback should have correct properties`() {
        val feedback =
            AnswerFeedback(
                isCorrect = true,
                correctIndex = 2,
                selectedIndex = 2,
                correctAnswer = "orange",
            )

        assertTrue(feedback.isCorrect)
        assertEquals(2, feedback.correctIndex)
        assertEquals(2, feedback.selectedIndex)
        assertEquals("orange", feedback.correctAnswer)
    }

    @Test
    fun `AnswerFeedback should handle null selected index`() {
        val feedback =
            AnswerFeedback(
                isCorrect = false,
                correctIndex = 0,
                selectedIndex = null,
                correctAnswer = "apple",
            )

        assertFalse(feedback.isCorrect)
        assertEquals(0, feedback.correctIndex)
        assertNull(feedback.selectedIndex)
        assertEquals("apple", feedback.correctAnswer)
    }
}
