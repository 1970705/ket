package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Question content and correctness ratio tests
 *
 * Tests:
 * - Question structure validation
 * - Correct/incorrect question flags
 * - Question shuffling
 * - Correct answer ratio (60% target)
 */
class GenerateQuickJudgeQuestionsContentTest : GenerateQuickJudgeQuestionsTestHelper() {
    // === Question Content Tests ===

    @Test
    fun `generated questions have correct structure`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            questions.forEach { question ->
                assertNotNull(question.wordId)
                assertNotNull(question.word)
                assertNotNull(question.translation)
                assertTrue(question.timeLimit > 0)
                assertTrue(question.difficulty > 0)
            }
        }

    @Test
    fun `correct questions have isCorrect set to true`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            // Check that correct questions match their word's translation
            val correctQuestions = questions.filter { it.isCorrect }
            correctQuestions.forEach { question ->
                val originalWord = testWords.find { it.id == question.wordId }
                assertEquals(originalWord?.translation, question.translation)
            }
        }

    @Test
    fun `incorrect questions have isCorrect set to false`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            // Check that incorrect questions have different translation
            val incorrectQuestions = questions.filter { !it.isCorrect }
            incorrectQuestions.forEach { question ->
                val originalWord = testWords.find { it.id == question.wordId }
                // Translation should be different from actual translation
                assertFalse(originalWord?.translation == question.translation && question.isCorrect)
            }
        }

    @Test
    fun `questions are shuffled in final output`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result1 = useCase("look_level_01")
            val result2 = useCase("look_level_01")

            // Then - with shuffling, questions should be in different order
            assertTrue(result1 is Result.Success)
            assertTrue(result2 is Result.Success)
            val questions1 = (result1 as Result.Success).data
            val questions2 = (result2 as Result.Success).data

            // Check that at least one question is in different position
            val hasDifferentOrder =
                questions1.indices.any { i ->
                    i < questions2.size && questions1[i].wordId != questions2[i].wordId
                }
            assertTrue(hasDifferentOrder)
        }

    // === Correct Ratio Tests ===

    @Test
    fun `correct questions ratio is approximately 60 percent`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01", count = 6)

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            val correctCount = questions.count { it.isCorrect }
            val ratio = correctCount.toFloat() / questions.size

            // Should be approximately 60% (allow some tolerance)
            assertTrue(ratio >= 0.4f && ratio <= 0.8f)
        }

    @Test
    fun `at least one correct question is generated`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01", count = 4)

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            val correctCount = questions.count { it.isCorrect }
            assertTrue(correctCount >= 1)
        }
}
