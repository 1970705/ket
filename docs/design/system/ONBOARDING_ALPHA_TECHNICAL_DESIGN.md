# Onboarding Alpha 技术方案设计

**文档版本**: 1.0
**创建日期**: 2026-02-25
**作者**: android-architect (Wordland团队)
**状态**: Alpha 技术设计
**Epic**: #10 - Onboarding Experience
**任务**: #12 - 技术方案设计

---

## 文档变更记录

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| 1.0 | 2026-02-25 | 初始版本，包含测试性改进 | android-architect |

---

## 目录

1. [执行摘要](#执行摘要)
2. [Clean Architecture 分层设计](#clean-architecture-分层设计)
3. [Domain 层设计](#domain-层设计)
4. [Data 层设计](#data-层设计)
5. [UI 层设计](#ui-层设计)
6. [导航流程设计](#导航流程设计)
7. [数据埋点方案](#数据埋点方案)
8. [数据库迁移计划](#数据库迁移计划)
9. [关键技术决策](#关键-技术决策)
10. [风险评估与缓解](#风险评估与缓解)
11. [测试策略](#测试策略)
12. [附录：测试性改进清单](#附录测试性改进清单)

---

## 执行摘要

### Alpha 目标

**Week 1-2 交付物**: 可玩的核心 Onboarding 流程
- 欢迎界面 → 宫物选择 → 教学关卡 → 首个宝箱

### 架构原则

1. **Clean Architecture**: UI → Domain → Data 严格分层
2. **可测试性优先**: 所有关键逻辑支持依赖注入
3. **最小化新代码**: 复用现有组件（如 MatchGameScreen）
4. **Service Locator DI**: 与现有架构一致

### 设计亮点

| 特性 | 说明 | 价值 |
|------|------|------|
| TimeProvider 接口 | 时间逻辑可注入 | 单元测试可控 |
| 首次启动标志 | 明确的 isFirstLaunch 检测 | 可测试的流程 |
| 单词长度约束 | 35%预填仅适用于 ≥3 字母单词 | 避免边界错误 |
| 周边界计算 | 明确周一为周起始 | 行为可预测 |

---

## Clean Architecture 分层设计

### 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ WelcomeScreen     │  │ PetSelectionScreen│  │ TutorialScreen   │  │
│  │                   │  │                   │  │                  │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌──────────────────┐  ┌──────────────────┐                          │
│  │ ChestRewardScreen│  │ SimpleAnalytics   │                          │
│  │                   │  │ (埋点)             │                          │
│  └──────────────────┘  └──────────────────┘                          │
│                           ↓                                           │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │              OnboardingViewModel (StateFlow)                   │   │
│  │  - 管理UI状态                                                  │   │
│  │  - 协调UseCases                                               │   │
│  │  - 处理用户交互                                                │   │
│  └───────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ calls
┌─────────────────────────────────────────────────────────────────────┐
│                            Domain Layer                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ OnboardingState   │  │ TutorialWordConfig│  │ PetType (enum)   │  │
│  │ (model)           │  │ (model)           │  │                  │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ ChestReward       │  │ TimeProvider      │  │ WeekCalculator   │  │
│  │ (sealed class)    │  │ (interface)       │  │ (interface)      │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │                      UseCases                                  │   │
│  │  ┌─────────────────┐  ┌─────────────────┐                     │   │
│  │  │ StartOnboarding │  │ SelectPetUseCase│                     │   │
│  │  │ UseCase         │  │                 │                     │   │
│  │  └─────────────────┘  └─────────────────┘                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐                     │   │
│  │  │ GetTutorialWords│  │ CompleteTutorial│                     │   │
│  │  │ UseCase         │  │ WordUseCase     │                     │   │
│  │  └─────────────────┘  └─────────────────┘                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐                     │   │
│  │  │ OpenFirstChest  │  │ CheckFirstLaunch│                     │   │
│  │  │ UseCase         │  │ UseCase         │                     │   │
│  │  └─────────────────┘  └─────────────────┘                     │   │
│  └───────────────────────────────────────────────────────────────┘   │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │                    Repository Interfaces                       │   │
│  │  - OnboardingRepository                                       │   │
│  │  - TutorialWordsRepository                                    │   │
│  └───────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    ↓ calls
┌─────────────────────────────────────────────────────────────────────┐
│                             Data Layer                               │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ OnboardingStateDao│  │ OnboardingRepo   │  │ WordDatabase     │  │
│  │ (Room)            │  │ Impl             │  │                  │  │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘  │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │                    SharedPreferences                           │   │
│  │  - isFirstLaunch (首次启动标志)                                │   │
│  └───────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 依赖方向

```
UI ──────> Domain ──────> Data
  ↑            ↓              ↓
  └────────────┴──────────────┘
    (所有依赖指向内层)
```

---

## Domain 层设计

### 3.1 核心数据模型

#### 3.1.1 OnboardingState

```kotlin
// File: domain/model/OnboardingState.kt
package com.wordland.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Onboarding 流程状态
 *
 * Alpha 范围: WELCOME → PET_SELECTION → TUTORIAL → FIRST_CHEST
 *
 * @property userId 用户ID (单用户MVP: 固定为"user_001")
 * @property currentPhase 当前阶段
 * @property selectedPet 选择的宠物
 * @property completedTutorialWords 教学关卡完成单词数
 * @property lastOpenedChest 上次开宝箱时间
 * @property totalStars 获得星星总数
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey
    val userId: String,
    val currentPhase: OnboardingPhase,
    val selectedPet: PetType? = null,
    val completedTutorialWords: Int = 0,
    val lastOpenedChest: Long = 0L,
    val totalStars: Int = 0,
    val firstLaunchTime: Long? = null,  // P0改进: 首次启动时间记录
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Onboarding 阶段枚举
 *
 * 流程顺序:
 * NOT_STARTED → WELCOME → PET_SELECTION → TUTORIAL → FIRST_CHEST → COMPLETED
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
 * 宫物类型枚举（Alpha 全部解锁）
 *
 * @property displayName 显示名称
 * @property emoji Emoji 表情
 * @property description 个性描述
 */
enum class PetType {
    DOLPHIN("海豚", "🐬", "活泼聪明，喜欢跳跃"),
    CAT("猫咪", "🐱", "好奇独立，有点傲娇"),
    DOG("小狗", "🐶", "忠诚热情，最爱玩闹"),
    FOX("狐狸", "🦊", "聪明机灵，带着神秘");

    val displayName: String
    val emoji: String
    val description: String

    operator fun component1() = displayName
    operator fun component2() = emoji
    operator fun component3() = description
}
```

#### 3.1.2 TutorialWordConfig (P1改进：单词长度约束)

```kotlin
// File: domain/model/TutorialWordConfig.kt
package com.wordland.domain.model

/**
 * 教学关卡单词配置
 *
 * Alpha 版本: 35%预填，最多3次提示
 *
 * P1改进: 明确单词长度约束，避免边界情况
 */
data class TutorialWordConfig(
    val word: String,
    val translation: String,
    val preFillRatio: Float = 0.35f,
    val minPreFillLetters: Int = 1,
    val hintsAllowed: Int = 3,
    val showFirstLetter: Boolean = true,
    val timeLimit: Int? = null
) {
    companion object {
        /** P1改进: 最小单词长度约束 */
        const val MIN_WORD_LENGTH = 3

        /** P1改进: 最大单词长度约束 */
        const val MAX_WORD_LENGTH = 10
    }

    init {
        // P1改进: 运行时验证单词长度
        require(word.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH) {
            "Tutorial word must be between $MIN_WORD_LENGTH and $MAX_WORD_LENGTH characters, got: ${word.length} for '$word'"
        }
        require(preFillRatio in 0f..1f) {
            "Pre-fill ratio must be between 0 and 1, got: $preFillRatio"
        }
    }

    /**
     * 计算预填字母数量
     *
     * 公式: ceil(wordLength * preFillRatio)，但不小于 minPreFillLetters
     */
    fun calculatePreFillCount(): Int {
        val ratioBased = (word.length * preFillRatio).toInt()
        return maxOf(ratioBased, minPreFillLetters).coerceAtMost(word.length - 1)
    }

    /**
     * 生成预填字母索引
     *
     * 策略: 随机选择，确保至少包含首字母（如果 showFirstLetter = true）
     */
    fun generatePreFilledIndices(): Set<Int> {
        val count = calculatePreFillCount()
        val allIndices = (0 until word.length).toMutableSet()

        // 如果需要显示首字母，确保索引0被包含
        val mustInclude = if (showFirstLetter) setOf(0) else emptySet()

        val remaining = allIndices - mustInclude
        val randomIndices = remaining.shuffled().take(count - mustInclude.size)

        return mustInclude + randomIndices.toSet()
    }

    /**
     * 验证用户答案
     */
    fun isCorrect(answer: String): Boolean {
        return answer.equals(word, ignoreCase = true)
    }
}
```

#### 3.1.3 ChestReward

```kotlin
// File: domain/model/ChestReward.kt
package com.wordland.domain.model

/**
 * 宝箱奖励模型（Alpha 简化版）
 *
 * P2改进: 添加 rarity 概率说明
 */
sealed class ChestReward {
    abstract val rarity: RewardRarity
    abstract val name: String
    abstract val description: String

    /**
     * 宫物表情奖励
     */
    data class PetEmoji(
        val petType: PetType,
        val emoji: String,
        val message: String
    ) : ChestReward() {
        override val rarity = RewardRarity.COMMON
        override val name = "宠物表情"
        override val description = message
    }

    /**
     * 庆祝特效奖励
     */
    data class CelebrationEffect(
        val effectName: String,
        val description: String
    ) : ChestReward() {
        override val rarity = RewardRarity.RARE
        override val name = "庆祝特效"
        override val description = description
    }

    /**
     * 稀有宫物造型
     */
    data class RarePetCostume(
        val petType: PetType,
        val costumeName: String
    ) : ChestReward() {
        override val rarity = RewardRarity.EPIC
        override val name = "稀有造型"
        override val description = "为你的${petType.displayName}解锁专属造型！"
    }
}

/**
 * 奖励稀有度枚举
 *
 * P2改进: 明确掉落概率
 */
enum class RewardRarity(val probability: Float, val colorHex: Long) {
    /** 普通: 50% 掉落率 */
    COMMON(0.50f, 0xFF2196F3),

    /** 稀有: 30% 掉落率 */
    RARE(0.30f, 0xFF9C27B0),

    /** 史诗: 20% 掉落率 */
    EPIC(0.20f, 0xFFFF9800);
}
```

### 3.2 P0-P1 问题解决方案

#### 3.2.1 P0: TimeProvider 接口

```kotlin
// File: domain/time/TimeProvider.kt
package com.wordland.domain.time

/**
 * 时间提供者接口
 *
 * P1改进: 用于单元测试注入，解决时间依赖逻辑
 *
 * 使用场景:
 * - 首次启动检测
 * - 每日登录检测
 * - 周边界计算
 */
interface TimeProvider {
    /**
     * 当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long

    /**
     * 当前日期（YYYY-MM-DD）
     */
    fun currentDate(): LocalDate

    /**
     * 当前周起始时间戳（周一 00:00:00）
     */
    fun weekStartDate(): Long

    /**
     * 当前周数（年内）
     */
    fun weekNumber(): Int

    /**
     * 判断两个时间戳是否在同一天
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean

    /**
     * 计算连续天数
     */
    fun calculateStreak(lastLoginTime: Long, currentDays: Int): Int
}

/**
 * 默认实现（生产环境）
 */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun currentDate(): LocalDate = Clock.System.now().toLocalDateTime(TimeSource.currentSystemDefault()).date

    override fun weekStartDate(): Long {
        val now = currentDate()
        val daysFromMonday = now.dayOfWeek.ordinal  // Monday = 0
        val monday = now.minus(daysFromMonday, DateTimeUnit.Day)
        return kotlinx.datetime.LocalDateTime(monday.year, monday.month, monday.dayOfMonth, 0, 0)
            .toInstant(TimeSource.currentSystemDefault())
            .toEpochMilliseconds()
    }

    override fun weekNumber(): Int {
        val now = currentDate()
        return now.weekOfYear(WeekDay.MONDAY).weekNumber
    }

    override fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = Instant.fromEpochMilliseconds(timestamp1).toLocalDateTime(TimeSource.currentSystemDefault()).date
        val day2 = Instant.fromEpochMilliseconds(timestamp2).toLocalDateTime(TimeSource.currentSystemDefault()).date
        return day1 == day2
    }

    override fun calculateStreak(lastLoginTime: Long, currentDays: Int): Int {
        val now = currentTimeMillis()
        val dayDiff = ((now - lastLoginTime) / (24 * 60 * 60 * 1000)).toInt()

        return when {
            dayDiff <= 1 -> currentDays + 1    // 连续
            dayDiff <= 2 -> currentDays        // 隔天（不算连续，但也不清零）
            else -> 1                          // 断开，重新开始
        }
    }
}

/**
 * 测试用实现
 */
class FixedTimeProvider(
    private val fixedTime: Long = 0L,
    private val fixedDate: LocalDate = LocalDate(2026, 2, 25)
) : TimeProvider {
    override fun currentTimeMillis(): Long = fixedTime
    override fun currentDate(): LocalDate = fixedDate

    override fun weekStartDate(): Long {
        val daysFromMonday = fixedDate.dayOfWeek.ordinal
        val monday = fixedDate.minus(daysFromMonday, DateTimeUnit.Day)
        return kotlinx.datetime.LocalDateTime(monday.year, monday.month, monday.dayOfMonth, 0, 0)
            .toInstant(TimeSource.currentSystemDefault())
            .toEpochMilliseconds()
    }

    override fun weekNumber(): Int = fixedDate.weekOfYear(WeekDay.MONDAY).weekNumber

    override fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean = true

    override fun calculateStreak(lastLoginTime: Long, currentDays: Int): Int = currentDays + 1
}
```

#### 3.2.2 P0: 首次启动检测

```kotlin
// File: domain/usecase/CheckFirstLaunchUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.time.TimeProvider
import com.wordland.domain.repository.FirstLaunchRepository

/**
 * 检查是否首次启动
 *
 * P0改进: 明确的首次启动检测逻辑
 */
class CheckFirstLaunchUseCase(
    private val firstLaunchRepository: FirstLaunchRepository,
    private val timeProvider: TimeProvider
) {
    /**
     * 检查是否首次启动
     *
     * @return true 如果是首次启动
     */
    suspend operator fun invoke(): Boolean {
        return firstLaunchRepository.isFirstLaunch()
    }

    /**
     * 标记首次启动已完成
     */
    suspend fun markFirstLaunchCompleted() {
        firstLaunchRepository.setFirstLaunch(false)
    }

    /**
     * 获取首次启动时间
     */
    suspend fun getFirstLaunchTime(): Long? {
        return firstLaunchRepository.getFirstLaunchTime()
    }
}
```

```kotlin
// File: domain/repository/FirstLaunchRepository.kt
package com.wordland.domain.repository

interface FirstLaunchRepository {
    suspend fun isFirstLaunch(): Boolean
    suspend fun setFirstLaunch(isFirst: Boolean)
    suspend fun getFirstLaunchTime(): Long?
}
```

```kotlin
// File: data/repository/FirstLaunchRepositoryImpl.kt
package com.wordland.data.repository

import android.content.SharedPreferences
import com.wordland.domain.repository.FirstLaunchRepository
import com.wordland.domain.time.TimeProvider

class FirstLaunchRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val timeProvider: TimeProvider
) : FirstLaunchRepository {

    companion object Keys {
        const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
    }

    override suspend fun isFirstLaunch(): Boolean {
        val isFirst = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        // 首次调用时记录时间
        if (isFirst && sharedPreferences.getLong(KEY_FIRST_LAUNCH_TIME, -1L) == -1L) {
            setFirstLaunchTime(timeProvider.currentTimeMillis())
        }
        return isFirst
    }

    override suspend fun setFirstLaunch(isFirst: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, isFirst).apply()
        if (!isFirst) {
            setFirstLaunchTime(timeProvider.currentTimeMillis())
        }
    }

    override suspend fun getFirstLaunchTime(): Long? {
        val time = sharedPreferences.getLong(KEY_FIRST_LAUNCH_TIME, -1L)
        return if (time == -1L) null else time
    }

    private fun setFirstLaunchTime(time: Long) {
        sharedPreferences.edit().putLong(KEY_FIRST_LAUNCH_TIME, time).apply()
    }
}
```

#### 3.2.3 P1: WeekCalculator 周边界计算

```kotlin
// File: domain/time/WeekCalculator.kt
package com.wordland.domain.time

/**
 * 周计算器接口
 *
 * P1改进: 明确周边界规则
 *
 * 规则:
 * - 周一为每周的第一天
 * - 周起始时间为周一 00:00:00
 * - 周数基于 ISO 8601 标准
 */
interface WeekCalculator {
    /**
     * 获取指定时间戳所在周的起始时间
     *
     * @param timestamp 时间戳
     * @return 周一 00:00:00 的时间戳
     */
    fun getWeekStartDate(timestamp: Long): Long

    /**
     * 获取当前周的起始时间
     */
    fun getCurrentWeekStartDate(): Long

    /**
     * 获取指定时间戳所在的周数
     *
     * @param timestamp 时间戳
     * @return 周数 (1-53)
     */
    fun getWeekNumber(timestamp: Long): Int

    /**
     * 判断两个时间戳是否在同一周
     */
    fun isSameWeek(timestamp1: Long, timestamp2: Long): Boolean

    /**
     * 获取本周序号（自首次启动以来的周数）
     */
    fun getWeeksSince(firstLaunchTime: Long): Int
}

/**
 * 默认实现
 */
class DefaultWeekCalculator(
    private val timeProvider: TimeProvider
) : WeekCalculator {

    override fun getWeekStartDate(timestamp: Long): Long {
        val dateTime = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeSource.currentSystemDefault())
        val daysFromMonday = dateTime.dayOfWeek.ordinal
        val monday = dateTime.date.minus(daysFromMonday, DateTimeUnit.Day)
        return kotlinx.datetime.LocalDateTime(monday.year, monday.month, monday.dayOfMonth, 0, 0)
            .toInstant(TimeSource.currentSystemDefault())
            .toEpochMilliseconds()
    }

    override fun getCurrentWeekStartDate(): Long {
        return timeProvider.weekStartDate()
    }

    override fun getWeekNumber(timestamp: Long): Long {
        val dateTime = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeSource.currentSystemDefault())
        return dateTime.date.weekOfYear(WeekDay.MONDAY).weekNumber.toLong()
    }

    override fun isSameWeek(timestamp1: Long, timestamp2: Long): Boolean {
        return getWeekStartDate(timestamp1) == getWeekStartDate(timestamp2)
    }

    override fun getWeeksSince(firstLaunchTime: Long): Int {
        val now = timeProvider.currentTimeMillis()
        val weekDiff = ((now - firstLaunchTime) / (7 * 24 * 60 * 60 * 1000)).toInt()
        return maxOf(1, weekDiff + 1)
    }
}
```

#### 3.2.4 P2: 临界奖励阈值计算

```kotlin
// File: domain/model/CriticalRewardState.kt
package com.wordland.domain.model

/**
 * 临界奖励状态
 *
 * P2改进: 明确距离计算公式
 */
data class CriticalRewardState(
    val rewardType: CriticalTrigger,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardContent: RewardContent,
    val isClaimed: Boolean = false
) {
    /**
     * P2改进: 距离奖励还差多少
     *
     * 公式: targetProgress - currentProgress
     *
     * 示例:
     * - (5, 6) → "还差 1 个单词"
     * - (4, 6) → "还差 2 个单词"
     * - (6, 6) → "已完成"
     */
    val distanceToReward: Int
        get() = (targetProgress - currentProgress).coerceAtLeast(0)

    /**
     * 是否处于临界状态（距离 <= 1）
     */
    val isCritical: Boolean
        get() = distanceToReward <= 1 && !isClaimed

    /**
     * 进度百分比
     */
    val progressPercentage: Float
        get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceAtMost(1f)
}

/**
 * 临界奖励触发类型
 */
enum class CriticalTrigger {
    ONE_WORD_LEFT,        // "还差1个单词解锁新宠物造型！"
    TWO_WORDS_LEFT,       // "还差2个单词解锁新游戏模式！"
    THREE_STARS_LEFT,     // "还差1颗星获得三星奖励！"
    COMBO_MILESTONE,      // "再连击3次解锁特殊动画！"
    SESSION_COMPLETE      // "完成今日学习获得神秘礼物！"
}

/**
 * 奖励内容
 */
sealed class RewardContent {
    data class PetCostume(val costumeName: String) : RewardContent()
    data class GameMode(val modeName: String) : RewardContent()
    data class SpecialAnimation(val animationName: String) : RewardContent()
    data class MysteryGift(val description: String) : RewardContent()
}
```

### 3.3 UseCase 定义

#### 3.3.1 StartOnboardingUseCase

```kotlin
// File: domain/usecase/StartOnboardingUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository

/**
 * 启动 Onboarding 流程
 */
class StartOnboardingUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(): OnboardingState {
        val existing = onboardingRepository.getOnboardingState()

        return if (existing == null) {
            // 首次启动，创建初始状态
            val initialState = OnboardingState(
                userId = "user_001",
                currentPhase = OnboardingPhase.WELCOME,
                selectedPet = null
            )
            onboardingRepository.saveOnboardingState(initialState)
            initialState
        } else {
            // 恢复现有状态
            existing
        }
    }
}
```

#### 3.3.2 SelectPetUseCase

```kotlin
// File: domain/usecase/SelectPetUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.PetType
import com.wordland.domain.repository.OnboardingRepository

/**
 * 选择宠物
 */
class SelectPetUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(pet: PetType): OnboardingState {
        val state = onboardingRepository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        val updated = state.copy(
            selectedPet = pet,
            currentPhase = OnboardingPhase.TUTORIAL,
            updatedAt = System.currentTimeMillis()
        )
        onboardingRepository.saveOnboardingState(updated)
        return updated
    }
}
```

#### 3.3.3 GetTutorialWordsUseCase

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
    companion object {
        const val TUTORIAL_WORD_COUNT = 5
    }

    suspend operator fun invoke(): List<TutorialWordConfig> {
        // 获取最简单的5个单词
        val simpleWords = wordRepository.getSimplestWords(count = TUTORIAL_WORD_COUNT)

        return simpleWords.map { word ->
            TutorialWordConfig(
                word = word.english,
                translation = word.chinese
            )
        }
    }
}
```

#### 3.3.4 CompleteTutorialWordUseCase

```kotlin
// File: domain/usecase/CompleteTutorialWordUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.repository.OnboardingRepository

/**
 * 完成教学关卡单词
 */
class CompleteTutorialWordUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    companion object {
        const val TARGET_WORDS = 5
    }

    suspend operator fun invoke(stars: Int): OnboardingState {
        val state = onboardingRepository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        val newCount = state.completedTutorialWords + 1
        val newPhase = if (newCount >= TARGET_WORDS) {
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
        onboardingRepository.saveOnboardingState(updated)
        return updated
    }
}
```

#### 3.3.5 OpenFirstChestUseCase

```kotlin
// File: domain/usecase/OpenFirstChestUseCase.kt
package com.wordland.domain.usecase

import com.wordland.domain.model.ChestReward
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.RewardRarity
import com.wordland.domain.repository.OnboardingRepository
import kotlin.random.Random

/**
 * 开启首个宝箱
 */
class OpenFirstChestUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(): ChestReward {
        val state = onboardingRepository.getOnboardingState()
            ?: throw IllegalStateException("Onboarding not started")

        val reward = generateReward(state.selectedPet!!)

        val updated = state.copy(
            currentPhase = OnboardingPhase.COMPLETED,
            lastOpenedChest = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        onboardingRepository.saveOnboardingState(updated)

        return reward
    }

    private fun generateReward(pet: PetType): ChestReward {
        val rand = Random.nextInt(100)
        return when {
            rand < RewardRarity.COMMON.probability * 100 -> ChestReward.PetEmoji(
                pet = pet,
                emoji = "🎉",
                message = "你获得了一个可爱的表情！"
            )
            rand < (RewardRarity.COMMON.probability + RewardRarity.RARE.probability) * 100 -> ChestReward.CelebrationEffect(
                effectName = "彩带",
                description = "庆祝特效解锁！"
            )
            else -> ChestReward.RarePetCostume(
                petType = pet,
                costumeName = "墨镜造型"
            )
        }
    }
}
```

---

## Data 层设计

### 4.1 DAO

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

    @Query("UPDATE onboarding_state SET currentPhase = :phase, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updatePhase(userId: String, phase: OnboardingPhase, updatedAt: Long)
}
```

### 4.2 Repository 实现

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

### 4.3 数据库迁移

```kotlin
// File: data/database/WordDatabase.kt (添加迁移)
package com.wordland.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Onboarding 表迁移
 *
 * 版本: N → N+1 (N 为当前数据库版本)
 */
val MIGRATION_N_ONBOARDING = object : Migration(N, N + 1) {
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
                firstLaunchTime INTEGER,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        // 创建索引以提高查询性能
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_onboarding_state_phase ON onboarding_state(currentPhase)"
        )
    }
}

// 注意: N 需要替换为实际的当前数据库版本号
// 例如: 如果当前版本是 6，则使用 MIGRATION_6_7
```

---

## UI 层设计

### 5.1 屏幕列表

| 屏幕名称 | 文件路径 | 功能描述 |
|----------|----------|----------|
| WelcomeScreen | ui/screens/onboarding/OnboardingWelcomeScreen.kt | 欢迎界面，显示开始按钮 |
| PetSelectionScreen | ui/screens/onboarding/OnboardingPetSelectionScreen.kt | 宫物选择，4个宫物全部解锁 |
| TutorialScreen | ui/screens/onboarding/OnboardingTutorialScreen.kt | 教学关卡，5个单词 |
| ChestRewardScreen | ui/screens/onboarding/OnboardingChestRewardScreen.kt | 宝箱奖励展示 |

### 5.2 UI 状态模型

```kotlin
// File: domain/model/OnboardingUiState.kt
package com.wordland.domain.model

/**
 * Onboarding UI 状态
 */
sealed class OnboardingUiState {
    object Idle : OnboardingUiState()

    data class Welcome(
        val showStartButton: Boolean = true
    ) : OnboardingUiState()

    data class PetSelection(
        val availablePets: List<PetType> = PetType.entries.toList(),
        val selectedPet: PetType? = null
    ) : OnboardingUiState()

    data class Tutorial(
        val currentWordIndex: Int,
        val totalWords: Int = 5,
        val question: TutorialQuestion,
        val progress: Float,
        val stars: Int = 0
    ) : OnboardingUiState()

    data class OpeningChest(
        val reward: ChestReward,
        val isAnimating: Boolean = true
    ) : OnboardingUiState()

    data class Completed(
        val pet: PetType,
        val wordsLearned: Int,
        val stars: Int
    ) : OnboardingUiState()

    data class Error(
        val message: String,
        val retry: (() -> Unit)? = null
    ) : OnboardingUiState()
}

/**
 * 教学关卡问题
 */
data class TutorialQuestion(
    val word: String,
    val translation: String,
    val preFilledLetters: Set<Int>,
    val hintsRemaining: Int,
    val showHint: Boolean = false
)
```

### 5.3 ViewModel

```kotlin
// File: ui/viewmodel/OnboardingViewModel.kt
package com.wordland.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.*
import com.wordland.domain.usecase.*
import com.wordland.domain.time.TimeProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Onboarding ViewModel
 *
 * 管理整个 Onboarding 流程的状态
 */
class OnboardingViewModel(
    private val startOnboarding: StartOnboardingUseCase,
    private val checkFirstLaunch: CheckFirstLaunchUseCase,
    private val selectPet: SelectPetUseCase,
    private val getTutorialWords: GetTutorialWordsUseCase,
    private val completeTutorialWord: CompleteTutorialWordUseCase,
    private val openFirstChest: OpenFirstChestUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val currentOnboardingState = MutableStateFlow<OnboardingState?>(null)
    private val tutorialWords = mutableListOf<TutorialWordConfig>()
    private val answers = mutableMapOf<Int, String>()

    /**
     * 启动 Onboarding
     */
    fun startOnboarding() {
        viewModelScope.launch {
            try {
                val isFirst = checkFirstLaunch()

                val state = startOnboarding()
                currentOnboardingState.value = state

                when (state.currentPhase) {
                    OnboardingPhase.NOT_STARTED, OnboardingPhase.WELCOME -> {
                        _uiState.value = OnboardingUiState.Welcome(showStartButton = isFirst)
                    }
                    OnboardingPhase.PET_SELECTION -> {
                        _uiState.value = OnboardingUiState.PetSelection()
                    }
                    OnboardingPhase.TUTORIAL -> {
                        loadTutorialWords()
                    }
                    OnboardingPhase.FIRST_CHEST -> {
                        openChest()
                    }
                    OnboardingPhase.COMPLETED -> {
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
     * 选择宫物
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
            progress = state.completedTutorialWords.toFloat() / 5f,
            stars = state.totalStars
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

    /**
     * 标记 Onboarding 完成
     */
    fun markCompleted() {
        viewModelScope.launch {
            try {
                checkFirstLaunch.markFirstLaunchCompleted()
            } catch (e: Exception) {
                // 非关键错误，忽略
            }
        }
    }
}
```

---

## 导航流程设计

### 6.1 导航路由定义

```kotlin
// File: navigation/OnboardingRoute.kt
package com.wordland.navigation

/**
 * Onboarding 路由定义
 */
sealed class OnboardingRoute(val route: String) {
    object Welcome : OnboardingRoute("onboarding/welcome")
    object PetSelection : OnboardingRoute("onboarding/pet_selection")
    object Tutorial : OnboardingRoute("onboarding/tutorial")
    object Chest : OnboardingRoute("onboarding/chest")

    /**
     * 构建欢迎路由
     */
    fun welcome(): String = Welcome.route

    /**
     * 构建宫物选择路由
     */
    fun petSelection(): String = PetSelection.route

    /**
     * 构建教学路由
     */
    fun tutorial(): String = Tutorial.route

    /**
     * 构建宝箱路由
     */
    fun chest(): String = Chest.route
}
```

### 6.2 导航图配置

```kotlin
// File: navigation/SetupNavGraph.kt (添加 Onboarding 路由)

// 在现有 NavGraph 中添加

// 欢迎界面
composable(
    route = OnboardingRoute.Welcome.route,
    deepLinks = listOf(navDeepLink { uriPattern = "wordland://onboarding" })
) {
    OnboardingWelcomeScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onStart = { navController.navigate(OnboardingRoute.PetSelection.route) }
    )
}

// 宫物选择
composable(route = OnboardingRoute.PetSelection.route) {
    OnboardingPetSelectionScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onPetSelected = { pet ->
            navController.navigate(OnboardingRoute.Tutorial.route)
        }
    )
}

// 教学关卡
composable(route = OnboardingRoute.Tutorial.route) {
    OnboardingTutorialScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onComplete = { navController.navigate(OnboardingRoute.Chest.route) }
    )
}

// 宝箱奖励
composable(route = OnboardingRoute.Chest.route) {
    OnboardingChestRewardScreen(
        viewModel = viewModel(factory = AppServiceLocator.provideOnboardingViewModelFactory()),
        onDismiss = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } }
    )
}
```

### 6.3 首次启动检测流程

```kotlin
// File: MainActivity.kt (修改)

@Composable
fun WordlandApp() {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = viewModel(
        factory = AppServiceLocator.provideOnboardingViewModelFactory()
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startOnboarding()
    }

    // 根据状态决定导航
    val startDestination = when (uiState) {
        is OnboardingUiState.Welcome -> OnboardingRoute.Welcome.route
        is OnboardingUiState.PetSelection -> OnboardingRoute.PetSelection.route
        is OnboardingUiState.Tutorial -> OnboardingRoute.Tutorial.route
        else -> "home"  // 已完成或出错，进入主页
    }

    SetupNavGraph(
        navController = navController,
        startDestination = startDestination
    )
}
```

---

## 数据埋点方案

### 7.1 SimpleAnalytics 设计

```kotlin
// File: analytics/SimpleAnalytics.kt
package com.wordland.analytics

import android.content.Context
import android.util.Log
import com.wordland.domain.model.OnboardingEvent

/**
 * 简单埋点系统 - MVP版本
 *
 * 用于 Onboarding 流程追踪，不依赖 Firebase
 */
class SimpleAnalytics(context: Context) {

    private val preferences = context.getSharedPreferences("analytics", Context.MODE_PRIVATE)
    private val eventQueue = mutableListOf<StoredEvent>()

    /**
     * 记录事件
     */
    fun logEvent(event: OnboardingEvent, params: Map<String, Any> = emptyMap()) {
        val timestamp = System.currentTimeMillis()

        // Logcat 输出（开发调试用）
        Log.d("OnboardingAnalytics", "${event.name}: $params")

        // 保存到本地
        val storedEvent = StoredEvent(
            id = generateEventId(),
            timestamp = timestamp,
            event = event,
            params = params
        )
        eventQueue.add(storedEvent)
        saveEvent(storedEvent)

        // 可选：发送到远程服务器（Beta 版本）
    }

    /**
     * 获取所有事件
     */
    fun getEvents(): List<StoredEvent> {
        return eventQueue.toList()
    }

    /**
     * 获取特定事件
     */
    fun getEvents(eventType: OnboardingEvent): List<StoredEvent> {
        return eventQueue.filter { it.event == eventType }
    }

    /**
     * 清除所有事件
     */
    fun clearEvents() {
        eventQueue.clear()
        preferences.edit().clear().apply()
    }

    private fun saveEvent(event: StoredEvent) {
        val key = "event_${event.id}"
        val value = "${event.timestamp}|${event.event.name}|${serializeParams(event.params)}"
        preferences.edit().putString(key, value).apply()
    }

    private fun generateEventId(): String {
        return "${System.currentTimeMillis()}_${(0..1000).random()}"
    }

    private fun serializeParams(params: Map<String, Any>): String {
        return params.entries.joinToString(",") { "${it.key}=${it.value}" }
    }
}

/**
 * 存储的事件
 */
data class StoredEvent(
    val id: String,
    val timestamp: Long,
    val event: OnboardingEvent,
    val params: Map<String, Any>
)

/**
 * Onboarding 事件枚举
 */
enum class OnboardingEvent {
    APP_LAUNCHED,           // 应用启动
    WELCOME_SHOWN,          // 欢迎界面显示
    WELCOME_STARTED,        // 点击开始按钮
    PET_SELECTION_SHOWN,    // 宫物选择显示
    PET_SELECTED,           // 选择宫物
    TUTORIAL_STARTED,       // 教学关卡开始
    TUTORIAL_WORD_SHOWN,    // 单词显示
    TUTORIAL_WORD_COMPLETED,// 单词完成
    HINT_USED,              // 使用提示
    HINT_EXHAUSTED,         // 提示用完
    TUTORIAL_COMPLETED,     // 教学关卡完成
    CHEST_OPENED,           // 开宝箱
    ONBOARDING_COMPLETED,   // Onboarding 完成
    SESSION_COMPLETED       // 会话完成
}
```

---

## 数据库迁移计划

### 8.1 迁移步骤

| 阶段 | 版本 | 操作 | 影响 |
|------|------|------|------|
| 1 | N → N+1 | 创建 onboarding_state 表 | 无破坏性 |
| 2 | N+1 → N+2 | 添加 SharedPreferences 键 | 无破坏性 |
| 3 | N+2 → N+3 | (Beta) 添加 weekly_report 表 | 无破坏性 |

### 8.2 回滚策略

```kotlin
/**
 * 回滚迁移（如果需要）
 */
val ROLLBACK_N_ONBOARDING = object : Migration(N + 1, N) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS onboarding_state")
        database.execSQL("DROP INDEX IF EXISTS index_onboarding_state_phase")
    }
}
```

---

## 关键技术决策

### 9.1 决策记录 (ADR)

| ID | 决策 | 原因 | 替代方案 |
|----|------|------|----------|
| ADR-001 | 使用 TimeProvider 接口 | 可测试性 | 直接使用 System.currentTimeMillis() |
| ADR-002 | SharedPreferences 存储首次启动标志 | 简单、持久 | Room 表存储 |
| ADR-003 | 35% 预填至少1个字母 | 避免空输入 | 精确计算不设最小值 |
| ADR-004 | 单词长度限制 3-10 | 避免边界问题 | 不限制长度 |
| ADR-005 | 周一为周起始 | ISO 8601 标准 | 周日为起始 |
| ADR-006 | Sealed Class 表示 UI 状态 | 类型安全 | enum + 数据字段 |
| ADR-007 | 复用 BubbleTile 组件 | 代码复用 | 新建组件 |

### 9.2 技术栈选择

| 组件 | 选择 | 原因 |
|------|------|------|
| UI 框架 | Jetpack Compose | 现有架构 |
| 状态管理 | StateFlow | 现有架构 |
| 依赖注入 | Service Locator | 现有架构 |
| 数据库 | Room | 现有架构 |
| 时间处理 | kotlinx-datetime | 类型安全 |

---

## 风险评估与缓解

### 10.1 风险矩阵

| 风险 | 概率 | 影响 | 级别 | 缓解措施 |
|------|------|------|------|----------|
| Room 迁移冲突 | 中 | 高 | P1 | 独立新表 |
| 时间相关测试失败 | 低 | 中 | P2 | TimeProvider 注入 |
| 35% 预填边界错误 | 低 | 中 | P2 | 单词长度约束 |
| 导航流程混乱 | 中 | 中 | P1 | 状态机管理 |
| 宫物动画性能 | 低 | 低 | P3 | Alpha 用简单动画 |

### 10.2 缓解策略

**P1: Room 迁移冲突**
- 使用独立表名（onboarding_state）
- 避免修改现有表结构
- 充分测试迁移脚本

**P2: 时间相关测试**
- TimeProvider 接口注入
- FixedTimeProvider 用于测试
- 避免直接使用 System.currentTimeMillis()

**P2: 35% 预填边界**
- 最小单词长度约束（≥3）
- 最大单词长度约束（≤10）
- 运行时验证

---

## 测试策略

### 11.1 单元测试

| 组件 | 测试内容 | 测试数量 | 优先级 |
|------|----------|----------|--------|
| TutorialWordConfig | 预填计算、边界验证 | 5 | P0 |
| TimeProvider | 时间计算、周边界 | 4 | P1 |
| WeekCalculator | 周计算 | 3 | P1 |
| SelectPetUseCase | 状态转换 | 2 | P0 |
| CompleteTutorialWordUseCase | 计数更新 | 3 | P0 |
| CriticalRewardState | 距离计算 | 2 | P2 |
| OnboardingViewModel | 流程状态 | 5 | P1 |

### 11.2 集成测试

```kotlin
// 测试完整 Alpha 流程
@Test
fun testOnboardingAlphaFlow() {
    // Given
    val viewModel = createViewModel()
    val testTimeProvider = FixedTimeProvider()

    // When: 启动
    viewModel.startOnboarding()
    assertTrue(viewModel.uiState.value is OnboardingUiState.Welcome)

    // When: 选择宫物
    viewModel.selectPet(PetType.DOLPHIN)
    assertTrue(viewModel.uiState.value is OnboardingUiState.Tutorial)

    // When: 完成5个单词
    repeat(5) { viewModel.submitAnswer("correct") }

    // Then: 进入宝箱阶段
    assertTrue(viewModel.uiState.value is OnboardingUiState.OpeningChest)
}
```

### 11.3 UI 测试

```kotlin
// Compose UI 测试
@Test
fun testWelcomeScreenStartButton() {
    composeTestRule.setContent {
        OnboardingWelcomeScreen(
            viewModel = viewModel,
            onStart = {}
        )
    }

    composeTestRule
        .onNodeWithText("开始探险")
        .assertIsDisplayed()
        .performClick()

    // 验证导航
    verify(navigationController).navigate("onboarding/pet_selection")
}
```

---

## 附录：测试性改进清单

### A.1 P0 问题（已解决）

| 问题 | 解决方案 | 位置 |
|------|----------|------|
| 首次启动检测未明确 | FirstLaunchRepository + SharedPreferences | domain/repository/ |
| isFirstLaunch 标志 | CheckFirstLaunchUseCase | domain/usecase/ |

### A.2 P1 问题（已解决）

| 问题 | 解决方案 | 位置 |
|------|----------|------|
| 时间依赖逻辑 | TimeProvider 接口 + FixedTimeProvider | domain/time/ |
| 周边界计算 | WeekCalculator 接口 + 明确规则 | domain/time/ |
| 35% 预填边界 | 单词长度约束 (3-10) + 运行时验证 | domain/model/TutorialWordConfig.kt |

### A.3 P2 问题（已解决）

| 问题 | 解决方案 | 位置 |
|------|----------|------|
| 临界奖励阈值 | 明确公式: targetProgress - currentProgress | domain/model/CriticalRewardState.kt |

---

**文档状态**: ✅ 技术方案设计完成
**预计代码量**: ~2500 行
**预计开发时间**: 10个工作日 (Week 1-2)
**验收标准**: android-engineer-3 可直接根据本方案开始实施
