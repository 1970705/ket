#!/bin/bash
# 截图对比脚本
# Epic #12 - Task 12.3: Real Device Testing Scripts
# 用途: 对比截图差异，检测视觉回归

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${GREEN}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 使用 ImageMagick 比较两张截图
compare_images() {
    local baseline=$1
    local current=$2
    local output_diff=$3
    local threshold=${4:-5}  # 允许的差异百分比

    if [ ! -f "$baseline" ]; then
        log_error "基线截图不存在: $baseline"
        return 1
    fi

    if [ ! -f "$current" ]; then
        log_error "当前截图不存在: $current"
        return 1
    fi

    if ! command -v compare &> /dev/null; then
        log_warn "ImageMagick 未安装，无法进行像素级对比"
        log_info "安装: brew install imagemagick"
        return 1
    fi

    log_step "对比截图..."
    log_info "基线: $baseline"
    log_info "当前: $current"

    # 使用 compare 命令生成差异图
    local result=$(compare -metric AE "$baseline" "$current" "$output_diff" 2>&1 | awk '{print $1}')
    local diff_pixels=$(echo "$result" | tr -d ' ')

    # 计算差异百分比
    local baseline_width=$(identify -format "%w" "$baseline" 2>/dev/null)
    local baseline_height=$(identify -format "%h" "$baseline" 2>/dev/null)
    local total_pixels=$((baseline_width * baseline_height))
    local diff_percent=$((diff_pixels * 100 / total_pixels))

    log_info "差异像素: $diff_pixels / $total_pixels ($diff_percent%)"

    if [ "$diff_percent" -gt "$threshold" ]; then
        log_warn "检测到显著差异! (阈值: $threshold%)"
        log_info "差异图已保存: $output_diff"
        return 1
    else
        log_info "截图在可接受范围内 ✅"
        return 0
    fi
}

# 批量对比目录中的截图
compare_directories() {
    local baseline_dir=$1
    local current_dir=$2
    local output_dir=$3
    local threshold=${4:-5}

    mkdir -p "$output_dir"

    log_step "批量对比截图..."
    log_info "基线目录: $baseline_dir"
    log_info "当前目录: $current_dir"
    log_info "输出目录: $output_dir"

    local total=0
    local passed=0
    local failed=0

    for baseline_file in "$baseline_dir"/*.png; do
        if [ -f "$baseline_file" ]; then
            local filename=$(basename "$baseline_file")
            local current_file="$current_dir/$filename"
            local diff_file="$output_dir/diff_$filename"

            if [ -f "$current_file" ]; then
                total=$((total + 1))
                if compare_images "$baseline_file" "$current_file" "$diff_file" "$threshold"; then
                    passed=$((passed + 1))
                else
                    failed=$((failed + 1))
                fi
            else
                log_warn "缺少对应截图: $filename"
            fi
        fi
    done

    echo ""
    log_info "对比完成"
    log_info "总计: $total"
    log_info "通过: $passed"
    log_info "失败: $failed"

    if [ "$failed" -gt 0 ]; then
        return 1
    fi
    return 0
}

# 生成对比报告HTML
generate_comparison_html() {
    local baseline_dir=$1
    local current_dir=$2
    local diff_dir=$3
    local output_html=$4

    log_step "生成对比报告..."

    cat > "$output_html" << EOF
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>截图对比报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        h1 { color: #333; }
        .comparison { display: flex; gap: 20px; margin: 20px 0; flex-wrap: wrap; }
        .image-container { background: white; padding: 10px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .image-container h3 { margin-top: 0; color: #555; }
        .image-container img { max-width: 300px; border: 1px solid #ddd; }
        .passed { border-left: 4px solid #4CAF50; }
        .failed { border-left: 4px solid #F44336; }
        .summary { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
    </style>
</head>
<body>
    <h1>📸 截图对比报告</h1>
    <div class="summary">
        <h2>摘要</h2>
        <p><strong>生成时间:</strong> $(date)</p>
        <p><strong>基线目录:</strong> $baseline_dir</p>
        <p><strong>当前目录:</strong> $current_dir</p>
    </div>
    <div id="comparisons">
EOF

    for baseline_file in "$baseline_dir"/*.png; do
        if [ -f "$baseline_file" ]; then
            local filename=$(basename "$baseline_file")
            local current_file="$current_dir/$filename"
            local diff_file="$diff_dir/diff_$filename"

            local status="passed"
            local status_text="✅ 通过"

            if [ -f "$diff_file" ]; then
                status="failed"
                status_text="❌ 差异"
            fi

            cat >> "$output_html" << EOF
        <div class="comparison">
            <div class="image-container $status">
                <h3>基线 - $filename</h3>
                <img src="../$baseline_dir/$filename" alt="基线">
            </div>
            <div class="image-container $status">
                <h3>当前 - $filename</h3>
                <img src="../$current_dir/$filename" alt="当前">
            </div>
            <div class="image-container $status">
                <h3>差异 - $status_text</h3>
                <img src="../$diff_dir/diff_$filename" alt="差异">
            </div>
        </div>
EOF
        fi
    done

    cat >> "$output_html" << EOF
    </div>
    <footer>
        <p>生成时间: $(date)</p>
    </footer>
</body>
</html>
EOF

    log_info "报告已生成: $output_html"
}

# 主流程
main() {
    local mode=${1:-compare}
    local baseline_dir=${2:-"./screenshots/baseline"}
    local current_dir=${3:-"./screenshots/current"}
    local output_dir=${4:-"./screenshots/diff"}

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  截图对比工具${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    case "$mode" in
        compare)
            compare_images "$baseline_dir" "$current_dir" "$output_dir"
            ;;
        batch)
            compare_directories "$baseline_dir" "$current_dir" "$output_dir"
            ;;
        html)
            generate_comparison_html "$baseline_dir" "$current_dir" "$output_dir" "$output_dir/report.html"
            ;;
        *)
            log_error "未知模式: $mode"
            echo "用法: $0 [compare|batch|html] [baseline_dir] [current_dir] [output_dir]"
            exit 1
            ;;
    esac
}

main "$@"
