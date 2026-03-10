# Star Rating Behavior Audit

**Document Version**: 1.0
**Audit Date**: 2026-02-24
**Auditor**: android-architect
**Epic**: #5 Dynamic Star Rating Algorithm
**Purpose**: Comprehensive audit of existing star rating system

---

## Executive Summary

The Wordland app has **two separate star rating systems** that operate at different levels:

1. **Per-Word Rating** (`SubmitAnswerUseCase.calculateStars()`) - Awards 0-3 stars per individual word
2. **Level-Level Rating** (`StarRatingCalculator.calculateStars()`) - Awards 0-3 stars for completing a level

**Key Finding**: The algorithm already exists and is fully functional. This is **not** a greenfield implementation.

---

## Part 1: Per-Word Rating System

**File**: `app/src/main/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCase.kt`
**Method**: `calculateStars()` (lines 204-225)

### Algorithm

```kotlin
private fun calculateStars(
    isCorrect: Boolean,
    isGuessing: Boolean,
    responseTime: Long,
    hintUsed: Boolean,
    wordLength: Int,
): Int {
    if (!isCorrect) return 0
    if (isGuessing) return 1

    // Calculate child-friendly threshold based on word length
    // Base time (1s) + 0.5s per letter
    val adequateThinkingTime =
        DomainConstants.MIN_RESPONSE_TIME_MS +
            (wordLength * DomainConstants.MILLIS_PER_LETTER_THRESHOLD)

    return when {
        hintUsed -> 2 // Used hint, max 2 stars
        responseTime < adequateThinkingTime -> 2 // Too fast, potential guessing
        else -> 3 // Adequate thinking time, full stars
    }
}
```

### Decision Tree

```
                    ┌─────────────┐
                    │ Answer      │
                    │ Correct?    │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │ NO                      │ YES
              ▼                         ▼
        ┌──────────┐           ┌──────────────┐
        │ Return 0 │           │ isGuessing?  │
        │ stars    │           └──────┬───────┘
        └──────────┘                  │
                              ┌────────┴────────┐
                              │ YES             │ NO
                              ▼                 ▼
                        ┌──────────┐    ┌──────────────────┐
                        │ Return 1 │    │ hintUsed?        │
                        │ star     │    └──────┬───────────┘
                        └──────────┘           │
                                    ┌──────────┴──────────┐
                                    │ YES                 │ NO
                                    ▼                     ▼
                              ┌──────────┐      ┌──────────────────┐
                              │ Return 2 │      │ Time < Threshold?│
                              │ stars    │      └──────┬───────────┘
                              └──────────┘             │
                                                 ┌────────┴────────┐
                                                 │ YES             │ NO
                                                 ▼                 ▼
                                           ┌──────────┐      ┌──────────┐
                                           │ Return 2 │      │ Return 3 │
                                           │ stars    │      │ stars    │
                                           └──────────┘      └──────────┘
```

### Factor Thresholds

| Factor | Threshold | Value Used |
|--------|-----------|------------|
| `MIN_RESPONSE_TIME_MS` | 1000 ms | Base time for any answer |
| `MILLIS_PER_LETTER_THRESHOLD` | 500 ms | Additional time per letter |

### Word Length Examples

| Word Length | Minimum Time for 3 Stars |
|-------------|--------------------------|
| 3 letters | 1000 + (3 × 500) = 2,500 ms (2.5s) |
| 4 letters | 1000 + (4 × 500) = 3,000 ms (3.0s) |
| 5 letters | 1000 + (5 × 500) = 3,500 ms (3.5s) |
| 6 letters | 1000 + (6 × 500) = 4,000 ms (4.0s) |
| 8 letters | 1000 + (8 × 500) = 5,000 ms (5.0s) |

### Star Matrix (Per-Word)

| Scenario | Correct | Guessing | Hint Used | Response Time | Stars |
|----------|---------|----------|-----------|---------------|-------|
| Perfect | Yes | No | No | ≥ threshold | 3 |
| Too fast | Yes | No | No | < threshold | 2 |
| With hint | Yes | No | Yes | Any | 2 |
| Guessing | Yes | Yes | No | Any | 1 |
| Wrong | No | N/A | N/A | Any | 0 |

