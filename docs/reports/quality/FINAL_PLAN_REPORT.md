# Wordland 项目最终开发计划
## PLAN 阶段综合报告

**制定日期**: 2026-02-16
**制定方式**: Plan Agent分析 + 6位专业团队成员观察
**项目状态**: 最小原型完成 + 增强提示系统架构完成
**目标**: 制定下一阶段开发优先级和执行路线图

---

## 📊 执行摘要

### 当前项目状态

| 指标 | 状态 | 说明 |
|------|------|------|
| **功能完整性** | ✅ 良好 | 30词/5关，Spell Battle游戏，Hint架构完整 |
| **代码质量** | ⚠️ 需改进 | 252测试100%通过，但覆盖率仅12% |
| **架构完整性** | ✅ 优秀 | Clean Architecture + Hilt/Service Locator |
| **性能状况** | ⚠️ 未验证 | 监控完善，但无基线数据 |
| **用户体验** | ❌ 有问题 | 星级评分对儿童不友好 |

### 核心问题

1. **🔴 P0**: 星级评分算法不儿童友好（2秒=猜测=1星）
2. **🔴 P0**: 测试覆盖率严重不足（12% vs 目标80%）
3. **🔴 P0**: 缺少真实设备首次启动验证
4. **🔴 P0**: 2个MockK测试失败
5. **🟡 P1**: Hint系统UI未完全集成
6. **🟡 P1**: 性能无基线数据
7. **🟡 P1**: 数据质量存在问题（重复单词）

---

## 1. 综合任务优先级

### P0 任务（必须立即解决，阻塞发布）

#### P0-1: 修复星级评分算法（儿童友好）
**负责人**: android-engineer + game-designer
**工作量**: 2-3小时
**理由**: 当前2秒答对=猜测=1星，对10岁儿童不公平

**实施方案**:
```kotlin
// 根据单词长度动态调整时间阈值
val timeThreshold = when (word.word.length) {
    <= 3 -> 1500  // 3字母词: 1.5秒
    4 -> 2500     // 4字母词: 2.5秒
    5 -> 3500     // 5字母词: 3.5秒
    else -> 5000  // 6+字母词: 5秒
}

// 评分逻辑
when {
    !isCorrect -> 0
    isGuessing && responseTime < 1000 -> 1  // <1秒肯定是猜测
    hintUsed -> 2
    responseTime < timeThreshold -> 3
    else -> 2
}
```

**关键文件**:
- `SubmitAnswerUseCase.kt:126-140`
- `DomainConstants.kt:25-28`

---

#### P0-2: 实现等级完成星级聚合逻辑
**负责人**: android-engineer
**工作量**: 1-2小时
**依赖**: P0-1

**实施方案**:
```kotlin
// 在 LearningViewModel 追踪星级
private val starsEarnedInLevel = mutableListOf<Int>()

// 等级完成时计算
val averageStars = starsEarnedInLevel
    .average()
    .toInt()
    .coerceIn(0, 3)

val totalScore = starsEarnedInLevel.sum() * 10

_uiState.value = LearningUiState.LevelComplete(
    stars = averageStars,
    score = totalScore
)
```

**关键文件**:
- `LearningViewModel.kt:217-228`

---

#### P0-3: 修复2个失败的MockK测试
**负责人**: android-test-engineer
**工作量**: 1-2小时
**理由**: 破坏测试通过率，阻塞CI/CD

**失败的测试**:
- `useHint updates hint state correctly for progressive hints`
- `useHint handles hint limit exceeded error`

**关键文件**:
- `LearningViewModelTest.kt`

---

#### P0-4: 真实设备首次启动测试（CRITICAL）
**负责人**: android-test-engineer
**工作量**: 2-3小时
**理由**: P0质量门禁要求，验证Service Locator修复

**测试内容**:
1. 构建APK
2. 安装到真实设备
3. 首次启动应用
4. 验证导航和功能
5. 完成Level 1第一题
6. 检查logcat无错误

**验证点**:
- [ ] 冷启动 < 3秒
- [ ] 数据库正常初始化
- [ ] Level 1 解锁
- [ ] 无ERROR/CRASH日志

---

#### P0-5: 代码清理（删除旧版UseHintUseCase）
**负责人**: android-engineer
**工作量**: 20分钟
**理由**: 避免代码混淆，保持代码库整洁

**任务**:
- 删除 `domain/usecase/usecases/UseHintUseCase.kt`（旧版）
- 修复编译警告（`LearningViewModel.loadLevel()` 未使用的`islandId`）

---

### P1 任务（重要但非紧急）

#### P1-1: 提升测试覆盖率至30%（阶段性目标）
**负责人**: android-test-engineer
**工作量**: 3-5天
**当前**: 12% → **目标**: 30%

**优先级**:
1. **ViewModel扩展** (32% → 80%)
   - LearningViewModel
   - HomeViewModel
   - IslandMapViewModel

