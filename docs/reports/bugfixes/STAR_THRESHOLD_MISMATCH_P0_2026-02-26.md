# P0 Bug: Star Rating Threshold Mismatch

**Bug ID**: P0-BUG-010
**Discovery Date**: 2026-02-26
**Discoverer**: android-test-engineer
**Severity**: P0 (Blocking)
**Status**: OPEN

---

## Summary

The `StarRatingCalculator` has incorrect threshold values that don't match the test expectations. This causes 25 out of 57 unit tests to fail (44% failure rate) and invalidates all real device testing results.

## Impact

- **Blocker**: Prevents Epic #8.2 completion
- **Game Balance**: Users need higher scores to earn the same stars
- **Test Data**: Previous 62.5% pass rate is based on wrong thresholds
- **User Experience**: Inconsistent star ratings

## Root Cause

**File**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`

**Lines 61-63**:
```kotlin
private const val STAR_THRESHOLD_3 = 3.0f  // Current: 3.0, Expected: 2.5
private const val STAR_THRESHOLD_2 = 2.0f  // Current: 2.0, Expected: 1.5
private const val STAR_THRESHOLD_1 = 1.0f  // Current: 1.0, Expected: 0.5
```

## Comparison

| Threshold | Current Value | Expected Value | Delta |
|-----------|---------------|----------------|-------|
| 3 Stars | 3.0 | 2.5 | +0.5 |
| 2 Stars | 2.0 | 1.5 | +0.5 |
| 1 Star | 1.0 | 0.5 | +0.5 |

## Failed Unit Tests

25 tests failing in `StarRatingCalculatorTest`:

1. "Exactly 1.5 score should earn 2 stars" - expected:2 but was:1
2. "Combo of 5 should earn bonus" - expected:2 but was:1
3. "Just above 15s per word should get slow penalty" - expected:2 but was:1
4. "Exactly 15s per word should not get slow penalty" - expected:2 but was:1
5. "2 hints should earn 2 stars" - expected:2 but was:1
6. "4/6 with hints and 5 combo should earn 2 stars" - expected:2 but was:1
7. "5/6 with guessing should earn 2 stars" - expected:2 but was:1
8. "1/2 correct should earn 2 stars" - expected:2 but was:1
9. "Scenario 5f - 1 correct with 1 error earns 1 star" - expected:3 but was:2
... (17 more failures)

## Fix Required

```kotlin
// File: StarRatingCalculator.kt
// Line 61-63

// BEFORE (INCORRECT):
private const val STAR_THRESHOLD_3 = 3.0f
private const val STAR_THRESHOLD_2 = 2.0f
private const val STAR_THRESHOLD_1 = 1.0f

// AFTER (CORRECT):
private const val STAR_THRESHOLD_3 = 2.5f
private const val STAR_THRESHOLD_2 = 1.5f
private const val STAR_THRESHOLD_1 = 0.5f
```

## Verification Plan

After fix:

1. Run unit tests: `./gradlew test`
2. Verify all 57 StarRatingCalculatorTest tests pass
3. Re-run Epic #8.2 scenarios 1-8 on real device
4. Update test report with new results

## Related Issues

- Epic #8.2: Real Device Validation (BLOCKED)
- Test Report: `docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md`

## Assignment

**Assign To**: android-engineer
**Estimated Fix Time**: 5 minutes (change 3 constants)

---

**Report Created**: 2026-02-26
**Last Updated**: 2026-02-26
