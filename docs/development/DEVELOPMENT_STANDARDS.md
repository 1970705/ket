# Wordland 项目开发规范

**版本**: 1.0
**生效日期**: 2026-02-16
**适用范围**: Wordland Android 项目

---

## 📋 目录

1. [文件组织规范](#文件组织规范)
2. [文档命名规范](#文档命名规范)
3. [代码组织规范](#代码组织规范)
4. [脚本组织规范](#脚本组织规范)
5. [文档编写规范](#文档编写规范)
6. [测试规范](#测试规范)
7. [Git 提交规范](#git-提交规范)

---

## 文件组织规范

### 项目根目录

根目录应该只包含必要的配置文件和入口文档，**不应该**包含大量的报告、测试脚本或临时文档。

**允许的文件**:
```
wordland/
├── CLAUDE.md                    # 项目说明（AI 助手使用）
├── README.md                    # 项目简介（面向用户/开发者）
├── LICENSE                      # 许可证
├── .gitignore                   # Git 忽略规则
├── build.gradle.kts             # 构建配置
├── settings.gradle.kts          # 项目设置
├── gradle.properties            # Gradle 属性
└── app/                         # 应用模块
```

**禁止的文件**:
- ❌ 测试脚本（应该放在 `scripts/`）
- ❌ 报告文档（应该放在 `docs/reports/`）
- ❌ 临时总结（应该放在 `.claude/team/history/` 或删除）
- ❌ 中间产物文档（应该删除或归档）

### 目录结构

```
wordland/
├── CLAUDE.md                    # 项目主文档
├── README.md                    # 项目简介
│
├── docs/                        # 文档目录
│   ├── README.md                # 文档导航
│   ├── development/             # 开发文档
│   │   ├── DEVELOPMENT_STANDARDS.md  # 本文档
│   │   ├── ARCHITECTURE.md           # 架构设计
│   │   ├── CODING_CONVENTIONS.md     # 编码规范
│   │   └── GETTING_STARTED.md        # 快速开始
│   │
│   ├── guides/                  # 操作指南
│   │   ├── DEVICE_TESTING_GUIDE.md   # 设备测试指南
│   │   ├── CRASH_DIAGNOSIS_GUIDE.md  # 崩溃诊断指南
│   │   └── MANUAL_TESTING_GUIDE.md   # 手动测试指南
│   │
│   ├── reports/                 # 报告文档
│   │   ├── testing/             # 测试报告
│   │   │   ├── UNIT_TEST_REPORT.md
│   │   │   ├── INTEGRATION_TEST_REPORT.md
│   │   │   └── REAL_DEVICE_TEST_REPORT.md
│   │   ├── architecture/        # 架构报告
│   │   │   └── ARCHITECTURE_AUDIT.md
│   │   └── issues/              # 问题报告
│   │       ├── HILT_CRASH_ANALYSIS.md
│   │       └── FIX_REPORTS.md
│   │
│   ├── history/                 # 历史文档（归档）
│   │   ├── implementation/      # 实现总结
│   │   │   ├── WEEK_1_SUMMARY.md
│   │   │   ├── WEEK_2_SUMMARY.md
│   │   │   └── ...
│   │   └── milestones/          # 里程碑
│   │       ├── MINIMUM_PROTOTYPE.md
│   │       └── INTEGRATION_COMPLETE.md
│   │
│   └── adr/                     # 架构决策记录
│       ├── 001-use-service-locator.md
│       ├── 002-hilt-compatibility.md
│       └── ...
│
├── scripts/                     # 脚本目录
│   ├── build/                   # 构建脚本
│   │   ├── build.sh
│   │   └── install-apk.sh
│   │
│   ├── test/                    # 测试脚本
│   │   ├── run-unit-tests.sh
│   │   ├── run-integration-tests.sh
│   │   ├── test-navigation.sh
│   │   ├── test-gameplay.sh
│   │   └── test-real-device.sh
│   │
│   └── utils/                   # 工具脚本
│       ├── diagnose-crash.sh
│       └── check-device.sh
│
├── app/                         # 应用代码
│   ├── src/
│   └── build.gradle.kts
│
└── docs-reports-archive/        # 旧报告归档（临时）
    └── [所有旧报告移动到这里]
```

---

## 文档命名规范

### 基本原则

1. **使用英文文件名**：便于跨平台兼容
2. **使用大写蛇形命名**：`DOCUMENT_NAME.md`
3. **名称应描述内容**：而不是 `report1.md`, `final_report.md`
4. **避免版本号**：使用 Git 管理版本

### 命名模式

#### 报告文档
```
{TYPE}_{SUBJECT}_{DATE}.md

示例:
- UNIT_TEST_REPORT_2026-02-16.md
- REAL_DEVICE_TEST_REPORT_2026-02-16.md
- ARCHITECTURE_AUDIT_REPORT.md
```

#### 指南文档
```
{SUBJECT}_GUIDE.md

示例:
- DEVICE_TESTING_GUIDE.md
- CRASH_DIAGNOSIS_GUIDE.md
- MANUAL_TESTING_GUIDE.md
```

#### 架构决策记录（ADR）
```
{NUMBER}-{short-description}.md

示例:
- 001-use-service-locator.md
- 002-hilt-compatibility.md
- 003-migrate-to-koin.md
```

#### 历史文档
```
{PERIOD}_{TYPE}.md

示例:
- WEEK_1_IMPLEMENTATION_SUMMARY.md
- WEEK_2_IMPLEMENTATION_SUMMARY.md
- MONTH_1_PROGRESS_REPORT.md
```

### 禁止的命名

- ❌ `final_report.md` - 什么是"最终"？
- ❌ `report_v1.md`, `report_v2.md` - 使用 Git
- ❌ `temp.md` - 临时文件不应该提交
- ❌ `new_report.md` - 不描述内容
- ❌ `claude1.md`, `claude2.md` - 不清晰

---

## 代码组织规范

### Android 项目结构

遵循标准 Android 项目结构：

```
app/src/main/java/com/wordland/
├── ui/                          # UI 层
│   ├── screens/                 # Compose Screens
│   ├── viewmodel/              # ViewModels
│   ├── uistate/                # UI State 数据类
│   ├── components/             # 可复用组件
│   └── theme/                  # 主题配置
│
├── domain/                     # 领域层
│   ├── model/                  # 领域模型
│   ├── usecase/                # 用例
│   │   └── usecases/          # 具体用例实现
│   └── algorithm/              # 业务算法
│
├── data/                       # 数据层
│   ├── repository/             # 仓库实现
│   ├── dao/                    # Room DAO
│   ├── database/               # Room 数据库
│   ├── assets/                 # 资源管理
│   └── converter/              # 数据转换器
│
├── navigation/                 # 导航配置
│   └── SetupNavGraph.kt
│
├── di/                         # 依赖注入
│   ├── AppServiceLocator.kt
│   └── AppModule.kt
│
└── WordlandApplication.kt      # Application 类
```

### 文件命名

- **Screen**: `{Name}Screen.kt` (例如 `HomeScreen.kt`)
- **ViewModel**: `{Name}ViewModel.kt` (例如 `HomeViewModel.kt`)
- **UiState**: `{Name}UiState.kt` (例如 `HomeUiState.kt`)
- **UseCase**: `{Action}{Subject}UseCase.kt` (例如 `GetUserProgressUseCase.kt`)
- **Repository**: `{Subject}Repository.kt` (接口), `{Subject}RepositoryImpl.kt` (实现)

---

## 脚本组织规范

### 脚本位置

所有脚本必须放在 `scripts/` 目录下，**不能**放在项目根目录。

### 脚本命名

```bash
{action}-{target}.sh

示例:
- build-apk.sh              # 构建 APK
- install-apk.sh            # 安装 APK
- run-unit-tests.sh         # 运行单元测试
- test-real-device.sh       # 真机测试
- diagnose-crash.sh         # 崩溃诊断
```

### 脚本要求

1. **Shebang**: 必须指定解释器
   ```bash
   #!/bin/bash
   ```

2. **可执行权限**: 提交时设置可执行权限
   ```bash
   chmod +x scripts/build/build-apk.sh
   ```

3. **错误处理**: 设置 `set -e`
   ```bash
   set -e  # 遇到错误立即退出
   ```

4. **文档注释**: 开头添加说明
   ```bash
   #!/bin/bash
   #
   # 构建 Wordland APK
   #
   # 用法: ./scripts/build/build-apk.sh [debug|release]
   #
   # 示例:
   #   ./scripts/build/build-apk.sh debug
   #   ./scripts/build/build-apk.sh release
   ```

5. **使用变量**: 避免硬编码路径
   ```bash
   PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
   ```

---

## 文档编写规范

### Markdown 规范

#### 标题层级

```markdown
# 一级标题（文档标题，每个文档只有一个）
## 二级标题（主要章节）
### 三级标题（子章节）
#### 四级标题（细节说明）
```

#### 文档结构

每个文档应包含：

1. **标题**: 清晰描述文档内容
2. **元数据**: 版本、日期、状态
3. **目录**: 长文档需要目录
4. **章节**: 逻辑清晰的组织
5. **总结**: 结尾总结或下一步

#### 示例模板

```markdown
# 文档标题

**版本**: 1.0
**日期**: 2026-02-16
**状态**: 草稿/审核中/已发布
**作者**: [可选]

---

## 概述

[简要说明文档内容和目的]

## 背景

[为什么需要这个文档/决策/功能]

## 详细内容

### 章节 1
[内容]

### 章节 2
[内容]

## 总结

[总结要点]

## 相关文档

- [相关文档1](path/to/doc1.md)
- [相关文档2](path/to/doc2.md)

---

**最后更新**: 2026-02-16
```

#### 代码块

指定语言以启用语法高亮：

````markdown
```kotlin
class MyViewModel : ViewModel() {
    // ...
}
```

```bash
#!/bin/bash
echo "Hello"
```
````

#### 链接

使用相对链接（便于移动文件）：

```markdown
❌ 错误: 绝对路径
[/Users/panshan/git/ai/ket/docs/ARCHITECTURE.md](/Users/panshan/git/ai/ket/docs/ARCHITECTURE.md)

✅ 正确: 相对路径
[架构设计](./ARCHITECTURE.md)
```

#### 列表

使用 `-` 作为无序列表标记：

```markdown
- 项目 1
- 项目 2
  - 子项目 2.1
  - 子项目 2.2
- 项目 3
```

---

## 测试规范

### 测试脚本组织

```
scripts/test/
├── run-unit-tests.sh           # 运行所有单元测试
├── run-integration-tests.sh    # 运行集成测试
├── test-navigation.sh          # 导航功能测试
├── test-gameplay.sh            # 游戏流程测试
└── test-real-device.sh         # 真机完整测试
```

### 测试报告

测试报告必须包含：

1. **测试概述**: 测试目标、范围
2. **测试环境**: 设备、SDK 版本
3. **测试结果**: 通过/失败统计
4. **问题清单**: 发现的问题
5. **结论**: 测试是否通过

#### 测试报告模板

```markdown
# {测试类型} 测试报告

**日期**: 2026-02-16
**测试人员**: [姓名]
**测试版本**: [版本号]

---

## 测试概述

- **测试目标**: [测试目标]
- **测试范围**: [测试范围]
- **测试方法**: [手动/自动化]

## 测试环境

- **设备**: [设备名称]
- **Android 版本**: [版本号]
- **APK 版本**: [版本号]

## 测试结果

### 测试统计

- 总测试数: X
- 通过: X
- 失败: X
- 通过率: XX%

### 测试详情

| 测试项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| 测试1  | ...      | ...      | ✅   |
| 测试2  | ...      | ...      | ❌   |

## 问题清单

### 问题 1: [问题描述]

**严重程度**: 高/中/低
**复现步骤**:
1. 步骤1
2. 步骤2

**预期**: [预期行为]
**实际**: [实际行为]

**日志**:
```
[相关日志]
```

## 结论

- ✅ 测试通过
- ❌ 测试未通过，需要修复以下问题: [问题列表]

## 相关文档

- [测试指南](../guides/MANUAL_TESTING_GUIDE.md)

---

**最后更新**: 2026-02-16
```

---

## Git 提交规范

### Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 类型

- `feat`: 新功能
- `fix`: 修复 Bug
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构（不是新功能也不是修复）
- `test`: 添加测试
- `chore`: 构建过程或辅助工具变动
- `perf`: 性能优化

#### Scope 范围

- `ui`: UI 层
- `domain`: 领域层
- `data`: 数据层
- `navigation`: 导航
- `di`: 依赖注入
- `docs`: 文档
- `scripts`: 脚本
- `tests`: 测试

#### 示例

```bash
# 新功能
feat(ui): add island map screen

# 修复 Bug
fix(data): fix word repository crash on null id

# 文档更新
docs: update development standards

# 重构
refactor(domain): extract business logic to UseCases

# 测试
test(gameplay): add integration test for level completion

# 脚本
scripts(test): add real device test script
```

---

## 文档生命周期

### 文档状态

1. **草稿** (Draft): 初稿，待审核
2. **审核中** (In Review): 正在审核
3. **已发布** (Published): 正式发布，当前有效
4. **已废弃** (Deprecated): 已被新文档替代
5. **归档** (Archived): 历史文档，仅供参考

### 文档归档

当文档过时或项目完成阶段性工作后：

1. **更新主文档**: 将最新信息保留在主文档
2. **归档旧文档**: 移动到 `.claude/team/history/` 或 `docs-reports-archive/`
3. **添加索引**: 在归档目录添加 README 说明内容

### 归档时机

- ✅ 每周实现总结：完成一周后归档到 `docs/history/implementation/`
- ✅ 里程碑报告：完成里程碑后归档到 `docs/history/milestones/`
- ✅ 旧版本报告：新版本发布后归档旧版本
- ❌ 不要归档：当前活跃使用的文档

---

## 代码审查检查清单

### 文档审查

- [ ] 文档位置正确（在合适的目录下）
- [ ] 文档命名符合规范
- [ ] 文档结构完整（标题、目录、内容）
- [ ] 包含元数据（版本、日期、状态）
- [ ] 链接正确（使用相对路径）
- [ ] 代码块指定语言
- [ ] 没有拼写错误和语法错误

### 脚本审查

- [ ] 脚本在 `scripts/` 目录下
- [ ] 命名符合规范
- [ ] 有 Shebang
- [ ] 有文档注释
- [ ] 设置了 `set -e`
- [ ] 使用变量避免硬编码
- [ ] 有可执行权限

---

## 违规处理

### 自动检查

使用以下命令检查常见问题：

```bash
# 检查根目录是否有不应该的文件
check_root_files() {
    echo "检查根目录文件..."
    illegal_files=$(find . -maxdepth 1 -name "*.sh" -o -name "TEST_*.md" -o -name "*REPORT*.md" -o -name "*SUMMARY*.md" | grep -v "CLAUDE.md\|README.md")

    if [ -n "$illegal_files" ]; then
        echo "❌ 发现不应该在根目录的文件:"
        echo "$illegal_files"
        echo "请移动到合适的目录（scripts/ 或 docs/）"
        return 1
    else
        echo "✅ 根目录文件正常"
        return 0
    fi
}

check_root_files
```

### 整理计划

对于现有的违规文件，按照以下步骤整理：

1. **创建新目录结构**
2. **分类文件**
3. **移动文件**
4. **更新链接**
5. **删除重复文件**

---

## 附录

### 常用命令

```bash
# 创建新文档
mkdir -p docs/reports/testing
touch docs/reports/testing/UNIT_TEST_REPORT_$(date +%Y-%m-%d).md

# 添加测试脚本
cat > scripts/test/test-new-feature.sh << 'EOF'
#!/bin/bash
set -e
# 测试新功能
EOF
chmod +x scripts/test/test-new-feature.sh

# 检查文档链接
grep -r "\.md" docs/ | grep -v "^Binary"

# 查找临时文件
find . -name "temp.md" -o -name "tmp.md" -o -name "new_*.md"
```

### 文档模板

见 `docs/templates/` 目录：
- `REPORT_TEMPLATE.md`
- `GUIDE_TEMPLATE.md`
- `ADR_TEMPLATE.md`

---

## 相关文档

- [项目架构](./development/ARCHITECTURE.md)
- [编码规范](./development/CODING_CONVENTIONS.md)
- [快速开始](./development/GETTING_STARTED.md)
- [测试指南](./guides/MANUAL_TESTING_GUIDE.md)

---

**版本历史**:
- v1.0 (2026-02-16): 初始版本

**最后更新**: 2026-02-16
