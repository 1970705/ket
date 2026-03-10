# Wordland 项目需求文档

**文档版本**: v2.0
**创建日期**: 2026-02-20
**最后更新**: 2026-02-20
**项目状态**: 增强功能开发阶段
**维护者**: Wordland 开发团队

---

## 📋 文档概述

本文档是 Wordland KET 词汇学习应用的完整需求文档,整合了所有游戏设计、系统设计和功能需求。

**⚠️ 文档重组通知 (2026-02-20)**:

为了符合项目文档结构规范，部分详细需求文档已移动到正式目录：
- 详细设计文档 → `docs/design/` (游戏设计、UI设计、系统设计)
- 团队协作文档 → `docs/team/` (Sprint计划、执行计划)
- 项目报告 → `docs/reports/` (优先级分析)

本文档保留作为需求总览和快速导航索引。

### 文档结构

**详细需求文档** (已移动):
```
docs/
├── design/
│   ├── game/
│   │   └── 01-game-mechanics.md (游戏机制需求) ⬅️ 已移动
│   ├── ui/
│   │   └── 02-ui-ux.md (UI/UX设计需求) ⬅️ 已移动
│   └── system/
│       └── 08-technical.md (技术需求) ⬅️ 已移动
├── team/
│   ├── TEAM_REVIEW_SUMMARY.md (团队评审汇总) ⬅️ 已移动
│   ├── EPER_ITERATION1_PLAN.md (EPER Iteration 1 计划) ⬅️ 已移动
│   └── EPER_ITERATION1_EXECUTION_PLAN.md (EPER Iteration 1 执行计划) ⬅️ 已移动
└── reports/
    └── PRIORITY_ANALYSIS_REPORT.md (优先级分析) ⬅️ 已移动
```

**本文档结构** (保留):
```
docs/requirements/
├── README.md (本文件 - 需求总览和导航索引)
├── 03-game-modes.md (游戏模式需求) - ✅ 已完成
│   └── 负责人: game-designer (2026-02-20完成)
│   └── 包含5种游戏模式 (新增 Word Detective)
├── 04-achievements.md (成就系统需求) - ✅ 已完成
│   └── 负责人: game-designer (2026-02-20完成)
├── 05-statistics.md (统计系统需求) - ✅ 已完成
│   └── 负责人: android-architect (2026-02-20完成)
├── 06-anti-addiction.md (防沉迷系统需求) - ✅ 已完成
│   └── 负责人: education-specialist (2026-02-20完成)
├── 07-content.md (内容需求) - ✅ 已完成
│   └── 负责人: education-specialist (2026-02-20完成)
└── 08-humor-engagement-design.md (幽默与参与度设计) - ✅ 新增
    └── 负责人: 团队讨论 (2026-02-20完成)
        - 幽默设计原则 (P1)
        - Oops Gallery错误展示模式 (P1)
```

---

## 🔗 快速导航

### 已移动文档链接

| 文档 | 描述 | 新位置 |
|------|------|--------|
| **[游戏机制需求](../design/game/01-game-mechanics.md)** | Spell Battle, Quick Judge, 记忆算法, 提示系统, 连击系统, 星级评分 | 📂 [docs/design/game/](../design/game/) |
| **[UI/UX 设计需求](../design/ui/02-ui-ux.md)** | 地图系统重构, 视觉反馈增强, 语境化学习, 探索机制, 儿童友好设计, 动画过渡 | 📂 [docs/design/ui/](../design/ui/) |
| **[技术需求](../design/system/08-technical.md)** | 性能要求, 兼容性要求, 质量标准, 数据库架构, API设计, 安全要求 | 📂 [docs/design/system/](../design/system/) |
| **[团队评审汇总](../team/TEAM_REVIEW_SUMMARY.md)** | EPER Iteration 1 前团队评估结果，4个P0问题识别，Epic #3完成确认 | 📂 [docs/team/](../team/) |
| **[EPER Iteration 1 计划](../team/EPER_ITERATION1_PLAN.md)** | EPER Iteration 1 详细计划，Epic/Story分解，时间表，团队分配 | 📂 [docs/team/](../team/) |
| **[EPER Iteration 1 执行计划](../team/EPER_ITERATION1_EXECUTION_PLAN.md)** | EPER Iteration 1 详细执行指南，P0修复，每日任务，风险缓解，成功指标 | 📂 [docs/team/](../team/) |
| **[优先级分析报告](../reports/PRIORITY_ANALYSIS_REPORT.md)** | P0/P1/P2需求优先级分析，决策矩阵，实施路线图 | 📂 [docs/reports/](../reports/) |

