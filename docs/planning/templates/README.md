# Planning Templates 目录

**目录说明**: 本目录包含项目规划和执行的模板文档，供未来Iteration和Epic参考。

**创建日期**: 2026-02-25
**来源**: 从docs/history/移动的EPER_Iteration1计划文档

---

## 📚 可用模板

### 1. EPER_ITERATION1_PLAN_TEMPLATE.md

**来源**: EPER_Iteration1 (2026-02-15 ~ 2026-02-17)
**大小**: 10KB
**用途**: Sprint/Iteration计划模板

**包含内容**:
- ✅ Sprint目标定义
- ✅ 成功指标设定
- ✅ Epic分解示例
- ✅ Story分解示例
- ✅ 验收标准示例
- ✅ 时间估算示例

**适用场景**:
- 规划新的Iteration
- 定义Epic和Story
- 设定成功指标
- 编写验收标准

**使用方法**:
```bash
# 复制模板
cp docs/planning/templates/EPER_ITERATION1_PLAN_TEMPLATE.md \
   docs/planning/iterations/EPER_Iteration3/EPER_ITERATION3_PLAN.md

# 编辑内容
vim docs/planning/iterations/EPER_Iteration3/EPER_ITERATION3_PLAN.md
```

---

### 2. EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md

**来源**: EPER_Iteration1 (2026-02-15 ~ 2026-02-17)
**大小**: 29KB
**用途**: 详细执行计划模板

**包含内容**:
- ✅ 执行决策流程
- ✅ P0关键问题修复计划
- ✅ 团队任务分配方法
- ✅ 每日工作计划示例
- ✅ 风险缓解策略
- ✅ 成功指标和验收标准

**适用场景**:
- 制定详细执行计划
- 团队任务分配
- 风险识别和缓解
- 每日工作计划

**使用方法**:
```bash
# 复制模板
cp docs/planning/templates/EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md \
   docs/planning/epics/Epic10/EPIC10_EXECUTION_PLAN.md

# 编辑内容
vim docs/planning/epics/Epic10/EPIC10_EXECUTION_PLAN.md
```

---

## 🎯 如何使用模板

### 步骤1: 选择合适的模板

**场景A: 规划新的Iteration**
- 使用: `EPER_ITERATION1_PLAN_TEMPLATE.md`
- 包含: Epic分解、Story分解、验收标准

**场景B: 规划新的独立Epic**
- 使用: `EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md`
- 包含: 执行决策、任务分配、风险管理

**场景C: 两者结合**
- 先用计划模板定义Epic
- 再用执行模板制定详细计划

### 步骤2: 复制模板到目标位置

```bash
# 示例: 为Epic 10创建执行计划
mkdir -p docs/planning/epics/Epic10
cp docs/planning/templates/EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md \
   docs/planning/epics/Epic10/EPIC10_EXECUTION_PLAN.md
```

### 步骤3: 编辑模板内容

**关键替换项**:
- `{Sprint周期}` → 实际时间范围
- `{Epic编号}` → 实际Epic编号
- `{Epic名称}` → 实际Epic名称
- `{负责人}` → 实际负责人
- `{时间估算}` → 实际估算

**示例**:
```markdown
# Epic #10: Listen Find - 听音选词模式

**Sprint周期**: 2周 (2026-03-10 至 2026-03-24)
**负责人**: android-engineer + compose-ui-designer
**目标**: 实现听音选词游戏模式

## 🎯 Epic目标

### 主要目标
1. 音频系统集成
2. 听音选词UI实现
3. ...
```

### 步骤4: 根据实际情况调整

**可以调整的内容**:
- Epic数量（模板有2个，可以增减）
- Story粒度（模板较细，可以简化）
- 验收标准（根据实际情况修改）
- 时间估算（根据团队能力调整）

---

## 📋 模板结构说明

### EPER_ITERATION1_PLAN_TEMPLATE.md 结构

```markdown
# Sprint X 实施计划

## 🎯 Sprint目标
### 主要目标
### 成功指标

## 📋 Sprint Backlog
### Epic #X: 名称
#### Story #X.1: 任务
#### Story #X.2: 任务
...

## 📅 时间线
## 👥 团队分配
## 🎯 验收标准
## ⚠️ 风险和缓解
```

### EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md 结构

```markdown
# Sprint X 详细执行计划

## 执行决策
## P0关键问题修复计划
## Sprint X调整后计划
## 团队任务分配
## 每日工作计划
## 风险缓解策略
## 成功指标和验收标准
```

