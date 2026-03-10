# 完整游戏统计系统设计文档

**项目**: Wordland
**版本**: 1.0
**日期**: 2026-02-20
**作者**: Team Lead
**状态**: 设计阶段

---

## 1. 概述

### 1.1 目标

设计并实现一个完整的游戏统计系统，提供：

1. **游戏历史记录** - 记录每局游戏的详细数据
2. **关卡统计** - 每关的详细统计和趋势
3. **全局统计** - 整体学习进度和成就
4. **成就系统** - 游戏化成就和奖励

### 1.2 用户价值

- ✅ 查看学习进度和提升
- ✅ 追踪历史表现
- ✅ 设置和达成目标
- ✅ 获得成就奖励
- ✅ 增强游戏粘性

---

## 2. 数据模型设计

### 2.1 GameHistory（游戏历史）

**用途**: 记录每局游戏的详细数据

```kotlin
@Entity(
    tableName = "game_history",
    indices = [
        Index(value = ["userId"], name = "index_game_history_userId"),
        Index(value = ["levelId"], name = "index_game_history_levelId"),
        Index(value = ["gameMode"], name = "index_game_history_gameMode"),
        Index(value = ["startTime"], name = "index_game_history_startTime"),
        Index(value = ["userId", "levelId", "startTime"], name = "index_game_history_user_level_time")
    ]
)
data class GameHistory(
    @PrimaryKey
    val gameId: String,  // UUID

    // 基本信息
    val userId: String,
    val levelId: String,  // "look_island_level_01"
    val islandId: String, // "look_island"
    val gameMode: GameMode, // SPELL_BATTLE, QUICK_JUDGE

    // 时间数据
    val startTime: Long,  // 开始时间戳（毫秒）
    val endTime: Long,    // 结束时间戳（毫秒）
    val duration: Long,   // 游戏时长（毫秒）

    // 游戏数据
    val score: Int,       // 总得分
    val stars: Int,       // 星级评分（0-3）
    val totalQuestions: Int,   // 总题数
    val correctAnswers: Int,   // 正确答案数
    val accuracy: Float,       // 准确率（0.0-1.0）

    // 详细统计
    val maxCombo: Int,    // 最大连击
    val hintsUsed: Int,   // 使用提示次数
    val wrongAnswers: Int,// 错误答案数

    // 性能数据
    val avgResponseTime: Long,  // 平均响应时间（毫秒）
    val fastestAnswer: Long?,   // 最快答题时间（毫秒）
    val slowestAnswer: Long?,   // 最慢答题时间（毫秒）

    // 难度设置
    val difficulty: String, // "easy", "normal", "hard"

    // 元数据
    val createdAt: Long = System.currentTimeMillis()
)

enum class GameMode {
    SPELL_BATTLE,
    QUICK_JUDGE,
    LISTEN_FIND,
    SENTENCE_MATCH
}
```

**字段说明**:
- `gameId`: 唯一标识符，使用 UUID
- `gameMode`: 游戏模式枚举
- `duration`: 游戏持续时长，用于分析学习效率
- `avgResponseTime`: 平均答题时间，用于分析反应速度
- `maxCombo`: 最大连击，用于评估连续表现

### 2.2 LevelStatistics（关卡统计）

**用途**: 聚合的关卡级别统计数据

```kotlin
@Entity(
    tableName = "level_statistics",
    primaryKeys = ["userId", "levelId"]
)
data class LevelStatistics(
    val userId: String,
    val levelId: String,

    // 游戏次数
    val totalGames: Int = 0,
    val completedGames: Int = 0,
    val perfectGames: Int = 0,  // 3星游戏次数

    // 得分统计
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val averageScore: Float = 0f,
    val totalScore: Int = 0,  // 累计得分

    // 时间统计
    val bestTime: Long? = null,  // 最佳完成时间（毫秒）
    val worstTime: Long? = null, // 最慢完成时间（毫秒）
    val averageTime: Long = 0,   // 平均完成时间（毫秒）
    val totalTime: Long = 0,     // 累计游戏时间（毫秒）

    // 表现统计
    val totalCorrect: Int = 0,   // 总正确数
    val totalQuestions: Int = 0, // 总题数
    val overallAccuracy: Float = 0f,  // 总体准确率

    // 连击记录
    val bestCombo: Int = 0,     // 历史最高连击

    // 首次和最近游戏
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,

    // 元数据
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
```

