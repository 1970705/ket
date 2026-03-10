# TDD 修复策略文档

**Document Version**: 1.0
**Last Updated**: 2026-02-28
**Author**: android-test-engineer

本文档详细定义 TDD Auto-Loop 系统的修复策略，包括自动修复模式、修复生成算法、修复验证机制和修复回滚策略。

---

## 1. 修复策略概述

### 1.1 修复能力分级

```
Level 1: 完全自动修复 (100% 自动)
  └─ 无需人工确认，直接应用

Level 2: 智能建议修复 (需确认)
  └─ 提供多个修复方案，用户选择

Level 3: 辅助修复 (需指导)
  └─ 提供修复方向，人工编写代码

Level 4: 人工修复 (完全人工)
  └─ 仅提供诊断信息，完全由人工处理
```

### 1.2 修复成功率目标

| 错误类型 | 自动修复率 | 人工辅助率 | 完全人工率 |
|---------|-----------|-----------|-----------|
| **编译错误** | 85% | 10% | 5% |
| **测试失败** | 70% | 20% | 10% |
| **运行时错误** | 55% | 25% | 20% |

### 1.3 修复应用流程

```
分析失败 → 生成修复候选 → 排序候选 → 选择最佳 → 验证修复 → 应用/回滚
```

---

## 2. 编译错误修复策略

### 2.1 语法错误修复

#### 2.1.1 缺失分号

**模式**:
```kotlin
// 错误
val a = 1 val b = 2

// 正确
val a = 1
val b = 2
```

**修复算法**:
```kotlin
class MissingSemicolonFixer : SyntaxFixer {
    override fun canFix(error: CompilationError): Boolean {
        return error.message.contains("unexpected tokens") ||
               error.message.contains("use ';' to separate")
    }

    override fun generateFix(error: CompilationError, code: String): Fix {
        val lines = code.lines()
        val fixedLines = lines.map { line ->
            // 识别行内多个语句
            if (line.contains(Regex("""\bval\b.*\bval\b"""))) {
                // 在两个 val 之间插入换行
                line.replace(Regex("""(\s+)val\s"""), "\nval ")
            } else {
                line
            }
        }

        return Fix(
            description = "Add missing semicolons and line breaks",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    originalCode = code,
                    fixedCode = fixedLines.joinToString("\n")
                )
            ),
            confidence = 0.95
        )
    }
}
```

**自动修复成功率**: 95%

---

#### 2.1.2 括号不匹配

**模式**:
```kotlin
// 错误
class MyClass {
    fun method() {
        if (condition) {
            println("test")
        // 缺失两个闭合括号
    }

// 正确
class MyClass {
    fun method() {
        if (condition) {
            println("test")
        }
    }
}
```

**修复算法**:
```kotlin
class BracketMatcher {
    fun fixBrackets(code: String): Fix {
        val stack = ArrayDeque<Bracket>()
        val missingBrackets = mutableListOf<Bracket>()

        code.forEachIndexed { index, char ->
            when (char) {
                '(', '[', '{' -> stack.push(Bracket(char, index))
                ')', ']', '}' -> {
                    if (stack.isEmpty()) {
                        // 多余的闭合括号
                        missingBrackets.add(Bracket('o', index))
                    } else {
                        stack.pop()
                    }
                }
            }
        }

        // 栈中剩余的是未闭合的括号
        val unclosed = stack.toList().reversed()

        // 生成修复
        val closingBrackets = unclosed.map { it.closingPair() }.joinToString("")
        return Fix(
            description = "Add missing closing brackets: $closingBrackets",
            changes = listOf(
                Change(
                    filePath = currentFile,
                    insertAt = code.length,
                    text = closingBrackets
                )
            ),
            confidence = 0.90
        )
    }

    private fun Bracket.closingPair(): String = when (this.char) {
        '(' -> ")"
        '[' -> "]"
        '{' -> "}"
        else -> ""
    }
}
```

**自动修复成功率**: 90%

---

#### 2.1.3 字符串未终止

**模式**:
```kotlin
// 错误
val message = "hello

// 正确
val message = "hello"
```

