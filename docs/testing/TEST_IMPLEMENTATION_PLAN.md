# Wordland 测试实施计划

**版本**: 1.0
**日期**: 2026-02-20
**维护者**: android-test-engineer
**目标覆盖率**: 80%

---

## 1. 实施概述

### 1.1 时间表

| Phase | 周次 | 工作内容 | 交付物 | 负责人 |
|-------|------|----------|--------|--------|
| Phase 1 | Week 1-2 | 覆盖率补充 | 60%覆盖率 | Test Engineer |
| Phase 2 | Week 3-4 | P0功能测试 | 地图+反馈测试 | Test Engineer |
| Phase 3 | Week 5-7 | P1功能测试 | 成就+统计+语境化 | Test Engineer |
| Phase 4 | Week 8-9 | P2+自动化 | 防沉迷+CI/CD | Test Engineer |

**总工作量**: 9周 (约2个月)

### 1.2 资源需求

| 资源类型 | 数量 | 用途 |
|----------|------|------|
| 测试工程师 | 1全职 | 编写测试代码 |
| 真机设备 | ≥2台 | UI测试验证 |
| CI/CD | GitHub Actions | 自动化测试 |

---

## 2. Phase 1: 覆盖率补充 (Week 1-2)

### 2.1 目标

- **当前覆盖率**: 21%
- **目标覆盖率**: 60%
- **新增测试**: 75个

### 2.2 任务分解

#### Day 1-3: Data层Repository测试

```kotlin
// 需要创建的测试文件
app/src/test/java/com/wordland/data/repository/
├── GameHistoryRepositoryTest.kt      (15 tests)
├── StatisticsRepositoryTest.kt       (15 tests)
├── AchievementRepositoryTest.kt      (10 tests)
└── SessionRepositoryTest.kt          (10 tests)
```

**GameHistoryRepositoryTest.kt**:
```kotlin
class GameHistoryRepositoryTest {
    private lateinit var repository: GameHistoryRepository
    private lateinit var database: WordDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder().build()
        repository = GameHistoryRepositoryImpl(database.gameHistoryDao())
    }

    @Test
    fun `insert game history persists to database`() = runTest {
        // Given
        val history = createTestGameHistory()

        // When
        repository.insertGameHistory(history)

        // Then
        val retrieved = repository.getGameHistory("user_001", limit = 1)
        assertEquals(1, retrieved.size)
        assertEquals(history, retrieved.first())
    }

    // ... 14 more tests
}
```

#### Day 4-5: ViewModel测试

```kotlin
// 需要创建的测试文件
app/src/test/java/com/wordland/ui/viewmodel/
├── StatisticsViewModelTest.kt        (15 tests)
├── SessionViewModelTest.kt            (15 tests)
└── AchievementViewModelTest.kt        (10 tests)
```

**StatisticsViewModelTest.kt**:
```kotlin
class StatisticsViewModelTest {
    private lateinit var viewModel: StatisticsViewModel
    private val getGameHistoryUseCase = mockk<GetGameHistoryUseCase>()
    private val getGlobalStatisticsUseCase = mockk<GetGlobalStatisticsUseCase>()

    @Before
    fun setup() {
        viewModel = StatisticsViewModel(
            getGameHistoryUseCase,
            getGlobalStatisticsUseCase
        )
    }

    @Test
    fun `load statistics updates uiState to Success`() = runTest {
        // Given
        val stats = createTestGlobalStatistics()
        coEvery { getGlobalStatisticsUseCase(any()) } returns flowOf(stats)

        // When
        viewModel.loadStatistics("user_001")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is StatisticsUiState.Success)
        assertEquals(stats, (state as StatisticsUiState.Success).globalStats)
    }

    // ... 14 more tests
}
```

#### Day 6-7: DAO集成测试

```kotlin
// 需要创建的测试文件
app/src/androidTest/java/com/wordland/data/dao/
├── GameHistoryDaoTest.kt             (10 tests)
├── LevelStatisticsDaoTest.kt          (10 tests)
├── AchievementDaoTest.kt              (10 tests)
└── SessionDaoTest.kt                  (10 tests)
```

