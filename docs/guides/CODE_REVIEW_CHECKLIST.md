# Code Review Checklist

**版本**: 2.0
**日期**: 2026-02-18
**目的**: 确保代码质量，预防常见bug，维护架构完整性

**使用方法**: 每个Pull Request必须通过此Checklist才能合并。

---

## 目录

1. [快速检查](#快速检查)
2. [架构验证](#架构验证)
3. [逻辑验证](#逻辑验证)
4. [数据验证](#数据验证)
5. [测试验证](#测试验证)
6. [UI/UX验证](#uiux验证)
7. [性能验证](#性能验证)
8. [安全验证](#安全验证)
9. [文档验证](#文档验证)
10. [发布前验证](#发布前验证)

---

## 快速检查

### 必须通过（阻塞合并）

- [ ] **所有测试通过** - `./gradlew test` 成功，无失败用例
- [ ] **代码编译无警告** - 无新的编译警告
- [ ] **静态分析通过** - Detekt和KtLint检查通过
- [ ] **无硬编码数据** - Screen使用ViewModel而非假数据
- [ ] **首次启动测试通过** - 在空数据库场景下测试
- [ ] **真机测试通过** - 在真实设备上验证（如可用）

### CI/CD检查

- [ ] **CI管道通过** - GitHub Actions所有检查通过
- [ ] **代码覆盖率无下降** - 新代码覆盖率≥80%
- [ ] **无新增Detekt问题** - 或有合理说明

---

## 架构验证

### Clean Architecture分层

- [ ] **UI层（Screen/ViewModel）**
  - [ ] 不包含业务逻辑（委托给UseCase）
  - [ ] 只管理UI状态（StateFlow）
  - [ ] 通过UseCase与Domain层交互
  - [ ] 不直接访问Data层

- [ ] **Domain层（UseCase/Model）**
  - [ ] 不依赖Android Framework
  - [ ] 不依赖Data层实现
  - [ ] 业务逻辑在UseCase中
  - [ ] Model是纯Kotlin数据类

- [ ] **Data层（Repository/DAO）**
  - [ ] 只负责数据持久化
  - [ ] 不包含业务逻辑
  - [ ] 通过接口与Domain交互

### 依赖注入（DI）

**为什么重要**: Service Locator重构时遗漏了初始化调用，导致100%用户无法使用（Bug #1）

- [ ] **如果修改了DI系统**
  - [ ] 列出了原框架的所有职责
  - [ ] 每项职责都手动实现了
  - [ ] 验证清单完成并签字

- [ ] **如果修改了Application类**
  - [ ] `onCreate()`中的初始化调用保持
  - [ ] Service Locator正确初始化
  - [ ] 数据库初始化在`onCreate()`中调用

- [ ] **依赖连接验证**
  - [ ] ViewModel通过ServiceLocator获取所有依赖
  - [ ] 无循环依赖
  - [ ] UseCase依赖正确注入
  - [ ] Repository依赖正确注入

### 导航验证

- [ ] **导航参数**
  - [ ] 使用NavType定义参数类型
  - [ ] 必需参数有默认值或验证
  - [ ] 复杂参数序列化/反序列化正确

- [ ] **导航流程**
  - [ ] 返回导航正确处理
  - [ ] 深层链接支持（如需要）
  - [ ] 无内存泄漏（正确保存/恢复状态）

---

## 逻辑验证

### 函数命名与实现一致性

**为什么重要**: `unlockFirstLevels()` 设置了 `LOCKED` 状态，导致第一关无法进入（Bug #2）

- [ ] **命名检查**
  - [ ] `unlockXXX()` 必须产生 `unlocked` 状态
  - [ ] `initializeXXX()` 必须执行初始化
  - [ ] `loadXXX()` 必须加载数据
  - [ ] `saveXXX()` 必须保存数据
  - [ ] 函数名与实现一致

- [ ] **反模式检查**
  - [ ] ❌ `fun unlockLevels() { status = LOCKED }`
  - [ ] ❌ `fun loadData() { /* 不返回数据 */ }`
  - [ ] ❌ `fun isValid() { /* 总是返回true */ }`
  - [ ] ✅ `fun unlockLevels() { status = UNLOCKED }`

### 业务逻辑验证

- [ ] **关键逻辑有单元测试**
  - [ ] UseCase有测试
  - [ ] 算法有测试
  - [ ] 初始化逻辑有测试

- [ ] **边界条件测试**
  - [ ] 空列表处理
  - [ ] null值处理
  - [ ] 最大/最小值
  - [ ] 零除保护

- [ ] **异常处理**
  - [ ] 网络错误处理
  - [ ] 数据库错误处理
  - [ ] 用户输入验证
  - [ ] 错误状态暴露给UI

### 状态管理

- [ ] **StateFlow使用**
  - [ ] 使用 `MutableStateFlow` 内部，`StateFlow` 对外
  - [ ] 初始状态正确
  - [ ] 状态更新在主线程
  - [ ] 无状态竞态条件

- [ ] **副作用处理**
  - [ ] 使用 `LaunchedEffect` 处理一次性事件
  - [ ] 使用 `SideEffect` 处理UI副作用
  - [ ] 无重复执行

---

## 数据验证

### 硬编码数据检查

**为什么重要**: LevelSelectScreen使用假数据，导致关卡无法解锁（Bug #6）

- [ ] **Screen数据来源**
  - [ ] 不使用 `val fakeData = listOf(...)`
  - [ ] 使用ViewModel获取数据
  - [ ] 数据从数据库读取
  - [ ] 无 `TODO: Replace with real data`

- [ ] **字符串检查**
  - [ ] 显示文本来自资源文件（strings.xml）
  - [ ] 不硬编码中文字符串
  - [ ] 不硬编码英文字符串（用户可见）

### 数据初始化

- [ ] **首次启动**
  - [ ] 数据库在首次启动时初始化
  - [ ] 第一关状态为 `UNLOCKED`
  - [ ] 必要数据预填充

- [ ] **数据迁移**
  - [ ] 如果数据库schema变更，有迁移策略
  - [ ] 迁移测试通过
  - [ ] 降级策略（如需要）

### Room数据库

- [ ] **DAO定义**
  - [ ] 使用 `@Query`, `@Insert`, `@Update`, `@Delete`
  - [ ] 查询参数正确（使用 `:param` 语法）
  - [ ] 返回类型正确（Flow/List/Single）

- [ ] **Entity定义**
  - [ ] 主键正确定义
  - [ ] 索引合理添加
  - [ ] 关系正确定义（一对一、一对多）

- [ ] **类型转换**
  - [ ] 自定义类型有Converter
  - [ ] Converter已注册
  - [ ] 测试覆盖转换逻辑

---

## 测试验证

### 单元测试

- [ ] **新代码有测试**
  - [ ] UseCase有单元测试
  - [ ] ViewModel有单元测试
  - [ ] 复杂逻辑有单元测试

- [ ] **测试质量**
  - [ ] 测试命名清晰（Given-When-Then）
  - [ ] 使用Mock隔离依赖
  - [ ] 测试覆盖正常和异常情况
  - [ ] 无 `@Ignore` 测试

- [ ] **测试结构**
  ```kotlin
  @Test
  fun `given valid input, when use case executes, then returns success`() {
      // Given
      val input = ...

      // When
      val result = useCase(input)

      // Then
      assertThat(result).isInstanceOf(Result.Success::class.java)
  }
  ```

### 集成测试

- [ ] **首次启动测试** (CRITICAL)
  ```bash
  ./scripts/test/test_first_launch.sh
  ```
  - [ ] 全新安装场景
  - [ ] 数据库创建成功
  - [ ] 数据填充成功
  - [ ] 第一关已解锁

- [ ] **真机测试** (CRITICAL)
  ```bash
  ./scripts/test/test_real_device_clean_install.sh
  ```
  - [ ] 在真实设备上测试
  - [ ] 首次启动无崩溃
  - [ ] 核心功能可用

- [ ] **导航测试**
  ```bash
  ./scripts/test/test_navigation.sh
  ```
  - [ ] 导航流程正常
  - [ ] 参数传递正确
  - [ ] 无崩溃

### 测试覆盖率

- [ ] **新代码覆盖率 ≥ 80%**
  - [ ] UseCase层 ≥ 90%
  - [ ] ViewModel层 ≥ 80%
  - [ ] Repository层 ≥ 80%
  - [ ] Data层 ≥ 75%

### UI测试（可选）

- [ ] **Compose测试**
  - [ ] 关键UI组件有测试
  - [ ] 使用ComposeTestRule
  - [ ] 测试用户交互

---

## UI/UX验证

### Compose最佳实践

- [ ] **组件设计**
  - [ ] 遵循单一职责原则
  - [ ] 可复用组件提取
  - [ ] 使用 `@Composable` 注解

- [ ] **状态管理**
  - [ ] 使用 `remember` 管理本地状态
  - [ ] 使用 `rememberSaveable` 保存跨重组状态
  - [ ] 使用 `derivedStateOf` 计算派生状态
  - [ ] 避免不必要的重组

- [ ] **性能优化**
  - [ ] 使用 `key()` 稳定列表项
  - [ ] 避免在Composable中创建对象
  - [ ] 使用 `LaunchedEffect` 替代 `SideEffect`

### Material 3规范

- [ ] **组件使用**
  - [ ] 使用Material 3组件
  - [ ] 遵循主题系统
  - [ ] 颜色使用定义的颜色方案

- [ ] **可访问性**
  - [ ] 内容描述添加
  - [ ] 最小触摸目标（48dp）
  - [ ] 对比度符合标准

### 响应式设计

- [ ] **屏幕适配**
  - [ ] 支持不同屏幕尺寸
  - [ ] 横竖屏支持（如需要）
  - [ ] 折叠屏适配（如需要）

---

## 性能验证

### 编译性能

- [ ] **编译时间**
  - [ ] 无显著编译时间增加
  - [ ] Kotlin增量编译工作
  - [ ] 无不必要的依赖

### 运行时性能

- [ ] **内存使用**
  - [ ] 无明显内存泄漏
  - [ ] 对象及时释放
  - [ ] Bitmap正确管理

- [ ] **UI性能**
  - [ ] 帧率稳定（≥60fps）
  - [ ] 无掉帧/卡顿
  - [ ] 列表滚动流畅

- [ ] **数据库性能**
  - [ ] 查询优化（使用索引）
  - [ ] 无N+1查询问题
  - [ ] 使用Flow而非轮询

### Baseline Profiles（可选）

- [ ] **Baseline Profile**
  - [ ] 关键用户路径优化
  - [ ] 启动时间改进
  - [ ] 滚动性能改进

---

## 安全验证

### 数据安全

- [ ] **敏感信息**
  - [ ] 无API密钥硬编码
  - [ ] 无密码明文存储
  - [ ] 用户数据加密（如需要）

- [ ] **日志安全**
  - [ ] 无敏感信息记录到logcat
  - [ ] release构建无调试日志

### 输入验证

- [ ] **用户输入**
  - [ ] 验证用户输入
  - [ ] 防止SQL注入
  - [ ] 防止XSS攻击（WebView）

### 权限

- [ ] **权限使用**
  - [ ] 只请求必需权限
  - [ ] 权限有说明
  - [ ] 运行时请求正确处理

---

## 文档验证

### 代码注释

- [ ] **复杂逻辑有注释**
  - [ ] 算法逻辑解释
  - [ ] 业务规则说明
  - [ ] 为什么要这样做

- [ ] **公共API有文档**
  - [ ] 函数用途说明（KDoc）
  - [ ] 参数说明
  - [ ] 返回值说明
  - [ ] 异常说明

### 架构决策

- [ ] **重大决策有ADR**
  - [ ] ADR编号分配
  - [ ] 问题背景描述
  - [ ] 替代方案分析
  - [ ] 影响评估
  - [ ] 更新ADR索引

### README更新

- [ ] **如果有新功能**
  - [ ] 更新README.md
  - [ ] 添加使用示例
  - [ ] 更新架构图（如需要）

---

## 发布前验证

### 完整测试流程

```bash
# 1. 单元测试
./gradlew test

# 2. 首次启动测试
./scripts/test/test_first_launch.sh

# 3. 真机测试（如果可用）
./scripts/test/test_real_device_clean_install.sh

# 4. 导航测试
./scripts/test/test_navigation.sh

# 5. 静态分析
./gradlew detekt ktlintCheck

# 6. 覆盖率报告
./gradlew jacocoTestReport
```

### 发布阻塞条件

**任一失败即不能发布**:
- ❌ 单元测试失败
- ❌ 首次启动测试失败
- ❌ 真机测试失败
- ❌ 导航测试失败
- ❌ 静态分析失败（无合理说明）
- ❌ 硬编码数据
- ❌ 第一关状态为LOCKED
- ❌ 架构分层违规
- ❌ 依赖注入问题

### 发布候选检查

- [ ] **版本号更新**
  - [ ] versionName更新
  - [ ] versionCode增加

- [ ] **变更日志**
  - [ ] 新功能列表
  - [ ] Bug修复列表
  - [ ] 已知问题

- [ ] **签名构建**
  - [ ] release构建成功
  - [ ] 签名正确
  - [ ] APK完整性验证

---

## Reviewer签名

### 作者自检

- [ ] 我已经完成了以上所有检查项
- [ ] 代码已经自我审查
- [ ] 所有测试通过
- [ ] 准备提交Review

**作者签名**: ________________  **日期**: ____________

### Code Reviewer检查

- [ ] 架构验证通过
- [ ] 逻辑验证通过
- [ ] 数据验证通过
- [ ] 测试验证通过
- [ ] UI/UX验证通过
- [ ] 性能验证通过
- [ ] 安全验证通过
- [ ] 文档验证通过

**Reviewer签名**: ________________  **日期**: ____________

### 最终批准

- [ ] 所有检查项通过
- [ ] 可以合并到主分支

**架构师签名**: ________________  **日期**: ____________

---

## 版本历史

| 版本 | 日期 | 变更 | 作者 |
|------|------|------|------|
| 1.0 | 2026-02-16 | 初始版本，基于7个bug分析 | Android Architect |
| 2.0 | 2026-02-18 | 全面扩展，添加UI/性能/安全验证 | Android Architect |

---

## 参考文档

- [完整Bug分析](../reports/issues/COMPLETE_BUG_ANALYSIS_AND_IMPROVEMENT_PLAN.md)
- [测试策略](../testing/strategy/TEST_STRATEGY.md)
- [发布清单](../testing/checklists/RELEASE_TESTING_CHECKLIST.md)
- [ADR流程](../adr/README.md)
- [开发规范](../development/DEVELOPMENT_STANDARDS.md)

---

**最后更新**: 2026-02-18
**维护者**: Android Architect Team
**下次审查**: 2026-03-18
