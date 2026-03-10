# 架构评审：游戏体验改进方案

**文档版本**: 1.0
**创建日期**: 2026-02-17
**作者**: android-architect
**优先级**: P0 - 核心体验改进
**状态**: 待团队讨论

---

## 执行摘要

本评审从架构师角度对游戏体验改进方案进行技术可行性分析。基于**第一性原理**和**Clean Architecture**原则，评估每个功能的技术风险、实施复杂度和架构影响。

**核心发现**：
- ✅ 世界地图系统：技术上可行，推荐渐进式实施
- ⚠️ 语境化学习：需谨慎设计，存在增加认知负荷的风险
- ✅ 探索机制：可以与现有进度系统集成，但需引导式设计
- ⚠️ 整体风险：功能范围较大，建议分阶段实施

**推荐方案**：
- **Phase 1**（P0）：增强现有地图（迷雾系统、缩放）
- **Phase 2**（P1）：语境化学习（简单句子模式）
- **Phase 3**（P2）：完整探索机制（非线性进度、收集系统）

---

## 第一部分：需求分析（第一性原理）

### 问题 1：什么是真正的用户体验问题？

| 表面需求 | 根本问题 | 第一性原理分析 |
|---------|---------|---------------|
| "拼写枯燥" | 缺乏即时反馈循环 | 认知科学：即时反馈增强记忆形成 |
| "不像游戏" | 缺乏游戏机制和奖励 | 心流理论：挑战与技能匹配、明确目标 |
| "世界观不符" | 品牌承诺与实际体验脱节 | 认知一致性：期望管理很重要 |
| "缺乏语境" | 单词脱离实际使用场景 | 语境学习理论： situated learning 更有效 |

### 问题 2：这些改进是否真正服务于学习目标？

| 改进方向 | 学习价值 | 风险评估 | 结论 |
|---------|---------|---------|------|
| 视觉反馈/动画 | ⭐⭐⭐ 即时反馈增强记忆 | 过度可能分散注意力 | 平衡实施 |
| 世界地图+迷雾 | ⭐⭐ 探索动机 | 可能增加导航复杂度 | 渐进式引入 |
| 句子/故事模式 | ⭐⭐⭐⭐ 语境增强记忆 | 认知负荷过载风险 | 可选模式 |
| 非线性探索 | ⭐ 自主性驱动 | 10岁儿童可能迷失 | 引导式探索 |

---

## 第二部分：世界地图系统架构评审

### 2.1 当前架构分析

```
Current Architecture:
┌─────────────────────────────────────────────────────────┐
│ UI Layer                                                │
│ ├── IslandMapScreen (Compose LazyColumn)               │
│ └── IslandCard (点击导航到 LevelSelectScreen)          │
├─────────────────────────────────────────────────────────┤
│ Domain Layer                                            │
│ ├── GetIslandsUseCase                                   │
│ └── Island (name, color, masteryPercentage)             │
├─────────────────────────────────────────────────────────┤
│ Data Layer                                              │
│ ├── IslandMasteryDao                                   │
│ └── LevelDataSeeder (5 islands × 10 levels)            │
└─────────────────────────────────────────────────────────┘
```

**问题**：
- 地图只是一个列表（LazyColumn），没有空间感
- 缺乏探索机制（所有岛屿立即可见）
- 没有渐进式发现（无迷雾系统）

### 2.2 技术方案评估

#### 方案 A：Compose 自定义地图组件

**技术实现**：
```kotlin
@Composable
fun WorldMapScreen(
    modifier: Modifier = Modifier,
    state: WorldMapState,
    onRegionClick: (Region) -> Unit
) {
    // 使用 Canvas + PointerInput 实现缩放/平移
    // 使用 graphicsLayer 实现迷雾效果
}
```

**优势**：
- ✅ 完全控制 UI/UX
- ✅ 符合现有技术栈（Compose）
- ✅ 无额外依赖
- ✅ 性能可控

**劣势**：
- ⚠️ 开发工作量大（2-3周）
- ⚠️ 需要处理手势（缩放、平移）
- ⚠️ 需要优化大图渲染

