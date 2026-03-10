# 团队文档位置修正方案

**日期**: 2026-02-24
**问题**: `.claude/team/` 不应该被 Git 跟踪
**建议**: 将团队文档移回 `docs/team/`

---

## 🎯 核心观点

**Git 仓库应该包含什么**：
1. ✅ App 代码（app/）
2. ✅ 项目文档（docs/）
   - 业务需求（requirements/）
   - 设计文档（design/）
   - 测试文档（testing/）
   - **团队协作文档（team/）** ← 应该在这里
3. ✅ 核心配置文件（CLAUDE.md, README.md）

**Git 仓库不应该包含什么**：
1. ❌ AI 助手的本地配置（.claude/）
2. ❌ 会话记录（.claude/sessions/）
3. ❌ 本地工具设置（.claude/settings/）
4. ❌ 技能和工作流定义（.claude/skills/, .claude/workflows/）

---

## 🏗️ 正确的架构

### Git 仓库结构

```
/Users/panshan/git/ai/ket/
├── app/                     (Android app 代码)
│   └── src/main/
├── docs/                    (项目文档 - Git 跟踪)
│   ├── requirements/        (业务需求)
│   ├── design/              (功能设计)
│   ├── testing/             (测试文档)
│   ├── analysis/            (技术分析)
│   ├── reports/             (项目报告)
│   └── team/                (团队协作文档) ← 移回这里
│       ├── teamagents.md
│       ├── FRAMEWORK_INTEGRATION.md
│       ├── STATE_PERSISTENCE_RULES.md
│       ├── execution/
│       ├── history/
│       └── meetings/
├── CLAUDE.md                (AI 助手指南 - Git 跟踪)
├── README.md                (项目概述 - Git 跟踪)
└── .gitignore
```

### .claude/ 本地配置（不跟踪）

```
.claude/
├── skills/                  (AI 技能定义 - 本地)
├── workflows/               (工作流配置 - 本地)
├── sessions/                (会话记录 - 本地)
└── settings/                (本地设置 - 本地)
```

---

## 📋 执行步骤

### 第一步：将团队文档从 `.claude/team/` 移回 `docs/team/`

```bash
# 创建 docs/team/
mkdir -p docs/team

# 移动文件
mv .claude/team/* docs/team/

# 删除空目录
rmdir .claude/team
```

### 第二步：更新 .gitignore

```gitignore
# Claude Code (local configuration, not tracked)
.claude/
```

### 第三步：更新引用

需要更新的文件：
1. **CLAUDE.md**: 更新 `.claude/team/` → `docs/team/`
2. **docs/README.md**: 已经有 `docs/team/` 引用
3. **.claude/skills/wordland/README.md**: 更新链接

### 第四步：恢复 teamagents.md 到项目根目录（可选）

```bash
# 如果需要快捷访问
cp docs/team/teamagents.md teamagents.md
```

---

## ✅ 优势

### 1. Git 仓库职责清晰

| 仓库内容 | 说明 |
|---------|------|
| **app/** | 核心：代码实现 |
| **docs/** | 核心：项目文档（包括团队协作） |
| **CLAUDE.md** | 辅助：AI 助手使用指南 |
| **.claude/** | 本地：AI 助手配置（不跟踪） |

### 2. 关注点分离

- **项目内容**（app + docs）：需要版本控制、团队共享
- **工具配置**（.claude/）：本地配置、不跟踪

### 3. 符合 Git 最佳实践

- ✅ Git 仓库专注于项目交付物
- ✅ 工具配置保持在本地
- ✅ `.gitignore` 规则简单清晰

### 4. 团队协作文档的位置合理

**为什么 `docs/team/` 是正确的**：
- ✅ `docs/` = 项目文档
- ✅ 团队协作是项目的一部分
- ✅ 需要版本控制和团队共享
- ✅ 与其他项目文档（requirements, design）平级

---

## 🔄 与之前重构的对比

### 之前的方案（错误）

```
.claude/team/  ← 配置层，但不应该包含团队活动记录
```

**问题**：
- ❌ 混淆了"AI 配置"和"团队协作"
- ❌ 需要复杂的 .gitignore 规则
- ❌ 不符合 Git 仓库的职责定位

### 正确的方案

```
docs/team/  ← 项目文档层，包含团队协作文档
.claude/    ← 本地配置，完全不跟踪
```

**优势**：
- ✅ 职责清晰：docs/ = 项目文档
- ✅ 简单的 .gitignore：`.claude/` 完全忽略
- ✅ 符合 Git 最佳实践
- ✅ 团队协作文档自然地属于项目文档

---

## 📝 总结

**你的观点是完全正确的** ✅

**核心理由**：
1. Git 应该跟踪 app 代码和项目文档
2. Team 协作文档是项目文档的一部分，应该在 `docs/team/`
3. `.claude/` 是 AI 助手的本地配置，不应该被跟踪
4. 简单的 `.gitignore` 规则：`.claude/` 完全忽略

**建议行动**：
1. 将 `.claude/team/*` 移回 `docs/team/`
2. 简化 `.gitignore`：`.claude/` 完全忽略
3. 更新引用链接
4. 提交修正

---

**分析完成时间**: 2026-02-24
**建议方案**: 将团队文档移回 `docs/team/`
**置信度**: 95%（基于清晰的职责分离原则）