---

## 🎯 项目概述

### 项目简介

**Wordland** 是一款面向 10-12 岁儿童的 KET/PET 词汇学习游戏应用,通过游戏化方式帮助儿童掌握核心英语词汇。

### 核心价值

- 🎮 **游戏化学习**: 让背单词像玩游戏一样有趣
- 🧠 **科学记忆**: 基于艾宾浩斯遗忘曲线的智能复习系统
- 📊 **可视化进度**: 家长可以清晰看到孩子的学习成果
- 🎯 **考试提分**: 专注 KET/PET 考试核心词汇
- 🛡️ **健康防沉迷**: COPPA 合规的儿童友好设计

### 目标用户

| 用户类型 | 年龄 | 角色 | 需求 |
|---------|-----|------|------|
| **主要用户** | 10-12岁 | 学习者 | 有趣的学习体验 |
| **次要用户** | 30-45岁 | 家长 | 学习进度监控和控制 |
| **目标市场** | 中国小学生 | KET考试准备 | 考试提分 |

---

## 📊 当前状态 (2026-02-20)

### 已完成功能 ✅

#### 架构基础 (100%)
- ✅ Clean Architecture 三层架构
- ✅ Hilt 2.48 + Service Locator 混合 DI
- ✅ 8 个 UseCases 全部实现
- ✅ 6 个 ViewModels 状态管理
- ✅ Room 数据库配置完成

#### 核心功能 (95%)
- ✅ 导航系统: 7 个界面完整流程
- ✅ **Spell Battle 玩法**: 虚拟键盘 + 答案验证
- ✅ **Quick Judge 玩法**: 快速判断游戏模式 (新)
- ✅ 关卡系统: 5 个关卡,每关 6 词
- ✅ 进度追踪: 题目队列、进度计算
- ✅ 内容数据: 30 个 KET 核心词汇

#### UI 组件 (100%)
- ✅ HomeScreen (主页)
- ✅ WorldMapScreen (世界地图)
- ✅ IslandMapScreen (岛屿地图)
- ✅ LevelSelectScreen (关卡选择)
- ✅ LearningScreen (学习界面 - Spell Battle)
- ✅ QuickJudgeScreen (快速判断界面) (新)
- ✅ ReviewScreen (复习界面)
- ✅ ProgressScreen (进度界面)

#### 测试状态 (95%)
- ✅ 3,075 个单元测试 (100% 通过率)
- ✅ ~21% 指令覆盖率
- ✅ Domain 层: 83%+ 覆盖
- ✅ 真机测试通过 (Xiaomi 设备)
- ✅ P0 质量门禁全部通过

#### 技术指标

| 指标 | 当前值 | 目标值 | 状态 |
|------|--------|--------|------|
| 冷启动时间 | 439ms | < 3s | ✅ 优秀 |
| 词汇数量 | 30 词 | 180 词 (MVP) | ⚠️ 17% |
| 关卡数量 | 5 关 | 24 关 (MVP) | ⚠️ 21% |
| 玩法类型 | 2 种 | 4 种 | ⚠️ 50% |
| 测试覆盖 | 21% | 80% | ⚠️ 进行中 |

---

## 🎮 一、游戏机制需求

### 1.1 核心游戏机制

#### Spell Battle (拼写战斗)

**状态**: ✅ 已完成

