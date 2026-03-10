package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Result of learning a word
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class LearnWordResult(
    val word: Word,
    val isCorrect: Boolean,
    val message: String,
    val newMemoryStrength: Int,
    val timeTaken: Long,
    val hintUsed: Boolean,
)
