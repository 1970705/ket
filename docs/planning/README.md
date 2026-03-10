# Planning 目录使用指南

**Last Updated**: 2026-02-28
**Version**: 2.0 (Reorganized)

---

## 📚 目录结构

`docs/planning/` 采用**功能 × 时间**二维矩阵结构，清晰组织 Epic、Sprint、Iteration 和 Daily 文档。

```
docs/planning/
├── INDEX.md                    # 总索引
├── MATRIX.md                   # Epic × Sprint 矩阵
├── README.md                   # 本文档
│
├── epics/                      # 【功能维度】Epic 文档
├── phases/                     # 【时间维度】Phase & Sprint 文档
├── workflows/                  # 【执行维度】E-P-E-R 工作流
├── daily/                      # 【记录维度】每日记录
├── roadmaps/                   # 【专项】路线图
├── templates/                  # 【模板】文档模板
└── _archive/                   # 【归档】旧文件
```

---

## 🎯 核心概念

### Epic（功能维度）
**定义**: 产品功能模块
**示例**: Epic #4 Hint System, Epic #9 Word Match Game
**位置**: `epics/EPIC#_NAME/`
**特点**: 跨越多个 Sprint，专注于产品功能

### Sprint（时间维度）
**定义**: 2-4 周的执行周期
**示例**: Sprint 1.1, Sprint 2.1
**位置**: `phases/PHASE#_NAME/SPRINT_X_Y/`
**特点**: 时间盒管理，可包含多个 Epic 的任务

### Phase（阶段维度）
**定义**: 多个 Sprint 组成的更大阶段
**示例**: Phase 1 Foundation, Phase 2 Integration
**位置**: `phases/PHASE#_NAME/`
**特点**: 战略层面的时间划分

### Iteration（执行维度）
**定义**: E-P-E-R 框架的一次完整迭代
**示例**: EPER Iteration 1, EPER Iteration 2
**位置**: `workflows/eper_iterations/EPER_ITERATION_#/`
**特点**: 执行方法论层面的循环

### Daily（记录维度）
**定义**: 每日工作记录
**示例**: 2026-02-25.md
**位置**: `daily/YYYY/MM_MMMM/YYYY-MM-DD.md`
**特点**: 工作日志和总结

---

## 📝 统一命名规则

### 格式
```
[ENTITY]_[ACTION]_[YYYY-MM-DD].md
```

### ENTITY（实体）
- `EPIC#` - Epic 编号（如 EPIC4, EPIC9）
- `PHASE#` - Phase 编号（如 PHASE1, PHASE2）
- `SPRINT_X_Y` - Sprint 编号（如 SPRINT_1_1, SPRINT_2_1）
- `EPER_ITERATION#` - E-P-E-R 迭代编号

### ACTION（动作）
- `PLAN` - 计划文档
- `STATUS` - 状态更新（需日期）
- `TASKS` - 任务列表
- `PROGRESS` - 进度报告
- `COMPLETION_REPORT` - 完成报告
- `SUMMARY` - 总结文档
- `RETROSPECTIVE` - 回顾总结

### 命名示例

**正确示例** ✅:
```
EPIC4_PLAN.md
EPIC9_STATUS_2026-02-28.md
SPRINT_2_1_TASKS.md
PHASE1_SUMMARY.md
EPER_ITERATION_1_PLAN.md
```

**错误示例** ❌:
```
epic6_daily_summary_20260226.md    # 小写 + 日期格式不一致
FUTURE_WORKFLOW_EXECUTION_PLAN.md  # 太长，FUTURE 前缀不必要
DONE_2026-02-26.md                 # DONE 不是标准动作
```

---

## 🗂️ 文件组织指南

### 创建新 Epic 文档

**位置**: `epics/EPIC#_NAME/`

**必需文件**:
```
EPIC#_PLAN.md                    # Epic 计划
EPIC#_STATUS_YYYY-MM-DD.md       # 状态更新
EPIC#_COMPLETION_REPORT.md       # 完成报告
```

**示例**:
```bash
mkdir -p docs/planning/epics/EPIC11_NEW_FEATURE
# 创建 EPIC11_PLAN.md
```

---

### 创建新 Sprint 文档

**位置**: `phases/PHASE#_NAME/SPRINT_X_Y/`