**技术风险**：
- 中等：Canvas 渲染性能需测试
- 低：手势处理已有成熟方案

**推荐指数**: ⭐⭐⭐⭐⭐

#### 方案 B：第三方地图库

**选项**：
- Google Maps SDK
- OpenStreetMap + OSMDroid
- Mapbox

**优势**：
- ✅ 成熟的缩放/平移功能
- ✅ 内置地图渲染

**劣势**：
- ❌ 需要网络/API密钥
- ❌ 过度依赖第三方
- ❌ 应用体积增大
- ❌ 不符合自定义世界观需求
- ❌ 无法控制离线体验

**推荐指数**: ⭐ 不推荐

### 2.3 Clean Architecture 设计

#### Domain Layer

```kotlin
// Domain Models
@Immutable
data class WorldMapRegion(
    val id: String,           // "look_island"
    val name: String,         // "Look Island"
    val position: MapPosition, // (x, y) coordinates
    val bounds: MapBounds,    // 触摸区域
    val theme: IslandTheme,   // 视觉主题
    val masteryLevel: MasteryLevel,
    val isExplored: Boolean,  // 是否已探索
    val isUnlocked: Boolean
)

@Immutable
data class FogOfWarState(
    val exploredRegions: Set<String>,  // 已探索区域 ID
    val currentViewBounds: MapBounds,  // 当前视口
    val zoomLevel: Float              // 1.0 - 3.0
)

// Use Cases
class ExploreRegionUseCase(
    private val masteryRepository: IslandMasteryRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(regionId: String): ExplorationResult
}
```

#### Data Layer

```kotlin
// Room Entity
@Entity(tableName = "world_map_exploration")
data class MapExplorationEntity(
    @PrimaryKey val userId: String,
    val exploredRegions: String, // JSON: Set<String>
    val discoveryTimestamps: String, // JSON: Map<String, Long>
    val totalDiscoveries: Int
)

// Repository
interface WorldMapRepository {
    suspend fun getExploredRegions(userId: String): Set<String>
    suspend fun markRegionExplored(userId: String, regionId: String)
    suspend fun getDiscoveryProgress(userId: String): DiscoveryProgress
}
```

#### UI Layer

```kotlin
// ViewModel
class WorldMapViewModel @Inject constructor(
    private val getMapStateUseCase: GetWorldMapStateUseCase,
    private val exploreRegionUseCase: ExploreRegionUseCase,
    private val worldMapRepository: WorldMapRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorldMapUiState>(WorldMapUiState.Loading)
    val uiState: StateFlow<WorldMapUiState> = _uiState.asStateFlow()
}

// UI State
sealed class WorldMapUiState {
    object Loading : WorldMapUiState()
    data class Ready(
        val regions: List<WorldMapRegion>,
        val fogState: FogOfWarState,
        val userProgress: WorldMapProgress
    ) : WorldMapUiState()
}
```

### 2.4 性能考虑

| 方面 | 优化策略 | 预期影响 |
|------|---------|---------|
| 大图渲染 | 使用 `Modifier.drawBehind` + Canvas + bitmap caching | < 16ms 渲染时间 |
| 缩放/平移 | `graphicsLayer { scaleX, scaleY, translationX, translationY }` | GPU 加速 |
| 迷雾效果 | 使用 `BlendMode` 或图片遮罩 | < 5ms 渲染时间 |
| 内存管理 | 按需加载区域资源 | < 50MB 额外内存 |

### 2.5 数据持久化

```kotlin
// Migration: Add exploration tracking
database.execSQL("""
    CREATE TABLE IF NOT EXISTS world_map_exploration (
        userId TEXT PRIMARY KEY,
        exploredRegions TEXT NOT NULL,
        discoveryTimestamps TEXT NOT NULL,
        totalDiscoveries INTEGER NOT NULL DEFAULT 0,
        createdAt INTEGER NOT NULL,
        updatedAt INTEGER NOT NULL
    )
""")
```

---

## 第三部分：语境化学习系统架构评审

### 3.1 当前架构分析

