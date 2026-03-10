# Epic #12: 真机 UI 自动化测试计划

**Epic ID**: EPIC-12
**优先级**: P1
**创建日期**: 2026-03-08
**负责人**: android-test-engineer
**状态**: 规划中

---

## 一、背景与动机

### 1.1 历史问题

在真机测试（Xiaomi 24031PN0DC）中发现了多个 P0/P1 UI BUG：

| Bug ID | 描述 | 根因 | 发现环境 |
|--------|------|------|---------|
| P0-BUG-003 | LearningScreen 提交按钮截断 | 布局无滚动 | 真机 |
| P1-BUG-002 | HintCard "提示"文字裁剪 | 按钮高度不足 | 真机 |

**关键发现**：这些问题在模拟器上未被发现，仅在真机测试中暴露。

### 1.2 当前测试覆盖率

| 层级 | 当前覆盖率 | 目标覆盖率 | 差距 |
|------|-----------|-----------|------|
| UI Components | 0% | 60% | 60% |
| UI Screens | 0% | 60% | 60% |
| ViewModel | 88% | 90% | 2% |
| Domain | 77% | 80% | 3% |

**核心问题**：UI 层测试覆盖率接近 0%，导致视觉 BUG 无法被自动化检测。

### 1.3 目标

建立完整的真机 UI 自动化测试体系：
1. 防止 UI 回归问题
2. 自动化视觉验证
3. 多设备兼容性检测
4. CI/CD 集成

---

## 二、技术选型分析

### 2.1 截图测试工具对比

| 工具 | 类型 | 优势 | 劣势 | 推荐场景 |
|------|------|------|------|---------|
| **Paparazzi** ⭐ | 仿真截图 | 秒级执行、无设备依赖、CI 友好 | 不验证真机渲染 | 组件级回归测试 |
| **Roborazzi** | 仿真截图 | Compose 支持好、主流 | 需要 Android 环境 | Compose 组件测试 |
| **Shot** | 真机截图 | 真实设备渲染、多设备支持 | 需要设备、较慢 | 真机视觉验证 |
| **Espresso** | 功能测试 | Google 官方、速度最快 | 仅 Android | 单应用功能测试 |
| **UI Automator** | 黑盒测试 | 跨应用、系统级操作 | 需要真机 | 真机自动化脚本 |

### 2.2 推荐方案：分层策略

```
┌─────────────────────────────────────────────────────────────┐
│                     UI 自动化测试金字塔                       │
├─────────────────────────────────────────────────────────────┤
│                    ┌─────────┐                              │
│                    │ 真机测试 │  ← 10% (UI Automator + ADB)   │
│                  ┌─┴─────────┴─┐                            │
│                  │ 功能测试     │  ← 20% (Espresso)          │
│                ┌─┴─────────────┴─┐                          │
│                │ 截图对比测试     │  ← 30% (Paparazzi) ⭐     │
│              ┌─┴─────────────────┴─┐                        │
│              │ 单元测试 (逻辑验证)  │  ← 40% (JUnit + MockK)  │
│              └─────────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 技术栈选择

| 测试类型 | 工具 | 用途 |
|---------|------|------|
| 组件截图测试 | **Paparazzi** ⭐ | UI 回归检测，防止布局问题 |
| 功能测试 | **Espresso** | 单应用功能测试（已有基础） |
| 真机自动化 | **UI Automator** | logcat 监控、自动安装测试 |
| CI 执行 | **GitHub Actions** | 自动化流水线 |

### 2.4 为什么选择 Paparazzi

| 优势 | 说明 |
|------|------|
| **秒级执行** | 直接在 JVM 渲染，比传统测试快 10x+ |
| **无设备依赖** | 无需启动模拟器/真机 |
| **CI 友好** | 完美支持 GitHub Actions |
| **防止布局问题** | 可检测类似 P0-BUG-003 布局溢出 |
| **无障碍报告** | 自动生成 Accessibility 验证 |

### 2.5 Paparazzi 详细配置

```kotlin
// build.gradle.kts (project level)
plugins {
    id("app.cash.paparazzi") version "1.3.1"
}

