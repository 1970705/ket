# 测试代码质量规范 (TEST_CODE_STANDARDS)

**版本**: 1.0
**日期**: 2026-03-02
**状态**: 已发布
**关联 Epic**: Epic #11 - 测试代码质量重构

---

## 📋 概述

本文档定义 Wordland 项目测试代码的质量标准和规范，确保测试代码的可维护性、可读性和可执行性。

### 目标

- 确保所有测试文件大小在合理范围内
- 提高测试代码的可维护性
- 降低测试执行时间
- 统一测试代码风格和结构

---

## 🎯 文件大小标准

### 测试文件行数限制

| 评级 | 行数范围 | 状态 | 说明 |
|------|---------|------|------|
| ✅ **优秀** | 200-300 行 | 推荐目标 | 最佳实践，易于维护 |
| ✅ **良好** | 301-400 行 | 可接受 | 符合标准 |
| ⚠️ **警告** | 401-500 行 | 需关注 | 接近上限，考虑拆分 |
| 🚨 **超标** | 501-600 行 | 需优化 | 强烈建议拆分 |
| 🔴 **严重** | >600 行 | 必须重构 | 阻止合并 |

### 不同测试类型的建议大小

| 测试类型 | 推荐行数 | 最大行数 | 说明 |
|---------|---------|---------|------|
| **Model 测试** | 150-250 | 400 | 纯数据类，测试相对简单 |
| **UseCase 测试** | 200-300 | 450 | 单一职责，场景有限 |
| **ViewModel 测试** | 250-350 | 500 | 状态管理复杂，需拆分 |
| **Repository 测试** | 200-300 | 450 | 数据操作测试 |
| **UI Component 测试** | 200-350 | 500 | Compose 组件测试 |
| **Integration 测试** | 300-400 | 550 | 跨层测试，可适度放宽 |

---

## 📁 文件组织结构

### 单一职责原则

每个测试类应只测试一个主要功能或场景。

#### 命名规范

```
原始类: LearningViewModel

测试文件拆分示例:
├── LearningViewModelLoadLevelTest.kt          # 加载关卡相关测试
├── LearningViewModelSubmitAnswerTest.kt       # 提交答案相关测试
├── LearningViewModelHintTest.kt               # 提示系统相关测试
├── LearningViewModelComboTest.kt              # 连击系统相关测试
├── LearningViewModelLevelCompletionTest.kt    # 关卡完成相关测试
└── LearningViewModelEdgeCasesTest.kt          # 边界情况和异常测试
```

#### 拆分维度

1. **按功能模块拆分**（推荐）
   - ViewModel: 按功能（加载、提交、完成、导航）
   - UseCase: 按业务场景（正常、异常、边界）
   - UI: 按组件或交互流程

2. **按测试类型拆分**
   - 单元测试 vs 集成测试
   - 正常路径 vs 异常路径
   - 快速测试 vs 慢速测试

3. **按测试数据拆分**
   - 不同输入规模的测试
   - 不同配置场景的测试

---

## 🏗️ 测试代码结构规范

### 标准测试类结构

```kotlin
/**
 * ClassName 测试
 *
 * 测试范围:
 * - 功能 A: 测试点描述
 * - 功能 B: 测试点描述
 *
 * 不包含:
 * - 功能 C 的测试 (见 OtherClassTest)
 */
class MyFeatureTest {

    // ========== 测试固件 ==========

    private lateinit var subject: MyFeature
    private val mockRepository = mockk<MyRepository>()

    @Before
    fun setup() {
        subject = MyFeature(mockRepository)
    }

    // ========== 正常路径测试 ==========

    @Test
    fun `should do something when condition is met`() {
        // Given
        val input = "test"
        every { mockRepository.getData() } returns input

        // When
        val result = subject.doSomething()

        // Then
        assertThat(result).isTrue()
    }

    // ========== 异常路径测试 ==========

    @Test
    fun `should throw exception when input is invalid`() {
        // Given
        val invalidInput = ""

        // When & Then
        assertThrows<IllegalArgumentException> {
            subject.validate(invalidInput)
        }
    }

    // ========== 边界条件测试 ==========

    @Test
    fun `should handle edge case correctly`() {
        // ...
    }
}
```

### 测试方法命名规范

使用 **Given-When-Then** 风格的反引号命名：

```kotlin
// ✅ 良好的命名
@Test
fun `should return true when valid answer is submitted`() { }

@Test
fun `should increment combo when consecutive correct answers given`() { }

@Test
fun `should apply hint penalty when hint is used`() { }

// ❌ 避免的命名
@Test
fun testSubmitAnswer() { }

@Test
fun testCombo() { }
```

---

## 🧪 测试编写最佳实践

### 1. AAA 模式 (Arrange-Act-Assert)

```kotlin
@Test
fun `should calculate stars based on accuracy`() {
    // Arrange (Given)
    val correctAnswers = 5
    val totalQuestions = 6
    val calculator = StarRatingCalculator()

    // Act (When)
    val stars = calculator.calculateStars(correctAnswers, totalQuestions)

    // Assert (Then)
    assertThat(stars).isEqualTo(3)
}
```

### 2. 测试隔离

```kotlin
// ✅ 每个测试独立设置状态
@Before
fun setup() {
    // 初始化干净的测试环境
}

@After
fun tearDown() {
    // 清理资源
    clearAllMocks()
}
```

### 3. 使用测试数据构建器

