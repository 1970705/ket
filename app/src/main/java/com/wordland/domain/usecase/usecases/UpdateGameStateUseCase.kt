package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.GameAction
import com.wordland.domain.model.MatchGameState
import com.wordland.domain.model.MatchResult
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateGameStateUseCase
    @Inject
    constructor(
        private val checkMatchUseCase: CheckMatchUseCase,
    ) {
        operator fun invoke(
            currentState: MatchGameState,
            action: GameAction,
            checkMatchResult: MatchResult? = null,
        ): MatchGameState {
            return when (action) {
                is GameAction.StartGame -> handleStartGame(currentState)
                is GameAction.SelectBubble -> handleSelectBubble(currentState, action.bubbleId)
                is GameAction.PauseGame -> handlePauseGame(currentState)
                is GameAction.ResumeGame -> handleResumeGame(currentState)
                is GameAction.ExitGame -> MatchGameState.GameOver
                is GameAction.ResetGame -> MatchGameState.Idle
                is GameAction.RestartGame -> handleRestartGame(currentState)
            }
        }

        private fun handleStartGame(currentState: MatchGameState): MatchGameState {
            return when (currentState) {
                is MatchGameState.Ready -> {
                    MatchGameState.Playing(
                        bubbles = currentState.bubbles,
                        selectedBubbleIds = emptySet(),
                        matchedPairs = 0,
                        startTime = currentTimeMillis(),
                    )
                }
                else -> currentState
            }
        }

        private fun handleSelectBubble(
            currentState: MatchGameState,
            bubbleId: String,
        ): MatchGameState {
            if (currentState !is MatchGameState.Playing) {
                return currentState
            }

            val bubbles = currentState.bubbles
            val clickedBubble = bubbles.find { it.id == bubbleId }

            if (!checkMatchUseCase.canSelectBubble(clickedBubble)) {
                return currentState
            }

            val selectedIds = currentState.selectedBubbleIds

            // Toggle selection - O(1) with Set
            val newSelectedIds =
                if (bubbleId in selectedIds) {
                    selectedIds - bubbleId
                } else {
                    if (selectedIds.size < 2) {
                        selectedIds + bubbleId
                    } else {
                        selectedIds
                    }
                }

            return if (newSelectedIds.size == 2) {
                checkMatchAndUpdate(currentState, newSelectedIds)
            } else {
                // Determine if bubble should be selected or deselected
                val shouldSelect = bubbleId !in selectedIds
                currentState.copy(
                    bubbles =
                        bubbles.map { bubble ->
                            if (bubble.id == bubbleId) {
                                if (shouldSelect) bubble.select() else bubble.deselect()
                            } else {
                                bubble
                            }
                        },
                    selectedBubbleIds = newSelectedIds,
                )
            }
        }

        private fun checkMatchAndUpdate(
            currentState: MatchGameState.Playing,
            selectedIds: Set<String>,
        ): MatchGameState {
            val bubbles = currentState.bubbles
            val first = bubbles.find { it.id == selectedIds.first() }
            val second = bubbles.find { it.id == selectedIds.last() }

            val matchResult = checkMatchUseCase(first, second)

            return when (matchResult) {
                is MatchResult.Success -> {
                    val updatedBubbles =
                        bubbles.map { bubble ->
                            when (bubble.id) {
                                first?.id, second?.id -> bubble.markAsMatched()
                                else -> bubble.deselect()
                            }
                        }

                    val newMatchedPairs = currentState.matchedPairs + 1
                    val totalPairs = bubbles.size / 2

                    if (newMatchedPairs >= totalPairs) {
                        MatchGameState.Completed(
                            elapsedTime = currentState.elapsedTime,
                            pairs = newMatchedPairs,
                        )
                    } else {
                        currentState.copy(
                            bubbles = updatedBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = newMatchedPairs,
                        )
                    }
                }
                is MatchResult.Failed -> {
                    currentState.copy(
                        bubbles = bubbles.map { it.deselect() },
                        selectedBubbleIds = emptySet(),
                    )
                }
                else -> {
                    currentState.copy(
                        bubbles = bubbles.map { it.deselect() },
                        selectedBubbleIds = emptySet(),
                    )
                }
            }
        }

        private fun handlePauseGame(currentState: MatchGameState): MatchGameState {
            return when (currentState) {
                is MatchGameState.Playing -> {
                    val updatedState =
                        currentState.copy(
                            elapsedTime = currentState.elapsedTime + (currentTimeMillis() - currentState.startTime),
                        )
                    MatchGameState.Paused(previousState = updatedState)
                }
                else -> currentState
            }
        }

        private fun handleResumeGame(currentState: MatchGameState): MatchGameState {
            return when (currentState) {
                is MatchGameState.Paused -> {
                    currentState.previousState.copy(
                        startTime = currentTimeMillis(),
                    )
                }
                else -> currentState
            }
        }

        private fun handleRestartGame(currentState: MatchGameState): MatchGameState {
            return when (currentState) {
                is MatchGameState.Ready, is MatchGameState.Playing, is MatchGameState.Paused -> {
                    MatchGameState.Idle
                }
                else -> currentState
            }
        }
    }