---

## Part 2: Level-Level Rating System

**File**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`
**Method**: `calculateStars()` (lines 121-171)
**Total Lines**: 337 lines

### Algorithm Formula

```
Base Score = Accuracy Score - Hint Penalty + Time Bonus - Error Penalty + Combo Bonus
Stars = Convert Score to 1-3 using thresholds
```

### Scoring Formula Components

```kotlin
val totalScore = accuracyScore - hintPenalty + timeBonus - errorPenalty + comboBonus
```

#### 1. Accuracy Score (60% weight)

```kotlin
accuracyScore = (correctAnswers / totalWords) × MAX_STARS
// MAX_STARS = 3
```

| Accuracy | Score |
|----------|-------|
| 6/6 (100%) | 3.0 |
| 5/6 (83%) | 2.5 |
| 4/6 (67%) | 2.0 |
| 3/6 (50%) | 1.5 |
| 2/6 (33%) | 1.0 |
| 1/6 (17%) | 0.5 |

#### 2. Hint Penalty

```kotlin
hintPenalty = minOf(hintsUsed × 0.25, 0.5)
```

| Hints Used | Penalty |
|------------|---------|
| 0 | 0.00 |
| 1 | 0.25 |
| 2 | 0.50 (capped) |
| 3+ | 0.50 (capped) |

#### 3. Time Bonus/Penalty

```kotlin
timeBonus = when {
    avgTimePerWord < 1500ms -> -0.6  // Guessing penalty
    avgTimePerWord < 5000ms -> +0.3  // Fast bonus
    avgTimePerWord ≤ 15000ms -> 0.0  // Normal
    else -> -0.2                      // Slow penalty
}
```

| Avg Time/Word | Bonus |
|---------------|-------|
| < 1.5s | -0.6 (guessing) |
| 1.5s - 5s | +0.3 (fast) |
| 5s - 15s | 0.0 (normal) |
| > 15s | -0.2 (slow) |

#### 4. Error Penalty

```kotlin
errorPenalty = minOf(wrongAnswers × 0.1, 0.3)
```

| Wrong Answers | Penalty |
|---------------|---------|
| 0 | 0.00 |
| 1 | 0.10 |
| 2 | 0.20 |
| 3+ | 0.30 (capped) |

#### 5. Combo Bonus

```kotlin
comboBonus = when {
    maxCombo >= 10 -> +1.0
    maxCombo >= 5 -> +0.5
    else -> 0.0
}
```

| Max Combo | Bonus |
|-----------|-------|
| 0-4 | 0.0 |
| 5-9 | +0.5 |
| 10+ | +1.0 |

### Star Thresholds

```kotlin
when {
    totalScore >= 2.5 → 3 stars
    totalScore >= 1.5 → 2 stars
    totalScore > 0 || correctAnswers > 0 → 1 star
    else → 0 stars
}
```

| Total Score | Stars |
|-------------|-------|
| ≥ 2.5 | ★★★ |
| ≥ 1.5 | ★★ |
| > 0 OR any correct | ★ |
| = 0 AND no correct | 0 |

### Decision Tree (Level-Level)

```
┌─────────────────────────────────────────────────────────────────┐
│                    START LEVEL CALCULATION                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    ┌────────┴────────┐
                    │ Any Correct?    │
                    └────────┬────────┘
                             │
                ┌────────────┴────────────┐
                │ NO                       │ YES
                ▼                           ▼
          ┌──────────┐            ┌──────────────────┐
          │ Return 0 │            │ Calculate        │
          │ stars    │            │ Accuracy Score   │
          └──────────┘            │ (correct/total)  │
                                   └────────┬─────────┘
                                            │
                                   ┌────────┴────────┐
                                   │ Calculate       │
                                   │ Hint Penalty    │
                                   └────────┬────────┘
                                            │
                                   ┌────────┴────────┐
                                   │ Calculate       │
                                   │ Time Bonus      │
                                   └────────┬────────┘
                                            │
                                   ┌────────┴────────┐
                                   │ Calculate       │
                                   │ Error Penalty   │
                                   └────────┬────────┘
                                            │
                                   ┌────────┴────────┐
                                   │ Calculate       │
                                   │ Combo Bonus     │
                                   └────────┬────────┘
                                            │
                                   ┌────────┴────────┐
                                   │ Combine All     │
                                   │ totalScore =    │
                                   │ acc - hint +    │
                                   │ time - err +    │
                                   │ combo           │
                                   └────────┬────────┘
                                            │
                          ┌─────────────────┼─────────────────┐
                          │                 │                 │
                     totalScore ≥ 2.5  totalScore ≥ 1.5  totalScore > 0
                          │                 │                 OR any correct
                          ▼                 ▼                 ▼
                   ┌──────────┐      ┌──────────┐      ┌──────────┐
                   │ 3 Stars  │      │ 2 Stars  │      │ 1 Star   │
                   │ ★★★     │      │ ★★      │      │ ★        │
                   └──────────┘      └──────────┘      └──────────┘
