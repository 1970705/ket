# 测试文档

本目录包含 Wordland 项目的测试策略、方法论和检查清单。

## 📑 文档分类

### 🎯 测试策略 ([strategy/](strategy/))
测试策略和覆盖率目标。

- [测试策略](strategy/TEST_STRATEGY.md) - 测试金字塔和 FIRST 原则
- [测试覆盖率报告](strategy/TEST_COVERAGE_REPORT.md) - 覆盖率分析和目标

### 📚 测试方法 ([methodology/](methodology/))
测试方法论和实践指南。

- [测试指南](methodology/TESTING_GUIDE.md) - 综合测试指南
- [TDD 指南](methodology/TDD_GUIDE.md) - 测试驱动开发
- [测试用例](methodology/TEST_CASES.md) - 测试用例设计

### ✅ 检查清单 ([checklists/](checklists/))
测试检查清单和验证步骤。

- [发布测试检查清单](checklists/RELEASE_TESTING_CHECKLIST.md) - 发布前验证
- [教育集成测试验收标准](EDUCATIONAL_INTEGRATION_TESTING_CRITERIA.md) - 教育有效性验收标准

## 🎯 测试金字塔

Wordland 采用经典测试金字塔：

```
        /\
       /UI\       10% - UI 测试
      /____\
     /      \
    /集成测试 \    20% - 集成测试
   /__________\
  /            \
 /   单元测试    \  70% - 单元测试
/________________\
```

### 当前状态
- ✅ 单元测试: 500 个测试，100% 通过率
- 📊 覆盖率: ~12% 指令覆盖率（目标 80%）
- 🎯 优先级: UI Screens (0%), UI Components (~2%)

## 📋 测试类型

### 1. 单元测试 (70%)
- **框架**: JUnit 5 + MockK
- **范围**: UseCase, ViewModel, Repository, Algorithm
- **目标**: >80% 覆盖率
- **位置**: `app/src/test/`

### 2. 集成测试 (20%)
- **框架**: Hilt + Room In-Memory
- **范围**: 数据层集成，依赖注入
- **目标**: 关键流程覆盖
- **位置**: `app/src/test/`

### 3. UI 测试 (10%)
- **框架**: Compose Testing
- **范围**: 关键用户流程
- **目标**: 核心功能验证
- **位置**: `app/src/androidTest/`

### 4. 真机测试 (Critical)
- **范围**: 首次启动测试，完整功能验证
- **目标**: 0 bugs, 0 crashes
- **报告**: [真机测试报告](../reports/testing/REAL_DEVICE_TEST_REPORT.md)

## 🎯 质量门禁

### P0 - 必须通过（阻塞发布）
- ✅ 单元测试全部通过
- ✅ 真机首次启动测试通过（CRITICAL）
- ✅ logcat 无 ERROR/CRASH
- ✅ 数据库初始化验证通过
- ✅ Level 1 状态为 UNLOCKED

### P1 - 应该通过（高质量标准）
- ✅ 测试覆盖率 ≥ 80%
- ✅ 静态分析通过（Detekt, KtLint）
- ✅ 真机测试 ≥ 2 台设备
- ✅ 性能测试通过（启动时间 < 3s）

### P2 - 最好具备（持续改进）
- ✅ TDD 开发模式（50% 新功能）
- ✅ 自动化质量门禁
- ✅ 完整测试文档

## 📖 测试实践

### FIRST 原则
- **F**ast - 测试应该快速运行
- **I**ndependent - 测试应该独立
- **R**epeatable - 测试应该可重复
- **S**elf-Validating - 测试应该自验证
- **T**imely - 测试应该及时编写

### Given-When-Then 模式
```kotlin
@Test
fun `should return correct answer when given valid input`() {
    // Given - 设置测试环境
    val question = createQuestion()

    // When - 执行被测试的操作
    val result = submitAnswer(question, "apple")

    // Then - 验证结果
    assertTrue(result.isCorrect)
}
```

## 🔧 测试工具

### 单元测试
- **JUnit 5** - 测试框架
- **MockK** - Mock 框架
- **JaCoCo** - 覆盖率工具

### 集成测试
- **Hilt** - 依赖注入
- **Room In-Memory** - 内存数据库

### UI 测试
- **Compose Testing** - Compose UI 测试
- **ActivityScenario** - Activity 测试

### 静态分析
- **Detekt** - 代码质量检查
- **KtLint** - 代码格式检查

### CI/CD
- **GitHub Actions** - 持续集成
- **Gradle** - 构建工具

## 📊 测试报告

### 覆盖率报告
- [测试覆盖率报告](strategy/TEST_COVERAGE_REPORT.md) - 详细覆盖率分析
- [JaCoCo 报告](../../app/build/reports/jacoco/jacocoTestReport/html/index.html) - HTML 报告

### 测试执行报告
- [真机测试报告](../reports/testing/REAL_DEVICE_TEST_REPORT.md) - 真机测试结果
- [集成测试报告](../reports/testing/REAL_DEVICE_INTEGRATION_TEST_REPORT.md) - 集成测试结果
- [性能测试报告](../reports/performance/PERFORMANCE_OPTIMIZATION_REPORT.md) - 性能测试结果

## 🚀 快速开始

### 运行所有测试
```bash
./gradlew test
```

### 运行特定测试
```bash
./gradlew test --tests HintGeneratorTest
```

### 运行带覆盖率的测试
```bash
./gradlew test jacocoTestReport
```

### 查看测试结果
```bash
# HTML 报告
open app/build/reports/tests/testDebugUnitTest/index.html

# 覆盖率报告
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## 📚 相关文档

- [测试策略](strategy/TEST_STRATEGY.md) - 详细测试策略
- [测试指南](methodology/TESTING_GUIDE.md) - 综合测试指南
- [TDD 指南](methodology/TDD_GUIDE.md) - 测试驱动开发
- [设备测试指南](../guides/testing/DEVICE_TESTING_GUIDE.md) - 真机测试指南
- [CI/CD 配置](../guides/development/CI_CD_SETUP.md) - 持续集成配置

## 🔗 外部资源

- [JUnit 5 文档](https://junit.org/junit5/docs/current/user-guide/)
- [MockK 文档](https://mockk.io/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)

---

**最后更新**: 2026-02-18
**维护者**: Wordland 开发团队
**当前覆盖率**: ~12% (目标: 80%)
