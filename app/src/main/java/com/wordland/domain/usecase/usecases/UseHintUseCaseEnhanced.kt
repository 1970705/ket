package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.hint.HintManager
import com.wordland.domain.model.BehaviorTracking
import com.wordland.domain.model.Result
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced UseCase for using hints during learning
 * Features:
 * - Multi-level hint generation
 * - Hint usage tracking and limitations
 * - Prevention of hint abuse
 * - Integration with scoring penalties
 */
@Singleton
class UseHintUseCaseEnhanced
    @Inject
    constructor(
        private val trackingRepository: TrackingRepository,
        private val wordRepository: WordRepository,
        private val hintGenerator: HintGenerator,
        private val hintManager: HintManager,
        private val behaviorAnalyzer: BehaviorAnalyzer,
    ) {
        /**
         * Request to use a hint for a word
         * @param userId User ID
         * @param wordId Word ID
         * @param levelId Level ID
         * @return Result containing HintResult with hint text and metadata
         */
        suspend operator fun invoke(
            userId: String,
            wordId: String,
            levelId: String,
        ): Result<HintResult> {
            return try {
                // Check if hint can be used
                val (canUse, reason) = hintManager.canUseHint(wordId)
                if (!canUse) {
                    return Result.Error(HintLimitException(reason ?: "提示不可用"))
                }

                // Get word information
                val word =
                    wordRepository.getWordById(wordId)
                        ?: return Result.Error(IllegalStateException("Word not found: $wordId"))

                // Record hint usage and get new hint level
                val hintLevel = hintManager.useHint(wordId)

                // Generate hint based on level
                val hintText =
                    hintGenerator.generateAdaptiveHint(
                        word = word.word,
                        hintCount = hintLevel,
                    )

                // Track behavior
                val tracking =
                    BehaviorTracking(
                        userId = userId,
                        wordId = wordId,
                        sceneId = levelId,
                        action = "hint_used",
                        isCorrect = null,
                        responseTime = null,
                        difficulty = word.difficulty,
                        hintUsed = true,
                        isNewWord = null,
                        timestamp = System.currentTimeMillis(),
                    )
                trackingRepository.insertTracking(tracking)

                // Get remaining hints
                val (used, remaining, total) = hintManager.getHintStats(wordId)

                Result.Success(
                    HintResult(
                        hintText = hintText,
                        hintLevel = hintLevel,
                        hintsRemaining = remaining,
                        hintsUsed = used,
                        totalHints = total,
                        word = word.word,
                        shouldApplyPenalty = true, // Hint usage always affects scoring
                    ),
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        /**
         * Get current hint availability for a word
         * @param wordId Word ID
         * @return Pair of (canUse: Boolean, reason: String?)
         */
        fun canUseHint(wordId: String): Pair<Boolean, String?> {
            return hintManager.canUseHint(wordId)
        }

        /**
         * Get hint statistics for a word
         * @param wordId Word ID
         * @return Hint statistics
         */
        fun getHintStats(wordId: String): HintStats? {
            val (used, remaining, total) = hintManager.getHintStats(wordId)
            val currentLevel = hintManager.getCurrentHintLevel(wordId)
            val dependencyScore = hintManager.getHintDependencyScore(wordId)

            return HintStats(
                wordId = wordId,
                currentLevel = currentLevel,
                hintsUsed = used,
                hintsRemaining = remaining,
                totalHints = total,
                dependencyScore = dependencyScore,
                isOverusing = hintManager.isOverusingHints(wordId),
            )
        }

        /**
         * Reset hints for a word (e.g., when starting a new attempt)
         * @param wordId Word ID
         */
        fun resetHints(wordId: String) {
            hintManager.resetHints(wordId)
        }

        /**
         * Configure hint settings for difficulty level
         * @param difficulty Word difficulty (1-5)
         */
        fun configureForDifficulty(difficulty: Int) {
            hintManager.setMaxHintsForDifficulty(difficulty)
        }
    }

/**
 * Result of using a hint
 */
data class HintResult(
    val hintText: String, // The generated hint text
    val hintLevel: Int, // Current hint level (1-3)
    val hintsRemaining: Int, // Hints still available
    val hintsUsed: Int, // Total hints used
    val totalHints: Int, // Total hint allowance
    val word: String, // The target word (for validation)
    val shouldApplyPenalty: Boolean, // Whether scoring penalty should apply
)

/**
 * Hint statistics for a word
 */
data class HintStats(
    val wordId: String,
    val currentLevel: Int, // Current hint level (0-3)
    val hintsUsed: Int, // Number of hints used
    val hintsRemaining: Int, // Hints still available
    val totalHints: Int, // Total hint allowance
    val dependencyScore: Float, // 0.0-1.0, higher = more dependent
    val isOverusing: Boolean, // True if showing dependency pattern
)

/**
 * Exception thrown when hint limit is reached
 */
class HintLimitException(message: String) : Exception(message)
