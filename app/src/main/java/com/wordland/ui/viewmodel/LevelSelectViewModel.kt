package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.usecase.usecases.GetLevelsUseCase
import com.wordland.ui.uistate.LevelSelectUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for level select screen
 * Refactored to use UseCases
 */
@HiltViewModel
class LevelSelectViewModel
    @Inject
    constructor(
        private val getLevels: GetLevelsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LevelSelectUiState>(LevelSelectUiState.Loading)
        val uiState: StateFlow<LevelSelectUiState> = _uiState.asStateFlow()

        private val userId: String = "user_001" // TODO: Get from user session

        /**
         * Load levels for an island
         * @param islandId The island ID to load levels for
         */
        fun loadLevels(islandId: String) {
            viewModelScope.launch {
                _uiState.value = LevelSelectUiState.Loading

                try {
                    getLevels(islandId, userId).collect { levels ->
                        _uiState.value = LevelSelectUiState.Success(levels)
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        LevelSelectUiState.Error(
                            e.message ?: "Failed to load levels",
                        )
                }
            }
        }

        /**
         * Refresh levels
         */
        fun refresh(islandId: String) {
            loadLevels(islandId)
        }
    }