**玩法描述**:
- 显示中文翻译
- 用户使用虚拟键盘拼写英文单词
- 实时反馈 (蓝色正确,红色错误)
- 提交答案验证

**核心机制**:
```kotlin
// 星级评分 (动态)
- 0 星: 错误答案
- 1 星: 正确但检测为猜测
- 2 星: 正确但使用提示 或 思考时间过短
- 3 星: 正确,无提示,适当思考时间

// 思考时间阈值
最小思考时间 = 1s + (0.5s × 单词长度)
例如: 3字母单词 = 1.5s, 6字母单词 = 3.0s

// 连击系统
- 3-4 连击: 1.2x 加成 (20%)
- 5+ 连击: 1.5x 加成 (50%)
- 猜测不增加连击 (响应时间过短)
```

**反作弊机制**:
- ✅ 时间阈值检测 (防止盲目快速点击)
- ✅ 模式分析检测 (最近5个答案模式)
- ✅ 猜测惩罚: 最多 1 星 + 记忆强度 -30
- ✅ 已掌握单词豁免 (避免挫败感)

#### Quick Judge (快速判断)

**状态**: ✅ 已完成

**玩法描述**:
- 显示英文单词 + 中文翻译
- 用户判断翻译是否正确
- 限时挑战 (Easy/Normal/Hard 三档难度)
- 连击系统与速度加成

**难度设置**:

| 难度 | 时间限制 | 题目数量 | 最大错误 | 目标用户 |
|------|---------|---------|---------|---------|
| Easy | 10秒/题 | 5题 | 2次 | 初学者 |
| Normal | 8秒/题 | 10题 | 2次 | 已掌握基础 |
| Hard | 5秒/题 | 15题 | 1次 | 高水平玩家 |

**评分标准** (Normal模式):
```
⭐⭐⭐ 3星: 准确率 ≥90%, 平均时间 <5秒, 错误 ≤1次
⭐⭐ 2星: 准确率 ≥75%, 平均时间 <6秒, 错误 ≤2次
⭐ 1星: 准确率 ≥60%, 通过
✗ 0星: 准确率 <60%, 失败
```

### 1.2 记忆算法

**状态**: ✅ 已完成 (SM-2 算法)

**间隔重复间隔**:
```kotlin
记忆强度 < 30:  10分钟后复习
记忆强度 < 50:  1小时后复习
记忆强度 < 70:  4小时后复习
记忆强度 < 85:  1天后复习
记忆强度 ≥ 85:  1周后复习
```

**强度增长**:
```kotlin
初始强度: 10
正确加成: +10
错误惩罚: -15
蒙对惩罚: -20 (记忆和连击均受影响)
蒙错惩罚: -20
难度乘数: Easy 0.8x, Normal 1.0x, Hard 1.2x
```

### 1.3 提示系统

**状态**: ✅ 架构完成,待 UI 集成

**三级渐进提示**:

| 级别 | 内容 | 示例 (单词: banana) | 惩罚 |
|------|------|-------------------|------|
| 1 | 首字母 | "首字母: B" | 最高 2 星 |
| 2 | 前半部分 | "前半部分: ban___" | 最高 2 星 |
| 3 | 元音隐藏 | "完整单词(元音隐藏): b_n_n_" | 最高 2 星 |

**使用限制**:
- 每词最多 3 次提示
- 提示间冷却时间: 3 秒
- 使用提示后星级封顶 2 星

---

## 🎨 二、UI/UX 设计需求

### 2.1 地图系统重构 ⚠️ **P0 优先级**

**当前问题**:
- 游戏名为 "Wordland" (单词大陆)
- 但实际显示的是孤立岛屿
- 缺乏探索感和世界感

**设计方案**: 混合方案 (推荐)

```
方案特点:
1. 添加"世界视图"切换按钮
2. 显示群岛布局和迷雾系统
3. 玩家船只在区域间移动
4. 迷雾随进度逐渐揭开

优势:
- 开发时间短 (1-2周)
- 低风险 (复用现有代码)
- 良好的用户体验
- 不破坏现有结构
```

