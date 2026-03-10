package com.wordland.domain.usecase.usecases.hint

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Hint limits and cooldown integration tests
 *
 * Tests hint usage limits, cooldown periods, and related behavior:
 * - Max hints per word limit
 * - Hint statistics
 * - Cooldown period enforcement
 * - First hint without cooldown
 */
class HintSystemIntegrationLimitsTest : HintSystemIntegrationTestHelper() {
    // ==================== Hint Limit Tests ====================

    @Test
    fun `hint limit enforced after max hints reached`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Use all 3 hints
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")

            val fourthAttempt = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Fourth attempt should fail
            assertTrue(fourthAttempt is Result.Error)
            val error = (fourthAttempt as Result.Error).exception

            assertTrue(error is com.wordland.domain.usecase.usecases.HintLimitException)
            assertTrue(error.message?.contains("上限") == true)
        }

    @Test
    fun `hint stats reflect correct usage counts`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Use 2 hints
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            val stats = useHintUseCase.getHintStats("test_word_001")

            assertNotNull(stats)
            assertEquals("test_word_001", stats?.wordId)
            assertEquals(2, stats?.currentLevel)
            assertEquals(2, stats?.hintsUsed)
            assertEquals(1, stats?.hintsRemaining)
            assertEquals(3, stats?.totalHints)
        }

    // ==================== Cooldown Period Tests ====================

    @Test
    fun `cooldown prevents rapid hint usage`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord
            hintManager.hintCooldownMs = 5000 // Enable cooldown

            // When - Use first hint
            useHintUseCase("user_001", "test_word_001", "level_01")

            // Try to use second hint immediately (before cooldown)
            val secondAttempt = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Should fail due to cooldown
            assertTrue(secondAttempt is Result.Error)
        }

    @Test
    fun `cooldown expires after real time delay`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord
            hintManager.hintCooldownMs = 100 // 100ms for test

            // When - Use hint
            val firstResult = useHintUseCase("user_001", "test_word_001", "level_01")

            // Wait for cooldown (real time since HintManager uses System.currentTimeMillis)
            Thread.sleep(150)

            val secondResult = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Both should succeed
            assertTrue(firstResult is Result.Success)
            assertTrue(secondResult is Result.Success)

            assertEquals(1, (firstResult as Result.Success).data.hintLevel)
            assertEquals(2, (secondResult as Result.Success).data.hintLevel)
        }

    @Test
    fun `first hint available immediately without cooldown`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord
            hintManager.hintCooldownMs = 5000 // 5 second cooldown

            // When - First hint use
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Should succeed immediately (no cooldown on first use)
            assertTrue(result is Result.Success)
            assertEquals(1, (result as Result.Success).data.hintLevel)
        }
}
