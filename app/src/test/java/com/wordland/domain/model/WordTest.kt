package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Word model
 */
class WordTest {
    @Test
    fun `Word creates with all required fields`() {
        // Given
        val word =
            Word(
                id = "word1",
                word = "apple",
                translation = "苹果",
                pronunciation = "/ˈæpl/",
                audioPath = "assets/audio/apple.mp3",
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 80,
                theme = "food",
                islandId = "look_island",
                levelId = "look_island_level_01",
                order = 1,
                ketLevel = true,
                petLevel = false,
                exampleSentences = "[{\"sentence\":\"I eat an apple.\",\"translation\":\"我吃一个苹果。\"}]",
                relatedWords = "[\"fruit\", \"banana\"]",
                root = null,
                prefix = null,
                suffix = null,
            )

        // Then
        assertEquals("word1", word.id)
        assertEquals("apple", word.word)
        assertEquals("苹果", word.translation)
        assertEquals("/ˈæpl/", word.pronunciation)
        assertEquals("assets/audio/apple.mp3", word.audioPath)
        assertEquals("noun", word.partOfSpeech)
        assertEquals(1, word.difficulty)
        assertEquals(80, word.frequency)
        assertEquals("food", word.theme)
        assertEquals("look_island", word.islandId)
        assertEquals("look_island_level_01", word.levelId)
        assertEquals(1, word.order)
        assertTrue(word.ketLevel)
        assertFalse(word.petLevel)
    }

    @Test
    fun `Word creates with optional fields null`() {
        // Given
        val word =
            Word(
                id = "word2",
                word = "banana",
                translation = "香蕉",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = null,
                difficulty = 2,
                frequency = 60,
                theme = "food",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = false,
                petLevel = true,
                exampleSentences = null,
                relatedWords = null,
                root = "ban",
                prefix = null,
                suffix = "-ana",
            )

        // Then
        assertNull(word.pronunciation)
        assertNull(word.audioPath)
        assertNull(word.partOfSpeech)
        assertNull(word.islandId)
        assertNull(word.levelId)
        assertEquals(0, word.order)
        assertFalse(word.ketLevel)
        assertTrue(word.petLevel)
        assertEquals("ban", word.root)
        assertEquals("-ana", word.suffix)
    }

    @Test
    fun `belongsToTheme returns true for matching theme`() {
        // Given
        val word =
            Word(
                id = "word1",
                word = "watch",
                translation = "观看",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = "verb",
                difficulty = 1,
                frequency = 70,
                theme = "look",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            )

        // When & Then
        assertTrue(word.belongsToTheme("look"))
        assertFalse(word.belongsToTheme("move"))
        assertFalse(word.belongsToTheme("say"))
    }

    @Test
    fun `Word handles edge difficulty values`() {
        // Test minimum difficulty
        val word1 =
            Word(
                id = "word1",
                word = "easy",
                translation = "简单",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = null,
                difficulty = 1,
                frequency = 100,
                theme = "test",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            )
        assertEquals(1, word1.difficulty)

        // Test maximum difficulty
        val word2 =
            Word(
                id = "word2",
                word = "hard",
                translation = "困难",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = null,
                difficulty = 5,
                frequency = 10,
                theme = "test",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = false,
                petLevel = true,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            )
        assertEquals(5, word2.difficulty)
    }

    @Test
    fun `Word handles both KET and PET levels`() {
        // Given
        val word =
            Word(
                id = "word1",
                word = "test",
                translation = "测试",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = null,
                difficulty = 3,
                frequency = 50,
                theme = "test",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = true,
                petLevel = true,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            )

        // Then
        assertTrue(word.ketLevel)
        assertTrue(word.petLevel)
    }

    @Test
    fun `Word preserves word parts`() {
        // Given
        val word =
            Word(
                id = "word1",
                word = "unbelievable",
                translation = "难以置信",
                pronunciation = null,
                audioPath = null,
                partOfSpeech = "adjective",
                difficulty = 5,
                frequency = 30,
                theme = "test",
                islandId = null,
                levelId = null,
                order = 0,
                ketLevel = false,
                petLevel = true,
                exampleSentences = null,
                relatedWords = null,
                root = "believe",
                prefix = "un-",
                suffix = "-able",
            )

        // Then
        assertEquals("believe", word.root)
        assertEquals("un-", word.prefix)
        assertEquals("-able", word.suffix)
    }
}
