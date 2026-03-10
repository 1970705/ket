#!/bin/bash
# 截图捕获脚本
# Epic #12 - Task 12.3: Real Device Testing Scripts
# 用途: 捕获真机屏幕截图用于视觉验证

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

# 获取设备ID
get_device_id() {
    local device_id=${1:-$(adb devices | grep -v "List" | grep "device$" | head -1 | cut -f1)}
    if [ -z "$device_id" ]; then
        log_error "没有检测到真机设备"
        exit 1
    fi
    echo "$device_id"
}

# 捕获单张截图
capture_single() {
    local device_id=$1
    local output_path=$2

    # 确保输出目录存在
    local output_dir=$(dirname "$output_path")
    mkdir -p "$output_dir"

    # 使用 screencap 捕获
    adb -s "$device_id" shell screencap -p /sdcard/temp_screenshot.png > /dev/null 2>&1

    # 拉取文件
    adb -s "$device_id" pull /sdcard/temp_screenshot.png "$output_path" > /dev/null 2>&1

    # 清理临时文件
    adb -s "$device_id" shell rm /sdcard/temp_screenshot.png > /dev/null 2>&1

    log_info "截图已保存: $output_path"
}

# 捕获系列截图 (用于视频流式捕获)
capture_series() {
    local device_id=$1
    local output_dir=$2
    local count=$3
    local interval=$4  # 间隔秒数

    mkdir -p "$output_dir"

    log_step "捕获系列截图 ($count 张, 间隔 ${interval}s)..."

    for i in $(seq 1 "$count"); do
        local filename=$(printf "%s/screenshot_%04d.png" "$output_dir" "$i")
        capture_single "$device_id" "$filename"
        sleep "$interval"
    done

    log_info "系列截图完成: $output_dir"
}

# 捕获特定UI组件的截图
capture_component() {
    local device_id=$1
    local component_name=$2
    local output_dir=$3

    local output_path="$output_dir/${component_name}.png"
    capture_single "$device_id" "$output_path"
}

# 生成GIF动画 (需要 ImageMagick)
generate_gif() {
    local screenshot_dir=$1
    local output_gif=$2
    local delay=${3:-10}  # 帧间隔 (10 = 100ms)

    if ! command -v convert &> /dev/null; then
        log_warn "ImageMagick 未安装，跳过 GIF 生成"
        log_info "安装: brew install imagemagick"
        return 1
    fi

    log_step "生成 GIF 动画..."

    convert -delay "$delay" -loop 0 "$screenshot_dir"/*.png "$output_gif"

    log_info "GIF 已生成: $output_gif"
}

# 主流程
main() {
    local device_id=$(get_device_id "$1")
    local mode=${2:-single}
    local output_path=${3:-"./screenshot.png"}

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  截图捕获工具${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo "设备: $device_id"
    echo "模式: $mode"
    echo ""

    case "$mode" in
        single)
            capture_single "$device_id" "$output_path"
            ;;
        series)
            local count=${4:-10}
            local interval=${5:-1}
            capture_series "$device_id" "$output_path" "$count" "$interval"
            ;;
        gif)
            generate_gif "$output_path" "$output_path"
            ;;
        *)
            log_error "未知模式: $mode"
            echo "用法: $0 [device_id] [single|series|gif] [output_path] [count] [interval]"
            exit 1
            ;;
    esac
}

main "$@"
