# Wordland 项目开发计划 (PLAN 阶段)

**制定日期**: 2026-02-16
**项目状态**: 最小原型完成 + 增强提示系统架构完成
**目标**: 制定下一阶段开发优先级和执行路线图

---

## 1. 当前状态评估

### 1.1 架构完整性 ✅ 优秀

**优势**:
- Clean Architecture 清晰分层 (UI → Domain → Data)
- Hilt 2.48 + Service Locator 混合DI方案稳定
- 领域层设计完善（UseCase, Model, Algorithm 分离）
- 提示系统架构完整（HintGenerator, HintManager, BehaviorAnalyzer）
- 性能监控基础设施完善（4个监控文件）

**待完善**:
- LearningViewModel 已集成 UseHintUseCaseEnhanced ✅
- LearningUiState 支持 hint 元数据 ✅
- AppServiceLocator 支持 hint 系统 ✅
- LearningScreen UI 显示 progressive hints ✅
- **缺少**: 等级完成星级聚合逻辑（当前固定3星）

### 1.2 代码质量 ⚠️ 良好但需改进

**优势**:
- 500单元测试，100%通过率
- 静态分析工具配置完整（Detekt, KtLint, JaCoCo）
- CI/CD流水线已配置（GitHub Actions）
- 代码风格一致性强

**问题**:
- **测试覆盖率仅12%**（目标80%），差距巨大
- 关键模块0%覆盖：
  - UI Screens: 0% (13,297 指令)
  - UI Components: 0% (9,925 指令)
  - Data Repository: 0%
  - Navigation: 0%
- 缺少 `@Immutable` / `@Stable` 注解（性能优化）
- 大型 Composable 可能过度重组（LearningScreen 432行）

### 1.3 测试覆盖 ❌ 严重不足

**当前覆盖分析**:
```
✅ 优秀模块 (80%+):
  - data.converter: 100%
  - domain.hint: 91% (HintGenerator, HintManager)
  - domain.model: 82%

⚠️ 需改进模块 (30-60%):
  - ui.viewmodel: 34%
  - ui.uistate: 42%
  - usecase.usecases: 40%
  - data.seed: 38%
  - domain.algorithm: 59%

❌ 关键空白 (0%):
  - ui.screens: 0%
  - ui.components: 0%
  - data.repository: 0%
  - navigation: 0%
  - domain.behavior: 0%
```

### 1.4 性能状况 ⚠️ 基础设施完善但未充分利用

**已具备**:
- PerformanceMonitor（帧率、内存、操作计时）
- StartupPerformanceTracker（启动时间追踪）
- ComposePerformanceHelper（重组追踪）
- Macrobenchmark + Microbenchmark 模块

**问题**:
- 性能监控未被使用（未在代码中集成）
- 缺少性能基线数据
- 未运行基准测试建立性能指标
- 缺少 `@Immutable` / `@Stable` 注解

### 1.5 用户体验 ❌ 存在严重问题

**关键问题**:
1. **星级评分算法对儿童不友好**
   - 当前：2秒答对 = 猜测 = 1星
   - 问题：10岁儿童拼写简单的词（如"cat"）可能只需1-2秒
   - 影响：挫败感，降低学习动力

2. **等级完成星级固定**
   - 当前：LevelComplete 始终显示3星
   - 问题：不反映实际表现
   - 影响：失去成就感和进步反馈

3. **提示系统UI已集成但未完全测试**
   - EnhancedHintCard 已实现
   - UseHintUseCaseEnhanced 已集成
   - 问题：完整用户流程未测试

**优势**:
- 渐进式提示设计合理
- 提示惩罚机制清晰
- 防滥用机制完善

---

## 2. 目标和假设

### 2.1 下一阶段目标（2-4周）

**核心目标**:
1. **修复儿童友好的评分系统**（P0）
2. **提升测试覆盖率至50%+**（P0，从12%提升）
3. **建立性能基线并优化关键路径**（P1）
4. **完整测试提示系统用户流程**（P1）

### 2.2 MVP 完成标准

**功能完整**:
- ✅ 30个词汇，5个关卡
- ✅ Spell Battle 游戏模式
- ✅ 提示系统完整工作（渐进式提示 + 限制 + 惩罚）
- ⏳ 动态星级评分（儿童友好）
- ⏳ 等级完成星级聚合

