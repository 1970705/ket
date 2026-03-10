# Release Testing Checklist

**Version**: 2.1
**Last Updated**: 2026-02-22
**Status**: ACTIVE
**Purpose**: 确保发布质量，防止用户影响性bug

---

## 概述

本Checklist定义了发布前必须完成的所有测试项目。**任何关键项目的失败都会阻塞发布**。

### 使用流程

1. **Pre-Merge Checklist**: 代码合并前必须完成
2. **Pre-Release Checklist**: 发布候选构建前必须完成
3. **Final Sign-off**: 发布前的最终批准

---

## Pre-Merge Checklist (代码合并前)

### Phase 1: 代码质量

- [ ] **代码编译成功**
  ```bash
  ./gradlew clean assembleDebug
  ```
  - 无编译错误
  - 无新的警告（或合理解释）

- [ ] **单元测试通过**
  ```bash
  ./gradlew test
  ```
  - 100%测试通过
  - 无测试被忽略

- [ ] **代码覆盖率达标**
  ```bash
  ./gradlew jacocoTestReport
  ```
  - 新代码覆盖率 ≥ 80%
  - 整体覆盖率无下降

- [ ] **静态分析通过**
  ```bash
  ./gradlew detekt ktlintCheck
  ```
  - Detekt无新增严重问题
  - KtLint格式检查通过

- [ ] **代码审查完成**
  - 至少一位审查者批准
  - 所有评论已处理
  - Checklist完成签字

---

### Phase 2: 架构验证

- [ ] **Clean Architecture分层**
  - [ ] UI层不直接访问Data层
  - [ ] Domain层无Android依赖
  - [ ] 依赖方向正确

- [ ] **依赖注入正确**
  - [ ] Service Locator配置正确
  - [ ] 无循环依赖
  - [ ] 初始化调用完整

- [ ] **导航配置正确**
  - [ ] 参数类型定义
  - [ ] 返回导航处理
  - [ ] 无内存泄漏

---

## Pre-Release Checklist (发布前)

### Phase 3: 功能测试

- [ ] **首次启动测试** ⚠️⚠️⚠️ CRITICAL
  ```bash
  ./scripts/test/test_first_launch.sh
  ```

  **验证项**:
  - [ ] 模拟器全新安装
  - [ ] 数据库创建成功
  - [ ] 数据库不为空
  - [ ] Level 1状态为UNLOCKED
  - [ ] 可以点击"开始冒险"
  - [ ] 可以看到岛屿列表
  - [ ] logcat无ERROR/CRASH

  **失败标准**: 任一项失败即阻塞发布

- [ ] **真机测试** ⚠️⚠️⚠️ MANDATORY
  ```bash
  ./scripts/test/test_real_device_clean_install.sh
  ```

  **验证项**:
  - [ ] 真机安装成功
  - [ ] 应用启动无崩溃
  - [ ] 数据库创建和填充成功
  - [ ] Level 1已解锁
  - [ ] 用户可以开始游戏
  - [ ] logcat无ERROR/CRASH

  **失败标准**: 任一项失败即阻塞发布

- [ ] **导航测试**
  ```bash
  ./scripts/test/test_navigation.sh
  ```
  - [ ] 所有屏幕可访问
  - [ ] 返回按钮工作
  - [ ] 参数传递正确
  - [ ] 无导航崩溃

- [ ] **游戏流程测试**
  ```bash
  ./scripts/test/test-gameplay.sh
  ```
  - [ ] 可以完成Level 1
  - [ ] 评分正确
  - [ ] 星级奖励正确
  - [ ] 进度保存

- [ ] **进度持久化测试**
  ```bash
  ./scripts/test/test_progress_save.sh
  ```
  - [ ] 进度在重启后保存
  - [ ] 解锁关卡保持解锁
  - [ ] 星级正确持久化

---

### Phase 4: 数据完整性

- [ ] **数据初始化验证**
  - [ ] `WordlandApplication.onCreate()`调用`initializeAppData()`
  - [ ] `AppDataInitializer.initializeAllData()`完成
  - [ ] 无初始化异常

- [ ] **数据库验证**
  - [ ] 所有表已创建
  - [ ] 所有种子数据存在
  - [ ] 无外键违规
  - [ ] 无数据损坏

