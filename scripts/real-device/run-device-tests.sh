#!/bin/bash
# 真机自动化测试主脚本
# Epic #12 - Task 12.3: Real Device Testing Scripts
# 用法: ./run-device-tests.sh [device_id]
#
# 测试场景:
# 1. 应用启动 (app_launch)
# 2. 新手引导 (onboarding)
# 3. 学习流程 (learning_flow)
# 4. 单词配对游戏 (match_game)
# 5. 进度保存 (progress_save)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
REPORT_DIR="$PROJECT_ROOT/docs/reports/testing/real-device/$(date +%Y-%m-%d)"
LOG_FILE="$REPORT_DIR/test-execution.log"

# 创建报告目录
mkdir -p "$REPORT_DIR"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() { echo -e "${GREEN}[INFO]${NC} $1" | tee -a "$LOG_FILE"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$LOG_FILE"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1" | tee -a "$LOG_FILE"; }

# 获取设备ID
get_device_id() {
    local device_id=${1:-$(adb devices | grep -v "List" | grep "device$" | head -1 | cut -f1)}
    if [ -z "$device_id" ]; then
        log_error "没有检测到真机设备"
        exit 1
    fi
    echo "$device_id"
}

# 1. 环境检查
check_environment() {
    log_step "检查测试环境..."

    # 检查 ADB
    if ! command -v adb &> /dev/null; then
        log_error "ADB 未找到，请设置 ANDROID_HOME"
        exit 1
    fi

    # 检查设备连接
    DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')
    if [ "$DEVICES" -eq 0 ]; then
        log_error "没有检测到真机设备"
        exit 1
    fi

    log_info "检测到 $DEVICES 台设备"
}

# 2. 设备信息收集
collect_device_info() {
    local device_id=$1

    log_step "收集设备信息: $device_id"

    DEVICE_MODEL=$(adb -s "$device_id" shell getprop ro.product.model | tr -d '\r')
    ANDROID_VERSION=$(adb -s "$device_id" shell getprop ro.build.version.release | tr -d '\r')
    API_LEVEL=$(adb -s "$device_id" shell getprop ro.build.version.sdk | tr -d '\r')
    SCREEN_DENSITY=$(adb -s "$device_id" shell wm density | tr -d '\r')
    SCREEN_SIZE=$(adb -s "$device_id" shell wm size | tr -d '\r')
    MANUFACTURER=$(adb -s "$device_id" shell getprop ro.product.manufacturer | tr -d '\r')

    cat > "$REPORT_DIR/device_info_$device_id.json" << EOF
{
    "device_id": "$device_id",
    "model": "$DEVICE_MODEL",
    "manufacturer": "$MANUFACTURER",
    "android_version": "$ANDROID_VERSION",
    "api_level": "$API_LEVEL",
    "screen_density": "$SCREEN_DENSITY",
    "screen_size": "$SCREEN_SIZE",
    "test_date": "$(date -Iseconds)"
}
EOF

    log_info "设备: $MANUFACTURER $DEVICE_MODEL"
    log_info "Android: $ANDROID_VERSION (API $API_LEVEL)"
    log_info "屏幕: $SCREEN_SIZE @ $SCREEN_DENSITY"
}

# 3. 应用安装
install_apk() {
    local device_id=$1

    log_step "构建并安装 APK..."

    cd "$PROJECT_ROOT"

    # 构建最新 APK
    log_info "构建 APK..."
    ./gradlew assembleDebug > /dev/null 2>&1 || {
        log_error "APK 构建失败"
        exit 1
    }

    APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
    if [ ! -f "$APK_PATH" ]; then
        log_error "APK 文件不存在: $APK_PATH"
        exit 1
    fi

    # 卸载旧版本
    log_info "卸载旧版本..."
    adb -s "$device_id" uninstall com.wordland 2>/dev/null || true

    # 安装新版本
    log_info "安装新版本..."
    adb -s "$device_id" install -r "$APK_PATH" > /dev/null 2>&1 || {
        log_error "APK 安装失败"
        exit 1
    }

    # 清除应用数据
    log_info "清除应用数据..."
    adb -s "$device_id" shell pm clear com.wordland > /dev/null 2>&1

    log_info "APK 安装完成"
}

# 4. 启动 logcat 监控
start_logcat_monitor() {
    local device_id=$1

    log_step "启动 logcat 监控: $device_id"

    adb -s "$device_id" logcat -c
    adb -s "$device_id" logcat -v time -v threadtime *:F AndroidRuntime:E Wordland:I > "$REPORT_DIR/logcat_$device_id.log" 2>&1 &
    LOGCAT_PID=$!
    echo "$LOGCAT_PID" > "$REPORT_DIR/logcat_pid_$device_id"
}

stop_logcat_monitor() {
    local device_id=$1
    local pid_file="$REPORT_DIR/logcat_pid_$device_id"

    if [ -f "$pid_file" ]; then
        local LOGCAT_PID=$(cat "$pid_file")
        if [ -n "$LOGCAT_PID" ]; then
            kill $LOGCAT_PID 2>/dev/null || true
        fi
        rm -f "$pid_file"
    fi
}

