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
 * Unit tests for SubmitAnswerUseCase star rating calculation
 *
 * **Child-Friendly Design** (Epic #5):
 * - 0 stars: Incorrect answer
 * - 1 star: Correct but detected as guessing
 * - 2 stars: Correct with hint OR too fast (potential guessing)
 * - 3 stars: Correct, no hint, adequate thinking time (not guessing)
 *
 * **Important**: Per-word rating does NOT consider attempt count.
 * This is intentional child-friendly design - every correct answer
 * earns full stars if given with adequate thinking time.
 */
class SubmitAnswerUseCaseStarRatingTest {
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

    // === 3 Star Tests ===

    @Test
    fun `first attempt correct answer gets 3 stars`() =
        runTest {
            // Given - first attempt, correct
            val userAnswer = "look"

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(3, (result as Result.Success).data.starsEarned)
        }

    @Test
    fun `correct answer after failed attempts still gets 3 stars (child-friendly)`() =
        runTest {
            // Given - child-friendly design: attempt count doesn't matter for per-word rating
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 2,
                    correctAttempts = 0,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - child-friendly: correct answer gets 3 stars regardless of attempt count
            assertTrue(result is Result.Success)
            assertEquals(3, (result as Result.Success).data.starsEarned)
        }

    @Test
    fun `correct answer after many failed attempts still gets 3 stars (child-friendly)`() =
        runTest {
            // Given - child-friendly design: even after many failures, correct answer gets 3 stars
            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 5,
                    correctAttempts = 0,
                    incorrectAttempts = 5,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then - child-friendly: correct answer gets 3 stars regardless of history
            assertTrue(result is Result.Success)
            assertEquals(3, (result as Result.Success).data.starsEarned)
        }

    // === 2 Star Tests ===

    @Test
    fun `correct answer with hint gets 2 stars regardless of attempt count (child-friendly)`() =
        runTest {
            // Given - child-friendly design: hint reduces to 2 stars, regardless of attempts
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

            // When - using hint
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, true, levelId)

            // Then - hint caps at 2 stars regardless of attempt count
            assertTrue(result is Result.Success)
            assertEquals(2, (result as Result.Success).data.starsEarned)
        }

    // === 1 Star Tests ===

    @Test
    fun `correct answer detected as guessing gets 1 star regardless of attempt count`() =
        runTest {
            // Given - setup guessing pattern history
            val now = System.currentTimeMillis()
            val guessingPatterns =
                listOf(
                    com.wordland.domain.algorithm.GuessingDetector.ResponsePattern(
                        now - 5000,
                        500L,
                        true,
                        false,
                    ),
                    com.wordland.domain.algorithm.GuessingDetector.ResponsePattern(
                        now - 4000,
                        600L,
                        true,
                        false,
                    ),
                    com.wordland.domain.algorithm.GuessingDetector.ResponsePattern(
                        now - 3000,
                        500L,
                        true,
                        false,
                    ),
                )
            coEvery { trackingRepository.getRecentPatterns(userId, 5) } returns guessingPatterns

            val existingProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 4,
                    correctAttempts = 0,
                    incorrectAttempts = 4,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns existingProgress

            // When - very fast answer (guessing)
            val result = submitAnswerUseCase(userId, "look_001", "look", 500L, false, levelId)

            // Then - guessing caps at 1 star
            assertTrue(result is Result.Success)
            assertEquals(1, (result as Result.Success).data.starsEarned)
        }

    // === Hint Penalty Tests ===

    @Test
    fun `correct answer with hint used reduces stars by 1`() =
        runTest {
            // Given
            val userAnswer = "look"

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, true, levelId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(2, (result as Result.Success).data.starsEarned) // 3 - 1 = 2
        }

    @Test
    fun `correct answer with hint gets 2 stars (child-friendly design)`() =
        runTest {
            // Given - child-friendly design: hint caps at 2 stars (not 1)
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

            // When - using hint
            val result = submitAnswerUseCase(userId, "look_001", "look", responseTime, true, levelId)

            // Then - hint caps at 2 stars (child-friendly design)
            assertTrue(result is Result.Success)
            assertEquals(2, (result as Result.Success).data.starsEarned)
        }

    // === Wrong Position Feedback Tests ===

    @Test
    fun `wrong answer result contains isGuessing flag`() =
        runTest {
            // Given
            val userAnswer = "lopk" // One letter wrong

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertFalse(answerResult.isCorrect)
            // isGuessing flag is set based on patterns
            assertNotNull(answerResult.isGuessing)
        }

    @Test
    fun `correct answer has isGuessing as false`() =
        runTest {
            // Given
            val userAnswer = "look"

            // When
            val result = submitAnswerUseCase(userId, "look_001", userAnswer, responseTime, false, levelId)

            // Then
            assertTrue(result is Result.Success)
            val answerResult = (result as Result.Success).data
            assertTrue(answerResult.isCorrect)
            // Correct answer with reasonable time is not guessing
            assertFalse(answerResult.isGuessing)
        }

    // === Hint Integration Tests ===

    @Test
    fun `hintUsed parameter is recorded in tracking`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, true, levelId)

            // Then
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = any(),
                    responseTime = responseTime,
                    difficulty = 1,
                    hintUsed = true,
                    isNewWord = true,
                )
            }
        }

    @Test
    fun `hintUsed defaults to false when not specified`() =
        runTest {
            // Given
            coEvery { progressRepository.getWordProgress(any(), any()) } returns null

            // When
            submitAnswerUseCase(userId, "look_001", "look", responseTime, false, levelId)

            // Then
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = true,
                    responseTime = responseTime,
                    difficulty = 1,
                    hintUsed = false, // Default value
                    isNewWord = true,
                )
            }
        }
}