- [ ] **Level状态验证**
  - [ ] Level 1是UNLOCKED
  - [ ] 其他Level是LOCKED
  - [ ] 解锁逻辑工作正常

- [ ] **数据迁移测试** (如需要)
  - [ ] 旧版本升级测试
  - [ ] 迁移无数据丢失
  - [ ] 降级策略测试

---

### Phase 5: 性能测试

- [ ] **启动性能**
  - [ ] 冷启动 < 3秒
  - [ ] 热启动 < 1秒
  - [ ] 首屏渲染无延迟

- [ ] **运行时性能**
  - [ ] 帧率稳定 ≥60fps
  - [ ] 列表滚动流畅
  - [ ] 无明显卡顿

- [ ] **内存性能**
  - [ ] 无内存泄漏
  - [ ] 内存使用合理
  - [ ] Bitmap正确管理

- [ ] **数据库性能**
  - [ ] 所有查询 < 100ms
  - [ ] 无UI阻塞
  - [ ] 索引使用正确

---

### Phase 6: 错误处理

- [ ] **网络错误** (如适用)
  - [ ] 优雅的离线模式
  - [ ] 友好的错误消息
  - [ ] 重试机制

- [ ] **数据库错误**
  - [ ] 无数据损坏崩溃
  - [ ] 恢复机制工作
  - [ ] 错误日志记录

- [ ] **边界条件**
  - [ ] 空数据库处理
  - [ ] 损坏缓存处理
  - [ ] 低内存处理
  - [ ] 极端输入处理

---

### Phase 7: 真机测试 (多设备)

- [ ] **设备覆盖**
  - [ ] 至少2台不同设备
  - [ ] 不同Android版本
  - [ ] 不同屏幕尺寸

- [ ] **核心用户流程**
  ```bash
  # 完整流程测试
  Home → Island Map → Level Select → Game → Victory → Next Level
  ```
  - [ ] Home → Island Map
  - [ ] Island Map → Level Select
  - [ ] Level Select → Game
  - [ ] Game → Victory/Defeat
  - [ ] Victory → Next Level
  - [ ] 返回导航工作

- [ ] **日志检查**
  ```bash
  adb logcat -d | grep -i "fatal\|crash\|exception"
  ```
  - 应该返回: (无结果)

---

### Phase 8: 安全与隐私

- [ ] **敏感信息检查**
  - [ ] 无硬编码API密钥
  - [ ] 无硬编码密码
  - [ ] 无调试日志中的敏感数据

- [ ] **权限检查**
  - [ ] 只请求必需权限
  - [ ] 权限已记录
  - [ ] 运行时请求正确

- [ ] **发布构建验证**
  - [ ] `isDebuggable = false`
  - [ ] 无Log.d调试日志
  - [ ] ProGuard/R8启用

---

### Phase 9: 用户界面

- [ ] **Material 3规范**
  - [ ] 使用正确的组件
  - [ ] 颜色符合主题
  - [ ] 排版一致

- [ ] **可访问性**
  - [ ] 内容描述完整
  - [ ] 触摸目标 ≥48dp
  - [ ] 对比度符合WCAG

- [ ] **多语言** (如适用)
  - [ ] 所有字符串已翻译
  - [ ] 无硬编码文本
  - [ ] 布局适应不同语言

---

### Phase 9.5: 视觉QA ⚠️⚠️⚠️ MANDATORY

> **新增原因**: P1-BUG-002 (HintCard文字裁剪) 在真机测试中被发现，但自动化测试未检测到。
> **参考**: `docs/reports/bugfixes/HINT_TEXT_TRUNCATION_BUG.md`
> **详细检查表**: `docs/testing/checklists/VISUAL_QA_CHECKLIST.md`

#### 9.5.1 UI组件显示完整性

- [ ] **每个UI组件在真机上验证**
  - [ ] HintCard 提示文字完整显示
  - [ ] SpellBattleGame 虚拟键盘布局正确
  - [ ] WordlandCard 内容无裁剪
  - [ ] WordlandButton 文字居中显示
  - [ ] LevelProgressBar 进度条完整渲染
  - [ ] 所有Card组件内容无溢出

#### 9.5.2 文字渲染验证

- [ ] **中文文字显示**
  - [ ] 无字符裁剪（特别是HintCard的"提示"文字）
  - [ ] 无字符重叠
  - [ ] 字体清晰可读
  - [ ] 标点符号正确显示

