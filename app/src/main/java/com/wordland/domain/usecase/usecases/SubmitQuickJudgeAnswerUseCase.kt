package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.combo.ComboManager
import com.wordland.domain.constants.DomainConstants
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.QuickJudgeQuestion
import com.wordland.domain.model.QuickJudgeResult
import com.wordland.domain.model.Result
import javax.inject.Inject

/**
 * UseCase for submitting a Quick Judge answer and updating learning progress
 *
 * Quick Judge is a speed-based game where users judge if a translation is correct.
 * This use case handles:
 * 1. Validating the judgment
 * 2. Calculating time bonus
 * 3. Tracking combo
 * 4. Updating progress and tracking
 */
class SubmitQuickJudgeAnswerUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
        private val progressRepository: ProgressRepository,
        private val trackingRepository: TrackingRepository,
    ) {
        companion object {
            // Base score for correct answer
            private const val BASE_SCORE_CORRECT = 100

            // Score penalty for wrong answer
            private const val SCORE_PENALTY_WRONG = 20

            // Combo bonus per consecutive correct answer
            private const val COMBO_BONUS = 10

            // Maximum combo bonus
            private const val MAX_COMBO_BONUS = 50

            /**
             * Calculate new combo state after Quick Judge answer
             * @param previousState Previous combo state
             * @param isCorrect Whether the judgment was correct
             * @param timeTakenMs Time taken to answer
             * @return New combo state
             */
            fun calculateCombo(
                previousState: ComboState,
                isCorrect: Boolean,
                timeTakenMs: Long,
            ): ComboState {
                return ComboManager.calculateCombo(
                    currentState = previousState,
                    isCorrect = isCorrect,
                    responseTime = timeTakenMs,
                    wordLength = 4, // Average word length for combo calculation
                )
            }

            /**
             * Calculate stars for Quick Judge performance
             * @param correctCount Number of correct answers
             * @param totalCount Total questions
             * @param avgTimeTaken Average time per question
             * @param maxTimeLimit Time limit per question
             * @return Star rating (0-3)
             */
            fun calculateStars(
                correctCount: Int,
                totalCount: Int,
                avgTimeTaken: Long,
                maxTimeLimit: Int,
            ): Int {
                if (totalCount == 0) return 0

                val accuracy = correctCount.toFloat() / totalCount.toFloat()
                val avgTimeRatio = avgTimeTaken.toFloat() / (maxTimeLimit * 1000f)

                return when {
                    accuracy >= 0.9f && avgTimeRatio < 0.5f -> 3 // 90%+ correct, very fast
                    accuracy >= 0.8f -> 2 // 80%+ correct
                    accuracy >= 0.6f -> 1 // 60%+ correct
                    else -> 0
                }
            }
        }

        /**
         * Submit a Quick Judge answer
         * @param userId User ID
         * @param question The question being answered
         * @param userSaidTrue True if user clicked "Correct", False if "Incorrect"
         * @param timeTakenMs Time taken to answer in milliseconds
         * @param levelId Current level ID
         * @param previousComboState Previous combo state
         * @return Result containing QuickJudgeResult with score and combo
         */
        suspend operator fun invoke(
            userId: String,
            question: QuickJudgeQuestion,
            userSaidTrue: Boolean,
            timeTakenMs: Long,
            levelId: String,
            previousComboState: ComboState = ComboState(),
        ): Result<QuickJudgeResult> {
            return try {
                // 1. Validate the judgment
                val isJudgmentCorrect = question.isJudgmentCorrect(userSaidTrue)

                // 2. Calculate scores
                val timeBonus = question.calculateTimeBonus(timeTakenMs)
                val baseScore =
                    if (isJudgmentCorrect) {
                        BASE_SCORE_CORRECT
                    } else {
                        SCORE_PENALTY_WRONG
                    }

                // 3. Get current combo state
                val currentCombo = previousComboState.consecutiveCorrect

                // 4. Calculate combo bonus
                val comboBonus =
                    if (isJudgmentCorrect && currentCombo > 0) {
                        (currentCombo * COMBO_BONUS).coerceAtMost(MAX_COMBO_BONUS)
                    } else {
                        0
                    }

                // 5. Calculate total score
                val totalScore = baseScore + timeBonus + comboBonus

                // 6. Create result
                val result =
                    QuickJudgeResult(
                        question = question,
                        userSaidTrue = userSaidTrue,
                        isCorrect = isJudgmentCorrect,
                        timeTakenMs = timeTakenMs,
                        timeBonus = timeBonus,
                        baseScore = baseScore,
                        totalScore = totalScore,
                    )

                // 7. Update progress if judgment was correct
                if (isJudgmentCorrect) {
                    // Get the word for progress update
                    val word =
                        wordRepository.getWordById(question.wordId)
                            ?: return Result.Error(
                                NoSuchElementException("Word not found: ${question.wordId}"),
                            )

                    // Update word progress (treat as correct practice)
                    val currentProgress = progressRepository.getWordProgress(userId, question.wordId)
                    val currentStrength =
                        currentProgress?.memoryStrength
                            ?: DomainConstants.MEMORY_STRENGTH_INITIAL

                    // Simplified memory strength update for Quick Judge
                    // Quick Judge is recognition-based, so give moderate strength increase
                    val strengthIncrease =
                        when {
                            timeTakenMs < question.timeLimit * 500 -> 5 // Very fast - high confidence
                            timeTakenMs < question.timeLimit * 1000 -> 3 // Fast - good confidence
                            else -> 1 // Normal speed - basic recognition
                        }
                    val newStrength = (currentStrength + strengthIncrease).coerceAtMost(100)

                    progressRepository.updatePracticeResult(
                        userId = userId,
                        wordId = question.wordId,
                        isCorrect = true,
                        responseTime = timeTakenMs,
                        newMemoryStrength = newStrength,
                    )

                    // Record behavior tracking
                    trackingRepository.recordAnswer(
                        userId = userId,
                        wordId = question.wordId,
                        sceneId = levelId,
                        isCorrect = true,
                        responseTime = timeTakenMs,
                        difficulty = question.difficulty,
                        hintUsed = false,
                        isNewWord = currentProgress == null,
                    )
                }

                Result.Success(result)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
