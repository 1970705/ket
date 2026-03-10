# Sprint 1 测试计划

**Sprint周期**: 2周 (2026-02-21 至 2026-03-06)
**测试负责人**: android-test-engineer
**Sprint目标**: 完成P0功能测试，确保质量门禁通过

---

## 📊 测试范围概览

| Epic | 测试用例数 | 工作量 | 测试类型 |
|------|-----------|--------|----------|
| Epic #1: 视觉反馈增强 | 15个 | 1.5天 | 单元 + UI + 性能 |
| Epic #2: 地图系统重构 | 25个 | 2天 | 单元 + 集成 + UI |
| Epic #3: 动态星级评分 | 20个 | 1天 | 单元 + 集成 |
| 集成与真机测试 | 10个 | 1天 | E2E + 性能 |
| **总计** | **70个** | **5.5天** | - |

---

## Epic #1: 视觉反馈增强测试

### 1.1 拼写动画测试 (LetterFlyInAnimation)

**测试文件**: `app/src/test/java/com/wordland/ui/components/AnswerAnimationsTest.kt`

```kotlin
class AnswerAnimationsTest {
    @Test fun `letter fly-in animation has correct duration`()
    @Test fun `letters animate sequentially with 100ms delay`()
    @Test fun `animation triggers on correct answer`()
    @Test fun `animation does not trigger on wrong answer`()
    @Test fun `animation completes within 500ms for 5-letter word`()
}
```

**验收标准**:
- ✅ 所有动画参数正确
- ✅ 动画时长可配置
- ✅ 60fps性能验证

### 1.2 庆祝动画测试 (CelebrationEffect)

```kotlin
class CelebrationEffectTest {
    @Test fun `celebration triggers on 3-star completion`()
    @Test fun `confetti emits for 2 seconds`()
    @Test fun `sound effect syncs with animation`()
    @Test fun `celebration does not trigger below 3 stars`()
    @Test fun `particle count is reasonable (< 100)`()
}
```

### 1.3 连击视觉效果测试 (ComboIndicator)

```kotlin
class ComboIndicatorTest {
    @Test fun `combo 3-4 shows single fire icon`()
    @Test fun `combo 5-9 shows double fire icon`()
    @Test fun `combo 10+ shows triple fire icon`()
    @Test fun `combo pulse animation is correct`()
    @Test fun `combo resets on wrong answer`()
}
```

### 1.4 进度条动画测试 (ProgressBarTest)

```kotlin
class ProgressBarTest {
    @Test fun `progress bar animates smoothly over 300ms`()
    @Test fun `stars pop sequentially with 200ms delay each`()
    @Test fun `combo counter scales up on combo`()
}
```

---

## Epic #2: 地图系统重构测试

### 2.1 可见性计算器测试 (VisibilityCalculator)

**测试文件**: `app/src/test/java/com/wordland/domain/map/VisibilityCalculatorTest.kt`

```kotlin
class VisibilityCalculatorTest {
    @Test fun `visibility radius is 15% for level 1-3`()
    @Test fun `visibility radius is 30% for level 4-6`()
    @Test fun `visibility radius is 50% for level 7+`()
    @Test fun `visibility updates when level increases`()
    @Test fun `adjacent regions are revealed on level complete`()
}
```

### 2.2 迷雾系统测试 (FogOverlay)

```kotlin
class FogOverlayTest {
    @Test fun `fog opacity is 1.0 for locked regions`()
    @Test fun `fog opacity is 0.0 for unlocked regions`()
    @Test fun `fog reveal animation completes in 500ms`()
    @Test fun `fog rendering takes less than 16ms`()
}
```

### 2.3 区域解锁管理器测试 (RegionUnlockManager)

```kotlin
class RegionUnlockManagerTest {
    @Test fun `completing level unlocks adjacent regions`()
    @Test fun `unlock status persists to database`()
    @Test fun `unlock triggers achievement check`()
}
```

### 2.4 世界地图UI测试 (WorldMapScreen)

**测试文件**: `app/src/androidTest/java/com/wordland/ui/screens/WorldMapScreenTest.kt`

```kotlin
@HiltAndroidTest
class WorldMapScreenTest {
    @Test fun `tapping unlocked region navigates to level select`()
    @Test fun `tapping locked region shows locked dialog`()
    @Test fun `world view toggle switches map modes`()
    @Test fun `ship animates to new region on unlock`()
}
```

---

## Epic #3: 动态星级评分测试

### 3.1 评分算法测试 (StarRatingCalculator)

**测试文件**: `app/src/test/java/com/wordland/domain/algorithm/StarRatingCalculatorTest.kt`

