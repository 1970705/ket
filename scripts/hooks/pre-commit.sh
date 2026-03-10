#!/bin/bash
#
# Pre-commit hook for quality checks
# 安装: cp scripts/hooks/pre-commit.sh .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit
#

set -e

echo "🔍 运行质量检查..."

# 获取变更的Java/Kotlin文件
CHANGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep -E '\.(kt|java)$' || true)

if [ -z "$CHANGED_FILES" ]; then
    echo "✅ 没有Java/Kotlin文件变更，跳过检查"
    exit 0
fi

echo "📝 变更的文件:"
echo "$CHANGED_FILES"
echo ""

# 1. 运行KtLint
echo "1️⃣ 运行KtLint代码格式检查..."
if [ -n "$CHANGED_FILES" ]; then
    # 将文件列表转换为KtLint格式模式
    KTLINT_PATTERNS=""
    for file in $CHANGED_FILES; do
        KTLINT_PATTERNS="$KTLINT_PATTERNS**/${file#*/}/**,**/$file/**,**/$file"
    done
    ./gradlew ktlintCheck -P ktlintFilter="$KTLINT_PATTERNS"
else
    ./gradlew ktlintCheck
fi
if [ $? -ne 0 ]; then
    echo "❌ KtLint检查失败"
    echo "💡 运行 './gradlew ktlintFormat' 自动修复所有格式问题"
    exit 1
fi

# 2. 运行Detekt
echo "2️⃣ 运行Detekt代码质量检查..."
./gradlew detekt -P detekt.config=$rootDir/config/detekt/detekt.yml
if [ $? -ne 0 ]; then
    echo "❌ Detekt检查失败"
    echo "💡 请查看detekt报告并修复代码质量问题"
    exit 1
fi

# 3. 运行单元测试
echo "3️⃣ 运行单元测试..."
./gradlew test
if [ $? -ne 0 ]; then
    echo "❌ 单元测试失败"
    exit 1
fi

# 4. 编译检查
echo "4️⃣ 编译检查..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "✅ 所有质量检查通过！"
echo "  - KtLint: ✅"
echo "  - Detekt: ✅"
echo "  - 单元测试: ✅"
echo "  - 编译: ✅"
echo ""
echo "🚀 可以提交代码了！"
