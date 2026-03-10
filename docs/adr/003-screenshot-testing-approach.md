# ADR 003: Screenshot Testing Approach - Compose Testing + ADB Scripts

## 状态
✅ 已采纳

## 日期
2026-03-08

## 决策者
android-architect, android-test-engineer

---

## 上下文

### 问题
Epic #12 需要建立真机 UI 自动化测试体系，以防止视觉 BUG 回归（如 P0-BUG-003: LearningScreen 按钮截断，P1-BUG-002: HintCard 文字裁剪）。

原始方案是使用 Paparazzi 作为截图测试工具，但在集成阶段遇到技术障碍。

### 技术背景

**Paparazzi 评估失败原因**:
1. **JDK 17 不兼容**: `java.lang.UnsupportedOperationException: class redefinition failed`
2. **资源加载失败**: `NoClassDefFoundError: Could not initialize class com.android.resources.ResourceType`
3. **JaCoCo 冲突**: 两个工具都使用 Java instrumentation

**参考**:
- [Paparazzi Issue #384](https://github.com/cashapp/paparazzi/issues/384) - Java 17 incompatibility
- [Paparazzi Issue #1030](https://github.com/cashapp/paparazzi/issues/1030) - JaCoCo incompatibility
- Epic #12 技术债务文档: `docs/planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md`

### 影响范围
- [x] UI层
- [ ] Domain层
- [ ] Data层
- [x] 构建系统
- [x] 测试
- [x] 文档

---

## 决策

### 选择方案
**Compose Testing + ADB Screenshot Scripts + Manual Visual QA**

采用混合方法：
1. **Compose Testing** - 组件逻辑验证和快速 UI 测试
2. **ADB Screenshot Scripts** - 真机截图捕获和对比
3. **Manual Visual QA** - 人工视觉验证

### 具体实现

#### 1. Compose Testing（组件级）

```kotlin
// 使用 createComposeRule() 进行组件测试
class HintCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hintCard_displaysCorrectly() {
        composeTestRule.setContent {
            HintCard(
                hintsRemaining = 3,
                onHintClick = {}
            )
        }

        // 验证组件存在
        composeTestRule.onNodeWithText("提示").assertExists()
    }
}
```

#### 2. ADB Screenshot Scripts（真机级）

```bash
# scripts/real-device/capture-screenshots.sh
#!/bin/bash
capture_screenshot() {
    local device_id=$1
    local output_path=$2

    adb -s "$device_id" shell screencap -p /sdcard/temp_screenshot.png
    adb -s "$device_id" pull /sdcard/temp_screenshot.png "$output_path"
    adb -s "$device_id" shell rm /sdcard/temp_screenshot.png
}

# 使用示例
capture_screenshot "DEVICE_ID" "reports/screenshots/home_screen.png"
```

#### 3. CI/CD 集成

```yaml
# .github/workflows/ui-tests.yml
jobs:
  compose-ui-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Compose UI tests
        run: ./gradlew connectedDebugAndroidTest

  real-device-tests:
    runs-on: self-hosted
    steps:
      - name: Capture screenshots
        run: ./scripts/real-device/capture-screenshots.sh
```

### 代码示例

```kotlin
// Epic1IntegrationTest.kt - 使用 Robolectric
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Epic1IntegrationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun letterFlyInAnimation_displaysCorrectly() {
        composeTestRule.setContent {
            MaterialTheme {
                LetterFlyInAnimation(
                    targetWord = "LOOK",
                    userAnswer = "LOOK"
                )
            }
        }

        // 验证组件树
        val tree = composeTestRule.onRoot().printToString()
        assert(tree.isNotEmpty())
    }
}
```

---

## 后果

### 正面影响
- ✅ **兼容性**: 与 JDK 17 和 JaCoCo 完全兼容
- ✅ **真机验证**: 实际设备渲染，发现真实 BUG
- ✅ **灵活性强**: ADB 脚本可定制任何测试场景
- ✅ **成本为零**: 不需要额外的 CI 资源（真机测试除外）
- ✅ **快速反馈**: Compose Testing 提供快速的组件级验证

### 负面影响
- ⚠️ **速度较慢**: 真机截图比 Paparazzi 慢
- ⚠️ **手动验证**: 截图对比需要人工或额外工具
- ⚠️ **设备依赖**: 真机测试需要物理设备或自托管 runner
- ⚠️ **维护成本**: ADB 脚本需要随 UI 变化更新

### 引入的风险
- 🟡 **中等风险**: 截图对比未自动化，可能遗漏细微变化
- 🟢 **低风险**: Compose Testing 稳定，Robolectric 4.13 大部分测试通过

### 真机 vs 模拟器
| 场景 | 模拟器 | 真机 | 一致性 |
|------|--------|------|--------|
| 组件布局 | ✅ | ✅ | ✅ 一致 |
| 文字渲染 | ✅ | ✅ | ⚠️ 字体差异 |
| 性能测试 | ⚠️ 仅供参考 | ✅ 准确 | ❌ 不一致 |
| 截图测试 | ✅ 快速 | ✅ 真实 | ❌ 像素差异 |

---

## 替代方案

### 方案 A: Paparazzi
**描述**: 使用 CashApp Paparazzi 进行 JVM 截图测试

**优点**:
- 秒级执行
- 无设备依赖
- CI 友好

**缺点**:
- ❌ JDK 17 不兼容
- ❌ 资源加载失败
- ❌ 与 JaCoCo 冲突

**为什么不选择**: 技术障碍无法在合理时间内解决，需要等待上游修复或降级 JDK

### 方案 B: Roborazzi
**描述**: 使用 Roborazzi（Compose 版 Paparazzi）

**优点**:
- Compose 原生支持
- 活跃社区

**缺点**:
- ❌ 预期有相同的 JDK/资源问题
- ❌ 未经验证

**为什么不选择**: 与 Paparazzi 有类似技术栈，预期相同问题

### 方案 C: Firebase Test Lab
**描述**: 使用 Google Firebase Test Lab 进行云端设备测试

**优点**:
- 真实设备
- 多厂商覆盖
- 自动化截图

**缺点**:
- 💰 需要付费（超出免费额度）
- ⏱️ 网络传输慢
- 🔑 账号配置复杂

**为什么不选择**: 超出预算，技术复杂度高

### 方案 D: Shot
**描述**: 使用 Shot 进行真机截图测试

**优点**:
- 真实设备渲染
- Gradle 插件集成

**缺点**:
- ⚠️ 需要连接设备
- ⚠️ CI/CD 集成复杂

**为什么不选择**: 与 ADB 脚本方案功能重复，直接使用 ADB 更灵活

---

## 实施计划

### 📋 Phase 1: 基础设施（已完成 - Epic #12）
- [x] Robolectric 4.13 升级
- [x] Compose Testing 框架就绪
- [x] ADB 截图脚本创建（5 个场景）
- [x] GitHub Actions 工作流配置

### 📋 Phase 2: 扩展测试覆盖（未来）
- [ ] 增加更多测试场景
- [ ] 设备矩阵扩展（5+ 设备）
- [ ] 自动化截图对比工具
- [ ] 性能基准测试

### 📋 Phase 3: 优化与自动化（未来）
- [ ] 截图差异自动检测
- [ ] 回归测试自动化
- [ ] CI/CD 完全集成真机测试

---

## 技术债务

### 已知债务
1. **Paparazzi 集成**
   - **影响**: 无法进行快速 JVM 截图测试
   - **优先级**: P2
   - **预估修复**: 等待 Paparazzi JDK 17 完全支持（预计 2-3 个季度）

2. **Robolectric Compose 测试**
   - **影响**: 15 个集成测试失败（ComponentActivity resolution error）
   - **优先级**: P1
   - **预估修复**: Task #10 已启动，预计 2-3 小时

3. **截图自动对比**
   - **影响**: 需要人工验证截图差异
   - **优先级**: P2
   - **预估修复**: 集成图像对比工具（如 PixelMatch），4-6 小时

---

## 相关文档

- [Epic #12 完成报告](../reports/testing/EPIC12_COMPLETION_REPORT.md)
- [Epic #12 技术债务](../planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md)
- [Paparazzi 替代方案](../reports/quality/PAPARAZZI_ALTERNATIVES.md)
- [测试策略](../testing/strategy/TEST_STRATEGY.md)
- [ADR 001: Service Locator](./001-use-service-locator.md)
- [ADR 002: Hilt Compatibility](./002-hilt-compatibility.md)

---

## 经验教训

### 技术层面
1. **JDK 版本选择很重要**: 新工具可能对 JDK 17+ 支持不完善
2. **工具兼容性验证**: 在大规模采用前需要完整 POC
3. **备选方案价值**: ADB 脚本作为备选方案挽救了 Epic #12

### 流程层面
1. **技术债务记录**: 完整记录决策过程和原因
2. **务实优先**: 在技术障碍面前，选择可行方案而非完美方案
3. **文档同步**: 决策文档应与实施同步更新

---

**审核者**: team-lead
**生效日期**: 2026-03-08
**下次审查**: 2026-06-08（或 JDK 约束变化时）
