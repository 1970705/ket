# APK 构建和安装指南

## 目的
生成测试用 APK 文件并安装到 Android 手机上进行导航测试

---

## 前提条件

### 1. 环境准备
确保已安装：
- ✅ Android Studio (最新版本)
- ✅ JDK 8 或更高版本
- ✅ Android SDK Platform Tools
- ✅ USB 驱动程序已安装

### 2. 项目配置检查
确认以下配置正确：

**app/build.gradle.kts** 中的构建配置：
```kotlin
android {
    compileSdkVersion = 34      // Android 14
    defaultConfig {
        applicationId = "com.wordland"
        minSdkVersion = 26       // Android 8.0
        targetSdkVersion = 34      // Android 14
        versionCode = 1           // 版本号
        versionName = "1.0"        // 版本名称
    }
}
```

**manifest 权限**（app/src/main/AndroidManifest.xml）：
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 方法一：Android Studio 构建（推荐）

### 步骤 1：构建 Debug APK

#### 在 Android Studio 中：

1. **打开项目**
   ```
   File → Open → 选择项目目录：/Users/panshan/git/ai/ket
   ```

2. **选择 Build Variant**
   ```
   Build → Select Build Variant
   选择：debug
   ```

3. **构建 APK**
   ```
   Build → Build Bundle(s) / APK(s)
   或直接点击：Build → Make Project
   ```

4. **等待构建完成**
   - 成功提示："BUILD SUCCESSFUL"
   - APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### 步骤 2：安装到设备

#### 通过 Android Studio：

1. **连接设备**
   - 通过 USB 连接 Android 手机
   - 确保手机已开启"开发者选项"和"USB 调试"

2. **运行应用**
   ```
   Run → Run 'app'
   或按快捷键：Shift + F10
   ```

3. **检查安装**
   - 应用图标出现在手机桌面
   - 或通知栏显示"Wordland 已安装"

---

## 方法二：命令行构建（推荐）

### 步骤 1：使用 Gradle 构建

#### 在项目根目录执行：

```bash
# 进入项目目录
cd /Users/panshan/git/ai/ket

# 清理旧的构建（可选）
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 或者构建 Release APK
./gradlew assembleRelease
```

**预期输出**：
```
BUILD SUCCESSFUL in 1s
47 actionable tasks: 46 executed
```

**APK 位置**：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### 步骤 2：通过 ADB 安装

#### 连接设备并安装：

```bash
# 检查设备连接
adb devices

# 安装 APK（如果已安装，会提示 Success/ Failure）
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 或者强制重新安装
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 验证安装成功
adb shell pm list packages | grep wordland
```

**预期结果**：
```
Success
Performing Streamed Install
Success
```

---

## 常用 ADB 命令

### 查看应用日志
```bash
# 实时查看日志
adb logcat | grep "wordland"

# 保存日志到文件
adb logcat > navigation_test.log &

# 只查看 Wordland 相关日志
adb logcat | grep -E "wordland|WordlandApplication"
```

### 清除应用数据（重新测试用）
```bash
# 完全卸载应用
adb uninstall com.wordland

# 或只清除数据
adb shell pm clear com.wordland

# 停止应用
adb shell am force-stop com.wordland
```

### 启动应用
```bash
# 启动主界面
adb shell am start -n com.wordland/.ui.MainActivity

# 启动特定界面
adb shell am start -n com.wordland/.ui.MainActivity
adb shell am start -n com.wordland/.ui.IslandMapActivity
```

### 截图和录屏
```bash
# 截图（需要 scrcpy）
adb shell screencap -p /sdcard/screen.png

# 录屏（需要记录权限）
adb shell screenrecord /sdcard/demo.mp4
```

---

## 安装后的测试步骤

### 1. 基础功能测试

#### 测试主界面
- [ ] 应用启动成功
- [ ] 主界面显示 4 个按钮
- [ ] 按钮响应正常

#### 测试岛屿地图
- [ ] 显示 7 个岛屿卡片
- [ ] 每个岛屿显示正确颜色
- [ ] 中文岛屿名称正确显示
- [ ] 熟练度百分比显示

