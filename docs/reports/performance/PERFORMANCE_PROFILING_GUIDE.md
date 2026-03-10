# Performance Profiling Guide - Wordland

**Version**: 1.0
**Last Updated**: 2026-02-18
**Project**: Wordland - KET Vocabulary Learning Android App

---

## Overview

This guide provides comprehensive instructions for profiling the Wordland app to identify and fix performance issues.

---

## Table of Contents

1. [Android Profiler Usage](#1-android-profiler-usage)
2. [Compose UI Profiling](#2-compose-ui-profiling)
3. [Benchmark Testing](#3-benchmark-testing)
4. [Common Performance Issues](#4-common-performance-issues)
5. [Performance Optimization Checklist](#5-performance-optimization-checklist)

---

## 1. Android Profiler Usage

Android Studio Profiler provides real-time data about your app's CPU, memory, network, and battery usage.

### 1.1 Opening Android Profiler

**Method 1**: From Android Studio
1. Run your app on a device or emulator
2. View > Tool Windows > Profiler
3. Select your app process (com.wordland)

**Method 2**: From Run Toolbar
1. Click the Profiler tab next to Logcat
2. Select your app process

### 1.2 CPU Profiling

**Purpose**: Identify CPU-intensive operations and optimize them

**Steps**:
1. Open Android Profiler
2. Click on **CPU** section
3. Click **Record** button
4. Perform the action you want to profile (e.g., navigate to a level)
5. Click **Stop**
6. Analyze the flame graph

**What to Look For**:
- **Flame Graph**: Shows call stack and execution time
- **Top Down**: Which methods consumed the most CPU time
- **Bottom Up**: Which methods were called most frequently
- **Hotspots**: Methods that take > 100ms

**Common Issues**:
- Main thread blocking (UI freeze)
- Expensive computations on main thread
- Too many object allocations

**Example**: If you see `SpellBattleQuestion.isCorrect()` taking > 10ms, consider caching or optimizing.

### 1.3 Memory Profiling

**Purpose**: Detect memory leaks and excessive allocations

**Steps**:
1. Open Android Profiler
2. Click on **Memory** section
3. Click **Record** button
4. Navigate through the app
5. Click **Dump Java Heap**
6. Analyze the heap dump

**What to Look For**:
- **Memory Churn**: Frequent allocations/deallocations (GC thrashing)
- **Memory Leaks**: Objects growing over time
- **Large Allocations**: Single objects > 1MB

**Key Metrics**:
- **Allocations**: Number of objects allocated (target: < 1000/sec)
- **Memory Size**: Total heap size (target: < 150MB)
- **GC Count**: Garbage collections (target: < 10/min)

**Example**: If you see 10,000 String allocations per second, consider reusing strings or using StringBuilder.

### 1.4 Network Profiling

**Purpose**: Analyze API calls and optimize network usage

**Note**: Wordland currently uses local database only. This section reserved for future cloud sync feature.

### 1.5 GPU Profiling

**Purpose**: Identify rendering performance issues

**Steps**:
1. Open Android Profiler
2. Click on **GPU** section
3. Look at the frame rendering graph

**What to Look For**:
- **Green bars**: Good (< 16ms per frame, 60 FPS)
- **Yellow bars**: Warning (16-33ms per frame, 30-60 FPS)
- **Red bars**: Critical (> 33ms per frame, < 30 FPS)

**Enable GPU Overdraw**:
```bash
# Enable overdraw debugging
adb shell setprop debug.hwui.overdraw show

# Disable overdraw debugging
adb shell setprop debug.hwui.overdraw false
```

**Color Guide**:
- **No color**: No overdraw (perfect)
- **Blue**: 1x overdraw (good)
- **Green**: 2x overdraw (acceptable)
- **Pink**: 3x overdraw (bad)
- **Red**: 4x overdraw (critical)

---

## 2. Compose UI Profiling

Jetpack Compose provides specific tools for profiling Compose UI.

### 2.1 Enable Compose Compiler Metrics

**Status**: Already enabled in Wordland (app/build.gradle.kts lines 33-37)

**Location**: `build/compose_metrics/`

**Metrics Available**:
- **composables.txt**: All composable functions
- **composables.csv**: Detailed metrics
- **restartability.txt**: Which composables can restart
- **skippability.txt**: Which composables can skip recomposition

**How to Read**:
```
composable "fun SpellBattleGame(
  question: SpellBattleQuestion,
  userAnswer: String,
  ...
)"
  restartable scheme:"restartable" stable arguments 2
  skippable true
```

**What to Look For**:
- **restartable**: Can restart without recomposing parent
- **skippable**: Can skip recomposition if inputs haven't changed
- **stable arguments**: Arguments are stable (won't change unexpectedly)

**Issues**:
- If a composable is not restartable/skippable, it may cause unnecessary recompositions

### 2.2 Using Compose Tracking

**Location**: `app/src/main/java/com/wordland/performance/ComposePerformanceHelper.kt`

**Usage**:
```kotlin
@Composable
fun MyScreen() {
    ComposePerformanceHelper.TrackRecomposition("MyScreen")

    // Your composable content
}
```

**Output in Logcat**:
```
V/ComposePerf: Recomposition: MyScreen (count: 1)
V/ComposePerf: Recomposition: MyScreen (count: 2)
V/ComposePerf: Recomposition: MyScreen (count: 3)
```

**What to Look For**:
- **Excessive recompositions**: Count > 10 per minute for the same screen
- **Unexpected recompositions**: Recomposition when inputs haven't changed

### 2.3 Using derivedStateOf

**Purpose**: Prevent unnecessary recalculations

**Before** (inefficient):
```kotlin
@Composable
fun LearningScreen(viewModel: LearningViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Recalculated on every recomposition
    val progress = uiState.correctCount * 100 / uiState.totalCount
}
```

**After** (optimized):
```kotlin
@Composable
fun LearningScreen(viewModel: LearningViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Only recalculated when uiState changes
    val progress by remember {
        derivedStateOf {
            uiState.correctCount * 100 / uiState.totalCount
        }
    }
}
```

### 2.4 Using remember

**Purpose**: Cache expensive calculations

**Before** (inefficient):
```kotlin
@Composable
fun AnswerBoxes(targetWord: String, userAnswer: String) {
    // Recalculated on every recomposition
    val wrongPositions = SpellBattleQuestion(
        wordId = "", translation = "", targetWord = targetWord, hint = null
    ).getWrongPositions(userAnswer)

    // Render boxes...
}
```

**After** (optimized):
```kotlin
@Composable
fun AnswerBoxes(targetWord: String, userAnswer: String) {
    // Only recalculated when inputs change
    val wrongPositions = remember(targetWord, userAnswer) {
        SpellBattleQuestion(
            wordId = "", translation = "", targetWord = targetWord, hint = null
        ).getWrongPositions(userAnswer)
    }

    // Render boxes...
}
```

### 2.5 Adding Stability Annotations

**Purpose**: Help Compose compiler optimize

**@Immutable**: For classes that never change
```kotlin
@Immutable
data class SpellBattleQuestion(
    val wordId: String,
    val translation: String,
    val targetWord: String,
    val hint: String?
)
```

**@Stable**: For classes that change but notify Compose
```kotlin
@Stable
class LearningUiState(
    val question: SpellBattleQuestion?,
    val isLoading: Boolean,
    val hintAvailable: Boolean
) {
    // All properties are val, so this is stable
}
```

---

## 3. Benchmark Testing

### 3.1 Running Macrobenchmarks

**Purpose**: Measure integration-level performance (startup, navigation, gameplay)

**Using Gradle**:
```bash
# Run all macrobenchmarks
./gradlew :benchmark:connectedCheck

# Run specific test class
./gradlew :benchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.benchmark.StartupBenchmark

# Run specific test method
./gradlew :benchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.benchmark.StartupBenchmark#benchmarkColdStart
```

**Using Script**:
```bash
./benchmark_performance.sh
```

**Requirements**:
- Android device or emulator (API 26+)
- Device unlocked and screen on
- Sufficient storage for heap dumps

**Results Location**:
`benchmark/build/reports/connected/androidTest/`

### 3.2 Running Microbenchmarks

**Purpose**: Measure unit-level performance (algorithms, functions)

**Using Gradle**:
```bash
# Run all microbenchmarks
./gradlew :microbenchmark:connectedCheck

# Run specific test class
./gradlew :microbenchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.microbenchmark.AlgorithmBenchmark
```

**Results Location**:
`microbenchmark/build/reports/connected/androidTest/`

### 3.3 Interpreting Benchmark Results

**Benchmark Report Structure**:
```
Benchmark                            Mode  Cnt   Score   Error  Units
AlgorithmBenchmark.benchmarkAns...  avgt   10   0.123 ± 0.015  ms/op
```

**What It Means**:
- **Mode**: Measurement mode (avgt = average time)
- **Cnt**: Number of iterations (10 is good)
- **Score**: Median execution time (0.123ms)
- **Error**: Margin of error (±0.015ms)
- **Units**: Units of measurement (ms/op = milliseconds per operation)

**What to Look For**:
- **Consistent scores**: Error should be < 20% of score
- **Improvements over time**: Scores should decrease as you optimize
- **Regressions**: Scores increase significantly (> 20%) = bad

---

## 4. Common Performance Issues

### 4.1 Main Thread Blocking

**Symptoms**:
- UI freezes or stutters
- Input lag
- Janky animations

**Detection**:
1. Open Android Profiler > CPU
2. Record while reproducing the issue
3. Look for long-running operations on main thread

**Common Causes**:
- Database queries on main thread
- File I/O on main thread
- Network calls on main thread
- Expensive calculations on main thread

**Solutions**:
```kotlin
// Bad: Database query on main thread
val words = wordDao.getAllWords()

// Good: Use coroutines
val words = withContext(Dispatchers.IO) {
    wordDao.getAllWords()
}

// Good: Use Flow
val words = wordDao.getAllWordsFlow()
```

### 4.2 Excessive Recomposition

**Symptoms**:
- UI lags during user interaction
- High CPU usage
- Battery drain

**Detection**:
1. Use ComposePerformanceHelper.TrackRecomposition
2. Look for functions recomposing > 10 times/minute
3. Check Compose compiler metrics

**Common Causes**:
- Unstable parameters in @Composable functions
- Missing remember blocks
- Recalculating derived values

**Solutions**:
```kotlin
// Bad: Unstable parameter
@Composable
fun MyScreen(data: List<Word>) {
    // List is not stable, recomposes every time
}

// Good: Use immutable wrapper
@Immutable
data class WordList(val words: List<Word>)

@Composable
fun MyScreen(data: WordList) {
    // WordList is immutable, skips unnecessary recompositions
}
```

### 4.3 Memory Leaks

**Symptoms**:
- Memory grows over time
- OutOfMemoryError crashes
- Slow performance after extended use

**Detection**:
1. Use LeakCanary (see Memory Leak Detection Guide)
2. Monitor memory with Android Profiler
3. Look for consistent memory growth

**Common Causes**:
- Static references to Activities/Fragments
- Non-static inner classes holding outer reference
- Listeners not unregistered
- ViewModels with wrong scope

**Solutions**:
```kotlin
// Bad: Static reference
object Cache {
    var activity: Activity? = null // Leak!
}

// Good: Weak reference
object Cache {
    var activityRef: WeakReference<Activity>? = null
}
```

### 4.4 Frame Drops (Jank)

**Symptoms**:
- Stuttering animations
- Choppy scrolling
- Visual lag

**Detection**:
1. Enable GPU profiling
2. Look for red/yellow bars in GPU section
3. Measure frame time with PerformanceMonitor

**Common Causes**:
- Expensive layout calculations
- Too many views in a layout
- Complex animations
- Image loading on main thread

**Solutions**:
```kotlin
// Bad: Load image on main thread
val bitmap = BitmapFactory.decodeFile(path)

// Good: Load in background
val bitmap = withContext(Dispatchers.IO) {
    BitmapFactory.decodeFile(path)
}

// Better: Use Coil (already implemented in Wordland)
AsyncImage(
    model = "file://$path",
    contentDescription = null
)
```

### 4.5 Slow Startup

**Symptoms**:
- App takes > 3 seconds to launch
- Black screen on startup
- ANR on startup

**Detection**:
1. Use StartupPerformanceTracker
2. Run StartupBenchmark
3. Profile with Android Profiler

**Common Causes**:
- Too much work in Application.onCreate
- Synchronous database initialization
- Blocking I/O on startup
- Heavy main thread work

**Solutions**:
```kotlin
// Bad: Block startup on database init
override fun onCreate() {
    super.onCreate()
    val database = Room.databaseBuilder(/*...*/).build() // Blocks!
    val words = database.wordDao().getAllWords() // Blocks!
}

// Good: Lazy initialization
override fun onCreate() {
    super.onCreate()
    // Don't block, initialize in background
    lifecycleScope.launch {
        val database = WordDatabase.getInstance(applicationContext)
        // Use database...
    }
}
```

---

## 5. Performance Optimization Checklist

Use this checklist when optimizing performance:

### Pre-Optimization
- [ ] Establish baseline measurements (see Performance Baseline doc)
- [ ] Identify bottlenecks with profiling
- [ ] Prioritize issues by user impact

### Optimization
- [ ] Fix main thread blocking
- [ ] Reduce unnecessary recompositions
- [ ] Add remember blocks for expensive calculations
- [ ] Add stability annotations (@Immutable, @Stable)
- [ ] Optimize database queries
- [ ] Fix memory leaks
- [ ] Reduce frame drops

### Post-Optimization
- [ ] Measure again with same benchmarks
- [ ] Compare with baseline
- [ ] Document improvements
- [ ] Regression testing (ensure no new issues)

### Continuous Monitoring
- [ ] Run benchmarks before each release
- [ ] Monitor crash reports for performance issues
- [ ] Collect user feedback on performance
- [ ] Update baseline as needed

---

## 6. Profiling Workflow

### Step-by-Step Guide

**Step 1: Define the Problem**
- What is slow? (startup, navigation, gameplay)
- How slow is it? (measure first)
- What should it be? (target metric)

**Step 2: Profile**
- Open Android Profiler
- Record the problematic scenario
- Capture metrics (CPU, memory, GPU)

**Step 3: Analyze**
- Identify the bottleneck
- Find the root cause
- Propose solutions

**Step 4: Optimize**
- Implement the fix
- Test locally
- Verify no regressions

**Step 5: Measure**
- Run same benchmarks
- Compare with baseline
- Document improvement

**Step 6: Iterate**
- If not enough improvement, repeat
- If improvement is good, move to next issue

---

## 7. Quick Reference Commands

### Profiling
```bash
# Start profiling
adb shell am start -n com.wordland/.ui.MainActivity

# Dump heap
adb shell am dumpheap com.wordland /data/local/tmp/heap.hprof

# Pull heap dump
adb pull /data/local/tmp/heap.hprof

# Measure startup time
adb shell am start -W -n com.wordland/.ui.MainActivity

# Check memory usage
adb shell dumpsys meminfo com.wordland

# Enable GPU overdraw
adb shell setprop debug.hwui.overdraw show

# Disable GPU overdraw
adb shell setprop debug.hwui.overdraw false
```

### Benchmarks
```bash
# Run all benchmarks
./benchmark_performance.sh

# Run macrobenchmarks only
./gradlew :benchmark:connectedCheck

# Run microbenchmarks only
./gradlew :microbenchmark:connectedCheck

# Build benchmark APK
./gradlew assembleBenchmark
```

---

## 8. Related Documentation

- [Performance Baseline](./PERFORMANCE_BASELINE.md) - Target metrics and current measurements
- [Memory Leak Detection Guide](./MEMORY_LEAK_DETECTION_GUIDE.md) - Detecting and fixing memory leaks
- [Performance Infrastructure Analysis](./PERFORMANCE_INFRASTRUCTURE_ANALYSIS.md) - Overview of performance systems
- [CLAUDE.md](../CLAUDE.md) - Project overview and commands

---

**Document Owner**: android-performance-expert
**Last Updated**: 2026-02-18
**Status**: ✅ Complete
