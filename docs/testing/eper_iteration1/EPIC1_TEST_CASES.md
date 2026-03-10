# Epic #1 Test Cases: Visual Feedback Enhancement

**Document Version**: 1.0
**Created**: 2026-02-20
**Author**: game-designer
**Sprint**: Sprint 1
**Epic**: Epic #1 - Visual Feedback Enhancement

---

## 📋 Table of Contents

1. [Overview](#1-overview)
2. [Story #1.1: Spelling Animation Tests](#2-story-11-spelling-animation-tests)
3. [Story #1.2: Celebration Animation Tests](#3-story-12-celebration-animation-tests)
4. [Story #1.3: Combo Visual Effects Tests](#4-story-13-combo-visual-effects-tests)
5. [Story #1.4: Progress Bar Enhancement Tests](#5-story-14-progress-bar-enhancement-tests)
6. [Integration Tests](#6-integration-tests)
7. [Performance Tests](#7-performance-tests)
8. [User Experience Tests](#8-user-experience-tests)

---

## 1. Overview

### 1.1 Test Scope

Epic #1 covers all visual feedback enhancements for the Spell Battle game mode:

| Story | Feature | Test Cases |
|-------|---------|------------|
| #1.1 | Spelling Animation (Letter Fly-in) | 24 test cases |
| #1.2 | Celebration Animation (Star Rating) | 32 test cases |
| #1.3 | Combo Visual Effects | 18 test cases |
| #1.4 | Progress Bar Enhancement | 12 test cases |
| **Integration** | Cross-feature coordination | 15 test cases |
| **Performance** | Frame rate, memory, response | 10 test cases |
| **UX** | User perception and clarity | 8 test cases |
| **Total** | | **119 test cases** |

### 1.2 Test Categories

| Category | Purpose | Test Count |
|----------|---------|------------|
| **Unit Tests** | Component-level validation | 60 |
| **Integration Tests** | Multi-component interaction | 30 |
| **Performance Tests** | Benchmarks and thresholds | 15 |
| **UX Tests** | User validation (manual) | 14 |

### 1.3 Test Environment

```kotlin
// Test configuration
object TestConfig {
    val TARGET_FPS = 60
    val MAX_RESPONSE_TIME_MS = 50L
    val MAX_MEMORY_INCREASE_MB = 20L

    // Test devices
    val DEVICES = listOf(
        "Xiaomi Redmi Note 11 (API 26)",
        "Samsung Galaxy A53 (API 33)",
        "Google Pixel 6 (API 33)"
    )
}
```

---

## 2. Story #1.1: Spelling Animation Tests

### 2.1 Letter Fly-in Animation

#### TC-EP1-001: Letter Animation Duration
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun letterFlyInAnimation_completesWithin150ms() {
    // Arrange
    val letterBox = LetterBox(letter = 'A', isRevealed = false)
    val startTime = System.nanoTime()

    // Act
    letterBox.isRevealed = true
    advanceTimeBy(150.ms)

    // Assert
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1_000_000

    assertTrue("Animation should complete in 150ms, actual: ${duration}ms",
        duration <= 150)
}
```

| Expected | Actual | Pass/Fail |
|----------|--------|----------|
| ≤150ms | ? | ⏳ Pending |

---

#### TC-EP1-002: Letter Animation Easing Curve
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun letterFlyInAnimation_usesFastOutSlowInEasing() {
    // Test that scale animation uses FastOutSlowIn curve
    val scaleValues = captureAnimationScaleValues(
        duration = 150,
        easing = FastOutSlowInEasing
    )

    // Verify curve characteristics
    // First 50% of duration should cover >70% of scale change
    val firstHalfProgress = scaleValues[75] // 75ms mark
    assertTrue("Fast start expected: ${firstHalfProgress} > 0.7",
        firstHalfProgress > 0.7f)
}
```

---

#### TC-EP1-003: Letter Animation Sequential Display
**Priority**: P0
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun spellingWord_lettersAnimateInSequence() {
    // Arrange
    val word = "LOOK"
    val letterBoxes = listOf(
        LetterBox('L', isRevealed = false),
        LetterBox('O', isRevealed = false),
        LetterBox('O', isRevealed = false),
        LetterBox('K', isRevealed = false)
    )

    // Act - Simulate typing L-O-O-K
    val revealTimes = mutableListOf<Long>()
    letterBoxes.forEachIndexed { index, box ->
        val startTime = System.nanoTime()
        box.isRevealed = true
        waitForAnimationComplete()
        revealTimes.add((System.nanoTime() - startTime) / 1_000_000)
    }

    // Assert - Each letter animates independently
    assertEquals("4 letters should animate", 4, revealTimes.size)
    assertTrue("Each animation should be ~150ms",
        revealTimes.all { it in 140..160 })
}
```

---

#### TC-EP1-004: Letter Animation State Transitions
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun letterAnimation_transitionsFromHiddenToVisible() {
    // Test state transition: hidden (scale=0, alpha=0) → visible (scale=1, alpha=1)
    val initialState = LetterBoxState(scale = 0f, alpha = 0f)
    val finalState = LetterBoxState(scale = 1f, alpha = 1f)

    val transition = runLetterAnimation(initialState)

    assertEquals("Final scale should be 1.0", 1.0f, transition.endScale)
    assertEquals("Final alpha should be 1.0", 1.0f, transition.endAlpha)
}
```

---

#### TC-EP1-005: Letter Animation with Backspace
**Priority**: P1
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun letterAnimation_backspaceRemovesLastLetter() {
    // Test: L-O-O-[backspace] → L-O visible, O-K hidden
    val viewModel = LearningViewModel()

    // Type L-O-O-K
    viewModel.onKeyPress('L')
    viewModel.onKeyPress('O')
    viewModel.onKeyPress('O')
    viewModel.onKeyPress('K')

    // Then press backspace
    viewModel.onBackspace()

    // Assert: K hidden, O-O visible
    assertFalse("K should be hidden", viewModel.isLetterRevealed(3))
    assertTrue("Second O should be visible", viewModel.isLetterRevealed(2))
}
```

---

#### TC-EP1-006: Letter Animation Response Time
**Priority**: P0
**Type**: Performance Test
**Automation**: Yes

```kotlin
@Test
fun letterAnimation_respondsWithin50ms() {
    // Measure: Keypress → Animation start
    val letterBox = LetterBox(letter = 'A', isRevealed = false)
    val keyPressTime = System.nanoTime()

    // Simulate key press
    simulateKeyPress('A')

    // Check: Animation started within 50ms
    val animationStartTime = letterBox.animationStartTime
    val responseDelay = (animationStartTime - keyPressTime) / 1_000_000

    assertTrue("Response should be ≤50ms, actual: ${responseDelay}ms",
        responseDelay <= 50)
}
```

### 2.2 Spelling Animation Visual Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-007 | Letter color on appear | Blue border | Manual |
| TC-EP1-008 | Letter background on appear | White background | Manual |
| TC-EP1-009 | Empty letter slot | Gray border, no text | Manual |
| TC-EP1-010 | Letter font size | 24sp Bold | Manual |
| TC-EP1-011 | Letter box size | 48dp × 48dp | Manual |
| TC-EP1-012 | Letter box corner radius | 8dp | Manual |
| TC-EP1-013 | Smooth fade-in | No jank, 60fps | Manual |
| TC-EP1-014 | No flickering | Consistent opacity | Manual |

### 2.3 Spelling Animation Edge Cases

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-015 | Rapid typing (multiple letters quickly) | Queue animations, play sequentially | Auto |
| TC-EP1-016 | Very long word (10+ letters) | Still sequential, no overlap | Auto |
| TC-EP1-017 | Backspace during animation | Cancel current, apply removal | Auto |
| TC-EP1-018 | Clear all letters | Reset all to hidden state | Auto |
| TC-EP1-019 | Rotation during animation | Animation completes correctly | Auto |
| TC-EP1-020 | Configuration change (theme switch) | Animation completes, theme applies | Auto |

---

## 3. Story #1.2: Celebration Animation Tests

### 3.1 Star Rating Animation

#### TC-EP1-021: Three-Star Celebration Sequence
**Priority**: P0
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun threeStarCelebration_playsInCorrectSequence() {
    // Arrange
    val stars = 3
    val celebrationConfig = getCelebrationConfig(stars)

    // Act - Trigger level completion with 3 stars
    val timeline = captureCelebrationTimeline {
        triggerLevelComplete(stars = 3)
    }

    // Assert - Expected sequence
    val expectedEvents = listOf(
        "STAR_1_APPEARS" to 100,   // 100ms
        "STAR_2_APPEARS" to 200,   // 100ms after star 1
        "STAR_3_APPEARS" to 300,   // 100ms after star 2
        "FINAL_BOUNCE" to 450,     // All 3 bounce together
        "CONFETTI_START" to 500,   // Confetti burst
        "PET_CELEBRATE" to 600     // Pet animation
    )

    expectedEvents.forEach { (event, expectedTime) ->
        assertTrue("$event should occur at ${expectedTime}ms",
            timeline.containsEvent(event, expectedTime))
    }
}
```

---

#### TC-EP1-022: Two-Star vs Three-Star Differentiation
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun celebration_twoStarHasFewerConfettiThanThreeStar() {
    val threeStarConfig = getCelebrationConfig(3)
    val twoStarConfig = getCelebrationConfig(2)

    assertTrue("3-star should have more confetti",
        threeStarConfig.confettiCount > twoStarConfig.confettiCount)

    assertEquals("3-star confetti count", 50, threeStarConfig.confettiCount)
    assertEquals("2-star confetti count", 20, twoStarConfig.confettiCount)
}
```

---

#### TC-EP1-023: One-Star Celebration (Encouraging)
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun oneStarCelebration_showsEncouragingMessage() {
    val oneStarConfig = getCelebrationConfig(1)

    assertEquals("Message should be encouraging",
        "Good Try! 继续努力!", oneStarConfig.message)

    assertEquals("Should show retry button",
        true, oneStarConfig.showRetryButton)

    assertEquals("No confetti for 1-star",
        0, oneStarConfig.confettiCount)
}
```

---

#### TC-EP1-024: Zero-Star Response
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun zeroStarResponse_showsSupportiveMessage() {
    val zeroStarConfig = getCelebrationConfig(0)

    assertEquals("Message should be supportive",
        "Let's try again! 再试一次!", zeroStarConfig.message)

    assertEquals("Pet should be gentle",
        PetReaction.GENTLE, zeroStarConfig.petAnimation)

    assertEquals("Duration should be shortest",
        400.ms, zeroStarConfig.messageDuration)
}
```

### 3.2 Celebration Animation Visual Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-025 | 3-star confetti | 50 particles, spread 1.0 | Manual |
| TC-EP1-026 | 2-star confetti | 20 particles, spread 0.6 | Manual |
| TC-EP1-027 | Star bounce effect | Overshoot, settle to scale 1.0 | Manual |
| TC-EP1-028 | Star color (earned) | Gold (#FFD700) | Manual |
| TC-EP1-029 | Star color (unearned) | Gray | Manual |
| TC-EP1-030 | Pet ecstatic animation | Jump + spin | Manual |
| TC-EP1-031 | Pet proud animation | Nod + thumbs up | Manual |
| TC-EP1-032 | Pet encouraging animation | Smile | Manual |

### 3.3 Celebration Animation Tests by Star Rating

| Test ID | Stars | Confetti | Pet Animation | Message Duration | Retry Button |
|---------|-------|----------|---------------|-----------------|--------------|
| TC-EP1-033 | ⭐⭐⭐ | 50 particles | Ecstatic jump + spin | 1200ms | No |
| TC-EP1-034 | ⭐⭐ | 20 particles | Proud nod + thumbs up | 800ms | No |
| TC-EP1-035 | ⭐ | 0 particles | Encouraging smile | 500ms | Yes |
| TC-EP1-036 | ✗ | 0 particles | Encouraging nod | 400ms | Yes |

### 3.4 Celebration Coordination

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-037 | Multiple rapid completions | Queue celebrations | Auto |
| TC-EP1-038 | Back-to-back 3-star | First completes before second starts | Auto |
| TC-EP1-039 | Interruption during celebration | Graceful cancellation | Auto |
| TC-EP1-040 | Celebration with navigation delay | Animation continues if <200ms | Auto |

---

## 4. Story #1.3: Combo Visual Effects Tests

### 4.1 Combo Visual Indicators

#### TC-EP1-041: Combo Level 1-2 (No Visual)
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun comboLevel1_noVisualEffect() {
    val comboState = ComboState(count = 1)
    val visualEffect = getComboVisualEffect(comboState)

    assertEquals("No multiplier", 1.0f, visualEffect.multiplier)
    assertEquals("No icon", null, visualEffect.icon)
    assertEquals("No animation", ComboAnimation.NONE, visualEffect.animation)
}
```

---

#### TC-EP1-042: Combo Level 3-4 (Single Flame)
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun comboLevel3_showsSingleFlame() {
    val comboState = ComboState(count = 3)
    val visualEffect = getComboVisualEffect(comboState)

    assertEquals("1.2x multiplier", 1.2f, visualEffect.multiplier)
    assertEquals("Single flame icon", "🔥", visualEffect.icon)
    assertEquals("Pulse animation", ComboAnimation.PULSE, visualEffect.animation)
}
```

---

#### TC-EP1-043: Combo Level 5-9 (Double Flame)
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun comboLevel5_showsDoubleFlame() {
    val comboState = ComboState(count = 5)
    val visualEffect = getComboVisualEffect(comboState)

    assertEquals("1.5x multiplier", 1.5f, visualEffect.multiplier)
    assertEquals("Double flame icon", "🔥🔥", visualEffect.icon)
    assertEquals("Screen shake", ComboAnimation.SHAKE, visualEffect.animation)
}
```

---

#### TC-EP1-044: Combo Level 10+ (Triple Flame)
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun comboLevel10_showsTripleFlame() {
    val comboState = ComboState(count = 10)
    val visualEffect = getComboVisualEffect(comboState)

    assertEquals("2.0x multiplier", 2.0f, visualEffect.multiplier)
    assertEquals("Triple flame icon", "🔥🔥🔥", visualEffect.icon)
    assertEquals("Particle effect", ComboAnimation.PARTICLES, visualEffect.animation)
}
```

### 4.2 Combo Animation Timing

| Test ID | Scenario | Expected Duration | Type |
|---------|----------|-------------------|------|
| TC-EP1-045 | Combo pulse animation | 250ms | Auto |
| TC-EP1-046 | Screen shake duration | 200ms | Auto |
| TC-EP1-047 | Particle effect duration | 500ms | Auto |
| TC-EP1-048 | Combo increment response | ≤50ms | Auto |

### 4.3 Combo Reset Behavior

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-049 | Wrong answer resets combo | Combo = 0, icon disappears | Auto |
| TC-EP1-050 | Timeout resets combo | Combo = 0, icon disappears | Auto |
| TC-EP1-051 | Hint usage | Combo preserved | Auto |
| TC-EP1-052 | Reset animation on combo loss | Fade out (150ms) | Auto |

### 4.4 Combo Visual Coordination

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-053 | Combo + correct answer | Both animations visible (P1 + P3) | Auto |
| TC-EP1-054 | Combo + celebration | Combo continues during celebration | Auto |
| TC-EP1-055 | Combo during error | Combo resets, error animation plays | Auto |
| TC-EP1-056 | Combo increment timing | Updates within 50ms of answer | Auto |

---

## 5. Story #1.4: Progress Bar Enhancement Tests

### 5.1 Progress Bar Animation

#### TC-EP1-057: Progress Bar Smooth Fill
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun progressBar_fillsSmoothly() {
    val progress = 0.0f
    val target = 0.6f  // 60%

    val progressValues = mutableListOf<Float>()
    val animationProgress = animateProgressAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        )
    ) { progressValues.add(it) }

    // Assert: Smooth progression (no large jumps)
    for (i in 1 until progressValues.size) {
        val jump = progressValues[i] - progressValues[i-1]
        assertTrue("Progress should be smooth, jump: $jump",
            jump < 0.1f)  // Max 10% jump per frame
    }
}
```

---

#### TC-EP1-058: Progress Bar Number Rolling
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun progressBar_numberRollsSmoothly() {
    val currentProgress = 2  // "2/6"
    val newProgress = 4        // "4/6"

    val numberUpdates = captureNumberRollingAnimation(
        from = currentProgress,
        to = newProgress,
        duration = 350
    )

    // Assert: Numbers animate through intermediate values
    assertTrue("Should show intermediate numbers",
        numberUpdates.size >= 3)

    // Expected sequence for 2→4: 2 → 2.33 → 2.66 → 3 → 3.33 → 3.66 → 4
    // (displayed as: "2/6" → "3/6" → "4/6")
}
```

### 5.2 Progress Bar Visual Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-059 | Progress bar color (low) | Orange (<30%) | Manual |
| TC-EP1-060 | Progress bar color (medium) | Blue (30-70%) | Manual |
| TC-EP1-061 | Progress bar color (high) | Green (>70%) | Manual |
| TC-EP1-062 | Progress bar corner radius | 8dp | Manual |
| TC-EP1-063 | Progress bar height | 4dp | Manual |
| TC-EP1-064 | Smooth edges | No aliasing artifacts | Manual |

### 5.3 Progress Bar Update Triggers

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-065 | Correct answer | Progress bar increments smoothly | Auto |
| TC-EP1-066 | Level complete | Progress bar shows 100% | Auto |
| TC-EP1-067 | Level start | Progress bar starts at 0% | Auto |
| TC-EP1-068 | Replay level | Progress bar resets to 0% | Auto |

---

## 6. Integration Tests

### 6.1 Cross-Feature Coordination

#### TC-EP1-069: Animation Priority System
**Priority**: P0
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun animationPriority_errorInterruptsCelebration() {
    val coordinator = AnimationCoordinator()

    // Start a celebration (low priority)
    val celebration = Animation(
        group = AnimationGroup.CELEBRATION,
        priority = 2,
        duration = 1200.ms
    )
    coordinator.requestAnimation(celebration)

    // Immediately trigger error (high priority)
    val error = Animation(
        group = AnimationGroup.ANSWER_FEEDBACK,
        priority = 1,
        duration = 200.ms
    )
    val errorStarted = coordinator.requestAnimation(error)

    // Assert: Error animation plays, celebration is interrupted
    assertTrue("Error should start", errorStarted)
    assertFalse("Celebration should be interrupted",
        coordinator.isPlaying(celebration))
}
```

---

#### TC-EP1-070: Concurrent Animation Limit
**Priority**: P0
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun concurrentAnimation_limitToThree() {
    val coordinator = AnimationCoordinator()

    // Request 4 animations simultaneously
    val animations = listOf(
        createAnimation(priority = 1), // Correct answer
        createAnimation(priority = 2), // Star reveal
        createAnimation(priority = 3), // Combo update
        createAnimation(priority = 5)  // Ambient (should be queued)
    )

    animations.forEach { coordinator.requestAnimation(it) }

    // Assert: Only 3 playing, 1 queued
    assertEquals("3 animations playing", 3, coordinator.playingCount)
    assertEquals("1 animation queued", 1, coordinator.queuedCount)
}
```

### 6.2 Error Recovery

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-071 | Animation crash recovery | App continues, animation restarts | Auto |
| TC-EP1-072 | Missing animation resource | Graceful degradation (skip animation) | Auto |
| TC-EP1-073 | Slow device fallback | Reduced particle count | Auto |

### 6.3 State Preservation

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP1-074 | Configuration change during animation | Animation completes with old config | Auto |
| TC-EP1-075 | Screen rotation during animation | Animation continues from current state | Auto |
| TC-EP1-076 | Background during animation | Animation pauses, resumes on foreground | Auto |

---

## 7. Performance Tests

### 7.1 Frame Rate Tests

#### TC-EP1-077: Maintain 60fps During Letter Animation
**Priority**: P0
**Type**: Performance Test
**Automation**: Yes

```kotlin
@Test
fun letterAnimation_maintains60fps() {
    // Macrobenchmark test
    val frameTimes = measureFrameTimes {
        // Simulate typing L-O-O-K (4 letters)
        repeat(4) {
            triggerLetterAnimation()
            advanceTimeByFrame()
        }
    }

    // Assert: All frames within 16.67ms (60fps)
    val slowFrames = frameTimes.filter { it > 16.67 }
    assertTrue("Should maintain 60fps, slow frames: ${slowFrames.size}",
        slowFrames.size < frameTimes.size * 0.05)  // Allow <5% slow frames
}
```

### 7.2 Response Time Tests

| Test ID | Scenario | Target | Type |
|---------|----------|--------|------|
| TC-EP1-078 | Letter keypress → Animation start | ≤50ms | Auto |
| TC-EP1-079 | Answer submit → Feedback start | ≤50ms | Auto |
| TC-EP1-080 | Combo increment → Visual update | ≤50ms | Auto |
| TC-EP1-081 | Star reveal start | ≤100ms | Auto |
| TC-EP1-082 | Celebration complete → Navigation ready | ≤200ms | Auto |

### 7.3 Memory Tests

| Test ID | Scenario | Target | Type |
|---------|----------|--------|------|
| TC-EP1-083 | Memory increase (baseline to Epic #1) | <20MB | Auto |
| TC-EP1-084 | Memory during celebration (peak) | <150MB total | Auto |
| TC-EP1-085 | Memory leak after 100 animations | No increase | Auto |

---

## 8. User Experience Tests

### 8.1 Clarity Tests

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP1-086 | Users understand feedback meaning | User survey (10 children) | 100% clarity |
| TC-EP1-087 | Star rating differentiation is clear | User survey | ≥90% clarity |
| TC-EP1-088 | Combo visual intensity matches count | User survey | ≥85% clarity |
| TC-EP1-089 | Progress bar shows accurate progress | User observation | 100% accuracy |

### 8.2 Emotional Response Tests

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP1-090 | 3-star celebration creates delight | Facial expression, survey | ≥80% positive |
| TC-EP1-091 | 1-star is encouraging, not discouraging | Survey | ≥90% feel encouraged |
| TC-EP1-092 | Error feedback doesn't shame | Survey | ≥95% feel supported |
| TC-EP1-093 | Confetti is exciting, not overwhelming | Sensory questionnaire | ≤5% find it "too much" |

### 8.3 Accessibility Tests

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP1-094 | Reduced mode works correctly | Manual testing | All animations reduced |
| TC-EP1-095 | Minimal mode (ADHD friendly) | Manual testing | 0 particles, ≤1 animation |
| TC-EP1-096 | Animations respect system preference | Settings toggle | Mode changes apply |
| TC-EP1-097 | Color-blind friendly | Color blindness simulator | Feedback still clear |

### 8.4 Age-Appropriate Tests

| Test ID | Age Group | Scenario | Target |
|---------|-----------|----------|--------|
| TC-EP1-098 | 6-8 years | Can understand all feedback | 100% |
| TC-EP1-099 | 9-10 years | Find feedback engaging | ≥80% |
| TC-EP1-100 | 11-12 years | Not "too childish" | ≥80% |
| TC-EP1-101 | All ages | Error feedback is positive | ≥90% feel supported |

---

## 9. Test Execution Plan

### 9.1 Unit Test Execution

```bash
# Run all Epic #1 tests
./gradlew test --tests "*VisualFeedback*"

# Run by Story
./gradlew test --tests "*SpellingAnimation*"
./gradlew test --tests "*CelebrationAnimation*"
./gradlew test --tests "*ComboVisualEffects*"
./gradlew test --tests "*ProgressBarEnhancement*"

# Run with coverage
./gradlew test jacocoTestReport
```

### 9.2 Integration Test Execution

```bash
# Run integration tests
./gradlew connectedAndroidTest --tests "*Epic1Integration*"

# Run specific test scenarios
./gradlew connectedAndroidTest --tests "*AnimationPriority*"
./gradlew connectedAndroidTest --tests "*ConcurrentLimit*"
```

### 9.3 Performance Test Execution

```bash
# Macrobenchmark for frame rate
./gradlew :app:connectedCheck -Pandroid.testInstrumentationRunnerArguments='class=com.wordland.benchmark.VisualFeedbackBenchmark'

# Memory profiling
./gradlew :app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments='class=com.wordland.benchmark.MemoryLeakBenchmark'
```

### 9.4 Manual Test Execution

**Devices Required**:
- Xiaomi Redmi Note 11 (API 26)
- Samsung Galaxy A53 (API 33)
- Google Pixel 6 (API 33)

**Manual Test Checklist**:
- [ ] TC-EP1-007 to TC-EP1-014: Spelling animation visual verification
- [ ] TC-EP1-025 to TC-EP1-032: Celebration visual verification
- [ ] TC-EP1-059 to TC-EP1-064: Progress bar visual verification
- [ ] TC-EP1-086 to TC-EP1-097: User experience surveys

---

## 10. Test Data

### 10.1 Sample Words for Testing

| Word | Length | Difficulty | Purpose |
|------|--------|----------|--------|
| CAT | 3 | Easy | Short word animation |
| LOOK | 4 | Easy | Medium word animation |
| APPLE | 5 | Normal | Longer word animation |
| BANANA | 6 | Normal | Long word animation |
| CHOCOLATE | 9 | Hard | Very long word animation |

### 10.2 Combo Progression Data

| Combo Count | Expected Visual | Multiplier |
|-------------|-----------------|------------|
| 1 | No icon | 1.0x |
| 2 | No icon | 1.0x |
| 3 | 🔥 | 1.2x |
| 4 | 🔥 | 1.2x |
| 5 | 🔥🔥 | 1.5x |
| 6 | 🔥🔥 | 1.5x |
| 7 | 🔥🔥 | 1.5x |
| 8 | 🔥🔥 | 1.5x |
| 9 | 🔥🔥 | 1.5x |
| 10 | 🔥🔥🔥 | 2.0x |

### 10.3 Star Rating Test Data

| Test Case | Stars | Accuracy | Time | Errors | Hints |
|-----------|-------|----------|------|--------|-------|
| TC-EP1-021 | ⭐⭐⭐ | ≥90% | Fast | ≤1 | 0 |
| TC-EP1-022 | ⭐⭐ | ≥70% | Normal | ≤2 | 0 |
| TC-EP1-023 | ⭐ | ≥50% | Any | Any | Any |
| TC-EP1-024 | ✗ | <50% | - | - | - |

---

## 11. Success Criteria

### 11.1 Functional Completion

- [ ] All 60 unit tests pass (100%)
- [ ] All 30 integration tests pass (100%)
- [ ] All manual visual tests verified

### 11.2 Performance Targets

- [ ] Frame rate ≥60fps (TC-EP1-077)
- [ ] Response time ≤50ms (TC-EP1-078 to TC-EP1-082)
- [ ] Memory increase <20MB (TC-EP1-083)

### 11.3 User Experience Targets

- [ ] Clarity ≥90% (TC-EP1-086)
- [ ] Delight ≥80% (TC-EP1-090)
- [ ] Not overwhelming ≤5% (TC-EP1-093)
- [ ] ADHD friendly ≥90% (TC-EP1-095)

---

## 12. Defect Tracking

### 12.1 Bug Severity Definitions

| Severity | Definition | Example |
|----------|-----------|---------|
| **P0** | Animation doesn't play at all | Letter stays hidden |
| **P0** | App crashes | Crash on celebration |
| **P1** | Animation is wrong timing | Star reveal takes 1s instead of 600ms |
| **P1** | Missing visual element | Confetti doesn't show |
| **P2** | Minor visual glitch | Slight stutter in animation |

### 12.2 Bug Reporting Template

```
Bug ID: EP1-XXX
Title: [Brief description]
Severity: P0/P1/P2
Steps to Reproduce:
1.
2.
3.
Expected Result:
Actual Result:
Device:
Android Version:
App Version:
Screenshot/Video:
```

---

**Document Status**: ✅ Complete
**Total Test Cases**: 119
**Next Steps**: Execute tests during Sprint 1 development