```

---

## Part 3: Behavior Matrix

### Scenario Matrix (6-Word Level)

| # | Correct | Hints | Avg Time | Errors | Combo | Calculation | Result |
|---|---------|-------|----------|--------|-------|-------------|--------|
| 1 | 6/6 | 0 | 4s | 0 | 6 | 3.0 + 0.3 = 3.3 | ★★★ |
| 2 | 6/6 | 0 | 1s | 0 | 0 | 3.0 - 0.6 = 2.4 | ★★ |
| 3 | 5/6 | 0 | 5s | 1 | 5 | 2.5 - 0.1 + 0.5 = 2.9 | ★★★ |
| 4 | 5/6 | 0 | 5s | 1 | 0 | 2.5 - 0.1 = 2.4 | ★★ |
| 5 | 5/6 | 2 | 5s | 1 | 0 | 2.5 - 0.5 - 0.1 = 1.9 | ★★ |
| 6 | 4/6 | 0 | 5s | 2 | 0 | 2.0 - 0.2 = 1.8 | ★★ |
| 7 | 4/6 | 0 | 5s | 2 | 5 | 2.0 - 0.2 + 0.5 = 2.3 | ★★ |
| 8 | 4/6 | 0 | 5s | 2 | 10 | 2.0 - 0.2 + 1.0 = 2.8 | ★★★ |
| 9 | 3/6 | 0 | 5s | 3 | 0 | 1.5 - 0.3 = 1.2 | ★ |
| 10 | 3/6 | 3 | 5s | 3 | 0 | 1.5 - 0.5 - 0.3 = 0.7 | ★ |
| 11 | 2/6 | 0 | 5s | 4 | 0 | 1.0 - 0.3 = 0.7 | ★ |
| 12 | 1/6 | 5 | 2s | 5 | 0 | 0.5 - 0.5 + 0.3 - 0.3 = 0.0 | ★ |
| 13 | 0/6 | 0 | 5s | 6 | 0 | 0.0 | 0 |

### Extreme Scenarios

| Scenario | Description | Stars |
|----------|-------------|-------|
| **Perfect Fast** | 6/6, 0 hints, 2s avg, 0 errors, 10 combo | ★★★ (3.0 - 0.6 + 1.0 = 3.4) |
| **Perfect Slow** | 6/6, 0 hints, 20s avg, 0 errors, 0 combo | ★★★ (3.0 - 0.2 = 2.8) |
| **Borderline 3★** | 5/6, 0 hints, 5s avg, 0 errors, 0 combo | ★★★ (2.5 = threshold) |
| **All Hints** | 6/6, 6 hints, 5s avg, 0 errors, 0 combo | ★★ (3.0 - 0.5 = 2.5) |
| **Massive Combo** | 4/6, 0 hints, 5s avg, 2 errors, 10 combo | ★★★ (2.0 - 0.2 + 1.0 = 2.8) |
| **Worst Passing** | 1/6, 5 hints, 2s avg, 5 errors, 0 combo | ★ (child-friendly min) |
| **All Wrong** | 0/6 | 0 |

---

## Part 4: Integration Points

**File**: `app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt`

### Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     LearningViewModel                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Level State (tracked per level):                               │
│  - levelStartTime: Long                                         │
│  - totalHintsUsedInLevel: Int                                   │
│  - totalWrongAnswersInLevel: Int                                │
│  - correctAnswersInLevel: Int                                   │
│  - maxComboInLevel: Int                                         │
│  - starsEarnedInLevel: List<Int> (per-word stars)              │
│                                                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
            ┌────────────────┴────────────────┐
            │                                 │
            ▼                                 ▼
┌───────────────────────┐         ┌─────────────────────────┐
│   submitAnswer()      │         │   calculateLevelStars() │
├───────────────────────┤         ├─────────────────────────┤
│ Calls                 │         │ Calls                   │
│ SubmitAnswerUseCase   │         │ StarRatingCalculator    │
│ which returns:        │         │                         │
│ - starsEarned (0-3)   │         │ Input:                  │
│ - comboState          │         │ - totalWords            │
│                       │         │ - correctAnswers        │
│ Updates:              │         │ - hintsUsed             │
│ - correctAnswersLevel │         │ - totalTimeMs           │
│ - totalWrongAnswers   │         │ - wrongAnswers          │
│ - currentComboState   │         │ - maxCombo              │
│ - maxComboInLevel     │         │                         │
│ - starsEarnedInLevel  │         │ Output: 0-3 stars       │
└───────────────────────┘         └─────────────────────────┘
            │                                 │
            └────────────────┬────────────────┘
                             │
                    ┌────────▼────────┐
                    │  onNextWord()   │
                    │  When level     │
                    │  complete:      │
                    │  Show stars     │
                    └─────────────────┘
```