**必需文件**:
```
PLAN.md                          # Sprint 计划
STATUS_YYYY-MM-DD.md            # Sprint 状态
TASKS.md                         # 任务列表
COMPLETION_REPORT.md            # 完成报告
```

**示例**:
```bash
mkdir -p docs/planning/phases/PHASE2_INTEGRATION/SPRINT_2_2
# 创建 PLAN.md, TASKS.md 等
```

---

### 创建 Daily 记录

**位置**: `daily/YYYY/MM_MMMM/`

**文件命名**: `YYYY-MM-DD.md`

**示例**:
```bash
# 创建今天的 daily 记录
touch docs/planning/daily/2026/02_FEBRUARY/2026-02-28.md
```

**模板**: 见 [daily/INDEX.md](daily/INDEX.md)

---

## 🔍 快速导航

### 我要找...

**Epic 相关文档** → [epics/INDEX.md](epics/INDEX.md)
**Sprint 相关文档** → [phases/INDEX.md](phases/INDEX.md)
**Epic × Sprint 关系** → [MATRIX.md](MATRIX.md)
**今日工作记录** → [daily/INDEX.md](daily/INDEX.md)
**模板文件** → [templates/](templates/)

---

## ⚠️ 常见错误

### 错误 1: 概念混淆
**问题**: 把 Sprint 文件放在 epics/ 目录
**解决**: Sprint 文件应放在 `phases/PHASE#/SPRINT_X_Y/`

### 错误 2: 命名不一致
**问题**: 使用 `done_2026-02-26.md` 或 `epic6_summary.md`
**解决**: 使用 `DAILY_SUMMARY_2026-02-26.md` 或 `EPIC6_SUMMARY.md`

### 错误 3: 日期格式错误
**问题**: `20260226` 或 `26-02-2026`
**解决**: 始终使用 `YYYY-MM-DD` 格式

### 错误 4: 文件位置错误
**问题**: Daily 文件直接放在 daily/ 根目录
**解决**: 应放在 `daily/YYYY/MM_MMMM/`

---

## 🛠️ 维护指南

### 每日维护
1. 更新 `daily/YYYY/MM_MMMM/YYYY-MM-DD.md`
2. 更新相关 Sprint 或 Epic 的 STATUS 文档

### 每周维护
1. 创建周总结 `daily/YYYY/SUMMARY/WEEKLY_SUMMARY_YYYY-MM-W#.md`
2. 更新 [MATRIX.md](MATRIX.md)

### Sprint 完成时
1. 创建 `COMPLETION_REPORT.md`
2. 更新 Phase 的 STATUS
3. 更新 [MATRIX.md](MATRIX.md)
4. 归档旧文件到 `_archive/`

---

## 📋 检查清单

### 创建新文档前
- [ ] 确认文档类型（Epic/Sprint/Daily）
- [ ] 确认文件位置（对应目录）
- [ ] 确认命名格式（ENTITY_ACTION_DATE）
- [ ] 检查是否已存在同名文件

### 提交前
- [ ] 更新索引文件（INDEX.md）
- [ ] 更新 [MATRIX.md](MATRIX.md)
- [ ] 检查链接是否有效
- [ ] 确保命名规则一致

---

## 📚 相关文档

### 规划文档
- [FUTURE_WORKFLOW_EXECUTION_PLAN.md](_archive/FUTURE_WORKFLOW_EXECUTION_PLAN.md) - 12 个月实施计划
- [PLANNING_STRUCTURE_REORGANIZATION_PLAN.md](PLANNING_STRUCTURE_REORGANIZATION_PLAN.md) - 重组计划

### 框架文档
- [.claude/team/FRAMEWORK_INTEGRATION.md](../../.claude/team/FRAMEWORK_INTEGRATION.md) - E-P-E-R 框架
- [.claude/skills/wordland/README.md](../../.claude/skills/wordland/README.md) - Skills 说明

---

**文档版本**: 2.0
**最后更新**: 2026-02-28
**维护者**: Project Team

---

**需要帮助？** 查看 [INDEX.md](INDEX.md) 或联系团队负责人。

### Epic (史诗/大型功能)

**定义**: Epic是一个大型功能单元，通常需要多天或多周完成。

