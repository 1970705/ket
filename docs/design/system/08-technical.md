# 技术需求详细文档

**文档版本**: v1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**状态**: 详细需求定义

---

## 📋 目录

1. [性能要求](#1-性能要求)
2. [兼容性要求](#2-兼容性要求)
3. [质量要求](#3-质量要求)
4. [数据库架构](#4-数据库架构)
5. [API设计](#5-api设计)
6. [安全要求](#6-安全要求)

---

## 1. 性能要求

### 1.1 性能指标

| 指标 | 当前值 | 目标值 | 测量方法 | 优先级 |
|------|--------|--------|----------|--------|
| **应用启动时间** | 439ms | < 3s | Android Profiler | P0 |
| **关卡加载时间** | 待测 | < 1s | 日志记录 | P0 |
| **界面响应延迟** | 待测 | < 100ms | 用户体验测试 | P0 |
| **帧率** | 待测 | 60fps | GPU Profiler | P0 |
| **内存占用** | 待测 | < 150MB | Android Profiler | P1 |
| **APK大小** | 8.4MB | < 50MB | Build Output | ✅ |
| **电池消耗** | 待测 | < 10%/小时 | Battery Historian | P1 |
| **网络流量** | 0 | 本地优先 | Network Profiler | P2 |

### 1.2 性能监控

#### 性能基准测试

```kotlin
/**
 * Macrobenchmark 基准测试
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun scrollBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5
        ) {
            // 启动应用
            startActivityAndWait()

            // 导航到关卡列表
            // 执行滚动操作
            device.findObject(By.text("Level 1"))
                .click()

            device.waitForIdle()

            // 滚动列表
            val list = device.findObject(By.res("level_list"))
            list.setGestureMargin(device.displayWidth / 5)
            list.fling(Direction.DOWN)

            device.waitForIdle()
        }
    }
}
```

#### 性能回归检测

```kotlin
/**
 * CI/CD 性能回归检测
 */
object PerformanceRegression {
    fun detectRegression(
        baseline: MetricData,
        current: MetricData,
        threshold: Float = 0.1f // 10% 阈值
    ): Boolean {
        val regression = (current.value - baseline.value) / baseline.value

        return when {
            regression > threshold -> {
                Log.w("Performance", "Regression detected: $regression")
                true
            }
            else -> false
        }
    }
}
```

### 1.3 性能优化策略

#### Compose 优化

```kotlin
/**
 * Compose 稳定性注解
 */
@Composable
fun OptimizedContent(
    data: List<Item>,
    onItemClick: (Item) -> Unit
) {
    // 使用 key 稳定性
    LazyColumn {
        items(
            items = data,
            key = { it.id } // 关键: 使用稳定的key
        ) { item ->
            ItemRow(
                item = item,
                onClick = onItemClick
            )
        }
    }
}

// 避免不必要重组
@Composable
fun ItemRow(
    item: Item,
    onClick: (Item) -> Unit
) {
    // 使用 remember 避免重复计算
    val formattedText = remember(item.text) {
        formatText(item.text)
    }

    Text(
        text = formattedText,
        modifier = Modifier.clickable { onClick(item) }
    )
}
```

#### 图片加载优化

```kotlin
/**
 * Coil 图片加载配置
 */
object ImageLoadingConfig {
    val imageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25) // 25% 内存
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .maxSizeBytes(50 * 1024 * 1024) // 50MB
                .build()
        }
        .respectCacheHeaders(false) // 开发环境
        .build()
}
```

#### 数据库查询优化

```kotlin
/**
 * 数据库查询优化
 */
@Dao
interface OptimizedWordDao {
    // 使用索引优化
    @Query("""
        SELECT * FROM words
        WHERE island_id = :islandId
        AND level_id = :levelId
        ORDER BY word_id ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getWordsPaginated(
        islandId: String,
        levelId: String,
        limit: Int,
        offset: Int
    ): List<WordEntity>

    // 预编译语句
    @Query("SELECT COUNT(*) FROM words WHERE mastery_level >= :level")
    suspend fun countMasteredWords(level: Int): Int

    // 批量操作
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    // 事务支持
    @Transaction
    suspend fun updateWithTracking(words: List<WordEntity>) {
        insertAll(words)
        // 更新追踪信息
    }
}
```

---

## 2. 兼容性要求

### 2.1 平台兼容性

| 平台 | 最低版本 | 目标版本 | 测试覆盖 |
|------|---------|---------|----------|
| **Android** | 8.0 (API 26) | 14 (API 34) | ✅ 必须 |
| **屏幕尺寸** | 4.5英寸 | 12英寸 | ✅ 必须 |
| **屏幕密度** | MDPI | XXXHDPI | ✅ 必须 |
| **CPU架构** | ARMv7 | ARM64, x86 | ⚠️ 优先ARM64 |
| **RAM** | 2GB | 4GB+ | ✅ 必须 |

### 2.2 设备兼容性

#### 测试设备矩阵

| 品牌 | 型号 | Android版本 | 屏幕尺寸 | 优先级 |
|------|------|-----------|----------|--------|
| **Xiaomi** | Redmi Note 11 | 12 | 6.43" | P0 ✅ |
| **Samsung** | Galaxy A53 | 13 | 6.5" | P0 ✅ |
| **Google** | Pixel 6 | 13 | 6.4" | P1 |
| **Huawei** | P50 Lite | 10 | 6.5" | P1 |
| **Motorola** | Moto G52 | 12 | 6.6" | P2 |

#### 屏幕适配

```kotlin
/**
 * 响应式布局
 */
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    when {
        screenWidthDp < 600 -> {
            // 手机布局 (< 600dp)
            PhoneLayout(modifier)
        }
        screenWidthDp < 840 -> {
            // 折叠屏布局 (600-840dp)
            FoldableLayout(modifier)
        }
        else -> {
            // 平板布局 (> 840dp)
            TabletLayout(modifier)
        }
    }
}
```

---

## 3. 质量要求

### 3.1 测试覆盖率

| 层级 | 当前覆盖率 | 目标覆盖率 | 优先级 |
|------|-----------|-----------|--------|
| **Domain层** | 83%+ | 90%+ | P0 |
| **Data层** | 15% | 80% | P0 |
| **UI层** | 5% | 50% | P1 |
| **整体** | 21% | 80% | P0 |

### 3.2 代码质量

#### Lint 检查

```bash
# KtLint 代码格式检查
./gradlew ktlintCheck

# KtLint 自动格式化
./gradlew ktlintFormat

# Detekt 代码质量分析
./gradlew detekt

# 输出报告
# app/build/reports/ktlint/
# app/build/reports/detekt/
```

#### 质量门禁

**P0 (必须通过)**:
- ✅ 所有单元测试通过
- ✅ 首次启动测试通过
- ✅ logcat 无 FATAL/CRASH 错误
- ✅ KtLint 检查通过
- ✅ Detekt 无严重问题

**P1 (重要但不阻塞)**:
- ✅ 覆盖率 ≥ 80%
- ✅ 静态分析通过
- ✅ ≥ 2 设备真机测试通过
- ✅ 性能基准测试通过

**P2 (增强)**:
- ✅ TDD 覆盖率 50%
- ✅ ≥ 5 设备真机测试通过
- ✅ UI 测试自动化

### 3.3 单元测试标准

```kotlin
/**
 * 单元测试示例
 */
class WordRepositoryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: WordRepositoryImpl
    private lateinit var dao: FakeWordDao

    @Before
    fun setup() {
        dao = FakeWordDao()
        repository = WordRepositoryImpl(dao)
    }

    @Test
    fun `getWordsByLevel should return words for given level`() = runTest {
        // Given
        val expectedWords = listOf(
            testWord1,
            testWord2
        )
        dao.insertWords(expectedWords)

        // When
        val actualWords = repository.getWordsByLevel("look_island_level_01")

        // Then
        assertEquals(expectedWords, actualWords)
    }

    @Test
    fun `updateWordProgress should update mastery level`() = runTest {
        // Given
        val word = testWord1.copy(masteryLevel = 50)
        dao.insertWords(listOf(word))

        // When
        repository.updateWordProgress(
            wordId = word.id,
            isCorrect = true,
            strengthGain = 10
        )

        // Then
        val updated = dao.getWordById(word.id)
        assertEquals(60, updated?.masteryLevel)
    }
}
```

---

## 4. 数据库架构

### 4.1 Schema 演进

| 版本 | 日期 | 主要变更 | 迁移脚本 |
|------|------|----------|----------|
| **v1** | 初始 | 基础表结构 | - |
| **v2** | 2026-02-10 | 添加连击系统 | Migration 1→2 |
| **v3** | 2026-02-15 | 添加提示系统 | Migration 2→3 |
| **v4** | 2026-02-18 | 添加游戏模式 | Migration 3→4 |
| **v5** | 2026-02-20 | 统计和成就 | Migration 4→5 ⏳ |

### 4.2 数据库迁移

```kotlin
/**
 * Migration 4 → 5
 */
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

        // 创建索引
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_game_history_userId
            ON game_history(userId)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_game_history_levelId
            ON game_history(levelId)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_game_history_startTime
            ON game_history(startTime DESC)
        """)
    }
}

// 更新数据库版本
@Database(
    entities = [
        WordEntity::class,
        UserWordProgressEntity::class,
        LevelProgressEntity::class,
        IslandMasteryEntity::class,
        WorldMapEntity::class,
        GameHistoryEntity::class,
        LevelStatisticsEntity::class,
        GlobalStatisticsEntity::class,
        AchievementProgressEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class WordDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "wordland_database"
    }
}
```

### 4.3 数据库优化

#### 索引策略

```sql
-- 复合索引优化查询
CREATE INDEX index_game_history_user_level_time
ON game_history(userId, levelId, startTime DESC);

-- 覆盖索引优化聚合查询
CREATE INDEX index_user_progress_stats
ON user_word_progress(userId, masteryLevel, lastReviewedAt);
```

#### 查询优化

```kotlin
// 分页加载
@Query("""
    SELECT * FROM game_history
    WHERE userId = :userId
    ORDER BY startTime DESC
    LIMIT :limit OFFSET :offset
""")
suspend fun getGameHistoryPaginated(
    userId: String,
    limit: Int = 20,
    offset: Int = 0
): List<GameHistoryEntity>

// COUNT 查询优化
@Query("""
    SELECT COUNT(*) FROM game_history
    WHERE userId = :userId
""")
suspend fun getGameHistoryCount(userId: String): Int
```

---

## 5. API设计

### 5.1 Repository接口

```kotlin
/**
 * WordRepository 接口
 */
interface WordRepository {
    // 基础 CRUD
    suspend fun getWordById(wordId: String): Word?
    suspend fun getWordsByLevel(levelId: String): List<Word>
    suspend fun updateWordProgress(
        wordId: String,
        isCorrect: Boolean,
        strengthGain: Int
    )

    // 查询方法
    suspend fun getWordsNeedingReview(userId: String): List<Word>
    suspend fun getMasteredWords(userId: String): List<Word>

    // 统计方法
    suspend fun getWordCount(islandId: String): Int
    suspend fun getMasteredWordCount(userId: String): Int
}
```

### 5.2 UseCase接口

```kotlin
/**
 * UseCase 基类
 */
abstract class UseCase<in P, R> {
    abstract suspend operator fun invoke(parameters: P): Result<R>
}

/**
 * 示例: GetNextWordUseCase
 */
class GetNextWordUseCase(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) : UseCase<GetNextWordParams, Word>() {

    override suspend fun invoke(
        parameters: GetNextWordParams
    ): Result<Word> {
        val userId = parameters.userId
        val levelId = parameters.levelId

        // 获取需要复习的单词
        val reviewWords = wordRepository.getWordsNeedingReview(userId)

        // 获取未学习单词
        val newWords = wordRepository.getWordsByLevel(levelId)
            .filter { word ->
                progressRepository.getUserProgress(userId, word.id) == null
            }

        // 优先复习，否则新词
        val nextWord = reviewWords.firstOrNull() ?: newWords.firstOrNull()

        return if (nextWord != null) {
            Result.Success(nextWord)
        } else {
            Result.Error(NoSuchElementException("No words available"))
        }
    }
}

data class GetNextWordParams(
    val userId: String,
    val levelId: String
)
```

---

## 6. 安全要求

### 6.1 数据安全

#### 敏感数据加密

```kotlin
/**
 * EncryptedSharedPreferences
 */
object SecurePreferences {
    fun create(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

// 使用
val securePrefs = SecurePreferences.create(context)
securePrefs.edit {
    putString("parent_pin_hash", hashPin(pin))
}
```

#### 数据库加密 (未来)

```kotlin
// SQLCipher 支持 (可选)
@Database(
    entities = [WordEntity::class, ...],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SecureDatabase : RoomDatabase() {
    companion object {
        fun getDatabase(context: Context): SecureDatabase {
            val passphrase = SQLiteDatabase.getBytes("secure-passphrase".toCharArray())
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context,
                SecureDatabase::class.java,
                "wordland_secure.db"
            )
                .openHelperFactory(factory)
                .build()
        }
    }
}
```

### 6.2 代码混淆

#### ProGuard配置

```proguard
# app/proguard-rules.pro

# 保留 Keep 数据类
-keep class com.wordland.domain.model.** { *; }

# 保留 Room 实体
-keep class com.wordland.data.entity.** { *; }
-keep @androidx.room.Entity class *

# 保留 UseCase
-keep class com.wordland.domain.usecase.** { *; }

# 保留序列化数据
-keepclassmembers class com.wordland.domain.model.** {
    public <init>(...);
}

# 优化配置
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
```

### 6.3 权限最小化

#### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 最小权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- 正常权限 (无需运行时请求) -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- 危险权限 (未来需要) -->
    <!-- <uses-permission android:name="android.permission.RECORD_AUDIO" /> -->

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.Wordland">
        ...
    </application>
</manifest>
```

---

## 📊 附录

### A. 性能监控工具

```kotlin
/**
 * 性能监控工具
 */
object PerformanceMonitor {
    fun startMonitoring() {
        // 启动性能监控
        // 1. 帧率监控
        // 2. 内存监控
        // 3. CPU监控
        // 4. 网络监控
    }

    fun reportMetrics() {
        // 上报性能指标
        // 发送到分析平台
    }
}
```

### B. 日志系统

```kotlin
/**
 * 统一日志系统
 */
object AppLogger {
    private const val TAG = "Wordland"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
        // 上报错误到分析平台
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }
}
```

---

**文档状态**: ✅ 详细需求定义完成
**下一步**: 实施参考
