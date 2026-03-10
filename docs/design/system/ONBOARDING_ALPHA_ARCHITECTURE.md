# Onboarding Alpha 技术架构方案

**文档版本**: 1.0
**创建日期**: 2026-02-25
**作者**: android-architect (Wordland团队)
**状态**: Alpha 规划
**Epic**: #10 - Onboarding Experience

---

## 执行摘要

### Alpha 目标

**Week 1-2 交付物**: 可玩的核心 Onboarding 流程
- 欢迎界面 → 宠物选择 → 教学关卡 → 首个宝箱

### 架构原则

- Clean Architecture (UI → Domain → Data)
- 最小化新代码（复用现有组件）
- Service Locator 依赖注入
- Room 持久化

---

## 第一部分：Clean Architecture 分层

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ WelcomeScreen   │  │ PetSelection    │  │ Tutorial    │ │
│  │                 │  │ Screen          │  │ Screen      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
│                           ↓                                    │
│  ┌───────────────────────────────────────────────────────┐   │
│  │         OnboardingViewModel (StateFlow)               │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ calls
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ OnboardingState │  │ TutorialConfig  │  │ PetType     │ │
│  │ (model)         │  │ (model)         │  │ (enum)      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
│  ┌───────────────────────────────────────────────────────┐   │
│  │              UseCases                                  │   │
│  │  - StartOnboardingUseCase                              │   │
│  │  - SelectPetUseCase                                    │   │
│  │  - CompleteTutorialUseCase                             │   │
│  │  - OpenFirstChestUseCase                               │   │
│  └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ calls
┌─────────────────────────────────────────────────────────────┐
│                         Data Layer                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │ OnboardingState │  │ OnboardingState │  │             │ │
│  │ Dao             │  │ Repository      │  │ WordDatabase│ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 第二部分：Domain 层设计

### 2.1 数据模型

```kotlin
// File: domain/model/OnboardingState.kt
package com.wordland.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Onboarding 流程状态
 *
 * Alpha 范围: WELCOME → PET_SELECTION → TUTORIAL → FIRST_CHEST
 */
@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey
    val userId: String,                       // 单用户MVP:固定为"user_001"
    val currentPhase: OnboardingPhase,         // 当前阶段
    val selectedPet: PetType?,                 // 选择的宠物
    val completedTutorialWords: Int = 0,       // 教学关卡完成单词数
    val lastOpenedChest: Long = 0L,            // 上次开宝箱时间
    val totalStars: Int = 0,                   // 获得星星总数
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Onboarding 阶段枚举
 */
enum class OnboardingPhase {
    NOT_STARTED,      // 未开始
    WELCOME,          // 欢迎界面
    PET_SELECTION,    // 宠物选择
    TUTORIAL,         // 教学关卡
    FIRST_CHEST,      // 首次开宝箱
    COMPLETED         // Alpha 完成
}

/**
 * 宠物类型枚举（Alpha 全部解锁）
 */
enum class PetType {
    DOLPHIN,    // 海豚 🐬
    CAT,        // 猫咪 🐱
    DOG,        // 小狗 🐶
    FOX         // 狐狸 🦊
}
```

```kotlin
// File: domain/model/TutorialWordConfig.kt
package com.wordland.domain.model

/**
 * 教学关卡单词配置
 *
 * Alpha 版本: 35%预填，最多3次提示
 */
data class TutorialWordConfig(
    val word: String,
    val translation: String,
    val preFillRatio: Float = 0.35f,       // 35%预填
    val minPreFillLetters: Int = 1,        // 至少预填1个字母
    val hintsAllowed: Int = 3,             // 最多3次提示
    val showFirstLetter: Boolean = true,   // 显示首字母
    val timeLimit: Int? = null             // 无时间限制
) {
    /**
     * 计算预填字母数量
     */
    fun calculatePreFillCount(): Int {
        val count = (word.length * preFillRatio).toInt()
        return maxOf(count, minPreFillLetters)
    }

    /**
     * 生成预填字母索引
     */
    fun generatePreFilledIndices(): Set<Int> {
        val count = calculatePreFillCount()
        return (0 until word.length)
            .shuffled()
            .take(count)
            .toSet()
    }
}
```

