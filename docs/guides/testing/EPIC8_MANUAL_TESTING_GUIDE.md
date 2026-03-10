# Epic #8 Task #8.2 - Manual Testing Guide

**Purpose**: Step-by-step guide for executing 8 test scenarios on real device
**Device**: Xiaomi 24031PN0DC
**Estimated Time**: 2-3 hours
**Date**: 2026-02-25

---

## Pre-Test Preparation

### 1. Device Setup

```bash
# Check device connection
adb devices -l

# Verify device is Xiaomi 24031PN0DC
# Expected output: device product: [model] device: [device_id]
```

### 2. Start Logcat Monitoring

**IMPORTANT**: Open a separate terminal window and run:

```bash
cd /Users/panshan/git/ai/ket
./epic8_test_logcat_monitor.sh
```

Keep this terminal window visible to monitor logs in real-time.

### 3. Quick Reference Commands

```bash
# Clear app data (run before each scenario)
adb shell pm clear com.wordland

# Launch app
adb shell am start -n com.wordland/.ui.MainActivity

# Capture screenshot
adb shell screencap -p > docs/screenshots/epic8/scenario[X].png

# Save logs
adb logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > docs/reports/testing/epic8_scenario[X].log
```

---

## Test Scenario Execution

### Common Steps for ALL Scenarios

1. **Clear app data**: `adb shell pm clear com.wordland`
2. **Launch app**: `adb shell am start -n com.wordland/.ui.MainActivity`
3. **Navigate to Level 1**:
   - Tap "岛屿地图" (Island Map)
   - Tap "Look Island" (看岛)
   - Tap "Level 1" (关卡 1)
4. **Execute scenario steps** (see below)
5. **Record results** on level complete screen
6. **Take screenshot**: `adb shell screencap -p > docs/screenshots/epic8/scenario[X].png`
7. **Navigate to StarBreakdownScreen** (if available):
   - Look for "查看详情" (View Details) button
   - Tap to view breakdown
   - Record displayed values
8. **Save logs**: `adb logcat -d | grep ... > docs/reports/testing/epic8_scenario[X].log`

---

## Scenario 1: Perfect Performance (★★★ Expected)

**Goal**: Complete all 6 words correctly, quickly, without hints

### Level 1 Words (Look Island):
1. look (看)
2. see (看见)
3. watch (观看)
4. eye (眼睛)
5. glass (玻璃)
6. find (寻找)

### Test Steps:

1. **Word 1: "look"**
   - Type: l-o-o-k (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit
   - Verify: Correct feedback shown

2. **Word 2: "see"**
   - Type: s-e-e (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit

3. **Word 3: "watch"**
   - Type: w-a-t-c-h (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit

4. **Word 4: "eye"**
   - Type: e-y-e (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit

5. **Word 5: "glass"**
   - Type: g-l-a-s-s (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit

6. **Word 6: "find"**
   - Type: f-i-n-d (deliberately, ~4 seconds)
   - Do NOT use hint button
   - Submit

### Expected Results:
- **Star Rating**: ★★★ (3 stars)
- **Logcat**: `score=3.XX → 3 stars`
- **Time**: ~24 seconds total
- **StarBreakdownScreen**:
  - Accuracy: 100%
  - Hints Used: 0
  - Time Taken: ~24s
  - Errors: 0
  - Combo: 6

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario1_perfect_3stars.png
```

---

## Scenario 2: All With Hints (★★ Expected)

**Goal**: Use hint on every word, then answer correctly

### Test Steps:

For each word (look, see, watch, eye, glass, find):

1. **Tap Hint Button**: 💡 提示
2. **Wait**: Let hint display (2-3 seconds)
3. **Type Answer**: Correct spelling
4. **Submit**: Submit answer

### Important Notes:
- Use hint for ALL 6 words
- Answer all correctly
- Each word should show 2 stars in per-word feedback (due to hint penalty)

### Expected Results:
- **Star Rating**: ★★ (2 stars)
- **Per-Word Stars**: 2 stars each (hint penalty)
- **Level Stars**: May show 2-3 stars (aggregation)
- **Total Hints**: 6+ hints used
- **StarBreakdownScreen**:
  - Accuracy: 100%
  - Hints Used: 6
  - Time Taken: ~30-36s
  - Errors: 0
  - Combo: 0

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario2_hints_2stars.png
```

---

## Scenario 3: Mixed Accuracy (★★ Expected)

**Goal**: 4 correct, 2 wrong answers

### Test Steps:

1. **Word 1: "look"** - Answer correctly (l-o-o-k, Submit)
2. **Word 2: "see"** - Answer correctly (s-e-e, Submit)
3. **Word 3: "watch"** - Answer correctly (w-a-t-c-h, Submit)
4. **Word 4: "eye"** - Answer correctly (e-y-e, Submit)
5. **Word 5: "glass"** - Answer INCORRECTLY (g-l-a-s, Submit wrong spelling)
6. **Word 6: "find"** - Answer INCORRECTLY (f-i-n, Submit wrong spelling)

### Expected Results:
- **Correct**: 4/6 (67%)
- **Wrong**: 2 errors
- **Star Rating**: ★★ (2 stars)
- **StarBreakdownScreen**:
  - Accuracy: 67%
  - Hints Used: 0
  - Time Taken: ~30s
  - Errors: 2
  - Combo: 0

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario3_mixed_2stars.png
```

---

## Scenario 4: Guessing Detected (★ Expected)

**Goal**: Answer correctly but VERY FAST (<1.5s per word)

### Test Steps:

For each word (look, see, watch, eye, glass, find):

1. **Type QUICKLY**: As fast as possible (<1.5 seconds per word)
2. **Submit IMMEDIATELY**: Don't think, just type and submit
3. **Repeat**: Do this for all 6 words

### Timing Guide:
- **look**: Type in <1.5s, submit
- **see**: Type in <1.5s, submit
- **watch**: Type in <1.5s, submit
- **eye**: Type in <1.5s, submit
- **glass**: Type in <1.5s, submit
- **find**: Type in <1.5s, submit

### Expected Results:
- **Total Time**: ~6-9 seconds (extremely fast)
- **Star Rating**: ★ or ★★ (1-2 stars)
- **Guessing Detected**: Yes (logcat should show)
- **Per-Word Stars**: May show 1 star each (guessing penalty)
- **StarBreakdownScreen**:
  - Accuracy: 100%
  - Hints Used: 0
  - Time Taken: ~6-9s
  - Errors: 0
  - Combo: 0 (fast answers don't build combo)

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario4_guessing_1star.png
```

---

## Scenario 5: High Combo (★★★ Expected)

**Goal**: Build 5-combo streak, then make 1 error

### Test Steps:

1. **Word 1: "look"** - Answer correctly (~5s)
   - Combo: 1
2. **Word 2: "see"** - Answer correctly (~5s)
   - Combo: 2
3. **Word 3: "watch"** - Answer correctly (~5s)
   - Combo: 3
4. **Word 4: "eye"** - Answer correctly (~5s)
   - Combo: 4
5. **Word 5: "glass"** - Answer correctly (~5s)
   - Combo: 5 (threshold reached!)
6. **Word 6: "find"** - Answer INCORRECTLY (f-i-n, Submit wrong)
   - Combo resets to 0

### Expected Results:
- **Correct**: 5/6 (83%)
- **Max Combo**: 5
- **Star Rating**: ★★★ (3 stars) - combo bonus saves it!
- **Logcat**: Should show "Combo bonus applied: +0.5"
- **StarBreakdownScreen**:
  - Accuracy: 83%
  - Hints Used: 0
  - Time Taken: ~25-30s
  - Errors: 1
  - Combo: 5

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario5_combo_3stars.png
```

---

## Scenario 6: Slow Performance (★★★ Expected)

**Goal**: Answer all correctly, but very slowly (20+ seconds per word)

### Test Steps:

For each word (look, see, watch, eye, glass, find):

1. **Wait**: Count to 20 (20 seconds)
2. **Type Answer**: Correct spelling
3. **Submit**: Submit answer
4. **Repeat**: Wait 20s for next word

### Timing Guide:
- **look**: Wait 20s, type, submit (~25s total)
- **see**: Wait 20s, type, submit (~25s total)
- **watch**: Wait 20s, type, submit (~25s total)
- **eye**: Wait 20s, type, submit (~25s total)
- **glass**: Wait 20s, type, submit (~25s total)
- **find**: Wait 20s, type, submit (~25s total)

### Expected Results:
- **Total Time**: ~120-150 seconds (2+ minutes)
- **Star Rating**: ★★★ (3 stars) - slow penalty is minimal
- **Logcat**: May show "Slow penalty applied: -0.2"
- **StarBreakdownScreen**:
  - Accuracy: 100%
  - Hints Used: 0
  - Time Taken: ~120-150s
  - Errors: 0
  - Combo: 0

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario6_slow_3stars.png
```

---

## Scenario 7: One Wrong (★★ Expected)

**Goal**: Perfect performance except 1 wrong answer

### Test Steps:

1. **Word 1: "look"** - Answer correctly
2. **Word 2: "see"** - Answer correctly
3. **Word 3: "watch"** - Answer correctly
4. **Word 4: "eye"** - Answer correctly
5. **Word 5: "glass"** - Answer correctly
6. **Word 6: "find"** - Answer INCORRECTLY (f-i-n-x, Submit wrong)

### Expected Results:
- **Correct**: 5/6 (83%)
- **Errors**: 1
- **Star Rating**: ★★ (2 stars)
- **StarBreakdownScreen**:
  - Accuracy: 83%
  - Hints Used: 0
  - Time Taken: ~30s
  - Errors: 1
  - Combo: 0

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario7_onewrong_2stars.png
```

---

## Scenario 8: Multiple Wrong (★ Expected)

**Goal**: Low accuracy, many errors (3/6 correct)

### Test Steps:

1. **Word 1: "look"** - Answer correctly (l-o-o-k, Submit)
2. **Word 2: "see"** - Answer INCORRECTLY (s-e, Submit wrong)
3. **Word 3: "watch"** - Answer correctly (w-a-t-c-h, Submit)
4. **Word 4: "eye"** - Answer INCORRECTLY (e-y, Submit wrong)
5. **Word 5: "glass"** - Answer correctly (g-l-a-s-s, Submit)
6. **Word 6: "find"** - Answer INCORRECTLY (f-i-n, Submit wrong)

### Alternative Pattern (mix it up):
- Correct: look, eye, find (3 words)
- Wrong: see, watch, glass (3 words)

### Expected Results:
- **Correct**: 3/6 (50%)
- **Errors**: 3
- **Star Rating**: ★ (1 star)
- **StarBreakdownScreen**:
  - Accuracy: 50%
  - Hints Used: 0
  - Time Taken: ~30s
  - Errors: 3
  - Combo: 0

### Capture Evidence:
```bash
adb shell screencap -p > docs/screenshots/epic8/scenario8_multiplewrong_1star.png
```

---

## Post-Test: Data Collection

### 1. Screenshot Collection

Verify all screenshots captured:
```bash
ls -lh docs/screenshots/epic8/
```

Expected files:
- scenario1_perfect_3stars.png
- scenario2_hints_2stars.png
- scenario3_mixed_2stars.png
- scenario4_guessing_1star.png
- scenario5_combo_3stars.png
- scenario6_slow_3stars.png
- scenario7_onewrong_2stars.png
- scenario8_multiplewrong_1star.png

### 2. Logcat Collection

Save all scenario logs:
```bash
# Save all star rating logs
adb logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > docs/reports/testing/epic8_all_scenarios.log
```

### 3. Update Test Report

Open the test report and fill in results:
```bash
open docs/reports/testing/EPIC8_TASK_8_2_REAL_DEVICE_VALIDATION_REPORT.md
```

Fill in:
- Actual star ratings
- Pass/Fail status
- StarBreakdownScreen values
- Any issues observed

---

## Verification Checklist

### For Each Scenario, Verify:

- [ ] App didn't crash
- [ ] Level complete screen shown
- [ ] Star rating displayed
- [ ] Star rating matches expected (±1 star tolerance)
- [ ] Screenshot captured
- [ ] Logcat shows calculation details
- [ ] StarBreakdownScreen accessible (if applicable)
- [ ] StarBreakdownScreen values match scenario parameters

### Final Verification:

- [ ] All 8 scenarios completed
- [ ] All 8 screenshots captured
- [ ] Logcat saved for analysis
- [ ] Test report updated with results
- [ ] Pass rate calculated
- [ ] Bugs documented (if any)

---

## Troubleshooting

### Issue: App Crashes on Level Complete

**Action**:
1. Check logcat for crash details
2. Note the error message
3. Report as P0 bug
4. Continue with next scenario

### Issue: Star Rating Not Shown

**Action**:
1. Check logcat for calculation
2. Verify level completion logic
3. Report as P1 bug
4. Continue with next scenario

### Issue: StarBreakdownScreen Not Accessible

**Action**:
1. Check if "查看详情" button exists on Level Complete screen
2. If missing, check navigation setup
3. Report as P2 bug (may not block Epic #8)
4. Continue with next scenario

### Issue: Device Disconnects

**Action**:
1. Reconnect device: `adb kill-server && adb start-server`
2. Verify connection: `adb devices`
3. Restart scenario from beginning

---

## Tips for Accurate Testing

1. **Timing Matters**: Use a stopwatch for time-sensitive scenarios (4, 6)
2. **Be Precise**: Follow steps exactly as written
3. **Monitor Logcat**: Keep logcat window visible during testing
4. **Take Notes**: Jot down any unexpected behaviors
5. **Stay Focused**: Test in a quiet environment to minimize distractions

---

## Expected Test Duration

| Scenario | Estimated Time | Reason |
|----------|---------------|--------|
| Scenario 1 | 5 min | Normal gameplay |
| Scenario 2 | 6 min | Extra time for hints |
| Scenario 3 | 5 min | Normal gameplay |
| Scenario 4 | 3 min | Fast gameplay |
| Scenario 5 | 5 min | Normal gameplay |
| Scenario 6 | 15 min | Slow gameplay (20s per word) |
| Scenario 7 | 5 min | Normal gameplay |
| Scenario 8 | 5 min | Normal gameplay |
| **Total** | **49 min** | Plus documentation time |

**Actual Time**: 2-3 hours (including setup, screenshots, documentation)

---

**End of Manual Testing Guide**

**Next Steps**:
1. Execute all 8 scenarios
2. Collect evidence (screenshots + logs)
3. Update test report
4. Analyze results
5. Report bugs (if any)
6. Provide Go/No-Go recommendation
