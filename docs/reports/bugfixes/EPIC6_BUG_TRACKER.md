# Epic #6 音频系统 - Bug 追踪

**创建日期**: 2026-03-07
**Epic**: Epic #6 音频系统集成
**状态**: 🔴 待修复

---

## 📊 Bug 总览

| Bug ID | 严重性 | 状态 | 组件 |
|--------|--------|------|------|
| **P0-BUG-AUDIO-001** | P0 | 🔴 待修复 | TTSController |
| **P0-BUG-AUDIO-002** | P0 | 🔴 待修复 | QuickJudge + SoundManager |
| **P0-BUG-AUDIO-003** | P0 | 🔴 待修复 | LearningScreen + TTS |

**预计修复时间**: 3-4 小时

---

## 🐛 Bug 详情

### P0-BUG-AUDIO-001: TTS 初始化失败

**发现日期**: 2026-03-07
**发现人**: android-test-engineer-retest
**严重性**: P0（Critical）
**状态**: 🔴 待修复

**症状**:
- ✅ TTS 按钮显示在 LearningScreen 右上角
- ❌ TTS 引擎初始化失败（status = -1）
- ❌ 用户点击按钮不会听到发音

**Logcat 证据**:
```
D TTSController: onInit called with status: -1 (SUCCESS=0)
E TTSController: TTS initialization failed with status: -1
```

**根本原因**:
1. 设备 TTS 引擎可能未正确配置或被禁用
2. **代码注释有误导**：第 109 行注释说 `SUCCESS = -1`，但实际 `SUCCESS = 0`
3. 缺少错误处理和降级方案

**影响**:
- 用户无法使用单词发音功能
- 核心功能缺失

**位置**:
- 文件: `app/src/main/java/com/wordland/media/TTSController.kt`
- 行号: 第 109 行附近

**修复建议**:
1. 修复代码注释（SUCCESS = 0, not -1）
2. 添加 TTS 初始化失败的用户提示
3. 考虑降级方案：显示错误消息而非静默失败
4. 添加设备 TTS 检测和引导

**验收标准**:
- [ ] TTS 初始化成功（status = 0）
- [ ] 点击按钮能听到发音
- [ ] 初始化失败时显示友好提示

**预计时间**: 1-2 小时

---

### P0-BUG-AUDIO-002: QuickJudge 未调用 SoundManager

**发现日期**: 2026-03-07
**发现人**: android-test-engineer-retest
**严重性**: P0（Critical）
**状态**: 🔴 待修复

**症状**:
- ✅ QuickJudgeViewModel 导入了 SoundManager
- ✅ 声明了 soundManager 参数
- ❌ 但没有调用任何音效方法
- ❌ Logcat 中完全没有 `SoundManager` 日志

**代码证据**:
- `QuickJudgeViewModel.kt` 有 `import SoundManager`
- 但 `submitAnswer()` 等方法中**没有**调用：
  - `playCorrectAnswer()`
  - `playWrongAnswer()`
  - `playCombo()`

**根本原因**:
- android-engineer-2 只添加了导入和声明
- 未在 `submitAnswer()` 方法中实际调用音效方法

**影响**:
- 答题无音效反馈
- 用户体验缺失

**位置**:
- 文件: `app/src/main/java/com/wordland/ui/viewmodel/QuickJudgeViewModel.kt`
- 方法: `submitAnswer()`, `handleTimeUp()` 等

**修复建议**:
1. 在 `submitAnswer()` 中调用 `soundManager.playCorrectAnswer()` 或 `playWrongAnswer()`
2. 在 combo 达到阈值时调用 `playCombo()`
3. 检查音频设置开关再播放
4. 添加异常处理

**代码示例**:
```kotlin
private fun submitAnswer(answer: String) {
    val isCorrect = // ... 现有逻辑

    // 添加音效
    if (audioSettings.soundEffectsEnabled) {
        if (isCorrect) {
            soundManager.playCorrectAnswer()
            if (combo >= 3) soundManager.playCombo()
        } else {
            soundManager.playWrongAnswer()
        }
    }

    // ... 现有逻辑
}
```

**验收标准**:
- [ ] 答对题有正确音效
- [ ] 答错题有错误音效
- [ ] Combo 达到阈值有连击音效
- [ ] 遵守音频设置开关

**预计时间**: 1 小时

