# TDD Auto-Loop 流程图

**Document Version**: 1.0
**Last Updated**: 2026-02-28
**Author**: android-test-engineer

本文档提供 TDD Auto-Loop 系统的各种流程图，用于理解系统的工作流程和决策逻辑。

---

## 1. 主循环流程图

```mermaid
flowchart TD
    Start([开始: 功能需求]) --> Init[初始化 Auto-Loop Controller]

    Init --> Phase1{首次迭代?}

    Phase1 -->|是| GenerateTests[生成测试用例<br/>TestGenerator]
    Phase1 -->|否| RunTests[运行测试<br/>Gradle test]

    GenerateTests --> RunTests

    RunTests --> CheckResult{测试结果?}

    CheckResult -->|全部通过| QualityGate[进入质量门禁<br/>QualityGate]
    CheckResult -->|失败| Analyze[分析失败原因<br/>FailureAnalyzer]

    Analyze --> ClassifyError{错误分类}

    ClassifyError -->|编译错误| CompileFix[生成编译修复<br/>FixGenerator]
    ClassifyError -->|测试失败| TestFix[生成逻辑修复<br/>FixGenerator]
    ClassifyError -->|运行时错误| RuntimeFix[生成运行时修复<br/>FixGenerator]

    CompileFix --> ApplyFix[应用修复]
    TestFix --> ApplyFix
    RuntimeFix --> ApplyFix

    ApplyFix --> CheckLimits{检查限制}

    CheckLimits -->|达到最大迭代次数| HumanHelp[请求人工介入]
    CheckLimits -->|超时| HumanHelp
    CheckLimits -->|需要人工决策| HumanHelp
    CheckLimits -->|可以继续| Increment[迭代次数 +1]

    Increment --> RunTests

    QualityGate --> CheckQuality{质量门禁}

    CheckQuality -->|覆盖率 < 80%| AddTests[补充测试用例]
    CheckQuality -->|代码质量失败| FixQuality[修复质量问题]
    CheckQuality -->|架构违规| FixArch[修复架构问题]
    CheckQuality -->|全部通过| Success([成功: 功能完成])

    AddTests --> RunTests
    FixQuality --> RunTests
    FixArch --> RunTests

    HumanHelp --> ManualReview[人工审查和修复]
    ManualReview --> Continue{是否继续?}

    Continue -->|是| RunTests
    Continue -->|否| Failed([失败: 需要人工处理])

    style Start fill:#90EE90
    style Success fill:#90EE90
    style Failed fill:#FFB6C1
    style HumanHelp fill:#FFD700
    style CheckQuality fill:#87CEEB
    style Analyze fill:#DDA0DD
```

---

## 2. 失败分析决策树

