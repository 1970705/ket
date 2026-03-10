# game-designer 游戏设计观察

**角色**: game-designer
**领域**: 游戏策划与玩法设计
**日期**: 2026-02-16
**项目**: Wordland - KET词汇学习游戏

---

## 1. 当前状态评估

### 1.1 已完成的工作

#### 核心游戏循环 ✅
- **Spell Battle 拼写游戏** (`ui/components/SpellBattleGame.kt`)
  - QWERTY虚拟键盘布局
  - 即时反馈系统（红色=错误位置，蓝色=正确）
  - 答案验证逻辑（不区分大小写）
  - 退格键功能

#### 关卡结构 ✅
- **5个关卡，每关6个单词** (`data/seed/LookIslandWords.kt`)
  - Level 1: 基础观察动词 (look, see, watch, eye, glass, find)
  - Level 2: 颜色和光线 (color, red, blue, dark, light, bright)
  - Level 3: 动作和注视 (stare, notice, observe, appear, view, scene)
  - Level 4: 观察行为 (notice, search, check, picture, photo, camera)
  - Level 5: 高级观察 (observe, examine, stare, display, appear, visible)
  - 解锁机制（完成当前关卡解锁下一关）

#### 增强提示系统 ✅ (架构完成，集成待测试)
- **HintGenerator** (`domain/hint/HintGenerator.kt`)
  - 3级渐进式提示
  - Level 1: 首字母提示
  - Level 2: 前半部分提示
  - Level 3: 元音隐藏提示
  - 自适应提示（根据单词长度调整）

- **HintManager** (`domain/hint/HintManager.kt`)
  - 使用次数限制（默认3次）
  - 冷却机制（默认3秒）
  - 难度动态调整

- **BehaviorAnalyzer** (`domain/behavior/BehaviorAnalyzer.kt`)
  - 猜测行为检测
  - 挣扎检测
  - 智能提示推荐

#### UI组件 ✅
- **EnhancedHintCard** (`ui/screens/LearningScreen.kt:31-85`)
  - 提示等级指示器
  - 剩余提示次数显示
  - 视觉反馈

### 1.2 存在的问题

#### 问题1: 星级评分算法过于简单 🔴 严重

**位置**: `domain/usecase/usecases/SubmitAnswerUseCase.kt:126-140`

**当前逻辑**:
```kotlin
private fun calculateStars(
    isCorrect: Boolean,
    isGuessing: Boolean,
    responseTime: Long,
    hintUsed: Boolean
): Int {
    if (!isCorrect) return 0
    if (isGuessing) return 1

    return when {
        hintUsed -> 2
        responseTime < 2000 -> 2  // 快速答对（<2秒）= 2星
        else -> 3
    }
}
```

**问题分析**:
1. 只有4种结果（0/1/2/3星），缺乏细粒度
2. 快速答对（<2秒）被判定为2星而非3星，不合理
3. 没有考虑错误次数（用户可能多次尝试）
4. 使用提示 = 2星，和快速答对相同，逻辑混乱

**对孩子的影响**:
- 10岁孩子在2秒内拼写单词很正常，不应该被"降级"
- 可能导致孩子故意拖延以获得3星

#### 问题2: 等级完成星级不反映实际表现 🔴 严重

**位置**: `ui/viewmodel/LearningViewModel.kt:218`

**当前逻辑**:
```kotlin
_uiState.value = LearningUiState.LevelComplete(
    stars = 3,  // 固定3星！
    score = 100,
    isNextIslandUnlocked = false,
    islandMasteryPercentage = 0.0
)
```

**问题分析**:
- 即使在答题过程中获得1星或2星，完成等级时仍然显示3星
- 没有累积或平均机制
- 丧失了星级评分的意义

#### 问题3: "猜测"判定时间阈值过短 🟡 中等

**位置**: `domain/constants/DomainConstants.kt:26`

**当前设置**:
```kotlin
const val GUESSING_THRESHOLD_FAST = 2000 // 2秒
const val GUESSING_THRESHOLD_VERY_FAST = 1500 // 1.5秒
```

**问题分析**:
- 10岁孩子的平均拼写时间应该在5-15秒
- 2秒阈值会将正常答题的孩子判定为"猜测"
- "猜测"直接给1星，过于严厉