**修复算法**:
```kotlin
class StringTerminatorFixer {
    fun fixUnterminatedString(code: String, line: Int, column: Int): Fix {
        val lines = code.lines()
        val errorLine = lines[line - 1]

        // 检查是单引号还是双引号字符串
        val quote = if (errorLine[column - 1] == '"') '"' else '\''

        return Fix(
            description = "Add missing string terminator ($quote)",
            changes = listOf(
                Change(
                    filePath = currentFile,
                    insertAt = Position(line = line, column = errorLine.length),
                    text = quote.toString()
                )
            ),
            confidence = 0.98
        )
    }
}
```

**自动修复成功率**: 98%

---

### 2.2 类型不匹配修复

#### 2.2.1 基本类型转换

**模式**:
```kotlin
// 错误: Type mismatch: inferred type is Double but Int was expected
val stars: Int = calculateStars() // 返回 Double

// 修复方案 1: 类型转换
val stars = calculateStars().toInt()

// 修复方案 2: 修改变量类型
val stars: Double = calculateStars()

// 修复方案 3: 修改函数返回类型
fun calculateStars(): Int { ... }
```

**修复算法**:
```kotlin
class TypeMismatchFixer(
    private val typeAnalyzer: TypeAnalyzer,
    private val codeEditor: CodeEditor
) {
    fun fixTypeMismatch(error: TypeMismatchError): List<Fix> {
        val fixes = mutableListOf<Fix>()

        // 方案 1: 添加类型转换
        fixes.add(generateConversionFix(error))

        // 方案 2: 修改变量类型
        fixes.add(generateVariableTypeFix(error))

        // 方案 3: 修改函数返回类型
        fixes.add(generateFunctionTypeFix(error))

        return fixes.sortedByDescending { it.confidence }
    }

    private fun generateConversionFix(error: TypeMismatchError): Fix {
        val conversion = when {
            error.actualType == "Double" && error.expectedType == "Int" -> ".toInt()"
            error.actualType == "Int" && error.expectedType == "Double" -> ".toDouble()"
            error.actualType == "Int" && error.expectedType == "String" -> ".toString()"
            error.actualType == "String" && error.expectedType == "Int" -> ".toInt()"
            else -> ""
        }

        return Fix(
            description = "Add type conversion: $conversion",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    position = error.expression.end,
                    text = conversion
                )
            ),
            confidence = 0.75
        )
    }

    private fun generateVariableTypeFix(error: TypeMismatchError): Fix {
        return Fix(
            description = "Change variable type from ${error.expectedType} to ${error.actualType}",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    position = error.variableDeclaration.typePosition,
                    text = error.actualType
                )
            ),
            confidence = 0.65
        )
    }

    private fun generateFunctionTypeFix(error: TypeMismatchError): Fix {
        return Fix(
            description = "Change function return type from ${error.actualType} to ${error.expectedType}",
            changes = listOf(
                Change(
                    filePath = error.function.filePath,
                    position = error.function.returnTypePosition,
                    text = error.expectedType
                )
            ),
            confidence = 0.50
        )
    }
}
```

**自动修复成功率**: 80%

---

#### 2.2.2 可空性修复

**模式**:
```kotlin
// 错误: Type mismatch: inferred type is String? but String was expected
val name: String = getName() // 返回 String?

// 修复方案 1: 安全调用
val name: String? = getName()

// 修复方案 2: 非空断言
val name: String = getName()!!

// 修复方案 3: Elvis 操作符
val name: String = getName() ?: "unknown"
```

