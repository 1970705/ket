# Epic #12 Testing Best Practices

**Real Device UI Automation Testing**

**Version**: 1.0
**Last Updated**: 2026-03-08
**Owner**: android-test-engineer

---

## Table of Contents

1. [Test Design Principles](#test-design-principles)
2. [Device Testing Strategy](#device-testing-strategy)
3. [Screenshot Management](#screenshot-management)
4. [CI/CD Best Practices](#cicd-best-practices)
5. [Code Quality](#code-quality)
6. [Team Collaboration](#team-collaboration)

---

## Test Design Principles

### 1. Pyramid Testing Approach

```
           /\
          /  \        E2E Tests (5%)
         /    \       - Real device scripts
        /------\      - CI automation
       /        \     - Manual validation
      /----------\    Integration Tests (15%)
     /            \   - ViewModel tests
    /--------------\  - Repository tests
   /                \ Unit Tests (80%)
  /                  \ - Domain logic
 /                    \ - Pure functions
/______________________\
```

**Ratios**:
- Unit tests: 80% (fast, isolated)
- Integration tests: 15% (ViewModel, Repository)
- E2E tests: 5% (real device, CI)

### 2. Test Scenario Categories

| Category | Purpose | Frequency | Example |
|----------|---------|-----------|---------|
| **Smoke** | Quick sanity check | Every PR | App launches, home screen displays |
| **Critical Path** | Core user flows | Every PR | Learning flow, level completion |
| **Regression** | Known bug areas | Every PR | Hint counter, layout overflow |
| **Visual** | UI appearance | Daily | Screenshot comparison |
| **Compatibility** | Cross-device | Weekly | Different screen sizes, API levels |

### 3. Test Naming Conventions

**File Naming**:
```
<Feature><Type>Test.kt

Examples:
- LearningScreenTest.kt
- HintCardComponentTest.kt
- StarRatingAlgorithmTest.kt
```

**Test Function Naming** (Given-When-Then):
```kotlin
@Test
fun `given learning flow when word submitted then feedback shown`() {
    // Given
    val question = createQuestion()

    // When
    viewModel.submitAnswer("word")

    // Then
    assertThat(viewModel.uiState.value).isInstanceOf<Feedback>()
}
```

---

## Device Testing Strategy

### 1. Device Priority Matrix

**P0 - Must Test (Every PR)**:
```
┌─────────────────────────────────────┐
│ Xiaomi 13 (API 33)                  │
│ - 6.36" @ 419 dpi                   │
│ - Most common user device           │
│ - Standard phone form factor        │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ Samsung Galaxy S23 (API 33)         │
│ - 6.1" @ 425 dpi                    │
│ - High-resolution display           │
│ - Dense screen                      │
└─────────────────────────────────────┘
```

**P1 - Should Test (Daily)**:
```
┌─────────────────────────────────────┐
│ Google Pixel 7 (API 33)             │
│ - Stock Android experience          │
│ - Reference device                  │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ Samsung Galaxy A10 (API 28)         │
│ - Low-end device                    │
│ - Minimum supported API level       │
└─────────────────────────────────────┘
```

**P2 - Nice to Test (Weekly)**:
```
┌─────────────────────────────────────┐
│ Tablet (8", API 33)                 │
│ - Larger screen layout              │
│ - Landscape orientation             │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ Foldable (7.6", API 33)             │
│ - Unique form factor                │
│ - Aspect ratio testing              │
└─────────────────────────────────────┘
```

### 2. Screen Size Coverage

```
┌──────────────────────────────────────────────────────────┐
│                    Small (4.5-5.5")                      │
│  - Navigation button spacing                            │
│  - Text readability                                     │
│  - Touch target size (44dp min)                         │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                    Normal (5.5-6.7")                     │
│  - Primary target                                       │
│  - Standard layout validation                           │
│  - Most common form factor                              │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│                    Large (6.7-7.5")                      │
│  - Content scaling                                      │
│  - Multi-column layouts                                 │
│  - One-handed reachability                              │
└──────────────────────────────────────────────────────────┘
```

### 3. API Level Testing

| API | Version | Focus Areas | Frequency |
|-----|---------|-------------|-----------|
| 34 | Android 14 | Edge-to-edge, predictive back | Every PR |
| 33 | Android 13 | Material 3, per-app language | Every PR |
| 29 | Android 10 | Compatibility, dark mode | Weekly |

---

## Screenshot Management

### 1. Baseline Storage

```
docs/screenshots/
├── baseline/
│   ├── api-29/
│   │   ├── phone_normal/
│   │   └── phone_small/
│   ├── api-33/
│   │   ├── phone_normal/
│   │   └── tablet_portrait/
│   └── api-34/
│       └── phone_normal/
└── diff/
    └── <timestamp>/
```

### 2. Baseline Versioning

```bash
# Tag baseline commits
git tag baseline-v1.0.0
git push origin baseline-v1.0.0

# Find baseline for specific version
git log --tags --simplify-by-decoration --pretty="format:%ci %d"
```

### 3. Screenshot Comparison Strategy

**Pixel-Perfect Comparison**:
```bash
# For critical UI elements (buttons, icons)
FUZZ_THRESHOLD=0 compare baseline.png current.png diff.png
```

**Permissive Comparison**:
```bash
# For text, gradients, anti-aliased elements
FUZZ_THRESHOLD=15 compare baseline.png current.png diff.png
```

**Structural Comparison**:
```bash
# Ignore colors, compare layout only
convert baseline.png -colorspace Gray baseline_gray.png
convert current.png -colorspace Gray current_gray.png
compare baseline_gray.png current_gray.png diff.png
```

---

## CI/CD Best Practices

### 1. Workflow Triggers

**Recommended Setup**:
```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      run-ui-tests:
        description: 'Run UI automation tests'
        type: boolean
        default: false
```

**Triggering Strategies**:

| Event | Auto-Run | Manual |
|-------|----------|--------|
| Push to main | Unit tests only | Full suite |
| PR to main | Unit + Lint | UI tests |
| Nightly | Full suite | N/A |

### 2. Artifact Management

```yaml
# Upload test artifacts
- name: Upload test results
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: ui-test-results-${{ github.sha }}
    path: |
      app/build/reports/androidTests/
      app/build/outputs/androidTest-results/
    retention-days: 30
```

**Retention Policy**:
- Test results: 30 days
- Screenshots: 90 days
- Bug reports: 1 year

### 3. Parallel Execution

```yaml
# Run tests on multiple API levels in parallel
strategy:
  matrix:
    api-level: [29, 33, 34]
  fail-fast: false
```

---

## Code Quality

### 1. Test Code Standards

**DO** ✅:
```kotlin
// Use descriptive test names
@Test
fun `given hint count 3 when requesting hint then shows limit reached message`() {}

// Arrange-Act-Assert pattern
@Test
fun testHintLimit() {
    // Arrange
    val manager = HintManager(maxHints = 3)

    // Act
    repeat(3) { manager.useHint() }

    // Assert
    assertThat(manager.hintsRemaining).isEqualTo(0)
}

// Use test builders
fun buildQuestion(word: String = "test"): SpellBattleQuestion {
    return SpellBattleQuestion(
        wordId = "test_001",
        translation = "测试",
        targetWord = word
    )
}
```

**DON'T** ❌:
```kotlin
// Vague test names
@Test
fun testItWorks() {}

// All logic in test method
@Test
fun test() {
    val vm = LearningViewModel(
        GetTutorialWordsUseCase(repository),
        CompleteTutorialWordUseCase(repository, manager),
        SubmitAnswerUseCase(calculator, detector, analyzer),
        UpdateGameStateUseCase(repository, dao),
        SelectPetUseCase(repository, dao, prefs),
        OpenFirstChestUseCase(repository, manager)
    )
    // ... 50 lines of test logic
}

// Hard-coded values everywhere
@Test
fun test() {
    assertThat(result.wordId).isEqualTo("look_001")
    assertThat(result.translation).isEqualTo("看")
    assertThat(result.hint).isEqualTo("First letter: L")
    // Not maintainable
}
```

### 2. Test File Size Limits

**Maximum Lines**: 400 lines per test file

**Strategies to Keep Files Small**:
1. Split by feature (e.g., `HintGeneratorTest.kt`, `HintManagerTest.kt`)
2. Use parameterized tests
3. Extract test helpers to separate files
4. Group related tests in nested classes

### 3. Test Independence

```kotlin
// GOOD: Each test is isolated
@Test
fun `test1 should start with zero progress`() {
    val progress = LevelProgress()
    assertThat(progress.completedWords).isEmpty()
}

@Test
fun `test2 should increment progress`() {
    val progress = LevelProgress()
    progress.completeWord("word1")
    assertThat(progress.completedWords).contains("word1")
}

// BAD: Tests depend on execution order
var sharedProgress = LevelProgress()

@Test
fun test1() {
    sharedProgress.completeWord("word1")
}

@Test
fun test2() {
    // Fails if test1 doesn't run first
    assertThat(sharedProgress.completedWords).isNotEmpty()
}
```

---

## Team Collaboration

### 1. Test Review Checklist

When reviewing test changes, check:

- [ ] Tests are named descriptively
- [ ] Each test has a single responsibility
- [ ] Tests are independent (can run in any order)
- [ ] File is under 400 lines
- [ ] Tests cover happy path + edge cases
- [ ] No hardcoded magic numbers
- [ ] Proper use of assertions
- [ ] Tests run fast (< 1 second each)

### 2. Documentation Standards

Every test file should include:

```kotlin
/**
 * Test suite for [HintGenerator].
 *
 * Tests the generation of progressive hints:
 * - Level 1: First letter only
 * - Level 2: First half of word
 * - Level 3: Full word with vowels masked
 *
 * @see com.wordland.domain.hint.HintGenerator
 */
class HintGeneratorTest {
    // ...
}
```

### 3. Test Maintenance

**Weekly**:
- Review failed tests
- Update flaky tests
- Archive old test artifacts

**Monthly**:
- Audit test coverage
- Remove obsolete tests
- Update baselines

**Per Epic**:
- Add new test scenarios
- Update test matrix
- Document new patterns

---

## Quick Reference

### Test Scenario Template

```bash
#!/bin/bash
# Test: <Scenario Name>
# Description: <What this test validates>
# Prerequisites: <Device state required>
# Screenshots: <Number and locations>

test_<scenario>() {
    log_step "Testing <scenario>..."

    # Setup
    adb shell am start -n com.wordland/.MainActivity

    # Actions
    # ...

    # Capture
    capture_screenshot "<screenshot_name>"

    # Verify
    # ...

    log_info "<scenario> test passed"
}
```

### CI Workflow Template

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run tests
        run: ./gradlew test
      - name: Upload results
        uses: actions/upload-artifact@v3
        if: always()
```

---

**Document Status**: ✅ Complete
**Last Updated**: 2026-03-08
**Next Review**: After Epic #12 completion