```
Current Learning Flow:
┌─────────────────────────────────────────────────────────┐
│ LearningScreen                                          │
│ ├── 显示：中文翻译 + 空白字母框                          │
│ ├── Word.exampleSentences (JSON, 未使用)                │
│ └── SpellBattleGame (拼写组件)                          │
└─────────────────────────────────────────────────────────┘
```

**问题**：
- Word 实体已包含 `exampleSentences` JSON 字段，但未使用
- 没有语境化学习模式
- UI 只显示单词，无句子/故事

### 3.2 技术方案设计

#### 数据模型扩展

```kotlin
// Domain Models
@Immutable
data class Story(
    val id: String,           // "story_001"
    val title: String,        // "小明的一天"
    val theme: String,        // "daily_life"
    val difficulty: Int,      // 1-5
    val requiredLevel: Int,   // 需要完成的前置关卡
    val scenes: List<StoryScene>,
    val totalWords: Int,
    val imageAssets: List<String>?
)

@Immutable
data class StoryScene(
    val id: String,           // "story_001_scene_01"
    val storyId: String,
    val order: Int,           // 1, 2, 3...
    val context: String,      // 故事背景文本
    val dialogue: List<Dialogue>,
    val targetWords: List<ContextualWord>,
    val illustration: String? // 资源路径
)

@Immutable
data class Dialogue(
    val speaker: String,      // "小明", "妈妈", "NPC"
    val text: String,         // "Hello!"
    val translation: String?  // "你好！"
)

@Immutable
data class ContextualWord(
    val wordId: String,       // "word_001"
    val blankPosition: Int,   // 在句子中的位置
    val hint: String?,        // 语境提示
    val isTarget: Boolean     // 是否本场景的目标词
)
```

#### Clean Architecture 分层

```
┌─────────────────────────────────────────────────────────┐
│ UI Layer                                                │
│ ├── StoryModeScreen (故事选择)                         │
│ ├── SceneScreen (场景播放)                              │
│ └── ContextualLearningScreen (语境化拼写)              │
├─────────────────────────────────────────────────────────┤
│ Domain Layer                                            │
│ ├── GetStoriesUseCase                                   │
│ ├── GetStoryProgressUseCase                             │
│ ├── CompleteSceneUseCase                                │
│ └── StoryProgress (Entity)                              │
├─────────────────────────────────────────────────────────┤
│ Data Layer                                              │
│ ├── StoryDao                                            │
│ ├── StoryProgressDao                                    │
│ └── StorySeeder (内容初始化)                            │
└─────────────────────────────────────────────────────────┘
```

### 3.3 渐进式实施策略

**Phase 1: 句子模式（1周）**
```kotlin
// 使用现有的 exampleSentences JSON
@Composable
fun SentenceModeLearningScreen(
    word: Word,
    sentence: ExampleSentence  // 解析自 Word.exampleSentences
) {
    Column {
        Text(sentence.sentence)  // "I eat an _____ every morning."
        // 现有的 SpellBattleGame 组件
        SpellBattleGame(
            targetWord = word.word,
            hint = sentence.translation
        )
    }
}
```

**Phase 2: 故事模式（2-3周）**
```kotlin
// 需要 Story 数据结构和新的 UI
@Composable
fun StoryModeScreen(story: Story) {
    // 横版滚动故事卡片
    // 每个场景包含对话 + 目标词拼写
}
```

### 3.4 风险评估

| 风险 | 影响 | 缓解策略 |
|------|------|---------|
| 认知负荷过载 | 高 | 可选模式，不强求 |
| 内容创作成本 | 中 | 先实现少量故事，验证效果 |
| 技术复杂度 | 低-中 | 利用现有架构 |
| 与现有系统集成 | 低 | 复用 SpellBattleGame |

### 3.5 推荐方案

✅ **推荐**：渐进式实施
1. **Week 1**: 实现简单句子模式（复用 `Word.exampleSentences`）
2. **Week 2-3**: 设计并实现 3-5 个微型故事
3. **Week 4+**: 根据用户反馈决定是否扩展

❌ **不推荐**：立即实现完整故事系统（成本高，风险未验证）