**GameHistoryDaoTest.kt**:
```kotlin
@HiltAndroidTest
class GameHistoryDaoTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: WordDatabase

    @Inject
    lateinit var dao: GameHistoryDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `insert and retrieve game history`() = runTest {
        // Given
        val history = createTestGameHistory()

        // When
        dao.insert(history)

        // Then
        val retrieved = dao.getGameHistoryById(history.gameId)
        assertEquals(history, retrieved)
    }

    // ... 9 more tests
}
```

### 2.3 验收标准

- [ ] 所有新测试通过
- [ ] 覆盖率达到60%
- [ ] CI/CD集成测试通过
- [ ] 代码审查通过

---

## 3. Phase 2: P0功能测试 (Week 3-4)

### 3.1 目标

- 地图系统重构测试
- 视觉反馈增强测试
- 新增测试: 40个

### 3.2 地图系统测试

#### Day 1-2: 单元测试

```kotlin
// VisibilityCalculatorTest.kt
class VisibilityCalculatorTest {
    @Test
    fun `visibility radius is 15% for level 1-3`() {
        assertEquals(0.15f, calculateVisibilityRadius(1))
        assertEquals(0.15f, calculateVisibilityRadius(2))
        assertEquals(0.15f, calculateVisibilityRadius(3))
    }

    @Test
    fun `visibility radius is 30% for level 4-6`() {
        assertEquals(0.30f, calculateVisibilityRadius(4))
        assertEquals(0.30f, calculateVisibilityRadius(5))
        assertEquals(0.30f, calculateVisibilityRadius(6))
    }

    @Test
    fun `visibility radius is 50% for level 7+`() {
        assertEquals(0.50f, calculateVisibilityRadius(7))
        assertEquals(0.50f, calculateVisibilityRadius(10))
        assertEquals(0.50f, calculateVisibilityRadius(100))
    }

    @Test
    fun `adjacent regions are revealed on level complete`() {
        val unlocked = calculateRevealedRegions("level_1", isComplete = true)
        assertTrue(unlocked.contains("level_2"))
    }

    // ... 11 more tests
}
```

#### Day 3-4: UI测试

```kotlin
// WorldMapScreenTest.kt
class WorldMapScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tapping unlocked region navigates to level select`() {
        var navigated = false
        val regionId = "level_1"