```kotlin
class StarRatingCalculatorTest {
    // 准确率测试
    @Test fun `90%+ accuracy gives base 3 stars`()
    @Test fun `70-89% accuracy gives base 2 stars`()
    @Test fun `50-69% accuracy gives base 1 star`()
    @Test fun `<50% accuracy gives 0 stars`()

    // 提示惩罚测试
    @Test fun `using hints caps at 2 stars`()
    @Test fun `no hints allows 3 stars`()

    // 思考时间测试
    @Test fun `too fast (< threshold) applies 0.7x coefficient`()
    @Test fun `too slow (> 3x threshold) applies 0.8x coefficient`()
    @Test fun `normal time gives 1.0x coefficient`()

    // 连击奖励测试
    @Test fun `combo 10+ gives +1 star bonus`()
    @Test fun `combo 5-9 gives no bonus`()
    @Test fun `high combo can offset one error`()

    // 边界测试
    @Test fun `perfect score gives 3 stars`()
    @Test fun `all wrong with max combo gives 1 star`()
    @Test fun `minimum score gives 0 stars`()
    @Test fun `stars are clamped between 0 and 3`()

    // 性能测试
    @Test fun `calculation completes under 10ms`()
}
```

### 3.2 Spell Battle集成测试

```kotlin
class LearningViewModelTest {
    @Test fun `spell battle uses dynamic star rating`()
    @Test fun `stars are calculated on level complete`()
    @Test fun `star rating updates UI correctly`()
}
```

### 3.3 Quick Judge集成测试

```kotlin
class QuickJudgeViewModelTest {
    @Test fun `quick judge uses dynamic star rating`()
    @Test fun `stars are calculated on game complete`()
    @Test fun `star rating displays correctly`()
}
```

---

## 集成与真机测试

### 测试脚本

```bash
#!/bin/bash
# test_sprint1_features.sh

echo "=== Sprint 1 Feature Test Script ==="

# 1. Build and install
echo "Building APK..."
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Launch app
echo "Launching app..."
adb shell am start -n com.wordland/.ui.MainActivity

# 3. Test visual feedback
echo "=== Testing Visual Feedback ==="
adb shell am start -n com.wordland/.ui.MainActivity -e mode "test_visual_feedback"
sleep 5
adb exec-out screencap -p > /tmp/visual_feedback.png
echo "Screenshot saved to /tmp/visual_feedback.png"

# 4. Test map system
echo "=== Testing Map System ==="
adb shell am start -n com.wordland/.ui.MainActivity -e mode "test_map_system"
sleep 5
adb exec-out screencap -p > /tmp/map_system.png
echo "Screenshot saved to /tmp/map_system.png"

# 5. Test dynamic rating
echo "=== Testing Dynamic Rating ==="
adb shell am start -n com.wordland/.ui.MainActivity -e mode "test_dynamic_rating"
sleep 5
adb exec-out screencap -p > /tmp/dynamic_rating.png
echo "Screenshot saved to /tmp/dynamic_rating.png"

# 6. Performance test
echo "=== Performance Test ==="
adb shell am start -n com.wordland/.ui.MainActivity -e mode "performance_test"
sleep 30
adb logcat -d | grep "Performance" > /tmp/performance.log
echo "Performance log saved to /tmp/performance.log"

echo "=== Test Complete ==="
```

### 真机测试清单

| 设备 | 测试项 | 状态 |
|------|--------|------|
| Device 1 (Xiaomi) | 视觉反馈流畅度 | ⬜ |
| Device 1 (Xiaomi) | 地图系统导航 | ⬜ |
| Device 1 (Xiaomi) | 动态星级评分 | ⬜ |
| Device 1 (Xiaomi) | 性能60fps | ⬜ |
| Device 2 | 视觉反馈流畅度 | ⬜ |
| Device 2 | 地图系统导航 | ⬜ |
| Device 2 | 动态星级评分 | ⬜ |
| Device 2 | 性能60fps | ⬜ |

---

## 性能测试

### Macrobenchmark配置

```kotlin
// benchmark/src/androidTest/java/com/wordland/benchmark/Sprint1Benchmarks.kt
@RunWith(AndroidJUnit4::class)
class Sprint1Benchmarks {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun letterAnimationBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            iterations = 10,
            startupMode = StartupMode.WARM
        ) {
            pressHome()
            startActivityAndWait()

            // Navigate to learning screen
            device.clickAndWait(
                By.text("Start Learning"),
                3000
            )

            // Trigger animation
            device.click(By.id("correct_button"))

            // Wait for animation
            Thread.sleep(500)
        }
    }

    @Test
    fun mapRenderingBenchmark() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(FrameTimingMetric()),
            iterations = 10
        ) {
            startActivityAndWait()

            // Navigate to world map
            device.clickAndWait(
                By.text("World Map"),
                2000
            )

            // Measure frame timing during fog reveal
            device.click(By.id("reveal_region_button"))
            Thread.sleep(1000)
        }
    }
}
```

