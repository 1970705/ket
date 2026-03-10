# Achievement System - Implementation Specifications

**Author**: Game Designer
**Date**: 2026-02-17
**Status**: Ready for Implementation
**Priority**: P1 (Core feature)

---

## Overview

Complete implementation specifications for the achievement system based on approved design (Task #4).

**Anti-Addiction Features**:
- Streak freezes: 2 grace days before streak breaks
- Session reminders: "Take a break!" after 20 minutes
- No FOMO mechanics: No limited-time events
- Intrinsic motivation: Celebrate effort, not luck

---

## 1. Data Models

### Achievement.kt

```kotlin
package com.wordland.domain.model

enum class AchievementCategory {
    MILESTONE,      // Learning progress
    SKILL,          // Performance-based
    SOCIAL,         // Community (future)
    COLLECTION,     // Content completion
    SPECIAL         // Limited/events (avoided)
}

enum class AchievementTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
}

data class Achievement(
    val id: String,                    // "first_steps", "word_collector_50"
    val name: String,                  // "First Steps"
    val description: String,           // "Complete your first level"
    val icon: String,                  // Icon resource name or emoji
    val category: AchievementCategory,
    val tier: AchievementTier,
    val isHidden: Boolean = false,     // Secret achievements
    val parentId: String? = null       // For multi-tier achievements
)
```

### UserAchievement.kt

```kotlin
package com.wordland.domain.model

data class UserAchievement(
    val userId: String,
    val achievementId: String,
    val unlockedAt: Long,              // Timestamp
    val progress: Float = 1f,          // For progress tracking
    val progressTarget: Int = 1        // Target for progress-based
)
```

### LearningEvent.kt

```kotlin
package com.wordland.domain.model

sealed class LearningEvent

data class LevelCompleteEvent(
    val levelNumber: Int,
    val islandId: String,
    val stars: Int
) : LearningEvent()

data class WordLearnedEvent(
    val wordId: String,
    val stars: Int,
    val responseTimeMs: Long
) : LearningEvent()

data class StreakUpdateEvent(
    val days: Int
) : LearningEvent()

data class HintUsedEvent(
    val wordId: String,
    val hintLevel: Int
) : LearningEvent()
```

---

## 2. Database Schema

### AchievementDao.kt

```kotlin
package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievement(achievementId: String): AchievementEntity?

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    suspend fun getUserAchievements(userId: String): List<UserAchievementEntity>

    @Query("""
        SELECT achievements.* FROM achievements
        INNER JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        WHERE user_achievements.userId = :userId
    """)
    suspend fun getUnlockedAchievements(userId: String): List<AchievementEntity>

    @Query("""
        SELECT achievements.* FROM achievements
        LEFT JOIN user_achievements
        ON achievements.id = user_achievements.achievementId
        AND user_achievements.userId = :userId
        WHERE user_achievements.userId IS NULL
    """)
    suspend fun getLockedAchievements(userId: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockAchievement(achievement: UserAchievementEntity)

    @Query("DELETE FROM user_achievements WHERE userId = :userId")
    suspend fun clearUserAchievements(userId: String)
}
```

### AchievementEntity.kt

```kotlin
package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,           // AchievementCategory.name
    val tier: String,               // AchievementTier.name
    val isHidden: Boolean,
    val parentId: String?
)

// Extension to convert to domain model
fun AchievementEntity.toDomainModel(): Achievement {
    return Achievement(
        id = id,
        name = name,
        description = description,
        icon = icon,
        category = AchievementCategory.valueOf(category),
        tier = AchievementTier.valueOf(tier),
        isHidden = isHidden,
        parentId = parentId
    )
}
```

### UserAchievementEntity.kt

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
    val unlockedAt: Long,
    val progress: Float,
    val progressTarget: Int
)

// Extension to convert to domain model
fun UserAchievementEntity.toDomainModel(): UserAchievement {
    return UserAchievement(
        userId = userId,
        achievementId = achievementId,
        unlockedAt = unlockedAt,
        progress = progress,
        progressTarget = progressTarget
    )
}
```

---

## 3. Achievement Definitions

### AchievementDefinitions.kt

```kotlin
package com.wordland.data.seed

import com.wordland.domain.model.Achievement
import com.wordland.domain.model.AchievementCategory
import com.wordland.domain.model.AchievementTier

object AchievementDefinitions {