**迷雾系统设计**:
```kotlin
// 迷雾揭示规则
fun calculateVisibilityRadius(playerLevel: Int): Float {
    return when {
        playerLevel <= 3 -> 0.15f   // 地图的15%
        playerLevel <= 6 -> 0.30f   // 地图的30%
        else -> 0.50f               // 地图的50%
    }
}
```

**视觉设计**:
```
┌─────────────────────────────────────────┐
│         W O R D L A N D                  │
│                                         │
│  🌫️🌫️🌫️         👁️ Look Peninsula   🌫️  │
│  🌫️              👁️                  🌫️   │
│                                         │
│       🏝️ Make Atoll    📚 Listen Cove   │
│            🛠️            👂             │
│                                         │
│  🌫️        🌫️🌫️🌫️        🌫️          │
│                                         │
│            🚢 Your Ship                │
└─────────────────────────────────────────┘
```

### 2.2 语境化学习 ⚠️ **P1 优先级**

**当前问题**:
- 每个拼写界面只显示单个单词
- 缺乏语境,枯燥的单词记忆

**渐进式框架**:

```
阶段 1: 基础 (Level 1-3)
├── 仅单词拼写
├── 重点: 拼写机制学习
└── 持续时间: 18个单词

阶段 2: 语境 (Level 4-6)
├── 句子模式
├── 示例: "我每天早上吃_____。"
├── 重点: 单词在语境中使用
└── 认知负荷: 新词80%, 复习20%

阶段 3: 叙事 (Level 7-10)
├── 微型故事模式
├── 示例: "Emma醒来。她_____她的牙齿..."
├── 重点: 单词之间的连接
└── 认知负荷: 新词20%, 复习80%
```

**UI 布局设计**:
```
┌─────────────────────────────────┐
│  📖 语境: 我每天早上吃_____。   │
│                                 │
│  提示: 第一顿饭                  │
│                                 │
│  ┌───────────────────────────┐ │
│  │  [ B R E A K F A S T ]    │ │
│  │       ↑ 正确! ✨           │ │
│  └───────────────────────────┘ │
│                                 │
│  完整句子: 我每天早上吃早餐...   │
└─────────────────────────────────┘
```

### 2.3 视觉反馈增强 ⚠️ **P0 优先级**

**分层反馈系统**:

1. **即时反馈** (0-100ms):
   - 颜色闪现 (绿色正确,红色错误)
   - 触觉反馈 (正确: 短脉冲,错误: 长脉冲)
   - 音效 (正确: 清脆叮声,错误: 温和嗡声)

2. **详细反馈** (100-500ms):
   - 星级评分动画 (星星依次弹出)
   - 记忆强度条动画
   - 连击计数器更新动画

3. **庆祝效果** (里程碑):
   - 3连击: 小彩花爆发
   - 5连击: 中等烟花效果
   - 关卡完成: 全屏庆祝

**宠物反应系统**:
```kotlin
enum class PetReaction {
    // 正向反应
    ECSTATIC,      // 3星,无提示
    PROUD,         // 2星且有进步
    ENCOURAGING,   // 1星或错误但有努力
    CELEBRATING,   // 关卡完成
    COMBO_HIGH,    // 5+连击

    // 负向反应 (温和,不打击)
    CONCERNED,     // 使用提示后错误
    SURPRISED,     // 检测到猜测
    SLEEPY,        // 多次快速错误
}
```

### 2.4 探索机制 ⚠️ **P1 优先级**

**引导式探索模型**:

```
┌─────────────────────────────────────┐
│        引导式探索模型                │
│                                     │
│  🔵 安全区    🟢 发现区             │
│     (当前      (可选但              │
│      等级)      结构化)             │
│                                     │
│  🌫️ 神秘区    🔴 锁定区            │
│     (未来       (尚不可访问          │
│      内容)      )                   │
└─────────────────────────────────────┘
```

