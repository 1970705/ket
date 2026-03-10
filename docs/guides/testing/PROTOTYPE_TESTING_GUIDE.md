# 最小原型测试指南

## 已完成的功能

### 1. 导航系统 ✅
- MainActivity 已连接到 SetupNavGraph
- 支持所有导航路由：Home, IslandMap, LevelSelect, Learning, Review, Progress
- 参数化导航支持（islandId, levelId）

### 2. 拼写战斗玩法 ✅
- **模型**: `SpellBattleQuestion.kt`
  - 答案验证
  - 错误位置检测
  - 进度计算

- **UI组件**: `SpellBattleGame.kt`
  - 虚拟键盘（QWERTY布局）
  - 答案框显示（实时反馈）
  - 错误位置高亮
  - 退格键支持

### 3. 内容扩充 ✅
- **30个单词**（原18个 → 30个）
- **5个关卡**（原3个 → 5个）
- 每个关卡6个词
- 关卡主题：
  - Level 1: Basic observation verbs (look, see, watch...)
  - Level 2: Colors and appearance
  - Level 3: Vision tools
  - Level 4: Looking actions
  - Level 5: Advanced observation

### 4. 关卡流程系统 ✅
- 题目队列管理（LearningViewModel）
- 进度追踪
- 关卡完成检测
- 基础星星评分（固定3星，需要改进）

---

## 测试步骤

### 前提条件
```bash
# 1. 确保有 Java 环境
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# 2. 构建项目
./gradlew clean assembleDebug

# 3. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 测试场景 1: 导航流程

**目标**: 验证所有界面可以导航

```
1. 启动应用
   → 应该显示 HomeScreen

2. 点击"岛屿地图"按钮
   → 应该导航到 IslandMapScreen
   → 显示 Look Island

3. 点击 Look Island
   → 应该导航到 LevelSelectScreen
   → 显示5个关卡（Level 1-5）

4. 点击 Level 1
   → 应该导航到 LearningScreen
   → 显示第一个单词的问题
```

**预期结果**: ✅ 所有导航正常工作

### 测试场景 2: 拼写战斗玩法

**目标**: 验证拼写战斗游戏机制

```
1. 在 LearningScreen 中
   → 显示中文翻译："看，观看"
   → 显示6个空答案框
   → 显示虚拟键盘

2. 使用虚拟键盘拼写 "look"
   → 每个字母填入一个框
   → 正确的字母显示蓝色
   → 错误的字母显示红色

3. 拼写错误测试
   → 故意拼写 "lok"（少一个字母）
   → 位置3应该高亮为错误

4. 使用退格键
   → 点击⌫键删除字母
   → 验证可以正常删除

5. 完成拼写
   → 正确拼写 "look"
   → 提交答案
   → 应该显示反馈界面
```

**预期结果**: ✅ 拼写战斗游戏正常工作

### 测试场景 3: 关卡流程

**目标**: 验证关卡内的单词队列

```
1. 完成 Level 1 的第1个词
   → 点击"继续"
   → 应该显示第2个词

2. 重复直到完成6个词
   → 应该显示关卡完成界面

3. 返回 LevelSelectScreen
   → Level 1 应该显示为已完成
   → Level 2 应该解锁
```

**预期结果**: ✅ 关卡流程正常工作

### 测试场景 4: 多个关卡

**目标**: 验证可以玩不同关卡

```
1. 选择 Level 2
   → 应该加载 Level 2 的6个词

2. 选择 Level 3
   → 应该加载 Level 3 的6个词

3. 尝试选择 Level 6（不存在）
   → 应该处理错误或显示空关卡
```

**预期结果**: ✅ 可以玩不同关卡

---

## 已知问题

### 1. 星星评分未实现
**现状**: 固定返回3星
**需要**: 根据正确率、响应时间计算真实星级

### 2. 拼写战斗未集成到 LearningScreen
**现状**: SpellBattleGame 组件已创建，但 LearningScreen 仍在使用简单文本问题
**需要**: 更新 LearningScreen 的 LearningContent 使用 SpellBattleGame

### 3. 无音频文件
**现状**: 所有 audioPath 指向不存在的文件
**影响**: 无法测试发音功能（拼写战斗不需要音频）

### 4. 无图片资源
**现状**: 无场景图片
**影响**: UI较单调，但不影响核心功能

---

## 下一步改进建议

### 优先级 P0（阻塞MVP）

1. **集成拼写战斗到 LearningScreen**
   ```kotlin
   // 在 LearningScreen.kt 的 LearningContent 中
   // 替换当前的 TextField 为 SpellBattleGame
   ```

2. **实现真实星星评分**
   ```kotlin
   // 在 LearningViewModel 中
   fun calculateStars(
       correctRate: Double,
       avgResponseTime: Long,
       hintsUsed: Int
   ): Int {
       // 根据正确率、速度、提示使用计算1-3星
   }
   ```

### 优先级 P1（改进体验）

3. **添加关卡结算动画**
   - 星星逐个显示
   - 分数滚动效果
   - 解锁下一关的提示

4. **添加声音效果**
   - 正确/错误音效
   - 星星获得音效
   - 按钮点击音效

5. **改进虚拟键盘**
   - 添加震动反馈
   - 添加按键音效
   - 支持滑动输入

---

## 测试检查清单

- [ ] 应用可以启动
- [ ] HomeScreen 显示正常
- [ ] 可以导航到 IslandMapScreen
- [ ] 可以导航到 LevelSelectScreen
- [ ] 可以导航到 LearningScreen
- [ ] 可以导航到 ReviewScreen
- [ ] 可以导航到 ProgressScreen
- [ ] 返回按钮在所有界面工作
- [ ] Level 1 有6个词
- [ ] 可以完成一个词
- [ ] 可以完成一个关卡（6个词）
- [ ] 关卡完成后显示结算界面
- [ ] 完成关卡后返回关卡选择，下一关已解锁
- [ ] 虚拟键盘可以输入字母
- [ ] 退格键可以删除字母
- [ ] 错误字母高亮显示
- [ ] 正确答案通过验证

---

## 性能检查

- [ ] 应用启动时间 < 3秒
- [ ] 关卡加载时间 < 1秒
- [ ] 按键响应延迟 < 100ms
- [ ] 内存使用 < 150MB
- [ ] 无卡顿或掉帧

---

## 快速验证命令

```bash
# 构建并安装
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk

# 查看日志
adb logcat | grep -E "Wordland|wordland"

# 清除数据重置
adb shell pm clear com.wordland
```

---

## 成功标准

最小原型成功的标志：

✅ **核心玩法可玩**: 用户可以完成一个完整关卡（6个词）
✅ **导航正常**: 所有界面可以互相导航
✅ **无崩溃**: 完整流程无任何崩溃
✅ **进度保存**: 关卡进度被保存（关闭应用后重新打开）

---

## 总结

当前原型状态：
- **完成度**: ~70%
- **核心功能**: ✅ 已实现
- **UI集成**: ⚠️ 部分完成
- **可玩性**: ⚠️ 需要最后集成

**剩余工作量**: 约2-3小时
- 集成 SpellBattleGame 到 LearningScreen
- 实现星星评分算法
- 端到端测试
- Bug修复
