# Planning 目录重组计划

**Date**: 2026-02-28
**Status**: 🎯 Approved - Ready to Execute
**Version**: 1.0

---

## 📋 Executive Summary

重组 `docs/planning/` 目录，建立**功能 × 时间**二维矩阵结构，统一命名规则，解决 Epic/Sprint/Iteration/Daily 混乱问题。

**目标**:
- ✅ 清晰的层级结构（功能/时间分离）
- ✅ 统一的命名规则（SCREAMING_SNAKE_CASE）
- ✅ 明确的文件组织（易于查找）
- ✅ 完整的索引体系（快速导航）

---

## 🎯 问题分析

### 当前混乱状况

**概念混用**:
- Epic（功能）× Sprint（时间）× Iteration（执行）× Daily（记录）

**文件分散**:
```
planning/
├── EPIC_INDEX.md                    # Epic 索引
├── FUTURE_WORKFLOW_EXECUTION_PLAN.md # 执行计划
├── SPRINT_2_1_STATUS_SAVE.md        # Sprint 状态
├── epics/epic9/                     # Epic #9 文件
├── iterations/EPER_Iteration2/      # Iteration 文件
└── daily/                           # Daily 文件
```

**命名不统一**:
- `EPIC_INDEX.md` vs `epic6_daily_summary_20260226.md`
- `SPRINT_2_1_STATUS_SAVE.md` vs `DAILY_SUMMARY_2026-02-25.md`

---

## ✅ 新结构设计

### 二维矩阵结构

**维度 1: 功能维度**（Epic）
```
epics/
├── EPIC4_HINT_SYSTEM/
├── EPIC5_STAR_RATING/
├── EPIC9_WORD_MATCH_GAME/
└── EPIC10_ONBOARDING/
```

**维度 2: 时间维度**（Phase → Sprint）
```
phases/
├── PHASE1_FOUNDATION/
│   ├── SPRINT_1_1/
│   ├── SPRINT_1_2/
│   ├── SPRINT_1_3/
│   └── SPRINT_1_4/
├── PHASE2_INTEGRATION/
│   ├── SPRINT_2_1/
│   ├── SPRINT_2_2/
│   └── SPRINT_2_3/
└── PHASE3_AUTOMATION/
```

**维度 3: 执行维度**（E-P-E-R Iteration）
```
workflows/
└── eper_iterations/
    ├── EPER_ITERATION_1/
    └── EPER_ITERATION_2/
```

**维度 4: 记录维度**（Daily）
```
daily/
└── 2026/
    └── 02_FEBRUARY/
        ├── 2026-02-25.md
        └── 2026-02-26.md
```

---

## 📝 统一命名规则

### 格式定义

```
[ENTITY]_[ACTION]_[YYYY-MM-DD].md
```

**ENTITY（实体）**:
- `EPIC#` - Epic 编号
- `PHASE#` - Phase 编号
- `SPRINT_X_Y` - Sprint 编号
- `EPER_ITERATION#` - E-P-E-R 迭代

**ACTION（动作）**:
- `PLAN` - 计划
- `STATUS` - 状态更新
- `TASKS` - 任务列表
- `PROGRESS` - 进度报告
- `COMPLETION_REPORT` - 完成报告
- `RETROSPECTIVE` - 回顾总结
- `SUMMARY` - 总结

**日期格式**: `YYYY-MM-DD`

### 命名示例

**Epic 文件**:
```
EPIC4_PLAN.md
EPIC4_STATUS_2026-02-24.md
EPIC4_COMPLETION_REPORT.md
```

**Phase 文件**:
```
PHASE1_PLAN.md
PHASE1_STATUS_2026-02-28.md
PHASE1_SUMMARY.md
```

**Sprint 文件**:
```
SPRINT_1_1_PLAN.md
SPRINT_1_1_STATUS_2026-02-28.md
SPRINT_1_1_TASKS.md
SPRINT_1_1_COMPLETION_REPORT.md
```

**Daily 文件**:
```
2026-02-25.md
DAILY_SUMMARY_2026-02-25.md
WEEKLY_SUMMARY_2026-02-W4.md
```

---

## 🗂️ 完整目录结构

