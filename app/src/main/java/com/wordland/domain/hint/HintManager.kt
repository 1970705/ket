package com.wordland.domain.hint

/**
 * Manages hint usage and limitations
 * Prevents hint abuse and tracks hint availability
 */
class HintManager {
    private val hintUsage = mutableMapOf<String, HintUsageInfo>()

    // Configuration
    var maxHintsPerWord = 3

    var hintCooldownMs = 500L // 0.5 seconds cooldown between hints (reduced from 3s)

    /**
     * Data class to track hint usage for a specific word
     */
    data class HintUsageInfo(
        var usageCount: Int = 0,
        var lastUsedAt: Long = 0L,
        var firstUseTime: Long? = null,
        var currentLevel: Int = 0,
        var consecutiveUsage: Int = 0,
    )

    /**
     * Check if hint can be used for a word
     * @param wordId The word ID
     * @return Pair of (canUse: Boolean, reason: String?)
     */
    fun canUseHint(wordId: String): Pair<Boolean, String?> {
        val info = hintUsage[wordId] ?: HintUsageInfo()

        // Check 1: Max hints limit
        if (info.usageCount >= maxHintsPerWord) {
            return Pair(false, "已达到提示次数上限 (${maxHintsPerWord}次)")
        }

        // Check 2: Cooldown - minimal cooldown to prevent accidental double-taps
        val now = System.currentTimeMillis()
        if (info.lastUsedAt > 0 && (now - info.lastUsedAt) < hintCooldownMs) {
            // Very short cooldown to prevent spamming (0.5s)
            // Allows progressive hints to flow naturally
            val remainingMs = hintCooldownMs - (now - info.lastUsedAt)
            val remainingSec = (remainingMs / 1000.0).toInt()
            if (remainingSec > 0) {
                return Pair(false, "请等待 $remainingSec 秒后再使用提示")
            }
        }

        return Pair(true, null)
    }

    /**
     * Record hint usage
     * @param wordId The word ID
     * @return The new hint level (1-3)
     */
    fun useHint(wordId: String): Int {
        val info = hintUsage.getOrPut(wordId) { HintUsageInfo() }

        val now = System.currentTimeMillis()
        val previousLastUsedAt = info.lastUsedAt // Save BEFORE updating

        if (info.usageCount == 0) {
            info.firstUseTime = now
        }
        info.usageCount++
        info.lastUsedAt = now

        // Determine new hint level
        val newLevel =
            when {
                info.usageCount == 1 -> 1
                info.usageCount == 2 -> 2
                else -> 3
            }

        info.currentLevel = newLevel

        // Update consecutive usage counter based on time since previous hint
        if (previousLastUsedAt > 0 && (now - previousLastUsedAt) < 5000) {
            // Used within 5 seconds of previous hint
            info.consecutiveUsage++
        } else {
            info.consecutiveUsage = 1
        }

        return newLevel
    }

    /**
     * Get current hint level for a word
     * @param wordId The word ID
     * @return The hint level (1-3), or 0 if no hints used
     */
    fun getCurrentHintLevel(wordId: String): Int {
        return hintUsage[wordId]?.currentLevel ?: 0
    }

    /**
     * Get remaining hint count for a word
     * @param wordId The word ID
     * @return Number of hints remaining
     */
    fun getRemainingHints(wordId: String): Int {
        val used = hintUsage[wordId]?.usageCount ?: 0
        return maxOf(0, maxHintsPerWord - used)
    }

    /**
     * Reset hint usage for a word (e.g., when moving to next word)
     * @param wordId The word ID
     */
    fun resetHints(wordId: String) {
        hintUsage.remove(wordId)
    }

    /**
     * Get hint usage statistics
     * @param wordId The word ID
     * @return Triple of (used, remaining, total)
     */
    fun getHintStats(wordId: String): Triple<Int, Int, Int> {
        val used = hintUsage[wordId]?.usageCount ?: 0
        val remaining = maxOf(0, maxHintsPerWord - used)
        return Triple(used, remaining, maxHintsPerWord)
    }

    /**
     * Check if user is overusing hints
     * @param wordId The word ID
     * @return True if user shows hint dependency pattern
     */
    fun isOverusingHints(wordId: String): Boolean {
        val info = hintUsage[wordId] ?: return false

        // Consider overusing if:
        // 1. Used all available hints
        // 2. High consecutive usage
        // 3. All hints used in quick succession
        return info.usageCount >= maxHintsPerWord ||
            info.consecutiveUsage >= 2 ||
            (info.usageCount >= 2 && info.lastUsedAt - (info.firstUseTime ?: info.lastUsedAt) < 10000)
    }

    /**
     * Get hint dependency score
     * Higher score indicates more dependency on hints
     * @param wordId The word ID
     * @return Score from 0.0 to 1.0
     */
    fun getHintDependencyScore(wordId: String): Float {
        val info = hintUsage[wordId] ?: return 0f

        val usageScore = info.usageCount.toFloat() / maxHintsPerWord.toFloat()
        val consecutiveScore = info.consecutiveUsage.toFloat() / 3f // Max out at 3

        return (usageScore + consecutiveScore) / 2f
    }

    /**
     * Reset all hint usage (e.g., for new session)
     */
    fun resetAll() {
        hintUsage.clear()
    }

    /**
     * Set maximum hints per word based on difficulty
     * @param difficulty The word difficulty (1-5)
     */
    fun setMaxHintsForDifficulty(difficulty: Int) {
        maxHintsPerWord =
            when (difficulty) {
                1 -> 5 // Easy words: more hints
                2 -> 4
                3 -> 3 // Normal: default
                4 -> 2
                5 -> 1 // Hard words: minimal hints
                else -> 3
            }
    }
}
