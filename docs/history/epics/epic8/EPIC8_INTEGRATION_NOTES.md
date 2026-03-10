# Epic #8 集成说明

**文档类型**: 集成指南
**创建日期**: 2026-02-26
**目标读者**: android-engineer, compose-ui-designer
**用途**: 为后续 Epic 复用 Epic #8 功能提供参考

---

## 概述

本文档说明 Epic #8 实现的各功能如何与现有系统集成，以及如何在未来的 Epic 中复用这些功能。

---

## Star Breakdown 集成

### 1. 与 LearningScreen 集成

**集成点**: 关卡完成时

**流程**:
```
LearningScreen (用户完成关卡)
    ↓
LevelCompleteScreen (显示星级)
    ↓ [用户点击"查看星级详情"]
StarBreakdownScreen (显示详细评分)
    ↓ [用户点击"继续探险"]
LevelSelectScreen / IslandMapScreen
```

**导航配置** (`SetupNavGraph.kt`):
```kotlin
composable(
    route = "starBreakdown/{levelId}/{islandId}/{stars}/{accuracy}/{hintsUsed}/{timeTaken}/{errorCount}",
    arguments = listOf(
        navArgument("levelId") { type = NavType.StringType },
        navArgument("islandId") { type = NavType.StringType },
        navArgument("stars") { type = NavType.IntType },
        navArgument("accuracy") { type = NavType.IntType },
        navArgument("hintsUsed") { type = NavType.IntType },
        navArgument("timeTaken") { type = NavType.IntType },
        navArgument("errorCount") { type = NavType.IntType }
    )
) { backStackEntry ->
    val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
    val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
    val stars = backStackEntry.arguments?.getInt("stars") ?: 0
    // ... 其他参数
    StarBreakdownScreen(
        levelId = levelId,
        islandId = islandId,
        stars = stars,
        // ... 其他参数
    )
}
```

**触发按钮** (`LearningScreen.kt`):
```kotlin
// 在 LevelCompleteState 中
Button(
    onClick = {
        navController.navigate(
            "starBreakdown/$levelId/$islandId/$stars/$accuracy/$hintsUsed/$timeTaken/$errorCount"
        )
    }
) {
    Text("查看星级详情")
}
```

### 2. 与 StarRatingCalculator 集成

**数据来源**: `StarRatingCalculator.calculateStars()`

**评分组成映射**:

| StarBreakdownScreen 显示 | StarRatingCalculator 输出 | 类型 |
|-------------------------|--------------------------|------|
| 准确率评分 | `accuracyScore` | Double (0-3.0) |
| 时间奖励/惩罚 | `timeBonus` | Double (-0.6 ~ +0.3) |
| 错误惩罚 | `errorPenalty` | Double (0 ~ -0.3) |
| Combo加成 | `comboBonus` | Double (0 ~ +1.0) |
| 使用提示 | `hintsUsed` | Int |
| 错误次数 | `errorCount` | Int |

**集成代码** (`LearningViewModel.kt`):
```kotlin
// 计算星级
val ratingResult = StarRatingCalculator.calculateStars(
    correctAnswers = correctCount,
    totalQuestions = totalCount,
    hintsUsed = hintsUsed,
    averageTimeSeconds = avgTime,
    maxCombo = maxCombo,
    wrongAnswers = wrongCount
)

// 导航到 StarBreakdownScreen
navController.navigate(
    "starBreakdown/$levelId/$islandId/${ratingResult.stars}/${ratingResult.accuracy}/..."
)
```

### 3. 与 Progress 系统集成

**数据持久化**: `LevelProgress` 表

**保存数据**:
```kotlin
@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: String,
    val islandId: String,
    val bestScore: Int,        // 最高星级
    val currentStars: Int,     // 当前星级
    val completionCount: Int,  // 完成次数
    val totalAttempts: Int     // 尝试次数
)
```

**更新时机**:
- 关卡完成时更新 `currentStars`
- 如果 `currentStars > bestScore`，更新 `bestScore`
- 增加完成计数和尝试计数

---

## 动画系统集成

### 1. 过渡动画

**位置**: `LearningScreenTransitions.kt`

**使用场景**:

| 动画 | 使用场景 | 触发条件 |
|------|---------|---------|
| `WordSwitchTransition` | 问题切换 | 用户提交答案后 |
| `QuestionToFeedbackTransition` | 反馈展示 | 答案验证完成 |
| `LevelCompleteReveal` | 关卡完成 | 所有单词完成 |
| `MilestoneCelebrationTransition` | 里程碑庆祝 | Combo = 5, 10, 15... |
| `HintExpandTransition` | 提示展开 | 用户点击提示按钮 |
| `StarBreakdownTransition` | 星级详情 | 进入详情屏幕 |

