# Wordland测试用例清单

**版本**: 1.0
**日期**: 2026-02-16
**范围**: Wordland KET App

---

## 📊 测试用例统计

| 模块 | 总用例数 | 已实现 | 通过 | 失败 | 跳过 | 覆盖率 |
|------|---------|--------|------|------|------|--------|
| Domain.Algorithm | 12 | 10 | 10 | 0 | 0 | 83% |
| Domain.UseCase | 5 | 3 | 3 | 0 | 0 | 60% |
| UI.ViewModel | 6 | 0 | 0 | 0 | 0 | 0% |
| Data.Repository | 3 | 0 | 0 | 0 | 0 | 0% |
| **总计** | **26** | **13** | **13** | **0** | **0** | **50%** |

---

## 🎯 Domain层测试用例

### 1. Algorithm模块

#### 1.1 MemoryStrengthAlgorithm（记忆强度算法）

**文件**: `app/src/test/java/com/wordland/domain/algorithm/MemoryStrengthAlgorithmTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DA-001 | `correct answer increases memory strength` | 正确答案应该增加记忆强度 | ✅ 通过 | P0 |
| DA-002 | `incorrect answer decreases memory strength` | 错误答案应该减少记忆强度 | ✅ 通过 | P0 |
| DA-003 | `guessing reduces memory strength increase` | 猜测行为减少记忆强度增长 | ✅ 通过 | P1 |
| DA-004 | `difficulty affects strength change` | 难度影响记忆强度变化 | ✅ 通过 | P1 |
| DA-005 | `memory strength clamps to max value` | 记忆强度上限为100 | ✅ 通过 | P0 |
| DA-006 | `memory strength clamps to min value` | 记忆强度下限为0 | ✅ 通过 | P0 |
| DA-007 | `initial strength is set correctly` | 初始记忆强度设置为10 | ✅ 通过 | P0 |
| DA-008 | `consecutive correct answers increase strength faster` | 连续正确答案加速增长 | ✅ 通过 | P1 |
| DA-009 | `consecutive incorrect answers decrease strength faster` | 连续错误答案加速衰减 | ⚠️ 未实现 | P1 |
| DA-010 | `strength change respects difficulty modifiers` | 难度修正系数生效 | ⚠️ 未实现 | P2 |

**测试数据示例**:
```kotlin
// DA-001: 正确答案增长测试
输入: currentStrength = 50, isCorrect = true, isGuessing = false, difficulty = 2
预期: newStrength > 50 && newStrength ≤ 100

// DA-003: 猜测惩罚测试
输入: currentStrength = 50, isCorrect = true, isGuessing = true, difficulty = 2
预期: newStrength < 正常增长值
```

---

#### 1.2 GuessingDetector（猜测检测算法）

**文件**: `app/src/test/java/com/wordland/domain/algorithm/GuessingDetectorTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DG-001 | `fast response indicates guessing` | 快速响应（<2秒）标记为猜测 | ✅ 通过 | P0 |
| DG-002 | `very fast response indicates high confidence guessing` | 极快响应（<1.5秒）标记为高置信度猜测 | ✅ 通过 | P0 |
| DG-003 | `normal response not guessing` | 正常响应时间不标记为猜测 | ✅ 通过 | P0 |
| DG-004 | `repeated fast responses increase guessing probability` | 连续快速响应增加猜测概率 | ⚠️ 未实现 | P1 |
| DG-005 | `alternating answers pattern indicates guessing` | 交替答案模式检测 | ⚠️ 未实现 | P1 |
| DG-006 | `all same answers pattern indicates guessing` | 全部相同答案模式检测 | ⚠️ 未实现 | P2 |

**测试数据示例**:
```kotlin
// DG-001: 快速响应检测
输入: responseTimeMs = 1500
预期: isGuessing = true, confidence = HIGH

// DG-003: 正常响应
输入: responseTimeMs = 5000
预期: isGuessing = false, confidence = null
```

---

### 2. UseCase模块

#### 2.1 SubmitAnswerUseCase（提交答案用例）

**文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCaseTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DU-001 | `correct answer returns success with isCorrect=true` | 正确答案返回成功结果 | ✅ 通过 | P0 |
| DU-002 | `incorrect answer returns success with isCorrect=false` | 错误答案返回失败结果 | ✅ 通过 | P0 |
| DU-003 | `empty answer returns error` | 空答案返回错误 | ✅ 通过 | P0 |
| DU-004 | `answer updates user progress correctly` | 答案正确更新用户进度 | ⚠️ 未实现 | P0 |
| DU-005 | `guessing is detected and recorded` | 猜测行为被检测并记录 | ⚠️ 未实现 | P1 |

