# Epic #12 团队培训指南

**目标受众**: Wordland 开发团队
**培训时长**: 30 分钟
**更新日期**: 2026-03-08

---

## 一、培训目标

完成培训后，团队成员将能够：
1. ✅ 理解 Epic #12 的 CI/CD 架构
2. ✅ 运行本地 UI 测试
3. ✅ 触发和监控 GitHub Actions
4. ✅ 使用真机测试脚本
5. ✅ 解读测试报告

---

## 二、CI/CD 架构概览

### 2.1 工作流架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Actions CI/CD                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   ci.yml     │  │ ui-tests.yml │  │device-matrix │      │
│  │              │  │              │  │    .yml      │      │
│  │ • 单元测试   │  │ • UI 测试    │  │ • 多设备     │      │
│  │ • 代码检查   │  │ • 截图       │  │ • 兼容性     │      │
│  │ • 构建 APK   │  │ • 真机测试   │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 触发条件

| 工作流 | 自动触发 | 手动触发 |
|--------|---------|---------|
| ci.yml | Push/PR | ✅ |
| ui-tests.yml | UI 文件变更 | ✅ |
| device-matrix.yml | Push to main | ✅ |

---

## 三、本地开发工作流

### 3.1 提交前检查

```bash
# 1. 运行单元测试
./gradlew testDebugUnitTest

# 2. 运行代码检查
./gradlew detekt ktlintCheck

# 3. 构建 APK
./gradlew assembleDebug
```

### 3.2 UI 测试本地运行

```bash
# Compose UI 测试
./gradlew connectedDebugAndroidTest

# 特定组件测试
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.ui.components.HintCardTest
```

### 3.3 真机测试

```bash
# 1. 连接设备
adb devices

# 2. 安装 APK
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 运行测试脚本
chmod +x scripts/real-device/capture-ci-screenshots.sh
./scripts/real-device/capture-ci-screenshots.sh
```

---

## 四、GitHub Actions 使用

### 4.1 查看工作流

```bash
# 列出所有工作流
gh workflow list

# 查看工作流状态
gh workflow view ui-tests.yml
```

### 4.2 手动触发

```bash
# 触发 UI 测试
gh workflow run ui-tests.yml

# 触发设备矩阵（指定测试套件）
gh workflow run device-matrix.yml -f test-suite=critical

# 触发真机测试
gh workflow run ui-tests.yml -f run-device-tests=true
```

### 4.3 监控运行

```bash
# 查看运行历史
gh run list --workflow=ui-tests.yml

# 查看特定运行
gh run view <run-id>

# 等待完成
gh run watch <run-id>
```

---

## 五、设备矩阵配置

### 5.1 设备规格

| 设备 | API | 屏幕 | 用途 |
|------|-----|------|------|
| Pixel 7 | 33 | 6.3" | 标准手机 |
| Samsung S23 | 33 | 6.1" | 主流设备 |
| Small Phone | 29 | 5.5" | 小屏测试 |
| Tablet | 33 | 10" | 平板适配 |

### 5.2 测试套件

```yaml
all:      # 完整测试（所有设备）
critical: # 关键路径（快速验证）
smoke:    # 冒烟测试（基本功能）
```

---

## 六、解读测试报告

### 6.1 Artifacts 位置

```
GitHub Actions Run
├── Artifacts
│   ├── compose-ui-test-results  # UI 测试结果
│   ├── screenshot-scripts        # 截图脚本
│   ├── real-device-report        # 真机报告
│   ├── test-results-<device>     # 设备测试结果
│   └── matrix-summary            # 矩阵摘要
```

### 6.2 PR 评论格式

```
## 📸 UI Testing Results

### Visual Regression
- ⏳ Screenshot comparison pending manual review
- ✅ No automated visual regressions detected

### Next Steps
1. Review UI changes on real device
2. Run manual tests from Visual QA checklist
3. Approve or request changes
```

---

## 七、故障排查

### 7.1 常见问题

**Q: UI 测试在 CI 中失败但本地通过**

A: 检查以下项目：
- CI 环境的屏幕密度配置
- 测试超时设置
- 依赖版本一致性

**Q: 设备矩阵测试超时**

A:
- 增加 AVD 缓存
- 减少 `fail-fast` 配置
- 分批运行设备测试

**Q: 真机测试无法连接**

A:
```bash
adb kill-server
adb start-server
adb devices
```

### 7.2 获取帮助

- 查看 CI/CD 配置指南: `docs/guides/testing/EPIC12_CI_CD_GUIDE.md`
- 查看 Epic #12 完成报告: `docs/reports/testing/EPIC12_COMPLETION_REPORT.md`
- 联系: android-architect

---

## 八、最佳实践

### ✅ DO

- 提交前运行本地测试
- 使用描述性的 commit message
- 及时查看 CI 反馈
- 保持设备配置更新

### ❌ DON'T

- 跳过失败的测试
- 忽略 PR 评论
- 直接推送到 main（使用 PR）
- 阻塞 CI 运行

---

## 九、快速参考卡

```bash
# === 本地开发 ===
./gradlew testDebugUnitTest           # 单元测试
./gradlew connectedDebugAndroidTest   # UI 测试
./gradlew assembleDebug               # 构建

# === GitHub Actions ===
gh workflow list                       # 列出工作流
gh workflow run ui-tests.yml           # 触发 UI 测试
gh run list --workflow=ui-tests.yml    # 查看历史

# === 真机测试 ===
adb devices                            # 检查设备
adb install -r app-debug.apk          # 安装 APK
./scripts/real-device/*.sh            # 运行脚本

# === 故障排查 ===
gh run view <run-id>                  # 查看运行详情
adb logcat | grep Wordland            # 查看日志
```

---

## 十、培训检查表

完成培训后，请确认：

- [ ] 理解 CI/CD 架构
- [ ] 成功运行本地 UI 测试
- [ ] 手动触发 GitHub Actions
- [ ] 查看并解读测试报告
- [ ] 连接真机并运行测试脚本
- [ ] 了解故障排查流程

---

**培训版本**: 1.0
**有效期**: 持续更新
**反馈**: 请向 team-lead 提供培训反馈
