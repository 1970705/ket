# 游戏模式优先级矩阵与详细设计

**Document Version**: 1.0
**Created**: 2026-02-17
**Designer**: game-designer
**Companion**: ONBOARDING_DESIGN_V2_FINAL.md

---

## 游戏模式优先级矩阵

```
好玩程度
   High │
       │    ╱──── Word Match (配对游戏)
       │   ╱  好玩: ⭐⭐⭐⭐⭐  难度: ⭐⭐
       │  ╱   MVP: ✅  Week: 3
       │ ╱
       │╱───── Boss Battle (Boss战)
       │  好玩: ⭐⭐⭐⭐⭐  难度: ⭐⭐⭐⭐
       │  MVP: ❌  Week: 5
       │
   Med │──── Speed Challenge (限时挑战)
       │  好玩: ⭐⭐⭐⭐  难度: ⭐⭐⭐
       │  MVP: ✅  Week: 3
       │
       │──── Listening Challenge (听音选词)
       │  好玩: ⭐⭐⭐⭐  难度: ⭐⭐⭐
       │  MVP: ✅  Week: 5
       │
   Low │──── Fill Blanks (句子填空)
       │  好玩: ⭐⭐⭐  难度: ⭐⭐
       │  MVP: ✅  Week: 4
       │
       │──── Spell Battle (拼写战斗)
       │  好玩: ⭐⭐⭐  难度: ⭐
       │  MVP: ✅  Week: 0 (已实现)
       │
       └──────────────────────────────────────
           Low        High
               实施难度
```

---

## 游戏模式详细设计

### 1. Multiple Choice (选择题) - P0

**优先级**: ⭐⭐⭐⭐⭐ (MVP必须)
**实施难度**: ⭐⭐
**好玩程度**: ⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  MULTIPLE CHOICE - 选择题模式                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         [图片: 🐱 Cat Illustration]                 │      │
│  │         200x150px, 圆角12dp                          │      │
│  │                                                     │      │
│  │         "猫" 的英文是什么？                          │      │
│  │         What is "猫" in English?                    │      │
│  │                                                     │      │
│  │    ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │    │   DOG   │  │   CAT   │  │  BIRD   │  │  FISH   │  │
│  │    │   🐕    │  │   🐱    │  │   🐦    │  │   🐟    │  │
│  │    └─────────┘  └─────────┘  └─────────┘  └─────────┘  │
│  │                                                     │      │
│  │         [选中时: 放大1.1x + 边框高亮]                 │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  宠物反馈: 🦉 "我知道你能做到！"                                 │
│                                                                 │
│  答对后:                                                        │
│  - ✅ 绿色对勾飞入                                              │
│  - ⭐ 1颗星星                                                   │
│  - 🎵 "叮"成功音效                                              │
│  - 🦊 跳跃庆祝                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- 第一题永远是CAT (最简单，100%成功率)
- 4个选项: 1个正确 + 3个干扰项
- 干扰项选择: 同类别的单词 (都是动物)
- 图片: 复用现有的单词插图资源

**数据结构**:
```kotlin
data class MultipleChoiceQuestion(
    val word: Word,
    val correctAnswer: String,
    val options: List<String>, // 4个选项
    val imageUrl: String
)

// 生成选择题
fun generateMultipleChoiceQuestion(
    targetWord: Word,
    allWords: List<Word>
): MultipleChoiceQuestion {
    val sameCategoryWords = allWords
        .filter { it.category == targetWord.category && it.id != targetWord.id }
        .shuffled()
        .take(3)

    val options = (sameCategoryWords.map { it.english } + targetWord.english)
        .shuffled()

    return MultipleChoiceQuestion(
        word = targetWord,
        correctAnswer = targetWord.english,
        options = options,
        imageUrl = targetWord.imageUrl
    )
}
```

---

### 2. Fill Blanks (填空题) - P0

