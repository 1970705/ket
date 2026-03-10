package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wordland.domain.constants.DomainConstants

/**
 * Island mastery tracking
 * Calculates overall progress for an island (theme)
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
@Entity(tableName = "island_mastery")
data class IslandMastery(
    @PrimaryKey
    val islandId: String, // "look_island", "move_valley"
    val userId: String, // "user_001"
    // Word Progress
    val totalWords: Int,
    val masteredWords: Int,
    // Level Progress
    val totalLevels: Int,
    val completedLevels: Int,
    // Cross-Scene Score (0-1)
    val crossSceneScore: Double, // How well user applies words in cross-scenes
    // Mastery Percentage
    val masteryPercentage: Double = 0.0, // 0-100
    // Unlock Status
    val isNextIslandUnlocked: Boolean = false, // Can unlock next island?
    // Timestamps
    val unlockedAt: Long? = null,
    val masteredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /**
     * Calculate mastery percentage
     * Formula: 70% word mastery + 30% cross-scene application
     */
    fun calculateMastery(): Double {
        val wordMastery = (masteredWords.toDouble() / totalWords) * 0.7
        val crossSceneMastery = crossSceneScore * 0.3
        return (wordMastery + crossSceneMastery) * 100
    }

    /**
     * Check if next island should be unlocked
     * Criteria: Mastery percentage >= 60%
     */
    fun updateUnlockStatus(): Pair<Boolean, Double> {
        val wasUnlocked = isNextIslandUnlocked
        val newMasteryPercentage = calculateMastery()
        val newUnlockStatus = newMasteryPercentage >= DomainConstants.MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND

        return Pair(newUnlockStatus, newMasteryPercentage)
    }
}
