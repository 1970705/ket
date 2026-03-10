# Wordland 测试文档

## 测试策略

### 测试金字塔

```
        /\
       /  \        E2E Tests (5%)
      /____\       UI Tests (15%)
     /      \      Integration Tests (20%)
    /________\     Unit Tests (60%)
```

### 测试分层

1. **单元测试 (Unit Tests)** - 60%
   - 测试独立的类和方法
   - 不依赖外部系统
   - 快速执行
   - Mock 所有依赖

2. **集成测试 (Integration Tests)** - 20%
   - 测试多个组件的交互
   - 可能使用真实的数据源（测试数据库）
   - 验证接口契约

3. **UI 测试 (UI Tests)** - 15%
   - 测试 Compose UI 组件
   - 验证用户交互
   - 使用 Compose Testing

4. **端到端测试 (E2E Tests)** - 5%
   - 完整的用户流程
   - 真实的 Android 环境
   - 使用 Espresso/UI Automator

---

## 测试覆盖率目标

| 层级 | 目标覆盖率 | 当前状态 |
|------|-----------|----------|
| Domain Layer (UseCases) | 90%+ | 🟡 待测试 |
| Domain Layer (Algorithms) | 95%+ | 🟢 已实现 |
| Data Layer (Repositories) | 80%+ | 🟡 待测试 |
| UI Layer (ViewModels) | 75%+ | 🟡 待测试 |
| UI Layer (Components) | 60%+ | 🔴 未开始 |

---

## 单元测试 (Unit Tests)

### 1. Domain Layer - UseCases

#### LoadLevelWordsUseCaseTest
**文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/LoadLevelWordsUseCaseTest.kt`

**测试场景**:
- ✅ 返回排序后的单词列表
- ✅ 空单词列表时返回错误
- ✅ Repository 抛出异常时处理错误
- ✅ 单个单词的处理

**覆盖率**: 4 个测试用例

---

#### SubmitAnswerUseCaseTest
**文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCaseTest.kt`

**测试场景**:
- ✅ 正确答案增加记忆强度并奖励星星
- ✅ 错误答案降低记忆强度
- ✅ 使用提示减少获得的星星
- ✅ 快速回答被检测为猜测
- ✅ 单词不存在时返回错误
- ✅ 大小写不敏感的答案匹配

**覆盖率**: 6 个测试用例

---

#### GetNextWordUseCaseTest
**文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/GetNextWordUseCaseTest.kt`

**测试场景**:
- ✅ 优先选择新单词
- ✅ 优先选择到期单词
- ✅ 跳过当前单词
- ✅ 没有可用单词时返回 null
- ✅ 空级别时返回错误
- ✅ 优先选择低记忆强度单词

**覆盖率**: 6 个测试用例

---

### 2. Domain Layer - Algorithms

#### MemoryStrengthAlgorithmTest
**文件**: `app/src/test/java/com/wordland/domain/algorithm/MemoryStrengthAlgorithmTest.kt`

**测试场景**:
- ✅ 正确答案增加记忆强度
- ✅ 错误答案降低记忆强度
- ✅ 猜测减少强度增长
- ✅ 困难单词增加较小
- ✅ 强度不超过最大值
- ✅ 强度不低于最小值
- ✅ 计算下次复习时间（高强度=更长时间）
- ✅ 错误答案后更短复习间隔
- ✅ 检测快速正确答案为猜测
- ✅ 慢速答案不被检测为猜测
- ✅ 多个连续快速答案被检测为猜测
- ✅ 猜测置信度随快速答案增加

**覆盖率**: 12 个测试用例

---

### 3. UI Layer - ViewModels

#### LearningViewModelTest
**文件**: `app/src/test/java/com/wordland/ui/viewmodel/LearningViewModelTest.kt`

**测试场景**:
- ✅ 加载级别成功时更新 uiState 为 Ready
- ✅ 加载失败时更新 uiState 为 Error
- ✅ 提交答案后更新 uiState 为 Feedback
- ✅ 使用提示后标记 hintShown
- ✅ onNextWord 移动到下一个单词或完成级别
- ✅ 进度计算正确
- ✅ currentWord 发出正确的单词

**覆盖率**: 7 个测试用例

---

## 集成测试 (Integration Tests)

### Repository 集成测试

#### WordRepositoryIntegrationTest
**文件**: `app/src/test/java/com/wordland/data/repository/WordRepositoryIntegrationTest.kt`

**测试场景**:
- 使用内存数据库测试 Room DAO
- 验证数据库操作的正确性
- 测试事务处理
- 测试 Flow 数据流

**需要创建**: 🔴

---

#### ProgressRepositoryIntegrationTest
**文件**: `app/src/test/java/com/wordland/data/repository/ProgressRepositoryIntegrationTest.kt`

**测试场景**:
- 测试学习进度保存和读取
- 测试记忆强度更新
- 测试复习时间计算
- 测试级别进度跟踪

**需要创建**: 🔴

---

## UI 测试 (UI Tests)

### Compose 组件测试

#### WordlandCardTest
**文件**: `app/src/test/java/com/wordland/ui/components/WordlandCardTest.kt`

**测试场景**:
- 验证卡片显示正确
- 测试点击事件
- 测试自定义样式应用

**需要创建**: 🔴

---

#### AnswerAnimationsTest
**文件**: `app/src/test/java/com/wordland/ui/components/AnswerAnimationsTest.kt`

**测试场景**:
- 验证正确答案动画
- 验证错误答案动画
- 测试动画完成回调

**需要创建**: 🔴

---

### Screen 测试

#### LearningScreenUI
**文件**: `app/src/androidTest/java/com/wordland/ui/screens/LearningScreenTest.kt`

**测试场景**:
- 完整的学习流程测试
- 答案提交交互
- 提示使用交互
- 导航交互

**需要创建**: 🔴

---

## 端到端测试 (E2E Tests)

### 用户流程测试

#### CompleteLearningFlowTest
**文件**: `app/src/androidTest/java/com/wordland/e2e/CompleteLearningFlowTest.kt`

**测试场景**:
1. 启动应用
2. 选择岛屿
3. 选择级别
4. 完成学习（回答多个单词）
5. 查看进度
6. 返回主页

**需要创建**: 🔴

---

## 测试工具和库

### 已配置的依赖

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.5")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("app.cash.turbine:turbine:1.0.0")
testImplementation("com.google.truth:truth:1.1.5")

// Android Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

### 工具说明

1. **MockK** - Kotlin mocking framework
2. **Turbine** - Flow testing utilities
3. **Truth** - Fluent assertions
4. **Compose Testing** - UI component testing
5. **Espresso** - Android UI testing

---

## 运行测试

### 运行所有测试

```bash
# Unit tests
./gradlew test

