# Sprint 2 任务分解和启动清单 📋

**规划日期**: 2026-02-22
**状态**: 🔄 准备启动

---

## 🎯 Sprint 2 核心目标

**主要目标**: 扩展内容到 60 个词汇，实现 Hint 系统和动态评分

**关键指标**:
- 词汇数量: 30 → 60 (+30)
- 关卡数量: 5 → 10 (+5)
- 测试覆盖率: 12% → 50%
- 新功能: Hint 系统、动态评分、音频

---

## 📋 Epic 任务分解

### Epic #3: Make Atoll 内容扩展 📝

**目标**: 添加 Make Atoll 岛屿，30 个新词汇，5 个关卡

**任务列表**:

#### Phase 1: 数据准备 (2 天)
- [ ] **Task #3.1**: 设计 30 个 Make Atoll 词汇
  - 词汇列表：make, create, build, do, fix, cook, clean, wash, draw, paint, write, read, etc.
  - 难度分级：6 个词/关卡 × 5 关卡
  - 翻译和音标准备
  - **负责人**: education-specialist + android-engineer
  - **时间**: 0.5 天

- [ ] **Task #3.2**: 实现 MakeLakeWords.kt
  - 创建 30 个 Word 对象
  - 按关卡分组
  - 数据验证
  - **负责人**: android-engineer
  - **时间**: 0.5 天

- [ ] **Task #3.3**: 实现 MakeLakeSeeder.kt
  - 创建 5 个关卡配置
  - 设置难度参数
  - 关卡解锁逻辑
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 2: UI 开发 (2 天)
- [ ] **Task #3.4**: 更新岛屿选择 UI
  - 添加 Make Atoll 入口
  - 岛屿图标和状态
  - 探索进度显示
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #3.5**: 实现 Make Atoll 关卡选择
  - 5 个关卡列表
  - 关卡状态显示
  - 解锁逻辑集成
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 3: 集成和测试 (2 天)
- [ ] **Task #3.6**: 集成到导航系统
  - 路由配置
  - 参数传递
  - 状态管理
  - **负责人**: android-engineer
  - **时间**: 0.5 天

- [ ] **Task #3.7**: 单元测试
  - MakeLakeWords 测试
  - MakeLakeSeeder 测试
  - 关卡解锁逻辑测试
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

- [ ] **Task #3.8**: 真机测试
  - 完整流程测试
  - 5 个关卡测试
  - 进度保存验证
  - **负责人**: android-test-engineer
  - **时间**: 1 天

**总时间**: 6 天
**优先级**: ⭐⭐⭐ 高
**依赖**: 无

---

### Epic #4: Hint 系统集成 💡

**目标**: 集成已实现的 Hint 系统架构到 UI

**任务列表**:

#### Phase 1: ViewModel 集成 (2 天)
- [ ] **Task #4.1**: 更新 LearningViewModel
  - 注入 UseHintUseCaseEnhanced
  - 实现 useHint() 方法
  - 更新 UiState 包含提示信息
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #4.2**: 实现提示逻辑
  - 提示使用追踪
  - 提示等级管理
  - 冷却时间处理
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 2: UI 更新 (2 天)
- [ ] **Task #4.3**: 更新 HintCard 组件
  - 显示多级提示
  - 提示按钮状态
  - 使用次数显示
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #4.4**: 集成到 LearningScreen
  - HintCard 位置和布局
  - 提示按钮交互
  - 提示显示动画
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 3: 评分惩罚集成 (1 天)
- [ ] **Task #4.5**: 更新 SubmitAnswerUseCase
  - 检测提示使用
  - 应用评分惩罚（-1 星）
  - 记忆强度调整（-50%）
  - **负责人**: android-engineer
  - **时间**: 0.5 天

- [ ] **Task #4.6**: 验证评分逻辑
  - 单元测试
  - 集成测试
  - 真机验证
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

**总时间**: 5 天
**优先级**: ⭐⭐⭐ 关键
**依赖**: Hint 系统架构（已完成）

---

### Epic #5: 动态星级评分算法 ⭐

