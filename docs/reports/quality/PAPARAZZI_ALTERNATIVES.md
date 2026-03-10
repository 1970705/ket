# Paparazzi 替代方案文档

**创建日期**: 2026-03-08
**作者**: android-architect
**状态**: ✅ 完成
**相关 Epic**: #12 Real Device UI Automation Testing

---

## 一、决策摘要

**决策**: 放弃 Paparazzi，采用 **Compose Testing + ADB Scripts** 混合方案

**理由**:
- Paparazzi 与 JDK 17 不兼容
- 资源加载失败无法解决
- JaCoCo 代码覆盖工具冲突

**替代方案**:
- Compose Testing (createComposeRule) - 组件逻辑验证
- ADB Screenshot Scripts - 真机截图捕获
- Manual Visual QA - 人工视觉验证

---

## 二、Paparazzi 评估过程

### 2.1 初始期望

| 特性 | 期望 |
|------|------|
| 执行速度 | 秒级完成 |
| 设备依赖 | 无需模拟器/真机 |
| CI 友好度 | 完美支持 GitHub Actions |
| 截图质量 | 准确反映 UI |

### 2.2 尝试的解决方案

| 尝试 | 结果 | 详情 |
|------|------|------|
| 升级 Paparazzi → v1.3.3 | ❌ 失败 | 仍有资源加载问题 |
| 配置 Jetifier ignore list | ⚠️ 部分 | 构建通过，运行时失败 |
| 禁用 JaCoCo | ❌ 不可行 | 需要单独配置测试任务 |
| 切换到 Roborazzi | ⏭️ 跳过 | 预期相同问题 |

### 2.3 最终错误

```
java.lang.UnsupportedOperationException: class redefinition failed
    at java.lang.instrument.InstrumentationImpl.retransformClasses0(Native Method)

NoClassDefFoundError: Could not initialize class com.android.resources.ResourceType
    at app.cash.paparazzi.internal.DynamicResourceIdManager
```

---

## 三、替代方案详解

### 3.1 Compose Testing

**用途**: 组件逻辑验证和快速 UI 测试

**示例**:
```kotlin
class HintCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun hintCard_showsRemainingHints() {
        composeTestRule.setContent {
            HintCard(hintsRemaining = 3, onHintClick = {})
        }
        composeTestRule.onNodeWithText("3").assertExists()
    }
}
```

**优势**:
- ✅ JDK 17 兼容
- ✅ JaCoCo 覆盖率支持
- ✅ 快速执行
- ✅ 稳定可靠

**限制**:
- ⚠️ 无内置截图对比
- ⚠️ 需要手动验证视觉变化

### 3.2 ADB Screenshot Scripts

**用途**: 真机截图捕获和对比

**核心脚本**:
```bash
#!/bin/bash
# scripts/real-device/capture-screenshots.sh

capture_screenshot() {
    local device_id=$1
    local output_path=$2

    adb -s "$device_id" shell screencap -p /sdcard/temp.png
    adb -s "$device_id" pull /sdcard/temp.png "$output_path"
}

# 使用场景
capture_screenshot "$DEVICE_ID" "reports/home.png"
```

