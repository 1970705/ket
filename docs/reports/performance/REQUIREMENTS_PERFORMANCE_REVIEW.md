# 需求文档性能影响评审报告

**文档版本**: 1.0
**创建日期**: 2026-02-20
**评审人**: android-performance-expert
**评审范围**: 游戏体验改进、成就系统、统计系统、社交功能、段位系统

---

## 执行摘要

本报告对 Wordland KET 应用的主要需求文档进行了全面的性能影响评估。评审涵盖了动画性能、内存占用、数据库查询性能、以及 UI 渲染性能等关键领域。

**总体评估**: ⚠️ **中等风险** - 需要性能优化措施

| 类别 | 风险等级 | 关键问题 |
|------|----------|----------|
| 动画性能 | 🟡 中 | 迷雾动画、成就弹窗、粒子效果可能影响 60fps |
| 内存占用 | 🟡 中 | 多个图片资源、成就数据缓存 |
| 数据库查询 | 🟢 低 | 索引设计良好，查询优化清晰 |
| UI 渲染 | 🟡 中 | 复杂的地图缩放/平移、Compose 重组 |
| 网络请求 | 🟢 低 | 大部分功能使用模拟数据，无实时网络 |

---

## 1. 游戏体验改进需求性能评估

### 1.1 世界地图 + 迷雾系统 (WORLD_MAP_FOG_UI_DESIGN.md)

**功能描述**:
- 可缩放/平移的世界地图
- 迷雾系统（未探索区域覆盖）
- 迷雾消散动画
- 小地图导航

**性能影响分析**:

| 组件 | 影响 | 严重性 | 优化建议 |
|------|------|--------|----------|
| **地图缩放/平移** | 🔴 高 | 使用 `graphicsLayer` 变换而非重绘 | 必须优化 |
| **迷雾渲染** | 🟡 中 | 预渲染迷雾纹理为 Bitmap | 强烈建议 |
| **迷雾消散动画** | 🟡 中 | 使用 `animateFloatAsState` 而非逐帧重绘 | 强烈建议 |
| **小地图更新** | 🟢 低 | 使用 `remember` 缓存计算结果 | 建议 |

**性能优化方案**:

```kotlin
// ❌ 问题：每帧重新绘制迷雾
@Composable
fun FogOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // 每帧重新绘制云朵纹理 - 性能差
        drawFogTexture()
    }
}

// ✅ 优化：预渲染为 Bitmap
@Composable
fun OptimizedFogOverlay() {
    val fogBitmap = remember {
        // 预渲染一次，复用 Bitmap
        createFogBitmap()
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawImage(fogBitmap) // 仅绘制，不重新计算
    }
}

// ✅ 优化：使用 graphicsLayer 实现缩放/平移
@Composable
fun ZoomableMap() {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableMutableStateOf(Offset.Zero) }

    MapContent(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            // 使用硬件加速变换
    )
}
```

**性能指标预估**:

| 操作 | 优化前 | 优化后 | 目标 |
|------|--------|--------|------|
| 地图缩放帧率 | 30-45 fps | 55-60 fps | ≥60 fps |
| 迷雾渲染耗时 | 15-25 ms | 2-5 ms | <16 ms |
| 内存增加 | +50 MB | +15 MB | <30 MB |

---

### 1.2 语境化学习系统 (CONTEXTUAL_LEARNING_UI_DESIGN.md)

**功能描述**:
- 句子/故事模式 UI
- 图片资源加载
- 音频播放

**性能影响分析**:

| 组件 | 影响 | 严重性 | 优化建议 |
|------|------|--------|----------|
| **场景图片加载** | 🟡 中 | 使用 Coil 异步加载 + 内存缓存 | 强烈建议 |
| **故事进度切换** | 🟢 低 | 预加载下一场景 | 建议 |
| **音频资源** | 🟢 低 | 预加载音频文件 | 建议 |

**优化方案**:

```kotlin
// ✅ 使用 Coil 加载和缓存图片
@Composable
fun ContextImageViewer(imageRes: ImageResource) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageRes.uri)
            .memoryCachePolicy(CachePolicy.ENABLED)  // 内存缓存
            .diskCachePolicy(CachePolicy.ENABLED)    // 磁盘缓存
            .build(),
        contentDescription = imageRes.description,
        modifier = Modifier.size(300.dp, 200.dp)
    )
}

// ✅ 预加载下一场景
@Composable
fun StoryModeViewer(story: StoryExercise) {
    val currentScene = story.scenes[currentSceneIndex]
    val nextScene = story.scenes.getOrNull(currentSceneIndex + 1)

    // 预加载下一场景图片
    LaunchedEffect(nextScene) {
        nextScene?.let { preloadImage(it.imageRes) }
    }
}
```

