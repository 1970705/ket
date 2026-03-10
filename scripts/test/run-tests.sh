#!/bin/bash

# Wordland 测试运行脚本
# 用于运行所有测试并生成报告

set -e

echo "=========================================="
echo "  Wordland 测试运行脚本"
echo "=========================================="
echo ""

# 设置 Java Home
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数：打印成功信息
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# 函数：打印错误信息
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# 函数：打印警告信息
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# 1. 清理旧的测试结果
echo "1. 清理旧的测试结果..."
./gradlew clean
print_success "清理完成"
echo ""

# 2. 运行单元测试
echo "2. 运行单元测试..."
if ./gradlew testDebugUnitTest --continue; then
    print_success "单元测试通过"
else
    print_error "单元测试失败"
    echo "查看测试报告: app/build/reports/tests/testDebugUnitTest/index.html"
fi
echo ""

# 3. 生成测试覆盖率报告
echo "3. 生成测试覆盖率报告..."
if ./gradlew jacocoTestReport; then
    print_success "覆盖率报告生成完成"
    echo "查看覆盖率报告: app/build/reports/jacoco/jacocoTestReport/html/index.html"
else
    print_warning "无法生成覆盖率报告（可能需要配置 JaCoCo）"
fi
echo ""

# 4. 检查是否有连接的设备
echo "4. 检查 Android 设备..."
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l | tr -d ' ')

if [ "$DEVICE_COUNT" -gt 0 ]; then
    print_success "发现 $DEVICE_COUNT 个 Android 设备"

    # 5. 运行 Android 集成测试
    echo ""
    echo "5. 运行 Android 集成测试..."
    if ./gradlew connectedAndroidTest; then
        print_success "Android 测试通过"
        echo "查看测试报告: app/build/reports/androidTests/connected/index.html"
    else
        print_error "Android 测试失败"
    fi
else
    print_warning "未检测到 Android 设备，跳过集成测试"
    echo "  提示: 连接 Android 设备或启动模拟器后重新运行此脚本"
fi
echo ""

# 6. 运行 Lint 检查
echo "6. 运行 Lint 检查..."
if ./gradlew lint; then
    print_success "Lint 检查通过"
    echo "查看 Lint 报告: app/build/reports/lint-results.html"
else
    print_warning "Lint 检查发现问题"
    echo "查看 Lint 报告: app/build/reports/lint-results.html"
fi
echo ""

# 7. 显示测试结果摘要
echo "=========================================="
echo "  测试结果摘要"
echo "=========================================="
echo ""

# 显示单元测试结果
if [ -f "app/build/test-results/testDebugUnitTest/*.xml" ]; then
    TESTS=$(grep -h "tests" app/build/test-results/testDebugUnitTest/*.xml | cut -d'"' -f2 | head -1)
    FAILURES=$(grep -h "failures" app/build/test-results/testDebugUnitTest/*.xml | cut -d'"' -f2 | head -1)
    ERRORS=$(grep -h "errors" app/build/test-results/testDebugUnitTest/*.xml | cut -d'"' -f2 | head -1)

    echo "单元测试统计:"
    echo "  总测试数: $TESTS"
    echo "  失败: $FAILURES"
    echo "  错误: $ERRORS"
    echo ""
fi

# 显示覆盖率信息
if [ -f "app/build/reports/jacoco/jacocoTestReport/html/index.html" ]; then
    echo "✓ 覆盖率报告已生成"
    echo "  打开: app/build/reports/jacoco/jacocoTestReport/html/index.html"
    echo ""
fi

echo "=========================================="
echo "  测试完成！"
echo "=========================================="
echo ""
echo "提示:"
echo "  - 查看所有报告: app/build/reports/"
echo "  - 查看测试日志: ./gradlew test --info"
echo "  - 运行特定测试: ./gradlew test --tests 'ClassName'"
echo ""
