package com.wordland.data.seed

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LookIslandWords
 */
class LookIslandWordsTest {
    @Test
    fun `getAllWords returns non-empty list`() {
        // When
        val words = LookIslandWords.getAllWords()

        // Then
        assertNotNull(words)
        assertTrue(words.isNotEmpty())
    }

    @Test
    fun `getAllWords returns correct number of words`() {
        // When
        val words = LookIslandWords.getAllWords()

        // Then - Should have 18 words (3 levels × 6 words)
        assertTrue(words.size >= 18)
    }

    @Test
    fun `getAllWords contains words with look theme`() {
        // When
        val words = LookIslandWords.getAllWords()

        // Then
        assertTrue(words.any { it.theme == "look" })
        assertTrue(words.all { it.islandId == "look_island" })
    }

    @Test
    fun `getWordsForLevel returns 6 words for valid level`() {
        // When
        val words = LookIslandWords.getWordsForLevel(0)

        // Then
        assertEquals(6, words.size)
    }

    @Test
    fun `getWordsForLevel returns correct level words`() {
        // When
        val level0Words = LookIslandWords.getWordsForLevel(0)
        val level1Words = LookIslandWords.getWordsForLevel(1)

        // Then
        assertTrue(level0Words.all { it.levelId == "look_island_level_01" })
        assertTrue(level1Words.all { it.levelId == "look_island_level_02" })
    }

    @Test
    fun `getWordsForLevel returns empty list for invalid level`() {
        // When
        val words = LookIslandWords.getWordsForLevel(999)

        // Then
        assertTrue(words.isEmpty())
    }

    @Test
    fun `getWordById returns correct word for valid ID`() {
        // When
        val word = LookIslandWords.getWordById("look_001")

        // Then
        assertNotNull(word)
        assertEquals("look_001", word?.id)
        assertEquals("look", word?.word)
    }

    @Test
    fun `getWordById returns null for invalid ID`() {
        // When
        val word = LookIslandWords.getWordById("invalid_id")

        // Then
        assertNull(word)
    }

    @Test
    fun `getWordById finds all level 1 words`() {
        // Given
        val level1Ids = listOf("look_001", "look_002", "look_003", "look_004", "look_005", "look_006")

        // When & Then
        level1Ids.forEach { id ->
            val word = LookIslandWords.getWordById(id)
            assertNotNull(word)
            assertEquals(id, word?.id)
            assertEquals("look_island_level_01", word?.levelId)
        }
    }

    @Test
    fun `words have correct island ID`() {
        // When
        val words = LookIslandWords.getAllWords()

        // Then
        assertTrue(words.all { it.islandId == "look_island" })
    }

    @Test
    fun `words have KET level set`() {
        // When
        val words = LookIslandWords.getAllWords()

        // Then
        assertTrue(words.any { it.ketLevel })
    }

    @Test
    fun `words are ordered correctly within levels`() {
        // When
        val level0Words = LookIslandWords.getWordsForLevel(0)

        // Then
        assertEquals(1, level0Words[0].order)
        assertEquals(2, level0Words[1].order)
        assertEquals(3, level0Words[2].order)
        assertEquals(4, level0Words[3].order)
        assertEquals(5, level0Words[4].order)
        assertEquals(6, level0Words[5].order)
    }

    @Test
    fun `getWordsForLevel handles edge cases`() {
        // Test first level
        val firstLevel = LookIslandWords.getWordsForLevel(0)
        assertEquals(6, firstLevel.size)

        // Test last valid level
        val lastLevel = LookIslandWords.getWordsForLevel(2)
        assertEquals(6, lastLevel.size)

        // Test level beyond range
        val beyondRange = LookIslandWords.getWordsForLevel(100)
        assertTrue(beyondRange.isEmpty())
    }
}