### 2.3 GlobalStatistics（全局统计）

**用途**: 全局学习进度统计

```kotlin
@Entity(
    tableName = "global_statistics",
    primaryKeys = ["userId"]
)
data class GlobalStatistics(
    val userId: String,

    // 游戏统计
    val totalGames: Int = 0,
    val totalScore: Int = 0,
    val totalPerfectGames: Int = 0,  // 3星游戏总数

    // 时间统计
    val totalStudyTime: Long = 0,  // 总学习时长（毫秒）
    val currentStreak: Int = 0,     // 当前连续学习天数
    val longestStreak: Int = 0,     // 最长连续学习天数
    val lastStudyDate: Long? = null, // 最后学习日期

    // 进度统计
    val totalLevelsCompleted: Int = 0,  // 完成的关卡数
    val totalLevelsPerfected: Int = 0,  // 完美通关的关卡数
    val totalWordsMastered: Int = 0,    // 掌握的单词数

    // 学习曲线
    val totalCorrectAnswers: Int = 0,
    val totalPracticeSessions: Int = 0,  // 总练习次数

    // 元数据
    val firstUsedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
```

### 2.4 AchievementProgress（成就进度）

**用途**: 追踪成就解锁进度

```kotlin
@Entity(
    tableName = "achievement_progress",
    primaryKeys = ["userId", "achievementId"]
)
data class AchievementProgress(
    val userId: String,
    val achievementId: String,

    val isUnlocked: Boolean = false,
    val progress: Int = 0,       // 当前进度
    val target: Int = 1,         // 目标值
    val unlockedAt: Long? = null,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
```

---

## 3. 数据库设计

### 3.1 Schema 版本

**当前版本**: 4
**目标版本**: 5

### 3.2 表结构

#### game_history 表

```sql
CREATE TABLE game_history (
    gameId TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    levelId TEXT NOT NULL,
    islandId TEXT NOT NULL,
    gameMode TEXT NOT NULL,
    startTime INTEGER NOT NULL,
    endTime INTEGER NOT NULL,
    duration INTEGER NOT NULL,
    score INTEGER NOT NULL,
    stars INTEGER NOT NULL,
    totalQuestions INTEGER NOT NULL,
    correctAnswers INTEGER NOT NULL,
    accuracy REAL NOT NULL,
    maxCombo INTEGER NOT NULL,
    hintsUsed INTEGER NOT NULL DEFAULT 0,
    wrongAnswers INTEGER NOT NULL DEFAULT 0,
    avgResponseTime INTEGER NOT NULL,
    fastestAnswer INTEGER,
    slowestAnswer INTEGER,
    difficulty TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE INDEX index_game_history_userId ON game_history(userId);
CREATE INDEX index_game_history_levelId ON game_history(levelId);
CREATE INDEX index_game_history_gameMode ON game_history(gameMode);
CREATE INDEX index_game_history_startTime ON game_history(startTime DESC);
CREATE INDEX index_game_history_user_level_time ON game_history(userId, levelId, startTime DESC);
```

#### level_statistics 表

```sql
CREATE TABLE level_statistics (
    userId TEXT NOT NULL,
    levelId TEXT NOT NULL,
    totalGames INTEGER NOT NULL DEFAULT 0,
    completedGames INTEGER NOT NULL DEFAULT 0,
    perfectGames INTEGER NOT NULL DEFAULT 0,
    highestScore INTEGER NOT NULL DEFAULT 0,
    lowestScore INTEGER NOT NULL DEFAULT 0,
    averageScore REAL NOT NULL DEFAULT 0,
    totalScore INTEGER NOT NULL DEFAULT 0,
    bestTime INTEGER,
    worstTime INTEGER,
    averageTime INTEGER NOT NULL DEFAULT 0,
    totalTime INTEGER NOT NULL DEFAULT 0,
    totalCorrect INTEGER NOT NULL DEFAULT 0,
    totalQuestions INTEGER NOT NULL DEFAULT 0,
    overallAccuracy REAL NOT NULL DEFAULT 0,
    bestCombo INTEGER NOT NULL DEFAULT 0,
    firstPlayedAt INTEGER,
    lastPlayedAt INTEGER,
    lastUpdatedAt INTEGER NOT NULL,
    PRIMARY KEY(userId, levelId)
);
```

