# 单词消消乐 - 游戏设计文档

**Epic**: #9
**任务**: #6 - 设计游戏逻辑和状态机
**设计师**: game-designer
**日期**: 2026-02-25
**版本**: 1.0

---

## 目录

1. [游戏状态机设计](#1-游戏状态机设计)
2. [核心游戏机制](#2-核心游戏机制)
3. [用户体验设计](#3-用户体验设计)
4. [游戏流程图](#4-游戏流程图)
5. [配对逻辑详细说明](#5-配对逻辑详细说明)

---

## 1. 游戏状态机设计

### 1.1 状态定义

| 状态 | 说明 | 可执行操作 | 持续时间 |
|------|------|-----------|---------|
| **IDLE** | 初始状态，游戏未开始 | 选择单词数量、导入词表、开始游戏 | 不限 |
| **PREPARING** | 准备游戏数据（加载单词、打乱泡泡） | 显示加载动画 | <500ms |
| **READY** | 游戏准备就绪，显示开始界面 | 开始游戏、返回主界面 | 不限 |
| **PLAYING** | 游戏进行中 | 点击泡泡配对、暂停、退出 | 动态（30s-∞） |
| **PAUSED** | 游戏暂停 | 继续游戏、退出游戏 | 不限 |
| **COMPLETED** | 游戏完成，显示结果 | 查看详情、继续挑战、返回主界面 | 不限 |
| **GAME_OVER** | 游戏退出或超时 | 返回主界面、重新开始 | 不限 |

### 1.2 状态转换图

```
                    [开始游戏按钮]
                         ↓
    ┌───────────────────────────────────────┐
    │                                       │
    ↓                    ┌──────────────────┴───────────────┐
┌─────────┐          ┌────────────┐      ┌──────────┐     ┌──────────┐
│  IDLE   │─────────→│ PREPARING  │─────→│  READY   │     │ GAME_OVER│
└─────────┘          └────────────┘      └──────────┘     └──────────┘
    ↑                                       │                   ↑
    │                                   [开始游戏]           [退出]
    │                                       │                   │
    │                                       ↓                   │
    │                                  ┌─────────┐             │
    │                                  │ PLAYING │─────────────┤
    │                                  └─────────┘             │
    │                                       │                   │
    │                    ┌──────────────────┼───────────────┐   │
    │                    │                  │               │   │
    │               [暂停/继续]        [全部消除]       [退出]  │
    │                    │                  │               │   │
    │                    ↓                  ↓               │   │
    │               ┌─────────┐      ┌──────────┐          │   │
    └───────────────│ PAUSED  │      │COMPLETED │──────────┘   │
                    └─────────┘      └──────────┘              │
                         │                  │                   │
                         └──────────────────┴───────────────────┘
                                 [返回主界面/退出]
```

### 1.3 状态转换表

| 当前状态 | 触发事件 | 目标状态 | 条件 |
|---------|---------|---------|------|
| IDLE | 点击"开始游戏" | PREPARING | 选择的单词数量 ≥ 5 |
| IDLE | 导入词表成功 | IDLE | 词表验证通过 |
| PREPARING | 数据准备完成 | READY | 泡泡生成完毕 |
| READY | 点击"开始游戏" | PLAYING | 游戏开始 |
| READY | 返回主界面 | IDLE | 用户主动返回 |
| PLAYING | 点击暂停按钮 | PAUSED | 游戏进行中 |
| PLAYING | 全部泡泡消除 | COMPLETED | matchedPairs == totalPairs |
| PLAYING | 退出游戏 | GAME_OVER | 用户主动退出 |
| PAUSED | 点击继续 | PLAYING | 恢复游戏 |
| PAUSED | 退出游戏 | GAME_OVER | 用户主动退出 |
| COMPLETED | 继续挑战 | PREPARING | 保持当前配置 |
| COMPLETED | 返回主界面 | IDLE | 用户主动返回 |
| GAME_OVER | 重新开始 | PREPARING | 用户选择重新开始 |
| GAME_OVER | 返回主界面 | IDLE | 用户主动返回 |

### 1.4 状态持久化策略

| 状态 | 是否保存 | 保存内容 |
|------|---------|---------|
| IDLE | ❌ | 无 |
| PREPARING | ❌ | 无 |
| READY | ✅ | 游戏配置（单词数量、泡泡状态） |
| PLAYING | ✅ | 游戏进度、已配对、已消除、计时 |
| PAUSED | ✅ | 游戏进度、已配对、已消除、计时 |
| COMPLETED | ❌ | 仅保存完成记录（历史成绩） |
| GAME_OVER | ❌ | 无 |

**持久化时机**:
- 每次配对完成后自动保存
- 暂停时保存
- 应用进入后台时保存

---

## 2. 核心游戏机制

### 2.1 单词选择逻辑

#### 2.1.1 数据来源

**优先级顺序**:
1. **自定义词表**（如果用户导入）
2. **当前岛屿词库**（Look Island / Make Lake）
3. **已完成单词库**（复习模式）

#### 2.1.2 选择算法

```kotlin
/**
 * 单词选择算法
 *
 * @param sourceWords 可用单词池
 * @param pairCount 目标配对数量 (5-50)
 * @return 选中的单词对列表
 */
fun selectWordPairs(
    sourceWords: List<Word>,
    pairCount: Int
): List<WordPair> {
    require(pairCount in 5..50) { "配对数量必须在5-50之间" }

    // 1. 随机打乱单词池
    val shuffled = sourceWords.shuffled()

    // 2. 取前 N 个单词
    val selected = shuffled.take(pairCount)

    // 3. 生成单词对（英文-中文）
    return selected.map { word ->
        WordPair(
            pairId = word.id,  // 使用单词ID作为配对ID
            english = word.text,
            chinese = word.translation,
            difficulty = word.difficulty
        )
    }
}
```

**数据模型**:
```kotlin
data class WordPair(
    val pairId: String,        // 配对唯一标识
    val english: String,       // 英文单词
    val chinese: String,       // 中文翻译
    val difficulty: Int = 1    // 难度等级 (1-5)
)
```

#### 2.1.3 单词池管理

| 词库类型 | 单词数量 | 使用场景 |
|---------|---------|---------|
| **Look Island** | 30 words | 基础模式 |
| **Make Lake** | 30 words | 进阶模式 |
| **全部词库** | 60 words | 挑战模式 |
| **自定义词库** | 不限 | 个性化学习 |

**智能推荐策略**:
- 优先选择用户错误率高的单词（基于历史数据）
- 难度均衡（避免全部简单或全部困难）
- 每次游戏随机选择（避免重复体验）

### 2.2 泡泡打乱算法

#### 2.2.1 泡泡生成流程

```
Step 1: 创建单词对列表
        └→ [(pairId1, "banana", "香蕉"), (pairId2, "red", "红色"), ...]

Step 2: 展开为单个泡泡
        └→ [(id1, "banana", pairId1), (id2, "香蕉", pairId1),
            (id3, "red", pairId2), (id4, "红色", pairId2), ...]

Step 3: 随机打乱顺序
        └→ [(id3, "red", pairId2), (id1, "banana", pairId1),
            (id4, "红色", pairId2), (id2, "香蕉", pairId1), ...]

Step 4: 分配随机背景色
        └→ 为每个泡泡分配6种颜色之一

Step 5: 生成泡泡状态列表
        └→ [BubbleState(...), BubbleState(...), ...]
```

#### 2.2.2 打乱算法实现

```kotlin
/**
 * 泡泡打乱算法
 *
 * @param wordPairs 单词对列表
 * @param colors 可用背景色列表
 * @return 打乱后的泡泡状态列表
 */
fun shuffleBubbles(
    wordPairs: List<WordPair>,
    colors: List<Color> = BUBBLE_COLORS
): List<BubbleState> {
    // 1. 创建所有泡泡（每个单词对生成2个泡泡）
    val bubbles = wordPairs.flatMap { pair ->
        listOf(
            createBubble(pair, pair.english, BubbleType.ENGLISH),
            createBubble(pair, pair.chinese, BubbleType.CHINESE)
        )
    }

    // 2. Fisher-Yates 洗牌算法（确保真随机）
    val shuffled = bubbles.toMutableList()
    val random = Random.Default
    for (i in shuffled.size - 1 downTo 1) {
        val j = random.nextInt(i + 1)
        shuffled[i] = shuffled[j].also { shuffled[j] = shuffled[i] }
    }

    // 3. 分配随机背景色（避免相邻泡泡同色）
    return shuffled.mapIndexed { index, bubble ->
        bubble.copy(
            color = getDistinctColor(colors, index, shuffled.size),
            position = index  // 记录位置用于布局
        )
    }
}

/**
 * 获取与相邻泡泡不同的颜色
 */
private fun getDistinctColor(
    colors: List<Color>,
    index: Int,
    total: Int
): Color {
    val previousIndex = (index - 6).coerceAtLeast(0)  // 6列布局
    // 简化版：直接随机（已足够打乱）
    return colors.random()
}
```

#### 2.2.3 颜色分配策略

**泡泡背景色**:
```kotlin
val BUBBLE_COLORS = listOf(
    Color(0xFFFFB6C1),  // 粉色 #FFB6C1
    Color(0xFF90EE90),  // 绿色 #90EE90
    Color(0xFFDDA0DD),  // 紫色 #DDA0DD
    Color(0xFFFFA500),  // 橙色 #FFA500
    Color(0xFFD2691E),  // 棕色 #D2691E
    Color(0xFF87CEEB)   // 蓝色 #87CEEB
)
```

**颜色分配原则**:
- 随机分配（确保游戏随机性）
- 每次游戏重新分配（避免位置记忆）
- 高对比度文字（白色文字，深色边框）

### 2.3 配对规则

#### 2.3.1 配对逻辑

**基本规则**:
1. 点击第一个泡泡 → 标记为"已选中"（高亮显示）
2. 点击第二个泡泡 → 进行配对检查
3. **匹配成功**:
   - 两个泡泡同时消失
   - 播放消除动画 + 粒子效果
   - 播放成功音效（叮咚声）
   - matchedPairs + 1
4. **匹配失败**:
   - 红色边框闪烁
   - 左右抖动动画
   - 播放失败音效（嘟嘟声）
   - 延迟500ms后取消选中状态

#### 2.3.2 配对检查算法

```kotlin
/**
 * 配对检查
 *
 * @param first 第一个选中的泡泡
 * @param second 第二个选中的泡泡
 * @return 配对结果
 */
data class MatchResult(
    val isMatch: Boolean,
    val message: String,
    val shouldAnimate: Boolean
)

fun checkMatch(
    first: BubbleState,
    second: BubbleState
): MatchResult {
    // 1. 检查是否是同一个泡泡
    if (first.id == second.id) {
        return MatchResult(
            isMatch = false,
            message = "不能选择同一个泡泡",
            shouldAnimate = false
        )
    }

    // 2. 检查是否已经匹配过
    if (first.isMatched || second.isMatched) {
        return MatchResult(
            isMatch = false,
            message = "该泡泡已消除",
            shouldAnimate = false
        )
    }

    // 3. 检查配对ID是否相同
    val isCorrectPair = first.pairId == second.pairId

    return MatchResult(
        isMatch = isCorrectPair,
        message = if (isCorrectPair) "配对成功！" else "配对失败，再试试",
        shouldAnimate = true
    )
}
```

#### 2.3.3 特殊情况处理

| 情况 | 处理方式 |
|------|---------|
| 点击已消除的泡泡 | 忽略（无响应） |
| 点击已选中的泡泡 | 取消选中 |
| 点击同一个泡泡两次 | 取消选中 |
| 快速连续点击多个泡泡 | 只保留最后两个选中的泡泡 |
| 配对失败后快速点击其他泡泡 | 取消之前的选择，响应新的点击 |

### 2.4 完成条件

#### 2.4.1 胜利条件

```kotlin
/**
 * 检查游戏是否完成
 *
 * @param gameState 当前游戏状态
 * @return 是否完成
 */
fun isGameCompleted(gameState: MatchGameState): Boolean {
    return gameState.matchedPairs == gameState.totalPairs
}
```

**完成标志**:
- `matchedPairs == totalPairs` （所有泡泡已消除）
- 自动触发完成动画
- 显示结算界面

#### 2.4.2 计时逻辑

```kotlin
/**
 * 计时器管理
 *
 * - PLAYING 状态: 计时器运行
 * - PAUSED 状态: 计时器暂停
 * - COMPLETED 状态: 计时器停止，保存最终用时
 */
class GameTimer {
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var isRunning: Boolean = false

    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
        }
    }

    fun pause() {
        if (isRunning) {
            elapsedTime = System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    fun reset() {
        startTime = 0L
        elapsedTime = 0L
        isRunning = false
    }

    fun getElapsedTime(): Long {
        return if (isRunning) {
            System.currentTimeMillis() - startTime
        } else {
            elapsedTime
        }
    }
}
```

#### 2.4.3 结算数据

```kotlin
/**
 * 游戏结算数据
 */
data class GameResult(
    val totalPairs: Int,              // 总配对数
    val matchedPairs: Int,            // 已配对数
    val elapsedTime: Long,            // 用时（毫秒）
    val accuracy: Float,              // 准确率（正确配对/总尝试次数）
    val averageTimePerPair: Long,     // 平均每对用时（毫秒）
    val starRating: Int,              // 星级评定（1-3星）
    val isNewRecord: Boolean          // 是否新纪录
)
```

---

## 3. 用户体验设计

### 3.1 首次进入引导

#### 3.1.1 引导流程

**Step 1: 欢迎界面**
```
┌─────────────────────────────────┐
│                                 │
│      🎮 单词消消乐 🎮           │
│                                 │
│     欢迎来到单词配对游戏！       │
│                                 │
│   [跳过引导]  [开始体验]        │
│                                 │
└─────────────────────────────────┘
```

**Step 2: 玩法说明（3页轮播）**

**第1页 - 基础玩法**:
```
┌─────────────────────────────────┐
│         📖 游戏玩法 (1/3)       │
├─────────────────────────────────┤
│                                 │
│   1. 选择单词数量（5-50对）      │
│   2. 点击两个泡泡进行配对        │
│   3. 英文-中文配对成功即消除     │
│                                 │
│   ┌───┐  ┌───┐                  │
│   │ban│  │🍌 │                  │
│   │ana│  │香蕉│ ← 配对成功      │
│   └───┘  └───┘                  │
│                                 │
│          [下一步]                │
└─────────────────────────────────┘
```

**第2页 - 反馈提示**:
```
┌─────────────────────────────────┐
│         📖 游戏玩法 (2/3)       │
├─────────────────────────────────┤
│                                 │
│   配对成功 → 泡泡爆炸消失 💥     │
│   配对失败 → 红色抖动提示 ❌     │
│                                 │
│   ┌───┐  ┌───┐                  │
│   │ban│  │红色│                  │
│   │ana│  │   │ ← 配对失败       │
│   └───┘  └───┘                  │
│                                 │
│   [上一步]  [下一步]             │
└─────────────────────────────────┘
```

**第3页 - 游戏目标**:
```
┌─────────────────────────────────┐
│         📖 游戏玩法 (3/3)       │
├─────────────────────────────────┤
│                                 │
│   全部消除 → 查看用时           │
│   挑战最快速度！⏱️              │
│                                 │
│   当前纪录:                      │
│   🥇 10对 - 15.2秒              │
│   🥈 10对 - 18.7秒              │
│   🥉 10对 - 20.5秒              │
│                                 │
│   [上一步]  [开始游戏]           │
└─────────────────────────────────┘
```

#### 3.1.2 引导记录

**持久化标志**:
```kotlin
data class UserPreferences(
    val hasSeenTutorial: Boolean = false,
    val tutorialVersion: Int = 1
)
```

**显示逻辑**:
- 首次安装 → 强制显示引导
- 引导后更新 `hasSeenTutorial = true`
- 后续启动 → 跳过引导，可在设置中重新查看

### 3.2 游戏反馈设计

#### 3.2.1 视觉反馈

| 事件 | 视觉效果 | 持续时间 |
|------|---------|---------|
| **点击泡泡** | 放大 1.2x + 发光效果 | 300ms |
| **选中第一个泡泡** | 蓝色边框 + 阴影 | 持续 |
| **选中第二个泡泡（匹配成功）** | 泡泡爆炸动画 + 粒子散开 | 800ms |
| **选中第二个泡泡（匹配失败）** | 红色边框闪烁 + 左右抖动 | 500ms |
| **游戏完成** | 全屏彩带 + 星级动画 | 2000ms |

**视觉效果实现**:
```kotlin
// 1. 选中动画（放大 + 发光）
@Composable
fun SelectedBubbleEffect() {
    val scale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Blue,
                spotColor = Color.Blue
            )
    ) {
        // 泡泡内容
    }
}

// 2. 匹配失败抖动
@Composable
fun ShakeAnimation(
    isError: Boolean,
    content: @Composable () -> Unit
) {
    val offset by animateDpAsState(
        targetValue = if (isError) 10.dp else 0.dp,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.offset(x = offset)) {
        content()
    }
}

// 3. 匹配成功粒子效果
@Composable
fun ExplosionEffect(
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn() + expandOut(expandFrom = Alignment.Center)
    ) {
        // 粒子散开动画
    }
}
```

#### 3.2.2 触觉反馈

| 事件 | 触觉反馈 | 说明 |
|------|---------|------|
| 点击泡泡 | 轻微震动 | `HapticFeedbackType.TextHandleMove` |
| 配对成功 | 强震动 | `HapticFeedbackType.LongPress` |
| 配对失败 | 短震动 | `HapticFeedbackType.TextHandleMove` |

**实现方式**:
```kotlin
val hapticFeedback = LocalHapticFeedback.current

// 点击泡泡
hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

// 配对成功
hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
```

#### 3.2.3 音效反馈

| 事件 | 音效 | 文件名 | 时长 |
|------|------|--------|------|
| 点击泡泡 | 啵 | `bubble_click.mp3` | 0.2s |
| 配对成功 | 叮咚 | `match_success.mp3` | 0.5s |
| 配对失败 | 嘟嘟 | `match_fail.mp3` | 0.3s |
| 游戏完成 | 胜利 | `game_complete.mp3` | 1.5s |

**音效管理**:
```kotlin
class SoundManager(private val context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()

    private val sounds = mapOf(
        "click" to loadSound("bubble_click.mp3"),
        "success" to loadSound("match_success.mp3"),
        "fail" to loadSound("match_fail.mp3"),
        "complete" to loadSound("game_complete.mp3")
    )

    fun play(soundName: String) {
        sounds[soundName]?.let { soundId ->
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    private fun loadSound(fileName: String): Int {
        val assetFileDescriptor = context.assets.openFd("sounds/$fileName")
        return soundPool.load(assetFileDescriptor, 1)
    }
}
```

### 3.3 结算界面设计

#### 3.3.1 结算界面布局

```
┌─────────────────────────────────┐
│      🎉 恭喜完成！🎉           │
├─────────────────────────────────┤
│                                 │
│         ⭐⭐⭐ 3星              │
│                                 │
│   ┌─────────────────────────┐   │
│   │  总用时: 15.2秒         │   │
│   │  准确率: 92.3%          │   │
│   │  平均每对: 1.52秒       │   │
│   └─────────────────────────┘   │
│                                 │
│   🏆 最佳纪录: 14.8秒           │
│                                 │
│   [🔄 继续挑战]  [🏠 返回]      │
│                                 │
└─────────────────────────────────┘
```

#### 3.3.2 星级评定标准

```kotlin
/**
 * 星级计算
 *
 * @param accuracy 准确率（0.0-1.0）
 * @param averageTime 每对平均用时（毫秒）
 * @param totalPairs 总配对数
 * @return 星级（1-3）
 */
fun calculateStarRating(
    accuracy: Float,
    averageTime: Long,
    totalPairs: Int
): Int {
    // 1. 准确率不足 60% → 1星
    if (accuracy < 0.6f) return 1

    // 2. 基准时间（每对3秒）
    val baseTimePerPair = 3000L

    // 3. 时间得分（0-100）
    val timeScore = when {
        averageTime <= baseTimePerPair -> 100
        averageTime <= baseTimePerPair * 1.5 -> 80
        averageTime <= baseTimePerPair * 2 -> 60
        else -> 40
    }

    // 4. 综合得分（准确率 40% + 时间 60%）
    val totalScore = accuracy * 40 + timeScore * 0.6f

    // 5. 星级判定
    return when {
        totalScore >= 85 -> 3
        totalScore >= 70 -> 2
        else -> 1
    }
}
```

**星级示例**:
| 准确率 | 平均每对用时 | 星级 |
|-------|------------|------|
| 100% | ≤3秒 | ⭐⭐⭐ |
| 90% | ≤4.5秒 | ⭐⭐⭐ |
| 80% | ≤6秒 | ⭐⭐ |
| 70% | ≤6秒 | ⭐⭐ |
| <60% | 任意 | ⭐ |

#### 3.3.3 成就系统

**成就类型**:
```kotlin
sealed class Achievement(
    val id: String,
    val title: String,
    val description: String
) {
    // 速度成就
    object SpeedDemon : Achievement(
        "speed_demon",
        "闪电侠",
        "10对单词在20秒内完成"
    )

    object LightSpeed : Achievement(
        "light_speed",
        "光速",
        "50对单词在120秒内完成"
    )

    // 准确率成就
    object PerfectScore : Achievement(
        "perfect_score",
        "完美通关",
        "100%准确率完成游戏"
    )

    object Consistency : Achievement(
        "consistency",
        "稳定发挥",
        "连续5次准确率≥90%"
    )

    // 数量成就
    object Marathon : Achievement(
        "marathon",
        "马拉松",
        "完成50对单词配对"
    )

    // 连胜成就
    object WinningStreak : Achievement(
        "winning_streak",
        "连胜大师",
        "连续10次游戏获得3星"
    )
}
```

**成就解锁动画**:
```
┌─────────────────────────────────┐
│                                 │
│        🏆 解锁成就 🏆           │
│                                 │
│         ⚡ 闪电侠 ⚡            │
│                                 │
│     10对单词在20秒内完成        │
│                                 │
│        [太棒了！]               │
│                                 │
└─────────────────────────────────┘
```

---

## 4. 游戏流程图

### 4.1 完整游戏流程

```
开始
  ↓
┌─────────────┐
│  IDLE 状态  │
└─────────────┘
  ↓
[用户选择单词数量: 5-50对]
  ↓
┌─────────────┐
│ PREPARING   │ ← 加载单词数据
│ 准备数据    │
└─────────────┘
  ↓
[从词库随机选择 N 对单词]
  ↓
[生成 2N 个泡泡（英文+中文）]
  ↓
[Fisher-Yates 洗牌算法打乱]
  ↓
[分配随机背景色]
  ↓
┌─────────────┐
│  READY 状态 │
└─────────────┘
  ↓
[用户点击"开始游戏"]
  ↓
┌─────────────┐
│ PLAYING 状态│ ← 启动计时器
└─────────────┘
  ↓
┌─────────────────────────────────┐
│                                 │
│  [循环: 等待用户点击泡泡]        │
│         ↓                       │
│  [用户点击第一个泡泡]            │
│         ↓                       │
│  [高亮显示选中状态]              │
│         ↓                       │
│  [用户点击第二个泡泡]            │
│         ↓                       │
│  ┌─────────────────┐            │
│  │ 检查配对是否正确 │            │
│  └─────────────────┘            │
│         ↓                       │
│    ┌────┴────┐                  │
│    ↓          ↓                  │
│  [正确]     [错误]               │
│    ↓          ↓                  │
│ [播放成功]  [播放失败]           │
│ [爆炸动画]  [抖动动画]           │
│ [泡泡消除]  [取消选中]           │
│ matchedPairs+1                   │
│    ↓          ↓                  │
│    └────┬────┘                  │
│         ↓                       │
│  [检查: matchedPairs == totalPairs?] │
│         ↓                       │
│    ┌────┴────┐                  │
│    ↓          ↓                  │
│  [是]       [否]                 │
│    ↓          └──────────────┐   │
│ [停止计时器]                 │   │
│    ↓                         │   │
│ [计算星级]                   │   │
│    ↓                         │   │
│ ┌─────────────┐              │   │
│ │ COMPLETED   │              │   │
│ │ 显示结算界面 │              │   │
│ └─────────────┘              │   │
│    ↓                         │   │
│ [保存游戏记录]               │   │
│    ↓                         │   │
│ [用户选择: 继续挑战/返回]     │   │
│    ↓                         │   │
│ [继续] ────────→ PREPARING    │   │
│ [返回] ────────→ IDLE         │   │
│                              │   │
│                              ←───┘
└─────────────────────────────────┘
```

### 4.2 暂停/恢复流程

```
PLAYING 状态
  ↓
[用户点击暂停按钮 / 按返回键]
  ↓
┌─────────────┐
│  PAUSED     │ ← 暂停计时器
│  暂停游戏   │   保存游戏进度
└─────────────┘
  ↓
[显示暂停菜单]
  ↓
┌─────────────────────────────────┐
│  ⏸️ 游戏已暂停                  │
│                                 │
│  [▶️ 继续游戏]  [🏠 退出游戏]   │
└─────────────────────────────────┘
  ↓                    ↓
[点击继续]           [点击退出]
  ↓                    ↓
PLAYING 状态        GAME_OVER 状态
```

### 4.3 数据流图

```
用户操作
  ↓
UI Layer (Compose)
  ↓
ViewModel (GameViewModel)
  ↓
UseCase Layer
  ├─ SelectWordPairsUseCase (选择单词)
  ├─ ShuffleBubblesUseCase (打乱泡泡)
  ├─ CheckMatchUseCase (检查配对)
  └─ CalculateStarRatingUseCase (计算星级)
  ↓
Domain Layer
  ├─ MatchGameConfig (配置)
  ├─ MatchGameState (状态)
  └─ GameResult (结果)
  ↓
Data Layer
  ├─ WordRepository (词库)
  └─ GameHistoryRepository (历史记录)
```

---

## 5. 配对逻辑详细说明

### 5.1 配对算法

#### 5.1.1 配对ID生成规则

**规则**:
- 每个单词对（英文-中文）共享同一个 `pairId`
- `pairId` 使用单词的唯一标识符（如 `word.id`）

**示例**:
```kotlin
// 单词数据
Word(
    id = "look_001",
    text = "look",
    translation = "看"
)

// 生成的泡泡对
Bubble(
    id = "bubble_1_en",
    text = "look",
    pairId = "look_001"  // 配对ID
)

Bubble(
    id = "bubble_1_zh",
    text = "看",
    pairId = "look_001"  // 配对ID（相同）
)
```

#### 5.1.2 配对检查流程

```
输入: firstBubble, secondBubble
  ↓
Step 1: 检查是否是同一个泡泡
  └─ if (first.id == second.id) → 返回 "不能选择同一个泡泡"
  ↓
Step 2: 检查泡泡是否已经消除
  └─ if (first.isMatched || second.isMatched) → 返回 "该泡泡已消除"
  ↓
Step 3: 检查配对ID是否相同
  └─ if (first.pairId == second.pairId) → 配对成功 ✅
  └─ else → 配对失败 ❌
  ↓
Step 4: 返回配对结果
```

#### 5.1.3 配对状态机

```
BubbleState
  ├─ id: String (唯一标识)
  ├─ text: String (显示文字)
  ├─ pairId: String (配对ID)
  ├─ isSelected: Boolean (是否选中)
  └─ isMatched: Boolean (是否已消除)

状态转换:
  未选中 (isSelected=false, isMatched=false)
    ↓ 点击
  已选中 (isSelected=true, isMatched=false)
    ↓ 配对成功
  已消除 (isSelected=false, isMatched=true) ← 最终状态

  或:
  已选中 (isSelected=true, isMatched=false)
    ↓ 配对失败
  未选中 (isSelected=false, isMatched=false) ← 回到初始状态
```

### 5.2 选中逻辑

#### 5.2.1 选中策略

| 当前选中数量 | 点击新泡泡 | 操作 |
|------------|----------|------|
| 0 | 任意 | 选中第一个泡泡 |
| 1 | 已选中的泡泡 | 取消选中 |
| 1 | 未选中的泡泡 | 选中第二个泡泡，触发配对检查 |
| 2 | 任意 | 清空之前的选择，选中当前泡泡 |

#### 5.2.2 选中状态管理

```kotlin
/**
 * 选中状态管理
 */
class SelectionManager {
    private val selectedBubbles = mutableSetOf<String>()

    /**
     * 处理泡泡点击
     *
     * @param bubbleId 点击的泡泡ID
     * @return 是否触发配对检查
     */
    fun handleBubbleClick(bubbleId: String): Boolean {
        return when {
            // 情况1: 点击已选中的泡泡 → 取消选中
            selectedBubbles.contains(bubbleId) -> {
                selectedBubbles.remove(bubbleId)
                false  // 不触发配对检查
            }

            // 情况2: 已选中2个泡泡 → 清空之前的选择，选中当前泡泡
            selectedBubbles.size >= 2 -> {
                selectedBubbles.clear()
                selectedBubbles.add(bubbleId)
                false  // 不触发配对检查（等待第二个选择）
            }

            // 情况3: 选中第一个泡泡
            selectedBubbles.isEmpty() -> {
                selectedBubbles.add(bubbleId)
                false  // 不触发配对检查
            }

            // 情况4: 选中第二个泡泡 → 触发配对检查
            else -> {
                selectedBubbles.add(bubbleId)
                true  // 触发配对检查
            }
        }
    }

    /**
     * 清空选中状态
     */
    fun clearSelection() {
        selectedBubbles.clear()
    }

    /**
     * 获取选中的泡泡ID列表
     */
    fun getSelectedBubbles(): Set<String> {
        return selectedBubbles.toSet()
    }
}
```

### 5.3 配对动画时序

#### 5.3.1 成功配对动画时序

```
0ms    ━━━━━━━━ 选中第二个泡泡
       ↓
50ms   ┌─────────────────────────────────┐
       │  播放音效: match_success.mp3    │
       └─────────────────────────────────┘
       ↓
100ms  ┌─────────────────────────────────┐
       │  开始粒子爆炸动画                │
       │  (粒子从泡泡中心向外扩散)        │
       └─────────────────────────────────┘
       ↓
200ms  ┌─────────────────────────────────┐
       │  泡泡淡出动画 (fadeOut)          │
       │  透明度 1.0 → 0.0 (200ms)       │
       └─────────────────────────────────┘
       ↓
400ms  ┌─────────────────────────────────┐
       │  泡泡从UI中移除                  │
       │  (AnimatedVisibility visible=false) │
       └─────────────────────────────────┘
       ↓
800ms  ━━━━━━━━ 动画完成，UI更新
```

#### 5.3.2 失败配对动画时序

```
0ms    ━━━━━━━━ 选中第二个泡泡，检测配对失败
       ↓
50ms   ┌─────────────────────────────────┐
       │  播放音效: match_fail.mp3        │
       └─────────────────────────────────┘
       ↓
100ms  ┌─────────────────────────────────┐
       │  红色边框闪烁                    │
       │  (边框颜色: Blue → Red)          │
       └─────────────────────────────────┘
       ↓
150ms  ┌─────────────────────────────────┐
       │  左右抖动动画 (Shake)            │
       │  3次往复运动 (300ms)             │
       └─────────────────────────────────┘
       ↓
450ms  ┌─────────────────────────────────┐
       │  红色边框淡出                    │
       │  (边框颜色: Red → Blue)          │
       └─────────────────────────────────┘
       ↓
500ms  ━━━━━━━━ 动画完成，取消选中状态
```

### 5.4 配对统计

#### 5.4.1 统计指标

```kotlin
/**
 * 配对统计数据
 */
data class MatchStatistics(
    val totalAttempts: Int,        // 总尝试次数
    val successfulMatches: Int,    // 成功配对次数
    val failedMatches: Int,        // 失败配对次数
    val accuracy: Float,           // 准确率
    val averageTimePerPair: Long,  // 平均每对用时（毫秒）
    val fastestMatch: Long,        // 最快配对（毫秒）
    val slowestMatch: Long         // 最慢配对（毫秒）
)

/**
 * 计算统计数据
 */
fun calculateStatistics(
    matchHistory: List<MatchRecord>
): MatchStatistics {
    val totalAttempts = matchHistory.size
    val successfulMatches = matchHistory.count { it.isSuccess }
    val failedMatches = totalAttempts - successfulMatches

    val accuracy = if (totalAttempts > 0) {
        successfulMatches.toFloat() / totalAttempts
    } else {
        0f
    }

    val matchTimes = matchHistory
        .filter { it.isSuccess }
        .map { it.timeTaken }

    val averageTimePerPair = if (matchTimes.isNotEmpty()) {
        matchTimes.average().toLong()
    } else {
        0L
    }

    val fastestMatch = matchTimes.minOrNull() ?: 0L
    val slowestMatch = matchTimes.maxOrNull() ?: 0L

    return MatchStatistics(
        totalAttempts = totalAttempts,
        successfulMatches = successfulMatches,
        failedMatches = failedMatches,
        accuracy = accuracy,
        averageTimePerPair = averageTimePerPair,
        fastestMatch = fastestMatch,
        slowestMatch = slowestMatch
    )
}
```

#### 5.4.2 历史记录

```kotlin
/**
 * 单次配对记录
 */
data class MatchRecord(
    val pairId: String,        // 配对ID
    val firstBubbleId: String, // 第一个泡泡ID
    val secondBubbleId: String,// 第二个泡泡ID
    val isSuccess: Boolean,    // 是否成功
    val timeTaken: Long,       // 用时（毫秒）
    val timestamp: Long        // 时间戳
)

/**
 * 游戏历史记录
 */
data class GameHistory(
    val gameId: String,                // 游戏ID
    val totalPairs: Int,               // 总配对数
    val elapsedTime: Long,             // 总用时（毫秒）
    val statistics: MatchStatistics,   // 统计数据
    val starRating: Int,               // 星级
    val date: Long                     // 日期
)
```

---

## 6. 实施检查清单

### 6.1 状态机实现

- [ ] 定义所有状态枚举
- [ ] 实现状态转换逻辑
- [ ] 添加状态持久化
- [ ] 编写状态机单元测试

### 6.2 游戏逻辑实现

- [ ] 实现单词选择算法
- [ ] 实现泡泡打乱算法
- [ ] 实现配对检查逻辑
- [ ] 实现计时器管理
- [ ] 实现星级计算

### 6.3 UI实现

- [ ] 设计并实现泡泡组件
- [ ] 实现选中动画
- [ ] 实现成功消除动画
- [ ] 实现失败抖动动画
- [ ] 实现结算界面

### 6.4 用户体验

- [ ] 实现首次引导流程
- [ ] 添加音效反馈
- [ ] 添加触觉反馈
- [ ] 实现成就系统
- [ ] 实现历史记录查看

---

## 7. 附录

### 7.1 术语表

| 术语 | 说明 |
|------|------|
| **泡泡** | 单词配对的UI元素，显示英文或中文 |
| **泡泡对** | 一个英文泡泡 + 一个中文泡泡（配对ID相同） |
| **配对** | 用户选择两个泡泡进行匹配的行为 |
| **消除** | 配对成功后泡泡消失的过程 |
| **pairId** | 泡泡配对的唯一标识符 |
| **准确率** | 成功配对次数 / 总尝试次数 |

### 7.2 参考资料

- [需求文档](/Users/panshan/git/ai/ket/docs/requirements/09-word-match-game.md)
- [游戏逻辑设计](/Users/panshan/git/ai/ket/docs/planning/Epic9/GAME_LOGIC_DESIGN.md)
- [Jetpack Compose 动画文档](https://developer.android.com/jetpack/compose/animation)
- [游戏设计模式：状态机](https://gameprogrammingpatterns.com/state.html)

---

**文档版本**: 1.0
**最后更新**: 2026-02-25
**状态**: ✅ 完成初稿
**下一步**: 等待团队评审
