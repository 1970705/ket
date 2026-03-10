# Performance Baseline Testing Guide

**Purpose**: Establish baseline performance metrics for the Wordland app
**Priority**: P0 - Critical for release
**Required**: Connected Android device or running emulator

---

## Prerequisites

1. **Android Device or Emulator**
   - API level 26+ (Android 8.0+)
   - USB debugging enabled
   - Connected via ADB

2. **Verify Connection**
   ```bash
   adb devices
   # Should show: "device" (not "offline" or "unauthorized")
   ```

3. **Gradle Daemon**
   ```bash
   ./gradlew --stop  # Clean state before testing
   ```

---

## Testing Steps

### Step 1: Quick Startup Measurement (5 minutes)

This provides an immediate baseline without running the full benchmark suite.

```bash
# 1. Build the app
./gradlew assembleDebug

# 2. Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Clear app data for cold start
adb shell pm clear com.wordland

# 4. Measure cold startup time
adb shell am start -W -n com.wordland/.ui.MainActivity
# Look for "TotalTime: XXXms"

# 5. Check memory usage
adb shell dumpsys meminfo com.wordland | grep "TOTAL:"
```

**Expected Results**:
- Cold startup: < 3000ms (3 seconds)
- Memory: < 150MB

---

### Step 2: Automated Benchmark Suite (15-30 minutes)

Run the full automated performance testing script:

```bash
./benchmark_performance.sh
```

This script will:
1. Check device connectivity
2. Build release APK
3. Run macrobenchmarks (startup, frame timing)
4. Run microbenchmarks (algorithm performance)
5. Measure manual metrics (startup time, memory usage)

**Results Location**:
- Macrobenchmark: `benchmark/build/reports/`
- Microbenchmark: `microbenchmark/build/reports/`

---

### Step 3: Manual GPU Profiling (Optional, 10 minutes)

For detailed frame rendering analysis:

1. **Enable GPU Profiling**
   ```bash
   adb shell setprop debug.hwui.profile true
   adb shell stop && adb shell start
   ```

2. **Navigate Through App**
   - Launch app
   - Navigate to Look Island
   - Complete a few words in Level 1
   - Use hints
   - Submit answers

3. **Check Profile Data**
   ```bash
   adb shell dumpsys gfxinfo com.wordland reset
   # ... use the app ...
   adb shell dumpsys gfxinfo com.wordland
   ```

4. **Disable Profiling**
   ```bash
   adb shell setprop debug.hwui.profile false
   ```

**Look For**:
- Green bars: < 16ms (good, 60fps)
- Yellow bars: 16-33ms (acceptable)
- Red bars: > 33ms (jank, needs optimization)

---

## Baseline Metrics Template

When you complete testing, document results in this format:

### Startup Performance
| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Cold Start | < 3000ms | ___ms | ✅/❌ |
| Warm Start | < 1000ms | ___ms | ✅/❌ |
| Time to First Level | < 5000ms | ___ms | ✅/❌ |

### Frame Performance
| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Average FPS | 60 | ___ fps | ✅/❌ |
| Frame Time | < 16.6ms | ___ms | ✅/❌ |
| Frame Drop Rate | < 5% | ___% | ✅/❌ |

### Memory Performance
| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Initial Memory | < 100MB | ___MB | ✅/❌ |
| Peak Memory | < 150MB | ___MB | ✅/❌ |
| Memory Leaks | None | ___ | ✅/❌ |

---

## Troubleshooting

### Device Not Found
```bash
# Restart ADB server
adb kill-server
adb start-server
# Check devices again
adb devices
```

### Benchmark Failures
```bash
# Clean build
./gradlew clean
# Try debug build first
./gradlew :benchmark:assembleDebug
# Check device has enough storage
adb shell df
```

### Permission Issues
```bash
# Grant permissions manually
adb shell pm grant com.wordland android.permission.WRITE_EXTERNAL_STORAGE
```

---

## Next Steps After Baseline

1. **Document Results**: Add actual metrics to this file
2. **Identify Bottlenecks**: Note any metrics that don't meet targets
3. **Create Optimization Tasks**: File specific tasks for identified issues
4. **Track Over Time**: Re-run tests weekly to detect regressions

---

**Status**: ⏳ Awaiting device connection to execute tests
**Last Updated**: 2026-02-17