**优先级**: ⭐⭐⭐⭐⭐ (MVP必须)
**实施难度**: ⭐⭐
**好玩程度**: ⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  FILL BLANKS - 填空题模式 (简单版)                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         [图片: 🍎 Apple Illustration]              │      │
│  │                                                     │      │
│  │         Apple = A____                               │      │
│  │         苹果 = 苹__                                  │      │
│  │                                                     │      │
│  │    选择缺失的字母 (按顺序):                           │      │
│  │                                                     │      │
│  │    当前需要: P                                       │      │
│  │                                                     │      │
│  │    ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐             │      │
│  │    │ A │ │ P │ │ L │ │ E │ │ O │ │ W │             │      │
│  │    └───┘ └───┘ └───┘ └───┘ └───┘ └───┘             │      │
│  │                                                     │      │
│  │         [选中P后，填入空位，动画效果]                  │      │
│  │                                                     │      │
│  │         Apple = AP__                                │      │
│  │         下一个: P                                    │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  宠物反馈: 🐱 "对了！继续加油！"                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- 首字母已给出 (降低难度)
- 字母池只包含正确字母 + 1-2个干扰项
- 逐个字母填入 (不是一次性拼写)
- 每填对一个字母 → 宠物庆祝

**数据结构**:
```kotlin
data class FillBlankQuestion(
    val word: Word,
    val firstLetter: Char,
    val remainingLetters: List<Char>, // 正确答案的剩余字母
    val letterPool: List<Char>, // 字母池 (包含干扰项)
    val currentProgress: String // 当前进度，如 "AP__"
)

// 生成填空题
fun generateFillBlankQuestion(word: Word): FillBlankQuestion {
    val letters = word.english.toList()
    val firstLetter = letters.first()
    val remainingLetters = letters.drop(1)

    // 字母池 = 正确字母 + 2个干扰项
    val distractors = ("A".."Z")
        .filter { it !in letters }
        .shuffled()
        .take(2)

    val letterPool = (remainingLetters + distractors).shuffled()

    return FillBlankQuestion(
        word = word,
        firstLetter = firstLetter,
        remainingLetters = remainingLetters,
        letterPool = letterPool,
        currentProgress = "$firstLetter${"_".repeat(letters.size - 1)}"
    )
}
```

---

### 3. Spell Battle (拼写题) - P0 (已实现)

**优先级**: ⭐⭐⭐⭐⭐ (已实现)
**实施难度**: ⭐ (已有代码)
**好玩程度**: ⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  SPELL BATTLE - 拼写题模式 (已实现，增强版本)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         [图片: 👁️ Eye Illustration]                │      │
│  │                                                     │      │
│  │         Spell: EYE                                  │      │
│  │         _ _ _                                       │      │
│  │                                                     │      │
│  │    💡 Hint: 第一个字母是 E                          │      │
│  │                                                     │      │
│  │    [虚拟键盘 - 已实现]                               │      │
│  │    Q W E R T Y U I O P                              │      │
│  │    A S D F G H J K L                               │      │
│  │    Z X C V B N M ⌫                                 │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  增强功能 (新增):                                                │
│  - 首次拼写时显示首字母提示                                      │
│  - 拼对每个字母 → 小火花特效                                     │
│  - 拼错字母 → 轻微摇晃 + 红色闪烁                                │
│  - 完成后 → 星星评级 (1-3⭐)                                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### 4. Word Match (配对游戏) - P1

**优先级**: ⭐⭐⭐⭐ (MVP+)
**实施难度**: ⭐⭐
**好玩程度**: ⭐⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  WORD MATCH - 配对游戏模式                                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │  ⏱️ 60s   Score: 0    Combo: 0                      │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         │
│  │  看见   │  │  LOOK   │  │  看    │  │  WATCH  │         │
│  │   👁️   │  │         │  │   👀   │  │         │         │
│  │  [卡片] │  │  [卡片] │  │  [卡片] │  │  [卡片] │         │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘         │
│                                                                 │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         │
│  │   玻璃  │  │  GLASS  │  │  观察  │  │   SEE   │         │
│  │   🥛   │  │         │  │   🔍   │  │         │         │
│  │  [卡片] │  │  [卡片] │  │  [卡片] │  │  [卡片] │         │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘         │
│                                                                 │
│  游戏流程:                                                      │
│  1. 点击"看见" → 卡片翻转 🔴                                    │
│  2. 点击"LOOK" → 卡片翻转 🔴                                    │
│  3. 配对成功! ✅ → 卡片消失 → +10分 → ⭐特效                    │
│  4. 继续配对...                                                 │
│                                                                 │
│  配对错误:                                                      │
│  1. 点击"看见" → 卡片翻转 🔴                                    │
│  2. 点击"WATCH" → 卡片翻转 🔴                                   │
│  3. 配对失败! ❌ → 卡片翻回 → 宠物摇头                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- 两种配对模式: 中文-英文、图片-单词
- 计时模式: 60秒挑战
- 配对成功: 卡片消失 + 特效 + 时间奖励 (+5秒)
- 连击奖励: 连续3对正确 → 2x分数

