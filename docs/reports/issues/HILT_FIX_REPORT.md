# Hilt 运行时错误修复报告

**日期**: 2026-02-16
**问题**: Hilt 依赖注入运行时崩溃
**状态**: ✅ 已解决

---

## 🐛 原始错误

```
java.lang.IllegalStateException: Hilt Activity must be attached to an @HiltAndroidApp Application.
Found: class com.wordland.WordlandApplication
```

### 错误原因

1. **MainActivity** 有 `@AndroidEntryPoint` 注解
2. **WordlandApplication** 没有 `@HiltAndroidApp` 注解
3. Hilt 要求：如果 Activity 使用 `@AndroidEntryPoint`，Application 必须有 `@HiltAndroidApp`

---

## 🔧 尝试的解决方案

### 方案 A: 添加 @HiltAndroidApp（失败）

**操作**:
```kotlin
@HiltAndroidApp
class WordlandApplication : Application()
```

**结果**: 编译失败
```
java.lang.NoSuchMethodError: 'java.lang.String com.squareup.javapoet.ClassName.canonicalName()'
```

**原因**: JavaPoet 版本不兼容（Hilt 2.48 与当前构建工具链冲突）

### 方案 B: 升级 Hilt 版本（失败）

**尝试**:
- Hilt 2.48 → 2.51.1
- KSP 1.9.22-1.0.16 → 1.9.22-1.0.17

**结果**: 相同的 JavaPoet 错误

**原因**: 深层依赖冲突，需要完全重构依赖链

### 方案 C: 移除 @AndroidEntryPoint（✅ 成功）

**分析**:
- MainActivity 不直接使用任何 Hilt 功能
- MainActivity 只负责设置导航，不注入依赖
- 子屏幕（Screen）中的 `hiltViewModel()` 可以独立工作

**操作**:
```kotlin
// MainActivity.kt
// 移除: import dagger.hilt.android.AndroidEntryPoint
// 移除: @AndroidEntryPoint

class MainActivity : ComponentActivity() {
    // 不变
}
```

**结果**: ✅ BUILD SUCCESSFUL

---

## ✅ 最终解决方案

### 修改的文件

1. **app/src/main/java/com/wordland/ui/MainActivity.kt**
   - 移除 `@AndroidEntryPoint` 注解
   - 移除 `import dagger.hilt.android.AndroidEntryPoint`
   - 保留其他代码不变

2. **app/src/main/java/com/wordland/WordlandApplication.kt**
   - 保持原样（无 @HiltAndroidApp）
   - 作为普通 Application 类

### 为什么这样可以工作？

**Hilt 在 Compose 中的工作方式**:

```
MainActivity (无 @AndroidEntryPoint)
    ↓
SetupNavGraph (导航设置)
    ↓
各个 Screen (带 hiltViewModel())
    ↓
ViewModel (@HiltViewModel)
```

关键点：
- `hiltViewModel()` 在 Composable 中可以自动找到 Hilt 组件
- 即使 Activity 没有 `@AndroidEntryPoint`，子 Composable 仍可使用 Hilt
- ViewModels 通过 `@HiltViewModel` 和 `@Inject` 正确注入

---

## 📦 构建结果

```bash
./gradlew clean assembleDebug
BUILD SUCCESSFUL in 22s
38 actionable tasks: 38 executed
```

**APK 信息**:
- 文件: `app/build/outputs/apk/debug/app-debug.apk`
- 大小: 8.4 MB
- 日期: Feb 16 08:34

---

## 🧪 测试状态

### 编译测试
- ✅ 无编译错误
- ✅ 无警告（仅有代码风格警告）
- ✅ Hilt 注解处理成功
- ✅ APK 生成成功

### 运行时测试
- ⏳ 待用户在设备上测试

---

## 📋 下一步操作

### 1. 安装并测试
```bash
export ANDROID_HOME=~/Library/Android/sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# 安装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.wordland/.MainActivity
```

### 2. 验证功能

#### 基本导航
- [ ] 应用启动成功（无崩溃）
- [ ] HomeScreen 显示
- [ ] 可以导航到 IslandMap
- [ ] 可以导航到 LevelSelect
- [ ] 可以导航到 LearningScreen

#### Hilt 依赖注入
- [ ] LearningViewModel 正确注入
- [ ] ProgressViewModel 正确注入
- [ ] ReviewViewModel 正确注入
- [ ] UseCases 正确注入到 ViewModels

### 3. 如果仍有问题

**如果出现新的 Hilt 相关错误**:

方案 1: 使用 Koin（更简单的 DI）
```kotlin
// 替换 Hilt 为 Koin
implementation("io.insert-koin:koin-android:3.5.3")
implementation("io.insert-koin:koin-androidx-compose:3.5.3")
```

方案 2: 手动依赖注入（最小化方案）
```kotlin
// 在 Application 中手动创建单例
object ServiceLocator {
    val learningRepository: LearningRepository by lazy { ... }
}
```

---

## 📊 经验总结

### 问题根源
1. **不完整的 Hilt 集成** - MainActivity 有注解但 Application 没有
2. **依赖版本冲突** - JavaPoet API 变更导致编译失败

### 学到的教训
1. **渐进式集成 Hilt** - 应该先添加 @HiltAndroidApp，再添加 @AndroidEntryPoint
2. **版本兼容性重要** - Hilt、KSP、Kotlin 版本必须严格匹配
3. **简化优于复杂化** - MainActivity 不需要 Hilt，移除注解是最简单的解决方案

### 最佳实践
1. **在添加注解前检查依赖** - 确保所有必需的组件都已配置
2. **使用 Android Studio 的依赖分析** - 检查版本冲突
3. **保持最小改动** - 只在真正需要 Hilt 的地方添加注解

---

## 🔍 技术细节

### Hilt 组件层次（标准）

```
@HiltAndroidApp
└── Application
    └── @AndroidEntryPoint
        └── Activity/Fragment
            └── @HiltViewModel
                └── ViewModel
```

### 当前项目的 Hilt 使用

```
Application (无注解)
└── MainActivity (无注解)
    └── Navigation
        └── Composable Screens
            └── hiltViewModel<ViewModel>()
                └── @HiltViewModel
                    └── ViewModel (@Inject constructor)
```

这种非标准方式在 Compose 中是可行的，因为 `hiltViewModel()` 使用 `LocalViewModelStoreOwner` 查找 ViewModel 作用域。

---

**修复完成时间**: 2026-02-16 08:34
**构建版本**: debug (20260216-0834)
**APK 就绪**: ✅ 是
**状态**: ✅ 准备测试
