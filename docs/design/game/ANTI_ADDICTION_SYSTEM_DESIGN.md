# Anti-Addiction System Design - Wordland KET
**Date**: 2026-02-18
**Designer**: Claude (game-designer)
**Priority**: P0 (Critical for child safety and COPPA compliance)
**Target Audience**: Children aged 10, Parents

---

## Executive Summary

This document outlines a comprehensive anti-addiction system for Wordland KET vocabulary learning app. The system is designed to promote healthy learning habits while maintaining engagement and educational value.

**Core Principles**:
1. **Child Safety First**: Prevent excessive screen time
2. **Positive Reinforcement**: Rewards for healthy behavior
3. **Parent Empowerment**: Give parents control and visibility
4. **Minimal Disruption**: Gentle integration into learning flow

---

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Anti-Addiction System                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Session      │  │ Break        │  │ Parental     │      │
│  │ Management   │  │ Rewards      │  │ Controls     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │              │
│         └──────────────────┴──────────────────┘              │
│                            │                                 │
│                   ┌────────▼────────┐                        │
│                   │ Usage Tracking  │                        │
│                   │ & Analytics     │                        │
│                   └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 1. Session Management System

### 1.1 Session Limits

#### Default Limits (Configurable by Parents)

| Limit Type | Duration | Trigger | Behavior |
|------------|----------|---------|----------|
| **Soft Reminder** | 15 minutes | Session timer | Gentle popup: "Time for a quick break?" |
| **Hard Limit** | 45 minutes | Session timer | Forced break: "You've been learning for 45 minutes! Take a break!" |
| **Daily Maximum** | 2 hours | Daily accumulator | Block: "Come back tomorrow for more learning!" |
| **Break Reminder** | Every 5 minutes | Interval | Subtle notification: "Look away for 20 seconds!" |

#### Time Accumulation Logic

```kotlin
/**
 * Session time tracking with pause/resume support
 * Only counts active gameplay time (not paused, minimized, or in menu)
 */
data class SessionTimeTracker(
    val sessionStartTime: Long,           // When current session started
    val activeTimeToday: Long,            // Accumulated active time today (ms)
    val activeTimeThisSession: Long,      // Current session active time (ms)
    val lastPauseTime: Long?,             // If paused, when did pause start
    val isPaused: Boolean,                // Currently paused state
    val isForcedBreak: Boolean            // Currently in forced break
) {
    /**
     * Check if user should receive soft reminder (15 min)
     */
    fun shouldShowSoftReminder(): Boolean {
        return activeTimeThisSession >= SOFT_REMINDER_MS &&
               !isPaused &&
               !isForcedBreak
    }

    /**
     * Check if user has hit hard limit (45 min)
     */
    fun hasHitHardLimit(): Boolean {
        return activeTimeThisSession >= HARD_LIMIT_MS
    }

    /**
     * Check if user has exceeded daily maximum (2 hours)
     */
    fun hasExceededDailyLimit(): Boolean {
        return activeTimeToday >= DAILY_MAX_MS
    }

    companion object {
        const val SOFT_REMINDER_MS = 15 * 60 * 1000L      // 15 minutes
        const val HARD_LIMIT_MS = 45 * 60 * 1000L         // 45 minutes
        const val DAILY_MAX_MS = 2 * 60 * 60 * 1000L      // 2 hours
        const val BREAK_REMINDER_INTERVAL_MS = 5 * 60 * 1000L  // 5 minutes
    }
}
```

### 1.2 Active Time Detection

**Important**: Only count active gameplay time, not:

- App in background (minimized)
- Paused gameplay
- In settings/menu screens
- Loading screens
- Reading instructions

```kotlin
/**
 * Activity state for time tracking
 */
enum class ActivityState {
    ACTIVE_GAMEPLAY,      // Count time (learning, answering questions)
    PAUSED,               // Don't count (user paused game)
    IN_MENU,              // Don't count (browsing settings, level select)
    BACKGROUND,           // Don't count (app minimized)
    LOCKED,               // Don't count (forced break active)
    READING               // Don't count (reading instructions/tutorial)
}

/**
 * Update activity state and adjust time tracking
 */
fun updateActivityState(
    tracker: SessionTimeTracker,
    newState: ActivityState,
    currentTime: Long
): SessionTimeTracker {
    val shouldTrackTime = newState == ActivityState.ACTIVE_GAMEPLAY

    return when {
        // Starting gameplay
        shouldTrackTime && !tracker.isPaused -> {
            val elapsed = currentTime - tracker.sessionStartTime
            tracker.copy(
                activeTimeToday = tracker.activeTimeToday + elapsed,
                activeTimeThisSession = tracker.activeTimeThisSession + elapsed,
                sessionStartTime = currentTime
            )
        }
        // Pausing gameplay
        !shouldTrackTime && tracker.isPaused -> {
            tracker.copy(
                lastPauseTime = currentTime,
                isPaused = true
            )
        }
        // Resuming from pause
        shouldTrackTime && tracker.isPaused -> {
            tracker.copy(
                lastPauseTime = null,
                isPaused = false,
                sessionStartTime = currentTime
            )
        }
        else -> tracker
    }
}
```

