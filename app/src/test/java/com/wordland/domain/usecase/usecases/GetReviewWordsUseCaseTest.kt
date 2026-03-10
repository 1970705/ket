package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.Result
import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetReviewWordsUseCase
 * Tests getting words that need review based on memory strength and due time
 */
class GetReviewWordsUseCaseTest {
    private lateinit var getReviewWordsUseCase: GetReviewWordsUseCase
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository

    private val testUserId = "test_user"

    private val testWord1 =
        Word(
            id = "word1",
            word = "apple",
            translation = "苹果",
            pronunciation = "/ˈæpl/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 1,
            frequency = 100,
            theme = "fruit",
            islandId = "test_island",
            levelId = "test_level",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    private val testWord2 =
        Word(
            id = "word2",
            word = "banana",
            translation = "香蕉",
            pronunciation = "/bəˈnɑːnə/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 2,
            frequency = 80,
            theme = "fruit",
            islandId = "test_island",
            levelId = "test_level",
            order = 2,
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
        wordRepository = mockk(relaxed = true)
        progressRepository = mockk(relaxed = true)

        getReviewWordsUseCase =
            GetReviewWordsUseCase(
                wordRepository,
                progressRepository,
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Success Cases ===

    @Test
    fun `invoke returns empty list when no words need review`() =
        runTest {
            coEvery { progressRepository.getWordsDueForReview(any(), any(), any()) } returns emptyList()
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isEmpty())
        }

    @Test
    fun `invoke returns words due for review`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 10,
                    correctAttempts = 7,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = System.currentTimeMillis() - 10000,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns listOf(progress1)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()
            coEvery { wordRepository.getWordById("word1") } returns testWord1

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(1, words.size)
            assertEquals("apple", words[0].word.word)
            assertEquals(50, words[0].memoryStrength)
        }

    @Test
    fun `invoke returns low strength words when none due for review`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 40,
                    totalAttempts = 5,
                    correctAttempts = 3,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = System.currentTimeMillis() + 10000, // Future
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns emptyList()
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns listOf(progress1)
            coEvery { wordRepository.getWordById("word1") } returns testWord1

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(1, words.size)
            assertEquals(40, words[0].memoryStrength)
        }

    @Test
    fun `invoke combines due and low strength words without duplicates`() =
        runTest {
            val now = System.currentTimeMillis()
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 30,
                    totalAttempts = 5,
                    correctAttempts = 3,
                    incorrectAttempts = 2,
                    createdAt = now - 100000,
                    updatedAt = now - 5000,
                    lastReviewTime = now - 10000,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns listOf(progress1)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns listOf(progress1)
            coEvery { wordRepository.getWordById("word1") } returns testWord1

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(1, words.size) // Not duplicated
        }

    @Test
    fun `invoke respects limit parameter`() =
        runTest {
            val progressList =
                (1..25).map { i ->
                    UserWordProgress(
                        wordId = "word$i",
                        userId = testUserId,
                        status = LearningStatus.LEARNING,
                        memoryStrength = 30,
                        totalAttempts = 5,
                        correctAttempts = 3,
                        incorrectAttempts = 2,
                        createdAt = System.currentTimeMillis() - 100000,
                        updatedAt = System.currentTimeMillis() - 5000,
                        lastReviewTime = System.currentTimeMillis() - 10000,
                    )
                }

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns progressList.take(20)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns progressList
            progressList.forEach {
                coEvery { wordRepository.getWordById(it.wordId) } returns testWord1.copy(id = it.wordId, word = "word$it")
            }

            val result = getReviewWordsUseCase(testUserId, 10)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(10, words.size)
        }

    @Test
    fun `invoke calculates correct rate correctly`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 10,
                    correctAttempts = 7,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = System.currentTimeMillis() - 10000,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns listOf(progress1)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()
            coEvery { wordRepository.getWordById("word1") } returns testWord1

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(0.7f, words[0].correctRate, 0.01f)
        }

    @Test
    fun `invoke returns zero correct rate for new words`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.NEW,
                    memoryStrength = 0,
                    totalAttempts = 0,
                    correctAttempts = 0,
                    incorrectAttempts = 0,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = null,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns listOf(progress1)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()
            coEvery { wordRepository.getWordById("word1") } returns testWord1

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(0f, words[0].correctRate, 0.01f)
        }

    @Test
    fun `invoke sorts by memory strength and correct rate`() =
        runTest {
            val now = System.currentTimeMillis()
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 30,
                    totalAttempts = 10,
                    correctAttempts = 8,
                    incorrectAttempts = 2,
                    createdAt = now - 100000,
                    updatedAt = now - 5000,
                    lastReviewTime = now - 10000,
                )
            val progress2 =
                UserWordProgress(
                    wordId = "word2",
                    userId = testUserId,
                    status = LearningStatus.LEARNING,
                    memoryStrength = 30,
                    totalAttempts = 10,
                    correctAttempts = 5,
                    incorrectAttempts = 5,
                    createdAt = now - 100000,
                    updatedAt = now - 5000,
                    lastReviewTime = now - 10000,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns listOf(progress1, progress2)
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()
            coEvery { wordRepository.getWordById("word1") } returns testWord1
            coEvery { wordRepository.getWordById("word2") } returns testWord2

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            val words = (result as Result.Success).data
            assertEquals(2, words.size)
            assertEquals("apple", words[0].word.word) // Higher correct rate first
            assertEquals("banana", words[1].word.word)
        }

    // === Error Cases ===

    @Test
    fun `invoke returns Error when exception occurs`() =
        runTest {
            coEvery { progressRepository.getWordsDueForReview(any(), any(), any()) } throws RuntimeException("Database error")

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Error)
        }

    // === Edge Cases ===

    @Test
    fun `invoke filters out zero and max memory strength words from low strength list`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.MASTERED,
                    memoryStrength = 100,
                    totalAttempts = 10,
                    correctAttempts = 10,
                    incorrectAttempts = 0,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = System.currentTimeMillis() - 10000,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns emptyList()
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns listOf(progress1)

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isEmpty())
        }

    @Test
    fun `invoke handles default limit parameter`() =
        runTest {
            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns emptyList()
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns emptyList()

            val result = getReviewWordsUseCase(testUserId)

            assertTrue(result is Result.Success)
        }

    @Test
    fun `invoke filters out words with zero attempts from low strength list`() =
        runTest {
            val progress1 =
                UserWordProgress(
                    wordId = "word1",
                    userId = testUserId,
                    status = LearningStatus.NEW,
                    memoryStrength = 0,
                    totalAttempts = 0,
                    correctAttempts = 0,
                    incorrectAttempts = 0,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 5000,
                    lastReviewTime = null,
                )

            coEvery { progressRepository.getWordsDueForReview(testUserId, any(), 20) } returns emptyList()
            coEvery { progressRepository.getAllWordProgress(testUserId) } returns listOf(progress1)

            val result = getReviewWordsUseCase(testUserId, 20)

            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isEmpty())
        }
}
