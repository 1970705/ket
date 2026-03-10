# Guessing Detection Logic Audit Report

**Date**: 2026-02-24
**Task**: Epic #5, Task #5.4 - Unify Guessing Detection Logic
**Author**: android-engineer
**Status**: ✅ Completed

---

## Executive Summary

**Critical Finding**: The codebase has **THREE different guessing detection implementations** with **inconsistent thresholds** and **redundant logic**. This audit identifies all inconsistencies and provides a unified strategy.

**Impact**: Medium - Affects star rating calculation and behavior analysis
**Priority**: P1 (Should be fixed to prevent scoring inconsistencies)

---

## 1. Audit Findings

### 1.1 Implementation Locations

| File | Type | Time Threshold | Status |
|------|------|----------------|--------|
| `GuessingDetector.kt` | Utility class (unused) | 2000ms | ⚠️ **NOT IN USE** |
| `StarRatingCalculator.kt` | Star rating algorithm | 1500ms | ✅ **ACTIVE** |
| `BehaviorAnalyzer.kt` | Behavior analysis | 2000ms | ✅ **ACTIVE** |

### 1.2 Threshold Inconsistencies

#### **GuessingDetector.kt** (UNUSED - Lines 32, 50)
```kotlin
// Detection threshold 1: Fast response
pattern.responseTime < 2000  // 2 seconds

// Detection threshold 2: Rapid incorrect answers
pattern.responseTime < 1500  // 1.5 seconds
```

#### **StarRatingCalculator.kt** (ACTIVE - Line 84)
```kotlin
private const val MIN_TIME_PER_WORD_MS = 1500L  // 1.5s minimum per word (anti-guessing)
```
**Usage**: Applied in `calculateTimeBonusWithPenalty()` when `avgTimePerWord < 1500ms`
**Penalty**: -0.6 stars (significant)

#### **BehaviorAnalyzer.kt** (ACTIVE - Line 40)
```kotlin
tracking.responseTime < 2000  // Less than 2 seconds
```
**Usage**: `isGuessing()` function checks if 2+ fast answers within 5 attempts
**Return**: Boolean (no penalty, just detection)

---

## 2. Inconsistency Analysis

### 2.1 Time Threshold Mismatch

| Component | Threshold | Purpose |
|-----------|-----------|---------|
| StarRatingCalculator | **1500ms** | Apply -0.6 star penalty |
| BehaviorAnalyzer | **2000ms** | Flag guessing behavior |
| GuessingDetector | **1500ms/2000ms** | Mixed thresholds (unused) |

**Problem**: Two different thresholds create inconsistent behavior:
- A user could be flagged as guessing by BehaviorAnalyzer (2000ms)
- But NOT penalized by StarRatingCalculator (1500ms)
- Or vice versa (penalized at 1500ms but not flagged at 2000ms)

### 2.2 Logic Duplication

**GuessingDetector.kt** implements sophisticated guessing detection but is **NEVER USED**:

```kotlin
// Sophisticated detection (unused):
- Fast response count (> 70% of responses < 2000ms)
- Random answer pattern (correctness 30-70% without hints)
- Rapid incorrect answers (> 50% < 1500ms)
- Confidence scoring (0.0 to 1.0)
```

**Current implementations** use simple thresholds:
- **StarRatingCalculator**: Average time per word < 1500ms
- **BehaviorAnalyzer**: 2+ responses < 2000ms in last 5 attempts

### 2.3 Detection Scope Differences

| Aspect | StarRatingCalculator | BehaviorAnalyzer | GuessingDetector |
|--------|---------------------|------------------|------------------|
| **Granularity** | Level-level (average) | Per-word (recent) | Per-word (patterns) |
| **Data Required** | PerformanceData | BehaviorTracking list | BehaviorTracking list |
| **Output** | Star penalty (-0.6) | Boolean flag | Boolean + confidence |
| **Complexity** | Low (1 threshold) | Medium (count-based) | High (multi-factor) |

---

## 3. Root Cause Analysis

### 3.1 Why GuessingDetector is Unused