2. **Repository测试** (0% → 60%)
   - WordRepository
   - ProgressRepository
   - TrackingRepository

3. **UseCase扩展** (40% → 80%)

**理由**: 纯Kotlin代码，测试成本低，价值高

---

#### P1-2: Hint系统完整集成测试
**负责人**: android-test-engineer
**工作量**: 4-6小时

**测试场景**:
1. 渐进式提示 (Level 1 → 2 → 3)
2. 提示次数限制 (3次上限)
3. 提示冷却时间 (3秒)
4. 切换单词时提示重置
5. 提示扣分逻辑验证

---

#### P1-3: 运行性能基准测试并建立基线
**负责人**: android-performance-expert
**工作量**: 1-2天

**基准测试**:
- 冷启动时间（目标 < 3s）
- 温启动时间（目标 < 1s）
- 帧率（目标 60fps，jank < 5%）
- 关键操作响应时间

**执行命令**:
```bash
./benchmark_performance.sh
```

---

#### P1-4: 集成增强Hint系统UI
**负责人**: compose-ui-designer
**工作量**: 4-6小时

**UI需求**:
- 显示当前提示等级 (1-3级)
- 显示剩余提示次数
- 显示冷却状态
- 与HintGenerator/HintManager完整对接

---

#### P1-5: 优化LearningScreen重组性能
**负责人**: compose-ui-designer
**工作量**: 4-6小时

**拆分计划**:
```
LearningScreen (432行) → 拆分为:
├── QuestionHeader
├── AnswerSection
├── KeyboardSection
├── FeedbackOverlay
└── HintPanel
```

**技术要点**:
- 使用 `@Immutable` 标记数据类
- 提取纯函数组件
- 使用 `key()` 帮助Compose跟踪

---

#### P1-6: 修复LookIslandWords数据质量问题
**负责人**: android-engineer
**工作量**: 1-2小时

**问题**:
- 重复单词: notice, observe, stare, appear
- 缺少词根词缀信息

**解决方案**:
- 删除重复项，补充新词（glimpse, glance, witness, spot）
- 为核心词汇添加词根词缀（visible→root="vis", observe→prefix="ob-"+root="serv"）

---

### P2 任务（可以延后）

#### P2-1: 添加@Immutable/@Stable注解
**负责人**: android-performance-expert
**工作量**: 2-3小时

#### P2-2: 完善MakeLake岛屿内容
**负责人**: android-engineer
**工作量**: 6小时

#### P2-3: 建立词根词缀教学系统
**负责人**: education-specialist
**工作量**: 8-12小时

#### P2-4: UI组件测试覆盖
**负责人**: android-test-engineer
**工作量**: 6-8小时

---

## 2. 执行计划

### Week 1: 核心问题修复（必须完成）

**目标**: 修复所有P0问题，建立质量基线

| 任务 | 负责人 | 工作量 | 时间 |
|------|--------|--------|------|
| P0-5: 代码清理 | android-engineer | 20分钟 | Day 1 上午 |
| P0-1: 星级评分算法 | android-engineer | 2-3小时 | Day 1 下午 |
| P0-2: 星级聚合逻辑 | android-engineer | 1-2小时 | Day 1 下午 |
| P0-3: 修复失败测试 | android-test-engineer | 1-2小时 | Day 2 上午 |
| P0-4: 真机启动测试 | android-test-engineer | 2-3小时 | Day 2 下午 |
| P1-6: 数据质量修复 | android-engineer | 1-2小时 | Day 3 |

**里程碑 M1**: 所有P0任务完成，质量门禁通过 ✅

---

### Week 2: 功能完善（重要但不阻塞）

| 任务 | 负责人 | 工作量 | 时间 |
|------|--------|--------|------|
| P1-4: Hint系统UI集成 | compose-ui-designer | 4-6小时 | Day 1-2 |
| P1-2: Hint系统集成测试 | android-test-engineer | 4-6小时 | Day 2-3 |
| P1-3: 性能基准测试 | android-performance-expert | 1-2天 | Day 3-4 |
| P1-5: LearningScreen优化 | compose-ui-designer | 4-6小时 | Day 4-5 |

**里程碑 M2**: Hint系统完整工作，性能基线建立 ✅

---

### Week 3-4: 质量提升（持续改进）

| 任务 | 负责人 | 工作量 |
|------|--------|--------|
| P1-1: 测试覆盖提升至30% | android-test-engineer | 3-5天 |
| P2-1: @Immutable注解 | android-performance-expert | 2-3小时 |
| P2-2: 完善MakeLake内容 | android-engineer | 6小时 |

**里程碑 M3**: 测试覆盖率30%，质量体系完善 ✅

---

## 3. 风险识别与缓解