**目标**: 实现基于表现的动态星级评分（1-3 星）

**任务列表**:

#### Phase 1: 算法设计和实现 (2 天)
- [ ] **Task #5.1**: 设计评分算法
  - 评分因素定义
  - 权重分配
  - 算法公式设计
  - **负责人**: android-engineer + education-specialist
  - **时间**: 0.5 天

- [ ] **Task #5.2**: 实现 StarRatingCalculator
  - 准确率计算
  - 提示惩罚
  - 时间奖励
  - 最终星级计算
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #5.3**: 单元测试
  - 边界测试
  - 算法验证
  - 性能测试
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

#### Phase 2: 集成和 UI (1 天)
- [ ] **Task #5.4**: 集成到 LearningViewModel
  - 调用 StarRatingCalculator
  - 更新星级状态
  - **负责人**: android-engineer
  - **时间**: 0.5 天

- [ ] **Task #5.5**: 更新 LevelCompleteScreen
  - 显示动态星级
  - 星级动画
  - **负责人**: android-engineer
  - **时间**: 0.5 天

**总时间**: 3 天
**优先级**: ⭐⭐ 高
**依赖**: 无

---

### Epic #6: 音频系统 🔊

**目标**: 添加 TTS 发音和音效支持

**任务列表**:

#### Phase 1: TTS 集成 (2 天)
- [ ] **Task #6.1**: 集成 TTSController
  - TTS 初始化
  - 语言设置（英语）
  - 发音方法
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #6.2**: 添加发音按钮
  - SpeakerButton 组件
  - 按钮交互
  - 状态管理
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 2: 音效系统 (2 天)
- [ ] **Task #6.3**: 准备音效资源
  - 正确音效
  - 错误音效
  - 完成音效
  - **负责人**: android-engineer
  - **时间**: 1 天

- [ ] **Task #6.4**: 实现音效播放
  - SoundManager
  - 音效触发逻辑
  - 音量控制
  - **负责人**: android-engineer
  - **时间**: 1 天

#### Phase 3: 设置和测试 (1 天)
- [ ] **Task #6.5**: 音频设置界面
  - 音频开关
  - 音量控制
  - **负责人**: android-engineer
  - **时间**: 0.5 天

- [ ] **Task #6.6**: 真机测试
  - TTS 发音测试
  - 音效播放测试
  - 性能验证
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

**总时间**: 5 天
**优先级**: ⭐ 中
**依赖**: 无

---

### Epic #7: 测试覆盖率提升 🧪

**目标**: 提升测试覆盖率从 ~12% 到 50%

**任务列表**:

#### Phase 1: ViewModel 测试 (2 天)
- [ ] **Task #7.1**: LearningViewModel 测试
  - 状态管理测试
  - UseCase 调用测试
  - 边界条件测试
  - **负责人**: android-test-engineer
  - **时间**: 1 天

- [ ] **Task #7.2**: 其他 ViewModel 测试
  - HomeViewModel
  - IslandMapViewModel
  - LevelSelectViewModel
  - **负责人**: android-test-engineer
  - **时间**: 1 天

#### Phase 2: Component 测试 (2 天)
- [ ] **Task #7.3**: SpellBattleGame 测试
  - Compose UI 测试
  - 用户交互测试
  - **负责人**: android-test-engineer
  - **时间**: 1 天

- [ ] **Task #7.4**: 其他组件测试
  - HintCard, AnswerAnimations, etc.
  - **负责人**: android-test-engineer
  - **时间**: 1 天

#### Phase 3: 覆盖率验证 (1 天)
- [ ] **Task #7.5**: 生成覆盖率报告
  - JaCoCo 配置
  - 报告生成
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

- [ ] **Task #7.6**: 覆盖率分析
  - 识别未覆盖代码
  - 补充关键测试
  - **负责人**: android-test-engineer
  - **时间**: 0.5 天

**总时间**: 5 天
**优先级**: ⭐⭐ 基础
**依赖**: 各 Epic 完成后

---

## 📅 Sprint 2 时间线

