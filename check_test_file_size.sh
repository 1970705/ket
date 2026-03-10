#!/bin/bash

###############################################################################
# Wordland 测试代码质量检查脚本
#
# 功能:
#   - 扫描所有测试文件，检查行数是否超标
#   - 按严重程度分类显示
#   - 支持作为 CI/CD 质量门禁
#
# 用法:
#   ./check_test_file_size.sh [--ci] [--verbose]
#
# 选项:
#   --ci       CI 模式，超标时返回非零退出码
#   --verbose  显示详细输出
#
# 作者: android-architect
# 日期: 2026-03-02
###############################################################################

# 使用 zsh 兼容模式（macOS 默认）
if [ -z "$BASH_VERSION" ] && [ -n "$ZSH_VERSION" ]; then
    # zsh 模式
    emulate sh
fi

# 颜色定义
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
GRAY='\033[0;90m'
NC='\033[0m' # No Color

# 配置
MAX_EXCELLENT=300    # 优秀上限
MAX_GOOD=400         # 良好上限
MAX_WARNING=500      # 警告上限
MAX_CRITICAL=600     # 严重上限

# 参数
CI_MODE=false
VERBOSE=false

# 统计变量
total_files=0
excellent_count=0
good_count=0
warning_count=0
critical_count=0
severe_count=0
total_lines=0

# 临时文件
TEMP_DIR=$(mktemp -d 2>/dev/null || echo "/tmp/test_quality_check.$$")
SEVERE_FILE="$TEMP_DIR/severe.txt"
CRITICAL_FILE="$TEMP_DIR/critical.txt"
WARNING_FILE="$TEMP_DIR/warning.txt"
GOOD_FILE="$TEMP_DIR/good.txt"
EXCELLENT_FILE="$TEMP_DIR/excellent.txt"

# 清理函数
cleanup() {
    rm -rf "$TEMP_DIR" 2>/dev/null
}
trap cleanup EXIT

# 初始化临时文件
: > "$SEVERE_FILE"
: > "$CRITICAL_FILE"
: > "$WARNING_FILE"
: > "$GOOD_FILE"
: > "$EXCELLENT_FILE"

# 解析参数
while [ $# -gt 0 ]; do
    case $1 in
        --ci)
            CI_MODE=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--ci] [--verbose]"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Wordland 测试代码质量检查${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 查找所有测试文件
test_files=$(find app/src/test -name "*Test.kt" -type f 2>/dev/null | sort)
android_test_files=$(find app/src/androidTest -name "*Test.kt" -type f 2>/dev/null | sort)
all_files="$test_files
$android_test_files"

# 检查是否找到文件
file_count=$(echo "$all_files" | grep -c "Test.kt" 2>/dev/null || echo "0")
if [ "$file_count" -eq 0 ]; then
    echo -e "${RED}错误: 未找到任何测试文件${NC}"
    exit 1
fi

# 分析每个文件
echo -e "${GRAY}正在分析测试文件...${NC}"
echo ""

echo "$all_files" | while IFS= read -r file; do
    [ -z "$file" ] && continue
    [ ! -f "$file" ] && continue

    lines=$(wc -l < "$file" | tr -d ' ')

    # 计算相对路径
    relative_path="${file#./}"

    # 输出到相应的临时文件
    if [ "$lines" -gt "$MAX_CRITICAL" ]; then
        echo "$relative_path|$lines" >> "$SEVERE_FILE"
    elif [ "$lines" -gt "$MAX_WARNING" ]; then
        echo "$relative_path|$lines" >> "$CRITICAL_FILE"
    elif [ "$lines" -gt "$MAX_GOOD" ]; then
        echo "$relative_path|$lines" >> "$WARNING_FILE"
    elif [ "$lines" -gt "$MAX_EXCELLENT" ]; then
        echo "$relative_path|$lines" >> "$GOOD_FILE"
    else
        echo "$relative_path|$lines" >> "$EXCELLENT_FILE"
    fi
done

# 计算统计数据
severe_count=$(wc -l < "$SEVERE_FILE" 2>/dev/null | tr -d ' ')
critical_count=$(wc -l < "$CRITICAL_FILE" 2>/dev/null | tr -d ' ')
warning_count=$(wc -l < "$WARNING_FILE" 2>/dev/null | tr -d ' ')
good_count=$(wc -l < "$GOOD_FILE" 2>/dev/null | tr -d ' ')
excellent_count=$(wc -l < "$EXCELLENT_FILE" 2>/dev/null | tr -d ' ')

# 处理空文件情况
[ -z "$severe_count" ] && severe_count=0
[ -z "$critical_count" ] && critical_count=0
[ -z "$warning_count" ] && warning_count=0
[ -z "$good_count" ] && good_count=0
[ -z "$excellent_count" ] && excellent_count=0

total_files=$((severe_count + critical_count + warning_count + good_count + excellent_count))

# 计算总行数
total_lines=0
for tempfile in "$SEVERE_FILE" "$CRITICAL_FILE" "$WARNING_FILE" "$GOOD_FILE" "$EXCELLENT_FILE"; do
    while IFS='|' read -r path lines; do
        [ -n "$lines" ] && total_lines=$((total_lines + lines))
    done < "$tempfile"
done

# 计算平均行数
if [ "$total_files" -gt 0 ]; then
    avg_lines=$((total_lines / total_files))
else
    avg_lines=0
fi

