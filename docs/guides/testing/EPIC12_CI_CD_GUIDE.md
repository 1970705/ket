# Epic #12 CI/CD 配置指南

**创建日期**: 2026-03-08
**Epic**: #12 Real Device UI Automation Testing
**状态**: ✅ 配置完成 (Task 12.4 + 12.5)

---

## 一、概览

### CI/CD 工作流架构

```
GitHub Actions CI/CD
├── .github/workflows/ci.yml (主 CI 流水线)
│   ├── unit-tests              # 单元测试 + JaCoCo 覆盖率
│   ├── code-quality            # Detekt + KtLint
│   ├── build                   # APK 构建
│   ├── ui-automation-tests     # ✨ UI 自动化测试 (Task 12.4)
│   ├── performance-tests       # 性能测试 (手动触发)
│   └── ui-test-summary         # ✨ 测试摘要 + PR 评论 (Task 12.4)
│
├── scripts/ci/multi-device-matrix.yml (✨ 新增 - Task 12.5)
│   ├── device_matrix           # 7 设备配置 (P0/P1/P2)
│   ├── screen_variants         # 6 种屏幕尺寸
│   ├── api_level_config        # 3 个 API 级别 (29, 33, 34)
│   └── test_scenarios          # 3 种测试场景 (all/critical/layout)
│
└── scripts/ci/generate-test-configs.sh (✨ 新增 - Task 12.5)
    ├── Robolectric 配置生成
    ├── Instrumentation 配置生成
    ├── 设备 JSON 配置生成
    └── 测试套件配置生成
```

### Task 12.4 完成项

| 组件 | 描述 | 状态 |
|------|------|------|
| ui-automation-tests Job | AVD API 34 自动化测试 | ✅ |
| AVD 缓存机制 | 加速模拟器启动 | ✅ |
| 工作流触发 | 手动 + commit message | ✅ |
| PR 评论集成 | 自动评论测试结果 | ✅ |

### Task 12.5 完成项

| 组件 | 描述 | 状态 |
|------|------|------|
| 多设备矩阵配置 | 7 设备 (P0: 2, P1: 3, P2: 2) | ✅ |
| API 级别配置 | Android 10/13/14 | ✅ |
| 屏幕尺寸变体 | phone small/medium/large, tablet | ✅ |
| 配置生成器 | 自动生成测试配置 | ✅ |

---

## 二、工作流详解

### 2.1 ui-automation-tests Job (Task 12.4)

**位置**: `.github/workflows/ci.yml`

**触发条件**:
```yaml
if: github.event_name == 'workflow_dispatch' && inputs.run-ui-tests == true
    || contains(github.event.head_commit.message, '[ui-test]')
```

**工作流步骤**:

```yaml
ui-automation-tests:
  runs-on: macos-latest

  steps:
    1. Checkout code
    2. Set up JDK 17
    3. Grant execute permission for gradlew
    4. Enable KVM group perms
    5. AVD cache (加速启动)
    6. Create AVD and generate snapshot
    7. Run Android instrumentation tests
    8. Upload UI test results
    9. Upload UI test screenshots
```

**关键配置**:

```yaml
# AVD 缓存配置
- name: AVD cache
  uses: actions/cache@v3
  with:
    path: |
      ~/.android/avd/*
      ~/.android/adb*
    key: avd-34

# 模拟器选项 (优化性能)
emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim

# 禁用动画 (加速测试)
disable-animations: true

# 测试命令
script: ./gradlew connectedDebugAndroidTest
```

**输出 Artifacts**:
- `ui-test-results`: 测试报告 XML/HTML
- `ui-test-screenshots`: 截图文件

---

### 2.2 ui-test-summary Job (Task 12.4)

**功能**: 生成测试摘要并添加 PR 评论

**触发条件**:
```yaml
if: always()
needs: [unit-tests, code-quality, build]
```

**生成的摘要**:
```markdown
## 🧪 测试执行摘要

**执行时间**: 2026-03-08

## 📊 测试结果

| 作业 | 状态 |
|------|------|
| 单元测试 | ✅ 通过 |
| 代码质量 | ✅ 通过 |
| 构建检查 | ✅ 通过 |
```

**PR 评论集成**:
```yaml
- name: Test summary comment
  if: always() && github.event_name == 'pull_request'
  uses: actions/github-script@v7
```

---

### 2.3 multi-device-matrix.yml (Task 12.5)

**位置**: `scripts/ci/multi-device-matrix.yml`

**设备矩阵结构**:

```yaml
device_matrix:
  priority_p0:  # 必须测试
    - Standard Fullscreen Phone (1080x2400, API 33/34)
    - Compact Phone (1080x2340, API 29/33)

  priority_p1:  # 重要
    - Large Screen Phone (1440x3200, API 33)
    - Small Screen Phone (720x1280, API 29/33)
    - Tablet Portrait (1200x2000, API 33)

  priority_p2:  # 低优先级
    - Tablet Landscape (2000x1200, API 33)
    - Foldable Unfolded (2208x1768, API 33)
```

