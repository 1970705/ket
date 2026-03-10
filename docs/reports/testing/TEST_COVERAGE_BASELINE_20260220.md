# 测试覆盖率基线报告

**日期**: 2026-02-20
**测试总数**: 1,314个 (100%通过)
**覆盖率工具**: JaCoCo 0.8.8
**报告人**: android-test-engineer

---

## 📊 整体覆盖率

| 指标 | 当前值 | 目标值 | 差距 | 状态 |
|------|--------|--------|------|------|
| **Instruction Coverage (指令覆盖率)** | **21%** | ≥80% | -59% | ❌ 未达成 |
| **Branch Coverage (分支覆盖率)** | **12%** | ≥70% | -58% | ❌ 未达成 |
| **Line Coverage (行覆盖率)** | **36%** (9,675/15,169) | ≥80% | -44% | ❌ 未达成 |
| **Method Coverage (方法覆盖率)** | **36%** (1,897/2,982) | ≥80% | -44% | ❌ 未达成 |
| **Class Coverage (类覆盖率)** | **27%** (654/899) | ≥90% | -63% | ❌ 未达成 |

---

## 🌟 高覆盖率模块 (>80%)

| 模块 | 指令覆盖率 | 分支覆盖率 | 文件数 |
|------|-----------|-----------|--------|
| `data.converter` | 100% | 83% | 1 |
| `domain.behavior` | 99% | 87% | 2 |
| `domain.performance` | 96% | 87% | 3 |
| `domain.combo` | 94% | 85% | 1 |
| `domain.hint` | 94% | 78% | 3 |
| `ui.viewmodel` | 88% | 57% | 8 |
| `domain.model` | 85% | 77% | 6 |
| `domain.algorithm` | 84% | 62% | 3 |
| `usecase.usecases` | 80% | 74% | 12 |

---

## ⚠️ 中等覆盖率模块 (20-79%)

| 模块 | 指令覆盖率 | 分支覆盖率 | 文件数 |
|------|-----------|-----------|--------|
| `domain.model.statistics` | 48% | 34% | 6 |
| `data.seed` | 49% | 50% | 11 |
| `data.repository` | 38% | 47% | 9 |
| `data.dao` | 38% | 50% | 7 |
| `platform` | 40% | 18% | 3 |
| `ui.uistate` | 36% | 50% | 4 |
| `data.entity` | 14% | 0% | 8 |
| `navigation` | 8% | 4% | 3 |
| `ui.theme` | 8% | 0% | 2 |
| `data.assets` | 30% | 0% | 4 |

---

## 🔴 待改进模块 (0%)

| 模块 | 指令覆盖率 | 分支覆盖率 | 文件数 | 优先级 |
|------|-----------|-----------|--------|--------|
| `ui.screens` | 0% | 0% | 6 | 🔴 P0 |
| `ui.components` | 0% | 0% | 5 | 🔴 P0 |
| `media` | 0% | 0% | 2 | 🟡 P1 |
| `domain.achievement` | 0% | 0% | 2 | 🟡 P1 |
| `performance` | 0% | 0% | 2 | 🟢 P2 |
| `data.database` | 0% | 0% | 1 | 🟢 P2 |
| `ui` | 0% | 0% | 1 | 🟢 P2 |

---

## 📈 按层分析

### Domain层: 77% (优秀)

| 子模块 | 指令覆盖率 | 分支覆盖率 |
|--------|-----------|-----------|
| domain.behavior | 99% | 87% |
| domain.performance | 96% | 87% |
| domain.combo | 94% | 85% |
| domain.hint | 94% | 78% |
| domain.model | 85% | 77% |
| domain.algorithm | 84% | 62% |
| usecase.usecases | 80% | 74% |
| domain.model.statistics | 48% | 34% |
| domain.achievement | 0% | 0% |

### Data层: 15% (需改进)

| 子模块 | 指令覆盖率 | 分支覆盖率 |
|--------|-----------|-----------|
| data.converter | 100% | 83% |
| data.seed | 49% | 50% |
| data.repository | 38% | 47% |
| data.dao | 38% | 50% |
| data.entity | 14% | 0% |
| data.assets | 30% | 0% |
| data.database | 0% | 0% |

### UI层: 5% (严重不足)

| 子模块 | 指令覆盖率 | 分支覆盖率 |
|--------|-----------|-----------|
| ui.viewmodel | 88% | 57% |
| ui.uistate | 36% | 50% |
| ui.theme | 8% | 0% |
| ui.screens | 0% | 0% |
| ui.components | 0% | 0% |
| ui | 0% | 0% |

---

## 📋 数据验证

### 测试执行命令

```bash
./gradlew clean
./gradlew testDebugUnitTest jacocoTestReport
```

### 测试结果

```
1314 tests completed, 0 failed, 7 skipped
BUILD SUCCESSFUL in 49s
```

### 报告位置

- HTML报告: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- XML报告: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`

---

## 🔍 根因分析

### 数据不一致问题

**问题**: 文档中存在两个不同的覆盖率数据
- `docs/requirements/README.md`: **21%** ✅ 正确
- `docs/testing/strategy/TEST_COVERAGE_REPORT.md`: 84.6% ❌ 错误

**根因**:
- 21% 是真实的项目整体指令覆盖率（通过 `./gradlew jacocoTestReport` 验证）
- 84.6% 可能是单个模块（如domain.behavior的99%）或部分运行的错误数据
- 可能来源于对特定视图或子模块的误解

**解决方案**: 统一所有文档使用21%作为权威数据

---

## 🎯 改进建议

### 短期 (1-2周)

1. **补充UI层测试** (P0优先级)
   - `ui.screens`: 0% → 60% (6个文件)
   - `ui.components`: 0% → 50% (5个文件)

2. **补充Data层测试** (P0优先级)
   - `data.dao`: 38% → 80%
   - `data.entity`: 14% → 70%

### 中期 (3-4周)

1. **完善Domain层**
   - `domain.model.statistics`: 48% → 85%
   - `domain.achievement`: 0% → 80%

2. **集成测试**
   - UseCase与Repository集成
   - 数据库迁移测试

---

## 📞 联系

**覆盖率问题**: 请提交GitHub Issue
**报告生成**: 运行 `./gradlew testDebugUnitTest jacocoTestReport`
**下次更新**: 2026-02-27

---

**报告生成时间**: 2026-02-20
**JaCoCo版本**: 0.8.8
**Gradle版本**: 8.x
