package com.wordland.domain.algorithm

/**
 * Algorithm for calculating and updating memory strength
 * Based on spaced repetition and forgetting curve principles
 */
object MemoryStrengthAlgorithm {
    /**
     * Calculate new memory strength based on practice result
     *
     * Hybrid guessing detection (Plan C):
     * - Memory strength ≥ 80 AND historical accuracy ≥ 90%: Considered mastered, skip guessing penalty
     * - Other cases: Use current guessing detection logic
     *
     * Hint penalty:
     * - When hint is used, memory strength growth is reduced by 50%
     * - Penalty applies only to correct answers (incorrect answers already penalized)
     *
     * @param currentStrength Current memory strength (0-100)
     * @param isCorrect Whether the answer was correct
     * @param isGuessing Whether the user was guessing (anti-cheating)
     * @param wordDifficulty Word difficulty (1-5)
     * @param historicalAccuracy Historical correct rate (0.0-1.0), null if unknown
     * @param hintUsed Whether the user used a hint before answering
     * @return New memory strength (0-100)
     */
    fun calculateNewStrength(
        currentStrength: Int,
        isCorrect: Boolean,
        isGuessing: Boolean,
        wordDifficulty: Int,
        historicalAccuracy: Float? = null,
        hintUsed: Boolean = false,
    ): Int {
        // Hybrid guessing detection: Skip guessing penalty for mastered words
        // A word is considered "mastered" if:
        // - Memory strength ≥ 80 (remembered well)
        // - Historical accuracy ≥ 90% (consistently correct)
        val isMastered =
            currentStrength >= 80 &&
                historicalAccuracy != null &&
                historicalAccuracy >= 0.9f

        // Only apply guessing penalty if NOT mastered
        if (isGuessing && !isMastered) {
            // Penalize guessing heavily
            return maxOf(0, currentStrength - 30)
        }

        val baseChange =
            when {
                isCorrect -> {
                    // Correct answer: increase strength
                    // Harder words get more boost for correct answers
                    when {
                        currentStrength < 30 -> 20
                        currentStrength < 60 -> 15
                        currentStrength < 80 -> 10
                        else -> 5
                    }
                }
                else -> {
                    // Incorrect answer: decrease strength
                    when {
                        currentStrength > 80 -> -20
                        currentStrength > 60 -> -15
                        currentStrength > 30 -> -10
                        else -> -5
                    }
                }
            }

        // Adjust based on word difficulty
        val difficultyMultiplier =
            when (wordDifficulty) {
                1 -> 0.8
                2 -> 0.9
                3 -> 1.0
                4 -> 1.1
                5 -> 1.2
                else -> 1.0
            }

        val adjustedChange = (baseChange * difficultyMultiplier).toInt()

        // Apply hint penalty: Reduce memory strength growth by 50% when hint was used
        // Only applies to correct answers (incorrect answers are already penalized)
        val finalChange =
            if (hintUsed && isCorrect) {
                // Reduce positive change by 50% - learner gets partial credit
                (adjustedChange * 0.5).toInt()
            } else {
                adjustedChange
            }

        return (currentStrength + finalChange).coerceIn(0, 100)
    }

    /**
     * Detect if user is guessing based on response time
     *
     * @param responseTime Time taken to respond in milliseconds
     * @param wordDifficulty Word difficulty (1-5)
     * @return True if likely guessing
     */
    fun detectGuessing(
        responseTime: Long,
        wordDifficulty: Int,
    ): Boolean {
        // Expected minimum time based on difficulty
        val expectedMinTime =
            when (wordDifficulty) {
                1 -> 1500L // 1.5 seconds for easy words
                2 -> 2000L // 2 seconds
                3 -> 2500L // 2.5 seconds
                4 -> 3000L // 3 seconds
                5 -> 3500L // 3.5 seconds for hard words
                else -> 2000L
            }

        // If response is too fast, likely guessing
        return responseTime < expectedMinTime * 0.5
    }

    /**
     * Calculate next review time based on current strength
     *
     * @param currentStrength Current memory strength (0-100)
     * @param lastReviewTime Time of last review
     * @return Next review time in milliseconds since epoch
     */
    fun calculateNextReview(
        currentStrength: Int,
        lastReviewTime: Long,
    ): Long {
        // Review intervals based on strength (SuperMemo-2 inspired)
        val intervalMinutes =
            when {
                currentStrength < 30 -> 10L // 10 minutes
                currentStrength < 50 -> 60L // 1 hour
                currentStrength < 70 -> 4 * 60L // 4 hours
                currentStrength < 85 -> 24 * 60L // 1 day
                else -> 7 * 24 * 60L // 1 week
            }

        return lastReviewTime + (intervalMinutes * 60 * 1000)
    }

    /**
     * Calculate initial strength for a new word
     *
     * @param wordDifficulty Word difficulty (1-5)
     * @return Initial memory strength
     */
    fun calculateInitialStrength(wordDifficulty: Int): Int {
        // Easier words start with higher initial strength
        return when (wordDifficulty) {
            1 -> 40
            2 -> 35
            3 -> 30
            4 -> 25
            5 -> 20
            else -> 30
        }
    }
}
