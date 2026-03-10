# Bug 修复报告 - Level 排序问题

**Bug ID**: LEVEL-001
**发现日期**: 2026-02-20
**修复日期**: 2026-02-20
**严重级别**: P2（UI 显示问题）
**状态**: ✅ 已修复并验证

---

## Bug 描述

### 问题
在真机上，Look Island 的 Level 1 和 Level 2 在 IslandMap 页面上显示顺序颠倒。

### 预期行为
Level 1 应该显示在最上面，然后依次是 Level 2, Level 3, Level 4, Level 5。

### 实际行为
Level 2 显示在最上面，Level 1 在第二位。

### 影响范围
- IslandMapScreen 关卡列表显示
- LevelSelectScreen 关卡列表显示
- 所有使用 `getLevelsByIsland` 的地方

---

## 根本原因分析

### 问题定位
文件：`app/src/main/java/com/wordland/data/dao/ProgressDao.kt`

### 问题代码
```kotlin
// 第 125 行 - 修复前
@Query("SELECT * FROM level_progress WHERE userId = :userId AND islandId = :islandId ORDER BY createdAt ASC")
suspend fun getLevelsByIsland(
    userId: String,
    islandId: String,
): List<LevelProgress>
```

### 原因分析
1. 使用 `createdAt` 字段排序
2. 所有关卡在 `LevelDataSeeder.seedLookIsland()` 中几乎同时创建
3. 由于创建时间相近，排序顺序不稳定
4. 导致 Level 2 可能比 Level 1 先创建

### levelId 格式
```
look_island_level_01  // Level 1
look_island_level_02  // Level 2
look_island_level_03  // Level 3
look_island_level_04  // Level 4
look_island_level_05  // Level 5
```

---

## 修复方案

### 修复内容
将排序字段从 `createdAt` 改为 `levelId`。

### 修复代码
```kotlin
// 第 125 行 - 修复后
@Query("SELECT * FROM level_progress WHERE userId = :userId AND islandId = :islandId ORDER BY levelId ASC")
suspend fun getLevelsByIsland(
    userId: String,
    islandId: String,
): List<LevelProgress>
```

### 同步修复
同时修复了其他相关查询方法：

1. **getAllLevelProgress**
```kotlin
// 修复前
@Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY createdAt ASC")

// 修复后
@Query("SELECT * FROM level_progress WHERE userId = :userId ORDER BY levelId ASC")
```

2. **getUnlockedLevels**
```kotlin
// 修复前
@Query("SELECT * FROM level_progress WHERE userId = :userId AND status = 'UNLOCKED' ORDER BY createdAt ASC")

// 修复后
@Query("SELECT * FROM level_progress WHERE userId = :userId AND status = 'UNLOCKED' ORDER BY levelId ASC")
```

---

## 验证

### 编译验证
```bash
./gradlew assembleDebug
```
**结果**: ✅ BUILD SUCCESSFUL in 7s

### 真机验证
**设备**: Xiaomi 24031PN0DC (aurora)
**测试步骤**:
1. 清除应用数据
2. 重新安装修复后的 APK
3. 启动应用
4. 导航到 IslandMap
5. 截图验证关卡顺序

**验证截图**: `/tmp/fixed_island.png`

**验证结果**: ✅ 关卡顺序正确
- Level 1 在第一位 ✅
- Level 2 在第二位 ✅
- Level 3, 4, 5 依次排列 ✅

---

## 影响评估

### 影响的方法
1. `ProgressDao.getLevelsByIsland()`
2. `ProgressDao.getAllLevelProgress()`
3. `ProgressDao.getUnlockedLevels()`

### 影响的 UseCase
1. `GetLevelsUseCase`
2. `GetIslandsUseCase`
3. `UnlockNextLevelUseCase`

### 影响的 UI Screen
1. `IslandMapScreen`
2. `LevelSelectScreen`
3. `HomeScreen`（进度显示）

---

## 数据库迁移

### 是否需要迁移？
❌ 不需要

**原因**:
- 这是查询排序的修改
- 数据库 schema 没有变化
- 不影响现有数据
- 不需要增加数据库版本

---

## 测试建议

### 单元测试
更新相关单元测试，验证排序逻辑：

```kotlin
@Test
fun `getLevelsByIsland returns levels in correct order`() = runTest {
    // Given
    val levels = listOf(
        LevelProgress(levelId = "look_island_level_03", ...),
        LevelProgress(levelId = "look_island_level_01", ...),
        LevelProgress(levelId = "look_island_level_02", ...),
    )

    // When
    val result = progressDao.getLevelsByIsland("user_001", "look_island")

    // Then
    assertEquals("look_island_level_01", result[0].levelId)
    assertEquals("look_island_level_02", result[1].levelId)
    assertEquals("look_island_level_03", result[2].levelId)
}
```

### 集成测试
- ✅ 已在真机上验证
- ✅ 关卡顺序显示正确

---

## 预防措施

### 代码审查要点
1. 检查所有 `ORDER BY` 子句
2. 确保排序字段稳定且有意义
3. 对于有自然顺序的实体（如关卡），使用编号字段排序

### 设计原则
1. **使用业务字段排序**：对于有业务顺序的实体，使用业务编号字段排序
2. **避免时间排序**：对于同时创建的记录，不要使用 `createdAt` 排序
3. **显式排序**：如果需要特定顺序，使用明确的排序字段

---

## 总结

### 修复前
```sql
ORDER BY createdAt ASC  -- 不稳定排序
```

### 修复后
```sql
ORDER BY levelId ASC  -- 稳定排序
```

### 关键改进
- ✅ 关卡顺序稳定可预测
- ✅ 符合用户预期（1, 2, 3, 4, 5）
- ✅ 不依赖创建时间
- ✅ 易于维护和扩展

---

## 附录

### 修复文件
- `app/src/main/java/com/wordland/data/dao/ProgressDao.kt`

### 修改行数
- 第 120 行：`getAllLevelProgress`
- 第 122 行：`getUnlockedLevels`
- 第 125 行：`getLevelsByIsland`

### 测试证据
- 修复前截图：`/tmp/island_map_check.png`（顺序错误）
- 修复后截图：`/tmp/fixed_island.png`（顺序正确）

---

**修复人员**: Team Lead
**审查人员**: 待审查
**测试人员**: Team Lead
**报告生成**: 2026-02-20
