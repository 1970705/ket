# Achievement System Design - Wordland KET
**Date**: 2026-02-18
**Designer**: Claude (game-designer)
**Priority**: P1 (High engagement, retention driver)
**Target Audience**: Children aged 10

---

## Executive Summary

A comprehensive achievement system that rewards learning progress, encourages healthy habits, and provides long-term motivation. The system is designed to be age-appropriate, positively framed, and integrated naturally into the learning flow.

**Core Principles**:
1. **Achievable Goals**: Mix of easy and challenging achievements
2. **Clear Progress**: Children can see what they need to do
3. **Positive Reinforcement**: Celebrate effort, not just results
4. **Variety**: Different types of achievements for different playstyles

---

## Achievement Categories

### Category Overview

| Category | Count | Focus | Examples |
|----------|-------|-------|----------|
| **Progress** | 5 | Learning milestones | First Steps, Word Hunter |
| **Performance** | 4 | Excellence | Perfectionist, Speed Demon |
| **Combo** | 2 | Streaks | Combo Master, On Fire |
| **Streak** | 2 | Consistency | Dedicated Student, Week Warrior |
| **Special** | 2 | Unique challenges | No Hints, Explorer |

---

## Complete Achievement List

### 1. Progress Achievements (5)

#### 🌟 First Steps
**Description**: Complete your first level
**Requirement**: Complete any level with 1+ stars
**Reward**: 10 stars
**Difficulty**: ⭐ (Very Easy)
**Icon**: 🌟
**Unlock Message**: "Your learning journey begins!"

#### 📚 Page Turner
**Description**: Complete 5 levels
**Requirement**: Complete 5 different levels with 2+ stars each
**Reward**: 25 stars
**Difficulty**: ⭐⭐ (Easy)
**Icon**: 📚
**Unlock Message**: "You're turning pages and learning fast!"

#### 🎓 Word Scholar
**Description**: Learn 30 words
**Requirement**: Master 30 unique words (memory strength ≥ 80)
**Reward**: 50 stars + Title: "Scholar"
**Difficulty**: ⭐⭐⭐ (Medium)
**Icon**: 🎓
**Unlock Message**: "A true scholar of words!"

#### 🏆 Word Hunter
**Description**: Learn 60 words
**Requirement**: Master 60 unique words (complete Look Island)
**Reward**: 100 stars + Badge: "Word Hunter"
**Difficulty**: ⭐⭐⭐⭐ (Hard)
**Icon**: 🏆
**Unlock Message**: "You've hunted down all the words!"

#### 🌟 Island Master
**Description**: Complete an island
**Requirement**: 100% mastery on any island
**Reward**: 150 stars + Pet Unlock
**Difficulty**: ⭐⭐⭐⭐⭐ (Very Hard)
**Icon**: 🏝️
**Unlock Message**: "Master of the island!"

---

### 2. Performance Achievements (4)

#### 💎 Perfectionist
**Description**: Complete a level with all 3-star words
**Requirement**: Complete a level where every word earned 3 stars
**Reward**: 30 stars
**Difficulty**: ⭐⭐⭐ (Medium)
**Icon**: 💎
**Unlock Message**: "Perfect performance!"

#### 🎯 Sharpshooter
**Description**: Get 10 correct answers in a row without hints
**Requirement**: 10 consecutive correct answers, 0 hints used
**Reward**: 20 stars
**Difficulty**: ⭐⭐⭐ (Medium)
**Icon**: 🎯
**Unlock Message**: "Sharp as a tack!"

#### ⚡ Speed Demon
**Description**: Answer correctly within optimal time range
**Requirement**: 5 consecutive correct answers in optimal time window (not too fast, not too slow)
**Reward**: 15 stars
**Difficulty**: ⭐⭐ (Easy-Medium)
**Icon**: ⚡
**Unlock Message**: "Fast AND thoughtful!"

#### 🧠 Memory Master
**Description**: Get 10 words to memory strength 100
**Requirement**: 10 different words at max memory strength
**Reward**: 40 stars + Title: "Memory Master"
**Difficulty**: ⭐⭐⭐⭐ (Hard)
**Icon**: 🧠
**Unlock Message**: "Your memory is amazing!"

---

### 3. Combo Achievements (2)

