# ADR 002: Hilt 版本兼容性解决方案

## 状态
已采纳

## 日期
2026-02-16

## 上下文

### 问题
Wordland 应用在真机上使用 Hilt 2.44 和 2.48 时都遇到问题：

1. **Hilt 2.44**
   - 模拟器：✅ 工作正常
   - 真机：❌ KSP 目录为空，代码未生成
   - 错误：`ClassNotFoundException: WordlandApplication`

2. **Hilt 2.48 + KSP 1.0.16/1.0.18**
   - 模拟器：✅ 编译成功
   - 真机：❌ JavaPoet 兼容性错误
   - 错误：`NoSuchMethodError: ClassName.canonicalName()`

### 根本原因
- Hilt 依赖 JavaPoet 库生成代码
- JavaPoet 1.x 和 2.x API 不兼容
- KSP 版本与 JavaPoet 版本不匹配
- 真机和模拟器的 KSP 行为不一致

### 技术背景
- **Hilt**: Google 推荐的 Android 依赖注入框架
- **KSP**: Kotlin Symbol Processing，用于代码生成
- **JavaPoet**: Java 代码生成库，Hilt 依赖它

---

## 决策

### 选择方案
**混合方案：保留 Hilt ViewModel 注解，使用 Service Locator 获取 ViewModel**

### 技术架构

```
编译时：
  ViewModels: 保留 @HiltViewModel 和 @Inject constructor
  UseCases: 保留 @Inject constructor
  Repositories: 保留 @Inject constructor

运行时：
  Application: 无 Hilt 注解
  Activity: 无 Hilt 注解
  Screens: 使用 Service Locator 获取 ViewModels
```

### 具体配置

