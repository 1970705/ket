package com.wordland.domain.behavior.detection

import com.wordland.domain.behavior.ProgressAnalysis
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for user progress analysis
 *
 * Tests the analyzeUserProgress() method which evaluates user
 * learning progress and returns appropriate status indicators.
 */
class BehaviorAnalyzerProgressTest : BehaviorAnalyzerTestHelper() {
    @Test
    fun `analyzeUserProgress returns NO_ATTEMPTS when no tracking data`() =
        runTest {
            // Given
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(emptyList())

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.NO_ATTEMPTS, result)
        }

    @Test
    fun `analyzeUserProgress returns NO_ATTEMPTS when no answer actions`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "hint_used"),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.NO_ATTEMPTS, result)
        }

    @Test
    fun `analyzeUserProgress returns MAKING_PROGRESS when mostly correct (80% threshold)`() =
        runTest {
            // Given - 80%+ correct returns MAKING_PROGRESS (4 out of 5 or 8 out of 10)
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = true),
                    createTracking(action = "answer", isCorrect = false),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.MAKING_PROGRESS, result)
        }

    @Test
    fun `analyzeUserProgress returns STRUGGLING when mostly incorrect`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = false),
                    createTracking(action = "answer", isCorrect = true),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.STRUGGLING, result)
        }

    @Test
    fun `analyzeUserProgress returns STRUGGLING when slow incorrect answers (70% threshold)`() =
        runTest {
            // Given - 70% incorrect triggers STRUGGLING before THINKING_TOO_LONG check
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = false, responseTime = 10000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 9000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 9500),
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 8500),
                    createTracking(action = "answer", isCorrect = false, responseTime = 9000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 8000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 9000),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.STRUGGLING, result)
        }

    @Test
    fun `analyzeUserProgress returns THINKING_TOO_LONG when slow incorrect answers (below 70% threshold)`() =
        runTest {
            // Given - Below 70% incorrect but slow answers
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
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.THINKING_TOO_LONG, result)
        }

    @Test
    fun `analyzeUserProgress returns MAKING_PROGRESS when fast correct answers (80%+ threshold)`() =
        runTest {
            // Given - 80%+ correct returns MAKING_PROGRESS before checking ANSWERING_TOO_FAST
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true, responseTime = 2000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 1500),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.MAKING_PROGRESS, result)
        }

    @Test
    fun `analyzeUserProgress returns ANSWERING_TOO_FAST when fast but below 80% correct`() =
        runTest {
            // Given - Below 80% correct but fast answers
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true, responseTime = 2000),
                    createTracking(action = "answer", isCorrect = true, responseTime = 1500),
                    createTracking(action = "answer", isCorrect = false, responseTime = 2000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 2000),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.ANSWERING_TOO_FAST, result)
        }

    @Test
    fun `analyzeUserProgress returns IN_PROGRESS when mixed results`() =
        runTest {
            // Given
            val tracking =
                listOf(
                    createTracking(action = "answer", isCorrect = true, responseTime = 5000),
                    createTracking(action = "answer", isCorrect = false, responseTime = 5000),
                )
            coEvery { trackingRepository.getTrackingByWord("user1", "word1", 10) }
                .returns(tracking)

            // When
            val result = analyzer.analyzeUserProgress("user1", "word1")

            // Then
            assertEquals(ProgressAnalysis.IN_PROGRESS, result)
        }
}