    val ALL_ACHIEVEMENTS = listOf(
        // === MILESTONES ===
        Achievement(
            id = "first_steps",
            name = "First Steps",
            description = "Complete your first level",
            icon = "🌱",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.BRONZE
        ),
        Achievement(
            id = "word_collector_50",
            name = "Word Collector",
            description = "Learn 50 words",
            icon = "📚",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "word_collector_100",
            name = "Vocabulary Builder",
            description = "Learn 100 words",
            icon = "🏗️",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.GOLD,
            parentId = "word_collector_50"
        ),
        Achievement(
            id = "ket_master",
            name = "Word Wizard",
            description = "Learn all KET words",
            icon = "🧙",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.PLATINUM
        ),
        Achievement(
            id = "streak_7",
            name = "Daily Learner",
            description = "Practice for 7 days in a row",
            icon = "🔥",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "streak_30",
            name = "Month Master",
            description = "Practice for 30 days in a row",
            icon = "📅",
            category = AchievementCategory.MILESTONE,
            tier = AchievementTier.GOLD,
            parentId = "streak_7"
        ),

        // === SKILLS ===
        Achievement(
            id = "perfect_10",
            name = "Perfect Spell",
            description = "Earn 3 stars on 10 different words",
            icon = "⭐",
            category = AchievementCategory.SKILL,
            tier = AchievementTier.SILVER
        ),
        Achievement(
            id = "hint_free_level",
            name = "Hint-Free Hero",
            description = "Complete a level without using hints",
            icon = "🚀",
            category = AchievementCategory.SKILL,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "thoughtful_learner",
            name = "Thoughtful Learner",
            description = "Maintain 80% 3-star rate over 50 answers",
            icon = "🧠",
            category = AchievementCategory.SKILL,
            tier = AchievementTier.GOLD
        ),

        // === COLLECTION ===
        Achievement(
            id = "island_master_look",
            name = "Look Explorer",
            description = "Complete all levels in Look Island",
            icon = "👀",
            category = AchievementCategory.COLLECTION,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "island_master_make",
            name = "Make Creator",
            description = "Complete all levels in MakeLake Island",
            icon = "🛠️",
            category = AchievementCategory.COLLECTION,
            tier = AchievementTier.GOLD
        ),
        Achievement(
            id = "multi_island",
            name = "Multi-Island Hopper",
            description = "Complete levels in 3 different islands",
            icon = "🚢",
            category = AchievementCategory.COLLECTION,
            tier = AchievementTier.SILVER
        )
    )
}
```

---

## 4. Achievement Tracking Logic

### AchievementTracker.kt

```kotlin
package com.wordland.domain.achievement

