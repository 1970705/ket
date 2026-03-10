package com.wordland.domain.model.achievement

import androidx.compose.runtime.Immutable

/**
 * Achievement definition - static metadata about an achievement
 * This is immutable and seeded from data/seed/AchievementSeeder.kt
 */
@Immutable
data class Achievement(
    val id: String, // Unique ID: "first_steps", "combo_master"
    val name: String, // Display name: "First Steps"
    val description: String, // User-facing description
    val icon: String, // Emoji or resource ID: "🌟"
    val category: AchievementCategory, // PROGRESS, PERFORMANCE, COMBO, STREAK, SPECIAL
    val tier: AchievementTier, // BRONZE, SILVER, GOLD, PLATINUM
    val requirement: AchievementRequirement, // Unlock condition
    val reward: AchievementReward, // Reward for unlocking
    val isHidden: Boolean = false, // Secret achievement
    val parentId: String? = null, // For chained achievements
)
