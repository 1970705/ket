# Wordland TDD实践指南

**版本**: 1.0
**日期**: 2026-02-16
**目标读者**: 希望采用测试驱动开发的Android开发者

---

## 📖 什么是TDD？

**TDD (Test-Driven Development)** = 测试驱动开发

### 核心理念

**传统开发流程**:
```
1. 编写代码
2. 编写测试
3. 运行测试
4. 修复bug
```

**TDD流程**:
```
1. 编写测试（先写！）
2. 运行测试（失败❌）
3. 编写最少代码使测试通过
4. 重构代码
5. 重复（Red-Green-Refactor循环）
```

### 为什么使用TDD？

✅ **更清晰的代码设计** - 先思考如何使用，再实现
✅ **更高的代码质量** - 每个功能都有测试保护
✅ **更少的bug** - 问题在早期发现
✅ **更好的文档** - 测试即文档，展示代码如何使用
✅ **重构更安全** - 有测试保护，重构不害怕

---

## 🎯 TDD核心原则

### 1. Red-Green-Refactor循环

```
    ┌─────────────────────────────────────┐
    │                                     │
    ▼                                     │
┌─────────┐   ┌─────────┐   ┌──────────────┐
│   RED   │→  │  GREEN  │→  │  REFACTOR    │
│ (测试失败) │  │ (测试通过) │  │   (重构代码)  │
└─────────┘   └─────────┘   └──────────────┘
    │                             │
    └─────────────────────────────┘
              (继续下一个测试)
```

#### RED阶段（写测试）
- 为新功能编写测试
- 测试目前**一定失败**（因为功能还未实现）
- 运行测试看到失败（❌）

#### GREEN阶段（写代码）
- 编写**最少**代码使测试通过
- 不求完美，只求通过
- 运行测试看到成功（✅）

#### REFACTOR阶段（重构）
- 清理代码，改进设计
- 确保测试仍然通过
- 提交代码

---

### 2. 三条法则（Uncle Bob）

1. **不允许写任何生产代码，除非是为了使失败的测试通过**
2. **不允许写多于一个失败的测试**
3. **在当前测试失败之前，不允许写下一个测试**

---

## 🚀 TDD实战示例

### 示例1: 简单函数 - 计算记忆强度

#### 场景
我们需要实现一个函数：根据用户答题结果计算记忆强度

#### Step 1: RED - 编写第一个测试

```kotlin
// MemoryStrengthAlgorithmTest.kt
class MemoryStrengthAlgorithmTest {

    @Test
    fun `correct answer increases memory strength`() {
        // Given
        val currentStrength = 50

        // When
        val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
            currentStrength = currentStrength,
            isCorrect = true,
            isGuessing = false,
            difficulty = 2
        )

        // Then
        assertTrue(newStrength > currentStrength)
    }
}
```

**运行测试**: ❌ **失败** - `MemoryStrengthAlgorithm` 类还不存在！

---

#### Step 2: GREEN - 编写最少代码使测试通过

```kotlin
// MemoryStrengthAlgorithm.kt
object MemoryStrengthAlgorithm {
    fun calculateNewStrength(
        currentStrength: Int,
        isCorrect: Boolean,
        isGuessing: Boolean,
        difficulty: Int
    ): Int {
        // 最简单的实现：只要正确就加10
        return if (isCorrect) currentStrength + 10 else currentStrength
    }
}
```

**运行测试**: ✅ **通过**！

---

#### Step 3: REFACTOR - 改进代码

当前代码可以工作了，但让我们添加更多测试驱动设计：

**编写第二个测试** (RED):
```kotlin
@Test
fun `incorrect answer decreases memory strength`() {
    val currentStrength = 50

    val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
        currentStrength = currentStrength,
        isCorrect = false,
        isGuessing = false,
        difficulty = 2
    )

    assertTrue(newStrength < currentStrength)
}
```

**运行测试**: ❌ **失败** - 错误答案没有减少强度

**实现** (GREEN):
```kotlin
fun calculateNewStrength(/* ... */): Int {
    return if (isCorrect) {
        currentStrength + 10
    } else {
        currentStrength - 15  // 惩罚更大
    }
}
```

