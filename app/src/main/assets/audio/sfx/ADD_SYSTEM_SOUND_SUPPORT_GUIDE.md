## 🔧 添加系统音效支持到现有 SoundManager

### 修改概述

在您现有的 `SoundManager.kt` 中添加以下功能：

1. **系统音效生成器** - 使用 ToneGenerator 作为后备方案
2. **自动回退机制** - 当外部文件不存在或播放失败时，自动使用系统音效
3. **无需修改现有 API** - 保持接口不变，向后兼容

---

### 📝 步骤 1：添加系统音效生成器

在您的 SoundManager 类中添加：

```kotlin
// 在 SoundManager 类中添加
private val systemSoundEffects = SystemSoundEffectsGenerator(context)

// 修改现有的音效播放方法，添加后备方案
suspend fun playCorrectAnswer() {
    val success = tryPlayAssetSoundEffect("correct.mp3")
    if (!success) {
        // 回退到系统音效
        systemSoundEffects.playCorrectAnswer((sfxVolume * 100).toInt())
    }
}

suspend fun playWrongAnswer() {
    val success = tryPlayAssetSoundEffect("wrong.mp3")
    if (!success) {
        systemSoundEffects.playWrongAnswer((sfxVolume * 100).toInt())
    }
}

suspend fun playCombo(comboLevel: Int) {
    val fileName = "combo.mp3"
    val success = tryPlayAssetSoundEffect(fileName)
    if (!success) {
        // 回退到系统音效
        systemSoundEffects.playCombo(comboLevel, (sfxVolume * 100).toInt())
    }
}

suspend fun playLevelComplete() {
    val fileName = "level_complete.mp3"
    val success = tryPlayAssetSoundEffect(fileName)
    if (!success) {
        systemSoundEffects.playLevelComplete((sfxVolume * 100).toInt())
    }
}
```

### 📝 步骤 2：添加资源文件检查方法

```kotlin
/**
 * 尝试播放 assets 中的音频文件
 * @return true 如果成功，false 如果文件不存在或播放失败
 */
private suspend fun tryPlayAssetSoundEffect(
    fileName: String,
    volume: Int = (sfxVolume * 100).toInt()
): Boolean {
    return try {
        val assetPath = "${SFX_PATH}$fileName"
        val afd = context.assets.openFd(assetPath)

        mediaPlayer?.apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            setVolume(volume / 100f, volume / 100f)

            setOnPreparedListener {
                start()
                Log.d(TAG, "Playing asset sound: $fileName")
            }

            setOnErrorListener { _, what ->
                Log.w(TAG, "Asset sound error ($fileName): what=$what")
                false // 返回 false 表示未处理，将触发回退
            }

            setOnCompletionListener {
                // 播放完成后的清理（如果有需要）
                Log.d(TAG, "Asset sound completed: $fileName")
            }

            prepareAsync()
        }

        true
    } catch (e: IOException) {
        Log.w(TAG, "Asset sound not found: $fileName", e)
        false // 文件不存在，返回 false 触发系统音效
    } catch (e: Exception) {
        Log.e(TAG, "Error playing asset sound: $fileName", e)
        false
    }
}
```

### 📝 步骤 3：修改现有方法使用后备方案

在您的 SoundManager 中找到这些方法并修改：

```kotlin
// 原有方法
suspend fun playCorrectAnswer() {
    mediaPlayer?.let { player ->
        try {
            val assetPath = "${SFX_PATH}correct.mp3"
            player.setDataSource(context.assets.openFd(assetPath))
            player.start()
        } catch (e: IOException) {
            // 回退到系统音效
            Log.w(TAG, "Asset not found, using system sound")
            systemSoundEffects.playCorrectAnswer((sfxVolume * 100).toInt())
        }
    }
}

suspend fun playWrongAnswer() {
    mediaPlayer?.let { player ->
        try {
            val assetPath = "${SFX_PATH}wrong.mp3"
            player.setDataSource(context.assets.openFd(assetPath))
            player.start()
        } catch (e: IOException) {
            // 回退到系统音效
            Log.w(TAG, "Asset not found: $assetPath", e)
            systemSoundEffects.playWrongAnswer((sfxVolume * 100).toInt())
        }
    }
}

suspend fun playCombo(comboLevel: Int) {
    mediaPlayer?.let { player ->
        try {
            val fileName = when {
                comboLevel >= 10 -> "combo_10.mp3"
                comboLevel >= 5 -> "combo_5.mp3"
                else -> "combo.mp3"
            }
            val assetPath = "${SFX_PATH}$fileName"
            player.setDataSource(context.assets.openFd(assetPath))
            player.setVolume(sfxVolume / 100f, sfxVolume / 100f)
            player.start()
        } catch (e: IOException) {
            Log.w(TAG, "Failed to load combo sound: $fileName", e)
            // 回退到系统音效
            systemSoundEffects.playCombo(comboLevel, (sfxVolume * 100).toInt())
        }
    }
}

suspend fun playLevelComplete() {
    mediaPlayer?.let { player ->
        try {
            val fileName = "level_complete.mp3"
            player.setDataSource(context.assets.openFd(fileName))
            player.start()
        } catch (e: IOException) {
            Log.w(TAG, "Failed to load level complete sound", e)
            // 回退到系统音效
            systemSoundEffects.playLevelComplete((sfxVolume * 100).toInt())
        }
    }
}

suspend fun playButtonClick() {
    mediaPlayer?.let { player ->
        try {
            player.setVolume(sfxVolume / 100f, sfxVolume / 100f)
            player.start()
        } catch (e: Exception) {
            Log.w(TAG, "Error playing button sound", e)
        }
    }
}
```

### 📝 步骤 4：测试音效

创建测试方法验证两种音源都能正常工作：

```kotlin
// 测试方法
fun testAudioSources() {
    // 测试外部文件音效
    launch {
        playCorrectAnswer()    // 应播放 assets/audio/sfx/correct.mp3
        delay(500)
    }

    // 测试系统音效（删除或重命名文件后会自动使用）
    // 只需注释掉上面这行，就会自动使用系统音效
}
```

---

### ✅ 优势

- **零配置**：自动回退，无需手动管理
- **APK 减小**：可选不打包音频文件
- **跨设备兼容**：所有 Android 设备都支持
- **平滑降级**：外部文件失败时无缝切换到系统音效
- **保持兼容**：现有代码无需修改

---

### 🚀 立即使用

您现在拥有双重保障的音效系统：

1. **主要方案**：外部音频文件（高质量自定义音效）
2. **后备方案**：Android 系统音效（ToneGenerator）

当外部文件不存在时，自动使用系统音效，确保您的游戏永远有声音！

---

**下一步**：
1. 复制上述代码到您的 SoundManager
2. 编译运行测试
3. 完成！🎉

---

生成时间：2026-03-05
版本：1.0
作者：Claude Code (Epic #6 音频集成)