**数据结构**:
```kotlin
data class WordMatchGame(
    val pairs: List<CardPair>,
    val timeLimit: Int = 60,
    val currentTime: Int,
    val score: Int,
    val combo: Int
)

data class CardPair(
    val id: String,
    val left: Card,
    val right: Card,
    val isMatched: Boolean
)

data class Card(
    val id: String,
    val content: CardContent,
    val isFlipped: Boolean,
    val isSelected: Boolean
)

sealed class CardContent {
    data class Chinese(val text: String, val emoji: String?) : CardContent()
    data class English(val text: String) : CardContent()
    data class Image(val url: String) : CardContent()
}

// 生成配对游戏
fun generateWordMatchGame(words: List<Word>): WordMatchGame {
    val pairs = words.take(6).map { word ->
        CardPair(
            id = word.id,
            left = Card(
                id = "${word.id}_left",
                content = CardContent.Chinese(word.chinese, word.emoji),
                isFlipped = false,
                isSelected = false
            ),
            right = Card(
                id = "${word.id}_right",
                content = CardContent.English(word.english),
                isFlipped = false,
                isSelected = false
            ),
            isMatched = false
        )
    }

    return WordMatchGame(
        pairs = pairs.shuffled(),
        timeLimit = 60,
        currentTime = 60,
        score = 0,
        combo = 0
    )
}
```

---

### 5. Speed Challenge (限时挑战) - P1

**优先级**: ⭐⭐⭐⭐ (MVP+)
**实施难度**: ⭐⭐⭐
**好玩程度**: ⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  SPEED CHALLENGE - 限时挑战模式                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         ⏱️ 45s    ⭐ 5连击!    Score: 50            │      │
│  │                                                     │      │
│  │         ████████████░░░░░░░░ 时间条                 │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         [图片: 🌈 Rainbow]                          │      │
│  │                                                     │      │
│  │         Spell: RAINBOW                              │      │
│  │         _ _ _ _ _ _ _                              │      │
│  │                                                     │      │
│  │    [虚拟键盘]                                        │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  规则:                                                          │
│  - 初始时间: 60秒                                               │
│  - 拼对1个单词: +10秒                                           │
│  - 拼错1个单词: -5秒                                            │
│  - 连击3+: +5秒额外奖励                                         │
│  - 时间归零: 游戏结束                                           │
│                                                                 │
│  难度递进:                                                      │
│  - 单词1-3: 3-4字母 (简单)                                     │
│  - 单词4-6: 4-5字母 (中等)                                     │
│  - 单词7+: 5+字母 (困难)                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- 时间压力 = 刺激感
- 拼对奖励时间 (正向循环)
- 难度递增保持挑战
- 结束时显示最高分

---

### 6. Listening Challenge (听音选词) - P2

**优先级**: ⭐⭐⭐ (扩展功能)
**实施难度**: ⭐⭐⭐
**好玩程度**: ⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  LISTENING CHALLENGE - 听音选词模式                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         🔊 点击播放发音                              │      │
│  │         [▶️ 大播放按钮]                              │      │
│  │                                                     │      │
│  │         [音波动画]                                   │      │
│  │         ∿∿∿∿∿∿∿∿∿∿                                   │      │
│  │                                                     │      │
│  │         听到的单词是?                                │      │
│  │                                                     │      │
│  │    ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │    │   🐕   │  │   🐱   │  │   🐦   │  │   🐟   │  │
│  │    │  DOG   │  │  CAT   │  │  BIRD  │  │  FISH  │  │
│  │    └─────────┘  └─────────┘  └─────────┘  └─────────┘  │
│  │                                                     │      │
│  │         [重复播放] [显示提示]                         │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  进阶版本 (听音拼写):                                            │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │         🔊 播放中... [🔊🔊🔊]                         │      │
│  │                                                     │      │
│  │         Spell the word you hear                     │      │
│  │         _ _ _ _ _ _                                 │      │
│  │                                                     │      │
│  │         [再次播放] [慢速播放]                         │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- 需要音频资源 (TTS或录制)
- 初级: 听音选图 (4选1)
- 高级: 听音拼写
- 播放控制: 正常速度 / 慢速 / 重复

