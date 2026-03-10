# 提示系统集成指南

## 概述
本文档说明如何将增强的提示系统集成到现有代码中。

## 新增组件

### 1. HintGenerator
负责生成多级提示内容。

**位置**: `com.wordland.domain.hint.HintGenerator`

**使用方法**:
```kotlin
val generator = HintGenerator()

// Level 1: 首字母
val hint1 = generator.generateHint("apple", 1)  // "首字母: A"

// Level 2: 前半部分
val hint2 = generator.generateHint("apple", 2)  // "前半部分: ap__"

// Level 3: 完整单词（元音隐藏）
val hint3 = generator.generateHint("apple", 3)  // "完整单词（元音隐藏）: _ppl_"

// 自适应提示
val adaptiveHint = generator.generateAdaptiveHint("banana", 1)
```

### 2. HintManager
管理提示使用限制和策略。

**位置**: `com.wordland.domain.hint.HintManager`

**使用方法**:
```kotlin
val manager = HintManager()

// 检查是否可以使用提示
val (canUse, reason) = manager.canUseHint("word_001")
if (!canUse) {
    showMessage(reason)  // "已达到提示次数上限 (3次)"
    return
}

// 使用提示
val level = manager.useHint("word_001")
showHint(generator.generateHint("apple", level))

// 获取剩余提示次数
val remaining = manager.getRemainingHints("word_001")
showMessage("剩余提示: $remaining")
```

### 3. BehaviorAnalyzer
分析用户行为并提供建议。

**位置**: `com.wordland.domain.behavior.BehaviorAnalyzer`

**使用方法**:
```kotlin
val analyzer = BehaviorAnalyzer(trackingRepository)

// 检查是否应该推荐提示
val shouldRecommend = analyzer.shouldRecommendHint(userId, wordId)

// 获取推荐提示等级
val recommendedLevel = analyzer.getRecommendedHintLevel(userId, wordId)

// 分析用户进度
val analysis = analyzer.analyzeUserProgress(userId, wordId)
when (analysis) {
    ProgressAnalysis.STRUGGLING -> showMessage("需要帮助吗？")
    ProgressAnalysis.MAKING_PROGRESS -> showMessage("做得很棒！")
    // ...
}
```

## 集成步骤

### 步骤 1: 更新 LearningViewModel

#### 当前实现
```kotlin
fun useHint() {
    viewModelScope.launch {
        // 只记录提示使用，提示内容直接用pronunciation
        useHint(userId, currentWord.id, levelId)
        _uiState.value = currentState.copy(hintShown = true)
    }
}
```

#### 增强实现
```kotlin
@HiltViewModel
class LearningViewModelEnhanced @Inject constructor(
    private val useHintUseCase: UseHintUseCaseEnhanced,
    private val hintManager: HintManager,
    private val hintGenerator: HintGenerator,
    // ... other dependencies
) : ViewModel() {

    private var currentHintLevel = 0

    fun useHint() {
        viewModelScope.launch {
            val currentWord = _currentWord.value ?: return@launch

            // 检查是否可以使用提示
            val (canUse, reason) = useHintUseCase.canUseHint(currentWord.id)
            if (!canUse) {
                _uiState.value = currentState.copy(
                    message = reason
                )
                return@launch
            }

            // 使用增强的提示UseCase
            when (val result = useHintUseCase(userId, currentWord.id, levelId)) {
                is Result.Success -> {
                    val hintResult = result.data

                    // 更新UI状态
                    _uiState.value = currentState.copy(
                        hintShown = true,
                        hintText = hintResult.hintText,
                        hintLevel = hintResult.hintLevel,
                        hintsRemaining = hintResult.hintsRemaining,
                        hintPenaltyApplied = false
                    )

                    currentHintLevel = hintResult.hintLevel
                }
                is Result.Error -> {
                    when (result.exception) {
                        is HintLimitException -> {
                            _uiState.value = currentState.copy(
                                message = result.exception.message
                            )
                        }
                        else -> {
                            _uiState.value = currentState.copy(
                                message = "提示获取失败"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNextWord() {
        // 重置提示状态
        currentWordIndex++
        if (currentWordIndex < levelWords.size) {
            val nextWord = levelWords[currentWordIndex]
            _currentWord.value = nextWord

            // 重置该单词的提示
            hintManager.resetHints(nextWord.id)
            currentHintLevel = 0

            _uiState.value = LearningUiState.Ready(
                question = generateQuestion(nextWord),
                hintAvailable = true,
                hintShown = false,
                hintText = null,
                hintLevel = 0,
                hintsRemaining = hintManager.getRemainingHints(nextWord.id)
            )
        } else {
            // Level complete
        }
    }
}
```

### 步骤 2: 更新 SubmitAnswerUseCase