- [ ] **英文文字显示**
  - [ ] 单词完整显示
  - [ ] 无字母被截断
  - [ ] 字体大小合适

- [ ] **数字和符号**
  - [ ] 计数器（如提示次数）正确显示
  - [ ] Emoji图标正确渲染（💡提示、⭐星星等）
  - [ ] 特殊符号无乱码

#### 9.5.3 布局检查

- [ ] **无布局溢出**
  - [ ] 内容不超出容器边界
  - [ ] 长文本正确换行或省略
  - [ ] Column/Row布局不重叠

- [ ] **间距和对齐**
  - [ ] 元素间距一致
  - [ ] 文字垂直居中
  - [ ] 按钮内容居中对齐

- [ ] **触摸目标尺寸**
  - [ ] 所有可点击元素 ≥48dp
  - [ ] 按钮高度足够容纳内容

#### 9.5.4 不同屏幕尺寸适配

- [ ] **手机屏幕**
  - [ ] 小屏设备（≤5.5英寸）布局正确
  - [ ] 标准屏（5.5-6.5英寸）无问题
  - [ ] 大屏设备（≥6.5英寸）显示正常

- [ ] **横屏模式**
  - [ ] 横屏布局不溢出
  - [ ] 文字不被裁剪
  - [ ] 触摸目标仍然可点击

- [ ] **不同DPI**
  - [ ] 低DPI设备（ldpi）文字可读
  - [ ] 高DPI设备（xxxhdpi）无裁剪

#### 9.5.5 真机vs模拟器差异

- [ ] **真机必测项**
  - [ ] 至少在1台真机上测试所有UI
  - [ ] 检查厂商自定义皮肤（如MIUI）的渲染差异
  - [ ] 验证系统字体大小设置的影响

- [ ] **差异记录**
  - [ ] 记录真机与模拟器的视觉差异
  - [ ] 拍照记录任何显示问题
  - [ ] 报告设备特定的渲染问题

#### 9.5.6 视觉QA验收标准

**阻塞条件**（任一即阻塞发布）:
- ❌ 文字被裁剪导致不可读
- ❌ UI元素超出屏幕边界
- ❌ 按钮内容无法辨认
- ❌ 中文显示乱码

**警告条件**:
- ⚠️ 间距轻微不一致（可延后修复）
- ⚠️ 某设备上的微小对齐偏差

**测试方法**:
```bash
# 1. 安装到真机
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. 截图对比
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png

# 3. 逐个界面检查
# - HomeScreen
# - IslandMapScreen
# - LevelSelectScreen
# - LearningScreen (重点检查HintCard)
# - ReviewScreen
# - ProgressScreen
```

**详细检查表**: 参见 `VISUAL_QA_CHECKLIST.md`

---

### Phase 10.5: 视觉QA完成确认

> **重要性**: ⚠️⚠️⚠️ MANDATORY

- [ ] **视觉QA检查表完成**
  - [ ] VISUAL_QA_CHECKLIST.md 所有项目已检查
  - [ ] 至少2台不同设备已验证
  - [ ] 所有阻塞问题已修复
  - [ ] 截图记录已保存

- [ ] **重点组件确认**
  - [ ] HintCard 文字无裁剪
  - [ ] 所有Card内容无溢出
  - [ ] 虚拟键盘布局正确
  - [ ] 中文显示完整

**检查表位置**: `docs/testing/checklists/VISUAL_QA_CHECKLIST.md`

---

### Phase 11: 发布准备

- [ ] **版本信息**
  - [ ] `versionName`更新
  - [ ] `versionCode`增加
  - [ ] 版本信息一致

- [ ] **变更日志**
  ```markdown
  ## Version X.X.X (YYYY-MM-DD)

  ### 新功能
  - 功能1
  - 功能2

  ### Bug修复
  - 修复1
  - 修复2

  ### 已知问题
  - 问题1 (跟踪链接)
  ```

- [ ] **文档更新**
  - [ ] CLAUDE.md更新 (如架构变更)
  - [ ] README.md更新 (如需要)
  - [ ] ADR创建 (如重大决策)
  - [ ] Bug RCA (如修复bug)

- [ ] **发布构建**
  ```bash
  ./gradlew assembleRelease
  ```
  - [ ] Release构建成功
  - [ ] 签名正确
  - [ ] APK完整性验证

