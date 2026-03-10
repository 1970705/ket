# android-architect 架构观察报告

**日期**: 2026-02-16
**角色**: android-architect (Team Lead)
**领域**: 技术架构设计

---

## 1. 架构完整性评估

### Clean Architecture 状态
✅ **三层架构完整实现**
```
UI Layer (Compose)
    ↓ calls
Domain Layer (UseCase + Model)
    ↓ calls
Data Layer (Repository + DAO + Entity)
```

**各层状态**:
- **UI Layer**: Compose Screens + ViewModels + UiState (sealed classes)
- **Domain Layer**: UseCases + Models + Algorithms + Hint System
- **Data Layer**: Repositories + DAOs + Room Database + Seed Data

### 依赖注入架构
✅ **Hilt 2.48 + Service Locator 混合方案**
- 解决了 Hilt 在真机上的 JavaPoet 兼容性问题
- ViewModels 保留 `@HiltViewModel` 注解便于未来迁移
- Screens 使用 `AppServiceLocator.provideFactory()` 获取实例

### 新增模块
✅ **性能监控模块** (`performance/`)
- `PerformanceMonitor` - 操作计时、帧率监控、内存追踪
- `StartupPerformanceTracker` - 启动阶段追踪
- `ComposePerformanceHelper` - 重组监控
- `ImageLoadingOptimizer` - Coil 图片加载优化

---

## 2. 代码质量评估

### 单元测试状态
| 指标 | 值 | 状态 |
|------|-----|------|
| 总测试数 | 252 | ✅ |
| 通过率 | 100% | ✅ |
| 指令覆盖率 | 12% | ⚠️ |

### 覆盖率细分
| 模块 | 覆盖率 | 状态 |
|------|--------|------|
| data.converter | 100% | ✅ |
| domain.hint | 91% | ✅ |
| domain.model | 82% | ✅ |
| ui.viewmodel | 34% | ⚠️ |
| usecase.usecases | 40% | ⚠️ |
| ui.screens | 0% | ❌ |
| ui.components | 0% | ❌ |
| data.repository | 0% | ❌ |

### 静态分析工具
- ✅ Detekt 配置存在
- ⚠️ 配置使用已废弃属性 (v1.x → v2.x 迁移待完成)
- ✅ KtLint 配置
- ✅ JaCoCo 覆盖率报告

---

## 3. 架构技术债务

### 高优先级
1. **Hint 系统未完整集成**
   - `UseHintUseCaseEnhanced` 已实现
   - `LearningViewModel` 未连接
   - `HintCard` UI 需要更新显示多级提示

2. **星级评分硬编码**
   - 当前: 固定 3 星
   - 需要: 基于用户表现的动态算法

3. **测试覆盖率严重不足**
   - UI 层: 0% 覆盖
   - Repository 层: 0% 覆盖
   - 目标: 80% 整体覆盖率

### 中优先级
4. **Detekt 配置过时**
   - 阻塞 CI/CD pipeline
   - 需要更新到 v2.x 格式

5. **KSP 偶发性编译错误**
   - 需要根本原因分析
   - 临时方案: 定期 `./gradlew --stop`

---

## 4. 下一阶段架构目标

### 短期目标 (本周)
1. **Hint 系统端到端打通**
   - 验证 UseCase → ViewModel → UI 完整链路
   - 真机测试 3 级提示功能

2. **性能基线建立**
   - 真机验证启动时间 <3s
   - 帧率监控 60fps 目标
   - 内存基线记录

3. **恢复 CI/CD**
   - 修复 Detekt 配置
   - 确保所有检查通过

### 中期目标 (下周)
4. **Domain 层测试覆盖提升**
   - 从 12% → 40%
   - 聚焦 UseCases、Hint、Behavior 模块

5. **动态星级评分算法**
   - 实现 `ScoringAlgorithm` UseCase
   - 考虑因素: 准确度、提示使用、时间

---

## 5. 架构风险识别

### 技术风险
| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| Hint 集成破坏现有功能 | 中 | 高 | 先写集成测试，再改代码 |
| Service Locator 扩展性 | 低 | 中 | 持续观察，必要时重构 |
| 性能监控影响运行时 | 低 | 低 | Release 构建通过标志禁用 |
| KSP 编译不稳定 | 中 | 中 | 定期清理 daemon |

### 依赖风险
| 依赖 | 版本 | 风险 | 缓解 |
|------|------|------|------|
| Hilt | 2.48 | JavaPoet 兼容 | Service Locator fallback |
| Room | 2.6.1 | Schema 迁移 | exportSchema=true |
| Compose BOM | 2023.06.01 | 较旧版本 | 计划升级 |

---

## 6. 关键假设

| 假设 | 验证方式 |
|------|----------|
| Hint 架构完整，仅需 UI 集成 | 编写集成测试验证 |
| Domain 层测试 ROI > UI 层 | 覆盖率增量分析 |
| Service Locator 可长期使用 | 持续真机测试 |
| 性能监控可生产禁用 | 条件编译验证 |
| 30 词/5 关卡足够 MVP | 用户反馈循环 |

---

## 7. 对 Plan Agent 计划的建议

### P0 任务调整
1. **Hint 系统集成测试** - 提升至 P0
   - 理由: 架构已完成，应尽快验证

2. **真机性能验证** - 保持 P0
   - 理由: 确保生产环境稳定性

3. **Detekt 配置修复** - 提升至 P0
   - 理由: 恢复 CI/CD pipeline

### P1 任务建议
4. **Domain 层测试覆盖** - 保持 P1
   - 理由: 核心业务逻辑需要保障

5. **动态星级评分** - 保持 P1
   - 理由: 完善游戏反馈机制

### 新增建议
6. **Repository 集成测试** - P2
   - 使用 Hilt + In-Memory DB
   - 验证数据访问逻辑

---

## 8. 架构决策记录 (ADR)

### ADR-001: Hilt + Service Locator 混合 DI
**状态**: 活跃
**理由**: Hilt 2.48 在真机上存在 JavaPoet 兼容性问题
**后果**: ViewModel 保留 Hilt 注解便于未来迁移

### ADR-002: 性能监控条件启用
**状态**: 活跃
**理由**: 开发阶段需要性能数据，生产环境禁用以减少开销
**实施**: `PerformanceMonitor.isEnabled` + BuildConfig.DEBUG

### ADR-003: 测试覆盖优先级
**状态**: 活跃
**理由**: 有限资源下优先保障核心业务逻辑
**优先级**: Domain > ViewModel > UI > Data

---

**报告完成时间**: 2026-02-16
**下次更新**: P0 任务完成后