#### 测试关卡选择（对每个岛屿）
- [ ] 点击岛屿进入关卡选择
- [ ] 显示 10 个关卡
- [ ] 第 1 关卡解锁
- [ ] 第 2-10 关卡锁定

#### 测试学习界面
- [ ] 进入学习界面
- [ ] 单词卡片正确显示
- [ ] 音频播放正常
- [ ] 提示系统可用
- [ ] 返回按钮可用

### 2. 导航流程测试

#### 测试返回导航
- [ ] 学习界面 → 返回 → 关卡选择
- [ ] 关卡选择 → 返回 → 岛屿地图
- [ ] 岛屿地图 → 返回 → 主界面

#### 测试深度链接
- [ ] 使用深度链接打开特定关卡
- [ ] 正确加载对应关卡数据
- [ ] 返回后回到对应关卡界面

### 3. 性能测试

#### 启动时间
- [ ] 冷启动时间 < 3 秒
- [ ] 热启动时间 < 2 秒
- [ ] 应用响应及时

#### 内存占用
- [ ] 空闲时内存 < 200MB
- [ ] 运行时内存 < 300MB
- [ ] 无明显内存增长

#### 导航速度
- [ ] 界面切换延迟 < 500ms
- [ ] 列表滚动流畅
- [ ] 动画帧率稳定

---

## 常见问题排查

### 应用安装失败
**可能原因**：
- USB 调试未开启
- 驱动未授权
- APK 签名不匹配
- 存储空间不足

**解决方法**：
```bash
# 检查设备连接
adb devices -l

# 检查 ADB 服务
adb kill-server && adb start-server

# 重新授权
adb usb
```

### 应用无法启动
**可能原因**：
- 应用崩溃
- 权限问题
- 依赖库缺失

**解决方法**：
```bash
# 查看崩溃日志
adb logcat -b crash | grep -E "FATAL|AndroidRuntime"

# 清除数据重试
adb shell pm clear com.wordland
```

### 岛屿未显示
**可能原因**：
- 数据库未初始化
- IslandMastery 表为空

**解决方法**：
```bash
# 完全重装应用
adb uninstall com.wordland
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 关卡无法选择
**可能原因**：
- LevelProgress 表未初始化
- 关卡数据未正确创建

**解决方法**：
```bash
# 检查关卡数据
adb shell dumpsys package com.wordland | grep -i level

# 重新初始化数据
adb shell pm clear com.wordland
```

---

## 测试报告模板

测试完成后，请按以下格式反馈：

### ✅ 测试通过情况

**基础功能**：
- [ ] 主界面加载
- [ ] 岛屿地图显示（7 个岛屿）
- [ ] 关卡选择界面
- [ ] 学习界面功能

**导航功能**：
- [ ] 所有 7 个岛屿可访问
- [ ] 关卡选择正常
- [ ] 返回导航正常
- [ ] 深度链接工作

**性能**：
- [ ] 启动时间 < 3 秒
- [ ] 内存占用正常
- [ ] 导航响应流畅

**发现的问题**：
1. 问题描述
2. 复现步骤
3. 预期行为
4. 实际行为
5. 严重程度（Critical/Major/Minor）

---

## 快速命令参考

### 完整测试流程
```bash
# 1. 构建并安装
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. 启动应用
adb shell am start -n com.wordland/.ui.MainActivity

# 3. 查看日志
adb logcat | grep "wordland"
```

### 构建不同变体
```bash
# 构建 Release 版本
./gradlew assembleRelease

# 构建并签名（如果配置了签名）
./gradlew assembleRelease

# 查看构建产物
ls -lh app/build/outputs/apk/
```

---

## 注意事项

1. **首次安装建议先卸载旧版本**
   ```bash
   adb uninstall com.wordland
   ```

2. **测试时保持 USB 连接**
   便于实时查看日志

3. **测试完成后建议重启设备**
   清除缓存确保干净的测试环境

4. **如需修改代码后重新测试**
   重复上述构建和安装步骤

---

## 下一步

完成测试并反馈后，我可以：

1. **修复发现的 bug**
2. **优化性能问题**
3. **继续实现其他功能**（TTS、资源采集等）
4. **准备正式版本发布**

准备好开始测试了吗？需要我帮你做什么？