```kotlin
// File: domain/model/ChestReward.kt
package com.wordland.domain.model

/**
 * 宝箱奖励模型（Alpha 简化版）
 */
sealed class ChestReward {
    abstract val rarity: RewardRarity
    abstract val name: String

    data class PetEmoji(
        val petType: PetType,
        val emoji: String,
        val description: String
    ) : ChestReward() {
        override val rarity = RewardRarity.COMMON
        override val name = "宠物表情"
    }

    data class CelebrationEffect(
        val effectName: String,
        val description: String
    ) : ChestReward() {
        override val rarity = RewardRarity.RARE
        override val name = "庆祝特效"
    }
}

enum class RewardRarity {
    COMMON,   // 普通（50%）
    RARE,     // 稀有（30%）
    EPIC      // 史诗（20%）
}
```

### 2.2 UI 状态模型

```kotlin
// File: domain/model/OnboardingUiState.kt
package com.wordland.domain.model

/**
 * Onboarding UI 状态
 */
sealed class OnboardingUiState {
    object Idle : OnboardingUiState()

    // 欢迎
    data class Welcome(
        val showStartButton: Boolean = true
    ) : OnboardingUiState()

    // 宠物选择
    data class PetSelection(
        val availablePets: List<PetType> = PetType.entries.toList(),
        val selectedPet: PetType? = null
    ) : OnboardingUiState()

    // 教学关卡
    data class Tutorial(
        val currentWordIndex: Int,
        val totalWords: Int = 5,
        val question: TutorialQuestion,
        val progress: Float
    ) : OnboardingUiState()

    // 开宝箱
    data class OpeningChest(
        val reward: ChestReward
    ) : OnboardingUiState()

    // 完成
    data class Completed(
        val pet: PetType,
        val wordsLearned: Int,
        val stars: Int
    ) : OnboardingUiState()

    // 错误
    data class Error(val message: String) : OnboardingUiState()
}

/**
 * 教学关卡问题
 */
data class TutorialQuestion(
    val word: String,
    val translation: String,
    val preFilledLetters: Set<Int>,
    val hintsRemaining: Int
)
```

### 2.3 UseCase 定义

```kotlin
// File: domain/usecase/StartOnboardingUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.repository.OnboardingRepository

/**
 * 启动 Onboarding 流程
 */
class StartOnboardingUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(): OnboardingState {
        val existing = repository.getOnboardingState()

        return if (existing == null) {
            // 首次启动，创建初始状态
            val initialState = OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.WELCOME,
                selectedPet = null
            )
            repository.saveOnboardingState(initialState)
            initialState
        } else {
            // 恢复现有状态
            existing
        }
    }
}
```

```kotlin
// File: domain/usecase/SelectPetUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository

/**
 * 选择宠物
 */
class SelectPetUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(pet: PetType): OnboardingState {
        val state = repository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        val updated = state.copy(
            selectedPet = pet,
            currentPhase = OnboardingPhase.TUTORIAL,
            updatedAt = System.currentTimeMillis()
        )
        repository.saveOnboardingState(updated)
        return updated
    }
}
```

```kotlin
// File: domain/usecase/GetTutorialWordsUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.TutorialWordConfig
import com.wordland.domain.repository.WordRepository

/**
 * 获取教学关卡单词
 *
 * Alpha 版本: 返回5个最简单的单词
 */
class GetTutorialWordsUseCase(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(): List<TutorialWordConfig> {
        // 获取最简单的5个单词
        val simpleWords = wordRepository.getSimplestWords(count = 5)

        return simpleWords.map { word ->
            TutorialWordConfig(
                word = word.english,
                translation = word.chinese
            )
        }
    }
}
```

```kotlin
// File: domain/usecase/CompleteTutorialWordUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.repository.OnboardingRepository

/**
 * 完成教学关卡单词
 */
class CompleteTutorialWordUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(stars: Int): OnboardingState {
        val state = repository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        val newCount = state.completedTutorialWords + 1
        val newPhase = if (newCount >= 5) {
            OnboardingPhase.FIRST_CHEST
        } else {
            state.currentPhase
        }

        val updated = state.copy(
            completedTutorialWords = newCount,
            currentPhase = newPhase,
            totalStars = state.totalStars + stars,
            updatedAt = System.currentTimeMillis()
        )
        repository.saveOnboardingState(updated)
        return updated
    }
}
```

```kotlin
// File: domain/usecase/OpenFirstChestUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.ChestReward
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.RewardRarity
import com.wordland.domain.repository.OnboardingRepository
import kotlin.random.Random

/**
 * 开启首个宝箱
 */
class OpenFirstChestUseCase(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke(): ChestReward {
        val state = repository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        // Alpha: 100% 掉落奖励
        val reward = generateReward(state.selectedPet!!)

        // 更新状态
        val updated = state.copy(
            currentPhase = OnboardingPhase.COMPLETED,
            lastOpenedChest = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveOnboardingState(updated)

        return reward
    }

    private fun generateReward(pet: PetType): ChestReward {
        return when (Random.nextInt(100)) {
            in 0..50 -> ChestReward.PetEmoji(pet, "🎉", "你获得了一个可爱的表情！")
            in 51..80 -> ChestReward.CelebrationEffect("彩带", "庆祝特效解锁！")
            else -> ChestReward.PetEmoji(pet, "✨", "稀有造型！")
        }
    }
}
```

