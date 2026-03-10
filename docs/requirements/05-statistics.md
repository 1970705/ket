# 统计系统需求文档

**文档版本**: 1.0
**创建日期**: 2026-02-20
**作者**: android-architect
**状态**: 需求定义
**优先级**: P1

---

## 1. 概述

### 1.1 目标

设计并实现一个完整的游戏统计系统，提供：

1. **游戏历史记录** - 记录每局游戏的详细数据
2. **关卡统计** - 每关的详细统计和趋势
3. **全局统计** - 整体学习进度和成就
4. **性能追踪** - 学习效率和表现分析

### 1.2 用户价值

- ✅ 查看学习进度和提升
- ✅ 追踪历史表现
- ✅ 设置和达成目标
- ✅ 分析学习效率
- ✅ 增强游戏粘性

### 1.3 与其他系统的关系

```
统计系统
    ├── 依赖: 学习系统 (Spell Battle, Quick Judge)
    ├── 依赖: 进度系统 (LevelProgress, UserWordProgress)
    ├── 驱动: 成就系统 (AchievementDetector)
    └── 驱动: 防沉迷系统 (SessionManager)
```

---

## 2. 数据模型

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
    val gameId: String,              // UUID 唯一标识

    // 基本信息
    val userId: String,
    val levelId: String,             // "look_island_level_01"
    val islandId: String,            // "look_island"
    val gameMode: GameMode,          // SPELL_BATTLE, QUICK_JUDGE

    // 时间数据
    val startTime: Long,             // 开始时间戳（毫秒）
    val endTime: Long,               // 结束时间戳（毫秒）
    val duration: Long,              // 游戏时长（毫秒）

    // 游戏数据
    val score: Int,                  // 总得分
    val stars: Int,                  // 星级评分（0-3）
    val totalQuestions: Int,         // 总题数
    val correctAnswers: Int,         // 正确答案数
    val accuracy: Float,             // 准确率（0.0-1.0）

    // 详细统计
    val maxCombo: Int,               // 最大连击
    val hintsUsed: Int,              // 使用提示次数
    val wrongAnswers: Int,           // 错误答案数

    // 性能数据
    val avgResponseTime: Long,       // 平均响应时间（毫秒）
    val fastestAnswer: Long?,        // 最快答题时间（毫秒）
    val slowestAnswer: Long?,        // 最慢答题时间（毫秒）

    // 难度设置
    val difficulty: String,          // "easy", "normal", "hard"

    // 元数据
    val createdAt: Long = System.currentTimeMillis()
)

enum class GameMode {
    SPELL_BATTLE,    // 拼写战斗
    QUICK_JUDGE,     // 快速判断
    LISTEN_FIND,     // 听音寻宝（未来）
    SENTENCE_MATCH   // 句子配对（未来）
}
```

**字段说明**:
- `gameId`: UUID 生成唯一标识符
- `accuracy`: 计算 `correctAnswers / totalQuestions`
- `duration`: `endTime - startTime`，用于分析学习效率
- `avgResponseTime`: 所有答题时间的平均值，排除 < 1s 的猜测

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
    val perfectGames: Int = 0,        // 3星游戏次数

    // 得分统计
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val averageScore: Float = 0f,
    val totalScore: Int = 0,          // 累计得分

    // 时间统计
    val bestTime: Long? = null,       // 最佳完成时间（毫秒）
    val worstTime: Long? = null,      // 最慢完成时间（毫秒）
    val averageTime: Long = 0,        // 平均完成时间（毫秒）
    val totalTime: Long = 0,          // 累计游戏时间（毫秒）

    // 表现统计
    val totalCorrect: Int = 0,        // 总正确数
    val totalQuestions: Int = 0,      // 总题数
    val overallAccuracy: Float = 0f,  // 总体准确率

    // 连击记录
    val bestCombo: Int = 0,           // 历史最高连击

    // 首次和最近游戏
    val firstPlayedAt: Long? = null,
    val lastPlayedAt: Long? = null,

    // 元数据
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
```

