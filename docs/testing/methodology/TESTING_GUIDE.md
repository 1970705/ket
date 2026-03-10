# Wordland测试指南

**版本**: 1.0
**日期**: 2026-02-16
**目标读者**: 新加入团队的Android开发者

---

## 📖 概述

本指南帮助新人快速了解Wordland项目的测试体系，包括：
- 如何运行现有测试
- 如何编写新的测试
- 测试工具和框架的使用
- 测试最佳实践

**前置知识**:
- Kotlin基础语法
- Android开发基础
- JUnit测试框架基础

**学习时间**: 约2小时

---

## 🚀 快速开始

### 第一步：环境准备

确保已安装以下工具：
```bash
# 检查Java版本
java -version  # 应该是Java 17

# 检查Gradle
./gradlew --version

# 检查Git
git --version
```

### 第二步：克隆项目并运行测试

```bash
# 1. 克隆项目（如果还没有）
git clone <repository-url>
cd ket

# 2. 运行所有测试
./gradlew test

# 3. 查看测试报告
open app/build/reports/tests/testDebugUnitTest/index.html
```

**预期结果**: 所有测试应该通过（✅）

---

## 📁 测试文件结构

理解测试文件的位置至关重要：

```
app/src/
├── test/                                    # 单元测试（本地JVM）
│   └── java/com/wordland/
│       ├── domain/
│       │   ├── algorithm/
│       │   │   ├── MemoryStrengthAlgorithmTest.kt
│       │   │   └── GuessingDetectorTest.kt
│       │   └── usecase/
│       │       └── usecases/
│       │           └── SubmitAnswerUseCaseTest.kt
│       └── ui/
│           └── viewmodel/
│               └── LearningViewModelTest.kt
│
└── androidTest/                             # 集成测试（Android设备/模拟器）
    └── java/com/wordland/
        ├── data/
        │   ├── dao/
        │   │   └── WordDaoTest.kt
        │   └── repository/
        │       └── WordRepositoryTest.kt
        └── ui/
            └── screens/
                └── LearningScreenTest.kt
```

**关键规则**:
- ✅ **单元测试** → `app/src/test/` - 快速，无需Android设备
- ✅ **集成测试** → `app/src/androidTest/` - 需要Android设备或模拟器

---

## 🛠️ 测试工具与框架

### 1. JUnit 4 - 测试框架

**作用**: 提供测试运行和断言

**基本用法**:
```kotlin
import org.junit.Test
import org.junit.Assert.*

class MyTest {

    @Test
    fun `addition is correct`() {
        // Given
        val a = 2
        val b = 3

        // When
        val result = a + b

        // Then
        assertEquals(5, result)
    }
}
```

**常用注解**:
- `@Test` - 标记测试方法
- `@Before` - 每个测试前执行
- `@After` - 每个测试后执行
- `@Ignore` - 跳过测试

---

### 2. MockK - Mock框架

**作用**: 模拟依赖对象，隔离测试

**示例场景**:
测试 `SubmitAnswerUseCase`，但不依赖真实的 `WordRepository`。

**基本用法**:
```kotlin
import io.mockk.*
import com.wordland.data.repository.WordRepository

class SubmitAnswerUseCaseTest {

    private lateinit var repository: WordRepository
    private lateinit var useCase: SubmitAnswerUseCase

    @Before
    fun setup() {
        // 创建Mock对象
        repository = mockk()

        // 创建被测对象，注入Mock
        useCase = SubmitAnswerUseCase(repository)
    }

    @Test
    fun `correct answer calls repository update`() = runTest {
        // Given - 配置Mock行为
        val word = createTestWord()
        coEvery { repository.getWordById(any()) } returns word

        // When - 调用被测方法
        useCase.submitAnswer(userId, wordId, "look")

        // Then - 验证Mock被调用
        coVerify { repository.updateProgress(any(), any()) }
    }
}
```

**常用MockK方法**:
- `mockk<T>()` - 创建Mock对象
- `coEvery { } returns ...` - 配置协程行为
- `coVerify { }` - 验证协程调用
- `slot<T>()` - 捕获参数

---

### 3. Kotlin Coroutines Test - 协程测试

**作用**: 测试协程代码，控制时间流逝

**基本用法**:
```kotlin
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle

class LoadLevelWordsUseCaseTest {

    @Test
    fun `load words completes successfully`() = runTest {
        // Given
        coEvery { repository.getWordsByLevel(any()) } returns words

        // When - 在runTest中执行协程
        val result = useCase.loadWords("level_1")

        // Then - 等待所有协程完成
        advanceUntilIdle()
        assertTrue(result.isSuccess)
    }
}
```

