# Star Rating Tuning Guide

**Version**: 1.0
**Created**: 2026-02-25
**Epic**: #5 Dynamic Star Rating Algorithm
**Target Audience**: Developers, Game Designers

---

## Overview

This guide explains how to tune and adjust the star rating algorithm in Wordland. The dynamic star rating system calculates 0-3 stars based on multiple performance factors.

**Algorithm Location**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`

---

## Scoring Formula

```
Base Score = Accuracy Score - Hint Penalty + Time Bonus - Error Penalty + Combo Bonus
Stars = Convert Score to 1-3 using thresholds
```

### Score Components

| Component | Weight | Range | Description |
|-----------|--------|-------|-------------|
| Accuracy | 60% | 0-3.0 points | Primary factor: correct/total × 3 |
| Hint Penalty | DISABLED | 0.0 | Per-word penalty only (see below) |
| Time Bonus | Variable | -0.6 to +0.3 | Fast completion bonus, guessing penalty |
| Error Penalty | Linear | 0 to -0.3 | -0.1 per wrong answer (capped) |
| Combo Bonus | Tiered | 0 to +1.0 | +0.5 at 5 combo, +1.0 at 10 combo |

---

## Star Thresholds

| Total Score | Stars Awarded | Description |
|-------------|---------------|-------------|
| >= 2.5 | ★★★ | Excellent performance |
| >= 1.5 | ★★ | Good performance |
| > 0 OR any correct | ★ | Minimum passing |
| = 0 AND no correct | 0 | Failed |

---

## Tunable Constants

### Location: StarRatingCalculator.kt (lines 59-88)

```kotlin
// Star Thresholds
private const val STAR_THRESHOLD_3 = 2.5f  // 3★ requirement
private const val STAR_THRESHOLD_2 = 1.5f  // 2★ requirement
private const val STAR_THRESHOLD_1 = 0.5f  // 1★ requirement

// Hint Penalty (DISABLED at level-level)
private const val HINT_PENALTY_PER_HINT = 0.25f
private const val MAX_HINT_PENALTY = 0.5f

// Time Bonus
private const val TIME_BONUS_FAST = 0.3f           // Bonus for fast completion
private const val FAST_TIME_PER_WORD_MS = 5000L    // 5 seconds = fast

// Error Penalty
private const val ERROR_PENALTY_PER_ERROR = 0.1f   // -0.1 per error
private const val MAX_ERROR_PENALTY = 0.3f         // Max -0.3

// Combo Bonus
private const val COMBO_BONUS_THRESHOLD_HIGH = 10  // 10+ combo
private const val COMBO_BONUS_THRESHOLD_LOW = 5    // 5+ combo
private const val COMBO_BONUS_HIGH = 1.0f          // +1 star
private const val COMBO_BONUS_LOW = 0.5f           // +0.5 star

// Anti-Guessing
private const val MIN_TIME_PER_WORD_MS = 1500L     // 1.5s = guessing threshold
private const val GUESSING_PENALTY = 0.6f          // -0.6 for guessing
private const val MAX_TIME_PER_WORD_MS = 15000L    // 15s = slow
private const val SLOW_PENALTY = 0.2f              // -0.2 for slow
```

---

## Tuning Guide by Scenario

### Scenario 1: Make 3 Stars Harder to Earn

**Current**: 5/6 correct (83%) with good time = 3 stars

**To increase difficulty**:
```kotlin
// Option A: Raise threshold
private const val STAR_THRESHOLD_3 = 2.7f  // was 2.5f

// Option B: Increase combo requirement
private const val COMBO_BONUS_THRESHOLD_LOW = 7  // was 5

// Option C: Reduce fast bonus
private const val TIME_BONUS_FAST = 0.2f  // was 0.3f
```

### Scenario 2: Make 2 Stars More Forgiving

**Current**: 3/6 correct (50%) = 2 stars (with combo)

**To increase forgiveness**:
```kotlin
// Option A: Lower threshold
private const val STAR_THRESHOLD_2 = 1.3f  // was 1.5f

// Option B: Increase combo bonus
private const val COMBO_BONUS_LOW = 0.7f  // was 0.5f

// Option C: Reduce error penalty
private const val ERROR_PENALTY_PER_ERROR = 0.05f  // was 0.1f
```

### Scenario 3: Adjust Anti-Guessing

**Current**: < 1.5s/word avg = guessing penalty

**To tune**:
```kotlin
// Make guessing detection stricter
private const val MIN_TIME_PER_WORD_MS = 2000L  // was 1500L

// Increase guessing penalty
private const val GUESSING_PENALTY = 0.8f  // was 0.6f

// Make guessing detection more lenient
private const val MIN_TIME_PER_WORD_MS = 1000L  // was 1500L
```

### Scenario 4: Reward High Combos More

**Current**: 10+ combo = +1 star

**To increase**:
```kotlin
// Lower threshold for max bonus
private const val COMBO_BONUS_THRESHOLD_HIGH = 7  // was 10

