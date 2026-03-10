# Wordland 框架集成完成指南

## ✅ 集成状态

plan-execute-review 框架已成功集成到 Wordland 项目。

### 已完成的工作

```
.claude/
├── skills/
│   ├── plan-execute-review/     # ✅ 通用框架（已存在）
│   └── wordland/               # ✅ Wordland 领域实现（新建）
│       ├── README.md            # ✅ 技能集说明
│       ├── skills/             # ✅ 领域技能
│       │   ├── game-designer.md
│       │   ├── education-specialist.md
│       │   └── python-developer.md
│       └── adapters/           # ✅ 适配器层
│           ├── game-designer-adapter.md
│           ├── education-specialist-adapter.md
│           └── python-developer-adapter.md
│
└── workflows/
    └── wordland-workflow.yaml  # ✅ Wordland 工作流配置
```

**总计**: 8个文件创建

---

## 🎯 如何使用框架

### 快速开始

#### 1. ELCITATE 阶段（需求浮现）

当你有新想法或不确定需求时：

```text
请进入 ELICITATE MODE：
- 通过对话探索和理解需求
- 快速生成原型验证假设
- 不急于写具体实现代码
- 需求是浮现的，而非预先定义的

背景：我正在开发 Wordland 游戏的 [具体模块]
```

**示例任务**：
- "探索 Look Island 的 Boss 关卡设计"
- "验证孩子是否会喜欢听音寻宝玩法"

#### 2. PLAN 阶段（规划设计）

当需求清晰，需要制定计划时：

```text
请进入 PLAN MODE：
- 不写代码
- 只输出：目标 / 假设 / 约束 / 任务拆解 / 交付物
- 如果信息不足，请明确列出问题

背景：
激活 skills/wordland/adapters/game-designer-adapter.md

任务：设计 Look Island 的第3个关卡
```

**你会得到**：
- 清晰的目标重述
- 需要验证的假设
- 明确的约束条件
- SMART原则的任务拆解
- 可交付的文档清单

#### 3. EXECUTE 阶段（高效执行）

当计划批准后，新建 Session 执行：

```text
基于以下 Plan 执行：
【粘贴 Plan 总结】

激活 skills/wordland/adapters/education-specialist-adapter.md

约束：
- 不重新设计
- 不扩展范围
- 严格按计划执行
- 遇到阻塞立即停止并报告
```

**根据任务类型选择适配器**：
- 游戏设计任务 → game-designer-adapter
- 教育内容任务 → education-specialist-adapter
- 技术实现任务 → python-developer-adapter

#### 4. REVIEW 阶段（校验总结）

当执行完成后：

```text
请 REVIEW 以下产出：
1. 是否满足原 Plan
2. 是否有明显风险或遗漏
3. 给出改进建议（不重构）
4. 提供 Go/No-Go 决策

激活 skills/wordland/adapters/education-specialist-adapter.md
```

---

## 📋 典型工作流示例

### 示例 1：设计新关卡

**ELICITATE**:
```
探索：如何让孩子学会 look vs see 的区别？
目标：生成一个关卡原型验证假设
```

**PLAN**:
```
激活：game-designer-adapter
任务：设计 Look Island Level 2（Look or See?）
```

**EXECUTE**:
```
激活：education-specialist-adapter
任务：提供 look vs see 的苏格拉底提问模板
```

**REVIEW**:
```
检查：提问是否能帮助孩子理解？
验证：是否符合10岁孩子认知？
```

### 示例 2：实现记忆算法

**ELICITATE**:
```
探索：如何科学地安排复习间隔？
验证：间隔重复算法的有效性
```

**PLAN**:
```
激活：education-specialist-adapter
任务：设计记忆强度模型和复习算法
```

**EXECUTE**:
```
激活：python-developer-adapter
任务：实现记忆巩固算法（Python代码）
```

**REVIEW**:
```
检查：算法是否符合记忆科学原理？
验证：代码是否有测试覆盖？
```

---

## 🔑 核心原则

### Session 管理

1. **一个 Session 只做一件事**
   - ❌ 不要在同一个 Session 从需求聊到上线
   - ✅ ELCITATE → PLAN → EXECUTE → REVIEW 分阶段

2. **Plan 完成后新建 Session**
   - ✅ PLAN 完成后，结束 Session
   - ✅ 新建 Session 时只粘贴 Plan 结论
   - ❌ 不要带着整个对话历史

3. **遇到偏题立即停止**
   - ✅ EXECUTE 时发现需要重新设计 → 停止
   - ✅ 回到 PLAN 阶段重新规划
   - ❌ 不要在 EXECUTE 时边做边改设计

### 协作规则

1. **明确角色职责**
   - 游戏策划：怎么玩
   - 教育专家：学什么、怎么教
   - 开发者：技术实现

2. **使用适配器激活**
   - 每个任务明确使用哪个适配器
   - 提供清晰的背景和约束

3. **可追踪的产出**
   - PLAN 阶段：计划文档
   - EXECUTE 阶段：代码、内容、数据
   - REVIEW 阶段：评审报告

---

## 📚 文档索引

### 框架文档
- `arch-refactor.md` - 框架设计完整规范
- `session-usage-rule.md` - Session 使用规范

### Wordland 文档
- `Wordland_Design_Doc_v2.md` - 产品设计文档
- `.claude/skills/wordland/README.md` - 技能集说明

### 适配器文档
- `game-designer-adapter.md` - 游戏策划适配器
- `education-specialist-adapter.md` - 教育专家适配器
- `python-developer-adapter.md` - Python开发者适配器

---

## 🚀 下一步建议

### 立即可做

1. ✅ **验证框架集成**
   ```bash
   # 检查文件结构
   ls -R .claude/skills/wordland/
   cat .claude/workflows/wordland-workflow.yaml
   ```

2. ✅ **尝试第一个任务**
   - 使用 ELCITATE 模式探索一个设计问题
   - 例如："如何让孩子愿意主动玩？"

3. ✅ **制定开发计划**
   - 使用 PLAN 模式设计第一个关卡
   - 激活 game-designer-adapter

### 短期目标（1-2周）

1. 完成第一个世界的详细设计（Look Island）
2. 实现核心玩法原型（1个关卡）
3. 邀请10岁孩子试玩并收集反馈

### 中期目标（1-2个月）

1. 完成MVP开发（2个世界，180词）
2. 100名种子用户测试
3. 收集数据并优化

---

## ❓ 常见问题

**Q: 什么时候使用 ELCITATE vs PLAN？**

A:
- ELCITATE: 需求不清晰，需要探索和验证
- PLAN: 需求清晰，需要制定执行计划

**Q: EXECUTE 阶段可以修改计划吗？**

A:
- 小调整（如变量名、UI细节）可以
- 大改动（如玩法机制）必须回到 PLAN 阶段

**Q: 如何选择使用哪个适配器？**

A: 根据任务的核心目标：
- 设计/策划类 → game-designer-adapter
- 教育/内容类 → education-specialist-adapter
- 技术/开发类 → python-developer-adapter

**Q: 必须严格按照 4 阶段吗？**

A: 推荐但非强制。关键是：
- 避免单个 Session 过长
- 明确区分"思考"和"执行"
- 可追踪的设计决策

---

## 📧 需要帮助？

如果在使用框架时遇到问题：

1. 查阅 `session-usage-rule.md` 获取最佳实践
2. 参考 `arch-refactor.md` 理解框架设计
3. 查看 TT 项目的适配器示例

---

**框架集成完成日期**: 2026-02-14
**版本**: v1.0
**状态**: ✅ 可用
