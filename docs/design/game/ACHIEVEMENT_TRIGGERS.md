# Achievement Trigger Logic - Wordland KET

**Document Version**: 1.0
**Date**: 2026-02-18
**Author**: Game Designer
**Related**: ACHIEVEMENT_SYSTEM_ARCHITECTURE.md

---

## Overview

This document defines the complete trigger logic for all achievements in the Wordland achievement system. It specifies which game events trigger which achievements, how progress is calculated, and the exact unlock conditions.

---

## Event-to-Achievement Mapping

### Trigger Matrix

| Game Event | Triggered Achievements | Check Frequency |
|------------|------------------------|-----------------|
| `WordMastered` | Word Scholar, Word Hunter, Memory Master, Perfectionist, Sharpshooter | After each word |
| `LevelComplete` | First Steps, Page Turner, Island Master, No Hints Hero | After each level |
| `ComboAchieved` | Combo Master, Unstoppable, Speed Demon | On combo milestone |
| `StreakUpdate` | Dedicated Student, Week Warrior | Daily check |
| `AnswerSubmitted` | Performance tracking (accumulators) | After each answer |

---

## Achievement Tracker Logic

### Core Algorithm

```kotlin
package com.wordland.domain.achievement

import com.wordland.data.repository.AchievementRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.Achievement
import com.wordland.domain.model.GameEvent
import com.wordland.domain.model.UserAchievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Core achievement tracking logic
 * Evaluates achievements against game events
 */
class AchievementTracker(
    private val progressRepository: ProgressRepository
) {

    /**
     * Check all achievements for a game event
     * @return List of newly unlocked achievements
     */
    suspend fun checkAchievements(
        userId: String,
        event: GameEvent,
        achievementRepository: AchievementRepository
    ): List<Achievement> = withContext(Dispatchers.Default) {

        val newlyUnlocked = mutableListOf<Achievement>()

        // Get all locked achievements
        val lockedAchievements = achievementRepository.getLockedAchievements(userId)
        val lockedMap = lockedAchievements.associateBy { it.achievement.id }

        // Filter achievements relevant to this event
        val relevantAchievements = lockedMap.values.filter { achievementWithProgress ->
            isEventRelevant(event, achievementWithProgress.achievement)
        }

        // Check each relevant achievement
        for (achievementWithProgress in relevantAchievements) {
            val achievement = achievementWithProgress.achievement
            val currentProgress = achievementWithProgress.progress ?: 0

            // Calculate new progress
            val newProgress = calculateProgress(
                event = event,
                achievement = achievement,
                currentProgress = currentProgress,
                progressRepository = progressRepository
            )

            // Update progress in database
            achievementRepository.updateProgress(userId, achievement.id, newProgress)

            // Check if unlocked
            if (newProgress >= achievement.requirement.targetValue) {
                achievementRepository.unlockAchievement(userId, achievement.id, newProgress)
                newlyUnlocked.add(achievement)
            }
        }

        newlyUnlocked
    }

    /**
     * Check if an event is relevant to an achievement
     * Optimizes by skipping unrelated achievements
     */
    private fun isEventRelevant(event: GameEvent, achievement: Achievement): Boolean {
        return when (event) {
            is GameEvent.WordMastered -> {
                achievement.category == AchievementCategory.PROGRESS ||
                achievement.requirement is AchievementRequirement.MasterWords ||
                achievement.requirement is AchievementRequirement.MaxMemoryStrength
            }
            is GameEvent.LevelComplete -> {
                achievement.requirement is AchievementRequirement.CompleteLevels ||
                achievement.requirement is AchievementRequirement.PerfectLevel ||
                achievement.requirement is AchievementRequirement.NoHintsLevel ||
                achievement.requirement is AchievementRequirement.CompleteIsland
            }
            is GameEvent.ComboAchieved -> {
                achievement.requirement is AchievementRequirement.ComboMilestone
            }
            is GameEvent.StreakUpdate -> {
                achievement.requirement is AchievementRequirement.StreakDays
            }
            is GameEvent.IslandComplete -> {
                achievement.requirement is AchievementRequirement.CompleteIsland ||
                achievement.requirement is AchievementRequirement.UnlockIslands
            }
            is GameEvent.AnswerSubmitted -> {
                // Used for accumulator tracking only
                false
            }
        }
    }

    /**
     * Calculate new progress for an achievement based on event
     */
    private suspend fun calculateProgress(
        event: GameEvent,
        achievement: Achievement,
        currentProgress: Int,
        progressRepository: ProgressRepository
    ): Int {
        return when (achievement.requirement) {
            // === Word counting achievements ===
            is AchievementRequirement.MasterWords -> {
                progressRepository.getMasteredWordCount(
                    userId = event.userId,
                    minStrength = (achievement.requirement as AchievementRequirement.MasterWords).memoryStrengthThreshold
                )
            }

            is AchievementRequirement.MaxMemoryStrength -> {
                progressRepository.getMaxStrengthWordCount(
                    userId = event.userId,
                    strength = (achievement.requirement as AchievementRequirement.MaxMemoryStrength).strength
                )
            }

            // === Level counting achievements ===
            is AchievementRequirement.CompleteLevels -> {
                val minStars = (achievement.requirement as AchievementRequirement.CompleteLevels).minStars
                progressRepository.getCompletedLevelCount(event.userId, minStars)
            }

            is AchievementRequirement.PerfectLevel -> {
                // Special case: track perfect levels separately
                when (event) {
                    is GameEvent.LevelComplete -> {
                        if (event.allThreeStar) currentProgress + 1 else currentProgress
                    }
                    else -> currentProgress
                }
            }

            is AchievementRequirement.NoHintsLevel -> {
                when (event) {
                    is GameEvent.LevelComplete -> {
                        if (event.hintsUsed == 0 && event.stars > 0) {
                            event.wordCount // Complete in one go
                        } else {
                            currentProgress
                        }
                    }
                    else -> currentProgress
                }
            }

            // === Island achievements ===
            is AchievementRequirement.CompleteIsland -> {
                val islandId = (achievement.requirement as AchievementRequirement.CompleteIsland).islandId
                progressRepository.getCompletedLevelCountInIsland(
                    userId = event.userId,
                    islandId = islandId
                )
            }

            is AchievementRequirement.UnlockIslands -> {
                progressRepository.getUnlockedIslandCount(event.userId)
            }

            // === Combo achievements ===
            is AchievementRequirement.ComboMilestone -> {
                when (event) {
                    is GameEvent.ComboAchieved -> {
                        event.comboCount
                    }
                    else -> currentProgress
                }
            }

            // === Streak achievements ===
            is AchievementRequirement.StreakDays -> {
                when (event) {
                    is GameEvent.StreakUpdate -> {
                        event.streakDays
                    }
                    else -> currentProgress
                }
            }
        }
    }
}
```

