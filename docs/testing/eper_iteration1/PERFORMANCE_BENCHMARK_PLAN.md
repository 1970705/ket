# Performance Benchmark Plan - Sprint 1

**Document Version**: 1.0
**Created**: 2026-02-20
**Author**: android-test-engineer
**Status**: Draft
**Sprint**: Sprint 1 (Epic #1 & Epic #2)

---

## 1. Executive Summary

This document outlines the comprehensive performance benchmark plan for Sprint 1, covering both Epic #1 (Visual Feedback Enhancement) and Epic #2 (Map System Reconstruction). The plan establishes performance baselines, defines testing methodologies, and sets acceptance criteria for all performance-related requirements.

### Performance Goals Summary

| Metric | Target | Epic #1 | Epic #2 |
|--------|--------|---------|---------|
| **Frame Rate** | 60fps (16.67ms/frame) | ✓ Critical | ✓ Critical |
| **Memory Increase** | <20MB | ✓ Primary | ≤5MB |
| **Animation Smoothness** | 100% jank-free | ✓ Primary | ✓ Primary |
| **Touch Response** | <50ms | ✓ Secondary | ✓ Secondary |
| **Total Memory** | <150MB peak | Baseline | Baseline |

---

## 2. Testing Infrastructure

### 2.1 Test Devices

**Primary Testing Device** (Development):
- Device: Xiaomi Redmi Note 11
- CPU: Snapdragon 680 (4x2.4GHz + 4x1.9GHz)
- RAM: 4GB
- GPU: Adreno 610
- Android: 12
- Screen: 90Hz FHD+
- **Rationale**: Mid-range device representing target user demographic

**Secondary Devices** (Compatibility):
| Device | CPU | RAM | GPU | Android | Purpose |
|--------|-----|-----|-----|---------|---------|
| Samsung Galaxy A53 | Exynos 1280 | 4GB | Mali-G68 | 13 | Samsung ecosystem |
| Google Pixel 6 | Tensor | 8GB | Mali-G78 | 14 | Stock Android |
| Redmi 9A | Helio G25 | 2GB | PowerVR GE8320 | 10 | Low-end baseline |
| Xiaomi 12T | Snapdragon 8+ Gen1 | 8GB | Adreno 730 | 13 | High-end reference |

### 2.2 Benchmark Tools

**Macrobenchmark Framework**:
```kotlin
// microbenchmark/build.gradle.kts
dependencies {
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.0")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-junit4:1.2.0")
}
```

**Profiling Tools**:
- Android Studio Profiler (CPU, Memory, GPU)
- Perfetto (system-wide tracing)
- GPU Inspector (rendering analysis)
- Macrobenchmark (startup, frame metrics)

### 2.3 CI/CD Integration

**GitHub Actions Performance Test**:
```yaml
name: Performance Benchmark
on:
  push:
    paths:
      - 'app/src/main/java/com/wordland/ui/components/**'
      - 'app/src/main/java/com/wordland/ui/screens/**'
  schedule:
    - cron: '0 6 * * *'  # Daily at 6 AM

jobs:
  benchmark:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Macrobenchmarks
        run: ./gradlew connectedCheck
      - name: Upload Results
        uses: actions/upload-artifact@v3
        with:
          name: benchmark-results
          path: app/build/outputs/
```

---

## 3. Epic #1: Visual Feedback Enhancement Benchmarks

### 3.1 Animation Frame Rate Tests

**Test Suite**: `SpellingAnimationBenchmark`

| Test ID | Test Scenario | Target FPS | Jank Allowance |
|---------|--------------|------------|----------------|
| EF1.1 | Single letter fly-in (no other animations) | 60 | 0% |
| EF1.2 | Sequential 6-letter word | 60 | 0% |
| EF1.3 | Rapid letter entry (5 letters/second) | 60 | ≤2% |
| EF1.4 | Letter fly-in + combo pulse (concurrent) | 60 | ≤5% |
| EF1.5 | Letter fly-in + progress bar update | 60 | ≤2% |
| EF1.6 | Letter fly-in + haptic feedback | 60 | 0% |

**Implementation**:
```kotlin
@RunWith(AndroidJUnit4::class)
class SpellingAnimationBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun letterFlyIn_sixLetters_consecutive() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(FrameTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()

            // Navigate to learning screen
            device.findObject(By.text("Look Island")).click()
            device.findObject(By.text("Level 1")).click()

            // Test letter entry
            repeat(6) { i ->
                device.pressKeyCode(KeyEvent.KEYCODE_A + i)
                device.waitForIdle(50)  // 50ms between letters
            }

            // Verify frame metrics
            val frameMetrics = FrameTimingMetric()
            assertTrue("Average frame time should be <16.67ms",
                frameMetricData.averageFrameTimeMs < 16.67)
        }
    }
}
```

### 3.2 Celebration Animation Benchmarks

**Test Suite**: `CelebrationAnimationBenchmark`

| Test ID | Test Scenario | Duration | Max Jank Frames |
|---------|--------------|----------|-----------------|
| EF2.1 | 3-star celebration (full confetti) | 1500ms | 0 |
| EF2.2 | 2-star celebration (partial confetti) | 1000ms | 0 |
| EF2.3 | 1-star celebration (single star) | 600ms | 0 |
| EF2.4 | Confetti + pet animation (concurrent) | 1200ms | ≤2 |
| EF2.5 | Back-to-back level completions | 2700ms | ≤3 |

**Particle System Performance**:
```kotlin
@Test
fun confettiParticle_count_impactOnFrameRate() {
    val particleCounts = listOf(50, 100, 150, 200, 250)

    particleCounts.forEach { count ->
        val frameTime = measureConfettiFrameTime(count)
        val fps = 1000.0 / frameTime

        assertWithMessage("Confetti with $count particles should maintain 60fps")
            .that(fps)
            .isAtLeast(55.0)  // Allow 5fps margin
    }
}

private fun measureConfettiFrameTime(particleCount: Int): Double {
    // Use Macrobenchmark to measure actual frame rendering time
    return benchmarkRule.measureRepeated {
        // Trigger confetti with specific particle count
        device.executeShellCommand("am broadcast -a com.wordland.TEST_CONFETTI --ei count $particleCount")
        device.waitForIdle(1500)  // Wait for animation
    }.singleFrameTimeMs.average
}
```

### 3.3 Combo Visual Effects Benchmarks

**Test Suite**: `ComboEffectsBenchmark`

| Test ID | Combo Level | Effects | Target FPS |
|---------|-------------|---------|------------|
| EF3.1 | 3-combo | Single flame + pulse | 60 |
| EF3.2 | 5-combo | Double flame + shake | 60 |
| EF3.3 | 10-combo | Triple flame + particles | 58 (min) |
| EF3.4 | 15-combo | All effects + screen glow | 55 (min) |
| EF3.5 | Combo transition (3→5→10) | Smooth progression | 60 |

**Continuous Combo Stress Test**:
```kotlin
@Test
fun comboStressTest_20CorrectAnswers_consecutive() {
    val frameTimes = mutableListOf<Double>()

    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(TraceSectionMetric("ComboAnimation"))
    ) {
        repeat(20) {
            // Simulate correct answer (triggers combo)
            device.findObject(By.res("word_input")).setText("apple")
            device.findObject(By.res("submit_btn")).click()
            device.waitForIdle(100)

            // Record frame time during combo animation
            val currentFrameTime = getCurrentFrameTime()
            frameTimes.add(currentFrameTime)
        }
    }

    val averageFps = 1000.0 / frameTimes.average()
    val minFps = 1000.0 / frameTimes.max()

    assertWithMessage("Average FPS during 20-combo should be ≥60")
        .that(averageFps).isAtLeast(60.0)

    assertWithMessage("Minimum FPS should not drop below 55")
        .that(minFps).isAtLeast(55.0)
}
```

### 3.4 Progress Bar Animation Benchmarks

| Test ID | Test Scenario | Animation Duration | Smoothness |
|---------|--------------|-------------------|------------|
| EF4.1 | 0% → 100% (single fill) | 350ms | 100% smooth |
| EF4.2 | Incremental updates (1% steps) | 6×50ms | 100% smooth |
| EF4.3 | Progress bar + percentage scroll | Concurrent | 100% smooth |
| EF4.4 | Rapid progress (level skip) | 100ms | No jank |

**Implementation**:
```kotlin
@Test
fun progressBarAnimation_smoothnessTest() {
    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(FrameTimingMetric())
    ) {
        // Trigger rapid progress updates
        repeat(100) { progress ->
            device.executeShellCommand(
                "am broadcast -a com.wordland.UPDATE_PROGRESS --ei progress $progress"
            )
            device.waitForIdle(10)  // 10ms between updates (aggressive)
        }

        // Check for frame drops
        val frameMetrics = FrameTimingMetric()
        val jankyFrames = frameMetricData.getOverCount(16.67)  // Frames >16.67ms

        assertWithMessage("Should have <5% janky frames")
            .that(jankyFrames.toFloat() / frameMetricData.totalFrames)
            .isLessThan(0.05f)
    }
}
```

### 3.5 Memory Impact Tests

**Test Suite**: `VisualFeedbackMemoryBenchmark`

| Component | Baseline | Epic #1 | Acceptance |
|-----------|----------|---------|------------|
| Animation Composables | ~5MB | ≤8MB | +3MB max |
| Particle System | ~0MB | ≤5MB | +5MB max |
| Combo Effects | ~0MB | ≤4MB | +4MB max |
| Total Epic #1 Impact | - | ≤20MB | **Strict** |

**Memory Profiling**:
```kotlin
@Test
fun visualFeedbackMemoryAllocation_levelComplete() {
    val baselineMemory = captureMemoryBaseline()

    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(
            MemoryAllocationMetric(),
            TraceSectionMetric("LevelCompleteAnimation")
        )
    ) {
        // Complete level with all visual effects
        completeLevelWithVisualEffects()

        // Force GC
        device.executeShellCommand("am broadcast -a com.wordland.FORCE_GC")
        device.waitForIdle(1000)
    }

    val peakMemoryIncrease = memoryMetricData.peakMemoryBytes - baselineMemory
    val retainedMemory = memoryMetricData.retainedMemoryBytes

    assertWithMessage("Peak memory increase should be <20MB")
        .that(peakMemoryIncrease).isLessThan(20 * 1024 * 1024)

    assertWithMessage("Retained memory after GC should be <5MB")
        .that(retainedMemory).isLessThan(5 * 1024 * 1024)
}
```

---

## 4. Epic #2: Map System Reconstruction Benchmarks

### 4.1 View Switch Performance

**Test Suite**: `MapViewSwitchBenchmark`

| Test ID | Transition | Duration | Jframes | GPU Usage |
|---------|-----------|----------|---------|-----------|
| MF1.1 | World → Island (no fog) | 300ms | 0 | <60% |
| MF1.2 | Island → World | 300ms | 0 | <60% |
| MF1.3 | World → Island (with fog) | 400ms | ≤1 | <70% |
| MF1.4 | Rapid toggle (5x) | 1500ms total | ≤2 | <65% avg |

**Implementation**:
```kotlin
@Test
fun worldIslandViewSwitch_frameTiming() {
    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(FrameTimingMetric()),
        iterations = 20
    ) {
        // Start on world map
        device.findObject(By.res("world_map")).waitUntilVisible(5000)

        // Measure world → island transition
        val startTime = System.nanoTime()
        device.findObject(By.res("view_toggle_btn")).click()
        device.waitForIdle(400)  // Wait for transition

        val frameMetrics = FrameTimingMetric()
        val transitionDuration = frameMetrics.lastFrameTimeMs - frameMetrics.firstFrameTimeMs

        assertWithMessage("Transition should complete in ≤400ms")
            .that(transitionDuration).isAtMost(400.0)

        // Verify no jank
        val jankyFrames = frameMetrics.getOverCount(16.67)
        assertWithMessage("Should have 0 janky frames")
            .that(jankyFrames).isEqualTo(0)
    }
}
```

### 4.2 Fog System Performance

**Test Suite**: `FogSystemBenchmark`

| Test ID | Fog Coverage | Reveal Duration | FPS During | GPU Memory |
|---------|--------------|-----------------|------------|------------|
| MF2.1 | Single region (15%) | 500ms | 60 | +2MB |
| MF2.2 | Multiple regions (50%) | 500ms | 60 | +5MB |
| MF2.3 | Full map fog reveal | 800ms | 58 (min) | +8MB |
| MF2.4 | Fog layer + ship animation | Concurrent | 60 | +6MB |

**Fog Rendering Performance**:
```kotlin
@Test
fun fogRevealAnimation_gpuMemoryImpact() {
    val baselineGpuMemory = captureGpuMemoryBaseline()

    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(
            FrameTimingMetric(),
            GpuMemoryMetric()
        )
    ) {
        // Trigger fog reveal
        device.findObject(By.res("locked_region")).click()
        device.waitForIdle(500)  // Wait for reveal animation

        val gpuMemory = GpuMemoryMetric()
        val memoryIncrease = gpuMemory.currentMemoryBytes - baselineGpuMemory

        assertWithMessage("GPU memory increase should be <10MB")
            .that(memoryIncrease).isLessThan(10 * 1024 * 1024)
    }
}
```

### 4.3 Player Ship Animation Benchmarks

| Test ID | Movement Type | Distance | Duration | FPS |
|---------|--------------|----------|----------|-----|
| MF3.1 | Region move (adjacent) | Short | 300ms | 60 |
| MF3.2 | Region move (distant) | Long | 500ms | 60 |
| MF3.3 | View switch + ship move | Combined | 400ms | 60 |
| MF3.4 | Continuous ship following | 10 moves | 3s | ≥58 |

**Ship Movement Smoothness**:
```kotlin
@Test
fun playerShipMovement_smoothnessTest() {
    val frameTimeSamples = mutableListOf<Double>()

    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(FrameTimingMetric())
    ) {
        // Simulate ship movement between regions
        val regions = listOf("look_island", "make_lake", "move_valley")

        regions.forEach { region ->
            device.findObject(By.res("region_$region")).click()

            // Capture frame times during movement
            repeat(10) {
                frameTimeSamples.add(getCurrentFrameTime())
                device.waitForIdle(30)  // Sample every 30ms
            }
        }
    }

    val stdDev = frameTimeSamples.standardDeviation()
    val maxFrameTime = frameTimeSamples.max()

    assertWithMessage("Frame time std dev should be <2ms (consistent timing)")
        .that(stdDev).isLessThan(2.0)

    assertWithMessage("Max frame time should be <20ms")
        .that(maxFrameTime).isLessThan(20.0)
}
```

### 4.4 Map System Memory Impact

| Component | Baseline | Epic #2 | Acceptance |
|-----------|----------|---------|------------|
| World View | ~15MB | ≤18MB | +3MB max |
| Fog Overlay | ~0MB | ≤8MB | +8MB max |
| Ship Animation | ~0MB | ≤2MB | +2MB max |
| Total Epic #2 Impact | - | ≤15MB | **Target** |

---

## 5. Cross-Epic Integration Benchmarks

### 5.1 Stress Test Scenarios

| Test ID | Scenario | Duration | Min FPS | Max Memory |
|---------|----------|----------|---------|------------|
| XF1.1 | Complete level + view switch | 2s | 55 | +30MB |
| XF2.1 | Rapid combo + fog reveal | Concurrent | 55 | +25MB |
| XF3.1 | Celebration + ship movement | Concurrent | 55 | +22MB |
| XF4.1 | All effects (worst case) | 3s | 50 | +35MB |

**Worst Case Scenario Test**:
```kotlin
@Test
fun worstCaseScenario_allEffects_concurrent() {
    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(
            FrameTimingMetric(),
            MemoryAllocationMetric(),
            GpuMemoryMetric()
        ),
        iterations = 5
    ) {
        // 1. Navigate to level (world → island view switch)
        device.findObject(By.res("world_map")).click()
        device.findObject(By.res("look_island")).click()
        device.waitForIdle(300)

        // 2. Build 10-combo during level
        repeat(10) {
            submitCorrectAnswer()
            device.waitForIdle(100)
        }

        // 3. Complete level with 3-star celebration
        submitFinalAnswer()  // Triggers celebration
        device.waitForIdle(1200)

        // 4. Fog reveal happens simultaneously
        // (level completion unlocks adjacent region)

        // 5. Ship moves to new region
        device.waitForIdle(300)

        // Check all metrics
        val avgFps = 1000.0 / FrameTimingMetric().averageFrameTimeMs
        val totalMemoryIncrease = MemoryAllocationMetric().peakMemoryBytes

        assertWithMessage("Worst case FPS should be ≥50")
            .that(avgFps).isAtLeast(50.0)

        assertWithMessage("Worst case memory increase should be <40MB")
            .that(totalMemoryIncrease).isLessThan(40 * 1024 * 1024)
    }
}
```

---

## 6. Performance Regression Detection

### 6.1 Baseline Establishment

**Pre-Sprint 1 Baseline** (to be measured):

| Metric | Value | Measurement Method |
|--------|-------|-------------------|
| App Startup (Cold) | ~800ms | Macrobenchmark |
| App Startup (Warm) | ~300ms | Macrobenchmark |
| Frame Rate (Learning) | 60fps | FrameTimingMetric |
| Frame Rate (World Map) | 60fps | FrameTimingMetric |
| Memory (Idle) | ~95MB | MemoryAllocationMetric |
| Memory (Peak) | ~125MB | MemoryAllocationMetric |
| APK Size | ~8.4MB | Build output |

### 6.2 Regression Thresholds

| Metric | Warning | Critical | Action |
|--------|---------|----------|--------|
| Frame Rate | <58fps | <55fps | Stop, investigate |
| Memory Increase | +15MB | +25MB | Stop, investigate |
| Animation Duration | +20% | +50% | Review, optimize |
| Startup Time | +10% | +25% | Review, optimize |

### 6.3 Continuous Monitoring

**Weekly Performance Reports**:
```markdown
## Performance Report - Week of [Date]

### Summary
- [ ] All benchmarks passed
- [ ] No regressions detected
- [ ] New baseline established

### Detailed Results
[Automatically generated from CI/CD]

### Action Items
[Any performance issues to address]
```

---

## 7. Device-Specific Testing

### 7.1 Low-End Device (Redmi 9A)

**Adjusted Expectations**:
- Frame Rate: ≥55fps (acceptable), ≥50fps (minimum)
- Animation Duration: +20% allowed
- Fog Quality: Reduced resolution enabled
- Particle Count: Capped at 100 (vs 250)

**Quality Scaling Implementation**:
```kotlin
// DeviceCapabilities.kt
fun getAnimationQualityLevel(): AnimationQuality {
    return when {
        hasLowEndDevice() -> AnimationQuality.REDUCED
        hasMidRangeDevice() -> AnimationQuality.BALANCED
        else -> AnimationQuality.HIGH
    }
}

// Visual Feedback Components
@Composable
fun ConfettiEffect(
    starCount: Int,
    quality: AnimationQuality = DeviceCapabilities.getAnimationQualityLevel()
) {
    val particleCount = when (quality) {
        AnimationQuality.REDUCED -> min(starCount * 20, 100)
        AnimationQuality.BALANCED -> starCount * 30
        AnimationQuality.HIGH -> starCount * 50
    }
    // ...
}
```

### 7.2 High-Refresh Devices (90Hz/120Hz)

**Adaptive Frame Rate**:
```kotlin
@Composable
fun rememberFrameRateSpec(): FrameRateSpec {
    val displayMode = LocalDisplayMode.current
    return FrameRateSpec(
        targetFps = displayMode.refreshRate,
        allowFramePacing = true,
        vsyncEnabled = true
    )
}
```

---

## 8. Testing Schedule

### Week 1: Baseline & Setup (Day 2-3)
- [ ] Establish pre-Sprint 1 baseline
- [ ] Set up Macrobenchmark project
- [ ] Configure CI/CD performance tests
- [ ] Test infrastructure validation

### Week 2: Epic #1 Benchmarks (Day 4-6)
- [ ] Animation frame rate tests
- [ ] Celebration animation tests
- [ ] Combo effects tests
- [ ] Progress bar tests
- [ ] Memory impact tests

### Week 2-3: Epic #2 Benchmarks (Day 7-9)
- [ ] View switch tests
- [ ] Fog system tests
- [ ] Ship animation tests
- [ ] Map memory tests

### Week 3: Integration & Regression (Day 10-12)
- [ ] Cross-epic integration tests
- [ ] Stress tests
- [ ] Device-specific tests
- [ ] Regression verification

### Week 3: Final Validation (Day 13-14)
- [ ] All benchmarks passed
- [ ] Performance report generated
- [ ] Sprint 1 performance summary

---

## 9. Success Criteria

### Epic #1 (Visual Feedback Enhancement)

| Criterion | Target | Measurement |
|-----------|--------|-------------|
| ✅ Frame Rate | 60fps | FrameTimingMetric |
| ✅ Memory Impact | <20MB | MemoryAllocationMetric |
| ✅ Animation Duration | ±10% spec | Manual timing |
| ✅ Jank-free | 100% | FrameTimingMetric |
| ✅ Low-end Compatible | ≥55fps | Device testing |

### Epic #2 (Map System Reconstruction)

| Criterion | Target | Measurement |
|-----------|--------|-------------|
| ✅ Frame Rate | 60fps | FrameTimingMetric |
| ✅ Memory Impact | <15MB | MemoryAllocationMetric |
| ✅ View Switch | ≤400ms | Manual timing |
| ✅ Fog Reveal | ≤500ms | Spec compliance |
| ✅ Smooth Ship Move | <20ms/frame | FrameTimingMetric |

### Overall Sprint 1

| Criterion | Target | Status |
|-----------|--------|--------|
| ✅ All P0 performance tests passed | 100% | Pending |
| ✅ No critical regressions | 0 | Pending |
| ✅ Baseline established | ✅ | Pending |
| ✅ CI/CD integrated | ✅ | Pending |

---

## 10. Appendix: Code Templates

### A. Frame Timing Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class [FeatureName]Benchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun [testName]() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(FrameTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()

            // [Test steps here]

            device.waitForIdle([animation_duration_ms])
        }
    }
}
```

### B. Memory Test Template

```kotlin
@Test
fun [featureName]_memoryImpact() {
    val baselineMemory = captureMemoryBaseline()

    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(MemoryAllocationMetric())
    ) {
        // [Execute feature]
        // [Force GC if needed]
        device.executeShellCommand("am broadcast -a com.wordland.FORCE_GC")
        device.waitForIdle(1000)
    }

    val memoryIncrease = memoryMetricData.peakMemoryBytes - baselineMemory

    assertWithMessage("[assertion_message]")
        .that(memoryIncrease).isLessThan([max_bytes])
}
```

### C. GPU Memory Capture

```kotlin
fun captureGpuMemoryBaseline(): Long {
    // Dump GPU memory profile
    device.executeShellCommand("dumpsys gfxinfo com.wordland reset")
    device.executeShellCommand("dumpsys gfxinfo com.wordland")

    // Parse output for GPU memory
    val gpuInfo = device.executeShellCommand("dumpsys SurfaceFlinger --latency 'SurfaceView'")
    return parseGpuMemoryFromDump(gpuInfo)
}
```

---

**Document Control**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-20 | android-test-engineer | Initial creation |

**Next Review**: After Sprint 1 completion (2026-03-06)
**Owner**: android-test-engineer
**Approvers**: android-architect, android-performance-expert
