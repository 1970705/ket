package com.wordland.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class GameAction {
    data class SelectBubble(val bubbleId: String) : GameAction()

    data object StartGame : GameAction()

    data object PauseGame : GameAction()

    data object ResumeGame : GameAction()

    data object ExitGame : GameAction()

    data object ResetGame : GameAction()

    data object RestartGame : GameAction()
}