**质量标准**:
- ⏳ 测试覆盖率 ≥ 50%（当前12%）
- ✅ 所有单元测试通过
- ⏳ 真机设备测试通过（2+设备）
- ⏳ 性能基线达标（启动 < 3s，帧率 60fps）

### 2.3 关键假设

**假设1: 儿童拼写速度**
- **假设**: 10岁儿童拼写3-5字母的KET词汇，平均需要3-5秒
- **验证**: 需要真机测试收集实际数据
- **风险**: 如果假设错误，评分算法仍不公平

**假设2: 性能优化收益**
- **假设**: 添加 `@Immutable` 注解可显著降低重组
- **验证**: 需要运行 Compose Benchmark 对比
- **风险**: 低（低成本高收益）

**假设3: 测试覆盖优先级**
- **假设**: 聚焦 Domain + Data 层可快速提升覆盖率至50%
- **验证**: 模块分析显示这两层当前覆盖率 40-60%
- **风险**: UI 层测试需要 instrumentation（成本高）

---

## 3. 任务优先级排序

### P0 任务（必须立即解决）

#### P0-1: 重新设计星级评分算法（儿童友好）

**描述**: 修改 SubmitAnswerUseCase 中的评分逻辑，使其对10岁儿童更友好

**当前问题**:
```kotlin
// SubmitAnswerUseCase.kt:137
responseTime < DomainConstants.GUESSING_THRESHOLD_FAST -> 2  // < 2000ms = 2星
isGuessing -> 1  // 猜测 = 1星
```

**问题分析**:
- `GUESSING_THRESHOLD_FAST = 2000ms` 对简单词（如"cat", "look"）太严
- 快速回答被判定为"猜测"，儿童会觉得不公平

**改进方案**:
```kotlin
// 根据单词长度动态调整时间阈值
val timeThreshold = when (word.word.length) {
    <= 3 -> 1500  // 3字母词: 1.5秒
    4 -> 2500     // 4字母词: 2.5秒
    5 -> 3500     // 5字母词: 3.5秒
    else -> 5000  // 6+字母词: 5秒
}

// 评分逻辑
when {
    !isCorrect -> 0
    isGuessing && responseTime < 1000 -> 1  // <1秒肯定是猜测
    hintUsed -> 2
    responseTime < timeThreshold -> 3  // 在合理范围内快速答对
    else -> 2  // 慢但正确
}
```

**影响**:
- ✅ 提升用户体验，减少挫败感
- ✅ 更准确反映学习水平
- ⚠️ 需要真机测试验证阈值

**工作量**: 2-3小时
**依赖**: 无

**关键文件**:
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCase.kt`
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/constants/DomainConstants.kt`

---

#### P0-2: 实现等级完成星级聚合逻辑

**描述**: 计算6个单词的平均星级，显示在 LevelCompleteScreen

**当前问题**:
```kotlin
// LearningViewModel.kt:217-218
_uiState.value = LearningUiState.LevelComplete(
    stars = 3,  // 固定3星
    score = 100,  // 固定100分
```

**改进方案**:
```kotlin
// 在 LearningViewModel 中追踪星级
private val starsEarnedInLevel = mutableListOf<Int>()

// 在 submitAnswer 成功后记录
when (val result = submitAnswer(...)) {
    is Result.Success -> {
        starsEarnedInLevel.add(result.data.starsEarned)
        // ...
    }
}

// 等级完成时计算
val averageStars = if (starsEarnedInLevel.isNotEmpty()) {
    starsEarnedInLevel.average().toInt().coerceIn(0, 3)
} else 0

val totalScore = starsEarnedInLevel.sum() * 10

_uiState.value = LearningUiState.LevelComplete(
    stars = averageStars,
    score = totalScore,
    // ...
)
```

**影响**:
- ✅ 真实反映学习表现
- ✅ 提供成就感反馈
- ✅ 激励儿童追求更高星级

**工作量**: 1-2小时
**依赖**: P0-1（星级评分算法）