---

## 第三部分：Data 层设计

### 3.1 DAO

```kotlin
// File: data/dao/OnboardingStateDao.kt
package com.wordland.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wordland.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingStateDao {
    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    fun getByUserId(userId: String): Flow<OnboardingState?>

    @Query("SELECT * FROM onboarding_state WHERE userId = :userId")
    suspend fun getByUserIdSync(userId: String): OnboardingState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: OnboardingState)

    @Update
    suspend fun update(state: OnboardingState)

    @Query("DELETE FROM onboarding_state WHERE userId = :userId")
    suspend fun delete(userId: String)
}
```

### 3.2 Repository

```kotlin
// File: data/repository/OnboardingRepositoryImpl.kt
package com.wordland.data.repository

import com.wordland.data.dao.OnboardingStateDao
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class OnboardingRepositoryImpl(
    private val dao: OnboardingStateDao,
    private val userId: String = "user_001"
) : OnboardingRepository {

    override fun getOnboardingStateFlow(): Flow<OnboardingState?> {
        return dao.getByUserId(userId)
    }

    override suspend fun getOnboardingState(): OnboardingState? {
        return dao.getByUserIdSync(userId)
    }

    override suspend fun saveOnboardingState(state: OnboardingState) {
        dao.insert(state)
    }

    override suspend fun updateOnboardingState(state: OnboardingState) {
        dao.update(state)
    }
}
```

```kotlin
// File: domain/repository/OnboardingRepository.kt
package com.wordland.domain.repository

import com.wordland.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun getOnboardingStateFlow(): Flow<OnboardingState?>
    suspend fun getOnboardingState(): OnboardingState?
    suspend fun saveOnboardingState(state: OnboardingState)
    suspend fun updateOnboardingState(state: OnboardingState)
}
```

### 3.3 数据库迁移

```kotlin
// File: data/database/WordDatabase.kt (添加迁移)
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 onboarding_state 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS onboarding_state (
                userId TEXT PRIMARY KEY NOT NULL,
                currentPhase TEXT NOT NULL,
                selectedPet TEXT,
                completedTutorialWords INTEGER NOT NULL DEFAULT 0,
                lastOpenedChest INTEGER NOT NULL DEFAULT 0,
                totalStars INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}
```

---

## 第四部分：UI 层设计

### 4.1 ViewModel

