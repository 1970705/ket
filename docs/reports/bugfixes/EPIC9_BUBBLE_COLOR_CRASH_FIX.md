# Epic #9 Bug Fix: BubbleColor Crash

**Date**: 2026-02-25
**Severity**: P0 - CRASH
**Status**: ✅ FIXED
**Device**: Xiaomi 24031PN0DC

---

## Bug Report

### Issue
App crashed when clicking "开始游戏" (Start Game) button in Match Game.

### Crash Log
```
FATAL EXCEPTION: main
java.lang.ArrayIndexOutOfBoundsException: length=18; index=43
at androidx.compose.ui.graphics.Color.getColorSpace-impl(Color.kt:673)
at androidx.compose.ui.graphics.Color.convert-vNxB06k(Color.kt:135)
at androidx.compose.ui.graphics.ColorKt.toArgb-8_81llA(Color.kt:639)
at androidx.compose.ui.graphics.AndroidPaint_androidKt.setNativeColor-4WTKRHQ(AndroidPaint.android.kt:172)
```

### Root Cause
**File**: `MatchGameScreen.kt:849`

```kotlin
// BEFORE (BROKEN)
private fun BubbleColor.toComposeColor(): Color = Color(this.colorValue)
```

The `Color(ULong)` constructor causes an ArrayIndexOutOfBoundsException because Compose's internal Color class interprets the ULong value incorrectly as a color space index rather than an ARGB color value.

### Fix Applied
```kotlin
// AFTER (FIXED)
private fun BubbleColor.toComposeColor(): Color = Color(this.colorValue.toLong())
```

By converting `ULong` to `Long` first, the value is properly interpreted as an ARGB color.

### Code Change
**File**: `app/src/main/java/com/wordland/ui/screens/MatchGameScreen.kt`

```diff
- private fun BubbleColor.toComposeColor(): Color = Color(this.colorValue)
+ private fun BubbleColor.toComposeColor(): Color = Color(this.colorValue.toLong())
```

---

## Verification

### Test Results (Xiaomi 24031PN0DC)
| Test | Status |
|------|--------|
| App launch | ✅ Pass |
| Navigate to Match Game | ✅ Pass |
| Click "开始游戏" | ✅ Pass - NO CRASH |
| Bubbles displayed with colors | ✅ Pass |
| Matching functionality | ✅ Pass |
| Pause/Resume | ✅ Pass |
| Game completion | ✅ Pass |

### Screenshots
- `docs/screenshots/epic9-match-game/epic9_fix_matchgame.png` - Match Game screen loaded
- `docs/screenshots/epic9-match-game/epic9_fix_started.png` - Game started without crash
- `docs/screenshots/epic9-match-game/epic9_fix_matched.png` - Match successful (1/2)

### Logcat Verification
No crashes in current session after fix.

---

## Technical Details

### BubbleColor Enum Definition
```kotlin
enum class BubbleColor(val colorValue: ULong) {
    PINK(0xFFFFB6C1u),
    GREEN(0xFF90EE90u),
    PURPLE(0xFFDDA0DDu),
    ORANGE(0xFFFFA500u),
    BROWN(0xFFD2691Eu),
    BLUE(0xFF87CEEBu),
}
```

The `0xFFFFB6C1u` format is a ULong literal representing:
- `FF` = Alpha (255)
- `FFB6C1` = RGB for Light Pink

When passed directly to `Color(ULong)`, Compose's internal logic attempted to use this as an index into a color space array, causing the crash.

### Why This Happens
Compose's `Color` class has multiple constructors:
- `Color(Long)` - expects ARGB packed as Long
- `Color(Int)` - expects ARGB packed as Int
- `Color(float, float, float, float)` - RGBA components
- Internal converters that use color space indices

The `ULong` to `Color` conversion path was going through an incorrect code path that interpreted the value as a color space index (43 or 30) rather than as an ARGB color value. Since there are only 18 color spaces defined, this caused an `ArrayIndexOutOfBoundsException`.

---

## Impact

### Before Fix
- ❌ App crashed immediately when clicking "开始游戏"
- ❌ Match Game completely unplayable
- ❌ Users could not access Epic #9 feature

### After Fix
- ✅ Game starts smoothly
- ✅ All bubble colors display correctly
- ✅ Full gameplay functional
- ✅ No performance impact

---

## Related Files
- `app/src/main/java/com/wordland/ui/screens/MatchGameScreen.kt` (line 849)
- `app/src/main/java/com/wordland/domain/model/BubbleState.kt` (BubbleColor enum)

---

## Reporter
**android-test-engineer-4**
**Epic #9 Task #3** - Real Device Testing

---

**Fix Applied**: 2026-02-25
**Verified On**: Xiaomi 24031PN0DC (5369b23a)
