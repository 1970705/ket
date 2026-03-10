# 游戏模式需求文档

**文档版本**: v1.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**状态**: 详细需求定义
**优先级**: P0

---

## 📋 目录

1. [概述](#1-概述)
2. [已实现模式](#2-已实现模式)
3. [计划中模式](#3-计划中模式)
4. [模式对比](#4-模式对比)
5. [技术要求](#5-技术要求)
6. [数据模型](#6-数据模型)
7. [实施优先级](#7-实施优先级)

---

## 1. 概述

### 1.1 目标

Wordland 提供 **5 种游戏模式**，覆盖不同的学习风格和 KET 考试要求：

| 模式 | 学习目标 | KET 对应 | 状态 | 优先级 |
|------|---------|---------|------|--------|
| Spell Battle | 拼写能力 | Writing | ✅ 已实现 | - |
| Quick Judge | 快速识别 | Reading | ✅ 已实现 | - |
| Listen Find | 听力理解 | Listening | ⏳ 计划中 | P1 |
| Sentence Match | 语法应用 | Use of English | ⏳ 计划中 | P2 |
| Word Detective | 细节辨析 | Reading (Part 1-3) | ⏳ 计划中 | P2 |

### 1.2 设计原则

1. **游戏为先** - 每种模式都具备游戏性和挑战性
2. **即时反馈** - 所有操作都有视觉和听觉反馈
3. **渐进难度** - 从简单到复杂，逐步提升
4. **儿童友好** - 适合 10 岁儿童的操作和认知特点
5. **教育有效** - 符合 KET 考纲要求和认知科学

---

## 2. 已实现模式

### 2.1 Spell Battle (拼写战斗)

**状态**: ✅ 已完成
**实现日期**: 2026-02-16
**文件位置**: `ui/screens/LearningScreen.kt`

#### 2.1.1 核心玩法

```
显示中文翻译 → 用户拼写英文单词 → 实时反馈 → 提交验证 → 星级评分
```

#### 2.1.2 界面布局

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
│  [QWERTY 虚拟键盘...]                    │
│                                         │
│  [⌫ 删除]  [✓ 提交]  [💡 提示]           │
└─────────────────────────────────────────┘
```

#### 2.1.3 核心机制

**动态星级评分**:
```kotlin
评分因素:
- 准确率: 正确答案 / 总答案
- 思考时间: 1s + (0.5s × 单词长度) 最小阈值
- 提示使用: 使用提示最高 2 星
- 错误次数: 错误越多星级越低
- 连击表现: 高连击可抵消 1 次错误

星级标准:
- ⭐⭐⭐ 3星: 准确率 ≥90%, 无提示, 适当时间
- ⭐⭐ 2星: 准确率 ≥70%, 或使用了提示
- ⭐ 1星: 准确率 ≥50%
- ✗ 0星: 错误答案
```

**连击系统**:
| 连击数 | 奖励倍数 | 视觉效果 |
|--------|---------|---------|
| 1-2 | 1.0x | 无 |
| 3-4 | 1.2x (+20%) | 🔥 单火焰 + 脉冲 |
| 5+ | 1.5x (+50%) | 🔥🔥 双火焰 + 屏幕微震 |

**反作弊机制**:
```kotlin
// 时间阈值检测
最小思考时间 = 1s + (0.5s × 单词长度)
例如: 3字母单词 = 1.5s, 6字母单词 = 3.0s

// 惩罚
- 检测为猜测: 最高 1 星
- 记忆强度增长减半
- 不计入连击
```

#### 2.1.4 技术实现

**核心组件**:
```kotlin
// UI 组件
- SpellBattleGame           // 虚拟键盘和答案区
- AnswerAnimations          // 反馈动画
- ComboIndicator            // 连击显示
- ProgressBarEnhanced       // 进度条

// ViewModel
- LearningViewModel         // 游戏状态管理

// UseCase
- GetNextWordUseCase        // 获取下一题
- SubmitAnswerUseCase       // 提交答案
- UseHintUseCaseEnhanced    // 使用提示

// Domain Model
- SpellBattleQuestion       // 问题模型
- LearnWordResult           // 结果模型
```

**性能指标**:
- 帧率: ≥60fps
- 输入延迟: <50ms
- 反馈动画: 200-500ms

---

### 2.2 Quick Judge (快速判断)

**状态**: ✅ 已完成
**实现日期**: 2026-02-20
**文件位置**: `ui/screens/QuickJudgeScreen.kt`

#### 2.2.1 核心玩法

```
显示英文+中文 → 用户判断翻译是否正确 → 限时挑战 → 连击奖励
```

#### 2.2.2 界面布局

```
┌─────────────────────────────────────────┐
│  ← 返回    快速判断     ⏸️              │
├─────────────────────────────────────────┤
│  进度: [████░░░░] 3/10                   │
│  分数: 520   🔥 Combo: 5                 │
├─────────────────────────────────────────┤
│                                         │
│         ┌─────────┐                     │
│         │  APPLE  │                     │
│         │ 苹果     │                     │
│         └─────────┘                     │
│                                         │
│      ⏱️ 倒计时: 5s                      │
│                                         │
│   ┌──────────┐ ┌──────────┐             │
│   │    ❌    │ │    ✅    │             │
│   │  错误    │ │  正确    │             │
│   └──────────┘ └──────────┘             │
└─────────────────────────────────────────┘
```

#### 2.2.3 难度系统

| 难度 | 时间限制 | 题目数量 | 最大错误 | 目标用户 |
|------|---------|---------|---------|---------|
| Easy | 10秒/题 | 5题 | 2次 | 初学者 |
| Normal | 8秒/题 | 10题 | 2次 | 已掌握基础 |
| Hard | 5秒/题 | 15题 | 1次 | 高水平玩家 |

#### 2.2.4 评分标准

**Normal 模式**:
| 星级 | 准确率 | 平均时间 | 错误次数 |
|------|--------|---------|---------|
| ⭐⭐⭐ 3星 | ≥ 90% | < 5秒 | ≤ 1次 |
| ⭐⭐ 2星 | ≥ 75% | < 6秒 | ≤ 2次 |
| ⭐ 1星 | ≥ 60% | 任意 | 通过 |
| ✗ 0星 | < 60% | - | 失败 |

**Hard 模式**:
| 星级 | 准确率 | 平均时间 | 错误次数 |
|------|--------|---------|---------|
| ⭐⭐⭐ 3星 | ≥ 95% | < 3秒 | 0次 |
| ⭐⭐ 2星 | ≥ 80% | < 4秒 | ≤ 1次 |
| ⭐ 1星 | ≥ 70% | 任意 | 通过 |
| ✗ 0星 | < 70% | - | 失败 |

#### 2.2.5 连击系统

| 连击数 | 奖励倍数 | 视觉效果 | 音效 |
|--------|---------|---------|------|
| 1-2 | 1.0x | 无 | 无 |
| 3-4 | 1.2x | 🔥 + 脉冲 | "Combo!" |
| 5-9 | 1.5x | 🔥🔥 + 微震 | "Great!" |
| 10+ | 2.0x | 🔥🔥🔥 + 粒子 | "Amazing!" |

**分数计算**:
```
基础分 = 100分 (答对) / -20分 (答错)
时间奖励 = (剩余时间 / 总时间) × 50分
连击加成 = 基础分 × (连击倍率 - 1.0)

总分 = 基础分 + 时间奖励 + 连击加成
```

#### 2.2.6 技术实现

**核心组件**:
```kotlin
// UI 组件
- QuickJudgeScreen           // 主界面
- QuickJudgeOptions          // 选项按钮

// ViewModel
- QuickJudgeViewModel        // 游戏状态管理

// UseCase
- GenerateQuickJudgeQuestionsUseCase  // 生成题目
- SubmitQuickJudgeAnswerUseCase        // 提交判断

// Domain Model
- QuickJudgeQuestion         // 问题模型
- QuickJudgeResult           // 结果模型
```

---

## 3. 计划中模式

### 3.1 Listen Find (听音寻宝)

**状态**: ⏳ 计划中
**优先级**: P1
**预计工作量**: 10人天

#### 3.1.1 核心玩法

```
播放单词发音 → 显示4张图片选项 → 用户选择 → 正确/错误反馈
```

#### 3.1.2 界面布局

```
┌─────────────────────────────────────────┐
│  ← 返回    听音寻宝     [🔄 重播]        │
├─────────────────────────────────────────┤
│  进度: [███░░░░░░] 2/5                   │
│  🔥 Combo: 1                            │
├─────────────────────────────────────────┤
│                                         │
│         🎵 播放音频                      │
│      [ /\\\\\\ ] ← 音频波形              │
│                                         │
│   ┌────┐ ┌────┐ ┌────┐ ┌────┐          │
│   │ 🍎 │ │ 🍌 │ │ 🍊 │ │ 🍇 │          │
│   └────┘ └────┘ └────┘ └────┘          │
│                                         │
│   点击图片选择正确答案                   │
│                                         │
└─────────────────────────────────────────┘
```

#### 3.1.3 难度设置

| 难度 | 音频播放次数 | 选项数量 | 时间限制 | 干扰项 |
|------|-------------|---------|---------|--------|
| Easy | 2次 | 3张图片 | 无限制 | 无 |
| Normal | 1次 | 4张图片 | 15秒 | 1个相似词 |
| Hard | 1次 | 4张图片 | 10秒 | 2个相似词 |

#### 3.1.4 技术要求

**音频播放**:
```kotlin
// 使用 ExoPlayer
val player = ExoPlayer.Builder(context).build()
val mediaItem = MediaItem.fromUri(audioUri)

// 音频资源
- 格式: MP3, 128kbps
- 时长: 1-2秒/单词
- 语速: 正常 (1.0x)
```

**图片加载**:
```kotlin
// 使用 Coil
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageRes)
        .crossfade(true)
        .build(),
    contentDescription = word
)
```

#### 3.1.5 数据模型

```kotlin
data class ListenFindQuestion(
    val wordId: String,
    val word: String,              // "apple"
    val audioRes: String,          // "audio/apple.mp3"
    val images: List<ImageOption>, // 4个选项
    val difficulty: Int
)

data class ImageOption(
    val imageRes: String,          // "drawable/apple.png"
    val word: String,              // "apple"
    val isCorrect: Boolean
)
```

#### 3.1.6 实施优先级

**Phase 1** (基础功能):
- ✅ 音频播放系统
- ✅ 图片选项显示
- ✅ 点击选择和验证
- ✅ 基础反馈动画

**Phase 2** (增强功能):
- ✅ 音频波形可视化
- ✅ 难度自适应
- ✅ 连击系统集成

---

### 3.2 Sentence Match (句子配对)

**状态**: ⏳ 计划中
**优先级**: P2
**预计工作量**: 12人天

#### 3.2.1 核心玩法

```
显示带空位句子 → 显示单词选项 → 拖拽填空 → 语法检查 → 反馈
```

#### 3.2.2 界面布局

```
┌─────────────────────────────────────────┐
│  ← 返回    句子配对                       │
├─────────────────────────────────────────┤
│  进度: [███░░░░░░] 2/3                   │
├─────────────────────────────────────────┤
│                                         │
│  📖 句子:                               │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  I eat _____ for breakfast.     │   │
│  │      [     ]  ← 拖放目标区       │   │
│  └─────────────────────────────────┘   │
│                                         │
│  💡 单词选项:                          │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │apple │ │banana│ │orange│ │ grape│  │
│  └──────┘ └──────┘ └──────┘ └──────┘  │
│   ↑ 可拖拽                             │
│                                         │
│  [🔄 重置]  [✓ 提交]                   │
└─────────────────────────────────────────┘
```

#### 3.2.3 题型设计

**Level 1: 单空位句子**
```
"I eat _____ for breakfast."
选项: [apple, banana, orange, grape]
```

**Level 2: 多空位句子**
```
"She _____ to school by _____."
选项: [go, goes, bus, bike, car]
```

**Level 3: 语法变形**
```
"He _____ his teeth every morning."
选项: [brush, brushes, brushing, brushed]
```

#### 3.2.4 语法验证

```kotlin
/**
 * 语法检查规则
 */
fun validateGrammar(
    sentence: String,
    filledWord: String,
    blankPosition: Int
): GrammarResult {
    // 1. 词性检查
    val posRequired = getRequiredPartOfSpeech(sentence, blankPosition)
    val posProvided = getPartOfSpeech(filledWord)

    // 2. 主谓一致
    val subject = extractSubject(sentence)
    val verbAgreement = checkSubjectVerbAgreement(subject, filledWord)

    // 3. 时态一致性
    val sentenceTense = detectTense(sentence)
    val wordTense = detectTense(filledWord)

    return GrammarResult(
        isCorrect = posRequired == posProvided && verbAgreement && sentenceTense == wordTense,
        errorType = if (posRequired != posProvided) "词性错误" else
                    if (!verbAgreement) "主谓不一致" else
                    if (sentenceTense != wordTense) "时态错误" else null
    )
}
```

#### 3.2.5 拖拽系统

```kotlin
// 使用 Compose DragDrop
val dragAndDrop = rememberDragAndDropState(
    onDragEnd = { word, target ->
        viewModel.fillBlank(word, target)
    }
)

Box(
    modifier = Modifier
        .dragAndDropSource(
            dragAndDrop,
            data = word
        )
        .dragAndDropTarget(
            dragAndDrop,
            onDrop = { data ->
                val word = data as String
                fillBlank(word)
            }
        )
)
```

#### 3.2.6 数据模型

```kotlin
data class SentenceMatchQuestion(
    val id: String,
    val sentence: String,          // "I eat _____ for breakfast."
    val blanks: List<BlankInfo>,    // 空位信息
    val options: List<WordOption>,  // 可选单词
    val difficulty: Int
)

data class BlankInfo(
    val position: Int,             // 在句子中的位置
    val requiredPos: PartOfSpeech,  // 需要的词性
    val hint: String?               // 提示文本
)

data class WordOption(
    val word: String,
    val pos: PartOfSpeech,
    val form: WordForm              // 原形/变形
)

enum class PartOfSpeech {
    NOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION
}

enum class WordForm {
    BASE, SINGULAR, PLURAL, PAST, PRESENT, GERUND
}
```

---

### 3.3 Word Detective (单词侦探) ⭐ NEW

**状态**: ⏳ 计划中
**优先级**: P2
**预计工作量**: 11.5天 (约2.5周)
**建议实施**: Sprint 3-4

#### 3.3.1 核心玩法

```
显示两句话 → 找出单词差异 → 选中验证 → 限时挑战
```

#### 3.3.2 界面布局

```
┌─────────────────────────────────────────┐
│  🕵️ Word Detective - Level 3      [⏱] │
├─────────────────────────────────────────┤
│  找出两句话的不同之处！                  │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │  I have a [cat] 🏠               │  │
│  │  I have a [cut] 🔪               │  │
│  │         ↑ 点击差异处               │  │
│  └──────────────────────────────────┘  │
│                                         │
│  差异: 1/3 | ⏱ 剩余: 25秒               │
│                                         │
│  [提交答案]  [使用提示]                 │
└─────────────────────────────────────────┘
```

#### 3.3.3 难度分级设计

| 等级 | 找茬内容 | 示例 | 教育目标 |
|------|----------|------|----------|
| Easy | 单词拼写差异 | "I have a **cat**" vs "I have a **cut**" | 拼写辨析 |
| Normal | 语法差异 | "She **go** to school" vs "She **goes** to school" | 语法规则 |
| Hard | 语境差异 | "I **read** books" vs "I **red** books" | 同音词辨析 |

#### 3.3.4 游戏机制

**时间限制**:
- Easy: 无限制
- Normal: 30秒/题
- Hard: 20秒/题

**评分系统**:
```kotlin
基础分 = 100分 (找到所有差异)
时间奖励 = (剩余时间 / 总时间) × 50分
错误惩罚 = 选错 × (-20分)

总分 = 基础分 + 时间奖励 + 错误惩罚
```

**连击系统**:
- 连续3题正确：1.2x加成
- 连续5题正确：1.5x加成

#### 3.3.5 数据模型

```kotlin
data class DetectiveQuestion(
    val id: String,
    val leftSentence: String,           // "I have a cat"
    val rightSentence: String,          // "I have a cut"
    val diffWord: String,               // "cat/cut"
    val diffPosition: Int,              // 差异字符位置
    val difficulty: Int,                // 1-3
    val explanation: String,            // "Cat是猫，Cut是切"
    val timeLimit: Int? = null          // 秒数
)
```

#### 3.3.6 教育价值

| 认知能力 | 对应KET技能 | 价值评估 |
|---------|------------|---------|
| **视觉辨析** | Reading Part 1-3 | ⭐⭐⭐⭐⭐ |
| **细节观察** | 语法细节 | ⭐⭐⭐⭐☆ |
| **模式识别** | 词汇规则 | ⭐⭐⭐⭐☆ |
| **专注力训练** | 考试注意力 | ⭐⭐⭐⭐⭐ |

#### 3.3.7 技术要求

**核心技术**:
- 文本差异检测算法
- 点击区域精确映射
- 语音对比朗读

**性能指标**:
- 帧率: ≥60fps
- 点击响应: <50ms
- 内存占用: <100MB

#### 3.3.8 团队评审结果

| 评审角度 | 评分 | 关键意见 |
|---------|------|----------|
| 教育价值 | ⭐⭐⭐⭐☆ | 训练细节观察，适合KET Reading |
| 游戏设计 | ⭐⭐⭐⭐☆ | 定位Level 3-5，训练高级技能 |
| 技术可行性 | ⭐⭐⭐☆☆ | 成本较高，需11.5天 |

**综合建议**: 教育价值高但技术成本高，建议作为Sprint 3-4的P2优先级功能。

---

## 4. 模式对比

### 4.1 学习效果对比

| 维度 | Spell Battle | Quick Judge | Listen Find | Sentence Match | Word Detective |
|------|-------------|-------------|------------|---------------|----------------|
| **技能训练** | 拼写能力 | 快速识别 | 听力理解 | 语法应用 | 细节辨析 |
| **KET对应** | Writing | Reading | Listening | Use of English | Reading (1-3) |
| **认知负荷** | 高 | 中 | 中 | 高 | 中 |
| **记忆效果** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **游戏乐趣** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ |

### 4.2 适用场景对比

| 场景 | 推荐模式 | 理由 |
|------|---------|------|
| **初次学习新词** | Spell Battle | 拼写强化记忆 |
| **复习已学词汇** | Quick Judge | 快速识别巩固 |
| **听力训练** | Listen Find | 专注听觉 |
| **语法应用** | Sentence Match | 实际运用 |
| **细节辨析** | Word Detective | 训练观察力 |
| **碎片时间** | Quick Judge | 快节奏 |
| **专注学习** | Spell Battle | 深度练习 |

### 4.3 数据流对比

```
Spell Battle:
Word → Translation → User Input → Validation → Memory Update

Quick Judge:
Word → Translation Pair → User Judgment → Score Update → Memory Update

Listen Find:
Audio → Images → User Selection → Validation → Memory Update

Sentence Match:
Sentence → Options → Drag/Drop → Grammar Check → Memory Update

Word Detective:
Two Sentences → Compare → Find Differences → Validation → Memory Update
```

---

## 5. 技术要求

### 5.1 共享组件

```kotlin
// 所有模式共享的组件
- ComboManager                // 连击系统管理
- StarRatingCalculator        // 星级评分计算
- MemoryStrengthAlgorithm     // 记忆强度算法
- HapticFeedback              // 触觉反馈
- AnswerAnimations            // 答案动画

// 共享 UI 组件
- ProgressBarEnhanced         // 进度条
- ComboIndicator              // 连击指示器
- LevelCompleteCard           // 关卡完成卡片
- StarRatingDisplay           // 星级显示
```

### 5.2 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 帧率 | ≥60fps | 所有动画流畅 |
| 输入延迟 | <50ms | 用户操作响应 |
| 音频延迟 | <100ms | Listen Find 音频播放 |
| 图片加载 | <500ms | 图片资源加载 |
| 内存占用 | <150MB | 单个模式内存占用 |

### 5.3 兼容性要求

- Android API 24+ (Android 7.0+)
- 屏幕尺寸: 5.5" - 7"
- 方向: 竖屏 portrait
- 网络: 无需网络 (离线可用)

---

## 6. 数据模型

### 6.1 统一问题模型

```kotlin
/**
 * 游戏模式枚举
 */
enum class GameMode {
    SPELL_BATTLE,      // 拼写战斗
    QUICK_JUDGE,       // 快速判断
    LISTEN_FIND,       // 听音寻宝
    SENTENCE_MATCH,    // 句子配对
    WORD_DETECTIVE     // 单词侦探 (新增)
}

/**
 * 统一问题接口
 */
sealed class GameQuestion {
    abstract val id: String
    abstract val wordId: String
    abstract val mode: GameMode
    abstract val difficulty: Int
}

data class SpellBattleQuestion(
    override val id: String,
    override val wordId: String,
    val translation: String,
    val targetWord: String,
    override val difficulty: Int
) : GameQuestion() {
    override val mode = GameMode.SPELL_BATTLE
}

data class QuickJudgeQuestion(
    override val id: String,
    override val wordId: String,
    val english: String,
    val chinese: String,
    val isCorrect: Boolean,
    override val difficulty: Int
) : GameQuestion() {
    override val mode = GameMode.QUICK_JUDGE
}

data class ListenFindQuestion(
    override val id: String,
    override val wordId: String,
    val word: String,
    val audioRes: String,
    val images: List<ImageOption>,
    override val difficulty: Int
) : GameQuestion() {
    override val mode = GameMode.LISTEN_FIND
}

data class SentenceMatchQuestion(
    override val id: String,
    override val wordId: String,
    val sentence: String,
    val blanks: List<BlankInfo>,
    val options: List<WordOption>,
    override val difficulty: Int
) : GameQuestion() {
    override val mode = GameMode.SENTENCE_MATCH
}

data class DetectiveQuestion(
    override val id: String,
    override val wordId: String,
    val leftSentence: String,        // 左侧句子
    val rightSentence: String,       // 右侧句子
    val diffWord: String,            // 差异单词对 "cat/cut"
    val diffPosition: Int,           // 差异位置
    val explanation: String,         // 解释
    override val difficulty: Int
) : GameQuestion() {
    override val mode = GameMode.WORD_DETECTIVE
}
```

### 6.2 统一结果模型

```kotlin
/**
 * 游戏结果
 */
sealed class GameResult {
    abstract val isCorrect: Boolean
    abstract val timeTaken: Long
    abstract val score: Int
}

data class SpellBattleResult(
    override val isCorrect: Boolean,
    override val timeTaken: Long,
    override val score: Int,
    val stars: Int,
    val hintsUsed: Int,
    val isGuessed: Boolean
) : GameResult()

data class QuickJudgeResult(
    override val isCorrect: Boolean,
    override val timeTaken: Long,
    override val score: Int,
    val comboBonus: Int,
    val timeBonus: Int
) : GameResult()

data class ListenFindResult(
    override val isCorrect: Boolean,
    override val timeTaken: Long,
    override val score: Int,
    val attempts: Int
) : GameResult()

data class SentenceMatchResult(
    override val isCorrect: Boolean,
    override val timeTaken: Long,
    override val score: Int,
    val grammarErrors: List<GrammarError>
) : GameResult()
```

---

## 7. 实施优先级

### 7.1 当前状态

| 模式 | 状态 | 完成度 | 测试覆盖率 |
|------|------|--------|-----------|
| Spell Battle | ✅ 已完成 | 100% | 85% |
| Quick Judge | ✅ 已完成 | 100% | 90% |
| Listen Find | ⏳ 计划中 | 0% | - |
| Sentence Match | ⏳ 计划中 | 0% | - |
| Word Detective | ⏳ 计划中 | 0% | - |

### 7.2 实施路线图

**Phase 1: Quick Judge 增强** (1周) ✅
- ✅ 动态星级评分集成
- ✅ 完整的难度差异化
- ✅ 数值调优

**Phase 2: Listen Find 开发** (2-3周)
- ⏳ 音频播放系统
- ⏳ 图片选项生成
- ⏳ 拖拽选择
- ⏳ 验证和反馈

**Phase 3: Sentence Match 开发** (3-4周)
- ⏳ 拖拽系统
- ⏳ 语法检查引擎
- ⏳ 句子生成
- ⏳ 验证和反馈

**Phase 4: Word Detective 开发** (2-3周) ⭐ NEW
- ⏳ 文本差异检测算法
- ⏳ 差异区域映射
- ⏳ 点击验证系统
- ⏳ 语音对比朗读

### 7.3 资源需求

**Listen Find 需求**:
- 音频资源: 180个单词录音 (MP3格式)
- 图片资源: 每词 4张图片 (共720张)
- 录音质量: 专业配音，标准英音

**Sentence Match 需求**:
- 句子库: 200个练习句子
- 语法规则: 覆盖KET考点
- 选项生成: 自动生成干扰项

---

## 8. 验收标准

### 8.1 功能验收

- [ ] 所有模式可从主界面访问
- [ ] 所有模式正确记录学习进度
- [ ] 所有模式集成动态星级评分
- [ ] 所有模式集成连击系统
- [ ] 所有模式支持离线使用

### 8.2 性能验收

- [ ] 所有模式保持 60fps
- [ ] 模式切换 <500ms
- [ ] 音频播放延迟 <100ms
- [ ] 内存占用 <150MB/模式

### 8.3 教育验收

- [ ] 覆盖 KET 听说读写四项技能
- [ ] 单词保留率提升 >30%
- [ ] 用户满意度 ≥4.0/5.0
- [ ] 家长满意度 ≥4.0/5.0

---

**文档状态**: ✅ 游戏模式需求定义完成
**下一步**: UI/UX 设计文档 (04-ui-ux-design.md)