```mermaid
flowchart TD
    AnalyzeStart([开始失败分析]) --> ParseOutput[解析测试输出<br/>Gradle XML report]

    ParseOutput --> ErrorType{错误类型判断}

    ErrorType -->|BUILD FAILED| CompileError[编译错误]
    ErrorType -->|TEST FAILED| TestError[测试失败]
    ErrorType -->|RUNTIME EXCEPTION| RuntimeError[运行时错误]

    CompileError --> CompileDetail{详细分析}

    CompileDetail -->|expecting| SyntaxError[语法错误]
    CompileDetail -->|Type mismatch| TypeError[类型不匹配]
    CompileDetail -->|Unresolved reference| ImportError[缺失导入]
    CompileDetail -->|Cannot access| VisibilityError[可见性错误]
    CompileDetail -->|Other| OtherCompile[其他编译问题]

    SyntaxError --> CompileFixable{可自动修复?}
    TypeError --> CompileFixable
    ImportError --> CompileFixable
    VisibilityError --> CompileFixable
    OtherCompile --> CompileFixable

    CompileFixable -->|是| AutoCompileFix[生成自动修复]
    CompileFixable -->|否| SuggestCompile[提供修复建议]

    TestError --> TestDetail{详细分析}

    TestDetail -->|AssertionViolation| AssertionFail[断言失败]
    TestDetail -->|Expected exception| ExceptionFail[异常测试失败]
    TestDetail -->|Unexpected exception| UnexpectedException[意外异常]
    TestDetail -->|Mock error| MockFail[Mock 失败]
    TestDetail -->|Timeout| TimeoutFail[超时]

    AssertionFail --> AssertionAnalysis[断言分析]
    ExceptionFail --> ExceptionAnalysis[异常分析]
    UnexpectedException --> ExceptionAnalysis
    MockFail --> MockAnalysis[Mock 分析]
    TimeoutFail --> TimeoutAnalysis[超时分析]

    AssertionAnalysis --> TestFixable{可自动修复?}
    ExceptionAnalysis --> TestFixable
    MockAnalysis --> TestFixable
    TimeoutAnalysis --> TestFixable

    TestFixable -->|是| AutoTestFix[生成自动修复]
    TestFixable -->|否| SuggestTest[提供修复建议]

    RuntimeError --> RuntimeDetail{详细分析}

    RuntimeDetail -->|NullPointerException| NullError[空指针异常]
    RuntimeDetail -->|IndexOutOfBounds| BoundsError[索引越界]
    RuntimeDetail -->|IllegalStateException| StateError[状态异常]
    RuntimeDetail -->|Concurrency| ConcurrencyError[并发问题]

    NullError --> NullAnalysis[空指针分析]
    BoundsError --> BoundsAnalysis[越界分析]
    StateError --> StateAnalysis[状态分析]
    ConcurrencyError --> ConcurrencyAnalysis[并发分析]

    NullAnalysis --> RuntimeFixable{可自动修复?}
    BoundsAnalysis --> RuntimeFixable
    StateAnalysis --> RuntimeFixable
    ConcurrencyAnalysis --> RuntimeFixable

    RuntimeFixable -->|是| AutoRuntimeFix[生成自动修复]
    RuntimeFixable -->|否| SuggestRuntime[提供修复建议]

    AutoCompileFix --> GenerateFix[生成修复方案]
    SuggestCompile --> GenerateFix
    AutoTestFix --> GenerateFix
    SuggestTest --> GenerateFix
    AutoRuntimeFix --> GenerateFix
    SuggestRuntime --> GenerateFix

    GenerateFix --> AnalyzeEnd([分析完成])

    style AnalyzeStart fill:#90EE90
    style AnalyzeEnd fill:#90EE90
    style ErrorType fill:#FFD700
    style CompileFixable fill:#87CEEB
    style TestFixable fill:#87CEEB
    style RuntimeFixable fill:#87CEEB
```

---

## 3. 质量门禁流程图

```mermaid
flowchart TD
    QualityStart([开始质量门禁]) --> AllTestsPass{所有测试通过?}

    AllTestsPass -->|否| FixTests[修复测试失败]
    AllTestsPass -->|是| Coverage[检查覆盖率<br/>JaCoCo report]

    Coverage --> CoverageThreshold{覆盖率 ≥ 80%?}

    CoverageThreshold -->|否| AnalyzeCoverage[分析未覆盖代码]
    CoverageThreshold -->|是| CodeQuality[运行代码质量检查<br/>Detekt + KtLint]

    AnalyzeCoverage --> IdentifyGaps[识别未覆盖分支]
    IdentifyGaps --> AddTests[添加测试用例]
    AddTests --> AllTestsPass

    CodeQuality --> QualityCheck{代码质量通过?}

    QualityCheck -->|KtLint 失败| FixFormat[修复格式问题<br/>ktlintFormat]
    QualityCheck -->|Detekt 失败| AnalyzeDetekt[分析 Detekt 问题]
    QualityCheck -->|通过| Architecture[检查架构合规]

    FixFormat --> VerifyQuality{重新验证}
    VerifyQuality -->|通过| Architecture
    VerifyQuality -->|失败| QualityCheck

    AnalyzeDetekt --> DetektSeverity{严重程度?}

    DetektSeverity -->|Critical| FixCritical[修复 Critical 问题]
    DetektSeverity -->|Major| CheckMajor{Major 问题数量?}
    DetektSeverity -->|Minor| WarningMinor[警告 Minor 问题]

    CheckMajor -->|> 5| FixMajor[修复 Major 问题]
    CheckMajor -->|≤ 5| LogMajor[记录 Major 问题<br/>申请例外]

    FixCritical --> QualityCheck
    FixMajor --> QualityCheck
    WarningMajor --> Architecture
    LogMajor --> Architecture

    Architecture --> LayerCheck{层次合规检查}

    LayerCheck -->|UI 访问 Data| FixLayerViolation[修复层次违规]
    LayerCheck -->|Domain 依赖 UI| FixLayerViolation
    LayerCheck -->|通过| DICheck{依赖注入检查}

    FixLayerViolation --> Refactor[重构代码]
    Refactor --> Architecture

    DICheck --> HiltCheck{Hilt 使用正确?}

    HiltCheck -->|字段注入| FixDI[修复为构造函数注入]
    HiltCheck -->|缺少注解| AddAnnotations[添加 Hilt 注解]
    HiltCheck -->|通过| NamingCheck{命名规范检查}

    FixDI --> VerifyQuality
    AddAnnotations --> VerifyQuality

    NamingCheck --> PackageCheck{包结构合规?}

    PackageCheck -->|层次分包| RefactorPackage[重构为功能分包]
    PackageCheck -->|通过| QualityPass([质量门禁通过])

    RefactorPackage --> NamingCheck

    FixTests --> AllTestsPass

    style QualityStart fill:#90EE90
    style QualityPass fill:#90EE90
    style FixTests fill:#FFB6C1
    style CoverageThreshold fill:#FFD700
    style QualityCheck fill:#87CEEB
    style DetektSeverity fill:#FFA500
```

