# Story #2.1: 世界视图切换架构设计

**Sprint**: Sprint 1
**Story**: #2.1 - 世界视图切换优化
**负责角色**: android-architect
**日期**: 2026-02-20
**状态**: ✅ 架构设计完成

---

## 📋 Story 概述

**目标**: 优化 WorldMapScreen 的视图切换体验，添加流畅的过渡动画和视觉反馈

**当前状态**: 基础功能已实现（ISLAND_VIEW/WORLD_VIEW 切换）
**需要完成**: 过渡动画 (500ms)、视图状态优化、切换按钮视觉反馈

---

## 🏗️ 现有架构分析

### 当前实现

**文件**: `ui/screens/WorldMapScreen.kt`

**已有组件**:
```kotlin
// 数据模型 (domain/model/WorldMapState.kt)
- MapViewMode enum (ISLAND_VIEW, WORLD_VIEW)
- WorldMapState (regions, fogState, playerPosition, viewMode, exploredRegions)
- MapRegion (id, islandId, name, position, bounds, fogLevel, isUnlocked, icon)
- FogLevel enum (VISIBLE, PARTIAL, HIDDEN, LOCKED)

// UseCase
- ToggleMapViewModeUseCase (简单的状态切换)

// ViewModel (ui/viewmodel/WorldMapViewModel.kt)
- toggleViewMode() 方法
- 状态管理: WorldMapUiState

// UI (ui/screens/WorldMapScreen.kt)
- WorldMapAppBar (带切换按钮)
- IslandViewContent (岛屿列表视图)
- WorldMapViewContent (世界地图视图)
- FogOverlay 组件 (已实现)
```

---

## 📐 架构设计

### 1. 状态管理扩展

```kotlin
// WorldMapViewModel.kt 扩展

/**
 * View transition state
 */
data class ViewTransitionState(
    val isTransitioning: Boolean = false,
    val progress: Float = 0f,  // 0f = 完全隐藏, 1f = 完全显示
    val fromMode: MapViewMode? = null,
    val toMode: MapViewMode? = null
)

// 添加到 ViewModel
private val _viewTransitionState = MutableStateFlow<ViewTransitionState>(ViewTransitionState())
val viewTransitionState: StateFlow<ViewTransitionState> = _viewTransitionState.asStateFlow()

/**
 * Toggle view mode with transition animation
 */
fun toggleViewModeWithAnimation() {
    val currentState = _uiState.value
    if (currentState !is WorldMapUiState.Ready) return

    val newMode = toggleMapViewModeUseCase(currentViewMode)
    val fromMode = currentViewMode

    // 启动过渡动画
    viewModelScope.launch {
        _viewTransitionState.value = ViewTransitionState(
            isTransitioning = true,
            progress = 0f,
            fromMode = fromMode,
            toMode = newMode
        )

        // 动画完成 500ms
        delay(500)

        _viewTransitionState.value = ViewTransitionState(
            isTransitioning = false,
            progress = 1f,
            fromMode = null,
            toMode = null
        )
    }

    currentViewMode = newMode
    _uiState.value = currentState.copy(viewMode = newMode)
}
```

### 2. 过渡动画组件

```kotlin
/**
 * Crossfade transition between view modes
 */
@Composable
fun ViewModeTransition(
    viewMode: MapViewMode,
    transitionState: ViewTransitionState,
    islandContent: @Composable () -> Unit,
    worldContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val slideOffset = with(density) { 100.dp.toPx() }

    Box(modifier = modifier) {
        // Island View (exit)
        androidx.compose.animation.AnimatedVisibility(
            visible = viewMode == MapViewMode.ISLAND_VIEW || transitionState.isTransitioning,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            ) + slideOutHorizontally(
                targetOffsetX = { -slideOffset },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                islandContent()
            }
        }

        // World View (enter)
        androidx.compose.animation.AnimatedVisibility(
            visible = viewMode == MapViewMode.WORLD_VIEW || transitionState.isTransitioning,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 250)) + slideInHorizontally(
                initialOffsetX = { slideOffset },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            ) + slideOutHorizontally(
                targetOffsetX = { slideOffset },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                worldContent()
            }
        }
    }
}
```

### 3. 切换按钮增强

