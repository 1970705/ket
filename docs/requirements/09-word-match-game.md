# 单词消消乐模式 - 详细需求文档

**Epic**: #9
**优先级**: P1 (最高)
**状态**: 📋 规划中
**工作量**: 9-12 人天
**创建日期**: 2026-02-25

---

## 1. 功能概述

### 核心玩法
单词消消乐是一个配对消除类游戏模式，类似于传统的记忆卡片游戏，但采用泡泡/方块UI展示。

**游戏循环**:
1. 选择单词数量（5-50对可调节）
2. 显示随机打乱的单词-翻译泡泡阵
3. 玩家点击两个泡泡进行配对
4. 匹配成功 → 泡泡消失 + 华丽消除动画
5. 匹配失败 → 红色抖动提示
6. 全部消除 → 显示用时 + 继续挑战选项

---

## 2. UI设计

### 2.1 主界面布局

```
┌─────────────────────────────────┐
│   🎮 单词消消乐  [点击修改标题]    │  ← 可点击修改
├─────────────────────────────────┤
│ [5]━━━━━━━●━━━━━━━━━━[50] 10对  │  ← 滑块+实时显示
│ [导入词表] [开始游戏]             │  ← 按钮组
├─────────────────────────────────⏱️ 45s │  ← 计时器
│                                 │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │ban│ │🍌 │ │red│ │红 │ │ora│  │  ← 6列布局
│  │ana│ │香蕉│ │色  │ │   │ │nge│  │
│  └───┘ └───┘ └───┘ └───┘ └───┘  │
│                                 │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐  │
│  │gre│ │绿 │ │blu│ │蓝 │ │pur│  │
│  │en │ │   │ │e  │ │   │ │ple│  │
│  └───┘ └───┘ └───┘ └───┘ └───┘  │
└─────────────────────────────────┘
```

### 2.2 组件说明

#### 控件区域
- **单词数量滑块**: 5-50对可调节
- **导入词表按钮**: 支持Excel导入
- **开始游戏按钮**: 开始/重新开始

#### 游戏区域
- **计时器**: 实时显示已用秒数
- **泡泡棋盘**: 6列 × N行，根据单词数量动态调整
- **每个泡泡**: 随机背景色 + 白色文字

### 2.3 颜色方案

**泡泡背景色** (随机分配):
- 粉色: #FFB6C1
- 绿色: #90EE90
- 紫色: #DDA0DD
- 橙色: #FFA500
- 棕色: #D2691E
- 蓝色: #87CEEB

---

## 3. 游戏逻辑

### 3.1 状态机

```
IDLE → READY → PLAYING → COMPLETED → IDLE
  ↓        ↓          ↓
PREPARING  PAUSED    GAME_OVER
```

**状态说明**:
- **IDLE**: 初始状态
- **PREPARING**: 准备单词数据
- **READY**: 等待开始
- **PLAYING**: 游戏进行中
- **PAUSED**: 暂停
- **COMPLETED**: 全部消除完成
- **GAME_OVER**: 退出或超时

### 3.2 配对逻辑

```kotlin
data class BubbleState(
    val id: String,
    val word: String,         // 英文或中文
    val pairId: String,       // 配对ID
    val isSelected: Boolean,
    val isMatched: Boolean   // 已消除
)

// 配对检查
fun checkMatch(first: BubbleState, second: BubbleState): Boolean {
    return first.pairId == second.pairId &&
           first.id != second.id &&
           !first.isMatched && !second.isMatched
}
```

---

## 4. 交互设计

### 4.1 点击交互

**第一次点击**:
- 泡泡放大 + 发光效果
- 播放"啵"音效

**第二次点击**:
- **匹配成功**:
  - 泡泡爆炸动画 💥
  - 粒子效果散开
  - 叮咚声
- **匹配失败**:
  - 红色边框闪烁
  - 左右抖动 ❌
  - 嘟嘟声

### 4.2 消除动画

使用Compose Animation实现:
```kotlin
// 匹配成功
AnimatedVisibility(
    visible = !isMatched,
    enter = fadeIn() + scaleIn()
) {
    WordBubble(word)
}

// 失败抖动
LaunchedEffect(matchFailed) {
    // 摇动动画
}
```

---

## 5. 数据模型

### 5.1 游戏配置

```kotlin
data class MatchGameConfig(
    val wordPairs: Int,           // 5-50
    val timeLimit: Int? = null,   // 可选计时
    val allowCustomWords: Boolean = true,
    val bubbleSize: Dp = 80.dp
)
```

### 5.2 游戏状态

```kotlin
data class MatchGameState(
    val bubbles: List<BubbleState>,
    val selectedBubbles: List<String>, // 已选择的泡泡ID
    val matchedPairs: Int,
    val totalPairs: Int,
    val elapsedTime: Long,          // 毫秒
    val isPlaying: Boolean,
    val isPaused: Boolean
)
```

---

## 6. 性能要求

### 6.1 性能指标

- **帧率**: ≥60fps (android-performance-expert要求)
- **内存增长**: <50MB
- **首屏加载**: <500ms

### 6.2 性能优化策略

**使用LazyVerticalGrid**:
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(6),
    contentPadding = PaddingValues(16.dp),
) {
    items(bubbles, key = { it.id }) { bubble ->
        WordBubble(
            bubble = bubble,
            onClick = { /* ... */ }
        )
    }
}
```

**限制泡泡数量**:
- 最多50对 = 100个泡泡
- 低端设备: 建议限制在30对

---

## 7. 功能优先级

### MVP (Minimum Viable Product)

**必需功能**:
- ✅ 单词配对逻辑
- ✅ 基础UI（固定颜色）
- ✅ 本地词库支持
- ✅ 计时功能

**可选功能**:
- ⏸️ 自定义词表导入
- ⏸️ 高级动画效果
- ⏸️ 音效

### 完整版

**MVP +**:
- ✅ 3D泡泡效果
- ✅ 华丽消除动画
- ✅ 音效反馈
- ✅ Excel词表导入
- ✅ 关卡系统
- ✅ 挑战模式

---

## 8. 测试要求

### 8.1 单元测试

- [ ] 配对逻辑测试
- [ ] 状态机转换测试
- [ ] 计时准确性测试
- [ ] 泡泡随机性测试

### 8.2 UI测试

- [ ] 泡泡点击交互测试
- [ ] 配对成功/失败动画测试
- [ ] 滑块交互测试
- [ ] 适配性测试（不同屏幕尺寸）

### 8.3 性能测试

- [ ] 100个泡泡渲染性能 (android-performance-expert)
- [ ] 动画帧率测试
- [ ] 内存占用测试

---

## 9. 验收标准

### 9.1 功能验收

- [ ] 可以选择5-50对单词
- [ ] 点击配对正确则消除
- [ ] 点击配对错误有提示
- [ ] 全部消除显示用时
- [ ] 可以继续挑战（下一轮）

### 9.2 性能验收

- [ ] 游戏中帧率 ≥60fps
- [ ] 内存增长 <50MB
- [ ] 无崩溃、无ANR

---

## 10. 实施时间表

| Week | 任务 | 产出 |
|------|------|------|
| Week 1 Day 1-2 | 游戏逻辑设计 + UI设计 | 设计文档 |
| Week 1 Day 3-7 | MVP实现 | 可玩版本 |
| Week 2 Day 1-3 | 动画与音效 | 完整功能 |
| Week 2 Day 4-5 | 测试与优化 | 上线版本 |

---

**文档版本**: 1.0
**作者**: wordland-dev-team
**状态**: ✅ 需求评审完成，待实施
**预计完成**: 2026-03-15 (Epic #9)
