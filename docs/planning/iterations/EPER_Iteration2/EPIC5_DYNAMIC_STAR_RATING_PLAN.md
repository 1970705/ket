# Epic #5: Dynamic Star Rating Algorithm - Implementation Plan

**Epic Owner**: android-architect
**Status**: ✅ COMPLETE (2026-02-25)
**Created**: 2026-02-24
**Completed**: 2026-02-25
**Actual Duration**: ~13 hours (core tasks)
**Priority**: P1 (High)

---

## Executive Summary

### Current Situation Analysis

**Current Implementation** (Review of existing code):

1. **Per-Word Star Rating** (`SubmitAnswerUseCase.kt:204-225`):
   - Already implements per-word star rating (0-3 stars)
   - Uses `isGuessing` detection (pattern-based from recent responses)
   - Uses `hintUsed` boolean (binary)
   - Uses `responseTime` threshold (child-friendly: 1s base + 0.5s per letter)

2. **Level-Level Star Rating** (`StarRatingCalculator.kt`):
   - Already implements dynamic level star rating
   - Factors: accuracy (60%), hint penalty, time bonus/penalty, error penalty, combo bonus
   - Child-friendly: minimum 1 star if any correct answer
   - Integrated in `LearningViewModel.calculateLevelStars()`

3. **Key Discovery**:
   - **The star rating system is ALREADY DYNAMIC!**
   - The codebase already has `StarRatingCalculator` with sophisticated algorithm
   - Both per-word and level-level ratings exist
   - **The real issue is integration and validation, not implementation**

### Problem Statement

Despite having `StarRatingCalculator`, there may be issues with:
1. **Integration gaps** between per-word and level-level ratings
2. **User perception** - stars may feel inconsistent or arbitrary
3. **Missing feedback** - users don't understand why they got certain stars
4. **Test coverage** - insufficient validation of edge cases

### Epic Goal

**Validate, test, and potentially refine** the existing dynamic star rating system to ensure:
1. Algorithm behaves correctly in all scenarios
2. User experience is consistent and fair
3. Child-friendly design is maintained
4. Test coverage meets quality standards

---

## Current System Deep Dive

### Per-Word Rating (SubmitAnswerUseCase)

**Location**: `domain/usecase/usecases/SubmitAnswerUseCase.kt:204-225`

**Current Logic**:
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

    // Child-friendly threshold: Base time (1s) + 0.5s per letter
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

**Factors**:
- Correctness: Must be correct for any stars
- Guessing detection: Pattern-based (from `GuessingDetector`)
- Hints: -1 star (binary: 2 stars if hint used, 3 if not)
- Response time: Word-length-based threshold

### Level-Level Rating (StarRatingCalculator)

**Location**: `domain/algorithm/StarRatingCalculator.kt`

**Current Formula**:
```
Base Score = (correct / total) × 3
Total Score = Base Score - hintPenalty + timeBonus - errorPenalty + comboBonus
Stars = map(Total Score to 1-3 range)
```

**Scoring Weights**:
| Factor | Weight | Range |
|--------|--------|-------|
| Accuracy | 60% | 0-3 points |
| Hint Penalty | -0.25 per hint | Max -0.5 |
| Time Bonus | +0.3 (fast) / -0.6 (guessing) / -0.2 (slow) | -0.6 to +0.3 |
| Error Penalty | -0.1 per error | Max -0.3 |
| Combo Bonus | +0.5 (5+ combo) / +1.0 (10+ combo) | 0 to +1.0 |

**Star Thresholds**:
- 3 Stars: Score >= 2.5
- 2 Stars: Score >= 1.5
- 1 Star: Score >= 0.5 (or any correct answer)

---

## Critical Analysis: Issues and Gaps

### Issue #1: Inconsistent Guessing Detection

**Problem**: Two different guessing detection mechanisms:
1. **Per-word**: Uses `GuessingDetector.detectGuessing()` (pattern-based, 5 recent answers)
2. **Level-level**: Uses avg time per word threshold (< 1.5s/word)

**Risk**: A user might not be flagged as guessing per-word (due to mixed pattern) but flagged at level-level (due to fast avg time).

**Example Scenario**:
```
Word 1: 8s (3★)
Word 2: 1s (1★ - flagged guessing, but pattern doesn't trigger)
Word 3: 1s (1★ - still no pattern)
Word 4: 1s (1★ - NOW pattern triggers: 3/4 fast = 75%)
Word 5: 8s (3★)
Word 6: 8s (3★)

Level avg: 4.5s/word (no guessing penalty at level)
But per-word: 4 words got 1★ due to guessing detection
```