**测试场景**:
```kotlin
// DU-001: 正确答案场景
Given: 用户选择单词"look"，正确答案为"look"
When: 提交答案
Then:
  - result is Result.Success
  - result.data.isCorrect == true
  - result.data.starsEarned > 0
  - repository.updateProgress() 被调用

// DU-003: 空答案验证
Given: 用户提交空字符串
When: 提交答案
Then:
  - result is Result.Error
  - result.exception is IllegalArgumentException
```

---

#### 2.2 LoadLevelWordsUseCase（加载关卡单词用例）

**文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/LoadLevelWordsUseCaseTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DL-001 | `load words returns success with correct data` | 成功加载关卡单词列表 | ⚠️ 未实现 | P0 |
| DL-002 | `load non-existent level returns error` | 加载不存在的关卡返回错误 | ⚠️ 未实现 | P0 |
| DL-003 | `load empty level returns empty list` | 空关卡返回空列表 | ⚠️ 未实现 | P1 |

---

### 3. Domain Model测试

#### 3.1 Word模型

**文件**: `app/src/test/java/com/wordland/domain/model/WordTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DM-001 | `word validation rejects empty fields` | 单词验证拒绝空字段 | ⚠️ 未实现 | P0 |
| DM-002 | `word validation rejects invalid difficulty` | 单词验证拒绝无效难度 | ⚠️ 未实现 | P1 |

#### 3.2 UserWordProgress模型

**文件**: `app/src/test/java/com/wordland/domain/model/UserWordProgressTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DP-001 | `memory strength update respects bounds` | 记忆强度更新遵守边界 | ⚠️ 未实现 | P0 |
| DP-002 | `mastery calculation uses correct percentage` | 熟练度计算使用正确百分比 | ⚠️ 未实现 | P0 |
| DP-003 | `cross scene validation requires minimum attempts` | 跨场景验证需要最少尝试次数 | ⚠️ 未实现 | P1 |

---

## 🗄️ Data层测试用例

### 1. Repository模块

#### 1.1 WordRepository

**文件**: `app/src/androidTest/java/com/wordland/data/repository/WordRepositoryTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DR-001 | `insert and retrieve word correctly` | 插入并正确检索单词 | ⚠️ 未实现 | P0 |
| DR-002 | `getWordsByLevel returns correct words` | 按关卡获取单词列表 | ⚠️ 未实现 | P0 |
| DR-003 | `update word progress persists changes` | 更新单词进度持久化 | ⚠️ 未实现 | P0 |
| DR-004 | `concurrent updates handle correctly` | 并发更新正确处理 | ⚠️ 未实现 | P1 |

**测试环境**:
- In-Memory Room数据库
- Hilt依赖注入
- TestDispatcher协程测试

---

#### 1.2 TrackingRepository

**文件**: `app/src/androidTest/java/com/wordland/data/repository/TrackingRepositoryTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DT-001 | `record behavior tracking saves correctly` | 行为追踪记录正确保存 | ⚠️ 未实现 | P0 |
| DT-002 | `get behavior by word returns correct data` | 按单词获取行为数据 | ⚠️ 未实现 | P0 |
| DT-003 | `guessing detection uses correct algorithm` | 猜测检测使用正确算法 | ⚠️ 未实现 | P1 |

---

### 2. DAO模块

#### 2.1 WordDao

**文件**: `app/src/androidTest/java/com/wordland/data/dao/WordDaoTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| DD-001 | `insert word adds to database` | 插入单词到数据库 | ⚠️ 未实现 | P0 |
| DD-002 | `getWordById returns correct word` | 按ID检索单词 | ⚠️ 未实现 | P0 |
| DD-003 | `getWordsByLevel filters correctly` | 按关卡过滤单词 | ⚠️ 未实现 | P0 |
| DD-004 | `update word modifies existing record` | 更新已有记录 | ⚠️ 未实现 | P0 |
| DD-005 | `delete word removes from database` | 删除单词记录 | ⚠️ 未实现 | P1 |

---

## 🖥️ UI层测试用例

### 1. ViewModel模块

#### 1.1 LearningViewModel

**文件**: `app/src/test/java/com/wordland/ui/viewmodel/LearningViewModelTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| UV-001 | `initial state is Loading` | 初始状态为Loading | ⚠️ 未实现 | P0 |
| UV-002 | `loadLevel updates uiState to Ready when words loaded` | 加载关卡后状态变为Ready | ⚠️ 未实现 | P0 |
| UV-003 | `loadLevel updates uiState to Error on failure` | 加载失败状态变为Error | ⚠️ 未实现 | P0 |
| UV-004 | `submitAnswer updates uiState with result` | 提交答案更新状态 | ⚠️ 未实现 | P0 |
| UV-005 | `submitAnswer with guess marks result as guessed` | 提交猜测答案标记为猜测 | ⚠️ 未实现 | P1 |
| UV-006 | `completeLevel saves progress correctly` | 完成关卡保存进度 | ⚠️ 未实现 | P0 |