**发现机制**:
- **迷雾揭示**: 完成关卡 → 揭示相邻区域
- **彩蛋**: 隐藏在已完成关卡中的奖励
- **平行路径**:
  ```
              ┌── Level 3A ──┐
  Level 1 → Level 2 ─┤             ├── Level 5
              └── Level 3B ──┘
  ```

---

## 🎯 三、游戏模式需求

### 3.1 已实现模式 (2/4)

#### Spell Battle (拼写战斗)
- ✅ 虚拟 QWERTY 键盘
- ✅ 实时答案验证
- ✅ 连击系统
- ✅ 反猜测机制

#### Quick Judge (快速判断)
- ✅ 三档难度
- ✅ 倒计时机制
- ✅ 连击奖励
- ✅ 自适应难度

### 3.2 计划中模式 (2/4)

#### Listen Find (听音寻宝) - P1

**核心玩法**:
- 用户听音频,从选项中选择正确的图片
- 音频播放系统 (ExoPlayer)
- 图片资源加载 (Coil)
- 选项生成和随机化

**UI 流程**:
```
1. 播放单词发音 🎵
   ↓
2. 显示4张图片选项 🖼️
   ↓
3. 用户选择图片 👆
   ↓
4. 正确/错误反馈 ✅/❌
```

#### Sentence Match (句子配对) - P2

**核心玩法**:
- 拖拽单词到句子空位
- 语法检查
- 句子完整性验证

**UI 流程**:
```
1. 显示带空位的句子 "I eat _____ for breakfast."
   ↓
2. 显示单词选项 [apple, banana, orange, grape]
   ↓
3. 拖拽单词到空位
   ↓
4. 语法检查和反馈
```

---

## 🏆 四、成就系统需求

**状态**: 设计完成,待实现

### 4.1 成就分类 (50个成就)

| 类别 | 数量 | 占比 | 难度分布 |
|------|------|------|---------|
| Progress (进度) | 12 | 24% | ⭐×4, ⭐⭐×3, ⭐⭐⭐×3, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| Performance (表现) | 10 | 20% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×3, ⭐⭐⭐⭐×2, ⭐⭐⭐⭐⭐×1 |
| Combo (连击) | 6 | 12% | ⭐×1, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1 |
| Streak (连续) | 6 | 12% | ⭐×1, ⭐⭐×1, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| Quick Judge (判断) | 8 | 16% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |
| Social (社交) | 8 | 16% | ⭐×2, ⭐⭐×2, ⭐⭐⭐×2, ⭐⭐⭐⭐×1, ⭐⭐⭐⭐⭐×1 |

### 4.2 奖励类型

**金币 (⭐)**:
- 用于解锁新岛屿、购买宠物皮肤、购买主题
- 难度奖励: ⭐ 10-20, ⭐⭐ 20-35, ⭐⭐⭐ 35-50, ⭐⭐⭐⭐ 50-100, ⭐⭐⭐⭐⭐ 100-200

**称号**:
- 显示在用户名旁
- 示例: "学者"、"记忆大师"、"势不可挡"、"传奇"

**徽章**:
- 永久显示在个人资料
- 示例: 🏆 Word Hunter, 📅 Dedicated Student, ⚡ Unstoppable

**宠物解锁**:
- 专属宠物奖励
- 示例: 🦁 岛屿守护狮, 🦄 完美独角兽, 🔥 传说凤凰

### 4.3 触发机制

**游戏事件类型**:
```kotlin
sealed class GameEvent {
    // 进度事件
    data class WordLearned(val userId: String, val wordId: String)
    data class LevelCompleted(val userId: String, val levelId: String, val stars: Int)

    // 表现事件
    data class PerfectLevel(val userId: String, val levelId: String)

    // 连击事件
    data class ComboAchieved(val userId: String, val comboCount: Int)

    // 连续事件
    data class DailyStreak(val userId: String, val streakDays: Int)

    // Quick Judge 事件
    data class QuickJudgeCompleted(val userId: String, val difficulty: String, val stars: Int)
}
```