---

## Achievement Definitions with Triggers

### 1. Progress Achievements

#### First Steps
```yaml
id: first_steps
name: First Steps
description: Complete your first level
icon: "🌟"
category: PROGRESS
tier: BRONZE
requirement:
  type: CompleteLevels
  count: 1
  minStars: 1
reward:
  type: Stars
  amount: 10
trigger:
  event: LevelComplete
  condition: levelNumber == 1 && stars >= 1
  progress: 1 if unlocked else 0
```

#### Page Turner
```yaml
id: page_turner
name: Page Turner
description: Complete 5 levels
icon: "📚"
category: PROGRESS
tier: SILVER
requirement:
  type: CompleteLevels
  count: 5
  minStars: 2
reward:
  type: Stars
  amount: 25
trigger:
  event: LevelComplete
  condition: null # Check all level completions
  progress: COUNT(level.id WHERE level.stars >= 2)
```

#### Word Scholar
```yaml
id: word_scholar
name: Word Scholar
description: Learn 30 words
icon: "🎓"
category: PROGRESS
tier: GOLD
requirement:
  type: MasterWords
  count: 30
  memoryStrengthThreshold: 80
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 50
    - type: Title
      titleId: scholar
      displayName: Scholar
trigger:
  event: WordMastered
  condition: memoryStrength >= 80
  progress: COUNT(DISTINCT word.id WHERE user_word_progress.memory_strength >= 80)
```

#### Word Hunter
```yaml
id: word_hunter
name: Word Hunter
description: Learn 60 words
icon: "🏆"
category: PROGRESS
tier: PLATINUM
requirement:
  type: MasterWords
  count: 60
  memoryStrengthThreshold: 80
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 100
    - type: Badge
      badgeId: word_hunter
      displayName: Word Hunter
      iconName: "🏆"
trigger:
  event: WordMastered
  condition: memoryStrength >= 80
  progress: COUNT(DISTINCT word.id WHERE user_word_progress.memory_strength >= 80)
```

