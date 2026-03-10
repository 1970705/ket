# Bug Report: Progress Bar Blocking Cat Animation

**Bug ID**: P1-BUG-005
**Reported By**: android-test-engineer (via user testing)
**Date**: 2026-02-25
**Priority**: P1 (High - affects visual experience)
**Status**: ✅ Fixed and Verified

---

## Summary

The `LevelProgressBarEnhanced` component visually blocks/overlaps the `CompactPetAnimation` (cat) in the top-right corner of the screen.

## Affected Screens

- `LearningScreen.kt` (Spell Battle mode)
- `QuickJudgeScreen.kt` (Quick Judge mode)

## Steps to Reproduce

1. Launch any level in Spell Battle or Quick Judge mode
2. Observe the top-right corner where the cat animation appears
3. The progress bar (LevelProgressBarEnhanced) overlaps the cat

## Expected Behavior

The cat animation should be fully visible and not obscured by any UI elements.

## Actual Behavior

The progress bar component extends over the cat animation, blocking it from view.

## Root Cause Analysis

**Layout Structure Issue**:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Cat positioned at top-right
    CompactPetAnimation(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .size(80.dp),
    )

    // Content doesn't account for cat's space
    QuestionState(...) // Contains LevelProgressBarEnhanced
}
```

The `QuestionState` (or `LearningContent`) uses `fillMaxSize()` without proper padding to avoid the top-right corner where the cat is positioned.

## Solution Options

### Option 1: Add padding to content area (Recommended)
```kotlin
QuestionState(
    // ...
    modifier = Modifier
        .fillMaxSize()
        .padding(top = 16.dp, end = 100.dp) // Space for cat
)
```

### Option 2: Adjust cat position
Move cat to a different location that doesn't conflict with progress bar.

### Option 3: Add top bar elevation
Elevate the cat above other UI elements using `Modifier.zIndex()`.

## Files to Modify

1. **Primary**:
   - `app/src/main/java/com/wordland/ui/screens/LearningScreen.kt` (lines 130-170)
   - `app/src/main/java/com/wordland/ui/screens/QuickJudgeScreen.kt` (lines 203-280)

2. **Related**:
   - `app/src/main/java/com/wordland/ui/components/LevelProgressBarEnhanced.kt`

---

## Fix Implementation

**Solution Applied**: Option 1 - Add padding to content area

### LearningScreen.kt (Line 287-296)
```kotlin
Column(
    modifier =
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(end = 80.dp) // Space for cat animation in top-right corner
            .padding(
                bottom = WindowInsets.systemBars.asPaddingValues()
                    .calculateBottomPadding(),
            ),
    // ...
)
```

### QuickJudgeScreen.kt (Line 216-223)
```kotlin
Column(
    modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(end = 80.dp), // Space for cat animation in top-right corner
    // ...
)
```

**Explanation**: Added 80.dp end padding to both `LearningContent` and `QuestionState` Column layouts. This creates space for the cat animation (48.sp size + 16.dp padding ≈ 80.dp) in the top-right corner, preventing the `LevelProgressBarEnhanced` from overlapping it.

## Acceptance Criteria

- [x] Code changes implemented
- [x] Cat animation is fully visible in both Spell Battle and Quick Judge modes
- [x] No UI elements overlap the cat
- [x] Layout looks balanced on device (Xiaomi 24031PN0DC)
- [x] Screenshot verification passed

## Attachment

Screenshot: `/tmp/device_screenshot.png` (1080x2400)
Device: Xiaomi 24031PN0DC
Test Date: 2026-02-25

---

**Assigned To**: android-engineer
**Estimated Time**: 1-2 hours
**Actual Time**: 2 hours (including iteration)
**Fix Version**: v1.6
**Verified By**: User (2026-02-25)
