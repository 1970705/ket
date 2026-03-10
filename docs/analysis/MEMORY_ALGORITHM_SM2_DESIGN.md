# Enhanced Memory Algorithm - SM-2 Design Specification

**Author**: Education Specialist
**Date**: 2026-02-17
**Status**: Design Phase
**Priority**: P2 - Enhancement

---

## 1. Overview

This document specifies the design for an enhanced memory algorithm based on the **SuperMemo-2 (SM-2)** algorithm, which powers Anki and other successful spaced repetition systems.

### Goals

1. **Individual Forgetting Rate**: Track how quickly each user forgets specific words
2. **Exponential Intervals**: Replace fixed intervals with dynamic, exponential scaling
3. **Ease Factor**: Track per-word difficulty based on actual performance
4. **Priority Scheduling**: Review most urgent words first

---

## 2. Current System Analysis

### 2.1 Current Algorithm (MemoryStrengthAlgorithm.kt)

```kotlin
// Current fixed intervals based on strength
val intervalMinutes = when {
    currentStrength < 30 -> 10L      // 10 minutes
    currentStrength < 50 -> 60L      // 1 hour
    currentStrength < 70 -> 4 * 60L  // 4 hours
    currentStrength < 85 -> 24 * 60L // 1 day
    else -> 7 * 24 * 60L             // 1 week
}
```

**Problems:**
- Fixed intervals don't account for individual differences
- No per-word difficulty tracking
- Linear progression (not exponential)
- Same schedule for "easy" and "hard" words at same strength level

### 2.2 Current Data Model (UserWordProgress.kt)

```kotlin
data class UserWordProgress(
    val wordId: String,
    val userId: String,
    val status: LearningStatus,
    val memoryStrength: Int = 30,  // 0-100
    val lastReviewTime: Long? = null,
    val nextReviewTime: Long? = null,
    // ... other fields
)
```

**Missing Fields:**
- Ease factor (EF)
- Interval (I)
- Repetitions (n)
- Quality score history

---

## 3. SM-2 Algorithm Design

### 3.1 Core Concepts

| Symbol | Meaning | Range |
|--------|---------|-------|
| **n** | Repetition number | 0, 1, 2, 3, ... |
| **EF** | Ease factor | 1.3 - 2.5 (initial) |
| **I** | Inter-repetition interval | days |
| **q** | Quality score | 0-5 |

### 3.2 Algorithm Steps

#### Step 1: Calculate Quality Score (q)

Based on user response:

| Response | Time | Accuracy | Quality (q) |
|----------|------|----------|-------------|
| Perfect | < 3s | Correct | 5 |
| Hesitant | 3-10s | Correct | 4 |
| Struggled | > 10s | Correct | 3 |
| Wrong (knew it) | N/A | Incorrect | 2 |
| Wrong (didn't know) | N/A | Incorrect | 1 |
| Blackout | N/A | Incorrect | 0 |

```kotlin
fun calculateQuality(
    isCorrect: Boolean,
    responseTimeMs: Long,
    wordDifficulty: Int
): Int {
    val expectedTime = when (wordDifficulty) {
        1 -> 2000L
        2 -> 3000L
        3 -> 4000L
        4 -> 5000L
        5 -> 6000L
        else -> 4000L
    }

    return when {
        !isCorrect -> {
            // Determine if partial knowledge
            when {
                responseTimeMs > 15000 -> 0 // Blackout
                responseTimeMs > 8000 -> 1  // Didn't know
                else -> 2                   // Knew it but forgot
            }
        }
        isCorrect && responseTimeMs < expectedTime * 0.7 -> 5 // Perfect
        isCorrect && responseTimeMs < expectedTime * 1.5 -> 4 // Hesitant
        else -> 3 // Struggled
    }
}
```

#### Step 2: Update Ease Factor (EF)

```
EF' = EF + (0.1 - (5 - q) × (0.08 + (5 - q) × 0.02))
```

Key properties:
- q ≥ 4: EF increases (word gets easier)
- q = 3: EF stays same
- q ≤ 2: EF decreases (word gets harder)

```kotlin
fun updateEaseFactor(currentEF: Float, quality: Int): Float {
    val newEF = currentEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    return newEF.coerceIn(MIN_EF, MAX_EF) // 1.3 to 2.7
}
```

#### Step 3: Calculate Next Interval (I)

```
IF n = 0:  I = 0          // First review
IF n = 1:  I = 1 day      // Second review
IF n ≥ 2:  I = I_prev × EF
```

```kotlin
fun calculateNextInterval(
    repetitions: Int,
    previousInterval: Int,
    easeFactor: Float
): Int {
    return when (repetitions) {
        0 -> 0           // First time
        1 -> 1           // 1 day
        else -> {
            val days = previousInterval * easeFactor
            days.toInt().coerceAtLeast(1)
        }
    }
}
```

#### Step 4: Update Repetitions

```
IF q ≥ 3: n = n + 1  (successful recall)
ELSE:     n = 0      (failed recall, restart)
```

---

## 4. Child-Friendly Modifications

### 4.1 Relaxed Quality Scoring

Children have variable attention spans and response times:

```kotlin
// More lenient time thresholds for children
val childMultiplier = 1.5
val adjustedTime = responseTimeMs * childMultiplier
```

### 4.2 Minimum Interval Protection

Ensure daily practice even for "mastered" words:

```kotlin
val maxInterval = 30  // Cap at 30 days for children
```

### 4.3 Hint Impact on Quality

Using hints should reduce quality:

```kotlin
val quality = when {
    hintsUsed > 0 -> baseQuality - 1  // Penalty for hints
    else -> baseQuality
}
```

---

## 5. Data Model Changes

### 5.1 New UserWordProgress Fields

```kotlin
data class UserWordProgress(
    // ... existing fields ...

    // SM-2 Algorithm Fields
    val easeFactor: Float = 2.5f,           // Default ease
    val sm2Interval: Int = 0,               // Days until next review
    val sm2Repetitions: Int = 0,            // Successful recalls
    val lastQuality: Int? = null,           // Last quality score

    // Forgetting Rate Estimation
    val forgettingRate: Float = 0.5f,       // Individual rate (0-1)
    val totalReviews: Int = 0,              // Total review count
    val successfulReviews: Int = 0          // Successful count
)
```

### 5.2 Database Migration

```sql
ALTER TABLE user_word_progress ADD COLUMN easeFactor REAL DEFAULT 2.5;
ALTER TABLE user_word_progress ADD COLUMN sm2Interval INTEGER DEFAULT 0;
ALTER TABLE user_word_progress ADD COLUMN sm2Repetitions INTEGER DEFAULT 0;
ALTER TABLE user_word_progress ADD COLUMN lastQuality INTEGER DEFAULT NULL;
ALTER TABLE user_word_progress ADD COLUMN forgettingRate REAL DEFAULT 0.5;
ALTER TABLE user_word_progress ADD COLUMN totalReviews INTEGER DEFAULT 0;
ALTER TABLE user_word_progress ADD COLUMN successfulReviews INTEGER DEFAULT 0;
```

---

## 6. Priority Review Scheduling

### 6.1 Priority Score Calculation

```kotlin
data class ReviewPriority(
    val wordId: String,
    val priorityScore: Float,  // Higher = more urgent
    val overdueDays: Int
)

fun calculatePriority(progress: UserWordProgress): Float {
    val now = System.currentTimeMillis()
    val daysUntilDue = when {
        progress.nextReviewTime == null -> 0
        else -> (progress.nextReviewTime!! - now) / (24 * 60 * 60 * 1000)
    }

    // Priority factors
    val overduePenalty = when {
        daysUntilDue < 0 -> abs(daysUntilDue).toFloat() * 10  // Urgent!
        daysUntilDue < 1 -> 5f  // Due today
        else -> 0f
    }

    val difficultyBonus = (6 - progress.forgettingRate * 10)  // Harder = higher priority
    const easeFactorPenalty = (2.7 - progress.easeFactor) * 2  // Lower EF = higher priority

    return overduePenalty + difficultyBonus + easeFactorPenalty
}
```

### 6.2 Review Queue Sorting

```kotlin
fun getReviewQueue(userId: String): List<ReviewWordItem> {
    return getAllWordProgress(userId)
        .map { progress ->
            val priority = calculatePriority(progress)
            ReviewPriority(progress.wordId, priority, ...)
        }
        .sortedByDescending { it.priorityScore }  // Most urgent first
        .take(20)  // Limit to 20 words per session
}
```

---

## 7. API Design

### 7.1 Enhanced MemoryStrengthAlgorithm

```kotlin
object MemoryStrengthAlgorithmEnhanced {

    /**
     * Process a review attempt and calculate next interval
     */
    fun processReview(
        currentProgress: UserWordProgress,
        isCorrect: Boolean,
        responseTimeMs: Long,
        hintsUsed: Int,
        wordDifficulty: Int
    ): ReviewResult {
        // 1. Calculate quality score
        val quality = calculateQuality(isCorrect, responseTimeMs, hintsUsed, wordDifficulty)

        // 2. Update ease factor
        val newEF = updateEaseFactor(currentProgress.easeFactor, quality)

        // 3. Update repetitions
        val newReps = if (quality >= 3) currentProgress.sm2Repetitions + 1 else 0

        // 4. Calculate next interval
        val newInterval = calculateNextInterval(
            newReps,
            currentProgress.sm2Interval,
            newEF
        )

        // 5. Update memory strength (mapped from EF and interval)
        val newStrength = calculateMemoryStrength(newEF, newReps, newInterval)

        return ReviewResult(
            newEaseFactor = newEF,
            newInterval = newInterval,
            newRepetitions = newReps,
            newMemoryStrength = newStrength,
            quality = quality,
            nextReviewTime = calculateNextReviewTime(newInterval)
        )
    }
}

data class ReviewResult(
    val newEaseFactor: Float,
    val newInterval: Int,        // days
    val newRepetitions: Int,
    val newMemoryStrength: Int,  // 0-100
    val quality: Int,            // 0-5
    val nextReviewTime: Long     // timestamp
)
```

---

## 8. Testing Strategy

### 8.1 Unit Tests

```kotlin
class MemoryStrengthAlgorithmEnhancedTest {

    @Test
    fun `quality score 5 increases ease factor`() {
        val initialEF = 2.5f
        val newEF = updateEaseFactor(initialEF, 5)
        assertTrue(newEF > initialEF)
    }

    @Test
    fun `quality score 0 decreases ease factor`() {
        val initialEF = 2.5f
        val newEF = updateEaseFactor(initialEF, 0)
        assertTrue(newEF < initialEF)
    }

    @Test
    fun `ease factor stays within bounds`() {
        val ef = updateEaseFactor(1.3f, 5)  // Min EF
        assertEquals(1.3f, ef, 0.01f)

        val ef2 = updateEaseFactor(2.7f, 5)  // Max EF
        assertEquals(2.7f, ef2, 0.01f)
    }

    @Test
    fun `interval grows exponentially with successful reviews`() {
        val interval1 = calculateNextInterval(1, 0, 2.5f)  // 1 day
        val interval2 = calculateNextInterval(2, interval1, 2.5f)  // ~2.5 days
        val interval3 = calculateNextInterval(3, interval2, 2.5f)  // ~6.25 days

        assertTrue(interval3 > interval2)
        assertTrue(interval2 > interval1)
    }

    @Test
    fun `failed review resets repetitions`() {
        val reps = 5
        val quality = 2  // Failed
        // Quality < 3 should reset to 0
    }
}
```

### 8.2 Integration Tests

```kotlin
@Test
fun `full review cycle from new to mastered`() {
    // Simulate 10 reviews with varying quality
    // Verify EF increases with good performance
    // Verify intervals grow appropriately
}
```

---

## 9. Migration Strategy

### 9.1 Backward Compatibility

For existing users, initialize SM-2 fields from current state:

```kotlin
fun migrateFromLegacy(progress: UserWordProgress): UserWordProgress {
    return progress.copy(
        easeFactor = when {
            progress.memoryStrength >= 80 -> 2.5f  // Easy
            progress.memoryStrength >= 50 -> 2.3f  // Medium
            else -> 2.0f  // Hard
        },
        sm2Repetitions = progress.correctAttempts.coerceAtMost(10),
        sm2Interval = when {
            progress.memoryStrength >= 80 -> 7
            progress.memoryStrength >= 60 -> 3
            progress.memoryStrength >= 40 -> 1
            else -> 0
        }
    )
}
```

### 9.2 Gradual Rollout

1. Phase 1: Deploy new fields with migration (no algorithm change)
2. Phase 2: Deploy new algorithm for new words only
3. Phase 3: Enable for all words with monitoring

---

## 10. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Long-term retention | > 80% | Words correct after 30 days |
| Review efficiency | > 90% | Words reviewed only when needed |
| User engagement | +20% | Daily active users |
| Learning velocity | +15% | Words mastered per week |

---

## 11. References

1. **SuperMemo-2 Paper**: Piotr Woźniak, "Optimization of repetition spacing"
2. **Anki Documentation**: https://docs.ankiweb.net/
3. **Ebbinghaus Forgetting Curve**: "Memory: A Contribution to Experimental Psychology" (1885)
4. **Child Learning Research**: Metacognitive development in spaced repetition

---

## Appendix A: Interval Comparison

### Current vs SM-2 Intervals

| Repetitions | Current (if strength=60) | SM-2 (EF=2.5) |
|-------------|-------------------------|---------------|
| 1st review | 4 hours | 1 day |
| 2nd review | 1 day | 2.5 days |
| 3rd review | 1 day | 6.25 days |
| 4th review | 1 week | 15.6 days |
| 5th review | 1 week | 39 days |

### Key Differences

1. **SM-2 grows faster** - Reviews become less frequent more quickly
2. **SM-2 adapts** - EF adjusts based on actual performance
3. **SM-2 resets on failure** - Missed reviews restart the cycle
