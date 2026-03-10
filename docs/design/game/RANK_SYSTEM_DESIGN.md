# 段位系统设计方案

**创建日期：** 2026-02-17
**状态：** 设计完成，待实现（P1优先级）
**参考：** 王者荣耀排位赛机制

---

## 一、核心概念

### 双轨并行系统

```
关卡内：短期挑战
├─ 普通模式：无血条，专注学习
└─ 困难模式：有血条，失败重开

长期：段位成长
└─ 段位模式：积分制，升降级
```

### 设计原则

1. **学习优先** - 普通模式保证学习体验
2. **挑战可选** - 困难模式/段位模式为可选增强
3. **正向激励** - 为主，降分为辅
4. **长期成长** - 通过赛季机制保持长期动力

---

## 二、段位等级系统

### 段位层级

```
🥉 青铜段位
  ├─ 青铜 III (0-99分)
  ├─ 青铜 II (100-199分)
  └─ 青铜 I (200-399分)

🥈 白银段位
  ├─ 白银 III (400-599分)
  ├─ 白银 II (600-799分)
  └─ 白银 I (800-999分)

🥇 黄金段位
  ├─ 黄金 III (1000-1299分)
  ├─ 黄金 II (1300-1599分)
  └─ 黄金 I (1600-1999分)

💎 铂金段位
  ├─ 铂金 III (2000-2399分)
  ├─ 铂金 II (2400-2799分)
  └─ 铂金 I (2800-3199分)

💍 钻石段位
  ├─ 钻石 III (3200-3699分)
  ├─ 钻石 II (3700-4199分)
  └─ 钻石 I (4200-4799分)

👑 大师段位 (4800-5499分)
🏆 王者段位 (5500分以上)
```

### 段位标识

- 青铜：🥉 Bronze
- 白银：🥈 Silver
- 黄金：🥇 Gold
- 铂金：💎 Platinum
- 钻石：💍 Diamond
- 大师：👑 Master
- 王者：🏆 Challenger

---

## 三、积分规则

### 基础分值

| 结果 | 积分变化 | 说明 |
|------|---------|------|
| ✅ 完全正确（3★） | +20分 | 无错误、无提示 |
| ✅ 基本正确（2★） | +15分 | 有小错误或使用1次提示 |
| ✅ 勉强正确（1★） | +10分 | 多次错误或使用多次提示 |
| ❌ 回答错误 | -15分 | 关卡失败 |
| 💡 使用提示 | -5分 | 每次使用扣分 |
| 🎯 困难模式通关 | 额外+30分 | 完成困难模式奖励 |
| 🔥 连续答对（Combo×3） | +10分 | 连续奖励 |

### 段位保护机制

| 段位 | 保护规则 |
|------|---------|
| 青铜 | 不会降段，最低0分 |
| 白银-黄金 | 降级保护（段位内最低小段位不掉段） |
| 铂金以上 | 无保护，正常升降 |

---

## 四、血条系统（困难模式）

### 血条设置

| 模式 | 血量 | 说明 |
|------|------|------|
| 普通模式 | 无限 | 专注学习，无失败压力 |
| 困难模式 | 5点HP | 挑战模式，HP归零需重开 |

### 扣分规则

- 答错单词：-1 HP
- 使用提示：-1 HP
- HP归零：关卡失败，必须重新开始

### 失败处理

- **血条归零**：显示"关卡失败"，重新开始
- **段位积分**：失败也会扣分（-15分）
- **学习进度**：失败不保存，必须成功才能保存进度

---

## 五、赛季机制

### 赛季设置

- **赛季长度**：30天（一个月）
- **赛季编号**：Season 1, Season 2, ...
- **赛季开始**：每月1号
- **赛季结束**：每月最后一天23:59

### 赛季奖励

| 最终段位 | 奖励 |
|---------|------|
| 🏆 王者 | 特殊徽章 + 限定宠物皮肤 + 大量金币 |
| 👑 大师 | 宠物皮肤 + 中等金币 |
| 💍 钻石 | 大量金币 |
| 💎 铂金 | 中等金币 |
| 🥇 黄金 | 少量金币 |
| 🥈 白银 | 少量金币 |
| 🥉 青铜 | 参与奖励 |

### 赛季重置规则