---

## 4. 修复生成流程图

```mermaid
flowchart TD
    FixStart([开始修复生成]) --> LoadAnalysis[加载失败分析结果]

    LoadAnalysis --> DetermineStrategy{确定修复策略}

    DetermineStrategy -->|编译错误| CompileStrategy[编译错误修复策略]
    DetermineStrategy -->|测试失败| TestStrategy[测试失败修复策略]
    DetermineStrategy -->|运行时错误| RuntimeStrategy[运行时错误修复策略]

    CompileStrategy --> CompilePatterns[匹配编译错误模式]

    CompilePatterns --> CompileMatch{模式匹配?}

    CompileMatch -->|语法错误| FixSyntax[修正语法]
    CompileMatch -->|类型不匹配| FixType[修复类型]
    CompileMatch -->|缺失导入| AddImport[添加导入]
    CompileMatch -->|可见性| FixVisibility[修复可见性]

    FixSyntax --> GenerateCompileFix[生成修复代码]
    FixType --> GenerateCompileFix
    AddImport --> GenerateCompileFix
    FixVisibility --> GenerateCompileFix

    TestStrategy --> TestPatterns[匹配测试失败模式]

    TestPatterns --> TestMatch{模式匹配?}

    TestMatch -->|断言失败| AnalyzeAssertion[分析断言]
    TestMatch -->|异常失败| AnalyzeException[分析异常]
    TestMatch -->|Mock 失败| AnalyzeMock[分析 Mock]
    TestMatch -->|超时| AnalyzeTimeout[分析超时]

    AnalyzeAssertion --> AssertionFix{断言修复策略}
    AnalyzeException --> ExceptionFix{异常修复策略}
    AnalyzeMock --> MockFix{Mock 修复策略}
    AnalyzeTimeout --> TimeoutFix{超时修复策略}

    AssertionFix -->|值计算错误| FixCalculation[修正计算逻辑]
    AssertionFix -->|边界错误| FixBoundary[修正边界条件]
    AssertionFix -->|预期错误| FixExpectation[修正预期值]

    ExceptionFix -->|缺少验证| AddValidation[添加参数校验]
    ExceptionFix -->|状态错误| FixState[修正状态管理]
    ExceptionFix -->|依赖错误| FixDependency[修复依赖配置]

    MockFix -->|配置错误| FixMockConfig[修正 Mock 配置]
    MockFix -->|存根错误| FixStub[修正 Mock 存根]

    TimeoutFix -->|无限循环| FixLoop[修正循环条件]
    TimeoutFix -->|死锁| FixLock[修正锁机制]

    FixCalculation --> GenerateTestFix[生成修复代码]
    FixBoundary --> GenerateTestFix
    FixExpectation --> GenerateTestFix
    AddValidation --> GenerateTestFix
    FixState --> GenerateTestFix
    FixDependency --> GenerateTestFix
    FixMockConfig --> GenerateTestFix
    FixStub --> GenerateTestFix
    FixLoop --> GenerateTestFix
    FixLock --> GenerateTestFix

    RuntimeStrategy --> RuntimePatterns[匹配运行时错误模式]

    RuntimePatterns --> RuntimeMatch{模式匹配?}

    RuntimeMatch -->|空指针| FixNull[修复空指针]
    RuntimeMatch -->|越界| FixBounds[修复越界]
    RuntimeMatch -->|状态异常| FixRuntimeState[修复状态]
    RuntimeMatch -->|并发| FixConcurrency[修复并发]

    FixNull --> NullStrategy{空指针修复策略}
    FixBounds --> BoundsStrategy{越界修复策略}
    FixRuntimeState --> StateStrategy{状态修复策略}
    FixConcurrency --> ConcurrencyStrategy{并发修复策略}

    NullStrategy -->|安全调用| AddSafeCall[添加 ?. 操作符]
    NullStrategy -->|默认值| AddDefault[添加默认值]
    NullStrategy -->|Elvis| AddElvis[添加 ?: 操作符]
    NullStrategy -->|断言| AddAssert[添加 !! 断言]

    BoundsStrategy -->|索引错误| FixIndex[修正索引]
    BoundsStrategy -->|循环错误| FixLoopRange[修正循环范围]

    StateStrategy -->|初始化| AddInit[添加初始化]
    StateStrategy -->|顺序| FixOrder[修正执行顺序]

    ConcurrencyStrategy -->|竞态| AddSync[添加同步机制]
    ConcurrencyStrategy -->|死锁| RemoveLock[移除或修复锁]

    AddSafeCall --> GenerateRuntimeFix
    AddDefault --> GenerateRuntimeFix
    AddElvis --> GenerateRuntimeFix
    AddAssert --> GenerateRuntimeFix
    FixIndex --> GenerateRuntimeFix
    FixLoopRange --> GenerateRuntimeFix
    AddInit --> GenerateRuntimeFix
    FixOrder --> GenerateRuntimeFix
    AddSync --> GenerateRuntimeFix
    RemoveLock --> GenerateRuntimeFix

    GenerateCompileFix --> ValidateFix{验证修复}
    GenerateTestFix --> ValidateFix
    GenerateRuntimeFix --> ValidateFix

    ValidateFix --> SafetyCheck{安全检查}

    SafetyCheck -->|通过| ApplyFix[应用修复]
    SafetyCheck -->|风险高| RequestConfirm[请求确认]

    RequestConfirm --> Confirmed{用户确认?}

    Confirmed -->|是| ApplyFix
    Confirmed -->|否| ManualFix[手动修复]

    ApplyFix --> TestFix[测试修复]

    TestFix --> FixSuccess{修复成功?}

    FixSuccess -->|是| FixEnd([修复完成])
    FixSuccess -->|否| Rollback[回滚修复]

    Rollback --> NextCandidate{有其他候选修复?}

    NextCandidate -->|是| DetermineStrategy
    NextCandidate -->|否| FailedFix([修复失败])

    ManualFix --> FailedFix

    style FixStart fill:#90EE90
    style FixEnd fill:#90EE90
    style FailedFix fill:#FFB6C1
    style ValidateFix fill:#FFD700
    style SafetyCheck fill:#FFA500
```