**运行测试**: ✅ **通过**！

**重构** (REFACTOR):
```kotlin
object MemoryStrengthAlgorithm {
    private const val CORRECT_BONUS = 10
    private const val INCORRECT_PENALTY = 15

    fun calculateNewStrength(/* ... */): Int {
        val change = if (isCorrect) CORRECT_BONUS else -INCORRECT_PENALTY
        return currentStrength + change
    }
}
```

---

#### Step 4: 继续TDD循环

**编写第三个测试** - 边界测试:
```kotlin
@Test
fun `memory strength clamps to max value`() {
    val currentStrength = 95

    val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
        currentStrength = currentStrength,
        isCorrect = true,
        isGuessing = false,
        difficulty = 2
    )

    assertEquals(100, newStrength)  // 最大值为100
}
```

**运行测试**: ❌ **失败** - 返回105，超过100

**实现边界限制** (GREEN):
```kotlin
fun calculateNewStrength(/* ... */): Int {
    val change = if (isCorrect) CORRECT_BONUS else -INCORRECT_PENALTY
    var newStrength = currentStrength + change
    return newStrength.coerceIn(0, 100)  // 限制在0-100之间
}
```

**运行测试**: ✅ **所有测试通过**！

---

### 示例2: UseCase - 提交答案

#### Step 1: RED - 编写测试

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

        // 验证进度被更新
        coVerify {
            repository.updateProgress(
                userId = "user_001",
                wordId = "1",
                isCorrect = true,
                isGuessing = any()
            )
        }
    }
}
```

**运行测试**: ❌ **失败** - `SubmitAnswerUseCase` 类不存在

---

#### Step 2: GREEN - 编写最少实现

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
        // 最简单的实现：总是返回成功
        return Result.Success(SubmitAnswerResult(isCorrect = true, isGuessing = false))
    }
}
```

**运行测试**: ❌ **还是失败** - 没有验证repository调用

等等...测试还验证了repository.updateProgress被调用！

**修改实现**:
```kotlin
suspend operator fun invoke(
    userId: String,
    wordId: String,
    userAnswer: String
): Result<SubmitAnswerResult> {
    // 调用repository以满足测试
    repository.updateProgress(userId, wordId, isCorrect = true, isGuessing = false)

    return Result.Success(SubmitAnswerResult(isCorrect = true, isGuessing = false))
}
```

**运行测试**: ✅ **通过**！

但这还不是正确的实现，继续下一个测试：

---

#### Step 3: 继续TDD - 添加验证逻辑

**编写测试** - 答案验证:
```kotlin
@Test
fun `incorrect answer returns success with isCorrect=false`() = runTest {
    // Given
    val word = Word(id = "1", english = "look", chinese = "观看")
    coEvery { repository.getWordById("1") } returns word

    // When
    val result = useCase("user_001", "1", "see")  // 错误答案

    // Then
    assertTrue(result is Result.Success)
    assertFalse(result.data.isCorrect)

    coVerify {
        repository.updateProgress(
            userId = "user_001",
            wordId = "1",
            isCorrect = false,  // 应该是false
            isGuessing = any()
        )
    }
}
```

**运行测试**: ❌ **失败** - 当前实现总是返回true

**实现正确逻辑** (GREEN):
```kotlin
suspend operator fun invoke(
    userId: String,
    wordId: String,
    userAnswer: String
): Result<SubmitAnswerResult> {
    val word = repository.getWordById(wordId)
        ?: return Result.Error(WordNotFoundException(wordId))

    val isCorrect = word.english.equals(userAnswer, ignoreCase = true)

    repository.updateProgress(userId, wordId, isCorrect, isGuessing = false)

    return Result.Success(SubmitAnswerResult(isCorrect, isGuessing = false))
}
```

**运行测试**: ✅ **所有测试通过**！

---

#### Step 4: REFACTOR - 改进设计

当前代码可以工作了，但猜测检测还没有实现。继续TDD：