| 赛季段位 | 重置后 | 积分保留 |
|---------|--------|---------|
| 🏆 王者 | 钻石 I | 80% |
| 👑 大师 | 黄金 I | 70% |
| 💍 钻石 | 铂金 II | 65% |
| 💎 铂金 | 黄金 II | 60% |
| 🥇 黄金 | 白银 II | 60% |
| 🥈 白银 | 青铜 I | 60% |
| 🥉 青铜 | 青铜 III | 50% |

---

## 六、UI设计

### 1. 主界面段位显示

```
┌─────────────────────────┐
│  我的段位                │
│  ┌───────────────────┐  │
│  │ 🥇 黄金 II         │  │
│  │ 1350 / 1600 分    │  │
│  └───────────────────┘  │
│                         │
│  赛季剩余：23天         │
│  本赛季场次：156场      │
└─────────────────────────┘
```

### 2. 关卡选择 - 模式选择

```
┌─────────────────────────┐
│  选择游戏模式            │
│                         │
│  ┌───────────────────┐  │
│  │ ○ 普通模式        │  │
│  │   专注学习，无压力 │  │
│  └───────────────────┘  │
│                         │
│  ┌───────────────────┐  │
│  │ ● 困难模式（段位） │  │
│  │   有血条，影响段位 │  │
│  └───────────────────┘  │
└─────────────────────────┘
```

### 3. 关卡内 - 血条显示

```
┌─────────────────────────┐
│  Level 1: Look Island   │
│  ███████████░░░░ 5/10    │
│                         │
│  HP: ❤️❤️❤️❤️❤️         │
└─────────────────────────┘
```

### 4. 关卡结算界面

```
┌─────────────────────────┐
│  🎉 关卡完成！           │
│  ⭐⭐⭐ 3星              │
│                         │
│  ┌───────────────────┐  │
│  │ 段位积分变化      │  │
│  │                   │  │
│  │   +35 分          │  │
│  │   1350 → 1385     │  │
│  └───────────────────┘  │
│                         │
│  当前段位：黄金 II       │
│  距离黄金 III还差115分   │
│                         │
│  [继续] [查看段位详情]   │
└─────────────────────────┘
```

### 5. 段位详情页面

```
┌─────────────────────────┐
│  🏆 我的段位            │
│                         │
│  ┌───────────────────┐  │
│  │   🥇 黄金 II      │  │
│  │                   │  │
│  │  1350 / 1600 分   │  │
│  │  ████████░░░░     │  │
│  └───────────────────┘  │
│                         │
│  本赛季数据：           │
│  - 场次：156场          │
│  - 胜率：78%            │
│  - 最高段位：黄金 I     │
│  - 赛季剩余：23天       │
│                         │
│  历史最佳：铂金 III      │
└─────────────────────────┘
```

---

## 七、数据模型

### UserRank 表

```sql
CREATE TABLE user_rank (
    user_id TEXT PRIMARY KEY,

    -- 当前段位信息
    current_rank_score INTEGER DEFAULT 0,           -- 当前积分
    current_rank TEXT DEFAULT 'bronze_iii',         -- 当前段位ID
    current_sub_rank INTEGER DEFAULT 0,             -- 0=III, 1=II, 2=I

    -- 赛季信息
    season_number INTEGER DEFAULT 1,                -- 赛季编号
    season_start_date INTEGER,                      -- 赛季开始时间戳

    -- 统计数据
    games_played INTEGER DEFAULT 0,                 -- 总场次
    games_won INTEGER DEFAULT 0,                    -- 胜利场次
    best_rank TEXT DEFAULT 'bronze_iii',            -- 历史最高段位
    total_score_all_time INTEGER DEFAULT 0,         -- 历史总积分

    -- 段位模式统计
    rank_mode_games INTEGER DEFAULT 0,              -- 段位模式场次
    rank_mode_wins INTEGER DEFAULT 0,               -- 段位模式胜利
    hard_mode_games INTEGER DEFAULT 0,              -- 困难模式场次
    hard_mode_wins INTEGER DEFAULT 0                -- 困难模式胜利
)
```

### RankMatchHistory 表（历史记录）

