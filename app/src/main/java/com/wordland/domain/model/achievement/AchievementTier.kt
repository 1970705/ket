package com.wordland.domain.model.achievement

/**
 * Achievement difficulty tiers
 * Maps to visual styling and reward magnitude
 */
enum class AchievementTier(val stars: Int, val color: Long) {
    /**
     * Easy achievements
     */
    BRONZE(1, 0xFFCD7F32),

    /**
     * Medium achievements
     */
    SILVER(2, 0xFFC0C0C0),

    /**
     * Hard achievements
     */
    GOLD(3, 0xFFFFD700),

    /**
     * Very hard achievements
     */
    PLATINUM(4, 0xFFE5E4E2),
}
