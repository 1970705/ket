# Epic #5 Real Device Test Plan

**Document Version**: 1.0
**Test Date**: 2026-02-25
**Tester**: android-test-engineer
**Epic**: #5 Dynamic Star Rating Algorithm
**Task**: #4 - Real Device Validation
**Priority**: P1 (High)
**Estimated Time**: 2 hours

---

## Executive Summary

This test plan validates the **Dynamic Star Rating Algorithm** on a real device. The algorithm calculates star ratings (1-3) based on multiple factors: accuracy, hint usage, time, errors, and combo.

**Goal**: Verify that all 8 scenarios produce the expected star ratings as per the algorithm specification.

---

## Pre-Test Setup

### Step 1: Start Logcat Monitoring (CRITICAL)

**IMPORTANT**: Start logcat monitoring BEFORE launching the app!

```bash
# Clear old logs
adb logcat -c

# Start monitoring star rating calculations
adb logcat | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector"
```

Expected log output format:
```
D/StarRatingCalculator: calculateStars: correct=6/6, hints=0, errors=0, combo=6, time=24000ms, score=3.30 → 3 stars
```

### Step 2: Build and Install

```bash
# Build debug APK
./gradlew clean assembleDebug

# Install to device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Clear app data for clean state
adb shell pm clear com.wordland
```

### Step 3: Navigate to Test Level

```bash
# Launch app
adb shell am start -n com.wordland/.ui.MainActivity

# Navigate: Home → Island Map → Look Island → Level 1
# Use manual navigation through UI
```

---

## Test Scenarios

### Scenario 1: Perfect Performance (Expected: 3★)

**Description**: All correct, no hints, fast time, no errors, good combo

**Parameters**:
- Correct: 6/6 (100%)
- Hints: 0
- Time: ~4s/word (24s total)
- Errors: 0
- Combo: 6

**Expected Calculation**:
```
Accuracy: 6/6 = 100% → 3.0 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 4s/word → +0.3 points (fast)
Error Penalty: 0 errors → 0.0 points
Combo Bonus: 6 combo → 0.0 points (below 5 threshold)
---
Total: 3.0 + 0.3 = 3.3 → 3 stars
```

**Test Steps**:
1. Start Level 1 (Look Island)
2. For each of 6 words, type the correct answer deliberately but quickly (4-5 seconds each)
3. Do NOT use hints
4. Monitor logcat output

**Expected Result**: ★★★ (3 stars)

**Verification**:
- [ ] Logcat shows `score=3.XX → 3 stars`
- [ ] Level complete screen shows 3 stars
- [ ] Screenshot captured

---

### Scenario 2: All With Hints (Expected: 2★)

**Description**: All correct, but used hints on every word

**Parameters**:
- Correct: 6/6 (100%)
- Hints: 6+
- Time: ~5s/word (30s total)
- Errors: 0
- Combo: 0 (hints might affect combo)

**Expected Calculation**:
```
Accuracy: 6/6 = 100% → 3.0 points
Hint Penalty: 6 hints → 0.0 points (DISABLED at level level)
Time Bonus: 5s/word → 0.0 points (normal)
Error Penalty: 0 errors → 0.0 points
Combo Bonus: 0 combo → 0.0 points
---
Total: 3.0 → 3 stars
NOTE: Per-word penalty means max 2 stars per word, but level-level calculation may still give 3
```

**Expected Result**: ★★ (2 stars) - Due to per-word hint penalty

**Test Steps**:
1. Start Level 1
2. For each word, click hint button before answering
3. Type correct answer
4. Verify per-word stars show 2 stars each

**Verification**:
- [ ] Each feedback shows 2 stars (per-word penalty)
- [ ] Level complete may show 2-3 stars (depending on aggregation)
- [ ] Screenshot captured

---

### Scenario 3: Mixed Accuracy (Expected: 2★)

**Description**: Some wrong, but mostly correct

**Parameters**:
- Correct: 4/6 (67%)
- Hints: 0
- Time: ~5s/word (30s total)
- Errors: 2
- Combo: 0

**Expected Calculation**:
```
Accuracy: 4/6 = 67% → 2.0 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 5s/word → 0.0 points (normal)
Error Penalty: 2 errors → -0.2 points
Combo Bonus: 0 combo → 0.0 points
---
Total: 2.0 - 0.2 = 1.8 → 2 stars
```

**Expected Result**: ★★ (2 stars)

**Test Steps**:
1. Start Level 1
2. Answer 4 words correctly
3. Answer 2 words incorrectly (submit wrong spelling)
4. Continue until all 6 words are done