**特点**:
- 独立的功能价值
- 跨越多个任务
- 可独立交付和测试
- 有明确的完成标准

**现有Epic**:
- ✅ Epic #1: 视觉反馈增强 (已完成)
- ✅ Epic #2: 地图系统重构 (已完成)
- ✅ Epic #3: 内容扩展 - Make Lake (已完成)
- ✅ Epic #4: Hint 系统集成 (已完成)
- ✅ Epic #5: 动态星级评分算法 (已完成)
- ⏳ Epic #6: 音频系统 (未开始)
- ⏳ Epic #7: 测试覆盖率提升 (未开始)
- 🔄 Epic #9: 单词消消乐 (进行中 12.5%)

### Iteration (迭代)

**定义**: Iteration是一个时间盒（通常1-2周），包含多个Epic。

**特点**:
- 固定时间周期
- 包含多个Epic
- 有明确目标和成功指标
- 用于规划和追踪进度

**现有Iteration**:
- ✅ EPER_Iteration1 (已完成) - 包含 Epic #1, #2
- 🔄 EPER_Iteration2 (进行中) - 计划包含 Epic #3-#7

---

## 📊 Epic 与 Iteration 的关系

```
EPER_Iteration1 (已完成)
├── Epic #1: 视觉反馈增强 ✅
└── Epic #2: 地图系统重构 ✅

EPER_Iteration2 (进行中)
├── Epic #3: 内容扩展 - Make Lake ✅
├── Epic #4: Hint 系统集成 ✅
├── Epic #5: 动态星级评分算法 ✅
├── Epic #6: 音频系统 ⏳
└── Epic #7: 测试覆盖率提升 ⏳

Standalone Epic (独立Epic)
└── Epic #9: 单词消消乐 🔄 (12.5%)
```

**说明**:
- Epic #8 被跳过（可能是计划调整）
- Epic #9 不在任何Iteration中，作为独立Epic存在

---

## 📁 Planning 目录结构

### 当前结构

```
docs/planning/
├── README.md                  # 本文件
├── EPIC_INDEX.md              # Epic索引
├── ROADMAP_INDEX.md           # 路线图索引
├── PENDING_TASKS_BACKLOG.md   # 待办任务
│
├── daily/                     # 📅 每日工作总结（新增）
│   ├── README.md
│   ├── 2026-02-25.md
│   └── 2026-02-26.md
│
├── templates/                 # 📝 计划和执行模板
│   ├── README.md
│   ├── EPIC_ITERATION1_PLAN_TEMPLATE.md
│   ├── EPIC_ITERATION1_EXECUTION_PLAN_TEMPLATE.md
│   └── PLANNING_STRUCTURE_SUMMARY.md
│
├── iterations/                # Iteration级别规划
│   └── EPER_Iteration2/       # (进行中)
│       ├── BACKLOG.md
│       ├── EPIC5_DYNAMIC_STAR_RATING_PLAN.md
│       ├── EPER_ITERATION2_KICKOFF_NOTIFICATION.md
│       ├── EPER_ITERATION2_LAUNCHED.md
│       ├── EPER_ITERATION2_PLANNING.md
│       ├── EPER_ITERATION2_PLANNING_COMPLETE.md
│       └── EPER_ITERATION2_TASK_BREAKDOWN.md
│
├── epics/                     # 独立Epic规划
│   ├── Epic8/                 # Epic #8 UI增强（已完成）
│   ├── Epic9/                 # Epic #9 单词消消乐（进行中 75%）
│   └── Epic10/                # Epic #10 Onboarding（已完成）
│
└── roadmaps/                  # 路线图文档
    ├── ONBOARDING_GAME_MODES_PRIORITY.md
    └── QUALITY_IMPROVEMENT_ROADMAP.md
```

### 建议的新结构