---

## CI/CD配置

### GitHub Actions - Sprint 1 测试流水线

```yaml
# .github/workflows/sprint1-test.yml
name: Sprint 1 Tests

on:
  push:
    branches: [feature/sprint1, main]
  pull_request:
    branches: [main, develop]

jobs:
  sprint1-unit-tests:
    name: Sprint 1 Unit Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Sprint 1 unit tests
        run: ./gradlew test --tests "*visual_feedback*" --tests "*map_system*" --tests "*dynamic_rating*"
      - name: Generate coverage
        run: ./gradlew jacocoTestReport

  sprint1-ui-tests:
    name: Sprint 1 UI Tests
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run Sprint 1 UI tests
        run: ./gradlew connectedAndroidTest --tests "*WorldMapScreenTest*"

  sprint1-performance:
    name: Sprint 1 Performance
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run benchmarks
        run: ./gradlew :benchmark:connectedCheck
```

---

## 测试时间表

### Week 1 (Day 1-7)

| 日期 | 测试任务 | 交付物 |
|------|----------|--------|
| Day 1-2 | Epic #1 单元测试 | AnswerAnimationsTest.kt |
| Day 3 | Epic #2 单元测试 | VisibilityCalculatorTest.kt |
| Day 4 | Epic #3 单元测试 | StarRatingCalculatorTest.kt |
| Day 5 | Epic #2 UI测试 | WorldMapScreenTest.kt |
| Day 6-7 | 集成测试准备 | 测试框架搭建 |

### Week 2 (Day 8-14)

| 日期 | 测试任务 | 交付物 |
|------|----------|--------|
| Day 8-9 | Epic #1 UI测试 | 动画组件测试 |
| Day 10 | Epic #2 集成测试 | RegionUnlockManagerTest.kt |
| Day 11 | Epic #3 集成测试 | ViewModel集成测试 |
| Day 12 | 真机测试 | 测试报告 |
| Day 13 | Bug修复与回归 | 所有测试通过 |
| Day 14 | Sprint Review准备 | 测试总结报告 |

---

## 测试用例详细清单

### Epic #1: 视觉反馈增强 (15个)

| ID | 用例名称 | 类型 | 优先级 |
|----|----------|------|--------|
| VFX-001 | 字母飞入动画时长正确 | 单元 | P0 |
| VFX-002 | 字母依次延迟100ms | 单元 | P0 |
| VFX-003 | 正确答案触发动画 | 单元 | P0 |
| VFX-004 | 错误答案不触发动画 | 单元 | P0 |
| VFX-005 | 5字母单词动画<500ms | 性能 | P0 |
| VFX-006 | 3星触发庆祝 | UI | P0 |
| VFX-007 | 庆祝持续2秒 | UI | P0 |
| VFX-008 | 粒子数量<100 | 性能 | P1 |
| VFX-009 | 连击3显示单火焰 | UI | P0 |
| VFX-010 | 连击5显示双火焰 | UI | P0 |
| VFX-011 | 连击10显示三火焰 | UI | P0 |
| VFX-012 | 进度条动画300ms | UI | P0 |
| VFX-013 | 星星依次弹出200ms | UI | P0 |
| VFX-014 | 连击计数器缩放 | UI | P0 |
| VFX-015 | 动画维持60fps | 性能 | P0 |

### Epic #2: 地图系统重构 (25个)

| ID | 用例名称 | 类型 | 优先级 |
|----|----------|------|--------|
| MAP-001 | Level 1可见半径15% | 单元 | P0 |
| MAP-002 | Level 4可见半径30% | 单元 | P0 |
| MAP-003 | Level 7可见半径50% | 单元 | P0 |
| MAP-004 | 完成关卡揭示相邻 | 单元 | P0 |
| MAP-005 | 锁定区域迷雾不透明度1.0 | 单元 | P0 |
| MAP-006 | 解锁区域迷雾不透明度0.0 | 单元 | P0 |
| MAP-007 | 迷雾动画500ms | 单元 | P0 |
| MAP-008 | 迷雾渲染<16ms | 性能 | P0 |
| MAP-009 | 解锁状态持久化 | 集成 | P0 |
| MAP-010 | 解锁触发成就 | 集成 | P1 |
| MAP-011 | 点击解锁区域导航 | UI | P0 |
| MAP-012 | 点击锁定区域显示弹窗 | UI | P0 |
| MAP-013 | 视图切换流畅 | UI | P0 |
| MAP-014 | 船只显示正确位置 | UI | P0 |
| MAP-015 | 船只移动动画流畅 | UI | P0 |
| MAP-016 | 世界视图正确显示 | UI | P0 |
| MAP-017 | 岛屿视图正确显示 | UI | P0 |
| MAP-018 | 区域解锁动画显示 | UI | P0 |
| MAP-019 | 导航无崩溃 | E2E | P0 |
| MAP-020 | 性能60fps验证 | 性能 | P0 |