---

### 4. Turbine - Flow测试工具

**作用**: 测试Kotlin Flow数据流

**基本用法**:
```kotlin
import app.cash.turbine.test

class LearningViewModelTest {

    @Test
    fun `uiState updates correctly`() = runTest {
        // When
        viewModel.loadLevel("level_1")

        // Then - 测试Flow
        viewModel.uiState.test {
            // 第一个 emissions
            val state1 = awaitItem()
            assertTrue(state1 is LearningUiState.Loading)

            // 第二个 emissions
            val state2 = awaitItem()
            assertTrue(state2 is LearningUiState.Ready)

            // 确保没有更多数据
            awaitComplete()
        }
    }
}
```

---

### 5. Room Testing - 数据库测试

**作用**: 测试Room数据库操作

**基本用法**:
```kotlin
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

@RunWith(AndroidJUnit4::class)
class WordDaoTest {

    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao

    @Before
    fun setup() {
        // 使用In-Memory数据库（测试后自动删除）
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WordDatabase::class.java
        ).build()

        wordDao = database.wordDao()
    }

    @Test
    fun `insert and retrieve word`() {
        // Given
        val word = Word(id = "1", english = "look", chinese = "观看")

        // When
        wordDao.insert(word)

        // Then
        val retrieved = wordDao.getWordById("1")
        assertEquals(word, retrieved)
    }

    @After
    fun teardown() {
        database.close()
    }
}
```

---

## 📝 测试编写规范

### 命名规范

#### 1. 测试类命名

**格式**: `<被测类>Test`

```kotlin
// ✅ 正确
class MemoryStrengthAlgorithmTest
class SubmitAnswerUseCaseTest
class LearningViewModelTest

// ❌ 错误
class MemoryStrengthTest  // 太模糊
class TestMemoryStrengthAlgorithm  // Test后缀位置错误
```

#### 2. 测试方法命名

**格式**: ``<操作> should <预期结果>``

**使用反引号使名称可读**:
```kotlin
@Test
fun `correct answer should increase memory strength`() {
    // ✅ 清晰描述测试意图
}

@Test
fun testCorrectAnswer() {
    // ❌ 不够描述性
}
```

**其他推荐格式**:
```kotlin
// Given-When-Then风格
@Test
fun `given correct answer when submitted then should increase strength`() {
    // ...
}

// 场景描述风格
@Test
fun `loading empty level returns empty list`() {
    // ...
}
```

---

### AAA模式（Arrange-Act-Assert）

每个测试应该分为三个部分：

```kotlin
@Test
fun `correct answer increases memory strength`() = runTest {
    // ========== Arrange（准备） ==========
    val currentStrength = 50
    val isCorrect = true
    val isGuessing = false
    val difficulty = 2

    // ========== Act（执行） ==========
    val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
        currentStrength, isCorrect, isGuessing, difficulty
    )

    // ========== Assert（验证） ==========
    assertTrue(newStrength > currentStrength)
    assertEquals(60, newStrength)  // 50 + 10 (correct bonus)
}
```

**为什么使用AAA**:
- ✅ 清晰的测试结构
- ✅ 易于理解和维护
- ✅ 快速定位问题

---

### Given-When-Then变体

更适合行为描述的测试：

```kotlin
@Test
fun `complete level should unlock next level`() = runTest {
    // Given - 设置前置条件
    val currentLevelId = "level_1"
    val userId = "user_001"
    coEvery { progressRepository.getLevelStatus(userId, "level_2") }
        returns LevelStatus.LOCKED

    // When - 执行操作
    viewModel.completeLevel()
    advanceUntilIdle()

    // Then - 验证结果
    val nextLevel = progressRepository.getLevelStatus(userId, "level_2")
    assertEquals(LevelStatus.UNLOCKED, nextLevel.status)
}
```

---

## 🎯 如何编写测试

### 场景1：测试纯函数（最简单）

**被测代码**:
```kotlin
// MemoryStrengthAlgorithm.kt
object MemoryStrengthAlgorithm {
    fun calculateNewStrength(
        currentStrength: Int,
        isCorrect: Boolean,
        isGuessing: Boolean,
        difficulty: Int
    ): Int {
        val change = if (isCorrect) 10 else -15
        var newStrength = currentStrength + change
        if (isGuessing) newStrength -= 5
        return newStrength.coerceIn(0, 100)
    }
}
```

