# ADR 001: 使用 Service Locator 替代 Hilt Activity 注入

## 状态
已采纳

## 日期
2026-02-16

## 上下文

### 问题
Wordland 应用在使用 Hilt 依赖注入框架时遇到真机兼容性问题：

1. **第一次崩溃**：应用启动失败
   - 错误：`ClassNotFoundException: WordlandApplication`
   - 原因：Hilt 代码生成在真机上失败

2. **第二次崩溃**：导航到岛屿地图失败
   - 错误：`MainActivity does not implement dagger.hilt.internal.GeneratedComponent`
   - 原因：移除 `@AndroidEntryPoint` 后，`hiltViewModel()` 无法工作

### 技术背景
- **Hilt 依赖链**：
  ```
  @HiltAndroidApp (Application)
      ↓ 必需
  @AndroidEntryPoint (Activity)
      ↓ 必需
  hiltViewModel<VM>() (Screen)
  ```

- **问题**：这是一个全有或全无的设计，移除任何一环都会崩溃

### 约束条件
- 应用必须在真机上运行
- 真机和模拟器行为必须一致
- 不能引入新的复杂度
- 需要保持代码可维护性

---

## 决策

### 选择方案
**使用 Service Locator 模式替代 Hilt 的 Activity 级注入**

### 具体实现

#### 1. 创建 Service Locator
```kotlin
// di/AppServiceLocator.kt
object AppServiceLocator {
    private val database by lazy { WordDatabase.getInstance(instance) }
    private val repositories by lazy { /* ... */ }
    private val useCases by lazy { /* ... */ }

    fun provideFactory(): ViewModelProvider.Factory = ...
}
```

#### 2. 更新 ViewModel 获取方式
```kotlin
// 之前
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
)

// 之后
@Composable
fun MyScreen(
    viewModel: MyViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    )
)
```

#### 3. 保留兼容性
- ViewModels 仍然使用 `@HiltViewModel` 注解
- UseCases 和 Repositories 仍然使用 `@Inject constructor`
- 只改变了 ViewModel 的获取方式

---

## 后果

### 正面影响

1. **真机兼容性**
   - ✅ 真机和模拟器行为一致
   - ✅ 不依赖 Hilt 代码生成
   - ✅ 更容易调试和理解

2. **简化依赖管理**
   - ✅ 所有依赖在一个文件中管理
   - ✅ Lazy 初始化保证性能
   - ✅ 清晰的依赖链

3. **支持迁移**
   - ✅ 可以轻松迁移到 Koin
   - ✅ 或在未来 Hilt 问题解决后恢复

4. **开发效率**
   - ✅ 更快的编译速度（无代码生成）
   - ✅ 更少的配置文件
   - ✅ 更容易理解

### 负面影响

1. **手动管理依赖**
   - ❌ 需要手动维护依赖图
   - ❌ 不如 Hilt 的编译时检查
   - ❌ 缺少 IDE 自动完成支持

2. **测试复杂度**
   - ❌ 单元测试需要手动提供依赖
   - ❌ 不能利用 Hilt 的测试绑定

3. **学习曲线**
   - ❌ 新开发者需要了解 Service Locator 模式

### 真机 vs 模拟器

| 特性 | 模拟器 | 真机 | 一致性 |
|------|--------|------|--------|
| 启动 | ✅ | ✅ | ✅ 一致 |
| 导航 | ✅ | ✅ | ✅ 一致 |
| 依赖注入 | ✅ | ✅ | ✅ 一致 |
| 性能 | ✅ | ✅ | ✅ 一致 |

**结论**: ✅ 真机和模拟器行为完全一致

---

## 替代方案

### 方案 A: 保持完整 Hilt
**描述**: 恢复 `@HiltAndroidApp` 和 `@AndroidEntryPoint`

**优点**:
- 标准做法
- 编译时检查
- IDE 支持

**缺点**:
- ❌ 真机上代码生成失败
- ❌ KSP 版本兼容性问题
- ❌ 无法解决当前问题

**为什么不选择**: 技术上不可行

### 方案 B: 迁移到 Koin
**描述**: 完全迁移到 Koin 框架

**优点**:
- 纯 Kotlin，无代码生成
- 简单易用
- 性能更好

**缺点**:
- ❌ 需要大量重写代码
- ❌ 学习成本高
- ❌ 时间投入大

**为什么不选择**: 时间成本太高，Service Locator 已经足够

### 方案 C: 手动依赖注入（无框架）
**描述**: 每个 Screen 手动创建 ViewModel

**优点**:
- 最简单
- 无依赖

**缺点**:
- ❌ 代码重复
- ❌ 难以维护
- ❌ 不符合最佳实践

**为什么不选择**: 不可维护

---

## 实施计划

1. ✅ **Phase 1**: 创建 Service Locator（已完成）
2. ✅ **Phase 2**: 更新所有 Screen 使用 Service Locator（已完成）
3. ✅ **Phase 3**: 真机测试验证（已完成）
4. ⏳ **Phase 4**: 未来考虑迁移到 Koin（可选）

---

## 相关文档

- [Hilt 崩溃根因分析](../reports/issues/HILT_CRASH_ROOT_CAUSE_ANALYSIS.md)
- [真机修复报告](../reports/issues/REAL_DEVICE_SUCCESS_REPORT.md)
- [开发规范](../development/DEVELOPMENT_STANDARDS.md)

---

**决策者**: Android Architect
**审核者**: Android Engineer
**生效日期**: 2026-02-16
