package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * UI state for review screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class ReviewUiState {
    @Immutable
    object Loading : ReviewUiState()

    @Immutable
    data class Ready(
        val hasWords: Boolean,
        val totalDue: Int,
        val totalLearning: Int,
    ) : ReviewUiState()

    @Immutable
    data class Error(val message: String) : ReviewUiState()
}