```kotlin
// File: ui/viewmodel/OnboardingViewModel.kt
package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingUiState
import com.wordland.domain.model.PetType
import com.wordland.domain.model.TutorialQuestion
import com.wordland.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val startOnboarding: StartOnboardingUseCase,
    private val selectPet: SelectPetUseCase,
    private val getTutorialWords: GetTutorialWordsUseCase,
    private val completeTutorialWord: CompleteTutorialWordUseCase,
    private val openFirstChest: OpenFirstChestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val currentOnboardingState = MutableStateFlow<com.wordland.domain.model.OnboardingState?>(null)
    private val tutorialWords = mutableListOf<TutorialWordConfig>()
    private val answers = mutableMapOf<Int, String>()  // wordIndex -> userAnswer

    /**
     * 启动 Onboarding
     */
    fun startOnboarding() {
        viewModelScope.launch {
            try {
                val state = startOnboarding()
                currentOnboardingState.value = state

                when (state.currentPhase) {
                    OnboardingPhase.NOT_STARTED, OnboardingPhase.WELCOME -> {
                        _uiState.value = OnboardingUiState.Welcome()
                    }
                    OnboardingPhase.PET_SELECTION -> {
                        _uiState.value = OnboardingUiState.PetSelection()
                    }
                    OnboardingPhase.TUTORIAL -> {
                        loadTutorialWords()
                    }
                    else -> {
                        _uiState.value = OnboardingUiState.Completed(
                            state.selectedPet!!,
                            state.completedTutorialWords,
                            state.totalStars
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * 选择宠物
     */
    fun selectPet(pet: PetType) {
        viewModelScope.launch {
            try {
                val state = selectPet(pet)
                currentOnboardingState.value = state
                loadTutorialWords()
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Failed to select pet")
            }
        }
    }

    /**
     * 加载教学单词
     */
    private fun loadTutorialWords() {
        viewModelScope.launch {
            try {
                val words = getTutorialWords()
                tutorialWords.clear()
                tutorialWords.addAll(words)
                showCurrentWord()
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Failed to load words")
            }
        }
    }

    /**
     * 显示当前单词
     */
    private fun showCurrentWord() {
        val state = currentOnboardingState.value ?: return
        val currentWord = tutorialWords[state.completedTutorialWords]

        val question = TutorialQuestion(
            word = currentWord.word,
            translation = currentWord.translation,
            preFilledLetters = currentWord.generatePreFilledIndices(),
            hintsRemaining = currentWord.hintsAllowed
        )

        _uiState.value = OnboardingUiState.Tutorial(
            currentWordIndex = state.completedTutorialWords,
            totalWords = 5,
            question = question,
            progress = state.completedTutorialWords.toFloat() / 5f
        )
    }

    /**
     * 提交单词答案
     */
    fun submitAnswer(answer: String) {
        viewModelScope.launch {
            try {
                val currentWord = tutorialWords[currentOnboardingState.value?.completedTutorialWords ?: 0]
                val isCorrect = answer.equals(currentWord.word, ignoreCase = true)
                val stars = if (isCorrect) 3 else 1

                val state = completeTutorialWord(stars)
                currentOnboardingState.value = state

                when {
                    state.completedTutorialWords >= 5 && state.currentPhase == OnboardingPhase.FIRST_CHEST -> {
                        // 触发开宝箱
                        openChest()
                    }
                    else -> {
                        showCurrentWord()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Failed to submit answer")
            }
        }
    }

    /**
     * 开宝箱
     */
    private fun openChest() {
        viewModelScope.launch {
            try {
                val reward = openFirstChest()
                _uiState.value = OnboardingUiState.OpeningChest(reward)
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Failed to open chest")
            }
        }
    }
}
```

### 4.2 Service Locator 更新

```kotlin
// File: di/AppServiceLocator.kt (添加)
class AppServiceLocator private constructor(
    private val context: Context
) {
    // ... existing code ...

    // Onboarding dependencies
    private val onboardingRepository: OnboardingRepository by lazy {
        OnboardingRepositoryImpl(
            database.onboardingStateDao()
        )
    }

    fun provideOnboardingViewModelFactory(): OnboardingViewModelFactory {
        return OnboardingViewModelFactory(
            startOnboarding = StartOnboardingUseCase(onboardingRepository),
            selectPet = SelectPetUseCase(onboardingRepository),
            getTutorialWords = GetTutorialWordsUseCase(wordRepository),
            completeTutorialWord = CompleteTutorialWordUseCase(onboardingRepository),
            openFirstChest = OpenFirstChestUseCase(onboardingRepository)
        )
    }
}
```

---

## 第五部分：导航流程

```kotlin
// File: navigation/SetupNavGraph.kt (添加 Onboarding 路由)

// Onboarding 路由定义
sealed class OnboardingRoute(val route: String) {
    object Welcome : OnboardingRoute("onboarding/welcome")
    object PetSelection : OnboardingRoute("onboarding/pet_selection")
    object Tutorial : OnboardingRoute("onboarding/tutorial")
    object Chest : OnboardingRoute("onboarding/chest")
}

// 导图配置
composable(
    route = OnboardingRoute.Welcome.route,
    deepLinks = listOf(navDeepLink { uriPattern = "wordland://onboarding" })
) {
    OnboardingWelcomeScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onStart = { /* Navigate to PetSelection */ }
    )
}

composable(route = OnboardingRoute.PetSelection.route) {
    OnboardingPetSelectionScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onPetSelected = { pet -> /* Navigate to Tutorial */ }
    )
}

composable(route = OnboardingRoute.Tutorial.route) {
    OnboardingTutorialScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onComplete = { /* Navigate to Chest */ }
    )
}

composable(route = OnboardingRoute.Chest.route) {
    OnboardingChestScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onDismiss = { /* Navigate to Home */ }
    )
}
```

---

## 第六部分：Alpha 任务分解

### Week 1 任务

| 任务ID | 任务 | 负责角色 | 预计时间 | 依赖 |
|--------|------|----------|----------|------|
| A-1 | 创建 Domain 数据模型 | android-engineer | 4小时 | - |
| A-2 | 创建 DAO + Repository | android-engineer | 3小时 | A-1 |
| A-3 | 实现 UseCases | android-engineer | 6小时 | A-2 |
| A-4 | 创建 OnboardingViewModel | android-engineer | 4小时 | A-3 |
| A-5 | 设计 Welcome UI | compose-ui-designer | 4小时 | - |
| A-6 | 设计 PetSelection UI | compose-ui-designer | 4小时 | A-5 |
| A-7 | 数据库迁移 | android-engineer | 2小时 | A-1 |

