package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.MatchGameConfig
import com.wordland.domain.model.MatchGameState
import com.wordland.domain.model.MatchResult
import com.wordland.domain.model.Result
import com.wordland.domain.usecase.usecases.CheckMatchUseCase
import com.wordland.domain.usecase.usecases.GetWordPairsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 单词消消乐游戏ViewModel
 *
 * Responsibilities:
 * - 管理游戏状态（MatchGameState）
 * - 处理用户交互（点击泡泡、暂停、继续）
 * - 调用UseCases执行业务逻辑
 * - 提供UI状态
 */
@HiltViewModel
class MatchGameViewModel
    @Inject
    constructor(
        private val getWordPairsUseCase: GetWordPairsUseCase,
        private val checkMatchUseCase: CheckMatchUseCase,
    ) : ViewModel() {
        // Game state
        private val _gameState = MutableStateFlow<MatchGameState>(MatchGameState.Idle)
        val gameState: StateFlow<MatchGameState> = _gameState.asStateFlow()

        // UI state
        private val _uiState = MutableStateFlow<MatchGameUiState>(MatchGameUiState.Loading)
        val uiState: StateFlow<MatchGameUiState> = _uiState.asStateFlow()

        // Exit dialog state
        var showExitDialog: Boolean = false
            private set

        // Current level/island IDs
        private var currentLevelId: String = ""
        private var currentIslandId: String = ""

        /**
         * Initialize game with level and island IDs
         */
        fun initializeGame(
            levelId: String,
            islandId: String,
        ) {
            currentLevelId = levelId
            currentIslandId = islandId
            showExitDialog = false

            viewModelScope.launch {
                try {
                    _gameState.value = MatchGameState.Preparing

                    // Get word pairs for this level
                    val config =
                        MatchGameConfig(
                            wordPairs = 6,
                            levelId = levelId,
                            islandId = islandId,
                        )
                    val result = getWordPairsUseCase(config)

                    when (result) {
                        is Result.Success -> {
                            val bubbles = result.data
                            _gameState.value =
                                MatchGameState.Ready(
                                    pairs = bubbles.size / 2,
                                    bubbles = bubbles,
                                )
                            _uiState.value = MatchGameUiState.Ready
                        }
                        is Result.Error -> {
                            _gameState.value =
                                MatchGameState.Error(
                                    result.exception.message ?: "Unknown error",
                                )
                            _uiState.value =
                                MatchGameUiState.Error(
                                    result.exception.message ?: "Unknown error",
                                )
                        }
                        is Result.Loading -> {
                            // Should not happen since we're already in Preparing state
                        }
                    }
                } catch (e: Exception) {
                    _gameState.value = MatchGameState.Error(e.message ?: "Unknown error")
                    _uiState.value = MatchGameUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        /**
         * Start the game
         */
        fun startGame() {
            val currentState = _gameState.value
            if (currentState is MatchGameState.Ready) {
                _gameState.value =
                    MatchGameState.Playing(
                        bubbles = currentState.bubbles,
                        selectedBubbleIds = emptySet(),
                        matchedPairs = 0,
                        startTime = System.currentTimeMillis(),
                    )
                _uiState.value = MatchGameUiState.Playing
            }
        }

        /**
         * Select a bubble - optimized for performance
         */
        fun selectBubble(bubbleId: String) {
            val currentState = _gameState.value
            if (currentState !is MatchGameState.Playing) return

            // Find the bubble without coroutine overhead for instant feedback
            val selectedBubble = currentState.bubbles.firstOrNull { it.id == bubbleId }
            if (selectedBubble == null || selectedBubble.isMatched) return

            // Toggle selection - O(1) with Set
            val newSelectedIds =
                if (bubbleId in currentState.selectedBubbleIds) {
                    currentState.selectedBubbleIds - bubbleId
                } else {
                    // Only allow up to 2 selections
                    if (currentState.selectedBubbleIds.size < 2) {
                        currentState.selectedBubbleIds + bubbleId
                    } else {
                        currentState.selectedBubbleIds
                    }
                }

            // Check if selection actually changed (avoid unnecessary state update)
            if (newSelectedIds == currentState.selectedBubbleIds) return

            // Optimized: Only update affected bubbles, not entire list
            val updatedState =
                currentState.copy(
                    bubbles =
                        currentState.bubbles.map { bubble ->
                            // Only copy if this bubble's selection state changed
                            val wasSelected = bubble.id in currentState.selectedBubbleIds
                            val isSelected = bubble.id in newSelectedIds
                            if (wasSelected != isSelected) {
                                bubble.copy(isSelected = isSelected)
                            } else {
                                bubble
                            }
                        },
                    selectedBubbleIds = newSelectedIds,
                )

            _gameState.value = updatedState

            // If 2 bubbles selected, check for match
            if (newSelectedIds.size == 2) {
                viewModelScope.launch { checkForMatch(updatedState) }
            }
        }

        /**
         * Check if selected bubbles match - optimized for performance
         */
        private suspend fun checkForMatch(state: MatchGameState.Playing) {
            if (state.selectedBubbleIds.size != 2) return

            val ids = state.selectedBubbleIds.iterator()
            val id1 = ids.next()
            val id2 = ids.next()

            val bubble1 = state.bubbles.firstOrNull { it.id == id1 }
            val bubble2 = state.bubbles.firstOrNull { it.id == id2 }

            val matchResult = checkMatchUseCase(bubble1, bubble2)

            when (matchResult) {
                is MatchResult.Success -> {
                    // Match found! Mark only matched bubbles
                    val newBubbles =
                        state.bubbles.map { bubble ->
                            if (bubble.id in state.selectedBubbleIds) {
                                bubble.copy(
                                    isSelected = false,
                                    isMatched = true,
                                )
                            } else {
                                bubble
                            }
                        }

                    val newMatchedPairs = state.matchedPairs + 1

                    // Check if game is completed before updating state
                    val isCompleted = newMatchedPairs >= (state.bubbles.size / 2)

                    _gameState.value =
                        if (isCompleted) {
                            val elapsedTime = System.currentTimeMillis() - state.startTime
                            MatchGameState.Completed(
                                elapsedTime = elapsedTime,
                                pairs = newMatchedPairs,
                                accuracy = calculateAccuracy(newMatchedPairs),
                            )
                        } else {
                            state.copy(
                                bubbles = newBubbles,
                                selectedBubbleIds = emptySet(),
                                matchedPairs = newMatchedPairs,
                            )
                        }

                    _uiState.value = if (isCompleted) MatchGameUiState.Completed else MatchGameUiState.Playing
                }
                is MatchResult.Failed -> {
                    // No match - clear selection after a short delay (non-blocking)
                    kotlinx.coroutines.delay(500)
                    val clearedState =
                        state.copy(
                            bubbles = state.bubbles.map { it.copy(isSelected = false) },
                            selectedBubbleIds = emptySet(),
                        )
                    _gameState.value = clearedState
                }
                is MatchResult.Invalid -> {
                    // Invalid selection - just clear immediately
                    _gameState.value =
                        state.copy(
                            bubbles = state.bubbles.map { it.copy(isSelected = false) },
                            selectedBubbleIds = emptySet(),
                        )
                }
            }
        }

        /**
         * Pause the game
         */
        fun pauseGame() {
            val currentState = _gameState.value
            if (currentState is MatchGameState.Playing) {
                _gameState.value = MatchGameState.Paused(currentState)
                _uiState.value = MatchGameUiState.Paused
            }
        }

        /**
         * Resume the game
         */
        fun resumeGame() {
            val currentState = _gameState.value
            if (currentState is MatchGameState.Paused) {
                // Adjust start time to account for pause duration
                val adjustedState =
                    currentState.previousState.copy(
                        startTime = System.currentTimeMillis() - currentState.previousState.elapsedTime,
                    )
                _gameState.value = adjustedState
                _uiState.value = MatchGameUiState.Playing
            }
        }

        /**
         * Show exit confirmation dialog
         */
        fun showExitConfirmation() {
            showExitDialog = true
        }

        /**
         * Hide exit confirmation dialog
         */
        fun hideExitConfirmation() {
            showExitDialog = false
        }

        /**
         * Calculate accuracy (correct matches / total attempts)
         * TODO: Track wrong attempts for proper accuracy calculation
         */
        private fun calculateAccuracy(matchedPairs: Int): Float {
            // For now, return perfect accuracy since we don't track wrong attempts yet
            return 1.0f
        }
    }

/**
 * UI state for MatchGameScreen
 */
sealed class MatchGameUiState {
    object Loading : MatchGameUiState()

    object Ready : MatchGameUiState()

    object Playing : MatchGameUiState()

    object Paused : MatchGameUiState()

    object Completed : MatchGameUiState()

    data class Error(val message: String) : MatchGameUiState()
}
