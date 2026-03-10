# Wordland 项目脚本

本目录包含所有项目的构建、测试和工具脚本。

---

## 📂 目录结构

```
scripts/
├── build/               # 构建脚本
│   ├── build.sh
│   └── install-apk.sh
│
├── test/                # 测试脚本
│   ├── run-unit-tests.sh
│   ├── run-integration-tests.sh
│   ├── test-navigation.sh
│   ├── test-gameplay.sh
│   ├── test-real-device.sh
│   └── ...
│
└── utils/               # 工具脚本
    ├── diagnose-crash.sh
    └── check-device.sh
```

---

## 🔧 构建脚本

### build.sh
构建 APK 文件。

```bash
# 构建 Debug APK
./scripts/build/build.sh

# 构建 Release APK
./scripts/build/build.sh release
```

### install-apk.sh
安装 APK 到连接的设备。

```bash
# 安装到设备
./scripts/build/install-apk.sh

# 安装到指定设备
./scripts/build/install-apk.sh <device_id>
```

---

## 🧪 测试脚本

### 运行所有测试
```bash
# 单元测试
./scripts/test/run-unit-tests.sh

# 集成测试
./scripts/test/run-integration-tests.sh
```

### 功能测试
```bash
# 导航测试
./scripts/test/test-navigation.sh

# 游戏流程测试
./scripts/test/test-gameplay.sh

# 真机完整测试
./scripts/test/test-real-device.sh
```

### ⚠️ 首次启动测试 (CRITICAL - 发布前必测)

**重要**: 这些测试必须在每次发布前运行，确保全新安装时 App 可以正常使用。

参考: `docs/reports/issues/FIRST_LAUNCH_BUG_ROOT_CAUSE_ANALYSIS.md`

```bash
# 首次启动测试（模拟器或真机）
./scripts/test/test_first_launch.sh

# 真机全新安装测试（必须在实际设备上测试）
./scripts/test/test_real_device_clean_install.sh
```

**测试内容**:
- ✅ 清空所有数据后安装 App
- ✅ 验证数据库已创建
- ✅ 验证数据库有数据（非空）
- ✅ 验证 Level 1 状态为 UNLOCKED
- ✅ 检查 logcat 无 ERROR/CRASH
- ✅ 验证用户可以开始游戏

**失败标准** (任一失败即阻塞发布):
- ❌ 数据库未创建或为空
- ❌ Level 1 状态为 LOCKED
- ❌ 点击"开始冒险"后界面为空
- ❌ logcat 有任何 ERROR 或 CRASH

### 关卡测试
```bash
# 测试 Level 1
./scripts/test/test_level1_complete.sh

# 测试 Level 2
./scripts/test/test_level2_complete.sh

# 测试所有剩余关卡
./scripts/test/test_all_remaining_levels.sh
```

### 进度测试
```bash
# 测试进度保存
./scripts/test/test_progress_save.sh
```

---

## 🛠️ 工具脚本

### diagnose-crash.sh
诊断应用崩溃问题。

```bash
# 查看崩溃日志
./scripts/utils/diagnose-crash.sh

# 实时监控崩溃
./scripts/utils/diagnose-crash.sh --watch
```

---

## 📝 脚本编写规范

### 基本要求
1. **Shebang**: 必须指定解释器
   ```bash
   #!/bin/bash
   ```

2. **错误处理**: 设置 `set -e`
   ```bash
   set -e  # 遇到错误立即退出
   ```

3. **文档注释**: 开头添加说明
   ```bash
   #!/bin/bash
   #
   # 脚本描述
   #
   # 用法: ./scripts/path/to/script.sh [args]
   #
   # 示例:
   #   ./scripts/path/to/script.sh arg1 arg2
   ```

4. **使用变量**: 避免硬编码路径
   ```bash
   PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
   ```

5. **可执行权限**: 提交时设置
   ```bash
   chmod +x scripts/path/to/script.sh
   ```

### 命名规范
```bash
{action}-{target}.sh

示例:
- build-apk.sh
- install-apk.sh
- run-unit-tests.sh
- diagnose-crash.sh
```

---

## 🚀 快速开始

### 首次使用
```bash
# 1. 赋予脚本执行权限
chmod +x scripts/**/*.sh

# 2. 构建项目
./scripts/build/build.sh

# 3. 运行测试
./scripts/test/run-unit-tests.sh

# 4. 安装到设备
./scripts/build/install-apk.sh
```

### 日常开发
```bash
# 构建并安装
./scripts/build/build.sh && ./scripts/build/install-apk.sh

# 测试功能
./scripts/test/test-navigation.sh
./scripts/test/test-gameplay.sh
```

---

## 🔍 故障排除

### 脚本无法执行
```bash
# 赋予执行权限
chmod +x scripts/**/*.sh

# 如果还是不行，检查文件权限
ls -l scripts/
```

### 找不到 Gradle
```bash
# 确保 ./gradlew 有执行权限
chmod +x gradlew

# 或使用完整路径
./gradlew assembleDebug
```

### 设备未连接
```bash
# 检查设备连接
adb devices

# 如果没有设备，检查 USB 调试是否开启
```

---

## 📊 脚本统计

| 类型 | 数量 | 位置 |
|------|------|------|
| 构建脚本 | 1 | `scripts/build/` |
| 测试脚本 | 13 | `scripts/test/` |
| 工具脚本 | 2 | `scripts/utils/` |

**测试脚本详情**:
- 导航测试: 1
- 游戏流程测试: 1
- 进度保存测试: 1
- 关卡完成测试: 6
- **首次启动测试**: 2 ⚠️ **CRITICAL**
- 真机测试: 1
- UI 测试: 1

---

## 📚 相关文档

- [开发规范](../docs/development/DEVELOPMENT_STANDARDS.md) - 脚本编写规范
- [设备测试指南](../docs/guides/DEVICE_TESTING_GUIDE.md) - 设备测试说明
- [崩溃诊断指南](../docs/guides/CRASH_DIAGNOSIS_GUIDE.md) - 崩溃诊断方法

---

**最后更新**: 2026-02-16