### Week 2 任务

| 任务ID | 任务 | 负责角色 | 预计时间 | 依赖 |
|--------|------|----------|----------|------|
| B-1 | 设计 Tutorial UI | compose-ui-designer | 6小时 | - |
| B-2 | 实现 35% 预填逻辑 | android-engineer | 3小时 | A-3 |
| B-3 | 设计 Chest 开启动画 | compose-ui-designer | 4小时 | - |
| B-4 | 实现开宝箱逻辑 | android-engineer | 2小时 | A-3 |
| B-5 | 集成测试 | android-test-engineer | 8小时 | 全部 |
| B-6 | 真机验证 | android-test-engineer | 4小时 | B-5 |

---

## 第七部分：技术风险与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| Room 迁移冲突 | 高 | 中 | 独立新表，不修改现有表 |
| ViewModel 状态复杂度 | 中 | 中 | 使用 sealed class UI 状态 |
| 动画性能问题 | 中 | 低 | Alpha 用简单缩放动画 |
| 导航流程中断 | 高 | 低 | 保持现有导航独立 |

---

## 第八部分：API 接口定义

### 8.1 Repository 接口

```kotlin
interface OnboardingRepository {
    fun getOnboardingStateFlow(): Flow<OnboardingState?>
    suspend fun getOnboardingState(): OnboardingState?
    suspend fun saveOnboardingState(state: OnboardingState)
    suspend fun updateOnboardingState(state: OnboardingState)
}
```

### 8.2 UseCase 接口

```kotlin
// 输入/输出定义
class StartOnboardingUseCase {
    suspend operator fun invoke(): OnboardingState
}

class SelectPetUseCase {
    suspend operator fun invoke(pet: PetType): OnboardingState
}

class GetTutorialWordsUseCase {
    suspend operator fun invoke(): List<TutorialWordConfig>
}

class CompleteTutorialWordUseCase {
    suspend operator fun invoke(stars: Int): OnboardingState
}

class OpenFirstChestUseCase {
    suspend operator fun invoke(): ChestReward
}
```

---

## 第九部分：测试策略

### 单元测试

| 组件 | 测试内容 | 预计测试数 |
|------|----------|------------|
| TutorialWordConfig | 预填计算 | 3 |
| SelectPetUseCase | 状态转换 | 2 |
| CompleteTutorialWordUseCase | 计数更新 | 3 |
| OnboardingViewModel | 流程状态 | 5 |

### 集成测试

```kotlin
// 测试完整 Alpha 流程
@Test
fun testOnboardingAlphaFlow() {
    // 1. Start → Welcome
    viewModel.startOnboarding()
    assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)

    // 2. Select Pet → Tutorial
    viewModel.selectPet(PetType.DOLPHIN)
    assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)

    // 3. Complete 5 words → Chest
    repeat(5) { viewModel.submitAnswer("correct") }
    assertTrue(viewModel.uiState.value is OnboardingUiState.OpeningChest)
}
```

---

## 附录A：文件清单

### 新建文件

```
domain/model/
  OnboardingState.kt
  OnboardingPhase.kt
  OnboardingUiState.kt
  PetType.kt
  TutorialWordConfig.kt
  TutorialQuestion.kt
  ChestReward.kt
  RewardRarity.kt

domain/usecase/
  StartOnboardingUseCase.kt
  SelectPetUseCase.kt
  GetTutorialWordsUseCase.kt
  CompleteTutorialWordUseCase.kt
  OpenFirstChestUseCase.kt

domain/repository/
  OnboardingRepository.kt

data/dao/
  OnboardingStateDao.kt

data/repository/
  OnboardingRepositoryImpl.kt

ui/viewmodel/
  OnboardingViewModel.kt

ui/screens/
  OnboardingWelcomeScreen.kt
  OnboardingPetSelectionScreen.kt
  OnboardingTutorialScreen.kt
  OnboardingChestScreen.kt

test/
  OnboardingViewModelTest.kt
  TutorialWordConfigTest.kt
  SelectPetUseCaseTest.kt
```

### 修改文件

```
di/AppServiceLocator.kt
navigation/SetupNavGraph.kt
data/database/WordDatabase.kt
```

---

**文档状态**: ✅ Alpha 技术方案完成
**预计代码量**: ~2000 行
**预计开发时间**: 10个工作日