---

## 5. 状态机图

```mermaid
stateDiagram-v2
    [*] --> Initializing: Start

    Initializing --> GeneratingTests: First iteration

    GeneratingTests --> RunningTests: Tests generated

    RunningTests --> CheckingQualityGate: All tests pass
    RunningTests --> AnalyzingFailure: Tests failed

    CheckingQualityGate --> Completed: Quality gate pass
    CheckingQualityGate --> GeneratingTests: Add more tests
    CheckingQualityGate --> ApplyingFix: Fix quality issues

    AnalyzingFailure --> GeneratingFix: Analysis complete
    AnalyzingFailure --> ManualIntervention: Cannot auto-fix

    GeneratingFix --> ApplyingFix: Fix generated

    ApplyingFix --> RunningTests: Fix applied
    ApplyingFix --> ManualIntervention: Fix failed

    ManualIntervention --> Completed: Manual fix complete
    ManualIntervention --> RunningTests: Retry

    Completed --> [*]: Success

    note right of RunningTests
        Run tests and collect results
        Check for compilation errors
        Check for test failures
        Check for runtime errors
    end note

    note right of AnalyzingFailure
        Classify error type
        Determine fix strategy
        Generate fix suggestions
    end note

    note right of CheckingQualityGate
        Coverage ≥ 80%
        KtLint pass
        Detekt pass
        Architecture compliance
    end note
```

