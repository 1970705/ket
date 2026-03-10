# Screenshot Testing Guide

**Epic #12 - Task 12.7**: Real Device UI Automation Testing

**Version**: 1.0
**Last Updated**: 2026-03-08
**Owner**: android-test-engineer

---

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Testing Methods](#testing-methods)
4. [Real Device Testing](#real-device-testing)
5. [CI/CD Integration](#cicd-integration)
6. [Troubleshooting](#troubleshooting)
7. [Best Practices](#best-practices)

---

## Overview

Screenshot testing is a visual regression testing technique that captures UI screenshots and compares them against baseline images to detect unintended visual changes.

### Why Screenshot Testing?

| Benefit | Description |
|---------|-------------|
| **Catch Visual Bugs** | Detect layout overflow, text clipping, color issues |
| **Cross-Device Validation** | Ensure UI works on different screen sizes |
| **API Level Testing** | Verify compatibility across Android versions |
| **Fast Feedback** | Visual issues caught before manual testing |

### Epic #12 Screenshot Testing Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Screenshot Testing Layer                  │
├─────────────────────────────────────────────────────────────┤
│  Real Device Scripts    │  CI/CD Pipeline   │  Manual QA   │
│  - run-device-tests.sh  │  - GitHub Actions │  - Visual QA │
│  - capture-screenshots  │  - AVD Testing    │  - Device    │
│  - compare-screenshots  │  - Artifact Store │    Matrix    │
└─────────────────────────────────────────────────────────────┘
```

---

## Quick Start

### Prerequisites

**Required**:
- Android SDK Platform Tools (adb)
- Connected Android device or emulator
- Bash shell (macOS/Linux)

**Optional**:
- ImageMagick (for visual diff)
- GitHub account (for CI/CD)

### 5-Minute Setup

```bash
# 1. Enable USB debugging on device
# Settings → About Phone → Tap "Build Number" 7 times
# Developer Options → USB Debugging → On

# 2. Verify ADB connection
adb devices

# 3. Run device tests
cd scripts/real-device
./run-device-tests.sh

# 4. View results
open docs/reports/testing/real-device/$(date +%Y-%m-%d)/SUMMARY_REPORT.md
```

---

## Testing Methods

### Method 1: Real Device ADB Testing

**Best for**: Full integration testing, real-world validation

**Location**: `scripts/real-device/`

```bash
# Run all test scenarios
./run-device-tests.sh

# Run specific scenario
SCENARIO=app_launch ./run-device-tests.sh

# Specify device
./run-device-tests.sh <device_id>
```

**Test Scenarios**:

| Scenario | Description | Screenshots |
|----------|-------------|-------------|
| `app_launch` | App startup and home screen | 1 |
| `onboarding` | New user onboarding flow | 3 |
| `learning_flow` | Learning screen navigation | 4 |
| `match_game` | Word match gameplay | 2 |
| `progress_save` | Progress persistence | 2 |

### Method 2: CI/CD Automated Testing

**Best for**: PR validation, continuous testing

**Location**: `.github/workflows/ci.yml`

```yaml
# Trigger manually
gh workflow run ci.yml -f run-ui-tests=true

# Trigger via commit message
git commit -m "feat: add feature [ui-test]"
```

### Method 3: Manual Visual QA

**Best for**: Exploratory testing, UX validation

**Location**: `docs/testing/checklists/VISUAL_QA_CHECKLIST.md`

```bash
# Follow visual QA checklist
cat docs/testing/checklists/VISUAL_QA_CHECKLIST.md
```

---

## Real Device Testing

### Device Setup

```bash
# 1. Check device compatibility
./scripts/real-device/setup-device.sh

# 2. Device info is captured automatically
# Output: device_info_<device_id>.json
```

### Running Tests

**Basic Execution**:
```bash
cd scripts/real-device
./run-device-tests.sh
```

**Advanced Options**:
```bash
# Specific scenario
SCENARIO=onboarding ./run-device-tests.sh

# Custom output directory
OUTPUT_DIR=/tmp/test-results ./run-device-tests.sh

# Skip cleanup (keep APK installed)
SKIP_CLEANUP=true ./run-device-tests.sh
```

### Capturing Screenshots

```bash
# Single screenshot
./scripts/real-device/capture-screenshots.sh <device_id> single /tmp/output.png

# Series of screenshots (with delays)
./scripts/real-device/capture-screenshots.sh <device_id> series /tmp/screenshots/
```

### Comparing Screenshots

```bash
# Compare single screenshot
./scripts/real-device/compare-screenshots.sh compare baseline.png current.png diff.png

# Batch comparison
./scripts/real-device/compare-screenshots.sh batch baseline/ current/ diffs/

# Parameters:
# - FUZZ_THRESHOLD: Pixel difference threshold (default: 10%)
# - DIFF_COLOR: Highlight color (default: red)
```

### Generating Reports

```bash
# Generate test report
./scripts/real-device/generate-report.sh /path/to/test/results <device_id> test

# Generate summary report
./scripts/real-device/generate-report.sh /path/to/test/results summary
```

---

## CI/CD Integration

### GitHub Actions Workflow

**File**: `.github/workflows/ci.yml`

**UI Automation Job**:
```yaml
ui-automation-tests:
  runs-on: macos-latest
  steps:
    - Checkout code
    - Setup JDK 17
    - Create AVD (cached)
    - Run instrumentation tests
    - Upload screenshots
```

### Triggering CI Tests

**Manual Trigger**:
```bash
gh workflow run ci.yml -f run-ui-tests=true
```

**Commit Message Trigger**:
```bash
git commit -m "feat: new feature [ui-test]"
git push
```

**PR Comment Integration**:
Test results are automatically posted as PR comments.

---

## Troubleshooting

### ADB Issues

**Problem**: `adb: device unauthorized`
```bash
# Solution
adb kill-server
adb start-server
# Re-connect device and authorize
```

**Problem**: `adb: device not found`
```bash
# Solution
export ANDROID_HOME=~/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"
adb devices
```

### Screenshot Issues

**Problem**: Screenshot capture fails
```bash
# Verify device is unlocked
adb shell dumpsys window | grep mShowLockscreen

# Check screen is on
adb shell dumpsys power | grep 'Display Power'
```

**Problem**: ImageMagick not found
```bash
# Install ImageMagick
brew install imagemagick
```

### CI/CD Issues

**Problem**: UI tests not running
```yaml
# Verify workflow_dispatch input is set
on:
  workflow_dispatch:
    inputs:
      run-ui-tests:
        type: boolean
        default: false
```

**Problem**: AVD creation timeout
```yaml
# Increase timeout or use caching
- name: AVD cache
  uses: actions/cache@v3
  with:
    path: |
      ~/.android/avd/*
      ~/.android/adb*
    key: avd-34
```

### Visual Comparison Issues

**Problem**: Too many false positives
```bash
# Adjust fuzz threshold
FUZZ_THRESHOLD=20 ./compare-screenshots.sh compare ...
```

**Problem**: Screenshots don't match due to rendering
```bash
# Disable animations during capture
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

---

## Best Practices

### 1. Test Organization

```
docs/reports/testing/real-device/
├── 2026-03-08/
│   ├── device_info_*.json       # Device specifications
│   ├── logcat_*.log             # System logs
│   ├── screenshots/             # Captured screenshots
│   │   └── <device_id>/
│   │       ├── app_launch/
│   │       ├── onboarding/
│   │       └── ...
│   ├── TEST_REPORT_*.md         # Per-device report
│   └── SUMMARY_REPORT.md        # Overall summary
```

### 2. Baseline Management

```bash
# Store baselines in version control
git add docs/screenshots/baseline/

# Tag baseline commits
git tag baseline-2026-03-08
git push origin baseline-2026-03-08
```

### 3. Device Matrix Testing

**Priority Levels**:

| Priority | Devices | Frequency |
|----------|---------|-----------|
| P0 | Xiaomi 13, Samsung S23 | Every PR |
| P1 | Pixel 7, Galaxy A10 | Daily |
| P2 | Tablet, Foldable | Weekly |

### 4. Screenshot Naming

```
<sceanrio>_<screen>_<state>_<device>.png

Examples:
- app_launch_home_default_xiaomi13.png
- onboarding_pet_selected_cats_samsungs23.png
- learning_game_active_pixel7.png
```

### 5. Test Data Isolation

```bash
# Clear app data before tests
adb shell pm clear com.wordland

# Use test-specific accounts
# Don't use personal accounts for testing
```

### 6. Performance Considerations

```bash
# Disable animations for faster tests
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# Re-enable after testing
adb shell settings put global window_animation_scale 1
```

---

## Appendix

### A. Multi-Device Matrix Configuration

**File**: `scripts/ci/multi-device-matrix.yml`

**Device Priorities**:
- **P0**: Standard phone, compact phone (must test)
- **P1**: Large screen, small screen, tablet (important)
- **P2**: Tablet landscape, foldable (nice to have)

### B. Test Scenario Definitions

**File**: `scripts/ci/multi-device-matrix.yml`

**Scenarios**:
- `all`: Complete test suite (45 min)
- `critical`: Critical path only (20 min)
- `layout`: Layout validation (15 min)

### C. Related Documentation

- [Real Device Scripts README](../../scripts/real-device/README.md)
- [CI/CD Guide](./EPIC12_CI_CD_GUIDE.md)
- [Visual QA Checklist](../../testing/checklists/VISUAL_QA_CHECKLIST.md)
- [Epic #12 Technical Debt](../../planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md)

---

**Document Status**: ✅ Complete
**Next Review**: After Epic #12 completion
**Maintainer**: android-test-engineer
