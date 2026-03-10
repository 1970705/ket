# Achievement System Architecture - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: Game Designer
**Status**: Design Phase
**Related Tasks**: #23 (Achievement System Architecture Design)

---

## Executive Summary

A comprehensive achievement system architecture designed for the Wordland KET vocabulary learning app. The system integrates with existing game mechanics (combo, memory strength, progression) and provides a modular, extensible framework for rewarding learning behaviors.

**Design Goals**:
1. **Clean Architecture Compliance**: Domain layer independence, clear separation of concerns
2. **Child-Friendly**: Age-appropriate rewards, positive reinforcement
3. **Anti-Addiction**: No FOMO mechanics, streak freeze days
4. **Performance**: Efficient event processing, minimal database impact
5. **Testability**: Pure domain logic, mockable dependencies

---

## System Architecture

### High-Level Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐        │
│  │ AchievementScreen│  │ AchievementPopup│  │ ProfileBadges   │        │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘        │
│           │                    │                    │                  │
│           └────────────────────┼────────────────────┘                  │
│                                │                                        │
└────────────────────────────────┼────────────────────────────────────────┘
                                 │ StateFlow
┌────────────────────────────────┼────────────────────────────────────────┐
│                           ViewModel Layer                                │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                    AchievementViewModel                        │      │
│  │  - achievementState: StateFlow<AchievementUiState>             │      │
│  │  - recentUnlocks: StateFlow<List<Achievement>>                 │      │
│  │  - notificationQueue: StateFlow<List<Achievement>>             │      │
│  └─────────────────────────────┬─────────────────────────────────┘      │
└────────────────────────────────┼────────────────────────────────────────┘
                                 │
┌────────────────────────────────┼────────────────────────────────────────┐
│                          Domain Layer                                    │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                      Use Cases                                  │      │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────┐  │      │
│  │  │CheckAchievements │  │ GetAchievements  │  │ GrantReward │  │      │
│  │  │UseCase           │  │UseCase           │  │UseCase      │  │      │
│  │  └────────┬─────────┘  └────────┬─────────┘  └──────┬──────┘  │      │
│  │           │                     │                    │         │      │
│  │  ┌────────┴─────────────────────┴────────────────────┐        │      │
│  │  │         AchievementTracker                        │        │      │
│  │  │  - checkAchievements()                            │        │      │
│  │  │  - calculateProgress()                            │        │      │
│  │  │  - isUnlockConditionMet()                         │        │      │
│  │  └────────┬──────────────────────────────────────────┘        │      │
│  │           │                                                    │      │
│  │  ┌────────┴──────────────────────────────────────────┐        │      │
│  │  │         StreakManager                              │        │      │
│  │  │  - calculateStreak()                               │        │      │
│  │  │  - applyFreezeDays()                               │        │      │
│  │  │  - getLastActiveDate()                             │        │      │
│  │  └────────────────────────────────────────────────────┘        │      │
│  └─────────────────────────────┬─────────────────────────────────┘      │
│                                │                                        │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                      Models                                     │      │
│  │  - Achievement (definition)                                    │      │
│  │  - UserAchievement (progress)                                  │      │
│  │  - GameEvent (trigger events)                                  │      │
│  │  - AchievementReward (sealed class)                            │      │
│  └────────────────────────────────────────────────────────────────┘      │
└────────────────────────────────┼────────────────────────────────────────┘
                                 │
┌────────────────────────────────┼────────────────────────────────────────┐
│                          Data Layer                                     │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                    AchievementRepository                       │      │
│  │  - getAllAchievements()                                       │      │
│  │  - getUserProgress()                                          │      │
│  │  - updateProgress()                                           │      │
│  │  - unlockAchievement()                                        │      │
│  └─────────────────────────────┬─────────────────────────────────┘      │
│                                │                                        │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                    AchievementDao                              │      │
│  │  - @Query: getAllAchievements()                               │      │
│  │  - @Query: getUserAchievements()                              │      │
│  │  - @Query: getUnlockedAchievements()                          │      │
│  │  - @Insert: unlockAchievement()                               │      │
│  └─────────────────────────────┬─────────────────────────────────┘      │
│                                │                                        │
│  ┌─────────────────────────────┴─────────────────────────────────┐      │
│  │                      Database                                   │      │
│  │  - achievements table (definitions)                           │      │
│  │  - user_achievements table (progress)                         │      │
│  │  - achievement_history table (unlock log)                     │      │
│  │  - user_rewards table (granted rewards)                       │      │
│  └────────────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Data Model Design