---

## 6. 时序图

```mermaid
sequenceDiagram
    participant User as 用户/需求
    participant Controller as Auto-Loop Controller
    participant TestGen as Test Generator
    participant Gradle as Gradle
    participant Analyzer as Failure Analyzer
    participant FixGen as Fix Generator
    participant QualityGate as Quality Gate
    participant Repo as 代码仓库

    User->>Controller: 提交功能需求

    Controller->>TestGen: 生成测试用例
    TestGen-->>Controller: 测试文件创建

    Controller->>Gradle: 运行测试
    Gradle-->>Controller: 测试结果

    alt 测试失败
        Controller->>Analyzer: 分析失败原因
        Analyzer-->>Controller: 失败分析报告

        Controller->>FixGen: 生成修复方案
        FixGen-->>Controller: 修复代码

        Controller->>Repo: 应用修复
        Repo-->>Controller: 修复已应用

        Controller->>Gradle: 重新运行测试
    else 测试通过
        Controller->>QualityGate: 质量门禁检查

        alt 质量门禁失败
            QualityGate-->>Controller: 质量问题报告
            Controller->>FixGen: 生成质量修复
        else 质量门禁通过
            QualityGate-->>Controller: 质量验证通过
            Controller-->>User: 功能完成
        end
    end

    note over Controller,Repo
        最多迭代 10 次
        超时 30 分钟
        失败则请求人工介入
    end note
```

---

## 7. 组件交互图

```mermaid
graph LR
    subgraph "TDD Auto-Loop System"
        Controller[Auto-Loop Controller]

        subgraph "核心组件"
            TestGen[Test Generator]
            Analyzer[Failure Analyzer]
            FixGen[Fix Generator]
            QualityGate[Quality Gate]
        end

        subgraph "外部工具"
            Gradle[Gradle]
            JaCoCo[JaCoCo]
            Detekt[Detekt]
            KtLint[KtLint]
            Git[Git]
        end

        subgraph "数据源"
            Template[Test Template]
            Code[Code Base]
            Reports[Test Reports]
        end
    end

    Controller --> TestGen
    Controller --> Analyzer
    Controller --> FixGen
    Controller --> QualityGate

    TestGen --> Template
    TestGen --> Code

    Analyzer --> Reports
    Analyzer --> Gradle

    FixGen --> Code
    FixGen --> Git

    QualityGate --> JaCoCo
    QualityGate --> Detekt
    QualityGate --> KtLint

    Gradle --> Reports
    Gradle --> Code

    style Controller fill:#FFD700
    style TestGen fill:#90EE90
    style Analyzer fill:#87CEEB
    style FixGen fill:#DDA0DD
    style QualityGate fill:#FFA500
```

---

## 8. 错误处理流程图

```mermaid
flowchart TD
    ErrorStart([检测到错误]) --> Categorize{错误分类}

    Categorize -->|可恢复| Recoverable[可恢复错误]
    Categorize -->|不可恢复| Unrecoverable[不可恢复错误]
    Categorize -->|未知| Unknown[未知错误]

    Recoverable --> Retry{已重试次数?}

    Retry -->|< 3| AutoRecover[自动恢复]
    Retry -->|≥ 3| RequestHelp[请求帮助]

    AutoRecover --> ApplyAutoFix[应用自动修复]
    ApplyAutoFix --> VerifyAutoFix{修复成功?}

    VerifyAutoFix -->|是| ContinueLoop[继续循环]
    VerifyAutoFix -->|否| Retry

    RequestHelp --> LogError[记录错误]
    LogError --> NotifyUser[通知用户]

    Unrecoverable --> Abort[中止循环]
    Abort --> SaveState[保存状态]
    SaveState --> ReportError[报告错误]

    Unknown --> AnalyzeUnknown[分析未知错误]
    AnalyzeUnknown --> UpdatePatterns{更新错误模式?}

    UpdatePatterns -->|是| LearnPattern[学习新模式]
    UpdatePatterns -->|否| TreatAsUnrecoverable[按不可恢复处理]

    LearnPattern --> RequestHelp
    TreatAsUnrecoverable --> Abort

    NotifyUser --> UserDecision{用户决策?}

    UserDecision -->|手动修复| ManualFix[手动修复]
    UserDecision -->|跳过| SkipLoop[跳过当前循环]
    UserDecision -->|中止| AbortLoop[中止整个循环]

    ManualFix --> ContinueLoop
    SkipLoop --> ContinueLoop
    AbortLoop --> SaveState

    ContinueLoop --> ErrorEnd([错误处理完成])

    style ErrorStart fill:#FFB6C1
    style ErrorEnd fill:#90EE90
    style Recoverable fill:#90EE90
    style Unrecoverable fill:#FFB6C1
    style Unknown fill:#FFD700
    style Abort fill:#FFB6C1
```