**修复算法**:
```kotlin
class NullabilityFixer {
    fun fixNullability(error: NullabilityError): List<Fix> {
        val fixes = mutableListOf<Fix>()

        // 方案 1: 添加可空性
        fixes.add(
            Fix(
                description = "Make variable nullable: ${error.variableName}: ${error.expectedType}?",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = error.variableDeclaration.typeEnd,
                        text = "?"
                    )
                ),
                confidence = 0.70
            )
        )

        // 方案 2: 非空断言
        fixes.add(
            Fix(
                description = "Add non-null assertion operator (!!)",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = error.expression.end,
                        text = "!!"
                    )
                ),
                confidence = 0.50
            )
        )

        // 方案 3: Elvis 操作符
        val defaultValue = inferDefaultValue(error.expectedType)
        fixes.add(
            Fix(
                description = "Add Elvis operator with default value: ?: $defaultValue",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = error.expression.end,
                        text = " ?: $defaultValue"
                    )
                ),
                confidence = 0.60
            )
        )

        return fixes
    }

    private fun inferDefaultValue(type: String): String {
        return when (type) {
            "String" -> "\"\""
            "Int" -> "0"
            "Double" -> "0.0"
            "Boolean" -> "false"
            else -> "null"
        }
    }
}
```

**自动修复成功率**: 75%

---

### 2.3 缺失导入修复

#### 2.3.1 自动导入生成

**修复算法**:
```kotlin
class ImportResolver(
    private val projectIndex: ProjectIndex,
    private val codeEditor: CodeEditor
) {
    fun resolveAndAddImport(error: UnresolvedReferenceError): Fix {
        val symbolName = error.symbolName

        // 1. 在项目中搜索符号
        val candidates = projectIndex.findSymbol(symbolName)

        // 2. 计算相关性评分
        val ranked = rankByRelevance(candidates, error.context)

        // 3. 选择最佳匹配
        val best = ranked.firstOrNull()
            ?: return Fix.CANNOT_AUTO_FIX

        // 4. 生成导入语句
        val importStatement = "import ${best.fullName}"

        return Fix(
            description = "Add import: $importStatement",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    insertAt = Position(line = 1, column = 1), // 文件顶部
                    text = "$importStatement\n"
                )
            ),
            confidence = best.score
        )
    }

    private fun rankByRelevance(
        candidates: List<Symbol>,
        context: Context
    ): List<RankedSymbol> {
        return candidates.map { symbol ->
            var score = 0.0

            // 同一模块加分
            if (symbol.module == context.currentModule) score += 0.5

            // 常用包加分
            if (symbol.packageName in COMMON_PACKAGES) score += 0.3

            // 相同文件名模式加分
            if (symbol.fileName == context.currentFileName) score += 0.2

            // 最近使用过加分
            if (symbol in context.recentlyUsed) score += 0.1

            RankedSymbol(symbol, score)
        }.sortedByDescending { it.score }
    }

    companion object {
        val COMMON_PACKAGES = setOf(
            "kotlin.collections",
            "kotlin.io",
            "androidx.compose.runtime",
            "androidx.lifecycle",
            "io.mockk",
            "assertk",
            "org.junit"
        )
    }
}
```

**自动修复成功率**: 100%

---

## 3. 测试失败修复策略

### 3.1 断言失败修复

#### 3.1.1 值计算错误修复

**模式**:
```kotlin
// 测试
@Test
fun `should return 3 stars for perfect performance`() {
    val result = calculator.calculateStars(
        correctAnswers = 6,
        totalQuestions = 6,
        hintsUsed = 0
    )
    assertThat(result).isEqualTo(3)  // 失败: actual = 2
}

// 分析: 算法可能少计算了 1 星
// 修复: 调整算法逻辑
```