#### 问题4: 提示系统UI已部分集成但未完全测试 🟡 中等

**位置**: `ui/screens/LearningScreen.kt`

**已完成**:
- `EnhancedHintCard` 组件已实现
- 提示按钮已显示剩余次数
- 提示等级已显示

**待测试**:
- 真机测试验证用户体验
- 提示内容显示是否正确
- 提示冷却机制是否生效

---

## 2. 目标和假设

### 2.1 下一阶段目标

#### 目标1: 实现动态星级评分算法 🔴 高优先级
**目的**: 反映孩子真实的努力程度，让评分更公平

**成功标准**:
- 星级与答题质量正相关
- 考虑错误次数、提示使用、答题时间
- 避免不公平的惩罚

#### 目标2: 完善提示系统用户体验 🟡 中优先级
**目的**: 让提示真正成为学习辅助而非惩罚工具

**成功标准**:
- 使用提示不会大幅降低星级
- 提示内容清晰易懂
- 提示是鼓励而非羞辱

#### 目标3: 增加游戏反馈丰富度 🟢 低优先级
**目的**: 让每次互动都有积极反馈

**成功标准**:
- 反馈消息多样化
- 视觉反馈更丰富
- 音效反馈（如果有资源）

### 2.2 关键假设

#### 假设1: 孩子需要正向激励 🔵 需验证
**内容**: 过度惩罚会降低学习兴趣

**验证方法**:
- 真机测试
- 观察孩子对当前评分的反应
- A/B测试不同评分方案

#### 假设2: 10岁孩子的平均拼写时间在5-15秒 🔵 需验证
**内容**: 当前的2秒阈值过低

**验证方法**:
- 收集答题时间数据
- 分析不同年龄段的答题时间分布
- 调整阈值

#### 假设3: 使用提示应该是"聪明的求助" 🔵 需验证
**内容**: 提示不应大幅降低星级

**验证方法**:
- 观察提示使用行为
- 分析提示使用与学习效果的关系
- 调整提示惩罚机制

---

## 3. 任务优先级建议

### P0 - 必须立即解决 🔴

#### 任务1: 重新设计星级评分算法

**文件**: `domain/usecase/usecases/SubmitAnswerUseCase.kt`

**当前问题**:
- 算法对孩子不友好
- "猜测"判定过于严格
- 没有考虑错误次数

**建议方案**:

```kotlin
/**
 * Calculate stars earned based on performance
 * Redesigned for 10-year-old children
 */
private fun calculateStars(
    isCorrect: Boolean,
    isGuessing: Boolean,
    responseTime: Long,
    hintUsed: Boolean,
    attemptCount: Int  // 新增：尝试次数
): Int {
    // 错误答案 = 0星
    if (!isCorrect) return 0

    // 基础分：答对就至少1星
    var baseStars = 1

    // 扣分因素
    when {
        // 多次尝试（>3次）= 保持1星
        attemptCount > 3 -> baseStars = 1
        // 使用提示 = 1星（但可以通过快速答题补偿）
        hintUsed -> baseStars = 1
        // 正常答题 = 2星起步
        else -> baseStars = 2
    }

    // 加分因素
    when {
        // 无错误 + 无提示 + 合理时间 = 3星
        !hintUsed && attemptCount <= 1 && responseTime >= 3000 -> baseStars = 3
        // 使用提示但仍然仔细作答 = 2星（鼓励正确使用提示）
        hintUsed && responseTime >= 5000 -> baseStars = 2
        // 快速答题（但非猜测）= 3星（奖励熟练度）
        !hintUsed && responseTime < 5000 && responseTime >= 3000 -> baseStars = 3
    }

    // 特殊处理：明显的猜测行为（<2秒且无提示）
    if (responseTime < 2000 && !hintUsed && attemptCount == 1) {
        baseStars = 1  // 猜测 = 1星
    }

    return baseStars.coerceIn(0, 3)
}
```

**关键变更**:
1. 新增 `attemptCount` 参数跟踪尝试次数
2. 调整"猜测"阈值判定逻辑
3. 鼓励正确使用提示（提示后仔细作答仍可得2星）
4. 奖励熟练度（快速但非猜测可得3星）

#### 任务2: 实现等级完成星级聚合逻辑

**文件**: `ui/viewmodel/LearningViewModel.kt`