---

## 2. UI/UX Design

### 2.1 Session Timer Display

**Location**: Top-right corner of all gameplay screens

**Design**:
```
┌────────────────────────────────────┐
│  Look Island - Level 1      [⏱ 12:34] │
└────────────────────────────────────┘
```

**States**:

| State | Appearance | Color |
|-------|------------|-------|
| Normal (0-10 min) | `⏱ 12:34` (gray) | Neutral |
| Caution (10-15 min) | `⏱ 14:23` (yellow) | Warning |
| Soft Limit Hit (15+ min) | `⏱ 15:47 ⚠️` (orange) | Alert |
| Hard Limit Approaching (40+ min) | `⏱ 42:15 🔶` (red-orange) | Urgent |

**Behavior**:
- Tappable to expand details
- Shows: "Session time: 15:47 | Daily total: 1:23:45"
- Updates every second while in active gameplay

### 2.2 Soft Reminder Popup (15 minutes)

**Trigger**: Session timer hits 15 minutes

**Design**:
```
┌────────────────────────────────────┐
│                                    │
│      🌟 Great job learning! 🌟      │
│                                    │
│    You've been practicing for      │
│         15 minutes!                │
│                                    │
│    Time for a quick break?         │
│    Your pet wants to stretch! 🐱   │
│                                    │
│   ┌─────────┐  ┌─────────┐        │
│   │ Continue│  │  Break  │        │
│   └─────────┘  └─────────┘        │
│                                    │
│  [⏱ 15:00 of 45:00 session]       │
│                                    │
└────────────────────────────────────┘
```

**Features**:
- Non-blocking (can dismiss)
- Gentle wording ("want to" not "must")
- Shows session progress
- "Continue" button: Dismisses, reminds again in 5 minutes
- "Break" button: Starts break mode with rewards

### 2.3 Hard Limit Popup (45 minutes)

**Trigger**: Session timer hits 45 minutes

**Design**:
```
┌────────────────────────────────────┐
│                                    │
│      🎉 Awesome session! 🎉         │
│                                    │
│   You've learned for 45 minutes    │
│      Time for a well-earned        │
│           break!                   │
│                                    │
│      Your pet is tired! 😴          │
│                                    │
│   💡 Take a 15-minute break to     │
│      earn bonus points on          │
│      your next level!              │
│                                    │
│        ┌──────────────┐            │
│        │   Take Break  │            │
│        └──────────────┘            │
│                                    │
│   [⏱ 45:00 - Session complete]    │
│                                    │
└────────────────────────────────────┘
```

**Features**:
- Blocking (must take break)
- Positive framing ("well-earned break")
- Shows break reward incentive
- 15-minute minimum break before unlock
- During break: App shows stretch exercises

### 2.4 Daily Limit Screen (2 hours)

**Trigger**: Daily accumulator hits 2 hours

**Design**:
```
┌────────────────────────────────────┐
│                                    │
│      🌟 Amazing work today! 🌟      │
│                                    │
│   You've learned for 2 hours       │
│      today. That's plenty!         │
│                                    │
│   Your brain needs rest to         │
│      remember what you             │
│      learned today.                │
│                                    │
│     Come back tomorrow for         │
│      more learning fun!            │
│                                    │
│      See you tomorrow! 👋           │
│                                    │
│   [📊 Today's progress saved]       │
│                                    │
│   [⏰ Tomorrow at: 8:00 AM]         │
│                                    │
└────────────────────────────────────┘
```

**Features**:
- Hard block (no gameplay until tomorrow)
- Shows tomorrow's unlock time
- Positive framing ("amazing work")
- Progress is saved
- Shows streak count

### 2.5 Eye Health Reminder (Every 5-10 minutes)

**Trigger**: 5 minutes of continuous gameplay

**Design**:
```
┌────────────────────────────────────┐
│                                    │
│      👀 Eye Health Time! 👀         │
│                                    │
│   Look away from the screen        │
│      for 20 seconds                │
│                                    │
│   Try to find:                     │
│   • 5 blue things in your room     │
│   • Something green outside        │
│   • A faraway object to focus on   │
│                                    │
│     [Timer: 20... 19... 18...]     │
│                                    │
│        ┌──────────┐                │
│        │   Done!   │                │
│        └──────────┘                │
│                                    │
└────────────────────────────────────┘
```

