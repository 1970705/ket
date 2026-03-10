package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * UI state for home screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class HomeUiState {
    @Immutable
    object Loading : HomeUiState()

    @Immutable
    data class Success(val data: HomeData) : HomeUiState()

    @Immutable
    data class Error(val message: String) : HomeUiState()
}

/**
 * Home screen data
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class HomeData(
    val userName: String = "Player",
    val totalProgress: Int = 0,
)
