# 真机测试脚本

Epic #12 - Task 12.3: Real Device Testing Scripts

## 📁 脚本列表

| 脚本 | 用途 | 用法 |
|------|------|------|
| `run-device-tests.sh` | 主测试脚本 | `./run-device-tests.sh [device_id]` |
| `setup-device.sh` | 设备准备检查 | `./setup-device.sh [device_id]` |
| `capture-screenshots.sh` | 截图捕获 | `./capture-screenshots.sh [device_id] [single\|series] [output]` |
| `compare-screenshots.sh` | 截图对比 | `./compare-screenshots.sh [compare\|batch] [baseline] [current] [output]` |
| `generate-report.sh` | 报告生成 | `./generate-report.sh [report_dir] [device_id] [test\|summary]` |

---

## 🚀 快速开始

### 前置要求

1. **启用 USB 调试**
   - 设置 → 关于手机 → 连续点击"版本号" 7次
   - 开发者选项 → USB 调试

2. **授权计算机**
   - 连接设备时选择"允许 USB 调试"

3. **验证 ADB 连接**
   ```bash
   adb devices
   ```

### 运行测试

```bash
# 1. 设备准备检查
cd scripts/real-device
./setup-device.sh

# 2. 运行完整测试
./run-device-tests.sh

# 3. 生成报告 (如需要)
./generate-report.sh ./docs/reports/testing/real-device/$(date +%Y-%m-%d)
```

---

## 📱 测试场景

| 场景 | 描述 | 截图数量 |
|------|------|----------|
| `app_launch` | 应用启动和主页显示 | 1 |
| `onboarding` | 新手引导流程 | 3 |
| `learning_flow` | 学习流程导航 | 4 |
| `match_game` | 单词配对游戏 | 2 |
| `progress_save` | 进度保存验证 | 2 |

---

## 📂 输出结构

```
docs/reports/testing/real-device/
├── 2026-03-08/
│   ├── device_info_{device_id}.json
│   ├── logcat_{device_id}.log
│   ├── screenshots/
│   │   └── {device_id}/
│   │       ├── app_launch/
│   │       ├── onboarding/
│   │       ├── learning_flow/
│   │       ├── match_game/
│   │       └── progress_save/
│   ├── TEST_REPORT_{device_id}.md
│   └── SUMMARY_REPORT.md
```

---

## ⚙️ 配置

### 设备矩阵配置

编辑 `../ci/device-test-matrix.yml` 来配置测试设备。

### 自定义测试场景

修改 `run-device-tests.sh` 中的 `test_*` 函数。

---

## 🔧 故障排查

### ADB 未找到
```bash
export ANDROID_HOME=~/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"
```

### 设备未授权
```bash
adb kill-server
adb start-server
# 重新连接设备并授权
```

### 截图失败
确保设备已解锁且屏幕处于活动状态。

---

## 📋 依赖

### 必需
- `adb` (Android SDK Platform Tools)

### 可选
- `ImageMagick` - 用于截图对比和 GIF 生成
  ```bash
  brew install imagemagick
  ```

---

## 📚 相关文档

- [Epic #12 计划](../../docs/planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/EPIC12_PLAN.md)
- [技术债务文档](../../docs/planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md)
- [真机测试指南](../../docs/guides/testing/DEVICE_TESTING_GUIDE.md)
