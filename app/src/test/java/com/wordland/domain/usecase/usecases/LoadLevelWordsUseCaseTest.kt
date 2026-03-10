package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadLevelWordsUseCase
 */
class LoadLevelWordsUseCaseTest {
    private lateinit var wordRepository: WordRepository
    private lateinit var useCase: LoadLevelWordsUseCase

    // Helper function to create test words
    private fun createTestWord(
        id: String,
        word: String,
        translation: String,
        order: Int,
    ) = Word(
        id = id,
        word = word,
        translation = translation,
        pronunciation = "/test/",
        audioPath = null,
        partOfSpeech = "noun",
        difficulty = 1,
        frequency = 80,
        theme = "test",
        islandId = "test_island",
        levelId = "test_level",
        order = order,
        ketLevel = true,
        petLevel = false,
        exampleSentences = null,
        relatedWords = null,
        root = null,
        prefix = null,
        suffix = null,
    )

    @Before
    fun setup() {
        wordRepository = mockk()
        useCase = LoadLevelWordsUseCase(wordRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke returns sorted words when level has words`() =
        runTest {
            // Given
            val levelId = "make_lake_level_1"
            val words =
                listOf(
                    createTestWord("word3", "zebra", "斑马", 3),
                    createTestWord("word1", "apple", "苹果", 1),
                    createTestWord("word2", "banana", "香蕉", 2),
                )
            coEvery { wordRepository.getWordsByLevel(levelId) } returns words

            // When
            val result = useCase(levelId)

            // Then
            assertTrue(result is Result.Success)
            val sortedWords = (result as Result.Success).data
            assertEquals(3, sortedWords.size)
            assertEquals("word1", sortedWords[0].id) // Sorted by order
            assertEquals("word2", sortedWords[1].id)
            assertEquals("word3", sortedWords[2].id)
        }

    @Test
    fun `invoke returns error when level has no words`() =
        runTest {
            // Given
            val levelId = "empty_level"
            coEvery { wordRepository.getWordsByLevel(levelId) } returns emptyList()

            // When
            val result = useCase(levelId)

            // Then
            assertTrue(result is Result.Error)
            val exception = (result as Result.Error).exception
            assertTrue(exception is NoSuchElementException)
            assertTrue(exception.message!!.contains("No words found"))
        }

    @Test
    fun `invoke returns error when repository throws exception`() =
        runTest {
            // Given
            val levelId = "error_level"
            val exception = RuntimeException("Database error")
            coEvery { wordRepository.getWordsByLevel(levelId) } throws exception

            // When
            val result = useCase(levelId)

            // Then
            assertTrue(result is Result.Error)
            assertEquals(exception, (result as Result.Error).exception)
        }

    @Test
    fun `invoke handles single word correctly`() =
        runTest {
            // Given
            val levelId = "single_word_level"
            val words =
                listOf(
                    createTestWord("word1", "apple", "苹果", 1),
                )
            coEvery { wordRepository.getWordsByLevel(levelId) } returns words

            // When
            val result = useCase(levelId)

            // Then
            assertTrue(result is Result.Success)
            val sortedWords = (result as Result.Success).data
            assertEquals(1, sortedWords.size)
            assertEquals("word1", sortedWords[0].id)
        }
}
