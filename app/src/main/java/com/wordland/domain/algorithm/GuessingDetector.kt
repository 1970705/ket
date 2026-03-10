package com.wordland.domain.algorithm

import com.wordland.domain.model.BehaviorTracking

/**
 * Detector for identifying guessing behavior
 *
 * **Unified Guessing Detection Strategy**
 *
 * This class provides constants and methods for guessing detection across the codebase.
 * It serves as the single source of truth for guessing thresholds and detection logic.
 *
 * **Definition of Guessing**:
 * Submitting answers without adequate thinking time, indicating random clicking
 * rather than learning. Detection varies by use case (level-level vs per-word).
 *
 * **Thresholds by Use Case**:
 * - **Level-level penalty** (`LEVEL_PENALTY_THRESHOLD_MS`): 1500ms/word average
 *   - Used by: StarRatingCalculator for -0.6 star penalty
 *   - Rationale: Stricter threshold for level aggregation to avoid false positives
 *
 * - **Per-word detection** (`WORD_DETECTION_THRESHOLD_MS`): 2000ms/response
 *   - Used by: BehaviorAnalyzer for behavior flagging
 *   - Rationale: More lenient for individual responses to catch potential guessing early
 *
 * - **Rapid incorrect** (`RAPID_INCORRECT_THRESHOLD_MS`): 1500ms/response
 *   - Used by: GuessingDetector.detectGuessing() for pattern analysis
 *   - Rationale: Stricter threshold for incorrect answers (indicates frustration/random)
 *
 * **Architecture Note**:
 * - StarRatingCalculator: Level-level penalty (uses LEVEL_PENALTY_THRESHOLD_MS)
 * - BehaviorAnalyzer: Per-word detection (uses WORD_DETECTION_THRESHOLD_MS)
 * - GuessingDetector: Sophisticated pattern analysis (currently unused, reserved for future)
 *
 * @see StarRatingCalculator.calculateTimeBonusWithPenalty
 * @see BehaviorAnalyzer.isGuessing
 */
object GuessingDetector {
    // ========== UNIFIED THRESHOLDS (Single Source of Truth) ==========

    /**
     * Level-level guessing threshold for star rating penalty
     *
     * Used when calculating average time per word for an entire level.
     * If average time per word < this threshold, apply -0.6 star penalty.
     *
     * Rationale: Stricter threshold because averaging across multiple words
     * reduces noise. Averaging 6 words at 1.5s each indicates systematic guessing.
     *
     * Used by: StarRatingCalculator.calculateTimeBonusWithPenalty()
     */
    const val LEVEL_PENALTY_THRESHOLD_MS = 1500L

    /**
     * Per-word guessing threshold for behavior detection
     *
     * Used when detecting guessing on individual word attempts.
     * If 2+ out of 5 recent responses < this threshold, flag as guessing.
     *
     * Rationale: More lenient because individual responses have more variance.
     * Allows for fast correct answers while still catching systematic guessing.
     *
     * Used by: BehaviorAnalyzer.isGuessing()
     */
    const val WORD_DETECTION_THRESHOLD_MS = 2000L

    /**
     * Rapid incorrect answer threshold for pattern detection
     *
     * Used when detecting rapid incorrect answers in sophisticated pattern analysis.
     * Stricter than WORD_DETECTION_THRESHOLD_MS because incorrect answers should
     * take more time (learning from mistakes).
     *
     * Used by: GuessingDetector.detectGuessing() (currently unused, reserved for future)
     */
    const val RAPID_INCORRECT_THRESHOLD_MS = 1500L

    // ========== PENALTY CONSTANTS (Reference Only) ==========

    /**
     * Star rating penalty for guessing at level-level
     *
     * Applied by StarRatingCalculator when average time per word < LEVEL_PENALTY_THRESHOLD_MS
     *
     * Example: Perfect score (6/6) with guessing = 3.0 - 0.6 = 2.4 → 2 stars
     */
    const val LEVEL_PENALTY_STARS = 0.6f

    // ========== DETECTION METHODS ==========

    /**
     * Response pattern for analysis
     */
    data class ResponsePattern(
        val timestamp: Long,
        val responseTime: Long,
        val isCorrect: Boolean,
        val hintUsed: Boolean,
    )

