# 团队文档组织架构分析

**日期**: 2026-02-24
**问题**: teamagents.md 和 docs/team/ 是否应该与 app 代码/文档分离
**建议者**: User

---

## 🎯 用户观点

> teamagents.md 和 docs/team/ 是为了执行这个 KET app 而进行的团队活动的声明和记录，他们应该和 app 的代码和文档分开放置。

**核心理由**: 团队协作文档与项目业务文档属于不同层次，应该分离。

---

## 📊 当前结构分析

### 当前布局

```
/Users/panshan/git/ai/ket/  (项目根目录)
├── app/                          (Android app 代码)
│   └── src/main/
├── docs/                         (项目文档)
│   ├── requirements/             (业务需求)
│   ├── design/                   (功能设计)
│   ├── architecture/             (架构设计)
│   ├── testing/                  (测试文档)
│   ├── reports/                  (项目报告)
│   └── team/                     (团队协作文档) ← 混合在这里
│       ├── execution/
│       ├── history/
│       └── meetings/
├── teamagents.md                 (团队配置) ← 在根目录
├── CLAUDE.md                     (AI助手指南)
└── .claude/                      (Claude配置和技能)
    └── skills/
        ├── wordland/             (Wordland技能集)
        └── workflows/            (工作流配置)
```

### 问题识别

