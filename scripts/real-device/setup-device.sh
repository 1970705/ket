#!/bin/bash
# 设备准备脚本
# Epic #12 - Task 12.3: Real Device Testing Scripts
# 用途: 准备真机设备用于自动化测试

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

# 1. 检查 ADB 连接
check_adb() {
    log_step "检查 ADB 连接..."

    if ! command -v adb &> /dev/null; then
        log_error "ADB 未找到，请设置 ANDROID_HOME"
        exit 1
    fi

    local devices=$(adb devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')
    if [ "$devices" -eq 0 ]; then
        log_error "没有检测到真机设备"
        log_info "请确保:"
        log_info "  1. 设备已通过 USB 连接"
        log_info "  2. USB 调试已启用"
        log_info "  3. 已授权此计算机进行调试"
        exit 1
    fi

    log_info "检测到 $devices 台设备"
}

# 2. 检查开发者选项
check_developer_options() {
    local device_id=$1

    log_step "检查开发者选项状态..."

    # 检查 USB 调试是否启用
    local usb_debug=$(adb -s "$device_id" shell settings get global development_settings_enabled 2>/dev/null | tr -d '\r')
    if [ "$usb_debug" != "1" ] && [ "$usb_debug" != "null" ]; then
        log_warn "USB 调试可能未启用"
    fi

    log_info "开发者选项: 已启用"
}

# 3. 检查关键设置
check_settings() {
    local device_id=$1

    log_step "检查关键设置..."

    # 检查保持唤醒状态
    local stay_on=$(adb -s "$device_id" shell settings get global stay_on_while_plugged_in 2>/dev/null | tr -d '\r')
    log_info "保持唤醒: $stay_on"

    # 检查动画设置 (建议关闭以提高测试稳定性)
    local window_anim=$(adb -s "$device_id" shell settings get global window_animation_scale 2>/dev/null | tr -d '\r')
    local transition_anim=$(adb -s "$device_id" shell settings get global transition_animation_scale 2>/dev/null | tr -d '\r')
    local animator_duration=$(adb -s "$device_id" shell settings get global animator_duration_scale 2>/dev/null | tr -d '\r')

    log_info "窗口动画: $window_anim"
    log_info "过渡动画: $transition_anim"
    log_info "Animator时长: $animator_duration"

    if [ "$window_anim" != "0.0" ] || [ "$transition_anim" != "0.0" ]; then
        log_warn "建议: 关闭动画以提高测试稳定性"
        log_info "  运行以下命令关闭动画:"
        log_info "  adb -s $device_id shell settings put global window_animation_scale 0.0"
        log_info "  adb -s $device_id shell settings put global transition_animation_scale 0.0"
        log_info "  adb -s $device_id shell settings put global animator_duration_scale 0.0"
    fi
}

# 4. 检查存储空间
check_storage() {
    local device_id=$1

    log_step "检查存储空间..."

    local storage=$(adb -s "$device_id" shell df /data | tail -1)
    local available=$(echo "$storage" | awk '{print $4}')
    local used_percent=$(echo "$storage" | awk '{print $5}')

    log_info "可用空间: $available"
    log_info "已使用: $used_percent"

    # 简单的KB检查
    local available_kb=$((available))
    if [ "$available_kb" -lt 1048576 ]; then  # 小于 1GB
        log_warn "存储空间不足 (可用 < 1GB)"
    fi
}

# 5. 检查电池状态
check_battery() {
    local device_id=$1

    log_step "检查电池状态..."

    local level=$(adb -s "$device_id" shell dumpsys battery | grep level | head -1 | grep -o '[0-9]*' | tr -d '\r')
    local status=$(adb -s "$device_id" shell dumpsys battery | grep status | head -1 | cut -d: -f2 | xargs | tr -d '\r')

    log_info "电池电量: $level%"
    log_info "充电状态: $status"

    if [ "$level" -lt 20 ]; then
        log_warn "电池电量低 ($level%)，建议充电后测试"
    fi
}

# 6. 检查网络连接
check_network() {
    local device_id=$1

    log_step "检查网络连接..."

    local wifi=$(adb -s "$device_id" shell dumpsys connectivity | grep "Wi-Fi" | head -1)
    if [ -n "$wifi" ]; then
        log_info "Wi-Fi: 已连接"
    else
        log_warn "Wi-Fi: 未检测到连接"
    fi
}

# 7. 设备信息摘要
print_device_summary() {
    local device_id=$1

    log_step "设备信息摘要"

    local model=$(adb -s "$device_id" shell getprop ro.product.model | tr -d '\r')
    local manufacturer=$(adb -s "$device_id" shell getprop ro.product.manufacturer | tr -d '\r')
    local android_version=$(adb -s "$device_id" shell getprop ro.build.version.release | tr -d '\r')
    local api_level=$(adb -s "$device_id" shell getprop ro.build.version.sdk | tr -d '\r')
    local build=$(adb -s "$device_id" shell getprop ro.build.display.id | tr -d '\r')
    local density=$(adb -s "$device_id" shell wm density | tr -d '\r')
    local size=$(adb -s "$device_id" shell wm size | tr -d '\r')

    echo ""
    echo -e "${GREEN}  制造商:${NC} $manufacturer"
    echo -e "${GREEN}  型号:${NC}    $model"
    echo -e "${GREEN}  Android:${NC} $android_version (API $api_level)"
    echo -e "${GREEN}  构建:${NC}    $build"
    echo -e "${GREEN}  屏幕:${NC}    $size @ $density"
    echo ""
}

# 8. 推荐配置
recommend_config() {
    log_step "推荐测试配置"

    echo ""
    echo -e "${YELLOW}建议配置:${NC}"
    echo "1. 关闭动画:"
    echo "   adb shell settings put global window_animation_scale 0.0"
    echo "   adb shell settings put global transition_animation_scale 0.0"
    echo "   adb shell settings put global animator_duration_scale 0.0"
    echo ""
    echo "2. 保持屏幕唤醒:"
    echo "   adb shell settings put global stay_on_while_plugged_in 7"
    echo ""
    echo "3. 设置屏幕超时为 30 分钟:"
    echo "   adb shell settings put system screen_off_timeout 1800000"
    echo ""
    echo "4. 关用自动锁定:"
    echo "   adb shell settings put secure lock_screen_disabled 1"
    echo ""
}

# 主流程
main() {
    local device_id=$(get_device_id "$1")

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  设备准备检查${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    check_adb
    check_developer_options "$device_id"
    check_settings "$device_id"
    check_storage "$device_id"
    check_battery "$device_id"
    check_network "$device_id"
    print_device_summary "$device_id"
    recommend_config

    echo ""
    log_info "设备检查完成 ✅"
    echo ""
}

main "$@"