    /**
     * Analyze recent responses to detect guessing (sophisticated pattern analysis)
     *
     * **Note**: This method is currently UNUSED in the codebase.
     * It is reserved for future enhancement if sophisticated detection is needed.
     * Current implementations use simple thresholds:
     * - StarRatingCalculator: LEVEL_PENALTY_THRESHOLD_MS (1500ms)
     * - BehaviorAnalyzer: WORD_DETECTION_THRESHOLD_MS (2000ms)
     *
     * Detection criteria:
     * 1. 70%+ responses < WORD_DETECTION_THRESHOLD_MS (2000ms)
     * 2. Correctness 30-70% without hints (random pattern)
     * 3. 50%+ rapid incorrect answers < RAPID_INCORRECT_THRESHOLD_MS (1500ms)
     *
     * @param patterns Recent response patterns (minimum 3)
     * @return True if guessing is detected
     *
     * @see WORD_DETECTION_THRESHOLD_MS
     * @see RAPID_INCORRECT_THRESHOLD_MS
     */
    fun detectGuessing(patterns: List<ResponsePattern>): Boolean {
        if (patterns.size < 3) return false

        // Check for consistently fast responses
        val fastResponseCount =
            patterns.count { pattern ->
                pattern.responseTime < WORD_DETECTION_THRESHOLD_MS
            }

        if (fastResponseCount > patterns.size * 0.7) {
            return true // 70%+ responses are suspiciously fast
        }

        // Check for random answer pattern (correctness rate near 50%)
        val correctCount = patterns.count { it.isCorrect }
        val correctness = correctCount.toDouble() / patterns.size

        // P0-BUG-011 fix: Narrow range to 20-40% (only true randomness)
        // Previous 30-70% was too broad, penalizing legitimate 66.7% accuracy
        if (correctness in 0.2..0.4 && patterns.all { !it.hintUsed }) {
            return true // Accuracy near chance level without hints
        }

        // Check for pattern of rapid incorrect answers
        val rapidIncorrectCount =
            patterns.count { pattern ->
                !pattern.isCorrect && pattern.responseTime < RAPID_INCORRECT_THRESHOLD_MS
            }

        if (rapidIncorrectCount > patterns.size * 0.5) {
            return true
        }

        return false
    }

    /**
     * Convert BehaviorTracking to ResponsePattern
     */
    fun BehaviorTracking.toResponsePattern(): ResponsePattern? {
        // Skip if responseTime is null (e.g., hint_used actions)
        if (this.responseTime == null) return null

        return ResponsePattern(
            timestamp = this.timestamp,
            responseTime = this.responseTime!!,
            isCorrect = this.isCorrect ?: false,
            hintUsed = this.hintUsed ?: false,
        )
    }

    /**
     * Calculate confidence score for guessing detection
     *
     * **Note**: Currently UNUSED. Reserved for future enhancement.
     *
     * Confidence factors:
     * - Response speed: +0.3 if avg < WORD_DETECTION_THRESHOLD_MS, +0.1 if avg < 3000ms
     * - Consistency: +0.2 if standard deviation < 1000ms (very consistent timing)
     * - Hint usage: +0.2 if no hints used in 5+ attempts
     * - Answer pattern: +0.3 if correctness 40-60% (near random)
     *
     * @param patterns Recent response patterns
     * @return Confidence score (0.0 to 1.0)
     *
     * @see WORD_DETECTION_THRESHOLD_MS
     */
    fun calculateGuessingConfidence(patterns: List<ResponsePattern>): Double {
        if (patterns.isEmpty()) return 0.0

        var confidence = 0.0

        // Factor 1: Response speed
        val avgResponseTime = patterns.map { it.responseTime }.average()
        if (avgResponseTime < WORD_DETECTION_THRESHOLD_MS) {
            confidence += 0.3
        } else if (avgResponseTime < 3000) {
            confidence += 0.1
        }

        // Factor 2: Consistency of speed
        val responseTimes = patterns.map { it.responseTime }
        val stdDev = calculateStandardDeviation(responseTimes)
        if (stdDev < 1000) {
            confidence += 0.2 // Very consistent timing suggests random clicking
        }

        // Factor 3: Hint usage (or lack thereof)
        val hintsUsed = patterns.count { it.hintUsed }
        if (hintsUsed == 0 && patterns.size >= 5) {
            confidence += 0.2
        }

        // Factor 4: Answer pattern
        val correctCount = patterns.count { it.isCorrect }
        val correctness = correctCount.toDouble() / patterns.size
        if (correctness in 0.4..0.6) {
            confidence += 0.3 // Near random
        }

        return confidence.coerceIn(0.0, 1.0)
    }

    private fun calculateStandardDeviation(values: List<Long>): Double {
        if (values.size < 2) return 0.0

        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
