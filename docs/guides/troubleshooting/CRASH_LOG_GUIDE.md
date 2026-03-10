# 真机崩溃日志查看指南

**问题**: 真机安装 APK 后闪退
**目标**: 查看崩溃日志找出原因

---

## 方法 1: 查看 Android 崩溃日志（推荐）

### 步骤 1: 连接真机

```bash
# 1. 在手机上启用 USB 调试
# 设置 → 关于手机 → 连续点击"版本号" 7次
# 设置 → 系统 → 开发者选项 → USB 调试 ✓

# 2. 使用 USB 线连接手机到电脑

# 3. 验证连接
adb devices
# 应该显示你的设备 ID
```

### 步骤 2: 清空旧日志

```bash
adb logcat -c
```

### 步骤 3: 启动应用并复现崩溃

```bash
# 启动应用
adb shell am start -n com.wordland/.ui.MainActivity
```

### 步骤 4: 查看崩溃日志

```bash
# 方法 A: 查看所有崩溃日志
adb logcat -b crash

# 方法 B: 过滤查看 FATAL EXCEPTION
adb logcat -s AndroidRuntime:E | grep -A 30 "FATAL"

# 方法 C: 实时监控所有日志
adb logcat | grep -E "Wordland|FATAL|AndroidRuntime"
```

### 步骤 5: 保存完整日志到文件

```bash
# 保存完整日志
adb logcat > crash_log_$(date +%Y%m%d_%H%M%S).txt

# 只保存崩溃相关日志
adb logcat -b crash > crash_only.txt
```

---

## 方法 2: 查看特定时间段的日志

```bash
# 查看最近 100 行日志
adb logcat -d -t 100

# 查看最近 5 分钟的日志
adb logcat -d -t 300000

# 只显示 ERROR 级别日志
adb logcat -d *:E

# 显示 Wordland 相关的所有日志
adb logcat -d | grep -i "wordland"
```

---

## 方法 3: 使用 adb bugreport（最详细）

```bash
# 生成完整报告（包含所有日志）
adb bugreport bugreport_$(date +%Y%m%d_%H%M%S).zip

# 这会创建一个包含所有系统日志的 zip 文件
# 解压后查看:
# - bugreport.txt (完整日志)
# - FS/data/data/com.wordland/ (应用数据)
```

---

## 方法 4: 使用 Android Studio Logcat

如果您安装了 Android Studio：

1. 打开 Android Studio
2. 连接真机
3. 底部工具栏点击 "Logcat" 标签
4. 在过滤器中输入:
   - `com.wordland` (查看应用日志)
   - `FATAL` (查看崩溃)
   - `AndroidRuntime` (查看运行时错误)

---

## 常见崩溃原因和日志特征

### 1. Hilt 相关崩溃

```
FATAL EXCEPTION: main
java.lang.IllegalStateException: Hilt Activity must be attached to @HiltAndroidApp
```

**原因**: Hilt 配置问题

**解决**: 检查 Hilt 版本和注解

---

### 2. 数据库相关崩溃

```
android.database.sqlite.SQLiteException: no such table: words
```

**原因**: 数据库未初始化

**解决**: 检查数据库创建和数据种子

---

### 3. 空指针异常

```
java.lang.NullPointerException
    at com.wordland.ui.screens.LearningScreen.onCreate
```

**原因**: 对象未初始化

**解决**: 检查对象初始化顺序

---

### 4. 类未找到异常

```
java.lang.ClassNotFoundException: Didn't find class "com.wordland.MainActivity"
```

**原因**: 构建配置问题

**解决**: Clean build: `./gradlew clean assembleDebug`

---

### 5. 权限相关崩溃

```
java.lang.SecurityException: Permission Denial
```

**原因**: 缺少必要权限

**解决**: 检查 AndroidManifest.xml 中的权限声明

---

## 快速诊断命令

### 一键诊断脚本

```bash
#!/bin/bash
# save as diagnose.sh

echo "=== Wordland 崩溃诊断 ==="
echo ""

echo "1. 检查设备连接..."
adb devices
echo ""

echo "2. 启动应用..."
adb shell am start -n com.wordland/.ui.MainActivity
sleep 2
echo ""

echo "3. 查看崩溃日志..."
echo "--- FATAL EXCEPTION ---"
adb logcat -d -s AndroidRuntime:E | grep -A 30 "FATAL" | tail -50
echo ""

echo "4. 查看 Wordland 日志..."
echo "--- Recent Wordland Logs ---"
adb logcat -d | grep -i "wordland" | tail -30
echo ""

echo "5. 保存完整日志..."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
adb logcat -d > full_log_$TIMESTAMP.txt
echo "完整日志已保存: full_log_$TIMESTAMP.txt"
echo ""

echo "=== 诊断完成 ==="
```

**使用方法**:
```bash
chmod +x diagnose.sh
./diagnose.sh
```

---

## 实时监控命令

### 监控应用启动过程

```bash
# 清空日志
adb logcat -c

# 启动应用
adb shell am start -n com.wordland/.ui.MainActivity

# 实时查看日志
adb logcat | grep -E "Wordland|AndroidRuntime|FATAL"
```

### 监控特定组件

```bash
# 只监控 Activity 生命周期
adb logcat | grep "ActivityManager"

# 只监控 Hilt
adb logcat | grep "Hilt"

# 只监控数据库
adb logcat | grep "Room"
```

---

## 发送日志给开发者

如果您需要将日志发送给开发者：

```bash
# 生成完整的诊断报告
adb bugreport wordland_crash_report.zip
```

然后发送 `wordland_crash_report.zip` 文件。

---

## 下一步操作

1. **运行诊断命令**: 上面提供的任何命令
2. **找到崩溃堆栈**: 查找 "FATAL EXCEPTION" 部分
3. **复制错误信息**: 复制完整的堆栈跟踪
4. **分享日志**: 将日志内容或文件分享

---

**快速开始**:
```bash
# 最简单的一行命令
adb logcat -d | grep -A 30 "FATAL"
```

将输出结果复制过来，我会帮您分析！
