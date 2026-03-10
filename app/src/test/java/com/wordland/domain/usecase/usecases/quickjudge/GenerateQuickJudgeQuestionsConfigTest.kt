package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Question count, time limit, and single question generation tests
 *
 * Tests:
 * - Question count constraints (min/max)
 * - Time limit configuration
 * - Single question generation
 */
class GenerateQuickJudgeQuestionsConfigTest : GenerateQuickJudgeQuestionsTestHelper() {
    // === Question Count Tests ===

    @Test
    fun `invoke respects custom count parameter`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01", count = 5)

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data
            assertEquals(5, questions.size)
        }

    @Test
    fun `invoke enforces minimum question count`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When - request less than minimum
            val result = useCase("look_level_01", count = 2)

            // Then - should enforce minimum of 4
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data
            assertTrue(questions.size >= 4)
        }

    @Test
    fun `invoke enforces maximum question count`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When - request more than maximum
            val result = useCase("look_level_01", count = 20)

            // Then - should enforce maximum of 8
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data
            assertTrue(questions.size <= 8)
        }

    // === Time Limit Tests ===

    @Test
    fun `questions use default time limit of 5 seconds`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When - use default time limit
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            questions.forEach { question ->
                assertEquals(5, question.timeLimit)
            }
        }

    @Test
    fun `questions use custom time limit when provided`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01", timeLimit = 10)

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            questions.forEach { question ->
                assertEquals(10, question.timeLimit)
            }
        }

    // === Single Question Generation Tests ===

    @Test
    fun `generateSingleQuestion returns correct question`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("look_001") } returns testWords[0]
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase.generateSingleQuestion("look_001", forceCorrect = true)

            // Then
            assertTrue(result is Result.Success)
            val question = (result as Result.Success).data
            assertEquals("look_001", question.wordId)
            assertEquals("look", question.word)
            assertEquals("看", question.translation)
            assertTrue(question.isCorrect)
        }

    @Test
    fun `generateSingleQuestion returns error when word not found`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("unknown_word") } returns null

            // When
            val result = useCase.generateSingleQuestion("unknown_word")

            // Then
            assertTrue(result is Result.Error)
        }
}