**关键文件**:
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt`

---

#### P0-3: 提升核心模块测试覆盖率至50%+

**描述**: 聚焦 Domain + Data 层，快速提升覆盖率

**目标**:
```
当前: 12% (500 tests)
目标: 50%+ (1000+ tests)
差距: +500 tests
```

**策略**: 高收益低成本的纯Kotlin测试

**模块优先级**:

1. **Data Repository（优先级最高）**
   - 当前: 0%
   - 目标: 60%
   - 方法: Integration tests with in-memory Room DB
   - 预计新增: ~150 tests
   - 关键文件:
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/data/repository/WordRepository.kt`
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/data/repository/ProgressRepository.kt`
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/data/repository/TrackingRepository.kt`

2. **UseCase 扩展**
   - 当前: 40%
   - 目标: 80%
   - 方法: 扩展现有测试，增加边界情况
   - 预计新增: ~100 tests
   - 关键文件:
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCase.kt`
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/usecase/usecases/UseHintUseCaseEnhanced.kt`

3. **ViewModel 扩展**
   - 当前: 34%
   - 目标: 60%
   - 方法: MockK 测试 UI 状态转换
   - 预计新增: ~100 tests
   - 关键文件:
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt`

4. **Data Seed 扩展**
   - 当前: 38%
   - 目标: 80%
   - 方法: 验证所有词汇数据完整性
   - 预计新增: ~100 tests
   - 关键文件:
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/data/seed/LookIslandWords.kt`

5. **Domain Algorithm 扩展**
   - 当前: 59%
   - 目标: 90%
   - 方法: 增加边界情况测试
   - 预计新增: ~50 tests
   - 关键文件:
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/algorithm/GuessingDetector.kt`
     - `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/algorithm/MemoryStrengthAlgorithm.kt`

**影响**:
- ✅ 快速提升整体覆盖率（从12% → 50%+）
- ✅ 提高代码质量和可维护性
- ✅ 防止回归错误

**工作量**: 3-5天
**依赖**: 无

**执行顺序**:
1. Repository tests（最高优先级，数据层核心）
2. UseCase tests（业务逻辑核心）
3. ViewModel tests（UI逻辑）
4. Algorithm + Seed tests（辅助功能）

---

#### P0-4: 真机首次启动测试（质量门禁）

**描述**: 在真实Android设备上执行完整首次启动流程测试

**测试内容**:
- APK 安装
- 首次启动（冷启动）
- 数据库初始化
- Level 1 解锁状态
- 完成一个单词（含提示）
- 等级完成
- 进度保存
- 应用重启后进度恢复
- Logcat 无 ERROR/CRASH

**脚本**:
```bash
#!/bin/bash
# scripts/test/test_first_launch.sh

# 1. 卸载旧版本
adb uninstall com.wordland

# 2. 安装新APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 清除数据
adb shell pm clear com.wordland

# 4. 启动应用并计时
adb shell am start -W -n com.wordland/.ui.MainActivity

# 5. 等待启动完成
sleep 3

# 6. 检查数据库
adb shell run-as com.wordland ls -la /data/data/com.wordland/databases/

# 7. 检查日志
adb logcat -d | grep -E "(Wordland|ERROR|FATAL)" | head -50

# 8. 截图
adb shell screencap -p /sdcard/launch_screen.png
adb pull /sdcard/launch_screen.png
```

**验证点**:
- [ ] 冷启动时间 < 3秒
- [ ] 数据库文件存在
- [ ] Level 1 状态为 UNLOCKED
- [ ] 无 ERROR/CRASH 日志
- [ ] UI 响应正常

**影响**:
- ✅ 验证真实设备可用性
- ✅ 发现模拟器无法发现的问题
- ✅ 建立质量门禁标准

**工作量**: 2-3小时（含设备准备）
**依赖**: 已有APK构建

---

### P1 任务（重要但非紧急）

#### P1-1: 运行性能基准测试并建立基线

**描述**: 执行 Macrobenchmark + Microbenchmark，建立性能基线

**基准测试内容**:

1. **Macrobenchmark**（`benchmark/` 模块）
   - 冷启动时间（目标: < 3s）
   - 温启动时间（目标: < 1s）
   - 关卡切换帧率（目标: 60fps，jank < 5%）
   - 提交答案响应时间

2. **Microbenchmark**（`microbenchmark/` 模块）
   - HintGenerator 性能
   - GuessingDetector 性能
   - MemoryStrengthAlgorithm 性能

**执行命令**:
```bash
# 运行自动化性能测试脚本
./benchmark_performance.sh

