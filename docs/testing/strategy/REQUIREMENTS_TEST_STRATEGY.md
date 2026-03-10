# Wordland 需求测试策略

**版本**: 1.0
**日期**: 2026-02-20
**评审人**: android-test-engineer
**基于文档**: `docs/requirements/README.md`

---

## 1. 测试覆盖率分析

### 1.1 当前状态

| 指标 | 当前值 | 目标值 | 差距 | 状态 |
|------|--------|--------|------|------|
| **整体覆盖率** | 21% | 80% | -59% | 🔴 P0 |
| Domain层覆盖率 | 83% | 90% | -7% | 🟡 P1 |
| Data层覆盖率 | 15% | 80% | -65% | 🔴 P0 |
| UI层覆盖率 | 0-5% | 60% | -55% | 🔴 P0 |
| 单元测试数量 | 3,075 | - | - | ✅ |
| 测试通过率 | 100% | 100% | 0% | ✅ |

### 1.2 覆盖率差距根因

| 层级 | 主要问题 | 影响 | 优先级 |
|------|----------|------|--------|
| **Data层** | Repository、DAO 缺少集成测试 | 数据持久化风险 | 🔴 P0 |
| **UI层** | Compose组件未测试 | UI交互风险 | 🔴 P0 |
| **Domain层** | 部分UseCase未覆盖 | 边界情况风险 | 🟡 P1 |

---

## 2. 新增功能测试需求

### 2.1 功能测试矩阵

| 功能模块 | 优先级 | 测试类型 | 用例数 | 工作量 | 状态 |
|----------|--------|----------|--------|--------|------|
| **地图系统重构** | P0 | 单元+集成+UI | 25 | 3天 | ❌ 未定义 |
| **视觉反馈增强** | P0 | 单元+UI | 15 | 2天 | ❌ 未定义 |
| **语境化学习** | P1 | 单元+集成+UI | 20 | 2天 | ❌ 未定义 |
| **成就系统** | P1 | 单元+集成 | 30 | 3天 | ⚠️ 部分定义 |
| **统计系统** | P1 | 单元+集成+UI | 35 | 3天 | ⚠️ 部分定义 |
| **防沉迷系统** | P2 | 单元+集成+UI | 40 | 4天 | ❌ 未定义 |
| **Listen Find模式** | P1 | 单元+集成+UI | 25 | 3天 | ❌ 未定义 |
| **Sentence Match模式** | P2 | 单元+集成+UI | 20 | 2天 | ❌ 未定义 |

**总计**: 210 个新测试用例，约 22 天工作量

---

## 3. P0功能测试需求

### 3.1 地图系统重构 (P0)

**核心组件**:
- `WorldMapScreen` - 世界地图UI
- `FogOverlay` - 迷雾覆盖层
- `VisibilityCalculator` - 可见性计算
- `RegionUnlockManager` - 区域解锁管理

**测试用例** (25个):

#### 单元测试 (15个)

```kotlin
// VisibilityCalculatorTest.kt
class VisibilityCalculatorTest {
    @Test fun `visibility radius is 15% for level 1-3`()
    @Test fun `visibility radius is 30% for level 4-6`()
    @Test fun `visibility radius is 50% for level 7+`()
    @Test fun `visibility updates when level increases`()
    @Test fun `adjacent regions are revealed on level complete`()
}

// FogOverlayTest.kt
class FogOverlayTest {
    @Test fun `fog covers locked regions`()
    @Test fun `fog reveals when region unlocked`()
    @Test fun `fog animation completes in 500ms`()
    @Test fun `fog opacity is 1.0 for locked regions`()
    @Test fun `fog opacity is 0.0 for unlocked regions`()
}

// RegionUnlockManagerTest.kt
class RegionUnlockManagerTest {
    @Test fun `unlocking region persists to database`()
    @Test fun `adjacent regions unlock after level complete`()
    @Test fun `non-adjacent regions remain locked`()
    @Test fun `unlock status survives app restart`()
    @Test fun `unlock triggers achievement check`()
}
```

#### 集成测试 (5个)

```kotlin
// WorldMapIntegrationTest.kt
@HiltAndroidTest
class WorldMapIntegrationTest {
    @Test fun `complete level unlocks adjacent regions`()
    @Test fun `region unlock persists across app restart`()
    @Test fun `fog reveals correctly on map navigation`()
    @Test fun `ship position updates on region unlock`()
    @Test fun `achievements trigger on milestones`()
}
```

#### UI测试 (5个)