**聚合规则**:
- `averageScore`: 实时计算 `totalScore / totalGames`
- `overallAccuracy`: 实时计算 `totalCorrect / totalQuestions`
- `averageTime`: 实时计算 `totalTime / totalGames`

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
    val totalPerfectGames: Int = 0,   // 3星游戏总数

    // 时间统计
    val totalStudyTime: Long = 0,     // 总学习时长（毫秒）
    val currentStreak: Int = 0,        // 当前连续学习天数
    val longestStreak: Int = 0,        // 最长连续学习天数
    val lastStudyDate: Long? = null,   // 最后学习日期

    // 进度统计
    val totalLevelsCompleted: Int = 0, // 完成的关卡数
    val totalLevelsPerfected: Int = 0, // 完美通关的关卡数
    val totalWordsMastered: Int = 0,   // 掌握的单词数

    // 学习曲线
    val totalCorrectAnswers: Int = 0,
    val totalPracticeSessions: Int = 0, // 总练习次数

    // 元数据
    val firstUsedAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
```

**连续学习规则**:
- 连续学习: `lastStudyDate` 为昨天或今天
- 连续中断: `lastStudyDate` 早于昨天
- 每日首次学习触发连续天数更新

---

## 3. 数据库设计

### 3.1 表结构

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

### 3.2 迁移策略

**当前版本**: 4 → **目标版本**: 5

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
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_userId ON game_history(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_levelId ON game_history(levelId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_gameMode ON game_history(gameMode)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_startTime ON game_history(startTime DESC)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_game_history_user_level_time ON game_history(userId, levelId, startTime DESC)")
    }
}
```

### 3.3 数据清理策略

为防止 game_history 表无限增长：

```kotlin
// 数据归档策略
val HISTORY_RETENTION_DAYS = 90  // 保留90天

// 每次启动时清理旧数据
suspend fun cleanOldHistory(userId: String) {
    val cutoffTime = System.currentTimeMillis() - (DAYS.toMillis(HISTORY_RETENTION_DAYS))
    gameHistoryDao.deleteOldHistory(userId, cutoffTime)
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
    suspend fun updateGlobalStatistics(userId: String, gameData: GameData)

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
        gameData: GameData
    )
}
```

### 4.2 UseCase 层

#### RecordGameHistoryUseCase

