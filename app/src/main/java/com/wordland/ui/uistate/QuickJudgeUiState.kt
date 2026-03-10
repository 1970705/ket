package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.QuickJudgeQuestion
import com.wordland.domain.model.QuickJudgeResult

/**
 * UI state for Quick Judge screen
 *
 * Quick Judge is a speed-based game mode where users judge if translations are correct.
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class QuickJudgeUiState {
    @Immutable
    object Loading : QuickJudgeUiState()

    @Immutable
    data class Ready(
        val question: QuickJudgeQuestion,
        val currentQuestionIndex: Int = 0,
        val totalQuestions: Int = 6,
        val comboState: ComboState = ComboState(),
        val currentScore: Int = 0,
    ) : QuickJudgeUiState()

    @Immutable
    data class Feedback(
        val result: QuickJudgeResult,
        val progress: Float,
        val comboState: ComboState = ComboState(),
        val currentScore: Int = 0,
    ) : QuickJudgeUiState()

    @Immutable
    data class LevelComplete(
        val stars: Int,
        val score: Int,
        val correctCount: Int,
        val totalQuestions: Int,
        val avgTimePerQuestion: Long,
        val maxCombo: Int = 0,
        val isNextLevelUnlocked: Boolean = false,
    ) : QuickJudgeUiState()

    @Immutable
    data class TimeUp(
        val correctAnswer: Boolean,
        val translation: String,
        val progress: Float,
    ) : QuickJudgeUiState()

    @Immutable
    data class Error(val message: String) : QuickJudgeUiState()
}
