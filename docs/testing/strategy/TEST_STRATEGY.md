# Wordland测试策略

**版本**: 1.0
**日期**: 2026-02-16
**范围**: Wordland KET App

---

## 🎯 测试目标

### 质量目标

| 指标 | 目标 | 当前 | 状态 |
|------|------|------|------|
| 整体测试覆盖率 | ≥80% | 84.6% | ✅ 已达成 |
| Domain层覆盖率 | ≥90% | 待测 | 🔄 进行中 |
| Data层覆盖率 | ≥80% | 待测 | 🔄 进行中 |
| UI层覆盖率 | ≥60% | ~10% | 🔄 进行中 |
| CI通过率 | 100% | 100% | ✅ 已达成 |

### Epic #12 更新 (2026-03-08)

**截图测试方案** (Epic #12 决策):
- ❌ **Paparazzi**: 已放弃（JDK 17 不兼容）
- ✅ **Compose Testing**: 组件逻辑验证
- ✅ **ADB Screenshot Scripts**: 真机截图捕获
- ✅ **Manual Visual QA**: 人工视觉验证

详见: [ADR 003: Screenshot Testing Approach](../../adr/003-screenshot-testing-approach.md)

---

## 📊 测试金字塔

```
        /\
       /E2E \  ← 10% (真机端到端测试)
     /------\
    /集成  \ ← 20% (数据库、Repository、UseCase集成)
   /--------\
  /  单元   \ ← 70% (UseCase、算法、ViewModel)
 /__________\
```

### 测试分层说明

#### 1. 单元测试 (70%)
**目标**: 快速、可靠、可维护

**覆盖范围**:
- ✅ **UseCase**: 所有业务逻辑
- ✅ **算法**: MemoryStrengthAlgorithm, GuessingDetector
- ✅ **ViewModel**: UI状态管理逻辑
- ✅ **Repository**: 内存数据库测试
- ✅ **Domain Model**: 数据验证逻辑

**不测试**:
- ❌ Android Framework依赖（需要Android Test）
- ❌ UI组件（需要UI Test）
- ❌ 网络调用（需要Mock）

**示例**:
```kotlin
@Test
fun `correct answer increases memory strength`() = runTest {
    // Given
    val currentStrength = 50
    val isCorrect = true
    val isGuessing = false
    val difficulty = 2

    // When
    val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
        currentStrength, isCorrect, isGuessing, difficulty
    )

    // Then
    assertTrue(newStrength > currentStrength)
}
```

#### 2. 集成测试 (20%)
**目标**: 验证组件间协作

**覆盖范围**:
- ✅ 数据库操作（Room）
- ✅ Repository与DAO集成
- ✅ UseCase与Repository集成
- ✅ ViewModel + Repository集成
- ✅ 首次启动数据初始化

**测试环境**:
- In-Memory数据库
- Hilt依赖注入
- TestDispatcher（协程）

**示例**:
```kotlin
@HiltAndroidTest
class WordRepositoryIntegrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: WordRepository

    @Test
    fun `insert and retrieve word correctly`() = runTest {
        // Given
        val word = createTestWord()

        // When
        repository.insertWord(word)

        // Then
        val retrieved = repository.getWordById(word.id)
        assertEquals(word, retrieved)
    }
}
```

#### 3. UI测试 (10%)
**目标**: 验证用户交互

**覆盖范围**:
- ✅ 关键用户流程（学习、关卡选择）
- ✅ 导航流程
- ✅ 组件交互
- ✅ 状态变化
- ✅ 视觉回归检测（Epic #12）

**测试工具**:
- **Compose Testing** - 组件测试（主要）
- **Robolectric 4.13** - 集成测试
- **ADB Screenshot Scripts** - 真机截图（Epic #12）
- **Manual Visual QA** - 人工验证

**Epic #12 截图测试方案**:
```bash
# 真机截图测试
scripts/real-device/capture-screenshots.sh

# 测试场景
- app_launch    # 应用启动
- onboarding    # 新手引导
- learning_flow # 学习流程
- match_game    # 单词配对
- progress_save # 进度保存
```

详见: [Paparazzi 替代方案文档](../../reports/quality/PAPARAZZI_ALTERNATIVES.md)

**示例**:
```kotlin
@Test
fun `clicking level card navigates to learning screen`() {
    composeTestRule.setContent {
        LevelSelectScreen(
            islandId = "look_island",
            onNavigateBack = { },
            onStartLevel = { }
        )
    }

    composeTestRule
        .onNodeWithText("Level 1")
        .performClick()

    // Verify navigation
    // ...
}
```

---

## 📋 测试分类

### 1. 单元测试

#### UseCase测试

**目的**: 验证业务逻辑正确性

**示例**:
```kotlin
class SubmitAnswerUseCaseTest {
    @Test
    fun `correct answer returns success with isCorrect=true`() = runTest {
        // Given
        val word = createTestWord("look", "观看")
        val userAnswer = "look"

        // When
        val result = submitAnswerUseCase(userId, wordId, userAnswer, ...)

        // Then
        assertTrue(result is Result.Success)
        assertTrue(result.data.isCorrect)
    }
}
```

#### 算法测试

**目的**: 验证核心算法逻辑

**示例**:
```kotlin
class MemoryStrengthAlgorithmTest {
    @Test
    fun `correct answer increases memory strength`() {
        val currentStrength = 50
        val newStrength = calculateNewStrength(
            currentStrength = currentStrength,
            isCorrect = true,
            isGuessing = false,
            wordDifficulty = 2
        )
        assertTrue(newStrength > currentStrength)
    }
}
```

#### ViewModel测试

**目的**: 验证UI状态管理

**示例**:
```kotlin
class LearningViewModelTest {
    @Test
    fun `loadLevel updates uiState to Ready when words loaded`() = runTest {
        // Given
        val words = listOf(createTestWord())
        coEvery { loadLevelWords(levelId) } returns Result.Success(words)

        // When
        viewModel.loadLevel(levelId, islandId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is LearningUiState.Ready)
    }
}
```

### 2. 集成测试

#### 数据库集成测试

**目的**: 验证Room数据库操作

**示例**:
```kotlin
@RunWith(AndroidJUnit4::class)
class WordDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val inMemoryDb = Room.inMemoryDatabaseBuilder().build()

    private lateinit var wordDao: WordDao

    @Before
    fun setup() {
        wordDao = inMemoryDb.wordDao()
    }

    @Test
    fun `insert and retrieve word`() {
        val word = createTestWord()
        wordDao.insert(word)

        val retrieved = wordDao.getWordById(word.id)
        assertEquals(word, retrieved)
    }
}
```

#### Repository集成测试

**目的**: 验证Repository与DAO协作

**示例**:
```kotlin
@HiltAndroidTest
class WordRepositoryIntegrationTest {
    @Inject lateinit var repository: WordRepository

    @Test
    fun `loadWords returns data from database`() = runTest {
        // Given
        val words = listOf(createTestWord())
        database.wordDao().insertAll(words)

        // When
        val result = repository.getWordsByLevel("level_1")

        // Then
        assertTrue(result is Result.Success)
        assertEquals(words, result.data)
    }
}
```

### 3. UI测试

#### Compose UI测试

**目的**: 验证Compose组件行为

**示例**:
```kotlin
class LevelSelectScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `clicking level card navigates correctly`() {
        var navigated = false
        val levelId = "level_1"

        composeTestRule.setContent {
            LevelSelectScreen(
                islandId = "look_island",
                onNavigateBack = {},
                onStartLevel = { id ->
                    navigated = true
                    assertEquals(levelId, id)
                }
            )
        }

        composeTestRule
            .onNodeWithText("Level 1")
            .performClick()

        assertTrue(navigated)
    }
}
```

#### 真机E2E测试

**目的**: 验证完整用户流程

**测试场景**:
1. 首次启动流程
2. 完整学习流程
3. 关卡解锁流程
4. 进度保存流程

---

## 🎯 测试场景

### 关键用户流程

#### 场景1: 首次启动

**测试步骤**:
1. 应用启动
2. Application.onCreate()执行
3. 数据库初始化
4. Level 1解锁

**验证点**:
- ✅ 数据库已创建
- ✅ 数据库已填充
- ✅ Level 1状态为UNLOCKED
- ✅ 无crash

**测试脚本**: `scripts/test/test_first_launch.sh`

#### 场景2: 学习流程

**测试步骤**:
1. 进入关卡
2. 显示单词
3. 提交答案
4. 查看反馈
5. 进入下一题

**验证点**:
- ✅ 单词显示正确
- ✅ 答案验证正确
- ✅ 星级计算正确
- ✅ 进度保存
- ✅ 下一题加载

#### 场景3: 关卡解锁

**测试步骤**:
1. 完成Level 1所有单词
2. 自动解锁Level 2
3. 返回关卡选择
4. Level 2显示UNLOCKED

**验证点**:
- ✅ Level 2解锁
- ✅ 进度正确
- ✅ 数据持久化

---

## 📁 测试文件组织

### 目录结构

```
app/src/
├── test/                                   # 单元测试
│   └── java/com/wordland/
│       ├── domain/
│       │   ├── algorithm/
│       │   │   └── MemoryStrengthAlgorithmTest.kt
│       │   ├── usecase/usecases/
│       │   │   ├── SubmitAnswerUseCaseTest.kt
│       │   │   └── ...
│       │   └── model/
│       └── ui/
│           └── viewmodel/
│               └── LearningViewModelTest.kt
│
└── androidTest/                            # 集成测试
    └── java/com/wordland/
        ├── data/
        │   ├── dao/
        │   │   └── WordDaoTest.kt
        │   └── repository/
        │       └── WordRepositoryIntegrationTest.kt
        └── ui/
            └── screens/
                └── LevelSelectScreenTest.kt
```

---

## 🔧 测试工具

### 单元测试

**依赖**:
```kotlin
// build.gradle.kts
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.5")
}
```

**工具说明**:
- **JUnit**: 测试框架
- **MockK**: Mock框架
- **Kotlin Coroutines Test**: 协程测试支持
- **Turbine**: Flow测试工具
- **Truth**: 断言库

### 集成测试

**依赖**:
```kotlin
// build.gradle.kts
dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
}
```

**工具说明**:
- **AndroidX Test**: JUnit扩展
- **Espresso**: UI测试框架
- **Compose Testing**: Compose UI测试
- **Hilt Testing**: 依赖注入

---

## 📝 测试编写规范

### 命名规范

**测试类命名**: `<被测类>Test`
```kotlin
class SubmitAnswerUseCaseTest // ✅
class submit_answer_usecase_test // ❌
```

**测试方法命名**: `<操作> should <预期结果>`
```kotlin
@Test
fun `correct answer should increase memory strength`() // ✅
fun testCorrectAnswer() // ❌ 不够描述性
```

### AAA模式

```kotlin
@Test
fun `answer validation should compare with word field`() = runTest {
    // Arrange（准备）
    val word = createTestWord("look", "观看")
    val userAnswer = "look"

    // Act（执行）
    val result = submitAnswerUseCase(userId, word.id, userAnswer, ...)

    // Assert（验证）
    assertTrue(result.data.isCorrect)
}
```

### Given-When-Then

```kotlin
@Test
fun `complete level should unlock next level`() = runTest {
    // Given
    val currentLevelId = "level_1"
    val userId = "user_001"

    // When
    viewModel.completeLevel()
    advanceUntilIdle()

    // Then
    val nextLevel = progressRepository.getLevelStatus(userId, "level_2")
    assertEquals(LevelStatus.UNLOCKED, nextLevel.status)
}
```

---

## 🚀 CI/CD集成

### 自动化测试流程

**GitHub Actions** (`.github/workflows/`):

| Workflow | 用途 | 触发条件 |
|----------|------|----------|
| `ci.yml` | 主 CI 流水线 | Push/PR |
| `ui-tests.yml` | UI 自动化测试 | UI 文件变更 |
| `device-matrix.yml` | 多设备测试矩阵 | Push to main |

**主 CI 流程** (`ci.yml`):
1. 推送代码
2. 自动运行单元测试
3. 生成覆盖率报告
4. 运行 Lint 检查
5. 编译 APK

**UI 测试流程** (`ui-tests.yml`) - Epic #12 新增:
1. Compose UI 测试
2. 截图脚本生成
3. 真机测试（自托管 runner）
4. 视觉回归检查
5. 测试摘要报告

**设备矩阵测试** (`device-matrix.yml`) - Epic #12 新增:
- Pixel 7 (API 33, 6.3")
- Samsung S23 (API 33, 6.1")
- Small Phone (API 29, 5.5")
- Tablet (API 33, 10")

**质量门禁** (`.github/workflows/quality-gate.yml`):
1. PR 创建时触发
2. 运行所有测试
3. 检查覆盖率 ≥ 80%
4. 阻止低质量代码合并

详见: [Epic #12 CI/CD 指南](../../guides/testing/EPIC12_CI_CD_GUIDE.md)

---

## 📊 测试覆盖率

### 当前状态

| 模块 | 覆盖率 | 测试数 |
|------|--------|--------|
| Domain.Algorithm | ~83% | 10/12 |
| Domain.UseCase | ~60% | 3/5 |
| UI.ViewModel | 0% | 0/6 |
| Data.Repository | 未测 | 0/3 |
| **整体** | **84.6%** | **22/26** |

### 测量命令

**生成覆盖率报告**:
```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/test/html/index.html
```

**查看覆盖率**:
```bash
./gradlew jacocoTestReport
cat app/build/reports/jacoco/test/html/index.html | grep -oP 'Instruction Coverage: \K[0-9.]+%'
```

---

## 🎓 测试最佳实践

### 1. 测试隔离

**原则**: 每个测试应该独立

**错误示例**:
```kotlin
// ❌ 测试之间有依赖
var sharedState = GlobalState

@Test
fun test1() {
    sharedState = "A"
}

@Test
fun test2() {
    // 假设sharedState是初始值，但实际是"A"
    assertEquals("", sharedState) // ❌ 失败
}
```

**正确示例**:
```kotlin
// ✅ 每个测试独立
@Before
fun setup() {
    // 重置状态
}
```

### 2. 使用Mock

**原则**: 隔离外部依赖

**示例**:
```kotlin
@Test
fun `loadWords from repository`() {
    // Given
    val words = listOf(createTestWord())
    val repository = mockk<WordRepository>()
    coEvery { repository.getWordsByLevel(any()) } returns words

    // When
    val useCase = LoadLevelWordsUseCase(repository)
    val result = useCase("level_1")

    // Then
    assertTrue(result is Result.Success)
}
```

### 3. 测试边界条件

**需要测试的边界**:
- 空列表
- null值
- 最大/最小值
- 错误情况
- 极端输入

**示例**:
```kotlin
@Test
fun `handle empty word list correctly`() {
    // Given
    val emptyList = emptyList<Word>()

    // When
    val result = useCase.processWords(emptyList)

    // Then
    assertTrue(result is Result.Error)
    assertTrue(result.exception is NoSuchElementException)
}
```

### 4. 避免测试实现细节

**原则**: 测试行为，不是实现

**错误示例**:
```kotlin
// ❌ 测试实现细节
@Test
fun `uses bubble sort algorithm`() {
    // 验证使用了冒泡排序
    // 但实际算法改变后测试会失败
}
```

**正确示例**:
```kotlin
// ✅ 测试行为
@Test
fun `sorts words in correct order`() {
    val unsorted = listOf(word3, word1, word2)
    val sorted = sortWords(unsorted)
    assertEquals(listOf(word1, word2, word3), sorted)
}
```

---

## 📚 相关文档

### 内部文档

- [Code Review Checklist](../guides/CODE_REVIEW_CHECKLIST.md)
- [静态分析工具指南](../guides/STATIC_ANALYSIS_GUIDE.md)
- [CI/CD设置](../guides/CI_CD_SETUP.md)

### 外部资源

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Testing Basics](https://developer.android.com/training/testing/fundamentals)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Test Lab](https://developer.android.com/training/testing/test-lab)

---

**最后更新**: 2026-03-08 (Epic #12 截图测试方案)
**维护者**: android-architect, android-test-engineer
**相关 ADR**: [ADR 003: Screenshot Testing Approach](../../adr/003-screenshot-testing-approach.md)