// build.gradle.kts (app level)
dependencies {
    testImplementation("app.cash.paparazzi:paparazzi:1.3.1")
}
```

**测试示例**:
```kotlin
class HintCardPaparazziTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun hintCard_defaultState() {
        paparazzi.snapshot {
            HintCard(
                hintsRemaining = 3,
                onHintClick = {}
            )
        }
    }

    @Test
    fun hintCard_noHintsRemaining() {
        paparazzi.snapshot {
            HintCard(
                hintsRemaining = 0,
                onHintClick = {}
            )
        }
    }

    @Test
    fun hintCard_hintShown() {
        paparazzi.snapshot {
            HintCard(
                hintsRemaining = 2,
                hintText = "首字母: A",
                onHintClick = {}
            )
        }
    }
}
```

---

## 三、自动化真机测试脚本设计

### 3.1 脚本架构

```
scripts/
├── real-device/
│   ├── run-device-tests.sh      # 主入口脚本
│   ├── setup-device.sh          # 设备准备
│   ├── capture-screenshots.sh   # 截图捕获
│   ├── compare-screenshots.sh   # 截图对比
│   └── generate-report.sh       # 报告生成
└── ci/
    └── device-test-matrix.yml   # CI 设备矩阵配置
```

### 3.2 核心脚本设计

#### 3.2.1 run-device-tests.sh

```bash
#!/bin/bash
# 真机自动化测试主脚本
# 用法: ./run-device-tests.sh [device_id]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPORT_DIR="docs/reports/testing/real-device/$(date +%Y-%m-%d)"
LOG_FILE="$REPORT_DIR/test-execution.log"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1" | tee -a "$LOG_FILE"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$LOG_FILE"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"; }

# 1. 环境检查
check_environment() {
    log_info "检查测试环境..."

    # 检查 ADB
    if ! command -v adb &> /dev/null; then
        log_error "ADB 未找到，请设置 ANDROID_HOME"
        exit 1
    fi

    # 检查设备连接
    DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)
    if [ "$DEVICES" -eq 0 ]; then
        log_error "没有检测到真机设备"
        exit 1
    fi

    log_info "检测到 $DEVICES 台设备"
}

# 2. 设备信息收集
collect_device_info() {
    local device_id=$1

    log_info "收集设备信息: $device_id"

    DEVICE_MODEL=$(adb -s "$device_id" shell getprop ro.product.model)
    ANDROID_VERSION=$(adb -s "$device_id" shell getprop ro.build.version.release)
    SCREEN_DENSITY=$(adb -s "$device_id" shell wm density)
    SCREEN_SIZE=$(adb -s "$device_id" shell wm size)

    cat > "$REPORT_DIR/device_info_$device_id.json" << EOF
{
    "device_id": "$device_id",
    "model": "$DEVICE_MODEL",
    "android_version": "$ANDROID_VERSION",
    "screen_density": "$SCREEN_DENSITY",
    "screen_size": "$SCREEN_SIZE",
    "test_date": "$(date -Iseconds)"
}
EOF
}

# 3. 应用安装
install_apk() {
    local device_id=$1

    log_info "安装 APK 到设备: $device_id"

    # 构建最新 APK
    ./gradlew assembleDebug

    # 卸载旧版本
    adb -s "$device_id" uninstall com.wordland 2>/dev/null || true

    # 安装新版本
    adb -s "$device_id" install -r app/build/outputs/apk/debug/app-debug.apk

    # 清除应用数据
    adb -s "$device_id" shell pm clear com.wordland
}

# 4. 启动 logcat 监控（CRITICAL）
start_logcat_monitor() {
    local device_id=$1

    log_info "启动 logcat 监控: $device_id"

    adb -s "$device_id" logcat -c
    adb -s "$device_id" logcat -v time | grep -E "(Wordland|AndroidRuntime|FATAL)" \
        > "$REPORT_DIR/logcat_$device_id.log" &
    LOGCAT_PID=$!
}

stop_logcat_monitor() {
    if [ -n "$LOGCAT_PID" ]; then
        kill $LOGCAT_PID 2>/dev/null || true
    fi
}