---

## 📊 五、统计系统需求

**状态**: 设计完成,待实现

### 5.1 数据模型

#### GameHistory (游戏历史)
```kotlin
data class GameHistory(
    val gameId: String,              // UUID
    val userId: String,
    val levelId: String,
    val islandId: String,
    val gameMode: GameMode,          // SPELL_BATTLE, QUICK_JUDGE
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val score: Int,
    val stars: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val maxCombo: Int,
    val hintsUsed: Int,
    val avgResponseTime: Long
)
```

#### LevelStatistics (关卡统计)
```kotlin
data class LevelStatistics(
    val userId: String,
    val levelId: String,
    val totalGames: Int,
    val completedGames: Int,
    val perfectGames: Int,           // 3星游戏次数
    val highestScore: Int,
    val averageScore: Float,
    val bestTime: Long?,
    val averageTime: Long,
    val overallAccuracy: Float,
    val bestCombo: Int
)
```

#### GlobalStatistics (全局统计)
```kotlin
data class GlobalStatistics(
    val userId: String,
    val totalGames: Int,
    val totalScore: Int,
    val totalStudyTime: Long,        // 总学习时长
    val currentStreak: Int,          // 当前连续学习天数
    val longestStreak: Int,          // 最长连续学习天数
    val totalLevelsCompleted: Int,
    val totalWordsMastered: Int
)
```

### 5.2 UI 页面

**StatisticsScreen** (主统计页面):
```
┌─────────────────────────────────────┐
│  全局统计卡片                         │
│  ┌─────────────────────────────────┐ │
│  │ 总游戏: 156 | 总分: 12,450     │ │
│  │ 学习时长: 8h 23m | 连续: 7天 🔥 │ │
│  └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│  [历史] [关卡] [成就]                │
├─────────────────────────────────────┤
│  游戏历史列表                         │
│  • Spell Battle - Level 1 (3⭐)    │
│  • Quick Judge - Normal (2⭐)      │
│  • Spell Battle - Level 2 (1⭐)    │
└─────────────────────────────────────┘
```

---

## 🛡️ 六、防沉迷系统需求

**状态**: 设计完成,COPPA 合规

### 6.1 会话管理

**默认限制** (可由家长配置):

| 限制类型 | 时长 | 触发 | 行为 |
|---------|-----|------|------|
| **软提醒** | 15分钟 | 会话计时器 | 温和弹窗: "要休息一下吗?" |
| **硬限制** | 45分钟 | 会话计时器 | 强制休息: "已经学习45分钟!休息一下吧!" |
| **每日上限** | 2小时 | 每日累加器 | 阻塞: "明天再来学习吧!" |
| **休息提醒** | 每5分钟 | 间隔 | 微妙通知: "远眺20秒!" |

**活跃时间检测**:
- ✅ 仅计算活跃游戏时间
- ❌ 不计算: 应用后台、暂停、菜单、加载

### 6.2 休息奖励系统

**奖励层级**:

| 休息时长 | 加成 | 描述 |
|---------|-----|------|
| 5分钟 | 1.2x | 下3个单词20%分数加成 |
| 15分钟 | 1.5x | 下5个单词50%分数加成 |
| 30分钟 | 2.0x | 下一关卡2倍分数加成 |
| 1小时 | 3.0x | 3倍加成 + "专注学习者"徽章 |
| 直到明天 | 5.0x | 5倍加成 + "休息充分学者"称号 |

### 6.3 家长控制

**家长仪表板**:
- ⏱️ 时间限制设置 (软提醒、硬限制、每日上限)
- 🌙 睡眠模式 (开始/结束时间)
- 📊 每周使用报告 (邮件 + 应用内)
- 🔒 家长PIN保护
- 📈 学习进度可视化