#### 🔥 Combo Master
**Description**: Reach a 5x combo
**Requirement**: Achieve 5 consecutive correct answers with adequate thinking time
**Reward**: 25 stars
**Difficulty**: ⭐⭐ (Easy)
**Icon**: 🔥
**Unlock Message**: "On fire!"

#### ⚡ Unstoppable
**Description**: Reach a 10x combo
**Requirement**: Achieve 10 consecutive correct answers with adequate thinking time
**Reward**: 50 stars + Title: "Unstoppable"
**Difficulty**: ⭐⭐⭐⭐ (Hard)
**Icon**: ⚡
**Unlock Message**: "Truly unstoppable!"

---

### 4. Streak Achievements (2)

#### 📅 Dedicated Student
**Description**: Practice for 7 days in a row
**Requirement**: 7-day streak (complete at least 1 word per day)
**Reward**: 35 stars + Badge: "Dedicated"
**Difficulty**: ⭐⭐⭐ (Medium)
**Icon**: 📅
**Unlock Message**: "Seven days of dedication!"

#### 🔥 Week Warrior
**Description**: Practice for 30 days in a row
**Requirement**: 30-day streak (complete at least 1 word per day)
**Reward**: 100 stars + Title: "Week Warrior" + Pet Unlock
**Difficulty**: ⭐⭐⭐⭐⭐ (Very Hard)
**Icon**: 🔥
**Unlock Message**: "A whole month of learning!"

---

### 5. Special Achievements (2)

#### 🚀 No Hints Hero
**Description**: Complete a level without using any hints
**Requirement**: Complete a full level (6 words) with 0 hints used
**Reward**: 30 stars
**Difficulty**: ⭐⭐⭐⭐ (Hard)
**Icon**: 🚀
**Unlock Message**: "Independent learner!"

#### 🗺️ Explorer
**Description**: Unlock all islands
**Requirement**: Reach all islands on the map (future: when 3+ islands exist)
**Reward**: 200 stars + Title: "Explorer"
**Difficulty**: ⭐⭐⭐⭐⭐ (Very Hard - Long-term)
**Icon**: 🗺️
**Unlock Message**: "The world is your classroom!"

---

## Achievement Progress Tracking

### Progress Indicators

Each achievement shows progress toward completion:

```kotlin
@Immutable
data class AchievementProgress(
    val achievementId: String,
    val current: Int,        // Current progress
    val target: Int,         // Goal
    val percentage: Float,   // Progress percentage (0-100)
    val isUnlocked: Boolean, // Whether unlocked
    val unlockedAt: Long?    // Timestamp when unlocked
)

// Examples:
// First Steps: current=0, target=1, percentage=0%
// Word Hunter: current=42, target=60, percentage=70%
// Combo Master: current=3, target=1, percentage=100% (binary achievement)
```

### Progress Display in UI

**Locked Achievement**:
```
┌─────────────────────────────┐
│  🏆 Word Hunter              │
│  ━━━━━━━━━━━━━━━━━━━━━━ 42/60│
│  Learn 60 words              │
│  Progress: 70%               │
└─────────────────────────────┘
```

**Unlocked Achievement**:
```
┌─────────────────────────────┐
│  ✅ 🏆 Word Hunter            │
│  Unlocked: Feb 15, 2026      │
│  Reward: 100 stars + Badge   │
└─────────────────────────────┘
```

---

## Achievement Rewards System

### Reward Types

| Reward Type | Description | Examples |
|-------------|-------------|----------|
| **Stars** | In-game currency for unlocking content | 10-200 stars per achievement |
| **Titles** | Display next to username | "Scholar", "Memory Master" |
| **Badges** | Permanent display on profile | "Dedicated", "Word Hunter" |
| **Pet Unlocks** | Exclusive pets | Island Master pet, Week Warrior pet |
| **Themes** | Visual customization | Golden theme (future) |

### Reward Tiers

| Achievement Difficulty | Star Reward | Bonus Rewards |
|------------------------|-------------|---------------|
| ⭐ Very Easy | 10-15 stars | None |
| ⭐⭐ Easy | 15-25 stars | None |
| ⭐⭐⭐ Medium | 25-40 stars | Title (some) |
| ⭐⭐⭐⭐ Hard | 40-60 stars | Badge + Title (some) |
| ⭐⭐⭐⭐⭐ Very Hard | 100-200 stars | Pet Unlock + Title |

---

## UI/UX Design

### 1. Achievement Unlock Notification