**测试场景** (Epic #12 实现):
1. `app_launch` - 应用启动
2. `onboarding` - 新手引导
3. `learning_flow` - 学习流程
4. `match_game` - 单词配对
5. `progress_save` - 进度保存

**优势**:
- ✅ 真实设备渲染
- ✅ 发现真机 BUG
- ✅ 灵活可定制
- ✅ 完全控制

**限制**:
- ⚠️ 需要连接设备
- ⚠️ 手动截图对比
- ⚠️ CI 需要自托管 runner

### 3.3 Manual Visual QA

**用途**: 人工视觉验证

**流程**:
1. 执行 ADB 脚本捕获截图
2. 对比基准截图
3. 人工审查差异
4. 更新基准或修复 BUG

**工具**:
- `docs/testing/checklists/VISUAL_QA_CHECKLIST.md`
- 真机测试报告模板

---

## 四、方案对比

| 维度 | Paparazzi | Compose + ADB | 评价 |
|------|-----------|---------------|------|
| **执行速度** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ADB 更慢 |
| **设备依赖** | 无 | 真机需要 | ADB 需要设备 |
| **CI 友好** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 真机需自托管 |
| **截图准确性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 真机更准确 |
| **JDK 17 兼容** | ❌ | ✅ | 关键差异 |
| **JaCoCo 支持** | ❌ | ✅ | 关键差异 |
| **维护成本** | 低 | 中 | ADB 脚本维护 |

**结论**: 虽然速度较慢，但 **Compose + ADB** 方案是唯一可行的选择。

---

## 五、未来路径

### 5.1 短期（3 个月内）

**保持当前方案**:
- 继续使用 Compose Testing
- 扩展 ADB 脚本场景
- 完善文档和培训

**可能的改进**:
- 集成图像对比工具（如 PixelMatch）
- 自动化截图对比流程
- 增加 PR 评论集成

### 5.2 中期（6-12 个月）

**重新评估 Paparazzi**:
- 检查 Paparazzi JDK 17 支持状态
- 验证 JaCoCo 兼容性修复
- 进行新的 POC

**考虑的其他方案**:
- Firebase Test Lab（如果预算允许）
- 云端设备农场（如 BrowserStack）

### 5.3 长期（12 个月以上）

**理想状态**:
- 混合使用多种工具
- JVM 截图测试（如果 Paparazzi 可用）
- 真机自动化测试
- 完整的 CI/CD 集成

---

## 六、技术债务记录

### 债务 #1: Paparazzi 未集成

| 属性 | 值 |
|------|-----|
| **类型** | 工具链兼容性 |
| **优先级** | P2 |
| **影响** | 无法进行快速 JVM 截图测试 |
| **创建日期** | 2026-03-08 |
| **预计修复** | 2-3 个季度（等待上游修复） |
| **工作量** | 4-6 小时（重新集成） |

**触发重新评估条件**:
- Paparazzi 发布 JDK 17 完全支持版本
- JaCoCo 兼容性问题解决
- 项目 JDK 版本降级（不太可能）

### 债务 #2: 截图自动对比未实现

| 属性 | 值 |
|------|-----|
| **类型** | 自动化缺失 |
| **优先级** | P2 |
| **影响** | 需要人工验证截图差异 |
| **创建日期** | 2026-03-08 |
| **预计修复** | 4-6 小时 |
| **工作量** | 集成图像对比工具 |

---

## 七、给未来团队的建议

### 7.1 工具选择原则

1. **验证兼容性**: 在大规模采用前进行完整 POC
2. **检查依赖**: 确认与现有工具（JaCoCo, Detekt 等）的兼容性
3. **备选方案**: 始终准备备选方案

### 7.2 如果要重新尝试 Paparazzi

**前提条件**:
- [ ] Paparazzi 声称 JDK 17 完全支持
- [ ] JaCoCo 兼容性已验证
- [ ] 社区反馈积极

**验证步骤**:
1. 创建分支进行 POC
2. 测试关键场景
3. 验证 CI/CD 集成
4. 性能对比（速度、资源）

**决策标准**:
- 性能提升 > 50%
- 稳定性 > 95%
- 维护成本 < 当前方案

### 7.3 维护当前方案

**定期检查**:
- 每季度更新测试场景
- 每半年审查设备矩阵
- 每年评估工具生态

**文档更新**:
- 重大变更更新本文档
- 更新 ADR 003
- 通知团队成员

---

## 八、相关资源

### 内部文档
- [ADR 003: Screenshot Testing Approach](../../adr/003-screenshot-testing-approach.md)
- [Epic #12 技术债务](../../planning/epics/EPIC12_REAL_DEVICE_UI_AUTOMATION/TECH_DEBT.md)
- [测试策略](../../testing/strategy/TEST_STRATEGY.md)

### 外部资源
- [Paparazzi GitHub](https://github.com/cashapp/paparazzi)
- [Paparazzi Issue #384 - Java 17](https://github.com/cashapp/paparazzi/issues/384)
- [Roborazzi GitHub](https://github.com/sergio-sastre/Roborazzi)

---

**文档版本**: 1.0
**最后更新**: 2026-03-08
**维护者**: android-architect