```sql
CREATE TABLE rank_match_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    level_id TEXT NOT NULL,
    island_id TEXT NOT NULL,

    -- 模式信息
    game_mode TEXT NOT NULL,                        -- 'normal', 'hard', 'rank'
    is_ranked_mode INTEGER DEFAULT 0,               -- 是否段位模式

    -- 结果信息
    result TEXT NOT NULL,                           -- 'win', 'lose'
    stars_earned INTEGER DEFAULT 0,
    score_change INTEGER DEFAULT 0,                 -- 积分变化
    rank_before TEXT,                               -- 赛前段位
    rank_after TEXT,                                -- 赛后段位

    -- 时间戳
    played_at INTEGER NOT NULL,                     -- 毫秒时间戳
    season_number INTEGER NOT NULL,                 -- 所属赛季

    FOREIGN KEY (user_id) REFERENCES user_rank(user_id)
)
```

### 段位配置数据结构

```kotlin
/**
 * 段位配置
 */
enum class RankTier(
    val id: String,
    val displayName: String,
    val icon: String,
    val minScore: Int,
    val maxScore: Int,
    val subRanks: Int = 3  // III, II, I
) {
    BRONZE("bronze", "青铜", "🥉", 0, 399),
    SILVER("silver", "白银", "🥈", 400, 999),
    GOLD("gold", "黄金", "🥇", 1000, 1999),
    PLATINUM("platinum", "铂金", "💎", 2000, 3199),
    DIAMOND("diamond", "钻石", "💍", 3200, 4799),
    MASTER("master", "大师", "👑", 4800, 5499),
    CHALLENGER("challenger", "王者", "🏆", 5500, Int.MAX_VALUE);

    fun getSubRank(score: Int): Int {
        return when (this) {
            BRONZE -> if (score < 100) 0 else if (score < 200) 1 else 2
            SILVER -> if (score < 600) 0 else if (score < 800) 1 else 2
            GOLD -> if (score < 1300) 0 else if (score < 1600) 1 else 2
            PLATINUM -> if (score < 2400) 0 else if (score < 2800) 1 else 2
            DIAMOND -> if (score < 3700) 0 else if (score < 4200) 1 else 2
            MASTER, CHALLENGER -> 0
        }
    }

    fun getSubRankName(subRank: Int): String {
        return when (subRank) {
            0 -> "III"
            1 -> "II"
            2 -> "I"
            else -> ""
        }
    }
}
```

---

## 八、业务逻辑

### UseCase 设计

#### 1. CalculateRankScoreChangeUseCase

```kotlin
/**
 * 计算段位积分变化
 */
class CalculateRankScoreChangeUseCase {
    fun execute(
        stars: Int,              // 获得星级 (0-3)
        isRankMode: Boolean,    // 是否段位模式
        isHardMode: Boolean,    // 是否困难模式
        hintsUsed: Int,         // 使用提示次数
        comboCount: Int,        // 连续答对次数
        isWin: Boolean          // 是否通关
    ): Int {
        if (!isRankMode) return 0

        var scoreChange = 0

        // 基础分
        when {
            isWin && stars == 3 -> scoreChange += 20
            isWin && stars == 2 -> scoreChange += 15
            isWin && stars == 1 -> scoreChange += 10
            !isWin -> scoreChange -= 15
        }

        // 提示扣分
        scoreChange -= hintsUsed * 5

        // 困难模式奖励
        if (isHardMode && isWin) {
            scoreChange += 30
        }

        // 连击奖励
        if (comboCount >= 3) {
            scoreChange += 10
        }

        return scoreChange
    }
}
```

#### 2. UpdateUserRankUseCase