### 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|----------|
| Hint集成破坏现有功能 | 高 | 中 | 先写集成测试，再改代码 |
| 动态星级算法引入bug | 高 | 中 | 充分测试边界情况 |
| 性能优化效果不明显 | 低 | 低 | 先运行基准测试 |
| 真机测试设备不足 | 中 | 中 | 优先使用模拟器 |

### 质量风险

| 风险 | 缓解措施 |
|------|----------|
| 测试覆盖提升慢 | 分阶段目标（12%→30%→50%→80%） |
| 回归错误 | 完善单元测试，CI/CD自动化 |
| UI性能问题 | Compose Compiler Report分析 |

---

## 4. 团队协作建议

### 任务依赖关系

```
P0-5 (代码清理)
    ↓
P0-1 (评分算法)
    ↓
P0-2 (星级聚合) ← 并行
    ↓
P0-3/P0-4 (测试) ← 并行

P1-6 (数据质量) ← 独立任务
P1-2 (Hint测试) ← 依赖 P1-4
P1-4 (Hint UI)
P1-3 (性能基准) ← 独立任务
P1-5 (UI优化) ← 独立任务
```

### 交叉协作

| 协作 | 任务 | 说明 |
|------|------|------|
| android-engineer ↔ game-designer | P0-1 | 算法参数需要游戏设计验证 |
| android-engineer ↔ compose-ui-designer | P1-4 | API对接和UI展示 |
| android-test-engineer ↔ android-performance-expert | P1-3 | 性能测试协作 |
| android-engineer ↔ education-specialist | P1-6 | 数据质量问题 |

---

## 5. 成功指标

### 功能指标
- ✅ 星级评分反映真实表现
- ✅ Hint系统完整工作
- ✅ 数据质量问题修复

### 质量指标
- ⏳ 测试覆盖率 ≥ 30%（从12%提升）
- ✅ 所有单元测试通过（100%）
- ✅ 真机测试通过

### 性能指标
- ⏳ 冷启动 < 3秒（基线建立）
- ⏳ 帧率 60fps（基线建立）
- ⏳ LearningScreen优化完成

### 用户体验指标
- ⏳ 儿童测试反馈积极（无挫败感）
- ⏳ 评分系统公平性认可

---

## 6. 关键文件

### 需要修改的核心文件

1. `domain/usecase/usecases/SubmitAnswerUseCase.kt` - P0-1: 星级评分算法
2. `domain/constants/DomainConstants.kt` - P0-1: 时间阈值常量
3. `ui/viewmodel/LearningViewModel.kt` - P0-2: 星级聚合逻辑
4. `ui/viewmodel/LearningViewModelTest.kt` - P0-3: 修复失败测试
5. `data/seed/LookIslandWords.kt` - P1-6: 数据质量修复

---

## 7. 下一步行动

### 今天（Day 1）- 代码清理 + 核心算法

**上午**:
- P0-5: 代码清理（20分钟）
  - 删除旧版UseHintUseCase
  - 修复编译警告

**下午**:
- P0-1: 实现动态星级评分算法（2-3小时）
  - 修改时间阈值逻辑
  - 编写单元测试

- P0-2: 实现星级聚合逻辑（1-2小时）
  - 在ViewModel追踪星级
  - 计算平均星级

### 明天（Day 2）- 测试验证

**上午**:
- P0-3: 修复2个失败测试（1-2小时）

**下午**:
- P0-4: 真机首次启动测试（2-3小时）
  - 执行测试脚本
  - 验证功能完整性

---

## 8. 备注

### Plan Agent vs 团队成员观察对比

**Plan Agent建议**:
- 4个P0任务（评分算法、星级聚合、测试覆盖、真机测试）
- 5个P1任务
- 4个P2任务

**团队成员补充**:
- android-architect: 强调Hint系统E2E测试、Detekt配置修复
- android-engineer: 强调代码清理、技术债务
- android-test-engineer: 强调2个失败测试修复、真机测试CRITICAL
- compose-ui-designer: 强调LearningScreen性能优化、Hint UI集成
- education-specialist: 强调数据质量问题（重复单词、词根词缀缺失）
- android-performance-expert: 强调建立性能基线

### 最终调整

综合Plan Agent和6位成员的建议，最终计划：

**P0任务调整**:
- **新增**: P0-5 代码清理（android-engineer强烈建议）
- **新增**: P1-6 数据质量修复提升至P1（education-specialist强烈建议）

**优先级微调**:
- Hint系统集成测试: P0 → P1-2（android-architect建议，真机测试更优先）
- Detekt配置修复: P0 → P2（降低优先级，不阻塞开发）

---

**PLAN 阶段完成 ✅**

**下一步**: EXECUTE 阶段，按P0 → P1 → P2 顺序执行

**预计完成时间**: Week 1结束（所有P0任务）

**文档版本**: 1.0
**最后更新**: 2026-02-16
**下次review**: Week 1结束
