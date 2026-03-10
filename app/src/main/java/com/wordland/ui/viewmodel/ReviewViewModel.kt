package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.Result
import com.wordland.domain.model.ReviewWordItem
import com.wordland.domain.usecase.usecases.GetReviewWordsUseCase
import com.wordland.ui.uistate.ReviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for review screen
 * Manages review word list and loading state
 */
@HiltViewModel
class ReviewViewModel
    @Inject
    constructor(
        private val getReviewWords: GetReviewWordsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
        val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

        private val _reviewWords = MutableStateFlow<List<ReviewWordItem>>(emptyList())
        val reviewWords: StateFlow<List<ReviewWordItem>> = _reviewWords.asStateFlow()

        private val _learningWords = MutableStateFlow<List<ReviewWordItem>>(emptyList())
        val learningWords: StateFlow<List<ReviewWordItem>> = _learningWords.asStateFlow()

        /**
         * Load review words for the user
         * @param userId User ID
         * @param limit Maximum number of words to return
         */
        fun loadReviewWords(
            userId: String,
            limit: Int = 20,
        ) {
            viewModelScope.launch {
                _uiState.value = ReviewUiState.Loading

                when (val result = getReviewWords(userId, limit)) {
                    is Result.Success -> {
                        val words = result.data

                        // Split into review words (due) and learning words (low strength)
                        val now = System.currentTimeMillis()
                        val dueWords =
                            words.filter {
                                it.lastReviewTime != null && it.lastReviewTime!! <= now
                            }
                        val learningWords =
                            words.filter {
                                it.lastReviewTime == null || it.lastReviewTime!! > now
                            }

                        _reviewWords.value = dueWords
                        _learningWords.value = learningWords

                        _uiState.value =
                            ReviewUiState.Ready(
                                hasWords = words.isNotEmpty(),
                                totalDue = dueWords.size,
                                totalLearning = learningWords.size,
                            )
                    }

                    is Result.Error -> {
                        _uiState.value =
                            ReviewUiState.Error(
                                message = result.exception.message ?: "Failed to load review words",
                            )
                    }

                    else -> {
                        // Loading state already set above
                    }
                }
            }
        }
    }
