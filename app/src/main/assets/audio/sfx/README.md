# 游戏音效文件说明

## 当前状态

由于网络下载失败，当前音效文件内容不正确。

## 解决方案

### 方案 A：使用 Android 系统音效（推荐）

修改 `SoundManager.kt` 使用 `ToneGenerator` 生成音效：

```kotlin
// 在 SoundManager 中添加
import android.media.ToneGenerator

fun playCorrectAnswer() {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200) // 正确答案音效
}

fun playWrongAnswer() {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 300) // 错误答案音效
}

fun playCombo(comboLevel: Int) {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    when (comboLevel) {
        1 -> toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        2 -> toneGen.startTone(ToneGenerator.TONE_PROP_BEEP_2, 150)
        3 -> toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        else -> toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }
}

fun playLevelComplete() {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 500)
}
```

### 方案 B：手动提供音效文件

1. 从可靠的免费音效网站下载：
   - [Freesound.org](https://freesound.org/) - 需要免费注册
   - [Mixkit](https://mixkit.co/free-sound-effects/) - 免费，无需注册
   - [Zapsplat](https://www.zapsplat.com/) - 免费音效库

2. 或者使用专业音效：
   - [Adobe Stock Audio](https://stock.adobe.com/free/audio)
   - [Epidemic Sound](https://www.epidemicsound.com/)

3. 将下载的 MP3/WAV 文件放到：
   ```
   app/src/main/assets/audio/sfx/
   ```

   命名规范：
   - `correct.mp3` - 正确答案音效
   - `wrong.mp3` - 错误答案音效
   - `combo.mp3` - 连击音效
   - `level_complete.mp3` - 关卡完成音效

### 方案 C：临时禁用音效

修改 `SoundManager.kt` 添加开关：

```kotlin
var soundEffectsEnabled = false // 设为 false 禁用音效

fun playCorrectAnswer() {
    if (!soundEffectsEnabled) return
    // ... 原有代码
}
```

## 推荐方案

建议采用 **方案 A**（使用系统音效），因为：
- ✅ 无需下载文件
- ✅ 无需担心文件内容错误
- ✅ 跨设备兼容性好
- ✅ APK 体积不受影响

## 音效参数说明

`ToneGenerator.TONE_*` 参数：
- `TONE_PROP_BEEP` - 简单的提示音
- `TONE_PROP_BEEP_2` - 第二种提示音
- `TONE_PROP_NACK` - 否定音效（错误）
- `TONE_CDMA_ALERT_CALL_GUARD` - 警报音
- `TONE_CDMA_ALERT_NETWORK_LITE` - 网络提示音

## 下一步

请告诉我您希望采用哪个方案：
- **方案 A**：我帮您实现 ToneGenerator 音效
- **方案 B**：您提供音效文件后，我帮您集成
- **方案 C**：临时禁用音效

---

生成时间：2026-03-05
版本：1.0
作者：Claude Code (Epic #6 音频集成)