import com.wordland.data.dao.AchievementDao
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.LearningEvent
import com.wordland.domain.model.LevelCompleteEvent
import com.wordland.domain.model.WordLearnedEvent
import com.wordland.domain.model.StreakUpdateEvent
import com.wordland.domain.model.HintUsedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AchievementTracker(
    private val achievementDao: AchievementDao,
    private val progressRepository: ProgressRepository
) {

    suspend fun checkAchievements(
        userId: String,
        event: LearningEvent
    ): List<Achievement> = withContext(Dispatchers.IO) {
        val unlocked = mutableListOf<Achievement>()
        val locked = achievementDao.getLockedAchievements(userId)
        val lockedMap = locked.associateBy { it.id }

        when (event) {
            is LevelCompleteEvent -> checkLevelAchievements(
                userId, event, lockedMap, unlocked
            )
            is WordLearnedEvent -> checkWordAchievements(
                userId, event, lockedMap, unlocked
            )
            is StreakUpdateEvent -> checkStreakAchievements(
                userId, event, lockedMap, unlocked
            )
            is HintUsedEvent -> {
                // Check hint-free achievement (fail if hint used)
                // This would need state tracking per level
            }
        }

        unlocked
    }

    private suspend fun checkLevelAchievements(
        userId: String,
        event: LevelCompleteEvent,
        lockedMap: Map<String, AchievementEntity>,
        unlocked: MutableList<Achievement>
    ) {
        // First Steps
        lockedMap["first_steps"]?.let {
            if (event.levelNumber == 1) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }

        // Island completions
        when (event.islandId) {
            "look_island" -> {
                val completedLevels = progressRepository.getCompletedLevelCount(
                    userId, "look_island"
                )
                if (completedLevels >= 5) {  // All 5 levels
                    lockedMap["island_master_look"]?.let {
                        unlockAchievement(userId, it.toDomainModel(), unlocked)
                    }
                }
            }
            "make_lake" -> {
                val completedLevels = progressRepository.getCompletedLevelCount(
                    userId, "make_lake"
                )
                if (completedLevels >= 10) {  // All 10 levels
                    lockedMap["island_master_make"]?.let {
                        unlockAchievement(userId, it.toDomainModel(), unlocked)
                    }
                }
            }
        }

        // Multi-island hopper
        val islandsCompleted = progressRepository.getCompletedIslandCount(userId)
        if (islandsCompleted >= 3) {
            lockedMap["multi_island"]?.let {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }
    }

    private suspend fun checkWordAchievements(
        userId: String,
        event: WordLearnedEvent,
        lockedMap: Map<String, AchievementEntity>,
        unlocked: MutableList<Achievement>
    ) {
        // Word Collector (50 words)
        lockedMap["word_collector_50"]?.let {
            val wordCount = progressRepository.getUniqueWordsLearned(userId)
            if (wordCount >= 50) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }

        // Vocabulary Builder (100 words)
        lockedMap["word_collector_100"]?.let {
            val wordCount = progressRepository.getUniqueWordsLearned(userId)
            if (wordCount >= 100) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }

        // Perfect 10 (3 stars on 10 words)
        lockedMap["perfect_10"]?.let {
            val threeStarCount = progressRepository.getThreeStarWordCount(userId)
            if (threeStarCount >= 10) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }

        // Thoughtful Learner (80% 3-star rate over 50 answers)
        lockedMap["thoughtful_learner"]?.let {
            val stats = progressRepository.getAnswerStats(userId)
            if (stats.totalAnswers >= 50 &&
                stats.threeStarRate >= 0.8f) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }
    }

    private suspend fun checkStreakAchievements(
        userId: String,
        event: StreakUpdateEvent,
        lockedMap: Map<String, AchievementEntity>,
        unlocked: MutableList<Achievement>
    ) {
        // Daily Learner (7 day streak)
        lockedMap["streak_7"]?.let {
            if (event.days >= 7) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }

        // Month Master (30 day streak)
        lockedMap["streak_30"]?.let {
            if (event.days >= 30) {
                unlockAchievement(userId, it.toDomainModel(), unlocked)
            }
        }
    }

    private suspend fun unlockAchievement(
        userId: String,
        achievement: Achievement,
        unlocked: MutableList<Achievement>
    ) {
        achievementDao.unlockAchievement(
            UserAchievementEntity(
                userId = userId,
                achievementId = achievement.id,
                unlockedAt = System.currentTimeMillis(),
                progress = 1f,
                progressTarget = 1
            )
        )
        unlocked.add(achievement)
    }
}
```

---

## 5. UI Components

### AchievementPopup.kt

```kotlin
package com.wordland.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wordland.domain.model.Achievement

@Composable
fun AchievementPopup(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .size(320.dp, 220.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Text(
                    text = achievement.icon,
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Achievement Unlocked badge
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Achievement Unlocked!",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
```

### AchievementItem.kt

```kotlin
package com.wordland.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wordland.domain.model.Achievement

@Composable
fun AchievementItem(
    achievement: Achievement,
    isUnlocked: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = if (isUnlocked) achievement.icon else "🔒",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(if (isUnlocked) 1f else 0.3f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tier badge
            TierBadge(achievement.tier, isUnlocked)
        }
    }
}

@Composable
fun TierBadge(tier: AchievementTier, isUnlocked: Boolean) {
    val (color, label) = when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32) to "Bronze"
        AchievementTier.SILVER -> Color(0xFFC0C0C0) to "Silver"
        AchievementTier.GOLD -> Color(0xFFFFD700) to "Gold"
        AchievementTier.PLATINUM -> Color(0xFFE5E4E2) to "Platinum"
    }

    Surface(
        color = if (isUnlocked) color else Color.Gray,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(24.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) Color.Black else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
```

---

## 6. Anti-Addiction Implementation

### StreakManager.kt

```kotlin
package com.wordland.domain.achievement

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class StreakManager {

    companion object {
        private const val FREEZE_DAYS = 2  // Allow 2 grace days
    }

    /**
     * Calculate streak with freeze days
     * Child-friendly: streak doesn't break immediately
     */
    fun calculateStreak(
        lastActiveDate: Long,
        currentStreak: Int,
        freezeDaysUsed: Int
    ): StreakResult {
        val daysSince = daysBetween(lastActiveDate, System.currentTimeMillis())

        return when {
            daysSince == 1L -> {
                // Consecutive day
                StreakResult(
                    newStreak = currentStreak + 1,
                    freezeDaysUsed = 0  // Reset freezes
                )
            }
            daysSince <= (FREEZE_DAYS - freezeDaysUsed + 1) -> {
                // Within freeze period - maintain streak
                StreakResult(
                    newStreak = currentStreak,
                    freezeDaysUsed = freezeDaysUsed + (daysSince.toInt() - 1).coerceAtLeast(0)
                )
            }
            else -> {
                // Streak broken - start fresh without shame
                StreakResult(
                    newStreak = 1,
                    freezeDaysUsed = 0
                )
            }
        }
    }

    private fun daysBetween(from: Long, to: Long): Long {
        val diff = to - from
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Get today's date string (YYYY-MM-DD)
     */
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(System.currentTimeMillis())
    }
}

data class StreakResult(
    val newStreak: Int,
    val freezeDaysUsed: Int
)
```

---

## 7. UseCase Layer

### CheckAchievementsUseCase.kt

```kotlin
package com.wordland.domain.usecase.usecases

import com.wordland.domain.achievement.AchievementTracker
import com.wordland.domain.achievement.AchievementNotificationService
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.LearningEvent
import javax.inject.Inject

class CheckAchievementsUseCase @Inject constructor(
    private val achievementTracker: AchievementTracker,
    private val notificationService: AchievementNotificationService
) {
    suspend operator fun invoke(
        userId: String,
        event: LearningEvent
    ): List<Achievement> {
        val newlyUnlocked = achievementTracker.checkAchievements(userId, event)

        if (newlyUnlocked.isNotEmpty()) {
            // Show notification for each newly unlocked achievement
            newlyUnlocked.forEach { achievement ->
                notificationService.showAchievementUnlocked(achievement)
            }
        }

        return newlyUnlocked
    }
}
```

### GetAchievementsUseCase.kt

```kotlin
package com.wordland.domain.usecase.usecases

import com.wordland.data.dao.AchievementDao
import com.wordland.domain.model.Achievement
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val achievementDao: AchievementDao
) {
    suspend operator fun invoke(userId: String): AchievementResult {
        val unlocked = achievementDao.getUnlockedAchievements(userId)
            .map { it.toDomainModel() }
        val locked = achievementDao.getLockedAchievements(userId)
            .map { it.toDomainModel() }

        return AchievementResult(
            unlocked = unlocked,
            locked = locked,
            total = unlocked.size + locked.size,
            progress = unlocked.size.toFloat() / (unlocked.size + locked.size)
        )
    }
}

data class AchievementResult(
    val unlocked: List<Achievement>,
    val locked: List<Achievement>,
    val total: Int,
    val progress: Float
)
```

---

## 8. Implementation Roadmap

### Phase 1: Foundation (P1)

**Data Layer** (~2 hours):
- [ ] Create AchievementEntity, UserAchievementEntity
- [ ] Create AchievementDao with all queries
- [ ] Update database version and migration

**Domain Layer** (~3 hours):
- [ ] Create Achievement, UserAchievement models
- [ ] Create LearningEvent sealed class
- [ ] Implement AchievementTracker logic
- [ ] Implement StreakManager

**Seeding** (~1 hour):
- [ ] Create AchievementSeeder.kt
- [ ] Add seeding to AppDataInitializer

### Phase 2: Core Features (P1)

**UseCases** (~1 hour):
- [ ] CheckAchievementsUseCase
- [ ] GetAchievementsUseCase

**UI Components** (~5 hours):
- [ ] AchievementPopup component
- [ ] AchievementItem component
- [ ] AchievementScreen with ViewModel
- [ ] Integrate with LearningViewModel

### Phase 3: Enhancement (P2)

**Profile Display** (~2 hours):
- [ ] ProfileBadgeGrid component
- [ ] BadgeCard component
- [ ] Add to ProgressScreen

**Anti-Addiction** (~2 hours):
- [ ] Session reminder logic (20 min check)
- [ ] Update StreakManager with freeze days
- [ ] UI for streak freeze indication

**Total Estimated Effort**: ~16 hours

---

## 9. Files to Create

```
domain/model/
  - Achievement.kt (NEW)
  - UserAchievement.kt (NEW)
  - LearningEvent.kt (NEW)

data/entity/
  - AchievementEntity.kt (NEW)
  - UserAchievementEntity.kt (NEW)

data/dao/
  - AchievementDao.kt (NEW)

data/seed/
  - AchievementSeeder.kt (NEW)

domain/achievement/
  - AchievementTracker.kt (NEW)
  - StreakManager.kt (NEW)
  - AchievementNotificationService.kt (NEW)

domain/usecase/usecases/
  - CheckAchievementsUseCase.kt (NEW)
  - GetAchievementsUseCase.kt (NEW)

ui/screens/
  - AchievementScreen.kt (NEW)
  - AchievementViewModel.kt (NEW)

ui/components/
  - AchievementPopup.kt (NEW)
  - AchievementItem.kt (NEW)
  - TierBadge.kt (NEW)
  - ProfileBadgeGrid.kt (NEW)
```

---

## 10. Testing Strategy

**Unit Tests**:
- [ ] StreakManager streak calculation with freeze days
- [ ] AchievementTracker event handling
- [ ] All achievement unlock conditions

**Integration Tests**:
- [ ] AchievementDao database operations
- [ ] CheckAchievementsUseCase flow

**UI Tests**:
- [ ] AchievementPopup display
- [ ] AchievementScreen rendering
- [ ] Achievement item locked/unlocked states

---

**Last Updated**: 2026-02-17
**Status**: Ready for Implementation
**Next Step**: Assign to android-engineer for P1 implementation