**编写测试** - 猜测检测:
```kotlin
@Test
fun `fast response marks as guessing`() = runTest {
    // Given
    val word = Word(id = "1", english = "look", chinese = "观看")
    coEvery { repository.getWordById("1") } returns word

    // When
    val result = useCase.submitAnswer(
        userId = "user_001",
        wordId = "1",
        userAnswer = "look",
        responseTimeMs = 1500  // 快速响应（<2秒）
    )

    // Then
    assertTrue(result.data.isGuessing)
}
```

**实现猜测检测** (GREEN):
```kotlin
suspend operator fun invoke(
    userId: String,
    wordId: String,
    userAnswer: String,
    responseTimeMs: Long = 0L
): Result<SubmitAnswerResult> {
    val word = repository.getWordById(wordId)
        ?: return Result.Error(WordNotFoundException(wordId))

    val isCorrect = word.english.equals(userAnswer, ignoreCase = true)
    val isGuessing = responseTimeMs < 2000  // 猜测阈值

    repository.updateProgress(userId, wordId, isCorrect, isGuessing)

    return Result.Success(SubmitAnswerResult(isCorrect, isGuessing))
}
```

**运行测试**: ✅ **所有测试通过**！

---

### 示例3: ViewModel - 状态管理

#### Step 1: RED - 编写测试

```kotlin
// LearningViewModelTest.kt
class LearningViewModelTest {

    private lateinit var loadLevelWords: LoadLevelWordsUseCase
    private lateinit var viewModel: LearningViewModel

    @Before
    fun setup() {
        loadLevelWords = mockk()
        viewModel = LearningViewModel(loadLevelWords)
    }

    @Test
    fun `loadLevel updates uiState to Ready when words loaded`() = runTest {
        // Given
        val words = listOf(
            Word(id = "1", english = "look", chinese = "观看")
        )
        coEvery { loadLevelWords("level_1") } returns Result.Success(words)

        // When
        viewModel.loadLevel("level_1", "look_island")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is LearningUiState.Ready)
        assertEquals(1, state.words.size)
    }
}
```

**运行测试**: ❌ **失败** - `LearningViewModel` 不存在

---

#### Step 2: GREEN - 最简单实现

```kotlin
// LearningViewModel.kt
class LearningViewModel(
    private val loadLevelWords: LoadLevelWordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Loading)
    val uiState: StateFlow<LearningUiState> = _uiState

    fun loadLevel(levelId: String, islandId: String) {
        viewModelScope.launch {
            _uiState.value = LearningUiState.Ready(emptyList())  // 最简单实现
        }
    }
}

sealed class LearningUiState {
    object Loading : LearningUiState()
    data class Ready(val words: List<Word>) : LearningUiState()
    data class Error(val message: String) : LearningUiState()
}
```

**运行测试**: ❌ **失败** - 状态中有单词，不是空列表

**修正** (GREEN):
```kotlin
fun loadLevel(levelId: String, islandId: String) {
    viewModelScope.launch {
        val result = loadLevelWords(levelId)
        if (result is Result.Success) {
            _uiState.value = LearningUiState.Ready(result.data)
        }
    }
}
```

**运行测试**: ✅ **通过**！

---

## 📋 TDD工作流程

### 日常开发流程

#### 1. 开始新功能

**步骤**:
1. ✅ 编写测试用例清单
2. ✅ 从最简单的场景开始
3. ✅ RED: 编写测试（失败）
4. ✅ GREEN: 编写实现（通过）
5. ✅ REFACTOR: 重构代码
6. ✅ 重复3-5，直到所有场景完成

**示例清单** - 记忆强度算法:
- [ ] 正确答案增加强度
- [ ] 错误答案减少强度
- [ ] 猜测减少增长幅度
- [ ] 难度影响变化量
- [ ] 强度上限为100
- [ ] 强度下限为0
- [ ] 连续正确加速增长
- [ ] 连续错误加速衰减

---

#### 2. 编写测试时的思考

**问题1: 测试什么？**

从用户角度思考：
- "如果我答对了，应该发生什么？" → 强度增加
- "如果我答错了，应该发生什么？" → 强度减少
- "如果我猜得很快，应该被检测到吗？" → 标记为猜测

**问题2: 如何验证？**