```
docs/planning/
├── README.md                  # 本文件
├── EPIC_INDEX.md              # Epic索引
├── ROADMAP_INDEX.md           # 路线图索引
├── PENDING_TASKS_BACKLOG.md   # 待办任务
│
├── daily/                     # 📅 每日工作总结
│   ├── README.md
│   ├── 2026-02-25.md
│   └── 2026-02-26.md
│
├── templates/                 # 📝 计划和执行模板
│   ├── README.md
│   ├── EPIC_ITERATION1_PLAN_TEMPLATE.md
│   ├── EPIC_ITERATION1_EXECUTION_PLAN_TEMPLATE.md
│   └── PLANNING_STRUCTURE_SUMMARY.md
│
├── iterations/                # Iteration级别规划
│   └── EPER_Iteration2/       # (进行中)
│
├── epics/                     # 独立Epic规划
│   ├── Epic8/                 # Epic #8 UI增强（已完成）
│   ├── Epic9/                 # Epic #9 单词消消乐（进行中 75%）
│   └── Epic10/                # Epic #10 Onboarding（已完成）
│
└── roadmaps/                  # 路线图文档
    ├── ONBOARDING_GAME_MODES_PRIORITY.md
    └── QUALITY_IMPROVEMENT_ROADMAP.md
```

---

## 🎯 组织原则

### 1. Iteration 文档位置

**规则**: Iteration文档放在 `iterations/` 目录下

**内容**:
- 迭代计划（时间、目标、成功指标）
- 迭代状态（进度、风险、问题）
- 包含的Epic列表
- Backlog管理

**示例**: `iterations/EPER_Iteration2/ITERATION2_PLAN.md`

### 2. Epic 文档位置

**规则**: Epic文档根据其归属放在不同位置

**情况A**: 属于某个Iteration的Epic
```
iterations/EPER_Iteration2/epics/EPIC5_DYNAMIC_STAR_RATING.md
```

**情况B**: 独立的Epic（不属于任何Iteration）
```
epics/EPIC9_WORD_MATCH_GAME/EPIC9_PLAN.md
```

### 3. 设计文档位置

**规则**: Epic的设计文档放在该Epic目录下的 `design/` 子目录

**示例**:
```
epics/EPIC9_WORD_MATCH_GAME/
├── EPIC9_PLAN.md
├── design/
│   ├── GAME_DESIGN.md
│   └── ARCHITECTURE_DESIGN.md
└── tasks/
    └── TASK6_DESIGN_BRIEF.md
```

### 4. 归档规则

**规则**: 已完成的Iteration和Epic移至 `history/` 目录

**示例**:
```
docs/history/
├── EPER_Iteration1/          # 已完成的Iteration
└── epics/
    ├── EPIC1_VISUAL_FEEDBACK/
    ├── EPIC2_MAP_SYSTEM/
    └── ...
```

---

## 📋 Epic 命名规范

### 格式

```
Epic#{编号}_{简短英文描述}
```

### 示例

- `EPIC1_VISUAL_FEEDBACK` - Epic #1 视觉反馈增强
- `EPIC2_MAP_SYSTEM` - Epic #2 地图系统重构
- `EPIC3_MAKE_LAKE` - Epic #3 Make Lake内容扩展
- `EPIC4_HINT_SYSTEM` - Epic #4 Hint系统集成
- `EPIC5_DYNAMIC_STAR_RATING` - Epic #5 动态星级评分
- `EPIC6_AUDIO_SYSTEM` - Epic #6 音频系统
- `EPIC7_TEST_COVERAGE` - Epic #7 测试覆盖率提升
- `EPIC9_WORD_MATCH_GAME` - Epic #9 单词消消乐

### 注意

- Epic编号不一定连续（可能跳过某些编号）
- 使用英文描述，便于目录命名
- 描述应简洁明了（大写+下划线）

---

## 🔄 当前目录重组计划

### Phase 1: 创建新结构（立即执行）

1. ✅ 创建 `planning/README.md`（本文件）
2. ⏳ 创建 `planning/iterations/` 目录
3. ⏳ 创建 `planning/epics/` 目录
4. ⏳ 创建 `planning/roadmaps/` 目录

### Phase 2: 移动现有文档（后续执行）

1. ⏳ 移动 `EPER_Iteration2/` 到 `iterations/EPER_Iteration2/`
2. ⏳ 移动 `Epic9/` 到 `epics/EPIC9_WORD_MATCH_GAME/`
3. ⏳ 移动 `ONBOARDING_GAME_MODES_PRIORITY.md` 到 `roadmaps/`
4. ⏳ 移动 `QUALITY_IMPROVEMENT_ROADMAP.md` 到 `roadmaps/`

### Phase 3: 重组Epic文档（可选）