# 或手动执行
./gradlew :benchmark:connectedCheck
./gradlew :microbenchmark:connectedCheck
```

**输出**:
- 性能报告 HTML
- 基线数据 JSON
- 回归检测结果

**影响**:
- ✅ 了解当前性能水平
- ✅ 检测性能回归
- ✅ 指导优化方向

**工作量**: 1-2天（含测试编写+执行）
**依赖**: Android 设备/模拟器

**关键文件**:
- `/Users/panshan/git/ai/ket/benchmark/`
- `/Users/panshan/git/ai/ket/microbenchmark/`
- `/Users/panshan/git/ai/ket/benchmark_performance.sh`

---

#### P1-2: 添加 Compose 性能优化注解

**描述**: 为数据类添加 `@Immutable` 注解，减少重组

**目标文件**:
```kotlin
// domain/model/*.kt
@Immutable
data class SpellBattleQuestion(...)

@Immutable
data class LearnWordResult(...)

@Immutable
data class SubmitAnswerResult(...)

// ui/uistate/*.kt
@Immutable
sealed class LearningUiState {
    @Immutable
    data class Ready(...)
}
```

**收益**:
- ✅ Compose 编译器优化
- ✅ 减少跳过重组（skips）
- ✅ 提升帧率稳定性

**工作量**: 2-3小时
**依赖**: 无

**关键文件**:
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/model/`
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/uistate/`

---

#### P1-3: 优化 LearningScreen 重组性能

**描述**: 拆分大型 Composable（432行），减少重组范围

**当前问题**:
```kotlin
// LearningScreen.kt:212-296
@Composable
private fun LearningContent(...) {
    // 432行代码，任何状态变化都会重组整个组件
}
```

**优化方案**:
1. 提取子组件：
   - `ProgressBar`
   - `HintSection`
   - `GameArea`
   - `ActionButtons`

2. 使用 `remember` 缓存计算结果
3. 使用 `derivedStateOf` 派生状态
4. 使用 `key()` 帮助 Compose 跟踪

**影响**:
- ✅ 减少重组范围
- ✅ 提升帧率
- ✅ 改善电池续航

**工作量**: 4-6小时
**依赖**: P1-2（添加 @Immutable 注解）

**关键文件**:
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/screens/LearningScreen.kt`

---

#### P1-4: 集成 PerformanceMonitor 到关键路径

**描述**: 在关键操作中添加性能监控

**监控点**:
```kotlin
// Application.onCreate
PerformanceMonitor.initialize()

// LearningViewModel.loadLevel
PerformanceMonitor.measure("LevelLoad") {
    loadLevelWords(levelId)
}

// LearningViewModel.submitAnswer
PerformanceMonitor.measure("SubmitAnswer") {
    submitAnswer(...)
}

// 关卡完成
PerformanceMonitor.logReport()
```

**影响**:
- ✅ 收集真实性能数据
- ✅ 发现性能瓶颈
- ✅ 监控生产环境性能

**工作量**: 2-3小时
**依赖**: 无

**关键文件**:
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/WordlandApplication.kt`
- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt`

---

#### P1-5: 完整测试提示系统用户流程

**描述**: 端到端测试提示系统的完整用户流程

**测试场景**:
1. 首次使用提示（Level 1）
2. 再次使用提示（Level 2）
3. 达到提示上限
4. 使用提示后提交答案（验证扣分）
5. 新单词提示重置
6. 冷却时间测试

**测试方法**:
- 真机手动测试
- 或编写 instrumentation test（androidTest）

**影响**:
- ✅ 验证提示系统完整性
- ✅ 发现UX问题
- ✅ 确保扣分逻辑正确

**工作量**: 4-6小时
**依赖**: P0-4（真机测试）

---

### P2 任务（可以延后）

#### P2-1: 增加反馈消息多样性

**描述**: 丰富 SubmitAnswerUseCase 的反馈消息

**当前问题**:
```kotlin
// 只有5条固定消息
"Excellent! Perfect answer!"
"Good job! You're doing great!"
"Nice try! Keep it up!"
```

**改进方案**:
- 每种星级3-5条随机消息
- 根据词汇难度调整消息
- 添加鼓励性表情符号

**影响**:
- ✅ 提升趣味性
- ✅ 避免单调重复

**工作量**: 1-2小时
**依赖**: 无

---

#### P2-2: UI Screens Instrumentation Tests