# Android tests (requires emulator or device)
./gradlew connectedAndroidTest

# Specific test class
./gradlew test --tests "com.wordland.domain.usecase.usecases.SubmitAnswerUseCaseTest"

# With coverage report
./gradlew testDebugUnitTest coverage
```

### 运行特定测试

```bash
# UseCase tests
./gradlew test --tests "*.usecase.*"

# Algorithm tests
./gradlew test --tests "*.algorithm.*"

# ViewModel tests
./gradlew test --tests "*.viewmodel.*"
```

---

## 测试最佳实践

### 1. Arrange-Act-Assert (AAA) 模式

```kotlin
@Test
fun `correct answer increases memory strength`() = runTest {
    // Arrange (准备)
    val currentStrength = 50
    val isCorrect = true

    // Act (执行)
    val newStrength = algorithm.calculateNewStrength(currentStrength, isCorrect, ...)

    // Assert (断言)
    assertTrue(newStrength > currentStrength)
}
```

### 2. 使用描述性测试名称

```kotlin
// ❌ 不好
@Test
fun test1() { }

// ✅ 好
@Test
fun `correct answer increases memory strength`() { }
```

### 3. Mock 外部依赖

```kotlin
@Before
fun setup() {
    repository = mockk()
    useCase = MyUseCase(repository)
}
```

### 4. 使用协程测试

```kotlin
@Test
fun `async operation works correctly`() = runTest {
    // Test suspend functions
    val result = useCase.loadData()
    assertEquals(expected, result)
}
```

### 5. 测试边界条件

```kotlin
@Test
fun `handles empty list correctly`() { }
@Test
fun `handles null values correctly`) { }
@Test
fun `handles maximum values correctly`) { }
```

---

## CI/CD 集成

### GitHub Actions 配置

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run unit tests
        run: ./gradlew test
      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedAndroidTest
```

---

## 下一步计划

### 短期 (1-2 周)
- [ ] 完成 Repository 集成测试
- [ ] 添加 ViewModel 测试覆盖
- [ ] 实现 Compose 组件测试

### 中期 (1 个月)
- [ ] 添加 UI 自动化测试
- [ ] 实现性能测试
- [ ] 添加压力测试

### 长期 (持续)
- [ ] 维护 80%+ 测试覆盖率
- [ ] 定期更新测试用例
- [ ] 监控测试执行时间

---

## 测试统计

### 当前状态

- **总测试数**: 29 个
- **单元测试**: 29 个
- **集成测试**: 0 个
- **UI 测试**: 0 个
- **E2E 测试**: 0 个

### 按层级分布

| 层级 | 测试数 | 状态 |
|------|--------|------|
| UseCases | 16 | 🟡 进行中 |
| Algorithms | 12 | 🟢 完成 |
| ViewModels | 7 | 🟡 进行中 |
| Repositories | 0 | 🔴 未开始 |
| UI Components | 0 | 🔴 未开始 |
| E2E | 0 | 🔴 未开始 |

---

**文档版本**: 1.0
**最后更新**: 2026-02-15
**维护者**: Claude Code
