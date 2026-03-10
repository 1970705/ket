package com.wordland.domain.usecase.usecases.quickjudge

import com.wordland.domain.model.ComboState
import com.wordland.domain.usecase.usecases.SubmitQuickJudgeAnswerUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Static method tests for SubmitQuickJudgeAnswerUseCase.
 * Tests calculateStars and calculateCombo utility methods.
 */
class SubmitQuickJudgeAnswerStaticTest {
    // === calculateStars Tests ===

    @Test
    fun `calculateStars returns 3 for perfect performance`() {
        // Given: 90%+ correct, very fast
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 9,
                totalCount = 10,
                avgTimeTaken = 1500L, // 1.5s average (very fast)
                maxTimeLimit = 5, // 5 second limit
            )

        // Then
        assertEquals(3, stars)
    }

    @Test
    fun `calculateStars returns 2 for good performance`() {
        // Given: 80%+ correct
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 8,
                totalCount = 10,
                avgTimeTaken = 3000L,
                maxTimeLimit = 5,
            )

        // Then
        assertEquals(2, stars)
    }

    @Test
    fun `calculateStars returns 1 for passing performance`() {
        // Given: 60%+ correct
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 6,
                totalCount = 10,
                avgTimeTaken = 4000L,
                maxTimeLimit = 5,
            )

        // Then
        assertEquals(1, stars)
    }

    @Test
    fun `calculateStars returns 0 for poor performance`() {
        // Given: less than 60% correct
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 3,
                totalCount = 10,
                avgTimeTaken = 4000L,
                maxTimeLimit = 5,
            )

        // Then
        assertEquals(0, stars)
    }

    @Test
    fun `calculateStars returns 0 for empty level`() {
        // Given: no questions
        val stars =
            SubmitQuickJudgeAnswerUseCase.calculateStars(
                correctCount = 0,
                totalCount = 0,
                avgTimeTaken = 0L,
                maxTimeLimit = 5,
            )

        // Then
        assertEquals(0, stars)
    }

    // === calculateCombo Tests ===

    @Test
    fun `calculateCombo increments on correct answer`() {
        // Given
        val previousState = ComboState(consecutiveCorrect = 2)

        // When
        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = true,
                timeTakenMs = 3000L,
            )

        // Then
        assertEquals(3, newState.consecutiveCorrect)
    }

    @Test
    fun `calculateCombo resets on wrong answer`() {
        // Given
        val previousState = ComboState(consecutiveCorrect = 5)

        // When
        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = false,
                timeTakenMs = 1000L,
            )

        // Then
        assertEquals(0, newState.consecutiveCorrect)
    }

    @Test
    fun `calculateCombo ignores guessing answers`() {
        // Given - very fast answer (likely guessing)
        val previousState = ComboState(consecutiveCorrect = 2)
        val guessingTime = 500L // Too fast for 4-letter word

        // When
        val newState =
            SubmitQuickJudgeAnswerUseCase.calculateCombo(
                previousState = previousState,
                isCorrect = true,
                timeTakenMs = guessingTime,
            )

        // Then - combo should not increment due to anti-guessing
        assertEquals(2, newState.consecutiveCorrect)
    }
}