**Verification**:
- [ ] Logcat shows correct=4, total=6
- [ ] Level complete shows 2 stars
- [ ] Screenshot captured

---

### Scenario 4: Guessing Detected (Expected: 1★)

**Description**: All correct but too fast (guessing behavior)

**Parameters**:
- Correct: 6/6 (100%)
- Hints: 0
- Time: ~1s/word (6s total) - BELOW GUESSING THRESHOLD
- Errors: 0
- Combo: 0 (fast answers don't increment combo)

**Expected Calculation**:
```
Accuracy: 6/6 = 100% → 3.0 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 1s/word → -0.6 points (GUESSING PENALTY)
Error Penalty: 0 errors → 0.0 points
Combo Bonus: 0 combo → 0.0 points (fast answers don't build combo)
---
Total: 3.0 - 0.6 = 2.4 → 2 stars
NOTE: Per-word guessing detection may give 1 star per word
```

**Expected Result**: ★ or ★★ (1-2 stars)

**Test Steps**:
1. Start Level 1
2. For each word, type answer as fast as possible (<1.5s per word)
3. This simulates guessing behavior

**Verification**:
- [ ] Logcat shows guessing detection
- [ ] Level complete shows 1-2 stars (not 3)
- [ ] Screenshot captured

---

### Scenario 5: High Combo (Expected: 3★)

**Description**: Good performance with high combo streak

**Parameters**:
- Correct: 5/6 (83%)
- Hints: 0
- Time: ~5s/word (25s total)
- Errors: 1
- Combo: 5

**Expected Calculation**:
```
Accuracy: 5/6 = 83% → 2.5 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 5s/word → 0.0 points (normal)
Error Penalty: 1 error → -0.1 points
Combo Bonus: 5 combo → +0.5 points
---
Total: 2.5 - 0.1 + 0.5 = 2.9 → 3 stars
```

**Expected Result**: ★★★ (3 stars)

**Test Steps**:
1. Start Level 1
2. Answer 5 consecutive words correctly
3. Make 1 error (but recover)
4. Maintain combo of 5+

**Verification**:
- [ ] Logcat shows combo bonus
- [ ] Level complete shows 3 stars
- [ ] Screenshot captured

---

### Scenario 6: Slow Performance (Expected: 2★)

**Description**: All correct but very slow

**Parameters**:
- Correct: 6/6 (100%)
- Hints: 0
- Time: ~20s/word (120s total) - ABOVE SLOW THRESHOLD
- Errors: 0
- Combo: 0

**Expected Calculation**:
```
Accuracy: 6/6 = 100% → 3.0 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 20s/word → -0.2 points (SLOW PENALTY)
Error Penalty: 0 errors → 0.0 points
Combo Bonus: 0 combo → 0.0 points
---
Total: 3.0 - 0.2 = 2.8 → 3 stars
```

**Expected Result**: ★★★ (3 stars) - Slow penalty is small

**Test Steps**:
1. Start Level 1
2. For each word, wait 15+ seconds before answering
3. Answer all correctly

**Verification**:
- [ ] Logcat shows slow penalty
- [ ] Level complete shows 3 stars (penalty is minimal)
- [ ] Screenshot captured

---

### Scenario 7: One Wrong (Expected: 3★)

**Description**: Perfect except one wrong answer

**Parameters**:
- Correct: 5/6 (83%)
- Hints: 0
- Time: ~5s/word (30s total)
- Errors: 1
- Combo: 0

**Expected Calculation**:
```
Accuracy: 5/6 = 83% → 2.5 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 5s/word → 0.0 points (normal)
Error Penalty: 1 error → -0.1 points
Combo Bonus: 0 combo → 0.0 points
---
Total: 2.5 - 0.1 = 2.4 → 2 stars
```

**Expected Result**: ★★ (2 stars)

**Test Steps**:
1. Start Level 1
2. Answer 5 words correctly
3. Submit wrong answer for 1 word
4. Complete level

**Verification**:
- [ ] Logcat shows correct=5, total=6
- [ ] Level complete shows 2 stars
- [ ] Screenshot captured

---

### Scenario 8: Multiple Wrong (Expected: 1★)

**Description**: Many errors, low accuracy

**Parameters**:
- Correct: 3/6 (50%)
- Hints: 0
- Time: ~5s/word (30s total)
- Errors: 3
- Combo: 0

**Expected Calculation**:
```
Accuracy: 3/6 = 50% → 1.5 points
Hint Penalty: 0 hints → 0.0 points
Time Bonus: 5s/word → 0.0 points (normal)
Error Penalty: 3 errors → -0.3 points (capped)
Combo Bonus: 0 combo → 0.0 points
---
Total: 1.5 - 0.3 = 1.2 → 1 star
```

**Expected Result**: ★ (1 star)

**Test Steps**:
1. Start Level 1
2. Answer only 3 words correctly
3. Submit wrong answers for 3 words
4. Complete level

**Verification**:
- [ ] Logcat shows correct=3, total=6
- [ ] Level complete shows 1 star
- [ ] Screenshot captured

---

## Screenshot Capture Commands

```bash
# After each scenario, capture screenshot
adb shell screencap -p /sdcard/epic5_scenario_X.png
adb pull /sdcard/epic5_scenario_X.png

# Or capture directly
adb shell screencap -p > epic5_scenario_X.png
```

**Screenshot Checklist**:
- [ ] Scenario 1: Perfect performance (3★ expected)
- [ ] Scenario 2: All with hints (2★ expected)
- [ ] Scenario 3: Mixed accuracy (2★ expected)
- [ ] Scenario 4: Guessing detected (1-2★ expected)
- [ ] Scenario 5: High combo (3★ expected)
- [ ] Scenario 6: Slow performance (3★ expected)
- [ ] Scenario 7: One wrong (2★ expected)
- [ ] Scenario 8: Multiple wrong (1★ expected)

---

## Logcat Monitoring Script

```bash
#!/bin/bash
# epic5_logcat_monitor.sh

echo "Starting Epic #5 Star Rating Test Logcat Monitor..."
echo "Press Ctrl+C to stop monitoring"

adb logcat -c
adb logcat -v time | grep --line-buffered -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager"
```

**Save as**: `/Users/panshan/git/ai/ket/epic5_logcat_monitor.sh`

---

## Test Execution Checklist

### Pre-Test
- [ ] Device connected via ADB
- [ ] Logcat monitoring started
- [ ] APK built and installed
- [ ] App data cleared
- [ ] Screenshot directory created

### During Test
- [ ] Each scenario executed in order
- [ ] Logcat output captured
- [ ] Screenshots captured after each level
- [ ] Unexpected behaviors noted

### Post-Test
- [ ] All screenshots collected
- [ ] Logcat saved to file
- [ ] Test report completed
- [ ] Go/No-Go recommendation determined

---

## Go/No-Go Criteria

**Go Condition** (All must pass):
- [ ] All 8 scenarios produce star ratings within expected range
- [ ] No crashes during testing
- [ ] Logcat shows correct calculation values
- [ ] No unexpected behaviors

**No-Go Condition** (Any one fails):
- [ ] Any scenario produces wrong star rating (off by more than 1)
- [ ] App crashes during level completion
- [ ] Star rating calculation errors in logcat
- [ ] Cannot complete scenarios due to bugs

---

## Expected Issues to Watch For

Based on the STAR_RATING_BEHAVIOR_AUDIT.md:

1. **Double Hint Penalty**: Hints penalize at both per-word AND level-level
   - **Watch**: Scenario 2 - Verify if star count is as expected

2. **Inconsistent Guessing Detection**: Per-word and level-level detection differ
   - **Watch**: Scenario 4 - Compare per-word vs level-level guessing detection

3. **Combo Can Override Guessing**: High combo may overcome guessing penalty
   - **Watch**: Scenarios 4 & 5 - Test this interaction

---

## Test Report Template

```markdown
# Epic #5 Real Device Test Report

**Test Date**: 2026-02-25
**Device**: [Device Model]
**Android Version**: [Version]
**Tester**: android-test-engineer

## Scenario Results

| Scenario | Expected | Actual | Pass/Fail | Notes |
|----------|----------|--------|-----------|-------|
| 1. Perfect | 3★ | ? | ? | |
| 2. All Hints | 2★ | ? | ? | |
| 3. Mixed Accuracy | 2★ | ? | ? | |
| 4. Guessing | 1★ | ? | ? | |
| 5. High Combo | 3★ | ? | ? | |
| 6. Slow | 3★ | ? | ? | |
| 7. One Wrong | 2★ | ? | ? | |
| 8. Multiple Wrong | 1★ | ? | ? | |

## Issues Found

1. [Issue description if any]

## Recommendation

[ ] GO - Algorithm working as expected
[ ] NO-GO - Issues found, requires fixes

## Next Steps

- [ ] Fix any discovered issues
- [ ] Re-test scenarios
- [ ] Complete Epic #5
```

---

**End of Test Plan**