---

## 2. 成就系统性能评估

### 2.1 成就检测系统 (ACHIEVEMENT_SYSTEM_DESIGN.md)

**功能描述**:
- 15 个成就类别
- 实时成就检测
- 成就解锁通知
- 成就进度追踪

**性能影响分析**:

| 操作 | 影响 | 严重性 | 优化建议 |
|------|------|--------|----------|
| **每局游戏后检查** | 🟡 中 | 批量检查，避免多次数据库查询 | 强烈建议 |
| **成就解锁通知** | 🟢 低 | 使用队列，避免弹窗堆积 | 建议 |
| **进度缓存** | 🟡 中 | 使用 Flow 缓存全局统计数据 | 强烈建议 |

**性能优化方案**:

```kotlin
// ❌ 问题：每次游戏都检查所有成就
suspend fun checkAllAchievements(userId: String) {
    val allAchievements = achievementRepository.getAllAchievements()
    for (achievement in allAchievements) {
        checkSingleAchievement(achievement) // N 次数据库查询
    }
}

// ✅ 优化：批量查询 + 内存计算
class OptimizedAchievementChecker(
    private val achievementRepository: AchievementRepository,
    private val globalStatsCache: GlobalStatisticsCache
) {
    suspend operator fun invoke(event: GameEvent, userId: String): List<Achievement> {
        // 1. 单次查询获取用户所有成就进度
        val userProgress = achievementRepository.getAllUserProgress(userId)

        // 2. 单次查询获取全局统计（或使用缓存）
        val globalStats = globalStatsCache.get(userId)

        // 3. 内存中计算哪些成就解锁
        val newlyUnlocked = userProgress.filter { progress ->
            shouldUnlock(progress, event, globalStats)
        }

        // 4. 批量更新
        if (newlyUnlocked.isNotEmpty()) {
            achievementRepository.batchUpdateProgress(userId, newlyUnlocked)
        }

        return newlyUnlocked
    }
}
```

**数据库查询优化**:

```sql
-- ✅ 确保这些索引存在
CREATE INDEX idx_user_achievements_user_unlocked
ON user_achievements(user_id, is_unlocked);

CREATE INDEX idx_achievement_progress_user
ON achievement_progress(user_id);

-- ✅ 使用批量查询而非逐个查询
-- 一次查询获取所有进度：
SELECT * FROM achievement_progress WHERE user_id = ?
```

---

## 3. 统计系统性能评估

### 3.1 统计数据库设计 (STATISTICS_SYSTEM_DESIGN.md)

**功能描述**:
- 游戏历史记录表 (game_history)
- 关卡统计表 (level_statistics)
- 全局统计表 (global_statistics)

**性能影响分析**:

| 操作 | 数据量 | 影响 | 评估 |
|------|--------|------|------|
| **插入游戏记录** | 每次 | 🟢 低 | ✅ 索引设计合理 |
| **查询历史列表** | 分页 | 🟢 低 | ✅ 有分页限制 |
| **统计聚合** | 增量 | 🟡 中 | ⚠️ 需要增量更新 |

**数据库优化建议**:

```sql
-- ✅ 设计已经包含必要的索引
-- 以下是关键索引验证：

-- 1. 复合索引用于常用查询
CREATE INDEX idx_game_history_user_level_time
ON game_history(userId, levelId, startTime DESC);

-- 2. 建议：添加部分索引减少索引大小
CREATE INDEX idx_game_history_completed
ON game_history(userId, startTime DESC)
WHERE stars > 0;  -- 只索引已完成游戏

-- 3. 建议：统计表使用 TRIGGER 自动更新
CREATE TRIGGER update_level_stats_after_game
AFTER INSERT ON game_history
BEGIN
    -- 自动更新 level_statistics
    UPDATE level_statistics
    SET totalGames = totalGames + 1,
        lastPlayedAt = NEW.startTime
    WHERE userId = NEW.userId AND levelId = NEW.levelId;
END;
```

**统计查询优化**:

```kotlin
// ❌ 问题：每次都重新计算所有统计
suspend fun getLevelStatistics(userId: String, levelId: String): LevelStatistics {
    // 每次都扫描所有历史记录
    val history = gameHistoryRepository.getGameHistoryByLevel(userId, levelId)
    return calculateFromHistory(history) // 计算密集
}

// ✅ 优化：增量更新 + 缓存
class StatisticsCache(
    private val repository: StatisticsRepository
) {
    private val cache = ConcurrentHashMap<String, LevelStatistics>()

    suspend fun updateAfterGame(userId: String, levelId: String, game: GameHistory) {
        val current = cache["$userId-$levelId"]
            ?: repository.getLevelStatistics(userId, levelId)

        // 增量更新（而非重新计算）
        val updated = current.copy(
            totalGames = current.totalGames + 1,
            totalScore = current.totalScore + game.score,
            averageScore = (current.totalScore + game.score).toFloat() / (current.totalGames + 1),
            // ... 其他增量更新
        )

        cache["$userId-$levelId"] = updated
        repository.updateLevelStatistics(userId, levelId, updated)
    }
}
```

---

## 4. 社交功能性能评估

### 4.1 全球学习者计数 (SOCIAL_FEATURES_DESIGN.md)

**功能描述**:
- 显示全球学习者数量
- 进度百分比显示
- 匿名排行榜

**性能影响分析**:

| 组件 | 影响 | 严重性 | 优化建议 |
|------|------|--------|----------|
| **模拟数据生成** | 🟢 低 | 纯计算，无 I/O | 无需优化 |
| **百分比计算** | 🟢 低 | 简单数学运算 | 无需优化 |
| **排行榜生成** | 🟡 中 | 限制模拟数量（50-100） | 建议 |

**性能评估**:

```kotlin
// ✅ 当前设计已经优化良好
object SimulatedGlobalLearners {
    // 简单计算，性能开销可忽略
    fun getCurrentCount(daysSinceInstall: Int): Int {
        val growth = (baseCount * dailyGrowthRate * daysSinceInstall).toInt()
        val fluctuation = (Math.random() * volatility).toInt()
        return baseCount + growth + fluctuation
    }
}

// ✅ 排行榜生成需要限制数量
class AnonymousLeaderboardManager {
    // 建议限制为 50 个条目，而非无限增长
    private val MAX_SIMULATED_LEARNERS = 50

    fun getTodayLeaderboard(userWordsLearned: Int): LeaderboardResult {
        val learnerCount = minOf(MAX_SIMULATED_LEARNERS, /* ... */)
        // ...
    }
}
```

---

## 5. 段位系统性能评估

### 5.1 段位计算系统 (RANK_SYSTEM_DESIGN.md)

**功能描述**:
- 7 个大段位，每个 3 个小段位
- 积分计算规则
- 赛季重置机制

**性能影响分析**:

| 操作 | 影响 | 严重性 | 优化建议 |
|------|------|--------|----------|
| **段位计算** | 🟢 低 | 简单 if-else 查表 | 无需优化 |
| **积分更新** | 🟢 低 | 单次 UPDATE 查询 | 无需优化 |
| **赛季重置** | 🟡 中 | 后台任务，避免阻塞 | 建议 |

**优化建议**:

```kotlin
// ✅ 段位计算已经是 O(1) 复杂度
class GetCurrentRankUseCase {
    fun execute(score: Int): RankInfo {
        val tier = when {
            score >= 5500 -> RankTier.CHALLENGER
            score >= 4800 -> RankTier.MASTER
            // ... 简单查表
        }
        // ...
    }
}

// ✅ 赛季重置使用后台任务
class SeasonResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 在后台执行，不阻塞 UI
        rankRepository.resetSeasonForAllUsers()
        return Result.success()
    }
}

// 调度：每天凌晨检查
val seasonResetRequest = PeriodicWorkRequestBuilder<SeasonResetWorker>(
    1, TimeUnit.DAYS
).build()
```

---

## 6. Compose 重组优化建议

### 6.1 防止不必要的重组

多个需求文档中的 UI 组件需要注意 Compose 重组优化：

```kotlin
// ❌ 问题：导致整个列表重组
@Composable
fun AchievementList(achievements: List<Achievement>) {
    LazyColumn {
        items(achievements) { achievement ->
            AchievementCard(
                achievement = achievement,
                onUnlock = { /* ... */ }
            )
        }
    }
}

// ✅ 优化：使用稳定 key
@Composable
fun OptimizedAchievementList(achievements: List<Achievement>) {
    LazyColumn {
        items(
            items = achievements,
            key = { it.id }  // 稳定 key 防止重组
        ) { achievement ->
            AchievementCard(
                achievement = achievement,
                onUnlock = { /* ... */ }
            )
        }
    }
}

// ✅ 确保数据类是稳定的
@Immutable  // 添加此注解
data class Achievement(
    val id: String,
    val name: String,
    // ...
)
```