**描述**: 为关键 UI Screens 添加 Compose 测试

**测试内容**:
- LearningScreen 导航
- HintCard 显示/隐藏
- 答案提交流程
- 等级完成显示

**影响**:
- ✅ 提升UI测试覆盖率
- ✅ 防止UI回归

**工作量**: 2-3天
**依赖**: P0-3（ViewModel tests完成）

---

#### P2-3: 设置 CI 性能回归检测

**描述**: 在 GitHub Actions 中添加性能基准测试

**实现**:
```yaml
# .github/workflows/performance.yml
name: Performance Benchmark
on: [pull_request]
jobs:
  benchmark:
    runs-on: macos-latest
    steps:
      - run: ./gradlew :benchmark:connectedCheck
      - run: ./scripts/compare_benchmark.py
```

**影响**:
- ✅ 自动检测性能回归
- ✅ 防止性能退化

**工作量**: 1-2天
**依赖**: P1-1（建立性能基线）

---

#### P2-4: Compose UI Components 测试

**描述**: 为 SpellBattleGame 等组件添加 Compose 测试

**测试内容**:
- 虚拟键盘点击
- 答案框更新
- 提交按钮状态

**影响**:
- ✅ 提升UI组件覆盖率
- ✅ 保证UI稳定性

**工作量**: 2-3天
**依赖**: P0-3（ViewModel tests完成）

---

## 4. 风险识别

### 4.1 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 评分算法阈值不准确 | 中 | 中 | 真机测试收集数据，A/B测试 |
| 性能优化效果不明显 | 低 | 低 | 先运行基准测试建立基线 |
| @Immutable注解引入bug | 低 | 低 | 完善单元测试，代码审查 |
| 提示系统扣分逻辑错误 | 高 | 低 | 完整端到端测试 |
| 测试覆盖提升速度慢 | 中 | 中 | 聚焦Domain+Data层（纯Kotlin） |

### 4.2 资源风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 真机测试设备不足 | 中 | 低 | 使用模拟器 + 1台真机 |
| 开发时间不足 | 高 | 中 | 严格优先级，P0任务优先 |
| 测试编写速度慢 | 中 | 中 | 复用现有测试模式，AI辅助 |

### 4.3 用户体验风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| 评分算法仍不公平 | 高 | 中 | 用户测试收集反馈，快速迭代 |
| 提示系统过于复杂 | 中 | 低 | 用户测试，简化UI |
| 性能问题影响体验 | 中 | 低 | 性能监控，及时优化 |

---

## 5. 执行计划

### 第一阶段（Week 1-2）：核心用户体验改进

**目标**: 修复关键的儿童友好性问题

**任务**:
1. **P0-1: 重新设计星级评分算法**（2-3小时）
   - 修改时间阈值逻辑
   - 根据单词长度动态调整
   - 单元测试验证

2. **P0-2: 实现等级完成星级聚合**（1-2小时）
   - 在 ViewModel 追踪星级
   - 计算平均星级
   - 更新 LevelComplete UI

3. **P0-4: 真机首次启动测试**（2-3小时）
   - 执行测试脚本
   - 验证功能完整性
   - 收集性能数据

4. **P1-5: 完整测试提示系统用户流程**（4-6小时）
   - 手动端到端测试
   - 验证扣分逻辑
   - 记录UX问题

**里程碑**:
- ✅ 星级评分系统儿童友好
- ✅ 真机测试通过
- ✅ 提示系统完整工作

**验证标准**:
- 10岁儿童真机测试，无挫败感反馈
- 真机测试脚本全部通过
- 提示系统流程无bug

---

### 第二阶段（Week 3-4）：测试覆盖提升 + 性能优化

**目标**: 提升代码质量和性能

**任务**:
1. **P0-3: 提升测试覆盖率至50%+**（3-5天）
   - Repository tests（~150 tests）
   - UseCase tests（~100 tests）
   - ViewModel tests（~100 tests）
   - Data Seed + Algorithm tests（~150 tests）

2. **P1-1: 运行性能基准测试**（1-2天）
   - 执行 Macrobenchmark
   - 执行 Microbenchmark
   - 生成性能报告

3. **P1-2: 添加 @Immutable 注解**（2-3小时）
   - 为 domain/model 添加注解
   - 为 ui/uistate 添加注解

