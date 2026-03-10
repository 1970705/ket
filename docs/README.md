# Wordland 项目文档

欢迎使用 Wordland 项目文档中心。本文档提供所有项目文档的导航索引。

---

## 📖 文档分类原则

### 🎯 核心区分

**docs/** vs **.claude/team/** 的区别：

| 维度 | `docs/` | `.claude/team/` |
|------|---------|----------------|
| **目的** | 项目文档（面向人类） | AI 团队管理配置 |
| **受众** | 开发者、项目经理 | AI 团队成员 |
| **内容** | 架构、设计、测试、项目历史 | 团队配置、协作规则、团队状态 |
| **示例** | ADR、测试报告、代码分析 | teamagents.md、协作规则 |

### 📂 docs/ 包含内容

✅ **属于这里**：
- **项目历史**（history/）：迭代记录、实施周报、工作状态
- **分析报告**（analysis/）：技术分析、团队分析（作为项目记录）
- **质量报告**（reports/quality/）：优化报告、完成报告
- **测试报告**（reports/testing/）：测试结果、覆盖率报告
- **架构文档**（architecture/、design/）：系统设计、技术决策
- **开发指南**（guides/、development/）：开发规范、操作指南

❌ **不属于这里**：
- 团队运营配置 → 应在 `.claude/team/`
- Agent 定义 → 应在 `.claude/skills/`
- 团队协作规则 → 应在 `.claude/team/execution/`

### 🔍 关键判断标准

**问题：这个文档是关于什么的？**

1. **如果是"项目的分析/报告"** → `docs/`
   - 例：性能分析、测试报告、团队分析报告

2. **如果是"项目历史记录"** → `docs/history/`
   - 例：迭代执行记录、实施周报、里程碑

3. **如果是"团队如何协作"** → `.claude/team/`
   - 例：协作规则、质量门禁、会议记录

4. **如果是"技术架构/设计"** → `docs/architecture/` 或 `docs/design/`
   - 例：系统架构、ADR、UI设计

**示例判断**：
- ✅ `docs/analysis/TEAM_DOCS_STRUCTURE_ANALYSIS.md` → 分析报告，属于项目文档
- ✅ `docs/reports/quality/TEAM_DOCS_REFACTORING_SUMMARY_2026-02-24.md` → 质量报告，属于项目文档
- ✅ `docs/history/TEAM_REVIEW_SUMMARY.md` → 项目历史中的团队评审记录
- ❌ `.claude/team/teamagents.md` → 团队配置，不属于项目文档
- ❌ `.claude/team/execution/TEAM_COLLABORATION_RULES.md` → 协作规则，不属于项目文档

### 📊 关于 "TEAM" 相关文档的说明

在 `docs/` 中发现多个包含 "TEAM" 的文件，这些文件**正确地位于项目文档中**：

| 文件 | 类型 | 位置说明 |
|------|------|---------|
| `docs/analysis/TEAM_DOCS_*.md` | 分析报告 | 分析关于团队文档的组织结构（项目研究） |
| `docs/reports/quality/TEAM_DOCS_*.md` | 质量报告 | 团队文档优化的质量报告（项目记录） |
| `docs/history/TEAM_REVIEW_SUMMARY.md` | 项目历史 | 2026-02-20 团队评审的项目历史记录 |

**判断依据**：这些文档是**关于**团队的分析/报告，而不是**团队的运营配置**。它们记录了项目过程中对团队工作的分析和优化，属于项目历史的一部分。

### 🔗 相关目录

- **团队配置**: [.claude/team/](../.claude/team/) - AI 团队管理配置
- **技能定义**: [.claude/skills/](../.claude/skills/) - AI 技能集定义

---

## 📑 文档分类

### 🏛️ 架构与设计
- [ADR - 架构决策记录](adr/) - 技术决策记录
- [架构设计](architecture/) - 系统架构文档
- [功能设计](design/) - 功能和系统设计
- [技术分析](analysis/) - 技术分析文档
  - [团队文档整合分析](analysis/TEAMAGENTS_WORDLAND_INTEGRATION_ANALYSIS.md) - teamagents.md 与 wordland/README.md 整合分析 (2026-02-23)

### 🎮 游戏设计
- [游戏系统设计](design/game/) - 游戏机制、成就系统、防沉迷设计
  - [游戏机制需求](design/game/01-game-mechanics.md) - Spell Battle, Quick Judge, 记忆算法, 提示系统, 连击系统, 星级评分
  - [成就系统设计](design/game/ACHIEVEMENT_SYSTEM_DESIGN.md) - 成就机制设计
  - [Quick Judge机制](design/game/quick_judge_mechanics.md) - 快速评判机制设计
  - [统计成就设计](design/game/statistics_achievements_design.md) - 统计系统设计
- [教育系统](education/) - 教育系统和记忆算法
- [游戏需求优先级](planning/ONBOARDING_GAME_MODES_PRIORITY.md) - Onboarding游戏模式优先级分析

### 🎨 UI/UX 设计
- [UI/UX 设计需求](design/ui/02-ui-ux.md) - 地图系统, 视觉反馈, 语境化学习, 探索机制, 儿童友好设计, 动画过渡
- [HintCard UI 组件设计](design/ui/HINTCARD_UI_DESIGN.md) - 提示卡片 UI 组件设计规范 (2026-02-24)

### 🏗️ 系统设计
- [技术需求](design/system/08-technical.md) - 性能要求, 兼容性, 质量标准, 数据库架构, API设计, 安全要求

### 💻 开发指南
- [开发规范](development/) - 编码标准和最佳实践
- [开发指南](guides/) - 各类开发指南
  - [快速开始](guides/quick-start/) - 快速入门指南
  - [测试指南](guides/testing/) - 测试相关指南
    - [截图测试指南](guides/testing/SCREENSHOT_TESTING_GUIDE.md) - Epic #12 截图测试完整指南 ✨
    - [CI/CD 配置指南](guides/testing/EPIC12_CI_CD_GUIDE.md) - Epic #12 CI/CD 配置 ✨
    - [故障排查指南](guides/testing/EPIC12_TROUBLESHOOTING_GUIDE.md) - Epic #12 故障排查 ✨
    - [最佳实践](guides/testing/EPIC12_BEST_PRACTICES.md) - Epic #12 测试最佳实践 ✨
    - [培训材料](guides/testing/EPIC12_TRAINING_MATERIALS.md) - Epic #12 团队培训资料 ✨
  - [开发指南](guides/development/) - 开发工具和流程
  - [故障排查](guides/troubleshooting/) - 问题排查指南

### 🧪 测试文档
- [测试策略](testing/) - 测试方法论和策略
  - [测试策略](testing/strategy/) - 测试策略和覆盖率
  - [测试方法](testing/methodology/) - 测试方法论
  - [检查清单](testing/checklists/) - 测试检查清单

### 📊 项目报告
- [测试报告](reports/testing/) - 各类测试报告
- [性能报告](reports/performance/) - 性能分析和优化报告
- [质量报告](reports/quality/) - 项目质量报告
- [UI集成测试](reports/ui-integration/) - UI集成测试报告
- [优先级分析报告](reports/PRIORITY_ANALYSIS_REPORT.md) - P0/P1/P2 需求优先级分析 (2026-02-20)

### 👥 团队协作
团队协作文档已移至配置层：[.claude/team/](../.claude/team/)
- [团队协作文档](.claude/team/README.md) - 团队概述、成员、工作流程、快速导航
- [框架集成指南](.claude/team/FRAMEWORK_INTEGRATION.md) - E-P-E-R + Team Operations 框架集成（630+行完整指南）⭐
- [状态持久化规则](.claude/team/STATE_PERSISTENCE_RULES.md) - 15分钟同步规则（CRITICAL）⭐

#### 执行指南
- [协作规则](.claude/team/execution/TEAM_COLLABORATION_RULES.md) - 沟通规范、角色边界
- [最佳实践](.claude/team/execution/TEAM_COLLABORATION_BEST_PRACTICES.md) - 任务管理、代码质量、测试策略（15,000+行）
- [会话结束检查清单](.claude/team/execution/SESSION_END_CHECKLIST.md) - 每日工作结束前检查

#### 历史记录 & 会议记录
- [团队状态历史](.claude/team/history/) - EPER Iteration 计划、团队状态
- [会议记录](.claude/team/meetings/) - 启动会、站会、回顾会

### 📜 历史记录
- [迭代记录](history/EPER_Iteration1/) - EPER Iteration 1 执行记录（30+报告）
- [实施历史](history/implementation/) - 开发实施历史（8周周报）
- [里程碑](history/milestones/) - 项目里程碑文档
- [计划阶段观察](history/plan-phase-observations/) - 2026-02-16 团队各角色观察报告

### 📄 模板
- [文档模板](templates/) - 各类文档模板

## 🎯 快速导航

### 常用文档
- [项目主 README](../README.md) - 项目概述和快速开始
- [开发标准](development/DEVELOPMENT_STANDARDS.md) - 编码规范
- [代码审查检查清单](guides/CODE_REVIEW_CHECKLIST.md) - Code Review 指南
- [团队协作规则](.claude/team/execution/TEAM_COLLABORATION_RULES.md) - 团队工作流程
- [测试策略](testing/strategy/TEST_STRATEGY.md) - 测试方法论
- [性能基线](reports/performance/PERFORMANCE_BASELINE.md) - 性能标准

### 核心设计文档
- [游戏设计分析](game/GAME_DESIGN_ANALYSIS_2026-02-18.md) - 游戏设计总览
- [成就系统设计](design/game/ACHIEVEMENT_SYSTEM_DESIGN.md) - 成就机制
- [防沉迷系统设计](design/game/ANTI_ADDICTION_SYSTEM_DESIGN.md) - 防沉迷机制
- [提示系统设计](design/system/SOCRATIC_HINT_SYSTEM_DESIGN.md) - 提示系统
- [教育系统](education/) - 教育相关文档

### 技术文档
- [架构决策 001: 使用 Service Locator](adr/001-use-service-locator.md)
- [架构决策 002: Hilt 兼容性](adr/002-hilt-compatibility.md)
- [提示系统集成指南](guides/HINT_SYSTEM_INTEGRATION.md) - 提示系统实现
- [性能优化报告](reports/performance/PERFORMANCE_OPTIMIZATION_REPORT.md) - 性能优化总结

## 📂 目录结构说明

```
docs/
├── adr/                    # 架构决策记录 (Architecture Decision Records)
├── architecture/           # 架构设计文档
│   └── reports/           # 架构报告
├── analysis/              # 技术分析文档
├── design/                # 功能和系统设计
│   ├── game/             # 游戏系统设计（机制、成就、防沉迷）
│   ├── system/           # 系统设计
│   └── ui/               # UI设计
├── development/           # 开发规范
├── education/             # 教育系统
├── guides/                # 开发指南
│   ├── quick-start/      # 快速开始
│   ├── testing/          # 测试指南
│   ├── development/      # 开发工具
│   └── troubleshooting/ # 故障排查
├── history/               # 项目历史
│   ├── EPER_Iteration1/  # 迭代1记录
│   ├── implementation/   # 实施周报
│   ├── milestones/       # 里程碑
│   └── plan-phase-observations/ # 计划阶段观察（2026-02-16）
├── planning/              # 计划文档
│   └── EPER_Iteration2/  # 迭代2计划
├── reports/               # 各类报告
│   ├── architecture/     # 架构报告（已迁移到 architecture/reports/）
│   ├── bugfixes/         # Bug修复报告
│   ├── game/             # 游戏需求报告
│   ├── issues/           # 问题报告
│   ├── performance/      # 性能报告
│   ├── quality/          # 质量报告
│   ├── testing/          # 测试报告
│   └── ui-integration/   # UI集成测试
├── requirements/          # 需求文档
│   └── content/          # 内容需求（词汇等）
├── testing/               # 测试策略和规范
│   ├── strategy/         # 测试策略
│   ├── methodology/      # 测试方法
│   └── checklists/       # 检查清单
└── requirements/          # 需求文档（已在上方）
```

## 🔍 查找文档

### 按主题查找
- **架构和技术**: [ADR](adr/) → [架构设计](architecture/) → [技术分析](analysis/)
- **游戏设计**: [游戏设计](game/) → [游戏系统](design/game/) → [教育系统](education/)
- **开发相关**: [开发规范](development/) → [开发指南](guides/) → [团队协作](team/)
- **测试相关**: [测试策略](testing/) → [测试指南](guides/testing/) → [测试报告](reports/testing/)
- **性能相关**: [性能报告](reports/performance/) → [性能基线](reports/performance/PERFORMANCE_BASELINE.md)

### 按文档类型查找
- **设计文档**: [design/](design/), [game/](game/), [education/](education/)
- **指南文档**: [guides/](guides/), [development/](development/)
- **报告文档**: [reports/](reports/)
- **测试文档**: [testing/](testing/), [reports/testing/](reports/testing/)
- **决策记录**: [adr/](adr/)

---

## 📝 文档规范

### 文档命名

- 使用英文大写蛇形命名：`DOCUMENT_NAME.md`
- 报告包含日期：`TEST_REPORT_2026-02-16.md`
- ADR 包含编号：`001-description.md`

### 文档状态

每个文档应包含元数据：
```markdown
**版本**: 1.0
**日期**: 2026-02-16
**状态**: 草稿/审核中/已发布/已废弃
```

### 文档更新

1. 更新文档时，更新版本号和日期
2. 旧版本归档：
   - 项目文档 → 归档到 `docs/history/` 相应子目录
   - 团队配置 → 归档到 `.claude/team/history/`
3. 重要更新在文档底部添加变更记录

---

## 🤝 贡献文档

### 添加新文档

1. 确定文档类型和位置
2. 使用[文档模板](./templates/)
3. 遵循命名规范
4. 添加到本导航文档

### 更新现有文档

1. 更新版本号
2. 添加更新日期
3. 在文档底部记录变更

### 归档旧文档

当文档过时：
1. **项目文档**：移动到 `docs/history/` 相应子目录（如 milestones/、implementation/）
2. **团队配置**：移动到 `.claude/team/history/`
3. 更新链接指向新文档
4. 在归档目录添加说明

---

## 📊 文档统计

| 类型 | 数量 | 位置 |
|------|------|------|
| 开发文档 | 4 | `docs/development/` |
| 操作指南 | 6 | `docs/guides/` |
| 测试报告 | 6 | `docs/reports/testing/` |
| 问题报告 | 5 | `docs/reports/issues/` |
| 架构报告 | 2 | `docs/architecture/reports/` |
| 项目历史 | 53 | `docs/history/` |
| 团队状态历史 | 5 | `.claude/team/history/` |
| 架构决策 | 2 | `docs/adr/` |

---

## 🔗 相关资源

### 项目文档
- [CLAUDE.md](../CLAUDE.md) - AI 助手项目说明
- [README.md](../README.md) - 项目简介（如存在）

### 脚本工具
- [构建脚本](../scripts/build/) - APK 构建和安装
- [测试脚本](../scripts/test/) - 自动化测试
- [工具脚本](../scripts/utils/) - 诊断工具

### 外部资源
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

---

## 📞 获取帮助

### 文档问题
如果文档有错误或不清楚：
1. 检查文档是否是最新的
2. 查看[问题报告](./reports/issues/)是否有相关说明
3. 查看[架构决策](./adr/)了解背景

### 技术问题
如果是技术实现问题：
1. 查看[开发文档](./development/)
2. 查看[操作指南](./guides/)
3. 查看[问题报告](./reports/issues/)

---

**最后更新**: 2026-02-23
**维护者**: Wordland 开发团队