### Epic #3: 动态星级评分 (20个)

| ID | 用例名称 | 类型 | 优先级 |
|----|----------|------|--------|
| STR-001 | 准确率90%+基础3星 | 单元 | P0 |
| STR-002 | 准确率70-89%基础2星 | 单元 | P0 |
| STR-003 | 准确率50-69%基础1星 | 单元 | P0 |
| STR-004 | 准确率<50%基础0星 | 单元 | P0 |
| STR-005 | 使用提示封顶2星 | 单元 | P0 |
| STR-006 | 无提示可达3星 | 单元 | P0 |
| STR-007 | 过快应用0.7x系数 | 单元 | P0 |
| STR-008 | 过慢应用0.8x系数 | 单元 | P0 |
| STR-009 | 正常时间1.0x系数 | 单元 | P0 |
| STR-010 | 连击10+加1星 | 单元 | P0 |
| STR-011 | 连击5-9无加成 | 单元 | P0 |
| STR-012 | 高连击可抵消错误 | 单元 | P1 |
| STR-013 | 满分得3星 | 边界 | P0 |
| STR-014 | 全错最高连击得1星 | 边界 | P0 |
| STR-015 | 最低分得0星 | 边界 | P0 |
| STR-016 | 星级限制0-3 | 边界 | P0 |
| STR-017 | 计算耗时<10ms | 性能 | P0 |
| STR-018 | Spell Battle集成 | 集成 | P0 |
| STR-019 | Quick Judge集成 | 集成 | P0 |
| STR-020 | 星级分布符合目标 | 验证 | P0 |

### 集成与真机测试 (10个)

| ID | 用例名称 | 类型 | 优先级 |
|----|----------|------|--------|
| INT-001 | 首次启动无崩溃 | E2E | P0 |
| INT-002 | 完整学习流程 | E2E | P0 |
| INT-003 | 地图导航流程 | E2E | P0 |
| INT-004 | 性能无退化 | 性能 | P0 |
| INT-005 | 内存无泄漏 | 性能 | P0 |
| INT-006 | 真机动画流畅 | 手动 | P0 |
| INT-007 | 真机导航正常 | 手动 | P0 |
| INT-008 | 真机评分正确 | 手动 | P0 |
| INT-009 | 多设备测试 | 手动 | P1 |
| INT-010 | 用户体验验证 | 手动 | P1 |

---

## 质量门禁

### P0质量门禁 (必须全部通过)

- [ ] 所有单元测试通过 (70个)
- [ ] UI测试通过 (20个)
- [ ] 真机测试通过 (≥2设备)
- [ ] 性能60fps达标
- [ ] 覆盖率 ≥ 80% (新代码)
- [ ] 0 P0/P1 Bug

### P1质量门禁 (建议通过)

- [ ] E2E测试通过 (10个)
- [ ] 多设备验证通过
- [ ] 性能基准无退化

---

## 测试报告模板

```markdown
# Sprint 1 测试报告

**测试日期**: 2026-03-06
**测试人员**: android-test-engineer
**Sprint周期**: 2026-02-21 至 2026-03-06

## 测试摘要

| Epic | 用例总数 | 通过 | 失败 | 通过率 |
|------|----------|------|------|--------|
| Epic #1 | 15 | - | - | -% |
| Epic #2 | 25 | - | - | -% |
| Epic #3 | 20 | - | - | -% |
| 集成 | 10 | - | - | -% |
| **总计** | **70** | **-** | **-** | **-%** |

## 详细结果

### Epic #1: 视觉反馈增强

### Epic #2: 地图系统重构

### Epic #3: 动态星级评分

### 真机测试

## 问题列表

| ID | 严重程度 | 描述 | 状态 |
|----|----------|------|------|
| BUG-001 | P0 | 描述 | 未修复 |

## 性能指标

| 指标 | 目标 | 实际 | 状态 |
|------|------|------|------|
| 动画帧率 | 60fps | - | - |
| 迷雾渲染 | <16ms | - | - |
| 评分计算 | <10ms | - | - |

## 建议

1. ...
2. ...
```

---

**文档状态**: ✅ Sprint 1 测试计划完成
**下一步**: 开始 Sprint 1 测试实施
