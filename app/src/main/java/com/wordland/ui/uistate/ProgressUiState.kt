package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.wordland.domain.model.UserWordProgress

/**
 * UI state for progress screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class ProgressUiState {
    @Immutable
    object Loading : ProgressUiState()

    @Immutable
    data class Success(val progress: List<UserWordProgress>) : ProgressUiState()

    @Immutable
    data class Error(val message: String) : ProgressUiState()
}
