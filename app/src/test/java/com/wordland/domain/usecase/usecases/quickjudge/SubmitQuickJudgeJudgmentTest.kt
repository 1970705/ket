package com.wordland.domain.usecase.usecases.submit

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.SubmitQuickJudgeAnswerUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SubmitQuickJudgeAnswerUseCase judgment logic
 * Tests correct/incorrect judgment handling
 */
class SubmitQuickJudgeJudgmentTest {
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

    private val incorrectQuestion =
        com.wordland.domain.model.QuickJudgeQuestion(
            wordId = "look_001",
            word = "look",
            translation = "看见",
            isCorrect = false,
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

    // === Correct Judgment Tests ===

    @Test
    fun `invoke returns success when user correctly judges true as true`() =
        runTest {
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.isCorrect)
            assertTrue(judgeResult.userSaidTrue)
            assertTrue(judgeResult.totalScore > 0)
        }

    @Test
    fun `invoke returns success when user correctly judges false as false`() =
        runTest {
            val result =
                useCase(
                    userId = userId,
                    question = incorrectQuestion,
                    userSaidTrue = false,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.isCorrect)
            assertFalse(judgeResult.userSaidTrue)
        }

    // === Wrong Judgment Tests ===

    @Test
    fun `invoke returns wrong judgment when user incorrectly says true`() =
        runTest {
            val result =
                useCase(
                    userId = userId,
                    question = incorrectQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertFalse(judgeResult.isCorrect)
            assertTrue(judgeResult.userSaidTrue)
        }

    @Test
    fun `invoke returns wrong judgment when user incorrectly says false`() =
        runTest {
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = false,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertFalse(judgeResult.isCorrect)
            assertFalse(judgeResult.userSaidTrue)
        }

    // === Error Handling Tests ===

    @Test
    fun `invoke returns error when word not found`() =
        runTest {
            coEvery { wordRepository.getWordById("unknown_word") } returns null

            val unknownQuestion =
                com.wordland.domain.model.QuickJudgeQuestion(
                    wordId = "unknown_word",
                    word = "unknown",
                    translation = "未知",
                    isCorrect = true,
                    timeLimit = 5,
                    difficulty = 1,
                )

            val result =
                useCase(
                    userId = userId,
                    question = unknownQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns error when repository throws exception`() =
        runTest {
            coEvery { wordRepository.getWordById(any()) } throws RuntimeException("Database error")

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            assertTrue(result is Result.Error)
        }
}
