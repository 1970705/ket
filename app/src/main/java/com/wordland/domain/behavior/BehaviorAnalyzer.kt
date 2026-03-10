package com.wordland.domain.behavior

import com.wordland.data.repository.TrackingRepository
import com.wordland.domain.algorithm.GuessingDetector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes user behavior for intelligent hint management
 * Detects patterns and provides recommendations
 *
 * **Guessing Detection Strategy**:
 * Uses unified constants from GuessingDetector for consistency.
 * - Per-word detection threshold: GuessingDetector.WORD_DETECTION_THRESHOLD_MS (2000ms)
 * - Level-level penalty threshold: GuessingDetector.LEVEL_PENALTY_THRESHOLD_MS (1500ms)
 *
 * This class focuses on per-word behavior analysis (flagging potential guessing).
 * StarRatingCalculator handles level-level penalties (actual star deduction).
 *
 * @see GuessingDetector for unified threshold constants
 * @see StarRatingCalculator for level-level guessing penalties
 */
@Singleton
class BehaviorAnalyzer
    @Inject
    constructor(
        private val trackingRepository: TrackingRepository,
    ) {
        /**
         * Check if user is showing guessing behavior
         *
         * **Detection**: 2+ out of 5 recent answers < WORD_DETECTION_THRESHOLD_MS (2000ms)
         * **Purpose**: Flag potential guessing for behavior tracking (no penalty applied here)
         * **Penalty**: Applied by StarRatingCalculator at level-level using LEVEL_PENALTY_THRESHOLD_MS
         *
         * @param userId User ID
         * @param wordId Word ID
         * @return True if guessing pattern detected
         *
         * @see GuessingDetector.WORD_DETECTION_THRESHOLD_MS
         * @see StarRatingCalculator.calculateTimeBonusWithPenalty
         */
        suspend fun isGuessing(
            userId: String,
            wordId: String,
        ): Boolean {
            val recentTracking =
                trackingRepository.getTrackingByWord(
                    userId = userId,
                    wordId = wordId,
                    limit = 5,
                )
            if (recentTracking.isEmpty()) return false

            // Check for fast consecutive answers
            // UNIFIED: Uses WORD_DETECTION_THRESHOLD_MS for per-word detection
            val fastAnswers =
                recentTracking.count { tracking ->
                    tracking.action == "answer" &&
                        tracking.responseTime != null &&
                        tracking.responseTime < GuessingDetector.WORD_DETECTION_THRESHOLD_MS
                }

            return fastAnswers >= 2
        }

        /**
         * Check if user needs a hint based on behavior
         * @param userId User ID
         * @param wordId Word ID
         * @return True if hint is recommended
         */
        suspend fun shouldRecommendHint(
            userId: String,
            wordId: String,
        ): Boolean {
            val recentTracking =
                trackingRepository.getTrackingByWord(
                    userId = userId,
                    wordId = wordId,
                    limit = 3,
                )
            if (recentTracking.isEmpty()) return false

            // Recommend hint if:
            // 1. Multiple incorrect attempts
            val incorrectAttempts =
                recentTracking.count { tracking ->
                    tracking.action == "answer" &&
                        tracking.isCorrect == false
                }

            // 2. Long response times (struggling)
            val struggling =
                recentTracking.any { tracking ->
                    tracking.action == "answer" &&
                        tracking.responseTime != null &&
                        tracking.responseTime > 10000 // More than 10 seconds
                }

            return incorrectAttempts >= 2 || struggling
        }

        /**
         * Get user's hint dependency level
         * @param userId User ID
         * @return Dependency score (0.0 to 1.0)
         */
        suspend fun getHintDependencyLevel(userId: String): Float {
            val allTracking = trackingRepository.getRecentTracking(userId, limit = 1000)
            if (allTracking.isEmpty()) return 0f

            // Calculate hint usage frequency
            val hintUsage = allTracking.count { it.action == "hint_used" }
            val totalActions = allTracking.size

            if (totalActions == 0) return 0f

            val hintFrequency = hintUsage.toFloat() / totalActions.toFloat()

            // Look for consecutive hint usage patterns
            var consecutiveHints = 0
            var maxConsecutiveHints = 0

            allTracking.forEach { tracking ->
                if (tracking.action == "hint_used") {
                    consecutiveHints++
                    maxConsecutiveHints = maxOf(maxConsecutiveHints, consecutiveHints)
                } else if (tracking.action == "answer") {
                    consecutiveHints = 0
                }
            }

            // Combine frequency and consecutive usage
            return (hintFrequency * 0.7f + (maxConsecutiveHints / 5f) * 0.3f).coerceIn(0f, 1f)
        }

        /**
         * Analyze if user is making progress or stuck
         * @param userId User ID
         * @param wordId Word ID
         * @return Analysis result
         */
        suspend fun analyzeUserProgress(
            userId: String,
            wordId: String,
        ): ProgressAnalysis {
            val recentTracking =
                trackingRepository.getTrackingByWord(
                    userId = userId,
                    wordId = wordId,
                    limit = 10,
                )
            if (recentTracking.isEmpty()) return ProgressAnalysis.NO_ATTEMPTS

            val answers = recentTracking.filter { it.action == "answer" }
            if (answers.isEmpty()) return ProgressAnalysis.NO_ATTEMPTS

            val correctAnswers = answers.count { it.isCorrect == true }
            val incorrectAnswers = answers.size - correctAnswers

            // Calculate average response time for correct vs incorrect
            val correctTimes =
                answers
                    .filter { it.isCorrect == true }
                    .mapNotNull { it.responseTime }
            val incorrectTimes =
                answers
                    .filter { it.isCorrect == false }
                    .mapNotNull { it.responseTime }

            val avgCorrectTime =
                if (correctTimes.isNotEmpty()) {
                    correctTimes.sum() / correctTimes.size
                } else {
                    null
                }

            val avgIncorrectTime =
                if (incorrectTimes.isNotEmpty()) {
                    incorrectTimes.sum() / incorrectTimes.size
                } else {
                    null
                }

            return when {
                correctAnswers >= answers.size * 0.8 -> ProgressAnalysis.MAKING_PROGRESS
                incorrectAnswers >= answers.size * 0.7 -> ProgressAnalysis.STRUGGLING
                avgIncorrectTime != null && avgIncorrectTime > 8000 -> ProgressAnalysis.THINKING_TOO_LONG
                avgCorrectTime != null && avgCorrectTime < 3000 -> ProgressAnalysis.ANSWERING_TOO_FAST
                else -> ProgressAnalysis.IN_PROGRESS
            }
        }

        /**
         * Check if user behavior suggests they need a hint
         * @param userId User ID
         * @param wordId Word ID
         * @return True if hint is appropriate
         */
        suspend fun shouldTriggerHintAutomatically(
            userId: String,
            wordId: String,
        ): Boolean {
            val analysis = analyzeUserProgress(userId, wordId)

            return when (analysis) {
                ProgressAnalysis.STRUGGLING,
                ProgressAnalysis.THINKING_TOO_LONG,
                -> true
                else -> false
            }
        }

        /**
         * Get recommended hint level based on user behavior
         * @param userId User ID
         * @param wordId Word ID
         * @return Recommended hint level (1-3), or 0 if no hint needed
         */
        suspend fun getRecommendedHintLevel(
            userId: String,
            wordId: String,
        ): Int {
            val analysis = analyzeUserProgress(userId, wordId)

            return when (analysis) {
                ProgressAnalysis.STRUGGLING -> 2 // Start with moderate help
                ProgressAnalysis.THINKING_TOO_LONG -> 1 // Gentle nudge
                ProgressAnalysis.NO_ATTEMPTS -> 1 // First attempt hint
                else -> 0 // No hint recommended
            }
        }
    }

/**
 * User progress analysis result
 */
enum class ProgressAnalysis {
    UNKNOWN, // Insufficient data
    NO_ATTEMPTS, // User hasn't tried yet
    IN_PROGRESS, // Mixed results
    MAKING_PROGRESS, // Doing well
    STRUGGLING, // Having trouble
    THINKING_TOO_LONG, // Taking too long
    ANSWERING_TOO_FAST, // Rushing (possible guessing)
}