4. **P1-4: 集成 PerformanceMonitor**（2-3小时）
   - 在关键路径添加监控
   - 收集真实性能数据

5. **P1-3: 优化 LearningScreen 重组**（4-6小时）
   - 拆分大型 Composable
   - 添加 remember/derivedStateOf
   - 验证性能提升

**里程碑**:
- ✅ 测试覆盖率 50%+
- ✅ 性能基线建立
- ✅ 关键路径性能监控
- ✅ Compose 优化完成

**验证标准**:
- `./gradlew test jacocoTestReport` 显示 50%+ 覆盖
- 性能报告显示启动 < 3s，帧率 60fps
- PerformanceMonitor 正常输出报告

---

### 里程碑总结

| 里程碑 | 目标 | 完成标志 | 预计时间 |
|--------|------|----------|----------|
| M1: 儿童友好评分 | 评分算法合理 | 真机测试通过 | Week 1 |
| M2: 测试覆盖50% | 500 → 1000+ tests | JaCoCo报告≥50% | Week 3 |
| M3: 性能基线 | 建立性能指标 | 基准测试报告 | Week 4 |
| M4: MVP完成 | 功能+质量达标 | 所有P0完成 | Week 4 |

---

## 6. 成功指标

### 6.1 功能指标
- ✅ 星级评分反映真实表现（等级完成星级动态）
- ✅ 提示系统完整工作（渐进式 + 限制 + 扣分）
- ✅ 真机测试2+设备通过

### 6.2 质量指标
- ⏳ 测试覆盖率 ≥ 50%（从12%提升）
- ✅ 所有单元测试通过（1000+ tests）
- ⏳ JaCoCo 报告无关键空白（0%模块 < 3个）

### 6.3 性能指标
- ⏳ 冷启动 < 3秒
- ⏳ 帧率稳定 60fps（jank < 5%）
- ⏳ 内存 < 150MB

### 6.4 用户体验指标
- ⏳ 儿童测试反馈积极（无挫败感）
- ⏳ 提示系统使用顺畅
- ⏳ 评分系统公平性认可

---

## 7. 下一步行动（立即执行）

### 今天/明天（优先级排序）
1. **立即开始**: P0-1（评分算法修改）- 2-3小时
2. **并行准备**: 真机测试设备 + 测试脚本
3. **第一周内完成**: P0-1, P0-2, P0-4

### 本周目标（Week 1）
- ✅ 修复评分算法（儿童友好）
- ✅ 实现等级星级聚合
- ✅ 完成真机首次启动测试
- ✅ 端到端测试提示系统

### 下周目标（Week 2）
- 开始测试覆盖提升（Repository tests）
- 执行性能基准测试
- 添加 @Immutable 注解

---

## 8. 假设验证计划

### 验证1: 儿童拼写速度假设
**方法**: 真机测试收集10个儿童的拼写时间数据
**指标**: 3-5字母词汇的平均拼写时间
**决策点**: 如果平均时间 > 5秒，调整阈值

### 验证2: 性能优化收益假设
**方法**: Compose Benchmark 对比优化前后
**指标**: 重组次数、帧时间
**决策点**: 如果提升 < 10%，调整优化策略

### 验证3: 测试覆盖优先级假设
**方法**: 追踪测试覆盖率增长速度
**指标**: 每小时提升的覆盖率百分比
**决策点**: 如果Domain+Data层 < 60%，调整测试策略

---

## Critical Files for Implementation

Based on this plan, here are the 5 most critical files for implementing the highest-priority tasks:

- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCase.kt` - P0-1: Modify star rating algorithm to be child-friendly (lines 126-140)

- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/viewmodel/LearningViewModel.kt` - P0-2: Implement level completion star aggregation logic (lines 110-224)

- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/domain/constants/DomainConstants.kt` - P0-1: Update guessing thresholds for different word lengths (lines 25-28)

- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/data/repository/WordRepository.kt` - P0-3: Add comprehensive integration tests for core data layer

- `/Users/panshan/git/ai/ket/app/src/main/java/com/wordland/ui/screens/LearningScreen.kt` - P1-3: Optimize recomposition by splitting large Composable (432 lines)

---

**PLAN 阶段完成**

**下一步**: 执行 EXECUTE 阶段，按优先级实施上述计划

**建议**: 从 P0-1 开始（评分算法修改），这是最高优先级的用户体验问题。
