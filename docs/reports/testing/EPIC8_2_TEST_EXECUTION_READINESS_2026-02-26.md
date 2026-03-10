# Epic #8.2 真机验证 - 执行就绪报告

**日期**: 2026-02-26
**任务**: Epic #8.2 真机验证（8个场景）
**执行者**: android-test-engineer
**状态**: 🟢 READY FOR EXECUTION

---

## ✅ 验收检查清单

### P0-BUG-010 修复验证
- [x] StarRatingCalculatorTest: 57/57 PASS
- [x] 阈值确认: 3★ ≥ 2.5, 2★ ≥ 1.5, 1★ ≥ 0.5
- [x] 单元测试无失败

### 环境准备
- [x] 真机 Xiaomi 24031PN0DC 已连接 (5369b23a)
- [x] 模拟器已连接 (emulator-5554)
- [x] APK 已安装到两个设备
- [x] Logcat 监控已启动

### 文档准备
- [x] START_TESTING_HERE.md - 快速开始指南
- [x] EPIC8_RETEST_CHECKLIST.md - 8场景重测清单
- [x] EPIC8_BUG_FIX_VALIDATION.md - Bug验证模板
- [x] epic8_test_cheat_sheet.sh - 快捷命令

### 工具脚本
- [x] collect_test_results.sh - 结果收集脚本
- [x] epic8_test_logcat_monitor.sh - logcat 监控
- [x] epic8_automated_test.sh - 自动化测试脚本

---

## 🎯 测试场景概览

| # | 场景 | 正确 | 提示 | 时间 | 错误 | Combo | 预期星级 |
|---|------|------|------|------|------|-------|----------|
| 1 | Perfect | 6/6 | 0 | ~4s | 0 | 6 | ★★★ |
| 2 | All Hints | 6/6 | 6+ | ~5s | 0 | 0 | ★★ |
| 3 | Mixed | 4/6 | 0 | ~5s | 2 | 0 | ★★ |
| 4 | Guessing | 6/6 | 0 | <1.5s | 0 | 0 | ★ |
| 5 | High Combo | 5/6 | 0 | ~5s | 1 | 5 | ★★★ |
| 6 | Slow | 6/6 | 0 | >15s | 0 | 0 | ★★ |
| 7 | One Wrong | 5/6 | 0 | ~5s | 1 | 0 | ★★ |
| 8 | Multiple Wrong | 3/6 | 0 | ~5s | 3 | 0 | ★ |

**关键验证点**:
- Scenario 1: 验证 3★ 阈值 (score >= 2.5)
- Scenario 3: 验证 2★ 阈值 (score >= 1.5)
- Scenario 7: 验证边界值 (score ~2.4 → 2★)
- Scenario 8: 验证 1★ 阈值 (score >= 0.5)

---

## 📋 执行步骤

### Step 1: 在真机上执行测试

```bash
# 1. 应用已在后台运行，打开它
# 2. 导航: Home → 岛屿地图 → Look Island → Level 1
# 3. 按照 EPIC8_RETEST_CHECKLIST.md 执行每个场景
```

### Step 2: 收集每个场景的结果

```bash
# 完成每个场景后运行:
./collect_test_results.sh

# 或手动收集:
adb -s 5369b23a shell screencap -p > docs/screenshots/epic8/scenario[N].png
adb -s 5369b23a logcat -d | grep StarRatingCalculator > docs/reports/testing/scenario[N].log
```

### Step 3: 验证结果

检查日志中的星级计算:
```bash
grep "calculateStars" docs/reports/testing/epic8_scenario*.log
```

预期输出示例:
```
calculateStars: correct=6/6, hints=0, errors=0, combo=6, time=24000ms, score=3.80 → 3 stars
calculateStars: correct=4/6, hints=0, errors=2, combo=0, time=30000ms, score=1.80 → 2 stars
```

### Step 4: 填写测试报告

编辑: `docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md`

填写:
- 每个场景的实际星级
- Pass/Fail 状态
- 截图路径
- 发现的问题

---

## 🚨 关键注意事项

1. **必须先启动 logcat 监控** - 否则无法捕获调试信息
2. **每个场景前清除应用数据** - `adb shell pm clear com.wordland`
3. **记录实际星级 vs 预期星级** - 验证阈值修复是否生效
4. **截图每个完成界面** - 作为测试证据
5. **检查 logcat 无 ERROR/CRASH** - 确保应用稳定

---

## 📊 成功标准

| 标准 | 要求 |
|------|------|
| 场景执行 | 8/8 场景完成 |
| 星级准确率 | ≥95% (至少 7/8 场景符合预期) |
| 稳定性 | 无崩溃，无 ERROR |
| 文档 | 截图 24 张，日志 8 份 |
| 报告 | 测试报告已填写 |

---

## 🔄 替代方案

如果真机测试无法完成，可以：

1. **使用模拟器测试** - 自动化程度高，但无法完全替代真机
2. **分析现有数据** - 结合单元测试结果推断
3. **代码审查** - 验证阈值修复是否正确

---

## 📞 支持资源

- **测试指南**: `docs/guides/testing/EPIC8_MANUAL_TESTING_GUIDE.md`
- **快速开始**: `docs/guides/testing/START_TESTING_HERE.md`
- **重测清单**: `docs/guides/testing/EPIC8_RETEST_CHECKLIST.md`
- **Bug验证**: `docs/reports/testing/EPIC8_BUG_FIX_VALIDATION.md`

---

## ✅ 签名

**准备完成时间**: 2026-02-26 21:50
**准备人员**: android-test-engineer
**状态**: READY FOR MANUAL EXECUTION

**准备就绪，可以开始执行真机测试！**
