# Hint Text Truncation Bug

**Bug ID**: P1-BUG-002
**Reported Date**: 2026-02-22
**Reporter**: User (Real Device Testing)
**Severity**: P1 - UI Issue (Non-blocking)
**Status**: Fixed

## Bug Description

The "提示" (Hint) text is not fully displayed in the HintCard button on real device testing.

**Affected Component**: `HintCard.kt` - `HintActionButton` composable
**Affected Devices**: Real device (Xiaomi 24031PN0DC)
**Emulator Status**: Unknown (needs verification)

## Visual Evidence

**Expected**: The button should display:
```
  💡
  提示
  (3)
```

**Actual**: The text "提示" is truncated at the bottom, likely showing only the top portion of the characters.

## Root Cause Analysis

### Code Location
File: `app/src/main/java/com/wordland/ui/components/HintCard.kt:313-343`

### Problematic Code
```kotlin
// Line 313-343: Initial hint button
Button(
    onClick = onUseHint,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(...),
    modifier = Modifier.height(44.dp),  // ⚠️ HEIGHT TOO SMALL
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83D\uDEA7",  // 💡 emoji
            style = MaterialTheme.typography.titleSmall,  // ~14-16sp
        )
        Text(
            text = "提示",  // Chinese text
            style = MaterialTheme.typography.labelMedium,  // ~12-14sp
        )
        Text(
            text = "($hintsRemaining)",  // Remaining count
            style = MaterialTheme.typography.labelSmall,  // ~10-12sp
        )
    }
}
```

### Technical Analysis

**Height Calculation**:
- Button height: 44.dp
- Content spacing (Column):
  - Emoji (titleSmall): ~16sp (~16-18dp with padding)
  - "提示" text (labelMedium): ~12-14sp (~14-16dp with padding)
  - Count text (labelSmall): ~10-12sp (~12-14dp with padding)
  - Default spacing between items: 4-6dp
  - Button internal padding: ~8dp top/bottom

**Estimated minimum height required**: ~60-70.dp
**Current height**: 44.dp
**Deficit**: ~16-26.dp

**Why it truncates**:
- The Column's content exceeds the button's fixed height
- Compose clips content that overflows the container
- Chinese characters may have different rendering than English

## Why This Was Not Caught in Testing

### 1. Test Coverage Gaps

**Current Test Status** (from `docs/reports/testing/TEST_COVERAGE_BASELINE_20260220.md`):
- UI Layer Coverage: **~5%**
- UI Components: **~2%**
- Compose UI Tests: **None**

**Missing Tests**:
- ❌ No UI tests for HintCard component
- ❌ No screenshot tests
- ❌ No visual regression tests
- ❌ No rendering tests on different screen sizes/densities

### 2. Emulator vs Real Device Differences

**Potential Factors**:
| Factor | Emulator | Real Device | Impact |
|--------|----------|-------------|--------|
| Screen Density | Standardized | Device-specific | Text rendering may differ |
| Font Rendering | Generic | Manufacturer-specific | Chinese character height may vary |
| DPI Scaling | Predictable | Variable | Layout calculations may differ |
| Display Calibration | Default | Custom | Color/contrast may hide truncation |

**Xiaomi Device Specifics**:
- MIUI skin may use custom font rendering
- Display settings (font size, display size) affect layout
- Chinese locale may render characters differently

### 3. Testing Methodology Gaps

**Current Testing Focus**:
- ✅ Functional testing (does the hint work?)
- ✅ Business logic testing (hint counts, penalties)
- ❌ **Visual testing** (is the text fully visible?)
- ❌ **Layout testing** (do components fit their containers?)

**What Was Tested**:
- Hint system logic (HintGenerator, HintManager, UseHintUseCase)
- Unit tests for hint generation (24 tests)
- Unit tests for hint management (18 tests)
- Manual functional testing (hint button clicks, correct answer submission)

**What Was NOT Tested**:
- Visual appearance of hint button
- Text rendering in different screen densities
- Layout correctness on real devices
- Chinese character display quality

### 4. Development Process Gap

**Issue**:
- UI component implemented by compose-ui-designer
- Business logic tested by android-test-engineer
- **Gap**: No visual QA step in testing workflow

**Missing Step**:
```
Current: Implement → Unit Test → Functional Test → Deploy
Needed:  Implement → Unit Test → Functional Test → **Visual QA** → Deploy
```

## Solution

### Option 1: Increase Button Height (Recommended)

```kotlin
// Change from
modifier = Modifier.height(44.dp)

// To
modifier = Modifier.height(64.dp)  // Or 72.dp for safety
```

