# UI Integration Test Checklist
**Role**: compose-ui-designer
**Date**: 2026-02-16
**Task**: #36 真机集成测试

---

## Test Environment
- Device: [To be filled by android-test-engineer]
- APK: app-debug.apk
- Build Time: [To be recorded]

---

## 1. Hint System UI Integration

### 1.1 Hint Button States

| State | Expected Appearance | Pass/Fail | Notes |
|-------|---------------------|-----------|-------|
| Initial (3 hints) | "提示 (3)" with primary border | | |
| After 1st hint | "再提示 (2)" with tertiary border | | |
| After 2nd hint | "再提示 (1)" with tertiary border | | |
| After 3rd hint | "提示已用完" disabled | | |
| Cooldown active | [Verify cooldown message] | | |

### 1.2 Hint Card Display

| Hint Level | Expected Color | Border | Text Style | Pass/Fail | Notes |
|------------|----------------|--------|------------|-----------|-------|
| Level 1 | Primary Container | Primary | Normal | | |
| Level 2 | Secondary Container | Secondary | Normal | | |
| Level 3 | Tertiary Container | Tertiary | **Bold** | | |

### 1.3 Hint Level Indicator

| Test Case | Expected | Pass/Fail | Notes |
|-----------|----------|-----------|-------|
| 0 hints used | 3 empty circles | | |
| 1 hint used | 1 filled, 2 empty | | |
| 2 hints used | 2 filled, 1 empty | | |
| 3 hints used | 3 filled | | |

### 1.4 Remaining Hints Display

| Hints Remaining | Expected Display | Pass/Fail | Notes |
|-----------------|------------------|-----------|-------|
| 2 | 2 dots + "剩余提示: 2" | | |
| 1 | 1 dot + "剩余提示: 1" | | |
| 0 | "已用完所有提示" (error color) | | |

---

## 2. Hint Content Verification

### 2.1 Hint Progression

| Word | Level 1 (First Letter) | Level 2 (Half) | Level 3 (Vowels Masked) | Pass/Fail | Notes |
|------|------------------------|----------------|-------------------------|-----------|-------|
| look | "首字母: L" | "前半部分: lo__" | "完整单词（元音隐藏）: l__k" | | |
| apple | "首字母: A" | "前半部分: app__" | "完整单词（元音隐藏）: _ppl_" | | |
| cat | [Skip to level 2] | "前半部分: ca_" | "完整单词（元音隐藏）: c_t" | | |

---

## 3. Animation & Transition Quality

### 3.1 Smoothness (Target: 60fps)

| Animation | Expected FPS | Actual FPS | Pass/Fail | Notes |
|-----------|--------------|------------|-----------|-------|
| Hint card appear | 60 | | | |
| Hint card update | 60 | | | |
| Button press | 60 | | | |
| Level indicator fill | 60 | | | |

### 3.2 Visual Feedback

| Interaction | Feedback Type | Present? | Pass/Fail | Notes |
|-------------|---------------|----------|-----------|-------|
| Hint button press | Color change | | | |
| Hint button press | Haptic | | | |
| Hint limit reached | Disabled state | | | |
| Hint cooldown | Message/Timer | | | |

---

## 4. Material Design 3 Compliance

### 4.1 Color System

| Element | Uses Theme Color? | Pass/Fail | Notes |
|---------|-------------------|-----------|-------|
| Hint button border | primary/tertiary | | |
| Hint card background | container colors | | |
| Level indicator dots | primary | | |
| Text colors | onSecondaryContainer | | |

### 4.2 Shape & Spacing

| Element | Corner Radius | Pass/Fail | Notes |
|---------|---------------|-----------|-------|
| Hint button | 12dp | | |
| Hint card | 16dp | | |
| Level dots | 6dp (4dp radius) | | |

---

## 5. Accessibility

| Check | Expected | Pass/Fail | Notes |
|-------|----------|-----------|-------|
| Touch target size | ≥48dp | | |
| Color contrast | ≥4.5:1 (WCAG AA) | | |
| Screen reader | contentDescription | | |

---

## 6. Child-Friendly UX

| Aspect | Assessment | Pass/Fail | Notes |
|--------|------------|-----------|-------|
| Emoji usage | 💡 for hints | | |
| Text size | ≥14sp | | |
| Button size | Easy to tap | | |
| Feedback clarity | Clear messages | | |

---

## 7. Edge Cases

| Scenario | Expected Behavior | Pass/Fail | Notes |
|----------|-------------------|-----------|-------|
| Rapid hint tap | Cooldown message | | |
| Rotate device | Layout preserved | | |
| Background/foreground | Hint state preserved | | |
| Word change | Hints reset | | |

---

## 8. Integration with Other Features

| Feature | Integration Test | Pass/Fail | Notes |
|---------|------------------|-----------|-------|
| Submit after hint | Penalty applied? | | |
| Star rating | Reduced by hint? | | |
| Progress saving | Hint usage saved? | | |

---

## Summary

| Category | Total Tests | Passed | Failed | Blocked |
|----------|-------------|--------|--------|---------|
| Hint UI States | | | | |
| Hint Content | | | | |
| Animations | | | | |
| MD3 Compliance | | | | |
| Accessibility | | | | |
| Child UX | | | | |
| Edge Cases | | | | |
| Integration | | | | |
| **TOTAL** | | | | |

---

## Issues Found

| ID | Severity | Description | Steps to Reproduce |
|----|----------|-------------|-------------------|
| | | | |

---

## Recommendations

1. [To be filled after testing]

---

**Tested By**: compose-ui-designer
**Date**: [To be filled]
**Signature**: ________________
