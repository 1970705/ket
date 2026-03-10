package com.wordland.domain.usecase.usecases.hint

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Basic hint functionality integration tests
 *
 * Tests the core hint generation and display features:
 * - Progressive hints (3 levels)
 * - Adaptive hints based on word length
 */
class HintSystemIntegrationBasicTest : HintSystemIntegrationTestHelper() {
    // ==================== Progressive Hints Tests ====================

    @Test
    fun `progressive hints show Level 1 (first letter) on first use`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When
            val result =
                useHintUseCase(
                    userId = "user_001",
                    wordId = "test_word_001",
                    levelId = "level_01",
                )

            // Then
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertEquals(1, hintResult.hintLevel)
            assertTrue(hintResult.hintText.contains("首字母"))
            assertTrue(hintResult.hintText.contains("A"))
            assertEquals(2, hintResult.hintsRemaining) // 3 total - 1 used
            assertEquals(1, hintResult.hintsUsed)
            assertTrue(hintResult.shouldApplyPenalty)
        }

    @Test
    fun `progressive hints show Level 2 (first half) on second use`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - First use
            useHintUseCase("user_001", "test_word_001", "level_01")

            // Second use (cooldown disabled in setup)
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertEquals(2, hintResult.hintLevel)
            assertTrue(hintResult.hintText.contains("前半部分"))
            assertEquals(1, hintResult.hintsRemaining) // 3 total - 2 used
            assertEquals(2, hintResult.hintsUsed)
        }

    @Test
    fun `progressive hints show Level 3 (vowels masked) on third use`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Use hints three times
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")

            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertEquals(3, hintResult.hintLevel)
            assertTrue(hintResult.hintText.contains("元音隐藏"))
            assertTrue(hintResult.hintText.contains("ppl")) // consonants shown
            assertEquals(0, hintResult.hintsRemaining) // All used
            assertEquals(3, hintResult.hintsUsed)
        }

    // ==================== Adaptive Hints Tests ====================

    @Test
    fun `adaptive hints work for short words`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_002") } returns shortWord

            // When - First hint for 3-letter word
            val result = useHintUseCase("user_001", "test_word_002", "level_01")

            // Then - Should skip to Level 2 for short words
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertEquals(1, hintResult.hintLevel) // Still count as 1st use
            assertTrue(hintResult.hintText.contains("前半部分")) // But shows Level 2 style
        }

    @Test
    fun `adaptive hints add intermediate level for long words`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_003") } returns longWord

            // When - Second hint for long word
            useHintUseCase("user_001", "test_word_003", "level_01")

            val result = useHintUseCase("user_001", "test_word_003", "level_01")

            // Then - Should show 40% instead of 50%
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertEquals(2, hintResult.hintLevel)
            assertTrue(hintResult.hintText.contains("部分提示"))
        }
}