### 1. Core Domain Models

#### Achievement

```kotlin
package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Achievement definition - static metadata about an achievement
 * This is immutable and seeded from data/seed/AchievementSeeder.kt
 */
@Immutable
data class Achievement(
    val id: String,                    // Unique ID: "first_steps", "combo_master"
    val name: String,                  // Display name: "First Steps"
    val description: String,           // User-facing description
    val icon: String,                  // Emoji or resource ID: "🌟"
    val category: AchievementCategory, // PROGRESS, PERFORMANCE, COMBO, STREAK, SPECIAL
    val tier: AchievementTier,         // BRONZE, SILVER, GOLD, PLATINUM
    val requirement: AchievementRequirement,  // Sealed class for unlock condition
    val reward: AchievementReward,     // Sealed class for reward
    val isHidden: Boolean = false,     // Secret achievement
    val parentId: String? = null       // For chained achievements
)

/**
 * Achievement categories for filtering and display
 */
enum class AchievementCategory {
    PROGRESS,       // Learning milestones (levels, words mastered)
    PERFORMANCE,    // Excellence (perfect levels, high accuracy)
    COMBO,          // Streak achievements
    STREAK,         // Consistency (daily practice)
    SPECIAL         // Unique challenges
}

/**
 * Achievement difficulty tiers
 * Maps to visual styling and reward magnitude
 */
enum class AchievementTier(val stars: Int, val color: Long) {
    BRONZE(1, 0xFFCD7F32),   // Easy achievements
    SILVER(2, 0xFFC0C0C0),   // Medium achievements
    GOLD(3, 0xFFFFD700),     // Hard achievements
    PLATINUM(4, 0xFFE5E4E2)  // Very hard achievements
}
```

#### AchievementRequirement (Sealed Class)

```kotlin
package com.wordland.domain.model

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
        val minStars: Int = 1
    ) : AchievementRequirement() {
        override val targetValue: Int = count
        override fun getProgressText(current: Int): String = "$current/$count levels"
    }

    /**
     * Master N words (memory strength >= threshold)
     */
    data class MasterWords(
        val count: Int,
        val memoryStrengthThreshold: Int = 80
    ) : AchievementRequirement() {
        override val targetValue: Int = count
        override fun getProgressText(current: Int): String = "$current/$count words"
    }

    /**
     * Achieve N consecutive correct answers
     */
    data class ComboMilestone(
        val comboCount: Int
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
        val minWordsPerDay: Int = 1
    ) : AchievementRequirement() {
        override val targetValue: Int = days
        override fun getProgressText(current: Int): String = "$current/$days days"
    }

    /**
     * Complete N perfect levels (all 3-star words)
     */
    data class PerfectLevel(
        val count: Int = 1
    ) : AchievementRequirement() {
        override val targetValue: Int = count
        override fun getProgressText(current: Int): String = "$current/$count levels"
    }

    /**
     * Complete level without hints
     */
    data class NoHintsLevel(
        val wordCount: Int = 6
    ) : AchievementRequirement() {
        override val targetValue: Int = wordCount
        override fun getProgressText(current: Int): String = "$current/$wordCount words"
    }

    /**
     * Max memory strength (100) on N words
     */
    data class MaxMemoryStrength(
        val count: Int,
        val strength: Int = 100
    ) : AchievementRequirement() {
        override val targetValue: Int = count
        override fun getProgressText(current: Int): String = "$current/$count words"
    }

    /**
     * Complete all levels in an island
     */
    data class CompleteIsland(
        val islandId: String,
        val levelCount: Int
    ) : AchievementRequirement() {
        override val targetValue: Int = levelCount
        override fun getProgressText(current: Int): String = "$current/$levelCount levels"
    }

    /**
     * Unlock N different islands
     */
    data class UnlockIslands(
        val count: Int
    ) : AchievementRequirement() {
        override val targetValue: Int = count
        override fun getProgressText(current: Int): String = "$current/$count islands"
    }
}
```

#### AchievementReward (Sealed Class)

