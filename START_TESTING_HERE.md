# 🚀 Quick Start - Epic #8 Task #8.2 Real Device Testing

**Start Here!** - This is your quick reference guide to begin testing immediately.

---

## ⚡ 5-Minute Setup

### Step 1: Connect Device (1 min)

```bash
# Connect Xiaomi 24031PN0DC via USB
# Verify connection
adb devices -l
```

Expected: Should show your device (not emulator)

### Step 2: Start Logcat Monitor (1 min)

Open a **new terminal window** and run:

```bash
cd /Users/panshan/git/ai/ket
./epic8_test_logcat_monitor.sh
```

Keep this window visible!

### Step 3: Open Manual Testing Guide (1 min)

```bash
open docs/guides/testing/EPIC8_MANUAL_TESTING_GUIDE.md
```

This is your main reference during testing.

### Step 4: Open Test Report (1 min)

```bash
open docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md
```

Keep this open to record results.

### Step 5: Clear Data and Launch (1 min)

```bash
adb shell pm clear com.wordland && sleep 2 && adb shell am start -n com.wordland/.ui.MainActivity
```

---

## 📋 Testing Checklist

Print this or keep it open for quick reference:

### Pre-Test
- [ ] Real device connected (Xiaomi 24031PN0DC)
- [ ] Logcat monitor running (separate terminal)
- [ ] Manual testing guide open
- [ ] Test report open
- [ ] App launched and on Level 1

### Scenario Execution (Repeat 8 times)

**For each scenario**:
1. Clear data: `adb shell pm clear com.wordland && sleep 2 && adb shell am start -n com.wordland/.ui.MainActivity`
2. Navigate: Home → Island Map → Look Island → Level 1
3. Execute scenario steps (see manual guide)
4. Record star rating on level complete
5. Take screenshot: `adb shell screencap -p > docs/screenshots/epic8/scenario[X].png`
6. Navigate to StarBreakdownScreen (if available)
7. Record breakdown values
8. Update test report

### Quick Scenario Reference

| # | Scenario | Key Action | Expected Stars |
|---|----------|------------|----------------|
| 1 | Perfect | 6/6 correct, ~4s each, no hints | ★★★ |
| 2 | All Hints | Use hint on EVERY word | ★★ |
| 3 | Mixed | 4 correct, 2 wrong | ★★ |
| 4 | Guessing | Answer FAST (<1.5s each) | ★ |
| 5 | Combo | Build 5-combo, then 1 error | ★★★ |
| 6 | Slow | Wait 20s before each word | ★★★ |
| 7 | One Wrong | 5 correct, 1 wrong | ★★ |
| 8 | Multi Wrong | 3 correct, 3 wrong | ★ |

### Post-Test
- [ ] All 8 scenarios completed
- [ ] All screenshots captured
- [ ] Test report filled in
- [ ] Logs saved
- [ ] Go/No-Go decision made

---

## 🎯 Level 1 Words (Quick Reference)

```
1. look      (看)
2. see       (看见)
3. watch     (观看)
4. eye       (眼睛)
5. glass     (玻璃)
6. find      (寻找)
```

---

## 🔧 Useful Commands

**If something goes wrong**:

```bash
# Reconnect device
adb kill-server && adb start-server

# Check app is running
adb shell ps | grep wordland

# Force restart app
adb shell am force-stop com.wordland
adb shell am start -n com.wordland/.ui.MainActivity

# View device logs
adb logcat | grep Wordland
```

---

## 📊 Expected Results Summary

```
Scenario 1 (Perfect):      ★★★ (3.8 pts) - 6/6, fast, no hints
Scenario 2 (All Hints):    ★★  (3.0 pts) - 6/6, 6 hints
Scenario 3 (Mixed):        ★★  (1.8 pts) - 4/6, 2 errors
Scenario 4 (Guessing):     ★   (2.4 pts) - 6/6, too fast
Scenario 5 (High Combo):   ★★★ (2.9 pts) - 5/6, combo=5
Scenario 6 (Slow):         ★★★ (2.8 pts) - 6/6, very slow
Scenario 7 (One Wrong):    ★★  (2.4 pts) - 5/6, 1 error
Scenario 8 (Multi Wrong):  ★   (1.2 pts) - 3/6, 3 errors
```

**Acceptance Criteria**: ≥7/8 scenarios match expected (±1 star tolerance)

---

## 🐛 If You Find Bugs

1. **Note the scenario number**
2. **Describe what went wrong**
3. **Capture logcat**: `adb logcat -d > bug_report.txt`
4. **Take screenshot**
5. **Document in test report**

---

## ⏱️ Estimated Time

- **Setup**: 5 minutes
- **Scenario 1**: 5 minutes
- **Scenario 2**: 6 minutes
- **Scenario 3**: 5 minutes
- **Scenario 4**: 3 minutes
- **Scenario 5**: 5 minutes
- **Scenario 6**: 15 minutes
- **Scenario 7**: 5 minutes
- **Scenario 8**: 5 minutes
- **Documentation**: 30 minutes

**Total**: ~2 hours

---

## 🎉 Completion

When all done:

1. Update test report with final results
2. Calculate pass rate
3. Provide Go/No-Go recommendation
4. Update task status to "completed"

---

## 📚 Full Documentation

- **Detailed Guide**: `docs/guides/testing/EPIC8_MANUAL_TESTING_GUIDE.md`
- **Test Report**: `docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md`
- **Cheat Sheet**: Run `./epic8_test_cheat_sheet.sh`

---

**Ready to begin?** Connect your device and start with Scenario 1! 🚀

---

**Quick Links**:
- [Full Manual Testing Guide](docs/guides/testing/EPIC8_MANUAL_TESTING_GUIDE.md)
- [Test Report Template](docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md)
- [Epic #8 Plan](docs/planning/epics/Epic8/EPIC8_UI_ENHANCEMENT_PLAN.md)