#### Island Master
```yaml
id: island_master
name: Island Master
description: Complete an island with 100% mastery
icon: "🏝️"
category: PROGRESS
tier: PLATINUM
requirement:
  type: CompleteIsland
  islandId: any
  levelCount: varies per island
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 150
    - type: PetUnlock
      petId: island_master_pet
      petName: Island Guardian
      petIcon: "🦜"
trigger:
  event: IslandComplete
  condition: levelsCompleted == totalLevels
  progress: levelsCompleted / totalLevels
```

---

### 2. Performance Achievements

#### Perfectionist
```yaml
id: perfectionist
name: Perfectionist
description: Complete a level with all 3-star words
icon: "💎"
category: PERFORMANCE
tier: GOLD
requirement:
  type: PerfectLevel
  count: 1
reward:
  type: Stars
  amount: 30
trigger:
  event: LevelComplete
  condition: allThreeStar == true
  progress: INCREMENT if allThreeStar else KEEP
```

#### Sharpshooter
```yaml
id: sharpshooter
name: Sharpshooter
description: 10 correct answers in a row without hints
icon: "🎯"
category: PERFORMANCE
tier: GOLD
requirement:
  type: ComboMilestone
  comboCount: 10
reward:
  type: Stars
  amount: 20
trigger:
  event: AnswerSubmitted
  condition: isCorrect == true && hintUsed == false
  progress: CONSECUTIVE_COUNT(isCorrect && !hintUsed)
```

#### Speed Demon
```yaml
id: speed_demon
name: Speed Demon
description: Answer correctly within optimal time range
icon: "⚡"
category: PERFORMANCE
tier: SILVER
requirement:
  type: ComboMilestone
  comboCount: 5
reward:
  type: Stars
  amount: 15
trigger:
  event: AnswerSubmitted
  condition:
    isCorrect == true &&
    responseTime >= MIN_THINK_TIME &&
    responseTime <= OPTIMAL_TIME
  progress: CONSECUTIVE_COUNT(condition)
```

**Optimal Time Calculation**:
```kotlin
fun calculateOptimalTime(wordLength: Int): LongRange {
    val minTime = ComboState.MIN_THINK_TIME_BASE_MS + (wordLength * ComboState.MILLIS_PER_LETTER)
    val maxTime = minTime * 3  // Upper bound for "optimal"
    return minTime..maxTime
}
```

#### Memory Master
```yaml
id: memory_master
name: Memory Master
description: Get 10 words to memory strength 100
icon: "🧠"
category: PERFORMANCE
tier: PLATINUM
requirement:
  type: MaxMemoryStrength
  count: 10
  strength: 100
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 40
    - type: Title
      titleId: memory_master
      displayName: Memory Master
trigger:
  event: WordMastered
  condition: memoryStrength == 100
  progress: COUNT(DISTINCT word.id WHERE user_word_progress.memory_strength == 100)
```

---

### 3. Combo Achievements

#### Combo Master
```yaml
id: combo_master
name: Combo Master
description: Reach a 5x combo
icon: "🔥"
category: COMBO
tier: SILVER
requirement:
  type: ComboMilestone
  comboCount: 5
reward:
  type: Stars
  amount: 25
trigger:
  event: ComboAchieved
  condition: comboCount >= 5
  progress: MAX(comboCount, currentProgress)
```

#### Unstoppable
```yaml
id: unstoppable
name: Unstoppable
description: Reach a 10x combo
icon: "⚡"
category: COMBO
tier: PLATINUM
requirement:
  type: ComboMilestone
  comboCount: 10
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 50
    - type: Title
      titleId: unstoppable
      displayName: Unstoppable
trigger:
  event: ComboAchieved
  condition: comboCount >= 10
  progress: MAX(comboCount, currentProgress)
```

---

### 4. Streak Achievements

#### Dedicated Student
```yaml
id: dedicated_student
name: Dedicated Student
description: Practice for 7 days in a row
icon: "📅"
category: STREAK
tier: GOLD
requirement:
  type: StreakDays
  days: 7
  minWordsPerDay: 1
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 35
    - type: Badge
      badgeId: dedicated
      displayName: Dedicated
      iconName: "📅"
trigger:
  event: StreakUpdate
  condition: streakDays >= 7
  progress: streakDays
```

