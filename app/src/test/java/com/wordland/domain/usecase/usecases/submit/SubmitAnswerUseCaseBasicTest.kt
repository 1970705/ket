package com.wordland.domain.usecase.usecases.submit

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.Result
import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SubmitAnswerUseCase basic answer submission
 * Tests correct/incorrect answers and progress updates
 */
class SubmitAnswerUseCaseBasicTest {
    private lateinit var submitAnswerUseCase: SubmitAnswerUseCase
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var trackingRepository: TrackingRepository

    // Test data
    private val testWord =
        Word(
            id = "look_001",
            word = "look",
            translation = "看",
            pronunciation = "/lʊk/",
            audioPath = null,
            partOfSpeech = "verb",
            difficulty = 1,
            frequency = 100,
            theme = "observation",
            islandId = "look_island",
            levelId = "look_level_01",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    private val userId = "user_001"
    private val levelId = "look_level_01"

    @Before
    fun setup() {
        wordRepository = mockk()
        progressRepository = mockk()
        trackingRepository = mockk()
        submitAnswerUseCase = SubmitAnswerUseCase(wordRepository, progressRepository, trackingRepository)

        // Setup default relaxed behavior
        coEvery { wordRepository.getWordById(any()) } returns testWord
        coEvery { progressRepository.getWordProgress(any(), any()) } returns null
        coEvery { progressRepository.updatePracticeResult(any(), any(), any(), any(), any()) } just Runs
        coEvery { trackingRepository.recordAnswer(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
        coEvery { trackingRepository.getRecentPatterns(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Correct Answer Tests ===

    @Test
    fun `invoke with correct answer returns success`() =
        runTest {
            // Given
            val userAnswer = "look"
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertTrue(answerResult.isCorrect)
            assertEquals("look", answerResult.word.word)
        }

    @Test
    fun `invoke with correct answer updates progress`() =
        runTest {
            // Given
            val userAnswer = "look"
            val responseTime = 3000L

            // When
            submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = responseTime,
                    newMemoryStrength = any(),
                )
            }
        }

    @Test
    fun `invoke with correct answer records tracking`() =
        runTest {
            // Given
            val userAnswer = "look"
            val responseTime = 3000L

            // When
            submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = true,
                    responseTime = responseTime,
                    difficulty = 1,
                    hintUsed = false,
                    isNewWord = true,
                )
            }
        }

    // === Incorrect Answer Tests ===

    @Test
    fun `invoke with incorrect answer returns success with isCorrect false`() =
        runTest {
            // Given
            val userAnswer = "wrong"
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertFalse(answerResult.isCorrect)
            assertEquals("look", answerResult.word.word) // word property contains the target word from database
        }

    @Test
    fun `invoke with incorrect answer updates progress with isCorrect false`() =
        runTest {
            // Given
            val userAnswer = "wrong"
            val responseTime = 3000L

            // When
            submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = false,
                    responseTime = responseTime,
                    newMemoryStrength = any(),
                )
            }
        }

    // === Case Sensitivity Tests ===

    @Test
    fun `invoke is case insensitive for correct answer`() =
        runTest {
            // Given
            val userAnswer = "LOOK"
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `invoke handles mixed case input`() =
        runTest {
            // Given
            val userAnswer = "LoOk"
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isCorrect)
        }

    // === Whitespace Handling Tests ===

    @Test
    fun `invoke trims leading whitespace from answer`() =
        runTest {
            // Given
            val userAnswer = "  look"
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `invoke trims trailing whitespace from answer`() =
        runTest {
            // Given
            val userAnswer = "look  "
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `invoke trims both leading and trailing whitespace`() =
        runTest {
            // Given
            val userAnswer = "  look  "
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isCorrect)
        }

    // === Progress State Tests ===

    @Test
    fun `invoke uses existing progress for non-new words`() =
        runTest {
            // Given
            val responseTime = 3000L
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 5,
                    correctAttempts = 3,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = any(),
                    responseTime = responseTime,
                    difficulty = 1,
                    hintUsed = false,
                    isNewWord = false, // Not a new word
                )
            }
        }

    // === Error Handling Tests ===

    @Test
    fun `invoke returns error when word not found`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("unknown") } returns null
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "unknown", "answer", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns error when repository throws exception`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById(any()) } throws RuntimeException("Database error")
            val responseTime = 3000L

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Error)
        }
}