# 5. 执行测试场景
run_test_scenarios() {
    local device_id=$1
    local failed_tests=()

    log_info "执行测试场景: $device_id"

    # 场景 1: 应用启动
    run_scenario "app_launch" "应用启动测试" "$device_id" || failed_tests+=("app_launch")

    # 场景 2: 新手引导
    run_scenario "onboarding" "新手引导测试" "$device_id" || failed_tests+=("onboarding")

    # 场景 3: 学习流程
    run_scenario "learning_flow" "学习流程测试" "$device_id" || failed_tests+=("learning_flow")

    # 场景 4: 单词配对游戏
    run_scenario "match_game" "单词配对游戏测试" "$device_id" || failed_tests+=("match_game")

    # 场景 5: 进度保存
    run_scenario "progress_save" "进度保存测试" "$device_id" || failed_tests+=("progress_save")

    # 报告失败测试
    if [ ${#failed_tests[@]} -gt 0 ]; then
        log_warn "失败的测试场景: ${failed_tests[*]}"
        return 1
    fi
    return 0
}

# 单个测试场景
run_scenario() {
    local scenario_name=$1
    local scenario_desc=$2
    local device_id=$3

    log_info "执行场景: $scenario_desc"

    # 截图保存路径
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/$scenario_name"
    mkdir -p "$screenshot_dir"

    case $scenario_name in
        "app_launch")
            # 启动应用
            adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity
            sleep 2
            capture_screenshot "$device_id" "$screenshot_dir/01_home.png"
            ;;

        "onboarding")
            # 清除数据后启动
            adb -s "$device_id" shell pm clear com.wordland
            adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity
            sleep 2
            capture_screenshot "$device_id" "$screenshot_dir/01_welcome.png"
            # ... 更多步骤
            ;;

        # ... 其他场景
    esac

    # 检查是否有崩溃
    if grep -q "FATAL EXCEPTION" "$REPORT_DIR/logcat_$device_id.log"; then
        log_error "场景 $scenario_name 发现崩溃"
        return 1
    fi

    return 0
}

# 截图捕获
capture_screenshot() {
    local device_id=$1
    local output_path=$2

    adb -s "$device_id" shell screencap -p /sdcard/temp_screenshot.png
    adb -s "$device_id" pull /sdcard/temp_screenshot.png "$output_path"
    adb -s "$device_id" shell rm /sdcard/temp_screenshot.png
}

# 6. 生成报告
generate_report() {
    local device_id=$1

    log_info "生成测试报告: $device_id"

    cat > "$REPORT_DIR/REPORT_$device_id.md" << EOF
# 真机测试报告

**设备**: $(adb -s "$device_id" shell getprop ro.product.model)
**Android 版本**: $(adb -s "$device_id" shell getprop ro.build.version.release)
**测试日期**: $(date)

## 测试结果

| 场景 | 状态 | 截图 |
|------|------|------|
| 应用启动 | ✅ | [查看](screenshots/$device_id/app_launch/01_home.png) |
| 新手引导 | ✅ | [查看](screenshots/$device_id/onboarding/) |

## logcat 日志

[查看完整日志](logcat_$device_id.log)

## 发现的问题

(自动填充)

EOF
}

# 主流程
main() {
    local device_id=${1:-$(adb devices | grep "device$" | head -1 | cut -f1)}

    mkdir -p "$REPORT_DIR"

    log_info "========== 真机自动化测试开始 =========="
    log_info "目标设备: $device_id"

    check_environment
    collect_device_info "$device_id"
    install_apk "$device_id"
    start_logcat_monitor "$device_id"

    trap stop_logcat_monitor EXIT

    run_test_scenarios "$device_id"
    TEST_RESULT=$?

    generate_report "$device_id"

    log_info "========== 真机自动化测试完成 =========="

    exit $TEST_RESULT
}

main "$@"
```

### 3.3 错误检测规则

```bash
# detect-errors.sh
# 自动检测 logcat 中的错误

