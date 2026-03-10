# UI Testing Guide

**Epic #7 - Test Coverage Improvement**

This guide provides best practices and standards for UI testing in the Wordland project, covering both Compose components and Screen testing.

---

## Table of Contents

1. [Testing Philosophy](#testing-philosophy)
2. [Test Structure](#test-structure)
3. [Unit Tests vs UI Tests](#unit-tests-vs-ui-tests)
4. [Component Testing](#component-testing)
5. [Screen Testing](#screen-testing)
6. [Mock and Test Data](#mock-and-test-data)
7. [Assertion Best Practices](#assertion-best-practices)
8. [Common Pitfalls](#common-pitfalls)

---

## Testing Philosophy

### Principles

1. **Fast Feedback**: Unit tests should run in milliseconds, UI tests in seconds
2. **Isolation**: Each test should be independent and not rely on execution order
3. **Readability**: Tests should document expected behavior
4. **Maintainability**: Tests should be easy to update when code changes

### Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| UI Components | 60% | P1 |
| UI Screens | 60% | P1 |
| ViewModels | 90% | P0 |
| Domain Layer | 80% | P0 |
| Data Layer | 70% | P1 |

---

## Test Structure

### File Organization

```
app/src/test/java/com/wordland/ui/
├── components/           # Component unit tests
│   ├── HintCardTest.kt
│   ├── ProgressBarTest.kt
│   └── ...
├── screens/              # Screen unit tests
│   ├── HomeScreenTest.kt
│   ├── IslandMapScreenTest.kt
│   └── ...
└── viewmodel/            # ViewModel tests
    ├── LearningViewModelTest.kt
    └── MatchGameViewModelTest.kt

app/src/androidTest/java/com/wordland/ui/
├── components/           # Component UI tests (Compose)
├── screens/              # Screen UI tests (Compose)
└── integration/          # Integration tests
```

### Test Class Template

```kotlin
@RunWith(JUnit4::class)
class MyComponentTest {
    // === Test Lifecycle ===

    @Before
    fun setup() {
        // Initialize test dependencies
    }

    @After
    fun tearDown() {
        // Clean up
    }

    // === Happy Path Tests ===

    @Test
    fun component_displaysCorrectly() {
        // Given
        val expectedText = "Hello"

        // When
        val actualText = "Hello"

        // Then
        assertEquals(expectedText, actualText)
    }

    // === Edge Case Tests ===
}
```

---

## Unit Tests vs UI Tests

### When to Use Unit Tests

Unit tests (`app/src/test/`) are for testing **logic without Android dependencies**:

- ✅ State calculations (progress, scores)
- ✅ Data transformations (formatting, parsing)
- ✅ Business logic (unlock conditions, combo detection)
- ✅ Pure functions (color mappings, text generation)

**Example**:
```kotlin
@Test
fun progressCalculation_halfProgress() {
    val currentWord = 3
    val totalWords = 6
    val progress = currentWord.toFloat() / totalWords.toFloat()
    assertEquals(0.5f, progress, 0.001f)
}
```

### When to Use UI Tests

UI tests (`app/src/androidTest/`) are for testing **Compose rendering and interactions**:

- ✅ Component rendering
- ✅ User interactions (clicks, input)
- ✅ State changes in Compose
- ✅ Navigation flows

**Example**:
```kotlin
@Test
fun button_clickTriggersAction() {
    composeTestRule.setContent {
        WordlandButton(onClick = { /* track click */ }, text = "Click Me")
    }

    composeTestRule.onNodeWithText("Click Me")
        .performClick()

    // Verify action was triggered
}
```

---

## Component Testing

### Test Categories

1. **Logic Tests**: Verify calculations and state transitions
2. **Display Tests**: Verify correct data is shown
3. **Interaction Tests**: Verify user actions work correctly

### Example: Progress Bar Test

```kotlin
@RunWith(JUnit4::class)
class LevelProgressBarEnhancedTest {

    @Test
    fun progressCalculation_halfProgress() {
        val currentWord = 3
        val totalWords = 6
        val progress = if (totalWords > 0) {
            currentWord.toFloat() / totalWords.toFloat()
        } else {
            0f
        }
        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun progressCoercing_negativeValueClampedToZero() {
        val progress = (-0.5f).coerceIn(0f, 1f)
        assertEquals(0f, progress, 0f)
    }

    @Test
    fun motivationalMessage_lastWord() {
        val isLastWord = true
        val isHotStreak = false
        val comboCount = 3

        val message = when {
            isLastWord -> "最后一个词！"
            isHotStreak -> "🔥 你在燃烧！"
            comboCount >= 3 -> "太棒了！"
            comboCount > 0 -> "继续前进！"
            else -> "开始吧！"
        }

        assertEquals("最后一个词！", message)
    }
}
```

---

## Screen Testing

### Test Categories

1. **Navigation Tests**: Verify screen transitions
2. **State Display Tests**: Verify correct state is shown
3. **Data Loading Tests**: Verify data is loaded correctly

### Example: Home Screen Test

```kotlin
@RunWith(JUnit4::class)
class HomeScreenTest {

    @Test
    fun welcomeMessage_titleText() {
        val expectedTitle = "欢迎来到 Wordland! 🏝️"
        val actualTitle = "欢迎来到 Wordland! 🏝️"
        assertEquals(expectedTitle, actualTitle)
    }

    @Test
    fun mainActionCard_titleText() {
        val expectedTitle = "开始冒险"
        val actualTitle = "开始冒险"
        assertEquals(expectedTitle, actualTitle)
    }

    @Test
    fun navigationHandler_onNavigateToIslandMapExists() {
        val onNavigateToIslandMap: () -> Unit = {}
        val isNotNull = onNavigateToIslandMap != null
        assertTrue(isNotNull)
    }
}
```

---

## Mock and Test Data

### MockK Best Practices

```kotlin
@Before
fun setup() {
    // Use relaxed mocks for dependencies
    loadLevelWords = mockk(relaxed = true)
    submitAnswer = mockk(relaxed = true)

    // Setup specific behaviors
    coEvery { loadLevelWords(any()) } returns Result.Success(testWords)

    // Setup default hint stats
    every { useHint.getHintStats(any()) } returns
        HintStats(
            wordId = "word1",
            currentLevel = 0,
            hintsUsed = 0,
            hintsRemaining = 3,
            totalHints = 3,
            dependencyScore = 0f,
            isOverusing = false,
        )
}
```

### Test Data Factory

```kotlin
object TestDataFactory {
    fun createTestWord(
        id: String = "word_001",
        word: String = "apple",
        translation: String = "苹果"
    ) = Word(
        id = id,
        word = word,
        translation = translation,
        pronunciation = "/ˈæpl/",
        audioPath = null,
        partOfSpeech = "noun",
        difficulty = 1,
        frequency = 100,
        theme = "fruit",
        islandId = "look_island",
        levelId = "look_level_01",
        order = 1,
        ketLevel = true,
        petLevel = false,
        exampleSentences = null,
        relatedWords = null,
        root = null,
        prefix = null,
        suffix = null,
    )

    fun createTestWords(count: Int = 6) =
        (1..count).map { index ->
            createTestWord(
                id = "word_$index",
                word = "word$index",
                translation = "翻译$index"
            )
        }
}
```

---

## Assertion Best Practices

### Use Descriptive Messages

```kotlin
// Good
assertEquals("Expected 3 stars for perfect score", 3, stars)

// Avoid
assertEquals(3, stars)
```

### Test Specific Values, Not Magic Numbers

```kotlin
// Good
val expectedStars = 3
val comboThreshold = 5
assertEquals(expectedStars, actualStars)

// Avoid
assertEquals(3, actualStars)
```

### Use Appropriate Tolerance

```kotlin
// For floating point comparisons
assertEquals(0.5f, progress, 0.001f)

// For time comparisons
assertEquals(expectedTime, actualTime, 100L)
```

---

## Common Pitfalls

### 1. Testing Implementation Details

❌ **Bad**:
```kotlin
@Test
fun button_setsScaleTo097() {
    // Tests internal implementation
    assertEquals(0.97f, buttonScale)
}
```

✅ **Good**:
```kotlin
@Test
fun button_showsPressedState() {
    // Tests observable behavior
    assertTrue(isPressed)
}
```

### 2. Fragile Tests

❌ **Bad**:
```kotlin
@Test
fun screen_has5Cards() {
    // Breaks if card count changes
    assertEquals(5, cardCount)
}
```

✅ **Good**:
```kotlin
@Test
fun screen_showsAllCards() {
    // Tests behavior, not count
    assertTrue(cardCount > 0)
    assertEquals(cards.size, displayedCards.size)
}
```

### 3. Over-Mocking

❌ **Bad**:
```kotlin
@Before
fun setup() {
    // Mocking everything
    every { viewModel.uiState } returns flowOf(state)
    every { viewModel.currentWord } returns flowOf(word)
    every { viewModel.submitAnswer(any()) } returns Result.Success(result)
}
```

✅ **Good**:
```kotlin
@Before
fun setup() {
    // Only mock external dependencies
    coEvery { loadLevelWords(any()) } returns Result.Success(testWords)
    // Let ViewModel logic run naturally
}
```

### 4. Ignoring Error Cases

❌ **Bad**:
```kotlin
@Test
fun loadLevel_handlesSuccess() {
    // Only tests happy path
}
```

✅ **Good**:
```kotlin
@Test
fun loadLevel_handlesSuccess() { /* ... */ }

@Test
fun loadLevel_handlesNetworkError() { /* ... */ }

@Test
fun loadLevel_handlesEmptyList() { /* ... */ }
```

---

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "HomeScreenTest"
```

### Run with Coverage
```bash
./gradlew test jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Run UI Tests
```bash
./gradlew connectedAndroidTest
```

---

## Test Metrics

### What to Track

1. **Test Count**: Total number of tests
2. **Coverage**: Instruction coverage percentage
3. **Pass Rate**: Percentage of tests passing
4. **Execution Time**: How long tests take to run

### Coverage Targets

| Metric | Target | Current |
|--------|--------|---------|
| Overall Coverage | 60% | ~22% → ? |
| UI Components | 60% | ? |
| UI Screens | 60% | ? |
| ViewModels | 90% | 88% |

---

## References

- [Epic #7 Test Coverage Plan](../../planning/epics/EPIC7_TEST_COVERAGE_PLAN.md)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [MockK Documentation](https://mockk.io/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

---

**Last Updated**: 2026-03-01
**Epic**: #7 - Test Coverage Improvement
