# Visual QA Checklist

**Version**: 1.0
**Last Updated**: 2026-02-22
**Status**: ACTIVE
**Purpose**: 确保UI在所有设备上的视觉呈现正确，防止类似P1-BUG-002的文字裁剪问题

---

## 概述

本检查表用于在真机测试中验证UI组件的视觉呈现完整性。它补充了功能测试，专注于视觉质量问题。

**触发条件**: 每次发布前、UI组件变更后、新设备支持时

**预计耗时**: 每台设备 15-20 分钟

---

## 一、测试前准备

### 1.1 设备准备

| 设备类型 | 最低要求 | 推荐设备 |
|---------|---------|---------|
| 主测试设备 | Android 7.0+ | 当前主力手机 |
| 小屏设备 | ≤5.5英寸 | iPhone SE尺寸的Android |
| 大屏设备 | ≥6.5英寸 | 折叠屏/平板 |
| 不同厂商 | 至少2家 | Samsung + Xiaomi/其他 |

### 1.2 测试工具准备

```bash
# 截图工具
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png

# 录屏工具（可选）
adb shell screenrecord /sdcard/demo.mp4
adb pull /sdcard/demo.mp4
```

### 1.3 参考设备记录

| 设备 | 型号 | Android版本 | 分辨率 | DPI |
|------|------|-------------|--------|-----|
| 设备1 | | | | |
| 设备2 | | | | |

---

## 二、通用UI检查

### 2.1 文字渲染

#### 中文文字显示

- [ ] **无裁剪检查**
  - [ ] 所有汉字完整显示
  - [ ] 字符上下不被截断
  - [ ] 笔画清晰可辨

- [ ] **字体质量**
  - [ ] 无锯齿或模糊
  - [ ] 字重一致
  - [ ] 行距适中

- [ ] **特殊字符**
  - [ ] 标点符号正确（，。！？）
  - [ ] 括号配对正确
  - [ ] 引号方向正确

#### 英文文字显示

- [ ] **字母显示**
  - [ ] 大写字母完整（A-Z）
  - [ ] 小写字母完整（a-z）
  - [ ] 上下延伸字母完整（g, j, p, q, y）

- [ ] **单词显示**
  - [ ] 长单词正确换行
  - [ ] 连字符正确（如需要）
  - [ ] 无字母被截断

#### 数字和符号

- [ ] **数字显示**
  - [ ] 0-9 所有数字完整
  - [ ] 计数器正确显示（如"剩余提示(3)"）
  - [ ] 百分比正确显示

- [ ] **Emoji显示**
  - [ ] 💡 提示图标完整
  - [ ] ⭐ 星星图标完整
  - [ ] 其他应用emoji无乱码

### 2.2 布局完整性

- [ ] **边界检查**
  - [ ] 内容不超出屏幕边界
  - [ ] 内容不被状态栏遮挡
  - [ ] 内容不被导航栏遮挡
  - [ ] 内容不被系统手势条遮挡

- [ ] **容器检查**
  - [ ] Card内容不溢出
  - [ ] Button文字完整在按钮内
  - [ ] Column内元素不重叠
  - [ ] Row内元素不重叠

- [ ] **间距检查**
  - [ ] 元素间距一致
  - [ ] Padding值合理
  - [ ] Margin值合理

---

## 三、组件级检查

### 3.1 HintCard（重点）

> **历史问题**: P1-BUG-002 - "提示"文字在真机被裁剪

**文件位置**: `app/src/main/java/com/wordland/ui/components/HintCard.kt`

- [ ] **HintActionButton显示**
  - [ ] Emoji 💡 完整显示
  - [ ] "提示"二字完整显示（不被底部裁剪）
  - [ ] 计数器 "(3)" 完整显示
  - [ ] 按钮高度足够（≥64dp）
  - [ ] 三元素垂直居中对齐

- [ ] **HintTextCard显示**
  - [ ] 提示内容文字完整
  - [ ] 长提示正确换行
  - [ ] Card内容不溢出

- [ ] **不同状态**
  - [ ] 初始状态（有提示可用）
  - [ ] 已使用状态（无提示可用）
  - [ ] 已显示状态（提示已展示）

**测试方法**:
1. 进入 LearningScreen
2. 观察提示按钮
3. 截图记录
4. 使用提示后再次观察

### 3.2 SpellBattleGame

**文件位置**: `app/src/main/java/com/wordland/ui/components/SpellBattleGame.kt`

- [ ] **虚拟键盘**
  - [ ] 所有26个字母按键完整显示
  - [ ] 退格键 ⌫ 完整显示
  - [ ] 按键文字居中
  - [ ] 按键尺寸一致

