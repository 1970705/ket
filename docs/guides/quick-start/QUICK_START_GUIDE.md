# 快速开始指南 - 设备测试

**APK 已构建完成**: `app/build/outputs/apk/debug/app-debug.apk` (8.4 MB)

---

## 方法 1: 使用 Android Studio（推荐）

1. **打开项目**
   ```bash
   open -a "Android Studio" /Users/panshan/git/ai/ket
   ```

2. **启动模拟器**
   - 点击 Android Studio 右上角的设备下拉菜单
   - 选择 "Medium_Phone_API_36.1" 或其他可用模拟器
   - 点击运行按钮 ▶️

3. **应用会自动安装并启动**

---

## 方法 2: 使用命令行 + 真实设备

### 步骤 1: 连接设备
```bash
# 1. 在手机上启用"开发者选项"和"USB 调试"
# 设置 → 关于手机 → 连续点击"版本号"7次
# 设置 → 系统 → 开发者选项 → USB 调试 ✓

# 2. 使用 USB 线连接手机到 Mac

# 3. 验证连接
export ANDROID_HOME=~/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"
adb devices
# 应该显示你的设备 ID
```

### 步骤 2: 安装 APK
```bash
cd /Users/panshan/git/ai/ket
adb install -r app/build/outputs/apk/debug/app-debug.apk
# 看到 "Success" 表示安装成功
```

### 步骤 3: 启动应用
```bash
adb shell am start -n com.wordland/.MainActivity
# 或者在手机上点击 Wordland 图标
```

---

## 方法 3: 使用命令行 + 模拟器

### 步骤 1: 启动模拟器
```bash
export ANDROID_HOME=~/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# 启动模拟器（在新终端窗口运行）
emulator -avd Medium_Phone_API_36.1 &

# 等待 30-60 秒让模拟器启动
```

### 步骤 2: 等待设备就绪
```bash
# 在新终端窗口运行
adb wait-for-device
adb shell getprop sys.boot_completed
# 应该返回 "1"
```

### 步骤 3: 安装并启动
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.wordland/.MainActivity
```

---

## 快速测试清单（5 分钟）

### ✅ 启动测试
- [ ] 应用图标出现在主屏幕
- [ ] 点击图标后应用启动
- [ ] 显示 HomeScreen（有"岛屿地图"、"每日复习"、"学习进度"按钮）

### ✅ 导航测试
- [ ] 点击"岛屿地图" → 显示 IslandMapScreen
- [ ] 看到"Look Island"卡片
- [ ] 点击 Look Island → 显示 5 个关卡
- [ ] 点击"Level 1" → 进入学习界面

### ✅ 游戏测试
- [ ] 看到中文翻译："看，观看"
- [ ] 看到虚拟键盘（QWERTY 布局）
- [ ] 看到答案框
- [ ] 点击键盘输入字母（L-O-O-K）
- [ ] 点击"提交"按钮
- [ ] 显示正确答案反馈
- [ ] 点击"继续"进入下一个词

### ✅ 关卡测试
- [ ] 完成 6 个词后显示关卡完成界面
- [ ] 显示星星评分（当前固定 3 星）
- [ ] 点击"返回"回到关卡选择界面
- [ ] Level 1 显示已完成状态

---

## 测试数据（Look Island - Level 1）

| 单词 | 中文翻译 | 拼写 |
|------|----------|------|
| look | 看，观看 | l-o-o-k |
| see | 看见，看到 | s-e-e |
| watch | 观看，注视 | w-a-t-c-h |
| eye | 眼睛 | e-y-e |
| glass | 玻璃，眼镜 | g-l-a-s-s |
| find | 发现，找到 | f-i-n-d |

---

## 预期功能

### ✅ 已实现
- 拼写战斗游戏（虚拟键盘 + 答案验证）
- 30 个 KET 核心词汇
- 5 个精心设计的关卡
- 关卡进度保存
- 关卡解锁机制
- 错误位置高亮显示
- 提示系统（显示首字母）

### ⏳ 已知限制
- **固定 3 星评分** - 待实现真实算法
- **无音效** - 静默游戏
- **无动画** - 简洁 UI

---

## 如果遇到问题

### 问题：adb: no devices/emulators found
**解决**:
1. 检查 USB 线连接
2. 确认手机上已启用"USB 调试"
3. 尝试 `adb kill-server && adb start-server`

### 问题：INSTALL_FAILED_INSUFFICIENT_STORAGE
**解决**: 在模拟器/设备上卸载旧版本或清理空间

### 问题：应用崩溃
**解决**:
```bash
# 查看崩溃日志
adb logcat -b crash | grep -A 20 "FATAL"

# 保存完整日志
adb logcat > crash_log.txt
```

### 问题：模拟器启动太慢
**解决**:
- 使用 Android Studio 启动（更可靠）
- 或使用真实设备（更快）

---

## 下一步

测试完成后，请记录结果：

1. **功能是否正常？** (是/否)
2. **发现了什么问题？** (列出)
3. **总体评分？** (1-10 分)

然后我们可以继续：
- **Phase 2**: 修复发现的 bug
- **Phase 3**: 实现星星评分算法
- **Phase 4**: 优化提示系统

---

**文档版本**: v1.0
**更新时间**: 2026-02-15 22:10
**APK 版本**: debug (build 20260215)
