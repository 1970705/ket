package com.wordland.domain.model.achievement

import androidx.compose.runtime.Immutable

/**
 * Achievement unlock requirements
 * Sealed class allows type-safe pattern matching
 */
@Immutable
sealed class AchievementRequirement {
    /**
     * Progress: Target value for progress tracking
     */
    abstract val targetValue: Int

    /**
     * Display text for progress (e.g., "42/60")
     */
    abstract fun getProgressText(current: Int): String

    /**
     * Complete N levels with minimum stars
     */
    data class CompleteLevels(
        val count: Int,
        val minStars: Int = 1,
    ) : AchievementRequirement() {
        override val targetValue: Int = count

        override fun getProgressText(current: Int): String = "$current/$count levels"
    }

    /**
     * Master N words (memory strength >= threshold)
     */
    data class MasterWords(
        val count: Int,
        val memoryStrengthThreshold: Int = 80,
    ) : AchievementRequirement() {
        override val targetValue: Int = count

        override fun getProgressText(current: Int): String = "$current/$count words"
    }

    /**
     * Achieve N consecutive correct answers
     */
    data class ComboMilestone(
        val comboCount: Int,
    ) : AchievementRequirement() {
        override val targetValue: Int = comboCount

        override fun getProgressText(current: Int): String {
            return if (current >= comboCount) "Reached!" else "Best: $current/$comboCount"
        }
    }

    /**
     * N-day practice streak
     */
    data class StreakDays(
        val days: Int,
        val minWordsPerDay: Int = 1,
    ) : AchievementRequirement() {
        override val targetValue: Int = days

        override fun getProgressText(current: Int): String = "$current/$days days"
    }

    /**
     * Complete N perfect levels (all 3-star words)
     */
    data class PerfectLevel(
        val count: Int = 1,
    ) : AchievementRequirement() {
        override val targetValue: Int = count

        override fun getProgressText(current: Int): String = "$current/$count levels"
    }

    /**
     * Complete level without hints
     */
    data class NoHintsLevel(
        val wordCount: Int = 6,
    ) : AchievementRequirement() {
        override val targetValue: Int = wordCount

        override fun getProgressText(current: Int): String = "$current/$wordCount words"
    }

    /**
     * Max memory strength (100) on N words
     */
    data class MaxMemoryStrength(
        val count: Int,
        val strength: Int = 100,
    ) : AchievementRequirement() {
        override val targetValue: Int = count

        override fun getProgressText(current: Int): String = "$current/$count words"
    }

    /**
     * Complete all levels in an island
     */
    data class CompleteIsland(
        val islandId: String,
        val levelCount: Int,
    ) : AchievementRequirement() {
        override val targetValue: Int = levelCount

        override fun getProgressText(current: Int): String = "$current/$levelCount levels"
    }

    /**
     * Unlock N different islands
     */
    data class UnlockIslands(
        val count: Int,
    ) : AchievementRequirement() {
        override val targetValue: Int = count

        override fun getProgressText(current: Int): String = "$current/$count islands"
    }
}
