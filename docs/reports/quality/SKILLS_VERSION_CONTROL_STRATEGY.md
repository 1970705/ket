# Skills 版本控制策略分析

**Date**: 2026-02-28
**Type**: 版本控制策略
**Status**: ✅ 分析完成

---

## 📋 问题定义

用户提出：Skills 是通用工具，不是项目特定的，是否应该从 Git 跟踪中排除？

---

## 🔍 分析结果

### 当前 Skills 的实际性质

经过分析 `/Users/panshan/git/ai/ket/.claude/skills/wordland/skills/` 中的所有 Skills，发现：

**结论**: 所有 11 个 Skills 都是 **Wordland 项目特定的**，而非通用工具。

#### 证据 1: 工作流 Skills 引用项目特定内容

**autonomous-tdd.md**:
```markdown
- 引用 "android-test-engineer"（Wordland 团队角色）
- 引用 "TddTestTemplate.kt"（Wordland 项目文件）
- 集成 E-P-E-R 框架（Wordland 项目方法论）
- 使用中文（Wordland 团队语言）
```

**code-review.md**:
```markdown
- 引用 Wordland 的 Clean Architecture 标准
- 引用 Wordland 的代码质量工具（Detekt, KtLint）
- 集成 Wordland 的 Hilt 2.48 + Service Locator 架构
```

**pre-implementation-check.md**:
```markdown
- 检查 Wordland 特定的文件结构
- 验证 Wordland 的依赖注入配置
- 使用 Wordland 的构建命令
```

**real-device-test.md**:
```markdown
- 引用 Wordland 的测试脚本（test_navigation.sh, test_gameplay.sh 等）
- 使用 Wordland 的测试设备（Xiaomi 24031PN0DC）
- 引用 Wordland 的测试检查清单
```

#### 证据 2: 角色 Skills 高度定制

**education-specialist.md**:
- 专门为 10 岁儿童设计
- 针对 KET/PET 考试
- 使用 Wordland 的词根教学法
- 集成 Wordland 的游戏化学习机制

**game-designer.md**:
- 基于 Wordland 的 Spell Battle 游戏模式
- 引用 Wordland 的岛屿和关卡设计
- 集成 Wordland 的星级评分系统

**android-*.md Skills** (7 个角色):
- 全部基于 Wordland 的 Clean Architecture
- 使用 Wordland 的 Hilt 2.48 + Service Locator 混合模式
- 引用 Wordland 的 Jetpack Compose UI
- 集成 Wordland 的测试策略

---

## ✅ 推荐的版本控制策略

### 策略：将 Skills 视为项目配置

**类比**：Skills 类似于以下项目配置文件，应该被跟踪：

| 配置类型 | 位置 | 是否跟踪 | 原因 |
|---------|------|---------|------|
| **GitHub Actions** | `.github/workflows/` | ✅ 是 | 项目特定 CI/CD 配置 |
| **package.json** | 项目根目录 | ✅ 是 | 项目依赖和脚本 |
| **Dockerfile** | 项目根目录 | ✅ 是 | 项目构建配置 |
| **Skills** | `.claude/skills/` | ✅ **是** | **项目工作流配置** |

### 理由

1. **团队协作需要**
   - Skills 定义了团队如何协作
   - 其他开发者需要这些 Skills 来理解项目工作流
   - 通过 Git 共享确保团队一致性

2. **项目特定定制**
   - 所有 Skills 都引用了 Wordland 特定的内容
   - 无法直接用于其他项目（需要修改）
   - 是项目架构的一部分

3. **版本控制的好处**
   - 追踪 Skills 的演进历史
   - 可以回滚到之前的工作流版本
   - 代码审查和协作改进

4. **文档性质**
   - Skills 是项目的工作流文档
   - 应该像其他文档（`docs/`）一样被跟踪
   - 是项目知识库的一部分

---

## 🔄 如果需要通用 Skills

### 场景：希望创建可跨项目复用的通用 Skills

**解决方案**：创建两层 Skills 结构

```
全局层（通用，不跟踪）
├── /Users/panshan/.claude/skills/
│   ├── tdd-workflow.md              # 通用 TDD 工作流
│   ├── code-review-workflow.md      # 通用代码审查工作流
│   └── device-testing-workflow.md   # 通用设备测试工作流

项目层（特定，跟踪）
├── /Users/panshan/git/ai/ket/.claude/skills/
│   └── wordland/
│       └── skills/
│           ├── autonomous-tdd.md          # 引用通用 tdd-workflow.md
│           ├── code-review.md             # 引用通用 code-review-workflow.md
│           ├── android-architect.md       # Wordland 特定
│           └── education-specialist.md    # Wordland 特定
```