```kotlin
// WorldMapScreenTest.kt
class WorldMapScreenTest {
    @Test fun `tapping unlocked region navigates to level select`()
    @Test fun `tapping locked region shows locked dialog`()
    @Test fun `fog overlay renders correctly`()
    @Test fun `ship animates to new region on unlock`()
    @Test fun `world view toggle switches map modes`()
}
```

---

### 3.2 视觉反馈增强 (P0)

**核心组件**:
- `AnswerAnimations` - 答案动画
- `ComboIndicator` - 连击指示器
- `ConfettiEffect` - 彩花效果
- `PetReactionSystem` - 宠物反应系统

**测试用例** (15个):

#### 单元测试 (8个)

```kotlin
// AnswerAnimationsTest.kt
class AnswerAnimationsTest {
    @Test fun `correct answer triggers green flash animation`()
    @Test fun `wrong answer triggers red flash animation`()
    @Test fun `animation duration is 100ms for flash`()
    @Test fun `animation duration is 500ms for detailed feedback`()
    @Test fun `stars animate sequentially with 100ms delay`()
}

// ComboIndicatorTest.kt
class ComboIndicatorTest {
    @Test fun `combo updates on consecutive correct answers`()
    @Test fun `combo resets on wrong answer`()
    @Test fun `combo does not increment on guessing`()
    @Test fun `3 combo triggers small confetti`()
    @Test fun `5 combo triggers medium fireworks`()
}
```

#### UI测试 (7个)

```kotlin
// FeedbackAnimationTest.kt
class FeedbackAnimationTest {
    @Test fun `correct answer shows green color flash`()
    @Test fun `wrong answer shows red color flash`()
    @Test fun `stars appear one by one with animation`()
    @Test fun `combo counter updates with bounce animation`()
    @Test fun `confetti emits at 3 combo`()
    @Test fun `pet reaction displays correct emotion`()
    @Test fun `level complete triggers full screen celebration`()
}
```

---

## 4. P1功能测试需求

### 4.1 成就系统 (P1)

**成就类别**: 50个成就
- Progress (12个)
- Performance (10个)
- Combo (6个)
- Streak (6个)
- Quick Judge (8个)
- Social (8个)

**测试用例** (30个):

#### 单元测试 (20个)

```kotlin
// AchievementTriggerTest.kt
class AchievementTriggerTest {
    // Progress achievements
    @Test fun `FIRST_STEPS unlocks on first word learned`()
    @Test fun `LEVEL_ONE_MASTER unlocks on level 1 complete`()
    @Test fun `VOCABULARY_BUILDER unlocks at 10 words`()
    @Test fun `WORD_EXPERT unlocks at 50 words`()

    // Performance achievements
    @Test fun `PERFECT_SCORE unlocks on 3-star level`()
    @Test fun `HINT_FREE unlocks when no hints used`()
    @Test fun `SPEED_DEMON unlocks under 2 minutes`()

    // Combo achievements
    @Test fun `COMBO_3 unlocks at 3 consecutive correct`()
    @Test fun `COMBO_5 unlocks at 5 consecutive correct`()
    @Test fun `combo resets on wrong answer`()

    // Streak achievements
    @Test fun `DAILY_LEARNER unlocks at 2 day streak`()
    @Test fun `WEEKLY_WARRIOR unlocks at 7 day streak`()
    @Test fun `streak resets on missed day`()

    // Quick Judge achievements
    @Test fun `QUICK_JUDGE_BEGINNER unlocks on first game`()
    @Test fun `PERFECT_JUDGE unlocks on 100% accuracy`()

    // Trigger timing
    @Test fun `achievement triggers immediately on condition`()
    @Test fun `duplicate achievement does not trigger again`()
    @Test fun `achievement progress persists`()
}

// AchievementRewardTest.kt
class AchievementRewardTest {
    @Test fun `unlocking achievement grants coins`()
    @Test fun `coin amount matches difficulty tier`()
    @Test fun `unlocking achievement grants title`()
    @Test fun `unlocking achievement grants badge`()
    @Test fun `unlocking achievement grants pet`()
    @Test fun `rewards add to user inventory`()
}
```

#### 集成测试 (5个)

```kotlin
// AchievementIntegrationTest.kt
@HiltAndroidTest
class AchievementIntegrationTest {
    @Test fun `game completion triggers achievement check`()
    @Test fun `achievement unlocks persist across restart`()
    @Test fun `achievement progress updates correctly`()
    @Test fun `multiple achievements can unlock simultaneously`()
    @Test fun `achievement notification displays correctly`()
}
```

#### UI测试 (5个)

