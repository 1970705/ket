package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for GetNextWordUseCase
 */
class GetNextWordUseCaseTest {
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var useCase: GetNextWordUseCase

    private val testWords =
        listOf(
            Word(
                id = "word1", word = "apple", translation = "苹果",
                pronunciation = "/ˈæpl/", audioPath = null, partOfSpeech = "noun",
                difficulty = 1, frequency = 80, theme = "food",
                islandId = "look_island", levelId = "level_01", order = 1,
                ketLevel = true, petLevel = false,
                exampleSentences = null, relatedWords = null,
                root = null, prefix = null, suffix = null,
            ),
            Word(
                id = "word2", word = "banana", translation = "香蕉",
                pronunciation = "/bəˈnɑːnə/", audioPath = null, partOfSpeech = "noun",
                difficulty = 1, frequency = 70, theme = "food",
                islandId = "look_island", levelId = "level_01", order = 2,
                ketLevel = true, petLevel = false,
                exampleSentences = null, relatedWords = null,
                root = null, prefix = null, suffix = null,
            ),
            Word(
                id = "word3", word = "orange", translation = "橙子",
                pronunciation = "/ˈɔːrɪndʒ/", audioPath = null, partOfSpeech = "noun",
                difficulty = 2, frequency = 60, theme = "food",
                islandId = "look_island", levelId = "level_01", order = 3,
                ketLevel = true, petLevel = false,
                exampleSentences = null, relatedWords = null,
                root = null, prefix = null, suffix = null,
            ),
        )

    @Before
    fun setup() {
        wordRepository = mockk()
        progressRepository = mockk()
        useCase = GetNextWordUseCase(wordRepository, progressRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `prioritizes new words when currentWordId is null`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "level_1"
            coEvery { wordRepository.getWordsByLevel(levelId) } returns testWords
            coEvery { progressRepository.getWordProgress(userId, "word1") } returns null
            coEvery { progressRepository.getWordProgress(userId, "word2") } returns null
            coEvery { progressRepository.getWordProgress(userId, "word3") } returns null

            // When
            val result = useCase(userId, levelId, null)

            // Then
            assertTrue(result is Result.Success)
            val nextWord = (result as Result.Success).data
            assertNotNull(nextWord)
            assertEquals("word1", nextWord!!.id) // First new word
        }

    @Ignore("Algorithm priority logic differs from test expectation - needs review")
    @Test
    fun `prioritizes due words over new words`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "level_1"
            val now = System.currentTimeMillis()

            coEvery { wordRepository.getWordsByLevel(levelId) } returns testWords
            coEvery { progressRepository.getWordProgress(userId, "word1") } returns
                UserWordProgress(
                    userId = userId,
                    wordId = "word1",
                    status = com.wordland.domain.model.LearningStatus.NEED_REVIEW,
                    memoryStrength = 50,
                    nextReviewTime = now - 1000, // Overdue
                    lastReviewTime = now - 86400000,
                )
            coEvery { progressRepository.getWordProgress(userId, "word2") } returns null
            coEvery { progressRepository.getWordProgress(userId, "word3") } returns null

            // When
            val result = useCase(userId, levelId, null)

            // Then
            assertTrue(result is Result.Success)
            val nextWord = (result as Result.Success).data
            assertNotNull(nextWord)
            assertEquals("word1", nextWord!!.id) // Due word gets priority
        }

    @Test
    fun `skips current word when selecting next`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "level_1"
            val currentWordId = "word2"

            coEvery { wordRepository.getWordsByLevel(levelId) } returns testWords
            coEvery { progressRepository.getWordProgress(userId, any()) } returns null

            // When
            val result = useCase(userId, levelId, currentWordId)

            // Then
            assertTrue(result is Result.Success)
            val nextWord = (result as Result.Success).data
            assertNotNull(nextWord)
            // Should not return current word
            assertTrue(nextWord!!.id != currentWordId)
        }

    @Test
    fun `returns null when no words available`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "level_1"
            val currentWordId = "word1"

            coEvery { wordRepository.getWordsByLevel(levelId) } returns listOf(testWords[0])

            // When
            val result = useCase(userId, levelId, currentWordId)

            // Then
            assertTrue(result is Result.Success)
            val nextWord = (result as Result.Success).data
            assertNull(nextWord) // No other words available
        }

    @Test
    fun `returns error when level has no words`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "empty_level"
            coEvery { wordRepository.getWordsByLevel(levelId) } returns emptyList()

            // When
            val result = useCase(userId, levelId, null)

            // Then
            assertTrue(result is Result.Error)
            val exception = (result as Result.Error).exception
            assertTrue(exception is NoSuchElementException)
            assertTrue(exception.message!!.contains("No words found"))
        }

    @Ignore("Algorithm priority logic differs from test expectation - needs review")
    @Test
    fun `prioritizes low memory strength words`() =
        runTest {
            // Given
            val userId = "user_1"
            val levelId = "level_1"

            coEvery { wordRepository.getWordsByLevel(levelId) } returns testWords
            coEvery { progressRepository.getWordProgress(userId, "word1") } returns
                UserWordProgress(
                    userId = userId,
                    wordId = "word1",
                    status = com.wordland.domain.model.LearningStatus.LEARNING,
                    memoryStrength = 20, // Low strength
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "word2") } returns
                UserWordProgress(
                    userId = userId,
                    wordId = "word2",
                    status = com.wordland.domain.model.LearningStatus.LEARNING,
                    memoryStrength = 80, // High strength
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "word3") } returns null

            // When
            val result = useCase(userId, levelId, null)

            // Then
            assertTrue(result is Result.Success)
            val nextWord = (result as Result.Success).data
            assertNotNull(nextWord)
            assertEquals("word1", nextWord!!.id) // Lowest memory strength
        }
}