detect_errors() {
    local log_file=$1
    local error_patterns=(
        "FATAL EXCEPTION"
        "AndroidRuntime"
        "java.lang.NullPointerException"
        "java.lang.IllegalStateException"
        "ConstraintLayout.*overflow"
        "TextView.*too small"
        "Choreographer.*skipped"
    )

    for pattern in "${error_patterns[@]}"; do
        if grep -q "$pattern" "$log_file"; then
            echo "发现错误: $pattern"
            grep "$pattern" "$log_file" >> errors.log
        fi
    done
}
```

---

## 四、多设备测试矩阵

### 4.1 设备选择策略

| 设备类型 | 代表设备 | 屏幕尺寸 | Android 版本 | 优先级 |
|---------|---------|---------|-------------|--------|
| 小屏手机 | Samsung Galaxy A10 | 5.6" | API 28 | P1 |
| 标准手机 | Xiaomi 13 | 6.36" | API 33 | P0 |
| 大屏手机 | Samsung Galaxy S23 Ultra | 6.8" | API 33 | P1 |
| 平板 | Samsung Galaxy Tab S9 | 11" | API 33 | P2 |

### 4.2 厂商覆盖矩阵

| 厂商 | 市场份额 | 代表机型 | ROM 特点 | 测试优先级 |
|------|---------|---------|---------|-----------|
| Samsung | 20%+ | Galaxy S/A 系列 | One UI | P0 |
| Xiaomi | 15%+ | 小米/红米系列 | MIUI/HyperOS | P0 |
| Google | 5% | Pixel 系列 | 原生 Android | P1 |
| OPPO/vivo | 15%+ | Reno/Find 系列 | ColorOS/OriginOS | P2 |

### 4.3 测试矩阵配置

```yaml
# device-test-matrix.yml
device_matrix:
  priority_p0:
    - name: "Xiaomi 13"
      model: "xiaomi_13"
      api_level: 33
      screen_size: "6.36 inch"
      density: "419 dpi"
      tests: ["all"]

    - name: "Samsung Galaxy S23"
      model: "galaxy_s23"
      api_level: 33
      screen_size: "6.1 inch"
      density: "425 dpi"
      tests: ["all"]

  priority_p1:
    - name: "Google Pixel 7"
      model: "pixel_7"
      api_level: 33
      screen_size: "6.3 inch"
      density: "416 dpi"
      tests: ["critical"]

    - name: "Samsung Galaxy A10"
      model: "galaxy_a10"
      api_level: 28
      screen_size: "5.6 inch"
      density: "295 dpi"
      tests: ["critical"]

test_suites:
  all:
    - app_launch
    - onboarding
    - learning_flow
    - match_game
    - progress_save
    - visual_qa

  critical:
    - app_launch
    - learning_flow
    - visual_qa
```

### 4.4 Firebase Test Lab 集成（可选）

```yaml
# firebase-test-lab.yml
gcloud:
  results-bucket: wordland-test-results
  record-video: true
  timeout: 30m
  async: true

flank:
  test-targets:
    - class com.wordland.ui.screens.HomeScreenTest
    - class com.wordland.ui.components.HintCardTest

  devices:
    - model: pixel7
      version: 33
      orientation: portrait
    - model: galaxy_s23
      version: 33
      orientation: portrait
```

---

## 五、CI/CD 集成方案

### 5.1 GitHub Actions 工作流

```yaml
# .github/workflows/ui-tests.yml
name: UI Tests

on:
  push:
    branches: [ main ]
    paths:
      - 'app/src/main/java/com/wordland/ui/**'
      - 'app/src/main/res/**'
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  # Job 1: Paparazzi 截图测试 (秒级执行)
  screenshot-tests:
    runs-on: ubuntu-latest  # 无需 macOS，因为不需要模拟器
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Paparazzi Tests
        run: ./gradlew testDebugUnitTest --tests "*PaparazziTest"

      - name: Verify Screenshots
        run: ./gradlew verifyPaparazziDebug

      - name: Upload screenshot diffs
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: screenshot-diffs
          path: app/build/reports/paparazzi/

  # Job 2: Espresso 功能测试
  espresso-tests:
    runs-on: macos-latest
    needs: screenshot-tests
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Espresso Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew connectedDebugAndroidTest

      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: espresso-test-results
          path: app/build/reports/androidTests/

  # Job 3: 真机测试（需要自托管 runner + 真机设备）
  real-device-tests:
    runs-on: self-hosted
    if: github.event_name == 'workflow_dispatch'
    steps:
      - uses: actions/checkout@v3

      - name: Run Real Device Tests
        run: |
          chmod +x scripts/real-device/run-device-tests.sh
          ./scripts/real-device/run-device-tests.sh

      - name: Upload test report
        uses: actions/upload-artifact@v3
        with:
          name: real-device-report
          path: docs/reports/testing/real-device/

  # Job 4: 视觉回归检查
  visual-regression:
    runs-on: ubuntu-latest
    needs: screenshot-tests
    steps:
      - uses: actions/checkout@v3

      - name: Download screenshots
        uses: actions/download-artifact@v3
        with:
          name: screenshot-diffs

      - name: Check for visual changes
        run: |
          if [ -d "diff" ] && [ "$(ls -A diff 2>/dev/null)" ]; then
            echo "::warning::Visual changes detected"
            # 不阻塞 CI，只发出警告
          fi
          echo "Visual regression check completed"
```

### 5.2 质量门禁

```yaml
# quality-gate.yml
quality_gates:
  ui_tests:
    pass_rate: 100%
    blocking: true

  screenshot_tests:
    diff_threshold: 0.1%  # 允许 0.1% 像素差异
    blocking: false
    notification: true

  visual_qa:
    blocking: false
    requires_approval: true