### When Each Rating is Used

| Rating | When Calculated | Display Location |
|--------|-----------------|------------------|
| Per-Word | Immediately after submitAnswer() | Feedback screen between words |
| Level-Level | When all words completed (onNextWord) | LevelComplete screen |

---

## Part 5: Guessing Detection

**File**: `app/src/main/java/com/wordland/domain/algorithm/GuessingDetector.kt`

### Per-Word Guessing (SubmitAnswerUseCase)

Uses pattern-based detection with recent history:

```kotlin
val recentPatterns = trackingRepository.getRecentPatterns(userId, limit = 5)
val isGuessing = GuessingDetector.detectGuessing(responsePatterns)
```

### GuessingDetector Rules

**Returns `true` if ANY of:**

1. **Fast Response Rule** (lines 29-37)
   - 70%+ of responses are < 2 seconds
   - Requires at least 3 patterns

2. **Random Pattern Rule** (lines 39-45)
   - Accuracy between 30-70% (chance level)
   - No hints used

3. **Rapid Incorrect Rule** (lines 47-55)
   - 50%+ of incorrect answers are < 1.5 seconds

### Level-Level Guessing (StarRatingCalculator)

Uses average time per word:

```kotlin
avgTimePerWord < 1500ms → -0.6 penalty (guessing)
```

---

## Part 6: Combo System

**File**: `app/src/main/java/com/wordland/domain/combo/ComboManager.kt`

### Combo Thresholds

| Combo | Multiplier | Bonus in Stars |
|-------|------------|----------------|
| 0-2 | 1.0x | 0.0 |
| 3-4 | 1.2x | 0.0 |
| 5-9 | 1.5x | +0.5 |
| 10+ | 1.5x | +1.0 |

### Anti-Guessing in Combo

Fast answers do NOT increment combo:

```kotlin
fun isGuessing(responseTime: Long, wordLength: Int): Boolean {
    val minimumThinkTime = 1000 + (wordLength * 500)
    return responseTime < minimumThinkTime
}
```

### Combo Messages

| Combo | Message |
|-------|---------|
| 1-2 | (none) |
| 3 | "Nice streak!" |
| 4 | "Keep it up!" |
| 5 | "On fire! 🔥" |
| 6 | "Unstoppable!" |
| 7 | "Incredible!" |
| 8 | "Amazing!" |
| 9 | "Legendary!" |
| 10 | "Godlike! 👑" |

---

## Part 7: Known Issues Catalog