```kotlin
package com.wordland.domain.model

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
        val displayName: String
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Title: $displayName"
    }

    /**
     * Permanent badge on profile
     */
    data class Badge(
        val badgeId: String,
        val iconName: String,
        val displayName: String
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Badge: $displayName $iconName"
    }

    /**
     * Unlock exclusive pet
     */
    data class PetUnlock(
        val petId: String,
        val petName: String,
        val petIcon: String
    ) : AchievementReward() {
        override fun getDisplayText(): String = "Pet: $petName $petIcon"
    }

    /**
     * Multiple rewards combined
     */
    data class Multiple(
        val rewards: List<AchievementReward>
    ) : AchievementReward() {
        override fun getDisplayText(): String {
            return rewards.joinToString(", ") { it.getDisplayText() }
        }
    }
}
```

#### UserAchievement

```kotlin
package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User's progress on an achievement
 * Stored in database, updated as user progresses
 */
@Immutable
@Entity(
    tableName = "user_achievements",
    primaryKeys = ["userId", "achievementId"]
)
data class UserAchievement(
    val userId: String,
    val achievementId: String,

    /**
     * Whether achievement is unlocked
     */
    val isUnlocked: Boolean = false,

    /**
     * Current progress value (toward targetValue)
     */
    val progress: Int = 0,

    /**
     * Target value from AchievementRequirement.targetValue
     */
    val target: Int = 1,

    /**
     * When achievement was unlocked (null if locked)
     */
    val unlockedAt: Long? = null,

    /**
     * Last time progress was updated
     */
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Calculate progress percentage (0-100)
     */
    val progressPercentage: Float
        get() = if (target > 0) {
            (progress.toFloat() / target.toFloat()).coerceAtMost(1f)
        } else {
            if (isUnlocked) 1f else 0f
        }

    /**
     * Get progress display text
     */
    fun getProgressText(requirement: AchievementRequirement): String {
        return requirement.getProgressText(progress)
    }
}
```

#### GameEvent (Sealed Class)

```kotlin
package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Game events that trigger achievement checks
 * Sealed class for type-safe event handling
 */
@Immutable
sealed class GameEvent {
    /**
     * User ID for this event
     */
    abstract val userId: String

    /**
     * Timestamp when event occurred
     */
    abstract val timestamp: Long

    /**
     * Word mastered (memory strength >= threshold)
     */
    data class WordMastered(
        override val userId: String,
        val wordId: String,
        val memoryStrength: Int,
        val stars: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Level completed
     */
    data class LevelComplete(
        override val userId: String,
        val levelId: String,
        val islandId: String,
        val levelNumber: Int,
        val stars: Int,
        val allThreeStar: Boolean,
        val hintsUsed: Int,
        val wordCount: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Combo milestone reached
     */
    data class ComboAchieved(
        override val userId: String,
        val comboCount: Int,
        val averageResponseTime: Long,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Daily streak updated
     */
    data class StreakUpdate(
        override val userId: String,
        val streakDays: Int,
        val freezeDaysUsed: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Answer submitted (for tracking)
     */
    data class AnswerSubmitted(
        override val userId: String,
        val wordId: String,
        val isCorrect: Boolean,
        val isGuessing: Boolean,
        val responseTime: Long,
        val hintUsed: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()

    /**
     * Island completed
     */
    data class IslandComplete(
        override val userId: String,
        val islandId: String,
        val levelsCompleted: Int,
        val totalLevels: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : GameEvent()
}
```

---

## Database Schema

### Room Entities

#### AchievementEntity

```kotlin
package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,

    // Display info
    val name: String,
    val description: String,
    val icon: String,

    // Classification
    val category: String,           // AchievementCategory.name
    val tier: String,               // AchievementTier.name

    // Requirement (stored as JSON)
    val requirementType: String,    // AchievementRequirement subclass name
    val requirementData: String,    // JSON: specific requirement fields

    // Reward (stored as JSON)
    val rewardType: String,         // AchievementReward subclass name
    val rewardData: String,         // JSON: specific reward fields

    // Flags
    val isHidden: Boolean = false,
    val parentId: String? = null
)

/**
 * Convert to domain model
 */
fun AchievementEntity.toDomainModel(): Achievement {
    // Parse requirement from JSON
    val requirement = AchievementRequirementParser.parse(
        type = requirementType,
        data = requirementData
    )

    // Parse reward from JSON
    val reward = AchievementRewardParser.parse(
        type = rewardType,
        data = rewardData
    )

    return Achievement(
        id = id,
        name = name,
        description = description,
        icon = icon,
        category = AchievementCategory.valueOf(category),
        tier = AchievementTier.valueOf(tier),
        requirement = requirement,
        reward = reward,
        isHidden = isHidden,
        parentId = parentId
    )
}
```