```

---

## 六、任务分解与工时估算

### 6.1 Task 列表

| Task ID | 任务名称 | 优先级 | 工时 | 依赖 |
|---------|---------|--------|------|------|
| 12.1 | Paparazzi 集成与配置 | P0 | 3h | - |
| 12.2 | 编写组件截图测试 | P0 | 6h | 12.1 |
| 12.3 | 真机测试脚本开发 (UI Automator + ADB) | P0 | 6h | - |
| 12.4 | GitHub Actions CI 集成 | P1 | 3h | 12.1, 12.3 |
| 12.5 | 多设备测试矩阵配置 | P1 | 3h | 12.3 |
| 12.6 | Visual QA 自动化 | P2 | 4h | 12.2 |
| 12.7 | 文档与培训 | P2 | 2h | ALL |

### 6.2 里程碑

```
Week 1 (2026-03-08 ~ 2026-03-14):
├── Task 12.1: Paparazzi 集成 (3h)
└── Task 12.3: 真机测试脚本开发 (6h)

Week 2 (2026-03-15 ~ 2026-03-21):
├── Task 12.2: 组件截图测试 (6h)
└── Task 12.4: CI 集成 (3h)

Week 3 (2026-03-22 ~ 2026-03-28):
├── Task 12.5: 多设备矩阵 (3h)
├── Task 12.6: Visual QA 自动化 (4h)
└── Task 12.7: 文档 (2h)
```

### 6.3 总工时估算

| 阶段 | 工时 | 说明 |
|------|------|------|
| P0 任务 | 15h | Paparazzi + 脚本 + 截图测试 |
| P1 任务 | 6h | CI + 多设备矩阵 |
| P2 任务 | 6h | Visual QA + 文档 |
| **总计** | **27h** | 约 3.5 个工作日 |

---

## 七、验收标准

### 7.1 完成定义 (DoD)

- [ ] Paparazzi 成功集成到项目
- [ ] 至少 10 个关键 UI 组件有截图测试
- [ ] 真机测试脚本可在 Xiaomi 设备上运行
- [ ] CI 流水线包含 Paparazzi 截图测试
- [ ] 测试报告自动生成
- [ ] 文档完整

### 7.2 质量指标

| 指标 | 目标 | 验证方法 |
|------|------|---------|
| 截图测试覆盖率 | ≥ 50% 组件 | 测试文件统计 |
| 真机测试通过率 | 100% | 测试报告 |
| CI 执行时间 | ≤ 10 分钟 | GitHub Actions |
| 误报率 | ≤ 5% | 人工审核 |

---

## 八、风险评估

### 8.1 技术风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| Paparazzi 与 Compose 版本不兼容 | 低 | 中 | 使用官方推荐版本 1.3.1 |
| 真机设备不可用 | 中 | 高 | 使用 Firebase Test Lab 备选 |
| 截图差异误报 | 中 | 中 | 设置合理的 diff 阈值 |
| CI 执行时间过长 | 低 | 低 | Paparazzi 秒级执行，无此问题 |

### 8.2 进度风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| 设备资源不足 | 中 | 中 | 优先完成 P0 任务 |
| 学习曲线 | 低 | 低 | 参考 Paparazzi 官方文档 |

---

## 九、参考资源

### 9.1 内部文档

- [Real Device UI Bug Discovery Summary](../../reports/testing/REAL_DEVICE_UI_BUG_DISCOVERY_SUMMARY_2026-02-22.md)
- [UI Testing Guide](../../guides/testing/UI_TESTING_GUIDE.md)
- [Test Strategy](../../testing/strategy/TEST_STRATEGY.md)
- [Visual QA Checklist](../../testing/checklists/VISUAL_QA_CHECKLIST.md)

### 9.2 外部资源

- [Paparazzi GitHub](https://github.com/cashapp/paparazzi) ⭐
- [Espresso 官方文档](https://developer.android.com/training/testing/espresso)
- [UI Automator 官方文档](https://developer.android.com/training/testing/ui-automator)
- [Firebase Test Lab](https://firebase.google.com/docs/test-lab)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

---

**文档版本**: 1.1
**创建日期**: 2026-03-08
**更新日期**: 2026-03-08
**作者**: android-test-engineer
**变更**: 根据 team-lead 技术选型调研报告，将 Roborazzi 替换为 Paparazzi