**每周报告**:
```
📊 Wordland Weekly Report
Feb 12 - Feb 18, 2026

📈 本周学习
• 总时长: 6h 45m
• 日均: 54分钟
• 新学单词: 28个
• 当前连续: 7天 🔥

🏆 解锁成就
• First Steps ✅
• Week Warrior 🔥
• Combo Master ⭐
```

### 6.4 COPPA 合规

**家长同意**:
- ✅ 数据收集前需可验证家长同意
- ✅ 清晰解释数据收集目的
- ✅ 简单的退出机制

**数据最小化**:
- ✅ 仅收集: 会话时长、学习进度、使用模式
- ❌ 从不收集: 位置、儿童联系方式、音视频、社交媒体、生物识别

---

## 📚 七、内容需求

### 7.1 当前内容

**Look Island** (30词, 5关):
```
Level 1: look, see, watch, eye, glass, find
Level 2: color, red, blue, green, yellow, black
Level 3: newspaper, book, television, computer, phone, magazine
Level 4: notice, search, check, picture, photo, camera
Level 5: observe, examine, stare, display, appear, visible
```

### 7.2 MVP 内容目标

| 岛屿 | 词汇数 | 关卡数 | 状态 |
|------|--------|--------|------|
| Look Island | 60 词 | 8 关 | ⚠️ 30词 (50%) |
| Move Valley | 60 词 | 8 关 | ❌ 未开始 |
| Say City | 60 词 | 8 关 | ❌ 未开始 |
| **总计** | **180 词** | **24 关** | **17% 完成** |

### 7.3 资源需求

**音频资源** (P1):
- 180个单词发音 (每个~1秒)
- 200+例句音频 (每个~3秒)
- 音效文件 (正确、错误、按键、关卡完成)
- 总时长: ~15分钟
- 格式: MP3 (128 kbps)
- 大小: ~2 MB

**图片资源** (P2):
- 场景图片: 180张
- 图标和按钮: ~50张
- 关卡预览图: 24张
- 背景图: ~20张
- 格式: PNG/WebP
- 大小: ~15 MB (压缩后)

---

## 🔧 八、技术需求

### 8.1 性能要求

| 指标 | 目标值 | 测量方法 |
|------|--------|----------|
| 应用启动时间 | < 3 秒 | Android Profiler |
| 关卡加载时间 | < 1 秒 | 日志记录 |
| 界面响应延迟 | < 100ms | 用户体验测试 |
| 内存占用 | < 150MB | Android Profiler |
| APK 大小 | < 50MB | Build Output |
| 电池消耗 | < 10%/小时 | Battery Historian |

### 8.2 兼容性要求

- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)
- **设备支持**: 手机和平板
- **屏幕尺寸**: 4.5英寸 - 12英寸
- **网络**: 纯本地 (MVP阶段)

### 8.3 质量要求

- **单元测试覆盖率**: ≥ 80% (当前 21%)
- **集成测试**: 核心流程全覆盖
- **Lint 检查**: 0 错误, 0 警告
- **崩溃率**: < 0.1%

### 8.4 数据库架构

**Schema 版本**: 4 → 5

**新增表**:
- `game_history`: 游戏历史记录
- `level_statistics`: 关卡统计
- `global_statistics`: 全局统计
- `achievement_progress`: 成就进度
- `session_logs`: 会话日志
- `daily_usage`: 每日使用统计
- `parental_settings`: 家长控制设置
- `break_history`: 休息历史

---

## 🚀 九、实施计划

### 9.1 短期需求 (1-2周) - P0

#### 需求 #1: 视觉反馈增强
- ✅ 添加拼写动画 (字母飞入效果)
- ✅ 答对时的庆祝动画
- ✅ 连击系统视觉效果
- ✅ 进度条增强

**验收**:
- [ ] 动画流畅不卡顿
- [ ] 用户反馈"更有趣了"
- [ ] 真机测试通过

#### 需求 #2: 地图系统重构
- ✅ 添加世界视图切换
- ✅ 实现迷雾系统
- ✅ 玩家船只动画
- ✅ 区域解锁逻辑