**Design**:
```
┌─────────────────────────────────────┐
│                                     │
│         🎉 ACHIEVEMENT UNLOCKED! 🎉  │
│                                     │
│           💎 Perfectionist           │
│                                     │
│      Complete a level with all      │
│         3-star words!               │
│                                     │
│         ┌─────────────┐              │
│         │   Awesome!   │              │
│         └─────────────┘              │
│                                     │
│          Reward: 30 stars ⭐         │
│                                     │
└─────────────────────────────────────┘
```

**Behavior**:
- Full-screen animation
- Celebration particles (confetti)
- Sound effect: Achievement fanfare
- Auto-dismiss after 5 seconds OR tap to continue
- Adds to notification queue (can view later)

### 2. Achievement Notification Queue

**Design**: Top-of-screen notification stack

```
┌─────────────────────────────────────┐
│ 🎉 Perfectionist unlocked!      [X] │
│ 🎉 Combo Master unlocked!       [X] │
│ 🎉 First Steps unlocked!        [X] │
└─────────────────────────────────────┘
```

**Behavior**:
- Shows up to 3 recent notifications
- Tap to expand details
- Swipe to dismiss
- "View All" button to achievement screen

### 3. Achievement Gallery Screen

**Navigation**: Home → Achievements

**Design**:
```
┌─────────────────────────────────────┐
│  Achievements                    [⏱] │
├─────────────────────────────────────┤
│                                     │
│  Progress: 7/15 unlocked (47%)      │
│  ████████████░░░░░░░░░░░░░░░░░       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  FILTER: [All] [Progress] [Performance] [Combo] [Streak] [Special]
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ✅ 🌟 First Steps            │    │
│  │ Progress: Complete ✓         │    │
│  │ Unlocked: Feb 10, 2026       │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ ✅ 📚 Page Turner            │    │
│  │ Progress: Complete ✓         │    │
│  │ Unlocked: Feb 12, 2026       │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 💎 Perfectionist             │    │
│  │ ━━━━━━━━━━━━━━━━━━━━━━ 0/1   │    │
│  │ Complete level with all 3★  │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🏆 Word Hunter               │    │
│  │ ━━━━━━━━━━━━━━━━━━━━━━ 42/60 │    │
│  │ Learn 60 words              │    │
│  │ Progress: 70%               │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🔥 Combo Master              │    │
│  │ ━━━━━━━━━━━━━━━━━━━━━━ 3/5   │    │
│  │ Reach 5x combo              │    │
│  │ Progress: 60%               │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 🚀 No Hints Hero             │    │
│  │ ━━━━━━━━━━━━━━━━━━━━━━ 0/6   │    │
│  │ Complete level with 0 hints │    │
│  │ Progress: 0%                │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ ← Back ]                    [ 🔍 Sort ] │
└─────────────────────────────────────┘
```

**Features**:
- Filter by category
- Sort by: Progress, Difficulty, Recent
- Tap achievement for details
- Progress bar at top shows overall completion
- Percentage display encourages collection

### 4. Achievement Detail View

**Tap any achievement to see details**:

```
┌─────────────────────────────────────┐
│  ← Back to Achievements              │
├─────────────────────────────────────┤
│                                     │
│              🏆 Word Hunter          │
│                                     │
│  Learn 60 words                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━ 42/60       │
│  Progress: 70%                      │
│                                     │
│  📊 How to unlock:                  │
│  • Master 60 unique words           │
│  • Memory strength must be ≥ 80     │
│  • Currently: 42/60 words mastered  │
│                                     │
│  🎁 Reward:                         │
│  • 100 stars                        │
│  • Badge: "Word Hunter"             │
│  • Shows on your profile            │
│                                     │
│  📈 Your progress:                  │
│  • Started: Feb 10, 2026            │
│  • Average: 3.5 words/day           │
│  • Estimated: 5 days to complete    │
│                                     │
│  💡 Tips:                           │
│  • Review words regularly to        │
│    increase memory strength         │
│  • Focus on one level at a time     │
│                                     │
│  [ Share Achievement ]              │
│                                     │
└─────────────────────────────────────┘
```

---

## Technical Implementation

### Data Models

