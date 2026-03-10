package com.wordland.domain.usecase.usecases.submit

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.ComboState
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
 * Unit tests for SubmitQuickJudgeAnswerUseCase scoring calculation
 * Tests base score, time bonus, and combo bonus
 */
class SubmitQuickJudgeScoringTest {
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

    // === Score Calculation Tests ===

    @Test
    fun `correct answer receives base score of 100`() =
        runTest {
            val timeTakenMs = 3000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                    previousComboState = ComboState(),
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertEquals(100, judgeResult.baseScore)
        }

    @Test
    fun `wrong answer receives penalty score`() =
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
            val judgeResult = (result as Result.Success).data
            assertEquals(20, judgeResult.baseScore)
        }

    @Test
    fun `fast answer receives time bonus`() =
        runTest {
            val timeTakenMs = 1000L

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
            assertTrue(judgeResult.timeBonus > 0)
            assertTrue(judgeResult.totalScore > judgeResult.baseScore)
        }

    @Test
    fun `slow answer receives no time bonus`() =
        runTest {
            val timeTakenMs = 6000L

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
            assertEquals(0, judgeResult.timeBonus)
        }

    @Test
    fun `combo increases total score`() =
        runTest {
            val comboState = ComboState(consecutiveCorrect = 3)
            val timeTakenMs = 2000L

            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                    previousComboState = comboState,
                )

            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.totalScore >= judgeResult.baseScore)
        }

    // === Static Method Tests ===

    @Test
    fun `calculateStars returns 3 for perfect performance`() {
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 9,
                totalCount = 10,
                avgTimeTaken = 1500L,
                maxTimeLimit = 5,
            )

        assertEquals(3, stars)
    }

    @Test
    fun `calculateStars returns 2 for good performance`() {
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 8,
                totalCount = 10,
                avgTimeTaken = 3000L,
                maxTimeLimit = 5,
            )

        assertEquals(2, stars)
    }

    @Test
    fun `calculateStars returns 1 for passing performance`() {
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 6,
                totalCount = 10,
                avgTimeTaken = 4000L,
                maxTimeLimit = 5,
            )

        assertEquals(1, stars)
    }

    @Test
    fun `calculateStars returns 0 for poor performance`() {
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 3,
                totalCount = 10,
                avgTimeTaken = 4000L,
                maxTimeLimit = 5,
            )

        assertEquals(0, stars)
    }

    @Test
    fun `calculateStars returns 0 for empty level`() {
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 0,
                totalCount = 0,
                avgTimeTaken = 0L,
                maxTimeLimit = 5,
            )

        assertEquals(0, stars)
    }

    @Test
    fun `calculateCombo increments on correct answer`() {
        val previousState = ComboState(consecutiveCorrect = 2)

        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = true,
                timeTakenMs = 3000L,
            )

        assertTrue(newState.consecutiveCorrect > previousState.consecutiveCorrect)
    }

    @Test
    fun `calculateCombo resets on wrong answer`() {
        val previousState = ComboState(consecutiveCorrect = 5)

        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = false,
                timeTakenMs = 1000L,
            )

        assertEquals(0, newState.consecutiveCorrect)
    }

    @Test
    fun `calculateCombo ignores guessing answers`() {
        val previousState = ComboState(consecutiveCorrect = 2)
        val guessingTime = 500L

        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = true,
                timeTakenMs = guessingTime,
            )

        assertEquals(2, newState.consecutiveCorrect)
    }
}
