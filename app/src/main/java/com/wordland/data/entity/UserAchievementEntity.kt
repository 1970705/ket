package com.wordland.data.entity

import androidx.room.Entity
import com.wordland.domain.model.achievement.UserAchievement

/**
 * Room entity for user achievement progress
 */
@Entity(
    tableName = "user_achievements",
    primaryKeys = ["userId", "achievementId"],
)
data class UserAchievementEntity(
    val userId: String,
    val achievementId: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1,
    val unlockedAt: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
)

/**
 * Convert UserAchievementEntity to domain model
 */
fun UserAchievementEntity.toDomainModel(): UserAchievement =
    UserAchievement(
        userId = userId,
        achievementId = achievementId,
        isUnlocked = isUnlocked,
        progress = progress,
        target = target,
        unlockedAt = unlockedAt,
        lastUpdated = lastUpdated,
    )

/**
 * Convert domain model to entity
 */
fun UserAchievement.toEntity(): UserAchievementEntity =
    UserAchievementEntity(
        userId = userId,
        achievementId = achievementId,
        isUnlocked = isUnlocked,
        progress = progress,
        target = target,
        unlockedAt = unlockedAt,
        lastUpdated = lastUpdated,
    )
