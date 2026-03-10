# Planning 目录结构梳理总结

**日期**: 2026-02-25
**操作**: 目录重组和文档整理
**状态**: ✅ 完成

---

## 📖 核心概念说明

### Epic vs Iteration

**Epic (史诗)**:
- 定义: 大型功能单元
- 特点: 独立价值、可交付、有明确完成标准
- 示例: Epic #9 单词消消乐（12.5%）

**Iteration (迭代)**:
- 定义: 时间盒（1-2周）
- 特点: 包含多个Epic、固定周期、有目标
- 示例: EPER_Iteration2（包含Epic #3-#7）

**关系图**:
```
Iteration (时间容器)
  └── Epic (功能单元)
      └── Task (具体任务)
```

---

## 📁 新的目录结构

### 最终结构

```
docs/planning/
├── README.md                       # 📖 Planning目录说明
├── EPIC_INDEX.md                   # 📋 Epic索引（所有Epic状态）
├── ROADMAP_INDEX.md                # 🗺️ 路线图索引
├── PLANNING_STRUCTURE_SUMMARY.md   # 📊 本文件
│
├── iterations/                     # 🔄 Iteration级别规划
│   └── EPER_Iteration2/           # Iteration 2（进行中）
│       ├── BACKLOG.md
│       ├── EPIC5_DYNAMIC_STAR_RATING_PLAN.md
│       ├── EPER_ITERATION2_KICKOFF_NOTIFICATION.md
│       ├── EPER_ITERATION2_LAUNCHED.md
│       ├── EPER_ITERATION2_PLANNING.md
│       ├── EPER_ITERATION2_PLANNING_COMPLETE.md
│       └── EPER_ITERATION2_TASK_BREAKDOWN.md
│
├── epics/                          # 🎯 独立Epic规划
│   └── Epic9/                      # Epic 9（进行中 12.5%）
│       ├── ARCHITECTURE_DESIGN_OUTPUT.md
│       ├── EPIC9_STATUS_UPDATE_2026-02-25.md
│       ├── GAME_DESIGN_OUTPUT.md
│       ├── GAME_LOGIC_DESIGN.md
│       └── TASK6_DESIGN_BRIEF.md
│
└── roadmaps/                       # 🗺️ 路线图文档
    ├── ONBOARDING_GAME_MODES_PRIORITY.md
    └── QUALITY_IMPROVEMENT_ROADMAP.md
```

### 与历史目录的关系

```
docs/
├── planning/                       # 当前规划和进行中的工作
│   ├── iterations/                 → Iteration 2
│   ├── epics/                      → Epic #9
│   └── roadmaps/                   → 路线图
│
└── history/                        # 已完成的工作
    ├── EPER_Iteration1/            → Iteration 1 (已完成)
    └── [未来的归档]
```

---

## 📊 Epic分布图

### 按Iteration分组

**EPER_Iteration1** (已完成 2026-02-15 ~ 2026-02-17):
- Epic #1: 视觉反馈增强 ✅
- Epic #2: 地图系统重构 ✅

**EPER_Iteration2** (进行中 2026-02-22 ~ ):
- Epic #3: Make Lake内容扩展 ✅
- Epic #4: Hint系统集成 ✅
- Epic #5: 动态星级评分算法 ✅
- Epic #6: 音频系统 ⏳
- Epic #7: 测试覆盖率提升 ⏳

**独立Epic** (不属于任何Iteration):
- Epic #9: 单词消消乐 🔄 12.5%

### 完成度统计

| 状态 | 数量 | 百分比 |
|------|------|--------|
| ✅ 完成 | 5 | 62.5% |
| 🔄 进行中 | 1 | 12.5% |
| ⏳ 未开始 | 2 | 25% |

**总计**: 8个Epic

---

## 🎯 组织原则

### 1. 位置规则

**Iteration文档**:
- 位置: `docs/planning/iterations/{IterationName}/`
- 内容: 迭代计划、状态、任务分解
- 示例: `iterations/EPER_Iteration2/EPER_ITERATION2_PLANNING.md`

**独立Epic文档**:
- 位置: `docs/planning/epics/{EpicName}/`
- 内容: Epic计划、设计文档、状态更新
- 示例: `epics/Epic9/GAME_DESIGN_OUTPUT.md`

**路线图文档**:
- 位置: `docs/planning/roadmaps/`
- 内容: 游戏模式优先级、质量改进计划
- 示例: `roadmaps/ONBOARDING_GAME_MODES_PRIORITY.md`

### 2. 命名规范

**Iteration目录**:
- 格式: `EPER_Iteration{N}`
- 示例: `EPER_Iteration1`, `EPER_Iteration2`

**Epic目录**:
- 格式: `Epic{N}` 或 `EPIC{N}_{DESCRIPTION}`
- 示例: `Epic9`, `EPIC9_WORD_MATCH_GAME`

**文档命名**:
- 计划文档: `{Epic或Iteration}_PLAN.md`
- 状态文档: `{Epic或Iteration}_STATUS_UPDATE_YYYY-MM-DD.md`
- 设计文档: `{主题}_DESIGN_OUTPUT.md`

### 3. 归档规则

**完成条件**:
- Iteration: 所有包含的Epic 100%完成
- Epic: 所有任务完成并通过验收

**归档位置**:
- 完成的Iteration: `docs/history/{IterationName}/`
- 完成的Epic: 
  - 如属于Iteration: 随Iteration一起归档
  - 如独立Epic: `docs/history/epics/{EpicName}/`