#### UserAchievementEntity

```kotlin
package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_achievements",
    primaryKeys = ["userId", "achievementId"]
)
data class UserAchievementEntity(
    val userId: String,
    val achievementId: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1,
    val unlockedAt: Long? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun UserAchievementEntity.toDomainModel(): UserAchievement = UserAchievement(
    userId = userId,
    achievementId = achievementId,
    isUnlocked = isUnlocked,
    progress = progress,
    target = target,
    unlockedAt = unlockedAt,
    lastUpdated = lastUpdated
)
```

### DAO Interface

```kotlin
package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    // === Achievement definitions ===

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE category = :category")
    suspend fun getAchievementsByCategory(category: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    // === User progress ===

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    suspend fun getUserAchievements(userId: String): List<UserAchievementEntity>

    @Query("SELECT * FROM user_achievements WHERE userId = :userId AND achievementId = :achievementId")
    suspend fun getUserAchievement(userId: String, achievementId: String): UserAchievementEntity?

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    fun getUserAchievementsFlow(userId: String): Flow<List<UserAchievementEntity>>

    // === Unlocked achievements ===

    @Query("""
        SELECT achievements.* FROM achievements
        INNER JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        WHERE user_achievements.userId = :userId
        AND user_achievements.isUnlocked = 1
        ORDER BY user_achievements.unlockedAt DESC
    """)
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity>

    @Query("""
        SELECT achievements.* FROM achievements
        INNER JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        WHERE user_achievements.userId = :userId
        AND user_achievements.isUnlocked = 1
        ORDER BY user_achievements.unlockedAt DESC
    """)
    fun getUnlockedAchievementsFlow(userId: String): Flow<List<AchievementEntity>>

    // === Locked achievements (in progress) ===

    @Query("""
        SELECT achievements.*, user_achievements.progress, user_achievements.target
        FROM achievements
        LEFT JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        AND user_achievements.userId = :userId
        WHERE user_achievements.isUnlocked = 0
        OR user_achievements.isUnlocked IS NULL
        ORDER BY achievements.tier ASC
    """)
    suspend fun getLockedAchievements(userId: String): List<AchievementWithProgress>

    // === Progress operations ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserAchievementEntity)

    @Query("""
        UPDATE user_achievements
        SET isUnlocked = 1,
            unlockedAt = :timestamp,
            lastUpdated = :timestamp
        WHERE userId = :userId
        AND achievementId = :achievementId
    """)
    suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_achievements
        SET progress = :progress,
            lastUpdated = :timestamp
        WHERE userId = :userId
        AND achievementId = :achievementId
    """)
    suspend fun updateProgress(
        userId: String,
        achievementId: String,
        progress: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    // === Batch operations ===

    @Transaction
    suspend fun unlockWithProgress(
        userId: String,
        achievementId: String,
        finalProgress: Int
    ) {
        insertOrUpdateProgress(
            UserAchievementEntity(
                userId = userId,
                achievementId = achievementId,
                isUnlocked = true,
                progress = finalProgress,
                unlockedAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    // === Stats queries ===

    @Query("SELECT COUNT(*) FROM user_achievements WHERE userId = :userId AND isUnlocked = 1")
    suspend fun getUnlockedCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementCount(): Int

    @Query("""
        SELECT COUNT(*) FROM user_achievements
        WHERE userId = :userId
        AND achievementId = :achievementId
        AND isUnlocked = 1
    """)
    suspend fun isUnlocked(userId: String, achievementId: String): Int
}

/**
 * Achievement with user progress info
 */
data class AchievementWithProgress(
    val achievement: AchievementEntity,
    val progress: Int?,
    val target: Int?
)
```

---

## Service Layer Design

### AchievementRepository

