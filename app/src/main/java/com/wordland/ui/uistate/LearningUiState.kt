package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.LearnWordResult
import com.wordland.domain.model.SpellBattleQuestion

/**
 * UI state for learning screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class LearningUiState {
    @Immutable
    object Loading : LearningUiState()

    @Immutable
    data class Ready(
        val question: SpellBattleQuestion,
        val hintAvailable: Boolean,
        val hintShown: Boolean,
        val hintText: String? = null,
        val hintLevel: Int = 0,
        val hintsRemaining: Int = 3,
        val hintPenaltyApplied: Boolean = false,
        val comboState: ComboState = ComboState(),
        val currentWordIndex: Int = 0,
        val totalWords: Int = 6,
    ) : LearningUiState()

    @Immutable
    data class Feedback(
        val result: LearnWordResult,
        val stars: Int,
        val progress: Float,
        val comboState: ComboState = ComboState(),
    ) : LearningUiState()

    @Immutable
    data class LevelComplete(
        val stars: Int,
        val score: Int,
        val isNextIslandUnlocked: Boolean,
        val islandMasteryPercentage: Double,
        val maxCombo: Int = 0,
        // Performance breakdown data for StarBreakdownScreen
        val accuracy: Int = 0, // Accuracy percentage (0-100)
        val hintsUsed: Int = 0, // Total hints used
        val timeTaken: Int = 0, // Total time in seconds
        val errorCount: Int = 0, // Total wrong answers
    ) : LearningUiState()

    @Immutable
    data class Error(val message: String) : LearningUiState()
}
