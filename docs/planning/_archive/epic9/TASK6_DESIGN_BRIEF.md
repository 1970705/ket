# Epic #9 任务 #6 - 游戏逻辑和状态机设计

**状态**: 🔄 进行中 (In Progress)
**开始时间**: 2026-02-25
**负责人**: game-designer + android-architect

---

## 📋 任务目标

设计单词消消乐模式的核心游戏逻辑，包括：
1. 游戏状态机设计
2. 数据模型定义
3. 配对逻辑设计
4. 与现有系统集成方案

---

## 👥 参与成员

### game-designer (Pane 6)
**任务**: 游戏设计
- 设计游戏状态机
- 定义核心游戏机制
- 设计用户体验流程
- 产出: 游戏设计文档

### android-architect (Pane 1)
**任务**: 架构设计
- 定义数据模型
- 设计UseCase接口
- 规划架构分层
- 产出: 架构设计文档

---

## 📖 需求文档

**详细需求**: `docs/requirements/09-word-match-game.md`

**核心玩法**:
1. 选择单词数量（5-50对）
2. 显示随机打乱的单词-翻译泡泡阵
3. 点击两个泡泡进行配对
4. 匹配成功 → 消除动画
5. 匹配失败 → 抖动提示
6. 全部消除 → 显示用时

---

## 🎮 游戏设计师工作内容

### 1. 状态机设计

**状态列表**:
```
IDLE (初始)
  ↓
PREPARING (准备数据)
  ↓
READY (等待开始)
  ↓
PLAYING (游戏中) ←→ PAUSED (暂停)
  ↓
COMPLETED (完成)
  ↓
IDLE (回到初始)

任意时刻 → GAME_OVER (退出)
```

### 2. 配对规则

**选中逻辑**:
- 第1次点击: 选中泡泡（高亮）
- 第2次点击: 检查配对
  - pairId相同 → 匹配成功
  - pairId不同 → 匹配失败

**反馈设计**:
- 匹配成功: 华丽消失 + 音效
- 匹配失败: 红色抖动 + 音效
- 完成: 庆祝动画 + 用时统计

### 3. 产出要求

1. 游戏状态机图
2. 游戏流程图
3. 配对逻辑详细说明
4. 用户体验设计说明

---

## 🏗️ 架构师工作内容

### 1. 数据模型设计

```kotlin
// 泡泡状态
data class BubbleState(
    val id: String,
    val word: String,           // 英文或中文
    val pairId: String,         // 配对ID
    val isSelected: Boolean,
    val isMatched: Boolean,
    val color: BubbleColor
)

// 游戏状态（sealed class）
sealed class MatchGameState {
    object Idle : MatchGameState()
    object Preparing : MatchGameState()
    data class Ready(val pairs: Int, val bubbles: List<BubbleState>) : MatchGameState()
    data class Playing(val elapsedTime: Long, val selectedBubbles: List<String>) : MatchGameState()
    object Paused : MatchGameState()
    data class Completed(val elapsedTime: Long, val pairs: Int) : MatchGameState()
    object GameOver : MatchGameState()
}

// 游戏配置
data class MatchGameConfig(
    val wordPairs: Int,          // 5-50
    val timeLimit: Int? = null,
    val allowCustomWords: Boolean = true,
    val bubbleSize: Dp = 80.dp
)
```

### 2. UseCase设计

1. **GetWordPairsUseCase**
   - 输入: MatchGameConfig
   - 输出: List<BubbleState>
   - 功能: 从词库获取单词对并打乱

2. **CheckMatchUseCase**
   - 输入: 两个BubbleState
   - 输出: Boolean (是否匹配)
   - 功能: 检查pairId是否相同

3. **UpdateGameStateUseCase**
   - 输入: 当前状态, 用户操作
   - 输出: 新状态
   - 功能: 状态转换逻辑

### 3. 架构分层

**Domain层**:
- `domain/model/`: BubbleState, MatchGameState, MatchGameConfig
- `domain/usecase/usecases/`: UseCase接口
- `domain/algorithm/`: 打乱算法

**Data层**:
- 复用 `WordRepository` 获取单词
- 可能需要新增 `WordPairRepository`

**UI层**:
- `MatchGameScreen.kt`: 主界面
- `BubbleTile.kt`: 泡泡组件
- `MatchGameViewModel.kt`: ViewModel

### 4. 产出要求

1. 数据模型定义（Kotlin代码）
2. UseCase接口定义
3. 架构分层设计图
4. 与现有系统集成方案

---

## ⏰ 时间估算

**总计**: 2-4小时
- 游戏设计师: 1-2小时
- 架构师: 1-2小时

---

## 🎯 下一步

任务 #6 完成后：
1. 评审设计产出
2. 进入任务 #7: 创建数据模型和UseCase
3. android-engineer开始实现

---

## 📊 检查清单

### 游戏设计师
- [ ] 状态机图完成
- [ ] 游戏流程图完成
- [ ] 配对逻辑说明完成
- [ ] UX设计说明完成

### 架构师
- [ ] 数据模型定义完成
- [ ] UseCase接口定义完成
- [ ] 架构分层设计完成
- [ ] 集成方案说明完成

---

**任务创建**: 2026-02-25
**状态**: 🔄 In Progress
**预计完成**: 2026-02-25 (今天)