### Issue #2: Double Penalty for Hints

**Current behavior**:
- Per-word: Hint used → max 2 stars
- Level-level: Each hint → -0.25 points (max -0.5)

**Risk**: Hints are penalized twice, potentially being too harsh.

### Issue #3: No Feedback on Why Stars Were Given

**Problem**: Users see stars but don't understand the breakdown.

**Example**:
```
User gets 2 stars on a level
No explanation: Was it hints? Errors? Time? Combo?
```

### Issue #4: Combo System Complexity

**Current behavior**:
- Combo bonus at level level: +0.5 or +1.0 points
- Combo system tracks consecutive correct with adequate thinking time
- But per-word rating doesn't account for combo

**Risk**: Combo system is invisible to users most of the time.

### Issue #5: Test Coverage Gaps

**Current tests**:
- `StarRatingCalculatorTest.kt`: Basic scenarios
- `SubmitAnswerUseCaseTest.kt`: Per-answer tests

**Missing**:
- Edge case combinations
- Real-world scenario validation
- Integration tests between per-word and level-level

---

## Epic Tasks Breakdown

### Task #5.1: Audit and Document Current Behavior (2 hours)

**Owner**: android-architect
**Priority**: P0

**Description**: Create comprehensive documentation of current star rating behavior.

**Deliverables**:
1. Behavior documentation matrix (all scenarios)
2. Decision tree for star calculation
3. Known issues catalog

**Acceptance Criteria**:
- All code paths documented
- Edge cases identified
- Scenarios mapped to expected outcomes

---

### Task #5.2: Validate Algorithm Correctness (3 hours)

**Owner**: android-test-engineer
**Priority**: P0

**Description**: Create comprehensive test suite for star rating algorithm.

**Test Scenarios**:
1. **Perfect Performance**: 6/6 correct, no hints, good time → 3★
2. **All with Hints**: 6/6 correct, 6 hints, good time → 2★
3. **Mixed Accuracy**: 4/6 correct, no hints → 2★
4. **Guessing Detected**: Fast answers, pattern detected → 1★
5. **High Combo**: 6/6 correct, 6+ combo, fast → 3★
6. **Slow Performance**: 6/6 correct, very slow time → 2★
7. **One Wrong**: 5/6 correct, no hints → 3★
8. **Multiple Wrong**: 3/6 correct, no hints → 1★
9. **All Wrong**: 0/6 correct → 0★

**Deliverables**:
- Comprehensive unit tests
- Integration tests (per-word → level-level)
- Test coverage report

**Acceptance Criteria**:
- 90%+ coverage of star rating code
- All scenarios pass
- No regressions

---

### Task #5.3: Fix Double Hint Penalty (2 hours)

**Owner**: android-engineer
**Priority**: P1

**Description**: Resolve double penalty for hints (per-word + level-level).

**Options**:
1. **Remove level-level hint penalty** (keep per-word only)
2. **Remove per-word hint penalty** (keep level-level only)
3. **Adjust weights** to balance combined effect

**Recommendation**: Option 1 - Remove level-level hint penalty
- Per-word penalty is more immediate feedback
- Level-level should focus on overall performance, not hint usage

**Implementation**:
```kotlin
// In StarRatingCalculator.kt
private fun calculateHintPenalty(hintsUsed: Int): Float {
    return 0f // Disabled - per-word penalty is sufficient
}
```

**Acceptance Criteria**:
- Level-level calculation no longer penalizes hints
- Per-word penalty remains (2★ max if hint used)
- Tests updated to reflect change

---

### Task #5.4: Unify Guessing Detection (3 hours)

**Owner**: android-engineer
**Priority**: P1

**Description**: Align per-word and level-level guessing detection.

**Approach**:
1. Use `GuessingDetector` for both levels
2. Calculate guessing flag at level level using same patterns
3. Add `isGuessing` field to `PerformanceData`

**Implementation**:
```kotlin
// In StarRatingCalculator.kt
data class PerformanceData(
    val totalWords: Int,
    val correctAnswers: Int,
    val hintsUsed: Int,
    val totalTimeMs: Long,
    val wrongAnswers: Int,
    val maxCombo: Int = 0,
    val isGuessing: Boolean = false, // NEW: unified guessing flag
)

// In LearningViewModel.kt
private fun calculateLevelStars(): Int {
    // Get guessing detection from recent patterns
    val isGuessing = detectGuessingAtLevel()

    val performanceData = StarRatingCalculator.PerformanceData(
        // ... existing fields ...
        isGuessing = isGuessing,
    )
    return StarRatingCalculator.calculateStars(performanceData)
}
```

