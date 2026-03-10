# Skills 目录结构纠正

**Date**: 2026-02-28
**Issue**: Skills 创建在错误的目录
**Status**: ✅ 已纠正

---

## 📋 问题描述

在 Sprint 1.1 执行过程中，`autonomous-tdd` Skill 最初被创建在全局目录：
```
❌ /Users/panshan/.claude/skills/wordland/skills/autonomous-tdd/SKILL.md
```

而不是项目目录：
```
✅ /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/autonomous-tdd.md
```

---

## 🔍 问题原因

### 目录混淆

**全局 Claude 配置目录** (`/Users/panshan/.claude/`):
- 系统级配置
- 不受版本控制
- 不适用于项目特定的内容

**项目配置目录** (`/Users/panshan/git/ai/ket/.claude/`):
- 项目特定配置
- 受版本控制
- 可与团队共享

### 原有 Skills 的正确位置

项目中原有的 Skills 已经在正确位置：
```
/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/
├── android-architect.md
├── android-engineer.md
├── android-test-engineer.md
├── compose-ui-designer.md
├── education-specialist.md
├── game-designer.md
└── android-performance-expert.md
```

**结构特点**: 扁平结构，所有 `.md` 文件在同一目录

---

## ✅ 纠正措施

### 1. 移动文件到正确位置

**之前的错误结构**:
```
/Users/panshan/.claude/skills/wordland/skills/
└── autonomous-tdd/
    └── SKILL.md
```

**纠正后的正确结构**:
```
/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/
└── autonomous-tdd.md
```

### 2. 所有 Skills 的正确位置

```
/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/
│
├── Role-Based Skills (7 个)
│   ├── android-architect.md
│   ├── android-engineer.md
│   ├── android-test-engineer.md
│   ├── compose-ui-designer.md
│   ├── education-specialist.md
│   ├── game-designer.md
│   └── android-performance-expert.md
│
└── Workflow Skills (4 个)
    ├── autonomous-tdd.md
    ├── code-review.md
    ├── pre-implementation-check.md
    └── real-device-test.md
```

**总计**: 11 个 Skill 文件

---

## 📚 正确的目录结构说明

### 项目配置根目录

```
/Users/panshan/git/ai/ket/.claude/
├── skills/
│   └── wordland/
│       ├── README.md                 (Skills 说明)
│       ├── adapters/                (适配器)
│       │   ├── android-architect-adapter.md
│       │   ├── android-engineer-adapter.md
│       │   └── ...
│       └── skills/                  (Skills 定义)
│           ├── android-architect.md
│           ├── android-engineer.md
│           ├── autonomous-tdd.md
│           └── ...
```

### 文件命名规范

**Role-Based Skills**:
- 格式: `{role-name}.md`
- 示例: `android-architect.md`, `android-engineer.md`

**Workflow Skills**:
- 格式: `{workflow-name}.md`
- 示例: `autonomous-tdd.md`, `code-review.md`

**注意**: 不使用子目录（如 `autonomous-tdd/SKILL.md`），所有文件扁平化在 `skills/` 目录下

---

## 🎯 最佳实践

### 创建新 Skill 时

1. **确认位置**: `/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/`
2. **使用扁平结构**: 直接创建 `.md` 文件，不创建子目录
3. **命名规范**: 使用 kebab-case（如 `autonomous-tdd.md`）
4. **参考现有 Skills**: 查看现有的 Skill 文件格式

### 示例

```bash
# ✅ 正确：直接创建文件
touch /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/new-skill.md

# ❌ 错误：创建子目录
mkdir /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/new-skill/
touch /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/new-skill/SKILL.md
```

---

## 📊 影响

### 已纠正的文件

| 文件 | 错误位置 | 正确位置 | 状态 |
|------|----------|----------|------|
| autonomous-tdd.md | `/Users/panshan/.claude/...` | `/Users/panshan/git/ai/ket/.claude/...` | ✅ 已移动 |
| code-review.md | 正确 | 正确 | ✅ 无需更改 |
| pre-implementation-check.md | 正确 | 正确 | ✅ 无需更改 |
| real-device-test.md | 正确 | 正确 | ✅ 无需更改 |

### 版本控制

现在所有 Skills 都在项目目录下，可以被 Git 跟踪：
```bash
git add .claude/skills/wordland/skills/autonomous-tdd.md
git commit -m "feat: add autonomous-tdd skill"
```

---

## ✅ 验证

### 检查命令

```bash
# 验证所有 Skills 在正确位置
ls -la /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/

# 应该看到 11 个 .md 文件
# - 7 个角色 Skills
# - 4 个工作流 Skills
```

### Git 状态

```bash
# 查看 Skills 的 Git 状态
git status .claude/skills/wordland/skills/
```

---

## 🎓 经验教训

### 关键要点

1. **项目目录优先**: 项目特定的配置应放在项目目录下（`/Users/panshan/git/ai/ket/.claude/`）
2. **版本控制**: 所有需要版本控制的文件必须在项目目录中
3. **扁平结构**: Skills 使用扁平结构，不使用子目录
4. **命名规范**: 使用 kebab-case 命名 `.md` 文件

### 预防措施

1. **在创建文件前检查现有结构**
   ```bash
   ls -la /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/
   ```

2. **参考现有文件格式**
   ```bash
   head -20 /Users/panshan/git/ai/ket/.claude/skills/wordland/skills/android-architect.md
   ```

3. **使用项目根目录的相对路径**
   ```bash
   # 在项目根目录执行
   cd /Users/panshan/git/ai/ket
   # 然后创建文件
   touch .claude/skills/wordland/skills/new-skill.md
   ```

---

## 🔄 后续行动

- [x] 移动 autonomous-tdd.md 到正确位置
- [x] 清理全局目录下的空目录
- [x] 创建此纠正文档
- [ ] 更新相关文档中的路径引用
- [ ] 在团队中分享此最佳实践

---

**纠正完成**: 2026-02-28
**状态**: ✅ 已解决
**下次创建 Skill 时**: 使用项目目录 `/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/`

---

## 📚 参考资源

- **现有 Skills**: `/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/`
- **Skills README**: `/Users/panshan/git/ai/ket/.claude/skills/wordland/README.md`
- **Project CLAUDE.md**: `/Users/panshan/git/ai/ket/CLAUDE.md`