**当前问题**:
- 等级完成时固定显示3星
- 不反映实际表现

**建议方案**:

```kotlin
// 在 LearningViewModel 中添加字段
private var levelStarsAccumulator = mutableListOf<Int>()

// 修改 submitAnswer 方法
fun submitAnswer(userAnswer: String, responseTime: Long, hintUsed: Boolean) {
    viewModelScope.launch {
        val currentWord = _currentWord.value ?: return@launch

        when (val result = submitAnswer(...)) {
            is Result.Success -> {
                // 累积星级
                levelStarsAccumulator.add(result.data.starsEarned)

                _uiState.value = LearningUiState.Feedback(
                    result = result.data.toLearnWordResult(),
                    stars = result.data.starsEarned,
                    progress = calculateProgress()
                )
            }
        }
    }
}

// 修改 onNextWord 方法中的等级完成逻辑
fun onNextWord() {
    currentWordIndex++
    if (currentWordIndex < levelWords.size) {
        // ... 显示下一题
    } else {
        // 计算等级星级
        val averageStars = if (levelStarsAccumulator.isNotEmpty()) {
            levelStarsAccumulator.average()
        } else 0.0

        val finalStars = when {
            averageStars >= 2.5 -> 3
            averageStars >= 1.5 -> 2
            averageStars >= 0.5 -> 1
            else -> 0
        }

        // 重置累积器
        levelStarsAccumulator.clear()

        _uiState.value = LearningUiState.LevelComplete(
            stars = finalStars,  // 使用计算后的星级！
            score = (finalStars * 33).toInt(),  // 简单分数计算
            isNextIslandUnlocked = false,
            islandMasteryPercentage = (finalStars / 3.0 * 100)
        )
    }
}
```

### P1 - 重要但非紧急 🟡

#### 任务3: 优化提示系统用户体验

**目的**: 提示应该是鼓励而非惩罚

**建议方案**:

1. **调整提示惩罚机制**
   ```kotlin
   // 当前: 使用提示直接给2星
   // 建议: 使用提示给2.5星（四舍五入到2或3）
   ```

2. **添加"聪明使用提示"的鼓励语**
   ```kotlin
   // 当用户使用提示后答对时
   "不错！你懂得使用工具来帮助学习！"
   "聪明的做法！继续加油！"
   ```

3. **在等级完成时显示"提示使用次数"而非直接惩罚**
   ```kotlin
   // LevelCompleteScreen 添加统计
   "本关使用了3次提示，下次试试不使用提示！"
   "本关没有使用提示，太棒了！"
   ```

#### 任务4: 增加反馈消息多样性

**文件**: `domain/usecase/usecases/SubmitAnswerUseCase.kt`

**当前问题**: 只有4条反馈消息，容易重复

**建议方案**:

```kotlin
/**
 * Feedback messages for different scenarios
 */
object FeedbackMessages {
    private val threeStarMessages = listOf(
        "Excellent! Perfect answer!",
        "Amazing! You're a star!",
        "Perfect! Well done!",
        "Fantastic! Keep it up!",
        "Wonderful! You're doing great!"
    )

    private val twoStarMessages = listOf(
        "Good job! You're doing great!",
        "Nice work! Almost perfect!",
        "Great effort! Keep going!",
        "Well done! You're improving!",
        "Good try! You can do even better!"
    )

    private val oneStarMessages = listOf(
        "Nice try! Keep it up!",
        "Good effort! Practice makes perfect!",
        "Don't give up! You're learning!",
        "Keep trying! You'll get it!",
        "Every mistake helps you learn!"
    )

    private val hintUsedMessages = listOf(
        "Smart move using the hint!",
        "Good thinking! Hints help us learn!",
        "Wise choice! Keep practicing!"
    )

    private val incorrectMessages = listOf(
        "Not quite right. Keep practicing!",
        "Almost there! Try again!",
        "Don't worry! You'll get it next time!",
        "Keep going! Learning takes time!"
    )

    fun getRandom(stars: Int, hintUsed: Boolean, isCorrect: Boolean): String {
        return when {
            !isCorrect -> incorrectMessages.random()
            hintUsed -> hintUsedMessages.random()
            stars == 3 -> threeStarMessages.random()
            stars == 2 -> twoStarMessages.random()
            else -> oneStarMessages.random()
        }
    }
}
```

