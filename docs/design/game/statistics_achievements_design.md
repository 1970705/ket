# 成就系统游戏设计文档

**项目**: Wordland Statistics & Achievements System
**版本**: 1.0
**日期**: 2026-02-20
**作者**: game-designer
**状态**: 设计阶段

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

## 2. 成就分类详解

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

## 3. 成就触发机制

### 3.1 游戏事件类型

成就系统通过监听游戏事件来触发检测：

```kotlin
/**
 * 游戏事件 - 触发成就检测
 */
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
        val maxTimePerQuestion: Long // 毫秒
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

### 3.2 成就检测逻辑

```kotlin
/**
 * 成就检测器
 * 负责检测游戏事件是否触发成就解锁
 */
class AchievementDetector(
    private val achievementRepository: AchievementRepository,
    private val progressRepository: ProgressRepository,
    private val statisticsRepository: StatisticsRepository
) {
    /**
     * 检测事件并返回新解锁的成就
     */
    suspend fun detect(
        event: GameEvent,
        userId: String
    ): List<AchievementUnlock> {
        val newlyUnlocked = mutableListOf<AchievementUnlock>()

        // 获取所有未解锁的成就
        val lockedAchievements = achievementRepository.getLockedAchievements(userId)

        // 根据事件类型筛选相关成就
        val relevantAchievements = filterRelevantAchievements(event, lockedAchievements)

        // 检测每个相关成就
        for (achievement in relevantAchievements) {
            val progress = checkProgress(event, achievement, userId)

            if (progress.isUnlocked) {
                // 解锁成就
                achievementRepository.unlockAchievement(userId, achievement.id)
                newlyUnlocked.add(
                    AchievementUnlock(
                        achievement = achievement,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            } else if (progress.hasProgress) {
                // 更新进度
                achievementRepository.updateProgress(
                    userId,
                    achievement.id,
                    progress.current,
                    progress.target
                )
            }
        }

        return newlyUnlocked
    }

    /**
     * 检查单个成就的进度
     */
    private suspend fun checkProgress(
        event: GameEvent,
        achievement: Achievement,
        userId: String
    ): AchievementProgress {
        return when (achievement.requirement) {
            // 进度类
            is AchievementRequirement.CompleteLevels -> {
                val count = progressRepository.getCompletedLevelCount(
                    userId,
                    (achievement.requirement as AchievementRequirement.CompleteLevels).minStars
                )
                AchievementProgress(
                    current = count,
                    target = achievement.requirement.count,
                    isUnlocked = count >= achievement.requirement.count
                )
            }

            is AchievementRequirement.MasterWords -> {
                val count = progressRepository.getMasteredWordCount(
                    userId,
                    (achievement.requirement as AchievementRequirement.MasterWords).memoryStrengthThreshold
                )
                AchievementProgress(
                    current = count,
                    target = achievement.requirement.count,
                    isUnlocked = count >= achievement.requirement.count
                )
            }

            is AchievementRequirement.IslandMastery -> {
                val mastery = progressRepository.getIslandMastery(
                    userId,
                    achievement.requirement.islandId
                )
                AchievementProgress(
                    current = (mastery * 100).toInt(),
                    target = (achievement.requirement.masteryPercentage * 100).toInt(),
                    isUnlocked = mastery >= achievement.requirement.masteryPercentage
                )
            }

            // 连击类
            is AchievementRequirement.ComboMilestone -> {
                if (event is GameEvent.ComboAchieved) {
                    AchievementProgress(
                        current = event.comboCount,
                        target = achievement.requirement.comboCount,
                        isUnlocked = event.comboCount >= achievement.requirement.comboCount
                    )
                } else {
                    AchievementProgress.noProgress()
                }
            }

            // 连续类
            is AchievementRequirement.StreakDays -> {
                if (event is GameEvent.DailyStreak) {
                    AchievementProgress(
                        current = event.streakDays,
                        target = achievement.requirement.days,
                        isUnlocked = event.streakDays >= achievement.requirement.days
                    )
                } else {
                    AchievementProgress.noProgress()
                }
            }

            // Quick Judge 类
            is AchievementRequirement.QuickJudgePerfect -> {
                if (event is GameEvent.QuickJudgeCompleted && event.perfect) {
                    val perfectCount = statisticsRepository.getQuickJudgePerfectCount(
                        userId,
                        event.difficulty
                    )
                    AchievementProgress(
                        current = perfectCount,
                        target = achievement.requirement.count,
                        isUnlocked = perfectCount >= achievement.requirement.count
                    )
                } else {
                    AchievementProgress.noProgress()
                }
            }

            // ... 其他类型
            else -> AchievementProgress.noProgress()
        }
    }
}
```

### 3.3 事件触发时机

| 事件 | 触发时机 | 检测的成就类型 |
|------|---------|--------------|
| `WordLearned` | 单词记忆强度≥80 | Word Collector, Vocabulary Builder, Word Scholar... |
| `LevelCompleted` | 关卡结束 | First Steps, Page Turner, Level Master... |
| `ComboAchieved` | 连击达成 | Warm Up, Combo Starter, Combo Master... |
| `DailyStreak` | 每日首次学习 | Streak Starter, Week Warrior Jr, Dedicated Student... |
| `QuickJudgeCompleted` | Quick Judge结束 | First Judge, Easy Winner, Perfect Judge... |

---

## 4. 成就奖励系统

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
| `pet_island_master` | 岛屿守护狮 | Island Master 成就 |
| `pet_perfect_island` | 完美独角兽 | Perfect Island 成就 |
| `pet_legendary_learner` | 传说凤凰 | Legendary Learner 成就 |
| `pet_lightning_judge` | 闪电兔子 | Lightning Judge 成就 |
| `pet_legend` | 社区海龟 | Legend 成就 |

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
    /**
     * 发放成就奖励
     */
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

## 5. UI/UX 设计

### 5.1 成就解锁通知

#### 全屏弹窗（重要成就）

```
┌─────────────────────────────────────┐
│                                     │
│         🎉 成就解锁！ 🎉            │
│                                     │
│           🏆 Word Hunter            │
│                                     │
│         学习 60 个单词               │
│                                     │
│      ┌─────────────────┐            │
│      │   太棒了！      │            │
│      └─────────────────┘            │
│                                     │
│      奖励: 100 ⭐ + 徽章 🏆         │
│                                     │
│         [继续学习]                 │
│                                     │
└─────────────────────────────────────┘
```

#### 顶部通知（普通成就）

```
┌─────────────────────────────────────┐
│ 🔥 Combo Master 解锁!        [×]    │
└─────────────────────────────────────┘
```

### 5.2 成就列表页面

```
┌─────────────────────────────────────┐
│  成就                        [🔍]    │
├─────────────────────────────────────┤
│  进度: 18/50 解锁 (36%)             │
│  ████████████░░░░░░░░░░░░░░░░░       │
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

### 5.3 成就详情页面

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

## 6. 成就进度追踪

### 6.1 进度显示规则

| 成就类型 | 进度显示 | 示例 |
|---------|---------|------|
| **计数类** | "当前/目标" | "42/60 单词" |
| **百分比类** | "XX%" | "70% 精通度" |
| **布尔类** | "完成/未完成" | "完成 ✓" |
| **最大值类** | "最大: XX" | "最大连击: 8" |

### 6.2 进度更新频率

| 事件类型 | 更新频率 | 说明 |
|---------|---------|------|
| WordLearned | 即时 | 每学会一个单词更新相关进度 |
| LevelCompleted | 即时 | 每完成关卡更新相关进度 |
| ComboAchieved | 即时 | 每次连击更新 |
| DailyStreak | 每日一次 | 每日首次学习更新连续天数 |
| IslandMastery | 每关卡完成后 | 计算岛屿精通度 |

---

## 7. 儿童友好设计

### 7.1 鼓励性文案

| 场景 | 文案 |
|------|------|
| 首次解锁成就 | "太棒了！你解锁了第一个成就！" |
| 进度更新 | "继续努力，还差一点点！" |
| 长期目标 | "这是一个长期目标，慢慢来，不着急！" |
| 成就解锁失败 | "没关系，下次一定能成功！" |

### 7.2 难度标识

| 难度 | 星级 | 图标 | 描述 |
|------|------|------|------|
| Very Easy | ⭐ | 🟢 | 轻松获得 |
| Easy | ⭐⭐ | 🔵 | 稍作努力 |
| Medium | ⭐⭐⭐ | 🟡 | 需要挑战 |
| Hard | ⭐⭐⭐⭐ | 🟠 | 高度挑战 |
| Very Hard | ⭐⭐⭐⭐⭐ | 🔴 | 终极挑战 |

### 7.3 时间预估

为长期成就显示预估完成时间：

```
📈 你的进度:
• 开始: Feb 10, 2026
• 平均: 3.5 单词/天
• 预计: 5 天完成
```

---

## 8. 成就系统集成

### 8.1 与现有系统集成

| 系统 | 集成方式 |
|------|---------|
| **学习系统** | 学习完成后触发进度成就检测 |
| **连击系统** | 连击达成后触发连击成就检测 |
| **统计系统** | 读取统计数据计算成就进度 |
| **Quick Judge** | 完成关卡后触发判断成就检测 |

### 8.2 数据流

```
游戏事件 → 成就检测器 → 进度更新 → 奖励发放 → UI通知
                ↓
            数据库保存
                ↓
            统计系统聚合
```

### 8.3 性能考虑

- **异步检测**：成就检测在后台线程执行
- **批量更新**：多个成就进度合并更新
- **缓存策略**：用户成就进度缓存5分钟
- **延迟加载**：成就列表分页加载

---

## 9. 成就数据定义（JSON）

### 9.1 完整成就列表（50个）

```json
{
  "achievements": [
    {
      "id": "first_steps",
      "name": "First Steps",
      "name_zh": "初出茅庐",
      "description": "Complete your first level",
      "description_zh": "完成第一个关卡",
      "category": "PROGRESS",
      "icon": "🌟",
      "difficulty": "VERY_EASY",
      "requirement": {
        "type": "CompleteLevels",
        "count": 1,
        "minStars": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 10
      },
      "targetValue": 1
    },
    {
      "id": "word_collector",
      "name": "Word Collector",
      "name_zh": "单词收集者",
      "description": "Learn 5 words",
      "description_zh": "学习5个单词",
      "category": "PROGRESS",
      "icon": "📝",
      "difficulty": "VERY_EASY",
      "requirement": {
        "type": "MasterWords",
        "count": 5,
        "memoryStrengthThreshold": 80
      },
      "reward": {
        "type": "Stars",
        "amount": 15
      },
      "targetValue": 5
    },
    {
      "id": "page_turner",
      "name": "Page Turner",
      "name_zh": "翻页者",
      "description": "Complete 5 levels",
      "description_zh": "完成5个关卡",
      "category": "PROGRESS",
      "icon": "📚",
      "difficulty": "EASY",
      "requirement": {
        "type": "CompleteLevels",
        "count": 5,
        "minStars": 2
      },
      "reward": {
        "type": "Stars",
        "amount": 25
      },
      "targetValue": 5
    },
    {
      "id": "word_scholar",
      "name": "Word Scholar",
      "name_zh": "单词学者",
      "description": "Learn 30 words",
      "description_zh": "学习30个单词",
      "category": "PROGRESS",
      "icon": "🎓",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "MasterWords",
        "count": 30,
        "memoryStrengthThreshold": 80
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 50 },
          { "type": "Title", "titleId": "title_scholar", "displayName": "学者" }
        ]
      },
      "targetValue": 30
    },
    {
      "id": "word_hunter",
      "name": "Word Hunter",
      "name_zh": "单词猎人",
      "description": "Learn 60 words",
      "description_zh": "学习60个单词",
      "category": "PROGRESS",
      "icon": "🏆",
      "difficulty": "HARD",
      "requirement": {
        "type": "MasterWords",
        "count": 60,
        "memoryStrengthThreshold": 80
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 100 },
          { "type": "Badge", "badgeId": "badge_word_hunter", "iconName": "🏆" }
        ]
      },
      "targetValue": 60
    },
    {
      "id": "island_master",
      "name": "Island Master",
      "name_zh": "岛屿大师",
      "description": "Master an island completely",
      "description_zh": "完美精通一个岛屿",
      "category": "PROGRESS",
      "icon": "👑",
      "difficulty": "VERY_HARD",
      "requirement": {
        "type": "IslandMastery",
        "islandId": "any",
        "masteryPercentage": 1.0
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 150 },
          { "type": "PetUnlock", "petId": "pet_island_master", "petName": "岛屿守护狮" }
        ]
      },
      "targetValue": 100
    },
    {
      "id": "perfectionist",
      "name": "Perfectionist",
      "name_zh": "完美主义者",
      "description": "Complete a level with all 3-star words",
      "description_zh": "完美关卡（所有单词3星）",
      "category": "PERFORMANCE",
      "icon": "💎",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "PerfectLevel",
        "count": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 30
      },
      "targetValue": 1
    },
    {
      "id": "combo_master",
      "name": "Combo Master",
      "name_zh": "连击大师",
      "description": "Reach a 10x combo",
      "description_zh": "达成10连击",
      "category": "COMBO",
      "icon": "🔥",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "ComboMilestone",
        "comboCount": 10
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 50 },
          { "type": "Title", "titleId": "title_combo_master", "displayName": "连击大师" }
        ]
      },
      "targetValue": 10
    },
    {
      "id": "unstoppable",
      "name": "Unstoppable",
      "name_zh": "势不可挡",
      "description": "Reach a 20x combo",
      "description_zh": "达成20连击",
      "category": "COMBO",
      "icon": "⚡",
      "difficulty": "HARD",
      "requirement": {
        "type": "ComboMilestone",
        "comboCount": 20
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 80 },
          { "type": "Badge", "badgeId": "badge_unstoppable", "iconName": "⚡" }
        ]
      },
      "targetValue": 20
    },
    {
      "id": "dedicated_student",
      "name": "Dedicated Student",
      "name_zh": "专注学生",
      "description": "Practice for 14 days in a row",
      "description_zh": "连续14天学习",
      "category": "STREAK",
      "icon": "🎓",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "StreakDays",
        "days": 14,
        "minWordsPerDay": 1
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 50 },
          { "type": "Badge", "badgeId": "badge_dedicated", "iconName": "🎓" }
        ]
      },
      "targetValue": 14
    },
    {
      "id": "legendary_learner",
      "name": "Legendary Learner",
      "name_zh": "传奇学习者",
      "description": "Practice for 100 days in a row",
      "description_zh": "连续100天学习",
      "category": "STREAK",
      "icon": "👑",
      "difficulty": "VERY_HARD",
      "requirement": {
        "type": "StreakDays",
        "days": 100,
        "minWordsPerDay": 1
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 200 },
          { "type": "PetUnlock", "petId": "pet_legendary_learner", "petName": "传说凤凰" },
          { "type": "Title", "titleId": "title_legendary", "displayName": "传奇" }
        ]
      },
      "targetValue": 100
    },
    {
      "id": "easy_winner",
      "name": "Easy Winner",
      "name_zh": "简单模式赢家",
      "description": "Get 3 stars in Easy mode",
      "description_zh": "Easy模式3星通关",
      "category": "QUICK_JUDGE",
      "icon": "🏅",
      "difficulty": "VERY_EASY",
      "requirement": {
        "type": "QuickJudgeStars",
        "difficulty": "easy",
        "stars": 3
      },
      "reward": {
        "type": "Stars",
        "amount": 20
      },
      "targetValue": 1
    },
    {
      "id": "perfect_judge",
      "name": "Perfect Judge",
      "name_zh": "完美判断",
      "description": "Complete Normal mode without errors",
      "description_zh": "Normal模式无错误通关",
      "category": "QUICK_JUDGE",
      "icon": "⭐️",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "QuickJudgePerfect",
        "difficulty": "normal",
        "count": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 40
      },
      "targetValue": 1
    },
    {
      "id": "speed_judge",
      "name": "Speed Judge",
      "name_zh": "速度判断",
      "description": "Get 3 stars in Hard mode",
      "description_zh": "Hard模式3星通关",
      "category": "QUICK_JUDGE",
      "icon": "⚡",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "QuickJudgeStars",
        "difficulty": "hard",
        "stars": 3
      },
      "reward": {
        "type": "Stars",
        "amount": 45
      },
      "targetValue": 1
    },
    {
      "id": "judge_master",
      "name": "Judge Master",
      "name_zh": "判断大师",
      "description": "3 stars in all difficulties",
      "description_zh": "所有难度模式3星通关",
      "category": "QUICK_JUDGE",
      "icon": "🎖️",
      "difficulty": "HARD",
      "requirement": {
        "type": "QuickJudgeAllDifficulties",
        "stars": 3
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 80 },
          { "type": "Title", "titleId": "title_judge_master", "displayName": "判断大师" }
        ]
      },
      "targetValue": 3
    },
    {
      "id": "lightning_judge",
      "name": "Lightning Judge",
      "name_zh": "闪电判断",
      "description": "20 combo + Hard mode 3 stars",
      "description_zh": "20连击+Hard模式3星通关",
      "category": "QUICK_JUDGE",
      "icon": "⚡",
      "difficulty": "VERY_HARD",
      "requirement": {
        "type": "QuickJudgeUltimate",
        "comboCount": 20,
        "difficulty": "hard",
        "stars": 3
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 150 },
          { "type": "PetUnlock", "petId": "pet_lightning_judge", "petName": "闪电兔子" }
        ]
      },
      "targetValue": 1
    },
    {
      "id": "sharer",
      "name": "Sharer",
      "name_zh": "分享者",
      "description": "Share your progress once",
      "description_zh": "分享一次成绩",
      "category": "SOCIAL",
      "icon": "📤",
      "difficulty": "VERY_EASY",
      "requirement": {
        "type": "ShareCount",
        "count": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 10
      },
      "targetValue": 1
    },
    {
      "id": "community_member",
      "name": "Community Member",
      "name_zh": "社区成员",
      "description": "Join or create a study group",
      "description_zh": "加入/创建一个学习小组",
      "category": "SOCIAL",
      "icon": "👥",
      "difficulty": "EASY",
      "requirement": {
        "type": "GroupJoin",
        "count": 1
      },
      "reward": {
        "type": "Stars",
        "amount": 25
      },
      "targetValue": 1
    },
    {
      "id": "helper",
      "name": "Helper",
      "name_zh": "助人者",
      "description": "Help 3 new users",
      "description_zh": "帮助3名新用户",
      "category": "SOCIAL",
      "icon": "🆘",
      "difficulty": "MEDIUM",
      "requirement": {
        "type": "HelpOthers",
        "count": 3
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 40 },
          { "type": "Badge", "badgeId": "badge_helper", "iconName": "🆘" }
        ]
      },
      "targetValue": 3
    },
    {
      "id": "legend",
      "name": "Legend",
      "name_zh": "传说",
      "description": "Help 100 users",
      "description_zh": "帮助100名用户",
      "category": "SOCIAL",
      "icon": "🏛️",
      "difficulty": "VERY_HARD",
      "requirement": {
        "type": "HelpOthers",
        "count": 100
      },
      "reward": {
        "type": "Multiple",
        "rewards": [
          { "type": "Stars", "amount": 150 },
          { "type": "PetUnlock", "petId": "pet_legend", "petName": "社区海龟" },
          { "type": "Title", "titleId": "title_legend", "displayName": "传说" }
        ]
      },
      "targetValue": 100
    }
  ]
}
```

---

## 10. 实施计划

### 10.1 阶段划分

#### Phase 1: 核心成就系统（P0）

**内容**：
- 15个核心成就（Progress类12个 + Combo类3个）
- 基础奖励系统（仅金币）
- 简单成就UI

**时间**：1-2周

#### Phase 2: 完整成就系统（P1）

**内容**：
- 50个完整成就
- 多样化奖励（金币、称号、徽章、宠物）
- 完整成就UI（列表、详情、通知）

**时间**：2-3周

#### Phase 3: 社交成就（P2）

**内容**：
- 社交功能实现
- 社交成就激活
- 分享功能

**时间**：1-2周

### 10.2 验收标准

- [ ] 所有50个成就定义完成
- [ ] 成就检测逻辑正确
- [ ] 奖励发放正常
- [ ] UI显示正确
- [ ] 进度追踪准确
- [ ] 性能满足要求（检测<100ms）

---

## 11. 总结

本成就系统设计提供了：

1. **50个精心设计的成就**，覆盖6大类别
2. **多样化奖励机制**，包括金币、称号、徽章、宠物
3. **清晰的进度追踪**，让用户看到进展
4. **儿童友好设计**，鼓励和庆祝而非压力
5. **完整的触发机制**，与游戏系统无缝集成

---

**文档版本**: 1.0
**最后更新**: 2026-02-20
**作者**: game-designer
**状态**: 待团队评审
