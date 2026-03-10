package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UserWordProgress
 */
class UserWordProgressTest {
    @Test
    fun `isMastered returns false when correct attempts less than 3`() {
        // Given
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 80,
                correctAttempts = 2, // Less than 3
                crossSceneCorrect = 5,
                crossSceneTotal = 5,
            )

        // When
        val result = progress.isMastered()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isMastered returns false when memory strength is low`() {
        // Given
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 50, // Less than 100 (MASTERY_PERCENTAGE_COMPLETE)
                correctAttempts = 3,
                crossSceneCorrect = 5,
                crossSceneTotal = 5,
            )

        // When
        val result = progress.isMastered()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isMastered returns false when cross scene rate is low`() {
        // Given
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 100,
                correctAttempts = 3,
                crossSceneCorrect = 3, // 3/5 = 60% < 70%
                crossSceneTotal = 5,
            )

        // When
        val result = progress.isMastered()

        // Then
        assertFalse(result)
    }

    @Test
    fun `isMastered returns true when all criteria met`() {
        // Given
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 100,
                correctAttempts = 3,
                crossSceneCorrect = 4, // 4/5 = 80% >= 70%
                crossSceneTotal = 5,
            )

        // When
        val result = progress.isMastered()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isMastered returns true when cross scene total is zero`() {
        // Given
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 100,
                correctAttempts = 3,
                crossSceneCorrect = 0,
                crossSceneTotal = 0, // No cross-scene tests yet
            )

        // When
        val result = progress.isMastered()

        // Then
        assertTrue(result) // Should still be mastered
    }

    @Test
    fun `isMastered returns true at mastery threshold`() {
        // Given - Edge case: exactly at threshold
        val progress =
            UserWordProgress(
                wordId = "word1",
                userId = "user_001",
                status = LearningStatus.LEARNING,
                memoryStrength = 100,
                correctAttempts = 3,
                crossSceneCorrect = 7, // 7/10 = 70% (exactly at threshold)
                crossSceneTotal = 10,
            )

        // When
        val result = progress.isMastered()

        // Then
        assertTrue(result)
    }
}
