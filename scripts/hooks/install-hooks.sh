#!/bin/bash
#
# 安装Git Hooks
# 用法: ./scripts/hooks/install-hooks.sh
#

echo "🔧 安装Git Hooks..."

# 复制pre-commit hook
cp scripts/hooks/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo "✅ Git Hooks安装完成！"
echo ""
echo "📋 已安装的Hooks:"
echo "  - pre-commit: 在提交前运行质量检查"
echo ""
echo "🚀 下次提交时将自动运行质量检查"
echo "  - KtLint（代码格式）"
echo "  - Detekt（代码质量）"
echo "  - 单元测试"
echo "  - 编译检查"
echo ""
echo "💡 如果需要跳过检查（不推荐），使用:"
echo "  git commit --no-verify"
