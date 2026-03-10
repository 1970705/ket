# Task 11.3: P2 轻微问题文件重构计划

**Epic**: #11 测试代码质量重构
**优先级**: P2
**预计时间**: 15-22 小时

---

## 📊 目标文件分析 (13 个文件，500-600 行)

### UseCase Tests (6 个)

| 文件 | 行数 | 测试数 | 拆分策略 |
|------|------|--------|----------|
| CompleteTutorialWordUseCaseTest.kt | 576 | 29 | 按功能拆分 2 个文件 |
| UpdateGameStateUseCaseTest.kt | 570 | 26 | 按功能拆分 2 个文件 |
| GetGameHistoryUseCaseTest.kt | 569 | 24 | 按功能拆分 2 个文件 |
| OpenFirstChestUseCaseTest.kt | 559 | ~22 | 按功能拆分 2 个文件 |
| GetWordPairsUseCaseTest.kt | 528 | ~20 | 优化代码 + 提取 helper |
| GenerateQuickJudgeQuestionsUseCaseTest.kt | 508 | ~18 | 优化代码 + 提取 helper |

### Domain Tests (2 个)

| 文件 | 行数 | 测试数 | 拆分策略 |
|------|------|--------|----------|
| Sm2AlgorithmTest.kt | 571 | ~24 | 按场景拆分 2 个文件 |
| BehaviorAnalyzerTest.kt | 503 | ~18 | 优化代码 + 提取 helper |

### 其他 (5 个)

| 文件 | 行数 | 测试数 | 拆分策略 |
|------|------|--------|----------|
| ProgressRepositoryImplTest.kt | 548 | ~22 | 按功能拆分 2 个文件 |
| MultipleChoiceQuestionTest.kt | 546 | ~20 | 优化代码 |
| HomeScreenTest.kt | 563 | ~24 | 按功能拆分 2 个文件 |
| IslandMapScreenTest.kt | 485 | ~18 | 仅优化（已接近目标） |
| WorldMapViewModelTest.kt | 569 | ~22 | 按功能拆分 2 个文件 |

---

## 🎯 重构策略

### 策略 1: 按功能拆分（适用于 8 个文件）

**示例**：`UpdateGameStateUseCaseTest.kt` (570行)

```
UpdateGameStateUseCaseTest.kt
├── UpdateGameStateUseCaseGameFlowTest.kt      # 游戏流程 (启动、暂停、继续、退出)
├── UpdateGameStateUseCaseSelectionTest.kt     # 气泡选择和匹配
└── UpdateGameStateUseCaseResetTest.kt         # 重置和重新开始
```

**示例**：`GetGameHistoryUseCaseTest.kt` (569行)

```
GetGameHistoryUseCaseTest.kt
├── GetGameHistoryUseCaseQueryTest.kt          # 查询操作 (getByXxx, getRecent)
├── GetGameHistoryUseCaseStatisticsTest.kt     # 统计操作 (getCount, getTotalScore)
└── GetGameHistoryUseCaseMaintenanceTest.kt    # 维护操作 (deleteOldHistory)
```

### 策略 2: 优化和精简（适用于 5 个文件）

**优化技术**：
1. 提取测试数据构建器
2. 提取通用断言方法
3. 合并相似测试（使用参数化测试）
4. 删除冗余测试

---

## 📋 执行计划

### 第 1 批：UseCase Tests (6-8h)

1. **CompleteTutorialWordUseCaseTest.kt** → 拆分 2 个文件 (1.5h)
2. **UpdateGameStateUseCaseTest.kt** → 拆分 2 个文件 (1.5h)
3. **GetGameHistoryUseCaseTest.kt** → 拆分 2 个文件 (1.5h)
4. **OpenFirstChestUseCaseTest.kt** → 拆分 2 个文件 (1h)
5. **GetWordPairsUseCaseTest.kt** → 优化 (0.5h)
6. **GenerateQuickJudgeQuestionsUseCaseTest.kt** → 优化 (0.5h)

### 第 2 批：Domain Tests (3-4h)

1. **Sm2AlgorithmTest.kt** → 拆分 2 个文件 (2h)
2. **BehaviorAnalyzerTest.kt** → 优化 (1h)

### 第 3 批：其他 Tests (6-8h)

1. **ProgressRepositoryImplTest.kt** → 拆分 2 个文件 (1.5h)
2. **MultipleChoiceQuestionTest.kt** → 优化 (1h)
3. **HomeScreenTest.kt** → 拆分 2 个文件 (1.5h)
4. **IslandMapScreenTest.kt** → 优化 (0.5h)
5. **WorldMapViewModelTest.kt** → 拆分 2 个文件 (1.5h)

---

## ✅ 验收标准

- [ ] 所有文件 < 400 行
- [ ] 测试覆盖率保持不变
- [ ] 所有测试通过
- [ ] 代码审查通过

---

## 📝 重构模板

### 拆分后的文件结构

```kotlin
// OriginalFeatureTest.kt (基础功能和设置)
class OriginalFeatureTest {
    // 共享的 setup
    // 共享的 test data builders
}

// OriginalFeatureActionTest.kt (主要操作测试)
class OriginalFeatureActionTest : OriginalFeatureTest() {
    // 测试主要操作
}

// OriginalFeatureQueryTest.kt (查询操作测试)
class OriginalFeatureQueryTest : OriginalFeatureTest() {
    // 测试查询操作
}

// OriginalFeatureEdgeCasesTest.kt (边界情况测试)
class OriginalFeatureEdgeCasesTest : OriginalFeatureTest() {
    // 测试边界情况
}
```

---

**创建日期**: 2026-03-02
**负责人**: android-engineer
**状态**: 计划中
