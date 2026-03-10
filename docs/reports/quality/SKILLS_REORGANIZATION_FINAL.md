# Skills 目录重组方案

**Date**: 2026-02-28
**Type**: 目录重组
**Status**: 🔄 执行中

---

## 📋 问题

用户指出：`.claude/skills/` 中包含大量通用框架，不应该被跟踪在项目中。

---

## 🔍 分析结果

### 完整的 .claude/skills/ 结构（1.2MB，118 文件）

| 目录 | 大小 | 性质 | 是否应保留 | 位置建议 |
|------|------|------|----------|---------|
| **elicit-plan-execute-review/** | 204K | 🌐 通用框架 | ❌ 移走 | 全局 |
| **team-operations-framework/** | 652K | 🌐 通用框架 | ❌ 移走 | 全局 |
| **plan-execute-review/** | 84K | 🌐 通用框架 | ❌ 移走 | 全局 |
| **tt/** | 88K | 🔧 其他项目 | ❌ 移走 | 全局或删除 |
| **wordland/** | 188K | 📦 Wordland 特定 | ✅ 可选 | **两种方案** |
| **workflows/** | 28K | 🌐 通用配置 | ❌ 移走 | 全局 |

### 通用框架的证据

**elicit-plan-execute-review/README.md**:
> "一个**跨行业通用**的 AI 辅助工作流框架"
> "可应用于任何行业"

**team-operations-framework/README.md**:
> "一套**独立、通用**的团队执行层方法论"
> "适用于软件开发、市场营销、咨询服务、制造业、教育培训等**所有行业**"

---

## ✅ 推荐方案

### 方案 A：完全通用化（推荐）

**原则**: 所有 Skills 都是通用工具，都不应该被跟踪

```bash
# 1. 将所有内容移到全局目录
mv /Users/panshan/git/ai/ket/.claude/skills/* \
   /Users/panshan/.claude/skills/

# 2. 更新 .gitignore
# .gitignore
.claude/
```

**优点**:
- ✅ 简单清晰，项目仓库更小
- ✅ Skills 可以在多个项目间共享
- ✅ 符合 Claude Code 设计理念（Skills 是工具，不是项目代码）
- ✅ 避免重复跟踪相同的框架

**缺点**:
- ❌ Wordland 团队配置需要单独文档说明
- ❌ 新成员需要手动安装 Skills

### 方案 B：混合模式（备选）

**原则**: 通用框架在全局，项目特定配置在项目

```bash
# 全局 Skills（不跟踪）
~/.claude/skills/
├── elicit-plan-execute-review/
├── team-operations-framework/
├── plan-execute-review/
└── workflows/

# 项目特定（跟踪）
.claude/
├── skills/
│   └── wordland/
│       ├── README.md           # 说明如何安装全局 Skills
│       ├── adapters/           # Wordland 特定的适配器
│       └── skills/             # Wordland 特定的 Skills（如果有的话）
└── team/
    └── teamagents.md           # 团队配置

# .gitignore
.claude/skills/generic/  # 忽略通用 Skills
!.claude/skills/wordland/  # 但跟踪 Wordland 特定的内容
```

**优点**:
- ✅ 明确区分通用和特定
- ✅ 团队配置通过 Git 共享
- ✅ 新成员自动获取项目特定配置

**缺点**:
- ❌ 复杂的 .gitignore 规则
- ❌ 需要明确哪些是"通用"，哪些是"特定"
- ❌ Wordland 的 11 个 Skills 目前都引用了项目特定内容

---

## 🎯 最终推荐：方案 A（完全通用化）

**理由**:

1. **Skills 是工具，不是项目代码**
   - Skills 定义了如何工作（方法论）
   - 类似于 plugins、IDE 配置
   - 不应该纳入项目版本控制

2. **通用框架远大于项目特定内容**
   - 通用框架：~1MB（elicit-plan-execute-review, team-operations-framework, etc.）
   - Wordland 特定：~188K（但其中也包含大量通用适配器）
   - 即使 Wordland Skills 也大多是通用角色 + 适配器模式

3. **避免重复和冲突**
   - 如果多个项目都跟踪相同的框架，会产生大量重复
   - 框架更新需要在多个项目中同步
   - 全局管理更方便

4. **通过文档解决团队协作**
   - 在 README.md 中说明需要安装的 Skills
   - 提供安装脚本
   - 在 CLAUDE.md 中说明依赖的框架

---

## 📝 实施步骤（方案 A）

### Step 1: 备份当前 Skills

```bash
# 创建备份
cp -r /Users/panshan/git/ai/ket/.claude/skills \
      /tmp/claude-skills-backup-$(date +%Y%m%d)
```

### Step 2: 移动到全局目录

```bash
# 移动所有 Skills 到全局目录
mv /Users/panshan/git/ai/ket/.claude/skills/* \
   /Users/panshan/.claude/skills/

# 清空项目目录（可选，或者 rm -rf）
rmdir /Users/panshan/git/ai/ket/.claude/skills
```

### Step 3: 更新 .gitignore

```gitignore
# Claude Code (all configuration, not tracked)
.claude/
```

### Step 4: 创建文档说明

**创建 `docs/guides/development/CLAUDE_SKILLS_SETUP.md`**:

```markdown
# Claude Code Skills 配置

本项目依赖以下 Claude Code Skills 框架：

## 必需的 Skills

1. **Elicit-Plan-Execute-Review Framework**
   - 位置：`~/.claude/skills/elicit-plan-execute-review/`
   - 版本：1.0
   - 用途：4 阶段工作流方法论

2. **Team Operations Framework**
   - 位置：`~/.claude/skills/team-operations-framework/`
   - 版本：1.0
   - 用途：团队协作和执行最佳实践

3. **Wordland Skills**
   - 位置：`~/.claude/skills/wordland/`
   - 内容：7 个角色 Skills + 4 个工作流 Skills
   - 角色：android-architect, android-engineer, etc.

## 安装步骤

### 方式 1：手动安装

```bash
# 从项目文档目录复制
cp -r docs/claude-skills/* ~/.claude/skills/
```

### 方式 2：使用安装脚本

```bash
./scripts/setup-claude-skills.sh
```

## 验证安装

```bash
# 检查 Skills 是否存在
ls ~/.claude/skills/wordland/skills/
```

## 团队配置

团队配置文件位于 `.claude/team/teamagents.md`，但这是本地配置，不纳入版本控制。

团队成员需要：
1. 从共享位置获取团队配置
2. 手动放置在 `.claude/team/teamagents.md`
3. 或者通过团队同步脚本自动配置
```

### Step 5: 创建安装脚本

**创建 `scripts/setup-claude-skills.sh`**:

```bash
#!/bin/bash
# Claude Code Skills 安装脚本

SKILLS_SOURCE="docs/claude-skills"
SKILLS_TARGET="$HOME/.claude/skills"

echo "📦 Installing Claude Code Skills..."

# 创建目标目录
mkdir -p "$SKILLS_TARGET"

# 复制 Skills
if [ -d "$SKILLS_SOURCE" ]; then
    cp -r "$SKILLS_SOURCE"/* "$SKILLS_TARGET/"
    echo "✅ Skills installed to $SKILLS_TARGET"
else
    echo "❌ Source directory not found: $SKILLS_SOURCE"
    exit 1
fi

echo "🎉 Installation complete!"
```

### Step 6: 将 Skills 备份到 docs/

```bash
# 将当前的 Skills 复制到文档目录作为备份
mkdir -p docs/claude-skills
cp -r /Users/panshan/.claude/skills/* docs/claude-skills/

# 添加到 Git
git add docs/claude-skills/
git commit -m "docs: backup Claude Code Skills to docs directory"
```

---

## 🔄 Git 状态更新

### .gitignore

```gitignore
# Claude Code (all configuration, not tracked)
.claude/

# But track Skills backup in docs/
!docs/claude-skills/
```

### docs/claude-skills/

```
docs/claude-skills/
├── elicit-plan-execute-review/    # 通用框架
├── team-operations-framework/     # 通用框架
├── plan-execute-review/           # 通用框架
├── workflows/                     # 通用工作流
└── wordland/                      # Wordland Skills
```

---

## ✅ 最终状态

### 项目目录（Git 跟踪）

```
/Users/panshan/git/ai/ket/
├── .claude/
│   └── team/                      # 仅本地配置，不跟踪
├── docs/
│   └── claude-skills/             # ✅ Skills 备份（跟踪）
└── scripts/
    └── setup-claude-skills.sh     # ✅ 安装脚本（跟踪）
```

### 全局目录（不跟踪）

```
/Users/panshan/.claude/skills/
├── elicit-plan-execute-review/    # 通用框架
├── team-operations-framework/     # 通用框架
├── plan-execute-review/           # 通用框架
├── workflows/                     # 通用工作流
└── wordland/                      # Wordland Skills
```

---

## 📚 相关文档

- `SKILLS_VERSION_CONTROL_STRATEGY.md` - 之前的分析（已过时）
- `GITIGNORE_PLUGINS_SUMMARY.md` - Plugins 分析
- `GLOBAL_CLAUDE_CLEANUP_REPORT.md` - 全局目录清理

---

## 🎓 经验教训

### 关键要点

1. **Skills 是通用工具**
   - E-P-E-R、Team Operations Framework 等都是跨行业通用的
   - 应该放在全局目录，不纳入项目版本控制

2. **项目特定配置 vs 通用工具**
   - 通用工具：Skills、Plugins、Frameworks → 全局
   - 项目代码：src/、tests/、docs/ → 项目
   - 项目配置：build.gradle、package.json → 项目

3. **通过文档和脚本解决团队协作**
   - 将 Skills 备份到 `docs/claude-skills/`
   - 提供安装脚本
   - 在 README 中说明依赖

---

**执行完成**: 2026-02-28
**状态**: ✅ 方案确定，等待执行确认

---

**让 AI 处理重复性工作，让人类专注于创造性决策！** 🚀