```kotlin
// AchievementScreenTest.kt
class AchievementScreenTest {
    @Test fun `achievement list displays all categories`()
    @Test fun `locked achievement shows progress bar`()
    @Test fun `unlocked achievement shows reward`()
    @Test fun `tapping achievement shows detail dialog`()
    @Test fun `achievement notification appears when unlocked`()
}
```

---

### 4.2 统计系统 (P1)

**核心组件**:
- `GameHistory` - 游戏历史
- `LevelStatistics` - 关卡统计
- `GlobalStatistics` - 全局统计

**测试用例** (35个):

#### 数据模型测试 (10个)

```kotlin
// GameHistoryTest.kt
class GameHistoryTest {
    @Test fun `game history record contains all required fields`()
    @Test fun `duration calculates correctly from start and end time`()
    @Test fun `accuracy calculates as correct divided by total`()
    @Test fun `avg response time excludes outliers`()
    @Test fun `game mode enum has all values`()
}

// LevelStatisticsTest.kt
class LevelStatisticsTest {
    @Test fun `average score calculates correctly`()
    @Test fun `overall accuracy calculates correctly`()
    @Test fun `statistics update on new game`()
    @Test fun `best time updates on faster completion`()
    @Test fun `perfect games count increments on 3-star`()
}

// GlobalStatisticsTest.kt
class GlobalStatisticsTest {
    @Test fun `total study time sums all games`()
    @Test fun `current streak increments on consecutive days`()
    @Test fun `current streak resets on missed day`()
    @Test fun `longest streak updates when current exceeds`()
    @Test fun `words mastered counts memory strength >= 85`()
}
```

#### Repository测试 (10个)

```kotlin
// GameHistoryRepositoryTest.kt
class GameHistoryRepositoryTest {
    @Test fun `insert game history persists to database`()
    @Test fun `get game history returns in descending order`()
    @Test fun `get game history by level filters correctly`()
    @Test fun `get game history by mode filters correctly`()
    @Test fun `pagination returns correct page size`()
}

// StatisticsRepositoryTest.kt
class StatisticsRepositoryTest {
    @Test fun `update level statistics recalculates aggregates`()
    @Test fun `update global statistics recalculates totals`()
    @Test fun `statistics update triggers achievement check`()
    @Test fun `concurrent updates handle correctly`()
    @Test fun `statistics query completes under 100ms`()
}
```

#### UseCase测试 (10个)

```kotlin
// RecordGameHistoryUseCaseTest.kt
class RecordGameHistoryUseCaseTest {
    @Test fun `recording game saves all three statistics`()
    @Test fun `recording game triggers achievement check`()
    @Test fun `recording game updates user progress`()
    @Test fun `error in database insert returns error result`()
    @Test fun `partial update rolls back all changes`()
}

// GetGameHistoryUseCaseTest.kt
class GetGameHistoryUseCaseTest {
    @Test fun `returns game history for user`()
    @Test fun `filter by level returns only matching games`()
    @Test fun `filter by mode returns only matching games`()
    @Test fun `filter by date range returns correct games`()
    @Test fun `empty result returns empty list`()
}
```

#### UI测试 (5个)

```kotlin
// StatisticsScreenTest.kt
class StatisticsScreenTest {
    @Test fun `global statistics card displays all metrics`()
    @Test fun `game history list paginates correctly`()
    @Test fun `level statistics card shows correct data`()
    @Test fun `tab switching updates content`()
    @Test fun `filter bar applies correct filters`()
}
```

---

### 4.3 语境化学习 (P1)

**测试用例** (20个):

#### 单元测试 (10个)

```kotlin
// ContextQuestionGeneratorTest.kt
class ContextQuestionGeneratorTest {
    @Test fun `sentence mode generates question with blank`()
    @Test fun `blank position is randomized`()
    @Test fun `sentence hint is relevant to word`()
    @Test fun `cognitive load is 80% new 20% review for level 4-6`()
    @Test fun `cognitive load is 20% new 80% review for level 7-10`()
}
```

#### 集成测试 (5个)

```kotlin
// ContextualLearningIntegrationTest.kt
@HiltAndroidTest
class ContextualLearningIntegrationTest {
    @Test fun `sentence mode loads correct words`()
    @Test fun `answer validation works with sentence context`()
    @Test fun `progress updates with sentence mode`()
    @Test fun `story mode loads consecutive sentences`()
    @Test fun `context feedback displays full sentence`()
}
```

#### UI测试 (5个)

```kotlin
// ContextualLearningScreenTest.kt
class ContextualLearningScreenTest {
    @Test fun `sentence context displays above answer area`()
    @Test fun `hint displays below sentence`()
    @Test fun `correct answer shows completed sentence`()
    @Test fun `story mode displays consecutive sentences`()
    @Test fun `progress bar tracks story completion`()
}
```

