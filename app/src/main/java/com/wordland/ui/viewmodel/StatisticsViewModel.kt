package com.wordland.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.ui.uistate.GameHistoryUiState
import com.wordland.ui.uistate.StatisticsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Statistics Screen
 *
 * Manages state for the main statistics screen with tabs for
 * game history, level statistics, and achievements.
 *
 * This is a stub implementation for compilation.
 * Full implementation will be done by compose-ui-designer.
 */
class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _gameHistoryState = MutableStateFlow<GameHistoryUiState>(GameHistoryUiState.Loading)
    val gameHistoryState: StateFlow<GameHistoryUiState> = _gameHistoryState.asStateFlow()

    /**
     * Load statistics for the current user
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading

            // Stub implementation - return default stats
            _uiState.value =
                StatisticsUiState.Ready(
                    globalStats =
                        com.wordland.ui.uistate.GlobalStatsUiModel(
                            totalGames = 0,
                            totalScore = 0,
                            totalStudyTime = 0,
                            currentStreak = 0,
                            longestStreak = 0,
                            totalWordsMastered = 0,
                            totalLevelsCompleted = 0,
                        ),
                )
        }
    }

    /**
     * Load game history
     */
    fun loadGameHistory(filter: com.wordland.domain.model.statistics.GameMode? = null) {
        viewModelScope.launch {
            _gameHistoryState.value = GameHistoryUiState.Loading
            // Stub implementation
            _gameHistoryState.value =
                GameHistoryUiState.Ready(
                    history = emptyList(),
                    filter = filter,
                    hasMore = false,
                )
        }
    }

    /**
     * Refresh statistics
     */
    fun refresh() {
        loadStatistics()
    }

    /**
     * Clear errors
     */
    fun clearError() {
        // No-op in stub
    }
}