---

## 📋 本次重组操作

### 新增文档 ✅

1. `docs/planning/README.md` - Planning目录说明
2. `docs/planning/EPIC_INDEX.md` - Epic索引
3. `docs/planning/ROADMAP_INDEX.md` - 路线图索引
4. `docs/planning/PLANNING_STRUCTURE_SUMMARY.md` - 本文件

### 新增目录 ✅

1. `docs/planning/iterations/` - Iteration文档
2. `docs/planning/epics/` - 独立Epic文档
3. `docs/planning/roadmaps/` - 路线图文档

### 待完成操作 ⏳

**Phase 2**: 移动现有文档
- ⏳ 移动 `EPER_Iteration2/` 到 `iterations/EPER_Iteration2/`
- ⏳ 移动 `Epic9/` 到 `epics/Epic9/`
- ⏳ 移动 `ONBOARDING_GAME_MODES_PRIORITY.md` 到 `roadmaps/`
- ⏳ 移动 `QUALITY_IMPROVEMENT_ROADMAP.md` 到 `roadmaps/`

**Phase 3**: 重组和优化
- ⏳ 从EPER_Iteration2提取各Epic文档
- ⏳ 统一文档格式
- ⏳ 创建Epic模板

---

## 🔗 快速导航

### 查找文档

**查找特定Epic**:
```bash
# 方法1: 查看索引
cat docs/planning/EPIC_INDEX.md

# 方法2: 搜索Epic编号
find docs/planning -name "*EPIC9*"

# 方法3: 搜索关键词
grep -r "单词消消乐" docs/planning/
```

**查找Iteration文档**:
```bash
# 查看所有Iteration
ls docs/planning/iterations/

# 查看Iteration 2计划
cat docs/planning/iterations/EPER_Iteration2/EPER_ITERATION2_PLANNING.md
```

**查找路线图**:
```bash
# 查看路线图索引
cat docs/planning/ROADMAP_INDEX.md

# 查看游戏模式优先级
cat docs/planning/roadmaps/ONBOARDING_GAME_MODES_PRIORITY.md
```

### 创建新文档

**创建新Epic**:
```bash
# 1. 创建Epic目录
mkdir -p docs/planning/epics/Epic10

# 2. 创建必要文件
touch docs/planning/epics/Epic10/EPIC10_PLAN.md
touch docs/planning/epics/Epic10/EPIC10_STATUS.md

# 3. 创建子目录
mkdir -p docs/planning/epics/Epic10/design
mkdir -p docs/planning/epics/Epic10/tasks
```

**更新Epic索引**:
```bash
# 编辑EPIC_INDEX.md，添加新Epic信息
vim docs/planning/EPIC_INDEX.md
```

---

## 📖 关键文档说明

### 必读文档

1. **README.md** - Planning目录说明
   - Epic vs Iteration概念
   - 组织原则
   - 最佳实践

2. **EPIC_INDEX.md** - Epic索引
   - 所有Epic的状态
   - 详细的Epic信息
   - 统计数据

3. **ROADMAP_INDEX.md** - 路线图索引
   - 游戏模式优先级
   - 质量改进计划
   - 实施时间线

### 参考文档

**已完成的Iteration**:
- `docs/history/EPER_Iteration1/` - Iteration 1完整记录

**进行中的Iteration**:
- `docs/planning/iterations/EPER_Iteration2/` - Iteration 2计划

**进行中的Epic**:
- `docs/planning/epics/Epic9/` - Epic #9单词消消乐

---

## ✅ 验收标准

### 目录结构验收

- ✅ `docs/planning/README.md` 存在且完整
- ✅ `docs/planning/EPIC_INDEX.md` 存在且完整
- ✅ `docs/planning/ROADMAP_INDEX.md` 存在且完整
- ✅ `docs/planning/iterations/` 目录已创建
- ✅ `docs/planning/epics/` 目录已创建
- ✅ `docs/planning/roadmaps/` 目录已创建

### 内容验收

- ✅ Epic与Iteration概念清晰
- ✅ 组织原则明确
- ✅ 所有8个Epic已索引
- ✅ 完成度统计正确
- ✅ 快速导航指南完整

### 可用性验收

- ✅ 用户能快速找到特定Epic
- ✅ 用户能理解Epic与Iteration关系
- ✅ 用户知道如何创建新文档
- ✅ 命名规范清晰可执行

---

## 🎯 下一步行动

### 立即行动

1. ✅ 完成本次文档整理
2. ⏳ 移动现有文档到新结构
3. ⏳ 更新CLAUDE.md中的文档链接

### 后续优化

1. ⏳ 为每个Epic创建README
2. ⏳ 统一文档模板
3. ⏳ 添加文档交叉引用
4. ⏳ 创建文档维护指南

---

## 📊 统计数据

### 文档统计

**本次新增**: 4个文档
- README.md
- EPIC_INDEX.md
- ROADMAP_INDEX.md
- PLANNING_STRUCTURE_SUMMARY.md

**总计Planning文档**: 17个
- Iteration 2: 8个
- Epic 9: 5个
- 索引和说明: 4个

### 覆盖范围

**Epic覆盖**: 8/8 (100%)
**Iteration覆盖**: 2/2 (100%)
**路线图覆盖**: 2/2 (100%)

---

**整理完成**: 2026-02-25
**整理者**: Team Lead
**状态**: ✅ Phase 1完成，Phase 2待执行
