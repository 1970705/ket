package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Basic generation and edge case tests for Quick Judge questions
 *
 * Tests:
 * - Basic question generation
 * - Error handling
 * - Edge cases (single word, empty level)
 */
class GenerateQuickJudgeQuestionsBasicTest : GenerateQuickJudgeQuestionsTestHelper() {
    // === Basic Generation Tests ===

    @Test
    fun `invoke returns success with questions for valid level`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data
            assertNotNull(questions)
            assertTrue(questions.size >= 4) // Minimum questions
            assertTrue(questions.size <= 8) // Maximum questions
        }

    @Test
    fun `invoke returns error when level has no words`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("empty_level") } returns emptyList()

            // When
            val result = useCase("empty_level")

            // Then
            assertTrue(result is Result.Error)
        }

    @Test
    fun `invoke returns error when repository throws exception`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("error_level") } throws RuntimeException("Database error")

            // When
            val result = useCase("error_level")

            // Then
            assertTrue(result is Result.Error)
            val exception = (result as Result.Error).exception
            assertTrue(exception.message?.contains("Database error") == true)
        }

    // === Edge Cases ===

    @Test
    fun `invoke handles single word level`() =
        runTest {
            // Given - level with only one word
            val singleWordLevel = listOf(testWords[0])
            coEvery { wordRepository.getWordsByLevel("single_level") } returns singleWordLevel

            // When
            val result = useCase("single_level")

            // Then - should generate questions but may be limited
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data
            // With only one word, can only generate correct questions
            // since there are no other words for wrong translations
            assertTrue(questions.size >= 1)
            assertTrue(questions.size <= 4)
        }

    @Test
    fun `invoke preserves word metadata in questions`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordsByLevel("look_level_01") } returns testWords

            // When
            val result = useCase("look_level_01")

            // Then
            assertTrue(result is Result.Success)
            val questions = (result as Result.Success).data

            questions.forEach { question ->
                val originalWord = testWords.find { it.id == question.wordId }
                assertEquals(originalWord?.word, question.word)
                assertEquals(originalWord?.difficulty, question.difficulty)
            }
        }
}
