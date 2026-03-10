# Hint Counter Display Bug

**Bug ID**: P2-BUG-004
**Reported Date**: 2026-02-24
**Reporter**: android-test-engineer (Real Device Testing)
**Severity**: P2 - Cosmetic Issue (Non-functional)
**Status**: Open (Assigned to compose-ui-designer)

## Bug Description

The hint counter displays "提示 (4/3)" when attempting to use the 4th hint, exceeding the maximum limit visually.

**Affected Component**: `HintCard.kt` - Hint counter display logic
**Affected Devices**: Real device (Xiaomi 24031PN0DC)
**Emulator Status**: Likely affected (not verified)

## Visual Evidence

**Expected Behavior**:
```
After 3 hints used: "提示 (3/3)"
On 4th attempt: Button should be disabled with "提示 (3/3)"
```

**Actual Behavior**:
```
After 3 hints used: "提示 (3/3)"
On 4th attempt: Counter displays "提示 (4/3)" ❌
Hint content IS blocked (functional limit works)
Button IS disabled (functional limit works)
```

**User Impact**: Confusion - counter suggests 4 hints used when max is 3

## Root Cause Analysis

### Problem Diagnosis

The counter display logic likely increments the counter **before** displaying, rather than showing `min(currentUsage, maxHints)`.

### Hypothesis

In `HintCard.kt`, the counter text is probably calculated as:
```kotlin
Text("提示 ($hintsUsed/$maxHints)")
```

Where `hintsUsed` can be 4 when user clicks 4th time, but it should display:
```kotlin
Text("提示 (${minOf(hintsUsed, maxHints)}/$maxHints)")
// Or use state-based display
```

### Code Location

**File**: `app/src/main/java/com/wordland/ui/components/HintCard.kt`
**Approximate Lines**: 200-300 (counter display logic)

## Functional Behavior Analysis

### What Works Correctly ✅

1. **Hint limit enforcement**: 4th hint content IS blocked
2. **Button disabled state**: Button IS correctly disabled on 4th attempt
3. **Hint progression**: Levels 1→2→3 work correctly
4. **Remaining counter decrements**: 3→2→1 works correctly

### What Doesn't Work ❌

1. **Counter cap display**: Shows "4/3" instead of "3/3" on 4th attempt
2. **Visual inconsistency**: Counter exceeds maximum visually

## Severity Assessment

**Severity**: P2 (Cosmetic/Non-functional)

**Rationale**:
- ✅ Functionality works correctly (limit enforced)
- ✅ No crash or data corruption
- ✅ User cannot abuse hint system
- ❌ Visual confusion (counter shows impossible value)
- ❌ Minor user experience issue

**Priority**: P2 (Should fix, but not blocking)

**Classification**: Cosmetic issue only
- Does not affect core functionality
- Does not affect game balance
- Does not affect data integrity
- Affects user perception slightly

## Why This Was Not Caught Earlier

### 1. Focus on Functional Testing

**What Was Tested**:
- ✅ Hint limit enforcement (can user use 4th hint?)
- ✅ Hint content blocking (is hint provided?)
- ✅ Button disabled state (is button clickable?)
- ✅ Progression 1→2→3 (do hints advance correctly?)

**What Was NOT Tested**:
- ❌ Counter display accuracy when limit exceeded
- ❌ Visual consistency of counter in all states
- ❌ Edge case UI rendering

### 2. Testing Priority Order

**Test Execution Order** (from android-test-engineer report):
1. Scenario 1: First Launch & Initialization ✅
2. Scenario 2: Hint Progression (1→2→3) ✅
3. Scenario 3: Hint Limit Test ⚠️ (Functional test passed, visual display not verified)
4. Scenario 4: Star Rating Penalty ✅
5. Scenario 5: Complete Level Flow ✅

**Gap**: Scenario 3 focused on "is hint blocked?" not "what does counter show?"

### 3. UI State Testing Gap

**Missing Test Case**:
```
Test: Attempt 4th hint
Expected:
  - Button state: Disabled ✅ Verified
  - Hint content: Blocked ✅ Verified
  - Counter display: "3/3" ❌ NOT VERIFIED
```

## Solution

### Option 1: Cap Display Value (Recommended)

```kotlin
// Change from:
"提示 ($hintsUsed/$maxHints)"

// To:
"提示 (${minOf(hintsUsed, maxHints)}/$maxHints)"
```

**Pros**:
- Simple fix (1-line change)
- Ensures counter never exceeds max
- Maintains current structure

