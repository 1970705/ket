package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckMatchUseCase
    @Inject
    constructor() {
        operator fun invoke(
            first: BubbleState?,
            second: BubbleState?,
        ): MatchResult {
            if (first == null || second == null) {
                return MatchResult.Invalid
            }

            if (first.id == second.id) {
                return MatchResult.Invalid
            }

            if (first.isMatched || second.isMatched) {
                return MatchResult.Invalid
            }

            return if (first.pairId == second.pairId) {
                MatchResult.Success
            } else {
                MatchResult.Failed
            }
        }

        fun canSelectBubble(bubble: BubbleState?): Boolean {
            return bubble != null && !bubble.isMatched && !bubble.isSelected
        }

        fun isGameCompleted(bubbles: List<BubbleState>): Boolean {
            return bubbles.all { it.isMatched }
        }
    }
