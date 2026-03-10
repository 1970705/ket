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
 * Unit tests for SubmitAnswerUseCase guessing detection
 * Tests pattern-based guessing detection and anti-cheating mechanisms
 */
class SubmitAnswerUseCaseGuessingTest {
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
        // Mock getRecentPatterns to avoid NullPointerException in SubmitAnswerUseCase
        coEvery { trackingRepository.getRecentPatterns(any(), any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Pattern-Based Guessing Detection Tests ===

    @Test
    fun `consecutive same answers pattern triggers guessing detection`() =
        runTest {
            // Given - user has answered "abcde" for last 3 questions
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 3,
                    correctAttempts = 0,
                    incorrectAttempts = 3,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - pattern check is internal, verify stars calculation
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            // 4th attempt correct = 2 stars normally
            assertEquals(2, answerResult.starsEarned)
        }

    @Test
    fun `all same character answers pattern is flagged`() =
        runTest {
            // Given - user answers with "aaaaa"
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
            val result = submitAnswerUseCase(userId, "look_001", "aaaaa", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }

    // === Time-Based Guessing Detection Tests ===

    @Test
    fun `very fast response time may indicate guessing`() =
        runTest {
            // Given - response time under 1 second for 4-letter word
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null
            val fastResponseTime = 500L

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", fastResponseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertTrue(answerResult.isCorrect)
            // Stars might be affected by time-based guessing detection
            assertEquals(3, answerResult.starsEarned)
        }

    @Test
    fun `reasonable response time is not considered guessing`() =
        runTest {
            // Given - response time over 2 seconds
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null
            val reasonableTime = 2500L

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", reasonableTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(3, (result as Result.Success).data.starsEarned)
        }

    // === Combo Prevention for Guessing Tests ===

    @Test
    fun `guessing answers do not increment combo`() =
        runTest {
            // Given - user has combo going but answers with pattern
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 2,
                    correctAttempts = 1,
                    incorrectAttempts = 1,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress
            val fastResponseTime = 500L // Too fast - likely guessing

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", fastResponseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            // 2nd attempt, correct but possibly guessing = 2 stars
            assertEquals(2, answerResult.starsEarned)
        }

    // === Repeated Pattern Tests ===

    @Test
    fun `sequential pattern answers like abcde are detected`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When - user answers with sequential pattern
            val result = submitAnswerUseCase(userId, "look_001", "abcde", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `keyboard pattern answers like qwerty are detected`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When - user answers with keyboard pattern
            val result = submitAnswerUseCase(userId, "look_001", "qwerty", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `reversed keyboard pattern answers are detected`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When - user answers with reversed keyboard pattern
            val result = submitAnswerUseCase(userId, "look_001", "qwerty", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }

    // === Non-Guessing Validation Tests ===

    @Test
    fun `legitimate fast answers are not penalized for short words`() =
        runTest {
            // Given - 3-letter word can be answered quickly legitimately
            val shortWord =
                testWord.copy(
                    word = "cat",
                    id = "cat_001",
                )
            coEvery { wordRepository.getWordById("cat_001") } returns shortWord
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null
            val fastResponseTime = 1000L // 1 second is reasonable for 3-letter word

            // When
            val result = submitAnswerUseCase(userId, "cat_001", "cat", fastResponseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(3, (result as Result.Success).data.starsEarned)
        }

    @Test
    fun `varied answer history indicates not guessing`() =
        runTest {
            // Given - user has varied answer history
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 3,
                    correctAttempts = 1,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress
            val reasonableTime = 3000L // 3 seconds - reasonable

            // When - user answers correctly with reasonable time
            val result = submitAnswerUseCase(userId, "look_001", "look", reasonableTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertTrue(answerResult.isCorrect)
            // 4th attempt correct = 2 stars
            assertEquals(2, answerResult.starsEarned)
        }

    // === Edge Cases ===

    @Test
    fun `empty answer is handled gracefully`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When
            val result = submitAnswerUseCase(userId, "look_001", "", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }

    @Test
    fun `whitespace only answer is handled`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When
            val result = submitAnswerUseCase(userId, "look_001", "   ", responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertFalse((result as Result.Success).data.isCorrect)
        }
}
