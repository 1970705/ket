package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class BubbleColor(val colorValue: ULong) {
    PINK(0xFFFFB6C1u),
    GREEN(0xFF90EE90u),
    PURPLE(0xFFDDA0DDu),
    ORANGE(0xFFFFA500u),
    BROWN(0xFFD2691Eu),
    BLUE(0xFF87CEEBu),
    ;

    companion object {
        fun random(): BubbleColor = entries.random()
    }
}

@Immutable
@Serializable
data class BubbleState(
    val id: String,
    val word: String,
    val pairId: String,
    val isSelected: Boolean = false,
    val isMatched: Boolean = false,
    val color: BubbleColor = BubbleColor.random(),
) {
    fun canMatchWith(other: BubbleState): Boolean {
        return !isMatched &&
            !other.isMatched &&
            pairId == other.pairId &&
            id != other.id
    }

    fun select(): BubbleState = copy(isSelected = true)

    fun deselect(): BubbleState = copy(isSelected = false)

    fun markAsMatched(): BubbleState = copy(isMatched = true, isSelected = false)
}