// Increase bonus amount
private const val COMBO_BONUS_HIGH = 1.5f  // was 1.0f
```

---

## Per-Word vs Level-Level Penalties

### Per-Word Penalties (SubmitAnswerUseCase)

**Location**: `domain/usecase/usecases/SubmitAnswerUseCase.kt`

| Factor | Penalty |
|--------|---------|
| Incorrect answer | 0 stars for that word |
| Guessing detected | 1 star (instead of 3) |
| Hint used | 2 stars (instead of 3) |
| Too fast (< threshold) | 2 stars (instead of 3) |

**Threshold Formula**:
```kotlin
val adequateThinkingTime = 1000 + (wordLength × 500)  // ms
// Example: 5-letter word = 1000 + 2500 = 3500ms
```

### Level-Level Penalties (StarRatingCalculator)

| Factor | Penalty | Status |
|--------|---------|--------|
| Hints used | 0.0 | DISABLED (per-word sufficient) |
| Guessing | -0.6 | Active |
| Slow (> 15s/word) | -0.2 | Active |
| Errors | -0.1 each (capped -0.3) | Active |
| Fast (< 5s/word) | +0.3 | Active |
| Combo 5+ | +0.5 | Active |
| Combo 10+ | +1.0 | Active |

---

## Calculation Examples

### Example 1: Perfect Performance (3 Stars)

```kotlin
PerformanceData(
    totalWords = 6,
    correctAnswers = 6,
    hintsUsed = 0,
    totalTimeMs = 24000L,  // 4s per word
    wrongAnswers = 0,
    maxCombo = 6,
)

// Calculation:
// Accuracy: 6/6 × 3 = 3.0
// Hint penalty: 0.0 (disabled)
// Time bonus: +0.3 (fast: 4s < 5s)
// Error penalty: 0.0
// Combo bonus: +0.5 (6 >= 5)
// Total: 3.0 + 0.3 + 0.5 = 3.8 → 3 stars
```

### Example 2: Good with Hints (2 Stars)

```kotlin
PerformanceData(
    totalWords = 6,
    correctAnswers = 5,
    hintsUsed = 2,
    totalTimeMs = 30000L,  // 5s per word
    wrongAnswers = 1,
    maxCombo = 4,
)

// Calculation:
// Accuracy: 5/6 × 3 = 2.5
// Hint penalty: 0.0 (disabled at level)
// Time bonus: 0.0 (normal pace)
// Error penalty: -0.1 (1 error)
// Combo bonus: 0.0 (4 < 5)
// Total: 2.5 - 0.1 = 2.4 → 2 stars
```

### Example 3: Guessing Detected (1 Star)

```kotlin
PerformanceData(
    totalWords = 6,
    correctAnswers = 6,
    hintsUsed = 0,
    totalTimeMs = 6000L,  // 1s per word (too fast!)
    wrongAnswers = 0,
    maxCombo = 0,  // No combo (anti-guessing)
)

// Calculation:
// Accuracy: 6/6 × 3 = 3.0
// Hint penalty: 0.0
// Time bonus: -0.6 (guessing: 1s < 1.5s)
// Error penalty: 0.0
// Combo bonus: 0.0
// Total: 3.0 - 0.6 = 2.4 → 2 stars
// Note: Per-word guessing would cap individual words at 1 star
```

### Example 4: Struggling but Passed (1 Star)

```kotlin
PerformanceData(
    totalWords = 6,
    correctAnswers = 3,
    hintsUsed = 3,
    totalTimeMs = 60000L,  // 10s per word
    wrongAnswers = 3,
    maxCombo = 1,
)

// Calculation:
// Accuracy: 3/6 × 3 = 1.5
// Hint penalty: 0.0
// Time bonus: -0.2 (slow)
// Error penalty: -0.3 (capped)
// Combo bonus: 0.0
// Total: 1.5 - 0.2 - 0.3 = 1.0 → 1 star (child-friendly)
```

---

## Testing Your Changes

### 1. Unit Test

```bash
./gradlew test --tests StarRatingCalculatorTest
```

### 2. Integration Test

```bash
./gradlew test --tests StarRatingIntegrationTest
```

### 3. Real Device Test

```bash
# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Monitor logs
adb logcat | grep StarRatingCalculator
```

### 4. Test Scenarios

| Scenario | Expected | Verification |
|----------|----------|--------------|
| Perfect (6/6, no hints) | 3 stars | Complete level perfectly |
| All hints (6/6, 6 hints) | 2 stars | Use hint on every word |
| Half correct (3/6) | 1 star | Get 3 wrong |
| All wrong (0/6) | 0 stars | Skip all words |
| Guessing (fast answers) | 1-2 stars | Answer < 1.5s each |

---

## Validation Checklist

Before deploying changes:

- [ ] All unit tests pass (StarRatingCalculatorTest)
- [ ] Integration tests pass
- [ ] Tested on real device
- [ ] Verified child-friendly minimum (1 star for any correct)
- [ ] Verified anti-guessing still works
- [ ] No regressions in existing levels
- [ ] Documentation updated (this file)
- [ ] Code review completed

---

## Known Limitations

1. **Word Difficulty Not Considered**: 6-letter words treated same as 3-letter
2. **Per-Word Detection is Independent**: Level calculation doesn't see per-word guessing flags
3. **No Adaptive Difficulty**: Same thresholds for all levels/ages

---

## Future Enhancements

### Consider for Epic #6+

1. **Difficulty-Based Thresholds**: Different star requirements by level
2. **Word Length Modifiers**: Longer words could be worth more
3. **Adaptive Tuning**: Auto-adjust based on aggregate player data
4. **User Feedback**: Show breakdown on LevelComplete screen (Task #5.5)

---

## References

- **Algorithm Source**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`
- **Test Suite**: `app/src/test/java/com/wordland/domain/algorithm/StarRatingCalculatorTest.kt`
- **Behavior Audit**: `docs/analysis/STAR_RATING_BEHAVIOR_AUDIT.md`
- **Implementation Plan**: `docs/planning/EPER_Iteration2/EPIC5_DYNAMIC_STAR_RATING_PLAN.md`

---

**End of Guide**
