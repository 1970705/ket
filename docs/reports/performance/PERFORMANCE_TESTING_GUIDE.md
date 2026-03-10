# Performance Testing Guide - Wordland

**Version**: 1.1
**Last Updated**: 2026-02-19
**Status**: ✅ Infrastructure Ready - Awaiting Device Connection

---

## Quick Start Guide for Performance Testing

**Important**: Before running benchmarks, ensure:
1. A physical Android device is connected (recommended) OR emulator is running
2. Device is unlocked with screen on
3. Developer mode and USB debugging are enabled

---

## Prerequisites

1. **Connected Device or Emulator**
   ```bash
   adb devices
   ```

2. **Android Studio** (latest version)
   - For profiling and analysis

3. **Build Tools**
   - Java 17
   - Android SDK 34

---

## Quick Start

### 1. Automated Performance Testing

```bash
# Run all performance tests
./benchmark_performance.sh

# Run only macrobenchmarks
./gradlew :benchmark:connectedCheck

# Run only microbenchmarks
./gradlew :microbenchmark:connectedCheck
```

### 2. Manual Startup Time Measurement

```bash
# Clear app data
adb shell pm clear com.wordland

# Force stop app
adb shell am force-stop com.wordland

# Measure cold startup
adb shell am start -W -n com.wordland/.ui.MainActivity
```

**Look for**: `TotalTime: XXXms` (should be < 3000ms)

### 3. Memory Usage Check

```bash
# Launch app
adb shell am start -n com.wordland/.ui.MainActivity

# Get memory stats
adb shell dumpsys meminfo com.wordland | grep "TOTAL:"
```

**Look for**: `TOTAL: XXXXX KB` (should be < 153600 KB = 150MB)

### 4. Frame Rate Monitoring

**In Android Studio**:
1. View > Tool Windows > Profiler
2. Select Wordland process
3. Click GPU section
4. Navigate through the app

**Look for**:
- Green bars (< 16ms per frame) = Good
- Yellow bars (16-32ms) = Warning
- Red bars (> 32ms) = Problem

---

## Performance Targets

| Metric | Target | Pass/Fail |
|--------|--------|-----------|
| Cold Startup | < 3s | ✓ if TotalTime < 3000ms |
| Frame Rate | 60fps | ✓ if bars are mostly green |
| Memory | < 150MB | ✓ if TOTAL < 153600 KB |
| Jank | < 5% | ✓ if < 5% red/yellow bars |

---

## Benchmark Results

### Macrobenchmark Results

**Location**: `benchmark/build/reports/macrobenchmark/`

**Key Files**:
- `index.html` - Summary report
- `StartupBenchmark.html` - Startup timing details
- `GameplayBenchmark.html` - Frame rate details

**How to Read**:
- Look for "median" and "p95" values
- Compare against targets
- Check for regression over time

### Microbenchmark Results

**Location**: `microbenchmark/build/reports/microbenchmark/`

**Key Files**:
- `index.html` - Summary report
- `AlgorithmBenchmark.html` - Algorithm timing details

**How to Read**:
- Look for "ns/op" (nanoseconds per operation)
- Lower is better
- Check for regression over time

---

## Troubleshooting

### Issue: Benchmark fails to run

**Solution**:
```bash
# Check device connection
adb devices

# Unlock device screen
adb shell input keyevent KEYCODE_WAKEUP

# Disable screen timeout
adb shell settings put system screen_off_timeout 1800000
```

### Issue: Frame rate is poor

**Possible Causes**:
1. Too much work on main thread
2. Expensive recompositions
3. Overdraw

**Debug Steps**:
1. Enable GPU profiling:
   ```bash
   adb shell setprop debug.hwui.profile true
   ```

2. Check for overdraw:
   ```bash
   adb shell setprop debug.hwui.show_overdraw true
   ```

3. Run Systrace:
   ```bash
   python systrace.py --time=10 -o trace.html gfx
   ```

### Issue: Memory usage is high

**Debug Steps**:
1. Heap dump:
   - Android Studio > Profiler > Memory > Dump Java Heap

2. Look for:
   - Large object allocations
   - Duplicate objects
   - Long-lived objects

3. Check for leaks:
   - LeakCanary (automatically enabled in debug builds)
   - Look for leak notifications

### Issue: Startup is slow

