# 成就系统需求文档

**文档版本**: v1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**状态**: 详细需求定义
**优先级**: P1

---

## 📋 目录

1. [概述](#1-概述)
2. [成就分类](#2-成就分类)
3. [成就层级](#3-成就层级)
4. [奖励系统](#4-奖励系统)
5. [触发机制](#5-触发机制)
6. [数据模型](#6-数据模型)
7. [UI/UX需求](#7-uiux需求)
8. [实施计划](#8-实施计划)

---

## 1. 概述

### 1.1 成就系统目标

成就系统是 Wordland 的核心游戏化机制，旨在：
- **激励学习**：通过目标设定激励持续学习
- **增强粘性**：提供长期目标增加用户留存
- **庆祝进步**：认可每个学习里程碑
- **多样化玩法**：支持不同游戏模式和用户偏好

### 1.2 设计原则

| 原则 | 描述 | 实现 |
|------|------|------|
| **可达性** | 混合简单和挑战性成就 | 40%简单(⭐)，30%中等(⭐⭐⭐)，30%困难(⭐⭐⭐⭐⭐) |
| **清晰性** | 进度可见，目标明确 | 每个成就显示进度条和完成条件 |
| **正向激励** | 庆祝努力而非仅结果 | 参与即有奖励，失败也有进度 |
| **多样性** | 不同类型满足不同玩家 | 6大类成就覆盖各种游戏风格 |

### 1.3 成就数量分布

| 类别 | 数量 | 占比 | 难度分布 |
|------|------|------|---------|
| **Progress（进度）** | 12 | 24% | ⭐×4, ⭐⭐×3, ⭐⭐⭐×3, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| **Performance（表现）** | 10 | 20% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×3, ⭐⭐⭐⭐×2, ⭐⭐⭐⭐⭐×1 |
| **Combo（连击）** | 6 | 12% | ⭐×1, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1 |
| **Streak（连续）** | 6 | 12% | ⭐×1, ⭐⭐×1, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| **Quick Judge（判断）** | 8 | 16% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| **Social（社交）** | 8 | 16% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| **总计** | **50** | **100%** | - |

---

## 2. 成就分类

### 2.1 Progress Achievements（进度成就）- 12个

进度成就奖励学习里程碑，适合所有玩家类型。

#### ⭐ Very Easy（4个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `first_steps` | First Steps | 🌟 | 完成第一个关卡 | 完成1个关卡（1+星） | 10 ⭐ |
| `word_collector` | Word Collector | 📝 | 学习5个单词 | 掌握5个单词（记忆≥80） | 15 ⭐ |
| `first_island` | First Island | 🏝️ | 解锁第一个岛屿 | 完成Look Island任意3关 | 20 ⭐ |
| `streak_starter` | Streak Starter | 🔥 | 连续学习3天 | 3天连续学习（每天≥1词） | 15 ⭐ |

#### ⭐⭐ Easy（3个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `page_turner` | Page Turner | 📚 | 完成5个关卡 | 完成5个不同关卡（2+星） | 25 ⭐ |
| `vocabulary_builder` | Vocabulary Builder | 📖 | 学习20个单词 | 掌握20个单词 | 30 ⭐ |
| `island_explorer` | Island Explorer | 🗺️ | 探索2个岛屿 | 解锁2个不同岛屿 | 35 ⭐ |

#### ⭐⭐⭐ Medium（3个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `word_scholar` | Word Scholar | 🎓 | 学习30个单词 | 掌握30个单词 | 50 ⭐ + 称号 |
| `level_master` | Level Master | 🏅 | 完美通关任意关卡 | 任意关卡3星通关 | 40 ⭐ |
| `island_half_master` | Half Island Master | ⚓ | 岛屿50%进度 | 任意岛屿50%精通度 | 45 ⭐ |

#### ⭐⭐⭐⭐ Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `word_hunter` | Word Hunter | 🏆 | 学习60个单词 | 掌握60个单词 | 100 ⭐ + 徽章 |

#### ⭐⭐⭐⭐⭐ Very Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `island_master` | Island Master | 👑 | 完美精通一个岛屿 | 任意岛屿100%精通度 | 150 ⭐ + 宠物 |

---

### 2.2 Performance Achievements（表现成就）- 10个

表现成就奖励卓越表现，适合追求完美的玩家。

#### ⭐ Very Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `getting_started` | Getting Started | 🚀 | 完成第一关无错误 | 任意关卡0错误完成 | 15 ⭐ |
| `first_perfect` | First Perfect | ✨ | 首次3星通关 | 任意关卡获得3星 | 20 ⭐ |

#### ⭐⭐ Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `consistent_learner` | Consistent Learner | 📊 | 5关均2+星 | 连续5个关卡2星以上 | 25 ⭐ |
| `speed_learner` | Speed Learner | ⏱️ | 快速完成关卡 | 5分钟内完成关卡 | 20 ⭐ |

#### ⭐⭐⭐ Medium（3个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `perfectionist` | Perfectionist | 💎 | 完美关卡 | 所有单词3星的关卡 | 30 ⭐ |
| `sharpshooter` | Sharpshooter | 🎯 | 10连对 | 10个连续正确答案（无提示） | 25 ⭐ |
| `efficiency_expert` | Efficiency Expert | ⚡ | 高效学习 | 单次学习掌握10个单词 | 35 ⭐ |

#### ⭐⭐⭐⭐ Hard（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `memory_master` | Memory Master | 🧠 | 记忆大师 | 10个单词记忆强度100 | 40 ⭐ + 称号 |
| `triple_threat` | Triple Threat | 🎰 | 三连完美 | 连续3个关卡3星 | 50 ⭐ |

#### ⭐⭐⭐⭐⭐ Very Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `perfect_island` | Perfect Island | 💯 | 完美岛屿 | 整个岛屿所有关卡3星 | 200 ⭐ + 宠物 + 称号 |

---

### 2.3 Combo Achievements（连击成就）- 6个

连击成就奖励连续正确表现，鼓励专注和准确。

#### ⭐ Very Easy（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `warm_up` | Warm Up | 🔥 | 首次连击 | 达成3连击 | 15 ⭐ |

#### ⭐⭐ Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `combo_starter` | Combo Starter | 🔥🔥 | 小连击 | 达成5连击 | 25 ⭐ |
| `steady_hands` | Steady Hands | ✋ | 稳定发挥 | 单关达成2次5连击 | 20 ⭐ |

#### ⭐⭐⭐ Medium（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `combo_master` | Combo Master | 🔥🔥🔥 | 连击大师 | 达成10连击 | 50 ⭐ + 称号 |
| `combo_keeper` | Combo Keeper | 🛡️ | 连击保持者 | 3关均达成8+连击 | 40 ⭐ |

#### ⭐⭐⭐⭐ Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `unstoppable` | Unstoppable | ⚡ | 势不可挡 | 达成20连击 | 80 ⭐ + 徽章 |

---

### 2.4 Streak Achievements（连续成就）- 6个

连续成就奖励学习习惯养成，鼓励每日学习。

#### ⭐ Very Easy（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `first_week` | First Week | 📅 | 首周连续 | 连续3天学习 | 15 ⭐ |

#### ⭐⭐ Easy（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `week_warrior_jr` | Week Warrior Jr | ⚔️ | 小小战士 | 连续7天学习 | 30 ⭐ |

#### ⭐⭐⭐ Medium（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `dedicated_student` | Dedicated Student | 🎓 | 专注学生 | 连续14天学习 | 50 ⭐ + 徽章 |
| `monthly_learner` | Monthly Learner | 📆 | 月度学习者 | 连续30天学习 | 70 ⭐ |

#### ⭐⭐⭐⭐ Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `loyalty_keeper` | Loyalty Keeper | 💎 | 忠诚守护者 | 连续60天学习 | 100 ⭐ + 称号 |

#### ⭐⭐⭐⭐⭐ Very Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `legendary_learner` | Legendary Learner | 👑 | 传奇学习者 | 连续100天学习 | 200 ⭐ + 宠物 + 称号 |

---

### 2.5 Quick Judge Achievements（判断模式成就）- 8个

专门为 Quick Judge 游戏模式设计的成就。

#### ⭐ Very Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `first_judge` | First Judge | ⚖️ | 初次判断 | 完成1局Quick Judge | 10 ⭐ |
| `easy_winner` | Easy Winner | 🏅 | 简单胜利 | Easy模式3星通关 | 20 ⭐ |

#### ⭐⭐ Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `quick_thinker` | Quick Thinker | 💭 | 快速思考 | 3秒内答对5题 | 25 ⭐ |
| `judge_apprentice` | Judge Apprentice | 📜 | 判断学徒 | 完成10局Quick Judge | 30 ⭐ |

#### ⭐⭐⭐ Medium（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `perfect_judge` | Perfect Judge | ⭐️ | 完美判断 | Normal模式无错误通关 | 40 ⭐ |
| `speed_judge` | Speed Judge | ⚡ | 速度判断 | Hard模式3星通关 | 45 ⭐ |

#### ⭐⭐⭐⭐ Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `judge_master` | Judge Master | 🎖️ | 判断大师 | 所有难度模式3星通关 | 80 ⭐ + 称号 |

#### ⭐⭐⭐⭐⭐ Very Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `lightning_judge` | Lightning Judge | ⚡ | 闪电判断 | 20连击+Hard模式通关 | 150 ⭐ + 宠物 |

---

### 2.6 Social Achievements（社交成就）- 8个

社交成就奖励分享和社区参与（未来功能）。

#### ⭐ Very Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `sharer` | Sharer | 📤 | 分享者 | 分享1次成绩 | 10 ⭐ |
| `friendly_helper` | Friendly Helper | 🤝 | 友好助手 | 为他人点赞1次 | 15 ⭐ |

#### ⭐⭐ Easy（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `community_member` | Community Member | 👥 | 社区成员 | 加入/创建1个学习小组 | 25 ⭐ |
| `regular_sharer` | Regular Sharer | 📢 | 常驻分享者 | 分享5次成绩 | 30 ⭐ |

#### ⭐⭐⭐ Medium（2个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `helper` | Helper | 🆘 | 助人者 | 帮助3名新用户 | 40 ⭐ + 徽章 |
| `group_leader` | Group Leader | 👑 | 小组长 | 创建小组并有5名成员 | 50 ⭐ |

#### ⭐⭐⭐⭐ Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `influencer` | Influencer | 🌟 | 影响者 | 分享被查看100次 | 70 ⭐ |

#### ⭐⭐⭐⭐⭐ Very Hard（1个）

| ID | 名称 | 图标 | 描述 | 要求 | 奖励 |
|----|------|------|------|------|------|
| `legend` | Legend | 🏛️ | 传说 | 帮助100名用户 | 150 ⭐ + 宠物 + 称号 |

---

## 3. 成就层级

### 3.1 难度等级定义

| 等级 | 星级 | 名称 | 占比 | 完成预期 |
|------|------|------|------|---------|
| 1 | ⭐ | Bronze (青铜) | 40% | 新手1周内完成 |
| 2 | ⭐⭐ | Silver (白银) | 30% | 入门1月内完成 |
| 3 | ⭐⭐⭐ | Gold (黄金) | 20% | 进阶2-3月内完成 |
| 4 | ⭐⭐⭐⭐ | Platinum (白金) | 8% | 高手3-6月内完成 |
| 5 | ⭐⭐⭐⭐⭐ | Diamond (钻石) | 2% | 传奇6月+完成 |

### 3.2 难度标识

| 难度 | 颜色 | 图标 | 描述 |
|------|------|------|------|
| Very Easy | 🟢 | ⭐ | 轻松获得 |
| Easy | 🔵 | ⭐⭐ | 稍作努力 |
| Medium | 🟡 | ⭐⭐⭐ | 需要挑战 |
| Hard | 🟠 | ⭐⭐⭐⭐ | 高度挑战 |
| Very Hard | 🔴 | ⭐⭐⭐⭐⭐ | 终极挑战 |

---

## 4. 奖励系统

### 4.1 奖励类型

#### 4.1.1 Stars（⭐ 金币）

基础货币，用于：
- 解锁新岛屿
- 购买宠物皮肤
- 购买主题

| 成就难度 | 金币奖励 |
|---------|---------|
| ⭐ Very Easy | 10-20 ⭐ |
| ⭐⭐ Easy | 20-35 ⭐ |
| ⭐⭐⭐ Medium | 35-50 ⭐ |
| ⭐⭐⭐⭐ Hard | 50-100 ⭐ |
| ⭐⭐⭐⭐⭐ Very Hard | 100-200 ⭐ |

#### 4.1.2 Titles（称号）

显示在用户名旁的荣誉头衔：

| 称号ID | 显示名称 | 获得方式 |
|--------|---------|---------|
| `title_scholar` | 学者 | Word Scholar 成就 |
| `title_memory_master` | 记忆大师 | Memory Master 成就 |
| `title_unstoppable` | 势不可挡 | Unstoppable 成就 |
| `title_legendary` | 传奇 | Legendary Learner 成就 |
| `title_judge_master` | 判断大师 | Judge Master 成就 |
| `title_legend` | 传说 | Legend 成就 |

#### 4.1.3 Badges（徽章）

永久显示在个人资料的徽章：

| 徽章ID | 图标 | 获得方式 |
|--------|------|---------|
| `badge_word_hunter` | 🏆 | Word Hunter 成就 |
| `badge_dedicated` | 📅 | Dedicated Student 成就 |
| `badge_unstoppable` | ⚡ | Unstoppable 成就 |
| `badge_helper` | 🆘 | Helper 成就 |
| `badge_influencer` | 🌟 | Influencer 成就 |

#### 4.1.4 Pet Unlocks（宠物解锁）

专属宠物奖励：

| 宠物ID | 名称 | 获得方式 |
|--------|------|---------|
| `pet_island_master` | 岛屿守护狮 🦁 | Island Master 成就 |
| `pet_perfect_island` | 完美独角兽 🦄 | Perfect Island 成就 |
| `pet_legendary_learner` | 传说凤凰 🔥 | Legendary Learner 成就 |
| `pet_lightning_judge` | 闪电兔子 ⚡ | Lightning Judge 成就 |
| `pet_legend` | 社区海龟 🐢 | Legend 成就 |

### 4.2 奖励发放流程

```kotlin
/**
 * 奖励发放器
 */
class RewardGranter(
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository,
    private val notificationService: NotificationService
) {
    suspend fun grantReward(
        userId: String,
        reward: AchievementReward,
        achievementName: String
    ): GrantResult {
        return when (reward) {
            is AchievementReward.Stars -> {
                userRepository.addStars(userId, reward.amount)
                GrantResult.Success("获得 ${reward.amount} 金币")
            }
            is AchievementReward.Title -> {
                userRepository.unlockTitle(userId, reward.titleId)
                GrantResult.Success("获得称号: ${reward.displayName}")
            }
            is AchievementReward.Badge -> {
                inventoryRepository.addBadge(userId, reward.badgeId)
                GrantResult.Success("获得徽章: ${reward.iconName}")
            }
            is AchievementReward.PetUnlock -> {
                inventoryRepository.unlockPet(userId, reward.petId)
                GrantResult.Success("解锁宠物: ${reward.petName}")
            }
            is AchievementReward.Multiple -> {
                val results = reward.rewards.map { grantReward(userId, it, achievementName) }
                if (results.all { it is GrantResult.Success }) {
                    GrantResult.Success("获得多项奖励！")
                } else {
                    GrantResult.PartialSuccess
                }
            }
        }
    }
}
```

---

## 5. 触发机制

### 5.1 游戏事件类型

成就系统通过监听游戏事件来触发检测：

```kotlin
@Immutable
sealed class GameEvent {
    // === 进度事件 ===
    data class WordLearned(
        val userId: String,
        val wordId: String,
        val memoryStrength: Int
    ) : GameEvent()

    data class LevelCompleted(
        val userId: String,
        val levelId: String,
        val islandId: String,
        val stars: Int,
        val allWordsThreeStar: Boolean,
        val timeTaken: Long,
        val hintsUsed: Int
    ) : GameEvent()

    data class IslandUnlocked(
        val userId: String,
        val islandId: String
    ) : GameEvent()

    data class MasteryThreshold(
        val userId: String,
        val islandId: String,
        val masteryPercentage: Float
    ) : GameEvent()

    // === 表现事件 ===
    data class PerfectLevel(
        val userId: String,
        val levelId: String,
        val zeroErrors: Boolean,
        val allThreeStar: Boolean
    ) : GameEvent()

    data class ConsecutiveLevels(
        val userId: String,
        val count: Int,
        val minStars: Int
    ) : GameEvent()

    // === 连击事件 ===
    data class ComboAchieved(
        val userId: String,
        val comboCount: Int,
        val levelId: String
    ) : GameEvent()

    data class MaxComboInLevel(
        val userId: String,
        val comboCount: Int
    ) : GameEvent()

    // === 连续事件 ===
    data class DailyStreak(
        val userId: String,
        val streakDays: Int
    ) : GameEvent()

    data class StudySession(
        val userId: String,
        val wordsLearned: Int
    ) : GameEvent()

    // === Quick Judge 事件 ===
    data class QuickJudgeCompleted(
        val userId: String,
        val difficulty: String, // "easy", "normal", "hard"
        val stars: Int,
        val perfect: Boolean
    ) : GameEvent()

    data class QuickJudgeCombo(
        val userId: String,
        val comboCount: Int
    ) : GameEvent()

    data class QuickJudgeSpeed(
        val userId: String,
        val correctCount: Int,
        val maxTimePerQuestion: Long
    ) : GameEvent()

    // === 社交事件 ===
    data class AchievementShared(
        val userId: String,
        val shareType: String
    ) : GameEvent()

    data class HelpedOther(
        val userId: String,
        val helpedUserId: String
    ) : GameEvent()

    data class GroupActivity(
        val userId: String,
        val activityType: String,
        val count: Int
    ) : GameEvent()
}
```

### 5.2 事件触发时机

| 事件 | 触发时机 | 检测的成就类型 |
|------|---------|--------------|
| `WordLearned` | 单词记忆强度≥80 | Word Collector, Vocabulary Builder, Word Scholar... |
| `LevelCompleted` | 关卡结束 | First Steps, Page Turner, Level Master... |
| `ComboAchieved` | 连击达成 | Warm Up, Combo Starter, Combo Master... |
| `DailyStreak` | 每日首次学习 | Streak Starter, Week Warrior Jr, Dedicated Student... |
| `QuickJudgeCompleted` | Quick Judge结束 | First Judge, Easy Winner, Perfect Judge... |

---

## 6. 数据模型

### 6.1 核心实体

```kotlin
/**
 * 成就实体
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,

    // 基本信息
    val name: String,
    val nameZh: String,
    val description: String,
    val descriptionZh: String,
    val category: AchievementCategory,
    val icon: String,

    // 难度和奖励
    val difficulty: AchievementDifficulty,
    val reward: AchievementReward,

    // 触发条件
    val requirementType: RequirementType,
    val targetValue: Int,

    // UI相关
    val isHidden: Boolean = false,
    val isHiddenUntil: Long? = null,

    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 用户成就实体
 */
@Entity(
    tableName = "user_achievements",
    indices = [
        Index(value = ["user_id", "achievement_id"], unique = true),
        Index(value = ["user_id", "unlocked_at"])
    ]
)
data class UserAchievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "achievement_id")
    val achievementId: String,

    // 进度追踪
    @ColumnInfo(name = "current_progress")
    val currentProgress: Int,

    @ColumnInfo(name = "target_value")
    val targetValue: Int,

    // 状态
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean,

    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null,

    // 时间戳
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 成就进度实体
 */
@Entity(
    tableName = "achievement_progress",
    indices = [Index(value = ["user_id", "achievement_id"], unique = true)]
)
data class AchievementProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "achievement_id")
    val achievementId: String,

    // 进度数据
    @ColumnInfo(name = "current_value")
    val currentValue: Int,

    @ColumnInfo(name = "target_value")
    val targetValue: Int,

    // 百分比（缓存）
    @ColumnInfo(name = "percentage")
    val percentage: Float,

    // 时间戳
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
```

### 6.2 枚举定义

```kotlin
/**
 * 成就类别
 */
enum class AchievementCategory {
    PROGRESS,        // 进度成就
    PERFORMANCE,     // 表现成就
    COMBO,           // 连击成就
    STREAK,          // 连续成就
    QUICK_JUDGE,    // 判断成就
    SOCIAL          // 社交成就
}

/**
 * 成就难度
 */
enum class AchievementDifficulty {
    VERY_EASY,   // ⭐
    EASY,         // ⭐⭐
    MEDIUM,       // ⭐⭐⭐
    HARD,        // ⭐⭐⭐⭐
    VERY_HARD    // ⭐⭐⭐⭐⭐
}

/**
 * 奖励类型
 */
sealed class AchievementReward {
    data class Stars(val amount: Int) : AchievementReward()
    data class Title(val titleId: String, val displayName: String) : AchievementReward()
    data class Badge(val badgeId: String, val iconName: String) : AchievementReward()
    data class PetUnlock(val petId: String, val petName: String) : AchievementReward()
    data class Multiple(val rewards: List<AchievementReward>) : AchievementReward()
}

/**
 * 要求类型
 */
enum class RequirementType {
    COMPLETE_LEVELS,      // 完成关卡
    MASTER_WORDS,         // 掌握单词
    ISLAND_MASTERY,       // 岛屿精通
    COMBO_MILESTONE,      // 连击里程碑
    STREAK_DAYS,          // 连续天数
    QUICK_JUDGE_STARS,    // Quick Judge星级
    QUICK_JUDGE_PERFECT,  // Quick Judge完美
    QUICK_JUDGE_ALL,      // 所有难度
    SHARE_COUNT,          // 分享次数
    GROUP_JOIN,           // 加入小组
    HELP_OTHERS           // 帮助他人
}
```

### 6.3 DAO 接口

```kotlin
/**
 * 成就 DAO
 */
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY difficulty ASC")
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND is_unlocked = 1 ORDER BY unlocked_at DESC")
    suspend fun getUnlockedAchievements(userId: String): List<UserAchievement>

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId ORDER BY percentage DESC")
    suspend fun getAchievementsByProgress(userId: String): List<UserAchievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievement(userAchievement: UserAchievement): Long

    @Update
    suspend fun updateUserAchievement(userAchievement: UserAchievement): Int

    @Query("UPDATE user_achievements SET current_progress = :progress, updated_at = :updatedAt WHERE user_id = :userId AND achievement_id = :achievementId")
    suspend fun updateProgress(
        userId: String,
        achievementId: String,
        progress: Int,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId AND achievement_id = :achievementId")
    suspend fun getUserAchievement(userId: String, achievementId: String): UserAchievement?

    @Query("UPDATE user_achievements SET is_unlocked = 1, unlocked_at = :unlockedAt, updated_at = :updatedAt WHERE user_id = :userId AND achievement_id = :achievementId")
    suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        unlockedAt: Long = System.currentTimeMillis()
    ): Int
}
```

---

## 7. UI/UX需求

### 7.1 成就解锁通知

#### 全屏弹窗（重要成就 - ⭐⭐⭐⭐⭐）

```
┌─────────────────────────────────────┐
│                                     │
│         🎉 成就解锁！ 🎉            │
│                                     │
│              👑                    │
│         Island Master              │
│       （岛屿大师）                  │
│                                     │
│         完美精通一个岛屿             │
│                                     │
│      ┌─────────────────┐            │
│      │   太棒了！      │            │
│      └─────────────────┘            │
│                                     │
│      奖励: 150 ⭐                     │
│         🦁 宠物解锁                   │
│                                     │
│         [继续学习]                 │
│                                     │
└─────────────────────────────────────┘
```

#### 顶部通知（普通成就 - ⭐ ~ ⭐⭐⭐）

```
┌─────────────────────────────────────┐
│ 🔥 Combo Master 解锁!        [×]    │
│   50 ⭐ + 称号"连击大师"              │
└─────────────────────────────────────┘
```

### 7.2 成就列表页面

```
┌─────────────────────────────────────┐
│  成就                        [🔍]    │
├─────────────────────────────────────┤
│  进度: 18/50 解锁 (36%)             │
│  ████████████░░░░░░░░░░░░░░░░░░       │
│                                     │
│  [全部] [进度] [表现] [连击]        │
│  [连续] [判断] [社交]               │
│                                     │
│  ✅ 🌟 First Steps                  │
│     完成第一个关卡                   │
│                                     │
│  ✅ 📚 Page Turner                  │
│     完成 5 个关卡                    │
│                                     │
│  🔥🔥 Combo Master                  │
│     ████████░░ 8/10 连击            │
│     进度: 80%                       │
│                                     │
│  🏆 Word Hunter                     │
│     ██████░░░░ 42/60 单词           │
│     进度: 70%                       │
│                                     │
└─────────────────────────────────────┘
```

### 7.3 成就详情页面

```
┌─────────────────────────────────────┐
│  ← 返回                              │
├─────────────────────────────────────┤
│                                     │
│              🏆 Word Hunter          │
│                                     │
│  学习 60 个单词                      │
│                                     │
│  ████████░░░░ 42/60                  │
│  进度: 70%                          │
│                                     │
│  📊 解锁条件:                        │
│  • 掌握 60 个单词                    │
│  • 记忆强度 ≥ 80                     │
│  • 当前: 42/60 单词                  │
│                                     │
│  🎁 奖励:                            │
│  • 100 金币 ⭐                       │
│  • 徽章: "Word Hunter" 🏆            │
│  • 显示在个人资料                    │
│                                     │
│  📈 你的进度:                        │
│  • 开始: Feb 10, 2026                │
│  • 平均: 3.5 单词/天                 │
│  • 预计: 5 天完成                    │
│                                     │
│  💡 小贴士:                          │
│  • 定期复习可以提高记忆强度           │
│  • 专注学习一个关卡                  │
│                                     │
│  [分享成就]                         │
│                                     │
└─────────────────────────────────────┘
```

---

## 8. 实施计划

### 8.1 阶段划分

#### Phase 1: 核心成就系统（P0）

**内容**:
- 15个核心成就（Progress类12个 + Combo类3个）
- 基础奖励系统（仅金币）
- 简单成就UI

**时间**: 1-2周

**交付物**:
- [ ] 成就数据模型和数据库
- [ ] 成就检测器
- [ ] 成就列表UI
- [ ] 成就解锁通知

#### Phase 2: 完整成就系统（P1）

**内容**:
- 50个完整成就
- 多样化奖励（金币、称号、徽章、宠物）
- 完整成就UI（列表、详情、通知）

**时间**: 2-3周

**交付物**:
- [ ] 所有50个成就定义
- [ ] 称号系统
- [ ] 徽章系统
- [ ] 宠物解锁

#### Phase 3: 社交成就（P2）

**内容**:
- 社交功能实现
- 社交成就激活
- 分享功能

**时间**: 1-2周

**交付物**:
- [ ] 分享功能
- [ ] 点赞系统
- [ ] 学习小组
- [ ] 社交成就激活

### 8.2 验收标准

- [ ] 所有50个成就定义完成
- [ ] 成就检测逻辑正确
- [ ] 奖励发放正常
- [ ] UI显示正确
- [ ] 进度追踪准确
- [ ] 性能满足要求（检测<100ms）
- [ ] 单元测试覆盖率≥80%

### 8.3 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 检测延迟 | <100ms | 从事件到完成检测 |
| UI刷新 | <200ms | 进度更新到UI显示 |
| 内存占用 | <50MB | 成就系统总占用 |
| 数据库查询 | <50ms | 成就列表加载 |

---

**文档状态**: ✅ 成就系统需求定义完成
**下一步**: UI/UX 设计文档 (05-ui-ux-design.md)
**预计工作量**: 12-18人天（完整实现）
**MVP工作量**: 8-10人天（核心成就 + 基础奖励）