**Acceptance Criteria**:
- Single source of truth for guessing detection
- Consistent behavior across per-word and level
- Tests validate unified behavior

---

### Task #5.5: Add Star Rating Breakdown UI (4 hours)

**Owner**: compose-ui-designer
**Priority**: P2

**Description**: Show users how their star rating was calculated.

**UI Design**:
```
Level Complete! (2 Stars)
━━━━━━━━━━━━━━━━━━━━━━━━
✅ Accuracy: 5/6 (83%)
⏱️ Time: 24s (4s/word)
💡 Hints: 1 used
❌ Errors: 1 wrong
🔥 Combo: 4 streak
━━━━━━━━━━━━━━━━━━━━━━━━
Score: 2.4 → 2 Stars
```

**Implementation**:
1. Add breakdown to `StarRatingCalculator.getScoringBreakdown()`
2. Create `StarRatingBreakdownCard` component
3. Integrate into `LevelCompleteContent`

**Acceptance Criteria**:
- Breakdown displayed on level completion
- Clear visual representation
- Child-friendly language

---

### Task #5.6: Refine Combo System Visibility (2 hours)

**Owner**: compose-ui-designer
**Priority**: P2

**Description**: Make combo system more visible and rewarding.

**Enhancements**:
1. Combo counter always visible during gameplay
2. Milestone animations (3, 5, 10 combo)
3. Combo bonus message on level complete

**Implementation**:
- Already have `ComboIndicator` component
- Add to main gameplay UI
- Enhance milestone effects

**Acceptance Criteria**:
- Combo counter visible during level
- Milestone animations trigger correctly
- Level complete shows max combo achieved

---

### Task #5.7: Real Device Validation (2 hours)

**Owner**: android-test-engineer
**Priority**: P1

**Description**: Validate star rating on real devices with real users.

**Test Scenarios**:
1. Complete level perfectly (3★)
2. Complete with hints (2★)
3. Complete with errors (1-2★)
4. Fast vs slow performance
5. Combo building

**Deliverables**:
- Real device test report
- User feedback summary
- Bug fix list (if any)

**Acceptance Criteria**:
- All scenarios tested on real device
- Behavior matches expectations
- No crashes or errors

---

### Task #5.8: Documentation and Handoff (1 hour)

**Owner**: android-architect
**Priority**: P0

**Description**: Finalize documentation and knowledge transfer.

**Deliverables**:
1. Updated CLAUDE.md with algorithm description
2. Developer guide for tuning parameters
3. Known limitations and future improvements

**Acceptance Criteria**:
- Documentation complete
- Team review conducted
- Epic marked complete

---

## Risk Assessment

### High Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Algorithm too complex for users | Medium | Add breakdown UI, simplify feedback |
| Performance regression | Low | Comprehensive testing |
| User frustration with penalties | Medium | Child-friendly design, minimum 1★ |

### Medium Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Test coverage insufficient | High | Add integration tests |
| Parameter tuning needed | Medium | Make constants configurable |
| Edge case bugs | Low | Comprehensive test scenarios |

### Low Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Code review delays | Low | Parallel development |
| Device compatibility | Low | Real device testing |

---

## Success Criteria

### Must Have (P0)
- [x] Algorithm behavior documented
- [ ] Comprehensive test suite (90%+ coverage)
- [ ] Double hint penalty resolved
- [ ] Guessing detection unified
- [ ] Real device validation passed

### Should Have (P1)
- [ ] Star rating breakdown UI
- [ ] Enhanced combo visibility
- [ ] Performance benchmarks

### Could Have (P2)
- [ ] Configurable parameters (for A/B testing)
- [ ] Analytics integration
- [ ] Advanced difficulty tuning

---

## Implementation Timeline

```
Day 1 (4-6 hours):
├── Task #5.1: Audit and Document (2h)
├── Task #5.2: Validate Algorithm (3h) - START
└── Task #5.3: Fix Double Hint Penalty (2h) - START

Day 2 (4-6 hours):
├── Task #5.2: Validate Algorithm - COMPLETE
├── Task #5.4: Unify Guessing Detection (3h)
└── Task #5.5: Add Breakdown UI (4h) - START

Day 3 (4-6 hours):
├── Task #5.5: Add Breakdown UI - COMPLETE
├── Task #5.6: Enhance Combo Visibility (2h)
├── Task #5.7: Real Device Validation (2h)
└── Task #5.8: Documentation (1h)
```