```
docs/planning/
├── INDEX.md                                    # 总索引
├── README.md                                   # 使用说明
├── ROADMAP.md                                  # 产品路线图
│
├── epics/                                      # 【功能维度】
│   ├── INDEX.md
│   ├── EPIC4_HINT_SYSTEM/
│   │   ├── EPIC4_PLAN.md
│   │   ├── EPIC4_STATUS_2026-02-24.md
│   │   └── EPIC4_COMPLETION_REPORT.md
│   ├── EPIC5_STAR_RATING/
│   ├── EPIC9_WORD_MATCH_GAME/
│   │   ├── EPIC9_PLAN.md
│   │   ├── EPIC9_STATUS_2026-02-25.md
│   │   └── TASKS.md
│   └── EPIC10_ONBOARDING/
│
├── phases/                                     # 【时间维度】
│   ├── INDEX.md
│   ├── PHASE1_FOUNDATION/
│   │   ├── PHASE1_PLAN.md
│   │   ├── SPRINT_1_1/
│   │   │   ├── PLAN.md
│   │   │   ├── STATUS_2026-02-28.md
│   │   │   ├── TASKS.md
│   │   │   └── COMPLETION_REPORT.md
│   │   ├── SPRINT_1_2/
│   │   ├── SPRINT_1_3/
│   │   ├── SPRINT_1_4/
│   │   └── PHASE1_SUMMARY.md
│   ├── PHASE2_INTEGRATION/
│   │   ├── PHASE2_PLAN.md
│   │   ├── SPRINT_2_1/
│   │   │   ├── PLAN.md
│   │   │   ├── STATUS_2026-02-28.md
│   │   │   ├── TASKS.md
│   │   │   └── COMPLETION_REPORT.md
│   │   └── SPRINT_2_2/
│   └── PHASE3_AUTOMATION/
│
├── workflows/                                  # 【执行维度】
│   ├── INDEX.md
│   └── eper_iterations/
│       ├── EPER_ITERATION_1/
│       │   ├── PLAN.md
│       │   ├── STATUS.md
│       │   └── RETROSPECTIVE.md
│       └── EPER_ITERATION_2/
│
├── daily/                                      # 【记录维度】
│   ├── INDEX.md
│   └── 2026/
│       ├── 02_FEBRUARY/
│       │   ├── 2026-02-25.md
│       │   ├── 2026-02-26.md
│       │   └── 2026-02-27.md
│       ├── 03_MARCH/
│       └── SUMMARY/
│           ├── WEEKLY_2026-02-W4.md
│           └── MONTHLY_2026-02.md
│
├── roadmaps/                                   # 保留：专项路线图
├── templates/                                  # 保留：模板
└── _archive/                                   # 旧文件归档
```

---

## 🚀 执行步骤

### Step 1: 创建新目录结构

```bash
mkdir -p docs/planning/epics/{EPIC4_HINT_SYSTEM,EPIC5_STAR_RATING,EPIC9_WORD_MATCH_GAME,EPIC10_ONBOARDING}
mkdir -p docs/planning/phases/PHASE1_FOUNDATION/{SPRINT_1_1,SPRINT_1_2,SPRINT_1_3,SPRINT_1_4}
mkdir -p docs/planning/phases/PHASE2_INTEGRATION/{SPRINT_2_1,SPRINT_2_2,SPRINT_2_3}
mkdir -p docs/planning/phases/PHASE3_AUTOMATION
mkdir -p docs/planning/workflows/eper_iterations/{EPER_ITERATION_1,EPER_ITERATION_2}
mkdir -p docs/planning/daily/2026/{02_FEBRUARY,03_MARCH,SUMMARY}
mkdir -p docs/planning/_archive
```

### Step 2: 创建索引文件

创建以下索引文件：
- `INDEX.md` - 总索引
- `epics/INDEX.md` - Epic 索引
- `phases/INDEX.md` - Phase 索引
- `workflows/INDEX.md` - Workflow 索引
- `daily/INDEX.md` - Daily 索引

### Step 3: 迁移现有文件

**Phase 1: 活跃文件**（立即迁移）
- `SPRINT_2_1_STATUS_SAVE.md` → `phases/PHASE2_INTEGRATION/SPRINT_2_1/STATUS_2026-02-28.md`
- `epics/epic9/*` → `epics/EPIC9_WORD_MATCH_GAME/`

**Phase 2: Phase 1 文件**
- Sprint 1.1-1.4 相关文件

**Phase 3: 其他文件**
- Daily 文件
- Workflow 文件

**Phase 4: 清理**
- 移动旧文件到 `_archive/`

### Step 4: 更新引用

更新所有文档中的文件路径引用。

---

## 📊 Epic × Sprint 矩阵

```
                    Sprint 1.1    Sprint 1.2    Sprint 1.3    Sprint 1.4
                    (Week 1-2)    (Week 3-4)    (Week 5)      (Week 6-8)
                   ┼─────────────┼─────────────┼─────────────┼─────────────
Epic #4: Hint     │    50%       │    100%     │             │
System            │              │             │             │
                   ┼─────────────┼─────────────┼─────────────┼─────────────
Epic #5: Star     │              │    30%      │    100%     │
Rating            │              │             │             │
                   ┼─────────────┼─────────────┼─────────────┼─────────────
Epic #9: Word     │              │             │             │    12.5%
Match             │              │             │             │
                   ┼─────────────┼─────────────┼─────────────┼─────────────
Epic #10:         │              │             │             │    100%
Onboarding        │              │             │             │
                   ┴─────────────┴─────────────┴─────────────┴─────────────
```

---

## ✅ 验收标准

- [ ] 所有目录已创建
- [ ] 所有索引文件已创建
- [ ] 活跃文件已迁移（Sprint 2.1, Epic #9）
- [ ] 命名规则统一
- [ ] 旧文件已归档
- [ ] README.md 已更新

---

## 📝 风险缓解

**风险 1**: 文件丢失
- **缓解**: 先复制再移动，保留原始文件在 `_archive/`

**风险 2**: 链接断裂
- **缓解**: 更新所有引用，使用相对路径

**风险 3**: 命名冲突
- **缓解**: 检查目标位置是否已有同名文件

---

**执行时间**: 预计 30 分钟
**执行方式**: 自动化脚本 + 手动验证
