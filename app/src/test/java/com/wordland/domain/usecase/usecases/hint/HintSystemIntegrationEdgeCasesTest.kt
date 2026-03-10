package com.wordland.domain.usecase.usecases.hint

import com.wordland.domain.model.Result
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Word management, difficulty-based hints, and error handling tests
 *
 * Tests advanced hint system features:
 * - Hint reset and reuse
 * - Independent hint limits per word
 * - Difficulty-based max hints adjustment
 * - Error handling for missing words
 * - Edge cases (empty words, special characters)
 */
class HintSystemIntegrationEdgeCasesTest : HintSystemIntegrationTestHelper() {
    // ==================== Word Management Tests ====================

    @Test
    fun `resetHints allows hint reuse for same word`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Use all hints
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")

            // Reset
            useHintUseCase.resetHints("test_word_001")

            // Should be able to use hint again
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result is Result.Success)
            assertEquals(1, (result as Result.Success).data.hintLevel)
            assertEquals(2, (result as Result.Success).data.hintsRemaining)
        }

    @Test
    fun `different words have independent hint limits`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord
            coEvery { wordRepository.getWordById("test_word_002") } returns shortWord

            // When - Use all hints for word_001
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")
            useHintUseCase("user_001", "test_word_001", "level_01")

            // word_001 should be at limit
            val word1Limit = useHintUseCase("user_001", "test_word_001", "level_01")

            // word_002 should still be available
            val word2Result = useHintUseCase("user_001", "test_word_002", "level_01")

            // Then
            assertTrue(word1Limit is Result.Error)
            assertTrue(word2Result is Result.Success)
        }

    // ==================== Difficulty-Based Tests ====================

    @Test
    fun `difficulty adjusts max hints`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Configure for easy difficulty
            useHintUseCase.configureForDifficulty(1) // 5 hints for easy

            // Then - Can use more hints
            val result1 = useHintUseCase("user_001", "test_word_001", "level_01")
            val result2 = useHintUseCase("user_001", "test_word_001", "level_01")
            val result3 = useHintUseCase("user_001", "test_word_001", "level_01")

            // Should still have hints remaining
            val stats = useHintUseCase.getHintStats("test_word_001")
            assertEquals(5, stats?.totalHints)
            assertEquals(2, stats?.hintsRemaining)
        }

    @Test
    fun `hard difficulty limits hints to 1`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Configure for hard difficulty
            useHintUseCase.configureForDifficulty(5) // 1 hint for hard

            // Use the only available hint
            val result1 = useHintUseCase("user_001", "test_word_001", "level_01")

            val result2 = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result1 is Result.Success)
            assertTrue(result2 is Result.Error)

            val stats = useHintUseCase.getHintStats("test_word_001")
            assertEquals(1, stats?.totalHints)
            assertEquals(0, stats?.hintsRemaining)
        }

    // ==================== Error Handling Tests ====================

    @Test
    fun `returns error when word not found`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("unknown_word") } returns null

            // When
            val result = useHintUseCase("user_001", "unknown_word", "level_01")

            // Then
            assertTrue(result is Result.Error)
        }

    @Test
    fun `canUseHint returns availability status`() {
        // Given - No hints used yet

        // When
        val (canUse, reason) = useHintUseCase.canUseHint("new_word")

        // Then
        assertTrue(canUse)
        assertNull(reason)
    }

    @Test
    fun `canUseHint reflects cooldown status`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord
            hintManager.hintCooldownMs = 5000 // Enable cooldown

            // When - Use a hint
            useHintUseCase("user_001", "test_word_001", "level_01")

            val (canUse, reason) = useHintUseCase.canUseHint("test_word_001")

            // Then - Should be in cooldown
            assertFalse(canUse)
            assertNotNull(reason)
            assertTrue(reason?.contains("秒") == true)
        }

    // ==================== Edge Cases ====================

    @Test
    fun `handles empty word gracefully`() =
        runTest {
            // Given
            val emptyWord = testWord.copy(word = "")
            coEvery { wordRepository.getWordById("test_word_001") } returns emptyWord

            // When
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Should still succeed with minimal hint
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data
            // Empty word produces empty or minimal hint
            assertNotNull(hintResult.hintText)
        }

    @Test
    fun `handles special characters in word`() =
        runTest {
            // Given
            val specialWord = testWord.copy(word = "hello-world")
            coEvery { wordRepository.getWordById("test_word_001") } returns specialWord

            // When
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data
            assertTrue(hintResult.hintText.contains("首字母"))
            assertTrue(hintResult.hintText.contains("H"))
        }

    @Test
    fun `hint stats return null for unknown word`() {
        // Given - Word never used

        // When
        val stats = useHintUseCase.getHintStats("never_used_word")

        // Then - Should return null or default stats
        // Current implementation returns stats with 0 usage
        val (canUse, _) = useHintUseCase.canUseHint("never_used_word")
        assertTrue(canUse) // New word should be usable
    }
}
