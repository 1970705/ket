package com.wordland.domain.usecase.usecases.hint

import com.wordland.domain.model.BehaviorTracking
import com.wordland.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Behavior tracking and score penalty integration tests
 *
 * Tests hint-related behavior tracking and scoring penalties:
 * - Score penalty flag is set when using hints
 * - Every hint level applies penalty
 * - Behavior tracking records hint usage
 * - Timestamp is included in tracking
 */
class HintSystemIntegrationBehaviorTest : HintSystemIntegrationTestHelper() {
    // ==================== Score Penalty Tests ====================

    @Test
    fun `hint usage sets penalty flag`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When
            val result = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then
            assertTrue(result is Result.Success)
            val hintResult = (result as Result.Success).data

            assertTrue(hintResult.shouldApplyPenalty)
        }

    @Test
    fun `every hint level applies penalty`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When - Use all 3 hint levels
            val result1 = useHintUseCase("user_001", "test_word_001", "level_01")
            val result2 = useHintUseCase("user_001", "test_word_001", "level_01")
            val result3 = useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - All should apply penalty
            assertTrue((result1 as Result.Success).data.shouldApplyPenalty)
            assertTrue((result2 as Result.Success).data.shouldApplyPenalty)
            assertTrue((result3 as Result.Success).data.shouldApplyPenalty)
        }

    // ==================== Behavior Tracking Tests ====================

    @Test
    fun `hint usage is tracked in BehaviorTracking`() =
        runTest {
            // Given
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When
            useHintUseCase("user_001", "test_word_001", "level_01")

            // Then - Verify tracking was called
            coVerify(exactly = 1) {
                trackingRepository.insertTracking(
                    match<BehaviorTracking> {
                        it.userId == "user_001" &&
                            it.wordId == "test_word_001" &&
                            it.sceneId == "level_01" &&
                            it.action == "hint_used" &&
                            it.hintUsed == true &&
                            it.difficulty == 3
                    },
                )
            }
        }

    @Test
    fun `tracking includes timestamp`() =
        runTest {
            // Given
            val beforeTime = System.currentTimeMillis()
            coEvery { wordRepository.getWordById("test_word_001") } returns testWord

            // When
            useHintUseCase("user_001", "test_word_001", "level_01")
            val afterTime = System.currentTimeMillis()

            // Then
            coVerify {
                trackingRepository.insertTracking(
                    match<BehaviorTracking> {
                        it.timestamp >= beforeTime && it.timestamp <= afterTime
                    },
                )
            }
        }
}
