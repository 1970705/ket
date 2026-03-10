# 需求评审结果 - 团队共识与实施计划

**日期**: 2026-02-25
**参与成员**: 5/7 (android-test-engineer, android-performance-expert, android-engineer, game-designer, compose-ui-designer)
**状态**: ✅ 团队评审完成，待另外2位成员补充

---

## 🎯 需求概述

### 原始需求
1. **幽默搞笑基调** - 整体产品风格调整为轻松幽默
2. **看错题模式** - 社区化学习，展示他人错误答案
3. **单词消消乐** - 单词配对消除游戏模式

---

## 📊 团队评审共识

### ✅ 一致同意的优先级排序

| 优先级 | 需求 | 投票理由 |
|--------|------|----------|
| **P1** | **单词消消乐** | 5/5成员投票 - 技术可行、用户价值高、无需后端 |
| **P2** | **幽默搞笑基调** | 5/5成员投票 - 影响整体体验、工作量可控 |
| **P3** | **看错题模式** | 5/5成员投票 - 需要后端、隐私风险高、建议延后 |

### 📈 工作量估计（团队平均）

| 需求 | 测试工程师 | 性能专家 | 工程师 | 游戏设计师 | UI设计师 | **平均** |
|------|----------|----------|--------|----------|---------|---------|
| 幽默基调 | 7人天 | 3-5人天 | 5-8人天 | 5-8人天 | 8-10人天 | **5-7人天** |
| 看错题模式 | 28人天 | 15-20人天 | 15-20人天 | 15-20人天 | 5-7人天 | **18-22人天** |
| 单词消消乐 | 12人天 | 8-12人天 | 10-12人天 | 8-12人天 | 10-12人天 | **9-12人天** |

**总工作量**: 32-41 人天

---

## 📋 详细实施方案

### P1: 单词消消乐 ⭐⭐⭐

**优先级**: P1（最高）
**工作量**: 9-12 人天
**技术可行性**: ✅ 高
**用户价值**: ✅ 明确

#### 核心功能

1. **游戏模式**
   - 单词-中文翻译配对消除
   - 随机泡泡颜色（粉、绿、紫、橙、棕、蓝）
   - 点击配对 → 匹配成功消除，匹配失败抖动
   - 实时计时器
   - 完成后显示用时和继续挑战

2. **UI组件**
   ```kotlin
   - WordBubble.kt - 3D泡泡组件
   - GameBoard.kt - 游戏棋盘（LazyVerticalGrid）
   - MatchGameScreen.kt - 游戏主界面
   - MatchGameViewModel.kt - 游戏逻辑
   ```

3. **特色功能**
   - 5-50对单词可调节滑块
   - 导入自定义词表（Excel）
   - 计时挑战模式
   - 连击奖励和特效

#### 技术实现要点

**性能优化** (android-performance-expert):
```kotlin
// 使用LazyVerticalGrid避免性能问题
LazyVerticalGrid(
    columns = GridCells.Fixed(6),
    modifier = Modifier.fillMaxSize()
) {
    items(wordBubbles, key = { it.id }) { bubble ->
        WordBubble(bubble = bubble)
    }
}
```

**动画效果** (compose-ui-designer):
- 选中: 放大 + 发光
- 匹配成功: 泡泡爆炸 💥
- 匹配失败: 红色抖动 ❌
- 使用 Compose Animation 实现

**数据流**:
```
WordRepository → GetWordsUseCase → MatchGameViewModel
↓
选词逻辑 → 打乱顺序 → 显示泡泡
↓
用户交互 → 配对检查 → 状态更新 → UI更新
```

#### 工作拆解

| 任务 | 负责角色 | 人天 |
|------|----------|------|
| 游戏逻辑设计 | game-designer | 2人天 |
| UI组件实现 | compose-ui-designer | 4人天 |
| ViewModel实现 | android-engineer | 2人天 |
| 单元测试 | android-test-engineer | 2人天 |
| 性能优化 | android-performance-expert | 1人天 |
| 真机测试 | android-test-engineer | 1人天 |
| **总计** | | **12人天** |

#### 实施阶段

**Phase 1: MVP** (5人天)
- 基础配对逻辑
- 简单UI（固定颜色，无动画）
- 本地词库支持

**Phase 2: 动画与特效** (4人天)
- 3D泡泡效果
- 消除动画
- 音效集成

**Phase 3: 高级功能** (3人天)
- 计时挑战模式
- 自定义词表导入
- 关卡系统

#### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 性能问题（100个泡泡） | 中 | 使用LazyGrid，限制同时显示数量 |
| 低端设备掉帧 | 中 | 性能分级，低端设备简化动画 |
| 动画卡顿 | 低 | Macrobenchmark测试，使用高性能API |

---

### P2: 幽默搞笑基调 ⭐⭐

**优先级**: P2
**工作量**: 5-7 人天
**技术可行性**: ✅ 高
**用户价值**: ✅ 改善体验

#### 核心改进

1. **文案幽默化**
   ```
   成功: "太棒了！" → "哇！你简直是个天才！🧠"
   失败: "继续加油" → "没关系，谁都有脑子短路的时候😅"
   连击: "Nice!" → "你在燃烧！🔥unstoppable!"
   ```

2. **反馈动画**
   - 答对: 搞笑emoji动画 (🎉🤪✨)
   - 答错: 可爱的错误提示 (😅💦)
   - 连击: 夸张的粒子效果