#### global_statistics 表

```sql
CREATE TABLE global_statistics (
    userId TEXT PRIMARY KEY NOT NULL,
    totalGames INTEGER NOT NULL DEFAULT 0,
    totalScore INTEGER NOT NULL DEFAULT 0,
    totalPerfectGames INTEGER NOT NULL DEFAULT 0,
    totalStudyTime INTEGER NOT NULL DEFAULT 0,
    currentStreak INTEGER NOT NULL DEFAULT 0,
    longestStreak INTEGER NOT NULL DEFAULT 0,
    lastStudyDate INTEGER,
    totalLevelsCompleted INTEGER NOT NULL DEFAULT 0,
    totalLevelsPerfected INTEGER NOT NULL DEFAULT 0,
    totalWordsMastered INTEGER NOT NULL DEFAULT 0,
    totalCorrectAnswers INTEGER NOT NULL DEFAULT 0,
    totalPracticeSessions INTEGER NOT NULL DEFAULT 0,
    firstUsedAt INTEGER NOT NULL,
    lastUpdatedAt INTEGER NOT NULL DEFAULT 0
);
```

#### achievement_progress 表

```sql
CREATE TABLE achievement_progress (
    userId TEXT NOT NULL,
    achievementId TEXT NOT NULL,
    isUnlocked INTEGER NOT NULL DEFAULT 0,
    progress INTEGER NOT NULL DEFAULT 0,
    target INTEGER NOT NULL DEFAULT 1,
    unlockedAt INTEGER,
    lastUpdatedAt INTEGER NOT NULL,
    PRIMARY KEY(userId, achievementId)
);
```

### 3.3 迁移策略

