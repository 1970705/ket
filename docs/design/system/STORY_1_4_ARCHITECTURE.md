# Story #1.4: 进度条增强架构设计

**Sprint**: Sprint 1
**Story**: #1.4 - 进度条增强
**负责角色**: android-architect (架构设计), compose-ui-designer (UI实现)
**日期**: 2026-02-21
**状态**: 📐 架构设计

---

## 📋 Story 概述

**目标**: 增强 LearningScreen 的进度条显示，添加平滑动画和视觉反馈

**当前状态**: 基础进度条已存在
**需要完成**:
1. 平滑进度动画 (spring 动画)
2. 里程碑标记 (关卡中途点)
3. 星级进度指示器

---

## 🏗️ 现有架构分析

### 当前实现

**文件**: `ui/components/ProgressBar.kt`

**已有组件**:
```kotlin
// LinearProgressIndicator - 基础进度条
@Composable
fun LevelProgressBar(
    progress: Float,  // 0f - 1f
    modifier: Modifier = Modifier
)

// 使用位置: LearningScreen.kt
LevelProgressBar(
    progress = currentWordIndex / totalWords.toFloat()
)
```

**问题**:
- 进度更新时无平滑过渡
- 无里程碑标记
- 无星级指示

---

## 📐 架构设计

### 1. 域模型扩展

```kotlin
// domain/model/ProgressAnimation.kt (新建)

/**
 * 进度动画配置
 */
data class ProgressAnimationConfig(
    val enabled: Boolean = true,
    val springDampingRatio: Float = 0.8f,  // 弹簧阻尼
    val springStiffness: Float = 300f,      // 弹簧刚度
    val durationMs: Int = 600               // 最大持续时间
)

/**
 * 里程碑标记
 */
data class ProgressMilestone(
    val progress: Float,        // 位置 (0f - 1f)
    val label: String,          // 标签 (如 "50%")
    val isAchieved: Boolean = false,
    val icon: String? = null     // 可选图标
)

/**
 * 星级进度状态
 */
data class StarProgressState(
    val currentStars: Int = 0,           // 当前获得星级
    val maxStars: Int = 3,               // 最大星级
    val progressToNextStar: Float = 0f,  // 到下一星的进度
    val milestones: List<ProgressMilestone> = emptyList()
)
```

### 2. ViewModel 扩展

```kotlin
// LearningViewModel.kt 扩展

// 添加状态
private val _progressAnimationState = MutableStateFlow(StarProgressState())
val progressAnimationState: StateFlow<StarProgressState> = _progressAnimationState.asStateFlow()

// 动画配置
private val progressAnimationConfig = ProgressAnimationConfig()

/**
 * 更新进度并触发动画
 */
fun updateProgressWithAnimation(
    currentWordIndex: Int,
    totalWords: Int,
    currentStars: Int
) {
    val progress = currentWordIndex.toFloat() / totalWords.toFloat()

    // 计算里程碑
    val milestones = generateProgressMilestones(progress, totalWords)

    // 计算到下一星的进度
    val progressToNextStar = calculateProgressToNextStar(
        currentWordIndex,
        totalWords,
        currentStars
    )

    _progressAnimationState.value = StarProgressState(
        currentStars = currentStars,
        maxStars = 3,
        progressToNextStar = progressToNextStar,
        milestones = milestones
    )
}

private fun generateProgressMilestones(
    progress: Float,
    totalWords: Int
): List<ProgressMilestone> {
    val milestones = mutableListOf<ProgressMilestone>()

    // 25% 里程碑
    if (progress >= 0.25f) {
        milestones.add(ProgressMilestone(0.25f, "25%", true, "🌟"))
    }

    // 50% 里程碑
    if (progress >= 0.5f) {
        milestones.add(ProgressMilestone(0.5f, "50%", true, "⭐"))
    }

    // 75% 里程碑
    if (progress >= 0.75f) {
        milestones.add(ProgressMilestone(0.75f, "75%", true, "✨"))
    }

    return milestones
}

private fun calculateProgressToNextStar(
    currentWordIndex: Int,
    totalWords: Int,
    currentStars: Int
): Float {
    // 每颗星需要完成约33%的关卡
    val starProgress = (currentWordIndex % (totalWords / 3)).toFloat() /
                       (totalWords / 3).toFloat()
    return starProgress.coerceIn(0f, 1f)
}
```

### 3. UI 组件设计