```kotlin
/**
 * 更新用户段位
 */
class UpdateUserRankUseCase(
    private val rankRepository: RankRepository,
    private val getCurrentRankUseCase: GetCurrentRankUseCase
) {
    suspend fun execute(
        userId: String,
        scoreChange: Int,
        levelId: String,
        islandId: String,
        isRankMode: Boolean,
        isHardMode: Boolean,
        result: String,
        stars: Int
    ): Result<RankUpdateResult> {
        if (!isRankMode) {
            return Result.success(RankUpdateResult(null, null, 0))
        }

        val currentRank = rankRepository.getUserRank(userId)
        val newScore = (currentRank.currentRankScore + scoreChange)
            .coerceAtLeast(0)  // 最低0分

        val newRank = getCurrentRankUseCase.execute(newScore)

        // 检查段位保护
        val finalRank = checkRankProtection(
            oldRank = currentRank.currentRank,
            newRank = newRank.id
        )

        // 更新数据库
        rankRepository.updateUserRank(
            userId = userId,
            newScore = newScore,
            newRank = finalRank,
            gamesPlayed = currentRank.games_played + 1,
            gamesWon = if (result == "win") currentRank.games_won + 1 else currentRank.games_won
        )

        return Result.success(
            RankUpdateResult(
                oldRank = currentRank.currentRank,
                newRank = finalRank,
                scoreChange = scoreChange
            )
        )
    }

    private fun checkRankProtection(oldRank: String, newRank: String): String {
        val oldTier = RankTier.valueOf(oldRank.split("_")[0].uppercase())
        val newTier = RankTier.valueOf(newRank.split("_")[0].uppercase())

        // 青铜段位不降级
        if (newTier == RankTier.BRONZE) return newRank

        // 白银-黄金段位保护
        if (oldTier in listOf(RankTier.SILVER, RankTier.GOLD)) {
            if (newTier.ordinal < oldTier.ordinal) {
                // 检查是否在最低小段位
                // 如果是，则不掉段
                return oldRank
            }
        }

        return newRank
    }
}
```

#### 3. GetCurrentRankUseCase

```kotlin
/**
 * 根据积分获取当前段位
 */
class GetCurrentRankUseCase {
    fun execute(score: Int): RankInfo {
        val tier = when {
            score >= 5500 -> RankTier.CHALLENGER
            score >= 4800 -> RankTier.MASTER
            score >= 3200 -> RankTier.DIAMOND
            score >= 2000 -> RankTier.PLATINUM
            score >= 1000 -> RankTier.GOLD
            score >= 400 -> RankTier.SILVER
            else -> RankTier.BRONZE
        }

        val subRank = tier.getSubRank(score)
        val subRankName = tier.getSubRankName(subRank)

        return RankInfo(
            tierId = tier.name.lowercase(),
            tierName = tier.displayName,
            tierIcon = tier.icon,
            subRank = subRank,
            subRankName = subRankName,
            score = score
        )
    }
}
```

#### 4. CheckSeasonResetUseCase

```kotlin
/**
 * 检查并执行赛季重置
 */
class CheckSeasonResetUseCase(
    private val rankRepository: RankRepository,
    private val seasonManager: SeasonManager
) {
    suspend fun execute(userId: String): SeasonResetResult? {
        val currentSeason = rankRepository.getUserRank(userId).season_number
        val activeSeason = seasonManager.getCurrentSeasonNumber()

        if (currentSeason >= activeSeason) {
            return null  // 无需重置
        }

        // 执行赛季重置
        val oldRank = rankRepository.getUserRank(userId)
        val resetResult = seasonManager.calculateSeasonReset(
            oldRank = oldRank.currentRank,
            oldScore = oldRank.currentRank_score
        )

        // 更新数据库
        rankRepository.resetSeason(
            userId = userId,
            newSeason = activeSeason,
            newScore = resetResult.newScore,
            newRank = resetResult.newRank
        )

        return SeasonResetResult(
            oldSeason = currentSeason,
            newSeason = activeSeason,
            oldRank = oldRank.currentRank,
            newRank = resetResult.newRank,
            oldScore = oldRank.currentRank_score,
            newScore = resetResult.newScore
        )
    }
}
```

---

## 九、实现优先级

### P1 核心功能（第一期）- 2-3周

**目标**：基础段位系统可玩

- [ ] **数据层**
  - [ ] 创建UserRank表
  - [ ] 创建RankMatchHistory表
  - [ ] RankRepository实现
  - [ ] 数据库迁移脚本

- [ ] **领域层**
  - [ ] RankTier枚举和配置
  - [ ] CalculateRankScoreChangeUseCase
  - [ ] UpdateUserRankUseCase
  - [ ] GetCurrentRankUseCase
  - [ ] RankInfo数据模型

- [ ] **UI层**
  - [ ] 关卡选择界面 - 模式选择（普通/困难/段位）
  - [ ] 关卡内 - 血条显示
  - [ ] 关卡结算界面 - 积分变化显示
  - [ ] 主界面 - 段位信息卡片
  - [ ] 段位详情页面

- [ ] **集成**
  - [ ] 修改LearningViewModel支持段位模式
  - [ ] 修改SubmitAnswerUseCase计算段位积分
  - [ ] 血条逻辑集成到游戏流程
  - [ ] 测试所有三种游戏模式