**Debug Steps**:
1. Check trace:
   ```bash
   adb shell am start -W -n com.wordland/.ui.MainActivity
   ```

2. Look at phases:
   - Application.onCreate
   - Activity.onCreate
   - First frame

3. Profile with Systrace:
   ```bash
   python systrace.py --time=5 -o startup.html am wm gfx view
   ```

---

## Performance Monitoring in Code

### Add Performance Tracking

```kotlin
// In Application.onCreate
PerformanceMonitor.initialize()
ImageLoadingOptimizer.initialize(this)

// Track expensive operations
PerformanceMonitor.startOperation("Data_Load")
// ... do work ...
PerformanceMonitor.endOperation("Data_Load", thresholdMs = 100)

// Get statistics
val frameStats = PerformanceMonitor.getFrameStats()
val memory = PerformanceMonitor.getMemoryUsage()

// Log full report
PerformanceMonitor.logReport()
```

### Enable Compose Metrics

Add to `app/build.gradle.kts`:

```kotlin
kotlinOptions {
    freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.metrics:destination=build/compose_metrics"
    )
}
```

---

## Continuous Performance Monitoring

### Add to CI/CD

**GitHub Actions Workflow** (`.github/workflows/performance.yml`):

```yaml
name: Performance Tests

on:
  pull_request:
    paths:
      - 'app/src/**'
      - 'benchmark/src/**'

jobs:
  benchmark:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3

      - name: Run Macrobenchmarks
        run: ./gradlew :benchmark:connectedCheck

      - name: Upload Results
        uses: actions/upload-artifact@v3
        with:
          name: benchmark-results
          path: benchmark/build/reports/
```

---

## Performance Tips

### Compose Optimization

1. **Use remember wisely**
   ```kotlin
   // Good: Cached calculation
   val sorted = remember(items) { items.sortedBy { it.name } }

   // Bad: Recalculates every time
   val sorted = items.sortedBy { it.name }
   ```

2. **Use derivedStateOf**
   ```kotlin
   val filtered = remember(filter, items) {
       derivedStateOf { items.filter { it.matches(filter) } }
   }
   ```

3. **Extract components**
   ```kotlin
   // Good: Smaller recomposition scope
   @Composable
   fun ItemRow(item: Item) { ... }

   // Bad: Large recomposition scope
   @Composable
   fun BigList(items: List<Item>) { ... }
   ```

### General Optimization

1. **Lazy initialization**
   ```kotlin
   private val repository by lazy { MyRepository() }
   ```

2. **Background work**
   ```kotlin
   viewModelScope.launch(Dispatchers.IO) {
       // Heavy work here
   }
   ```

3. **Cache results**
   ```kotlin
   private val cache = mutableMapOf<String, Result>()

   fun getData(key: String): Result {
       return cache.getOrPut(key) { computeData(key) }
   }
   ```

---

## Checklist

Before releasing a new version:

- [ ] Run benchmark_performance.sh
- [ ] Verify startup time < 3s
- [ ] Verify stable 60fps
- [ ] Verify memory usage < 150MB
- [ ] Check for memory leaks
- [ ] Review benchmark reports
- [ ] Check for performance regression
- [ ] Optimize any bottlenecks found

---

## Resources

- [Android Performance Patterns](https://www.youtube.com/playlist?list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE)
- [Compose Performance](https://developer.android.com/jetpack/compose/performance)
- [Macrobenchmark](https://developer.android.com/topic/performance/benchmarks/macrobenchmark-overview)
- [Profiling Tools](https://developer.android.com/studio/profile)

---

**Quick Help**:
- Run `./benchmark_performance.sh` for automated testing
- Check `docs/reports/performance/PERFORMANCE_BASELINE.md` for target metrics
- Contact: android-performance-expert (wordland-dev-team)

---

## Recent Updates (2026-02-19)

### Infrastructure Improvements
- ✅ **Benchmark build type added** to `app/build.gradle.kts`
- ✅ **LeakCanary initialized** in `WordlandApplication.kt` (reflection-based for debug/release compatibility)
- ✅ **Build verified** - APK size: ~11.6 MB

### Next Steps
1. Connect a physical Android device (API 26+)
2. Run `./benchmark_performance.sh`
3. Review results in:
   - `benchmark/build/reports/connected/index.html`
   - `microbenchmark/build/reports/connected/index.html`
4. Update `PERFORMANCE_BASELINE.md` with actual measurements