**Pros**:
- Simple fix (1 line change)
- Ensures all content fits
- Maintains current layout structure

**Cons**:
- Slightly larger button (may affect layout)

### Option 2: Change to Horizontal Layout

```kotlin
Button(
    onClick = onUseHint,
    modifier = Modifier.height(44.dp),
) {
    Row(  // Changed from Column
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "\uD83D\uDEA7")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "提示")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "($hintsRemaining)")
    }
}
```

**Pros**:
- Maintains 44.dp height
- All content fits horizontally

**Cons**:
- Wider button (may not fit narrow screens)
- Different visual design

### Option 3: Reduce Content

Show only emoji and count, hide "提示" text:

```kotlin
Column {
    Text(text = "\uD83D\uDEA7")
    Text(text = "($hintsRemaining)")
}
```

**Pros**:
- Fits in 44.dp
- Simpler UI

**Cons**:
- Less clear (what does the number mean?)
- Not accessible (no text label)

## Recommendation

**Primary Solution**: Option 1 - Increase button height to 64.dp or 72.dp

**Rationale**:
- Maintains current design intent
- Ensures content fits on all devices
- Minimal layout impact
- Quick fix for real device testing

## Testing Plan

### Before Fix
1. ✅ Reproduce issue on Xiaomi device
2. ⏳ Verify issue on emulator (if available)
3. ⏳ Test different screen sizes/densities

### After Fix
1. Verify "提示" text is fully visible
2. Verify button looks good on different devices
3. Verify no layout overlap with adjacent components
4. Add visual regression test for HintCard

## Prevention Measures

### Short Term
1. **Add real device testing to QA checklist**
   - Mandatory visual inspection on at least 1 real device
   - Test Chinese text rendering specifically

2. **Add screenshot tests**
   - Capture HintCard rendering
   - Compare against reference images
   - Run in CI pipeline

3. **Update testing checklist**
   - Add "Visual QA" step to `docs/testing/checklists/RELEASE_TESTING_CHECKLIST.md`
   - Include text rendering verification

### Long Term
1. **Improve UI test coverage**
   - Target: 80% UI component coverage (from current 2%)
   - Add Compose UI tests for all components

2. **Add visual regression testing**
   - Screenshot comparison tool (e.g., Paparazzi, Shot)
   - Automated visual diff detection

3. **Device testing matrix**
   - Test on multiple real devices
   - Different screen sizes (phone, tablet, foldable)
   - Different manufacturers (Samsung, Xiaomi, Pixel)

4. **Layout validation**
   - Add `Modifier.fillInAvailableHeight()` checks
   - Validate content doesn't overflow containers
   - Test with dynamic text scaling

## Related Issues

- Test Coverage: `docs/reports/testing/TEST_COVERAGE_BASELINE_20260220.md`
- Testing Strategy: `docs/testing/strategy/TEST_STRATEGY.md`
- Device Testing Guide: `docs/guides/testing/DEVICE_TESTING_GUIDE.md`

## Impact Assessment

**Severity**: P1 (Non-blocking)
- User can still use hint feature
- Text is partially visible (just truncated)
- Does not affect functionality

**Priority**: High (should fix before release)
- Affects user experience
- Visible to all users
- Easy to fix

## References

- Screenshot: [User provided screenshot of truncated text]
- Device: Xiaomi 24031PN0DC
- Android Version: [Need to check]
- App Version: [Current build]

---

**Reported By**: User (Real Device Testing)
**Assigned To**: compose-ui-designer
**Status**: Fixed - 2026-02-22
**Last Updated**: 2026-02-22

## Fix Summary

**Solution Applied**: Option 1 - Increase button height to 64.dp

**Changes Made**:
- File: `app/src/main/java/com/wordland/ui/components/HintCard.kt`
- Lines modified: 268, 290, 325
- Change: `Modifier.height(44.dp)` → `Modifier.height(64.dp)`
- All three hint button states updated consistently:
  1. Line 268: "No hints remaining" state
  2. Line 290: "Hint already shown" state
  3. Line 325: "Initial hint button" state

**Rationale for 64.dp**:
- Accommodates 3 elements in Column (emoji + Chinese text + count)
- Provides adequate spacing between elements
- Maintains visual consistency across all button states
- Fits within recommended touch target size (48dp minimum)

**Verification Status**: ✅ VERIFIED - 2026-02-22

**Test Results** (Xiaomi 24031PN0DC):
- ✅ "提示" text is fully visible on real device
- ✅ All three button states display correctly
- ✅ No layout overlap with adjacent components
- ✅ Button height (64.dp) is appropriate
- ✅ User confirmed: "这次测试可以了"