### P1 增强功能（第二期）- 1-2周

**目标**：赛季系统和完整体验

- [ ] **赛季系统**
  - [ ] SeasonManager实现
  - [ ] CheckSeasonResetUseCase
  - [ ] 赛季倒计时UI
  - [ ] 赛季奖励发放

- [ ] **段位保护**
  - [ ] 完善降级保护逻辑
  - [ ] 青铜段位保护

- [ ] **奖励系统**
  - [ ] 段位奖励配置
  - [ ] 奖励发放UseCase
  - [ ] 奖励展示UI

- [ ] **统计和记录**
  - [ ] 历史战绩查询
  - [ ] 段位曲线图
  - [ ] 胜率统计

### P2 社交功能（第三期）- 未来

**目标**：竞技和社交

- [ ] **排行榜**
  - [ ] 好友排行榜
  - [ ] 全服排行榜
  - [ ] 段位排行榜

- [ ] **社交功能**
  - [ ] 段位对比
  - [ ] 段位徽章分享
  - [ ] 战绩分享

- [ ] **高级功能**
  - [ ] 段位匹配（模拟）
  - [ ] 段位赛活动
  - [ ] 特殊赛季主题

---

## 十、与现有系统的关系

### 记忆强度系统 vs 段位系统

| 特性 | 记忆强度系统 | 段位系统 |
|------|-------------|---------|
| 目的 | 学习效果跟踪 | 游戏成就激励 |
| 算法 | 间隔重复算法 | 积分升降制 |
| 时间跨度 | 长期（数周/数月） | 赛季制（30天） |
| 影响因素 | 答题正确性、复习间隔 | 答题结果、使用提示、模式难度 |
| 用户感知 | 隐性（后台跟踪） | 显性（段位、积分、排名） |
| 主要功能 | 优化复习时机 | 激励持续学习 |

**共存策略：**
- 两个系统独立运行，互不干扰
- 记忆强度控制学习节奏
- 段位系统提供游戏动力
- 数据层面可相互关联（如：高段位玩家通常记忆强度更高）

### Combo系统 vs 段位系统

- **Combo**：关卡内的短期激励（连续答对）
- **段位**：长期的成长系统（跨关卡、赛季）
- **配合**：Combo奖励可以同时影响星级和段位积分

---

## 十一、关键设计决策

### 已确定

1. ✅ **血条系统** - 困难模式5点HP
2. ✅ **段位等级** - 青铜到王者，7个大段位
3. ✅ **积分规则** - 答对加分，答错扣分，提示扣分
4. ✅ **赛季机制** - 30天赛季，结束时重置
5. ✅ **段位保护** - 青铜不降，白银-黄金有保护
6. ✅ **模式选择** - 普通/困难（段位）可选

### 待讨论

1. ❓ **每日上限** - 是否限制每日最多获得积分？
2. ❓ **初始积分** - 新用户从0分还是100分开始？
3. ❓ **降分机制** - 白银黄金的段位保护具体阈值？
4. ❓ **赛季奖励** - 具体奖励内容和数量？

---

## 十二、测试计划

### 功能测试

- [ ] 段位升降测试（积分增加/减少）
- [ ] 段位保护测试（青铜不掉段）
- [ ] 赛季重置测试
- [ ] 血条系统测试（困难模式）
- [ ] 三种游戏模式集成测试

### 边界测试

- [ ] 积分为0时的行为
- [ ] 段位边界测试（如399分→400分）
- [ ] 连续快速游戏
- [ ] 异常情况处理（网络断开、应用关闭）

### 性能测试

- [ ] 大量历史记录查询性能
- [ ] 段位计算性能
- [ ] 数据库迁移性能

---

## 附录

### 参考资源

- 王者荣耀排位赛机制
- 英雄联盟段位系统
- 炉石传说传说等级系统

### 相关文档

- `docs/design/COMBO_SYSTEM_DESIGN.md` - Combo系统设计
- `docs/design/MEMORY_STRENGTH_ALGORITHM.md` - 记忆强度算法
- `docs/analysis/HINT_SYSTEM_ANALYSIS.md` - 提示系统分析

---

**文档版本：** 1.0
**最后更新：** 2026-02-17
**作者：** Claude + 用户讨论
**状态：** 已批准，待实现（P1优先级）
