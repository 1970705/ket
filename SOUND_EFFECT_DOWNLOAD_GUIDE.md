# 音效文件手动下载指南

## 快速下载（5-10 分钟）

### 方法 1: Mixkit (最推荐) ✅

**步骤**:

1. **打开浏览器**，访问以下链接：

   | 音效 | 链接 |
   |------|------|
   | correct.mp3 | https://mixkit.co/free-sound-effects/success/ |
   | wrong.mp3 | https://mixkit.co/free-sound-effects/wrong/ |
   | combo 音效 | https://mixkit.co/free-sound-effects/positive/ |
   | level_complete.mp3 | https://mixkit.co/free-sound-effects/win/ |

2. **选择音效**:
   - 搜索类似 "Game success", "Correct answer", "Positive" 的音效
   - 点击音效卡片
   - 点击右上角 **"Free Download"** 按钮

3. **重命名并保存**:
   - 浏览器下载后，重命名为指定文件名
   - 移动到项目目录：
     ```bash
     ~/Downloads/下载的文件.mp3 → ~/git/ai/ket/app/src/main/assets/audio/sfx/
     ```

---

### 方法 2: Pixabay (备选)

**网址**: https://pixabay.com/music/sound-effects/

**步骤**:
1. 搜索 "success", "wrong", "win", "combo"
2. 筛选时长 < 1 秒
3. 下载并重命名

---

### 方法 3: Freesound (备选)

**网址**: https://freesound.org/

**步骤**:
1. 注册免费账户
2. 搜索关键词
3. 筛选条件：
   - Duration: 0-1s
   - License: CC0 (无需署名)
4. 下载并重命名

---

## 验证步骤

下载完成后，验证文件：

```bash
# 1. 检查文件是否存在
ls -lh app/src/main/assets/audio/sfx/

# 2. 验证是 MP3 格式
file app/src/main/assets/audio/sfx/*.mp3

# 应该显示:
# correct.mp3: Audio data with 44100 Hz
# wrong.mp3: Audio data with 44100 Hz
```

---

## 快速命令验证

```bash
# 构建项目验证资源打包
./gradlew assembleDebug

# 如果成功，输出 APK:
# app/build/outputs/apk/debug/app-debug.apk
```

---

**下一步**: 下载完成后，继续执行 Task #5 真机测试

**需要帮助?** 查看 `docs/design/game/AUDIO_RESOURCES_ACQUISITION_GUIDE.md` 获取详细指南
