# 音效文件替换指南

## 问题概述

当前所有 4 个音效文件内容都错误，需要重新下载并替换。

**文件位置**: `app/src/main/assets/audio/sfx/`

## 文件规格要求

| 参数 | 要求 | 说明 |
|------|------|------|
| **格式** | MP3 | MPEG ADTS, layer III |
| **采样率** | 44.1 kHz 或 48 kHz | 标准音频采样率 |
| **比特率** | 128 kbps | 平衡音质和文件大小 |
| **声道** | 单声道或立体声 | JntStereo 或 Mono |
| **时长** | 1-3 秒 | 短促音效，避免过长 |
| **文件大小** | 10-100 KB | 保持 APK 体积合理 |

## 需要的音效类型

### 1. `correct.mp3` - 正确答案音效

**情感**: 积极、鼓励、成功
**音效类型**:
- ✅ 清脆的 "ding" 声
- ✅ 简短的上升音调
- ✅ 硬币收集声
- ✅ 成功提示音

**关键词搜索**: success, correct, ding, positive, chime, coin collect

**参考示例**:
- Mixkit: "Success 1" or "Game Success"
- Freesound: "ding", "chime", "positive"

---

### 2. `wrong.mp3` - 错误答案音效

**情感**: 温和的提醒，不刺耳
**音效类型**:
- ✅ 柔和的 "buzz" 声
- ✅ 低沉的下降音调
- ✅ 温和的错误提示音
- ❌ 避免过于刺耳的声音

**关键词搜索**: wrong, error, fail, buzz, negative (gentle)

**参考示例**:
- Mixkit: "Wrong 1" or "Game Error"
- Freesound: "buzzer", "error", "wrong"

---

### 3. `combo.mp3` - 连击音效

**情感**: 兴奋、奖励、升级
**音效类型**:
- ✅ 连续上升的音调
- ✅ 多层叠加的音效
- ✅ 能量增强的声音
- ✅ 级联音效（如 "ding-ding-ding"）

**关键词搜索**: combo, streak, multi-hit, power up, positive升级

**参考示例**:
- Mixkit: "Positive 3" or "Success Streak"
- Freesound: "combo", "streak", "power-up"

---

### 4. `level_complete.mp3` - 关卡完成音效

**情感**: 庆祝、胜利、宏大
**音效类型**:
- ✅ 胜利号角声
- ✅ 完整的旋律
- ✅ 烟花/庆祝音效
- ✅ 长一点的音效（2-3秒）

**关键词搜索**: win, victory, level complete, celebrate, fanfare

**参考示例**:
- Mixkit: "Win 1" or "Level Complete"
- Freesound: "victory", "fanfare", "celebrate"

---

## 推荐的免费音效资源网站

### 1. Mixkit（当前使用，但需要重新下载）
- 网址: https://mixkit.co/free-sound-effects/
- 优点: 无需注册，免费商用
- 分类: success, wrong, positive, win
- 建议: 仔细试听后再下载

### 2. Freesound.org
- 网址: https://freesound.org/
- 优点: 庞大的音效库，高质量
- 要求: 需要免费注册
- 搜索技巧: 使用关键词 + "sfx" 或 "effect"

### 3. Zapsplat
- 网址: https://www.zapsplat.com/
- 优点: 专业音效库
- 要求: 需要免费注册，部分音效需署名

### 4. Google YouTube Audio Library
- 网址: https://www.youtube.com/audiolibrary/sound_effects
- 优点: 免费使用，无需署名
- 要求: 需要 Google 账户

---

## 下载和替换步骤

### 步骤 1: 下载音效文件

1. 访问推荐的音效网站
2. 搜索对应的关键词
3. **在线试听确认**音效内容正确
4. 下载 MP3 格式文件

### 步骤 2: 验证文件规格

使用以下命令验证音频文件：
```bash
# 检查文件信息
file path/to/sound.mp3

# 检查音频详细信息
ffmpeg -i path/to/sound.mp3 2>&1 | grep -E "(Duration|Audio)"
```

### 步骤 3: 重命名文件

确保文件名完全匹配：
- `correct.mp3`
- `wrong.mp3`
- `combo.mp3`
- `level_complete.mp3`

### 步骤 4: 替换文件

```bash
# 备份现有文件（可选）
cp app/src/main/assets/audio/sfx/*.mp3 ~/backup_sounds/

# 替换新文件
cp /path/to/new/correct.mp3 app/src/main/assets/audio/sfx/
cp /path/to/new/wrong.mp3 app/src/main/assets/audio/sfx/
cp /path/to/new/combo.mp3 app/src/main/assets/audio/sfx/
cp /path/to/new/level_complete.mp3 app/src/main/assets/audio/sfx/
```

### 步骤 5: 重新编译和测试

```bash
# 清理构建
./gradlew clean

# 重新编译
./gradlew assembleDebug

# 安装到真机
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动应用并测试音效
adb shell am start -n com.wordland/.ui.MainActivity
```

---

## 临时解决方案：禁用音效

如果您暂时无法下载正确的音效文件，可以先禁用音效功能：

### 选项 A: 注释音效调用

修改 `LearningViewModel.kt`:
```kotlin
// 在 submitAnswer() 中注释音效调用
if (isCorrect) {
    correctAnswersInLevel++
    // playCorrectSound()  // 临时禁用
    triggerPetAnimation(PetAnimationState.HAPPY)
} else {
    totalWrongAnswersInLevel++
    // playWrongSound()  // 临时禁用
}
```

### 选项 B: 使用系统音效

修改 `SoundManager.kt` 使用 `ToneGenerator`:
```kotlin
import android.media.ToneGenerator
import android.media.AudioManager

// 替换 MediaPlayer 播放
fun playCorrectAnswer() {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
}
```

---

## 验证清单

替换音效文件后，使用以下清单验证：

- [ ] 正确答案播放成功音效（不是下雨声）
- [ ] 错误答案播放错误音效
- [ ] 连击（3题以上）播放连击音效
- [ ] 关卡完成播放庆祝音效
- [ ] 所有音效时长合理（1-3秒）
- [ ] 音效音量适中，不会吓到用户
- [ ] 音效风格统一，符合儿童学习应用

---

## 文件大小限制

为保持 APK 体积合理：
- 单个音效文件: ≤ 100 KB
- 4 个音效总计: ≤ 400 KB
- 当前 APK 大小: ~13 MB

---

## 最后更新
- 日期: 2026-03-05
- 版本: 1.0
- 作者: Claude Code (Epic #6 音频集成)