---

### 7. Boss Battle (Boss战) - P2

**优先级**: ⭐⭐⭐ (高级功能)
**实施难度**: ⭐⭐⭐⭐
**好玩程度**: ⭐⭐⭐⭐⭐

```
┌─────────────────────────────────────────────────────────────────┐
│  BOSS BATTLE - 关卡Boss战                                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────┐      │
│  │                                                     │      │
│  │              👑 BOSS: 长单词怪                      │      │
│  │                                                     │      │
│  │              ████████░░ 80%                        │      │
│  │              血条 Health                           │      │
│  │                                                     │      │
│  │         挑战: 拼写 "BREAKFAST"                      │      │
│  │               _ _ _ _ _ _ _ _ _                    │      │
│  │                                                     │      │
│  │    [用户拼对 B]                                      │      │
│  │                                                     │      │
│  │              [宠物攻击动画] 💥                       │      │
│  │                                                     │      │
│  │              ████████░░ 70%  (-10%)                │      │
│  │                                                     │      │
│  │         当前进度: B_R_E_A_K_F_A_S_T                 │      │
│  │                     ✅ ✅ ✅                        │      │
│  │                                                     │      │
│  │    💡 提示: 早上吃的第一餐                           │      │
│  │    [3 hints available]                             │      │
│  │                                                     │      │
│  └─────────────────────────────────────────────────────┘      │
│                                                                 │
│  击败Boss奖励:                                                  │
│  - 🏆 "Boss击败者" 成就                                         │
│  - 💎 100宝石                                                   │
│  - 🎁 神秘宝箱                                                  │
│  - 🚀 解锁下一区域                                              │
│                                                                 │
│  Boss设计:                                                      │
│  - Level 1 Boss: BREAKFAST (9字母)                             │
│  - Level 2 Boss: BIRTHDAY (8字母)                              │
│  - Level 3 Boss: AFTERNOON (9字母)                             │
│  - Level 4 Boss: CHALLENGE (9字母)                             │
│  - Level 5 Boss: BEAUTIFUL (9字母)                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**设计要点**:
- Boss = 该关卡最长的单词
- 每拼对一个字母 = Boss掉血
- 宠物攻击动画 = 奖励视觉反馈
- 击败Boss = 关卡高潮体验

**Boss血量计算**:
```kotlin
data class BossState(
    val bossName: String,
    val bossWord: String,
    val currentHealth: Int,
    val maxHealth: Int,
    val phase: BossPhase // 可选: 多阶段Boss
)

// 血量 = 字母数 * 10
fun calculateBossHealth(word: String): Int {
    return word.length * 10
}

// 每个字母掉血
fun damagePerLetter(word: String): Int {
    return calculateBossHealth(word) / word.length
}
```

---

## 游戏模式对比总结

| 模式 | 好玩 | 难度 | MVP | Week | 主要特点 |
|------|-----|------|-----|------|---------|
| Multiple Choice | ⭐⭐⭐⭐ | ⭐⭐ | ✅ | 1 | 100%成功率，建立信心 |
| Fill Blanks | ⭐⭐⭐⭐ | ⭐⭐ | ✅ | 1 | 渐进式，首字母提示 |
| Spell Battle | ⭐⭐⭐ | ⭐ | ✅ | 0 | 已实现，需增强反馈 |
| Word Match | ⭐⭐⭐⭐⭐ | ⭐⭐ | ✅ | 3 | 配对游戏，最有趣 |
| Speed Challenge | ⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | 3 | 时间压力，刺激 |
| Listening | ⭐⭐⭐⭐ | ⭐⭐⭐ | ❌ | 5 | 需音频资源 |
| Boss Battle | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ❌ | 5 | 关卡高潮，需动画 |

---

**文档状态**: 完成
**下一步**: 开发优先级确认
