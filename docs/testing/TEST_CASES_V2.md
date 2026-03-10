# Wordland 测试用例清单

**版本**: 2.0
**日期**: 2026-02-20
**维护者**: android-test-engineer
**范围**: 新增功能 + 边界条件

---

## 目录

1. [地图系统重构 (25个用例)](#1-地图系统重构)
2. [视觉反馈增强 (15个用例)](#2-视觉反馈增强)
3. [成就系统 (30个用例)](#3-成就系统)
4. [统计系统 (35个用例)](#4-统计系统)
5. [语境化学习 (20个用例)](#5-语境化学习)
6. [Listen Find模式 (25个用例)](#6-listen-find模式)
7. [防沉迷系统 (40个用例)](#7-防沉迷系统)
8. [边界条件测试 (30个用例)](#8-边界条件测试)

**总计**: 220 个测试用例

---

## 1. 地图系统重构

### 1.1 单元测试 - VisibilityCalculator

| 用例ID | 用例名称 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| MAP-001 | Level 1可见性半径为15% | level=1 | radius=0.15f | P0 |
| MAP-002 | Level 3可见性半径为15% | level=3 | radius=0.15f | P0 |
| MAP-003 | Level 4可见性半径为30% | level=4 | radius=0.30f | P0 |
| MAP-004 | Level 6可见性半径为30% | level=6 | radius=0.30f | P0 |
| MAP-005 | Level 7可见性半径为50% | level=7 | radius=0.50f | P0 |
| MAP-006 | Level 10可见性半径为50% | level=10 | radius=0.50f | P0 |
| MAP-007 | 升级时可见性更新 | level=1→2 | radius增加 | P0 |
| MAP-008 | 完成关卡揭示相邻区域 | levelComplete | adjacent=true | P0 |
| MAP-009 | 未解锁区域保持迷雾 | locked | fog=true | P0 |
| MAP-010 | 已解锁区域无迷雾 | unlocked | fog=false | P0 |

### 1.2 单元测试 - FogOverlay

| 用例ID | 用例名称 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| MAP-011 | 锁定区域迷雾不透明度为1.0 | locked | alpha=1.0f | P0 |
| MAP-012 | 解锁区域迷雾不透明度为0.0 | unlocked | alpha=0.0f | P0 |
| MAP-013 | 迷雾动画时长500ms | animate | duration=500ms | P1 |
| MAP-014 | 迷雾动画使用缓动函数 | animate | easing | P1 |
| MAP-015 | 迷雾层在最上层渲染 | compose | zIndex=max | P0 |

### 1.3 集成测试 - RegionUnlockManager

| 用例ID | 用例名称 | 前置条件 | 操作 | 预期结果 | 优先级 |
|--------|----------|----------|------|----------|--------|
| MAP-016 | 完成关卡解锁相邻区域 | Level 1完成 | checkUnlock() | Level 2解锁 | P0 |
| MAP-017 | 解锁状态持久化 | 数据库空 | restart | 状态保持 | P0 |
| MAP-018 | 非相邻区域保持锁定 | Level 1完成 | checkUnlock() | Level 3锁定 | P0 |
| MAP-019 | 解锁触发成就检查 | 条件满足 | unlock() | 成就解锁 | P1 |
| MAP-020 | 船只位置更新 | 解锁区域 | updateShip() | position=新区域 | P1 |

### 1.4 UI测试 - WorldMapScreen

| 用例ID | 用例名称 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| MAP-021 | 点击解锁区域导航 | tap unlocked | 导航到关卡选择 | P0 |
| MAP-022 | 点击锁定区域显示弹窗 | tap locked | 显示锁定提示 | P0 |
| MAP-023 | 迷雾层正确渲染 | render | 迷雾覆盖锁定区 | P0 |
| MAP-024 | 船只动画到新区域 | unlock | 船只移动动画 | P1 |
| MAP-025 | 世界视图切换 | toggle | 切换地图模式 | P0 |

---

## 2. 视觉反馈增强

### 2.1 单元测试 - AnswerAnimations

| 用例ID | 用例名称 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| VFX-001 | 正确答案触发绿色闪烁 | isCorrect=true | flash=Color.Green | P0 |
| VFX-002 | 错误答案触发红色闪烁 | isCorrect=false | flash=Color.Red | P0 |
| VFX-003 | 闪烁动画时长100ms | flash | duration=100ms | P0 |
| VFX-004 | 详细反馈动画时长500ms | detail | duration=500ms | P0 |
| VFX-005 | 星星依次弹出 | stars=3 | delay=100ms | P0 |
| VFX-006 | 触觉反馈正确答案短脉冲 | correct | haptic=short | P1 |
| VFX-007 | 触觉反馈错误答案长脉冲 | wrong | haptic=long | P1 |
| VFX-008 | 音效播放正确答案 | correct | sound=ding | P1 |

### 2.2 单元测试 - ComboIndicator

| 用例ID | 用例名称 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| VFX-009 | 连续正确增加连击 | correct=3 | combo=3 | P0 |
| VFX-010 | 错误答案重置连击 | wrong | combo=0 | P0 |
| VFX-011 | 猜测不增加连击 | guessing | combo不变 | P0 |
| VFX-012 | 3连击触发小彩花 | combo=3 | confetti=small | P0 |
| VFX-013 | 5连击触发中烟花 | combo=5 | confetti=medium | P0 |
| VFX-014 | 连击计数器弹跳动画 | combo++ | bounce | P1 |
| VFX-015 | 关卡完成触发全屏庆祝 | complete | fullscreen | P0 |

---

## 3. 成就系统

### 3.1 进度成就测试 (Progress - 12个)

| 用例ID | 成就ID | 触发条件 | 预期奖励 | 优先级 |
|--------|--------|----------|----------|--------|
| ACH-001 | FIRST_STEPS | 学习第1个词 | 10金币 | P0 |
| ACH-002 | LEVEL_ONE_MASTER | 完成Level 1 | 20金币 | P0 |
| ACH-003 | VOCABULARY_BUILDER | 学会10个词 | 35金币 | P0 |
| ACH-004 | WORD_EXPERT | 学会50个词 | 50金币 | P1 |
| ACH-005 | HALF_WAY_THERE | 学会90个词 | 100金币 | P1 |
| ACH-006 | WORDLAND_MASTER | 学会180个词 | 200金币 | P1 |
| ACH-007 | ISLAND_EXPLORER | 解锁1个岛屿 | 称号 | P0 |
| ACH-008 | ARCHIPELAGO_MASTER | 解锁3个岛屿 | 宠物 | P1 |
| ACH-009 | LEVEL_PERFECTIONIST | 10关3星 | 徽章 | P1 |
| ACH-010 | DEDICATED_LEARNER | 连续学习7天 | 徽章 | P0 |
| ACH-011 | MONTHLY_CHAMPION | 连续学习30天 | 宠物 | P1 |
| ACH-012 | EARLY_ADOPTER | 首周内注册 | 称号 | P2 |

### 3.2 表现成就测试 (Performance - 10个)

| 用例ID | 成就ID | 触发条件 | 预期奖励 | 优先级 |
|--------|--------|----------|----------|--------|
| ACH-013 | PERFECT_SCORE | 任一关卡3星 | 20金币 | P0 |
| ACH-014 | HINT_FREE | 无提示完成关卡 | 35金币 | P0 |
| ACH-015 | SPEED_DEMON | 2分钟内完成 | 35金币 | P0 |
| ACH-016 | NO_MISTAKES | 100%准确率 | 50金币 | P1 |
| ACH-017 | FLAWLESS_RUN | 连续5题无错 | 徽章 | P1 |
| ACH-018 | COMBO_MASTER | 达到10连击 | 徽章 | P0 |
| ACH-019 | UNSTOPPABLE | 达到20连击 | 宠物 | P1 |
| ACH-020 | TIME_ATTACK | 30秒完成题 | 20金币 | P2 |
| ACH-021 | MARATHONER | 单局30分钟 | 徽章 | P2 |
| ACH-022 | PERFECT_SESSION | 所有题全对 | 100金币 | P1 |

### 3.3 连击成就测试 (Combo - 6个)

| 用例ID | 成就ID | 触发条件 | 预期奖励 | 优先级 |
|--------|--------|----------|----------|--------|
| ACH-023 | COMBO_3 | 3连击 | 10金币 | P0 |
| ACH-024 | COMBO_5 | 5连击 | 20金币 | P0 |
| ACH-025 | COMBO_10 | 10连击 | 50金币 | P1 |
| ACH-026 | COMBO_20 | 20连击 | 宠物 | P1 |
| ACH-027 | COMBO_MASTER | 达到50连击 | 称号 | P2 |
| ACH-028 | UNBROKEN | 单局无错误 | 徽章 | P1 |

### 3.4 连续成就测试 (Streak - 6个)

| 用例ID | 成就ID | 触发条件 | 预期奖励 | 优先级 |
|--------|--------|----------|----------|--------|
| ACH-029 | DAILY_LEARNER | 连续2天 | 15金币 | P0 |
| ACH-030 | WEEKLY_WARRIOR | 连续7天 | 50金币 | P0 |
| ACH-031 | MONTHLY_MASTER | 连续30天 | 宠物 | P1 |
| ACH-032 | CENTURION | 连续100天 | 称号 | P2 |
| ACH-033 | COMEBACK | 中断后回归 | 徽章 | P2 |
| ACH-034 | CONSISTENT | 90天内有80天 | 徽章 | P2 |

### 3.5 Quick Judge成就测试 (8个)

| 用例ID | 成就ID | 触发条件 | 预期奖励 | 优先级 |
|--------|--------|----------|----------|--------|
| ACH-035 | QUICK_JUDGE_BEGINNER | 完成1局 | 10金币 | P0 |
| ACH-036 | QUICK_JUDGE_REGULAR | 完成10局 | 30金币 | P1 |
| ACH-037 | PERFECT_JUDGE | 100%准确率 | 50金币 | P0 |
| ACH-038 | SPEED_JUDGE | 平均<2秒/题 | 35金币 | P1 |
| ACH-039 | HARD_MODE_WIN | Hard模式3星 | 宠物 | P1 |
| ACH-040 | QUICK_THINKER | 连续5局<3秒 | 徽章 | P1 |
| ACH-041 | JUDGE_MASTER | 完成100局 | 称号 | P2 |
| ACH-042 | ALL_DIFFICULTIES | 全难度3星 | 宠物 | P2 |

### 3.6 触发机制测试

| 用例ID | 测试场景 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| ACH-043 | 成就立即触发 | 条件满足 | 即时解锁 | P0 |
| ACH-044 | 重复成就不触发 | 已解锁 | 不重复触发 | P0 |
| ACH-045 | 成就进度持久化 | restart | 进度保持 | P0 |
| ACH-046 | 多成就同时解锁 | 多条件 | 全部解锁 | P0 |
| ACH-047 | 成就通知显示 | unlock | 弹窗通知 | P0 |
| ACH-048 | 金币奖励到账 | unlock | coins增加 | P0 |
| ACH-049 | 称号装备显示 | unlock | 称号可见 | P1 |
| ACH-050 | 宠物解锁显示 | unlock | 宠物可用 | P1 |

### 3.7 UI测试

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| ACH-051 | 成就列表显示 | open | 所有类别显示 | P0 |
| ACH-052 | 锁定成就显示进度 | locked | 进度条显示 | P0 |
| ACH-053 | 解锁成就显示奖励 | unlocked | 奖励显示 | P0 |
| ACH-054 | 成就详情弹窗 | tap | 详情显示 | P0 |
| ACH-055 | 成就通知动画 | unlock | 弹窗动画 | P1 |

---

## 4. 统计系统

### 4.1 GameHistory测试 (10个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| STAT-001 | 游戏历史记录完整 | 所有字段非空 | P0 |
| STAT-002 | 时长计算正确 | endTime-startTime=duration | P0 |
| STAT-003 | 准确率计算 | correct/total=accuracy | P0 |
| STAT-004 | 平均响应时间 | 排除极端值 | P0 |
| STAT-005 | GameMode枚举值 | 包含所有模式 | P0 |
| STAT-006 | 历史记录按时间倒序 | latest first | P0 |
| STAT-007 | 按关卡筛选 | 只返回匹配关卡 | P0 |
| STAT-008 | 按模式筛选 | 只返回匹配模式 | P0 |
| STAT-009 | 分页返回 | 每页20条 | P0 |
| STAT-010 | 日期范围筛选 | 范围内记录 | P1 |

### 4.2 LevelStatistics测试 (10个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| STAT-011 | 平均分计算 | totalScore/games | P0 |
| STAT-012 | 总体准确率 | totalCorrect/totalQuestions | P0 |
| STAT-013 | 新游戏更新统计 | 完成游戏后更新 | P0 |
| STAT-014 | 最佳时间更新 | 更快时间覆盖 | P0 |
| STAT-015 | 完美游戏计数 | 3星游戏计数 | P0 |
| STAT-016 | 首次游戏时间 | 首次非空 | P0 |
| STAT-017 | 最近游戏时间 | 最近更新 | P0 |
| STAT-018 | 最高连击记录 | maxCombo更新 | P1 |
| STAT-019 | 统计持久化 | restart后保持 | P0 |
| STAT-020 | 多关卡查询 | 返回所有关卡 | P0 |

### 4.3 GlobalStatistics测试 (10个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| STAT-021 | 总学习时间 | 所有游戏时长之和 | P0 |
| STAT-022 | 当前连续天数 | 连续学习天数 | P0 |
| STAT-023 | 连续重置 | 隔天后重置 | P0 |
| STAT-024 | 最长连续更新 | 超过当前时更新 | P0 |
| STAT-025 | 掌握单词计数 | strength≥85 | P0 |
| STAT-026 | 完成关卡计数 | completedLevels | P0 |
| STAT-027 | 完美关卡计数 | 3星关卡数 | P0 |
| STAT-028 | 总练习次数 | sessions | P1 |
| STAT-029 | 总正确答题数 | correctAnswers | P1 |
| STAT-030 | 全局统计持久化 | restart后保持 | P0 |

### 4.4 UseCase测试 (10个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| STAT-031 | 记录游戏历史 | save | 三个统计都更新 | P0 |
| STAT-032 | 记录触发成就 | save | 检查成就 | P0 |
| STAT-033 | 记录更新进度 | save | 用户进度更新 | P0 |
| STAT-034 | 数据库错误处理 | db error | 返回Error | P0 |
| STAT-035 | 部分更新回滚 | fail | 全部回滚 | P0 |
| STAT-036 | 获取游戏历史 | get | 返回用户历史 | P0 |
| STAT-037 | 按关卡筛选 | filterByLevel | 匹配关卡 | P0 |
| STAT-038 | 按模式筛选 | filterByMode | 匹配模式 | P0 |
| STAT-039 | 按日期筛选 | filterByDate | 匹配日期 | P0 |
| STAT-040 | 空结果处理 | empty | 返回空列表 | P0 |

### 4.5 UI测试 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| STAT-041 | 全局统计卡片 | render | 所有指标显示 | P0 |
| STAT-042 | 游戏历史列表 | render | 分页加载 | P0 |
| STAT-043 | 关卡统计卡片 | render | 正确数据显示 | P0 |
| STAT-044 | 标签切换 | tapTab | 内容切换 | P0 |
| STAT-045 | 筛选器应用 | applyFilter | 筛选正确 | P0 |

---

## 5. 语境化学习

### 5.1 单元测试 (10个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| CTX-001 | 句子模式生成空位 | blank position | P0 |
| CTX-002 | 空位位置随机 | 不同位置 | P0 |
| CTX-003 | 句子提示相关 | hint相关词 | P0 |
| CTX-004 | Level 4-6认知负荷 | 80%新20%复习 | P0 |
| CTX-005 | Level 7-10认知负荷 | 20%新80%复习 | P0 |
| CTX-006 | 故事模式句子连续 | 前后连贯 | P1 |
| CTX-007 | 语境难度匹配 | 匹配单词难度 | P1 |
| CTX-008 | 句子长度适中 | <15词 | P1 |
| CTX-009 | 无重复句子 | 每次不同 | P1 |
| CTX-010 | 语境缓存有效 | 复用已加载 | P1 |

### 5.2 集成测试 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| CTX-011 | 句子模式加载 | loadLevel | 正确单词 | P0 |
| CTX-012 | 答案验证 | submit | 语境验证 | P0 |
| CTX-013 | 进度更新 | complete | 更新正确 | P0 |
| CTX-014 | 故事模式加载 | loadStory | 连续句子 | P0 |
| CTX-015 | 语境反馈显示 | feedback | 完整句子 | P0 |

### 5.3 UI测试 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| CTX-016 | 句子语境显示 | render | 句子显示 | P0 |
| CTX-017 | 提示显示 | render | 提示显示 | P0 |
| CTX-018 | 正确答案反馈 | correct | 完整句子 | P0 |
| CTX-019 | 故事模式显示 | render | 连续显示 | P0 |
| CTX-020 | 进度条更新 | progress | 故事进度 | P0 |

---

## 6. Listen Find模式

### 6.1 单元测试 (12个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| LIS-001 | 生成4个图片选项 | options.size=4 | P0 |
| LIS-002 | 正确答案在选项中 | contains correct | P0 |
| LIS-003 | 选项随机化 | 顺序随机 | P0 |
| LIS-004 | 干扰项同关卡 | 同级别词 | P0 |
| LIS-005 | 音频路径正确 | path正确 | P0 |
| LIS-006 | 正确选择验证 | isCorrect=true | P0 |
| LIS-007 | 错误选择验证 | isCorrect=false | P0 |
| LIS-008 | 超时处理 | timeout=false | P0 |
| LIS-009 | 分数计算 | score公式 | P0 |
| LIS-010 | 难度调整时间限制 | time by difficulty | P0 |
| LIS-011 | 音频预加载 | preload | P1 |
| LIS-012 | 图片缓存有效 | cached | P1 |

### 6.2 集成测试 (8个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| LIS-013 | 音频播放 | startQuestion | 音频播放 | P0 |
| LIS-014 | 正确选择下一题 | selectCorrect | 下一题 | P0 |
| LIS-015 | 错误选择反馈 | selectWrong | 反馈显示 | P0 |
| LIS-016 | 音频重播 | tapReplay | 再次播放 | P0 |
| LIS-017 | 图片加载Coil | loadImages | 图片显示 | P0 |
| LIS-018 | 音频释放 | release | 资源释放 | P0 |
| LIS-019 | 进度保存 | complete | 进度保存 | P0 |
| LIS-020 | 统计更新 | complete | 统计更新 | P0 |

### 6.3 UI测试 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| LIS-021 | 播放按钮显示 | render | 按钮可见 | P0 |
| LIS-022 | 图片网格显示 | render | 4图片网格 | P0 |
| LIS-023 | 正确选择高亮 | selectCorrect | 绿色 | P0 |
| LIS-024 | 错误选择高亮 | selectWrong | 红色 | P0 |
| LIS-025 | 倒计时显示 | render | 时间显示 | P0 |

---

## 7. 防沉迷系统

### 7.1 会话管理测试 (15个)

| 用例ID | 测试场景 | 验证点 | 优先级 |
|--------|----------|--------|--------|
| ADD-001 | 活跃游戏时间计数 | active=true | 时间增加 | P0 |
| ADD-002 | 后台时间不计数 | background | 时间不增加 | P0 |
| ADD-003 | 暂停时间不计数 | paused | 时间不增加 | P0 |
| ADD-004 | 菜单时间不计数 | inMenu | 时间不增加 | P0 |
| ADD-005 | 软提醒15分钟触发 | 15min | 显示提醒 | P0 |
| ADD-006 | 硬限制45分钟触发 | 45min | 强制休息 | P0 |
| ADD-007 | 每日上限2小时触发 | 2hours | 阻塞 | P0 |
| ADD-008 | 休息提醒每5分钟 | 5min | 显示提醒 | P0 |
| ADD-009 | 会话状态持久化 | restart | 状态保持 | P0 |
| ADD-010 | 时间跨会话累积 | new session | 累积 | P0 |
| ADD-011 | 活跃状态检测 | gameplay | active=true | P0 |
| ADD-012 | 非活跃状态检测 | menu | active=false | P0 |
| ADD-013 | 今日时间计算 | today | 累计今日 | P0 |
| ADD-014 | 跨日时间重置 | new day | 重置 | P0 |
| ADD-015 | 时区变化处理 | timezone | 正确处理 | P1 |

### 7.2 休息奖励测试 (10个)

| 用例ID | 测试场景 | 输入 | 预期输出 | 优先级 |
|--------|----------|------|----------|--------|
| ADD-016 | 5分钟休息1.2倍 | 5min | 1.2x | P0 |
| ADD-017 | 15分钟休息1.5倍 | 15min | 1.5x | P0 |
| ADD-018 | 30分钟休息2倍 | 30min | 2.0x | P0 |
| ADD-019 | 1小时休息3倍 | 60min | 3.0x | P0 |
| ADD-020 | 隔天休息5倍 | tomorrow | 5.0x | P1 |
| ADD-021 | 奖励应用单词数 | 1.5x | 5词 | P0 |
| ADD-022 | 奖励到期时间 | apply | 24h后到期 | P0 |
| ADD-023 | 奖励消耗 | useWord | count-- | P0 |
| ADD-024 | 奖励成就解锁 | 1h | DEDICATED_LEARNER | P1 |
| ADD-025 | 隔天成就 | tomorrow | WELL_RESTED | P1 |

### 7.3 家长控制测试 (10个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| ADD-026 | PIN保护设置 | changeSettings | 需要PIN | P0 |
| ADD-027 | 错误PIN拒绝 | wrongPIN | 拒绝 | P0 |
| ADD-028 | 软提醒时间可配置 | config | 生效 | P0 |
| ADD-029 | 硬限制时间可配置 | config | 生效 | P0 |
| ADD-030 | 每日上限可配置 | config | 生效 | P0 |
| ADD-031 | 睡眠模式启用 | bedtime | 阻塞 | P0 |
| ADD-032 | 睡眠模式30分钟提醒 | bedtime-30min | 提醒 | P0 |
| ADD-033 | 周报生成 | weekly | 报告生成 | P1 |
| ADD-034 | 邮件通知配置 | config | 生效 | P1 |
| ADD-035 | PIN重置流程 | reset | 可重置 | P1 |

### 7.4 UI测试 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| ADD-036 | 软提醒对话框 | 15min | 显示对话框 | P0 |
| ADD-037 | 硬限制对话框 | 45min | 阻塞 | P0 |
| ADD-038 | 每日限制屏幕 | 2hours | 阻塞 | P0 |
| ADD-039 | 休息屏幕倒计时 | break | 倒计时 | P0 |
| ADD-040 | 家长仪表板 | open | 显示设置 | P0 |

---

## 8. 边界条件测试

### 8.1 超时相关 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| EDGE-001 | Quick Judge超时 | timeout | 算错 | P0 |
| EDGE-002 | 超时后提交 | submit | 接受答案 | P0 |
| EDGE-003 | 连续超时 | timeout×3 | 提示 | P0 |
| EDGE-004 | 超时动画 | timeout | 动画显示 | P1 |
| EDGE-005 | 超时音效 | timeout | 音效播放 | P1 |

### 8.2 屏幕旋转 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| EDGE-006 | 答题中旋转 | rotate | 状态保持 | P0 |
| EDGE-007 | 动画中旋转 | rotate | 动画继续 | P1 |
| EDGE-008 | 反馈中旋转 | rotate | 反馈保持 | P0 |
| EDGE-009 | 进度中旋转 | rotate | 进度保持 | P0 |
| EDGE-010 | 菜单中旋转 | rotate | 状态保持 | P1 |

### 8.3 快速点击 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| EDGE-011 | 连续快速提交 | spam | 防抖 | P0 |
| EDGE-012 | 同时点击多个按钮 | multiTap | 只响应第一个 | P0 |
| EDGE-013 | 快速跳过动画 | skip | 可跳过 | P1 |
| EDGE-014 | 快速切换关卡 | switch | 平滑切换 | P0 |
| EDGE-015 | 快速点击提示 | spamHint | 冷却 | P0 |

### 8.4 应用中断 (5个)

| 用例ID | 测试场景 | 操作 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| EDGE-016 | 来电话时答题 | call | 暂停 | P0 |
| EDGE-017 | 通知中断 | notification | 状态保持 | P0 |
| EDGE-018 | 切换应用 | switch | 暂停计时 | P0 |
| EDGE-019 | 低电量模式 | lowBattery | 正常运行 | P1 |
| EDGE-020 | 内存压力 | memory | 不崩溃 | P0 |

### 8.5 数据边界 (10个)

| 用例ID | 测试场景 | 输入 | 预期结果 | 优先级 |
|--------|----------|------|----------|--------|
| EDGE-021 | 空单词列表 | [] | 错误处理 | P0 |
| EDGE-022 | 超长单词 | >15字母 | 处理 | P0 |
| EDGE-023 | 特殊字符 | @#$ | 过滤 | P0 |
| EDGE-024 | 空用户答案 | "" | 提示 | P0 |
| EDGE-025 | 最大连击 | maxInt | 封顶 | P1 |
| EDGE-026 | 最小响应时间 | 0ms | 猜测 | P0 |
| EDGE-027 | 最大响应时间 | >5min | 超时 | P1 |
| EDGE-028 | 负分数 | negative | 最小0 | P0 |
| EDGE-029 | 最大关卡数 | Int.MAX | 正常 | P1 |
| EDGE-030 | 数据库满 | full | 清理 | P1 |

---

## 附录: 测试用例模板

```kotlin
/**
 * 测试用例模板
 */
@Test
fun `用例ID - 用例名称`() = runTest {
    // Given - 准备测试数据
    val input = /* 测试输入 */

    // When - 执行被测操作
    val result = /* 被测函数调用 */

    // Then - 验证结果
    assertEquals(expected, result)
}
```

---

**文档状态**: ✅ 完成
**总计用例**: 220个
**预计工作量**: 22人天

**下一步**: 开始Phase 1实施
