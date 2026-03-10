# UseCase Layer Architecture Design

**设计者**: Android Architect (workflow)
**日期**: 2026-02-15
**版本**: 1.0

---

## 1. 设计目标

### 1.1 核心原则
- **单一职责**: 每个UseCase只负责一个业务操作
- **依赖倒置**: UseCase依赖Repository接口，不依赖具体实现
- **可测试性**: 所有UseCase可独立单元测试
- **可复用性**: UseCase可在多个ViewModel中复用

### 1.2 架构分层

```
┌─────────────────────────────────────┐
│         UI Layer (ViewModels)       │
│  - 只处理UI状态                      │
│  - 不包含业务逻辑                    │
└──────────────┬──────────────────────┘
               │ depends on
               ↓
┌─────────────────────────────────────┐
│      Domain Layer (UseCases)        │
│  - 业务逻辑实现                      │
│  - 协调多个Repository                │
│  - 处理错误和转换数据                │
└──────────────┬──────────────────────┘
               │ depends on
               ↓
┌─────────────────────────────────────┐
│       Data Layer (Repositories)     │
│  - 数据访问抽象                      │
│  - 缓存策略                          │
│  - 数据转换                          │
└─────────────────────────────────────┘
```

---

## 2. UseCase分类设计

### 2.1 Query UseCases (查询类)
返回数据流 `Flow<T>` 或单次结果 `Result<T>`

### 2.2 Command UseCases (命令类)
执行操作并返回 `Result<T>` 或 `Unit`

### 2.3 命名规范
- Query: `Get{Entity}`, `Observe{Entity}`
- Command: `{Action}{Entity}`, `Submit{Action}`

---

## 3. 核心UseCase定义

### 3.1 Home & Island Map

#### `GetIslandsUseCase`
```kotlin
/**
 * 获取所有岛屿列表
 */
class GetIslandsUseCase @Inject constructor(
    private val islandRepository: IslandMasteryRepository,
    private val wordRepository: WordRepository
) {
    operator fun invoke(userId: String): Flow<List<IslandWithProgress>> {
        return islandRepository.getAllIslandMastery(userId)
            .map { masteryList ->
                masteryList.map { mastery ->
                    IslandWithProgress(
                        islandId = mastery.islandId,
                        masteryPercentage = mastery.masteryPercentage,
                        isUnlocked = mastery.isNextIslandUnlocked,
                        wordCount = mastery.totalWords
                    )
                }
            }
    }
}
```

#### `GetUserStatsUseCase`
```kotlin
/**
 * 获取用户统计数据
 */
class GetUserStatsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val islandRepository: IslandMasteryRepository
) {
    suspend operator fun invoke(userId: String): Result<UserStats> {
        return try {
            val totalLevels = progressRepository.getTotalLevels(userId)
            val completedLevels = progressRepository.getCompletedLevels(userId)
            val masteredIslands = islandRepository.getMasteredIslandCount(userId)

            Result.Success(UserStats(
                totalLevels = totalLevels,
                completedLevels = completedLevels,
                masteredIslands = masteredIslands,
                completionRate = if (totalLevels > 0) {
                    completedLevels.toFloat() / totalLevels
                } else 0f
            ))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 3.2 Level Select

#### `GetLevelsUseCase`
```kotlin
/**
 * 获取指定岛屿的关卡列表
 */
class GetLevelsUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository
) {
    operator fun invoke(islandId: String, userId: String): Flow<List<LevelWithProgress>> {
        return progressRepository.getLevelsByIsland(islandId, userId)
            .map { levels ->
                levels.map { level ->
                    LevelWithProgress(
                        levelId = level.levelId,
                        levelName = level.levelName,
                        stars = level.stars,
                        isCompleted = level.isCompleted,
                        isUnlocked = level.isUnlocked
                    )
                }
            }
    }
}
```

### 3.3 Learning (Core)

#### `LoadLevelWordsUseCase`
```kotlin
/**
 * 加载关卡单词
 * 返回该关卡的所有单词，按学习顺序排列
 */
class LoadLevelWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(levelId: String): Result<List<Word>> {
        return try {
            val words = wordRepository.getWordsByLevel(levelId)
            if (words.isEmpty()) {
                Result.Error(NoSuchElementException("No words found for level: $levelId"))
            } else {
                Result.Success(words.sortedBy { it.order })
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

#### `SubmitAnswerUseCase`
```kotlin
/**
 * 提交答案并更新学习进度
 * 核心业务逻辑：
 * 1. 检查答案是否正确
 * 2. 更新记忆强度（使用MemoryStrengthAlgorithm）
 * 3. 检测猜测行为（使用GuessingDetector）
 * 4. 更新UserWordProgress
 * 5. 记录行为追踪
 */
class SubmitAnswerUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository,
    private val trackingRepository: TrackingRepository
) {
    suspend operator fun invoke(
        userId: String,
        wordId: String,
        userAnswer: String,
        responseTime: Long,
        hintUsed: Boolean,
        levelId: String
    ): Result<SubmitAnswerResult> {
        return try {
            // 1. 获取单词信息
            val word = wordRepository.getWordById(wordId)
                ?: return Result.Error(NoSuchElementException("Word not found: $wordId"))

            // 2. 检查答案正确性（忽略大小写）
            val isCorrect = userAnswer.equals(word.translation, ignoreCase = true)

            // 3. 获取当前进度
            val currentProgress = progressRepository.getWordProgress(userId, wordId)

            // 4. 检测猜测行为
            val recentPatterns = trackingRepository.getRecentPatterns(userId, limit = 5)
            val isGuessing = GuessingDetector.detectGuessing(recentPatterns)

            // 5. 计算新的记忆强度
            val currentStrength = currentProgress?.memoryStrength
                ?: DomainConstants.MEMORY_STRENGTH_INITIAL
            val newStrength = MemoryStrengthAlgorithm.calculateNewStrength(
                currentStrength = currentStrength,
                isCorrect = isCorrect,
                isGuessing = isGuessing,
                wordDifficulty = word.difficulty
            )

            // 6. 更新进度
            progressRepository.updateWordProgress(
                userId = userId,
                wordId = wordId,
                isCorrect = isCorrect,
                newMemoryStrength = newStrength,
                responseTime = responseTime
            )

            // 7. 记录行为
            trackingRepository.recordAnswer(
                userId = userId,
                wordId = wordId,
                sceneId = levelId,
                isCorrect = isCorrect,
                responseTime = responseTime,
                difficulty = word.difficulty,
                hintUsed = hintUsed,
                isNewWord = currentProgress == null
            )

            // 8. 返回结果
            Result.Success(SubmitAnswerResult(
                word = word,
                isCorrect = isCorrect,
                newMemoryStrength = newStrength,
                isGuessing = isGuessing,
                timeTaken = responseTime,
                hintUsed = hintUsed
            ))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

#### `GetNextWordUseCase`
```kotlin
/**
 * 获取下一个待学习的单词
 * 基于遗忘算法和优先级排序
 */
class GetNextWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(
        userId: String,
        levelId: String,
        currentWordId: String?
    ): Result<Word?> {
        return try {
            val allWords = wordRepository.getWordsByLevel(levelId)
            val progressMap = progressRepository.getWordProgresses(userId, allWords.map { it.id })

            // 筛选逻辑：
            // 1. 优先返回未学习的词
            // 2. 其次返回需要复习的词（nextReviewTime < now）
            // 3. 最后返回记忆强度最低的词

            val nextWord = allWords
                .filter { it.id != currentWordId }
                .sortedByDescending { word ->
                    val progress = progressMap[word.id]
                    when {
                        progress == null -> 100  // 未学习，最高优先级
                        progress.nextReviewTime != null &&
                        progress.nextReviewTime!! < System.currentTimeMillis() -> 80  // 需要复习
                        else -> 100 - progress.memoryStrength  // 记忆强度越低优先级越高
                    }
                }
                .firstOrNull()

            Result.Success(nextWord)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 3.4 Review

#### `GetReviewWordsUseCase`
```kotlin
/**
 * 获取需要复习的单词列表
 * 条件：nextReviewTime <= now 或 memoryStrength < 60
 */
class GetReviewWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository
) {
    suspend operator fun invoke(userId: String, limit: Int = 20): Result<List<ReviewWordItem>> {
        return try {
            // 获取所有需要复习的单词进度
            val progressList = progressRepository.getWordsNeedReview(userId, limit)

            // 获取单词详情
            val reviewWords = progressList.map { progress ->
                val word = wordRepository.getWordById(progress.wordId)!!
                ReviewWordItem(
                    word = word,
                    memoryStrength = progress.memoryStrength,
                    lastReviewTime = progress.lastReviewTime,
                    correctRate = if (progress.totalAttempts > 0) {
                        progress.correctAttempts.toFloat() / progress.totalAttempts
                    } else 0f
                )
            }

            Result.Success(reviewWords.sortedBy { it.memoryStrength })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## 4. 数据模型定义

### 4.1 UseCase返回模型

```kotlin
// 使用独立的数据类，避免污染Domain Model
data class IslandWithProgress(
    val islandId: String,
    val masteryPercentage: Double,
    val isUnlocked: Boolean,
    val wordCount: Int
)

data class LevelWithProgress(
    val levelId: String,
    val levelName: String,
    val stars: Int,
    val isCompleted: Boolean,
    val isUnlocked: Boolean
)

data class UserStats(
    val totalLevels: Int,
    val completedLevels: Int,
    val masteredIslands: Int,
    val completionRate: Float
)

data class SubmitAnswerResult(
    val word: Word,
    val isCorrect: Boolean,
    val newMemoryStrength: Int,
    val isGuessing: Boolean,
    val timeTaken: Long,
    val hintUsed: Boolean
)
```

### 4.2 错误处理

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## 5. 依赖注入配置

### 5.1 UseCase Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Home & Island Map
    @Provides
    @Singleton
    fun provideGetIslandsUseCase(
        islandRepo: IslandMasteryRepository,
        wordRepo: WordRepository
    ) = GetIslandsUseCase(islandRepo, wordRepo)

    @Provides
    @Singleton
    fun provideGetUserStatsUseCase(
        progressRepo: ProgressRepository,
        islandRepo: IslandMasteryRepository
    ) = GetUserStatsUseCase(progressRepo, islandRepo)

    // Level Select
    @Provides
    @Singleton
    fun provideGetLevelsUseCase(
        progressRepo: ProgressRepository,
        wordRepo: WordRepository
    ) = GetLevelsUseCase(progressRepo, wordRepo)

    // Learning
    @Provides
    @Singleton
    fun provideLoadLevelWordsUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository
    ) = LoadLevelWordsUseCase(wordRepo, progressRepo)

    @Provides
    @Singleton
    fun provideSubmitAnswerUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository,
        trackingRepo: TrackingRepository
    ) = SubmitAnswerUseCase(wordRepo, progressRepo, trackingRepo)

    @Provides
    @Singleton
    fun provideGetNextWordUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository
    ) = GetNextWordUseCase(wordRepo, progressRepo)

    // Review
    @Provides
    @Singleton
    fun provideGetReviewWordsUseCase(
        wordRepo: WordRepository,
        progressRepo: ProgressRepository
    ) = GetReviewWordsUseCase(wordRepo, progressRepo)
}
```

---

## 6. ViewModel集成示例

### 6.1 重构后的LearningViewModel

```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val loadLevelWords: LoadLevelWordsUseCase,
    private val submitAnswer: SubmitAnswerUseCase,
    private val getNextWord: GetNextWordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Loading)
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    fun loadLevel(levelId: String, islandId: String) {
        viewModelScope.launch {
            _uiState.value = LearningUiState.Loading
            loadLevelWords(levelId)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = LearningUiState.Ready(
                                question = generateQuestion(result.data.first()),
                                hintAvailable = true,
                                hintShown = false
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = LearningUiState.Error(result.exception.message)
                        }
                    }
                }
        }
    }

    fun submitAnswer(userAnswer: String, responseTime: Long, hintUsed: Boolean) {
        viewModelScope.launch {
            val currentWord = (_uiState.value as? LearningUiState.Ready)?.currentWord
                ?: return@launch

            submitAnswer(
                userId = "user_001",
                wordId = currentWord.id,
                userAnswer = userAnswer,
                responseTime = responseTime,
                hintUsed = hintUsed,
                levelId = currentWord.levelId
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = LearningUiState.Feedback(
                            result = result.data.toLearnWordResult(),
                            stars = if (result.data.isCorrect) 3 else 1,
                            progress = calculateProgress()
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = LearningUiState.Error(result.exception.message)
                    }
                }
            }
        }
    }
}
```

---

## 7. 测试策略

### 7.1 UseCase单元测试模板

```kotlin
@ExtendWith(MockKExtension::class)
class SubmitAnswerUseCaseTest {

    @RelaxedMockK
    lateinit var wordRepository: WordRepository

    @RelaxedMockK
    lateinit var progressRepository: ProgressRepository

    @RelaxedMockK
    lateinit var trackingRepository: TrackingRepository

    @Test
    fun `invoke should return success when answer is correct`() = runTest {
        // Given
        val word = Word(id = "word_001", translation = "观看", ...)
        every { wordRepository.getWordById("word_001") } returns word
        every { progressRepository.getWordProgress("user_001", "word_001") } returns null

        val useCase = SubmitAnswerUseCase(wordRepository, progressRepository, trackingRepository)

        // When
        val result = useCase(
            userId = "user_001",
            wordId = "word_001",
            userAnswer = "观看",
            responseTime = 3000,
            hintUsed = false,
            levelId = "level_01"
        )

        // Then
        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isCorrect)
        verify { progressRepository.updateWordProgress(any(), any(), any(), any(), any()) }
    }
}
```

---

## 8. 实现检查清单

### Phase 1: P0 UseCases (核心)
- [ ] GetIslandsUseCase
- [ ] GetLevelsUseCase
- [ ] LoadLevelWordsUseCase
- [ ] SubmitAnswerUseCase
- [ ] GetNextWordUseCase
- [ ] GetReviewWordsUseCase

### Phase 2: P1 UseCases (完善)
- [ ] GetUserStatsUseCase
- [ ] GetUserProgressUseCase
- [ ] CheckIslandUnlockedUseCase
- [ ] SubmitReviewAnswerUseCase
- [ ] UseHintUseCase

### Phase 3: 测试 (质量)
- [ ] UseCase单元测试 (100%覆盖)
- [ ] ViewModel集成测试
- [ ] Repository Mock测试

### Phase 4: 文档 (规范)
- [ ] UseCase KDoc注释
- [ ] 架构决策记录(ADR)
- [ ] 使用示例

---

## 9. 风险与缓解

### 风险1: Repository接口不完整
**缓解**: 先完善Repository接口，再实现UseCase

### 风险2: UseCase之间依赖复杂
**缓解**: 保持UseCase单一职责，避免UseCase之间相互调用

### 风险3: 异步处理复杂
**缓解**: 统一使用Flow/Result模式，协程处理

---

## 10. 下一步

1. ✅ 架构设计完成
2. ⏭️ 实现P0核心UseCases (android-engineer workflow)
3. ⏭️ 编写单元测试
4. ⏭️ 重构ViewModels
5. ⏭️ 验证架构合规性

---

**设计者签名**: Android Architect (Claude)
**审核者**: 待审核
**批准者**: 待批准