**Streak Calculation with Freeze Days**:
```kotlin
data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActiveDate: Long,
    val freezeDaysUsed: Int,
    val freezeDaysAvailable: Int = 2
)

fun calculateStreak(
    streakData: StreakData,
    today: Long = System.currentTimeMillis()
): StreakResult {
    val daysSinceLastActive = daysBetween(streakData.lastActiveDate, today)

    return when {
        daysSinceLastActive == 1L -> {
            // Consecutive day - increment streak
            StreakResult(
                newStreak = streakData.currentStreak + 1,
                freezeDaysUsed = 0,
                longestStreak = maxOf(streakData.longestStreak, streakData.currentStreak + 1)
            )
        }
        daysSinceLastActive <= (streakData.freezeDaysAvailable - streakData.freezeDaysUsed) -> {
            // Within freeze period - maintain streak
            StreakResult(
                newStreak = streakData.currentStreak,
                freezeDaysUsed = streakData.freezeDaysUsed + daysSinceLastActive.toInt() - 1,
                longestStreak = streakData.longestStreak
            )
        }
        else -> {
            // Streak broken - reset (no shame!)
            StreakResult(
                newStreak = 1,
                freezeDaysUsed = 0,
                longestStreak = streakData.longestStreak
            )
        }
    }
}

data class StreakResult(
    val newStreak: Int,
    val freezeDaysUsed: Int,
    val longestStreak: Int
)
```

#### Week Warrior
```yaml
id: week_warrior
name: Week Warrior
description: Practice for 30 days in a row
icon: "🔥"
category: STREAK
tier: PLATINUM
requirement:
  type: StreakDays
  days: 30
  minWordsPerDay: 1
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 100
    - type: Title
      titleId: week_warrior
      displayName: Week Warrior
    - type: PetUnlock
      petId: week_warrior_pet
      petName: Flame Phoenix
      petIcon: "🔥"
trigger:
  event: StreakUpdate
  condition: streakDays >= 30
  progress: streakDays
```

---

### 5. Special Achievements

#### No Hints Hero
```yaml
id: no_hints_hero
name: No Hints Hero
description: Complete a level without using any hints
icon: "🚀"
category: SPECIAL
tier: GOLD
requirement:
  type: NoHintsLevel
  wordCount: 6
reward:
  type: Stars
  amount: 30
trigger:
  event: LevelComplete
  condition: hintsUsed == 0 && stars > 0
  progress: wordCount if condition else 0
```

**State Tracking**:
```kotlin
// Track hints used in current level session
data class LevelSessionState(
    val levelId: String,
    val hintsUsed: Int = 0,
    val wordsCompleted: Int = 0,
    val allThreeStar: Boolean = true
)

fun checkNoHintsHero(state: LevelSessionState): Boolean {
    return state.hintsUsed == 0 &&
           state.wordsCompleted >= 6 &&
           state.allThreeStar
}
```

#### Explorer
```yaml
id: explorer
name: Explorer
description: Unlock all islands
icon: "🗺️"
category: SPECIAL
tier: PLATINUM
requirement:
  type: UnlockIslands
  count: 3  // Will increase as more islands are added
reward:
  type: Multiple
  rewards:
    - type: Stars
      amount: 200
    - type: Title
      titleId: explorer
      displayName: Explorer
trigger:
  event: IslandComplete
  condition: null
  progress: COUNT(DISTINCT island.id WHERE island.isUnlocked == true)
```

---

## Progress Repository Queries

### Required Methods

```kotlin
package com.wordland.data.repository

import com.wordland.domain.model.UserWordProgress
import com.wordland.domain.model.LevelProgress

interface ProgressRepository {

    /**
     * Count words mastered (memory strength >= threshold)
     */
    suspend fun getMasteredWordCount(
        userId: String,
        minStrength: Int = 80
    ): Int

    /**
     * Count words at max memory strength
     */
    suspend fun getMaxStrengthWordCount(
        userId: String,
        strength: Int = 100
    ): Int

    /**
     * Count completed levels
     */
    suspend fun getCompletedLevelCount(
        userId: String,
        minStars: Int = 1
    ): Int

    /**
     * Count completed levels in an island
     */
    suspend fun getCompletedLevelCountInIsland(
        userId: String,
        islandId: String
    ): Int

    /**
     * Count unlocked islands
     */
    suspend fun getUnlockedIslandCount(userId: String): Int

    /**
     * Get user's current streak
     */
    suspend fun getUserStreak(userId: String): StreakData

    /**
     * Update streak data
     */
    suspend fun updateStreak(
        userId: String,
        newStreak: Int,
        freezeDaysUsed: Int
    )
}
```

---

## Integration Points

### LearningViewModel Integration