**Features**:
- 20-second countdown
- Fun mini-game (find objects)
- Non-blocking (can skip)
- Pet animation: Pet stretches and looks around

---

## 3. Break Rewards System

### 3.1 Reward Tiers

| Break Duration | Bonus | Description |
|----------------|-------|-------------|
| 5 minutes | 1.2x | 20% score bonus for next 3 words |
| 15 minutes | 1.5x | 50% score bonus for next 5 words |
| 30 minutes | 2.0x | 2x score bonus for next level |
| 1 hour | 3.0x | 3x score bonus + "Dedicated Learner" badge |
| Until tomorrow | 5.0x | 5x score bonus + "Well-Rested Scholar" title |

### 3.2 Break Implementation

```kotlin
/**
 * Break system with rewards
 */
data class BreakState(
    val breakStartTime: Long,           // When break started
    val minimumBreakDuration: Long,     // Required minimum (ms)
    val isBonusActive: Boolean,         // User returned after full break
    val bonusMultiplier: Float,         // Current bonus multiplier
    val bonusWordsRemaining: Int,       // Words left with bonus
    val breakAchievementUnlocked: String? // Achievement earned
) {
    /**
     * Check if break has met minimum duration
     */
    fun isBreakComplete(currentTime: Long): Boolean {
        val elapsed = currentTime - breakStartTime
        return elapsed >= minimumBreakDuration
    }

    /**
     * Calculate break bonus based on duration
     */
    fun calculateBreakBonus(currentTime: Long): Float {
        val elapsed = currentTime - breakStartTime
        val elapsedMinutes = elapsed / (60 * 1000L)

        return when {
            elapsedMinutes >= 60 -> 3.0f     // 1 hour: 3x bonus
            elapsedMinutes >= 30 -> 2.0f     // 30 min: 2x bonus
            elapsedMinutes >= 15 -> 1.5f     // 15 min: 1.5x bonus
            elapsedMinutes >= 5 -> 1.2f      // 5 min: 1.2x bonus
            else -> 1.0f                     // Too short: no bonus
        }
    }

    /**
     * Get achievement for break duration
     */
    fun getBreakAchievement(currentTime: Long): String? {
        val elapsed = currentTime - breakStartTime
        val elapsedMinutes = elapsed / (60 * 1000L)

        return when {
            elapsedMinutes >= 60 * 24 && isNextDay() -> "WELL_RESTED_SCHOLAR"
            elapsedMinutes >= 60 -> "DEDICATED_LEARNER"
            elapsedMinutes >= 15 -> "BREAK_TAKER"
            else -> null
        }
    }

    private fun isNextDay(): Boolean {
        // Implementation: check if break crossed midnight
        return true
    }
}
```

### 3.3 Break Screen Design

**During Break (15-minute hard limit break)**:

```
┌────────────────────────────────────┐
│                                    │
│      💤 Break Time! 💤              │
│                                    │
│   Your pet is taking a nap!        │
│                                    │
│      [Animation: Pet sleeping]     │
│                                    │
│   Time remaining:                  │
│    ⏱ 12:34 / 15:00                │
│                                    │
│   💡 Try these activities:         │
│   • Stretch your arms 🙆           │
│   • Drink some water 💧             │
│   • Look out the window 🪟         │
│   • Walk around 🚶                 │
│                                    │
│   [Break Bonus: 1.5x for 5 words]  │
│                                    │
│   ⏰ Unlock in: 12:34              │
│                                    │
└────────────────────────────────────┘
```

**Break Complete**:

```
┌────────────────────────────────────┐
│                                    │
│      ⭐ Break Complete! ⭐           │
│                                    │
│   You took a 15-minute break!      │
│      Well done!                    │
│                                    │
│   Your bonus:                      │
│   🎁 1.5x score for next 5 words   │
│                                    │
│   ┌──────────────────┐             │
│   │   Continue Learning │          │
│   └──────────────────┘             │
│                                    │
│      Your pet is refreshed! 🐱✨    │
│                                    │
└────────────────────────────────────┘
```

---

## 4. Parental Controls System

### 4.1 Parent Dashboard

**Access**: Settings → Parental Controls (PIN protected)