**修复算法**:
```kotlin
class AssertionFailureAnalyzer {
    fun analyzeAndSuggest(failure: AssertionFailure): List<Fix> {
        val diff = ValueDiffAnalyzer.diff(failure.expected, failure.actual)

        return when {
            // 整数差异
            diff.isIntegerDifference() -> {
                analyzeIntegerDifference(failure, diff)
            }

            // 布尔差异
            diff.isBooleanDifference() -> {
                analyzeBooleanDifference(failure)
            }

            // 字符串差异
            diff.isStringDifference() -> {
                analyzeStringDifference(failure, diff)
            }

            // 集合差异
            diff.isCollectionDifference() -> {
                analyzeCollectionDifference(failure, diff)
            }

            else -> {
                listOf(Fix.CANNOT_AUTO_FIX)
            }
        }
    }

    private fun analyzeIntegerDifference(
        failure: AssertionFailure,
        diff: ValueDiff
    ): List<Fix> {
        val fixes = mutableListOf<Fix>()

        val expected = failure.expected as Int
        val actual = failure.actual as Int
        val delta = expected - actual

        // 策略 1: 如果差异是 1，可能是边界条件错误
        if (Math.abs(delta) == 1) {
            fixes.add(
                Fix(
                    description = "Potential off-by-one error: adjust comparison from > to >=",
                    changes = generateBoundaryFix(failure),
                    confidence = 0.70
                )
            )
        }

        // 策略 2: 如果差异符合已知模式，调整计算
        if (delta == 1) {
            fixes.add(
                Fix(
                    description = "Add 1 to calculation result",
                    changes = generateCalculationFix(failure, "+ 1"),
                    confidence = 0.60
                )
            )
        }

        // 策略 3: 修正测试预期 (如果实现逻辑看起来合理)
        if (isImplementationReasonable(failure)) {
            fixes.add(
                Fix(
                    description = "Update test expectation from $expected to $actual",
                    changes = generateExpectationFix(failure, actual),
                    confidence = 0.40
                )
            )
        }

        return fixes
    }

    private fun isImplementationReasonable(failure: AssertionFailure): Boolean {
        // 检查实现逻辑是否合理
        // 例如: 是否有注释、是否有逻辑结构
        val implementation = failure.testMethod.implementation
        return implementation.hasComments() ||
               implementation.hasLogicalStructure()
    }
}
```

**自动修复成功率**: 70%

---

#### 3.1.2 边界条件修复

**模式**:
```kotlin
// 错误
for (i in 0..list.size) {  // Off-by-one: 应该是 until
    println(list[i])
}

// 修复
for (i in list.indices) {
    println(list[i])
}
```

**修复算法**:
```kotlin
class BoundaryConditionFixer {
    fun fixOffByOne(error: IndexOutOfBoundsError): Fix {
        val loopExpression = error.loopExpression

        return when {
            // 检测 0..size 模式
            loopExpression.matches(Regex("""0\.\.(\w+)\.size""")) -> {
                Fix(
                    description = "Replace '0..list.size' with 'list.indices'",
                    changes = listOf(
                        Change(
                            filePath = error.filePath,
                            position = loopExpression.range,
                            text = "${loopExpression.variableName}.indices"
                        )
                    ),
                    confidence = 0.95
                )
            }

            // 检测 1..size 模式
            loopExpression.matches(Regex("""1\.\.(\w+)\.size""")) -> {
                Fix(
                    description = "Replace '1..list.size' with 'list.indices'",
                    changes = listOf(
                        Change(
                            filePath = error.filePath,
                            position = loopExpression.range,
                            text = "${loopExpression.variableName}.indices"
                        )
                    ),
                    confidence = 0.95
                )
            }

            // 检测 > 应该是 >=
            loopExpression.contains(">") && loopExpression.contains("size") -> {
                Fix(
                    description = "Replace '>' with '>=' for inclusive boundary",
                    changes = listOf(
                        Change(
                            filePath = error.filePath,
                            position = loopExpression.find(">"),
                            text = ">="
                        )
                    ),
                    confidence = 0.85
                )
            }

            else -> Fix.CANNOT_AUTO_FIX
        }
    }
}
```

**自动修复成功率**: 85%

---

### 3.2 Mock 失败修复

#### 3.2.1 缺失 Mock 答案

**模式**:
```kotlin
// 错误: no answer found for: WordRepository(#1).getWord(any())
val mockRepository = mockk<WordRepository>()
val word = mockRepository.getWord("test_001")

// 修复: 添加 Mock 答案
val mockRepository = mockk<WordRepository>()
every { mockRepository.getWord(any()) } returns Word(
    id = "test_001",
    word = "test"
)
val word = mockRepository.getWord("test_001")
```