```kotlin
// 测试数据构建器
class WordProgressBuilder {
    private var wordId = "test_word_001"
    private var correctCount = 0
    private var incorrectCount = 0
    private var masteryLevel = 0

    fun withWordId(id: String) = apply { wordId = id }
    fun withCorrectCount(count: Int) = apply { correctCount = count }
    fun withIncorrectCount(count: Int) = apply { incorrectCount = count }
    fun withMasteryLevel(level: Int) = apply { masteryLevel = level }

    fun build() = UserWordProgress(
        wordId = wordId,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        masteryLevel = masteryLevel
    )
}

// 使用
val progress = WordProgressBuilder()
    .withWordId("look_001")
    .withCorrectCount(5)
    .withMasteryLevel(3)
    .build()
```

### 4. 避免测试重复

```kotlin
// ❌ 重复的设置代码
@Test
fun `test A`() {
    val mock1 = mockk<Repo1>()
    val mock2 = mockk<Repo2>()
    val mock3 = mockk<Repo3>()
    every { mock1.getData() } returns "data"
    every { mock2.getData() } returns "data"
    every { mock3.getData() } returns "data"
    // ...
}

@Test
fun `test B`() {
    val mock1 = mockk<Repo1>()
    val mock2 = mockk<Repo2>()
    val mock3 = mockk<Repo3>()
    every { mock1.getData() } returns "data"
    every { mock2.getData() } returns "data"
    every { mock3.getData() } returns "data"
    // ...
}

// ✅ 提取到公共方法
private fun createMocks(): TestMocks {
    val mock1 = mockk<Repo1>()
    val mock2 = mockk<Repo2>()
    val mock3 = mockk<Repo3>()
    every { mock1.getData() } returns "data"
    every { mock2.getData() } returns "data"
    every { mock3.getData() } returns "data"
    return TestMocks(mock1, mock2, mock3)
}
```

---

## 🚫 反模式与禁忌

### 禁止事项

| 模式 | 问题 | 替代方案 |
|------|------|---------|
| 测试文件 > 600 行 | 难以维护，理解困难 | 按功能拆分文件 |
| 一个测试方法 > 50 行 | 难以理解测试意图 | 提取辅助方法 |
| 硬编码测试数据 | 测试脆弱，难以扩展 | 使用测试数据构建器 |
| 测试间相互依赖 | 执行顺序影响结果 | 每个测试独立 |
| 忽略测试而不修复 | 技术债务累积 | 修复或删除 |
| 在测试中写业务逻辑 | 测试本身可能含 bug | 只做验证，不写逻辑 |

### 代码示例

```kotlin
// ❌ 反模式: 测试方法过长
@Test
fun `should handle all scenarios`() {
    // 100+ 行代码...
}

// ✅ 拆分为多个测试
@Test
fun `should handle normal scenario`() { }
@Test
fun `should handle empty input`() { }
@Test
fun `should handle invalid input`() { }

// ❌ 反模式: 测试间依赖
private var sharedState = 0

@Test
fun `test A`() {
    sharedState = 1
}

@Test
fun `test B`() {
    // 依赖 test A 先执行
    assertThat(sharedState).isEqualTo(1)
}

// ✅ 每个测试独立
@Test
fun `test B`() {
    val state = setUpInitialState()
    assertThat(state.value).isEqualTo(1)
}
```

---

## 📊 测试覆盖率要求

### 覆盖率目标

| 层次 | 目标覆盖率 | 当前状态 |
|------|-----------|---------|
| **Domain 层** | 60% | ~45% |
| **Data 层** | 50% | ~15% |
| **UI 层 (ViewModel)** | 70% | ~40% |
| **UI 层 (Component)** | 40% | ~40% |
| **整体** | 50% | ~25-30% |

### 覆盖率质量

不仅关注覆盖率数字，还需关注：

1. **关键路径覆盖**: 核心业务逻辑必须有测试
2. **边界条件**: 极值、空值、异常情况
3. **错误处理**: 异常路径的测试

---

## 🔍 Code Review 检查清单

### 提交测试代码前检查

- [ ] 测试文件行数不超过 500 行
- [ ] 每个测试方法遵循 AAA 模式
- [ ] 测试命名清晰描述测试意图
- [ ] 没有硬编码的重复测试数据
- [ ] Mock 对象正确设置和验证
- [ ] 测试之间完全独立
- [ ] 边界条件和异常情况有测试覆盖
- [ ] 测试执行时间合理（单个测试 < 1 秒）

### 合并前检查

- [ ] 所有新测试通过
- [ ] 测试覆盖率未下降
- [ ] 没有忽略的测试
- [ ] 代码审查通过

---

## 🛠️ 工具支持

### 自动检查脚本

项目提供了自动检查脚本：

```bash
./check_test_file_size.sh
```

该脚本会：
1. 扫描所有测试文件
2. 报告超标文件
3. 在 CI 中作为质量门禁

### CI/CD 集成

质量检查已集成到 GitHub Actions：

```yaml
# .github/workflows/ci.yml
- name: Check Test Code Quality
  run: |
    ./check_test_file_size.sh
```

---

## 📚 参考资源

### 内部文档

- [UI 测试指南](../guides/testing/UI_TESTING_GUIDE.md)
- [测试代码质量分析](../../reports/testing/TEST_CODE_QUALITY_ANALYSIS_2026-03-02.md)
- [Epic #11 质量门禁报告](../../reports/testing/EPIC11_QUALITY_GATE_REPORT.md)

### 外部资源

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [MockK 文档](https://mockk.io/)
- [Kotlin 测试最佳实践](https://kotlinlang.org/docs/testing.html)

---

## 📝 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| 1.0 | 2026-03-02 | 初始版本，建立测试代码质量规范 |

---

**维护者**: android-architect + android-test-engineer
**审核周期**: 每季度
**反馈**: 请通过团队会议提出改进建议
