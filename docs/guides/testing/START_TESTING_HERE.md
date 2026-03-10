# Epic #8 测试快速开始指南

**Last Updated**: 2026-02-26
**Purpose**: Epic #8 Star Breakdown 功能测试快速入口

---

## 🚀 快速开始

### 前置条件检查

```bash
# 1. 检查设备连接
adb devices -l

# 2. 检查 APK 是否构建
ls -lh app/build/outputs/apk/debug/app-debug.apk

# 3. 创建截图目录
mkdir -p docs/screenshots/epic8
```

### 测试流程

```
┌─────────────────────────────────────────────────────────┐
│                    Epic #8 测试流程                       │
├─────────────────────────────────────────────────────────┤
│  1. 运行单元测试 (验证 Bug 修复)                          │
│     ./gradlew test --tests "*StarRatingCalculatorTest"  │
│                                                         │
│  2. 启动 logcat 监控                                     │
│     ./epic8_test_logcat_monitor.sh                      │
│                                                         │
│  3. 执行 8 个测试场景                                    │
│     参考: EPIC8_RETEST_CHECKLIST.md                     │
│                                                         │
│  4. 填写测试报告                                        │
│     EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md     │
└─────────────────────────────────────────────────────────┘
```

---

## 📋 关键文档

| 文档 | 用途 |
|------|------|
| `EPIC8_RETEST_CHECKLIST.md` | 8 场景重测清单 |
| `EPIC8_MANUAL_TESTING_GUIDE.md` | 详细测试步骤 |
| `EPIC8_BUG_FIX_VALIDATION.md` | Bug 修复验证 |
| `epic8_test_cheat_sheet.sh` | 快捷命令参考 |

---

## ⚠️ 已知 Bug

**P0-BUG-010**: Star Threshold Mismatch
- **状态**: OPEN (待修复)
- **影响**: 阻塞所有真机测试
- **修复后**: 必须先运行单元测试验证

---

## 🔧 常用命令

```bash
# 清除应用数据并启动
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity

# 截图
adb shell screencap -p > docs/screenshots/epic8/scenario[X].png

# 查看日志
adb logcat -v time | grep -E "StarRatingCalculator"
```

---

## 📞 需要帮助？

- 查看 `EPIC8_MANUAL_TESTING_GUIDE.md` 获取详细步骤
- 查看故障排除部分
- 联系 android-test-engineer

---

**开始测试前，请确保已阅读并理解测试场景要求！**
