# Wordland MVP 完整实施计划 (12周, 2岛180词)

**版本**: v1.0
**日期**: 2026-02-14
**状态**: 待审批
**制定人**: Claude (Plan Agent)

---

## 执行摘要

基于ELICITATE阶段的核心发现，本计划详细规划了Wordland游戏化KET词汇学习应用的MVP开发，目标是在12周内完成2.5个主题世界（180个核心KET词汇），采用Kotlin + Jetpack Compose原生Android开发，实现双岛屿渐进解锁系统、跨场景验证机制和智能记忆系统。

**核心验证目标**：
- 应用能力提升：能在句子/对话中正确使用所学词汇
- 考试提分：2周后KET模拟考提升5-10分
- 参与度提升：次日留存率≥50%，单次游戏时长≥8分钟

---

## 目录

- [一、ELICITATE阶段核心发现](#一elicitate阶段核心发现)
- [二、详细设计文档](#二详细设计文档)
- [三、技术架构文档](#三技术架构文档)
- [四、开发计划](#四开发计划)
- [五、附录](#五附录)

---

## 一、ELICITATE阶段核心发现

### 1.1 方向A：游戏设计

**核心方案**：双岛屿渐进解锁系统

| 关键决策 | 依据 |
|---------|------|
| 双岛屿（MVP） | 验证核心假设，可落地 |
| 渐进解锁（60%精通度） | Netflix式期待感 |
| 自由探索（岛内） | Minecraft式无压力 |
| 学习渐进（知识不跳跃） | 能力适配，不超出范围 |
| 三级提示（防挫败） | 解释清楚为什么错 |

**验证目标**：孩子会为了解锁下个岛而努力吗？

### 1.2 方向B：教育方法

**核心方案**：三级防蒙 + 记忆强度系统

| 关键决策 | 依据 |
|---------|------|
| 跨场景验证（最高优先级） | 防止记位置，确保真理解 |
| 三级验证 + 跨关卡追踪 | 够用，不用太复杂 |
| 记忆强度模型（优先级最高） | 个性化复习间隔 |
| 行为分析（响应时间<2秒） | 检测蒙答行为 |

**验证目标**：跨场景验证能有效防止蒙混？

### 1.3 方向C：技术实现

**核心方案**：Native Android + 可扩展架构

| 关键决策 | 依据 |
|---------|------|
| Kotlin + Compose | 现代化，效果导向 |
| 纯本地 + 简单视角切换 | MVP快速落地 |
| 可扩展数据模型 | 为云同步、社交预留接口 |
| SQLite + Room | 记忆强度模型不复杂 |

**验证目标**：12周内完成MVP（2岛、180词）

### 1.4 方向D：产品验证

**核心方案**：应用能力 + 考试提分 + 前后对比

| 关键决策 | 依据 |
|---------|------|
| 应用能力（最高价值） | 会用 > 记住 |
| 考试提分（家长最关心） | KET模拟考分数提升 |
| 前后对比验证（不做A/B测试） | 使用前后的真实变化 |

**验证目标**：2周后KET模拟考提升5-10分

---

## 二、详细设计文档

### 2.1 双岛屿系统详细设计

#### 系统架构

```
双岛屿渐进解锁系统
│
├── Look Island（看之岛）【已解锁】
│   ├── 60个核心词汇
│   ├── 8个关卡（教学关×1 + 普通关×5 + Boss关×1 + 复习关×1）
│   ├── 4种玩法循环
│   ├── 精通度目标：60%（解锁下一岛）
│   └── 主题：look, see, watch, find, notice
│
├── Move Valley（动之谷）【需Look Island 60%精通度】
│   ├── 60个核心词汇
│   ├── 8个关卡（同结构）
│   ├── 引入跨场景验证
│   ├── 精通度目标：60%
│   └── 主题：go, come, move, run, walk, jump
│
└── Say City（说之城）【前半部分，MVP阶段】
    ├── 60个核心词汇（前30词完整流程）
    ├── 6个关卡（前30词完整流程）
    ├── 引入词根萌芽系统
    └── 主题：say, tell, ask, speak, talk
```

#### 精通度计算模型

```kotlin
data class IslandMastery(
    val islandId: String,
    val totalWords: Int,
    val masteredWords: Int,
    val levelProgress: Map<String, LevelProgress>,
    val crossSceneScore: Double  // 跨场景验证分数
) {
    val masteryPercentage: Double
        get() {
            // 70% 单词掌握 + 30% 跨场景应用
            val wordMastery = (masteredWords.toDouble() / totalWords) * 0.7
            val crossSceneMastery = crossSceneScore * 0.3
            return (wordMastery + crossSceneMastery) * 100
        }

    val isNextIslandUnlocked: Boolean
        get() = masteryPercentage >= 60.0
}
```

#### 岛屿内探索机制（Minecraft式）

**自由探索规则**：

1. **解锁机制**：
   - 完成教学关后解锁前3个普通关卡
   - 每完成1个关卡，解锁下一个
   - Boss关需前面5个普通关卡全3星解锁

2. **重玩机制**：
   - 已完成关卡可无限重玩
   - 重玩不影响首次通关奖励
   - 重玩可获得"练习能量"（兑换提示）

3. **难度选择**：
   - 每个关卡有3个难度：简单/普通/挑战
   - 简单：更多提示，更慢语速，更少选项
   - 普通：标准难度
   - 挑战：更少提示，更快语速，更多混淆项
   - 难度选择不影响精通度计算（基于普通难度）

4. **地图可视化**：
   ```
   🏝️ Look Island Map
   ┌──────────────────────────┐
   │                    │
   │ [🎓教学]──→[⭐关卡1]✅ ─→│
   │              ↓         │
   │           [⭐关卡2]✅ ─→│
   │              ↓         │
   │           [⭐关卡3]⭐ ─→│
   │              ↓         │
   │        [📅复习]🔖 ← [🏝️关卡4]✅ ─→│
   │              ↓         │
   │           [⭐关卡5]✅ ───│
   │                    │
   │ 精通度: ███████░░ 62%      │
   │ 下一岛解锁: ✅ 已解锁         │
   └──────────────────────────┘
   ```

### 2.2 关卡设计详解

#### 单个关卡结构（约6-8分钟）

```yaml
关卡模板：
  id: "look_island_level_01"
  名称: "Just Look!"
  主题: "look的基本含义"
  时长: 6分钟
  新词: 3-4个
  复习词: 2-3个
  玩法类型: "listen_find"  // 听音寻宝

  阶段:
    1. 热身 (30秒):
       - 单词复习题
       - 激活已有知识

    2. 新词学习 (2分钟):
       - 第1个新词：情境演示 → 听音 → 跟读
       - 第2个新词：情境演示 → 听音 → 跟读
       - 第3个新词：情境演示 → 听音 → 跟读
       - 每1个词配1个简单练习

    3. 应用练习 (2.5分钟):
       - 混合练习新词和复习词
       - 5-8道题目
       - 即时反馈 + 三级提示

    4. 巩固测试 (1分钟):
       - 不给提示的快速测试
       - 测试本关掌握度

    5. 关卡结算 (30秒):
       - 显示得分、星星、奖励
       - 解锁内容预览
```

#### 2.5个主题世界详细设计

##### **Look Island（看之岛）- 60词**

```
关卡列表：
├── Level 0: 教学关 "Hello, Look!" (5分钟)
│   └── 教学：什么是"look" + 基础操作
│
├── Level 1: "Just Look!" (6分钟)
│   ├── 新词: look, see
│   ├── 复习词: -
│   └── 玩法: 听音寻宝
│
├── Level 2: "Look or See?" (7分钟)
│   ├── 新词: watch
│   ├── 复习词: look, see
│   └── 重点: look vs see 区分
│   └── 玩法: 句子配对
│
├── Level 3: "Watch It!" (6分钟)
│   ├── 新词: find, notice
│   ├── 复习词: look, see, watch
│   └── 玩法: 拼写战斗
│
├── Level 4: "I Can Find It!" (7分钟)
│   ├── 新词: look at, look for
│   ├── 复习词: 全部
│   └── 重点: 短语应用
│   └── 玩法: 快速判断
│
├── Level 5: "Looking Around!" (7分钟)
│   ├── 新词: look after, look out
│   ├── 复习词: 全部
│   └── 重点: 短语应用
│   └── 玩法: 综合应用
│
├── Boss关: "Look Master Challenge" (10分钟)
│   ├── 全部60词综合测试
│   ├── 4种玩法混合
│   ├── 跨场景初步验证
│   └── 通过奖励：解锁Move Valley
│
└── 复习关（第2天触发）: "Look Review" (5分钟)
   └── 不给提示测试记忆保持
```

**Look Island词汇清单（60词）**：

| 核心 | 短语 | 相关 |
|------|------|------|
| look | look at | eye |
| see | look for | glasses |
| watch | look after | camera |
| find | look out | picture |
| notice | look forward to | photo |
| - | look up | television |
| - | look in | computer |
| - | look on | phone |
| - | look over | magazine |
| - | look through | newspaper |

（详细60词表见附录A）

##### **Move Valley（动之谷）- 60词**

```
关卡结构同Look Island，但增加：

1. 跨场景验证：
   - "在Look Island中用过的词"
   - 场景切换：从Look的场景切换到Move的动作

2. 复习Look Island词汇：
   - Level 1-5中每关复习2-3个Look词

3. 联想学习：
   - "Look at me running!" (look + run)
   - "See me jumping!" (see + jump)
```

**Move Valley词汇清单（60词）**：

| 核心 | 短语 | 相关 |
|------|------|------|
| go | go to | sport |
| come | come back | game |
| move | move on | playground |
| run | run away | race |
| walk | walk to | park |
| jump | - | garden |
| - | - | street |
| - | - | bicycle |
| - | - | car |
| - | - | bus |

（详细60词表见附录A）

##### **Say City（说之城）- 前60词（MVP完成前30词）**

```
MVP阶段：
├── Level 0-5: 前30词完整实现
│   ├── 引入"词根萌芽"系统
│   ├── 词根彩蛋："say和sayings长得好像！"
│   └── 关卡结构完整
│
└── Level 6+: 后30词基础框架
   ├── 关卡结构完整
   ├── 内容占位（开发优先级低）
   └── 为下一版本准备
```

**Say City词汇清单（60词）**：

| 核心 | 短语 | 相关 |
|------|------|------|
| say | say hello | conversation |
| tell | tell a story | message |
| ask | ask for | question |
| speak | speak up | voice |
| talk | talk to | language |

（详细60词表见附录A）

### 2.3 UI/UX线框图

#### 2.3.1 世界地图界面

```
┌────────────────────────────────────┐
│ [头像] [能量]⭐245 [设置]             │ ← 顶部栏
├────────────────────────────────────┤
│                                          │
│        🗺️ Wordland Map                   │
│        语言驱动的世界                   │
│                                          │
│    ┌────────────────────┐              │
│    │  🏝️ Look Island          │              │
│    │  ████████░░ 62%        │              │
│    │  ✅ 已精通               │              │
│    └────────────────────┘              │
│            ↓ ✅                          │
│    ┌────────────────────┐              │
│    │  🏔️ Move Valley         │              │
│    │  ██████░░░░░ 45%        │              │
│    │  ⏸️ 进行中 (Level 3)     │              │
│    └────────────────────┘              │
│            ↓ 🔒                          │
│    ┌────────────────────┐              │
│    │  🌆 Say City             │              │
│    │  ░░░░░░░░░░ 0%         │              │
│    │  🔒 需Move Valley 60%   │              │
│    └────────────────────┘              │
│                                          │
│    ┌─────┐ ┌─────┐ ┌─────┐             │
│    │词书│ │成就│ │任务│             │
└────────────────────────────────────┘
```

**交互说明**：
- 点击岛屿：进入关卡选择界面
- 滚动地图：查看更多世界（未来扩展）
- 能量值：每局游戏消耗5能量，自然恢复1/5分钟
- 星星数：通关获得，解锁新关卡条件

#### 2.3.2 关卡选择界面

```
┌────────────────────────────────────┐
│ [←返回]     🏝️ Look Island           │
├────────────────────────────────────┤
│                                          │
│ 精通度: ████████░░ 62%                   │
│ 下一岛解锁: ✅ 已解锁                    │
│                                          │
│    ┌─────┐                 ┌─────┐      │
│    │🎓教学│ ✅      ┌─────┐│      │
│    │🏠关卡1│ ✅ ─→ │🏠关卡2││      │
│    └─────┘       └─────┘│      │
│          ↓              └─────┐│      │
│          ┌─────────────┐ │🏠关卡3││      │
│          │📅复习│ 🔒 ← ┌─────┤│      │
│          └─────────────┘ │🏠关卡4││      │
│                         ↓      └─────┘│      │
│                         ┌─────────────┐      │
│                         │🏠关卡5│ ✅ ────┘      │
│                         └─────────────┘      │
│                                          │
│    ┌─────────────────────────────┐    │
│    │📅 每日复习               │    │
│    │ 🔰 需明天解锁 (3词待复习)│    │
│    └─────────────────────────────┘    │
└────────────────────────────────────┘
```

**状态标识**：
- ✅ = 已完成（1-3星）
- 🔒 = 未解锁
- ⏸️ = 进行中
- 数字 = 星星数量

#### 2.3.3 游戏内界面（听音寻宝玩法）

```
┌────────────────────────────────────┐
│ Level 3: "Watch It!"        [⚡️暂停]   │
├────────────────────────────────────┤
│ 进度: ●●●○○ (3/6)                    │
│                                          │
│         ┌──────────────────┐           │
│         │  [场景图片]        │           │
│         │  电视在播放         │           │
│         │                  │           │
│         └──────────────────┘           │
│         看看什么：           │           │
│         "What's on TV?"     │           │
│                                          │
│    ┌──────┐ ┌──────┐ ┌──────┐        │
│    │ 📺   │ │ 📻   │ │ 📱   │        │
│    │ TV   │ │ radio│ │phone│        │
│    └──────┘ └──────┘ └──────┘        │
│                                          │
│  [💡提示×2] [❌×1] [⏱️0:45]           │
└────────────────────────────────────┘
```

**交互流程**：
1. 播放音频（可重播）
2. 点击选项
3. 正确：动画反馈 + 音效 + 分数+10
4. 错误：晃动 + 扣生命值 + 提示

#### 2.3.4 家长报告界面

```
┌────────────────────────────────────┐
│ [切换到孩子视角]    👤 家长中心        │
├────────────────────────────────────┤
│                                          │
│  📊 本周学习报告 (2月18日-2月24日)         │
├────────────────────────────────────┤
│                                          │
│  📊 词汇掌握                           │
│  │  ████████████ 75/180 词 (42%)        │
│  │                                       │
│  │  Look Island: ████████ 24/24         │
│  │  Move Valley: ██████░░░ 18/24         │
│  │  Say City:     ███░░░░░░░ 9/24         │
│  └───────────────────────────────────┤   │
│                                          │
│  ⏱️ 学习时长                           │
│  │  本周: 5.2小时                      │
│  │  平均: 45分钟/天                    │
│  │                                       │
│  │   周一 周二 周三 周四 周五 周六 周日   │
│  │   ██   █   ███  ██  ███  ██   █   │
│  └───────────────────────────────────┤   │
│                                          │
│  📈 学习效果                           │
│  │  本次学习正确率: 78%                │
│  │  次日复习正确率: 71% ✅             │
│  │  7日后保持率: 65%                   │
│  │                                       │
│  │  KET词汇达标: 42%                    │
│  │  预计备考时间: 2.5个月               │
│  └───────────────────────────────────┤   │
│                                          │
│  ⚠️ 需要关注                           │
│  │  • look vs see 容易混淆              │
│  │  • 复习关完成率仅40%                 │
│  │  • 周三、周五未学习                   │
│  └───────────────────────────────────┤   │
│                                          │
│  [📥导出报告] [📧发送到邮箱]            │
└────────────────────────────────────┘
```

### 2.4 词根教学方案（MVP阶段）

#### 三阶段渐进在MVP中的应用

**MVP范围**：
- 180词处于"阶段1：语境记忆期"到"阶段2：词根萌芽期"
- 不实施完整的"阶段3：词根系统期"

#### 阶段1：语境记忆期（前50-100词）

**实施策略**：
- Look Island前60词 + Move Valley前40词
- 完全不提"词根"概念
- 通过场景、句子、声音记忆

**具体实施**：

```kotlin
// 词汇学习流程
fun learnWord(word: Word) {
    // 1. 场景演示（3-5秒动画）
    showScene(word.sceneImage)

    // 2. 听发音
    playAudio(word.pronunciation)

    // 3. 例句
    showSentence(word.exampleSentence)

    // 4. 跟读（语音识别，可选）
    if (voiceEnabled) {
        checkPronunciation(userSpoken)
    }

    // 5. 简单练习
    practice(word)
}
```

**Look Island示例**：
```
单词: watch

1. 动画：一个人坐在沙发上看电视（持续播放）
2. 音频："Watch. I watch TV every evening."
3. 例句："Watch. Watch."
4. 练习：点击电视图片
```

#### 阶段2：词根萌芽期（100-180词）

**实施策略**：
- Move Valley后20词 + Say City前60词
- 偶尔出现"词根彩蛋"
- 培养词根意识，不教学

**词根彩蛋系统**：

```kotlin
// 彩蛋触发条件
data class WordEgg(
    val triggerWords: List<String>,  // 同时出现时触发
    val message: String,             // 发现提示
    val animation: String            // 庆祝动画
)

// 彩蛋示例
val eggs = listOf(
    WordEgg(
        triggerWords = listOf("say", "sayings"),
        message = "哇！'say'和'sayings'长得好像！\n它们都有'say'这部分哦！",
        animation = "sparkle_connection"
    ),
    WordEgg(
        triggerWords = listOf("look", "look out"),
        message = "发现！'look out'里面有个'look'！\n你已经认识这个词了！",
        animation = "lightbulb_moment"
    )
)
```

**Say City词根彩蛋设计**：

| 触发词组 | 彩蛋提示 | 目的 |
|---------|---------|------|
| say + sayings | "它们都有say部分！" | 观察词形 |
| tell + retell | "tell的前面多了re！" | 意知前缀 |
| ask + question | "这两个词有点像！" | 感知关联 |

**词根萌芽期的关键**：
- ✅ 不测试词根知识
- ✅ 不要求记忆词根
- ✅ 只是"有趣的发现"
- ✅ 培养"观察意识"

#### 阶段3：词根系统期（MVP后，300+词）

MVP不实施，但在数据模型中预留：

```kotlin
// 数据模型预留
data class Word(
    val id: String,
    val word: String,
    val root: String? = null,           // 词根（MVP后使用）
    val prefix: String? = null,          // 前缀（MVP后使用）
    val suffix: String? = null,          // 后缀（MVP后使用）
    val relatedWords: List<String>? = null  // 相关词（MVP后使用）
)
```

### 2.5 苏格拉底提问模板库

#### 核心考点的提问模板

##### **Look vs See**

```
情境: 孩子混淆look和see

Level 1提示（引导式）:
  "想一想，电视上的足球比赛是在动还是不动？"
  → 继续
  "对！动的东西要用什么？"
  → 提示"watch"或引导说"持续看"

Level 2提示（半帮助）:
  "look是看一眼，see是看见。"
  "看电视是一直看还是看一眼？"
  → 引导到"持续看" → watch

Level 3提示（直接）:
  "正确答案是watch。"
  "因为watch是持续看，球赛一直在动。"
  "look是'看'这个动作，see是'看见'这个结果。"
```

##### **Listen vs Hear**

```
Level 1:
  "你是故意听音乐，还是不小心听到了？"
  → 故意 → listen
  → 不小心 → hear

Level 2:
  "listen是'认真听'，hear是'听到'。"
  "音乐是你主动打开的吗？"

Level 3:
  "正确答案是listen。"
  "因为你是主动打开音乐认真听的。"
```

##### **Say vs Tell**

```
Level 1:
  "你是说话的内容，还是告诉某人？"
  → 内容 → say
  → 告诉某人 → tell

Level 2:
  "say后面直接接说的话，tell后面接人。"
  "你是说给谁听吗？"

Level 3:
  "正确答案是tell。"
  "因为tell是'告诉某人'，say是'说的内容'。"
```

#### 提问模板数据结构

```kotlin
data class SocraticPrompt(
    val wordPair: String,          // "look_vs_see"
    val level1: Prompt,             // 引导式
    val level2: Prompt,             // 半帮助
    val level3: Prompt              // 直接解释
)

data class Prompt(
    val questions: List<String>,    // 问题列表
    val expectedAnswer: String?,     // 期望答案（可为空，开放式）
    val followUp: String?           // 追问
)

// 示例
val lookVsSee = SocraticPrompt(
    wordPair = "look_vs_see",
    level1 = Prompt(
        questions = listOf(
            "想一想，{object}是在动还是不动？",
            "你是在'看'这个动作，还是已经'看见'了？"
        ),
        expectedAnswer = null,
        followUp = "动的东西要持续看哦"
    ),
    level2 = Prompt(
        questions = listOf(
            "look是看的动作，see是看见的结果。",
            "{object}是一直在变化的吗？"
        ),
        expectedAnswer = "yes",
        followUp = "对，所以需要持续看"
    ),
    level3 = Prompt(
        questions = listOf(),
        expectedAnswer = "watch",
        followUp = "正确答案是{answer}。因为{reason}"
    )
)
```

---

## 三、技术架构文档

### 3.1 技术栈选择

#### 为什么选择Kotlin + Jetpack Compose

**核心决策**：

```
┌─────────────────────────────────┐
│ 为什么选择原生Android而非跨平台？       │
└─────────────────────────────────┘

✅ 性能优势：
   - 游戏需要流畅动画（60fps）
   - 音频播放需要低延迟
   - 原生比Flutter/React Native更流畅

✅ 用户体验：
   - Material Design 3 原生支持
   - 系统级交互（通知、Widget）
   - 更好的无障碍支持

✅ 学习曲线：
   - Kotlin语言简洁（比Java更易学）
   - Google官方推荐
   - 团队可能已有Android经验

✅ MVP阶段：
   - 单平台开发更快
   - 避免跨平台陷阱
   - 后期可考虑Flutter多端
```

#### 技术栈清单

```yaml
核心框架:
  语言: Kotlin 1.9+
  UI框架: Jetpack Compose 1.5+
  最低API: Android 7.0 (API 24)
  目标API: Android 14 (API 34)

架构组件:
  导航: Compose Navigation
  状态管理: StateFlow + Compose State
  依赖注入: Hilt (可选，MVP可手动DI)
  协程: Kotlin Coroutines + Flow

数据持久化:
  数据库: Room 2.6+
  本地存储: DataStore (替代SharedPreferences)
  文件存储: Android File API

媒体资源:
  音频: ExoPlayer (Google官方)
  图片: Coil (轻量级图片加载)
  动画: Compose Animation + Lottie

网络（MVP后）:
  客户端: Retrofit + OkHttp
  同步: WorkManager (后台任务)

测试:
  单元测试: JUnit 5 + MockK
  UI测试: Compose Testing
  集成测试: Robolectric

构建工具:
  构建系统: Gradle (Kotlin DSL)
  CI/CD: GitHub Actions (可选)
  版本管理: Git
```

### 3.2 数据模型设计

#### 核心数据表结构

##### **Word（词汇表）**

```kotlin
@Entity(tableName = "words")
data class Word(
    @PrimaryKey val id: String,              // "word_001"
    val word: String,                        // "watch"
    val translation: String,                  // "观看"
    val pronunciation: String?,               // "/wɒtʃ/"
    val audioPath: String?,                    // "assets/audio/watch.mp3"
    val partOfSpeech: String?,                 // "verb"
    val difficulty: Int,                       // 1-5
    val frequency: Int,                        // 1-100
    val theme: String,                         // "look"
    val ketLevel: Boolean,                     // true
    val petLevel: Boolean,                     // false
    val exampleSentences: List<ExampleSentence>?, // JSON string
    val relatedWords: List<String>?,           // JSON string
    val root: String?,                         // 词根（预留）
    val prefix: String?,                      // 前缀（预留）
    val suffix: String?,                       // 后缀（预留）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExampleSentence(
    val sentence: String,                     // "I watch TV every evening."
    val translation: String,                  // "我每天晚上看电视。"
    val audioPath: String?                    // 句子音频
)
```

##### **UserWordProgress（用户词汇进度表）**

```kotlin
@Entity(tableName = "user_word_progress")
data class UserWordProgress(
    @PrimaryKey val wordId: String,          // FK to Word.id
    val userId: String,                       // MVP单用户，预留多用户
    val status: LearningStatus,               // NEW, LEARNING, MASTERED
    val memoryStrength: Int,                  // 0-100
    val lastReviewTime: Long?,                 // 上次复习时间
    val nextReviewTime: Long?,                 // 下次复习时间
    val totalAttempts: Int,                   // 总尝试次数
    val correctAttempts: Int,                 // 正确次数
    val incorrectAttempts: Int,                // 错误次数
    val averageResponseTime: Long,             // 平均响应时间（毫秒）
    val firstLearnTime: Long?,                // 首次学习时间
    val masteryTime: Long?,                    // 精通时间
    val sceneExposure: List<String>?,          // JSON: 在哪些关卡出现过
    val crossSceneCorrect: Int,                // 跨场景正确次数
    val crossSceneTotal: Int,                   // 跨场景总次数
    val isGuessingDetected: Boolean,            // 是否检测到蒙答
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class LearningStatus {
    NEW,         // 新词，未学习
    LEARNING,     // 学习中
    MASTERED,     // 已精通（连续3次正确 + 时间间隔）
    NEED_REVIEW   // 需要复习
}
```

##### **LevelProgress（关卡进度表）**

```kotlin
@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: String,         // "look_island_level_01"
    val userId: String,
    val status: LevelStatus,                 // LOCKED, UNLOCKED, IN_PROGRESS, COMPLETED
    val stars: Int,                          // 0-3
    val totalTime: Long,                     // 总用时（毫秒）
    val bestTime: Long?,                      // 最佳用时
    val attempts: Int,                        // 尝试次数
    val firstPlayTime: Long?,                // 首次游玩时间
    val bestPlayTime: Long?,                  // 最佳通关时间
    val lastPlayTime: Long?,                  // 最近游玩时间
    val difficulty: String,                  // "easy", "normal", "hard"
    val isNewWordsCompleted: Boolean,          // 是否完成新词学习
    val isReviewCompleted: Boolean,           // 是否完成复习
    val score: Int,                          // 最高分
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class LevelStatus {
    LOCKED,       // 未解锁
    UNLOCKED,     // 已解锁，未玩
    IN_PROGRESS,  // 进行中
    COMPLETED,    // 已完成
    PERFECT       // 完美通关（3星）
}
```

##### **BehaviorTracking（行为追踪表）**

```kotlin
@Entity(tableName = "behavior_tracking")
data class BehaviorTracking(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val userId: String,
    val levelId: String?,                   // 关联关卡（可为空）
    val wordId: String?,                    // 关联词汇（可为空）
    val action: String,                      // "question_answered", "hint_used", "level_completed"
    val timestamp: Long,
    val responseTime: Long?,                 // 响应时间（毫秒）
    val isCorrect: Boolean?,                  // 是否正确（答题类行为）
    val hintLevel: Int?,                     // 使用了几级提示
    val difficulty: String?,                 // 当前难度
    val sessionDuration: Long?,               // 本次会话时长
    val metadata: String?                    // JSON: 其他元数据
)
```

##### **IslandMastery（岛屿精通度表）**

```kotlin
@Entity(tableName = "island_mastery")
data class IslandMastery(
    @PrimaryKey val islandId: String,     // "look_island"
    val userId: String,
    val totalWords: Int,                   // 总词数
    val masteredWords: Int,                 // 精通词数
    val totalLevels: Int,                   // 总关卡数
    val completedLevels: Int,               // 完成关卡数
    val crossSceneScore: Double,            // 跨场景分数 0-1
    val masteryPercentage: Double,          // 精通度百分比 0-100
    val isNextIslandUnlocked: Boolean,      // 是否解锁下一岛
    val unlockedAt: Long?,                  // 解锁时间
    val masteredAt: Long?,                  // 精通时间
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 数据关系图

```
┌─────────┐       ┌──────────────────┐
│  Word    │───────>│ UserWordProgress │
│ (词汇表) │  1:N   │(用户词汇进度)  │
└─────────┘       └──────────────────┘
                          ↓
                   ┌──────────────────┐
                   │ IslandMastery     │
                   │(岛屿精通度)     │
                   └──────────────────┘

┌─────────┐       ┌──────────────────┐
│  Level   │───────>│ LevelProgress     │
│(关卡表)  │  1:N   │(关卡进度)       │
└─────────┘       └──────────────────┘
                          ↓
                   ┌──────────────────┐
                   │ BehaviorTracking  │
                   │(行为追踪)        │
                   └──────────────────┘
```

### 3.3 记忆强度算法实现

#### 核心算法

```kotlin
class MemoryStrengthAlgorithm {

    companion object {
        const val INITIAL_STRENGTH = 10
        const val MAX_STRENGTH = 100
        const val CORRECT_BONUS = 10
        const val INCORRECT_PENALTY = 15
        const val GUESSING_THRESHOLD = 2000  // 2秒内视为蒙答
    }

    /**
     * 计算新记忆强度
     * 新强度 = 当前强度 + (正确 × 10) - (错误 × 15)
     */
    fun calculateNewStrength(
        currentStrength: Int,
        isCorrect: Boolean,
        isGuessing: Boolean  // 是否蒙答
    ): Int {
        val bonus = if (isCorrect) {
            if (isGuessing) CORRECT_BONUS / 2  // 蒙对的加成减半
            else CORRECT_BONUS
        } else {
            -INCORRECT_PENALTY
        }

        return (currentStrength + bonus).coerceIn(0, MAX_STRENGTH)
    }

    /**
     * 计算下次复习时间
     * 复习间隔 = 强度 / 10 天
     */
    fun calculateNextReview(
        strength: Int,
        lastReviewTime: Long
    ): Long {
        val intervalDays = strength / 10.0
        val intervalMillis = intervalDays * 24 * 60 * 60 * 1000
        return lastReviewTime + intervalMillis.toLong()
    }

    /**
     * 检测是否蒙答
     * 响应时间 < 2秒 + 随机误差 → 视为蒙答
     */
    fun detectGuessing(responseTime: Long, wordDifficulty: Int): Boolean {
        val threshold = GUESSING_THRESHOLD + (wordDifficulty * 500)
        return responseTime < threshold
    }

    /**
     * 判断是否精通
     * 条件：
     * 1. 连续3次正确
     * 2. 记忆强度 >= 60
     * 3. 跨场景正确率 >= 70%
     */
    fun isMastered(progress: UserWordProgress): Boolean {
        val recentCorrect = progress.correctAttempts >= 3
        val strengthMet = progress.memoryStrength >= 60
        val crossSceneMet = if (progress.crossSceneTotal > 0) {
            (progress.crossSceneCorrect.toDouble() / progress.crossSceneTotal) >= 0.7
        } else {
            false
        }

        return recentCorrect && strengthMet && crossSceneMet
    }

    /**
     * 生成每日复习队列
     */
    fun generateDailyReviewQueue(
        allProgress: List<UserWordProgress>,
        currentTime: Long
    ): List<String> {
        return allProgress
            .filter { it.nextReviewTime <= currentTime }
            .sortedBy { it.nextReviewTime }
            .take(20)  // 每天最多复习20词
            .map { it.wordId }
    }
}
```

#### 记忆强度追踪示例

```
单词: watch

Day 1 (首次学习):
  - 学习时间: 2026-02-14 10:00
  - 练习5次，对4错1
  - 记忆强度: 10 + 4×10 - 1×15 = 35
  - 下次复习: 10:00 + (35/10)天 = 2/15 10:00

Day 2 (第1次复习):
  - 复习时间: 2026-02-15 10:00
  - 练习3次，对2错1
  - 记忆强度: 35 + 2×10 = 55
  - 下次复习: 2/15 10:00 + 6.5天 = 2/21 19:00

Day 4 (跨场景出现):
  - 在Move Valley关卡中遇到watch
  - 正确应用
  - 跨场景计数: correct+1, total+1
  - 跨场景正确率: 100%

Day 7 (第2次复习):
  - 复习时间: 2026-02-21 19:00
  - 练习3次，对2错1
  - 记忆强度: 55 + 2×10 = 75
  - 跨场景正确率: 83% (5/6)
  - 判断: 精通！连续3次正确 + 强度60+ + 跨场景70%+
  - 状态: MASTERED
```

### 3.4 跨场景验证机制实现

#### 核心设计

```kotlin
class CrossSceneValidator {

    data class CrossSceneQuestion(
        val word: Word,
        val originalScene: String,           // "look_island_level_02"
        val newScene: String,                // "move_valley_level_03"
        val context: String,                 // 场景描述
        val question: String                 // 问题
    )

    /**
     * 生成跨场景问题
     * 原则：在Move Valley中测试Look Island的词
     */
    fun generateCrossSceneQuestions(
        currentLevel: Level,
        userProgress: List<UserWordProgress>
    ): List<CrossSceneQuestion> {
        val otherIslandWords = userProgress
            .filter {
                it.status == LearningStatus.MASTERED ||
                it.status == LearningStatus.LEARNING
            }
            .filterNot { it.wordId.startsWith(currentLevel.theme) }
            .map { it.wordId }
            .shuffled()
            .take(3)  // 每关3个跨场景词

        return otherIslandWords.map { wordId ->
            val word = wordRepository.getWord(wordId)
            CrossSceneQuestion(
                word = word,
                originalScene = extractScene(wordId),
                newScene = currentLevel.id,
                context = buildContext(word, currentLevel),
                question = buildQuestion(word, currentLevel)
            )
        }
    }

    /**
     * 计算跨场景分数
     */
    fun calculateCrossSceneScore(
        userId: String,
        islandId: String
    ): Double {
        val tracking = behaviorRepository
            .getCrossSceneAttempts(userId, islandId)

        if (tracking.isEmpty()) return 0.0

        val correct = tracking.count { it.isCorrect == true }
        return correct.toDouble() / tracking.size
    }
}
```

#### 跨场景问题示例

```
当前关卡: Move Valley Level 3
主题: run, jump, walk

跨场景问题1:
  背景: 狐色在跑道上跑步
  问题: "Look at me! I'm ______ fast!"
  选项: [run, running, runs]
  正确: running
  考点: Look Island的"look" + Move Valley的"run"

跨场景问题2:
  背景: 狗色停下来看东西
  问题: "I ______ to find my bag."
  选项: [look, am looking, looking]
  正确: am looking
  考点: look在动作场景中的应用

跨场景问题3:
  背景: 看着别人跳高
  问题: "Do you ______ him jumping?"
  选项: [see, look, watch]
  正确: watch
  考点: see vs watch区分 + jump场景
```

#### 跨场景追踪

```kotlin
// 在UserWordProgress中更新
fun updateCrossSceneAttempt(
    wordId: String,
    isCorrect: Boolean,
    sceneId: String
) {
    val progress = getProgress(wordId)
    progress.crossSceneTotal++
    if (isCorrect) progress.crossSceneCorrect++

    // 记录行为
    val tracking = BehaviorTracking(
        userId = currentUserId,
        wordId = wordId,
        action = "cross_scene_answer",
        isCorrect = isCorrect,
        metadata = """{"scene": "$sceneId"}"""
    )
    behaviorRepository.insert(tracking)

    // 更新进度
    update(progress)
}
```

### 3.5 防蒙检测系统实现

#### 行为分析模型

```kotlin
class GuessingDetector {

    data class ResponsePattern(
        val responseTime: Long,             // 响应时间
        val isCorrect: Boolean,              // 是否正确
        val difficulty: Int,                // 目标难度
        val isNewWord: Boolean,             // 是否新词
        val hintUsed: Boolean,               // 是否使用提示
        val consecutiveFast: Int            // 连续快速答题次数
    )

    /**
     * 检测蒙答
     * 综合多个信号判断
     */
    fun detectGuessing(pattern: ResponsePattern): Boolean {
        val signals = mutableListOf<Boolean>()

        // 信号1: 响应时间过短（<2秒）
        if (pattern.responseTime < 2000) {
            signals.add(true)
        }

        // 信号2: 新词却答得很快且正确（可疑）
        if (pattern.isNewWord &&
            pattern.responseTime < 3000 &&
            pattern.isCorrect) {
            signals.add(true)
        }

        // 信号3: 连续快速答题（>3次<2秒）
        if (pattern.consecutiveFast > 3) {
            signals.add(true)
        }

        // 信号4: 高难度题却答得很快
        if (pattern.difficulty >= 4 &&
            pattern.responseTime < 2500) {
            signals.add(true)
        }

        // 信号5: 不用提示却秒答
        if (!pattern.hintUsed &&
            pattern.responseTime < 1500) {
            signals.add(true)
        }

        // 至少2个信号 → 判定为蒙答
        return signals.size >= 2
    }

    /**
     * 对蒙答的处理
     */
    fun handleGuessing(
        wordId: String,
        pattern: ResponsePattern
    ): GuessingResponse {
        return when {
            // 检测到蒙答且答错 → 降低记忆强度
            pattern.isGuessing && !pattern.isCorrect -> {
                GuessingResponse(
                    treatAsIncorrect = true,
                    penalty = 20,  // 更大惩罚
                    message = "没关系，慢慢想，不用着急哦"
                )
            }

            // 检测到蒙答但答对 → 降低奖励
            pattern.isGuessing && pattern.isCorrect -> {
                GuessingResponse(
                    treatAsIncorrect = false,
                    penalty = 5,  // 小惩罚
                    bonus = 3,  // 奖励减半
                    message = "答对了！但下次可以多想想哦"
                )
            }

            // 正常答题 → 正常处理
            else -> {
                GuessingResponse(
                    treatAsIncorrect = false,
                    penalty = 0,
                    bonus = if (pattern.isCorrect) 10 else 0
                )
            }
        }
    }

data class GuessingResponse(
    val treatAsIncorrect: Boolean,  // 是否视为错误
    val penalty: Int,               // 惩罚值
    val bonus: Int = 0,             // 奖励值
    val message: String? = null      // 提示消息
)
```

#### 实时监控UI

```
┌────────────────────────────────────┐
│  ⚠️ 检测到快速答题                   │
│                                     │
│  最近5题平均用时: 1.2秒              │
│  建议: 是否太快了？                  │
│                                     │
│  [我懂了，慢点答] [继续快速答题]      │
└────────────────────────────────────┘
```

### 3.6 代码结构

#### 模块划分

```
app/
├── src/main/java/com/wordland/
│   │
│   ├── WordlandApp.kt             // Application入口
│   │
│   ├── core/                      // 核心模块
│   │   ├── domain/                // 领域层
│   │   │   ├── model/             // 数据模型
│   │   │   │   ├── Word.kt
│   │   │   │   ├── UserWordProgress.kt
│   │   │   │   ├── LevelProgress.kt
│   │   │   │   ├── BehaviorTracking.kt
│   │   │   │   └── IslandMastery.kt
│   │   │   │
│   │   │   ├── repository/        // 仓库接口
│   │   │   │   ├── WordRepository.kt
│   │   │   │   ├── ProgressRepository.kt
│   │   │   │   ├── UserRepository.kt
│   │   │   │   └── BehaviorRepository.kt
│   │   │   │
│   │   │   └── usecase/           // 用例
│   │   │       ├── LearnWordUseCase.kt
│   │   │       ├── UpdateProgressUseCase.kt
│   │   │       └── GetReviewWordsUseCase.kt
│   │   │
│   │   ├── data/                   // 数据层
│   │   │   ├── local/             // 本地数据
│   │   │   │   ├── database/      // Room数据库
│   │   │   │   │   ├── WordDatabase.kt
│   │   │   │   │   ├── WordDao.kt
│   │   │   │   │   ├── ProgressDao.kt
│   │   │   │   │   └── TrackingDao.kt
│   │   │   │   └── preferences/    // DataStore
│   │   │   │       └── WordlandPreferences.kt
│   │   │   │
│   │   │   └── repository/        // 仓库实现
│   │   │       ├── WordRepositoryImpl.kt
│   │   │       └── ProgressRepositoryImpl.kt
│   │   │
│   │   ├── algorithm/            // 算法模块
│   │   │   ├── MemoryStrengthAlgorithm.kt
│   │   │   ├── CrossSceneValidator.kt
│   │   │   └── GuessingDetector.kt
│   │   │
│   │   ├── ui/                     // UI层
│   │   │   ├── theme/               // 主题
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Typography.kt
│   │   │   │   └── Theme.kt
│   │   │   │
│   │   │   ├── components/          // 通用组件
│   │   │   │   ├── ProgressCard.kt
│   │   │   │   ├── StarRating.kt
│   │   │   │   └── EnergyBar.kt
│   │   │   │
│   │   │   ├── screens/             // 屏幕
│   │   │   │   ├── map/            // 世界地图
│   │   │   │   │   ├── MapScreen.kt
│   │   │   │   │   ├── IslandCard.kt
│   │   │   │   │   └── MapViewModel.kt
│   │   │   │   │
│   │   │   │   ├── level/          // 关卡选择
│   │   │   │   │   ├── LevelScreen.kt
│   │   │   │   │   ├── LevelCard.kt
│   │   │   │   │   └── LevelViewModel.kt
│   │   │   │   │
│   │   │   │   ├── game/           // 游戏内
│   │   │   │   │   ├── GameScreen.kt
│   │   │   │   │   ├── QuestionCard.kt
│   │   │   │   │   └── GameViewModel.kt
│   │   │   │   │
│   │   │   │   └── report/          // 家长报告
│   │   │   │       ├── ReportScreen.kt
│   │   │   │       └── ReportViewModel.kt
│   │   │   │
│   │   │   └── navigation/          // 导航
│   │   │       └── NavGraph.kt
│   │   │
│   │   ├── di/                      // 依赖注入
│   │   │   ├── AppModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   └── RepositoryModule.kt
│   │   │
│   │   └── utils/                   // 工具类
│   │       ├── Extensions.kt
│   │       └── Constants.kt
│   │
│   └── src/main/res/                   // 资源文件
│       ├── drawable/                  // 图片
│       ├── raw/                        // 音频
│       ├── look/                       // Look岛资源
│       ├── move/                       // Move谷资源
│       └── say/                        // Say城资源
│
└── build.gradle.kts                   // 构建配置
```

#### 架构层次

```
┌─────────────────────────────────┐
│     UI Layer (Compose)           │
│  - Screens, Components, ViewModels │
└─────────────────────────────────┘
                  ↓
┌─────────────────────────────────┐
│      Domain Layer (业务逻辑)       │
│  - UseCases, Models, Repositories  │
└─────────────────────────────────┘
                  ↓
┌─────────────────────────────────┐
│      Data Layer (数据持久化)       │
│  - Room Database, Files, Network    │
└─────────────────────────────────┘
```

---

## 四、开发计划

### 4.1 12周时间表

#### **Phase 1: 基础搭建（第1-2周）**

**目标**: 搭建项目框架，完成核心架构

```
Week 1: 项目初始化 + 架构搭建
├── Day 1-2: 环境搭建
│   ├── 创建Android项目
│   ├── 配置Kotlin + Compose
│   ├── 设置依赖（Room, Hilt, Coil等）
│   └── Git仓库初始化
│
├── Day 3-4: 数据库设计
│   ├── 定义Entity（Word, Progress等）
│   ├── 创建DAO接口
│   ├── 编写Database类
│   └── 编写Repository接口
│
└── Day 5-7: 核心算法实现
   ├── MemoryStrengthAlgorithm
   ├── CrossSceneValidator
   ├── GuessingDetector
   └── 交付物:
      - 可运行的项目骨架
      - 数据库表结构
      - 核心算法单元测试
```

#### **Phase 2: 核心玩法开发（第3-5周）**

**目标**: 实现4种玩法机制

```
Week 3: 玩法1 + 玩法2
├── Day 1-3: 听音寻宝
│   ├── 音频播放系统
│   ├── 场景图片展示
│   ├── 选项交互
│   ├── 即时反馈动画
│   └── 提示系统（三级）
│
└── Day 4-5: 句子配对
   ├── 拖拽系统
   ├── 空位检测
   ├── 正确验证
   └── 错误反馈

Week 4: 玩法3 + 玩法4
├── Day 1-3: 拼写战斗
   ├── 虚拟键盘
   ├── 拼写验证
   ├── 错误位置反馈
   └── 提示字母
│
└── Day 4-5: 快速判断
   ├── 场景动态展示
   ├── 快速点击交互
   ├── 计时系统
   └── 连击奖励

Week 5: 游戏系统集成
├── Day 1-2: 关卡流程
│   ├── 关卡状态管理
│   ├── 题目队列
│   ├── 结算界面
│   └── 星星评分
│
├── Day 3-4: 进度系统
│   ├── 精通度计算
│   ├── 星星评分
│   ├── 岛屿解锁
│   └── 能量系统
│
└── Day 5-7: 优化调试
   ├── 动画优化
   ├── 性能优化
   ├── Bug修复
   └── 交付物:
      - 4种玩法可演示
      - 20道测试题
      - 关卡编辑器原型
```

#### **Phase 3: 内容制作（第6-8周）**

**目标**: 完成2.5个世界内容

```
Week 6: Look Island 完善
├── Day 1-2: 关卡填充
│   ├── 8个关卡内容完整填充
│   ├── 60词数据完整
│   ├── 200+题目编写
│   └── 音频/图片全部到位
│
├── Day 3-5: 关卡调优
│   ├── 难度平衡调整
│   ├── 题目顺序优化
│   ├── 奖励节奏调整
│   └── 反馈动画优化
│
└── Day 6-7: 测试
   ├── 内部测试
   ├── Bug修复
   └── 交付物:
      - Look Island完整版
      - 可对外测试版

Week 7: Move Valley 开发
├── Day 1-3: 关卡开发
│   ├── 8个关卡结构复制
│   ├── 60词数据录入
│   └── 跨场景问题生成
│
├── Day 4-5: 跨场景验证
│   ├── 跨场景问题集成
│   ├── 跨场景追踪
│   ├── 跨场景分数计算
│
└── Day 6-7: 整合测试
   ├── 内部测试
   ├── 3岛联调
   ├── 性能测试
   └── 交付物:
      - Move Valley完整版
      - 2岛完整版

Week 8: Say City 开发（前半）
├── Day 1-3: 前30词完整
│   ├── Level 0-5完整实现
│   ├── 30词数据
│   ├── 词根彩蛋系统
│   └── 100+题目
│
├── Day 4-5: 后30词框架
│   ├── 关卡结构完整
│   ├── 内容占位
│   └── 为下一版准备
│
└── Day 6-7: 整合测试
   ├── 3岛联调
   ├── 进度同步测试
   ├── 性能测试
   └── 交付物:
      - Say City前30词完整版
      - MVP内容完成（180词）
```

#### **Phase 4: 学习系统 + 数据（第9-10周）**

**目标**: 实现智能复习和数据分析

```
Week 9: 智能复习系统
├── Day 1-2: 复习算法
│   ├── 间隔重复计算
│   ├── 复习队列生成
│   ├── 复习时间提醒
│   └── 复习关卡创建
│
├── Day 3-4: 学习进度追踪
│   ├── 词汇掌握度追踪
│   ├── 关卡完成率统计
│   ├── 学习时长统计
│   └── 行为埋点完善
│
└── Day 5-7: 家长报告
│   ├── 报告数据计算
│   ├── 报告UI界面
│   ├── 数据可视化
│   └── 导出功能
│
└── 交付物:
   - 智能复习系统
   - 家长报告基础版
```

#### **Phase 5: 测试优化（第11周）**

**目标**: 用户测试 + Bug修复

```
Week 11: 用户测试
├── Day 1-2: 测试准备
│   ├── 测试版本发布
│   ├── 测试用户招募（5-10名10岁儿童）
│   ├── 测试任务设计
│   └── 反馈收集系统
│
├── Day 3-4: 用户测试
│   ├── 观察用户游玩
│   ├── 记录问题点
│   ├── 收集反馈
│   └── 记录行为数据
│
├── Day 5-7: 优化迭代
│   ├── Bug修复（高优先级）
│   ├── 难度调整
│   ├── UI优化
│   └── 性能优化
│
└── 交付物:
   - 测试报告
   - 问题清单
   - 优化建议
```

#### **Phase 6: 发布准备（第12周）**

**目标**: 上线准备

```
Week 12: 发布准备
├── Day 1-2: 内容打磨
│   ├── 最终内容检查
│   ├── 语音质量检查
│   ├── 图片优化
│   └── 文案校对
│
├── Day 3-4: 应用商店准备
│   ├── 应用图标
│   ├── 截图/视频
│   ├── 应用描述
│   └── 隐私政策
│
├── Day 5-6: 内部验收
│   ├── 功能验收
│   ├── 性能验收
│   ├── 兼容性测试
│   └── 安全审查
│
└── Day 7: 发布
   ├── Google Play上传
   ├── 发布（限制地区）
   └── 初始数据监控
```

### 4.2 团队配置

#### 最小可行团队（6.5人全职）

```
┌─────────────────────────────────┐
│  Wordland MVP 团队配置             │
└─────────────────────────────────┘

核心团队（全职）:

1. 产品经理 (1人)
   职责:
   - 需求管理
   - 优先级决策
   - 验收标准制定
   - 数据分析
   工作量: 100%
   关键阶段: 全程

2. 游戏策划 (1人)
   职责:
   - 关卡设计
   - 题目编写
   - 难度曲线调整
   - 苏格拉底提问设计
   工作量: 100%
   关键阶段: Week 1-8

3. Android开发 (2人)
   职责:
   - 架构搭建
   - 功能开发
   - 数据库实现
   - 算法实现
   - 性能优化
   工作量: 100%
   关键阶段: 全程

4. UI/UX设计 (1人)
   职责:
   - UI设计
   - 场景绘制
   - 图标设计
   - 动画设计
   工作量: 100%
   关键阶段: Week 1-6

5. 英语教育顾问 (0.5人 - 兼职)
   职责:
   - 词汇选择
   - 例句编写
   - 词根提炼
   - KET考试对齐
   工作量: 50%
   关键阶段: Week 2, Week 6-8

6. 测试工程师 (1人)
   职责:
   - 功能测试
   - 兼容性测试
   - 性能测试
   - 用户测试组织
   工作量: 100%
   关键阶段: Week 5-12
```

#### 外包/顾问（按需）

```
外包/顾问（按需）:

音频制作:
  - 词汇发音录制（180词）
  - 例句录制（200+句）
  - 场景音乐/音效
  预算: $3,000
  时间: Week 2-6

QA测试:
  - 专业游戏测试
  - 兼容性测试（10+机型）
  - 压力测试
  预算: $2,000
  时间: Week 11

KET考试专家:
  - 词汇表审定
  - 题目质量审查
  - 学习效果评估
  预算: $1,500
  时间: Week 2, Week 10
```

### 4.3 里程碑和交付物

#### 主要里程碑

```
M1: 项目启动 (Week 0)
  交付物:
   - ✅ 项目计划
   - ✅ 团队组建
   - ✅ 开发环境搭建

M2: Alpha版 (Week 5)
  交付物:
   - ✅ 4种玩法实现
   - ✅ Look Island 8个关卡
   - ✅ 核心算法实现
   - ✅ 内部可测试版

M3: Beta版 (Week 8)
  交付物:
   - ✅ 2.5个世界完成（180词）
   - ✅ 跨场景验证实现
   - ✅ 智能复习系统
   - ✅ 家长报告
   - ✅ 可对外测试版

M4: 测试完成 (Week 11)
  交付物:
   - ✅ 5-10名儿童试玩
   - ✅ 测试报告
   - ✅ 问题修复
   - ✅ 优化建议

M5: MVP发布 (Week 12)
  交付物:
   - ✅ Google Play上架
   - ✅ 首批数据监控
   - ✅ 用户反馈渠道
```

#### 每周交付物清单

```
Week 1:
  - 项目架构代码
  - 数据库表设计
  - 核心算法单元测试

Week 2:
  - UI设计系统
  - 60词数据
  - 可运行的空壳App

Week 3:
  - 2种玩法可演示
  - 10道测试题
  - 音频播放系统

Week 4:
  - 4种玩法全部完成
  - 20道测试题
  - 关卡编辑器原型

Week 5:
  - Look Island 8个关卡
  - Alpha测试版
  - 内部测试报告

Week 6:
  - Look Island完整版
  - 60词完整内容
  - 200+题目
  - 可对外测试版

Week 7:
  - Move Valley 8个关卡
  - 跨场景验证
  - 2岛完整版

Week 8:
  - Say City前30词
  - 180词MVP内容
  - Beta测试版

Week 9:
  - 智能复习系统
  - 家长报告
  - 数据追踪

Week 10:
  - 完整音频系统
  - 数据分析
  - Beta优化版

Week 11:
  - 用户测试报告
  - 问题修复
  - Release Candidate

Week 12:
  - 应用商店素材
  - 最终发布版
  - Google Play上架
```

### 4.4 风险识别和应对

#### 主要风险

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|----------|
| **孩子觉得不好玩** | 高 | 高 | Week 2开始邀请孩子试玩，快速迭代 |
| **学习效果不明显** | 中 | 高 | Week 9开始学习效果验证，必要时强化复习系统 |
| **开发进度延误** | 中 | 高 | 每周检查进度，及时砍需求或增人力 |
| **内容质量不达标** | 中 | 中 | Week 4邀请教育专家审查，必要时外包 |
| **团队成员流失** | 低 | 高 | 关键岗位有备份计划 |
| **应用商店审核问题** | 低 | 中 | Week 11提前审核，准备合规材料 |

#### 风险应对详细方案

##### **风险1: 孩子觉得不好玩**

**早期预警信号**：
- 单次游戏时长 < 5分钟
- 次日留存率 < 40%
- 用户反馈"无聊"

**应对措施**：

```
Week 2: 第1次试玩（3个孩子）
  - 观察孩子反应
  - 记录喜欢的部分/不喜欢的部分
  - 快速调整（如果需要）

Week 5: 第2次试玩（5个孩子）
  - Look Island完整测试
  - 检查参与度数据
  - 数据反馈优化

Week 8: 第3次试玩（10个孩子）
  - MVP完整测试
  - 对比数据
  - 最终调整
```

**备用方案**：
- 如果孩子普遍不喜欢某种玩法 → 降低该玩法占比
- 如果关卡太难 → 降低难度或增加提示
- 如果反馈"不像游戏" → 加强游戏化元素

##### **风险2: 学习效果不明显**

**验证方法**：

```
Week 9: 学习效果验证
  - 5个孩子使用2周
  - 前测：KET词汇测试
  - 使用App 2周
  - 后测：相同测试
  - 对比分数

Week 10: 效果分析
  - 计算提升幅度
  - 分析薄弱环节
  - 优化学习系统
```

**应对措施**：
- 如果提升 < 5分 → 强化复习系统
- 如果容易遗忘 → 增加复习频率
- 如果跨场景应用差 → 加强跨场景题目

##### **风险3: 开发进度延误**

**预警机制**：
- 每周五检查进度
- 如果延误 > 3天 → 启动应对

**应对措施**：

```
轻度延误 (3-5天):
  - 砍低优先级功能（如词根系统）
  - 延后Say City后30词
  - 专注核心2岛完美化

中度延误 (5-10天):
  - 砍低Move Valley关卡数
  - 降低跨场景验证复杂度
  - 专注Look Island完美化

重度延误 (>10天):
  - MVP范围缩减至1岛120词
  - 延后跨场景验证
  - 快速发布1岛版本验证假设
```

### 4.5 验证标准

#### MVP成功标准

**核心假设验证**：

```
假设: 10岁孩子在游戏中通过4种玩法学习词汇，
     能在2周内掌握60个KET核心词汇，
     次日留存率≥50%，KET模拟考提升5-10分
```

**量化指标**：

| 指标类别 | 指标 | 目标值 | 验证方法 |
|---------|------|--------|----------|
| **学习效果** | 词汇掌握 | ≥50词 | 前后测对比 |
|  | KET模拟考提升 | ≥5分 | 前后测对比 |
|  | 次日复测正确率 | ≥70% | App内测试 |
| **参与度** | 次日留存率 | ≥50% | 数据统计 |
|  | 单次游戏时长 | ≥8分钟 | 数据统计 |
|  | 周使用次数 | ≥4次 | 数据统计 |
| **用户满意度** | 孩子喜爱度 | ≥4.0/5.0 | 问卷 |
|  | 家长满意度 | ≥4.0/5.0 | 问卷 |
|  | 推荐意愿 | ≥70% | 问卷 |

#### 验证方法详解

##### **1. 学习效果验证**

**前后测设计**：

```
前测 (Day 0):
  - KET词汇测试（180词范围）
  - 形式：选择题 + 填空题
  - 时长：30分钟
  - 记录：初始分数

使用App (2周):
  - 自由使用，无限制
  - 记录：使用时长、完成关卡

后测 (Day 14):
  - 相同测试（不同题目顺序）
  - 时长：30分钟
  - 记录：最终分数

分析:
  - 分数提升 = 后测 - 前测
  - 词汇掌握 = App内掌握数
  - 相关性 = 使用时长 vs 提升幅度
```

**成功标准**：
- 分数提升 ≥ 5分 → 成功
- 分数提升 3-5分 → 部分成功，需优化
- 分数提升 < 3分 → 失败，需重新设计

##### **2. 参与度验证**

**数据指标**：

```kotlin
data class EngagementMetrics(
    val retentionDay1: Double,      // 次日留存
    val retentionDay7: Double,      // 7日留存
    val avgSessionDuration: Double, // 平均单次时长
    val avgWeeklySessions: Double, // 周平均使用次数
    val levelCompletionRate: Double // 关卡完成率
)

// 计算方法
fun calculateMetrics(userId: String): EngagementMetrics {
    val day1Users = getUsersWhoInstalledOnDay(0)
    val day2Users = getUsersWhoReturnedOnDay(1)
    val retentionDay1 = day2Users.size.toDouble() / day1Users.size

    // ... 其他指标
}
```

**成功标准**：
- 次日留存 ≥ 50% → 孩子愿意回来
- 单次时长 ≥ 8分钟 → 游戏够好玩
- 周使用 ≥ 4次 → 形成习惯

##### **3. 用户满意度验证**

**孩子问卷（10岁友好）**：

```
1. 你喜欢这个游戏吗？
   ⭐⭐⭐⭐⭐
   (非常喜欢 → 完全不喜欢)

2. 你想继续玩吗？
   A. 肯定想
   B. 可能想
   C. 不太想
   D. 不想玩

3. 你会推荐给朋友吗？
   A. 一定会
   B. 可能会
   C. 不确定
   D. 不会

4. 你最喜欢哪个部分？（多选）
   [ ] 听音寻宝
   [ ] 句子配对
   [ ] 拼写战斗
   [ ] 快速判断
   [ ] 收集星星

5. 你觉得哪个地方不好玩？
   ______________________________________
```

**家长问卷**：

```
1. 您觉得孩子的英语有进步吗？
   A. 明显进步
   B. 有一些进步
   C. 不太确定
   D. 没有进步

2. 您会推荐给其他家长吗？
   A. 一定会
   B. 可能会
   C. 不确定
   D. 不会

3. 您觉得学习报告有用吗？
   ⭐⭐⭐⭐⭐
   (非常有用 → 完全没用)

4. 您希望改进哪些方面？（多选）
   [ ] 更多词汇
   [ ] 更多关卡
   [ ] 更详细的学习报告
   [ ] 更好的画面
   [ ] 其他：______
```

**成功标准**：
- 孩子满意度 ≥ 4.0/5.0 → 愿意玩
- 家长满意度 ≥ 4.0/5.0 → 愿意付费
- 推荐意愿 ≥ 70% → 有价值

#### 验证时间表

```
Week 11-12: 小规模验证 (5-10个孩子)
  - 2周使用期
  - 收集数据
  - 初步判断

Month 2-3: 中规模验证 (50-100个用户)
  - 发布
  - 收集数据
  - 优化产品

Month 3-4: 决策点
  - 分析所有数据
  - 判断MVP是否成功
  - 决定: 继续开发 / 停止项目 / 调整方向
```

---

## 五、附录

### 附录A: 完整词汇表（180词）

#### Look Island (60词)

**核心词 (10)**: look, see, watch, find, notice, eye, ear, nose, mouth, face
**短语 (10)**: look at, look for, look after, look out, look up, look forward to, look in, look out, look over, look through
**物品 (20)**: glasses, camera, photo, picture, television, TV, computer, phone, newspaper, magazine, book, map, clock, watch, poster, card, letter, email, message, sign
**动作 (10)**: read, write, draw, paint, show, point, stare, gaze, glance, peek

#### Move Valley (60词)

**核心词 (10)**: go, come, move, run, walk, jump, fly, swim, climb, dance
**短语 (8)**: go to, go back, go out, go in, come here, come back, come in, come out
**交通工具 (10)**: car, bus, train, plane, bicycle, boat, ship, taxi, subway, motorcycle
**运动 (10)**: sport, game, play, race, win, lose, score, team, player, coach
**地点 (12)**: park, playground, garden, street, road, bridge, station, airport, hospital, school, library, museum
**动作 (10)**: start, stop, turn, fall, push, pull, sit, stand, lie, wait

#### Say City (60词)

**核心词 (10)**: say, tell, ask, speak, talk, shout, whisper, sing, laugh, cry
**短语 (8)**: say hello, say goodbye, tell a story, tell the truth, ask for, ask about, speak English, talk to
**交流 (12)**: hello, goodbye, good morning, good night, please, thank you, sorry, excuse me, hi, bye, thanks, welcome
**词汇 (10)**: word, sentence, question, answer, story, message, letter, email, phone, call
**人物 (10)**: friend, teacher, student, parent, child, family, brother, sister, mother, father
**描述 (10)**: loud, quiet, fast, slow, happy, sad, angry, excited, tired, hungry

### 附录B: 苏格拉底提问完整库

#### **Look vs See**

```
情境: 孩子混淆look和see

Level 1提示（引导式）:
  "想一想，{object}是在动还是不动？"
  → 继续
  "对！动的东西要用什么？"
  → 提示"watch"或引导说"持续看"

Level 2提示（半帮助）:
  "look是看一眼，see是看见。"
  "{object}是一直在变化的吗？"
  → 引导到"持续看" → watch

Level 3提示（直接）:
  "正确答案是watch。"
  "因为watch是持续看，{object}一直在动。"
  "look是'看'这个动作，see是'看见'这个结果。"
```

#### **Listen vs Hear**

```
Level 1:
  "你是故意听音乐，还是不小心听到了？"
  → 故意 → listen
  → 不小心 → hear

Level 2:
  "listen是'认真听'，hear是'听到'。"
  "音乐是你主动打开的吗？"

Level 3:
  "正确答案是listen。"
  "因为你是主动打开音乐认真听的。"
```

#### **Say vs Tell**

```
Level 1:
  "你是说话的内容，还是告诉某人？"
  → 内容 → say
  → 告诉某人 → tell

Level 2:
  "say后面直接接说的话，tell后面接人。"
  "你是说给谁听吗？"

Level 3:
  "正确答案是{answer}。"
  "因为{reason}"
```

### 附录C: 关键文件清单

#### 设计文档

| 文件 | 描述 | 优先级 |
|------|------|--------|
| Wordland_Design_Doc_v2.md | 产品设计文档，包含所有核心概念和设计原则 | P0 |
| .claude/skills/wordland/skills/game-designer.md | 游戏策划技能定义 | P0 |
| .claude/skills/wordland/skills/education-specialist.md | 教育专家技能定义 | P0 |
| .claude/skills/wordland/skills/python-developer.md | Python开发者技能定义 | P0 |

#### 架构文档

| 文件 | 描述 | 优先级 |
|------|------|--------|
| .claude/arch-refactor.md | 框架设计完整规范 | P1 |
| .claude/session-usage-rule.md | Session管理规范 | P1 |
| .claude/skills/plan-execute-review/README.md | 框架总览 | P1 |
| .claude/skills/plan-execute-review/adapters/adapter-guide.md | 适配器创建指南 | P1 |

#### 工作流配置

| 文件 | 描述 | 优先级 |
|------|------|--------|
| .claude/workflows/wordland-workflow.yaml | Wordland工作流配置 | P0 |

---

## 文档变更记录

| 版本 | 日期 | 变更内容 | 责任人 |
|------|------|----------|--------|
| v1.0 | 2026-02-14 | 初始版本，基于ELICITATE发现制定完整MVP计划 | Claude (Plan Agent) |

---

**文档审阅**：

- [ ] 产品经理
- [ ] 技术负责人
- [ ] 教育顾问
- [ ] 游戏策划
- [ ] 测试负责人

**审阅通过后，即可进入开发阶段。**

---

*本文档为Wordland MVP完整实施计划，仅供内部使用*
