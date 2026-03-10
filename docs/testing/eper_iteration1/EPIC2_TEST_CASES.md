# Epic #2 Test Cases: Map System Reconstruction

**Document Version**: 1.0
**Created**: 2026-02-20
**Author**: game-designer
**Sprint**: Sprint 1
**Epic**: Epic #2 - Map System Reconstruction

---

## 📋 Table of Contents

1. [Overview](#1-overview)
2. [Story #2.1: World View Toggle Tests](#2-story-21-world-view-toggle-tests)
3. [Story #2.2: Fog System Tests](#3-story-22-fog-system-tests)
4. [Story #2.3: Player Ship Display Tests](#4-story-23-player-ship-display-tests)
5. [Story #2.4: Region Unlock Tests](#5-story-24-region-unlock-tests)
6. [Integration Tests](#6-integration-tests)
7. [Performance Tests](#7-performance-tests)
8. [User Experience Tests](#8-user-experience-tests)

---

## 1. Overview

### 1.1 Test Scope

Epic #2 covers the world map system reconstruction to create the "Wordland" experience:

| Story | Feature | Test Cases |
|-------|---------|------------|
| #2.1 | World View Toggle (World/Island) | 18 test cases |
| #2.2 | Fog System (15%-50% visibility) | 24 test cases |
| #2.3 | Player Ship Display and Movement | 16 test cases |
| #2.4 | Region Unlock Logic | 14 test cases |
| **Integration** | Cross-feature interaction | 12 test cases |
| **Performance** | Frame rate, rendering, memory | 10 test cases |
| **UX** | Navigation, clarity, delight | 8 test cases |
| **Total** | | **102 test cases** |

### 1.2 Test Categories

| Category | Purpose | Test Count |
|----------|---------|------------|
| **Unit Tests** | Component-level validation | 50 |
| **Integration Tests** | Multi-component interaction | 25 |
| **Performance Tests** | Benchmarks and thresholds | 12 |
| **UX Tests** | User validation (manual) | 15 |

---

## 2. Story #2.1: World View Toggle Tests

### 2.1 Toggle Button Tests

#### TC-EP2-001: Toggle Button Visibility
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun worldViewToggle_buttonVisibleOnWorldMapScreen() {
    val screen = WorldMapScreen()
    val hasToggleButton = screen.hasToggleButton()

    assertTrue("Toggle button should be visible", hasToggleButton)
}
```

---

#### TC-EP2-002: Toggle Button Icons
**Priority**: P0
**Type**: Visual Test
**Automation**: No

| State | Icon | Expected |
|-------|------|----------|
| World view | 🏝️ Island (tap to switch) | Manual |
| Island view | 🌍 World (tap to switch) | Manual |

---

#### TC-EP2-003: Toggle Animation
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun viewToggle_animatesSmoothly() {
    // Measure: World → Island transition
    val transitionDuration = measureViewToggleTransition()

    // Assert: Transition completes in 300ms
    assertTrue("Transition should be smooth",
        transitionDuration in 250..350)
}
```

### 2.2 View State Management

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-004 | World view → Island view | Current region expands, list of levels shows | Auto |
| TC-EP2-005 | Island view → World view | Current region collapses, world map shows | Auto |
| TC-EP2-006 | State persistence on rotation | View state preserved | Auto |
| TC-EP2-007 | State persistence on back navigation | View state preserved | Auto |

### 2.3 View Toggle Behavior

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-008 | Toggle in middle of animation | Current animation completes, then toggle | Auto |
| TC-EP2-009 | Rapid toggle (3x in 1 second) | Debounce: Only last toggle executes | Auto |
| TC-EP2-010 | Toggle during region unlock | Unlock animation completes, then toggle | Auto |
| TC-EP2-011 | Toggle with unsaved progress | Progress saved, toggle allowed | Auto |

### 2.4 World View Layout

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-012 | World view shows all regions | All 6 regions visible | Manual |
| TC-EP2-013 | Fogged regions show with overlay | Gray + cloud texture | Manual |
| TC-EP2-014 | Explored regions show full color | Blue, green, purple icons | Manual |
| TC-EP2-015 | Completed regions show star badge | ⭐ badge visible | Manual |
| TC-EP2-016 | Player position marker visible | 🚢 ship icon | Manual |

### 2.5 Island View Layout

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-017 | Island view shows current region | Single region expanded | Manual |
| TC-EP2-018 | Level list displays correctly | 6 level cards with star ratings | Manual |
| TC-EP2-019 | Locked levels show padlock | 🔒 icon visible | Manual |
| TC-EP2-020 | Mastery percentage displayed | Progress bar shows % | Manual |

---

## 3. Story #2.2: Fog System Tests

### 3.1 Fog State Tests

#### TC-EP2-021: Fog State Transitions
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun fogState_transitionsCorrectly() {
    // Test: Unexplored → Partial → Explored → Completed
    val region = MapRegion(
        id = "look_island",
        fogState = FogState.UNEXPLORED
    )

    // Act: Unlock region (adjacent to explored)
    region.unlock(adjacentToExplored = true)
    assertEquals("Should be partially revealed",
        FogState.PARTIALLY_REVEALED, region.fogState)

    // Act: Complete first level
    region.completeLevel(1)
    assertEquals("Should be explored",
        FogState.EXPLORED, region.fogState)

    // Act: Complete all levels
    region.setMastery(1.0f)
    assertEquals("Should be completed",
        FogState.COMPLETED, region.fogState)
}
```

### 3.2 Fog Visual Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-022 | Unexplored fog color | #696969 (Dim Gray) | Manual |
| TC-EP2-023 | Fog opacity | 70% | Manual |
| TC-EP2-024 | Fog texture | Cloud pattern visible | Manual |
| TC-EP2-025 | Fog edge softness | 32dp gradient | Manual |
| TC-EP2-026 | Fog animation | Slow drift (30s cycle) | Manual |

### 3.3 Fog Reveal Animation

#### TC-EP2-027: Fog Reveal Duration
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun fogRevealAnimation_completesIn500ms() {
    val fogOverlay = FogOverlay(region = createRegion())
    val startTime = System.nanoTime()

    fogOverlay.reveal()
    waitForAnimationComplete()

    val duration = (System.nanoTime() - startTime) / 1_000_000
    assertTrue("Reveal should complete in 500ms",
        duration <= 500)
}
```

---

#### TC-EP2-028: Fog Reveal Easing
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun fogReveal_usesFastOutSlowInEasing() {
    // Verify fog dissipates from center outward
    val opacityProgression = captureFogRevealProgression()

    // First 50% of duration should clear >70% of fog
    val halfwayProgress = opacityProgression[250] // 250ms mark
    assertTrue("Center should clear first",
        halfwayProgress < 0.3f)  // <30% opacity at halfway
}
```

### 3.4 Visibility Radius Tests

| Test ID | Player Level | Expected Radius | Test Type |
|---------|-------------|---------------|-----------|
| TC-EP2-029 | Level 1-3 | 15% of map | Auto |
| TC-EP2-030 | Level 4-6 | 30% of map | Auto |
| TC-EP2-031 | Level 7+ | 50% of map | Auto |
| TC-EP2-032 | Radius updates on level completion | Checked after each level | Auto |

```kotlin
@Test
fun visibilityRadius_calculatesCorrectly() {
    fun calculateVisibilityRadius(playerLevel: Int): Float {
        return when {
            playerLevel <= 3 -> 0.15f
            playerLevel <= 6 -> 0.30f
            else -> 0.50f
        }
    }

    assertEquals(0.15f, calculateVisibilityRadius(1))
    assertEquals(0.30f, calculateVisibility(4))
    assertEquals(0.50f, calculateVisibilityRadius(7))
}
```

### 3.5 Fog Interaction Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-033 | Tap unexplored region (adjacent) | "Explore?" dialog appears | Auto |
| TC-EP2-034 | Tap unexplored region (not adjacent) | "Explore nearby areas first!" toast | Auto |
| TC-EP2-035 | Tap explored region | Navigate to region (island view) | Auto |
| TC-EP2-036 | Tap completed region | Navigate to region (island view) | Auto |
| TC-EP2-037 | Tap partially revealed region | Show unlock dialog | Auto |

### 3.6 Fog Performance Tests

| Test ID | Scenario | Target | Type |
|---------|----------|--------|------|
| TC-EP2-038 | Fog rendering at 60fps | ≥60fps | Auto |
| TC-EP2-039 | Fog animation doesn't impact frame rate | No drop >5fps | Auto |
| TC-EP2-040 | Multiple fog animations (3 regions) | Still 60fps | Auto |

---

## 4. Story #2.3: Player Ship Display Tests

### 4.1 Ship Icon Tests

#### TC-EP2-041: Ship Icon Visibility
**Priority**: P0
**Type**: Visual Test
**Automation**: No

| View | Ship Visible? | Position |
|------|---------------|----------|
| World view | Yes | Current region location |
| Island view | No | Hidden |

---

#### TC-EP2-042: Ship Icon Appearance
**Priority**: P1
**Type**: Visual Test
**Automation**: No

| Element | Expected | Type |
|---------|----------|------|
| Icon | 🚢 emoji or custom ship icon | Manual |
| Size | 24dp × 24dp | Manual |
| Color | Blue (or brand color) | Manual |
| Shadow | Elevation shadow for depth | Manual |

### 4.2 Ship Movement Tests

#### TC-EP2-043: Ship Movement Animation Duration
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun shipMovement_animatesIn300ms() {
    val ship = PlayerShip(position = Offset(0.2f, 0.3f))
    val targetPosition = Offset(0.4f, 0.6f)

    val startTime = System.nanoTime()
    ship.moveTo(targetPosition)
    waitForAnimationComplete()

    val duration = (System.nanoTime() - startTime) / 1_000_000
    assertTrue("Movement should complete in 300ms",
        duration <= 300)
}
```

---

#### TC-EP2-044: Ship Movement Easing
**Priority**: P1
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun shipMovement_usesEaseInOutEasing() {
    val positionCurve = captureShipMovementCurve()

    // Verify: Smooth start and end (easeInOut)
    // First 25%: <20% of movement
    val firstQuarter = positionCurve[75] // 75ms mark of 300ms
    assertTrue("Smooth start", firstQuarter < 0.2f)

    // Last 25%: <20% of movement
    val lastQuarter = positionCurve[225] // 225ms mark
    val totalMovement = positionCurve.last()
    assertTrue("Smooth end", lastQuarter > totalMovement * 0.8f)
}
```

### 4.3 Ship Position Synchronization

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-045 | Ship position updates on region change | Ship moves to new region | Auto |
| TC-EP2-046 | Ship position saves to database | Position persists across sessions | Auto |
| TC-EP2-047 | Ship position loads on app start | Correct position restored | Auto |
| TC-EP2-048 | Ship position during level play | Ship shows current level location | Auto |

### 4.4 Ship Visual Feedback

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-049 | Ship arrival animation | Gentle bounce + "ding" sound | Manual |
| TC-EP2-050 | Ship departure animation | Smooth fade out | Manual |
| TC-EP2-051 | Ship pulse on location change | Brief highlight (200ms) | Manual |
| TC-EP2-052 | Ship with "You are here" label | Small label appears below ship | Manual |

---

## 5. Story #2.4: Region Unlock Tests

### 5.1 Unlock Condition Tests

#### TC-EP2-053: Unlock Adjacent Region Requirement
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun regionUnlock_requiresAdjacentExploredRegion() {
    val worldMap = WorldMap()
    val makeLake = worldMap.getRegion("make_lake")
    val lookIsland = worldMap.getRegion("look_island")

    // Make Look Island explored
    lookIsland.explore()
    lookIsland.completeLevel(3) // 60% mastery

    // Act: Try to unlock Make Lake (adjacent)
    val canUnlock = makeLake.canUnlock()

    assertTrue("Should unlock (adjacent to explored)", canUnlock)
}
```

---

#### TC-EP2-054: Unlock Non-Adjacent Region Fails
**Priority**: P0
**Type**: Unit Test
**Automation**: Yes

```kotlin
@Test
fun regionUnlock_failsForNonAdjacentRegion() {
    val worldMap = WorldMap()
    val listenValley = worldMap.getRegion("listen_valley")
    val lookIsland = worldMap.getRegion("look_island")

    // Only Look Island explored (not adjacent to Listen Valley)
    lookIsland.explore()

    // Act: Try to unlock Listen Valley (not adjacent)
    val canUnlock = listenValley.canUnlock()

    assertFalse("Should NOT unlock (not adjacent)", canUnlock)
}
```

### 5.2 Unlock Animation Tests

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-055 | Unlock dialog shows correctly | Region name, icon, confirm button | Auto |
| TC-EP2-056 | Unlock confirmation triggers fog reveal | Fog dissipates (500ms) | Auto |
| TC-EP2-057 | "Discovered!" toast appears | Toast shows after reveal | Auto |
| TC-EP2-058 | Unlock cancellation | Fog returns, no changes | Auto |

### 5.3 Unlock Persistence

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-059 | Unlock saves to database | State persists after restart | Auto |
| TC-EP2-060 | Unlock survives app background | State preserved | Auto |
| TC-EP2-061 | Unlock survives screen rotation | State preserved | Auto |
| TC-EP2-062 | Multiple regions unlock correctly | All unlocks saved | Auto |

### 5.4 Unlock UI Feedback

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-063 | Unlock button appearance on explored adjacent | 🔓 icon visible | Manual |
| TC-EP2-064 | No unlock button on unexplored region | Grayed out or hidden | Manual |
| TC-EP2-065 | Unlock button on completed region | Hidden or changed to "Select" | Manual |
|| TC-EP2-066 | Unlock confirmation includes region details | Icon, name, level count | Manual |

---

## 6. Integration Tests

### 6.1 Cross-Feature Coordination

#### TC-EP2-067: Fog Reveal During Toggle
**Priority**: P0
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun fogReveal_completesDuringViewToggle() {
    // Start: World view, begin unlock
    val worldMapState = WorldMapState(view = ViewMode.WORLD)
    val region = worldMapState.getRegion("make_lake")
    region.unlock()

    // Act: Toggle to Island view during reveal
    worldMapState.toggleView()

    // Assert: Reveal animation completes, then view toggles
    assertTrue("Region should be unlocked", region.isUnlocked)
    assertEquals("View should be Island", ViewMode.ISLAND, worldMapState.view)
}
```

---

#### TC-EP2-068: Ship Moves With Fog Reveal
**Priority**: P1
**Type**: Integration Test
**Automation**: Yes

```kotlin
@Test
fun ship_movesWithPlayerAfterUnlock() {
    val worldMap = WorldMap()
    val ship = worldMap.playerShip

    // Start: Player at Look Island
    ship.position = Offset(0.3f, 0.3f)

    // Unlock Make Lake (adjacent)
    val makeLake = worldMap.getRegion("make_lake")
    makeLake.unlock()

    // Simulate player moving to Make Lake
    ship.moveToRegion("make_lake")

    // Assert: Ship position updates, animation plays
    assertEquals("Ship should be at Make Lake",
        "make_lake", ship.currentRegion)
    assertTrue("Movement animation should play", ship.isAnimating)
}
```

### 6.2 Multi-Region Unlock

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-069 | Rapid unlock of 2 regions | Animations queued, play sequentially | Auto |
| TC-EP2-070 | Unlock during ship movement | Ship completes move, then unlock | Auto |
| TC-EP2-071 | Unlock during celebration | Unlock animation completes, celebration continues | Auto |
| TC-EP2-072 | Region unlock updates ship path | New route calculated | Auto |

### 6.3 State Consistency

| Test ID | Scenario | Expected Result | Type |
|---------|----------|-----------------|------|
| TC-EP2-073 | Consistent fog state across views | World and Island views agree | Auto |
| TC-EP2-074 | Ship position consistent in all views | Position matches in both views | Auto |
| TC-EP2-075 | Mastery calculation updates correctly | Progress bar accurate in all views | Auto |
| TC-EP2-076 | Unlock state persists across sessions | Database correctly stores/retrieves | Auto |

---

## 7. Performance Tests

### 7.1 Rendering Performance

#### TC-EP2-077: Fog Rendering At 60fps
**Priority**: P0
**Type**: Performance Benchmark
**Automation**: Yes

```kotlin
@Test
fun fogRendering_maintains60fps() {
    benchmarkRule.measureRepeated(
        packageName = "com.wordland",
        metrics = listOf(FrameTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.WARM
    ) {
        // Render scene with 6 regions, 3 with fog
        val worldMap = createWorldMapWithFog()
        worldMap.renderFrame()
    }

    // Assert: Frame time ≤ 16.67ms (60fps)
    // Check in benchmark report
}
```

### 7.2 Memory Tests

| Test ID | Scenario | Target | Type |
|---------|----------|--------|------|
| TC-EP2-078 | World view memory baseline | <80MB | Auto |
| TC-EP2-079 | Fog texture memory overhead | <10MB | Auto |
| TC-EP2-080 | Ship animation memory increase | <5MB | Auto |
| TC-EP2-081 | No memory leaks during view toggle | No increase after 10 toggles | Auto |

### 7.3 Load Time Tests

| Test ID | Scenario | Target | Type |
|---------|----------|--------|------|
| TC-EP2-082 | World view initial load | <500ms | Auto |
| TC-EP2-083 | Island view initial load | <300ms | Auto |
| TC-EP2-084 | View toggle transition | <300ms | Auto |
| TC-EP2-085 | Unlock animation doesn't block UI | UI remains responsive | Auto |

---

## 8. User Experience Tests

### 8.1 Navigation Usability

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP2-086 | Users understand toggle button | User survey (10 children) | 100% clarity |
| TC-EP2-087 | View toggle is discoverable | Observation | ≥80% find it without help |
| TC-EP2-088 | Navigation between views is smooth | User survey | ≥90% smooth |
| TC-EP2-089 | "Back" button returns to correct view | User observation | 100% correct |

### 8.2 Exploration Engagement

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP2-090 | Users tap fogged areas | Observation | ≥80% try at least once |
| TC-EP2-091 | Users spend time exploring map | Analytics | ≥2 minutes on first visit |
| TC-092 | "Discovery" feeling is achieved | Survey | ≥70% feel "exploring" |
| TC-EP2-093 | Unlock mechanism is clear | Survey | ≥90% understand how to unlock |

### 8.3 Visual Appeal

| Test ID | Scenario | Test Method | Target |
|---------|----------|-------------|--------|
| TC-EP2-094 | Cartoon style appeals to children | Survey | ≥80% like the visuals |
| TC-EP2-095 | Colors are vibrant but not overwhelming | Survey | ≤5% find it "too bright" |
| TC-EP2-096 | Fog animation is intriguing | Observation | ≥70% watch fog animation |
| TC-EP2-097 | Ship icon is cute/appealing | Survey | ≥80% like the ship |

### 8.4 Age Appropriateness

| Test ID | Age Group | Scenario | Target |
|---------|-----------|----------|--------|
| TC-EP2-098 | 6-8 years | Can navigate map | 100% |
| TC-EP2-099 | 6-8 years | Understand fog concept | ≥90% |
| TC-EP2-100 | 9-10 years | Find map engaging | ≥80% |
| TC-EP2-101 | 11-12 years | Not "too childish" | ≥80% |

---

## 9. Test Execution Plan

### 9.1 Unit Test Execution

```bash
# Run all Epic #2 tests
./gradlew test --tests "*WorldMap*"

# Run by Story
./gradlew test --tests "*WorldViewToggle*"
./gradlew test --tests "*FogSystem*"
./gradlew test --tests "*PlayerShip*"
./gradlew test --tests "*RegionUnlock*"
```

### 9.2 Instrumentation Test Execution

```bash
# Run integration tests
./gradlew connectedAndroidTest --tests "*Epic2Integration*"

# Run specific scenarios
./gradlew connectedAndroidTest --tests "*FogReveal*"
./gradlew connectedAndroidTest --tests "*ShipMovement*"
```

### 9.3 Performance Benchmark Execution

```bash
# Fog rendering performance
./gradlew :app:connectedCheck -Pandroid.testInstrumentationRunnerArguments='class=com.wordland.benchmark.FogRenderingBenchmark'

# View toggle performance
./gradlew :app:connectedCheck -Pandroid.testInstrumentationRunnerArguments='class=com.wordland.benchmark.ViewToggleBenchmark'
```

---

## 10. Test Data

### 10.1 Region Test Data

| Region ID | Name | Icon | Theme | Initial State |
|-----------|------|------|-------|--------------|
| look_island | Look Island | 🌲 | Forest | UNEXPLORED |
| make_lake | Make Lake | 🌊 | Ocean | UNEXPLORED |
| listen_valley | Listen Valley | ⛰️ | Mountain | UNEXPLORED |
| speak_hill | Speak Hill | 🏡 | Village | UNEXPLORED |
| read_castle | Read Castle | 🏰 | Castle | UNEXPLORED |
| write_farm | Write Farm | 🚜 | Farm | UNEXPLORED |

### 10.2 Fog Test Data

| Test Scenario | Fog State | Visibility | Unlock Status |
|--------------|----------|-----------|--------------|
| New game start | UNEXPLORED | 15% (Look Island only) | - |
| After Level 1 | UNEXPLORED → EXPLORED | 15% | - |
| After Level 3 | EXPLORED | 15% | Make Lake unlockable |
| After Level 6 | EXPLORED | 30% | Listen Valley unlockable |
| After Level 12 | EXPLORED | 50% | All regions visible |

### 10.3 Ship Movement Test Data

| From Region | To Region | Distance | Duration |
|-------------|-----------|----------|----------|
| look_island | make_lake | 0.2 | 300ms |
| make_lake | listen_valley | 0.25 | 375ms |
| listen_valley | speak_hill | 0.15 | 225ms |

---

## 11. Success Criteria

### 11.1 Functional Completion

- [ ] All 50 unit tests pass (100%)
- [ ] All 25 integration tests pass (100%)
- [ ] All manual visual tests verified

### 11.2 Performance Targets

- [ ] Frame rate ≥60fps (TC-EP2-038)
- [ ] Memory overhead <20MB (TC-EP2-078)
- [ ] Load time <500ms (TC-EP2-082)

### 11.3 User Experience Targets

- [ ] Toggle clarity ≥90% (TC-EP2-086)
- [ ] Discovery feeling ≥70% (TC-EP2-092)
- [ ] Visual appeal ≥80% (TC-EP2-094)
- [ ] Age appropriate ≥80% (TC-EP2-098)

---

## 12. Defect Tracking

### 12.1 Bug Severity Definitions

| Severity | Definition | Example |
|----------|-----------|---------|
| **P0** | View doesn't toggle | Toggle button does nothing |
| **P0** | Fog doesn't reveal | Region stays gray after unlock |
| **P0** | Ship position incorrect | Ship shows wrong location |
| **P1** | Animation timing off | Toggle takes 1s instead of 300ms |
| **P1** | Visual glitch | Fog flickers during reveal |
| **P2** | Minor UI issue | Spacing incorrect in island view |

### 12.2 Bug Reporting Template

```
Bug ID: EP2-XXX
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
**Total Test Cases**: 102
**Next Steps**: Execute tests during Sprint 1 development
