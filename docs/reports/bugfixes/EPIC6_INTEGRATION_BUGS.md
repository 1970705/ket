# Epic #6 音频系统集成 Bug 报告

**报告日期**: 2026-03-04
**测试设备**: Xiaomi 24031PN0DC (Aurora)
**严重程度**: P0 (阻塞功能)
**状态**: ⚠️ 待修复

---

## Bug #1: TTS 功能未集成到学习界面

**Bug ID**: P0-BUG-EPIC6-001
**优先级**: P0
**状态**: ⚠️ Open

### 问题描述
在学习界面点击发音按钮（TTSSpeakerButtonEmoji），没有触发单词发音，logcat 中也没有相关日志。

### 复现步骤
1. 启动应用并进入学习界面（LearningScreen）
2. 点击单词发音按钮（位于问题卡片右上角）
3. 观察是否有单词发音输出
4. 检查 logcat 日志

### 预期行为
- TTSController 成功初始化（status = SUCCESS）
- 听到清晰的英语单词发音
- 日志显示 "TTS initialized successfully" 和 "Speaking: [word]"

### 实际行为
- 点击按钮后无任何响应
- logcat 中没有 TTS 相关日志
- TTSSpeakerButtonEmoji 组件未触发 TTSController

### 根本原因
**分析中** - 可能的原因：
1. TTSController 未在 LearningViewModel 中初始化
2. 发音按钮的 onClick 事件未正确绑定到 TTSController.speak()
3. TTSController 初始化失败但错误被静默处理

### 修复建议
**文件**: `LearningViewModel.kt`

```kotlin
class LearningViewModel(
    // ... 现有依赖
    private val ttsController: TTSController, // 添加 TTS 依赖
) : ViewModel() {
    // ...

    /**
     * Play word pronunciation using TTS
     */
    fun speakWord(word: String) {
        viewModelScope.launch {
            try {
                if (ttsController.isReady()) {
                    ttsController.speak(word)
                    Log.d(TAG, "Speaking word: $word")
                } else {
                    Log.w(TAG, "TTS not ready, cannot speak: $word")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to speak word: $word", e)
            }
        }
    }
}
```

**文件**: `LearningScreen.kt`

```kotlin
@Composable
private fun QuestionCard(
    // ... 现有参数
    onSpeakWord: (String) -> Unit, // 添加回调
) {
    // ...

    TTSSpeakerButtonEmoji(
        onClick = { onSpeakWord(question.word) },
        modifier = Modifier.align(Alignment.TopEnd)
    )
}
```

**依赖注入**: `di/AppServiceLocator.kt`

```kotlin
fun provideLearningViewModel(
    // ... 现有参数
): LearningViewModel {
    return LearningViewModel(
        // ... 现有依赖
        ttsController = TTSController(application),
    )
}
```

### 验证步骤
1. 重新构建并安装 APK
2. 进入学习界面
3. 点击发音按钮
4. 验证能听到单词发音
5. 检查 logcat 日志确认 TTS 调用

---

## Bug #2: SoundManager 未集成到答题流程

**Bug ID**: P0-BUG-EPIC6-002
**优先级**: P0
**状态**: ⚠️ Open

### 问题描述
答题时（正确或错误答案）没有播放相应的音效，关卡完成时也没有庆祝音效。

### 复现步骤
1. 进入学习界面（LearningScreen）
2. 输入正确答案并提交
3. 观察是否有音效播放
4. 检查 logcat 日志中的 SoundManager 记录
5. 输入错误答案并提交
6. 再次观察音效和日志

### 预期行为
- 正确答案：播放 correct.mp3
- 错误答案：播放 wrong.mp3
- 连续答对：播放 combo.mp3
- 关卡完成：播放 level_complete.mp3
- logcat 显示 "Playing sound effect: [文件名]"

### 实际行为
- 没有任何音效播放
- logcat 中没有 SoundManager 相关日志
- SubmitAnswerUseCase 和 LearningViewModel 都没有调用 SoundManager

### 根本原因
**已确认** - SoundManager 实现完整，但未集成到游戏流程：
- LearningViewModel 没有 SoundManager 依赖
- SubmitAnswerUseCase 不负责音效播放（设计正确）
- LearningViewModel 应该在处理 UseCase 结果后播放音效

### 修复建议

**步骤 1**: 在 LearningViewModel 中添加 SoundManager 依赖

```kotlin
class LearningViewModel(
    // ... 现有依赖
    private val soundManager: SoundManager, // 添加 SoundManager
) : ViewModel() {
    // ...
}
```