**集成示例**:
```kotlin
// 在 LearningScreen 中使用
@Composable
fun LearningScreen(
    viewModel: LearningViewModel = viewModel(...)
) {
    val uiState by viewModel.uiState.collectAsState()

    LearningScreenTransitions.QuestionToFeedbackTransition(
        visible = uiState is LearningUiState.Feedback,
        content = {
            FeedbackContent(
                result = (uiState as LearningUiState.Feedback).result
            )
        }
    )
}
```

### 2. 庆祝动画

**位置**: `CelebrationAnimation.kt`, `OptimizedConfettiRenderer.kt`

**使用场景**:
- 3星获得（完整庆祝）
- 里程碑达成（combo=5, 10等）
- 关卡完成

**触发代码**:
```kotlin
@Composable
fun LevelCompleteScreen(stars: Int) {
    Box {
        LevelCompleteContent(stars = stars)

        // 3星时触发庆祝动画
        if (stars == 3) {
            CelebrationAnimation(
                type = CelebrationType.Full,
                onComplete = { /* 动画结束回调 */ }
            )
        }
    }
}
```

### 3. 性能配置

**性能模式**:
```kotlin
enum class PerformanceMode {
    HIGH,    // 完整粒子效果（高端设备）
    BALANCED, // 平衡模式（中端设备）
    LOW      // 简化动画（低端设备）
}

// 设置性能模式
CelebrationAnimation(
    type = CelebrationType.Full,
    performanceMode = PerformanceMode.BALANCED
)
```

**自动检测** (建议实现):
```kotlin
fun detectPerformanceMode(): PerformanceMode {
    val memInfo = ActivityManager.MemoryInfo()
    (activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        .getMemoryInfo(memInfo)

    return when {
        memInfo.totalMem > 6_000_000_000L -> PerformanceMode.HIGH
        memInfo.totalMem > 3_000_000_000L -> PerformanceMode.BALANCED
        else -> PerformanceMode.LOW
    }
}
```

---

## UI 打磨集成

### 1. 主题系统

**文件位置**: `ui/theme/`

**颜色主题**:
```kotlin
// 使用 Material 3 色彩系统
@Composable
fun ScoreFactorCard(
    performance: PerformanceLevel  // Excellent, Good, NeedsImprovement
) {
    val color = when (performance) {
        PerformanceLevel.Excellent -> MaterialTheme.colorScheme.primary
        PerformanceLevel.Good -> MaterialTheme.colorScheme.secondary
        PerformanceLevel.NeedsImprovement -> MaterialTheme.colorScheme.error
    }
    // ...
}
```

**字体系统**:
```kotlin
// 使用 Material 3 typescale
Text(
    text = "星级详情",
    style = MaterialTheme.typography.headlineMedium
)
```

### 2. 无障碍集成

**组件**: `AccessibleComponents.kt`

**使用**:
```kotlin
// 替换基础 Button 为可访问版本
import com.wordland.ui.components.AccessibleButton

AccessibleButton(
    onClick = { /* ... */ },
    contentDescription = "查看星级详情",  // TalkBack 描述
    role = Role.Button                     // 语义角色
) {
    Text("查看星级详情")
}
```

**验证清单**:
- [ ] 所有交互元素有 `contentDescription`
- [ ] 触摸目标 ≥ 48dp
- [ ] 语义角色定义正确
- [ ] TalkBack 测试通过

---

## 测试集成

### 1. 单元测试

**文件**: `StarRatingCalculatorTest.kt`

**覆盖范围**:
- 星级评分算法（57个测试）
- 边界值测试
- Combo系统测试
- 时间奖励/惩罚测试

**运行测试**:
```bash
./gradlew test --tests "*StarRatingCalculatorTest"
```

### 2. UI 测试

**场景**: 8个真机测试场景

**文档**: `docs/guides/testing/EPIC8_RETEST_CHECKLIST.md`

**执行流程**:
```bash
# 1. 启动 logcat 监控
./epic8_test_logcat_monitor.sh

# 2. 执行测试场景
# 参考 EPIC8_RETEST_CHECKLIST.md

# 3. 填写测试报告
# EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md
```

---

## 后续 Epic 集成建议

### Epic #9: 单词消消乐

**可复用组件**:
- ✅ `LearningScreenTransitions` - 过渡动画
- ✅ `CelebrationAnimation` - 庆祝动画
- ✅ `StarBreakdownScreen` - 评分详情（可修改）

