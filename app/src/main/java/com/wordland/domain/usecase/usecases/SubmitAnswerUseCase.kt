package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.TrackingRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.algorithm.GuessingDetector
import com.wordland.domain.algorithm.MemoryStrengthAlgorithm
import com.wordland.domain.combo.ComboManager
import com.wordland.domain.constants.DomainConstants
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Result
import com.wordland.domain.model.SubmitAnswerResult
import javax.inject.Inject

/**
 * UseCase for submitting an answer and updating learning progress
 * This is the core business logic use case that:
 * 1. Validates the answer
 * 2. Detects guessing behavior
 * 3. Calculates combo (anti-guessing, based on thinking time)
 * 4. Calculates memory strength changes
 * 5. Updates progress and tracking
 */
class SubmitAnswerUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
        private val progressRepository: ProgressRepository,
        private val trackingRepository: TrackingRepository,
    ) {
        /**
         * Submit an answer for a word
         * @param userId User ID
         * @param wordId Word ID
         * @param userAnswer User's answer (will be compared with translation)
         * @param responseTime Time taken to answer in milliseconds
         * @param hintUsed Whether the user used a hint
         * @param levelId Current level ID
         * @param previousComboState Previous combo state (for tracking consecutive correct)
         * @return Result containing answer outcome with updated combo state
         */
        suspend operator fun invoke(
            userId: String,
            wordId: String,
            userAnswer: String,
            responseTime: Long,
            hintUsed: Boolean,
            levelId: String,
            previousComboState: ComboState = ComboState(),
        ): Result<SubmitAnswerResult> {
            return try {
                // 1. Get the word to validate answer
                val word =
                    wordRepository.getWordById(wordId)
                        ?: return Result.Error(NoSuchElementException("Word not found: $wordId"))

                // 2. Check answer correctness (case-insensitive, trimmed)
                val trimmedAnswer = userAnswer.trim()
                val isCorrect = trimmedAnswer.equals(word.word, ignoreCase = true)

                // 3. Get current progress
                val currentProgress = progressRepository.getWordProgress(userId, wordId)

                // 4. Detect guessing behavior using recent patterns
                val recentPatterns = trackingRepository.getRecentPatterns(userId, limit = 5)
                // Convert BehaviorTracking to ResponsePattern for guessing detection
                val responsePatterns =
                    recentPatterns.mapNotNull { tracking ->
                        // Skip if responseTime is null (e.g., hint_used actions)
                        if (tracking.responseTime == null) {
                            null
                        } else {
                            GuessingDetector.ResponsePattern(
                                timestamp = tracking.timestamp,
                                responseTime = tracking.responseTime,
                                isCorrect = tracking.isCorrect ?: false,
                                hintUsed = tracking.hintUsed ?: false,
                            )
                        }
                    }
                val isGuessing = GuessingDetector.detectGuessing(responsePatterns)

                // 5. Calculate new memory strength using domain algorithm
                val currentStrength =
                    currentProgress?.memoryStrength
                        ?: DomainConstants.MEMORY_STRENGTH_INITIAL

                android.util.Log.d(
                    "SubmitAnswerUseCase",
                    "invoke: wordId=$wordId, " +
                        "currentProgress=$currentProgress, currentStrength=$currentStrength, " +
                        "isCorrect=$isCorrect, responseTime=$responseTime",
                )

                // Calculate historical accuracy for optimized guessing detection
                val historicalAccuracy =
                    currentProgress?.let { progress ->
                        if (progress.totalAttempts > 0) {
                            progress.correctAttempts.toFloat() / progress.totalAttempts.toFloat()
                        } else {
                            null
                        }
                    }

                val newStrength =
                    MemoryStrengthAlgorithm.calculateNewStrength(
                        currentStrength = currentStrength,
                        isCorrect = isCorrect,
                        isGuessing = isGuessing,
                        wordDifficulty = word.difficulty,
                        historicalAccuracy = historicalAccuracy,
                        hintUsed = hintUsed,
                    )

                android.util.Log.d(
                    "SubmitAnswerUseCase",
                    "invoke: newStrength=$newStrength, " +
                        "isGuessing=$isGuessing, historicalAccuracy=$historicalAccuracy, " +
                        "isMastered=${currentStrength >= 80 && historicalAccuracy != null && historicalAccuracy >= 0.9f}",
                )

                // 6. Update word progress with pre-calculated memory strength
                // This ensures both UI and database use the same value (pattern-based guessing)
                progressRepository.updatePracticeResult(
                    userId = userId,
                    wordId = wordId,
                    isCorrect = isCorrect,
                    responseTime = responseTime,
                    newMemoryStrength = newStrength,
                )

                // 7. Record behavior tracking
                trackingRepository.recordAnswer(
                    userId = userId,
                    wordId = wordId,
                    sceneId = levelId,
                    isCorrect = isCorrect,
                    responseTime = responseTime,
                    difficulty = word.difficulty,
                    hintUsed = hintUsed,
                    isNewWord = currentProgress == null,
                )

                // 8. Calculate stars earned
                val previousAttempts = currentProgress?.totalAttempts ?: 0
                val starsEarned =
                    calculateStars(
                        isCorrect = isCorrect,
                        isGuessing = isGuessing,
                        responseTime = responseTime,
                        hintUsed = hintUsed,
                        wordLength = word.word.length,
                        previousAttempts = previousAttempts,
                    )

                // 9. Calculate new combo state (anti-guessing based on thinking time)
                val newComboState =
                    ComboManager.calculateCombo(
                        currentState = previousComboState,
                        isCorrect = isCorrect,
                        responseTime = responseTime,
                        wordLength = word.word.length,
                    )

                // 10. Generate feedback message
                val message =
                    generateFeedbackMessage(
                        isCorrect = isCorrect,
                        isGuessing = isGuessing,
                        starsEarned = starsEarned,
                        comboMessage = ComboManager.getComboMessage(newComboState.consecutiveCorrect),
                    )

                // 11. Return result with combo state
                Result.Success(
                    SubmitAnswerResult(
                        word = word,
                        isCorrect = isCorrect,
                        newMemoryStrength = newStrength,
                        isGuessing = isGuessing,
                        timeTaken = responseTime,
                        hintUsed = hintUsed,
                        starsEarned = starsEarned,
                        message = message,
                        comboState = newComboState,
                    ),
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        /**
         * Calculate stars earned based on performance
         * Child-friendly star rating:
         * - 0 stars: Incorrect answer
         * - 1 star: Correct but detected as guessing OR hint used on 2nd+ attempt
         * - 2 stars: Correct with hint on 1st attempt OR too fast (potential guessing)
         * - 3 stars: Correct, no hint, adequate thinking time (not guessing) - always 3 stars
         *
         * Child-friendly design:
         * - WITHOUT hint: Always 3 stars for correct answer (encourage learning)
         * - WITH hint: 2 stars on 1st attempt, 1 star on 2nd+ attempt
         */
        private fun calculateStars(
            isCorrect: Boolean,
            isGuessing: Boolean,
            responseTime: Long,
            hintUsed: Boolean,
            wordLength: Int,
            previousAttempts: Int = 0,
        ): Int {
            if (!isCorrect) return 0
            if (isGuessing) return 1

            // Calculate child-friendly threshold based on word length
            val adequateThinkingTime =
                DomainConstants.MIN_RESPONSE_TIME_MS +
                    (wordLength * DomainConstants.MILLIS_PER_LETTER_THRESHOLD)

            // Child-friendly design: Correct answers ALWAYS get at least 2 stars without hint
            // This encourages children to keep trying without feeling punished
            return if (!hintUsed) {
                // Without hint: 3 stars for correct answer (child-friendly, encourage persistence)
                // Note: Even fast answers get 3 stars without hint - we don't penalize fast thinkers
                3
            } else {
                // With hint: 2 stars on 1st attempt, 1 star on 2nd+ attempt
                if (previousAttempts > 0) 1 else 2
            }
        }

        /**
         * Generate feedback message based on performance
         */
        private fun generateFeedbackMessage(
            isCorrect: Boolean,
            isGuessing: Boolean,
            starsEarned: Int,
            comboMessage: String? = null,
        ): String {
            val baseMessage =
                when {
                    !isCorrect -> "不对，继续练习！"
                    isGuessing -> "答对了！但要仔细想想哦～"
                    starsEarned == 3 -> "太棒了！完全正确！"
                    starsEarned == 2 -> "做得好！继续加油！"
                    else -> "不错！继续努力！"
                }

            // Append combo message if available
            return if (comboMessage != null && isCorrect) {
                "$baseMessage $comboMessage"
            } else {
                baseMessage
            }
        }
    }