**Mock对象**:
```kotlin
private val mockLoadLevelWordsUseCase = mockk<LoadLevelWordsUseCase>()
private val mockSubmitAnswerUseCase = mockk<SubmitAnswerUseCase>()
private val mockUpdateProgressUseCase = mockk<UpdateProgressUseCase>()
```

---

#### 1.2 LevelSelectViewModel

**文件**: `app/src/test/java/com/wordland/ui/viewmodel/LevelSelectViewModelTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| UL-001 | `loadLevels populates uiState with level data` | 加载关卡数据到状态 | ⚠️ 未实现 | P0 |
| UL-002 | `unlockLevel updates level status to Unlocked` | 解锁关卡更新状态 | ⚠️ 未实现 | P0 |
| UL-003 | `locked level cannot be started` | 锁定关卡无法启动 | ⚠️ 未实现 | P0 |

---

### 2. UI Screen测试

#### 2.1 LearningScreen

**文件**: `app/src/androidTest/java/com/wordland/ui/screens/LearningScreenTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| US-001 | `screen displays word correctly` | 屏幕正确显示单词 | ⚠️ 未实现 | P0 |
| US-002 | `clicking answer option submits answer` | 点击选项提交答案 | ⚠️ 未实现 | P0 |
| US-003 | `correct answer shows success feedback` | 正确答案显示成功反馈 | ⚠️ 未实现 | P0 |
| US-004 | `incorrect answer shows error feedback` | 错误答案显示错误反馈 | ⚠️ 未实现 | P0 |
| US-005 | `progress bar updates correctly` | 进度条正确更新 | ⚠️ 未实现 | P1 |

**测试工具**: Compose Testing

---

#### 2.2 LevelSelectScreen

**文件**: `app/src/androidTest/java/com/wordland/ui/screens/LevelSelectScreenTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| UL-001 | `clicking level card navigates to learning screen` | 点击关卡卡片导航到学习界面 | ⚠️ 未实现 | P0 |
| UL-002 | `locked level shows lock icon` | 锁定关卡显示锁图标 | ⚠️ 未实现 | P1 |
| UL-003 | `unlocked level shows stars count` | 解锁关卡显示星级数量 | ⚠️ 未实现 | P1 |

---

## 🔄 集成测试用例

### 1. 数据库集成

**文件**: `app/src/androidTest/java/com/wordland/integration/DatabaseIntegrationTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| ID-001 | `first launch initializes database correctly` | 首次启动正确初始化数据库 | ⚠️ 未实现 | P0 |
| ID-002 | `database seed data loads correctly` | 种子数据正确加载 | ⚠️ 未实现 | P0 |
| ID-003 | `level 1 is unlocked after first launch` | 首次启动后关卡1解锁 | ⚠️ 未实现 | P0 |

**测试环境**:
- Room In-Memory数据库
- Hilt完整依赖注入
- 真实DAO和Repository实现

---

### 2. UseCase集成

**文件**: `app/src/androidTest/java/com/wordland/integration/UseCaseIntegrationTest.kt`

| 测试ID | 测试名称 | 描述 | 状态 | 优先级 |
|--------|---------|------|------|--------|
| IU-001 | `submit answer workflow end-to-end` | 提交答案端到端流程 | ⚠️ 未实现 | P0 |
| IU-002 | `complete level unlocks next level` | 完成关卡解锁下一关 | ⚠️ 未实现 | P0 |
| IU-003 | `progress persists across app restarts` | 进度在应用重启后持久化 | ⚠️ 未实现 | P0 |

---

## 📱 E2E测试用例

### 关键用户流程

#### 场景1: 首次启动

**文件**: `app/src/androidTest/java/com/wordland/e2e/FirstLaunchTest.kt`

| 测试ID | 测试步骤 | 验证点 | 状态 |
|--------|---------|--------|------|
| EE-001 | 1. 启动应用<br>2. 等待初始化完成<br>3. 检查关卡选择界面 | - 数据库已创建<br>- Level 1状态为UNLOCKED<br>- 无crash | ⚠️ 未实现 |

---

#### 场景2: 完整学习流程

**文件**: `app/src/androidTest/java/com/wordland/e2e/LearningFlowTest.kt`

| 测试ID | 测试步骤 | 验证点 | 状态 |
|--------|---------|--------|------|
| EE-002 | 1. 选择Level 1<br>2. 完成5个单词<br>3. 提交答案<br>4. 查看反馈 | - 单词显示正确<br>- 答案验证正确<br>- 星级计算正确<br>- 进度保存 | ⚠️ 未实现 |

---

#### 场景3: 关卡解锁

**文件**: `app/src/androidTest/java/com/wordland/e2e/LevelUnlockTest.kt`