**验收**:
- [ ] 世界视图正常显示
- [ ] 迷雾正确揭开
- [ ] 导航流畅无崩溃

### 9.2 中期需求 (3-4周) - P1

#### 需求 #3: 语境化学习
- ⏳ 句子模式数据模型
- ⏳ 30个句子语境 (Level 4-6)
- ⏳ 句子UI布局
- ⏳ 故事模式数据模型
- ⏳ 10个微型故事 (Level 7-10)

#### 需求 #4: 成就系统
- ⏳ 50个成就定义
- ⏳ 成就检测逻辑
- ⏳ 成就UI (列表、详情、通知)
- ⏳ 奖励发放系统

#### 需求 #5: 统计系统
- ⏳ 游戏历史记录
- ⏳ 关卡统计
- ⏳ 全局统计
- ⏳ 统计UI页面

### 9.3 长期需求 (1-2月) - P2

#### 需求 #6: 防沉迷系统
- ⏳ 会话管理
- ⏳ 休息奖励系统
- ⏳ 家长控制仪表板
- ⏳ COPPA 合规审查

#### 需求 #7: 完整四种玩法
- ⏳ Listen Find (听音寻宝)
- ⏳ Sentence Match (句子配对)

#### 需求 #8: 内容扩充
- ⏳ Move Valley (60词, 8关)
- ⏳ Say City (60词, 8关)

---

## 📈 十、成功指标

### 10.1 学习效果 (主要)

| 指标 | 当前 | 目标 | 测量 |
|------|------|------|------|
| 7天后单词保留率 | 待定 | +20% | A/B测试 |
| 关卡完成率 | 20% | 60% | 分析 |
| 7天回访率 | 待定 | +30% | 分析 |

### 10.2 参与度 (次要)

| 指标 | 当前 | 目标 | 测量 |
|------|------|------|------|
| 平均会话时长 | 5分钟 | 15分钟 | 分析 |
| 日活用户 | 基线 | +50% | 分析 |
| 功能使用(世界地图) | N/A | >50% | 分析 |

### 10.3 用户满意度 (定性)

| 指标 | 测量方式 | 目标 |
|------|---------|------|
| "有趣"评分 | 调查 | >4/5 |
| 家长满意度 | 调查 | >4/5 |
| 支持工单量 | 量 | <10%增长 |

---

## 📖 相关文档

### 需求细分文档

- `01-game-mechanics.md` - 详细游戏机制需求
- `02-ui-ux.md` - 详细UI/UX设计需求
- `03-game-modes.md` - 详细游戏模式需求
- `04-achievements.md` - 详细成就系统需求
- `05-statistics.md` - 详细统计系统需求
- `06-anti-addiction.md` - 详细防沉迷系统需求
- `07-content.md` - 详细内容需求
- `08-technical.md` - 详细技术需求

### 设计文档

- `game/GAME_DESIGN_ANALYSIS_2026-02-18.md` - 游戏设计分析
- `game/P0_GAME_EXPERIENCE_IMPROVEMENT_DESIGN.md` - P0游戏体验改进设计
- `game/quick_judge_mechanics.md` - Quick Judge机制设计
- `game/statistics_achievements_design.md` - 统计成就系统设计
- `design/game/ANTI_ADDICTION_SYSTEM_DESIGN.md` - 防沉迷系统设计
- `design/system/STATISTICS_SYSTEM_DESIGN.md` - 统计系统设计

### 实施指南

- `guides/HINT_SYSTEM_INTEGRATION.md` - 提示系统集成指南
- `guides/CODE_REVIEW_CHECKLIST.md` - 代码审查检查清单
- `testing/strategy/TEST_STRATEGY.md` - 测试策略

---

## 📝 版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2026-02-15 | 初始版本 (docs-reports-archive/requirement.md) | Claude Code |
| v2.0 | 2026-02-20 | 整合所有设计文档,系统化需求梳理 | Team Lead |

---

**文档状态**: ✅ 已完成
**下一步**: 分配给团队评审和优先级排序
