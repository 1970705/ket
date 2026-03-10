package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
sealed class MatchGameState {
    @Immutable
    @Serializable
    data object Idle : MatchGameState()

    @Immutable
    @Serializable
    data object Preparing : MatchGameState()

    @Immutable
    @Serializable
    data class Ready(
        val pairs: Int,
        val bubbles: List<BubbleState>,
    ) : MatchGameState()

    @Immutable
    @Serializable
    data class Playing(
        val bubbles: List<BubbleState>,
        val selectedBubbleIds: Set<String> = emptySet(),
        val matchedPairs: Int = 0,
        val elapsedTime: Long = 0L,
        val startTime: Long = java.lang.System.currentTimeMillis(),
    ) : MatchGameState() {
        // Computed progress - cached since it's accessed frequently
        private val totalPairs: Int = bubbles.size / 2

        val progress: Float
            get() = if (totalPairs == 0) 0f else matchedPairs.toFloat() / totalPairs

        // Optimized completion check - early exit when matched count equals total
        val isCompleted: Boolean
            get() = matchedPairs >= totalPairs

        // Helper for backward compatibility
        @Deprecated("Use selectedBubbleIds instead for O(1) lookup", ReplaceWith("selectedBubbleIds"))
        val selectedBubbles: List<String>
            get() = selectedBubbleIds.toList()
    }

    @Immutable
    @Serializable
    data class Paused(
        val previousState: Playing,
    ) : MatchGameState()

    @Immutable
    @Serializable
    data class Completed(
        val elapsedTime: Long,
        val pairs: Int,
        val accuracy: Float = 1.0f,
    ) : MatchGameState()

    @Immutable
    @Serializable
    data object GameOver : MatchGameState()

    @Immutable
    @Serializable
    data class Error(val message: String) : MatchGameState()
}
