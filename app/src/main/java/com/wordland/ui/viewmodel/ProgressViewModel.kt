package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.usecase.usecases.GetUserProgressUseCase
import com.wordland.ui.uistate.ProgressUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for progress screen
 * Displays user's overall learning progress
 */
@HiltViewModel
class ProgressViewModel
    @Inject
    constructor(
        private val getUserProgress: GetUserProgressUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
        val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

        /**
         * Load user progress data
         * @param userId User ID
         */
        fun loadProgress(userId: String) {
            viewModelScope.launch {
                _uiState.value = ProgressUiState.Loading

                getUserProgress(userId)
                    .catch { exception ->
                        _uiState.value =
                            ProgressUiState.Error(
                                message = exception.message ?: "Failed to load progress",
                            )
                    }
                    .collect { progressList ->
                        _uiState.value = ProgressUiState.Success(progress = progressList)
                    }
            }
        }
    }