添加提示扣分逻辑：

```kotlin
suspend operator fun invoke(
    userId: String,
    wordId: String,
    userAnswer: String,
    responseTime: Long,
    hintUsed: Boolean,
    levelId: String
): Result<SubmitAnswerResult> {

    // ... existing logic ...

    // 计算原始星级
    var stars = when {
        isPerfect && !hintUsed && responseTime < 3000 -> 3
        isCorrect && !hintUsed -> 2
        isCorrect && hintUsed -> 1  // 使用提示降低星级
        isCorrect -> 1
        else -> 0
    }

    // 调整记忆强度增长
    var strengthIncrease = when {
        stars == 3 -> 20
        stars == 2 -> 15
        stars == 1 -> 10
        else -> 0
    }

    // 如果使用了提示，记忆强度增长减半
    if (hintUsed) {
        strengthIncrease = (strengthIncrease / 2).toInt()
    }

    // ... rest of logic ...
}
```

### 步骤 3: 更新 HintCard 组件

增强UI以显示多级提示：

```kotlin
@Composable
fun EnhancedHintCard(
    hintText: String?,
    hintLevel: Int,
    hintsRemaining: Int,
    totalHints: Int,
    onUseHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (hintLevel) {
                0 -> MaterialTheme.colorScheme.surface
                1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                3 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 提示等级指示器
            HintLevelIndicator(
                currentLevel = hintLevel,
                maxLevel = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 提示文本
            if (hintText != null) {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = "需要提示？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 剩余提示次数
            Text(
                text = "剩余提示: $hintsRemaining/$totalHints",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 使用提示按钮
            Button(
                onClick = onUseHint,
                enabled = hintsRemaining > 0
            ) {
                Text("获取提示")
            }
        }
    }
}

@Composable
fun HintLevelIndicator(
    currentLevel: Int,
    maxLevel: Int,
    modifier: Modifier = Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxLevel) { level ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (level < currentLevel) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}
```

## 测试

### HintGeneratorTest
```kotlin
class HintGeneratorTest {
    @Test
    fun `Level 1 hint shows first letter`() {
        val generator = HintGenerator()
        val hint = generator.generateHint("apple", 1)
        assertEquals("首字母: A", hint)
    }

    @Test
    fun `Level 2 hint shows first half`() {
        val generator = HintGenerator()
        val hint = generator.generateHint("banana", 2)
        assertTrue(hint.startsWith("前半部分: ba"))
        assertTrue(hint.contains("_"))
    }
}
```

### HintManagerTest
```kotlin
class HintManagerTest {
    @Test
    fun `allows hints within limit`() {
        val manager = HintManager()
        manager.maxHintsPerWord = 3

        val (canUse1, _) = manager.canUseHint("word1")
        assertTrue(canUse1)

        manager.useHint("word1")
        val (canUse2, _) = manager.canUseHint("word1")
        assertTrue(canUse2)

        // Use all hints
        repeat(2) { manager.useHint("word1") }

        val (canUse3, _) = manager.canUseHint("word1")
        assertFalse(canUse3)
    }

    @Test
    fun `enforces cooldown period`() {
        val manager = HintManager()
        manager.hintCooldownMs = 100 // Short for testing

        manager.useHint("word1")
        val (canUse, _) = manager.canUseHint("word1")

        assertFalse(canUse) // Should be in cooldown
    }
}
```

## 迁移策略

### 阶段1: 并行实现（推荐）
1. 创建新的Enhanced UseCase和ViewModel
2. 保留旧的UseCase作为fallback
3. 逐步迁移UI到新系统

### 阶段2: 直接替换
1. 更新现有UseCase
2. 更新ViewModel
3. 一次性切换

### 阶段3: 渐进式增强
1. 先实现核心功能
2. 添加高级功能
3. 优化用户体验

## 配置选项

### 根据难度调整提示数量
```kotlin
// Easy words
hintManager.setMaxHintsForDifficulty(1)  // 5 hints

// Hard words
hintManager.setMaxHintsForDifficulty(5)  // 1 hint
```

### 调整冷却时间
```kotlin
// Fast mode
hintManager.hintCooldownMs = 1000  // 1 second

// Normal mode
hintManager.hintCooldownMs = 3000  // 3 seconds

// Strict mode
hintManager.hintCooldownMs = 5000  // 5 seconds
```

## 注意事项

1. **向后兼容**: 确保旧的提示记录仍然有效
2. **测试覆盖**: 新功能需要完整的测试
3. **性能**: 提示系统应该是轻量级的
4. **用户体验**: 提示应该是帮助，不是障碍

## 已知限制

1. **提示质量**: 取决于HintGenerator的逻辑
2. **滥用检测**: 可能需要更多的用户数据
3. **多语言**: 当前只支持英文单词