```kotlin
package com.wordland.data.repository

import com.wordland.data.dao.AchievementDao
import com.wordland.data.entity.toDomainModel
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.AchievementCategory
import com.wordland.domain.model.UserAchievement
import com.wordland.data.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao,
    private val progressRepository: ProgressRepository
) {

    /**
     * Get all achievement definitions
     */
    suspend fun getAllAchievements(): List<Achievement> {
        return achievementDao.getAllAchievements().map { it.toDomainModel() }
    }

    /**
     * Get achievements by category
     */
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return achievementDao.getAchievementsByCategory(category.name)
            .map { it.toDomainModel() }
    }

    /**
     * Get user's achievement progress
     */
    suspend fun getUserAchievements(userId: String): List<UserAchievement> {
        return achievementDao.getUserAchievements(userId).map { it.toDomainModel() }
    }

    /**
     * Get user's achievement progress as Flow
     */
    fun getUserAchievementsFlow(userId: String): Flow<List<UserAchievement>> {
        return achievementDao.getUserAchievementsFlow(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    /**
     * Get unlocked achievements
     */
    suspend fun getUnlockedAchievements(userId: String): List<Achievement> {
        return achievementDao.getUnlockedAchievements(userId).map { it.toDomainModel() }
    }

    /**
     * Get unlocked achievements as Flow
     */
    fun getUnlockedAchievementsFlow(userId: String): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievementsFlow(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    /**
     * Get locked (in-progress) achievements
     */
    suspend fun getLockedAchievements(userId: String): List<AchievementWithProgress> {
        return achievementDao.getLockedAchievements(userId)
    }

    /**
     * Update progress for an achievement
     */
    suspend fun updateProgress(
        userId: String,
        achievementId: String,
        progress: Int
    ) {
        val existing = achievementDao.getUserAchievement(userId, achievementId)

        if (existing != null) {
            achievementDao.updateProgress(userId, achievementId, progress)
        } else {
            val achievement = achievementDao.getAchievement(achievementId)
            achievementDao.insertOrUpdateProgress(
                UserAchievementEntity(
                    userId = userId,
                    achievementId = achievementId,
                    isUnlocked = false,
                    progress = progress,
                    target = achievement?.requirementData?.toInt() ?: 1,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Unlock an achievement
     */
    suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        finalProgress: Int
    ) {
        achievementDao.unlockWithProgress(userId, achievementId, finalProgress)
    }

    /**
     * Check if achievement is unlocked
     */
    suspend fun isUnlocked(userId: String, achievementId: String): Boolean {
        return achievementDao.isUnlocked(userId, achievementId) > 0
    }

    /**
     * Get achievement completion stats
     */
    suspend fun getCompletionStats(userId: String): CompletionStats {
        val unlocked = achievementDao.getUnlockedCount(userId)
        val total = achievementDao.getTotalAchievementCount()
        return CompletionStats(
            unlocked = unlocked,
            total = total,
            percentage = if (total > 0) unlocked.toFloat() / total else 0f
        )
    }
}

data class CompletionStats(
    val unlocked: Int,
    val total: Int,
    val percentage: Float
)

data class AchievementWithProgress(
    val achievement: Achievement,
    val progress: Int,
    val target: Int
)
```

---

## Use Case Layer

### CheckAchievementsUseCase

```kotlin
package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.AchievementRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.achievement.AchievementTracker
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.GameEvent
import com.wordland.domain.model.Result
import javax.inject.Inject

/**
 * Check achievements after a game event
 * Returns list of newly unlocked achievements
 */
class CheckAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val achievementTracker: AchievementTracker,
    private val grantRewardUseCase: GrantRewardUseCase
) {

    suspend operator fun invoke(
        userId: String,
        event: GameEvent
    ): Result<List<Achievement>> {
        return try {
            val newlyUnlocked = achievementTracker.checkAchievements(
                userId = userId,
                event = event,
                achievementRepository = achievementRepository
            )

            // Grant rewards for newly unlocked achievements
            for (achievement in newlyUnlocked) {
                grantRewardUseCase(userId, achievement.reward)
            }

            Result.Success(newlyUnlocked)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### GetAchievementsUseCase

```kotlin
package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.AchievementRepository
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.AchievementCategory
import com.wordland.domain.model.Result
import javax.inject.Inject

/**
 * Get achievements for display
 * Returns grouped achievements with progress
 */