---

## ✅ 模板最佳实践

### 1. Epic规划

**DO ✅**:
- 明确Epic目标和价值
- 合理分解Story（1-3天完成）
- 设定清晰的验收标准
- 估算准确的时间

**DON'T ❌**:
- 分解过细（Story < 0.5天）
- 目标模糊或不明确
- 验收标准无法验证
- 忽略风险评估

### 2. 执行计划

**DO ✅**:
- 考虑团队能力和资源
- 识别关键风险并制定缓解措施
- 分配合理的每日任务
- 设定可测量的成功指标

**DON'T ❌**:
- 过度乐观的时间估算
- 忽视依赖关系
- 缺少风险应对
- 团队分配不合理

### 3. 文档维护

**DO ✅**:
- 定期更新计划状态
- 记录实际vs计划的偏差
- 及时更新风险和问题
- 完成后归档到history/

**DON'T ❌**:
- 计划过时后不更新
- 缺少状态跟踪
- 完成后不总结经验

---

## 🔗 相关文档

### 模板来源

**EPER_Iteration1** (2026-02-15 ~ 2026-02-17)
- 包含Epic #1: 视觉反馈增强
- 包含Epic #2: 地图系统重构
- 完成度: 100%
- 状态: ✅ 完成

**详细记录**: `docs/history/EPER_Iteration1/`

### 使用这些模板的Epic

- ✅ EPER_Iteration1 (来源)
- 🔄 EPER_Iteration2 (参考)
- 🔄 Epic #9 (参考)

### 其他参考文档

- **Planning README**: `docs/planning/README.md`
- **Epic索引**: `docs/planning/EPIC_INDEX.md`
- **路线图索引**: `docs/planning/ROADMAP_INDEX.md`

---

## 📝 自定义和扩展

### 创建新模板

**场景**: 需要特定类型的模板（如测试计划、UI设计计划）

**步骤**:
1. 从现有模板复制
2. 根据需求调整结构
3. 添加特定章节
4. 更新本README

**示例**:
```bash
# 创建测试计划模板
cp docs/planning/templates/EPER_ITERATION1_PLAN_TEMPLATE.md \
   docs/planning/templates/TEST_PLAN_TEMPLATE.md

# 编辑为测试计划模板
vim docs/planning/templates/TEST_PLAN_TEMPLATE.md
```

### 模板版本管理

**当前版本**: v1.0 (基于EPER_Iteration1)

**未来改进**:
- 根据使用反馈优化
- 增加更多模板类型
- 统一模板格式
- 添加模板使用示例

---

## ⚠️ 注意事项

### 模板局限性

1. **时间估算**: 基于EPER_Iteration1的团队配置，可能不适用其他团队
2. **Epic数量**: 模板有2个Epic，实际可以增减
3. **Story粒度**: 模板的Story较细，可根据项目调整
4. **技术栈**: 模板基于Android/Kotlin，其他技术需调整

### 使用建议

1. **不要盲目复制**: 根据实际情况调整
2. **保持灵活性**: 模板是参考，不是规则
3. **及时反馈**: 使用后总结经验，优化模板
4. **持续改进**: 随着项目演进，模板也应演进

---

## 🎯 快速开始

### 创建新的Iteration计划

```bash
# 1. 创建目录
mkdir -p docs/planning/iterations/EPER_Iteration3

# 2. 复制模板
cp docs/planning/templates/EPER_ITERATION1_PLAN_TEMPLATE.md \
   docs/planning/iterations/EPER_Iteration3/EPER_ITERATION3_PLAN.md

# 3. 编辑计划
vim docs/planning/iterations/EPER_Iteration3/EPER_ITERATION3_PLAN.md
```

### 创建新的Epic执行计划

```bash
# 1. 创建目录
mkdir -p docs/planning/epics/Epic10

# 2. 复制模板
cp docs/planning/templates/EPER_ITERATION1_EXECUTION_PLAN_TEMPLATE.md \
   docs/planning/epics/Epic10/EPIC10_EXECUTION_PLAN.md

# 3. 编辑计划
vim docs/planning/epics/Epic10/EPIC10_EXECUTION_PLAN.md
```

---

**模板维护**: Team Lead
**更新频率**: 根据需要更新
**反馈**: 请将使用经验反馈给Team Lead，以便持续改进模板