- 返回值是否符合预期？
- 是否调用了依赖方法？
- 状态是否正确更新？

**问题3: 边界条件？**

- 最大值/最小值
- 空列表/null
- 极端输入（如响应时间为0）

---

#### 3. 编写实现时的原则

**GREEN阶段目标**: **最少代码使测试通过**

❌ **不要过度设计**:
```kotlin
// 还不需要，因为测试还没要求
fun calculateNewStrength(/* ... */): Int {
    val change = if (isCorrect) 10 else -15
    var newStrength = currentStrength + change
    if (isGuessing) newStrength -= 5
    if (consecutiveCorrect > 2) newStrength += bonus
    if (difficulty > 2) newStrength += difficultyBonus
    newStrength = newStrength.coerceIn(0, 100)
    logUpdate(newStrength)
    notifyObservers(newStrength)
    return newStrength
}
```

✅ **只写测试要求的**:
```kotlin
// 第一个测试只要求：正确答案增加强度
fun calculateNewStrength(/* ... */): Int {
    return if (isCorrect) currentStrength + 10 else currentStrength
}
```

---

#### 4. 重构时的勇气

**有了测试，重构不可怕**

```kotlin
// 重构前
fun calculateNewStrength(/* ... */): Int {
    val change = if (isCorrect) CORRECT_BONUS else -INCORRECT_PENALTY
    var newStrength = currentStrength + change
    if (isGuessing) newStrength -= 5
    return newStrength.coerceIn(0, 100)
}

// 重构后 - 提取函数
fun calculateNewStrength(/* ... */): Int {
    val baseChange = calculateBaseChange(isCorrect)
    val guessingAdjustment = calculateGuessingAdjustment(isGuessing)
    return applyBounds(currentStrength + baseChange + guessingAdjustment)
}

private fun calculateBaseChange(isCorrect: Boolean) =
    if (isCorrect) CORRECT_BONUS else -INCORRECT_PENALTY

private fun calculateGuessingAdjustment(isGuessing: Boolean) =
    if (isGuessing) -GUESSING_PENALTY else 0

private fun applyBounds(strength: Int) = strength.coerceIn(0, 100)
```

**运行测试**: ✅ **仍然通过**！

---

## 🎯 TDD最佳实践

### 1. 小步快跑

**✅ 推荐**:
- 每个测试只测试一个场景
- 每次只写最少代码
- 频繁运行测试（每个循环30秒-2分钟）

**❌ 避免**:
- 一次写10个测试
- 一次性实现所有功能
- 长时间不运行测试

---

### 2. 测试名称即文档

**✅ 好的测试名称**:
```kotlin
@Test
fun `correct answer should increase memory strength`() { }

@Test
fun `guessing reduces memory strength increase by 5 points`() { }

@Test
fun `memory strength should clamp to maximum value of 100`() { }
```

**❌ 差的测试名称**:
```kotlin
@Test
fun test1() { }  // 测试什么？

@Test
fun strengthTest() { }  // 太模糊

@Test
fun `correct answer`() { }  // 然后呢？
```

---

### 3. 只测试行为，不测试实现

**❌ 错误：测试实现细节**:
```kotlin
@Test
fun `uses bubble sort algorithm`() {
    // 验证使用了冒泡排序
    // 但如果改用快速排序，测试会失败
}
```

**✅ 正确：测试行为**:
```kotlin
@Test
fun `sorts words in alphabetical order`() {
    val unsorted = listOf(wordC, wordA, wordB)
    val sorted = sortWords(unsorted)
    assertEquals(listOf(wordA, wordB, wordC), sorted)
}
```

---

### 4. 使用Mock隔离依赖

**测试UseCase时**:
```kotlin
@Test
fun `submit answer updates repository`() = runTest {
    // Given
    coEvery { repository.getWordById("1") } returns word

    // When
    useCase("user_001", "1", "look")

    // Then - 验证依赖被正确调用
    coVerify { repository.updateProgress(any(), any(), any(), any()) }
}
```

**不要测试被Mock的对象**:
```kotlin
// ❌ 不要测试Repository的实现细节
// 那是RepositoryTest的职责

// ✅ UseCase只测试业务逻辑和依赖调用
```