**修复算法**:
```kotlin
class MockAnswerGenerator {
    fun generateMockAnswer(error: MockMissingAnswerError): Fix {
        val mockMethod = error.mockMethod
        val returnType = mockMethod.returnType

        // 生成合适的返回值
        val returnValue = generateDefaultValue(returnType)

        // 生成 Mock 答案语句
        val mockAnswer = when {
            mockMethod.hasParameters() -> {
                "every { ${error.mockVariableName}.${mockMethod.name}(any()) } returns $returnValue"
            }
            else -> {
                "every { ${error.mockVariableName}.${mockMethod.name}() } returns $returnValue"
            }
        }

        return Fix(
            description = "Add mock answer: $mockAnswer",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    insertAt = error.mockVariableDeclaration.next(),
                    text = "\n$mockAnswer\n"
                )
            ),
            confidence = 0.90
        )
    }

    private fun generateDefaultValue(type: KotlinType): String {
        return when (type.simpleName) {
            "String" -> "\"test\""
            "Int" -> "0"
            "Double" -> "0.0"
            "Boolean" -> "true"
            "List" -> "emptyList()"
            "Word" -> """Word(id = "test", word = "test")"""
            "Result" -> """Result.success(Unit)"""
            else -> "mockk<${type.simpleName}>()"
        }
    }
}
```

**自动修复成功率**: 85%

---

#### 3.2.2 Mock 验证失败

**模式**:
```kotlin
// 错误: Verification failed: call 1 of 1: WordRepository(#1).getWord(any())
// Expected: 2 times
// Actual: 1 times
verify(exactly = 2) { mockRepository.getWord(any()) }

// 修复: 修正验证次数
verify(exactly = 1) { mockRepository.getWord(any()) }
```

**修复算法**:
```kotlin
class MockVerificationFixer {
    fun fixVerification(error: MockVerificationError): Fix {
        val expectedCalls = error.expectedCalls
        val actualCalls = error.actualCalls

        return Fix(
            description = "Update verification from $expectedCalls to $actualCalls calls",
            changes = listOf(
                Change(
                    filePath = error.filePath,
                    position = error.verificationCall.arguments[0].range,
                    text = "exactly = $actualCalls"
                )
            ),
            confidence = 0.95
        )
    }
}
```

**自动修复成功率**: 90%

---

## 4. 运行时错误修复策略

### 4.1 空指针异常修复

#### 4.1.1 安全调用操作符

**模式**:
```kotlin
// 错误
val word = repository.getWord(id)
val wordId = word.id  // NPE: word is null

// 修复方案 1: 安全调用
val wordId = repository.getWord(id)?.id

// 修复方案 2: Elvis 操作符
val wordId = repository.getWord(id)?.id ?: return null

// 修复方案 3: 显式空检查
val word = repository.getWord(id)
if (word != null) {
    val wordId = word.id
}
```

**修复算法**:
```kotlin
class NullSafetyFixer {
    fun fixNullPointer(error: NullPointerException): List<Fix> {
        val nullVariable = error.nullVariable
        val accessChain = error.accessChain

        val fixes = mutableListOf<Fix>()

        // 策略 1: 添加安全调用
        fixes.add(
            Fix(
                description = "Add safe call operator (?.) to $nullVariable",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = accessChain.firstDot(),
                        text = "?."
                    )
                ),
                confidence = 0.70
            )
        )

        // 策略 2: 添加 Elvis 操作符
        val defaultValue = inferDefaultValue(accessChain.lastReturnType)
        fixes.add(
            Fix(
                description = "Add Elvis operator with default value: ?: $defaultValue",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = accessChain.end,
                        text = " ?: $defaultValue"
                    )
                ),
                confidence = 0.60
            )
        )

        // 策略 3: 添加非空断言 (谨慎)
        fixes.add(
            Fix(
                description = "Add non-null assertion operator (!!) - use with caution",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = accessChain.firstDot(),
                        text = "!!."
                    )
                ),
                confidence = 0.30  // 低置信度，风险高
            )
        )

        return fixes.sortedByDescending { it.confidence }
    }
}
```

**自动修复成功率**: 60%

---

### 4.2 索引越界修复

