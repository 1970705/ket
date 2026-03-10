package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LearnWordResult
 */
class LearnWordResultTest {
    private val testWord =
        Word(
            id = "word1",
            word = "apple",
            translation = "苹果",
            pronunciation = "/ˈæpl/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 1,
            frequency = 80,
            theme = "food",
            islandId = "look_island",
            levelId = "look_island_level_01",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    @Test
    fun `LearnWordResult creates with correct values for correct answer`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确！",
                newMemoryStrength = 70,
                timeTaken = 3000L,
                hintUsed = false,
            )

        // Then
        assertEquals(testWord, result.word)
        assertTrue(result.isCorrect)
        assertEquals("正确！", result.message)
        assertEquals(70, result.newMemoryStrength)
        assertEquals(3000L, result.timeTaken)
        assertFalse(result.hintUsed)
    }

    @Test
    fun `LearnWordResult creates with correct values for incorrect answer`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = false,
                message = "错误！答案是 apple",
                newMemoryStrength = 20,
                timeTaken = 5000L,
                hintUsed = true,
            )

        // Then
        assertFalse(result.isCorrect)
        assertEquals("错误！答案是 apple", result.message)
        assertEquals(20, result.newMemoryStrength)
        assertEquals(5000L, result.timeTaken)
        assertTrue(result.hintUsed)
    }

    @Test
    fun `LearnWordResult handles zero timeTaken`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确！",
                newMemoryStrength = 60,
                timeTaken = 0L,
                hintUsed = false,
            )

        // Then
        assertEquals(0L, result.timeTaken)
    }

    @Test
    fun `LearnWordResult handles very fast response`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确！",
                newMemoryStrength = 80,
                timeTaken = 500L,
                hintUsed = false,
            )

        // Then
        assertEquals(500L, result.timeTaken)
    }

    @Test
    fun `LearnWordResult handles slow response`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = false,
                message = "太慢了",
                newMemoryStrength = 10,
                timeTaken = 30000L,
                hintUsed = false,
            )

        // Then
        assertEquals(30000L, result.timeTaken)
    }

    @Test
    fun `LearnWordResult preserves word information`() {
        // Given
        val result =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确！",
                newMemoryStrength = 75,
                timeTaken = 2000L,
                hintUsed = false,
            )

        // Then
        assertEquals("apple", result.word.word)
        assertEquals("苹果", result.word.translation)
        assertEquals("noun", result.word.partOfSpeech)
    }

    @Test
    fun `LearnWordResult handles edge memoryStrength values`() {
        // Test minimum
        val result1 =
            LearnWordResult(
                word = testWord,
                isCorrect = false,
                message = "错误",
                newMemoryStrength = 0,
                timeTaken = 1000L,
                hintUsed = true,
            )
        assertEquals(0, result1.newMemoryStrength)

        // Test maximum
        val result2 =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确",
                newMemoryStrength = 100,
                timeTaken = 1000L,
                hintUsed = false,
            )
        assertEquals(100, result2.newMemoryStrength)
    }

    @Test
    fun `LearnWordResult tracks hint usage correctly`() {
        // Given - with hint
        val resultWithHint =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确（用了提示）",
                newMemoryStrength = 50,
                timeTaken = 5000L,
                hintUsed = true,
            )

        // Given - without hint
        val resultWithoutHint =
            LearnWordResult(
                word = testWord,
                isCorrect = true,
                message = "正确",
                newMemoryStrength = 70,
                timeTaken = 3000L,
                hintUsed = false,
            )

        // Then
        assertTrue(resultWithHint.hintUsed)
        assertFalse(resultWithoutHint.hintUsed)
    }
}
