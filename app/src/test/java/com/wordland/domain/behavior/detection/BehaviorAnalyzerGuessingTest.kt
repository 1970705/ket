package com.wordland.domain.behavior.detection

import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for guessing behavior detection
 *
 * Tests the isGuessing() method which detects when users are
 * rapidly guessing answers instead of thinking through them.
 */
class BehaviorAnalyzerGuessingTest : BehaviorAnalyzerTestHelper() {
    @Test
    fun `isGuessing returns false when no tracking data exists`() =
        runTest {
            // Given
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 5) }
                .returns(emptyList())

            // When
            val result = analyzer.isGuessing("user1", "word1")

            // Then
            assertFalse(result)
        }

    @Test
    fun `isGuessing returns false when answers are slow`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 5000, action = "answer"),
                    createTracking(responseTime = 4000, action = "answer"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 5) }
                .returns(tracking)

            // When
            val result = analyzer.isGuessing("user1", "word1")

            // Then
            assertFalse(result)
        }

    @Test
    fun `isGuessing returns true when multiple fast answers`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 1000, action = "answer"),
                    createTracking(responseTime = 1500, action = "answer"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 5) }
                .returns(tracking)

            // When
            val result = analyzer.isGuessing("user1", "word1")

            // Then
            assertTrue(result)
        }

    @Test
    fun `isGuessing returns false when only one fast answer`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 1000, action = "answer"),
                    createTracking(responseTime = 3000, action = "answer"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 5) }
                .returns(tracking)

            // When
            val result = analyzer.isGuessing("user1", "word1")

            // Then
            assertFalse(result)
        }

    @Test
    fun `isGuessing ignores non-answer actions`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(responseTime = 1000, action = "hint_used"),
                    createTracking(responseTime = 1500, action = "hint_used"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 5) }
                .returns(tracking)

            // When
            val result = analyzer.isGuessing("user1", "word1")

            // Then
            assertFalse(result)
        }
}