**Design**:
```
┌────────────────────────────────────┐
│  Parental Controls           [⚙️]   │
├────────────────────────────────────┤
│                                    │
│  📊 Today's Usage                  │
│  ┌──────────────────────────┐      │
│  │ Time: 1h 23m / 2h         │      │
│  │ ████████░░░░░░░░░ 70%     │      │
│  └──────────────────────────┘      │
│                                    │
│  ⏱️ Time Limits                    │
│  ┌──────────────────────────┐      │
│  │ Soft Reminder: [15] min   │      │
│  │ Hard Limit:     [45] min   │      │
│  │ Daily Max:      [2.0] hr   │      │
│  └──────────────────────────┘      │
│                                    │
│  🌙 Bedtime Mode                   │
│  ┌──────────────────────────┐      │
│  │ Start: [ 9:00 PM ]        │      │
│  │ End:   [ 7:00 AM ]        │      │
│  │ [✓] Enabled                │      │
│  └──────────────────────────┘      │
│                                    │
│  📅 Weekly Report                  │
│  ┌──────────────────────────┐      │
│  │ Mon: 45m  Tue: 1h 23m    │      │
│  │ Wed: 1h 10m  Thu: [Today]│      │
│  │ [View Full Report →]      │      │
│  └──────────────────────────┘      │
│                                    │
│  🔒 Parent PIN                     │
│  ┌──────────────────────────┐      │
│  │ [****]                    │      │
│  │ [Change PIN]              │      │
│  └──────────────────────────┘      │
│                                    │
│  [Save Changes]  [Reset Defaults]  │
│                                    │
└────────────────────────────────────┘
```

### 4.2 Usage Report Design

**Weekly Report** (Email + In-App):

```
┌────────────────────────────────────┐
│     📊 Wordland Weekly Report       │
│      Feb 12 - Feb 18, 2026          │
├────────────────────────────────────┤
│                                    │
│  📈 This Week's Learning            │
│  • Total Time: 6h 45m              │
│  • Daily Average: 54 minutes        │
│  • Words Learned: 28 new words     │
│  • Current Streak: 7 days 🔥        │
│                                    │
│  📅 Daily Breakdown                 │
│  Mon: 45m  │ Tue: 1h 23m  │ Wed: 55m│
│  Thu: 1h10m │ Fri: 1h 05m  │ Sat: 1h30m│
│  Sun: 57m                            │
│                                    │
│  🏆 Achievements Unlocked           │
│  • First Steps ✅                   │
│  • Week Warrior 🔥                  │
│  • Combo Master ⭐                  │
│                                    │
│  🎯 Learning Progress                │
│  Look Island: 80% mastered          │
│  Words mastered: 42 / 60            │
│                                    │
│  💡 Tips                            │
│  Great consistency! Your child     │
│  is learning every day. Keep up    │
│  the good work!                    │
│                                    │
└────────────────────────────────────┘
```

### 4.3 Parent Notification System

**Email Notifications** (Configurable):

| Event | Notification | Frequency |
|-------|--------------|-----------|
| Daily limit reached | "Child reached daily limit" | Immediate |
| Weekly summary | Weekly learning report | Weekly (Sunday) |
| Streak milestone | "7-day streak achieved!" | Event |
| Achievement unlocked | "New achievement: Combo Master" | Event |
| Unusual activity | "Usage outside set hours" | Event |

**Notification Content**:

```kotlin
/**
 * Parent notification data model
 */
data class ParentNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val actionUrl: String? = null
)

enum class NotificationType {
    DAILY_LIMIT_REACHED,
    WEEKLY_REPORT,
    STREAK_MILESTONE,
    ACHIEVEMENT_UNLOCKED,
    UNUSUAL_ACTIVITY,
    BEDTIME_MODE_VIOLATION
}
```

### 4.4 Bedtime Mode

**Configuration**:
- Start time (e.g., 9:00 PM)
- End time (e.g., 7:00 AM)
- Enabled/Disabled toggle

**Behavior**:
- During bedtime hours: App shows "It's bedtime! Come back tomorrow"
- Override requires parent PIN
- Soft transition: Reminder 30 minutes before bedtime

**Bedtime Reminder** (30 minutes before):

```
┌────────────────────────────────────┐
│                                    │
│      🌙 Bedtime Soon! 🌙             │
│                                    │
│   Bedtime is at 9:00 PM            │
│   Time remaining: 30 minutes       │
│                                    │
│   Finish your current level,       │
│   then it's time to rest!          │
│                                    │
│      [Continue to 9:00 PM]         │
│                                    │
└────────────────────────────────────┘
```

---

## 5. Technical Implementation

### 5.1 Data Models