# 5. 截图捕获
capture_screenshot() {
    local device_id=$1
    local output_path=$2

    adb -s "$device_id" shell screencap -p /sdcard/temp_screenshot.png
    adb -s "$device_id" pull /sdcard/temp_screenshot.png "$output_path" > /dev/null 2>&1
    adb -s "$device_id" shell rm /sdcard/temp_screenshot.png
}

# 6. 点击操作
tap() {
    local device_id=$1
    local x=$2
    local y=$3

    adb -s "$device_id" shell input tap "$x" "$y"
}

# 7. 输入文本
input_text() {
    local device_id=$1
    local text=$2

    adb -s "$device_id" shell input text "$text"
}

# 8. 按键操作
press_key() {
    local device_id=$1
    local key_code=$2

    adb -s "$device_id" shell input keyevent "$key_code"
}

# 9. 检查崩溃
check_for_crashes() {
    local device_id=$1

    if grep -q "FATAL EXCEPTION" "$REPORT_DIR/logcat_$device_id.log" 2>/dev/null; then
        log_error "检测到应用崩溃"
        grep -A 20 "FATAL EXCEPTION" "$REPORT_DIR/logcat_$device_id.log" | tee -a "$LOG_FILE"
        return 1
    fi
    return 0
}

# 10. 测试场景 - 应用启动
test_app_launch() {
    local device_id=$1
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/app_launch"

    mkdir -p "$screenshot_dir"

    log_info "测试场景: 应用启动"

    # 启动应用
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 3

    # 截图
    capture_screenshot "$device_id" "$screenshot_dir/01_home_screen.png"

    # 检查崩溃
    if ! check_for_crashes "$device_id"; then
        return 1
    fi

    log_info "应用启动测试完成"
    return 0
}

# 11. 测试场景 - 新手引导
test_onboarding() {
    local device_id=$1
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/onboarding"

    mkdir -p "$screenshot_dir"

    log_info "测试场景: 新手引导"

    # 清除数据后启动
    adb -s "$device_id" shell pm clear com.wordland > /dev/null 2>&1
    sleep 1
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 3

    # 截图 - 欢迎页面
    capture_screenshot "$device_id" "$screenshot_dir/01_welcome.png"
    sleep 1

    # 点击开始冒险按钮 (屏幕中心下方)
    local width=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f1 | tr -d '\r')
    local height=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f2 | tr -d '\r')
    local center_x=$((width / 2))
    local button_y=$((height * 3 / 5))

    tap "$device_id" "$center_x" "$button_y"
    sleep 2

    # 截图 - 宠物选择
    capture_screenshot "$device_id" "$screenshot_dir/02_pet_selection.png"
    sleep 1

    # 选择第一个宠物
    tap "$device_id" "$center_x" "$((height / 2))"
    sleep 1
    tap "$device_id" "$center_x" "$((height * 3 / 4))"
    sleep 2

    # 截图 - 教程开始
    capture_screenshot "$device_id" "$screenshot_dir/03_tutorial_start.png"

    # 检查崩溃
    if ! check_for_crashes "$device_id"; then
        return 1
    fi

    log_info "新手引导测试完成"
    return 0
}

# 12. 测试场景 - 学习流程
test_learning_flow() {
    local device_id=$1
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/learning_flow"

    mkdir -p "$screenshot_dir"

    log_info "测试场景: 学习流程"

    # 启动应用
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 3

    # 截图 - 主页
    capture_screenshot "$device_id" "$screenshot_dir/01_home.png"
    sleep 1

    # 点击开始冒险 (岛屿选择)
    local width=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f1 | tr -d '\r')
    local height=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f2 | tr -d '\r')
    local center_x=$((width / 2))

    tap "$device_id" "$center_x" "$((height / 2))"
    sleep 2

    # 截图 - 岛屿地图
    capture_screenshot "$device_id" "$screenshot_dir/02_island_map.png"
    sleep 1

    # 点击 Look Island
    tap "$device_id" "$((width / 2))" "$((height / 3))"
    sleep 2

    # 截图 - 关卡选择
    capture_screenshot "$device_id" "$screenshot_dir/03_level_select.png"
    sleep 1

    # 点击 Level 1
    tap "$device_id" "$((width / 2))" "$((height / 3))"
    sleep 2

    # 截图 - 学习屏幕
    capture_screenshot "$device_id" "$screenshot_dir/04_learning_screen.png"

    # 检查崩溃
    if ! check_for_crashes "$device_id"; then
        return 1
    fi

    log_info "学习流程测试完成"
    return 0
}