# 打印统计信息
echo -e "${BLUE}📊 总体统计${NC}"
echo "────────────────────────────────────"
echo "  总文件数: ${BLUE}$total_files${NC}"
echo "  总行数:   ${BLUE}$total_lines${NC}"
echo "  平均行数: ${BLUE}$avg_lines${NC}"
echo ""

# 打印分布
echo -e "${BLUE}📈 文件分布${NC}"
echo "────────────────────────────────────"
echo -e "  ${GREEN}✅ 优秀 (≤${MAX_EXCELLENT}行)${NC}: $excellent_count"
echo -e "  ${GREEN}✓ 良好 (${MAX_EXCELLENT+1}-${MAX_GOOD}行)${NC}: $good_count"
echo -e "  ${YELLOW}⚠️  警告 (${MAX_GOOD+1}-${MAX_WARNING}行)${NC}: $warning_count"
echo -e "  ${RED}🚨 超标 (${MAX_WARNING+1}-${MAX_CRITICAL}行)${NC}: $critical_count"
echo -e "  ${RED}🔴 严重 (>${MAX_CRITICAL}行)${NC}: $severe_count"
echo ""

# 计算健康比例
healthy_files=$((excellent_count + good_count))
healthy_percentage=0
if [ "$total_files" -gt 0 ]; then
    healthy_percentage=$((healthy_files * 100 / total_files))
fi

# 打印健康度
echo -e "${BLUE}💚 健康度${NC}"
echo "────────────────────────────────────"
if [ "$healthy_percentage" -ge 80 ]; then
    echo -e "  ${GREEN}优秀${NC} ($healthy_percentage% 符合标准)"
elif [ "$healthy_percentage" -ge 60 ]; then
    echo -e "  ${YELLOW}良好${NC} ($healthy_percentage% 符合标准)"
else
    echo -e "  ${RED}需改进${NC} ($healthy_percentage% 符合标准)"
fi
echo ""

# 打印问题文件
print_files() {
    local file="$1"
    local color="$2"
    local symbol="$3"
    local label="$4"

    if [ -s "$file" ]; then
        echo ""
        echo -e "${color}${label}${NC}"
        while IFS='|' read -r path lines; do
            [ -z "$path" ] && continue
            echo -e "  ${color}${symbol}${NC} ${GRAY}${path}${NC} ${color}($lines 行)${NC}"
        done < "$file"
    fi
}

if [ -s "$SEVERE_FILE" ] || [ -s "$CRITICAL_FILE" ] || [ -s "$WARNING_FILE" ]; then
    echo -e "${BLUE}⚠️  需要关注的文件${NC}"
    echo "────────────────────────────────────"

    print_files "$SEVERE_FILE" "${RED}" "✗" "🔴 严重问题 (>${MAX_CRITICAL}行) - 必须重构:"
    print_files "$CRITICAL_FILE" "${RED}" "✗" "🚨 超标 (${MAX_WARNING+1}-${MAX_CRITICAL}行) - 需要优化:"
    print_files "$WARNING_FILE" "${YELLOW}" "△" "⚠️  警告 (${MAX_GOOD+1}-${MAX_WARNING}行) - 建议关注:"

    echo ""
fi

# Verbose 模式显示优秀文件
if [ "$VERBOSE" = true ] && [ -s "$EXCELLENT_FILE" ]; then
    echo -e "${BLUE}✅ 优秀文件示例 (≤${MAX_EXCELLENT}行)${NC}"
    echo "────────────────────────────────────"
    count=0
    while IFS='|' read -r path lines; do
        [ -z "$path" ] && continue
        echo -e "  ${GREEN}✓${NC} ${GRAY}${path}${NC} ${GREEN}($lines 行)${NC}"
        count=$((count + 1))
        [ "$count" -ge 10 ] && break
    done < "$EXCELLENT_FILE"
    if [ "$excellent_count" -gt 10 ]; then
        echo -e "  ${GRAY}... 还有 $((excellent_count - 10)) 个文件${NC}"
    fi
    echo ""
fi

# 决定退出码
exit_code=0
if [ "$severe_count" -gt 0 ]; then
    exit_code=2
elif [ "$critical_count" -gt 0 ]; then
    exit_code=1
fi

# CI 模式下的严格检查
if [ "$CI_MODE" = true ]; then
    echo -e "${BLUE}🔒 CI 质量门禁${NC}"
    echo "────────────────────────────────────"
    if [ "$severe_count" -gt 0 ]; then
        echo -e "${RED}❌ 质量门禁失败: 发现 $severe_count 个严重超标文件${NC}"
        echo -e "   请参考: ${GRAY}docs/testing/strategy/TEST_CODE_STANDARDS.md${NC}"
    elif [ "$critical_count" -gt 0 ]; then
        echo -e "${YELLOW}⚠️  质量门禁警告: 发现 $critical_count 个超标文件${NC}"
        echo -e "   请参考: ${GRAY}docs/testing/strategy/TEST_CODE_STANDARDS.md${NC}"
    else
        echo -e "${GREEN}✅ 质量门禁通过: 所有文件符合标准${NC}"
    fi
    echo ""
fi

# 最终结果
echo -e "${BLUE}========================================${NC}"
if [ "$exit_code" -eq 0 ]; then
    echo -e "${GREEN}  ✅ 检查通过${NC}"
elif [ "$exit_code" -eq 1 ]; then
    echo -e "${YELLOW}  ⚠️  检查完成，发现需优化文件${NC}"
else
    echo -e "${RED}  ❌ 检查失败，发现严重问题${NC}"
fi
echo -e "${BLUE}========================================${NC}"

exit $exit_code