        composeTestRule.setContent {
            WorldMapScreen(
                viewModel = mockViewModel(),
                onNavigateToLevel = { id ->
                    navigated = true
                    assertEquals(regionId, id)
                }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Region $regionId")
            .performClick()

        assertTrue(navigated)
    }

    @Test
    fun `tapping locked region shows locked dialog`() {
        composeTestRule.setContent {
            WorldMapScreen(
                viewModel = mockViewModel(lockedRegions = listOf("level_3")),
                onNavigateToLevel = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Region level_3")
            .performClick()

        composeTestRule
            .onNodeWithText("This region is locked")
            .assertIsDisplayed()
    }

    // ... 3 more tests
}
```

### 3.3 视觉反馈测试

#### Day 1-2: 单元测试

```kotlin
// AnswerAnimationsTest.kt
class AnswerAnimationsTest {
    @Test
    fun `correct answer triggers green flash animation`() {
        val animation = AnswerAnimations(isCorrect = true)
        assertEquals(Color.Green, animation.flashColor)
        assertEquals(100, animation.flashDuration)
    }

    @Test
    fun `wrong answer triggers red flash animation`() {
        val animation = AnswerAnimations(isCorrect = false)
        assertEquals(Color.Red, animation.flashColor)
    }

    @Test
    fun `stars animate sequentially with 100ms delay`() {
        val stars = StarRatingAnimation(starCount = 3)
        assertEquals(3, stars.animations.size)
        assertEquals(0, stars.animations[0].delay)
        assertEquals(100, stars.animations[1].delay)
        assertEquals(200, stars.animations[2].delay)
    }

    // ... 12 more tests
}
```

#### Day 3: UI测试

```kotlin
// FeedbackAnimationTest.kt
class FeedbackAnimationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `correct answer shows green color flash`() {
        composeTestRule.setContent {
            FeedbackContent(
                isCorrect = true,
                onAnimationComplete = {}
            )
        }

        composeTestRule
            .onNodeWithTestTag("correct_flash")
            .assertExists()
    }

    @Test
    fun `combo counter updates with bounce animation`() {
        composeTestRule.setContent {
            ComboIndicator(combo = 5)
        }

        composeTestRule
            .onNodeWithText("5")
            .assertExists()
    }

    // ... 5 more tests
}
```

### 3.4 验收标准

- [ ] 地图系统40个测试通过
- [ ] 视觉反馈15个测试通过
- [ ] 真机测试通过
- [ ] 动画性能正常

---

## 4. Phase 3: P1功能测试 (Week 5-7)

### 4.1 目标

- 成就系统测试 (30个)
- 统计系统测试 (35个)
- 语境化学习测试 (20个)
- Listen Find测试 (25个)
- 新增测试: 110个

### 4.2 成就系统测试

#### Day 1-2: 触发测试

```kotlin
// AchievementTriggerTest.kt
class AchievementTriggerTest {
    private lateinit var achievementManager: AchievementManager
    private lateinit var repository: AchievementRepository

    @Before
    fun setup() {
        achievementManager = AchievementManager(repository)
    }

    @Test
    fun `FIRST_STEPS unlocks on first word learned`() = runTest {
        // Given
        val event = WordLearnedEvent(userId = "user_001", wordId = "word_001", count = 1)

        // When
        achievementManager.processEvent(event)

        // Then
        val achievement = repository.getAchievement("user_001", "FIRST_STEPS")
        assertTrue(achievement.isUnlocked)
    }

    @Test
    fun `PERFECT_SCORE unlocks on 3-star level`() = runTest {
        val event = LevelCompletedEvent(userId = "user_001", stars = 3)
        achievementManager.processEvent(event)

        val achievement = repository.getAchievement("user_001", "PERFECT_SCORE")
        assertTrue(achievement.isUnlocked)
    }

    // ... 28 more tests
}
```

#### Day 3: 奖励测试

```kotlin
// AchievementRewardTest.kt
class AchievementRewardTest {
    @Test
    fun `unlocking achievement grants coins`() = runTest {
        val before = repository.getUserCoins("user_001")
        achievementManager.unlock("user_001", "FIRST_STEPS")
        val after = repository.getUserCoins("user_001")

        assertTrue(after > before)
    }

    @Test
    fun `coin amount matches difficulty tier`() {
        val easy = AchievementRepository.getCoinsForTier(AchievementTier.ONE_STAR)
        val legendary = AchievementRepository.getCoinsForTier(AchievementTier.FIVE_STAR)

        assertEquals(20, easy)  // ⭐ tier
        assertEquals(200, legendary)  // ⭐⭐⭐⭐⭐ tier
    }

    // ... 8 more tests
}
```

### 4.3 统计系统测试

#### Day 4-5: 数据模型测试

```kotlin
// LevelStatisticsTest.kt
class LevelStatisticsTest {
    @Test
    fun `average score calculates correctly`() {
        val stats = LevelStatistics(
            userId = "user_001",
            levelId = "level_1",
            totalGames = 5,
            totalScore = 500
        )

        assertEquals(100f, stats.averageScore)
    }

    @Test
    fun `overall accuracy calculates correctly`() {
        val stats = LevelStatistics(
            totalCorrect = 45,
            totalQuestions = 50
        )

        assertEquals(0.9f, stats.overallAccuracy)
    }

    // ... 33 more tests
}
```

#### Day 6: Repository测试

```kotlin
// StatisticsRepositoryTest.kt
class StatisticsRepositoryTest {
    @Test
    fun `update level statistics recalculates aggregates`() = runTest {
        // Given
        val gameHistory = createTestGameHistory(score = 300)

        // When
        repository.updateLevelStatistics("user_001", "level_1", gameHistory)

        // Then
        val stats = repository.getLevelStatistics("user_001", "level_1")
        assertTrue(stats.totalGames > 0)
        assertEquals(300, stats.highestScore)
    }

    // ... 9 more tests
}
```

### 4.4 语境化学习测试

#### Day 7-8: 语境生成测试

```kotlin
// ContextQuestionGeneratorTest.kt
class ContextQuestionGeneratorTest {
    @Test
    fun `sentence mode generates question with blank`() {
        val sentence = "I eat _____ for breakfast."
        val question = generator.generateFromSentence(sentence)

        assertTrue(question.targetWord.isNotBlank())
        assertTrue(question.contextSentence.contains("_____"))
    }

    @Test
    fun `cognitive load is 80% new 20% review for level 4-6`() {
        val words = generator.generateForLevel(4, count = 10)
        val newWords = words.filter { it.isNew }

        assertTrue(newWords.size >= 7)  // 80%
        assertTrue(newWords.size <= 9)  // Allow some variance
    }

    // ... 18 more tests
}
```

### 4.5 Listen Find测试

#### Day 9-10: 音频测试

```kotlin
// ListenFindValidatorTest.kt
class ListenFindValidatorTest {
    @Test
    fun `correct image selection returns true`() {
        val result = validator.validate(selectedImageId = "correct_id", correctImageId = "correct_id")
        assertTrue(result.isCorrect)
    }

    @Test
    fun `audio file path is correct`() {
        val word = Word(id = "word_001", audioPath = "audio/word_001.mp3")
        val path = AudioPathResolver.getWordAudioPath(word)

        assertEquals("android.resource://com.wordland/raw/word_001", path)
    }

    // ... 23 more tests
}
```

### 4.6 验收标准

- [ ] 成就系统30个测试通过
- [ ] 统计系统35个测试通过
- [ ] 语境化20个测试通过
- [ ] Listen Find 25个测试通过
- [ ] 集成测试通过

---

## 5. Phase 4: P2功能+自动化 (Week 8-9)

### 5.1 目标

- 防沉迷系统测试 (40个)
- CI/CD完整集成
- 新增测试: 40个

### 5.2 防沉迷测试

#### Day 1-2: 会话管理测试

```kotlin
// SessionTimeTrackerTest.kt
class SessionTimeTrackerTest {
    @Test
    fun `active gameplay time is counted`() = runTest {
        val tracker = SessionTimeTracker(sessionStartTime = 0)
        val updated = tracker.update(
            activityState = ActivityState.ACTIVE_GAMEPLAY,
            currentTime = 60000  // 1 minute later
        )

        assertEquals(60000, updated.activeTimeThisSession)
    }

    @Test
    fun `background time is not counted`() = runTest {
        val tracker = SessionTimeTracker(sessionStartTime = 0)
        val updated = tracker.update(
            activityState = ActivityState.BACKGROUND,
            currentTime = 60000
        )

        assertEquals(0, updated.activeTimeThisSession)
    }

    @Test
    fun `soft reminder triggers at 15 minutes`() {
        val tracker = SessionTimeTracker(activeTimeThisSession = 15 * 60 * 1000L)
        assertTrue(tracker.shouldShowSoftReminder())
    }

    @Test
    fun `hard limit triggers at 45 minutes`() {
        val tracker = SessionTimeTracker(activeTimeThisSession = 45 * 60 * 1000L)
        assertTrue(tracker.hasHitHardLimit())
    }

    // ... 36 more tests
}
```

#### Day 3-4: 休息奖励测试

```kotlin
// BreakRewardCalculatorTest.kt
class BreakRewardCalculatorTest {
    @Test
    fun `5 minute break gives 1.2x multiplier`() {
        val bonus = calculateBreakBonus(duration = 5 * 60 * 1000L)
        assertEquals(1.2f, bonus.multiplier)
    }

    @Test
    fun `15 minute break gives 1.5x multiplier`() {
        val bonus = calculateBreakBonus(duration = 15 * 60 * 1000L)
        assertEquals(1.5f, bonus.multiplier)
        assertEquals(5, bonus.wordsRemaining)
    }

    // ... 8 more tests
}
```

### 5.3 CI/CD集成

#### Day 5: GitHub Actions配置

```yaml
# .github/workflows/test.yml
name: Test Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run unit tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: app/build/reports/jacoco/test/jacocoTestReport.xml
      - name: Coverage check
        run: |
          COVERAGE=$(./gradlew jacocoTestReport | grep "Instruction Coverage" | awk '{print $3}' | sed 's/%//')
          if (( $(echo "$COVERAGE < 80" | bc -l) )); then
            echo "Coverage $COVERAGE% is below 80%"
            exit 1
          fi

  integration-tests:
    name: Integration Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run integration tests
        run: ./gradlew connectedAndroidTest
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: integration-test-results
          path: app/build/reports/androidTests/

  ui-tests:
    name: UI Tests
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run UI tests
        run: ./gradlew connectedCheck
      - name: Upload screenshots
        uses: actions/upload-artifact@v3
        if: failure()
        with:
          name: ui-failure-screenshots
          path: app/build/outputs/connected_android_test_additional_output/
```

#### Day 6: 覆盖率门禁配置

```kotlin
// build.gradle.kts
tasks.register("coverageCheck") {
    dependsOn("jacocoTestReport")

    doLast {
        val reportFile = file("app/build/reports/jacoco/test/html/index.html")
        if (!reportFile.exists()) {
            throw GradleException("Coverage report not found")
        }

        val content = reportFile.readText()
        val coveragePattern = Regex("Total.*?([0-9]+)%")
        val match = coveragePattern.find(content)
        val coverage = match?.groupValues?.get(1)?.toIntOrNull() ?: 0

        println("Current coverage: $coverage%")

        if (coverage < 80) {
            throw GradleException("Coverage $coverage% is below 80% threshold")
        }

        println("✅ Coverage check passed: $coverage% >= 80%")
    }
}
```

### 5.4 验收标准

- [ ] 防沉迷40个测试通过
- [ ] CI/CD完整运行
- [ ] 覆盖率达到80%
- [ ] 所有质量门禁通过

---

## 6. 测试工具配置

### 6.1 Gradle配置

```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("jacoco")
}

android {
    // ... existing config

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        animationsDisabled = true
    }
}