```kotlin
// ui/components/EnhancedProgressBar.kt (新建)

/**
 * 增强进度条 - 带平滑动画
 */
@Composable
fun EnhancedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    config: ProgressAnimationConfig = ProgressAnimationConfig(),
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = config.springDampingRatio,
            stiffness = config.springStiffness
        ),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
        color = color,
        trackColor = backgroundColor
    )
}

/**
 * 里程碑进度条 - 带标记点
 */
@Composable
fun MilestoneProgressBar(
    progress: Float,
    milestones: List<ProgressMilestone>,
    modifier: Modifier = Modifier,
    config: ProgressAnimationConfig = ProgressAnimationConfig()
) {
    Box(modifier = modifier) {
        EnhancedProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            config = config
        )

        // 里程碑标记
        milestones.forEach { milestone ->
            MilestoneMarker(
                milestone = milestone,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = ((milestone.progress - 0.5f) * 2).dp)
            )
        }
    }
}

/**
 * 单个里程碑标记
 */
@Composable
private fun MilestoneMarker(
    milestone: ProgressMilestone,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (milestone.isAchieved) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "milestone_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (milestone.isAchieved) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
        ) {
            Text(
                text = milestone.icon ?: "✓",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 星级进度指示器
 */
@Composable
fun StarProgressIndicator(
    state: StarProgressState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 星级显示
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(state.maxStars) { index ->
                val isFilled = index < state.currentStars
                val scale by animateFloatAsState(
                    targetValue = if (isFilled) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = "star_$index"
                )

                Text(
                    text = if (isFilled) "⭐" else "☆",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                )
            }
        }

        // 到下一星的进度
        if (state.currentStars < state.maxStars) {
            Spacer(Modifier.height(8.dp))

            EnhancedProgressBar(
                progress = state.progressToNextStar,
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp)
            )

            Text(
                text = "${(state.progressToNextStar * 100).toInt()}% 下一星",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### 4. LearningScreen 集成

```kotlin
@Composable
fun LearningScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(...)
) {
    val uiState by viewModel.uiState.collectAsState()
    val progressState by viewModel.progressAnimationState.collectAsState()

    Scaffold(
        topBar = {
            LearningAppBar(
                onNavigateBack = onNavigateBack,
                progressState = progressState
            )
        }
    ) { paddingValues ->
        // ... 现有内容
    }
}

@Composable
private fun LearningAppBar(
    onNavigateBack: () -> Unit,
    progressState: StarProgressState
) {
    Column {
        TopAppBar(
            title = { /* ... */ },
            navigationIcon = { /* ... */ }
        )

        // 进度条区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MilestoneProgressBar(
                progress = progressState.currentStars / 3f,
                milestones = progressState.milestones,
                modifier = Modifier.fillMaxWidth()
            )

            StarProgressIndicator(
                state = progressState,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

---

## 📊 性能考虑

### 动画性能目标

| 指标 | 目标值 | 实现方式 |
|------|--------|----------|
| 动画帧率 | 60fps | Spring Animation |
| 内存增长 | < 5MB | 无额外资源 |
| 重组频率 | 最小化 | animateFloatAsState |

### 优化策略

1. **使用 Spring Animation**
   - 自然的物理效果
   - Compose 原生支持
   - 性能优于 tween

2. **避免过度重组**
   - 使用 `remember` 缓存计算结果
   - 仅在进度变化时触发动画

3. **里程碑懒加载**
   - 仅计算可见里程碑
   - 避免不必要的对象创建

---

## ✅ 验收标准

### 功能验收

- [ ] 进度条平滑动画 (spring 效果)
- [ ] 25%/50%/75% 里程碑标记
- [ ] 星级进度指示器
- [ ] 到下一星的百分比显示

### 性能验收

- [ ] 帧率 ≥ 60fps
- [ ] 动画无卡顿
- [ ] 内存增长 < 5MB

### 代码质量

- [ ] 通过 KtLint 检查
- [ ] 通过 Detekt 分析
- [ ] 单元测试覆盖率 ≥ 80%

---

## 📅 实施计划

| 任务 | 工作量 | 依赖 | 负责人 |
|------|--------|------|--------|
| 域模型创建 | 1小时 | 无 | android-architect |
| ViewModel 扩展 | 1.5小时 | 域模型 | android-architect |
| UI 组件实现 | 2小时 | ViewModel | compose-ui-designer |
| LearningScreen 集成 | 0.5小时 | UI 组件 | compose-ui-designer |
| 测试和调优 | 0.5小时 | 所有 | 全员 |

**总计**: 0.5天 (4小时)

---

## 🔗 相关文件

| 文件 | 操作 | 状态 |
|------|------|------|
| `domain/model/ProgressAnimation.kt` | 新建 | 待创建 |
| `domain/model/StarProgressState.kt` | 新建 | 待创建 |
| `ui/viewmodel/LearningViewModel.kt` | 扩展 | 待修改 |
| `ui/components/EnhancedProgressBar.kt` | 新建 | 待创建 |
| `ui/screens/LearningScreen.kt` | 集成 | 待修改 |

---

**文档状态**: 📐 架构设计完成
**下一步**: 提交给 compose-ui-designer 开始实现
