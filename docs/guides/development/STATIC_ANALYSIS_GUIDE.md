# 静态分析工具使用指南

**更新日期**: 2026-02-16

---

## 📋 概述

项目已配置以下静态分析工具：

1. **Detekt** - Kotlin代码质量检测
2. **KtLint** - Kotlin代码格式化

---

## 🔧 Detekt - 代码质量检测

### 运行Detekt

**检查所有代码**:
```bash
./gradlew detekt
```

**检查特定文件**:
```bash
./gradlew detekt -P detekt.config=config/detekt/detekt.yml
```

**生成HTML报告**:
```bash
./gradlew detekt
# 报告位置: app/build/reports/detekt/detekt.html
```

### Detekt规则分类

| 类别 | 说明 | 严重性 |
|------|------|--------|
| **Complexity** | 复杂度规则（方法长度、参数数量等） | 中 |
| **Style** | 代码风格（命名、注释等） | 低 |
| **Potential Bugs** | 潜在bug（空指针、不必要的操作等） | 高 |
| **Performance** | 性能问题（不必要的临时对象等） | 中 |
| **Exceptions** | 异常处理（捕获过于宽泛的异常等） | 中 |

### 修复Detekt问题

**1. 查看问题**:
```bash
./gradlew detekt
# 查看控制台输出或HTML报告
```

**2. 常见问题及修复**:

**LongMethod** - 方法过长
```kotlin
// ❌ 之前：方法超过30行
fun myLongFunction() {
    // ... 50行代码
}

// ✅ 修复：拆分为小函数
fun myLongFunction() {
    step1()
    step2()
}

private fun step1() { /* ... */ }
private fun step2() { /* ... */ }
```

**MagicNumber** - 魔术数字
```kotlin
// ❌ 之前：直接使用数字
if (count == 42) { }

// ✅ 修复：使用常量
companion object {
    private const val MAX_COUNT = 42
}
if (count == MAX_COUNT) { }
```

**TooManyFunctions** - 函数过多
```kotlin
// ❌ 之前：一个文件15+个函数
class MyBigClass {
    fun a() { /* ... */ }
    fun b() { /* ... */ }
    // ... 13个函数
}

// ✅ 修复：拆分为多个类
class MyFeature {
    fun a() { /* ... */ }
    fun b() { /* ... */ }
}

class MyUtils {
    fun c() { /* ... */ }
    fun d() { /* ... */ }
}
```

---

## 📐 KtLint - 代码格式化

### 运行KtLint

**检查代码格式**:
```bash
./gradlew ktlintCheck
```

**自动修复代码格式**:
```bash
./gradlew ktlintFormat
```

**检查特定文件**:
```bash
./gradlew ktlintCheck -P ktlintFilter="**/ui/**"
```

### KtLint规则

**常用规则**:
- 最大行长度：120字符
- 缩进：4个空格
- 函数命名：camelCase
- 类命名：PascalCase
- 禁止通配符导入：`import package.*`

**常见问题及修复**:

**MaxLineLength** - 行过长
```kotlin
// ❌ 之前：行超过120字符
val result = repository.getUserData(userId).transform { it.map { it.value }.filter { it.isValid }.map { it.toDto() } }

// ✅ 修复：拆分多行
val result = repository.getUserData(userId)
    .transform { it.map { it.value } }
    .filter { it.isValid }
    .map { it.toDto() }
```

**WildcardImport** - 通配符导入
```kotlin
// ❌ 之前
import com.wordland.model.*

// ✅ 修复
import com.wordland.model.Word
import com.wordland.model.UserWordProgress
```

---

## 🔗 集成到开发流程

### 方式1: 手动运行

**在提交代码前**:
```bash
# 1. 格式化代码
./gradlew ktlintFormat

# 2. 检查代码质量
./gradlew detekt

# 3. 运行测试
./gradlew test

# 4. 提交
git add .
git commit -m "feat: Your changes"
```

### 方式2: Pre-commit Hook（推荐）

**安装hooks**:
```bash
./scripts/hooks/install-hooks.sh
```

**自动检查**:
```bash
# 提交时自动运行质量检查
git add .
git commit -m "feat: Your changes"
# Hook自动运行ktlintCheck、detekt、test、assembleDebug
```