**步骤 2**: 在 submitAnswer 方法中播放音效

```kotlin
fun submitAnswer(userAnswer: String) {
    viewModelScope.launch {
        _uiState.value = LearningUiState.Submitting

        try {
            val result = submitAnswerUseCase(userAnswer)

            // 播放音效
            when (result) {
                is LearnWordResult.Correct -> {
                    soundManager.playCorrectAnswer()
                    Log.d(TAG, "Played correct answer sound effect")
                }
                is LearnWordResult.Incorrect -> {
                    soundManager.playWrongAnswer()
                    Log.d(TAG, "Played wrong answer sound effect")
                }
            }

            // 更新 UI 状态
            _uiState.value = LearningUiState.Feedback(
                result = result,
                stars = result.stars,
                progress = calculateProgress()
            )

            // 检查关卡完成
            if (isLevelComplete()) {
                soundManager.playLevelComplete()
                Log.d(TAG, "Played level complete sound effect")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit answer", e)
            _uiState.value = LearningUiState.Error(
                message = e.message ?: "提交答案失败"
            )
        }
    }
}
```

**步骤 3**: 添加 combo 音效支持

```kotlin
// 在 LearningViewModel 中添加 combo 计数
private var currentCombo = 0

private fun updateCombo(result: LearnWordResult) {
    when (result) {
        is LearnWordResult.Correct -> {
            currentCombo++
            if (currentCombo >= 3) {
                soundManager.playCombo(currentCombo)
                Log.d(TAG, "Played combo sound for level: $currentCombo")
            }
        }
        is LearnWordResult.Incorrect -> {
            currentCombo = 0
        }
    }
}
```

**步骤 4**: 在 AppServiceLocator 中提供 SoundManager

```kotlin
fun provideLearningViewModel(
    // ... 现有参数
): LearningViewModel {
    return LearningViewModel(
        // ... 现有依赖
        soundManager = provideSoundManager(),
    )
}
```

### 验证步骤
1. 重新构建并安装 APK
2. 进入学习界面
3. 答对一题 → 验证播放 correct.mp3
4. 答错一题 → 验证播放 wrong.mp3
5. 连续答对 3 题 → 验证播放 combo.mp3
6. 完成关卡 → 验证播放 level_complete.mp3
7. 检查 logcat 日志确认所有音效调用

---

## 测试检查清单

### TTS 功能验证
- [ ] TTSController 成功初始化（logcat: "TTS initialized successfully"）
- [ ] 点击发音按钮能听到单词发音
- [ ] 日志显示 "Speaking: [word]"
- [ ] 多次点击发音都能正常工作
- [ ] 快速连续点击不会导致崩溃

### 音效功能验证
- [ ] 正确答案播放 correct.mp3
- [ ] 错误答案播放 wrong.mp3
- [ ] Combo 3 播放 combo.mp3
- [ ] Combo 5 播放 combo_5.mp3（如果实现）
- [ ] Combo 10 播放 combo_10.mp3（如果实现）
- [ ] 关卡完成播放 level_complete.mp3
- [ ] 日志显示所有音效调用
- [ ] 音效不会相互截断

### 音频设置验证
- [ ] 可以进入音频设置界面
- [ ] 音量滑块能正常调节
- [ ] 开关切换能禁用/启用音效
- [ ] TTS 语言切换生效
- [ ] 设置在重启应用后保持

---

## 优先级说明

### P0 (必须立即修复)
- **TTS 集成**: 这是核心学习功能，用户需要听单词发音
- **SoundManager 集成**: 音效是游戏反馈的重要部分，影响用户体验

### 修复顺序建议
1. 先修复 TTS 集成（相对简单，单一功能）
2. 再修复 SoundManager 集成（涉及多个音效和 combo 逻辑）
3. 最后验证音频设置持久化

### 预计工作量
- TTS 集成：2-3 小时
- SoundManager 集成：3-4 小时
- 测试验证：1-2 小时
- **总计**：6-9 小时

---

## 相关文档
- [Epic #6 完成报告](quality/EPIC6_COMPLETION_REPORT.md)
- [真机测试报告](testing/EPIC6_TASK5_REAL_DEVICE_TEST_REPORT.md)
- [SoundManager API 文档](../../../app/src/main/java/com/wordland/media/SoundManager.kt)
- [TTSController API 文档](../../../app/src/main/java/com/wordland/media/TTSController.kt)

---

**报告创建时间**: 2026-03-04 14:30
**报告作者**: Claude Code (android-test-engineer-audio)
**下一步**: 分配给 android-engineer 进行修复
