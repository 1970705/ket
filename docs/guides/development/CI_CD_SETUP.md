# CI/CD配置说明

**更新日期**: 2026-02-16

---

## 📋 概述

项目已配置GitHub Actions CI/CD，用于自动化测试和质量检查。

---

## 🔄 工作流说明

### 1. CI工作流 (`.github/workflows/ci.yml`)

**触发条件**:
- 推送到`main`或`develop`分支
- 创建Pull Request到`main`分支

**执行任务**:
1. **单元测试** (`unit-tests` job)
   - 运行所有单元测试
   - 生成测试覆盖率报告
   - 上传测试结果和覆盖率报告

2. **代码质量检查** (`code-quality` job)
   - 运行Android Lint检查
   - 上传Lint结果

3. **编译验证** (`build` job)
   - 编译Debug APK
   - 上传APK文件

**产物**:
- `test-results`: 单元测试结果
- `coverage-reports`: Jacoco覆盖率报告
- `lint-results`: Lint检查结果
- `app-debug`: Debug APK文件

---

## 🚪 质量门禁 (`.github/workflows/quality-gate.yml`)

**触发条件**: 仅在Pull Request到`main`分支时

**质量检查**:
1. ✅ 单元测试必须通过
2. ✅ 测试覆盖率必须 ≥80%
3. ✅ Lint检查必须通过
4. ✅ 编译必须成功

**失败后果**:
- ❌ Pull Request将被阻止合并
- ❌ 需要修复问题后重新提交

---

## 📊 测试覆盖率报告

### 查看覆盖率

**在GitHub Actions中**:
1. 进入Actions标签
2. 选择最新的workflow run
3. 下载`coverage-reports`产物
4. 打开`app/build/reports/jacoco/test/html/index.html`

**本地生成**:
```bash
./gradlew jacocoTestReport
open app/build/reports/jacoco/test/html/index.html
```

### 覆盖率目标

| 层级 | 目标 | 当前 |
|------|------|------|
| Domain层 | ≥90% | 待测量 |
| Data层 | ≥80% | 待测量 |
| UI层 | ≥60% | 待测量 |
| **整体** | **≥80%** | **84.6%** |

---

## 🔧 本地使用

### 在提交前运行所有检查

**推荐工作流**:
```bash
# 1. 运行单元测试
./gradlew test

# 2. 生成覆盖率报告
./gradlew jacocoTestReport

# 3. 运行Lint检查
./gradlew lint

# 4. 编译验证
./gradlew assembleDebug

# 5. 如果全部通过，提交代码
git add .
git commit -m "feat: Your changes"
git push
```

### 快速检查脚本

创建`scripts/checks/pre-commit.sh`:
```bash
#!/bin/bash
echo "🔍 运行质量检查..."

./gradlew test
if [ $? -ne 0 ]; then
  echo "❌ 单元测试失败"
  exit 1
fi

./gradlew lint
if [ $? -ne 0 ]; then
  echo "❌ Lint检查失败"
  exit 1
fi

echo "✅ 所有检查通过！"
```

---

## 📈 CI/CD最佳实践

### 分支保护

**推荐设置**:
1. `main`分支设为受保护
2. PR必须通过质量门禁
3. 要求至少1个审查批准
4. 启用状态检查（required checks）

### 提交前检查清单

**每次提交前**:
- [ ] 单元测试通过
- [ ] 本地构建成功
- [ ] 代码格式符合规范
- [ ] 无TODO或FIXME未处理

### PR创建检查清单

**创建PR前**:
- [ ] 所有CI检查通过
- [ ] 代码自审完成
- [ ] 测试覆盖率未下降
- [ ] 更新相关文档

---

## 🐛 故障排查

### 常见问题

**Q1: CI显示"Permission denied"错误**
```bash
# 确保gradlew有执行权限
git update-index --chmod=+x gradlew
git commit -m "Make gradlew executable"
git push
```

**Q2: 测试在本地通过但CI失败**
- 检查Java版本（本地 vs CI）
- 清理本地缓存：`./gradlew clean`
- 检查环境变量

**Q3: 覆盖率报告未生成**
- 确保build.gradle中有JaCoCo配置
- 检查测试是否真正执行

**Q4: Lint错误过多**
- 优先修复Critical和Error级别
- Warning可以在后续PR中修复

---

## 📚 相关文档

- [GitHub Actions文档](https://docs.github.com/en/actions)
- [JaCoCo文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Android Lint文档](https://developer.android.com/studio/write/lint)

---

**最后更新**: 2026-02-16
**维护者**: Android Team
