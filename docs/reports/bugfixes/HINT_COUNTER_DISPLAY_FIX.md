# BUG-004: Hint Counter Display Fix

**Date**: 2026-02-24
**Severity**: P2 (cosmetic issue, functional limit works)
**Status**: ✅ Fixed

## Problem Description

The hint counter was reported to display "提示 (4/3)" when attempting to use a 4th hint, even though the functional limit was correctly enforced.

## Root Cause Analysis

After thorough investigation and real-device testing, the bug was determined to be a potential edge case in state management:

1. **Primary Issue**: In `LearningViewModel.useHint()`, the error handler (hint limit reached) did not explicitly update `hintsRemaining = 0`. This could theoretically cause UI inconsistency.

2. **Defense in Depth**: The UI components (`HintTextContent`, `HintIconWithLevel`) did not have safety caps on the `hintLevel` display value. While the backend logic correctly capped the level at 3, the UI layer should also have defensive checks.

## Changes Made

### 1. LearningViewModel.kt (app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt)

**Line 312**: Added explicit `hintsRemaining = 0` in error handler

```kotlin
is com.wordland.domain.model.Result.Error -> {
    val currentState = _uiState.value
    if (currentState is LearningUiState.Ready) {
        _uiState.value = currentState.copy(
            hintAvailable = false,
            hintText = result.exception.message,
            hintsRemaining = 0, // Added: Ensure UI shows disabled state
        )
    }
}
```

### 2. HintCard.kt (app/src/main/java/com/wordland/ui/components/HintCard.kt)

**Lines 209-218**: Added safety cap in `HintTextContent`

```kotlin
@Composable
private fun HintTextContent(
    hintText: String,
    hintLevel: Int,
) {
    // Cap hint level at 3 for display (safety check)
    val displayLevel = minOf(hintLevel, 3)

    Column {
        Text(
            text = "提示 $displayLevel/3",  // Uses capped value
            // ...
        )
    }
}
```

**Lines 158-203**: Added safety cap in `HintIconWithLevel`

```kotlin
@Composable
private fun HintIconWithLevel(hintLevel: Int) {
    // Cap hint level at 3 for display (safety check)
    val displayLevel = minOf(hintLevel, 3)

    // Icon and dots now use displayLevel instead of raw hintLevel
    // ...
}
```

## Testing Results

**Device**: Xiaomi 24031PN0DC (5369b23a)
**Test Date**: 2026-02-24

| Click | Hint Level Display | Button State | Remaining Count |
|-------|-------------------|--------------|-----------------|
| 1 | "提示 1/3" | Enabled (💡) | (2) |
| 2 | "提示 2/3" | Enabled (💡) | (1) |
| 3 | "提示 3/3" | Enabled (💡) | (0) |
| 4 | "提示 3/3" | Disabled (🚫) | N/A |
| 5+ | "提示 3/3" | Disabled (🚫) | N/A |

**Result**: ✅ All states display correctly. The hint level properly caps at "3/3" and the button correctly shows disabled state after 3 hints.

## Verification Checklist

- [x] Hint counter displays "提示 3/3" (not "4/3") on 4th click
- [x] Button correctly disabled after 3 hints
- [x] Hint content correctly blocked when limit reached
- [x] All 4 UI states work correctly:
  - Empty state: "提示"
  - Level 1/2/3: "提示 X/3" with "(remaining)"
  - Limit Reached: Disabled button with 🚫 icon

## Related Files

- `app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt`
- `app/src/main/java/com/wordland/ui/components/HintCard.kt`
- `app/src/main/java/com/wordland/domain/hint/HintManager.kt` (unchanged, already correct)
- `app/src/main/java/com/wordland/domain/usecase/usecases/UseHintUseCaseEnhanced.kt` (unchanged, already correct)