---

### 4.4 Listen Find模式 (P1)

**测试用例** (25个):

#### 单元测试 (12个)

```kotlin
// AudioQuestionGeneratorTest.kt
class AudioQuestionGeneratorTest {
    @Test fun `generates 4 image options`()
    @Test fun `correct answer is in options`()
    @Test fun `options are randomized`()
    @Test fun `distractors are from same level`()
    @Test fun `audio file path is correct`()
}

// ListenFindValidatorTest.kt
class ListenFindValidatorTest {
    @Test fun `correct image selection returns true`()
    @Test fun `wrong image selection returns false`()
    @Test fun `timeout returns false`()
    @Test fun `score calculates correctly`()
}
```

#### 集成测试 (8个)

```kotlin
// ListenFindIntegrationTest.kt
@HiltAndroidTest
class ListenFindIntegrationTest {
    @Test fun `audio plays on question start`()
    @Test fun `selecting correct image advances to next`()
    @Test fun `selecting wrong image shows feedback`()
    @Test fun `audio replay works on second tap`()
    @Test fun `images load from coil correctly`()
}
```

#### UI测试 (5个)

```kotlin
// ListenFindScreenTest.kt
class ListenFindScreenTest {
    @Test fun `audio play button displays`()
    @Test fun `4 image options display in grid`()
    @Test fun `correct selection shows green highlight`()
    @Test fun `wrong selection shows red highlight`()
    @Test fun `timer displays countdown`()
}
```

---

## 5. P2功能测试需求

### 5.1 防沉迷系统 (P2)

**测试用例** (40个):

#### 会话管理测试 (15个)

```kotlin
// SessionTimeTrackerTest.kt
class SessionTimeTrackerTest {
    @Test fun `active gameplay time is counted`()
    @Test fun `background time is not counted`()
    @Test fun `paused time is not counted`()
    @Test fun `menu time is not counted`()
    @Test fun `soft reminder triggers at 15 minutes`()
    @Test fun `hard limit triggers at 45 minutes`()
    @Test fun `daily limit blocks at 2 hours`()
    @Test fun `break reminder triggers every 5 minutes`()
    @Test fun `session state survives app restart`()
    @Test fun `time accumulates across multiple sessions`()
}
```

#### 奖励系统测试 (10个)

```kotlin
// BreakRewardCalculatorTest.kt
class BreakRewardCalculatorTest {
    @Test fun `5 minute break gives 1.2x multiplier`()
    @Test fun `15 minute break gives 1.5x multiplier`()
    @Test fun `30 minute break gives 2.0x multiplier`()
    @Test fun `1 hour break gives 3.0x multiplier`()
    @Test fun `until tomorrow gives 5.0x multiplier`()
    @Test fun `bonus applies to correct number of words`()
    @Test fun `bonus expires after word count`()
    @Test fun `break achievement unlocks correctly`()
}
```

#### 家长控制测试 (10个)

```kotlin
// ParentalControlTest.kt
class ParentalControlTest {
    @Test fun `parent pin protects settings`()
    @Test fun `wrong pin rejects changes`()
    @Test fun `soft reminder time is configurable`()
    @Test fun `hard limit time is configurable`()
    @Test fun `daily max is configurable`()
    @Test fun `bedtime mode blocks during hours`()
    @Test fun `bedtime reminder shows 30 min before`()
    @Test fun `weekly report generates correctly`()
}
```

#### UI测试 (5个)

```kotlin
// SessionLimitScreenTest.kt
class SessionLimitScreenTest {
    @Test fun `soft reminder dialog displays at 15 min`()
    @Test fun `hard limit dialog blocks at 45 min`()
    @Test fun `daily limit screen blocks at 2 hours`()
    @Test fun `break screen shows countdown`()
    @Test fun `parent dashboard displays all settings`()
}
```

---

## 6. 质量门禁

### 6.1 P0质量门禁

| 检查项 | 标准 | 验证方法 |
|--------|------|----------|
| 所有单元测试通过 | 100% | `./gradlew test` |
| 首次启动测试通过 | 无崩溃 | 真机测试 |
| logcat无FATAL | 0条 | adb logcat |
| 数据库初始化 | 成功 | 验证db文件 |
| 关键流程无ANR | 0个 | UI测试 |

### 6.2 P1质量门禁

