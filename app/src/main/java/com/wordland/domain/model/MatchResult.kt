package com.wordland.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class MatchResult {
    data object Success : MatchResult()

    data object Failed : MatchResult()

    data object Invalid : MatchResult()
}
