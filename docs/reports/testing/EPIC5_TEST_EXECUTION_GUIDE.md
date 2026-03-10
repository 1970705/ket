# Epic #5 Test Execution Guide

**For**: android-test-engineer executing real device validation
**Date**: 2026-02-25
**Device**: Emulator (API 16) - **Real device recommended**

---

## Quick Reference: Logcat Monitor Running

The logcat monitor is running in background (PID: captured at start).

To view logs in real-time:
```bash
adb logcat -v time | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager"
```

---

## Test Scenarios - Step by Step

### Navigation to Level 1

1. From Home Screen, tap "岛屿地图" (Island Map)
2. Tap "Look Island" (看岛)
3. Tap "Level 1" (关卡 1)
4. Game starts with first word

---

### Scenario 1: Perfect Performance (Expected: ★★★)

**Parameters**: 6/6 correct, 0 hints, ~4s/word, 0 errors, combo=6

**Execution**:
1. For each of 6 words: look, see, watch, eye, glass, find
2. Type the correct answer deliberately (about 4 seconds per word)
3. Do NOT use hint button
4. Submit when complete

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=6/6, hints=0, errors=0, combo=6, time=24000ms, score=3.30 → 3 stars
```

**Screenshot command after completion**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario1_perfect.png
```

---

### Scenario 2: All With Hints (Expected: ★★)

**Parameters**: 6/6 correct, 6+ hints, ~5s/word, 0 errors

**Execution**:
1. Clear app data: `adb shell pm clear com.wordland`
2. Launch app and navigate to Level 1
3. For each word:
   - Tap the 💡 提示 Hint button
   - Wait for hint to display
   - Type correct answer
4. Submit each word

**Watch for in logcat**:
- Per-word: `starsEarned=2` (hint penalty applied)
- Level: May show 2-3 stars depending on aggregation

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario2_hints.png
```

---

### Scenario 3: Mixed Accuracy (Expected: ★★)

**Parameters**: 4/6 correct, 0 hints, ~5s/word, 2 errors

**Execution**:
1. Clear app data and navigate to Level 1
2. Answer correctly: look, see, watch, eye (4 correct)
3. Answer incorrectly: glass, find (submit wrong spelling)
4. Complete all 6 words

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=4/6, hints=0, errors=2, combo=0, score=1.80 → 2 stars
```

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario3_mixed.png
```

---

### Scenario 4: Guessing Detected (Expected: ★)

**Parameters**: 6/6 correct, 0 hints, ~1s/word (too fast), 0 errors

**Execution**:
1. Clear app data and navigate to Level 1
2. For each word, type answer as FAST as possible (<1.5 seconds)
3. Submit immediately
4. This simulates random clicking/guessing

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=6/6, hints=0, errors=0, combo=0, time=6000ms, score=2.40 → 2 stars
D/GuessingDetector: Guessing detected - avg time per word: 1000ms
```

**Note**: Per-word guessing detection may give 1 star per word

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario4_guessing.png
```

---

### Scenario 5: High Combo (Expected: ★★★)

**Parameters**: 5/6 correct, 0 hints, ~5s/word, 1 error, combo=5

**Execution**:
1. Clear app data and navigate to Level 1
2. Answer 5 consecutive words correctly (builds combo)
3. Make 1 error (submit wrong spelling on word 6)
4. Maintain proper timing (5 seconds per word)

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=5/6, hints=0, errors=1, combo=5, score=2.90 → 3 stars
D/ComboManager: Combo bonus applied: +0.5
```

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario5_combo.png
```

---

### Scenario 6: Slow Performance (Expected: ★★★)

**Parameters**: 6/6 correct, 0 hints, ~20s/word (very slow), 0 errors

**Execution**:
1. Clear app data and navigate to Level 1
2. For each word, wait 15-20 seconds before typing
3. Answer all correctly
4. This tests the slow penalty (minimal impact)

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=6/6, hints=0, errors=0, combo=0, time=120000ms, score=2.80 → 3 stars
```

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario6_slow.png
```

---

### Scenario 7: One Wrong (Expected: ★★)

**Parameters**: 5/6 correct, 0 hints, ~5s/word, 1 error

**Execution**:
1. Clear app data and navigate to Level 1
2. Answer 5 words correctly
3. Submit wrong answer for 1 word
4. Complete level

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=5/6, hints=0, errors=1, combo=0, score=2.40 → 2 stars
```

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario7_onewrong.png
```

---

### Scenario 8: Multiple Wrong (Expected: ★)

**Parameters**: 3/6 correct, 0 hints, ~5s/word, 3 errors

**Execution**:
1. Clear app data and navigate to Level 1
2. Answer only 3 words correctly
3. Submit wrong answers for 3 words
4. Complete level

**Watch for in logcat**:
```
D/StarRatingCalculator: calculateStars: correct=3/6, hints=0, errors=3, combo=0, score=1.20 → 1 star
```

**Screenshot command**:
```bash
adb shell screencap -p > docs/reports/testing/screenshots/scenario8_multiplewrong.png
```

---

## Log Collection Commands

### Save Full Logcat
```bash
adb logcat -d > docs/reports/testing/epic5_full_log.txt
```

### Save Star Rating Logs Only
```bash
adb logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > docs/reports/testing/epic5_star_rating_logs.txt
```

---

## Quick Data Clear Between Scenarios

```bash
adb shell pm clear com.wordland
sleep 2
adb shell am start -n com.wordland/.ui.MainActivity
```

---

## Testing Checklist

Use this checklist while testing:

- [ ] Scenario 1: Perfect (★★★ expected) - Screenshot captured
- [ ] Scenario 2: All Hints (★★ expected) - Screenshot captured
- [ ] Scenario 3: Mixed (★★ expected) - Screenshot captured
- [ ] Scenario 4: Guessing (★ expected) - Screenshot captured
- [ ] Scenario 5: High Combo (★★★ expected) - Screenshot captured
- [ ] Scenario 6: Slow (★★★ expected) - Screenshot captured
- [ ] Scenario 7: One Wrong (★★ expected) - Screenshot captured
- [ ] Scenario 8: Multiple Wrong (★ expected) - Screenshot captured
- [ ] Logcat saved to file
- [ ] Screenshots collected in directory

---

## Expected Results Summary

| Scenario | Expected Score | Expected Stars |
|----------|---------------|----------------|
| 1. Perfect | 3.30 | ★★★ |
| 2. All Hints | 2.50-3.00 | ★★-★★★ |
| 3. Mixed | 1.80 | ★★ |
| 4. Guessing | 2.40 | ★★ (or ★ if per-word penalty) |
| 5. High Combo | 2.90 | ★★★ |
| 6. Slow | 2.80 | ★★★ |
| 7. One Wrong | 2.40 | ★★ |
| 8. Multiple Wrong | 1.20 | ★ |

---

**End of Execution Guide**
