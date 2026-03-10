package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SpellBattleQuestion
 */
class SpellBattleQuestionTest {
    @Test
    fun `isCorrect returns true for exact match`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
                difficulty = 1,
            )

        // When
        val result = question.isCorrect("apple")

        // Then
        assertTrue(result)
    }

    @Test
    fun `isCorrect is case insensitive`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "Apple",
                hint = null,
            )

        // Then
        assertTrue(question.isCorrect("apple"))
        assertTrue(question.isCorrect("APPLE"))
        assertTrue(question.isCorrect("Apple"))
        assertTrue(question.isCorrect("aPpLe"))
    }

    @Test
    fun `isCorrect handles whitespace trimming`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // Then
        assertTrue(question.isCorrect(" apple "))
        assertTrue(question.isCorrect("  apple  "))
    }

    @Test
    fun `isCorrect returns false for wrong answer`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // Then
        assertFalse(question.isCorrect("banana"))
        assertFalse(question.isCorrect("apples"))
        assertFalse(question.isCorrect("appl"))
    }

    @Test
    fun `getHintLetter returns first letter`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val hint = question.getHintLetter()

        // Then
        assertEquals("A", hint)
    }

    @Test
    fun `getHintLetter returns null for empty word`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "",
                hint = null,
            )

        // When
        val hint = question.getHintLetter()

        // Then
        assertNull(hint)
    }

    @Test
    fun `getWrongPositions returns empty list for correct answer`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val wrongPositions = question.getWrongPositions("apple")

        // Then
        assertTrue(wrongPositions.isEmpty())
    }

    @Test
    fun `getWrongPositions detects all errors`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val wrongPositions = question.getWrongPositions("apxle")

        // Then
        assertEquals(1, wrongPositions.size)
        assertEquals(2, wrongPositions[0])
    }

    @Test
    fun `getWrongPositions handles extra characters`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "cat",
                hint = null,
            )

        // When
        val wrongPositions = question.getWrongPositions("cats")

        // Then
        assertEquals(1, wrongPositions.size)
        assertEquals(3, wrongPositions[0])
    }

    @Test
    fun `getWrongPositions handles shorter answer`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val wrongPositions = question.getWrongPositions("app")

        // Then
        assertEquals(2, wrongPositions.size)
        assertTrue(wrongPositions.contains(3))
        assertTrue(wrongPositions.contains(4))
    }

    @Test
    fun `getAnswerProgress returns correct count`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val progress = question.getAnswerProgress("appl")

        // Then
        assertEquals(4, progress.first) // 4 correct letters
        assertEquals(5, progress.second) // 5 total letters
    }

    @Test
    fun `getAnswerProgress handles wrong letters`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val progress = question.getAnswerProgress("apxle")

        // Then - a=a✓, p=p✓, x≠p✗, l=l✓, e=e✓ = 4 correct
        assertEquals(4, progress.first)
        assertEquals(5, progress.second)
    }

    @Test
    fun `getAnswerProgress handles whitespace`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // When
        val progress = question.getAnswerProgress(" apple ")

        // Then
        assertEquals(5, progress.first)
        assertEquals(5, progress.second)
    }

    @Test
    fun `SpellBattleQuestion creates with correct default difficulty`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = null,
            )

        // Then
        assertEquals(1, question.difficulty)
    }

    @Test
    fun `SpellBattleQuestion creates with custom difficulty`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "apple",
                hint = "a",
                difficulty = 5,
            )

        // Then
        assertEquals(5, question.difficulty)
        assertEquals("a", question.hint)
    }

    @Test
    fun `getWrongPositions is case insensitive`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "Apple",
                hint = null,
            )

        // When
        val wrongPositions = question.getWrongPositions("APPLE")

        // Then
        assertTrue(wrongPositions.isEmpty())
    }

    @Test
    fun `getAnswerProgress is case insensitive`() {
        // Given
        val question =
            SpellBattleQuestion(
                wordId = "word1",
                translation = "苹果",
                targetWord = "Apple",
                hint = null,
            )

        // When
        val progress = question.getAnswerProgress("APPLE")

        // Then
        assertEquals(5, progress.first)
    }
}