class GetAchievementsUseCase @Inject constructor(
    private val achievementRepository: AchievementRepository
) {

    suspend operator fun invoke(
        userId: String,
        category: AchievementCategory? = null
    ): Result<AchievementGalleryData> {
        return try {
            val allAchievements = if (category != null) {
                achievementRepository.getAchievementsByCategory(category)
            } else {
                achievementRepository.getAllAchievements()
            }

            val userProgress = achievementRepository.getUserAchievements(userId)
            val progressMap = userProgress.associateBy { it.achievementId }

            val grouped = AchievementGalleryData(
                unlocked = allAchievements.filter { achievement ->
                    progressMap[achievement.id]?.isUnlocked == true
                },
                inProgress = allAchievements.filter { achievement ->
                    val progress = progressMap[achievement.id]
                    progress != null && !progress.isUnlocked
                },
                locked = allAchievements.filter { achievement ->
                    progressMap[achievement.id] == null
                },
                completionStats = achievementRepository.getCompletionStats(userId)
            )

            Result.Success(grouped)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

data class AchievementGalleryData(
    val unlocked: List<Achievement>,
    val inProgress: List<Achievement>,
    val locked: List<Achievement>,
    val completionStats: CompletionStats
)
```

---

## Event Flow

### Achievement Check Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Gameplay Occurs                                   │
│                    (User answers question)                               │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    LearningViewModel                                     │
│  submitAnswer() → LearnWordResult                                       │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    Create GameEvent                                      │
│  GameEvent.WordMastered(...)                                            │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 CheckAchievementsUseCase                                 │
│  invoke(userId, event)                                                  │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  AchievementTracker                                      │
│  - Check which achievements are affected by this event                  │
│  - Calculate new progress for each                                      │
│  - Determine if any are unlocked                                        │
└────────────────────────────┬────────────────────────────────────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
            ┌──────────────┐  ┌──────────────┐
            │ Update DB    │  │ Grant Reward │
            │ (progress)   │  │ (stars, etc) │
            └──────────────┘  └──────────────┘
                    │                 │
                    └────────┬────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                  Show Notification                                       │
│  AchievementPopup displays for each newly unlocked                      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Performance Considerations

### 1. Database Indexing

```sql
-- Indexes for fast queries
CREATE INDEX idx_user_achievements_user_id ON user_achievements(userId);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(isUnlocked);
CREATE INDEX idx_user_achievements_unlocked_at ON user_achievements(unlockedAt);
CREATE INDEX idx_achievements_category ON achievements(category);
CREATE INDEX idx_achievements_tier ON achievements(tier);
```

### 2. Batch Operations

- Achievement checks are batched after gameplay events
- Progress updates use transactional batches
- Notification queue prevents UI spam

### 3. Caching Strategy

```kotlin
@Singleton
class AchievementCache @Inject constructor(
    private val achievementRepository: AchievementRepository
) {
    private val achievementsCache = mutableMapOf<String, Achievement>()

    suspend fun getAllAchievements(): List<Achievement> {
        if (achievementsCache.isEmpty()) {
            achievementsCache.putAll(
                achievementRepository.getAllAchievements()
                    .associateBy { it.id }
            )
        }
        return achievementsCache.values.toList()
    }

    fun invalidate() {
        achievementsCache.clear()
    }
}
```

---

## Testing Strategy

### Unit Tests

1. **AchievementRequirement tests**
   - Progress text generation
   - Target value calculation

2. **AchievementReward tests**
   - Display text generation
   - Reward type validation

3. **UserAchievement tests**
   - Progress percentage calculation
   - Unlock status checks

### Integration Tests

1. **AchievementDao tests**
   - CRUD operations
   - Query validation
   - Transaction tests

2. **AchievementRepository tests**
   - End-to-end progress tracking
   - Unlock flow
   - Stats calculation

### Instrumentation Tests

1. **AchievementTracker tests**
   - Event processing
   - Progress calculation
   - Unlock detection

2. **UI tests**
   - AchievementScreen display
   - Popup animations
   - Filter functionality

---

## Migration Path

### Phase 1: Foundation (Week 1)

- [ ] Create domain models (Achievement, UserAchievement, GameEvent)
- [ ] Create database entities and DAO
- [ ] Create AchievementRepository
- [ ] Write unit tests for models

### Phase 2: Core Logic (Week 2)

- [ ] Implement AchievementTracker
- [ ] Implement CheckAchievementsUseCase
- [ ] Create achievement definitions seed
- [ ] Write integration tests

### Phase 3: UI Integration (Week 3)

- [ ] Create AchievementViewModel
- [ ] Create AchievementPopup component
- [ ] Create AchievementScreen
- [ ] Integrate with LearningViewModel
- [ ] Write UI tests

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Documents**: ACHIEVEMENT_TRIGGERS.md, ACHIEVEMENT_UI_DESIGN.md