### P2 - 可以延后 🟢

#### 任务5: 添加连胜/连续正确奖励机制

**目的**: 增加游戏趣味性

**建议方案**:

```kotlin
// 在 LearningViewModel 中添加连胜追踪
private var consecutiveCorrect = 0
private var bestStreak = 0

fun submitAnswer(userAnswer: String, responseTime: Long, hintUsed: Boolean) {
    viewModelScope.launch {
        val currentWord = _currentWord.value ?: return@launch

        when (val result = submitAnswer(...)) {
            is Result.Success -> {
                if (result.data.isCorrect) {
                    consecutiveCorrect++
                    if (consecutiveCorrect > bestStreak) {
                        bestStreak = consecutiveCorrect
                    }
                } else {
                    consecutiveCorrect = 0
                }

                // 传递连胜信息到UI
                _uiState.value = currentState.copy(
                    streak = consecutiveCorrect,
                    isNewBestStreak = consecutiveCorrect == bestStreak && consecutiveCorrect > 1
                )
            }
        }
    }
}

// UI 中显示连胜效果
@Composable
fun StreakIndicator(streak: Int) {
    if (streak >= 3) {
        Row {
            repeat(streak) {
                Icon(Icons.Default.Flame, "streak", tint = Color.Orange)
            }
            Text("${streak}连胜！")
        }
    }
}
```

---

## 4. 风险识别

### 4.1 技术风险

#### 风险1: 修改评分算法可能影响现有进度数据

**描述**: 数据库中已有的星级记录可能与新算法不一致

**影响**: 中等

**缓解方案**:
- 保持数据库字段兼容
- 新算法只在新的答题记录生效
- 考虑添加数据迁移脚本

#### 风险2: 真机测试可能发现UI性能问题

**描述**: 新增的动画和反馈可能影响性能

**影响**: 低

**缓解方案**:
- 已有性能监控模块 (`PerformanceMonitor.kt`)
- 可实时检测帧率
- 必要时可禁用部分动画

### 4.2 设计风险

#### 风险1: 新的评分算法可能仍然不适合10岁儿童

**描述**: 基于成人经验设计的算法可能不符合儿童行为模式

**影响**: 高

**缓解方案**:
- 需要真机测试
- 最好有真实10岁儿童参与测试
- 准备多个备选方案

#### 风险2: 降低难度可能导致"刷分"行为

**描述**: 孩子可能发现"漏洞"来获得高星级

**影响**: 低

**缓解方案**:
- 保持记忆强度算法不变
- 星级只是表面奖励
- 不影响实际学习进度

#### 风险3: 提示系统可能被过度使用

**描述**: 如果提示惩罚太轻，孩子可能依赖提示

**影响**: 中等

**缓解方案**:
- 保持提示次数限制
- 添加提示冷却时间
- 长期依赖提示会影响星级

### 4.3 资源风险

#### 风险1: 缺乏10岁儿童进行可用性测试

**描述**: 团队成员都是成年人，难以模拟儿童思维

**影响**: 高

**缓解方案**:
- 成年人测试时模拟儿童行为
- 回忆自己10岁时的状态
- 寻找外部儿童测试资源

---

## 5. 对其他角色的建议

### 对 android-architect
- 建议将评分算法作为独立的策略类，方便后续调整
- 考虑添加A/B测试框架，支持不同评分方案

### 对 android-engineer
- 实现时注意添加 `attemptCount` 跟踪
- 确保星级累积逻辑正确

### 对 compose-ui-designer
- 考虑添加连胜视觉特效
- 优化星级显示动画

###对 education-specialist
- 验证评分假设是否符合儿童认知特点
- 提供更准确的年龄段答题时间参考

### 对 android-test-engineer
- 为新的评分算法编写单元测试
- 覆盖各种边界情况

---

## 6. 总结

**当前状态**: 游戏核心机制完整，但评分系统需要针对10岁儿童进行优化

**关键问题**:
1. 星级评分算法过于简单，对孩子不友好
2. 等级完成星级不反映实际表现
3. "猜测"判定时间阈值过短

**优先任务**:
1. 重新设计星级评分算法
2. 实现等级完成星级聚合逻辑
3. 优化提示系统用户体验

**风险**: 缺乏真实儿童测试，设计假设需要验证

---

**文档结束**