**Cons**:
- Need to find all display locations
- May need to update multiple UI states

### Option 2: State-Based Display

```kotlin
// When hintState == LimitReached:
Text("提示 ($maxHints/$maxHints)")  // Always show 3/3

// When hintState == Available:
Text("提示 ($hintsUsed/$maxHints)")
```

**Pros**:
- Explicit state handling
- Clearer intent
- Easier to test

**Cons**:
- Requires state logic changes
- More complex than Option 1

### Option 3: Domain Layer Validation

```kotlin
// In HintCard UI state:
data class HintCardState(
    val hintsUsed: Int,
    val hintsRemaining: Int,
    val displayCount: Int  // NEW: Always capped at max
) {
    init {
        displayCount = minOf(hintsUsed, MAX_HINTS)
    }
}
```

**Pros**:
- Fixes at state level
- All UI consumers get correct value
- More robust solution

**Cons**:
- Requires state model changes
- More extensive refactoring
- Overkill for simple display issue

## Recommendation

**Primary Solution**: Option 1 - Cap display value with `minOf()`

**Rationale**:
- Minimal code change
- Fixes visual issue immediately
- Low risk of introducing new bugs
- Can be done in 30 minutes

**Implementation Steps**:
1. Find all counter display locations in `HintCard.kt`
2. Apply `minOf(hintsUsed, maxHints)` wrapper
3. Test on real device (attempt 4th hint)
4. Verify counter shows "3/3" not "4/3"
5. Confirm functionality unchanged

## Testing Plan

### Before Fix
1. ✅ Reproduce issue on Xiaomi device (already done)
2. ⏳ Verify on emulator (if different behavior)

### After Fix
1. Verify counter caps at "3/3" on 4th attempt
2. Verify hint still blocked (functionality intact)
3. Verify button still disabled (functionality intact)
4. Test all 4 UI states:
   - Empty: "提示"
   - Level 1: "提示 (1/3)"
   - Level 2: "提示 (2/3)"
   - Level 3: "提示 (3/3)"
   - Limit Exceeded: "提示 (3/3)" ← Fixed

### Acceptance Criteria
- [ ] Counter never shows value greater than max
- [ ] Display logic consistent across all states
- [ ] No regression in functionality
- [ ] Verified on real device (Xiaomi 24031PN0DC)

## Prevention Measures

### Short Term
1. **Add edge case UI testing to checklist**
   - Test boundary values (0, max, max+1)
   - Verify display doesn't exceed limits
   - Check visual consistency

2. **Update hint system testing**
   - Add "counter display" verification to Scenario 3
   - Test visual state, not just functional behavior

### Long Term
1. **State validation in UI layer**
   - Add display value validation in ViewModel
   - Ensure UI state never shows impossible values

2. **Visual QA for edge cases**
   - Test boundary conditions in visual QA
   - Verify display makes sense at extremes

## Related Issues

- Epic #4: Enhanced Hint System Integration (completed with this bug)
- P1-BUG-002: Hint Text Truncation (fixed 2026-02-22)
- P0-BUG-003: Learning Screen Layout Overflow (fixed 2026-02-22)

## Impact Assessment

**Severity**: P2 (Cosmetic)
- User can still use hint system correctly
- Functional limits enforced properly
- Only visual confusion on counter display

**Priority**: Medium (should fix soon)
- Affects user perception slightly
- May confuse users momentarily
- Easy to fix (30 minutes)
- Low risk fix

**User Experience Impact**:
- Minor: User sees "4/3" and may wonder "why does it say 4 when max is 3?"
- Temporary: User quickly realizes hint doesn't work
- Functional: No actual gameplay impact

## References

- Test Report: `docs/reports/testing/HINT_SYSTEM_REAL_DEVICE_TEST_REPORT.md`
- Screenshots: `docs/reports/testing/screenshots/hint_test_*.png`
- Component: `app/src/main/java/com/wordland/ui/components/HintCard.kt`
- Device: Xiaomi 24031PN0DC (5369b23a)

---

**Reported By**: android-test-engineer-2
**Assigned To**: compose-ui-designer-2-2
**Task**: #4.7 - Fix Hint Counter Display
**Status**: Open - 2026-02-24
**Last Updated**: 2026-02-24

## Fix Summary

**Status**: ⏳ PENDING (Task #4.7 in progress)

**Solution To Apply**: Option 1 - Cap display value with `minOf()`

**Estimated Time**: 30 minutes
**Assigned To**: compose-ui-designer-2-2
**Currently**: Working on fix
