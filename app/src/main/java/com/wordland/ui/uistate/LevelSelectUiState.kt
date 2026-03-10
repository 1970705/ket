package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * UI state for level select screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class LevelSelectUiState {
    @Immutable
    object Loading : LevelSelectUiState()

    @Immutable
    data class Success(val levels: List<com.wordland.domain.model.LevelProgress>) : LevelSelectUiState()

    @Immutable
    data class Error(val message: String) : LevelSelectUiState()
}
