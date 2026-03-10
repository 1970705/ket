package com.wordland.domain.behavior.detection

import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for hint recommendation and triggering logic
 *
 * Tests hint-related functionality:
 * - Hint recommendation (shouldRecommendHint)
 * - Hint dependency level calculation
 * - Automatic hint triggering
 * - Recommended hint level selection
 */
class BehaviorAnalyzerHintTest : BehaviorAnalyzerTestHelper() {
    // ==================== Hint Recommendation Tests ====================

    @Test
    fun `shouldRecommendHint returns false when no tracking data`() =
        runTest {
            // Given
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 3) }
                .returns(emptyList())

            // When
            val result = analyzer.shouldRecommendHint("user1", "word1")

            // Then
            assertFalse(result)
        }

    @Test
    fun `shouldRecommendHint returns true when multiple incorrect attempts`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(isCorrect = false, action = "answer"),
                    createTracking(isCorrect = false, action = "answer"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 3) }
                .returns(tracking)

            // When
            val result = analyzer.shouldRecommendHint("user1", "word1")

            // Then
            assertTrue(result)
        }

    @Test
    fun `shouldRecommendHint returns true when struggling with long response time`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 12000, action = "answer", isCorrect = true),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 3) }
                .returns(tracking)

            // When
            val result = analyzer.shouldRecommendHint("user1", "word1")

            // Then
            assertTrue(result)
        }

    @Test
    fun `shouldRecommendHint returns false when performing well`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 5000, action = "answer", isCorrect = true),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 3) }
                .returns(tracking)

            // When
            val result = analyzer.shouldRecommendHint("user1", "word1")

            // Then
            assertFalse(result)
        }

    // ==================== Hint Dependency Level Tests ====================

    @Test
    fun `getHintDependencyLevel returns 0 when no tracking data`() =
        runTest {
            // Given
            coEvery { trackingRepository.getRecentTracking("user1", 1000) }
                .returns(emptyList())

            // When
            val result = analyzer.getHintDependencyLevel("user1")

            // Then
            assertEquals(0f, result)
        }

    @Test
    fun `getHintDependencyLevel calculates frequency correctly`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "hint_used"),
                    createTracking(action = "hint_used"),
                    createTracking(action = "answer"),
                    createTracking(action = "answer"),
                )
            coEvery { trackingRepository.getRecentTracking("user1", 1000) }
                .returns(tracking)

            // When
            val result = analyzer.getHintDependencyLevel("user1")

            // Then - 2 hints out of 4 actions = 0.5 frequency
            // max consecutive hints = 2
            // (0.5 * 0.7) + (2/5 * 0.3) = 0.35 + 0.12 = 0.47
            assertTrue(result > 0.3f && result < 0.6f)
        }

    @Test
    fun `getHintDependencyLevel detects consecutive hint usage`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "hint_used"),
                    createTracking(action = "hint_used"),
                    createTracking(action = "hint_used"),
                    createTracking(action = "answer"),
                    createTracking(action = "hint_used"),
                )
            coEvery { trackingRepository.getRecentTracking("user1", 1000) }
                .returns(tracking)

            // When
            val result = analyzer.getHintDependencyLevel("user1")

            // Then - 4 hints out of 5 actions = 0.8 frequency
            // max consecutive hints = 3
            // (0.8 * 0.7) + (3/5 * 0.3) = 0.56 + 0.18 = 0.74
            assertTrue(result > 0.6f)
        }

    // ==================== Automatic Hint Trigger Tests ====================

    @Test
    fun `shouldTriggerHintAutomatically returns true for STRUGGLING`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.shouldTriggerHintAutomatically("user1", "word1")

            // Then
            assertTrue(result)
        }

    @Test
    fun `shouldTriggerHintAutomatically returns true for THINKING_TOO_LONG`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false, responseTime = 10000),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.shouldTriggerHintAutomatically("user1", "word1")

            // Then
            assertTrue(result)
        }

    @Test
    fun `shouldTriggerHintAutomatically returns false for MAKING_PROGRESS`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = true),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.shouldTriggerHintAutomatically("user1", "word1")

            // Then
            assertFalse(result)
        }

    // ==================== Recommended Hint Level Tests ====================

    @Test
    fun `getRecommendedHintLevel returns 2 for STRUGGLING`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.getRecommendedHintLevel("user1", "word1")

            // Then
            assertEquals(2, result)
        }

    @Test
    fun `getRecommendedHintLevel returns 1 for THINKING_TOO_LONG`() =
        runTest {
            // Given - Below 70% incorrect threshold but slow
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false, responseTime = 10000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 9000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.getRecommendedHintLevel("user1", "word1")

            // Then
            assertEquals(1, result)
        }

    @Test
    fun `getRecommendedHintLevel returns 1 for NO_ATTEMPTS`() =
        runTest {
            // Given
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(emptyList())

            // When
            val result = analyzer.getRecommendedHintLevel("user1", "word1")

            // Then
            assertEquals(1, result)
        }

    @Test
    fun `getRecommendedHintLevel returns 0 for MAKING_PROGRESS`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = true),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.getRecommendedHintLevel("user1", "word1")

            // Then
            assertEquals(0, result)
        }
}
