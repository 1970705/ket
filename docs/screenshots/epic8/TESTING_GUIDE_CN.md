# Epic #8.2 场景测试指南

**设备**: Xiaomi 24031PN0DC
**测试位置**: 主页 → 岛屿地图 → Look Island → Level 1
**Level 1 单词**: look, see, watch, eye, glass, find

---

## 场景1: Perfect Performance (预期★★★)

**目标**: 6/6正确，~4秒/词，无提示

| 单词 | 中文 | 操作 |
|------|------|------|
| look | 看 | 输入l-o-o-k，提交 (~4秒) |
| see | 看见 | 输入s-e-e，提交 (~4秒) |
| watch | 观看 | 输入w-a-t-c-h，提交 (~4秒) |
| eye | 眼睛 | 输入e-y-e，提交 (~4秒) |
| glass | 玻璃 | 输入g-l-a-s-s，提交 (~4秒) |
| find | 寻找 | 输入f-i-n-d，提交 (~4秒) |

**预期结果**: ★★★ (3星)
**预期时间**: ~24秒
**预期Combo**: 6

---

## 场景2: All With Hints (预期★★)

**目标**: 每个单词都使用提示

| 单词 | 操作 |
|------|------|
| 所有单词 | 1. 点击💡提示 → 2. 输入正确答案 → 3. 提交 |

**预期结果**: ★★ (2星) - 每个单词因提示减1星

---

## 场景3: Mixed Accuracy (预期★★)

**目标**: 4正确，2错误

| 单词 | 操作 |
|------|------|
| look, see, watch, eye | 正确输入并提交 |
| glass | 输入 "glas" (错误) |
| find | 输入 "fin" (错误) |

**预期结果**: ★★ (2星)
**正确率**: 4/6 = 67%

---

## 场景4: Guessing (预期★)

**目标**: 快速答题，触发猜测检测

| 单词 | 操作 |
|------|------|
| 所有单词 | 快速输入 (<1.5秒/词) 并提交 |

**预期结果**: ★ (1星)
**预期时间**: ~6-9秒
**注意**: 太快不会累积combo

---

## 场景5: High Combo (预期★★★)

**目标**: 达到5连击然后故意错一个

| 单词 | 操作 |
|------|------|
| look, see, watch, eye, glass | 正确输入 (建立combo=5) |
| find | 输入 "fin" (错误，重置combo) |

**预期结果**: ★★★ (3星) - combo bonus补偿

---

## 场景6: Slow Performance (预期★★★)

**目标**: 每个单词等待20秒

| 单词 | 操作 |
|------|------|
| 所有单词 | 等20秒 → 输入正确答案 → 提交 |

**预期结果**: ★★★ (3星) - 慢速惩罚很小

---

## 场景7: One Wrong (预期★★)

**目标**: 只有1个错误

| 单词 | 操作 |
|------|------|
| look, see, watch, eye, glass | 正确输入并提交 |
| find | 输入 "finx" (错误) |

**预期结果**: ★★ (2星)
**正确率**: 5/6 = 83%

---

## 场景8: Multiple Wrong (预期★)

**目标**: 只有3个正确

| 单词 | 操作 |
|------|------|
| look, eye, find | 正确输入 |
| see, watch, glass | 输入错误拼写 |

**预期结果**: ★ (1星)
**正确率**: 3/6 = 50%

---

## 快速命令

```bash
# 清除数据并启动App
adb shell pm clear com.wordland && sleep 2 && adb shell am start -n com.wordland/.ui.MainActivity

# 捕获截图
adb shell screencap -p > docs/screenshots/epic8/scenario[X].png

# 查看星级日志
adb logcat -d | grep -E "StarRatingCalculator|stars|score"
```

---

## 测试后检查

- [ ] 8个场景全部完成
- [ ] 每个场景都有截图
- [ ] 记录实际星级评分
- [ ] 对比预期与实际结果
- [ ] 记录任何bug或异常
