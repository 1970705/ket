# Epic #7 测试覆盖率提升计划

**Plan ID**: EPIC7-2026-001
**Version**: 1.0
**Date**: 2026-03-01
**Status**: 📋 Pending Approval
**Framework**: E-P-E-R

---

## 📋 Executive Summary

**目标**: 将测试覆盖率从 22% 提升至 60%+，为 FUTURE_WORKFLOW 质量目标奠定基础

**范围**:
- UI 组件测试（ui.components: 0% → 50%）
- UI Screen 测试（ui.screens: 0% → 60%）
- ViewModel 测试补充（ui.viewmodel: 88% → 90%）

**预期时间**: 16-20 小时
**优先级**: P0 (Critical)
**责任人**: android-test-engineer

**ROI**: 为后续所有 Epic 提供质量保障，减少 UI bug 发现时间

---

## 🎯 调整后的 Epic 优先级

根据 FUTURE_WORKFLOW 的**质量优先**和**快速胜利**原则：

| Epic | 原优先级 | 新优先级 | 调整理由 |
|------|----------|----------|----------|
| **#7** | P0 | **P0** ⭐ | 直接关联测试覆盖率目标（22% → 80%） |
| **#10** | P1 | **P1** | 新游戏模式，价值中等 |
| **#6** | P1 | **P2** | 音频系统是功能增强，可后置 |

---

## 📊 当前测试覆盖率状况

| 模块 | 当前覆盖率 | 目标覆盖率 | 缺口 |
|------|-----------|-----------|------|
| ui.components | 0% | 50% | 50% |
| ui.screens | 0% | 60% | 60% |
| ui.viewmodel | 88% | 90% | 2% |
| domain.model | 82% | 85% | 3% |
| **total** | **22%** | **60%+** | **38%+** |

---

## 🚀 执行计划

### Task 7.1: UI 组件测试 (8-10 小时)

**优先级**: P0
**目标覆盖率**: 50%+

**组件列表**:
1. **HintCard** (P0) - 多级提示、按钮状态、计数器
2. **SpellBattleGame** (P0) - 虚拟键盘、答案输入、验证
3. **BubbleTile** (P1) - 泡泡渲染、选择状态、动画
4. **LevelProgressBarEnhanced** (P1) - 进度显示、星级显示
5. **WordlandButton** (P2) - 按钮样式、点击事件
6. **WordlandCard** (P2) - 卡片布局、内容显示

**验收标准**:
- 每个组件 ≥ 60% 覆盖率
- 所有交互场景已测试

---

### Task 7.2: UI Screen 测试 (6-8 小时)

**优先级**: P0
**目标覆盖率**: 60%+

**Screen 列表**:
1. **LearningScreen** (P0) - 游戏流程、状态转换、提示集成
2. **MatchGameScreen** (P0) - 游戏启动、泡泡交互、配对逻辑
3. **HomeScreen** (P1) - 导航、岛屿显示、进度显示
4. **IslandMapScreen** (P1) - 岛屿选择、解锁逻辑
5. **LevelSelectScreen** (P2) - 关卡列表、选择逻辑

**验收标准**:
- 每个 Screen ≥ 60% 覆盖率
- 主要用户流程覆盖

---

### Task 7.3: ViewModel 测试补充 (2-3 小时)

**优先级**: P1
**目标覆盖率**: 88% → 90%+

**目标 ViewModel**:
1. **LearningViewModel** - 空列表、网络错误、边界值
2. **MatchGameViewModel** - 异常状态、超时处理

---

### Task 7.4: CI/CD 集成 (1-2 小时)

**优先级**: P1

**更新内容**:
- GitHub Actions 配置
- 覆盖率阈值设置（60%）
- 集成测试验证

---

### Task 7.5: 文档和报告 (1-2 小时)

**优先级**: P2

**交付文档**:
1. UI_TESTING_GUIDE.md
2. EPIC7_COMPLETION_REPORT.md
3. CLAUDE.md 更新 (v1.8)

---

## 📈 成功指标

| 指标 | 基线 | 目标 |
|------|------|------|
| 测试覆盖率 | 22% | 60%+ |
| UI 组件覆盖率 | 0% | 50%+ |
| UI Screen 覆盖率 | 0% | 60%+ |
| 新增测试数 | 0 | 100+ |

---

## ✅ 验收标准

- [ ] 测试覆盖率 ≥ 60%
- [ ] 所有新测试通过
- [ ] 真机测试通过
- [ ] CI/CD 集成完成
- [ ] 文档完整

---

## 📞 下一步

**等待用户确认后执行**

1. 启动 Epic #7 团队
2. 分配任务给 android-test-engineer
3. 开始 UI 组件测试

---

**计划创建**: 2026-03-01
**状态**: ⏸️ Pending Approval
**预计开始**: 待确认
