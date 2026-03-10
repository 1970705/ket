package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class MatchGameConfig(
    val wordPairs: Int = 10,
    @Contextual val bubbleSize: Dp = 80.dp,
    val columns: Int = 6,
    val enableTimer: Boolean = true,
    val enableSound: Boolean = true,
    val enableAnimation: Boolean = true,
    val islandId: String? = null,
    val levelId: String? = null,
) {
    companion object {
        const val MIN_PAIRS = 5
        const val MAX_PAIRS = 50
    }

    init {
        require(wordPairs in MIN_PAIRS..MAX_PAIRS) {
            "wordPairs must be between $MIN_PAIRS and $MAX_PAIRS, but was $wordPairs"
        }
        require(columns > 0) { "columns must be positive" }
        require(bubbleSize.value > 0) { "bubbleSize must be positive" }
    }

    val totalBubbles: Int
        get() = wordPairs * 2
}