- [ ] **最终测试**
  - [ ] 清洁安装测试
  - [ ] 升级测试 (从上一版本)
  - [ ] 全流程测试
  - [ ] 最终签批

---

## 发布阻塞条件

### Critical Blockers (任一即阻塞)

- ❌ 首次启动测试失败
- ❌ 真机测试失败
- ❌ 数据库首次启动为空
- ❌ Level 1状态为LOCKED
- ❌ 应用启动崩溃
- ❌ 核心用户流程中断

### High Blockers

- ❌ 进度不保存
- ❌ 导航崩溃
- ❌ 数据丢失
- ❌ 性能不可接受

### Medium Blockers

- ❌ 多个logcat崩溃
- ❌ 错误处理不当
- ❌ 文档缺失

**规则**: 即使一个Critical Blocker也 = 不能发布

---

## 测试结果总结

| Phase | 状态 | 测试者 | 日期 | 备注 |
|-------|------|--------|------|------|
| 代码质量 | ⬜ Pass | | | |
| 架构验证 | ⬜ Pass | | | |
| 首次启动 | ⬜ Pass | | | CRITICAL |
| 真机测试 | ⬜ Pass | | | MANDATORY |
| 功能测试 | ⬜ Pass | | | |
| 数据完整性 | ⬜ Pass | | | |
| 性能测试 | ⬜ Pass | | | |
| 错误处理 | ⬜ Pass | | | |
| 安全验证 | ⬜ Pass | | | |
| 用户界面 | ⬜ Pass | | | |
| 视觉QA | ⬜ Pass | | | MANDATORY |
| 发布准备 | ⬜ Pass | | | |

**Overall Status**: ⬜ READY FOR RELEASE

---

## 签名

### 测试工程师

- [ ] 所有测试已完成
- [ ] 所有结果已记录
- [ ] 无Critical Blockers

**签名**: ________________  **日期**: ________  **时间**: ________

### 架构师

- [ ] 架构验证通过
- [ ] 无技术债务引入
- [ ] 性能符合标准

**签名**: ________________  **日期**: ________  **时间**: ________

### 发布经理

- [ ] 发布准备完整
- [ ] 变更日志确认
- [ ] 最终批准

**签名**: ________________  **日期**: ________  **时间**: ________

---

## 常见问题

### Q1: 首次启动测试失败

**症状**: 数据库为空

**解决方案**:
1. 检查 `WordlandApplication.onCreate()`
2. 验证 `initializeAppData()` 被调用
3. 检查logcat中的异常
4. 修复后重新测试

### Q2: Level 1是LOCKED

**症状**: 第一关无法进入

**解决方案**:
1. 检查 `AppDataInitializer.unlockFirstLevels()`
2. 验证状态设置为 `UNLOCKED`
3. 重新构建测试

### Q3: 真机安装失败

**症状**: `INSTALL_FAILED_USER_RESTRICTED`

**解决方案**:
1. 手动从文件管理器安装
2. 检查设备存储
3. 先卸载再安装

### Q4: 模拟器通过但真机失败

**症状**: 真机崩溃

**解决方案**:
1. 检查logcat获取详情
2. 验证DI配置
3. 检查设备特定问题
4. 审查Hilt/Service Locator

---

## 参考文档

- [首次启动Bug RCA](../../reports/issues/FIRST_LAUNCH_BUG_ROOT_CAUSE_ANALYSIS.md)
- [开发规范](../../development/DEVELOPMENT_STANDARDS.md)
- [设备测试指南](../guides/testing/DEVICE_TESTING_GUIDE.md)
- [崩溃诊断指南](../guides/troubleshooting/CRASH_LOG_GUIDE.md)
- [Code Review Checklist](../guides/CODE_REVIEW_CHECKLIST.md)

---

## 版本历史

| 版本 | 日期 | 变更 |
|------|------|------|
| 1.0 | 2026-02-16 | 初始版本 |
| 2.0 | 2026-02-18 | 添加架构验证、性能测试、签名流程 |
| 2.1 | 2026-02-22 | 添加Phase 9.5视觉QA检查（响应P1-BUG-002） |

---

**Document Version**: 2.1
**Last Updated**: 2026-02-22
**Next Review**: 2026-03-22
**Maintainer**: Android Architect Team