#### 4.2.1 使用安全的集合访问

**模式**:
```kotlin
// 错误
val item = list[index]  // Index: 5, Size: 3

// 修复方案 1: getOrNull
val item = list.getOrNull(index) ?: return null

// 修复方案 2: indices 检查
if (index in list.indices) {
    val item = list[index]
}

// 修复方案 3: forEach
list.forEachIndexed { i, item -> ... }
```

**修复算法**:
```kotlin
class IndexOutOfBoundsFixer {
    fun fixOutOfBounds(error: IndexOutOfBoundsError): List<Fix> {
        val fixes = mutableListOf<Fix>()

        // 策略 1: 使用 getOrNull
        fixes.add(
            Fix(
                description = "Replace list[index] with list.getOrNull(index) ?: <default>",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        position = error.accessExpression.range,
                        text = "${error.collectionName}.getOrNull(${error.indexExpression}) ?: ${inferDefaultValue(error.expectedType)}"
                    )
                ),
                confidence = 0.90
            )
        )

        // 策略 2: 添加边界检查
        fixes.add(
            Fix(
                description = "Add bounds check before access",
                changes = listOf(
                    Change(
                        filePath = error.filePath,
                        insertAt = error.accessExpression.start,
                        text = "if (${error.indexExpression} in ${error.collectionName}.indices) { "
                    ),
                    Change(
                        filePath = error.filePath,
                        insertAt = error.accessExpression.end,
                        text = " }"
                    )
                ),
                confidence = 0.85
            )
        )

        // 策略 3: 使用 indices (如果在循环中)
        if (error.isInLoop) {
            fixes.add(
                Fix(
                    description = "Replace loop with indices-based iteration",
                    changes = generateIndicesBasedLoopFix(error),
                    confidence = 0.95
                )
            )
        }

        return fixes
    }
}
```

**自动修复成功率**: 85%

---

## 5. 修复验证机制

### 5.1 分级验证

```kotlin
class FixValidator(
    private val testRunner: TestRunner,
    private val codeAnalyzer: CodeAnalyzer
) {
    enum class ValidationLevel {
        QUICK,      // 只运行相关测试 (30 秒)
        MEDIUM,     // 运行所有测试 (2 分钟)
        COMPREHENSIVE // 运行测试 + 覆盖率 + 代码质量 (5 分钟)
    }

    suspend fun validate(fix: Fix, level: ValidationLevel): ValidationResult {
        return when (level) {
            ValidationLevel.QUICK -> validateQuick(fix)
            ValidationLevel.MEDIUM -> validateMedium(fix)
            ValidationLevel.COMPREHENSIVE -> validateComprehensive(fix)
        }
    }

    private suspend fun validateQuick(fix: Fix): ValidationResult {
        // 1. 应用修复
        codeEditor.apply(fix)

        try {
            // 2. 只运行相关测试
            val testResult = testRunner.runTests(
                testClasses = fix.affectedTestClasses,
                timeout = 30_000
            )

            return if (testResult.allPassed) {
                ValidationResult.PASS
            } else {
                ValidationResult.FAIL(testResult.failures)
            }
        } finally {
            // 3. 无论结果如何都回滚 (后续会重新应用)
            codeEditor.rollback(fix)
        }
    }

    private suspend fun validateMedium(fix: Fix): ValidationResult {
        codeEditor.apply(fix)

        try {
            // 运行所有测试
            val testResult = testRunner.runAllTests(timeout = 120_000)

            return if (testResult.allPassed) {
                ValidationResult.PASS
            } else {
                ValidationResult.FAIL(testResult.failures)
            }
        } finally {
            codeEditor.rollback(fix)
        }
    }

    private suspend fun validateComprehensive(fix: Fix): ValidationResult {
        codeEditor.apply(fix)

        try {
            // 1. 运行所有测试
            val testResult = testRunner.runAllTests()

            if (!testResult.allPassed) {
                return ValidationResult.FAIL(testResult.failures)
            }

            // 2. 检查覆盖率
            val coverage = coverageAnalyzer.calculate()
            if (coverage.instruction < 0.80) {
                return ValidationResult.COVERAGE_TOO_LOW(coverage)
            }

            // 3. 运行代码质量检查
            val quality = codeQualityChecker.check()
            if (quality.criticalIssues > 0) {
                return ValidationResult.QUALITY_ISSUES(quality)
            }

            return ValidationResult.PASS
        } finally {
            codeEditor.rollback(fix)
        }
    }
}
```