### Issue #1: Double Hint Penalty
**Severity**: P2
**Description**: Hints penalize at both per-word AND level-level
- Per-word: Reduces max from 3 to 2 stars
- Level-level: -0.25 per hint (capped at -0.5)
**Impact**: Over-penalizes hint usage
**Recommendation**: Choose one penalty level

### Issue #2: Inconsistent Guessing Detection
**Severity**: P1
**Description**: Two different guessing detection mechanisms
- Per-word: Pattern-based (last 5 answers)
- Level-level: Simple time threshold (avg < 1.5s)
**Impact**: May give different results for same behavior
**Recommendation**: Unify detection logic

### Issue #3: Missing User Feedback
**Severity**: P2
**Description**: Users don't know why they got specific star count
**Impact**: Confusing UX, no learning opportunity
**Recommendation**: Add breakdown display on LevelComplete

### Issue #4: Test Coverage Gaps
**Severity**: P1
**Description**: Several test methods are `@Ignore`
- `SubmitAnswerUseCaseTest` has 4 ignored tests
**Impact**: Uncertain if edge cases work correctly
**Recommendation**: Fix and enable all tests

### Issue #5: Word Length Not Used in Level Calculation
**Severity**: P3
**Description**: Level calculation doesn't consider word difficulty
**Impact**: 6-letter words treated same as 3-letter words
**Recommendation**: Consider adding difficulty modifier

### Issue #6: Combo Bonus Can Override Guessing Penalty
**Severity**: P2
**Description**: High combo (10+) gives +1.0, can overcome -0.6 guessing penalty
**Impact**: Fast+consistent guessing can still get 3 stars
**Recommendation**: Review bonus/penalty balance

---

## Part 8: Edge Cases

### Edge Case #1: Single Word Level
**Input**: `totalWords=1, correctAnswers=1, hintsUsed=0, errors=0`
**Output**: 3 stars
**Code Location**: `StarRatingCalculatorTest.kt:233-249`

### Edge Case #2: Zero Total Words
**Input**: `totalWords=0`
**Output**: 0 stars (safe fallback)
**Code Location**: `StarRatingCalculator.kt:122-123`

### Edge Case #3: All Wrong
**Input**: `correctAnswers=0`
**Output**: 0 stars (no child-friendly minimum)
**Code Location**: `StarRatingCalculator.kt:122-123`

### Edge Case #4: One Correct, Many Errors
**Input**: `correctAnswers=1, wrongAnswers=5`
**Output**: 1 star (child-friendly rounding)
**Code Location**: `StarRatingCalculatorTest.kt:358-374`

### Edge Case #5: Hint Penalty Cap
**Input**: `hintsUsed=10`
**Output**: Same penalty as `hintsUsed=2` (capped at -0.5)
**Code Location**: `StarRatingCalculator.kt:203-208`

### Edge Case #6: Error Penalty Cap
**Input**: `wrongAnswers=10`
**Output**: Same penalty as `wrongAnswers=3` (capped at -0.3)
**Code Location**: `StarRatingCalculator.kt:264-269`

### Edge Case #7: Perfect with No Errors (5/6)
**Input**: `correctAnswers=5, wrongAnswers=0`
**Output**: 3 stars (exactly at threshold)
**Code Location**: `StarRatingCalculatorTest.kt:320-336`

---

## Part 9: Constants Reference

### DomainConstants
**File**: `app/src/main/java/com/wordland/domain/constants/DomainConstants.kt`

| Constant | Value | Usage |
|----------|-------|-------|
| `MIN_RESPONSE_TIME_MS` | 1000 | Base time for per-word stars |
| `MILLIS_PER_LETTER_THRESHOLD` | 500 | Per-letter time for per-word stars |