**测试代码**:
```kotlin
// MemoryStrengthAlgorithmTest.kt
class MemoryStrengthAlgorithmTest {

    @Test
    fun `correct answer increases memory strength`() {
        // Given
        val current = 50

        // When
        val result = MemoryStrengthAlgorithm.calculateNewStrength(
            currentStrength = current,
            isCorrect = true,
            isGuessing = false,
            difficulty = 2
        )

        // Then
        assertEquals(60, result)  // 50 + 10
    }

    @Test
    fun `memory strength clamps to max value`() {
        // Given
        val current = 95

        // When
        val result = MemoryStrengthAlgorithm.calculateNewStrength(
            currentStrength = current,
            isCorrect = true,
            isGuessing = false,
            difficulty = 2
        )

        // Then
        assertEquals(100, result)  // 不超过最大值
    }
}
```

**关键点**:
- ✅ 无需Mock（纯函数）
- ✅ 测试各种边界值
- ✅ 验证所有分支逻辑

---

### 场景2：测试UseCase（需要Mock）

**被测代码**:
```kotlin
// SubmitAnswerUseCase.kt
class SubmitAnswerUseCase(
    private val repository: WordRepository
) {
    suspend operator fun invoke(
        userId: String,
        wordId: String,
        userAnswer: String
    ): Result<SubmitAnswerResult> {
        val word = repository.getWordById(wordId)
            ?: return Result.Error(WordNotFoundException(wordId))

        val isCorrect = word.english.equals(userAnswer, ignoreCase = true)
        val isGuessing = detectGuessing(/* ... */)

        repository.updateProgress(userId, wordId, isCorrect, isGuessing)

        return Result.Success(SubmitAnswerResult(isCorrect, isGuessing))
    }
}
```

**测试代码**:
```kotlin
// SubmitAnswerUseCaseTest.kt
class SubmitAnswerUseCaseTest {

    private lateinit var repository: WordRepository
    private lateinit var useCase: SubmitAnswerUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SubmitAnswerUseCase(repository)
    }

    @Test
    fun `correct answer returns success with isCorrect=true`() = runTest {
        // Given
        val word = Word(id = "1", english = "look", chinese = "观看")
        coEvery { repository.getWordById("1") } returns word

        // When
        val result = useCase("user_001", "1", "look")

        // Then
        assertTrue(result is Result.Success)
        assertTrue(result.data.isCorrect)

        // 验证repository被调用
        coVerify {
            repository.updateProgress(
                userId = "user_001",
                wordId = "1",
                isCorrect = true,
                isGuessing = any()
            )
        }
    }

    @Test
    fun `word not found returns error`() = runTest {
        // Given
        coEvery { repository.getWordById("999") } returns null

        // When
        val result = useCase("user_001", "999", "look")

        // Then
        assertTrue(result is Result.Error)
        assertTrue(result.exception is WordNotFoundException)
    }
}
```

**关键点**:
- ✅ Mock Repository依赖
- ✅ 测试成功和失败场景
- ✅ 验证Mock调用（`coVerify`）

---

### 场景3：测试ViewModel（需要Mock和Flow）

**被测代码**:
```kotlin
// LearningViewModel.kt
class LearningViewModel(
    private val loadLevelWords: LoadLevelWordsUseCase,
    private val submitAnswer: SubmitAnswerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Loading)
    val uiState: StateFlow<LearningUiState> = _uiState

    fun loadLevel(levelId: String, islandId: String) {
        viewModelScope.launch {
            _uiState.value = LearningUiState.Loading

            when (val result = loadLevelWords(levelId)) {
                is Result.Success -> {
                    _uiState.value = LearningUiState.Ready(result.data)
                }
                is Result.Error -> {
                    _uiState.value = LearningUiState.Error(result.exception.message)
                }
            }
        }
    }
}
```

**测试代码**:
```kotlin
// LearningViewModelTest.kt
class LearningViewModelTest {

    private lateinit var loadLevelWords: LoadLevelWordsUseCase
    private lateinit var submitAnswer: SubmitAnswerUseCase
    private lateinit var viewModel: LearningViewModel

    @Before
    fun setup() {
        loadLevelWords = mockk()
        submitAnswer = mockk()
        viewModel = LearningViewModel(loadLevelWords, submitAnswer)
    }

    @Test
    fun `loadLevel updates uiState to Ready when words loaded`() = runTest {
        // Given
        val words = listOf(
            Word(id = "1", english = "look", chinese = "观看"),
            Word(id = "2", english = "watch", chinese = "观看")
        )
        coEvery { loadLevelWords("level_1") } returns Result.Success(words)

        // When
        viewModel.loadLevel("level_1", "look_island")
        advanceUntilIdle()  // 等待所有协程完成

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is LearningUiState.Ready)
        assertEquals(2, state.words.size)
    }

    @Test
    fun `loadLevel updates uiState to Error on failure`() = runTest {
        // Given
        coEvery { loadLevelWords("level_1") } returns
            Result.Error(Exception("Network error"))

        // When
        viewModel.loadLevel("level_1", "look_island")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is LearningUiState.Error)
        assertEquals("Network error", state.message)
    }
}
```