---

## 9. 覆盖率优化流程图

```mermaid
flowchart TD
    CoverageStart([开始覆盖率优化]) --> MeasureCoverage[测量当前覆盖率<br/>JaCoCo report]

    MeasureCoverage --> CheckThreshold{覆盖率 ≥ 80%?}

    CheckThreshold -->|是| CoveragePass([覆盖率达标])
    CheckThreshold -->|否| AnalyzeGaps[分析覆盖缺口]

    AnalyzeGaps --> IdentifyUncovered[识别未覆盖代码]

    IdentifyUncovered --> ClassifyUncovered{分类未覆盖代码}

    ClassifyUncovered -->|主路径| MissingMain[缺失主路径测试]
    ClassifyUncovered -->|边界条件| MissingEdge[缺失边界测试]
    ClassifyUncovered -->|错误处理| MissingError[缺失错误处理测试]
    ClassifyUncovered -->|死代码| DeadCode[标记死代码]

    MissingMain --> GenerateMainTests[生成主路径测试]
    MissingEdge --> GenerateEdgeTests[生成边界测试]
    MissingError --> GenerateErrorTests[生成错误处理测试]

    DeadCode --> RemoveDead[移除死代码]
    RemoveDead --> DocumentRemoval[文档化移除原因]

    GenerateMainTests --> AddTestsToSuite[添加到测试套件]
    GenerateEdgeTests --> AddTestsToSuite
    GenerateErrorTests --> AddTestsToSuite

    AddTestsToSuite --> RunNewTests[运行新测试]

    RunNewTests --> NewTestsPass{新测试通过?}

    NewTestsPass -->|是| ReMeasureCoverage[重新测量覆盖率]
    NewTestsPass -->|否| FixNewTests[修复新测试]

    FixNewTests --> RunNewTests

    ReMeasureCoverage --> CheckThreshold

    CoveragePass --> ReportCoverage[报告覆盖率]
    ReportCoverage --> CoverageEnd([覆盖率优化完成])

    style CoverageStart fill:#90EE90
    style CoveragePass fill:#90EE90
    style CoverageEnd fill:#90EE90
    style CheckThreshold fill:#FFD700
    style DeadCode fill:#FFB6C1
```

---

## 10. 使用指南

### 10.1 如何使用这些流程图

1. **理解系统**: 从"主循环流程图"开始，了解整个 TDD Auto-Loop 的工作流程
2. **深入分析**: 查看"失败分析决策树"，理解错误如何被分类和分析
3. **质量保证**: 参考"质量门禁流程图"，了解质量检查的完整流程
4. **修复机制**: 查看"修复生成流程图"，理解自动修复如何工作
5. **状态管理**: 使用"状态机图"理解系统的各种状态转换
6. **交互流程**: 查看"时序图"和"组件交互图"，理解组件间的协作

### 10.2 流程图维护

- **更新频率**: 每次架构变更后更新
- **责任人**: android-architect + android-test-engineer
- **审核周期**: 每个 Sprint 审核 1 次
- **版本控制**: 使用 Git 追踪所有变更

---

**文档结束**

**相关文档**:
- `TDD_AUTO_LOOP_ARCHITECTURE.md` - 完整架构设计
- `TDD_FAILURE_CLASSIFICATION.md` - 失败分类策略
- `TDD_FIX_STRATEGIES.md` - 修复策略文档
