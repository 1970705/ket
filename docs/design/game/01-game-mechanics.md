# 游戏机制需求详细文档

**文档版本**: v1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**状态**: 详细需求定义

---

## 📋 目录

1. [Spell Battle 游戏机制](#1-spell-battle-游戏机制)
2. [Quick Judge 游戏机制](#2-quick-judge-游戏机制)
3. [记忆算法系统](#3-记忆算法系统)
4. [提示系统](#4-提示系统)
5. [连击系统](#5-连击系统)
6. [星级评分系统](#6-星级评分系统)

---

## 1. Spell Battle 游戏机制

### 1.1 游戏概述

**状态**: ✅ 已实现

**核心玩法**:
- 显示中文翻译
- 用户使用虚拟QWERTY键盘拼写英文单词
- 实时反馈 (蓝色正确, 红色错误位置)
- 提交答案验证
- 动态星级评分

### 1.2 界面布局

```
┌─────────────────────────────────────────┐
│  Look Island - Level 1          [⏱ 5:23] │
├─────────────────────────────────────────┤
│  进度: [████████░░] 4/6                  │
│  🔥 Combo: 3  ⭐ 分数: 450              │
├─────────────────────────────────────────┤
│                                         │
│          中文: 看                        │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │  [ L _ O _ O _ K ]               │ │
│  │     ↑ ↑ ↑                         │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐│
│  │  Q  │ │  W  │ │  E  │ │  R  │ │  T  ││
│  └─────┘ └─────┘ └─────┘ └─────┘ └─────┘│
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐│
│  │  A  │ │  S  │ │  D  │ │  F  │ │  G  ││
│  └─────┘ └─────┘ └─────┘ └─────┘ └─────┘│
│  ... (继续显示完整键盘)                   │
│                                         │
│  ┌─────────┐  ┌──────────┐              │
│  │  ⌫     │  │   提交    │              │
│  └─────────┘  └──────────┘              │
└─────────────────────────────────────────┘
```

### 1.3 核心流程

```kotlin
/**
 * Spell Battle 游戏流程
 */
1. 显示中文翻译
   ↓
2. 用户点击虚拟键盘输入字母
   ↓
3. 实时显示输入状态
   - 正确字母: 蓝色
   - 错误位置: 红色
   - 空位: 下划线
   ↓
4. 用户点击提交
   ↓
5. 答案验证
   - 正确: 进入步骤7
   - 错误: 进入步骤6
   ↓
6. 错误反馈
   - 显示正确拼写
   - 错误字母高亮
   - 记录错误
   ↓
7. 成功反馈
   - 庆祝动画
   - 更新连击
   - 更新分数
   - 更新记忆强度
   ↓
8. 显示星级评分
   - 根据准确率、时间、提示、连击计算
   ↓
9. 加载下一个单词
```

### 1.4 反馈机制

#### 实时反馈

| 用户动作 | 反馈 | 视觉 | 触觉 |
|---------|------|------|------|
| 输入正确字母 | 字母显示蓝色 | 蓝色边框 | 无 |
| 输入错误字母 | 字母显示红色 | 红色边框 + 抖动 | 轻微震动 |
| 删除字母 | 字母移除 | 淡出动画 | 无 |
| 填满所有字母 | 可以提交 | 提交按钮高亮 | 无 |

#### 提交后反馈

| 结果 | 反馈类型 | 视觉效果 | 音效 | 时长 |
|------|---------|----------|------|------|
| 正确 | 成功动画 | 星星飞入 + 庆祝 | 清脆叮声 | 500ms |
| 错误 | 错误提示 | 正确答案显示 + 高亮 | 温和嗡声 | 1000ms |

### 1.5 防作弊机制

#### 时间阈值检测

```kotlin
/**
 * 检测是否盲目快速点击
 */
fun detectGuessing(
    answerTime: Long,
    wordLength: Int
): Boolean {
    // 最小思考时间 = 1s + (0.5s × 单词长度)
    val minThinkTime = 1000 + (500 * wordLength)

    // 如果答题时间小于最小思考时间，可能是猜测
    return answerTime < minThinkTime
}
```

**惩罚**:
- 检测为猜测: 最高1星
- 记忆强度增长减半
- 不计入连击

#### 模式分析

```kotlin
/**
 * 检测答题模式
 */
fun detectPattern(
    recentAnswers: List<Answer>
): Boolean {
    // 检查最近5个答案是否重复或遵循固定模式
    if (recentAnswers.size < 5) return false

    val recent = recentAnswers.takeLast(5)

    // 检测ABABA模式
    val pattern1 = recent[0] != recent[1] &&
                   recent[0] == recent[2] &&
                   recent[1] == recent[3]

    // 检测全部相同
    val pattern2 = recent.all { it == recent[0] }

    return pattern1 || pattern2
}
```

---

## 2. Quick Judge 游戏机制

### 2.1 游戏概述

**状态**: ✅ 已实现

**核心玩法**:
- 显示英文单词 + 中文翻译
- 用户判断翻译是否正确
- 限时挑战 (Easy/Normal/Hard 三档)
- 连击系统与速度加成

### 2.2 难度系统

| 难度 | 时间限制 | 题目数量 | 最大错误 | 目标用户 |
|------|---------|---------|---------|---------|
| Easy | 10秒/题 | 5题 | 2次 | 初学者 |
| Normal | 8秒/题 | 10题 | 2次 | 已掌握基础 |
| Hard | 5秒/题 | 15题 | 1次 | 高水平玩家 |

### 2.3 评分系统

#### Easy模式评分

| 星级 | 准确率 | 平均时间 | 错误次数 |
|------|--------|---------|---------|
| ⭐⭐⭐ 3星 | ≥ 90% | < 7秒 | ≤ 1次 |
| ⭐⭐ 2星 | ≥ 70% | < 8秒 | ≤ 2次 |
| ⭐ 1星 | ≥ 50% | 任意 | 通过 |
| ✗ 0星 | < 50% | - | 失败 |

#### Normal模式评分

| 星级 | 准确率 | 平均时间 | 错误次数 |
|------|--------|---------|---------|
| ⭐⭐⭐ 3星 | ≥ 90% | < 5秒 | ≤ 1次 |
| ⭐⭐ 2星 | ≥ 75% | < 6秒 | ≤ 2次 |
| ⭐ 1星 | ≥ 60% | 任意 | 通过 |
| ✗ 0星 | < 60% | - | 失败 |

#### Hard模式评分

| 星级 | 准确率 | 平均时间 | 错误次数 |
|------|--------|---------|---------|
| ⭐⭐⭐ 3星 | ≥ 95% | < 3秒 | 0次 |
| ⭐⭐ 2星 | ≥ 80% | < 4秒 | ≤ 1次 |
| ⭐ 1星 | ≥ 70% | 任意 | 通过 |
| ✗ 0星 | < 70% | - | 失败 |

### 2.4 连击系统

| 连击数 | 奖励倍数 | 视觉效果 | 音效 |
|--------|---------|---------|------|
| 1-2 | 1.0x (无奖励) | 无 | 无 |
| 3-4 | 1.2x (+20%) | 🔥 单火焰 + 脉冲 | "Combo!" |
| 5-9 | 1.5x (+50%) | 🔥🔥 双火焰 + 屏幕微震 | "Great!" |
| 10+ | 2.0x (+100%) | 🔥🔥🔥 三火焰 + 粒子特效 | "Amazing!" |

**分数计算公式**:
```
基础分 = 100分 (答对) / -20分 (答错)
时间奖励 = (剩余时间 / 总时间) × 50分
连击加成 = 基础分 × (连击倍率 - 1.0)

总分 = 基础分 + 时间奖励 + 连击加成
```

---

## 3. 记忆算法系统

### 3.1 SM-2算法实现

**状态**: ✅ 已实现

**核心原理**:
- 间隔重复 (Spaced Repetition)
- 难度调整 (Ease Factor)
- 跨天复习 (Inter-Day Repetition)

### 3.2 记忆强度计算

```kotlin
/**
 * 计算记忆强度
 */
fun calculateMemoryStrength(
    currentStrength: Int,
    isCorrect: Boolean,
    isGuessed: Boolean,
    difficultyMultiplier: Float
): Int {
    // 基础增长
    val baseGrowth = if (isCorrect) 10 else -15

    // 猜测惩罚
    val guessPenalty = if (isGuessed) -5 else 0

    // 难度乘数
    val adjustedGrowth = (baseGrowth + guessPenalty) * difficultyMultiplier

    // 计算新强度
    val newStrength = currentStrength + adjustedGrowth.toInt()

    // 限制范围 [0, 100]
    return newStrength.coerceIn(0, 100)
}
```

### 3.3 复习间隔

| 记忆强度 | 复习间隔 | 状态 |
|---------|---------|------|
| < 30 | 10分钟后 | 需要巩固 |
| 30-49 | 1小时后 | 正在学习 |
| 50-69 | 4小时后 | 逐渐掌握 |
| 70-84 | 1天后 | 基本掌握 |
| 85-100 | 1周后 | 完全掌握 |

---

## 4. 提示系统

### 4.1 三级渐进提示

**状态**: ✅ 架构完成, 待UI集成

| 级别 | 内容 | 示例 (单词: banana) | 惩罚 |
|------|------|-------------------|------|
| 1 | 首字母 | "首字母: B" | 最高 2 星 |
| 2 | 前半部分 | "前半部分: ban___" | 最高 2 星 |
| 3 | 元音隐藏 | "完整单词(元音隐藏): b_n_n_" | 最高 2 星 |

### 4.2 使用限制

```kotlin
/**
 * 提示使用限制
 */
data class HintLimit(
    val maxHintsPerWord: Int = 3,
    val hintCooldownMs: Long = 3000L,  // 3秒
    val consecutiveLimit: Int = 1      // 连续使用限制
)
```

**规则**:
- 每词最多3次提示
- 提示间冷却3秒
- 使用提示后星级封顶2星
- 第3次提示后必须提交答案

---

## 5. 连击系统

### 5.1 连击定义

**Combo (连击)**: 连续正确答题且不中断的次数

**连击条件**:
- ✅ 答案正确
- ✅ 在时间限制内完成
- ✅ 没有使用提示 (Quick Judge 无提示功能)

**连击计数规则**:
```
答对 → Combo +1
答错 → Combo 归零
超时 → Combo 归零
```

### 5.2 连击奖励

| 连击数 | 奖励倍数 | 视觉效果 | 音效 |
|--------|---------|---------|------|
| 1-2 | 1.0x (无奖励) | 无 | 无 |
| 3-4 | 1.2x (+20%) | 🔥 单火焰 + 脉冲 | "Combo!" |
| 5-9 | 1.5x (+50%) | 🔥🔥 双火焰 + 屏幕微震 | "Great!" |
| 10+ | 2.0x (+100%) | 🔥🔥🔥 三火焰 + 粒子特效 | "Amazing!" |

---

## 6. 星级评分系统

### 6.1 动态星级评分

**状态**: ✅ 设计完成, 待实现

**评分因素**:
1. **准确率** (Accuracy)
   - 正确答案 / 总答案

2. **思考时间** (Thinking Time)
   - 防止盲目快速点击
   - 最小思考时间 = 1s + (0.5s × 单词长度)

3. **提示使用** (Hint Usage)
   - 使用提示: -1星 (最高2星)
   - 未使用提示: 可达3星

4. **错误次数** (Error Count)
   - 错误越多星级越低

5. **连击表现** (Combo Performance)
   - 高连击可以抵消1次错误

### 6.2 评分算法

```kotlin
/**
 * 计算星级评分
 */
fun calculateStars(
    correctCount: Int,
    totalCount: Int,
    avgTimeTaken: Long,
    minTimeThreshold: Long,
    hintsUsed: Int,
    maxCombo: Int,
    errorCount: Int
): Int {
    // 准确率 (0.0 - 1.0)
    val accuracy = correctCount.toFloat() / totalCount.toFloat()

    // 思考时间系数
    val timeCoefficient = when {
        avgTimeTaken < minTimeThreshold -> 0.7f  // 太快，可能是猜测
        avgTimeTaken > minTimeThreshold * 3 -> 0.8f // 太慢
        else -> 1.0f
    }

    // 基础星级
    val baseStars = when {
        accuracy >= 0.9f -> 3
        accuracy >= 0.7f -> 2
        accuracy >= 0.5f -> 1
        else -> 0
    }

    // 提示惩罚
    val hintPenalty = if (hintsUsed > 0) -1 else 0

    // 连击奖励 (高连击可以抵消1次错误)
    val comboBonus = when {
        maxCombo >= 10 -> 1
        maxCombo >= 5 -> 0
        else -> 0
    }

    // 最终星级
    val finalStars = baseStars + hintPenalty + comboBonus

    return finalStars.coerceIn(0, 3)
}
```

### 6.3 目标星级分布

| 难度 | 3星比例 | 2星比例 | 1星比例 | 0星比例 |
|------|---------|---------|---------|---------|
| Easy | ~60% | ~30% | ~10% | <5% |
| Normal | ~30% | ~50% | ~20% | <5% |
| Hard | ~10% | ~40% | ~50% | <5% |

---

## 📊 附录

### A. 数据模型

```kotlin
// Spell Battle 问题模型
data class SpellBattleQuestion(
    val wordId: String,
    val translation: String,
    val targetWord: String,
    val hint: String? = null,
    val difficulty: Int = 1
)

// Quick Judge 问题模型
data class QuickJudgeQuestion(
    val wordId: String,
    val english: String,
    val chinese: String,
    val isCorrect: Boolean,
    val difficulty: String
)

// 用户答题结果
data class AnswerResult(
    val isCorrect: Boolean,
    val timeTaken: Long,
    val hintsUsed: Int,
    val isGuessed: Boolean
)
```

### B. 配置常量

```kotlin
object GameConfig {
    // 思考时间
    const val MIN_THINK_TIME_BASE_MS = 1000L
    const val MIN_THINK_TIME_PER_LETTER_MS = 500L

    // 连击
    const val COMBO_LEVEL_1_THRESHOLD = 3
    const val COMBO_LEVEL_2_THRESHOLD = 5
    const val COMBO_LEVEL_3_THRESHOLD = 10

    // 连击奖励
    const val COMBO_MULTIPLIER_1 = 1.2f
    const val COMBO_MULTIPLIER_2 = 1.5f
    const val COMBO_MULTIPLIER_3 = 2.0f

    // 提示
    const val MAX_HINTS_PER_WORD = 3
    const val HINT_COOLDOWN_MS = 3000L

    // 星级评分
    const val ACCURACY_THRESHOLD_3_STAR = 0.9f
    const val ACCURACY_THRESHOLD_2_STAR = 0.7f
    const val ACCURACY_THRESHOLD_1_STAR = 0.5f
}
```

---

**文档状态**: ✅ 详细需求定义完成
**下一步**: UI/UX设计需求文档