```kotlin
/**
 * Achievement definition
 */
@Immutable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val icon: String, // Emoji or drawable resource
    val difficulty: AchievementDifficulty,
    val requirement: AchievementRequirement,
    val reward: AchievementReward,
    val isSecret: Boolean = false // Hidden until unlocked
)

/**
 * Achievement categories
 */
enum class AchievementCategory {
    PROGRESS,       // Learning milestones
    PERFORMANCE,    // Excellence achievements
    COMBO,          // Combo milestones
    STREAK,         // Consistency achievements
    SPECIAL         // Unique challenges
}

/**
 * Achievement difficulty levels
 */
enum class AchievementDifficulty {
    VERY_EASY,  // ⭐
    EASY,        // ⭐⭐
    MEDIUM,      // ⭐⭐⭐
    HARD,        // ⭐⭐⭐⭐
    VERY_HARD    // ⭐⭐⭐⭐⭐
}

/**
 * Achievement requirements (sealed class for different types)
 */
@Immutable
sealed class AchievementRequirement {
    /**
     * Complete N levels
     */
    data class CompleteLevels(
        val count: Int,
        val minStars: Int = 1
    ) : AchievementRequirement()

    /**
     * Master N words (memory strength ≥ threshold)
     */
    data class MasterWords(
        val count: Int,
        val memoryStrengthThreshold: Int = 80
    ) : AchievementRequirement()

    /**
     * Achieve N combo
     */
    data class ComboMilestone(
        val comboCount: Int
    ) : AchievementRequirement()

    /**
     * N-day streak
     */
    data class StreakDays(
        val days: Int,
        val minWordsPerDay: Int = 1
    ) : AchievementRequirement()

    /**
     * Perfect level (all 3-star words)
     */
    data class PerfectLevel(
        val count: Int = 1
    ) : AchievementRequirement()

    /**
     * Complete level without hints
     */
    data class NoHintsLevel(
        val wordCount: Int = 6
    ) : AchievementRequirement()

    /**
     * Max memory strength on N words
     */
    data class MaxMemoryStrength(
        val count: Int,
        val strength: Int = 100
    ) : AchievementRequirement()

    /**
     * Unlock all islands
     */
    data class UnlockAllIslands(
        val islandCount: Int
    ) : AchievementRequirement()
}

/**
 * Achievement rewards
 */
@Immutable
sealed class AchievementReward {
    /**
     * Star reward
     */
    data class Stars(val amount: Int) : AchievementReward()

    /**
     * Title reward (displayed by username)
     */
    data class Title(val titleId: String, val displayName: String) : AchievementReward()

    /**
     * Badge reward (permanent display on profile)
     */
    data class Badge(val badgeId: String, val iconName: String) : AchievementReward()

    /**
     * Pet unlock reward
     */
    data class PetUnlock(val petId: String, val petName: String) : AchievementReward()

    /**
     * Combined rewards
     */
    data class Multiple(
        val rewards: List<AchievementReward>
    ) : AchievementReward()
}

/**
 * User's achievement progress
 */
@Immutable
data class UserAchievement(
    val userId: String,
    val achievementId: String,
    val isUnlocked: Boolean,
    val progress: Int,           // Current progress value
    val target: Int,             // Target value
    val unlockedAt: Long?,       // Timestamp if unlocked
    val lastUpdated: Long        // Last progress update
)

/**
 * Achievement unlock event
 */
@Immutable
data class AchievementUnlockEvent(
    val achievementId: String,
    val userId: String,
    val timestamp: Long,
    val reward: AchievementReward
)
```

### Use Cases