// JaCoCo configuration
tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$ViewInjector*.*",
        "**/*\$ViewBinder*.*",
        "**/databinding/*",
        "**/android/databinding/*",
        "**/androidx/databinding/*",
        "**/*_Hilt*.*"
    )

    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(listOf(mainSrc)))
    classDirectories.setFrom(files(listOf(debugTree)))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/*.exec")
    })
}
```

### 6.2 测试依赖

```kotlin
// build.gradle.kts
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.truth:truth:1.1.5")

    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Hilt testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")

    // Room testing
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // Compose testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.1")

    // Navigation testing
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")

    // Benchmark
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.2")
}
```

---

## 7. 风险管理

### 7.1 风险矩阵

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| UI测试不稳定 | 高 | 中 | 使用稳定ID，重试机制 |
| 真机设备不足 | 中 | 低 | Firebase Test Lab |
| 覆盖率目标无法达成 | 高 | 低 | 分阶段目标 |
| 测试代码维护成本 | 中 | 中 | 保持简单，避免过度设计 |
| CI/CD超时 | 中 | 低 | 优化测试速度，并行执行 |

### 7.2 回退计划

- **Phase 1失败**: 延长1周，降低目标到50%
- **Phase 2失败**: 先完成核心测试，UI测试延后
- **Phase 3失败**: 优先成就和统计，其他延后
- **Phase 4失败**: 保留核心自动化，手动补充

---

## 8. 成功标准

### 8.1 定量指标

| 指标 | 当前 | 目标 | 测量方法 |
|------|------|------|----------|
| 整体覆盖率 | 21% | 80% | JaCoCo |
| 单元测试数 | 3,075 | 3,500+ | gradlew test |
| 集成测试数 | ~20 | 150+ | gradlew connectedAndroidTest |
| UI测试数 | ~5 | 80+ | gradlew connectedCheck |
| CI通过率 | 100% | 100% | GitHub Actions |

### 8.2 定性指标

- 所有P0功能有测试覆盖
- 关键用户流程有E2E测试
- 测试代码质量符合标准
- 测试文档完整

---

**文档状态**: ✅ 完成
**版本**: 1.0
**下一步**: 开始Phase 1实施