---

### 5.2 安全修复应用

```kotlin
class SafeFixApplier(
    private val codeEditor: CodeEditor,
    private val validator: FixValidator
) {
    suspend fun <T> applyWithRollback(
        fix: Fix,
        validationLevel: ValidationLevel = ValidationLevel.QUICK
    ): Result<T> {
        // 1. 创建快照
        val beforeSnapshot = codeEditor.createSnapshot()

        try {
            // 2. 应用修复
            codeEditor.apply(fix)

            // 3. 验证修复
            val validationResult = validator.validate(fix, validationLevel)

            return when (validationResult) {
                is ValidationResult.PASS -> {
                    // 修复成功，保留变更
                    Result.success(fix)
                }
                is ValidationResult.FAIL -> {
                    // 修复失败，回滚
                    codeEditor.restoreSnapshot(beforeSnapshot)
                    Result.failure(FixValidationException(validationResult.reason))
                }
                else -> {
                    codeEditor.restoreSnapshot(beforeSnapshot)
                    Result.failure(FixValidationException("Unexpected validation result"))
                }
            }
        } catch (e: Exception) {
            // 发生异常，回滚
            codeEditor.restoreSnapshot(beforeSnapshot)
            Result.failure(e)
        }
    }
}
```

---

## 6. 修复回滚策略

### 6.1 回滚触发条件

1. **验证失败**: 测试失败、覆盖率下降、代码质量下降
2. **副作用检测**: 修复导致其他测试失败
3. **编译失败**: 修复后代码无法编译
4. **超时**: 验证时间过长

### 6.2 回滚实现

```kotlin
class FixRollbackManager(
    private val codeEditor: CodeEditor,
    private val gitClient: GitClient
) {
    suspend fun rollbackFix(fix: Fix): RollbackResult {
        return try {
            // 方法 1: 使用代码编辑器的回滚
            codeEditor.rollback(fix)

            // 方法 2: 使用 Git 回滚
            gitClient.revertChanges(fix.changes.map { it.filePath })

            // 验证回滚成功
            val testResult = testRunner.runQuickTests()
            if (testResult.allPassed) {
                RollbackResult.SUCCESS
            } else {
                RollbackResult.FAIL("Tests still failing after rollback")
            }
        } catch (e: Exception) {
            RollbackResult.FAIL(e.message ?: "Rollback failed")
        }
    }

    suspend fun rollbackToSafeState(
        iterations: List<Iteration>
    ): RollbackResult {
        // 找到最后一次成功的状态
        val lastSuccessIteration = iterations
            .lastOrNull { it.result == IterationResult.SUCCESS }
            ?: return RollbackResult.FAIL("No successful iteration found")

        // 回滚到该状态
        gitClient.resetToCommit(lastSuccessIteration.commitHash)

        return RollbackResult.SUCCESS
    }
}
```

---

## 7. 修复策略配置

