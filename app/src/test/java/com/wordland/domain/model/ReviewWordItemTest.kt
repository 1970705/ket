package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ReviewWordItem
 */
class ReviewWordItemTest {
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
    fun `ReviewWordItem creates with correct values`() {
        // Given
        val item =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 60,
                lastReviewTime = System.currentTimeMillis(),
                correctRate = 0.8f,
            )

        // Then
        assertEquals(testWord, item.word)
        assertEquals(60, item.memoryStrength)
        assertEquals(0.8f, item.correctRate, 0.01f)
        assertNotNull(item.lastReviewTime)
    }

    @Test
    fun `ReviewWordItem handles null lastReviewTime`() {
        // Given
        val item =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 30,
                lastReviewTime = null,
                correctRate = 0.5f,
            )

        // Then
        assertNull(item.lastReviewTime)
        assertEquals(0.5f, item.correctRate, 0.01f)
    }

    @Test
    fun `ReviewWordItem handles zero correctRate`() {
        // Given
        val item =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 10,
                lastReviewTime = null,
                correctRate = 0.0f,
            )

        // Then
        assertEquals(0.0f, item.correctRate, 0.01f)
        assertEquals(10, item.memoryStrength)
    }

    @Test
    fun `ReviewWordItem handles perfect correctRate`() {
        // Given
        val item =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 100,
                lastReviewTime = System.currentTimeMillis(),
                correctRate = 1.0f,
            )

        // Then
        assertEquals(1.0f, item.correctRate, 0.01f)
        assertEquals(100, item.memoryStrength)
    }

    @Test
    fun `ReviewWordItem handles edge memoryStrength values`() {
        // Test minimum
        val item1 =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 0,
                lastReviewTime = null,
                correctRate = 0.0f,
            )
        assertEquals(0, item1.memoryStrength)

        // Test maximum
        val item2 =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 100,
                lastReviewTime = null,
                correctRate = 1.0f,
            )
        assertEquals(100, item2.memoryStrength)
    }

    @Test
    fun `ReviewWordItem preserves word data`() {
        // Given
        val item =
            ReviewWordItem(
                word = testWord,
                memoryStrength = 50,
                lastReviewTime = 123456789L,
                correctRate = 0.7f,
            )

        // Then
        assertEquals("word1", item.word.id)
        assertEquals("apple", item.word.word)
        assertEquals("苹果", item.word.translation)
        assertEquals("/ˈæpl/", item.word.pronunciation)
    }
}
