package com.wordland.ui.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * UI state for island map screen
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class IslandMapUiState {
    @Immutable
    object Loading : IslandMapUiState()

    @Immutable
    data class Success(val islands: List<IslandInfo>) : IslandMapUiState()

    @Immutable
    data class Error(val message: String) : IslandMapUiState()
}

/**
 * Island info for UI display
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class IslandInfo(
    val id: String,
    val name: String,
    val color: androidx.compose.ui.graphics.Color,
    val masteryPercentage: Float,
    val isUnlocked: Boolean,
)