**跳过检查**（不推荐）:
```bash
git commit --no-verify -m "feat: Your changes"
```

### 方式3: IDE插件

**Android Studio**:
1. 安装Detekt插件
2. 安装KtLint插件
3. 配置自动运行
4. 设置保存时自动格式化

**IntelliJ IDEA**:
1. File → Settings → Plugins
2. 搜索"Detekt"和"KtLint"
3. 安装并重启IDE

---

## 🚀 CI/CD集成

Detekt和KtLint已集成到GitHub Actions中：

**CI工作流** (`.github/workflows/ci.yml`):
```yaml
- name: Run Lint checks
  run: ./gradlew lintDebug
```

**质量门禁** (`.github/workflows/quality-gate.yml`):
```yaml
# Lint检查必须通过才能合并PR
```

---

## 📊 质量门禁标准

### Pull Request必须通过

| 检查项 | 标准 | 状态 |
|--------|------|------|
| KtLint | 无格式错误 | ⏳ 待配置 |
| Detekt | 0个Critical/High问题 | ⏳ 待配置 |
| 单元测试 | 100%通过 | ✅ 已配置 |
| 测试覆盖率 | ≥80% | ✅ 已配置 |
| 编译 | 成功 | ✅ 已配置 |

---

## 🛠️ 配置文件

### Detekt配置

**位置**: `config/detekt/detekt.yml`

**自定义规则**:
```yaml
complexity:
  LongMethod:
    threshold: 30
  ComplexMethod:
    threshold: 10

style:
  MaxLineLength:
    maxLineLength: 120
```

### KtLint配置

**位置**: `app/build.gradle.kts`

**当前配置**:
```kotlin
ktlint {
    version = "1.0.1"
    debug = false
    outputToConsole = true
    android = true
}
```

---

## 💡 最佳实践

### 1. 开发流程

**推荐的代码编写流程**:
1. 编写代码
2. 实时格式化（IDE插件）
3. 本地检查（ktlintCheck + detekt）
4. 提交代码（pre-commit hook自动检查）

### 2. 修复优先级

**必须修复**（阻塞合并）:
- ❌ Critical级别问题
- ❌ High级别bug
- ❌ 测试失败

**建议修复**（可后续PR）:
- ⚠️ Warning级别问题
- ⚠️ 代码风格问题

### 3. 团队协作

**Code Review时**:
- 检查Detekt报告
- 验证KtLint格式
- 确保测试覆盖率未下降

---

## 📈 质量指标追踪

### 初始基准

**当前状态**（待测量）:
- Detekt问题数：?
- KtLint格式问题：?
- 代码覆盖率：84.6%

**目标**（1个月内）:
- Detekt问题：< 50个
- KtLint问题：0个
- 代码覆盖率：≥80%

### 每周回顾

**团队会议检查项**:
- [ ] Detekt报告生成
- [ ] KtLint检查通过
- [ ] 覆盖率未下降
- [ ] 新增问题已修复

---

## 🐛 故障排查

### 常见问题

**Q1: KtLint报告的格式问题如何快速修复？**
```bash
# 自动修复大部分格式问题
./gradlew ktlintFormat

# 检查是否还有未修复的问题
./gradlew ktlintCheck
```

**Q2: Detekt报告如何查看？**
```bash
# 生成HTML报告
./gradlew detekt

# 打开报告
open app/build/reports/detekt/detekt.html
# 或
start app/build/reports/detekt/detekt.html
```

**Q3: 某些规则想临时禁用怎么办？**
```kotlin
// 在文件顶部添加注释
@Suppress("MagicNumber")
fun myFunction() {
    val count = 42  // 此MagicNumber被抑制
}
```

**Q4: CI中静态分析失败怎么办？**
```bash
# 本地运行相同的检查
./gradlew detekt ktlintCheck

# 如果本地通过但CI失败，检查：
# - Gradle版本是否一致
# - 配置文件是否提交
# - 依赖是否完整
```

---

## 📚 参考资源

- [Detekt文档](https://detekt.dev/)
- [KtLint文档](https://pinterest.github.io/ktlint/)
- [Kotlin代码规范](https://kotlinlang.org/docs/coding-conventions.html)

---

**最后更新**: 2026-02-16
**维护者**: Android Team