---

### 5. 保持测试简单

**✅ 推荐**:
```kotlin
@Test
fun `correct answer increases strength`() {
    val result = calculateStrength(50, isCorrect = true)
    assertEquals(60, result)
}
```

**❌ 避免**:
```kotlin
@Test
fun `correct answer increases strength`() {
    // Given
    val currentStrength = 50
    val expected = 60
    val isCorrect = true
    val isGuessing = false
    val difficulty = 2
    val userId = "user_001"
    val wordId = "word_001"
    val timestamp = System.currentTimeMillis()

    // When
    val result = calculateStrength(currentStrength, isCorrect, isGuessing, difficulty)

    // Then
    assertEquals(expected, result)
    assertTrue(result > currentStrength)
    assertTrue(result <= 100)
    assertTrue(result >= 0)
    // ... 过多验证
}
```

---

## 🚀 TDD实战技巧

### 技巧1: 从开始到结束，再从结束到开始

**正向测试** (Happy Path):
```kotlin
@Test
fun `correct answer completes successfully`() { }
```

**反向测试** (Error Path):
```kotlin
@Test
fun `empty answer returns error`() { }

@Test
fun `word not found returns error`() { }
```

---

### 技巧2: 测试数据构建器

**避免重复代码**:
```kotlin
// Before: 每个测试重复创建
@Test
fun test1() {
    val word = Word("1", "look", "观看", 1, "level_1", "look_island", emptyMap())
    // ...
}

@Test
fun test2() {
    val word = Word("1", "look", "观看", 1, "level_1", "look_island", emptyMap())
    // ...
}

// After: 使用构建函数
private fun createTestWord(
    id: String = "1",
    english: String = "look",
    chinese: String = "观看"
) = Word(id, english, chinese, 1, "level_1", "look_island", emptyMap())

@Test
fun test1() {
    val word = createTestWord()
    // ...
}

@Test
fun test2() {
    val word = createTestWord(english = "watch")
    // ...
}
```

---

### 技巧3: 参数化测试

**测试多个输入**:
```kotlin
// ❌ 重复测试
@Test
fun `difficulty 1 increases strength by 10`() {
    assertEquals(10, calculateBonus(1))
}

@Test
fun `difficulty 2 increases strength by 12`() {
    assertEquals(12, calculateBonus(2))
}

@Test
fun `difficulty 3 increases strength by 15`() {
    assertEquals(15, calculateBonus(3))
}

// ✅ 参数化测试
@Test
fun `difficulty bonus is calculated correctly`() {
    mapOf(
        1 to 10,
        2 to 12,
        3 to 15
    ).forEach { (difficulty, expectedBonus) ->
        assertEquals(expectedBonus, calculateBonus(difficulty))
    }
}
```

---

### 技巧4: 测试快速失败

**验证先于执行**:
```kotlin
@Test
fun `submit answer with empty string returns error`() = runTest {
    // 先验证边界条件
    val result = useCase("user_001", "word_001", "")

    assertTrue(result is Result.Error)
    assertTrue(result.exception is IllegalArgumentException)

    // 如果测试通过，后面不会执行
    coVerify(exactly = 0) { repository.updateProgress(any(), any(), any(), any()) }
}
```

---

## 📊 TDD效果评估

### 定量指标

| 指标 | 传统开发 | TDD | 改善 |
|------|---------|-----|------|
| 代码覆盖率 | 40-60% | 80-95% | +40% |
| Bug密度 | 5-10个/KLOC | 1-2个/KLOC | -80% |
| 重构信心 | 低 | 高 | ✅ |
| 开发速度（初期） | 快 | 慢 | -30% |
| 开发速度（长期） | 中等 | 快 | +20% |

### 定性收益

✅ **更清晰的API设计** - 先从使用角度思考
✅ **活文档** - 测试即文档，总是最新
✅ **重构勇气** - 有测试保护，敢于重构
✅ **调试效率** - 问题快速定位（最后一个失败的测试）
✅ **代码审查** - 测试展示代码如何使用

---