| 层次 | 当前位置 | 内容 | 问题 |
|------|---------|------|------|
| **团队配置** | 项目根目录 | teamagents.md | 与项目代码混在一起 |
| **团队活动** | docs/team/ | 执行指南、历史记录 | 与项目文档混在一起 |
| **项目文档** | docs/* | 需求、设计、测试 | 各类项目文档 |
| **代码** | app/ | Android app | 业务逻辑代码 |

**观察**:
- ✅ teamagents.md 在根目录（已经与代码分离）
- ⚠️ docs/team/ 与其他项目文档混在一起
- ⚠️ 团队活动文档与项目业务文档缺乏明确边界

---

## 💡 优化方案对比

### 方案 A: 移到 .claude/team/（推荐）✅

**结构**:
```
/Users/panshan/git/ai/ket/
├── app/                         (Android app 代码)
├── docs/                        (项目业务文档)
│   ├── requirements/            (业务需求)
│   ├── design/                  (功能设计)
│   ├── testing/                 (测试文档)
│   └── reports/                 (项目报告)
├── .claude/                     (Claude配置和团队)
│   ├── skills/                  (技能集)
│   │   └── wordland/
│   ├── workflows/               (工作流配置)
│   └── team/                    (团队协作文档) ← 移到这里
│       ├── teamagents.md        (团队配置)
│       ├── execution/          (执行指南)
│       ├── history/            (活动记录)
│       └── meetings/           (会议记录)
├── CLAUDE.md                    (AI助手指南)
└── README.md                    (项目概述)
```

**优点**:
- ✅ **清晰的关注点分离**: Claude/团队 ← 项目/业务
- ✅ **可复用性**: .claude/ 目录本身就是可复用的配置
- ✅ **与skills/一致**: wordland/ 也是项目特定的
- ✅ **易于理解**: .claude/ = AI助手和团队配置
- ✅ **保持根目录整洁**: 只有核心配置文件

**缺点**:
- ❌ 需要更新所有引用链接
- ❌ docs/README.md 需要更新引用

**适用场景**: 重视配置可复用性和层次清晰度

---

### 方案 B: 创建独立的 team/ 目录

**结构**:
```
/Users/panshan/git/ai/ket/
├── app/                         (Android app 代码)
├── docs/                        (项目业务文档)
│   ├── requirements/
│   ├── design/
│   └── ...
├── team/                        (团队协作文档) ← 独立目录
│   ├── teamagents.md           (团队配置)
│   ├── execution/
│   ├── history/
│   └── meetings/
├── .claude/                     (Claude配置)
│   └── skills/
│       └── wordland/
├── CLAUDE.md
└── README.md
```

**优点**:
- ✅ **物理分离**: 团队文档与项目文档完全独立
- ✅ **易于理解**: team/ 明确表示团队相关
- ✅ **可复用**: team/ 可以用于其他项目

**缺点**:
- ❌ **根目录混乱**: 又增加了一个顶级目录
- ❌ **与skills/分离**: wordland/ 在 .claude/ 下，但 team/ 在根目录
- ❌ **层次不清晰**: .claude/ 和 team/ 都是配置，但位置不同

**适用场景**: 团队文档量大，需要独立管理

---

### 方案 C: 保持现状（不推荐）

**结构**:
```
[当前结构不变]
```

**优点**:
- ✅ 无需修改引用
- ✅ 团队历史与其他项目历史在一起

**缺点**:
- ❌ **关注点混淆**: 团队协作与项目设计混在一起
- ❌ **层次不清晰**: 无法快速区分"团队文档"vs"项目文档"
- ❌ **可复用性差**: 团队配置深嵌在项目文档中

**适用场景**: 团队文档很少，不值得重构

---

## 🎯 推荐: 方案 A（移到 .claude/team/）

### 核心理由

#### 1. 清晰的层次分离

```
层次1: 业务层 (app/ + docs/)
  - app/                代码实现
  - docs/requirements/  业务需求
  - docs/design/        功能设计
  - docs/testing/       测试文档
  ↓ 关注点：Wordland app的业务逻辑和功能

层次2: 配置层 (.claude/)
  - .claude/skills/     AI助手技能
  - .claude/workflows/  工作流配置
  - .claude/team/       团队配置和活动 ← 推荐
  ↓ 关注点：如何协作、如何执行

层次3: 根目录 (项目根)
  - teamagents.md      可能保留为快捷引用
  - CLAUDE.md          AI助手指南
  - README.md          项目概述
  ↓ 关注点：项目入口
```

#### 2. 与现有架构一致

**当前 .claude/ 的定位**:
```
.claude/
├── skills/             (AI助手技能集)
│   ├── wordland/      (Wordland项目特定)
│   └── ...
└── workflows/         (工作流配置)
    └── wordland-workflow.yaml
```

**扩展后**:
```
.claude/
├── skills/             (AI助手技能集)
├── workflows/          (工作流配置)
└── team/               (团队配置和活动)
    ├── teamagents.md
    ├── execution/
    ├── history/
    └── meetings/
```

#### 3. 可复用性

**场景1: 创建新项目**
```
new-project/
├── app/
├── docs/
├── .claude/
│   ├── skills/       (复制或复用)
│   └── team/         (复制或复用) ← 可以复用
└── README.md
```

**场景2: 团队配置独立更新**
```bash
# 更新团队配置
cd .claude/team/
git add .
git commit -m "update team collaboration rules"
```

---

## 📋 实施方案 A 的步骤

### 第一步: 创建新目录结构

```bash
mkdir -p .claude/team
```

### 第二步: 移动文件

```bash
# 移动 teamagents.md
mv teamagents.md .claude/team/

# 移动 docs/team/
mv docs/team/* .claude/team/
rmdir docs/team
```

### 第三步: 创建快捷引用（可选）

在项目根目录创建 `teamagents.md` 作为快捷引用：

```markdown
# teamagents.md

**这是 .claude/team/teamagents.md 的快捷引用**

完整文档: [ .claude/team/teamagents.md ](.claude/team/teamagents.md)

---
为了保持根目录简洁，团队配置已移至 `.claude/team/` 目录。
```

### 第四步: 更新引用

需要更新的文件：
1. **CLAUDE.md**:
   - 更新 `teamagents.md` 引用 → `.claude/team/teamagents.md`
   - 更新 `docs/team/` 引用 → `.claude/team/`

2. **docs/README.md**:
   - 更新团队协作文档链接

3. **.claude/skills/wordland/README.md**:
   - 更新 teamagents.md 引用

4. **其他文档**:
   - 搜索所有引用 `teamagents.md` 或 `docs/team/` 的文档
   - 更新链接

### 第五步: 更新 .gitignore（可选）

如果不想在 IDE 中显示 .claude/：

```gitignore
# IDE
.idea/
.vscode/

# 但保留 .claude/（不需要添加到 .gitignore）
```

---

## 🔍 对比表

| 维度 | 当前结构 | 方案 A (.claude/team/) | 方案 B (team/) |
|------|---------|----------------------|---------------|
| **关注点分离** | ⚠️ 混合 | ✅ 清晰 | ✅ 清晰 |
| **根目录整洁度** | ⚠️ 中等 | ✅ 整洁 | ❌ 混乱 |
| **可复用性** | ⚠️ 低 | ✅ 高 | ✅ 高 |
| **与skills/一致性** | ⚠️ 分离 | ✅ 一致 | ❌ 不一致 |
| **引用更新工作量** | ✅ 无 | ⚠️ 中等 | ⚠️ 中等 |
| **直观性** | ⚠️ 中等 | ✅ 高 | ✅ 高 |
| **推荐度** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

---

## 🎯 最终建议

### 🏆 推荐: **方案 A（移到 .claude/team/）**

**理由**:
1. **清晰的层次分离**: 业务层 ← 配置层
2. **与现有架构一致**: skills/, workflows/ 都在 .claude/ 下
3. **可复用性**: .claude/ 本身就是可复用配置
4. **根目录整洁**: 只保留核心配置文件
5. **易于理解**: .claude/ = Claude 和团队配置

**额外好处**:
- ✅ 团队文档可以独立版本控制
- ✅ 方便跨项目复用团队配置
- ✅ 与 wordland/ 技能集的组织方式一致

---

## 📝 总结

**你的观点是正确的** ✅

teamagents.md 和 docs/team/ 确实应该与 app 代码和项目文档分离。

**最佳实践**: 将团队协作文档移至 `.claude/team/`，与 skills/ 和 workflows/ 保持一致。

**实施优先级**: P1（建议，但非紧急）

**实施时机**:
- 可以在下一次文档重构时执行
- 或者在新项目开始前执行
- 需要预留时间更新所有引用链接

---

**分析完成时间**: 2026-02-24
**推荐方案**: 方案 A（移到 .claude/team/）
**置信度**: 90%（基于清晰的架构原则）