- [ ] **答案框**
  - [ ] 所有字母框完整显示
  - [ ] 输入的字母不超出框边界
  - [ ] 框之间间距一致

- [ ] **中文翻译**
  - [ ] 中文翻译完整显示
  - [ ] 长翻译正确换行
  - [ ] 不与答案框重叠

### 3.3 WordlandCard

**文件位置**: `app/src/main/java/com/wordland/ui/components/WordlandCard.kt`

- [ ] **Card内容**
  - [ ] 标题完整显示
  - [ ] 内容不溢出Card边界
  - [ ] 长文本正确截断或换行
  - [ ] 圆角完整渲染

### 3.4 WordlandButton

**文件位置**: `app/src/main/java/com/wordland/ui/components/WordlandButton.kt`

- [ ] **Button内容**
  - [ ] 按钮文字完整在按钮内
  - [ ] 文字垂直居中
  - [ ] 文字水平居中
  - [ ] 按钮高度≥48dp（触摸目标）

### 3.5 LevelProgressBar

**文件位置**: `app/src/main/java/com/wordland/ui/components/LevelProgressBar.kt`

- [ ] **进度条**
  - [ ] 进度条完整渲染
  - [ ] 进度文字不被遮挡
  - [ ] 动画流畅

### 3.6 ComboIndicator

- [ ] **连击显示**
  - [ ] 连击数字完整显示
  - [ ] 动画效果完整
  - [ ] 不遮挡其他元素

---

## 四、屏幕级检查

### 4.1 HomeScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/HomeScreen.kt`

- [ ] **标题和Logo**
  - [ ] 应用标题完整显示
  - [ ] Logo不模糊

- [ ] **菜单按钮**
  - [ ] 所有按钮完整显示
  - [ ] 按钮文字不被裁剪
  - [ ] 按钮垂直排列整齐

- [ ] **底部导航**
  - [ ] 导航项完整显示
  - [ ] 选中状态清晰
  - [ ] 不被手势条遮挡

### 4.2 IslandMapScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/IslandMapScreen.kt`

- [ ] **岛屿卡片**
  - [ ] 岛屿名称完整显示
  - [ ] 精通度百分比显示
  - [ ] 卡片不溢出屏幕

- [ ] **返回按钮**
  - [ ] 顶部返回按钮完整显示
  - [ ] 不被状态栏遮挡

### 4.3 LevelSelectScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/LevelSelectScreen.kt`

- [ ] **关卡列表**
  - [ ] 关卡名称完整显示
  - [ ] 星级评分图标完整
  - [ ] 锁定/解锁图标完整
  - [ ] 列表可滚动（如有多个关卡）

### 4.4 LearningScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/LearningScreen.kt`

- [ ] **顶部栏**
  - [ ] 关卡名称完整显示
  - [ ] 退出按钮完整显示
  - [ ] 进度条完整显示

- [ ] **游戏区域**
  - [ ] 中文翻译完整显示
  - [ ] 答案框完整显示
  - [ ] 虚拟键盘完整显示
  - [ ] 提示卡片完整显示

- [ ] **底部按钮**
  - [ ] 提交按钮完整显示
  - [ ] 按钮文字不被裁剪
  - [ ] 不被手势条遮挡

### 4.5 ReviewScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/ReviewScreen.kt`

- [ ] **复习列表**
  - [ ] 词汇项完整显示
  - [ ] 列表正确滚动
  - [ ] 空状态提示完整显示

### 4.6 ProgressScreen

**文件位置**: `app/src/main/java/com/wordland/ui/screens/ProgressScreen.kt`

- [ ] **统计数据**
  - [ ] 数字完整显示
  - [ ] 百分比完整显示
  - [ ] 图表不溢出

---

## 五、特殊场景检查

### 5.1 横屏模式

- [ ] **切换到横屏后检查**
  - [ ] 布局自动适应
  - [ ] 文字不被裁剪
  - [ ] 按钮仍然可点击
  - [ ] 内容不超出边界

**测试方法**:
```bash
# 强制横屏
adb shell settings put system user_rotation 1

# 恢复竖屏
adb shell settings put system user_rotation 0
```

### 5.2 不同系统字体大小

- [ ] **小字体**
  - [ ] 文字仍然可读
  - [ ] 布局不崩溃

- [ ] **大字体**
  - [ ] 文字不被裁剪
  - [ ] 布局适应变化
  - [ ] 长文本正确换行

**测试方法**:
```
设置 → 显示 → 字体大小 → 调整到最大
```

### 5.3 不同显示尺寸

- [ ] **小显示尺寸**
  - [ ] UI自动缩小
  - [ ] 文字仍然清晰

- [ ] **大显示尺寸**
  - [ ] UI不拉伸变形
  - [ ] 文字不过大

### 5.4 深色模式