```kotlin
/**
 * Enhanced view mode toggle button with animation feedback
 */
@Composable
private fun ViewModeToggleButton(
    viewMode: MapViewMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (viewMode == MapViewMode.WORLD_VIEW) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (viewMode == MapViewMode.WORLD_VIEW) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape,
                )
                .graphicsLayer { rotationZ = rotation },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (viewMode == MapViewMode.ISLAND_VIEW) "🏝️" else "🌍",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
```

### 4. WorldMapScreen 集成

```kotlin
@Composable
fun WorldMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelSelect: (String) -> Unit,
    viewModel: WorldMapViewModel = viewModel(...)
) {
    LaunchedEffect(Unit) {
        viewModel.initialize(WordlandApplication.USER_ID)
    }

    val uiState by viewModel.uiState.collectAsState()
    val transitionState by viewModel.viewTransitionState.collectAsState()

    Scaffold(
        topBar = {
            WorldMapAppBar(
                onNavigateBack = onNavigateBack,
                viewMode = (uiState as? WorldMapUiState.Ready)?.viewMode ?: MapViewMode.ISLAND_VIEW,
                onViewModeToggle = { viewModel.toggleViewModeWithAnimation() },
                transitionState = transitionState
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is WorldMapUiState.Ready -> {
                ViewModeTransition(
                    viewMode = state.viewMode,
                    transitionState = transitionState,
                    islandContent = {
                        IslandViewContent(
                            state = state,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            onRegionClick = { regionId ->
                                viewModel.onRegionTap(WordlandApplication.USER_ID, regionId)
                                onNavigateToLevelSelect(regionId)
                            },
                        )
                    },
                    worldContent = {
                        WorldMapViewContent(
                            state = state,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            onRegionClick = { regionId ->
                                viewModel.onRegionTap(Wordland.APPLICATION_USER_ID, regionId)
                                onNavigateToLevelSelect(regionId)
                            },
                        )
                    }
                )
            }
            // ... other states
        }
    }
}
```

---

## 📊 性能考虑

### 动画性能目标

| 指标 | 目标值 | 测量方法 |
|------|--------|----------|
| 过渡动画时长 | 500ms | AnimationSpec |
| 帧率 | 60fps | FrameMetrics |
| 内存增长 | < 10MB | Android Profiler |

### 优化策略

1. **使用 Compose 内置动画**
   - `animateFloatAsState` - 单值动画
   - `animateColorAsState` - 颜色动画
   - `AnimatedVisibility` - 进入/退出动画

2. **避免过度重组**
   - 使用 `key()` 稳定 LazyColumn 项
   - 使用 `remember` 缓存计算结果

3. **GPU 渲染优化**
   - 硬件加速 (默认启用)
   - 避免过多透明度叠加

---

## ✅ 验收标准

### 功能验收

- [ ] 视图切换流畅，过渡动画 500ms
- [ ] 切换按钮有明显视觉状态变化（背景色、旋转动画）
- [ ] 世界视图显示群岛布局效果
- [ ] 岛屿视图显示卡片列表

### 性能验收

- [ ] 帧率 ≥ 60fps（使用 GPU Profiler 测量）
- [ ] 过渡期间无卡顿
- [ ] 内存增长 < 10MB

### 代码质量

- [ ] 代码通过 KtLint 检查
- [ ] 代码通过 Detekt 分析
- [ ] 单元测试覆盖率 ≥ 80%

---

## 📅 实施计划

| 任务 | 工作量 | 依赖 |
|------|--------|------|
| ViewModel 扩展（过渡状态） | 2小时 | 无 |
| 过渡动画组件 | 3小时 | 无 |
| 切换按钮增强 | 1小时 | 无 |
| WorldMapScreen 集成 | 1小时 | 上述完成 |
| 测试和调优 | 1小时 | 所有功能 |

**总计**: 1天 (8小时)

---

## 🔗 相关文件

| 文件 | 操作 | 状态 |
|------|------|------|
| `ui/screens/WorldMapScreen.kt` | 修改 | 待修改 |
| `ui/viewmodel/WorldMapViewModel.kt` | 扩展 | 待修改 |
| `domain/model/WorldMapState.kt` | 已完整 | ✅ 无需修改 |
| `ui/components/FogOverlay.kt` | 已完整 | ✅ 无需修改 |

---

**文档状态**: ✅ 架构设计完成
**下一步**: 提交给 android-engineer 开始实现