### 4 周计划

| 周次 | Epic | 主要任务 | 交付 |
|------|------|---------|------|
| **Week 1** | #3, #4 | Make Atoll 内容 + Hint 集成 | 30 个新词汇，Hint 系统 |
| **Week 2** | #5 | 动态星级评分 | 动态评分算法 |
| **Week 3** | #6 | 音频系统 | TTS + 音效 |
| **Week 4** | #7 + 测试 | 测试覆盖率 + 回归测试 | Sprint 2 验收 |

### 里程碑

- **M1** (Day 7): Epic #3 和 #4 完成
- **M2** (Day 14): Epic #5 完成
- **M3** (Day 21): Epic #6 完成
- **M4** (Day 28): Sprint 2 100% 完成

---

## 👥 团队分工

### android-engineer

**主要任务**:
- Epic #3: Make Atoll 内容开发
- Epic #4: Hint 系统集成
- Epic #5: 动态评分算法
- Epic #6: 音频系统开发

**预计工作量**: 18-20 天

### android-test-engineer

**主要任务**:
- 所有 Epic 的测试执行
- 单元测试编写
- 真机测试验证
- Epic #7: 测试覆盖率提升

**预计工作量**: 12-15 天

### android-performance-expert

**主要任务**:
- 每个 Epic 后的性能验证
- 性能优化建议
- 性能报告生成

**预计工作量**: 4-5 天（分摊到各 Epic）

### education-specialist

**主要任务**:
- Make Atoll 词汇教育验证
- Hint 系统教育有效性验证
- 动态评分激励效果验证
- 教育报告生成

**预计工作量**: 3-4 天（分摊到各 Epic）

### team-lead

**主要任务**:
- Sprint 2 整体协调
- Epic 验收
- 问题解决
- 进度跟踪

**预计工作量**: 持续

---

## ✅ Sprint 2 启动检查清单

### 规划阶段 ✅

- [x] Sprint 2 规划文档完成
- [x] Epic 任务分解完成
- [x] 时间线制定完成
- [x] 团队分工确定

### 启动前准备 (待完成)

- [ ] **团队确认**
  - [ ] 确认团队成员可用性
  - [ ] 分配 Epic 负责人
  - [ ] 设定沟通机制

- [ ] **技术准备**
  - [ ] Hint 系统架构回顾
  - [ ] 开发环境检查
  - [ ] 依赖库确认

- [ ] **设备准备**
  - [ ] 测试设备连接
  - [ ] 真机设备确认
  - [ ] 模拟器配置

- [ ] **流程准备**
  - [ ] CI/CD 管道确认
  - [ ] 代码Review流程
  - [ ] 测试流程确认

### Kickoff 会议议程

**会议时间**: [待定]
**参会人员**: 所有团队成员

**议程**:
1. Sprint 1 回顾（10 分钟）
2. Sprint 2 目标和规划（15 分钟）
3. Epic 详细说明（20 分钟）
4. 任务分配和时间线（10 分钟）
5. Q&A（5 分钟）

---

## 📊 风险和缓解

### 高风险项

| 风险 | 缓解措施 | 应急计划 |
|------|---------|---------|
| Hint 系统集成复杂 | 提前技术验证 | 降级到简单提示 |
| 音频资源获取困难 | 使用 TTS | 音效延后 |
| 时间紧张 | 优先级管理 | Epic #6/#7 延后 |

---

## 🚀 立即行动

### 今天 (启动日)

1. ⏳ **发送 Sprint 2 规划给团队**
2. ⏳ **确认团队成员可用性**
3. ⏳ **安排 Kickoff 会议**
4. ⏳ **开始 Task #3.1**（Make Atoll 词汇设计）

### 本周

5. ⏳ **完成 Kickoff 会议**
6. ⏳ **开始 Epic #3 开发**
7. ⏳ **Hint 系统技术验证**

---

**文档创建**: 2026-02-22
**创建人**: team-lead
**状态**: ✅ 任务分解完成

**Sprint 2 准备就绪，等待团队确认和启动！** 🚀🎯
