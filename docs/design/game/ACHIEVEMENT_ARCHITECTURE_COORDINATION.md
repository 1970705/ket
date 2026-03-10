# Achievement System Architecture Coordination

**Version**: 1.0
**Date**: 2026-02-18
**Purpose**: 技术架构与UI设计的协调文档
**Collaborators**: android-architect + compose-ui-designer

---

## 数据模型 (Data Model)

### Domain Models (UI 可直接使用)

```kotlin
// File: domain/model/achievement/Achievement.kt
@Immutable
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,              // Emoji: "🌟", "🏆", etc.
    val category: AchievementCategory,
    val tier: AchievementTier,     // BRONZE, SILVER, GOLD, PLATINUM
    val isHidden: Boolean = false
)

enum class AchievementCategory {
    PROGRESS,       // 绿色 - 学习里程碑
    PERFORMANCE,    // 黄色 - 卓越表现
    COMBO,          // 橙色 - 连击成就
    STREAK,         // 蓝色 - 连续学习
    SPECIAL         // 紫色 - 特殊挑战
}

enum class AchievementTier(val stars: Int, val color: Long) {
    BRONZE(1, 0xFFCD7F32),
    SILVER(2, 0xFFC0C0C0),
    GOLD(3, 0xFFFFD700),
    PLATINUM(4, 0xFFE5E4E2)
}
```

```kotlin
// File: domain/model/achievement/UserAchievement.kt
@Immutable
data class UserAchievement(
    val userId: String,
    val achievementId: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1,
    val unlockedAt: Long? = null
) {
    val progressPercentage: Float
        get() = if (target > 0) (progress.toFloat() / target).coerceAtMost(1f) else 0f
}
```

```kotlin
// File: domain/model/achievement/AchievementWithProgress.kt
@Immutable
data class AchievementWithProgress(
    val achievement: Achievement,
    val userProgress: UserAchievement
) {
    val isUnlocked: Boolean get() = userProgress.isUnlocked
    val progress: Int get() = userProgress.progress
    val target: Int get() = userProgress.target
    val percentage: Float get() = userProgress.progressPercentage
}
```

### UI 需要的字段映射

| UI 需求 | 数据模型字段 | 来源 |
|---------|-------------|------|
| 成就图标 | `achievement.icon` | Achievement |
| 成就名称 | `achievement.name` | Achievement |
| 成就描述 | `achievement.description` | Achievement |
| 分类颜色 | `achievement.category` | Achievement (映射到颜色) |
| 阶级颜色 | `achievement.tier.color` | AchievementTier |
| 是否解锁 | `userProgress.isUnlocked` | UserAchievement |
| 当前进度 | `userProgress.progress` | UserAchievement |
| 目标值 | `userProgress.target` | UserAchievement |
| 进度百分比 | `userProgress.progressPercentage` | 计算属性 |
| 解锁时间 | `userProgress.unlockedAt` | UserAchievement |

---

## 状态管理 (State Management)

### ViewModel 设计

```kotlin
// File: ui/viewmodel/AchievementViewModel.kt
@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val checkAchievementsUseCase: CheckAchievementsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow<AchievementUiState>(AchievementUiState.Loading)
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    // 解锁事件流 (用于通知)
    private val _unlockEvents = MutableSharedFlow<AchievementUnlockEvent>(
        replay = 3,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val unlockEvents: SharedFlow<AchievementUnlockEvent> = _unlockEvents.asSharedFlow()

    init {
        loadAchievements()
    }

    fun filterByCategory(category: AchievementCategory?) {
        viewModelScope.launch {
            if (category == null) {
                getAchievements(userId).collect { achievements ->
                    _uiState.value = AchievementUiState.Ready(
                        achievements = achievements,
                        statistics = getAchievements.getStatistics(userId)
                    )
                }
            } else {
                getAchievements.getByCategory(userId, category).collect { achievements ->
                    _uiState.value = AchievementUiState.Ready(
                        achievements = achievements,
                        statistics = getAchievements.getStatistics(userId)
                    )
                }
            }
        }
    }
}

// UI 状态 Sealed Class
sealed class AchievementUiState {
    object Loading : AchievementUiState()
    data class Ready(
        val achievements: List<AchievementWithProgress>,
        val statistics: AchievementStatistics
    ) : AchievementUiState()
    data class Error(val message: String) : AchievementUiState()
}

// 统计数据
data class AchievementStatistics(
    val total: Int,
    val unlocked: Int,
    val percentage: Float
)
```

### UI 订阅更新

```kotlin
// 在 Composable 中使用
@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 订阅解锁事件 (用于显示通知)
    val unlockEvents by viewModel.unlockEvents
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // 显示通知层
    AchievementNotificationOverlay(
        events = unlockEvents,
        onDismiss = { viewModel.dismissNotification() }
    )

    when (val state = uiState) {
        is AchievementUiState.Loading -> { /* Loading UI */ }
        is AchievementUiState.Ready -> {
            AchievementList(
                achievements = state.achievements,
                statistics = state.statistics
            )
        }
        is AchievementUiState.Error -> { /* Error UI */ }
    }
}
```

---

## 组件架构 (Component Architecture)

### 推荐的包结构

```
ui/
├── screens/
│   └── AchievementScreen.kt           # 成就墙主屏幕
├── components/
│   └── achievement/
│       ├── AchievementCard.kt         # 成就卡片
│       ├── AchievementIcon.kt         # 成就图标组件
│       ├── AchievementProgressBar.kt  # 进度条
│       ├── AchievementFilterChip.kt   # 分类过滤
│       ├── AchievementUnlockedDialog.kt # 解锁弹窗
│       ├── AchievementNotificationOverlay.kt # 通知层
│       └── AchievementStatsHeader.kt  # 统计头部
└── viewmodel/
    └── AchievementViewModel.kt        # (已设计)
```