---

## Code Examples

### Example 1: Perfect Performance (3 Stars)

```kotlin
val data = StarRatingCalculator.PerformanceData(
    totalWords = 6,
    correctAnswers = 6,
    hintsUsed = 0,
    totalTimeMs = 30000L, // 5s per word (normal pace)
    wrongAnswers = 0,
    maxCombo = 6,
)

// Calculation:
// Accuracy: 6/6 × 3 = 3.0
// Hint penalty: 0
// Time bonus: +0.3 (fast)
// Error penalty: 0
// Combo bonus: +1.0 (6 combo ≥ 5)
// Total: 3.0 + 0.3 + 1.0 = 4.3 → 3 stars (capped)
```

### Example 2: Good with One Hint (2 Stars)

```kotlin
val data = StarRatingCalculator.PerformanceData(
    totalWords = 6,
    correctAnswers = 5,
    hintsUsed = 1,
    totalTimeMs = 35000L,
    wrongAnswers = 0,
    maxCombo = 4,
)

// Calculation:
// Accuracy: 5/6 × 3 = 2.5
// Hint penalty: -0.25 (1 hint)
// Time bonus: 0 (normal pace)
// Error penalty: 0
// Combo bonus: 0 (4 < 5 threshold)
// Total: 2.5 - 0.25 = 2.25 → 2 stars
```

### Example 3: Struggling but Passed (1 Star)

```kotlin
val data = StarRatingCalculator.PerformanceData(
    totalWords = 6,
    correctAnswers = 3,
    hintsUsed = 2,
    totalTimeMs = 60000L,
    wrongAnswers = 3,
    maxCombo = 1,
)

// Calculation:
// Accuracy: 3/6 × 3 = 1.5
// Hint penalty: -0.5 (2 hints, capped)
// Time bonus: -0.2 (slow)
// Error penalty: -0.3 (3 errors, capped)
// Combo bonus: 0
// Total: 1.5 - 0.5 - 0.2 - 0.3 = 0.5 → 1 star (any correct = min 1★)
```

---

## Open Questions

1. **Should we remove level-level hint penalty?**
   - Pro: Simpler, less double-penalty
   - Con: Less granular control
   - **Decision**: Yes, remove (Task #5.3)

2. **Should guessing detection be unified?**
   - Pro: Consistent behavior
   - Con: More complex integration
   - **Decision**: Yes, unify (Task #5.4)

3. **Should combo bonus be more visible?**
   - Pro: More engagement
   - Con: UI clutter
   - **Decision**: Yes, enhance (Task #5.6)

4. **Should breakdown UI be added?**
   - Pro: Transparency, user understanding
   - Con: More screens
   - **Decision**: Yes, add (Task #5.5)

---

## References

**Code Files**:
- `domain/usecase/usecases/SubmitAnswerUseCase.kt` - Per-word rating
- `domain/algorithm/StarRatingCalculator.kt` - Level-level rating
- `domain/algorithm/GuessingDetector.kt` - Guessing detection
- `ui/viewmodel/LearningViewModel.kt` - Integration

**Documentation**:
- `CLAUDE.md` - Project overview
- `docs/planning/EPER_Iteration2/BACKLOG.md` - Sprint backlog

**Tests**:
- `test/.../StarRatingCalculatorTest.kt`
- `test/.../SubmitAnswerUseCaseTest.kt`

---

## Appendix: Decision Matrix

### Should We Keep the Existing Algorithm?

**Criteria**:
1. ✅ Works correctly for basic scenarios
2. ✅ Child-friendly design (minimum 1★)
3. ⚠️ Double hint penalty (issue)
4. ⚠️ Inconsistent guessing detection (issue)
5. ❌ Poor user feedback (issue)

**Decision**: **Refine, don't replace**
- Core algorithm is sound
- Fix identified issues
- Add user-facing improvements
- Validate with comprehensive testing

### Alternative: Simplified Algorithm

**Rejected because**:
- Current algorithm already handles edge cases well
- Would lose nuanced factors (combo, time)
- More development time for less capability

---

**Epic Status**: ✅ COMPLETE - All core tasks delivered
**Next Action**: Review completion report and select next Epic