## 🐛 TDD常见陷阱

### 陷阱1: 写太多测试后再实现

**❌ 错误**:
```kotlin
// 一次性写10个测试
@Test fun test1() { }
@Test fun test2() { }
// ...
@Test fun test10() { }

// 然后花2小时实现所有代码
```

**✅ 正确**:
- 写1个测试
- 实现它
- 重构
- 写下一个测试

---

### 陷阱2: 跳过重构阶段

**❌ 错误**:
```
RED → GREEN → RED → GREEN → RED → GREEN
```

**✅ 正确**:
```
RED → GREEN → REFACTOR → RED → GREEN → REFACTOR
```

重构是TDD的重要部分，不要跳过！

---

### 陷阱3: 测试依赖实现细节

**❌ 错误**:
```kotlin
@Test
fun `uses ArrayList internally`() {
    // 如果改用LinkedList，测试失败
    assertTrue(obj.internalList is ArrayList)
}
```

**✅ 正确**:
```kotlin
@Test
fun `returns sorted words`() {
    // 无论内部如何实现
    assertEquals(sorted, obj.getWords())
}
```

---

### 陷阱4: 不运行测试

**❌ 错误**:
- 写了5个测试
- 写了实现
- 最后才运行测试
- 一次性修复10个错误

**✅ 正确**:
- 每个循环都运行测试
- 快速反馈
- 问题立即发现

---

## 🎓 TDD学习路径

### Level 1: 新手（1-2周）

**目标**: 掌握基本流程

**练习**:
1. 为简单函数编写测试（纯函数）
2. 练习Red-Green-Refactor循环
3. 学习Mock基本用法

**推荐练习**:
- `MemoryStrengthAlgorithm` - 纯函数
- `GuessingDetector` - 简单逻辑

---

### Level 2: 进阶（2-4周）

**目标**: 掌握依赖注入和Mock

**练习**:
1. 测试UseCase（依赖Repository）
2. 测试ViewModel（依赖UseCase）
3. 处理异步代码（协程）

**推荐练习**:
- `SubmitAnswerUseCase` - Mock Repository
- `LearningViewModel` - Mock UseCase + Flow

---

### Level 3: 高级（1-3个月）

**目标**: 掌握集成测试和E2E

**练习**:
1. 测试Repository（Room数据库）
2. 测试UI组件（Compose）
3. 端到端测试

**推荐练习**:
- `WordRepositoryTest` - Room In-Memory
- `LearningScreenTest` - Compose Testing

---

## 📚 TDD资源

### 内部文档
- [测试策略](./TEST_STRATEGY.md) - 了解测试分层
- [测试用例清单](./TEST_CASES.md) - 查看所有测试场景
- [测试指南](./TESTING_GUIDE.md) - 测试工具使用

### 推荐书籍
- 《Test-Driven Development: By Example》- Kent Beck
- 《Growing Object-Oriented Software, Guided by Tests》- Steve Freeman
- 《Working Effectively with Legacy Code》- Michael Feathers

### 在线资源
- [Android Testing Guide](https://developer.android.com/training/testing)
- [MockK Documentation](https://mockk.io/)
- [TDD kata exercises](https://github.com/gabrielelana/awesome-tdd-katas)

---

## ✅ TDD检查清单

### 开始编码前
- [ ] 是否有明确的需求？
- [ ] 能否从用户角度写测试？

### RED阶段
- [ ] 测试能运行吗？
- [ ] 测试失败了吗？（应该失败！）
- [ ] 失败原因清晰吗？

### GREEN阶段
- [ ] 写了最少代码吗？
- [ ] 测试通过了吗？
- [ ] 没有引入不必要的复杂性？

### REFACTOR阶段
- [ ] 代码清晰吗？
- [ ] 有重复代码吗？
- [ ] 测试仍然通过吗？

### 完成后
- [ ] 所有测试通过？
- [ ] 测试覆盖率良好？
- [ ] 代码已提交？

---

**开始TDD之旅吧！** 🚀

记住：TDD是技能，需要练习。从小处着手，持续改进。

---

**最后更新**: 2026-02-16
**维护者**: Android Team
