# SubmitAnswerUseCaseStarRatingTest 架构审查报告

## 审查概述
- **审查日期**: 2026-03-09
- **审查者**: android-architect
- **审查范围**: SubmitAnswerUseCase.calculateStars() 设计与测试一致性
- **相关 Epic**: Task #18 阶段 2 - Domain 层测试修复

---

## 当前实现分析

### calculateStars() 方法

**文件位置**: `app/src/main/java/com/wordland/domain/usecase/usecases/SubmitAnswerUseCase.kt:204-225`

**方法签名**:
```kotlin
private fun calculateStars(
    isCorrect: Boolean,
    isGuessing: Boolean,
    responseTime: Long,
    hintUsed: Boolean,
    wordLength: Int,
): Int
```

**计算逻辑**:
```
1. 错误答案 → 0 星
2. 检测为猜测 → 1 星
3. 使用提示 → 2 星
4. 响应时间过快（< 阈值） → 2 星
5. 正确答案 + 充足思考时间 → 3 星
```

**是否考虑尝试次数**: ❌ **否**（方法签名中没有尝试次数参数）

**文档说明**:
- 代码注释 (191-203行): "Child-friendly star rating" 设计
- 鼓励学习而非惩罚
- 关注准确性而非速度
- 星级递减原因只有：猜测检测、提示使用、响应时间过快

---

## 测试设计分析

### 测试意图

测试文件: `app/src/test/java/com/wordland/domain/usecase/usecases/submit/SubmitAnswerUseCaseStarRatingTest.kt`

**测试假设**: 星级评分应该考虑**尝试次数**

**测试预期行为**:

| 测试场景 | 测试名称 | 预期星级 | 实际星级 |
|---------|---------|---------|---------|
| 第1次尝试正确 | `first attempt correct answer gets 3 stars` | 3 | 3 ✅ |
| 第2次尝试正确 | `second attempt correct answer gets 2 stars` | 2 | **3** ❌ |
| 第3次尝试正确 | `third attempt correct answer gets 2 stars` | 2 | **3** ❌ |
| 第4次尝试正确 | `fourth attempt correct answer gets 2 stars` | 2 | **3** ❌ |
| 第5次尝试正确 | `fifth attempt correct answer gets 1 star` | 1 | **3** ❌ |
| 第6次尝试正确 | `sixth or later attempt correct answer gets 1 star` | 1 | **3** ❌ |
| 第1次+提示 | `correct answer with hint used reduces stars by 1` | 2 | 2 ✅ |
| 第2次+提示 | `second attempt correct with hint gets 1 star` | 1 | **2** ❌ |

**失败统计**: 约 **18 个测试失败**（所有基于尝试次数的测试）

---

## 设计一致性评估

### 1. 官方文档确认

**参考文档**: `docs/analysis/STAR_RATING_BEHAVIOR_AUDIT.md` (2026-02-24)

该文档明确指出：

> The Wordland app has **two separate star rating systems**:
>
> 1. **Per-Word Rating** (`SubmitAnswerUseCase.calculateStars()`) - Awards 0-3 stars per individual word
> 2. **Level-Level Rating** (`StarRatingCalculator.calculateStars()`) - Awards 0-3 stars for completing a level

**Per-Word 星级矩阵**（来自文档）:

| Scenario | Correct | Guessing | Hint Used | Response Time | Stars |
|----------|---------|----------|-----------|---------------|-------|
| Perfect | Yes | No | No | ≥ threshold | **3** |
| Too fast | Yes | No | No | < threshold | **2** |
| With hint | Yes | No | Yes | Any | **2** |
| Guessing | Yes | Yes | No | Any | **1** |
| Wrong | No | N/A | N/A | Any | **0** |

**关键发现**: 官方文档中的 Per-Word 星级矩阵 **不包含尝试次数**因素。

---

### 2. 子友好设计原则评估

#### ✅ 鼓励学习而非惩罚

**实现行为**: 无论尝试多少次，只要正确答案且符合条件，就能获得 3 星

**测试期望**: 第二次尝试开始降低星级

**评估**: **实现符合子友好原则**，测试违反

#### ✅ 关注准确性而非速度

**实现行为**: 星级基于准确性、思考时间、提示使用

**测试期望**: 星级基于尝试次数（历史准确性）

**评估**: **实现符合设计原则**，测试混淆了两种评分维度

#### ✅ 符合 KET 教学目标

**KET 教学目标**: 鼓励学习者反复练习，掌握词汇

**实现支持**: 反复尝试后正确仍给予高星级
**测试阻碍**: 惩罚反复尝试，降低学习积极性

---

### 3. 业务逻辑评估

#### 问题: 星级评分应该考虑尝试次数吗？

**分析**:

**Per-Word 星级** (即时反馈):
- 目的: 对单次回答给予即时反馈
- 设计原则: 鼓励正确答案，不过度惩罚
- **不考虑尝试次数** ✅

**Level-Level 星级** (关卡总结):
- 目的: 评估整个关卡的表现
- 设计原则: 考虑准确率、错误次数、时间
- **考虑尝试次数（通过错误惩罚）** ✅

**结论**: 两种星级系统有不同的职责，Per-Word 星级不应该考虑尝试次数。

---

### 4. 测试意图分析

**测试可能的设计来源**:
- 可能混淆了 Per-Word 和 Level-Level 星级系统
- 可能基于旧版本实现编写（Epic #5 重构前）
- 可能误解了"子友好"设计原则

**测试代码证据**:

```kotlin
// 第93-117行：测试期望第二次尝试获得 2 星
@Test
fun `second attempt correct answer gets 2 stars`() {
    val existingProgress = UserWordProgress(
        totalAttempts = 1,      // 第一次尝试失败
        correctAttempts = 0,
        incorrectAttempts = 1,
        // ...
    )
    // 期望: 2 星
    // 实际: 3 星（因为测试设置中 responseTime = 3000L，符合阈值）
}
```

**分析**: 测试设计者认为尝试次数应该影响星级，但这与官方文档不符。

---

## 决策建议

### ✅ 推荐方案: **选项 A - 测试错误，实现正确**

### 理由

#### 1. 官方文档明确支持实现

`STAR_RATING_BEHAVIOR_AUDIT.md` 清楚地记录了 Per-Word 星级不包含尝试次数因素。这是经过架构审查的设计决策。

#### 2. 符合子友好设计原则

- 鼓励学习：孩子反复尝试后正确，应该获得积极反馈
- 避免挫败感：不会因为尝试次数减少星级
- 保持动力：每次正确都能获得最多 3 星

#### 3. 职责分离清晰

| 星级系统 | 职责 | 考虑尝试次数 |
|---------|------|-------------|
| Per-Word | 即时反馈 | ❌ 否 |
| Level-Level | 关卡总结 | ✅ 是（通过错误惩罚） |

#### 4. 代码实现与文档一致

- `calculateStars()` 方法签名没有尝试次数参数
- 代码注释明确说明 "Child-friendly star rating"
- 实现逻辑与官方文档的决策树完全匹配

#### 5. 测试违反 Clean Architecture

测试测试了不存在的业务逻辑（尝试次数影响星级），这是测试设计的错误。

---

## 实施建议

### 需要更新的测试

**测试文件**: `app/src/test/java/com/wordland/domain/usecase/usecases/submit/SubmitAnswerUseCaseStarRatingTest.kt`

#### 需要删除的测试（6个）

| 测试名称 | 原因 |
|---------|------|
| `second attempt correct answer gets 2 stars` | 尝试次数不影响星级 |
| `third attempt correct answer gets 2 stars` | 尝试次数不影响星级 |
| `fourth attempt correct answer gets 2 stars` | 尝试次数不影响星级 |
| `fifth attempt correct answer gets 1 star` | 尝试次数不影响星级 |
| `sixth or later attempt correct answer gets 1 star` | 尝试次数不影响星级 |
| `second attempt correct with hint gets 1 star` | 提示只降到 2 星，不会因尝试次数再降 |

#### 需要修改的测试（2个）

| 测试名称 | 修改内容 |
|---------|---------|
| `second attempt correct with hint gets 1 star` | 改为期望 2 星（提示惩罚不叠加） |
| 类似的组合测试 | 移除尝试次数相关的断言 |

#### 需要保留的测试

| 测试名称 | 状态 |
|---------|------|
| `first attempt correct answer gets 3 stars` | ✅ 保留 |
| `correct answer with hint used reduces stars by 1` | ✅ 保留 |
| `wrong answer result contains isGuessing flag` | ✅ 保留 |
| `correct answer has isGuessing as false` | ✅ 保留 |
| `hintUsed parameter is recorded in tracking` | ✅ 保留 |

---

### 新的测试方向（建议）

删除基于尝试次数的测试后，应该增加以下测试以完善覆盖：

#### 1. 响应时间边界测试

```kotlin
@Test
fun `answer below thinking time threshold gets 2 stars`() {
    // 3-letter word, 4-letter word threshold测试
}

@Test
fun `answer above thinking time threshold gets 3 stars`() {
    // 验证充足思考时间获得 3 星
}
```

#### 2. 猜测检测集成测试

```kotlin
@Test
fun `answer detected as guessing gets 1 star regardless of time`() {
    // 验证猜测检测优先级
}
```

#### 3. 提示使用不叠加测试

```kotlin
@Test
fun `hint with fast response still gets 2 stars not 1`() {
    // 验证提示惩罚不与其他惩罚叠加
}
```

---

## 后续行动

### 立即行动

1. **android-engineer**: 根据 `STAR_RATING_BEHAVIOR_AUDIT.md` 更新测试文件
2. **android-test-engineer**: 验证更新后的测试通过率
3. **android-architect**: 审查更新后的测试设计

### 中期行动

1. **更新测试文档**: 在测试文件顶部添加 Per-Word 星级设计的引用
2. **添加架构注释**: 在 `SubmitAnswerUseCase.kt` 中添加更多子友好设计的说明
3. **测试代码审查**: 建立测试与文档一致性的审查流程

### 长期行动

1. **文档完善**: 考虑创建测试编写指南，明确如何验证测试设计与业务文档一致性
2. **架构守护**: 在 CI 中添加测试设计检查，防止类似的测试-实现不匹配

---

## 附录: Per-Word vs Level-Level 星级对比

| 维度 | Per-Word (SubmitAnswerUseCase) | Level-Level (StarRatingCalculator) |
|------|-------------------------------|-----------------------------------|
| **目的** | 即时反馈 | 关卡总结 |
| **粒度** | 单个单词 | 整个关卡（6个单词） |
| **考虑因素** | 正确性、猜测、提示、时间 | 准确率、错误次数、提示、时间、连击 |
| **尝试次数** | ❌ 不考虑 | ✅ 考虑（通过错误惩罚） |
| **星级范围** | 0-3 | 0-3 |
| **使用场景** | 答题后立即显示 | 关卡完成后显示 |

---

## 审查结论

✅ **实现正确**，测试需要更新以匹配官方文档和子友好设计原则。

**预计影响**: 删除约 6-8 个测试，新增 3-5 个测试以覆盖实际业务逻辑。

**风险等级**: 低（测试更新不影响生产代码）

**建议优先级**: P1（应该尽快修复以避免误导未来的开发）