---

### P0-BUG-AUDIO-003: LearningScreen TTS 按钮未连接功能

**发现日期**: 2026-03-07
**发现人**: android-test-engineer-retest
**严重性**: P0（Critical）
**状态**: 🔴 待修复

**症状**:
- ✅ TTS 按钮显示在界面
- ❌ 点击没有调用 `TTSController.speakWord()`
- ❌ 点击无任何效果

**根本原因**:
- android-engineer 创建了 `TTSSpeakerButtonEmoji` UI 组件
- 添加到 LearningScreen
- 但未将按钮点击事件连接到 `TTSController.speakWord()` 方法

**影响**:
- 用户点击按钮无效果
- UI 存在但功能缺失

**位置**:
- 文件: `app/src/main/java/com/wordland/ui/screens/LearningScreen.kt`
- 组件: `TTSSpeakerButtonEmoji`

**修复建议**:
1. 在 `TTSSpeakerButtonEmoji` 中添加 `onClick` 处理
2. 连接到 `ttsController.speakWord(currentWord)`
3. 检查 TTS 是否就绪再播放
4. 更新按钮状态（播放中动画）

**代码示例**:
```kotlin
TTSSpeakerButtonEmoji(
    isReady = ttsController.isReady,
    isPlaying = isPlaying,
    onClick = {
        if (ttsController.isReady) {
            ttsController.speakWord(currentWord)
            isPlaying = true
        }
    }
)
```

**验收标准**:
- [ ] 点击按钮播放当前单词发音
- [ ] 播放时按钮显示 🔊 动画
- [ ] 播放完成后恢复 🔈 状态
- [ ] TTS 未就绪时按钮禁用

**预计时间**: 1 小时

---

## 🔧 修复顺序建议

### 阶段 1: 最简单的修复（1 小时）
**Bug #2: QuickJudge 音效集成**
- 修复最简单
- 影响最大
- 立即改善用户体验

### 阶段 2: TTS 按钮连接（1 小时）
**Bug #3: TTS 按钮功能连接**
- 难度中等
- 让按钮真正工作

### 阶段 3: TTS 初始化调试（1-2 小时）
**Bug #1: TTS 初始化失败**
- 最复杂
- 可能需要设备调试
- 添加错误处理

---

## 🧪 修复验证计划

### 1. 单元测试
- [ ] QuickJudge 音效调用测试
- [ ] TTS 按钮点击测试
- [ ] TTS 初始化状态测试

### 2. 真机测试
- [ ] TTS 发音功能（10+ 单词）
- [ ] 音效反馈（正确/错误/combo）
- [ ] 音频设置开关验证
- [ ] Logcat 监控无错误

### 3. 回归测试
- [ ] 确保修复未引入新问题
- [ ] 验证现有功能正常

---

## 📚 相关文档

### Epic #6 文档
- Epic #6 计划: `docs/planning/epics/Epic6/`
- Epic #6 完成报告: `docs/reports/quality/EPIC6_COMPLETION_REPORT.md`
- 会话状态: `docs/reports/quality/EPIC6_SESSION_STATUS_2026-03-07.md`

### 测试报告
- 真机测试报告: `docs/reports/testing/EPIC6_REAL_DEVICE_INTEGRATION_TEST_REPORT.md`（待生成）

---

## 📊 优先级矩阵

| Bug | 用户影响 | 修复难度 | 优先级 |
|-----|----------|----------|--------|
| **Bug #2** | 高 | 低 | ⭐⭐⭐ 最高 |
| **Bug #3** | 高 | 中 | ⭐⭐ 高 |
| **Bug #1** | 高 | 高 | ⭐ 中高 |

**建议修复顺序**: Bug #2 → Bug #3 → Bug #1

---

## 🎯 完成标准

Epic #6 音频系统修复完成的定义：

- [ ] ✅ TTS 按钮能播放单词发音
- [ ] ✅ 答题有音效反馈（正确/错误/combo）
- [ ] ✅ 音频设置开关生效
- [ ] ✅ 真机测试通过（logcat 无错误）
- [ ] ✅ 所有 P0 bug 关闭
- [ ] ✅ 完成报告生成

---

**文档创建**: 2026-03-07
**最后更新**: 2026-03-07
**维护者**: Team Lead
**状态**: 🔴 3 个 P0 bug 待修复