### StarRatingCalculator Constants
**File**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`

| Constant | Value | Usage |
|----------|-------|-------|
| `MAX_STARS` | 3 | Maximum stars per level |
| `MIN_STARS` | 1 | Minimum stars (if passed) |
| `STAR_THRESHOLD_3` | 2.5 | Score needed for 3 stars |
| `STAR_THRESHOLD_2` | 1.5 | Score needed for 2 stars |
| `STAR_THRESHOLD_1` | 0.5 | Score needed for 1 star |
| `HINT_PENALTY_PER_HINT` | 0.25 | Penalty per hint |
| `MAX_HINT_PENALTY` | 0.5 | Max hint penalty |
| `TIME_BONUS_FAST` | 0.3 | Bonus for fast completion |
| `FAST_TIME_PER_WORD_MS` | 5000 | Fast threshold (5s) |
| `ERROR_PENALTY_PER_ERROR` | 0.1 | Penalty per error |
| `MAX_ERROR_PENALTY` | 0.3 | Max error penalty |
| `COMBO_BONUS_HIGH` | 1.0 | Bonus for 10+ combo |
| `COMBO_BONUS_LOW` | 0.5 | Bonus for 5+ combo |
| `COMBO_BONUS_THRESHOLD_HIGH` | 10 | Threshold for +1.0 bonus |
| `COMBO_BONUS_THRESHOLD_LOW` | 5 | Threshold for +0.5 bonus |
| `MIN_TIME_PER_WORD_MS` | 1500 | Guessing threshold |
| `MAX_TIME_PER_WORD_MS` | 15000 | Slow threshold |
| `GUESSING_PENALTY` | 0.6 | Penalty for guessing |
| `SLOW_PENALTY` | 0.2 | Penalty for being slow |

### ComboState Constants
**File**: `app/src/main/java/com/wordland/domain/model/ComboState.kt`

| Constant | Value | Usage |
|----------|-------|-------|
| `COMBO_THRESHOLD_3X` | 3 | Threshold for 1.2x multiplier |
| `COMBO_THRESHOLD_5X` | 5 | Threshold for 1.5x multiplier |
| `MULTIPLIER_3X` | 1.2 | Multiplier for 3-4 combo |
| `MULTIPLIER_5X` | 1.5 | Multiplier for 5+ combo |
| `MULTIPLIER_BASE` | 1.0 | Base multiplier |
| `MIN_THINK_TIME_BASE_MS` | 1000 | Base anti-guessing time |
| `MILLIS_PER_LETTER` | 500 | Per-letter anti-guessing |

---

## Part 10: Test Coverage Summary

### StarRatingCalculatorTest
**File**: `app/src/test/java/com/wordland/domain/algorithm/StarRatingCalculatorTest.kt`
**Total Tests**: 27 tests

| Test Category | Count |
|---------------|-------|
| Basic scenarios (1-5) | 10 |
| Edge cases | 4 |
| Combo bonus | 3 |
| Anti-guessing | 3 |
| Combined scenarios | 2 |
| Utility methods | 5 |

### SubmitAnswerUseCaseTest
**File**: `app/src/test/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCaseTest.kt`
**Total Tests**: 8 tests (4 ignored)

| Status | Count |
|--------|-------|
| Active | 4 |
| Ignored | 4 |

**Ignored Tests**:
1. `correct answer increases memory strength and awards stars`
2. `incorrect answer decreases memory strength`
3. `answer with hint reduces stars earned`
4. `fast answer detected as guessing`
5. `case insensitive answer matching`

---

## Summary

### Key Findings

1. **Algorithm Exists**: The star rating system is fully implemented with 337 lines of code
2. **Two-Tier System**: Per-word (0-3) and level-level (0-3) ratings work independently
3. **Child-Friendly**: Minimum 1 star if any correct answer (except all wrong = 0)
4. **Anti-Guessing**: Multiple mechanisms detect and penalize guessing behavior
5. **Combo Rewards**: High combos (5+) can boost star rating by +1.0

### Critical Constants

| Constant | Value | Impact |
|----------|-------|--------|
| 3★ Threshold | 2.5 | Need 5/6 correct with good performance |
| 2★ Threshold | 1.5 | Need 3/6 correct |
| Per-word thinking time | 1000 + 500×letters | Affects individual word stars |
| Level guessing threshold | <1.5s/word avg | -0.6 penalty |

### Recommendation for Epic #5

Since the algorithm already exists, Epic #5 should focus on:
1. Fixing known issues (double penalty, inconsistent guessing)
2. Improving test coverage (enable ignored tests)
3. Adding user-facing feedback (score breakdown)
4. Balancing combo bonus vs guessing penalty

---

**End of Audit**