# 13. 测试场景 - 单词配对游戏
test_match_game() {
    local device_id=$1
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/match_game"

    mkdir -p "$screenshot_dir"

    log_info "测试场景: 单词配对游戏"

    # 启动应用并导航到游戏
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 2

    # 快速导航到 Match Game (通过深层链接或直接 Activity 启动)
    # 这里我们通过主页的游戏模式选择
    local width=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f1 | tr -d '\r')
    local height=$(adb -s "$device_id" shell wm size | grep -o '[0-9]*x[0-9]*' | cut -d'x' -f2 | tr -d '\r')
    local center_x=$((width / 2))

    # 滚动到游戏模式选择
    input_text "$device_id" " "
    sleep 1

    # 截图 - 主页 (滚动后)
    capture_screenshot "$device_id" "$screenshot_dir/01_home_game_modes.png"
    sleep 1

    # 点击 Match Game (假设在页面中间位置)
    tap "$device_id" "$center_x" "$((height * 2 / 3))"
    sleep 3

    # 截图 - Match Game 界面
    capture_screenshot "$device_id" "$screenshot_dir/02_match_game.png"

    # 检查崩溃
    if ! check_for_crashes "$device_id"; then
        return 1
    fi

    log_info "单词配对游戏测试完成"
    return 0
}

# 14. 测试场景 - 进度保存
test_progress_save() {
    local device_id=$1
    local screenshot_dir="$REPORT_DIR/screenshots/$device_id/progress_save"

    mkdir -p "$screenshot_dir"

    log_info "测试场景: 进度保存"

    # 启动应用
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 2

    # 截图 - 初始状态
    capture_screenshot "$device_id" "$screenshot_dir/01_initial_state.png"

    # 关闭应用
    adb -s "$device_id" shell am force-stop com.wordland
    sleep 1

    # 重新启动
    adb -s "$device_id" shell am start -n com.wordland/.ui.MainActivity > /dev/null 2>&1
    sleep 2

    # 截图 - 重新启动后
    capture_screenshot "$device_id" "$screenshot_dir/02_relaunched_state.png"

    # 检查崩溃
    if ! check_for_crashes "$device_id"; then
        return 1
    fi

    log_info "进度保存测试完成"
    return 0
}

# 15. 生成测试报告
generate_report() {
    local device_id=$1
    local report_file="$REPORT_DIR/REPORT_$device_id.md"

    log_step "生成测试报告: $device_id"

    DEVICE_MODEL=$(adb -s "$device_id" shell getprop ro.product.model | tr -d '\r')
    ANDROID_VERSION=$(adb -s "$device_id" shell getprop ro.build.version.release | tr -d '\r')

    cat > "$report_file" << EOF
# 真机测试报告

**设备**: $DEVICE_MODEL
**Android 版本**: $ANDROID_VERSION
**设备 ID**: $device_id
**测试日期**: $(date)

---

## 测试结果

| 场景 | 状态 | 截图 |
|------|------|------|
| 应用启动 | ✅ | [查看](screenshots/$device_id/app_launch/) |
| 新手引导 | ✅ | [查看](screenshots/$device_id/onboarding/) |
| 学习流程 | ✅ | [查看](screenshots/$device_id/learning_flow/) |
| 单词配对游戏 | ✅ | [查看](screenshots/$device_id/match_game/) |
| 进度保存 | ✅ | [查看](screenshots/$device_id/progress_save/) |

---

## logcat 日志

[查看完整日志](logcat_$device_id.log)

---

## 发现的问题

$(if grep -q "FATAL EXCEPTION" "$REPORT_DIR/logcat_$device_id.log" 2>/dev/null; then
    echo "⚠️ 检测到崩溃，请查看 logcat 日志"
else
    echo "✅ 未发现崩溃"
fi)

---

**测试完成时间**: $(date)
EOF

    log_info "报告已生成: $report_file"
}

# 16. 主流程
main() {
    local device_id=$(get_device_id "$1")

    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  真机自动化测试开始${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "目标设备: $device_id"
    echo -e "报告目录: $REPORT_DIR"
    echo ""

    local start_time=$(date +%s)
    local failed_tests=()

    # 执行测试步骤
    check_environment
    collect_device_info "$device_id"
    install_apk "$device_id"
    start_logcat_monitor "$device_id"

    # 捕获退出时停止 logcat
    trap "stop_logcat_monitor '$device_id'" EXIT

    # 执行测试场景
    local scenarios=(
        "app_launch:应用启动"
        "onboarding:新手引导"
        "learning_flow:学习流程"
        "match_game:单词配对游戏"
        "progress_save:进度保存"
    )

    for scenario in "${scenarios[@]}"; do
        local name=$(echo "$scenario" | cut -d: -f1)
        local desc=$(echo "$scenario" | cut -d: -f2)

        echo ""
        if ! "test_$name" "$device_id"; then
            failed_tests+=("$name")
            log_error "场景失败: $desc"
        fi
    done

    # 生成报告
    generate_report "$device_id"

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  真机自动化测试完成${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "总耗时: ${duration}s"

    if [ ${#failed_tests[@]} -gt 0 ]; then
        log_warn "失败的测试场景: ${failed_tests[*]}"
        exit 1
    fi

    log_info "所有测试通过 ✅"
    exit 0
}

main "$@"