| 检查项 | 标准 | 验证方法 |
|--------|------|----------|
| 测试覆盖率 | ≥80% | JaCoCo |
| 静态分析通过 | 0错误 | Detekt + KtLint |
| 多设备测试 | ≥2设备 | 真机测试矩阵 |
| 性能基准 | 无退化 | Macrobenchmark |

### 6.3 P2质量门禁

| 检查项 | 标准 | 验证方法 |
|--------|------|----------|
| TDD覆盖率 | ≥50% | 代码审查 |
| UI自动化 | 关键流程 | Compose Test |
| E2E测试 | 完整流程 | Test Lab |

---

## 7. 测试工具和框架

### 7.1 单元测试

```kotlin
// build.gradle.kts
dependencies {
    // 测试框架
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.truth:truth:1.1.5")

    // Flow测试
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
```

### 7.2 集成测试

```kotlin
// build.gradle.kts
dependencies {
    // Android测试
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Hilt测试
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")

    // Room测试
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

### 7.3 UI测试

```kotlin
// build.gradle.kts
dependencies {
    // Compose测试
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.1")

    // Navigation测试
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.6")
}
```

### 7.4 性能测试

```kotlin
// build.gradle.kts
dependencies {
    // Macrobenchmark
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.2")
}

// benchmark/build.gradle.kts
dependencies {
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.2")
}
```

---

## 8. 测试实施计划

### 8.1 Phase 1: 覆盖率补充 (Week 1-2)

**目标**: 将整体覆盖率从21%提升至50%

**任务**:
1. Data层Repository测试 (3天)
   - GameHistoryRepository
   - StatisticsRepository
   - AchievementRepository

2. ViewModel测试 (2天)
   - LearningViewModel补充测试
   - QuickJudgeViewModel补充测试
   - StatisticsViewModel新测试

3. DAO集成测试 (2天)
   - GameHistoryDao
   - LevelStatisticsDao
   - AchievementDao

**交付物**:
- 60个新单元测试
- 15个新集成测试
- 覆盖率报告

### 8.2 Phase 2: P0功能测试 (Week 3-4)

**目标**: 地图系统+视觉反馈的完整测试

**任务**:
1. 地图系统测试 (3天)
   - VisibilityCalculator测试
   - FogOverlay测试
   - WorldMapScreen UI测试

2. 视觉反馈测试 (2天)
   - AnswerAnimations测试
   - ComboIndicator测试
   - ConfettiEffect测试

**交付物**:
- 25个地图系统测试
- 15个视觉反馈测试
- UI测试报告

### 8.3 Phase 3: P1功能测试 (Week 5-7)

**目标**: 成就+统计+语境化+Listen Find测试

**任务**:
1. 成就系统测试 (3天)
2. 统计系统测试 (3天)
3. 语境化学习测试 (2天)
4. Listen Find测试 (3天)

**交付物**:
- 30个成就系统测试
- 35个统计系统测试
- 20个语境化测试
- 25个Listen Find测试

### 8.4 Phase 4: P2功能+自动化 (Week 8-9)

**目标**: 防沉迷+完整自动化

**任务**:
1. 防沉迷系统测试 (4天)
2. UI自动化完善 (2天)
3. CI/CD集成 (1天)

**交付物**:
- 40个防沉迷测试
- 完整UI自动化套件
- CI/CD测试流水线

---

## 9. 测试自动化

### 9.1 CI/CD集成

```yaml
# .github/workflows/test.yml
name: Test Suite

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - checkout
      - setup Java 17
      - run: ./gradlew test
      - run: ./gradlew jacocoTestReport
      - upload coverage report

  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - checkout
      - setup Java 17
      - run: ./gradlew connectedAndroidTest

  ui-tests:
    runs-on: macos-latest
    steps:
      - checkout
      - setup Java 17
      - run: ./gradlew connectedCheck
      - run: ./gradlew screenshotTests
```

### 9.2 覆盖率门禁

```kotlin
// build.gradle.kts
tasks.jacocoTestReport {
    dependsOn(tasks.test)

    doLast {
        val report = file("app/build/reports/jacoco/test/html/index.html")
        val coverage = extractCoverage(report)

        if (coverage < 0.80) {
            throw GradleException("Coverage $coverage is below 80%")
        }
    }
}
```

---

## 10. 风险与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| UI测试不稳定 | 高 | 中 | 使用稳定的ID，重试机制 |
| 真机测试设备不足 | 中 | 低 | 使用Firebase Test Lab |
| 覆盖率目标无法达成 | 高 | 低 | 调整目标或分层要求 |
| 新功能测试时间不足 | 高 | 中 | 优先P0功能 |

---

**文档版本**: 1.0
**状态**: 待评审
**下一步**: 团队评审确认