#### build.gradle.kts
```kotlin
// 保持 Hilt 2.48
plugins {
    id("com.google.dagger.hilt.android") version "2.48"
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

#### Application（无注解）
```kotlin
class WordlandApplication : Application() {
    companion object {
        const val USER_ID = "user_001"
        lateinit var instance: WordlandApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
```

#### Activity（无注解）
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordlandTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController)
            }
        }
    }
}
```

#### ViewModel（保留注解）
```kotlin
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val loadLevelWords: LoadLevelWordsUseCase,
    private val submitAnswer: SubmitAnswerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel()
```

#### Service Locator（新增）
```kotlin
object AppServiceLocator {
    private val database by lazy { WordDatabase.getInstance(instance) }
    private val repositories by lazy { /* ... */ }
    private val useCases by lazy { /* ... */ }

    fun provideFactory(): ViewModelProvider.Factory = ...
}
```

#### Screen（使用 Service Locator）
```kotlin
@Composable
fun LearningScreen(
    viewModel: LearningViewModel = viewModel(
        factory = AppServiceLocator.provideFactory()
    )
)
```

---

## 后果

### 正面影响

1. **解决兼容性问题**
   - ✅ 真机和模拟器行为一致
   - ✅ 不依赖 Hilt 代码生成
   - ✅ 避免 JavaPoet 兼容性问题

2. **保持灵活性**
   - ✅ ViewModels 保留 Hilt 注解
   - ✅ 支持未来恢复完整 Hilt
   - ✅ 支持迁移到其他 DI 框架

3. **简化调试**
   - ✅ 依赖关系清晰可见
   - ✅ 更容易理解依赖链
   - ✅ 更容易排查问题

### 负面影响

1. **失去编译时检查**
   - ❌ 不能在编译时发现依赖问题
   - ❌ 需要运行时才能发现问题

2. **手动管理依赖**
   - ❌ 需要手动维护 Service Locator
   - ❌ 新增依赖时需要更新多个地方

3. **混合模式复杂度**
   - ❌ 部分使用 Hilt，部分不使用
   - ❌ 新开发者可能困惑

### 真机 vs 模拟器

| 场景 | 模拟器 | 真机 | 一致性 |
|------|--------|------|--------|
| 应用启动 | ✅ | ✅ | ✅ 一致 |
| 导航功能 | ✅ | ✅ | ✅ 一致 |
| 依赖注入 | ✅ | ✅ | ✅ 一致 |
| 编译速度 | ⚡ 快 | ⚡ 快 | ✅ 一致 |

**结论**: ✅ 真机和模拟器完全一致

---

## 替代方案

### 方案 A: 使用 Hilt 2.44
**描述**: 降级到 Hilt 2.44

**优点**:
- 标准做法

**缺点**:
- ❌ 真机上代码生成失败
- ❌ KSP 目录为空

**为什么不选择**: 技术上不可行

### 方案 B: 升级 JavaPoet
**描述**: 等待 Hilt 2.49+ 修复 JavaPoet 兼容性

**优点**:
- 可能完全解决问题
- 标准做法

**缺点**:
- ❌ 时间不确定
- ❌ 可能引入新问题
- ❌ 阻塞当前开发

**为什么不选择**: 不能等待

### 方案 C: 迁移到 Koin
**描述**: 完全迁移到 Koin 框架

**优点**:
- 无代码生成问题
- 纯 Kotlin

**缺点**:
- ❌ 需要大量重写
- ❌ 时间成本高
- ❌ 团队需要学习

**为什么不选择**: 时间成本太高

### 方案 D: 移除 Hilt 完全不用
**描述**: 移除所有 Hilt 注解，纯手动依赖注入

**优点**:
- 最简单

**缺点**:
- ❌ 失去依赖注入的好处
- ❌ 代码重复
- ❌ 难以测试

**为什么不选择**: 不可维护

---

## 技术细节

### JavaPoet API 变更
```java
// JavaPoet 1.x
public String getCanonicalName()

// JavaPoet 2.x
public ClassName canonicalName()
```

Hilt 2.48 使用了 JavaPoet 2.x 的 API，但 KSP 1.0.16/1.0.18 依赖 JavaPoet 1.x。

### KSP 版本兼容性
| Hilt 版本 | KSP 版本 | JavaPoet 版本 | 真机状态 |
|-----------|----------|---------------|----------|
| 2.44 | 1.0.16 | 1.x | ❌ 代码生成失败 |
| 2.48 | 1.0.16 | 1.x | ❌ API 不兼容 |
| 2.48 | 1.0.18 | 1.x | ❌ API 不兼容 |
| 2.48 | 1.0.20 | ? | ❌ 版本不存在 |

### 为什么混合方案可行
1. ViewModels 的 `@HiltViewModel` 只是标记注解，不触发代码生成
2. `@Inject constructor` 也只是标记，实际创建由 Service Locator 完成
3. 只有 `@HiltAndroidApp` 和 `@AndroidEntryPoint` 会触发实际的代码生成
4. 移除这两个注解后，不会触发有问题的代码生成路径

---

## 迁移指南

### 从混合方案到完整 Hilt
如果未来 Hilt 兼容性问题解决：

1. 恢复 `@HiltAndroidApp` 到 WordlandApplication
2. 恢复 `@AndroidEntryPoint` 到 MainActivity
3. 将 `viewModel(factory = ServiceLocator...)` 改回 `hiltViewModel()`
4. 删除 Service Locator

### 从混合方案到 Koin
如果决定迁移到 Koin：

1. 保留 `@HiltViewModel` 和 `@Inject constructor`（Koin 也支持）
2. 删除 Service Locator
3. 创建 Koin 模块
4. 在 Application 中启动 Koin
5. Screen 使用 `koinViewModel()`

---

## 相关文档

- [ADR 001: Service Locator](./001-use-service-locator.md)
- [Hilt 崩溃根因分析](../reports/issues/HILT_CRASH_ROOT_CAUSE_ANALYSIS.md)
- [真机修复报告](../reports/issues/REAL_DEVICE_SUCCESS_REPORT.md)

---

## 经验教训

### 技术层面
1. **理解框架依赖**：Hilt 依赖 JavaPoet，JavaPoet 版本很重要
2. **真机测试必需**：模拟器成功 ≠ 真机成功
3. **混合方案可行**：可以结合多种技术的优点

### 流程层面
1. **版本锁定**：明确记录所有依赖版本
2. **真机优先**：重要的功能先在真机上验证
3. **文档记录**：记录兼容性问题和解决方案

---

**决策者**: Android Architect
**审核者**: Android Engineer
**生效日期**: 2026-02-16
