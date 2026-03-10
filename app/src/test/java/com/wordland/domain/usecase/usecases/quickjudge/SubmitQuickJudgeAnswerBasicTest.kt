package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.QuickJudgeQuestion
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.SubmitQuickJudgeAnswerUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Basic functionality tests for SubmitQuickJudgeAnswerUseCase.
 * Tests correct/wrong judgments and score calculation.
 */
class SubmitQuickJudgeAnswerBasicTest {
    private lateinit var wordRepository: WordRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var trackingRepository: TrackingRepository
    private lateinit var useCase: SubmitQuickJudgeAnswerUseCase

    private val testWord =
        com.wordland.domain.model.Word(
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
        QuickJudgeQuestion(
            wordId = "look_001",
            word = "look",
            translation = "看",
            isCorrect = true,
            timeLimit = 5,
            difficulty = 1,
        )

    private val incorrectQuestion =
        QuickJudgeQuestion(
            wordId = "look_001",
            word = "look",
            translation = "看见", // Wrong translation
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
        // No cleanup needed with mockk
    }

    // === Correct Judgment Tests ===

    @Test
    fun `invoke returns success when user correctly judges true as true`() =
        runTest {
            // Given - correct question (translation IS correct)
            // User says True (it IS correct)

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.isCorrect)
            assertEquals(true, judgeResult.userSaidTrue)
            assertTrue(judgeResult.totalScore > 0)
        }

    @Test
    fun `invoke returns success when user correctly judges false as false`() =
        runTest {
            // Given - incorrect question (translation is WRONG)
            // User says False (it is NOT correct)

            // When
            val result =
                useCase(
                    userId = userId,
                    question = incorrectQuestion,
                    userSaidTrue = false,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.isCorrect)
            assertEquals(false, judgeResult.userSaidTrue)
        }

    // === Wrong Judgment Tests ===

    @Test
    fun `invoke returns wrong judgment when user incorrectly says true`() =
        runTest {
            // Given - incorrect question (translation is WRONG)
            // User incorrectly says True (it is NOT correct)

            // When
            val result =
                useCase(
                    userId = userId,
                    question = incorrectQuestion,
                    userSaidTrue = true,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertFalse(judgeResult.isCorrect)
            assertEquals(true, judgeResult.userSaidTrue)
        }

    @Test
    fun `invoke returns wrong judgment when user incorrectly says false`() =
        runTest {
            // Given - correct question (translation IS correct)
            // User incorrectly says False (it IS correct)

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = false,
                    timeTakenMs = 2000L,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertFalse(judgeResult.isCorrect)
            assertEquals(false, judgeResult.userSaidTrue)
        }

    // === Score Calculation Tests ===

    @Test
    fun `correct answer receives base score of 100`() =
        runTest {
            // Given
            val timeTakenMs = 3000L // Within time limit

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                    previousComboState = ComboState(),
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertEquals(100, judgeResult.baseScore)
        }

    @Test
    fun `wrong answer receives penalty score`() =
        runTest {
            // Given
            val timeTakenMs = 2000L

            // When - wrong judgment
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = false, // Wrong - it IS correct
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            // Wrong answer gets SCORE_PENALTY_WRONG (20 points) as consolation
            assertEquals(20, judgeResult.baseScore)
        }

    @Test
    fun `fast answer receives time bonus`() =
        runTest {
            // Given - very fast answer (1 second out of 5)
            val timeTakenMs = 1000L

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertTrue(judgeResult.timeBonus > 0)
            // Time bonus should be higher for faster answers
            assertTrue(judgeResult.totalScore > judgeResult.baseScore)
        }

    @Test
    fun `slow answer receives no time bonus`() =
        runTest {
            // Given - answer after time limit
            val timeTakenMs = 6000L // Exceeds 5 second limit

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            assertEquals(0, judgeResult.timeBonus)
        }

    @Test
    fun `combo increases total score`() =
        runTest {
            // Given - user has a combo going
            val comboState = ComboState(consecutiveCorrect = 3)
            val timeTakenMs = 2000L

            // When
            val result =
                useCase(
                    userId = userId,
                    question = correctQuestion,
                    userSaidTrue = true,
                    timeTakenMs = timeTakenMs,
                    levelId = levelId,
                    previousComboState = comboState,
                )

            // Then
            assertTrue(result is Result.Success)
            val judgeResult = (result as Result.Success).data
            // Total score should include combo bonus
            assertTrue(judgeResult.totalScore >= judgeResult.baseScore)
        }
}
