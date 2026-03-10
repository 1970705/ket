# Performance Optimization Quick Reference

**Wordland Android Performance Monitoring**

---

## Quick Commands

### Automated Testing
```bash
./benchmark_performance.sh
```

### Manual Measurements
```bash
# Startup time
adb shell am start -W -n com.wordland/.ui.MainActivity

# Memory usage
adb shell dumpsys meminfo com.wordland

# Clear app data
adb shell pm clear com.wordland
```

### Run Benchmarks
```bash
# Macrobenchmark (startup, gameplay)
./gradlew :benchmark:connectedCheck

# Microbenchmark (algorithms)
./gradlew :microbenchmark:connectedCheck
```

---

## Performance Monitoring API

```kotlin
// Initialize (in Application.onCreate)
PerformanceMonitor.initialize()
ImageLoadingOptimizer.initialize(context)

// Track an operation
PerformanceMonitor.startOperation("Data_Load")
// ... do work ...
PerformanceMonitor.endOperation("Data_Load", thresholdMs = 100)

// Or use measure()
PerformanceMonitor.measure("ExpensiveOperation") {
    // Do work
}

// Get statistics
val frameStats = PerformanceMonitor.getFrameStats()
val memory = PerformanceMonitor.getMemoryUsage()

// Log full report
PerformanceMonitor.logReport()
```

---

## Performance Targets

| Metric | Target | Check Command |
|--------|--------|---------------|
| Cold Startup | < 3s | `adb shell am start -W ...` |
| Frame Rate | 60fps | GPU Profiler (green bars) |
| Memory | < 150MB | `adb shell dumpsys meminfo ...` |
| Jank | < 5% | GPU Profiler (red bars) |

---

## Troubleshooting

### Slow Startup
1. Check `adb shell am start -W -n com.wordland/.ui.MainActivity`
2. Look at TotalTime (should be < 3000ms)
3. Profile with Systrace: `python systrace.py -o startup.html am wm gfx`

### Poor Frame Rate
1. Open Android Studio Profiler > GPU
2. Look for red/yellow bars (> 16ms)
3. Enable GPU profiling: `adb shell setprop debug.hwui.profile true`

### High Memory
1. Check `adb shell dumpsys meminfo com.wordland`
2. Look for TOTAL: (should be < 153600 KB)
3. Heap dump in Android Studio Profiler > Memory

---

## Optimization Tips

### Compose
```kotlin
// ✅ Good: Use remember
val sorted = remember(items) { items.sortedBy { it.name } }

// ❌ Bad: Recalculates every time
val sorted = items.sortedBy { it.name }
```

```kotlin
// ✅ Good: Use derivedStateOf
val filtered = remember(filter) {
    derivedStateOf { items.filter { it.matches(filter) } }
}
```

```kotlin
// ✅ Good: Use key() for lists
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}
```

### General
```kotlin
// ✅ Good: Lazy initialization
private val repository by lazy { MyRepository() }

// ✅ Good: Background work
viewModelScope.launch(Dispatchers.IO) {
    // Heavy work
}
```

---

## Files Reference

### Performance Monitoring
- `PerformanceMonitor.kt` - Frame time, memory, operation tracking
- `ComposePerformanceHelper.kt` - Recomposition tracking
- `StartupPerformanceTracker.kt` - Startup timing
- `ImageLoadingOptimizer.kt` - Image loading optimization

### Benchmarks
- `benchmark/` - Macrobenchmark module
- `microbenchmark/` - Microbenchmark module

### Documentation
- `PERFORMANCE_OPTIMIZATION_REPORT.md` - Detailed report
- `PERFORMANCE_TESTING_GUIDE.md` - Testing guide
- `PERFORMANCE_IMPLEMENTATION_SUMMARY.md` - Implementation summary

### Scripts
- `benchmark_performance.sh` - Automated testing

---

## Checklist

Before releasing:
- [ ] Run `./benchmark_performance.sh`
- [ ] Verify startup < 3s
- [ ] Verify stable 60fps
- [ ] Verify memory < 150MB
- [ ] Check for memory leaks
- [ ] Review benchmark reports

---

## Help

- Full documentation: `docs/PERFORMANCE_OPTIMIZATION_REPORT.md`
- Testing guide: `docs/PERFORMANCE_TESTING_GUIDE.md`
- Contact: android-performance-expert@wordland-dev-team

---

**Last Updated**: 2026-02-16
**Version**: 1.1
