# Audio Resources Acquisition Guide

**Epic**: #6 Task 2 - 准备游戏音效资源文件
**Version**: 1.0
**Last Updated**: 2026-03-04
**Author**: game-designer + education-specialist

---

## Overview

本指南提供获取 Wordland 游戏所需音效资源的具体步骤和资源链接。

---

## Quick Start - 5分钟完成

### 必需音效（P0 优先级）

| 文件名 | 描述 | 时长 | 推荐资源 |
|--------|------|------|----------|
| `sfx_correct.mp3` | 正确答案音效 | 0.5-1s | [Mixkit](https://mixkit.co/free-sound-effects/success/) |
| `sfx_incorrect.mp3` | 错误答案音效 | 0.5-1s | [Mixkit](https://mixkit.co/free-sound-effects/wrong/) |
| `sfx_combo_3.mp3` | 3连击音效 | 0.5s | [Freesound](https://freesound.org/) |
| `sfx_level_complete.mp3` | 关卡完成音效 | 2-3s | [Mixkit](https://mixkit.co/free-sound-effects/win/) |

---

## 推荐资源网站

### 1. Mixkit (最推荐)

**网址**: https://mixkit.co/free-sound-effects/

**优点**:
- ✅ 完全免费，无需署名
- ✅ 高质量音频
- ✅ 直接下载 MP3 格式
- ✅ 无需注册

**推荐音效**:

| 分类 | 链接 | 文件命名 |
|------|------|----------|
| 正确答案 | https://mixkit.co/free-sound-effects/success/ | sfx_correct.mp3 |
| 错误答案 | https://mixkit.co/free-sound-effects/wrong/ | sfx_incorrect.mp3 |
| 胜利/完成 | https://mixkit.co/free-sound-effects/win/ | sfx_level_complete.mp3 |
| 按钮点击 | https://mixkit.co/free-sound-effects/click/ | sfx_click.mp3 |
| 成就解锁 | https://mixkit.co/free-sound-effects/achievement/ | sfx_achievement.mp3 |

### 2. Freesound.org

**网址**: https://freesound.org/

**优点**:
- ✅ 海量音效库
- ✅ 高质量
- ⚠️ 部分需要署名

**Combo 音效搜索关键词**:
- `combo` - 连击音效
- `chime` - 铃声
- `bell` - 钟声
- `sparkle` - 闪光音效

**搜索步骤**:
1. 访问 https://freesound.org/
2. 搜索关键词
3. 筛选 "Duration: 0-1s"
4. 下载 MP3 格式

### 3. Pixabay Sound Effects

**网址**: https://pixabay.com/music/sound-effects/

**优点**:
- ✅ 完全免费，无需署名
- ✅ 无需注册
- ✅ 商业使用友好

---

## 详细下载指南

### 步骤 1: sfx_correct.mp3 (正确答案)

**推荐方案 A - Mixkit**:
1. 访问: https://mixkit.co/free-sound-effects/success/
2. 选择类似 "Game success sound" 或 "Correct answer"
3. 点击 "Free Download"
4. 重命名为 `sfx_correct.mp3`

**推荐方案 B - Pixabay**:
1. 访问: https://pixabay.com/music/sound-effects/
2. 搜索 "success" 或 "correct"
3. 筛选时长 < 1秒
4. 下载并重命名

**音效特征**:
- 欢快、积极
- 儿童友好
- 不要过于刺耳

### 步骤 2: sfx_incorrect.mp3 (错误答案)

**推荐方案 A - Mixkit**:
1. 访问: https://mixkit.co/free-sound-effects/wrong/
2. 选择柔和的失败提示音
3. 下载并重命名

**推荐方案 B - Pixabay**:
1. 访问: https://pixabay.com/music/sound-effects/
2. 搜索 "wrong" 或 "error"
3. 选择温和的音效

**音效特征**:
- 柔和、不打击积极性
- 避免刺耳的蜂鸣声
- 简短（0.5秒内）

### 步骤 3: sfx_combo_3.mp3 (Combo 音效)

**推荐方案 - Freesound**:
1. 访问: https://freesound.org/
2. 搜索 "chime" 或 "bell"
3. 筛选条件:
   - Duration: 0-1s
   - License: CC0 (无需署名)
4. 下载并重命名

**备选关键词**:
- `ding`
- `positive chime`
- `game chime`

### 步骤 4: sfx_level_complete.mp3 (关卡完成)

**推荐方案 - Mixkit**:
1. 访问: https://mixkit.co/free-sound-effects/win/
2. 选择类似 "Level complete" 或 "Victory"
3. 确保时长 2-3秒
4. 下载并重命名

**音效特征**:
- 庆祝感
- 胜利感
- 可以包含上升的旋律

---

## 文件放置

### 目标目录

```bash
app/src/main/res/raw/
```

### 操作步骤

```bash
# 1. 确保目录存在
mkdir -p app/src/main/res/raw/

# 2. 复制下载的文件到该目录
cp ~/Downloads/sfx_correct.mp3 app/src/main/res/raw/
cp ~/Downloads/sfx_incorrect.mp3 app/src/main/res/raw/
cp ~/Downloads/sfx_combo_3.mp3 app/src/main/res/raw/
cp ~/Downloads/sfx_level_complete.mp3 app/src/main/res/raw/

# 3. 验证文件
ls -lh app/src/main/res/raw/sfx_*.mp3
```

### 预期输出

```
-rw-r--r-- 1 user staff  45K Mar  4 10:00 sfx_correct.mp3
-rw-r--r-- 1 user staff  38K Mar  4 10:01 sfx_incorrect.mp3
-rw-r--r-- 1 user staff  28K Mar  4 10:02 sfx_combo_3.mp3
-rw-r--r-- 1 user staff 156K Mar  4 10:03 sfx_level_complete.mp3
```

---

## 音频规格验证

### 推荐工具

```bash
# macOS/Linux - 使用 ffprobe
# 安装: brew install ffmpeg

# 验证音频格式
ffprobe app/src/main/res/raw/sfx_correct.mp3

# 预期输出包含:
# Stream #0:0: Audio: mp3, 44100 Hz, mono, fltp, 128 kb/s
```

### 可接受规格

| 参数 | 可接受范围 | 推荐值 |
|------|-----------|--------|
| 格式 | MP3, OGG | MP3 |
| 采样率 | 44.1kHz, 48kHz | 44.1kHz |
| 比特率 | 96-192 kbps | 128 kbps |
| 声道 | Mono, Stereo | Mono |
| 文件大小 | < 500KB | < 200KB |

---

## 许可证追踪

### 下载记录模板

创建 `app/src/main/res/raw/AUDIO_SOURCES.md`:

```markdown
# Audio Sources License Record

| File | Source | URL | License | Attribution |
|------|--------|-----|---------|--------------|
| sfx_correct.mp3 | Mixkit | https://mixkit.co/... | Free (no attribution) | No |
| sfx_incorrect.mp3 | Mixkit | https://mixkit.co/... | Free (no attribution) | No |
| sfx_combo_3.mp3 | Freesound | https://freesound.org/... | CC0 | No |
| sfx_level_complete.mp3 | Pixabay | https://pixabay.com/... | Free (no attribution) | No |

**Last Updated**: 2026-03-04
```

---

## 验收检查表

### 文件准备

- [ ] 创建 `app/src/main/res/raw/` 目录
- [ ] 下载 `sfx_correct.mp3` (< 500KB)
- [ ] 下载 `sfx_incorrect.mp3` (< 500KB)
- [ ] 下载 `sfx_combo_3.mp3` (< 500KB)
- [ ] 下载 `sfx_level_complete.mp3` (< 500KB)

### 质量检查

- [ ] 所有文件可正常播放
- [ ] 音量水平一致
- [ ] 无背景噪音
- [ ] 时长符合规格

### 文档记录

- [ ] 创建 `AUDIO_SOURCES.md` 许可证记录
- [ ] 记录所有音效来源

---

## 备选方案

### 方案 A: 使用 Android 系统音效

如果无法获取外部音效，可以使用系统内置音效：

```kotlin
// 使用系统音效
val soundEffect = when (type) {
    CORRECT -> SoundEffect.TONE_PROP_ACK
    WRONG -> SoundEffect.TONE_PROP_NACK
    COMBO -> SoundEffect.TONE_CDMA_ALERT_NETWORK_LITE
    LEVEL_COMPLETE -> SoundEffect.TONE_CDMA_ALERT_CALL_GUARD
}
```

### 方案 B: 程序化生成音效

使用 Android ToneGenerator 生成简单音效：

```kotlin
val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

// 正确答案 - 高音
toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)

// 错误答案 - 低音
toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
```

---

## 下一步

完成音效资源准备后：

1. **Task 6.3**: 实现 AudioManager
   - 参考: `docs/design/AUDIO_IMPLEMENTATION_GUIDE.md`
   - 创建: `domain/audiomanager/AudioManager.kt`

2. **Task 6.4**: 集成音效到游戏逻辑
   - LearningViewModel 添加音效调用
   - 测试音效播放

---

## 故障排除

### 问题: 下载的文件无法播放

**解决方案**:
1. 检查文件格式是否为 MP3
2. 尝试使用音频转换工具重新编码
3. 确认文件未损坏

### 问题: 音效音量不一致

**解决方案**:
1. 使用音频编辑软件（如 Audacity）调整音量
2. 统一标准化到 -3dB
3. 或在代码中使用音量调整

### 问题: 文件太大

**解决方案**:
1. 重新编码为更低比特率（96kbps）
2. 裁剪多余静音部分
3. 使用单声道代替立体声

---

## 联系支持

如有问题，请联系:
- **game-designer**: 音效设计咨询
- **android-engineer**: 技术实现支持

---

**Version History**:
| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-03-04 | Initial version with specific resource links |