3. **宠物系统升级**
   - CompactPetAnimation增加搞笑表情
   - 根据表现变化情绪 (开心/失望/加油)

#### 工作拆解

| 任务 | 负责角色 | 人天 |
|------|----------|------|
| 文案改写 | game-designer + education-specialist | 2人天 |
| UI动画实现 | compose-ui-designer | 3人天 |
| 宠物表情系统 | compose-ui-designer | 2人天 |
| 测试 | android-test-engineer | 1人天 |
| **总计** | | **8人天** |

#### 实施建议

**渐进式实现** (compose-ui-designer):
- 先在Spell Battle模式中加入幽默元素
- 收集用户反馈
- 逐步推广到其他模式

**可配置选项** (android-test-engineer):
- 提供"严肃模式"和"幽默模式"切换
- 允许家长选择适合孩子的风格

#### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 过度娱乐化影响学习 | 中 | 保留教育核心，幽默作为调味 |
| 文案可能不适宜儿童 | 低 | 内容审核机制 |
| 分散注意力 | 低 | A/B测试，收集数据 |

---

### P3: 看错题模式 ⭐

**优先级**: P3
**工作量**: 18-22 人天
**技术可行性**: ⚠️ 中（需要后端）
**用户价值**: ✅ 有教育价值

#### 核心功能

1. **错题展示**
   - 卡片式展示其他用户的错误答案
   - 显示题目 + 错误答案 + 搞笑评论
   - 正确答案可点击显示
   - 点赞机制（"最搞笑错误"投票）

2. **社区功能**
   - 用户答题错误自动上传（可选）
   - 内容审核机制（人工或AI）
   - 隐私保护（匿名或实名）

#### 工作拆解

| 任务 | 负责角色 | 人天 |
|------|----------|------|
| 后端API设计 | android-architect | 5人天 |
| 数据模型设计 | android-architect | 3人天 |
| 前端UI实现 | compose-ui-designer | 5人天 |
| ViewModel实现 | android-engineer | 4人天 |
| 内容审核机制 | android-engineer | 3人天 |
| 测试 | android-test-engineer | 5人天 |
| **总计** | | **25人天** |

#### 实施建议

**MVP优先** (所有成员建议):
- **Phase 1**: 本地版（无后端）
  - 展示预设的搞笑错题
  - 验证教育价值
  - 收集用户反馈

- **Phase 2**: 社区版（有后端）
  - 实现后端API
  - 用户上传功能
  - 审核机制

#### 架构设计

**数据模型**:
```kotlin
// 错题提交实体
data class WrongAnswerSubmission(
    val submissionId: String,
    val wordId: String,
    val wrongAnswer: String,
    val userId: String, // 可匿名
    val isAnonymous: Boolean,
    val timestamp: Long,
    val status: SubmissionStatus // PENDING, APPROVED, REJECTED
)

// 错题展示实体
data class MistakeCard(
    val word: String,
    val correctAnswer: String,
    val wrongAnswer: String,
    val funnyComment: String,
    val upvotes: Int,
    val isAnonymous: Boolean
)
```

#### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 不当内容传播 | **高** | 人工审核 + AI过滤 + 举报机制 |
| 用户隐私问题 | **高** | 严格脱敏、可选参与、家长控制 |
| 后端成本 | 中 | MVP验证后再投入 |
| 内容质量控制 | 中 | 审核团队 + 社区举报 |

---

## 🎯 实施计划

### Phase 1: 单词消消乐 (P1) - 优先实现

**时间**: 12人天 (约2-3周)
**目标**: 验证新游戏模式的用户价值

**关键里程碑**:
- Day 5: MVP完成（基础配对）
- Day 9: 动画和音效完成
- Day 12: 完整功能上线

---

### Phase 2: 幽默搞笑基调 (P2) - 并行实施

**时间**: 8人天 (约1-2周)
**目标**: 改善整体用户体验

**关键里程碑**:
- Day 3: 文案改写完成
- Day 6: 动画效果实现
- Day 8: 全模式上线

---

### Phase 3: 看错题模式 (P3) - 长期规划

**前置条件**: 单词消消乐验证成功
**时间**: 25人天 (约3-4周)
**建议**: 作为独立Epic规划

---

## 📚 相关文档

### 原始需求
- `docs/requirements/addtionalreq.md`

### 参考文档
- `docs/requirements/03-game-modes.md` - 游戏模式文档
- `docs/requirements/04-achievements.md` - 成就系统
- `docs/reports/testing/EPIC5_ALGORITHM_ISSUES_TODO.md` - Epic #5待修复问题

---

## ✅ 下一步行动

### 立即行动
1. ✅ 团队评审完成
2. ⏳ 等待另外2位成员的补充意见
3. ⏳ 更新原始需求文档
4. ⏳ 创建详细的Epic规划

### 建议的Epic划分

**Epic #9: 单词消消乐模式**
- 优先级: P1
- 工作量: 12人天
- 目标: 新增单词配对消除游戏

**Epic #10: 幽默搞笑基调**
- 优先级: P2
- 工作量: 8人天
- 目标: 整体产品风格优化

**Epic #11: 看错题社区模式**
- 优先级: P3
- 工作量: 25人天
- 目标: 社区化学习功能（需要后端）

---

**报告版本**: 1.0
**创建日期**: 2026-02-25
**状态**: ✅ 团队评审完成
**下次更新**: 等待education-specialist和android-architect补充意见
