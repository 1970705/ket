package com.wordland.domain.model.achievement

import androidx.compose.runtime.Immutable

/**
 * Achievement rewards
 * Sealed class for type-safe reward granting
 */
@Immutable
sealed class AchievementReward {
    /**
     * Display text for the reward
     */
    abstract fun getDisplayText(): String

    /**
     * Star currency reward
     */
    data class Stars(val amount: Int) : AchievementReward() {
        override fun getDisplayText(): String = "$amount stars ⭐"
    }

    /**
     * Title displayed next to username
     */
    data class Title(
        val titleId: String,
        val displayName: String,
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Title: $displayName"
    }

    /**
     * Permanent badge on profile
     */
    data class Badge(
        val badgeId: String,
        val iconName: String,
        val displayName: String,
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Badge: $displayName $iconName"
    }

    /**
     * Unlock exclusive pet
     */
    data class PetUnlock(
        val petId: String,
        val petName: String,
        val petIcon: String,
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Pet: $petName $petIcon"
    }

    /**
     * Multiple rewards combined
     */
    data class Multiple(
        val rewards: List<AchievementReward>,
    ) : AchievementReward() {
        override fun getDisplayText(): String {
            return rewards.joinToString(", ") { it.getDisplayText() }
        }
    }
}