---

## 第四部分：探索机制架构评审

### 4.1 非线性进度系统

#### 当前架构（线性）

```
Home → IslandMap → LevelSelect → Learning → LevelComplete
                                    ↓
                                Next Level (自动解锁)
```

#### 推荐架构（引导式非线性）

```
                    ┌─────────────────┐
                    │   Home Screen   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  World Map      │
                    │  (Fog of War)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
   ┌────▼────┐        ┌─────▼─────┐       ┌─────▼─────┐
   │ Level 1 │        │  Level 2  │       │  Level 3  │
   │ (主线)   │        │  (可选)   │       │  (隐藏)   │
   └────┬────┘        └───────────┘       └───────────┘
        │
        │ 完成解锁更多区域
        ▼
   ┌─────────┐
   │ Secret  │
   │ Content │
   └─────────┘
```

### 4.2 数据模型设计

```kotlin
// 探索进度追踪
@Immutable
@Entity(tableName = "exploration_progress")
data class ExplorationProgress(
    @PrimaryKey val userId: String,

    // 已发现内容
    val discoveredWords: String,      // JSON: Set<wordId>
    val discoveredSecrets: String,    // JSON: Set<secretId>
    val discoveredEggs: String,       // JSON: Set<eggId>

    // 探索统计
    val totalExplorations: Int,
    val uniqueDiscoveries: Int,
    val explorationStreak: Int,       // 连续探索天数

    // 特殊触发器
    val triggeredEvents: String,      // JSON: Set<eventId>
    val unlockedPaths: String,        // JSON: Set<pathId>

    val lastExplorationTime: Long,
    val updatedAt: Long
)

// 隐藏内容定义
@Immutable
@Entity(tableName = "hidden_content")
data class HiddenContent(
    @PrimaryKey val id: String,
    val type: HiddenContentType,     // WORD, SECRET, EGG, AREA
    val triggerCondition: TriggerCondition,
    val position: MapPosition?,      // 地图位置（如果是区域）
    val content: String,             // JSON 实际内容
    val rarity: DiscoveryRarity,     // COMMON, RARE, EPIC, LEGENDARY
    val isOneTime: Boolean           // 只能发现一次
)

enum class HiddenContentType {
    HIDDEN_WORD,      // 隐藏单词
    SECRET_AREA,      // 秘密区域
    EASTER_EGG,       // 彩蛋
    BONUS_CHALLENGE   // 奖励挑战
}

enum class TriggerCondition {
    CLICK_POSITION(position: MapPosition),
    COMPLETE_LEVEL(levelId: String, stars: Int),
    FIND_WORDS(wordIds: List<String>),
    TIME_BETWEEN_DISCOVERIES(ms: Long),
    COMBINATION(requirements: List<TriggerCondition>)
}
```

### 4.3 收集系统

```kotlin
// 收集图鉴数据模型
@Immutable
@Entity(tableName = "collection_progress")
data class CollectionProgress(
    @PrimaryKey val userId: String,

    // 按类别收集
    val animals: String,     // JSON: Set<wordId>
    val foods: String,       // JSON: Set<wordId>
    val actions: String,     // JSON: Set<wordId>
    val colors: String,      // JSON: Set<wordId>
    val clothes: String,     // JSON: Set<wordId>
    // ... 更多类别

    // 成就
    val totalCollected: Int,
    val categoriesCompleted: Int,
    val rarestFind: String?, // 最稀有的发现

    val updatedAt: Long
)

// Use Case
class UpdateCollectionUseCase(
    private val collectionRepository: CollectionRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(
        userId: String,
        wordId: String,
        category: WordCategory
    ): CollectionUpdateResult
}
```

### 4.4 技术实现要点

| 功能 | 实现方式 | 复杂度 |
|------|---------|-------|
| 随机事件 | `Random` + 权重系统 | 低 |
| 隐藏区域 | Map `position` + 触发检测 | 中 |
| 彩蛋 | 特殊日期/时间触发器 | 低 |
| 收集图鉴 | Room 查询 + Compose Grid | 中 |
| 非线性解锁 | 前置条件检查系统 | 中 |