| 测试ID | 测试步骤 | 验证点 | 状态 |
|--------|---------|--------|------|
| EE-003 | 1. 完成Level 1所有单词<br>2. 返回关卡选择<br>3. 检查Level 2状态 | - Level 2解锁<br>- 进度正确<br>- 数据持久化 | ⚠️ 未实现 |

---

## 🚧 待修复的测试

### 已禁用的测试

**当前无禁用测试**

如需禁用某个测试，使用：
```kotlin
@Ignore("Reason for disabling")
@Test
fun `test name`() {
    // ...
}
```

---

## 📈 测试优先级说明

### P0 - Critical（关键）
- 核心业务逻辑
- 用户数据完整性
- 应用崩溃风险
- **目标**: 100%覆盖

### P1 - High（高）
- 重要功能验证
- 边界条件测试
- 性能关键路径
- **目标**: 80%覆盖

### P2 - Medium（中）
- 辅助功能验证
- UI细节测试
- 优化场景测试
- **目标**: 60%覆盖

---

## 🎯 测试实现计划

### Phase 1: 补全Domain层（Week 1-2）
- [ ] DA-009: 连续错误答案测试
- [ ] DA-010: 难度修正系数测试
- [ ] DG-004~006: 猜测检测模式测试
- [ ] DU-004~005: 答案提交进度更新测试
- [ ] DL-001~003: 关卡加载用例测试
- [ ] DM-001~002: Word模型验证测试
- [ ] DP-001~003: UserWordProgress模型测试

**预期覆盖率**: Domain层 90%+

---

### Phase 2: 实现Data层（Week 2-3）
- [ ] DR-001~004: WordRepository集成测试
- [ ] DT-001~003: TrackingRepository集成测试
- [ ] DD-001~005: DAO基础操作测试
- [ ] ID-001~003: 数据库集成测试

**预期覆盖率**: Data层 80%+

---

### Phase 3: 实现UI层（Week 3-4）
- [ ] UV-001~006: LearningViewModel测试
- [ ] UL-001~003: LevelSelectViewModel测试
- [ ] US-001~005: LearningScreen UI测试
- [ ] UL-001~003: LevelSelectScreen UI测试

**预期覆盖率**: UI层 60%+

---

### Phase 4: 集成与E2E测试（Week 4）
- [ ] IU-001~003: UseCase集成测试
- [ ] EE-001~003: 端到端流程测试

**预期覆盖率**: 整体 80%+

---

## 🔍 测试覆盖率追踪

### 当前状态（2026-02-16）

| 模块 | 当前 | 目标 | 差距 | 行动 |
|------|------|------|------|------|
| Domain.Algorithm | 83% | 90% | -7% | 补充DA-009, DA-010 |
| Domain.UseCase | 60% | 90% | -30% | 补充DU-004, DL-001~003 |
| UI.ViewModel | 0% | 60% | -60% | 实现所有ViewModel测试 |
| Data.Repository | 0% | 80% | -80% | 实现所有Repository测试 |
| **整体** | **50%** | **80%** | **-30%** | 按计划执行Phase 1-4 |

---

## 📝 测试编写规范

### 命名约定

**测试类**: `<被测类>Test.kt`
```
MemoryStrengthAlgorithmTest.kt
SubmitAnswerUseCaseTest.kt
LearningViewModelTest.kt
```

**测试方法**: ``<操作> should <预期结果>``
```kotlin
@Test
fun `correct answer should increase memory strength`() {
    // ...
}
```

### 测试结构

```kotlin
@Test
fun `descriptive test name`() = runTest {
    // Given - 准备测试数据
    val input = createTestData()

    // When - 执行被测操作
    val result = useCase.execute(input)

    // Then - 验证结果
    assertTrue(result.isSuccess)
    assertEquals(expected, result.data)
}
```

---

## 🔧 测试运行命令

### 运行所有测试
```bash
./gradlew test
```

### 运行特定模块
```bash
# Domain层
./gradlew test --tests "com.wordland.domain.*"

# Data层
./gradlew test --tests "com.wordland.data.*"

# UI层
./gradlew test --tests "com.wordland.ui.*"
```

### 运行单个测试类
```bash
./gradlew test --tests "com.wordland.domain.algorithm.MemoryStrengthAlgorithmTest"
```

### 运行单个测试方法
```bash
./gradlew test --tests "com.wordland.domain.algorithm.MemoryStrengthAlgorithmTest.correct answer increases memory strength"
```

### 生成覆盖率报告
```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/test/html/index.html
```

---

## 📚 相关文档

- [测试策略](./TEST_STRATEGY.md)
- [覆盖率报告](./TEST_COVERAGE_REPORT.md)
- [测试指南](./TESTING_GUIDE.md)
- [TDD实践指南](./TDD_GUIDE.md)

---

**最后更新**: 2026-02-16
**维护者**: Android Team
**下次审查**: 2026-03-01