```kotlin
/**
 * Main session management data model
 */
@Immutable
data class SessionLimitConfig(
    val softReminderMinutes: Int = 15,
    val hardLimitMinutes: Int = 45,
    val dailyMaxHours: Double = 2.0,
    val breakReminderIntervalMinutes: Int = 5,
    val bedtimeEnabled: Boolean = false,
    val bedtimeStartHour: Int = 21,  // 9 PM
    val bedtimeEndHour: Int = 7      // 7 AM
)

/**
 * Daily usage tracking
 */
@Immutable
data class DailyUsage(
    val date: LocalDate,
    val totalActiveTimeMs: Long,
    val sessionCount: Int,
    val wordsLearned: Int,
    const val streakDays: Int
)

/**
 * Session state for UI
 */
@Immutable
data class SessionState(
    val isActive: Boolean,
    val sessionStartTime: Long,
    val currentSessionTimeMs: Long,
    val dailyTotalTimeMs: Long,
    val isPaused: Boolean,
    val isInBreak: Boolean,
    val breakEndTime: Long? = null,
    val activeBonus: BreakBonus? = null
)

/**
 * Break bonus information
 */
@Immutable
data class BreakBonus(
    val multiplier: Float,
    val wordsRemaining: Int,
    val expiresAt: Long
)

/**
 * Parental controls settings
 */
@Immutable
data class ParentalSettings(
    val sessionLimitConfig: SessionLimitConfig,
    val parentPin: String, // Hashed, not stored plain
    val emailNotificationsEnabled: Boolean,
    val notificationEmail: String,
    val weeklyReportDay: DayOfWeek = DayOfWeek.SUNDAY
)
```

### 5.2 Use Cases

```kotlin
/**
 * Use case: Start session timer
 */
class StartSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val usageTracker: UsageTracker
) {
    suspend operator fun invoke(userId: String): Result<SessionState> {
        val config = sessionRepository.getParentalConfig(userId)
        val todayUsage = usageTracker.getTodayUsage(userId)

        // Check if daily limit exceeded
        if (todayUsage.totalActiveTimeMs >= config.dailyMaxHours * 3600000) {
            return Result.Error(DailyLimitException("Daily limit reached"))
        }

        // Check if bedtime mode active
        if (config.bedtimeEnabled && isInBedtime(config)) {
            return Result.Error(BedtimeException("Bedtime mode active"))
        }

        // Start new session
        val session = SessionState(
            isActive = true,
            sessionStartTime = System.currentTimeMillis(),
            currentSessionTimeMs = 0L,
            dailyTotalTimeMs = todayUsage.totalActiveTimeMs,
            isPaused = false,
            isInBreak = false
        )

        sessionRepository.saveSessionState(userId, session)
        return Result.Success(session)
    }
}

/**
 * Use case: Update session time
 */
class UpdateSessionTimeUseCase(
    private val sessionRepository: SessionRepository,
    private val usageTracker: UsageTracker
) {
    suspend operator fun invoke(
        userId: String,
        activityState: ActivityState
    ): Result<SessionUpdateResult> {
        val currentSession = sessionRepository.getSessionState(userId)
        val config = sessionRepository.getParentalConfig(userId)
        val currentTime = System.currentTimeMillis()

        // Calculate elapsed time since last update
        val elapsed = currentTime - currentSession.lastUpdateTime

        // Only count if actively playing
        val newSessionTime = if (activityState == ActivityState.ACTIVE_GAMEPLAY) {
            currentSession.currentSessionTimeMs + elapsed
        } else {
            currentSession.currentSessionTimeMs
        }

        val newDailyTotal = currentSession.dailyTotalTimeMs +
            if (activityState == ActivityState.ACTIVE_GAMEPLAY) elapsed else 0

        val updatedSession = currentSession.copy(
            currentSessionTimeMs = newSessionTime,
            dailyTotalTimeMs = newDailyTotal,
            lastUpdateTime = currentTime
        )

        sessionRepository.saveSessionState(userId, updatedSession)
        usageTracker.recordUsage(userId, elapsed, activityState)

        // Check for limit triggers
        return Result.Success(SessionUpdateResult(
            sessionState = updatedSession,
            shouldShowSoftReminder = newSessionTime >= config.softReminderMinutes * 60000,
            shouldEnforceHardLimit = newSessionTime >= config.hardLimitMinutes * 60000,
            hasExceededDailyLimit = newDailyTotal >= config.dailyMaxHours * 3600000
        ))
    }
}

/**
 * Use case: Start break
 */
class StartBreakUseCase(
    private val sessionRepository: SessionRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(userId: String): Result<BreakState> {
        val currentSession = sessionRepository.getSessionState(userId)
        val currentTime = System.currentTimeMillis()

        val breakState = BreakState(
            breakStartTime = currentTime,
            minimumBreakDuration = 15 * 60 * 1000L, // 15 minutes
            isBonusActive = false,
            bonusMultiplier = 1.0f,
            bonusWordsRemaining = 0
        )

        val updatedSession = currentSession.copy(
            isInBreak = true,
            breakEndTime = currentTime + 15 * 60 * 1000L
        )

        sessionRepository.saveSessionState(userId, updatedSession)
        sessionRepository.saveBreakState(userId, breakState)

        return Result.Success(breakState)
    }
}

/**
 * Use case: End break and calculate rewards
 */
class EndBreakUseCase(
    private val sessionRepository: SessionRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(userId: String): Result<BreakReward> {
        val breakState = sessionRepository.getBreakState(userId)
        val currentTime = System.currentTimeMillis()

        val breakDuration = currentTime - breakState.breakStartTime
        val bonusMultiplier = breakState.calculateBreakBonus(currentTime)
        val achievement = breakState.getBreakAchievement(currentTime)

        val bonusWords = when {
            bonusMultiplier >= 3.0f -> 10  // 1+ hour: 10 words
            bonusMultiplier >= 2.0f -> 6   // 30 min: 6 words
            bonusMultiplier >= 1.5f -> 5   // 15 min: 5 words
            bonusMultiplier >= 1.2f -> 3   // 5 min: 3 words
            else -> 0
        }

        val reward = BreakReward(
            multiplier = bonusMultiplier,
            wordsRemaining = bonusWords,
            achievementId = achievement
        )

        // Unlock achievement if earned
        if (achievement != null) {
            achievementRepository.unlockAchievement(userId, achievement)
        }

        // Update session state
        val currentSession = sessionRepository.getSessionState(userId)
        sessionRepository.saveSessionState(
            userId,
            currentSession.copy(
                isInBreak = false,
                breakEndTime = null,
                activeBonus = BreakBonus(
                    multiplier = bonusMultiplier,
                    wordsRemaining = bonusWords,
                    expiresAt = currentTime + (24 * 60 * 60 * 1000L) // 24 hours
                )
            )
        )

        return Result.Success(reward)
    }
}

/**
 * Use case: Update parental settings
 */
class UpdateParentalSettingsUseCase(
    private val sessionRepository: SessionRepository,
    private val authService: AuthService
) {
    suspend operator fun invoke(
        userId: String,
        currentPin: String,
        newSettings: SessionLimitConfig
    ): Result<Boolean> {
        // Verify current PIN
        if (!authService.verifyParentPin(userId, currentPin)) {
            return Result.Error(UnauthorizedException("Invalid PIN"))
        }

        // Update settings
        sessionRepository.saveParentalConfig(userId, newSettings)
        return Result.Success(true)
    }
}
```