---

## 第五部分：综合风险评估

### 5.1 技术风险矩阵

| 功能 | 技术风险 | 架构影响 | 性能影响 | 开发时间 | 总体风险 |
|------|---------|---------|---------|---------|---------|
| 世界地图+迷雾 | 中 | 低 | 中 | 2-3周 | **中** |
| 语境化学习 | 低 | 低 | 低 | 1-4周 | **中** |
| 探索机制 | 低-中 | 中 | 低 | 2-3周 | **中** |
| 收集系统 | 低 | 低 | 低 | 1周 | **低** |

### 5.2 架构兼容性分析

```
✅ 完全兼容：
- 所有功能都可以在现有 Clean Architecture 下实现
- 不需要破坏现有数据结构
- 可以渐进式添加

⚠️ 需要注意：
- 数据库迁移（添加新表）
- Service Locator 需要更新（新增 UseCases）
- 导航路由扩展（新 Screen）

❌ 避免的陷阱：
- 不要为了新功能破坏现有架构
- 不要引入过度抽象
- 不要过早优化
```

### 5.3 向后兼容性

| 组件 | 兼容性 | 说明 |
|------|-------|------|
| 数据库 | ⚠️ 需迁移 | 添加新表，现有表结构不变 |
| UseCase | ✅ 完全兼容 | 新增 UseCase，不修改现有 |
| ViewModel | ✅ 完全兼容 | 新增 ViewModel，不修改现有 |
| UI Screen | ✅ 完全兼容 | 新增 Screen，保留现有 |
| Repository | ✅ 完全兼容 | 扩展接口，不破坏实现 |

---

## 第六部分：实施路线图

### Phase 1: 地图增强（P0 - 2-3周）

**目标**：增强现有地图，添加探索感

```
Week 1:
├── 设计：地图 UI、迷雾效果
├── 实现：WorldMapScreen（Compose Canvas）
└── 测试：渲染性能、手势交互

Week 2:
├── 数据：FogOfWarState、探索进度表
├── 功能：区域探索、进度保存
└── 测试：数据持久化、进度恢复

Week 3:
├── 集成：与现有系统集成
├── 优化：性能调优、动画
└── 测试：真机测试、边界情况
```

**交付物**：
- `WorldMapScreen.kt` - 可缩放、可平移的地图
- `FogOfWarOverlay.kt` - 迷雾效果组件
- `WorldMapViewModel.kt` - 地图状态管理
- `WorldMapRepository.kt` + Dao - 探索进度数据

### Phase 2: 语境化学习（P1 - 3-4周）

**目标**：添加句子模式，验证语境学习效果

```
Week 1:
├── 数据：复用 Word.exampleSentences
├── UI：SentenceModeScreen
└── 集成：可从学习模式切换

Week 2-3:
├── 内容：设计 3-5 个微型故事
├── 数据：Story, StoryScene 实体
├── UI：StoryModeScreen, SceneScreen
└── 集成：故事进度追踪

Week 4:
├── 测试：用户接受度测试
├── 分析：学习效果评估
└── 决策：是否扩展故事系统
```

**交付物**：
- `SentenceModeScreen.kt` - 句子模式学习
- `StoryModeScreen.kt` - 故事模式 UI
- `StoryProgressViewModel.kt` - 故事进度管理
- 内容：3-5 个微型故事

### Phase 3: 探索机制（P2 - 3-4周）

**目标**：非线性进度和收集系统

```
Week 1-2:
├── 数据：HiddenContent, ExplorationProgress
├── 功能：随机发现系统
├── UI：发现动画、收集图鉴
└── 测试：发现概率调优

Week 3-4:
├── 功能：非线性解锁逻辑
├── 内容：隐藏区域、彩蛋设计
├── 集成：成就系统集成
└── 测试：完整用户体验测试
```

**交付物**：
- `HiddenContentService.kt` - 隐藏内容管理
- `CollectionScreen.kt` - 收集图鉴 UI
- `ExplorationTracker.kt` - 探索进度追踪
- 内容：隐藏内容定义