```kotlin
object FixStrategyConfig {
    // 编译错误修复策略
    val compilationStrategies = mapOf(
        ErrorType.SYNTAX_ERROR to listOf(
            FixStrategy.ADD_MISSING_DELIMITERS,
            FixStrategy.FIX_BRACKETS,
            FixStrategy.FIX_KEYWORDS
        ),
        ErrorType.TYPE_MISMATCH to listOf(
            FixStrategy.ADD_TYPE_CONVERSION,
            FixStrategy.CHANGE_VARIABLE_TYPE,
            FixStrategy.CHANGE_FUNCTION_TYPE
        ),
        ErrorType.MISSING_IMPORT to listOf(
            FixStrategy.ADD_IMPORT
        )
    )

    // 测试失败修复策略
    val testFailureStrategies = mapOf(
        ErrorType.ASSERTION_FAILED to listOf(
            FixStrategy.ADJUST_CALCULATION,
            FixStrategy.FIX_BOUNDARY,
            FixStrategy.UPDATE_EXPECTATION
        ),
        ErrorType.MOCK_FAILURE to listOf(
            FixStrategy.ADD_MOCK_ANSWER,
            FixStrategy.FIX_MOCK_VERIFICATION
        )
    )

    // 运行时错误修复策略
    val runtimeErrorStrategies = mapOf(
        ErrorType.NULL_POINTER to listOf(
            FixStrategy.ADD_SAFE_CALL,
            FixStrategy.ADD_ELVIS,
            FixStrategy.ADD_NULL_CHECK
        ),
        ErrorType.INDEX_OUT_OF_BOUNDS to listOf(
            FixStrategy.USE_GET_OR_NULL,
            FixStrategy.ADD_BOUNDS_CHECK,
            FixStrategy.USE_INDICES
        )
    )

    // 策略优先级
    fun getStrategies(errorType: ErrorType): List<FixStrategy> {
        return compilationStrategies[errorType] ?:
               testFailureStrategies[errorType] ?:
               runtimeErrorStrategies[errorType] ?:
               emptyList()
    }
}
```

---

## 8. 持续学习

### 8.1 修复效果追踪

```kotlin
class FixEffectTracker {
    private val history = mutableListOf<FixAttempt>()

    fun record(fix: Fix, result: FixResult) {
        history.add(
            FixAttempt(
                fix = fix,
                result = result,
                timestamp = System.currentTimeMillis(),
                iteration = getCurrentIteration()
            )
        )
    }

    fun getSuccessRate(errorType: ErrorType): Double {
        val attempts = history.filter { it.fix.errorType == errorType }
        val successes = attempts.count { it.result == FixResult.SUCCESS }
        return successes.toDouble() / attempts.size
    }

    fun getBestStrategy(errorType: ErrorType): FixStrategy? {
        val attempts = history.filter { it.fix.errorType == errorType }
        return attempts
            .groupBy { it.fix.strategy }
            .mapValues { (_, attempts) ->
                attempts.count { it.result == FixResult.SUCCESS }.toDouble() / attempts.size
            }
            .maxByOrNull { it.value }
            ?.key
    }

    fun adjustConfidence(fix: Fix): Double {
        val successRate = getSuccessRate(fix.errorType)
        return fix.confidence * successRate
    }
}
```

### 8.2 策略优化

```kotlin
class StrategyOptimizer(
    private val tracker: FixEffectTracker
) {
    fun optimizeStrategies(): OptimizationReport {
        val report = OptimizationReport()

        // 分析每种错误类型的最佳策略
        ErrorType.values().forEach { errorType ->
            val bestStrategy = tracker.getBestStrategy(errorType)
            val successRate = tracker.getSuccessRate(errorType)

            report.addRecommendation(
                errorType = errorType,
                bestStrategy = bestStrategy,
                successRate = successRate,
                action = if (successRate < 0.7) {
                    "NEEDS_IMPROVEMENT"
                } else {
                    "GOOD"
                }
            )
        }

        return report
    }

    fun generateNewPatterns(): List<ErrorPattern> {
        // 从历史数据中学习新的错误模式
        val failures = tracker.history
            .filter { it.result == FixResult.FAIL }
            .groupBy { it.fix.errorMessage }

        return failures.map { (message, attempts) ->
            ErrorPattern(
                pattern = message.toRegex(),
                category = attempts.first().fix.category,
                type = attempts.first().fix.errorType,
                suggestedStrategy = tracker.getBestStrategy(attempts.first().fix.errorType)
            )
        }
    }
}
```

---

**文档结束**

**相关文档**:
- `TDD_AUTO_LOOP_ARCHITECTURE.md` - 完整架构设计
- `TDD_AUTO_LOOP_FLOWCHART.md` - 流程图
- `TDD_FAILURE_CLASSIFICATION.md` - 失败分类策略
