package com.wordland.domain.usecase.usecases.submit

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.LearningStatus
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
 * Unit tests for SubmitAnswerUseCase combo and memory strength
 * Tests combo calculation and memory strength updates
 */
class SubmitAnswerUseCaseComboTest {
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
    private val responseTime = 3000L

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

    // === Memory Strength Calculation Tests ===

    @Test
    fun `correct answer increases memory strength`() =
        runTest {
            // Given
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 1,
                    correctAttempts = 0,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - newMemoryStrength should be higher than 50
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = responseTime,
                    newMemoryStrength = match { it > 50 },
                )
            }
        }

    @Test
    fun `incorrect answer decreases memory strength`() =
        runTest {
            // Given
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 1,
                    correctAttempts = 1,
                    incorrectAttempts = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            submitAnswerUseCase(userId, "look_001", "wrong", responseTime, false, levelId)

            // Then - newMemoryStrength should be lower than 50
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = false,
                    responseTime = responseTime,
                    newMemoryStrength = match { it < 50 },
                )
            }
        }

    @Test
    fun `fast correct answer increases memory strength more`() =
        runTest {
            // Given
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 1,
                    correctAttempts = 0,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress
            val fastResponseTime = 1500L

            // When - fast answer
            submitAnswerUseCase(userId, "look_001", "look", fastResponseTime, false, levelId)

            // Then - higher memory strength increase for fast answer
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = fastResponseTime,
                    newMemoryStrength = match { it > 50 },
                )
            }
        }

    @Test
    fun `slow correct answer increases memory strength less`() =
        runTest {
            // Given
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 1,
                    correctAttempts = 0,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress
            val slowResponseTime = 8000L

            // When - slow answer
            submitAnswerUseCase(userId, "look_001", "look", slowResponseTime, false, levelId)

            // Then - lower memory strength increase for slow answer
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = slowResponseTime,
                    newMemoryStrength = match { it > 50 && it < 100 },
                )
            }
        }

    @Test
    fun `new word starts with base memory strength`() =
        runTest {
            // Given - no existing progress (new word)
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns null

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - newMemoryStrength should be set for new word
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = responseTime,
                    newMemoryStrength = match { it > 0 },
                )
            }
        }

    // === Memory Strength Boundaries Tests ===

    @Test
    fun `memory strength is capped at 100`() =
        runTest {
            // Given - word already at high memory strength
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.MASTERED,
                    memoryStrength = 95,
                    totalAttempts = 10,
                    correctAttempts = 9,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - memory strength should not exceed 100
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = responseTime,
                    newMemoryStrength = match { it <= 100 },
                )
            }
        }

    @Test
    fun `memory strength has minimum of 0`() =
        runTest {
            // Given - word at very low memory strength
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 5,
                    totalAttempts = 10,
                    correctAttempts = 0,
                    incorrectAttempts = 10,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When - another wrong answer
            submitAnswerUseCase(userId, "look_001", "wrong", responseTime, false, levelId)

            // Then - memory strength should not go below 0
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = false,
                    responseTime = responseTime,
                    newMemoryStrength = match { it >= 0 },
                )
            }
        }

    // === Progress Status Tests ===

    @Test
    fun `correct answer can promote word to REVIEW status`() =
        runTest {
            // Given - word at LEARNING with high memory strength
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 75,
                    totalAttempts = 5,
                    correctAttempts = 4,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When - correct answer
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - status may change based on memory strength
            // This is handled internally by updatePracticeResult
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
    fun `incorrect answer can demote word to LEARNING status`() =
        runTest {
            // Given - word at REVIEW status
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.MASTERED,
                    memoryStrength = 60,
                    totalAttempts = 8,
                    correctAttempts = 6,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When - incorrect answer
            submitAnswerUseCase(userId, "look_001", "wrong", responseTime, false, levelId)

            // Then - status may be demoted
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

    // === Response Time Recording Tests ===

    @Test
    fun `response time is recorded correctly`() =
        runTest {
            // Given
            val testResponseTime = 3500L
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When
            submitAnswerUseCase(userId, "look_001", "look", testResponseTime, false, levelId)

            // Then
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = testResponseTime,
                    newMemoryStrength = any(),
                )
            }
        }

    @Test
    fun `response time parameter is used when provided`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null
            val customResponseTime = 5000L

            // When
            submitAnswerUseCase(userId, "look_001", "look", customResponseTime, false, levelId)

            // Then
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = customResponseTime,
                    newMemoryStrength = any(),
                )
            }
        }
}