```kotlin
class RecordGameHistoryUseCase(
    private val gameHistoryRepository: GameHistoryRepository,
    private val statisticsRepository: StatisticsRepository,
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(
        gameData: GameData,
        userId: String
    ): Result<GameHistory> {
        // 1. 创建 GameHistory 记录
        // 2. 更新 LevelStatistics
        // 3. 更新 GlobalStatistics
        // 4. 触发成就检测
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

---

## 5. UI 设计

### 5.1 StatisticsScreen（主统计页面）

```
┌─────────────────────────────────────────┐
│  📊 学习统计                    [设置]    │
├─────────────────────────────────────────┤
│  全局统计卡片                             │
│  ┌─────────────────────────────────────┐ │
│  │ 总游戏: 156  │ 总分: 12,450        │ │
│  │ 学习: 8h 23m │ 连续: 7天 🔥       │ │
│  └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│  [历史记录] [关卡统计] [成就]            │
├─────────────────────────────────────────┤
│                                          │
│  内容区域（根据Tab显示）                 │
│                                          │
└─────────────────────────────────────────┘
```

### 5.2 GameHistoryScreen（历史记录）

```
┌─────────────────────────────────────────┐
│  筛选: [全部 ▼] [Spell Battle ▼] [7天 ▼]│
├─────────────────────────────────────────┤
│  📝 Feb 20, 14:30                        │
│  Spell Battle - Level 1           3⭐   │
│  准确率: 92% | 连击: 5 | 用时: 2:30     │
├─────────────────────────────────────────┤
│  ⚖️ Feb 20, 12:15                        │
│  Quick Judge - Normal             2⭐   │
│  准确率: 78% | 连击: 3 | 用时: 1:45     │
├─────────────────────────────────────────┤
│  📝 Feb 19, 16:45                        │
│  Spell Battle - Level 2           1⭐   │
│  准确率: 65% | 连击: 2 | 用时: 3:15     │
└─────────────────────────────────────────┘
```

### 5.3 LevelStatisticsScreen（关卡统计）

```
┌─────────────────────────────────────────┐
│  选择岛屿: [Look Island ▼]              │
├─────────────────────────────────────────┤
│  📍 Look Island - Level 1                │
│  ┌─────────────────────────────────────┐ │
│  │ 游戏次数: 12  │ 完美: 5次          │ │
│  │ 最高分: 850   │ 平均分: 720        │ │
│  │ 最佳时间: 1:45│ 平均时间: 2:10     │ │
│  │ 准确率: 85%   │ 最佳连击: 8        │ │
│  └─────────────────────────────────────┘ │
│  ████████████████░░░░░░░░░ 85% 精通     │
├─────────────────────────────────────────┤
│  📍 Look Island - Level 2                │
│  ...                                     │
└─────────────────────────────────────────┘
```

---

## 6. 功能需求

### 6.1 游戏历史追踪

**需求ID**: STATS-001
**优先级**: P0
**描述**: 记录每局游戏的完整数据

**验收标准**:
- ✅ 游戏结束时自动保存记录
- ✅ 包含所有必要字段（得分、星级、准确率等）
- ✅ 时间戳准确记录
- ✅ 支持4种游戏模式

### 6.2 统计数据聚合

**需求ID**: STATS-002
**优先级**: P0
**描述**: 自动聚合关卡和全局统计数据

**验收标准**:
- ✅ 每次游戏后更新关卡统计
- ✅ 每次游戏后更新全局统计
- ✅ 聚合计算准确无误
- ✅ 并发更新数据一致性

### 6.3 历史记录查询

**需求ID**: STATS-003
**优先级**: P1
**描述**: 支持多维度筛选历史记录

**验收标准**:
- ✅ 支持按关卡筛选
- ✅ 支持按游戏模式筛选
- ✅ 支持按时间范围筛选
- ✅ 分页加载流畅

### 6.4 性能指标计算

**需求ID**: STATS-004
**优先级**: P1
**描述**: 计算学习效率相关指标

**验收标准**:
- ✅ 平均响应时间排除猜测（<1s）
- ✅ 连续学习天数准确
- ✅ 学习时长只计算活跃时间
- ✅ 单词掌握度计算正确

---

## 7. 性能要求

### 7.1 查询性能

| 操作 | 目标时间 | 测量方法 |
|------|---------|----------|
| 插入游戏历史 | < 50ms | 数据库日志 |
| 查询历史记录（20条） | < 100ms | 性能测试 |
| 更新关卡统计 | < 50ms | 数据库日志 |
| 获取全局统计 | < 50ms | 性能测试 |

### 7.2 数据库优化

- **索引**: 为常用查询字段添加索引
- **分页**: 历史记录每页20条
- **清理**: 定期清理90天前的历史记录
- **缓存**: 全局统计使用内存缓存（5分钟TTL）

---

## 8. 测试要求

### 8.1 单元测试

- DAO 测试（数据库操作）
- Repository 测试（业务逻辑）
- UseCase 测试（集成逻辑）
- 聚合计算测试（统计准确性）

### 8.2 集成测试

- 游戏完成 → 历史保存流程
- 统计数据更新流程
- 连续学习天数计算

### 8.3 性能测试

- 大量数据插入测试（1000条记录）
- 复杂查询性能测试
- 并发更新数据一致性测试

---

## 9. 实施计划

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
7. 集成到游戏结束流程
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

---

## 10. 附录

### 10.1 术语表

| 术语 | 定义 |
|------|------|
| GameHistory | 单局游戏记录 |
| LevelStatistics | 关卡统计 |
| GlobalStatistics | 全局统计 |
| Streak | 连续学习天数 |
| Perfect Game | 3星完美通关 |

### 10.2 相关文档

- 系统设计: `docs/design/system/STATISTICS_SYSTEM_DESIGN.md`
- 游戏设计: `docs/design/game/statistics_achievements_design.md`
- 主需求文档: `docs/requirements/README.md`

---

**文档状态**: ✅ 完成
**下一步**: 实施阶段 1（数据模型和存储）
