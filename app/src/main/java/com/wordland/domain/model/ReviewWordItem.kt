package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Data class representing a word item in the review queue
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class ReviewWordItem(
    val word: Word,
    val memoryStrength: Int,
    val lastReviewTime: Long?,
    val correctRate: Float,
)