### 实施步骤

1. **提取通用部分**
   ```bash
   # 从 Wordland Skills 中提取通用工作流
   # 创建通用版本（去除项目引用）
   ```

2. **创建引用结构**
   ```markdown
   # autonomous-tdd.md (Wordland 项目)
   ---
   extends: /Users/panshan/.claude/skills/tdd-workflow.md

   project:
     - 使用 Wordland 的 TddTestTemplate.kt
     - 集成 Wordland 的 android-test-engineer 角色
   ```

3. **更新 .gitignore**
   ```gitignore
   # 忽略全局通用 Skills
   .claude/skills/generic/

   # 跟踪项目特定 Skills
   !.claude/skills/wordland/
   ```

---

## 📊 当前状态总结

### 当前配置（.gitignore）

```gitignore
# Claude Code system configuration (not tracked)
.claude/plugins/

# But track project-specific Claude Code configs
!.claude/
!.claude/skills/
!.claude/team/
!.claude/settings.local.json
```

**评估**: ✅ **正确** - 当前配置是合理的

### 当前 Skills 分类

| 类型 | 文件数 | 是否跟踪 | 位置 | 理由 |
|------|--------|---------|------|------|
| **工作流 Skills** | 4 | ✅ 是 | `.claude/skills/wordland/skills/` | 引用项目特定内容 |
| **角色 Skills** | 7 | ✅ 是 | `.claude/skills/wordland/skills/` | 高度项目特定 |
| **插件** | 128 | ❌ 否 | `~/.claude/plugins/` | 系统级工具 |

**总计**: 11 个 Skills 文件，4,570 行 Markdown

---

## 🎯 建议

### 短期建议（当前状态）

✅ **保持当前配置不变**

**理由**:
1. 所有当前 Skills 都是 Wordland 项目特定的
2. 团队协作需要共享这些 Skills
3. 版本控制提供历史追踪和协作能力
4. 类似于 `.github/workflows/` 等项目配置

**操作**:
- ✅ 在 `.gitignore` 中保持 `!.claude/skills/`
- ✅ 将 Skills 纳入 Git 跟踪
- ✅ 团队成员通过 Git 共享 Skills

### 长期建议（如果需要通用 Skills）

**创建通用 Skills 库**（如果未来有多个项目）:

1. **在全局目录创建通用 Skills**
   ```bash
   mkdir -p /Users/panshan/.claude/skills/generic/
   ```

2. **提取通用工作流模式**
   - 通用 TDD 工作流（无项目引用）
   - 通用代码审查工作流（无项目引用）
   - 通用测试工作流（无项目引用）

3. **项目 Skills 引用通用 Skills**
   ```markdown
   # autonomous-tdd.md (Wordland)
   ---
   extends: generic/tdd-workflow.md

   customizations:
     - 项目特定的模板和工具
   ```

4. **更新 .gitignore**
   ```gitignore
   # 忽略通用 Skills
   .claude/skills/generic/

   # 跟踪项目特定 Skills
   !.claude/skills/wordland/
   ```

---

## 📚 相关文档

- `GITIGNORE_PLUGINS_SUMMARY.md` - Plugins 目录分析
- `GLOBAL_CLAUDE_CLEANUP_REPORT.md` - 全局目录清理
- `PLUGINS_DIRECTORY_ANALYSIS.md` - Plugins 分析报告

---

## ✅ 结论

**回答用户的问题**:

> "我觉得skills不是专门为一个项目用的，它是通用的"

**分析后的答案**:
- **理论上**：Skills 的**概念**是通用的（TDD 工作流、代码审查等）
- **实际上**：**当前的 11 个 Skills 都是 Wordland 项目特定的**

**建议**:
- ✅ **当前 Skills 应该被 Git 跟踪**（因为它们是项目特定的）
- ✅ **当前 .gitignore 配置是正确的**
- 📝 **如果未来需要通用 Skills**，可以创建两层结构（全局通用 + 项目特定）

---

**分析完成**: 2026-02-28
**状态**: ✅ 分析完成
**建议**: 保持当前 .gitignore 配置不变

---

**让 AI 处理重复性工作，让人类专注于创造性决策！** 🚀