---

## 第七部分：性能影响评估

### 7.1 启动时间影响

| 组件 | 预估增加 | 优化策略 |
|------|---------|---------|
| 数据库迁移 | +100ms (首次) | 后台异步迁移 |
| 地图资源加载 | +50ms | 按需加载 |
| 故事内容加载 | +30ms | 延迟加载 |

**总计**: +180ms (首次), < 50ms (后续启动)

### 7.2 运行时性能影响

| 组件 | 预估影响 | 优化策略 |
|------|---------|---------|
| 地图渲染 | +5ms/frame | Canvas 缓存 |
| 迷雾效果 | +3ms/frame | BlendMode 优化 |
| 探索检测 | +1ms/click | 空间索引 |

**结论**: 在可接受范围内，60 FPS 可维持

### 7.3 内存占用

| 组件 | 预估增加 | 优化策略 |
|------|---------|---------|
| 地图资源 | +20MB | 按分辨率加载 |
| 故事资源 | +10MB | 按需加载 |
| 探索状态 | +5MB | 数据库查询 |

**总计**: +35MB（可接受）

---

## 第八部分：最终建议

### 8.1 核心建议（第一性原理）

**问题回归**：用户真正需要什么？
1. **参与度**：游戏要"好玩"才能持续学习
2. **有效性**：游戏不能干扰学习目标
3. **适龄性**：10岁儿童需要引导，不是开放世界

**架构原则**：
1. **Clean Architecture 优先**：所有新功能必须符合现有分层
2. **渐进式实施**：MVP 验证 → 扩展
3. **向后兼容**：不破坏现有功能
4. **性能第一**：60 FPS 不能妥协

### 8.2 推荐实施顺序

```
优先级排序（基于风险/价值比）:

P0 (必须 - 2-3周):
1. 世界地图增强（迷雾、缩放）
   - 风险：中
   - 价值：高
   - 理由：解决品牌承诺与体验脱节的核心问题

P1 (应该 - 3-4周):
2. 简单句子模式
   - 风险：低
   - 价值：中-高
   - 理由：利用现有数据，快速验证语境学习效果

P2 (可选 - 3-4周):
3. 完整故事系统
   - 风险：中
   - 价值：中
   - 理由：在句子模式验证成功后实施

P2 (可选 - 2-3周):
4. 探索机制 + 收集系统
   - 风险：低-中
   - 价值：中
   - 理由：增强重玩价值，但非核心学习功能
```

### 8.3 技术选型总结

| 决策 | 选择 | 理由 |
|------|------|------|
| 地图实现 | Compose Canvas 自定义 | 完全控制、无依赖 |
| 迷雾效果 | graphicsLayer + BlendMode | GPU 加速 |
| 故事存储 | Room (JSON 字段) | 灵活、易扩展 |
| 探索触发 | 条件检查系统 | 可配置、易测试 |
| 收集系统 | Room + Compose Grid | 简单、高效 |

### 8.4 风险缓解策略

1. **技术风险**：每个 Phase 完成后进行真机测试
2. **性能风险**：设置性能预算，持续监控
3. **架构风险**：Code Review 确保符合 Clean Architecture
4. **进度风险**：设置 MVP 范围，优先核心功能

---

## 附录：数据模型定义

### A.1 世界地图相关

```kotlin
// Domain Models
@Immutable
data class MapPosition(
    val x: Float,
    val y: Float
)

@Immutable
data class MapBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Immutable
data class WorldMapRegion(
    val id: String,
    val name: String,
    val position: MapPosition,
    val bounds: MapBounds,
    val theme: IslandTheme,
    val masteryLevel: MasteryLevel,
    val isExplored: Boolean,
    val isUnlocked: Boolean
)

// Room Entities
@Entity(tableName = "world_map_exploration")
data class MapExplorationEntity(
    @PrimaryKey val userId: String,
    val exploredRegions: String,  // JSON: Set<String>
    val discoveryTimestamps: String,  // JSON: Map<String, Long>
    val totalDiscoveries: Int,
    val createdAt: Long,
    val updatedAt: Long
)
```