---

## 7. 内存优化建议

### 7.1 图片资源管理

```kotlin
// ✅ 使用 Coil 的内存缓存策略
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25)  // 限制为应用内存的 25%
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache"))
            .maxSizeBytes(50 * 1024 * 1024)  // 50 MB
            .build()
    }
    .build()
```

### 7.2 成就数据缓存

```kotlin
// ✅ 使用 LRU 缓存
class AchievementCache(size: Int = 20) : LruCache<String, Achievement>(size) {

    fun get(userId: String, achievementId: String): Achievement? {
        val key = "$userId:$achievementId"
        return get(key)
    }

    fun put(userId: String, achievementId: String, achievement: Achievement) {
        val key = "$userId:$achievementId"
        put(key, achievement)
    }
}
```

---

## 8. 启动性能影响

### 8.1 预计启动时间增加

| 新增功能 | 预计增加时间 | 优化后增加时间 |
|----------|-------------|---------------|
| 数据库迁移（版本 4→5） | +500 ms | +200 ms (异步) |
| 成就数据初始化 | +100 ms | +50 ms (延迟加载) |
| 统计数据加载 | +150 ms | +0 ms (按需加载) |
| **总计** | **+750 ms** | **+250 ms** |

**建议**: 使用 StartupInitializer 延迟非关键初始化

```kotlin
// ✅ 延迟初始化非关键组件
class WordlandInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // 不阻塞启动
        CoroutineScope(Dispatchers.IO).launch {
            achievementRepository.initialize()
            statisticsRepository.warmCache()
        }
    }
}
```

---

## 9. 性能监控建议

### 9.1 关键指标监控

建议为以下功能添加性能监控：

```kotlin
// ✅ 使用 Jetpack Macrobenchmark
@RunWith(AndroidJUnit4::class)
class MapPerformanceBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun mapZoomPerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric())
        ) {
            // 启动并测试地图缩放
            pressHome()
            startActivityAndWait()
            // ... 测试缩放操作
        }
    }
}
```

### 9.2 自定义性能追踪

```kotlin
// ✅ 添加性能追踪
class PerformanceTracker {
    fun trackAchievementCheckTime(block: suspend () -> Unit) {
        val startTime = System.nanoTime()
        runBlocking {
            block()
        }
        val duration = (System.nanoTime() - startTime) / 1_000_000 // ms
        if (duration > 100) {
            Log.w("Performance", "Achievement check took ${duration}ms")
        }
    }
}
```

---

## 10. 总结与建议

### 10.1 高优先级优化（必须）

1. **地图缩放/平移优化**
   - 使用 `graphicsLayer` 而非重绘
   - 预渲染迷雾纹理

2. **成就检测优化**
   - 批量数据库查询
   - 缓存全局统计数据

3. **数据库索引**
   - 验证所有关键索引存在
   - 添加部分索引减少大小

### 10.2 中优先级优化（建议）

1. **图片资源管理**
   - 使用 Coil 缓存
   - 预加载下一场景

2. **统计数据增量更新**
   - 使用 TRIGGER 或增量计算
   - 避免全表扫描

3. **Compose 重组优化**
   - 使用 `@Immutable` 注解
   - 为 LazyColumn items 添加 key

### 10.3 低优先级优化（可选）

1. **赛季重置后台化**
2. **成就解锁队列管理**
3. **LRU 缓存实现**

---

## 附录 A: 性能测试计划

### A.1 基准测试

| 测试项 | 目标 | 测试方法 |
|--------|------|----------|
| 应用冷启动 | <3000 ms | `am start -W` |
| 地图缩放帧率 | ≥55 fps | Macrobenchmark |
| 成就检测耗时 | <100 ms | 单元测试计时 |
| 统计查询耗时 | <50 ms | 数据库 EXPLAIN |

### A.2 压力测试

- 模拟 1000+ 游戏历史记录
- 测试 15 个成就同时解锁
- 测试排行榜 100+ 条目

---

**文档版本**: 1.0
**评审状态**: ✅ 完成
**下一步**: 与团队分享优化建议
