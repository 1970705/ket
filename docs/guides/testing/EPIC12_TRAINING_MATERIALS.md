# Epic #12 Team Training Materials

**Real Device UI Automation Testing**

**Version**: 1.0
**Last Updated**: 2026-03-08
**Owner**: android-test-engineer + android-architect

---

## Training Agenda

1. [Epic #12 Overview](#epic-12-overview) (15 min)
2. [Real Device Testing Scripts](#real-device-testing-scripts) (30 min)
3. [CI/CD Pipeline Walkthrough](#cicd-pipeline-walkthrough) (20 min)
4. [Multi-Device Testing Matrix](#multi-device-testing-matrix) (15 min)
5. [Hands-On Practice](#hands-on-practice) (30 min)
6. [Q&A](#qa) (10 min)

**Total Duration**: 2 hours

---

## Epic #12 Overview

### Learning Objectives

After this training, team members will be able to:

- ✅ Run real device tests using automation scripts
- ✅ Interpret test results and identify issues
- ✅ Configure CI/CD for UI testing
- ✅ Set up multi-device testing matrix
- ✅ Troubleshoot common testing issues

### Epic #12 Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Epic #12 Components                     │
├─────────────────────────────────────────────────────────────┤
│  Task 12.1  │ Robolectric 4.13 Upgrade                      │
│  Task 12.3  │ Real Device Testing Scripts (5 scripts)       │
│  Task 12.4  │ GitHub Actions CI/CD Integration              │
│  Task 12.5  │ Multi-Device Testing Matrix (7 devices)       │
│  Task 12.7  │ Documentation & Training Materials            │
└─────────────────────────────────────────────────────────────┘
```

### Key Statistics

| Metric | Value |
|--------|-------|
| Scripts Created | 5 |
| Devices Configured | 7 |
| API Levels Covered | 3 (29, 33, 34) |
| Screen Sizes | 6 variants |
| Test Scenarios | 5 |
| Documentation Pages | 4 |

---

## Real Device Testing Scripts

### Script Overview

```
scripts/real-device/
├── run-device-tests.sh         # 主测试脚本
├── setup-device.sh             # 设备准备
├── capture-screenshots.sh      # 截图捕获
├── compare-screenshots.sh      # 视觉对比
└── generate-report.sh          # 报告生成
```

### Script 1: run-device-tests.sh

**Purpose**: Execute all test scenarios on connected device

**Usage**:
```bash
cd scripts/real-device
./run-device-tests.sh [device_id]
```

**Test Scenarios**:
1. `app_launch` - App startup verification
2. `onboarding` - New user flow
3. `learning_flow` - Learning screen navigation
4. `match_game` - Word match gameplay
5. `progress_save` - Data persistence

**Output Structure**:
```
docs/reports/testing/real-device/2026-03-08/
├── device_info_{device_id}.json
├── logcat_{device_id}.log
├── screenshots/{device_id}/
├── TEST_REPORT_{device_id}.md
└── SUMMARY_REPORT.md
```

### Script 2: setup-device.sh

**Purpose**: Verify device readiness for testing

**Checks**:
- [ ] ADB connection
- [ ] USB debugging enabled
- [ ] Device authorization
- [ ] Battery level (>20%)
- [ ] Storage space (>512MB)
- [ ] Screen state (unlocked)

**Usage**:
```bash
./setup-device.sh [device_id]
```

### Script 3: capture-screenshots.sh

**Purpose**: Capture screenshots with optional GIF generation

**Modes**:
- `single`: Capture one screenshot
- `series`: Capture multiple with delays

**Usage**:
```bash
# Single screenshot
./capture-screenshots.sh <device_id> single /path/output.png

# Series with 2-second delays
./capture-screenshots.sh <device_id> series /path/screenshots/
```

### Script 4: compare-screenshots.sh

**Purpose**: Visual regression detection using ImageMagick

**Features**:
- Single image comparison
- Batch comparison
- Configurable fuzz threshold
- Diff image generation

**Usage**:
```bash
# Compare two images
./compare-screenshots.sh compare baseline.png current.png diff.png

# Batch comparison
./compare-screenshots.sh batch baseline/ current/ diffs/

# Custom threshold (default: 10%)
FUZZ_THRESHOLD=15 ./compare-screenshots.sh compare ...
```

### Script 5: generate-report.sh

**Purpose**: Generate test reports in Markdown and HTML

**Report Types**:
- `test`: Per-device detailed report
- `summary`: Overall test summary

**Usage**:
```bash
# Generate test report
./generate-report.sh /path/to/results <device_id> test

# Generate summary
./generate-report.sh /path/to/results summary
```

---

## CI/CD Pipeline Walkthrough

### GitHub Actions Workflow

**File**: `.github/workflows/ci.yml`

**Jobs Overview**:

```
CI Pipeline
├── unit-tests              # Unit tests + coverage
├── code-quality            # Detekt + KtLint
├── build                   # APK build
├── ui-automation-tests     # ✨ NEW: UI tests (Task 12.4)
├── performance-tests       # Benchmarks (manual)
└── ui-test-summary         # ✨ NEW: Test summary (Task 12.4)
```

### Triggering UI Tests

**Method 1: Manual Trigger**
```bash
# Via GitHub CLI
gh workflow run ci.yml -f run-ui-tests=true

# Via GitHub UI
# Actions → CI → Run workflow → Check "Run UI tests"
```

**Method 2: Commit Message**
```bash
git commit -m "feat: add feature [ui-test]"
git push
```

### Viewing Results

**Artifacts**:
- `ui-test-results`: Test reports (XML/HTML)
- `ui-test-screenshots`: Captured screenshots
- `microbenchmark-results`: Performance metrics
- `macrobenchmark-results`: Baseline metrics

**PR Comments**:
Test results are automatically posted as comments on PRs.

---

## Multi-Device Testing Matrix

### Device Configuration

**File**: `scripts/ci/multi-device-matrix.yml`

**Priority Levels**:

| Priority | Count | Description | Devices |
|----------|-------|-------------|---------|
| P0 | 2 | Must test (every PR) | Xiaomi 13, Samsung S23 |
| P1 | 3 | Important (daily) | Pixel 7, Galaxy A10, Tablet |
| P2 | 2 | Nice to have (weekly) | Tablet landscape, Foldable |

### Screen Size Coverage

```
Phone Sizes:
├── Small:   720x1280   (4.5")
├── Medium:  1080x2400  (6.5")
└── Large:   1440x3200  (6.8")

Tablet Sizes:
├── Portrait 8":   1200x2000
├── Portrait 10":  1200x2000
└── Landscape 10": 2000x1200
```

### API Level Coverage

| API | Version | Focus | Frequency |
|-----|---------|-------|-----------|
| 29 | Android 10 | Compatibility | Weekly |
| 33 | Android 13 | Material 3 | Every PR |
| 34 | Android 14 | Latest features | Every PR |

### Config Generator

**Script**: `scripts/ci/generate-test-configs.sh`

**Generates**:
- Robolectric properties
- Instrumentation test configs
- Device JSON configs
- Test suite configs

**Usage**:
```bash
cd scripts/ci
./generate-test-configs.sh
```

---

## Hands-On Practice

### Exercise 1: Run Device Tests (15 min)

**Objective**: Execute tests on a real device

**Steps**:
1. Connect your Android device via USB
2. Enable USB debugging
3. Verify connection: `adb devices`
4. Run the test script:
   ```bash
   cd scripts/real-device
   ./run-device-tests.sh
   ```
5. Review the generated report

**Expected Output**:
```
docs/reports/testing/real-device/$(date +%Y-%m-%d)/SUMMARY_REPORT.md
```

### Exercise 2: Capture and Compare Screenshots (10 min)

**Objective**: Use screenshot capture and comparison tools

**Steps**:
1. Capture a screenshot:
   ```bash
   ./capture-screenshots.sh <device_id> single /tmp/baseline.png
   ```
2. Make a UI change (e.g., change button color)
3. Capture another screenshot:
   ```bash
   ./capture-screenshots.sh <device_id> single /tmp/current.png
   ```
4. Compare:
   ```bash
   ./compare-screenshots.sh compare /tmp/baseline.png /tmp/current.png /tmp/diff.png
   ```

### Exercise 3: Generate Test Configs (5 min)

**Objective**: Generate test configuration files

**Steps**:
1. Run the config generator:
   ```bash
   cd scripts/ci
   ./generate-test-configs.sh
   ```
2. Review generated files:
   - `app/src/test/resources/robolectric_properties.properties`
   - `app/src/androidTest/resources/config/`
   - `app/src/androidTest/resources/config/device_*.json`

---

## Q&A

### Common Questions

**Q: What if I don't have a real device?**
A: You can use the Android Emulator. The scripts work with both real devices and emulators.

**Q: How do I add a new test scenario?**
A: Edit `run-device-tests.sh` and add a new `test_<scenario>()` function following the existing pattern.

**Q: Can I run tests on multiple devices simultaneously?**
A: Yes! Run the script in parallel:
```bash
./run-device-tests.sh device1 &
./run-device-tests.sh device2 &
wait
```

**Q: How do I ignore minor visual differences?**
A: Increase the fuzz threshold:
```bash
FUZZ_THRESHOLD=20 ./compare-screenshots.sh compare ...
```

**Q: What's the difference between P0, P1, and P2 devices?**
A:
- P0: Must test on every PR (most common user devices)
- P1: Should test daily (important variants)
- P2: Nice to test weekly (edge cases)

---

## Resources

### Documentation

| Document | Location |
|----------|----------|
| Screenshot Testing Guide | `docs/guides/testing/SCREENSHOT_TESTING_GUIDE.md` |
| Troubleshooting Guide | `docs/guides/testing/EPIC12_TROUBLESHOOTING_GUIDE.md` |
| Best Practices | `docs/guides/testing/EPIC12_BEST_PRACTICES.md` |
| CI/CD Guide | `docs/guides/testing/EPIC12_CI_CD_GUIDE.md` |
| Real Device Scripts README | `scripts/real-device/README.md` |

### Quick Reference

```bash
# Run tests
cd scripts/real-device && ./run-device-tests.sh

# Check devices
adb devices

# Capture screenshot
adb exec-out screencap -p > screen.png

# View logs
adb logcat | grep Wordland

# Generate configs
cd scripts/ci && ./generate-test-configs.sh
```

### Contact

**Technical Lead**: android-architect
**Test Engineer**: android-test-engineer
**Issue Tracker**: GitHub issues with label `epic-12`

---

## Training Checklist

Use this checklist to track your progress:

- [ ] Understand Epic #12 objectives and components
- [ ] Run device tests on a real device/emulator
- [ ] Capture screenshots using the automation script
- [ ] Compare screenshots and interpret diff results
- [ ] Generate test reports
- [ ] Trigger CI/CD UI tests manually
- [ ] Review multi-device matrix configuration
- [ ] Generate test configs using the script
- [ ] Troubleshoot at least one common issue

**Certificate**: 🎉 Complete all items to receive Epic #12 certification!

---

**Document Status**: ✅ Complete
**Last Updated**: 2026-03-08
**Version**: 1.0