```kotlin
/**
 * Check and update achievement progress
 */
class CheckAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
    private val progressRepository: ProgressRepository,
    private val notificationService: NotificationService
) {
    /**
     * Check all achievements after a game event
     * @param event Game event that occurred
     * @param userId User ID
     * @return List of newly unlocked achievements
     */
    suspend operator fun invoke(
        event: GameEvent,
        userId: String
    ): Result<List<Achievement>> {
        val newlyUnlocked = mutableListOf<Achievement>()

        // Get all achievements
        val allAchievements = achievementRepository.getAllAchievements()

        // Check each achievement
        for (achievement in allAchievements) {
            val userProgress = achievementRepository.getUserProgress(userId, achievement.id)

            // Skip if already unlocked
            if (userProgress?.isUnlocked == true) continue

            // Check if event is relevant to this achievement
            if (isEventRelevant(event, achievement)) {
                // Calculate new progress
                val newProgress = calculateProgress(event, achievement, userProgress)

                // Update progress
                achievementRepository.updateProgress(
                    userId = userId,
                    achievementId = achievement.id,
                    progress = newProgress
                )

                // Check if unlocked
                if (newProgress >= achievement.targetValue) {
                    // Unlock achievement
                    achievementRepository.unlockAchievement(userId, achievement.id)
                    newlyUnlocked.add(achievement)

                    // Grant reward
                    grantReward(userId, achievement.reward)

                    // Show notification
                    notificationService.showAchievementNotification(achievement)
                }
            }
        }

        return Result.Success(newlyUnlocked)
    }

    /**
     * Calculate progress for an achievement based on event
     */
    private fun calculateProgress(
        event: GameEvent,
        achievement: Achievement,
        currentProgress: UserAchievement?
    ): Int {
        return when (event) {
            is GameEvent.WordMastered -> {
                when (achievement.requirement) {
                    is AchievementRequirement.MasterWords -> {
                        // Count mastered words
                        val masteredCount = progressRepository.getMasteredWordCount(
                            event.userId,
                            minStrength = (achievement.requirement as AchievementRequirement.MasterWords).memoryStrengthThreshold
                        )
                        masteredCount
                    }
                    else -> currentProgress?.progress ?: 0
                }
            }
            is GameEvent.LevelComplete -> {
                when (achievement.requirement) {
                    is AchievementRequirement.CompleteLevels -> {
                        // Count completed levels
                        val completedCount = progressRepository.getCompletedLevelCount(
                            event.userId,
                            minStars = (achievement.requirement as AchievementRequirement.CompleteLevels).minStars
                        )
                        completedCount
                    }
                    else -> currentProgress?.progress ?: 0
                }
            }
            is GameEvent.ComboAchieved -> {
                when (achievement.requirement) {
                    is AchievementRequirement.ComboMilestone -> {
                        // Use max combo achieved
                        maxOf(event.comboCount, currentProgress?.progress ?: 0)
                    }
                    else -> currentProgress?.progress ?: 0
                }
            }
            else -> currentProgress?.progress ?: 0
        }
    }

    /**
     * Check if an event is relevant to an achievement
     */
    private fun isEventRelevant(event: GameEvent, achievement: Achievement): Boolean {
        return when (event) {
            is GameEvent.WordMastered -> {
                achievement.requirement is AchievementRequirement.MasterWords ||
                achievement.requirement is AchievementRequirement.MaxMemoryStrength
            }
            is GameEvent.LevelComplete -> {
                achievement.requirement is AchievementRequirement.CompleteLevels ||
                achievement.requirement is AchievementRequirement.PerfectLevel ||
                achievement.requirement is AchievementRequirement.NoHintsLevel
            }
            is GameEvent.ComboAchieved -> {
                achievement.requirement is AchievementRequirement.ComboMilestone
            }
            is GameEvent.DailyStreak -> {
                achievement.requirement is AchievementRequirement.StreakDays
            }
            else -> false
        }
    }

    /**
     * Grant reward to user
     */
    private suspend fun grantReward(userId: String, reward: AchievementReward) {
        when (reward) {
            is AchievementReward.Stars -> {
                // Add stars to user balance
                achievementRepository.addStars(userId, reward.amount)
            }
            is AchievementReward.Title -> {
                // Unlock title
                achievementRepository.unlockTitle(userId, reward.titleId)
            }
            is AchievementReward.Badge -> {
                // Unlock badge
                achievementRepository.unlockBadge(userId, reward.badgeId)
            }
            is AchievementReward.PetUnlock -> {
                // Unlock pet
                achievementRepository.unlockPet(userId, reward.petId)
            }
            is AchievementReward.Multiple -> {
                // Grant all rewards
                reward.rewards.forEach { grantReward(userId, it) }
            }
        }
    }
}

/**
 * Game events that trigger achievement checks
 */
@Immutable
sealed class GameEvent {
    data class WordMastered(
        val userId: String,
        val wordId: String,
        val memoryStrength: Int
    ) : GameEvent()

    data class LevelComplete(
        val userId: String,
        val levelId: String,
        val stars: Int,
        val allThreeStar: Boolean,
        val hintsUsed: Int
    ) : GameEvent()

    data class ComboAchieved(
        val userId: String,
        val comboCount: Int
    ) : GameEvent()

    data class DailyStreak(
        val userId: String,
        val streakDays: Int
    ) : GameEvent()
}
```

### Database Schema