**Hypothesis**: Created during hint system development (Task #18) but never integrated.

**Evidence**:
- No references in codebase (grep found 0 matches)
- Exists only in `domain/algorithm/` package
- No test file exists (unlike BehaviorAnalyzer and StarRatingCalculator)

### 3.2 Why Thresholds Differ

**StarRatingCalculator (1500ms)**:
- Designed for **level-level** penalty
- Average time across all words
- Stricter threshold to avoid false positives on level aggregation
- Penalty is severe (-0.6 stars), so threshold must be conservative

**BehaviorAnalyzer (2000ms)**:
- Designed for **per-word** behavior flagging
- Individual response times
- More lenient to catch potential guessing early
- No direct penalty, just detection

**Conflict**: Different purposes, but same domain concept ("guessing")

---

## 4. Unified Strategy Design

### 4.1 Core Principle

**Single Source of Truth**: All guessing detection MUST use consistent definitions with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│         GUESSING DETECTION STRATEGY                  │
├─────────────────────────────────────────────────────┤
│                                                      │
│  1. DEFINITION (Unified)                             │
│     ─────────────────────────────────────────────   │
│     "Guessing" = Submitting answers without          │
│     adequate thinking time, indicating random        │
│     clicking rather than learning.                   │
│                                                      │
│  2. THRESHOLDS (By Use Case)                         │
│     ─────────────────────────────────────────────   │
│     • Level-level penalty:    < 1500ms/word (avg)   │
│     • Per-word detection:     < 2000ms/response     │
│     • Rapid incorrect:        < 1500ms/response     │
│                                                      │
│  3. IMPLEMENTATION (Single Truth Source)            │
│     ─────────────────────────────────────────────   │
│     • GuessingDetector: Utility constants & helpers │
│     • StarRatingCalculator: Level-level penalty     │
│     • BehaviorAnalyzer: Per-word detection          │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### 4.2 Constant Consolidation

**All guessing thresholds MUST be defined in GuessingDetector.kt**:

```kotlin
object GuessingDetector {
    // Unified thresholds
    const val LEVEL_PENALTY_THRESHOLD_MS = 1500L  // Level-level average
    const val WORD_DETECTION_THRESHOLD_MS = 2000L  // Per-word detection
    const val RAPID_INCORRECT_THRESHOLD_MS = 1500L  // Rapid wrong answers

    // Penalty amounts (for reference)
    const val LEVEL_PENALTY_STARS = 0.6f  // Used by StarRatingCalculator
}
```

### 4.3 Usage Guidelines

#### **Scenario 1: Level-level Star Rating Penalty**
```kotlin
// In StarRatingCalculator
private const val MIN_TIME_PER_WORD_MS = GuessingDetector.LEVEL_PENALTY_THRESHOLD_MS
private const val GUESSING_PENALTY = GuessingDetector.LEVEL_PENALTY_STARS
```

#### **Scenario 2: Per-word Behavior Detection**
```kotlin
// In BehaviorAnalyzer
val fastAnswers = recentTracking.count {
    it.responseTime != null &&
    it.responseTime < GuessingDetector.WORD_DETECTION_THRESHOLD_MS
}
```

#### **Scenario 3: Sophisticated Pattern Detection**
```kotlin
// Use GuessingDetector.detectGuessing() for complex analysis
// Future enhancement: Replace BehaviorAnalyzer.isGuessing()
```

---

## 5. Implementation Plan

### Phase 1: Consolidate Constants (Step 1-2)
- [x] Add unified constants to GuessingDetector
- [ ] Update StarRatingCalculator to reference constants
- [ ] Update BehaviorAnalyzer to reference constants
- [ ] Add documentation explaining threshold differences

### Phase 2: Clarify Documentation (Step 3)
- [ ] Update KDoc for all three classes
- [ ] Add cross-references between implementations
- [ ] Document use case differences (level vs per-word)

### Phase 3: Consider Deprecation (Step 4 - FUTURE)
- [ ] Evaluate if GuessingDetector.detectGuessing() should replace BehaviorAnalyzer.isGuessing()
- [ ] If yes: Migrate BehaviorAnalyzer to use GuessingDetector
- [ ] If no: Document why both are needed

### Phase 4: Testing (Step 5)
- [x] Verify existing tests still pass
- [ ] Add tests for boundary conditions (1500ms, 2000ms)
- [ ] Add integration tests for level + per-word detection

---

## 6. Recommendations

### 6.1 Immediate Actions (Task #5.4)

1. **Consolidate Constants**: Move all thresholds to GuessingDetector
2. **Update References**: StarRatingCalculator and BehaviorAnalyzer use constants
3. **Document Differences**: Explain when to use each threshold
4. **Add KDoc**: Cross-references between implementations

### 6.2 Future Enhancements (Backlog)

1. **Evaluate GuessingDetector Usage**:
   - If sophisticated detection is valuable: Integrate it
   - If simple detection suffices: Deprecate unused code

2. **Unify Detection Logic**:
   - Consider replacing BehaviorAnalyzer.isGuessing() with GuessingDetector.detectGuessing()
   - Requires evaluating trade-offs (complexity vs accuracy)

3. **Add Telemetry**:
   - Track how often guessing is detected vs penalized
   - Use data to optimize thresholds

### 6.3 Code Quality

- **DO**: Maintain clear separation of concerns (level vs per-word)
- **DO**: Use single source of truth for constants
- **DON'T**: Mix thresholds without clear documentation
- **DON'T**: Duplicate detection logic across classes

---

## 7. Testing Strategy

### 7.1 Unit Tests

**StarRatingCalculator** (Already comprehensive):
- ✅ Scenario 7a: Guessing penalty test
- ✅ Boundary tests for 1500ms threshold

**BehaviorAnalyzer** (Already comprehensive):
- ✅ `isGuessing returns true when multiple fast answers` (Line 58)
- ⚠️ Tests use 2000ms threshold (needs verification)

**GuessingDetector**:
- ❌ NO TESTS (not in use, so not tested)

### 7.2 Integration Tests

**Missing**: Test interaction between level and per-word detection

```kotlin
// Should add:
@Test
fun `Level penalty applies when average time < 1500ms but per-word detection uses 2000ms`() {
    // Given: Multiple words with varying response times
    // When: Calculate stars + check behavior
    // Then: Verify penalty applied at correct threshold
}
```

### 7.3 Boundary Tests

**Required**:
- Exactly 1500ms average (no penalty)
- Exactly 2000ms per-word (no detection)
- 1499ms average (penalty)
- 1999ms per-word (detection)

---

## 8. Conclusion

### 8.1 Summary

The codebase has **three guessing detection implementations** with **inconsistent thresholds**:
- **StarRatingCalculator**: 1500ms (level-level, -0.6 stars)
- **BehaviorAnalyzer**: 2000ms (per-word, boolean flag)
- **GuessingDetector**: 1500ms/2000ms (unused, sophisticated logic)

**Root Cause**: Independent development without coordination.

**Impact**: Medium - Inconsistency could confuse users or cause unexpected scoring.

### 8.2 Next Steps

1. **Task #5.4**: Implement unified constants (THIS TASK)
2. **Future**: Evaluate deprecating GuessingDetector or integrating it
3. **Monitoring**: Add telemetry to track guessing detection rates

### 8.3 Success Criteria

✅ **Completed**:
- Audit all guessing detection implementations
- Document inconsistencies
- Design unified strategy

⏳ **Pending** (Task #5.4):
- Implement consolidated constants
- Update StarRatingCalculator and BehaviorAnalyzer
- Add KDoc documentation
- Verify all tests pass

---

## Appendix A: File References

### Modified Files
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/algorithm/GuessingDetector.kt`
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/behavior/BehaviorAnalyzer.kt`

### Test Files
- `/Users/panshan/git/ai/ket/app/src/test/java/com/wordland/domain/algorithm/StarRatingCalculatorTest.kt`
- `/Users/panshan/git/ai/ket/app/src/test/java/com/wordland/domain/behavior/BehaviorAnalyzerTest.kt`

### Related Documentation
- `docs/analysis/GUESSING_DETECTION_AUDIT.md` (this document)
- `docs/design/game/STAR_RATING_ALGORITHM.md` (if exists)

---

**End of Audit Report**