**屏幕尺寸变体**:
```yaml
screen_variants:
  phone:
    small: 720x1280 (4.5")
    medium: 1080x2400 (6.5")
    large: 1440x3200 (6.8")

  tablet:
    portrait_8: 1200x2000 (8")
    portrait_10: 1200x2000 (10")
    landscape_10: 2000x1200 (10" 横屏)
```

**API 级别配置**:
```yaml
api_levels:
  - version: 29  # Android 10
    features: [dark_mode, gesture_nav]
  - version: 33  # Android 13
    features: [material3, predictive_back, per_app_language]
  - version: 34  # Android 14
    features: [edge_to_edge, back_arrow]
```

**测试场景**:
```yaml
test_scenarios:
  all:        # 完整测试 (45 分钟)
    - app_launch, onboarding, learning_flow, match_game, progress_save

  critical:   # 关键路径 (20 分钟)
    - app_launch, learning_flow

  layout:     # 布局验证 (15 分钟)
    - app_launch, onboarding
```

**设备配置**:

| 设备 | API Level | 屏幕尺寸 | 密度 | 优先级 | 测试套件 |
|------|----------|---------|------|--------|---------|
| Pixel 7 | 33 | 6.3" | 416dpi | P0 | all, critical, smoke |
| Samsung S23 | 33 | 6.1" | 425dpi | P0 | all, critical, smoke |
| Small Phone | 29 | 5.5" | xhdpi | P1 | critical, smoke |
| Tablet | 33 | 10" | xhdpi | P1 | critical |

**测试套件**:
- `all`: 完整测试套件
- `critical`: 关键路径测试
- `smoke`: 冒烟测试

---

## 三、使用指南

### 3.1 本地验证 CI 配置

```bash
# 验证 workflow 语法
act -l  # 需要安装 act (本地 GitHub Actions)

# 手动触发 UI 测试
gh workflow run ui-tests.yml

# 手动触发设备矩阵测试
gh workflow run device-matrix.yml -f test-suite=critical
```

### 3.2 真机测试设置

**自托管 Runner 配置**:
```bash
# 在测试机器上安装 GitHub Actions Runner
# 1. 下载 runner
# 2. 配置 runner
./config.sh --url https://github.com/<repo> --token <token>

# 3. 安装服务
sudo ./svc.sh install
sudo ./svc.sh start

# 4. 连接测试设备
adb devices
```

### 3.3 截图脚本使用

```bash
# 生成截图脚本位置
scripts/real-device/capture-ci-screenshots.sh

# 手动运行截图捕获
chmod +x scripts/real-device/capture-ci-screenshots.sh
./scripts/real-device/capture-ci-screenshots.sh
```

---

## 四、配置文件位置

| 文件 | 路径 | 用途 |
|------|------|------|
| 主 CI | `.github/workflows/ci.yml` | 单元测试、代码检查、构建 |
| UI 测试 | `.github/workflows/ui-tests.yml` | Compose UI 测试、截图、真机 |
| 设备矩阵 | `.github/workflows/device-matrix.yml` | 多设备兼容性测试 |

---

## 五、故障排查

### 5.1 UI 测试失败

**问题**: Compose UI tests 在 CI 中失败

**解决**:
```bash
# 本地运行相同的测试
./gradlew connectedDebugAndroidTest

# 检查模拟器配置
adb devices
```

### 5.2 设备矩阵超时

**问题**: 设备矩阵测试超时

**解决**:
- 增加 `timeout-minutes` 配置
- 使用 AVD 缓存加速启动
- 减少 `fail-fast: false` 配置

### 5.3 真机测试无法连接

**问题**: Real device tests 无法找到设备

**解决**:
```bash
# 检查设备连接
adb devices -l

# 重启 adb
adb kill-server
adb start-server
```

---

## 六、最佳实践

### 6.1 PR 工作流

```
创建 PR → 自动触发:
  ├── ui-tests (快速反馈)
  └── 必要时手动触发 device-matrix
```

### 6.2 主分支合并

```
合并到 main → 自动触发:
  ├── ci.yml (完整 CI)
  ├── ui-tests.yml (UI 测试)
  └── device-matrix.yml (多设备验证)
```

### 6.3 性能优化

- 使用 AVD 缓存减少启动时间
- `fail-fast: false` 确保所有设备测试完成
- 并行执行独立任务

---

## 七、后续改进

- [ ] 集成 Firebase Test Lab
- [ ] 自动化截图对比工具
- [ ] 性能基准回归检测
- [ ] 多语言测试支持

---

**文档版本**: 1.0
**最后更新**: 2026-03-08
**维护者**: android-architect
