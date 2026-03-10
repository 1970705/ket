# Memory Leak Detection Guide - Wordland

**Version**: 1.0
**Last Updated**: 2026-02-18
**Project**: Wordland - KET Vocabulary Learning Android App

---

## Overview

This guide provides instructions for detecting and fixing memory leaks in the Wordland app using LeakCanary.

---

## What is LeakCanary?

[LeakCanary](https://square.github.io/leakcanary/) is a memory leak detection library for Android that automatically detects and reports memory leaks in your application.

**Status in Wordland**: ✅ Configured (debug builds only)

**Location**: `app/build.gradle.kts` line 102
```kotlin
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
```

---

## How LeakCanary Works

1. **Automatic Detection**: LeakCanary automatically watches destroyed objects (Activities, Fragments, etc.)
2. **Heap Dump**: If a destroyed object is not garbage collected, LeakCanary dumps the heap
3. **Analysis**: It analyzes the heap dump to find the reference chain that caused the leak
4. **Notification**: A notification is shown with the leak details
5. **Dashboard**: All leaks are displayed in the LeakCanary UI

---

## Using LeakCanary in Development

### Step 1: Install Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Use the App

Navigate through the app normally:
- Launch the app
- Navigate to different screens
- Play the game
- Go back to home screen
- Minimize and restore the app

### Step 3: Check for Leaks

**Notification**: If a leak is detected, you'll see a notification like:
> "LeakCanary: Detected 1 leak in com.wordland"

**LeakCanary UI**: Tap the notification to see:
- Leaked activity/fragment
- Reference chain (what's holding the reference)
- Source code location (if available)

---

## Manual Leak Detection Scenarios

### Scenario 1: Activity Lifecycle Leaks

**Purpose**: Detect Activities not being garbage collected

**Steps**:
```bash
# Clear app data
adb shell pm clear com.wordland

# Launch app
adb shell am start -n com.wordland/.ui.MainActivity

# Navigate through screens
# - Go to Island Map
# - Select a Level
# - Play through questions
# - Go back to Home

# Force stop
adb shell am force-stop com.wordland

# Trigger GC (if using Android Studio Profiler)
# Or wait for LeakCanary to auto-detect
```

**Expected**: No leaks detected

**What to Look For**:
- MainActivity leaked
- IslandMapScreen leaked
- LearningScreen leaked

### Scenario 2: ViewModel Leaks

**Purpose**: Detect ViewModels with incorrect scoping

**Steps**:
1. Navigate to LearningScreen
2. Complete a level
3. Navigate away
4. Rotate device multiple times
5. Check for leaks

**Expected**: No ViewModel leaks

**Common Causes**:
- ViewModel scoped to Application instead of Activity
- Direct context references in ViewModel

### Scenario 3: Database Connection Leaks

**Purpose**: Detect unclosed database connections

**Steps**:
1. Launch app
2. Play through several levels
3. Navigate between screens
4. Minimize app for 5 minutes
5. Restore app
6. Check for leaks

**Expected**: No database-related leaks

### Scenario 4: Coroutine Scope Leaks

**Purpose**: Detect coroutines running after activity destruction

**Steps**:
1. Navigate to LearningScreen
2. Start an operation (e.g., load level)
3. Immediately press back
4. Check for leaks

**Expected**: No coroutine scope leaks

### Scenario 5: Image Loading Leaks

**Purpose**: Detect image cache not being cleared

**Steps**:
1. Navigate through many screens with images
2. Minimize app
3. Wait 1 minute
4. Restore app
5. Check for leaks

**Expected**: No image loading leaks

---

## Interpreting LeakCanary Results

### Leak Reference Chain

LeakCanary shows a reference chain like:
```
* com.wordland.ui.MainActivity has leaked:
* GC ROOT static com.wordland.di.AppServiceLocator.instance
* holds com.wordland.di.AppServiceLocator.repositories
* holds java.util.HashMap
* holds android.util.ArrayMap
* holds com.wordland.ui.viewmodel.HomeViewModel
* holds com.wordland.ui.MainActivity
```

**How to Read**:
1. **Leaked Object**: The object that should have been GC'd
2. **GC ROOT**: The source of the leak (usually a static reference)
3. **Chain**: The path keeping the object alive

### Common Leak Patterns

#### Pattern 1: Static Reference
```
GC ROOT static MyClass.instance
```
**Fix**: Make the instance weak reference or clear it when done

#### Pattern 2: Non-Static Inner Class
```
GC ROOT com.wordland.ui.MainActivity$InnerClass this$0
```
**Fix**: Make inner class static or use weak reference

#### Pattern 3: Context Reference
```
holds android.content.Context
```
**Fix**: Use application context or weak reference

#### Pattern 4: Listener Not Unregistered
```
holds com.wordland.SomeManager$Listener
```
**Fix**: Unregister listener in onDestroy/onPause

---

## Common Memory Leaks in Jetpack Compose

### 1. remember with Non-Serializable Objects

**Leak**:
```kotlin
@Composable
fun MyScreen() {
    val activity = remember { LocalContext.current as Activity }
    // Activity leaked if screen rotates
}
```

**Fix**:
```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current
    // Use context instead of activity
}
```

### 2. LaunchedEffect Not Canceled

**Leak**:
```kotlin
@Composable
fun MyScreen() {
    LaunchedEffect(Unit) {
        // Runs forever
        while (true) { delay(1000) }
    }
}
```

**Fix**: LaunchedEffect is automatically canceled when composition ends

### 3. ViewModel in Wrong Scope

**Leak**:
```kotlin
// In Application scope
val viewModel: LearningViewModel = viewModel()
```

**Fix**:
```kotlin
// In Activity/Screen scope
@Composable
fun LearningScreen(
    viewModel: LearningViewModel = viewModel() // Correct scope
) { }
```

---

## Manual Heap Dump Analysis

If LeakCanary doesn't auto-detect, you can manually dump the heap:

### Using Android Studio

1. **Open Profiler**: View > Tool Windows > Profiler
2. **Select Process**: Choose com.wordland
3. **Capture Heap**: Click "Memory" > "Dump Java Heap"
4. **Analyze**: Look for:
   - Activities that should be destroyed
   - Duplicate singletons
   - Large objects retained

### Using adb

```bash
# Dump heap
adb shell am dumpheap com.wordland /data/local/tmp/heap.hprof

# Pull heap dump
adb pull /data/local/tmp/heap.hprof

# Convert to standard format (if needed)
hprof-conv heap.hprof heap-conv.hprof

# Open in Android Studio or Eclipse MAT
```

---

## Memory Leak Testing Checklist

Use this checklist when testing for memory leaks:

- [ ] Navigate to every screen in the app
- [ ] Rotate device on each screen (if applicable)
- [ ] Press back button multiple times
- [ ] Minimize and restore app
- [ ] Force stop and relaunch app
- [ ] Complete a full level
- [ ] Navigate between islands
- [ ] Use hint system
- [ ] Submit correct and incorrect answers
- [ ] Test virtual keyboard typing
- [ ] Wait 5 minutes with app idle
- [ ] Check for leaks after each scenario

---

## Fixing Memory Leaks

### General Approach

1. **Identify the leak**: Use LeakCanary to find the reference chain
2. **Locate the source**: Find where the reference is held
3. **Fix the issue**:
   - Use weak references
   - Clear references in lifecycle methods
   - Use proper scoping for ViewModels
   - Unregister listeners
4. **Verify the fix**: Reproduce the scenario and confirm leak is gone

### Common Fixes

#### Fix 1: Static Reference Leak

**Before**:
```kotlin
object AppServiceLocator {
    var context: Context? = null // Leak!
}
```

**After**:
```kotlin
object AppServiceLocator {
    private var contextRef: WeakReference<Context>? = null

    fun setContext(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }
}
```

#### Fix 2: Listener Leak

**Before**:
```kotlin
class MyManager {
    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener) {
        listeners.add(listener) // Leak!
    }
}
```

**After**:
```kotlin
class MyManager {
    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

// In Activity/Fragment:
override fun onDestroy() {
    super.onDestroy()
    MyManager.removeListener(this)
}
```

#### Fix 3: Coroutine Scope Leak

**Before**:
```kotlin
class MyViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO) // Leak!

    fun loadData() {
        scope.launch { /* ... */ }
    }
}
```

**After**:
```kotlin
class MyViewModel : ViewModel() {
    private val scope = viewModelScope // Auto-canceled

    fun loadData() {
        scope.launch { /* ... */ }
    }
}
```

---

## Performance Impact of LeakCanary

LeakCanary runs in debug builds only. It has minimal performance impact:

- **CPU**: < 1% during heap analysis
- **Memory**: ~10-20MB during heap dump
- **Battery**: Negligible impact
- **Apk Size**: ~200KB

**Note**: LeakCanary is NOT included in release builds.

---

## Current Status (2026-02-18)

| Component | Status | Notes |
|-----------|--------|-------|
| LeakCanary Setup | ✅ Complete | Added to app/build.gradle.kts |
| Initialization | ✅ Complete | Added to WordlandApplication |
| Testing Required | ⏳ Pending | Run manual leak detection |
| Known Leaks | 🔍 None found | Awaiting testing |

---

## Next Steps

1. **Run Manual Tests**: Execute the scenarios in this guide
2. **Document Findings**: Update this doc with any leaks found
3. **Fix Leaks**: Address any leaks that are discovered
4. **Regression Test**: Re-test after fixes

---

## Additional Resources

- [LeakCanary Documentation](https://square.github.io/leakcanary/)
- [Android Memory Best Practices](https://developer.android.com/topic/performance/memory)
- [Avoiding Memory Leaks](https://developer.android.com/training/articles/memory-leaks)
- [MAT (Memory Analyzer Tool)](https://eclipse.org/mat/)

---

**Document Owner**: android-performance-expert
**Last Updated**: 2026-02-18
**Status**: ✅ Setup Complete, Testing Pending