1. ⏳ 从 `EPER_Iteration2/` 提取各Epic文档到 `epics/` 子目录
2. ⏳ 统一Epic文档命名和格式
3. ⏳ 创建Epic索引文档

---

## 📖 使用指南

### 查找特定Epic的文档

**步骤**:
1. 确定Epic编号和名称
2. 判断是否属于某个Iteration
3. 查找对应目录：

```bash
# 情况A: Epic属于Iteration
find docs/planning/iterations -name "*EPIC5*"

# 情况B: 独立Epic
find docs/planning/epics -name "*EPIC9*"

# 情况C: 已完成Epic
find docs/history -name "*EPIC1*"
```

### 创建新Epic的文档

**步骤**:
1. 判断Epic是否属于当前Iteration
2. 在对应位置创建目录：

```bash
# 独立Epic
mkdir -p docs/planning/epics/EPIC10_NEW_FEATURE
cd docs/planning/epics/EPIC10_NEW_FEATURE

# 创建必要文件
touch EPIC10_PLAN.md
touch EPIC10_STATUS.md
mkdir design
mkdir tasks
```

### 更新Epic状态

**规则**:
- Epic状态文档命名: `EPIC{N}_STATUS_UPDATE_YYYY-MM-DD.md`
- 状态文档放在Epic目录下
- 定期更新（每日或每周）

---

## 🎯 最佳实践

### 1. 文档命名

✅ **推荐**:
- `EPIC5_DYNAMIC_STAR_RATING_PLAN.md`
- `EPIC9_STATUS_UPDATE_2026-02-25.md`
- `GAME_DESIGN_OUTPUT.md`

❌ **不推荐**:
- `plan.md` (太模糊)
- `status.md` (缺少Epic编号和日期)
- `output.md` (缺少上下文)

### 2. 文档结构

每个Epic应包含：
1. **PLAN.md** - Epic计划和目标
2. **STATUS.md** - 当前状态（或定期更新的STATUS_UPDATE文件）
3. **design/** - 设计文档（如需要）
4. **tasks/** - 任务文档（如需要）

### 3. 目录深度

**规则**: 最多3层目录

```
✅ docs/planning/epics/EPIC9_WORD_MATCH_GAME/design/GAME_DESIGN.md (3层)
❌ docs/planning/epics/EPIC9_WORD_MATCH_GAME/design/ui/screens/... (4+层，太深)
```

### 4. 交叉引用

使用相对路径引用其他文档：

```markdown
详见 [Epic #5计划](../iterations/EPER_Iteration2/epics/EPIC5_DYNAMIC_STAR_RATING.md)
```

---

## 📊 当前状态总结

### 已完成的Iteration和Epic

| 迭代/Epic | 状态 | 文档位置 |
|-----------|------|----------|
| EPER_Iteration1 | ✅ 完成 | `docs/history/EPER_Iteration1/` |
| Epic #1 | ✅ 完成 | `docs/history/EPER_Iteration1/` |
| Epic #2 | ✅ 完成 | `docs/history/EPER_Iteration1/` |

### 进行中的Iteration和Epic

| 迭代/Epic | 状态 | 文档位置 |
|-----------|------|----------|
| EPER_Iteration2 | 🔄 进行中 | `docs/planning/EPER_Iteration2/` |
| Epic #3 | ✅ 完成 | (同上) |
| Epic #4 | ✅ 完成 | (同上) |
| Epic #5 | ✅ 完成 | (同上) |
| Epic #6 | ⏳ 未开始 | (同上) |
| Epic #7 | ⏳ 未开始 | (同上) |
| Epic #9 | 🔄 12.5% | `docs/planning/Epic9/` |

---

## 🚀 下一步行动

### 立即行动

1. ✅ 创建 `planning/README.md`（本文档）
2. ⏳ 创建新的目录结构
3. ⏳ 移动现有文档到新结构

### 后续优化

1. ⏳ 提取EPER_Iteration2中的各Epic文档
2. ⏳ 统一文档格式和命名
3. ⏳ 创建Epic索引文档
4. ⏳ 归档已完成的Epic和Iteration

---

**文档维护**: 本文档应随planning目录结构变化而更新
**最后更新**: 2026-02-28（新增 daily/ 目录）
**维护者**: Team Lead