**关键点**:
- ✅ Mock所有UseCase依赖
- ✅ 使用 `advanceUntilIdle()` 等待协程
- ✅ 测试 `StateFlow` 状态变化
- ✅ 测试成功和失败场景

---

## 🏃 运行测试

### 基本命令

```bash
# 运行所有单元测试
./gradlew test

# 运行所有集成测试（需要设备/模拟器）
./gradlew connectedAndroidTest

# 运行特定类的测试
./gradlew test --tests "com.wordland.domain.algorithm.MemoryStrengthAlgorithmTest"

# 运行特定测试方法
./gradlew test --tests "*.MemoryStrengthAlgorithmTest.correct answer increases memory strength"
```

### 生成测试报告

```bash
# 运行测试并生成报告
./gradlew test

# 打开HTML报告
open app/build/reports/tests/testDebugUnitTest/index.html

# 生成覆盖率报告
./gradlew jacocoTestReport

# 打开覆盖率报告
open app/build/reports/jacoco/test/html/index.html
```

### Android Studio中运行

**运行单个测试**:
1. 打开测试文件
2. 点击方法左侧的绿色运行按钮▶️
3. 选择 "Run"

**运行整个测试类**:
1. 打开测试文件
2. 点击类名左侧的绿色运行按钮▶️
3. 选择 "Run"

---

## ✅ 测试检查清单

在提交代码前，确保：

### 测试完整性
- [ ] 测试了正常场景（Happy Path）
- [ ] 测试了错误场景（Error Path）
- [ ] 测试了边界条件（Edge Cases）
- [ ] 验证了Mock对象调用

### 测试质量
- [ ] 测试名称清晰描述意图
- [ ] 使用AAA或Given-When-Then结构
- [ ] 无重复代码（提取公共setup）
- [ ] 测试独立（无相互依赖）

### 测试运行
- [ ] 所有新测试通过
- [ ] 所有现有测试未破坏
- [ ] 覆盖率未下降
- [ ] 测试运行时间合理（<5秒）

---

## 🐛 常见问题与解决

### Q1: 测试报错 "Unresolved reference: test"

**原因**: 缺少测试依赖

**解决**: 确保 `build.gradle.kts` 中有：
```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

---

### Q2: 协程测试不工作

**原因**: 没有使用 `runTest`

**解决**:
```kotlin
// ❌ 错误
@Test
fun myTest() {
    viewModelScope.launch { /* ... */ }
    // 测试可能在协程完成前就结束了
}

// ✅ 正确
@Test
fun myTest() = runTest {
    viewModelScope.launch { /* ... */ }
    advanceUntilIdle()  // 等待协程完成
}
```

---

### Q3: Mock对象不工作

**原因**: 使用了真实对象而非Mock

**解决**:
```kotlin
// ❌ 错误：使用了真实对象
@Before
fun setup() {
    repository = WordRepository()  // 真实对象
}

// ✅ 正确：使用Mock
@Before
fun setup() {
    repository = mockk<WordRepository>()
}
```

---

### Q4: 数据库测试失败

**原因**: 没有使用In-Memory数据库

**解决**:
```kotlin
// ✅ 使用In-Memory数据库
database = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(),
    WordDatabase::class.java
).build()
```

---

## 📚 学习资源

### 内部文档
- [测试策略](./TEST_STRATEGY.md) - 了解测试金字塔和分层
- [测试用例清单](./TEST_CASES.md) - 查看所有测试用例
- [覆盖率报告](./TEST_COVERAGE_REPORT.md) - 查看当前覆盖率状态
- [TDD实践指南](./TDD_GUIDE.md) - 学习测试驱动开发

### 外部资源
- [Android Testing Guide](https://developer.android.com/training/testing)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [MockK Documentation](https://mockk.io/)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

---

## 🎯 下一步

1. **阅读现有测试** - 选择一个简单的测试文件（如 `MemoryStrengthAlgorithmTest.kt`）阅读并理解
2. **编写第一个测试** - 为一个简单函数编写测试
3. **Code Review** - 请团队成员review你的测试
4. **持续改进** - 随着经验积累，学习更高级的测试技巧

---

**祝测试愉快！** 🎉

有问题？随时向团队请教或提交Issue。

---

**最后更新**: 2026-02-16
**维护者**: Android Team
