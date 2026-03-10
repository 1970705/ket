# Epic #8 重测清单 (Retest Checklist)

**Purpose**: P0-BUG-010 修复后的重测快速参考
**Created**: 2026-02-26
**Device**: Xiaomi 24031PN0DC (或 Android Emulator)

---

## 📦 重测前准备

### 单元测试验证 (必须全部通过)

```bash
./gradlew test --tests "*StarRatingCalculatorTest"
```

**验证点**:
- [ ] 全部 57 个测试通过
- [ ] 0 failures, 0 errors
- [ ] 阈值修复生效 (2.5/1.5/0.5)

### 设备准备

- [ ] 设备已连接 (`adb devices -l`)
- [ ] APK 已安装 (`adb install -r app-debug.apk`)
- [ ] Logcat 监控已启动 (`./epic8_test_logcat_monitor.sh`)
- [ ] 截图目录已创建 (`docs/screenshots/epic8/`)

---

## 🎯 8 场景快速参考表

| 场景 | 名称 | 正确数 | 提示 | 时间 | 错误 | Combo | 猜测 | 预期星级 |
|------|------|--------|------|------|------|-------|------|----------|
| 1 | Perfect | 6/6 | 0 | ~4s/词 | 0 | 6 | 否 | ★★★ |
| 2 | All Hints | 6/6 | 6+ | ~5s/词 | 0 | 0 | 否 | ★★ |
| 3 | Mixed | 4/6 | 0 | ~5s/词 | 2 | 0 | 否 | ★★ |
| 4 | Guessing | 6/6 | 0 | <1.5s/词 | 0 | 0 | 是 | ★ |
| 5 | High Combo | 5/6 | 0 | ~5s/词 | 1 | 5 | 否 | ★★★ |
| 6 | Slow | 6/6 | 0 | >15s/词 | 0 | 0 | 否 | ★★★ |
| 7 | One Wrong | 5/6 | 0 | ~5s/词 | 1 | 0 | 否 | ★★ |
| 8 | Multiple Wrong | 3/6 | 0 | ~5s/词 | 3 | 0 | 否 | ★ |

---

## 📝 场景执行检查清单

### 场景 1: Perfect Performance (★★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航: Home → 岛屿地图 → Look Island → Level 1
- [ ] Word 1 "look": 正确拼写，~4秒，无提示
- [ ] Word 2 "see": 正确拼写，~4秒，无提示
- [ ] Word 3 "watch": 正确拼写，~4秒，无提示
- [ ] Word 4 "eye": 正确拼写，~4秒，无提示
- [ ] Word 5 "glass": 正确拼写，~4秒，无提示
- [ ] Word 6 "find": 正确拼写，~4秒，无提示
- [ ] 截图: `scenario1_perfect_3stars.png`
- [ ] 日志: `epic8_scenario1.log`

**验证**:
- [ ] 实际星级 = ★★★
- [ ] Logcat 显示 score >= 2.5

---

### 场景 2: All With Hints (★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] 每个单词: 点击提示按钮 → 等待 → 正确拼写 → 提交
- [ ] 截图: `scenario2_hints_2stars.png`
- [ ] 日志: `epic8_scenario2.log`

**验证**:
- [ ] 实际星级 = ★★ (由于每词提示惩罚)
- [ ] Per-word 显示 2 星/词

---

### 场景 3: Mixed Accuracy (★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] 正确: look, see, watch, eye (4个)
- [ ] 错误: glass (拼为 gla), find (拼为 fin)
- [ ] 截图: `scenario3_mixed_2stars.png`
- [ ] 日志: `epic8_scenario3.log`

**验证**:
- [ ] 实际星级 = ★★
- [ ] 准确率显示 67%

---

### 场景 4: Guessing Detected (★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] **快速拼写所有单词** (<1.5秒/词)
- [ ] 不要思考，立即提交
- [ ] 截图: `scenario4_guessing_1star.png`
- [ ] 日志: `epic8_scenario4.log`

**验证**:
- [ ] 实际星级 = ★
- [ ] Logcat 显示 "guessing=true"
- [ ] 总时间 ~6-9 秒

---

### 场景 5: High Combo (★★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] 正确: look, see, watch, eye, glass (5个)
- [ ] 错误: find (故意拼错)
- [ ] 注意: 前5个正确建立5-combo
- [ ] 截图: `scenario5_combo_3stars.png`
- [ ] 日志: `epic8_scenario5.log`

**验证**:
- [ ] 实际星级 = ★★★ (combo bonus)
- [ ] Max combo = 5

---

### 场景 6: Slow Performance (★★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] **每个单词前等待 20 秒**
- [ ] 全部正确拼写
- [ ] 截图: `scenario6_slow_3stars.png`
- [ ] 日志: `epic8_scenario6.log`

**验证**:
- [ ] 实际星级 = ★★★ (slow penalty 最小)
- [ ] 总时间 > 120 秒

---

### 场景 7: One Wrong (★★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] 正确: look, see, watch, eye, glass (5个)
- [ ] 错误: find (拼为 finx)
- [ ] 截图: `scenario7_onewrong_2stars.png`
- [ ] 日志: `epic8_scenario7.log`

**验证**:
- [ ] 实际星级 = ★★
- [ ] Score 在 2.0-2.5 之间

---

### 场景 8: Multiple Wrong (★)

**准备**:
```bash
adb shell pm clear com.wordland && adb shell am start -n com.wordland/.ui.MainActivity
```

**执行**:
- [ ] 导航到 Level 1
- [ ] 正确: look, watch, glass (3个)
- [ ] 错误: see, eye, find (3个)
- [ ] 截图: `scenario8_multiplewrong_1star.png`
- [ ] 日志: `epic8_scenario8.log`

**验证**:
- [ ] 实际星级 = ★
- [ ] 准确率显示 50%

---

## ✅ 重测完成检查

### 所有场景执行完毕后:

- [ ] 8/8 场景已执行
- [ ] 8/8 截图已保存
- [ ] 8/8 日志已保存
- [ ] 星级符合预期 (±0容差)
- [ ] 无崩溃，无 ERROR logcat

### 验证阈值修复:

- [ ] 场景1: score >= 2.5 → 3★ ✅
- [ ] 场景3: score ~1.8 → 2★ ✅
- [ ] 场景7: score ~2.4 → 2★ ✅
- [ ] 场景8: score ~1.2 → 1★ ✅

---

## 📊 结果记录模板

```
| 场景 | 预期星级 | 实际星级 | 实际分数 | 状态 | 备注 |
|------|----------|----------|----------|------|------|
| 1    | ★★★      |          |          |      |      |
| 2    | ★★       |          |          |      |      |
| 3    | ★★       |          |          |      |      |
| 4    | ★        |          |          |      |      |
| 5    | ★★★      |          |          |      |      |
| 6    | ★★★      |          |          |      |      |
| 7    | ★★       |          |          |      |      |
| 8    | ★        |          |          |      |      |

通过率: ___/8 (___%)
测试日期: ___________
测试人员: ___________
```

---

## 🐛 遇到问题？

1. **应用崩溃**: 保存 logcat，记录 Bug
2. **星级不符**: 记录预期 vs 实际，检查 logcat
3. **设备断开**: `adb kill-server && adb start-server`
4. **截图失败**: 检查目录权限

---

**重测完成后，更新测试报告并通知 team-lead！**