**Migration 4 → 5**:

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 game_history 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS game_history (
                gameId TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                levelId TEXT NOT NULL,
                islandId TEXT NOT NULL,
                gameMode TEXT NOT NULL,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                score INTEGER NOT NULL,
                stars INTEGER NOT NULL,
                totalQuestions INTEGER NOT NULL,
                correctAnswers INTEGER NOT NULL,
                accuracy REAL NOT NULL,
                maxCombo INTEGER NOT NULL,
                hintsUsed INTEGER NOT NULL DEFAULT 0,
                wrongAnswers INTEGER NOT NULL DEFAULT 0,
                avgResponseTime INTEGER NOT NULL,
                fastestAnswer INTEGER,
                slowestAnswer INTEGER,
                difficulty TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """)

        // 创建 level_statistics 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS level_statistics (
                userId TEXT NOT NULL,
                levelId TEXT NOT NULL,
                totalGames INTEGER NOT NULL DEFAULT 0,
                completedGames INTEGER NOT NULL DEFAULT 0,
                perfectGames INTEGER NOT NULL DEFAULT 0,
                highestScore INTEGER NOT NULL DEFAULT 0,
                lowestScore INTEGER NOT NULL DEFAULT 0,
                averageScore REAL NOT NULL DEFAULT 0,
                totalScore INTEGER NOT NULL DEFAULT 0,
                bestTime INTEGER,
                worstTime INTEGER,
                averageTime INTEGER NOT NULL DEFAULT 0,
                totalTime INTEGER NOT NULL DEFAULT 0,
                totalCorrect INTEGER NOT NULL DEFAULT 0,
                totalQuestions INTEGER NOT NULL DEFAULT 0,
                overallAccuracy REAL NOT NULL DEFAULT 0,
                bestCombo INTEGER NOT NULL DEFAULT 0,
                firstPlayedAt INTEGER,
                lastPlayedAt INTEGER,
                lastUpdatedAt INTEGER NOT NULL,
                PRIMARY KEY(userId, levelId)
            )
        """)

        // 创建 global_statistics 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS global_statistics (
                userId TEXT PRIMARY KEY NOT NULL,
                totalGames INTEGER NOT NULL DEFAULT 0,
                totalScore INTEGER NOT NULL DEFAULT 0,
                totalPerfectGames INTEGER NOT NULL DEFAULT 0,
                totalStudyTime INTEGER NOT NULL DEFAULT 0,
                currentStreak INTEGER NOT NULL DEFAULT 0,
                longestStreak INTEGER NOT NULL DEFAULT 0,
                lastStudyDate INTEGER,
                totalLevelsCompleted INTEGER NOT NULL DEFAULT 0,
                totalLevelsPerfected INTEGER NOT NULL DEFAULT 0,
                totalWordsMastered INTEGER NOT NULL DEFAULT 0,
                totalCorrectAnswers INTEGER NOT NULL DEFAULT 0,
                totalPracticeSessions INTEGER NOT NULL DEFAULT 0,
                firstUsedAt INTEGER NOT NULL,
                lastUpdatedAt INTEGER NOT NULL DEFAULT 0
            )
        """)

        // 创建索引
        createIndexes(database)
    }

    private fun createIndexes(database: SupportSQLiteDatabase) {
        // game_history 索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_userId ON game_history(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_levelId ON game_history(levelId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_gameMode ON game_history(gameMode)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_startTime ON game_history(startTime DESC)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_user_level_time ON game_history(userId, levelId, startTime DESC)")
    }
}
```

---

## 4. API 设计

### 4.1 Repository 层

#### GameHistoryRepository

```kotlin
interface GameHistoryRepository {
    suspend fun insertGameHistory(history: GameHistory)
    suspend fun getGameHistory(
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<GameHistory>

    suspend fun getGameHistoryByLevel(
        userId: String,
        levelId: String,
        limit: Int = 20
    ): List<GameHistory>

    suspend fun getGameHistoryByMode(
        userId: String,
        gameMode: GameMode,
        limit: Int = 20
    ): List<GameHistory>

    suspend fun getRecentGames(
        userId: String,
        days: Int = 7
    ): List<GameHistory>
}
```

#### StatisticsRepository

```kotlin
interface StatisticsRepository {
    // 全局统计
    suspend fun getGlobalStatistics(userId: String): GlobalStatistics
    suspend fun updateGlobalStatistics(userId: String)

    // 关卡统计
    suspend fun getLevelStatistics(
        userId: String,
        levelId: String
    ): LevelStatistics

    suspend fun getAllLevelStatistics(
        userId: String,
        islandId: String
    ): List<LevelStatistics>

    suspend fun updateLevelStatistics(
        userId: String,
        levelId: String,
        gameHistory: GameHistory
    )
}
```

### 4.2 UseCase 层

#### GetGameHistoryUseCase

```kotlin
class GetGameHistoryUseCase(
    private val repository: GameHistoryRepository
) {
    operator fun invoke(
        userId: String,
        filter: HistoryFilter = HistoryFilter.All
    ): Flow<PaginatedData<GameHistory>>
}

data class HistoryFilter(
    val levelId: String? = null,
    val gameMode: GameMode? = null,
    val timeRange: TimeRange = TimeRange.All
)

sealed class TimeRange {
    object All : TimeRange()
    data class LastDays(val days: Int) : TimeRange()
    data class DateRange(val start: Long, val end: Long) : TimeRange()
}
```

#### GetGlobalStatisticsUseCase

```kotlin
class GetGlobalStatisticsUseCase(
    private val repository: StatisticsRepository
) {
    operator fun invoke(userId: String): Flow<GlobalStatistics>
}
```

#### RecordGameHistoryUseCase

```kotlin
class RecordGameHistoryUseCase(
    private val gameHistoryRepository: GameHistoryRepository,
    private val statisticsRepository: StatisticsRepository
) {
    suspend operator fun invoke(
        gameData: GameData,
        userId: String
    ): Result<GameHistory> {
        // 1. 创建 GameHistory 记录
        // 2. 更新 LevelStatistics
        // 3. 更新 GlobalStatistics
        // 4. 检查成就解锁
    }
}

data class GameData(
    val levelId: String,
    val islandId: String,
    val gameMode: GameMode,
    val score: Int,
    val stars: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val maxCombo: Int,
    val hintsUsed: Int,
    val duration: Long,
    val difficulty: String
)
```

---

## 5. UI 设计

### 5.1 StatisticsScreen（主统计页面）

```kotlin
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部统计卡片
        GlobalStatsCard(
            totalGames = uiState.globalStats.totalGames,
            totalScore = uiState.globalStats.totalScore,
            totalStudyTime = uiState.globalStats.totalStudyTime,
            currentStreak = uiState.globalStats.currentStreak
        )

        // Tab 选择
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = false, onClick = { selectTab(0) }) {
                Text("历史")
            }
            Tab(selected = false, onClick = { selectTab(1) }) {
                Text("关卡")
            }
            Tab(selected = false, onClick = { selectTab(2) }) {
                Text("成就")
            }
        }

        // Tab 内容
        when (selectedTab) {
            0 -> GameHistoryScreen()
            1 -> LevelStatisticsScreen()
            2 -> AchievementScreen()
        }
    }
}
```

### 5.2 GameHistoryScreen（历史记录）

```kotlin
@Composable
fun GameHistoryScreen(
    viewModel: GameHistoryViewModel
) {
    val history by viewModel.gameHistory.collectAsState()

    LazyColumn {
        // 筛选器
        item {
            HistoryFilterBar(
                onFilterChanged = { viewModel.updateFilter(it) }
            )
        }

        // 历史记录列表
        items(history) { game ->
            GameHistoryCard(
                game = game,
                onClick = { /* 显示详情 */ }
            )
        }
    }
}

@Composable
fun GameHistoryCard(
    game: GameHistory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 游戏模式图标
            GameModeIcon(
                mode = game.gameMode,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 游戏信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLevelDisplayName(game.levelId),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${game.gameMode.name} | ${getAccuracyText(game.accuracy)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 得分和星级
            Column(horizontalAlignment = Alignment.End) {
                StarsDisplay(stars = game.stars)
                Text(
                    text = "${game.score}分",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
```

### 5.3 LevelStatisticsScreen（关卡统计）

```kotlin
@Composable
fun LevelStatisticsScreen(
    viewModel: LevelStatisticsViewModel
) {
    val levels by viewModel.levelStatistics.collectAsState()

    LazyColumn {
        // Island 选择
        item {
            IslandSelector(
                selectedIsland = viewModel.selectedIsland,
                onIslandSelected = { viewModel.selectIsland(it) }
            )
        }

        // 关卡统计卡片
        items(levels) { levelStats ->
            LevelStatisticsCard(
                stats = levelStats,
                onClick = { /* 显示详细图表 */ }
            )
        }
    }
}

@Composable
fun LevelStatisticsCard(
    stats: LevelStatistics
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = getLevelDisplayName(stats.levelId),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 统计网格
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("最高分", "${stats.highestScore}")
                StatItem("平均分", "%.0f".format(stats.averageScore))
                StatItem("完美次数", "${stats.perfectGames}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { stats.completedGames.toFloat() / stats.totalGames },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "完成: ${stats.completedGames}/${stats.totalGames}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

## 6. 实施计划

### 阶段 1: 数据模型和存储（1-2天）

**任务**:
1. 创建 GameHistory 实体
2. 创建 LevelStatistics 实体
3. 创建 GlobalStatistics 实体
4. 创建 GameHistoryDao
5. 创建 StatisticsDao
6. 编写 Migration 4→5
7. 编写单元测试

**验收**:
- ✅ 所有实体和 DAO 创建完成
- ✅ 数据库迁移成功
- ✅ 单元测试通过

### 阶段 2: Repository 和 UseCase（2-3天）

**任务**:
1. 实现 GameHistoryRepository
2. 实现 StatisticsRepository
3. 实现 GetGameHistoryUseCase
4. 实现 GetGlobalStatisticsUseCase
5. 实现 GetLevelStatisticsUseCase
6. 实现 RecordGameHistoryUseCase
7. 集成到 QuickJudgeViewModel
8. 编写单元测试

**验收**:
- ✅ 所有 Repository 和 UseCase 实现
- ✅ 游戏完成后自动保存历史
- ✅ 统计数据正确计算

### 阶段 3: UI 实现（3-4天）

**任务**:
1. 实现 StatisticsScreen
2. 实现 GameHistoryScreen
3. 实现 LevelStatisticsScreen
4. 实现统计卡片组件
5. 实现筛选和排序
6. 集成导航
7. UI 测试

**验收**:
- ✅ 所有 UI 页面实现
- ✅ 数据正确显示
- ✅ 交互流畅

### 阶段 4: 成就系统（2-3天）

**任务**:
1. 完善成就数据（50个成就）
2. 实现成就检测逻辑
3. 实现 AchievementScreen
4. 实现成就进度追踪
5. 集成测试

**验收**:
- ✅ 成就系统完整
- ✅ 成就正确解锁
- ✅ UI 显示正确

### 阶段 5: 图表和可视化（2-3天）

**任务**:
1. 集成 Vico 图表库
2. 实现得分趋势图
3. 实现学习时长图
4. 实现准确率趋势图
5. 性能优化

**验收**:
- ✅ 图表正确显示
- ✅ 动画流畅
- ✅ 查询性能 < 100ms

---

## 7. 性能优化

### 7.1 数据库优化

1. **索引优化**
   - 为常用查询字段添加索引
   - 复合索引优化

2. **查询优化**
   - 分页加载（每页 20 条）
   - 延迟加载
   - 使用 COUNT 查询而非全量查询

3. **缓存策略**
   - 全局统计数据缓存（Live Data）
   - 关卡统计数据缓存
   - 定期刷新策略

### 7.2 UI 优化

1. **懒加载**
   - LazyColumn 分页
   - 图片懒加载

2. **状态管理**
   - Flow 数据流
   - 避免过度重组

3. **动画优化**
   - 使用 Compose 动画
   - 避免过度绘制

---

## 8. 测试策略

### 8.1 单元测试

- DAO 测试（数据库操作）
- Repository 测试（业务逻辑）
- UseCase 测试（集成测试）
- ViewModel 测试（状态管理）

### 8.2 集成测试

- 游戏完成 → 历史保存流程
- 统计数据更新流程
- 成就解锁流程

### 8.3 UI 测试

- Compose UI 测试
- 导航测试
- 交互测试

### 8.4 性能测试

- 查询性能测试
- UI 渲染性能测试
- 内存泄漏测试

---

## 9. 发布计划

### 9.1 MVP 版本（P0 功能）

**包含**:
- 游戏历史记录
- 基础统计数据
- 简单的 UI 显示

**时间**: 1-2 周

### 9.2 完整版本（P0 + P1 功能）

**包含**:
- 完整的统计系统
- 关卡详细统计
- 成就系统

**时间**: 2-3 周

### 9.3 增强版本（P0 + P1 + P2）

**包含**:
- 图表可视化
- 数据导出
- 社交分享

**时间**: 3-4 周

---

## 10. 附录

### 10.1 术语表

| 术语 | 定义 |
|------|------|
| GameHistory | 单局游戏记录 |
| LevelStatistics | 关卡统计 |
| GlobalStatistics | 全局统计 |
| Achievement | 成就 |
| Streak | 连续学习天数 |
| Perfect Game | 3星完美通关 |

### 10.2 参考资料

- Room Database: https://developer.android.com/training/data-storage/room
- Vico Charts: https://patrykandroid.github.io/Vico/
- Compose UI: https://developer.android.com/jetpack/compose

---

**文档状态**: ✅ 完成
**下一步**: 等待评审和确认
