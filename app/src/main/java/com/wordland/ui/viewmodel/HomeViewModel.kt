package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.GetIslandsUseCase
import com.wordland.domain.usecase.usecases.GetUserStatsUseCase
import com.wordland.ui.uistate.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for home screen
 * Refactored to use UseCases
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getUserStats: GetUserStatsUseCase,
        private val getIslands: GetIslandsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        private val userId: String = "user_001" // TODO: Get from user session

        init {
            loadHomeData()
        }

        /**
         * Load home screen data
         */
        private fun loadHomeData() {
            viewModelScope.launch {
                _uiState.value = HomeUiState.Loading

                when (val result = getUserStats(userId)) {
                    is Result.Success -> {
                        _uiState.value =
                            HomeUiState.Success(
                                data =
                                    com.wordland.ui.uistate.HomeData(
                                        userName = "Player",
                                        totalProgress = result.data.completedLevels,
                                    ),
                            )
                    }
                    is Result.Error -> {
                        _uiState.value =
                            HomeUiState.Error(
                                message = result.exception.message ?: "Failed to load data",
                            )
                    }
                    is Result.Loading -> {
                        // Already loading
                    }
                }
            }
        }

        /**
         * Refresh home data
         */
        fun refresh() {
            loadHomeData()
        }
    }