```sql
-- Achievements definition table
CREATE TABLE achievements (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL, -- Progress, Performance, Combo, Streak, Special
    icon TEXT NOT NULL,
    difficulty TEXT NOT NULL, -- VERY_EASY, EASY, MEDIUM, HARD, VERY_HARD
    requirement_type TEXT NOT NULL, -- CompleteLevels, MasterWords, etc.
    requirement_data TEXT NOT NULL, -- JSON: specific requirement data
    reward_type TEXT NOT NULL, -- Stars, Title, Badge, PetUnlock, Multiple
    reward_data TEXT NOT NULL, -- JSON: specific reward data
    is_secret INTEGER DEFAULT 0,
    target_value INTEGER NOT NULL -- Progress target
);

-- User achievement progress table
CREATE TABLE user_achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    achievement_id TEXT NOT NULL,
    is_unlocked INTEGER NOT NULL DEFAULT 0,
    progress INTEGER NOT NULL DEFAULT 0,
    target INTEGER NOT NULL,
    unlocked_at INTEGER, -- Timestamp
    last_updated INTEGER NOT NULL,
    UNIQUE(user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (achievement_id) REFERENCES achievements(id)
);

-- Achievement unlock history table
CREATE TABLE achievement_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    achievement_id TEXT NOT NULL,
    unlocked_at INTEGER NOT NULL,
    reward_granted TEXT NOT NULL, -- JSON: reward details
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (achievement_id) REFERENCES achievements(id)
);

-- User rewards (stars, titles, badges, pets)
CREATE TABLE user_rewards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    reward_type TEXT NOT NULL, -- stars, title, badge, pet
    reward_id TEXT NOT NULL,
    granted_at INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index for fast achievement lookups
CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(is_unlocked);
CREATE INDEX idx_achievement_history_user_id ON achievement_history(user_id);
```

---

## Achievement Definitions (JSON)

```json
{
  "achievements": [
    {
      "id": "first_steps",
      "name": "First Steps",
      "description": "Complete your first level",
      "category": "PROGRESS",
      "icon": "🌟",
      "difficulty": "VERY_EASY",
      "requirement": {
        "type": "CompleteLevels",
        "count": 1,
        "minStars": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 10
      }
    },
    {
      "id": "word_hunter",
      "name": "Word Hunter",
      "description": "Learn 60 words",
      "category": "PROGRESS",
      "icon": "🏆",
      "difficulty": "VERY_HARD",
      "requirement": {
        "type": "MasterWords",
        "count": 60,
        "memoryStrengthThreshold": 80
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 100 },
          { "type": "Badge", "badgeId": "word_hunter", "iconName": "🏆" }
        ]
      }
    },
    {
      "id": "perfectionist",
      "name": "Perfectionist",
      "description": "Complete a level with all 3-star words",
      "category": "PERFORMANCE",
      "icon": "💎",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "PerfectLevel",
        "count": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 30
      }
    },
    {
      "id": "combo_master",
      "name": "Combo Master",
      "description": "Reach a 5x combo",
      "category": "COMBO",
      "icon": "🔥",
      "difficulty": "EASY",
      "requirement": {
        "type": "ComboMilestone",
        "comboCount": 5
      },
      "reward": {
        "type": "Stars",
        "amount": 25
      }
    },
    {
      "id": "dedicated_student",
      "name": "Dedicated Student",
      "description": "Practice for 7 days in a row",
      "category": "STREAK",
      "icon": "📅",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "StreakDays",
        "days": 7,
        "minWordsPerDay": 1
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 35 },
          { "type": "Badge", "badgeId": "dedicated", "iconName": "📅" }
        ]
      }
    },
    {
      "id": "no_hints_hero",
      "name": "No Hints Hero",
      "description": "Complete a level without using any hints",
      "category": "SPECIAL",
      "icon": "🚀",
      "difficulty": "HARD",
      "requirement": {
        "type": "NoHintsLevel",
        "wordCount": 6
      },
      "reward": {
        "type": "Stars",
        "amount": 30
      }
    }
  ]
}
```

---

## Integration with Existing Gameplay

### 1. Post-Game Achievement Check