```kotlin
// In LearningViewModel, after answer submission
fun submitAnswer(userAnswer: String, responseTime: Long) {
    viewModelScope.launch {
        // ... existing submit logic ...

        // Create game event
        val event = when {
            result.isCorrect && result.newMemoryStrength >= 80 -> {
                GameEvent.WordMastered(
                    userId = userId,
                    wordId = currentWord.id,
                    memoryStrength = result.newMemoryStrength,
                    stars = result.stars
                )
            }
            else -> null
        }

        // Check achievements
        event?.let {
            checkAchievementsUseCase(userId, it)
        }
    }
}

// After level completion
fun onLevelComplete() {
    viewModelScope.launch {
        val event = GameEvent.LevelComplete(
            userId = userId,
            levelId = currentLevelId,
            islandId = currentIslandId,
            levelNumber = currentLevelNumber,
            stars = calculatedStars,
            allThreeStar = wordsEarnedStars.all { it == 3 },
            hintsUsed = totalHintsUsed,
            wordCount = wordsInLevel.size
        )

        checkAchievementsUseCase(userId, event)
    }
}

// After combo update
fun onComboUpdate(comboState: ComboState) {
    viewModelScope.launch {
        if (comboState.consecutiveCorrect in listOf(5, 10)) {
            val event = GameEvent.ComboAchieved(
                userId = userId,
                comboCount = comboState.consecutiveCorrect,
                averageResponseTime = comboState.averageResponseTime
            )

            checkAchievementsUseCase(userId, event)
        }
    }
}
```

---

## Trigger Optimization

### Batch Checking Strategy

```kotlin
/**
 * Batch achievement checker to reduce database queries
 */
class BatchAchievementChecker(
    private val tracker: AchievementTracker,
    private val repository: AchievementRepository
) {

    private val pendingEvents = mutableListOf<GameEvent>()
    private val checkInterval = 2000L  // Check every 2 seconds max

    suspend fun queueEvent(event: GameEvent) {
        pendingEvents.add(event)

        // Check immediately for critical events
        if (isCriticalEvent(event)) {
            flush()
        }
    }

    suspend fun flush() {
        if (pendingEvents.isEmpty()) return

        val userId = pendingEvents.first().userId
        val unlocked = mutableListOf<Achievement>()

        // Process all events
        for (event in pendingEvents) {
            unlocked.addAll(
                tracker.checkAchievements(userId, event, repository)
            )
        }

        pendingEvents.clear()

        // Show notifications for newly unlocked
        if (unlocked.isNotEmpty()) {
            showNotifications(unlocked)
        }
    }

    private fun isCriticalEvent(event: GameEvent): Boolean {
        return event is GameEvent.LevelComplete ||
               event is GameEvent.IslandComplete
    }
}
```

---

## Testing Triggers

### Unit Test Examples

```kotlin
class AchievementTrackerTest {

    @Test
    fun `WordMastered event updates Word Scholar progress`() = runTest {
        // Given
        val tracker = AchievementTracker(mockProgressRepository)
        val event = GameEvent.WordMastered(
            userId = "user_001",
            wordId = "word_001",
            memoryStrength = 85,
            stars = 3
        )

        // When
        val progress = tracker.calculateProgress(
            event = event,
            achievement = wordScholarAchievement,
            currentProgress = 29,
            progressRepository = mockProgressRepository
        )

        // Then
        assertEquals(30, progress)  // Should increment
    }

    @Test
    fun `LevelComplete with all 3-star unlocks Perfectionist`() = runTest {
        // Given
        val tracker = AchievementTracker(mockProgressRepository)
        val event = GameEvent.LevelComplete(
            userId = "user_001",
            levelId = "level_01",
            allThreeStar = true,
            hintsUsed = 0,
            wordCount = 6
        )

        // When
        val progress = tracker.calculateProgress(
            event = event,
            achievement = perfectionistAchievement,
            currentProgress = 0,
            progressRepository = mockProgressRepository
        )

        // Then
        assertEquals(1, progress)  // Should reach target
    }

    @Test
    fun `StreakUpdate event with freeze days maintains streak`() = runTest {
        // Given
        val streakData = StreakData(
            currentStreak = 5,
            longestStreak = 5,
            lastActiveDate = yesterday - 1.days,  // 2 days ago
            freezeDaysUsed = 0
        )

        // When
        val result = calculateStreak(streakData)

        // Then
        assertEquals(5, result.newStreak)  // Maintained with freeze
        assertEquals(1, result.freezeDaysUsed)
    }
}
```

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Next Document**: ACHIEVEMENT_UI_DESIGN.md