**集成步骤**:
1. 复制 `LearningScreenTransitions.kt` 到单词消消乐模块
2. 修改动画参数以适配游戏节奏
3. 创建 `WordMatchBreakdownScreen` 复用星级详情UI

**代码示例**:
```kotlin
// Epic #9 中复用动画
import com.wordland.ui.components.LearningScreenTransitions

@Composable
fun WordMatchGameScreen() {
    LearningScreenTransitions.MilestoneCelebrationTransition(
        visible = showCelebration,
        content = { WordMatchCelebration() }
    )
}
```

### Epic #10: Onboarding

**可复用组件**:
- ✅ `LearningScreenTransitions` - 过渡动画
- ✅ Material Design 3 主题系统
- ✅ 无障碍组件

**集成要点**:
- 使用更慢的动画速度（新手友好）
- 遵循 Material Design 3 规范
- 确保完整无障碍支持

### 其他功能

**通用复用建议**:
1. 使用 `ScoreFactorCard` 显示任何评分详情
2. 使用 `LearningScreenTransitions` 统一动画风格
3. 遵循 Epic #8 建立的 UI 标准

---

## 维护注意事项

### 1. 星级算法更新

**当星级算法变更时**:
1. 更新 `StarRatingCalculator.kt`
2. 更新 `StarBreakdownScreen.kt` 显示
3. 更新 `StarRatingCalculatorTest.kt` 测试
4. 更新本文档和 `EPIC8_UI_ENHANCEMENT_SUMMARY.md`
5. 重新执行真机测试（8场景）

**验证清单**:
- [ ] 单元测试全部通过
- [ ] UI 显示正确
- [ ] 文档已更新
- [ ] 真机测试通过

### 2. 动画性能监控

**定期检查**:
- 在真机上测试帧率（目标60fps）
- 监控低端设备表现（Xiaomi 24031PN0DC）
- 收集用户反馈

**性能问题处理**:
1. 使用 Android Profiler 定位瓶颈
2. 优化过度绘制
3. 减少布局嵌套
4. 调整性能模式阈值

### 3. 无障碍持续改进

**定期测试**:
- TalkBack 屏幕阅读器
- 大字体模式
- 高对比度模式
- 开关控制

**改进优先级**:
1. P0: 基础 TalkBack 支持（已完成）
2. P1: 完整 TalkBack 测试（待完成）
3. P2: 高对比度模式（待完成）
4. P3: 开关控制支持（待完成）

---

## 故障排除

### 常见问题

**Q1: StarBreakdownScreen 显示数据不正确**
- 检查导航参数传递
- 验证 StarRatingCalculator 输出
- 查看 logcat 日志

**Q2: 动画卡顿**
- 检查性能模式设置
- 使用 Android Profiler 分析
- 考虑降低粒子数量

**Q3: 无障碍功能不工作**
- 验证 contentDescription 设置
- 检查触摸目标大小
- 使用 TalkBack 测试

### 调试命令

```bash
# 查看 StarRatingCalculator 日志
adb logcat | grep "StarRatingCalculator"

# 监控帧率
adb shell dumpsys gfxinfo com.wordland

# 检查内存使用
adb shell dumpsys meminfo com.wordland
```

---

## 参考资料

### Epic #8 文档
- **UI增强总结**: `docs/design/ui/EPIC8_UI_ENHANCEMENT_SUMMARY.md`
- **完成报告**: `docs/reports/quality/EPIC8_COMPLETION_REPORT.md`
- **Epic计划**: `docs/planning/epics/Epic8/EPIC8_UI_ENHANCEMENT_PLAN.md`

### 源代码
- **StarBreakdownScreen**: `app/src/main/java/com/wordland/ui/screens/StarBreakdownScreen.kt`
- **动画过渡**: `app/src/main/java/com/wordland/ui/components/LearningScreenTransitions.kt`
- **星级计算**: `app/src/main/java/com/wordland/domain/algorithm/StarRatingCalculator.kt`
- **导航配置**: `app/src/main/java/com/wordland/navigation/SetupNavGraph.kt`

### 外部参考
- **Material Design 3**: https://m3.material.io/
- **Compose Animation**: https://developer.android.com/jetpack/compose/animation
- **无障碍指南**: https://developer.android.com/guide/topics/ui/accessibility

---

**文档版本**: 1.0
**最后更新**: 2026-02-26
**维护者**: android-architect
**状态**: ✅ 完成（可提前完成的40%文档工作）