### 5.3 Database Schema

```sql
-- Session tracking table
CREATE TABLE session_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    session_start_time INTEGER NOT NULL,
    session_end_time INTEGER,
    active_time_ms INTEGER NOT NULL DEFAULT 0,
    was_forced_break INTEGER DEFAULT 0,
    break_taken INTEGER DEFAULT 0,
    break_duration_ms INTEGER DEFAULT 0,
    date TEXT NOT NULL, -- YYYY-MM-DD
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Daily usage summary table
CREATE TABLE daily_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    date TEXT NOT NULL UNIQUE, -- YYYY-MM-DD
    total_active_time_ms INTEGER NOT NULL DEFAULT 0,
    session_count INTEGER NOT NULL DEFAULT 0,
    words_learned INTEGER NOT NULL DEFAULT 0,
    achievements_unlocked TEXT, -- JSON array
    streak_days INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Parental settings table
CREATE TABLE parental_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL UNIQUE,
    soft_reminder_minutes INTEGER NOT NULL DEFAULT 15,
    hard_limit_minutes INTEGER NOT NULL DEFAULT 45,
    daily_max_hours REAL NOT NULL DEFAULT 2.0,
    break_reminder_interval_minutes INTEGER NOT NULL DEFAULT 5,
    bedtime_enabled INTEGER NOT NULL DEFAULT 0,
    bedtime_start_hour INTEGER NOT NULL DEFAULT 21,
    bedtime_end_hour INTEGER NOT NULL DEFAULT 7,
    parent_pin_hash TEXT NOT NULL,
    email_notifications_enabled INTEGER NOT NULL DEFAULT 1,
    notification_email TEXT,
    weekly_report_day TEXT NOT NULL DEFAULT 'SUNDAY',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Break history table
CREATE TABLE break_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    break_start_time INTEGER NOT NULL,
    break_end_time INTEGER NOT NULL,
    break_duration_ms INTEGER NOT NULL,
    bonus_multiplier REAL NOT NULL DEFAULT 1.0,
    bonus_words_used INTEGER NOT NULL DEFAULT 0,
    achievement_unlocked TEXT,
    date TEXT NOT NULL, -- YYYY-MM-DD
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 6. Integration with Existing Gameplay

### 6.1 LearningScreen Integration Points

```kotlin
@Composable
fun LearningScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory()),
    sessionViewModel: SessionViewModel = viewModel(factory = AppServiceLocator.provideFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState by sessionViewModel.sessionState.collectAsState()

    // Session timer in top bar
    TopAppBar(
        title = { Text(getLevelDisplayName(levelId)) },
        actions = {
            SessionTimerDisplay(
                sessionTime = sessionState.currentSessionTimeMs,
                dailyTime = sessionState.dailyTotalTimeMs,
                onClick = { sessionViewModel.showSessionDetails() }
            )
        },
        navigationIcon = { /* back button */ }
    )

    // Session reminder dialogs
    when {
        sessionState.shouldShowSoftReminder -> {
            SoftReminderDialog(
                onContinue = { sessionViewModel.dismissSoftReminder() },
                onBreak = { sessionViewModel.startBreak() }
            )
        }
        sessionState.shouldEnforceHardLimit -> {
            HardLimitDialog(
                onTakeBreak = { sessionViewModel.startBreak() }
            )
        }
        sessionState.hasExceededDailyLimit -> {
            DailyLimitDialog(
                onBack = onNavigateBack
            )
        }
    }

    // Break screen
    if (sessionState.isInBreak) {
        BreakScreen(
            breakEndTime = sessionState.breakEndTime!!,
            onBreakComplete = { /* unlock */ }
        )
    }

    // Main learning content
    // ... existing content
}
```

### 6.2 Bonus Display in Feedback

```kotlin
@Composable
fun FeedbackContent(
    result: LearnWordResult,
    stars: Int,
    progress: Float,
    comboState: ComboState,
    bonusMultiplier: Float, // NEW
    onContinue: () -> Unit
) {
    Column {
        // Existing feedback UI

        // NEW: Bonus indicator
        if (bonusMultiplier > 1.0f) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Break Bonus: ${bonusMultiplier}x score!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
```

---

## 7. COPPA Compliance Considerations

### 7.1 Parental Consent

**Required**:
- Verifiable parental consent before data collection
- Clear explanation of what data is collected and why
- Easy opt-out mechanism

**Implementation**:
```
┌────────────────────────────────────┐
│      Parental Consent Required      │
├────────────────────────────────────┤
│                                    │
│  To use Wordland, we need your     │
│  permission to collect usage       │
│  data for:                         │
│                                    │
│  ✓ Tracking learning progress      │
│  ✓ Session time management         │
│  ✓ Sending weekly reports          │
│                                    │
│  [Read Full Privacy Policy]        │
│                                    │
│  Parent Email: [________________]   │
│                                    │
│  [ ] I consent to data collection  │
│                                    │
│     [Agree & Continue]             │
│     [Decline]                      │
│                                    │
└────────────────────────────────────┘
```

### 7.2 Data Minimization

**Collect Only**:
- Session time (duration)
- Words learned (progress)
- App usage patterns
- Parent email (for reports)

**Never Collect**:
- Location data
- Contact information (child's)
- Voice/video recordings
- Social media information
- Biometric data

### 7.3 Data Retention

**Policy**:
- Daily usage data: 90 days
- Weekly reports: 1 year
- Progress data: Until account deletion

**Parent Control**:
- Export all data on request
- Delete account and all data on request

---

## 8. Testing Checklist

### 8.1 Functional Testing

- [ ] Session timer counts only active gameplay
- [ ] Soft reminder appears at 15 minutes
- [ ] Hard limit enforced at 45 minutes
- [ ] Daily limit blocks at 2 hours
- [ ] Break rewards calculate correctly
- [ ] Parent PIN protects settings
- [ ] Bedtime mode blocks during set hours
- [ ] Break timer countdown works correctly
- [ ] Bonus multiplier applies to scores
- [ ] Background/paused time doesn't count

### 8.2 UX Testing

- [ ] Timers are visible but not intrusive
- [ ] Break reminders feel encouraging, not punitive
- [ ] Parent dashboard is intuitive
- [ ] Children understand break system
- [ ] Returning from break feels rewarding
- [ ] Daily limit screen is positive, not disappointing

### 8.3 Edge Cases

- [ ] App crashes during session → time saved
- [ ] Phone call during session → time paused
- [ ] Date change during session → handled correctly
- [ ] Multiple devices → sync properly
- [ ] Timezone changes → handled correctly
- [ ] Parent forgot PIN → recovery process

### 8.4 Child Testing (10-year-olds)

- [ ] Understand break system?
- [ ] Feel motivated to take breaks?
- [ ] Like the break rewards?
- [ ] Find timer helpful?
- [ ] Understand bedtime message?
- [ ] Feel encouraged, not restricted?

---

## 9. Success Metrics

### 9.1 Health Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Average session duration | 15-30 minutes | Session tracking |
| Daily active users < 1 hour | >80% | Usage analytics |
| Daily active users > 2 hours | <5% | Usage analytics |
| Break completion rate | >70% | Break tracking |
| Parent control usage | >50% | Feature analytics |

### 9.2 Engagement Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| 7-day retention | >60% | Cohort analysis |
| 30-day retention | >40% | Cohort analysis |
| Average words learned/day | 10-15 | Progress tracking |
| Streak days (average) | >5 days | Streak tracking |

### 9.3 Parent Satisfaction

| Metric | Target | Measurement |
|--------|--------|-------------|
| Parent report open rate | >70% | Email analytics |
| Parent settings modification | >30% | Feature analytics |
| Parent satisfaction score | >4.0/5.0 | In-app survey |
| COPPA compliance | 100% | Legal audit |

---

## 10. Implementation Phases

### Phase 1: Core Session Tracking (Week 1)
- [ ] Session timer implementation
- [ ] Active time detection
- [ ] Soft reminder UI
- [ ] Hard limit enforcement
- [ ] Daily limit blocking
- [ ] Unit tests

### Phase 2: Break System (Week 2)
- [ ] Break state management
- [ ] Break rewards calculation
- [ ] Break screen UI
- [ ] Bonus integration with scoring
- [ ] Break achievements
- [ ] Integration tests

### Phase 3: Parental Controls (Week 3)
- [ ] Parent dashboard UI
- [ ] Settings persistence
- [ ] PIN authentication
- [ ] Bedtime mode
- [ ] Usage report generation
- [ ] Email notifications

### Phase 4: Polish & Testing (Week 4)
- [ ] UI/UX refinements
- [ ] Animations and transitions
- [ ] Child user testing
- [ ] Parent user testing
- [ ] COPPA compliance review
- [ ] Performance optimization

---

## 11. Future Enhancements

### 11.1 Smart Scheduling

```kotlin
/**
 * AI-powered optimal learning time suggestions
 * Based on child's historical performance
 */
data class OptimalLearningTime(
    val hourOfDay: Int,
    val dayOfWeek: DayOfWeek,
    val predictedPerformance: Float,
    val reason: String
)

// Example:
// "Best time to learn: 4 PM - 6 PM on weekdays"
// "Your child scores 15% higher during afternoon sessions"
```

### 11.2 Gamified Breaks

```kotlin
/**
 * Mini-games during breaks to encourage physical activity
 */
sealed class BreakActivity {
    data class EyeExercise(
        val description: "Find 5 blue objects",
        val duration: 30
    ) : BreakActivity()

    data class Stretching(
        val description: "Stretch your arms overhead",
        val duration: 20
    ) : BreakActivity()

    data class Breathing(
        val description: "Take 5 deep breaths",
        val duration: 60
    ) : BreakActivity()
}
```

### 11.3 Family Challenges

```kotlin
/**
 * Family-wide learning challenges with anti-addiction safeguards
 */
data class FamilyChallenge(
    val name: String,
    val description: String,
    val maxDailyTimePerChild: Int, // Time limit per child
    val collectiveGoal: Int, // Total words family learns together
    val reward: FamilyReward
)
```

---

## 12. Conclusion

This anti-addiction system design prioritizes child safety while maintaining an engaging learning experience. The key principles are:

1. **Gentle Guidance**: Soft reminders before hard limits
2. **Positive Reinforcement**: Rewards for healthy behavior
3. **Parent Empowerment**: Full visibility and control
4. **Minimal Disruption**: Seamless integration with gameplay

The system is designed to feel like a helpful feature, not a restriction. By framing breaks as "well-earned rest" and providing tangible rewards, children will develop healthy screen time habits naturally.

---

**Next Steps**:
1. Review with android-engineer for technical feasibility
2. Review with education-specialist for age-appropriateness
3. Create UI mockups with compose-ui-designer
4. Begin Phase 1 implementation

**Document Version**: 1.0
**Last Updated**: 2026-02-18
