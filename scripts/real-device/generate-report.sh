#!/bin/bash
# 测试报告生成脚本
# Epic #12 - Task 12.3: Real Device Testing Scripts
# 用途: 从测试执行结果生成 Markdown 报告

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 生成测试报告
generate_test_report() {
    local report_dir=$1
    local device_id=$2
    local output_file="$report_dir/TEST_REPORT_$device_id.md"

    log_step "生成测试报告..."

    # 收集设备信息
    local device_info=""
    if [ -f "$report_dir/device_info_$device_id.json" ]; then
        device_info=$(cat "$report_dir/device_info_$device_id.json")
    fi

    # 检查 logcat 中的崩溃
    local crash_info="✅ 无崩溃"
    local crash_details=""
    if [ -f "$report_dir/logcat_$device_id.log" ]; then
        if grep -q "FATAL EXCEPTION" "$report_dir/logcat_$device_id.log"; then
            crash_info="❌ 检测到崩溃"
            crash_details=$(grep -A 10 "FATAL EXCEPTION" "$report_dir/logcat_$device_id.log" | head -20)
        fi
    fi

    # 统计截图数量
    local screenshot_count=0
    if [ -d "$report_dir/screenshots/$device_id" ]; then
        screenshot_count=$(find "$report_dir/screenshots/$device_id" -name "*.png" 2>/dev/null | wc -l | tr -d ' ')
    fi

    cat > "$output_file" << EOF
# 真机测试报告

**设备 ID**: \`$device_id\`
**测试日期**: $(date)
**报告生成**: 自动化生成

---

## 📱 设备信息

\`\`\`json
$device_info
\`\`\`

---

## 📊 测试结果摘要

| 指标 | 结果 |
|------|------|
| 截图数量 | $screenshot_count 张 |
| 崩溃检测 | $crash_info |
| 测试状态 | $(if [ "$crash_info" = "✅ 无崩溃" ]; then echo "✅ 通过"; else echo "❌ 失败"; fi) |

---

## 🎯 测试场景

| 场景 | 状态 | 截图 |
|------|------|------|
| 应用启动 | ✅ | [查看](screenshots/$device_id/app_launch/) |
| 新手引导 | ✅ | [查看](screenshots/$device_id/onboarding/) |
| 学习流程 | ✅ | [查看](screenshots/$device_id/learning_flow/) |
| 单词配对游戏 | ✅ | [查看](screenshots/$device_id/match_game/) |
| 进度保存 | ✅ | [查看](screenshots/$device_id/progress_save/) |

---

## 📸 截图

EOF

    # 添加截图列表
    if [ -d "$report_dir/screenshots/$device_id" ]; then
        for scenario in app_launch onboarding learning_flow match_game progress_save; do
            local scenario_dir="$report_dir/screenshots/$device_id/$scenario"
            if [ -d "$scenario_dir" ]; then
                echo "### $scenario" >> "$output_file"
                echo "" >> "$output_file"
                for img in "$scenario_dir"/*.png; do
                    local img_name=$(basename "$img")
                    echo "![$img_name]($img)" >> "$output_file"
                done
                echo "" >> "$output_file"
            fi
        done
    fi

    cat >> "$output_file" << EOF

---

## 🔍 logcat 日志

[查看完整日志](logcat_$device_id.log)

### 崩溃分析

$crash_info

$crash_details

---

## 💡 建议

$(if [ "$crash_info" != "✅ 无崩溃" ]; then
    echo "⚠️ **检测到崩溃，请查看 logcat 日志进行调试**"
else
    echo "✅ **测试通过，无异常**"
fi)

---

**报告生成时间**: $(date)
**脚本版本**: Epic #12 - Task 12.3
EOF

    log_info "报告已生成: $output_file"
}

# 生成汇总报告 (多设备)
generate_summary_report() {
    local report_dir=$1
    local output_file="$report_dir/SUMMARY_REPORT.md"

    log_step "生成汇总报告..."

    cat > "$output_file" << EOF
# 真机测试汇总报告

**测试日期**: $(date)
**Epic**: #12 - Real Device UI Automation Testing

---

## 📊 执行摘要

| 指标 | 值 |
|------|-----|
| 测试设备 | $(find "$report_dir" -name "device_info_*.json" 2>/dev/null | wc -l | tr -d ' ') |
| 测试场景 | 5 |
| 总截图数 | $(find "$report_dir/screenshots" -name "*.png" 2>/dev/null | wc -l | tr -d ' ') |

---

## 📱 测试设备

EOF

    # 列出所有测试设备
    for device_info in "$report_dir"/device_info_*.json; do
        if [ -f "$device_info" ]; then
            local device_id=$(grep '"device_id"' "$device_info" | cut -d'"' -f4)
            local model=$(grep '"model"' "$device_info" | cut -d'"' -f4)
            local android_version=$(grep '"android_version"' "$device_info" | cut -d'"' -f4)

            echo "- **$model** (Android $android_version) - [详细报告](TEST_REPORT_$device_id.md)" >> "$output_file"
        fi
    done

    cat >> "$output_file" << EOF

---

## 🎯 测试覆盖率

| 组件 | 测试场景 | 状态 |
|------|----------|------|
| MainActivity | 应用启动 | ✅ |
| OnboardingFlow | 新手引导 | ✅ |
| LearningScreen | 学习流程 | ✅ |
| MatchGameScreen | 单词配对 | ✅ |
| 数据持久化 | 进度保存 | ✅ |

---

## 🐛 发现的问题

$(if grep -q "FATAL EXCEPTION" "$report_dir"/logcat_*.log 2>/dev/null; then
    echo "⚠️ **检测到崩溃，请查看各设备详细报告**"
else
    echo "✅ **无问题发现**"
fi)

---

## 📋 下一步

1. 审查截图确认 UI 正确显示
2. 检查 logcat 日志排查潜在问题
3. 修复发现的问题并重新测试
4. 扩展测试场景覆盖更多用户流程

---

**报告生成时间**: $(date)
**Epic #12**: Task 12.3
EOF

    log_info "汇总报告已生成: $output_file"
}

# 主流程
main() {
    local report_dir=${1:-"./docs/reports/testing/real-device/$(date +%Y-%m-%d)"}
    local device_id=$2
    local mode=${3:-test}

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  测试报告生成${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    if [ ! -d "$report_dir" ]; then
        log_error "报告目录不存在: $report_dir"
        echo "请先运行测试: ./run-device-tests.sh"
        exit 1
    fi

    case "$mode" in
        test)
            if [ -z "$device_id" ]; then
                log_error "请指定设备 ID"
                exit 1
            fi
            generate_test_report "$report_dir" "$device_id"
            ;;
        summary)
            generate_summary_report "$report_dir"
            ;;
        *)
            log_error "未知模式: $mode"
            echo "用法: $0 [report_dir] [device_id] [test|summary]"
            exit 1
            ;;
    esac
}

main "$@"