- [ ] **切换到深色模式**
  - [ ] 所有文字可见
  - [ ] 对比度足够
  - [ ] 无颜色冲突

**测试方法**:
```bash
# 切换深色模式
adb shell cmd uimode night yes
```

---

## 六、截图记录模板

### 6.1 截图命名规范

```
YYYY-MM-DD_DeviceName_ScreenName_Status.png

例如:
2026-02-22_Xiaomi_HomeScreen_Normal.png
2026-02-22_Xiaomi_HintCard_TextTruncated_BUG.png
2026-02-22_Pixel5_LearningScreen_DarkMode.png
```

### 6.2 问题记录表

| 截图文件 | 组件 | 问题描述 | 严重性 | 状态 |
|---------|------|---------|--------|------|
| | HintCard | "提示"文字底部裁剪 | P1 | 已修复 |
| | | | | |
| | | | | |

---

## 七、验收标准

### 7.1 阻塞条件（任一即不通过）

- ❌ 文字被裁剪导致不可读
- ❌ UI元素超出屏幕边界
- ❌ 按钮内容无法辨认
- ❌ 中文显示乱码
- ❌ 触摸目标被遮挡无法点击
- ❌ 关键功能图标不显示

### 7.2 警告条件（可延后修复）

- ⚠️ 间距轻微不一致（不影响使用）
- ⚠️ 某特定设备上的对齐偏差
- ⚠️ 非关键组件的微小显示问题

### 7.3 通过标准

- ✅ 所有主要UI组件显示完整
- ✅ 所有文字清晰可读
- ✅ 无布局溢出或裁剪
- ✅ 触摸目标可正常点击
- ✅ 至少2台不同设备验证通过

---

## 八、问题报告流程

### 8.1 发现问题

1. **截图记录**
   ```bash
   adb shell screencap -p /sdcard/issue.png
   adb pull /sdcard/issue.png
   ```

2. **记录设备信息**
   - 设备型号
   - Android版本
   - 屏幕分辨率
   - 系统字体大小设置

3. **创建Bug报告**
   - 参考模板: `docs/reports/bugfixes/BUG_REPORT_TEMPLATE.md`
   - 包含截图
   - 描述复现步骤

### 8.2 优先级分类

| 优先级 | 描述 | 示例 | 修复时限 |
|-------|------|------|---------|
| P0 | 阻塞使用 | 文字完全不可读 | 立即 |
| P1 | 严重影响 | 文字部分裁剪 | 本周 |
| P2 | 轻微影响 | 间距不一致 | 下版本 |

---

## 九、自动化测试补充

虽然本检查表专注于手动视觉QA，但以下自动化测试可以预防部分问题：

### 9.1 Compose UI测试

```kotlin
// 测试组件文字是否可读
@Test
fun testHintCard_textIsDisplayed() {
    composeTestRule.setContent {
        HintCard(hintsRemaining = 3)
    }

    // 验证文字存在
    composeTestRule.onNodeWithText("提示")
        .assertIsDisplayed()
}
```

### 9.2 截图测试（推荐工具）

- **Paparazzi**: 仿真截图测试
- **Shot**: 真机截图测试
- **Dropshots**: CI截图对比

### 9.3 布局验证

```kotlin
// 验证组件高度足够
@Test
fun testHintButton_heightIsSufficient() {
    val minHeight = 64.dp
    // 验证按钮高度 ≥ 64.dp
}
```

---

## 十、完成检查表

### 测试人员信息

**测试人员**: _____________________

**测试日期**: _____________________

**设备列表**:
1. _____________________
2. _____________________

### 测试结果

| 组件 | 设备1 | 设备2 | 备注 |
|------|-------|-------|------|
| HintCard | ✅/❌ | ✅/❌ | |
| SpellBattleGame | ✅/❌ | ✅/❌ | |
| WordlandCard | ✅/❌ | ✅/❌ | |
| WordlandButton | ✅/❌ | ✅/❌ | |
| HomeScreen | ✅/❌ | ✅/❌ | |
| LearningScreen | ✅/❌ | ✅/❌ | |

### 总体评估

**Visual QA状态**: ⬜ 通过 / ⬜ 有警告 / ⬜ 不通过

**阻塞问题数量**: _______

**警告问题数量**: _______

**测试人员签名**: _____________________

**日期**: _____________________

---

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0 | 2026-02-22 | 初始版本，响应P1-BUG-002 |

---

## 参考文档

- Bug报告: `docs/reports/bugfixes/HINT_TEXT_TRUNCATION_BUG.md`
- 设备测试指南: `docs/guides/testing/DEVICE_TESTING_GUIDE.md`
- 发布检查表: `docs/testing/checklists/RELEASE_TESTING_CHECKLIST.md`
