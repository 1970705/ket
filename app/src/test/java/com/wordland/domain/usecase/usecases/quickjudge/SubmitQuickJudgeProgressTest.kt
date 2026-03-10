package com.wordland.domain.usecase.usecases.submit

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.SubmitQuickJudgeAnswerUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SubmitQuickJudgeAnswerUseCase progress updates and behavior tracking
 * Tests progress tracking updates and behavior tracking
 */
class SubmitQuickJudgeProgressTest {
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var trackingRepository: TrackingRepository
    private lateinit var useCase: SubmitQuickJudgeAnswerUseCase

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

    private val correctQuestion =
        com.wordland.domain.model.QuickJudgeQuestion(
            wordId = "look_001",
            word = "look",
            translation = "看",
            isCorrect = true,
            timeLimit = 5,
            difficulty = 1,
        )

    private val userId = "user_001"
    private val levelId = "look_level_01"

    @Before
    fun setup() {
        wordRepository = mockk()
        progressRepository = mockk()
        trackingRepository = mockk()
        useCase = SubmitQuickJudgeAnswerUseCase(wordRepository, progressRepository, trackingRepository)

        coEvery { wordRepository.getWordById(any()) } returns testWord
        coEvery { progressRepository.getWordProgress(any(), any()) } returns null
        coEvery { progressRepository.updatePracticeResult(any(), any(), any(), any(), any()) } just Runs
        coEvery { trackingRepository.recordAnswer(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Progress Update Tests ===

    @Test
    fun `correct answer updates word progress`() =
        runTest {
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = timeTakenMs,
                    newMemoryStrength = any(),
                )
            }
        }

    @Test
    fun `wrong answer does not update progress positively`() =
        runTest {
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = false,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            coVerify(exactly = 0) {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = any(),
                    newMemoryStrength = any(),
                )
            }
        }

    @Test
    fun `memory strength increases based on response time`() =
        runTest {
            val fastTimeMs = 1000L
            val currentProgress =
                UserWordProgress(
                    userId = userId,
                    wordId = "look_001",
                    status = com.wordland.domain.model.LearningStatus.LEARNING,
                    memoryStrength = 50,
                    totalAttempts = 5,
                    correctAttempts = 3,
                    incorrectAttempts = 2,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastReviewTime = System.currentTimeMillis(),
                )
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns currentProgress

            useCase(
                userId = userId,
                question = correctQuestion,
                userSaidTrue = true,
                timeTakenMs = fastTimeMs,
                levelId = levelId,
            )

            coVerify {
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = "look_001",
                    isCorrect = true,
                    responseTime = fastTimeMs,
                    newMemoryStrength = match { it > 50 },
                )
            }
        }

    // === Behavior Tracking Tests ===

    @Test
    fun `correct answer records behavior tracking`() =
        runTest {
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = true,
                    responseTime = timeTakenMs,
                    difficulty = 1,
                    hintUsed = false,
                    isNewWord = any(),
                )
            }
        }

    @Test
    fun `new word is tracked correctly`() =
        runTest {
            coEvery { progressRepository.getWordProgress(userId, "look_001") } returns null

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            coVerify {
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = "look_001",
                    sceneId = levelId,
                    isCorrect = true,
                    responseTime = any(),
                    difficulty = any(),
                    hintUsed = false,
                    isNewWord = true,
                )
            }
        }

    // === Result Structure Tests ===

    @Test
    fun `result contains all expected fields`() =
        runTest {
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertNotNull(judgeResult.question)
            assertNotNull(judgeResult.timeTakenMs == timeTakenMs)
            assertNotNull(judgeResult.baseScore)
            assertNotNull(judgeResult.totalScore)
        }

    @Test
    fun `result preserves question metadata`() =
        runTest {
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertEquals(correctQuestion.wordId, judgeResult.question.wordId)
            assertEquals(correctQuestion.word, judgeResult.question.word)
            assertEquals(correctQuestion.translation, judgeResult.question.translation)
        }
}