### 组件职责

| 组件 | 职责 | 复用性 |
|------|------|--------|
| `AchievementCard` | 显示单个成就，支持锁定/进行中/已解锁状态 | 高 - 可在其他地方复用 |
| `AchievementIcon` | 显示成就图标（带背景色） | 高 |
| `AchievementProgressBar` | 显示进度条 | 中 |
| `AchievementUnlockedDialog` | 解锁庆祝弹窗 | 高 |
| `AchievementNotificationOverlay` | 全局通知层 | 中 |
| `AchievementFilterChip` | 分类筛选 | 低 (仅成就墙) |
| `AchievementStatsHeader` | 统计信息 | 低 (仅成就墙) |

### 与现有组件的复用策略

**可复用的现有组件**:
- `LinearProgressIndicator` (Material 3) - 直接使用
- `Card` (Material 3) - 直接使用
- `FilterChip` (Material 3) - 直接使用
- `Icon` (Material Icons) - 使用已有图标

**需要新建的组件**:
- `AchievementCard` - 成就特有布局
- `AchievementIcon` - 成就特有图标样式
- `ConfettiEffect` - 庆祝动画（未来可用于其他场景）

---

## 导航集成 (Navigation Integration)

### 路由定义

```kotlin
// 在 SetupNavGraph.kt 中添加
composable(
    route = "achievements",
    arguments = emptyList()
) { backStackEntry ->
    val achievementViewModel: AchievementViewModel = hiltViewModel()

    AchievementScreen(
        onNavigateBack = { navController.popBackStack() },
        viewModel = achievementViewModel
    )
}
```

### 导航触发

```kotlin
// 从 HomeScreen 进入
Button(onClick = { navController.navigate("achievements") }) {
    Text("Achievements")
}

// 从 ProfileScreen 进入
Row(
    modifier = Modifier.clickable { navController.navigate("achievements") }
) {
    Icon(imageVector = Icons.Outlined.EmojiEvents, contentDescription = null)
    Text("My Achievements")
}
```

---

## 数据预加载/缓存策略

### Repository 缓存

```kotlin
@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    // 内存缓存
    private val achievementsCache = mutableMapOf<String, Achievement>()

    suspend fun getAllAchievements(): List<Achievement> {
        return if (achievementsCache.isEmpty()) {
            val fromDb = achievementDao.getAllAchievements()
            fromDb.forEach { achievementsCache[it.id] = it.toDomainModel() }
            fromDb.map { it.toDomainModel() }
        } else {
            achievementsCache.values.toList()
        }
    }
}
```

### UI 加载优化

1. **首次加载**: 显示骨架屏 (Skeleton)
2. **分类切换**: 使用内存缓存，无延迟
3. **图片**: 使用 Emoji，无需网络加载

---

## 颜色主题映射

### Category 颜色

```kotlin
object AchievementCategoryColors {
    fun getColor(category: AchievementCategory): Color = when (category) {
        AchievementCategory.PROGRESS -> Color(0xFF4CAF50)    // Green
        AchievementCategory.PERFORMANCE -> Color(0xFFFFC107) // Amber
        AchievementCategory.COMBO -> Color(0xFFFF5722)       // Orange Red
        AchievementCategory.STREAK -> Color(0xFF2196F3)       // Blue
        AchievementCategory.SPECIAL -> Color(0xFF9C27B0)      // Purple
    }
}
```

### Tier 颜色

```kotlin
object AchievementTierColors {
    fun getColor(tier: AchievementTier): Color = when (tier) {
        AchievementTier.BRONZE -> Color(0xFFCD7F32)
        AchievementTier.SILVER -> Color(0xFFC0C0C0)
        AchievementTier.GOLD -> Color(0xFFFFD700)
        AchievementTier.PLATINUM -> Color(0xFFE5E4E2)
    }
}
```

---

## 动画规格

### 解锁动画时序

```kotlin
// 时序定义
object AchievementAnimationSpec {
    val fadeInDuration = 300.ms
    val scaleInDuration = 400.ms
    val confettiDuration = 1000.ms
    val dialogDisplayDuration = 5000.ms  // 自动关闭时间

    val fadeInEasing = FastOutSlowInEasing
    val scaleInEasing = SpringSpec dampingRatio = 0.6f
}
```

---

## Compose 最佳实践

### 组件设计原则

1. **@Immutable**: 所有数据模型使用 `@Immutable`
2. **单一职责**: 每个组件只负责一个功能
3. **可组合性**: 小组件组合成大组件
4. **状态提升**: State 向上提升，Event 向下传递

### 示例

```kotlin
@Composable
fun AchievementCard(
    achievementWithProgress: AchievementWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 状态：无内部状态，完全受控
    // 事件：onClick 向上传递
    // 组合：由 AchievementIcon + Text + ProgressBar 组成
}
```

---

## 下一步协作

### compose-ui-designer 需要

1. 确认组件包结构是否合理
2. 确认数据模型满足 UI 需求
3. 确认颜色方案
4. 设计具体的 Compose 组件

### android-architect 将提供

1. 完整的数据模型代码
2. ViewModel 接口定义
3. UseCase 接口定义
4. 导航集成代码

---

**Document Version**: 1.0
**Last Updated**: 2026-02-18
**Collaborators**: android-architect, compose-ui-designer