### A.2 故事系统相关

```kotlin
// Domain Models
@Immutable
data class Story(
    val id: String,
    val title: String,
    val theme: String,
    val difficulty: Int,
    val requiredLevel: Int,
    val scenes: List<StoryScene>,
    val totalWords: Int,
    val imageAssets: List<String>?
)

@Immutable
data class StoryScene(
    val id: String,
    val storyId: String,
    val order: Int,
    val context: String,
    val dialogue: List<Dialogue>,
    val targetWords: List<ContextualWord>,
    val illustration: String?
)

@Immutable
data class Dialogue(
    val speaker: String,
    val text: String,
    val translation: String?
)

@Immutable
data class ContextualWord(
    val wordId: String,
    val blankPosition: Int,
    val hint: String?,
    val isTarget: Boolean
)

// Room Entities
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val theme: String,
    val difficulty: Int,
    val requiredLevel: Int,
    val scenesJson: String,  // JSON: List<StoryScene>
    val totalWords: Int,
    val imageAssetsJson: String,  // JSON: List<String>
)

@Entity(tableName = "story_progress")
data class StoryProgressEntity(
    @PrimaryKey val userId_stroyId: String,  // composite key
    val userId: String,
    val storyId: String,
    val currentScene: Int,
    val isCompleted: Boolean,
    val stars: Int,
    val startedAt: Long,
    val completedAt: Long?
)
```

### A.3 探索系统相关

```kotlin
// Domain Models
@Immutable
data class HiddenContent(
    val id: String,
    val type: HiddenContentType,
    val triggerCondition: TriggerCondition,
    val position: MapPosition?,
    val content: String,
    val rarity: DiscoveryRarity,
    val isOneTime: Boolean
)

enum class HiddenContentType {
    HIDDEN_WORD, SECRET_AREA, EASTER_EGG, BONUS_CHALLENGE
}

enum class DiscoveryRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

// Room Entities
@Entity(tableName = "exploration_progress")
data class ExplorationProgressEntity(
    @PrimaryKey val userId: String,
    val discoveredWordsJson: String,
    val discoveredSecretsJson: String,
    val discoveredEggsJson: String,
    val totalExplorations: Int,
    val uniqueDiscoveries: Int,
    val explorationStreak: Int,
    val triggeredEventsJson: String,
    val unlockedPathsJson: String,
    val lastExplorationTime: Long,
    val updatedAt: Long
)

@Entity(tableName = "hidden_content")
data class HiddenContentEntity(
    @PrimaryKey val id: String,
    val type: String,  // HiddenContentType
    val triggerConditionJson: String,
    val positionJson: String?,  // MapPosition
    val content: String,
    val rarity: String,  // DiscoveryRarity
    val isOneTime: Boolean
)
```

### A.4 收集系统相关

```kotlin
// Domain Models
@Immutable
data class CollectionProgress(
    val userId: String,
    val collections: Map<WordCategory, Set<String>>,
    val totalCollected: Int,
    val categoriesCompleted: Int,
    val rarestFind: String?
)

enum class WordCategory {
    ANIMALS, FOODS, ACTIONS, COLORS, CLOTHES,
    BODY, FAMILY, SCHOOL, NATURE, WEATHER
}

// Room Entities
@Entity(tableName = "collection_progress")
data class CollectionProgressEntity(
    @PrimaryKey val userId: String,
    val animalsJson: String,
    val foodsJson: String,
    val actionsJson: String,
    val colorsJson: String,
    val clothesJson: String,
    val bodyJson: String,
    val familyJson: String,
    val schoolJson: String,
    val natureJson: String,
    val weatherJson: String,
    val totalCollected: Int,
    val categoriesCompleted: Int,
    val rarestFind: String?,
    val updatedAt: Long
)
```

---

**文档状态**: ✅ 完成
**下一步**: 提交给团队讨论，根据反馈调整方案

**关键决策点**：
1. 是否接受渐进式实施策略？
2. 世界地图使用 Compose Canvas 是否同意？
3. 优先级排序是否合理？
4. 是否有其他技术风险需要评估？