```kotlin
// In LearningViewModel, after submitting answer
fun submitAnswer(userAnswer: String, responseTime: Long, hintUsed: Boolean) {
    viewModelScope.launch {
        // ... existing submit logic ...

        // Check achievements after answer
        when (val result = submitAnswer(...)) {
            is Result.Success -> {
                // Trigger achievement check
                val event = when {
                    result.data.isCorrect && !result.data.isGuessing -> {
                        GameEvent.WordMastered(
                            userId = userId,
                            wordId = currentWord.id,
                            memoryStrength = result.data.newMemoryStrength
                        )
                    }
                    else -> null
                }
                event?.let { checkAchievements(it) }
            }
        }
    }
}

// After completing a level
fun onNextWord() {
    if (currentWordIndex >= levelWords.size) {
        // Level complete - trigger achievement check
        val event = GameEvent.LevelComplete(
            userId = userId,
            levelId = currentLevelId,
            stars = calculateLevelStars(),
            allThreeStar = starsEarnedInLevel.all { it == 3 },
            hintsUsed = totalHintsUsed
        )
        checkAchievements(event)
    }
}

// After combo update
fun updateCombo(comboState: ComboState) {
    if (comboState.consecutiveCorrect >= comboState.previousCount) {
        // Combo increased - check achievement
        if (comboState.consecutiveCorrect in listOf(3, 5, 10)) {
            val event = GameEvent.ComboAchieved(
                userId = userId,
                comboCount = comboState.consecutiveCorrect
            )
            checkAchievements(event)
        }
    }
}
```

### 2. Achievement Notification Display

```kotlin
@Composable
fun LearningScreen(
    // ... existing parameters ...
    achievementViewModel: AchievementViewModel = viewModel(...)
) {
    val achievementEvents by achievementViewModel.recentUnlocks.collectAsState()

    // Show achievement notification overlay
    if (achievementEvents.isNotEmpty()) {
        AchievementNotificationOverlay(
            achievements = achievementEvents,
            onDismiss = { achievementViewModel.dismissNotifications() }
        )
    }

    // ... existing UI ...
}

@Composable
fun AchievementNotificationOverlay(
    achievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    // Show notification for most recent achievement
    val latestAchievement = achievements.first()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        AchievementUnlockedDialog(
            achievement = latestAchievement,
            onContinue = onDismiss
        )
    }
}
```

---

## Success Metrics

### Engagement Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Achievement unlock rate | 2-3 per session | Analytics |
| Achievement gallery views | >50% of users | Feature usage |
| Achievement share rate | >10% of unlocks | Social feature |
| Completion rate (all 15) | >20% of active users | Long-term retention |

### Retention Impact

| Metric | Expected Lift | Measurement |
|--------|---------------|-------------|
| Day 7 retention | +15% vs baseline | Cohort analysis |
| Day 30 retention | +25% vs baseline | Cohort analysis |
| Avg session duration | +20% vs baseline | Session analytics |
| Words learned per week | +30% vs baseline | Progress tracking |

---

## Future Enhancements

### 1. Seasonal Achievements

```kotlin
/**
 * Limited-time achievements for events
 * Example: "Summer Learning Challenge" - Learn 50 words in August
 */
data class SeasonalAchievement(
    val base: Achievement,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val seasonName: String
)
```

### 2. Community Achievements

```kotlin
/**
 * Achievements tied to community goals
 * Example: "Together We Learn" - Community learns 10,000 words
 */
data class CommunityAchievement(
    val base: Achievement,
    val communityGoal: Int,
    val currentProgress: Int,
    val participants: Int
)
```

### 3. Achievement Chains

```kotlin
/**
 * Sequential achievements that unlock each other
 * Example: First Steps → Page Turner → Word Scholar → Word Hunter → Island Master
 */
data class AchievementChain(
    val chainId: String,
    val achievements: List<Achievement>,
    val chainBonus: AchievementReward // Bonus for completing entire chain
)
```

---

## Conclusion

This achievement system provides:
- **15 well-balanced achievements** across 5 categories
- **Clear progression** from very easy to very hard
- **Variety of rewards** (stars, titles, badges, pets)
- **Age-appropriate design** for 10-year-olds
- **Natural integration** with existing gameplay
- **Long-term motivation** through collection goals

The system is designed to be fun, encouraging, and aligned with educational goals without being distracting from the core learning experience.

---

**Next Steps**:
1. Review with android-engineer for implementation
2. Create UI mockups with compose-ui-designer
3. Define achievement icons and animations
4. Begin Phase 1 implementation

**Document Version**: 1.0
**Last Updated**: 2026-02-18
