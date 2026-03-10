# P0 Phase 1 Real Device Test Plan

**Test Date**: 2026-02-17
**Tester**: android-test-engineer
**Device**: Xiaomi 24031PN0DC (ID: 5369b23a)
**Android Version**: 16
**APK**: app/build/outputs/apk/debug/app-debug.apk

---

## Test Status

**Installation Status**: ❌ BLOCKED - Device requires "USB Installation" to be enabled in Developer Options

---

## Test Objectives

Verify P0 Phase 1 features work correctly on real device:

1. **Combo System** - Display consecutive correct answers with fire effect
2. **Enhanced Progress Bar** - Dynamic color changes (red → yellow → green)
3. **Motivational Messages** - Display based on user performance
4. **Performance Detection** - Auto-detect device tier (high/medium/low)

---

## Pre-Test Setup (Device Configuration)

### Enable USB Installation on Xiaomi Devices

**Method 1: Developer Options**
1. Go to Settings → Additional Settings → Developer Options
2. Find and enable "USB Install" (USB 安装) or "Install via USB"
3. Retry installation

**Method 2: MIUI Security Settings**
1. Go to Settings → Additional Settings → Privacy → Special Permissions
2. Find "Install unknown apps" and enable for ADB
3. Or go to Security → Unknown Sources and enable

**Method 3: Manual Install**
1. Copy `app/build/outputs/apk/debug/app-debug.apk` to device via USB
2. Use file manager to install the APK
3. Allow installation from unknown sources if prompted

---

## Test Cases

### TC-01: App Installation and First Launch

| Step | Action | Expected Result | Actual | Status |
|------|--------|-----------------|--------|--------|
| 1 | Install APK | Installation succeeds | - | ⏳ Blocked |
| 2 | Launch app | Home screen appears | - | ⏳ |
| 3 | Check database | 30 words seeded | - | ⏳ |
| 4 | Check Level 1 | Unlocked status | - | ⏳ |

### TC-02: Combo System Test

| Step | Action | Expected Result | Actual | Status |
|------|--------|-----------------|--------|--------|
| 1 | Answer 1st word correctly | Combo count = 1, fire effect appears | - | ⏳ |
| 2 | Answer 2nd word correctly | Combo count = 2, fire effect grows | - | ⏳ |
| 3 | Answer 3rd word correctly | Combo count = 3, maximum fire effect | - | ⏳ |
| 4 | Answer incorrectly | Combo resets to 0, fire disappears | - | ⏳ |

### TC-03: Enhanced Progress Bar Test

| Step | Action | Expected Result | Actual | Status |
|------|--------|-----------------|--------|--------|
| 1 | Start Level 1 | Progress bar is red (0% progress) | - | ⏳ |
| 2 | Complete 2 words | Progress bar turns yellow (~33% progress) | - | ⏳ |
| 3 | Complete 4 words | Progress bar turns green (~66% progress) | - | ⏳ |
| 4 | Complete all 6 words | Progress bar is full green (100% progress) | - | ⏳ |

### TC-04: Motivational Messages Test

| Step | Action | Expected Result | Actual | Status |
|------|--------|-----------------|--------|--------|
| 1 | Get 3+ combo | See "Amazing!" or similar message | - | ⏳ |
| 2 | Break combo | See "Keep going!" or similar | - | ⏳ |
| 3 | Complete level | See "Level Complete!" with stars | - | ⏳ |

### TC-05: Performance Detection Test

| Step | Action | Expected Result | Actual | Status |
|------|--------|-----------------|--------|--------|
| 1 | Check logcat for performance tier | See "DeviceTier: HIGH/MEDIUM/LOW" | - | ⏳ |
| 2 | Navigate animations | Smooth, no lag | - | ⏳ |
| 3 | Combo animations | Smooth 60fps | - | ⏳ |

---

## Test Commands

```bash
# Install APK (after enabling USB installation)
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.wordland/.ui.MainActivity

# Check logcat for performance detection
adb logcat | grep -E "Performance|DeviceTier|Combo|MemoryStrength"

# Check database
adb shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT COUNT(*) FROM words'"

# Clear data (if needed for retesting)
adb shell pm clear com.wordland
```

---

## Automated Test Script

After enabling USB installation, run:

```bash
./scripts/test/test_first_launch.sh
```

Then manually verify visual features (combo, progress bar colors, animations).

---

## Notes

- Device: Xiaomi Aurora with Android 16
- ADB installation requires "USB Installation" to be enabled in Developer Options
- This is a security feature on MIUI devices

---

## Test Results Summary

| Category | Status | Notes |
|----------|--------|-------|
| Installation | ❌ BLOCKED | Device security restriction |
| Combo System | ⏳ Pending | Waiting for install |
| Progress Bar | ⏳ Pending | Waiting for install |
| Motivational Messages | ⏳ Pending | Waiting for install |
| Performance Detection | ⏳ Pending | Waiting for install |

---

**Next Steps**: Enable "USB Installation" in Developer Options and retry installation.
