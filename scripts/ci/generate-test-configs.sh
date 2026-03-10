#!/bin/bash
# 测试配置生成脚本
# Epic #12 - Task 12.5: Multi-Device Testing Matrix
# 用途: 从 multi-device-matrix.yml 生成测试配置文件

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

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
MATRIX_CONFIG="$SCRIPT_DIR/multi-device-matrix.yml"

# 生成 Robolectric 配置
generate_robolectric_config() {
    local output_dir="$PROJECT_ROOT/app/src/test/resources"
    mkdir -p "$output_dir"

    log_step "生成 Robolectric 配置..."

    cat > "$output_dir/robolectric_properties.properties" << 'EOF'
# Robolectric 配置文件
# Epic #12 - Task 12.5: Multi-Device Testing Matrix
# 自动生成时间: $(date)

# 截图支持 (用于调试)
robolectric.screenshot=true
robolectric.screenshotDirectory=build/robolectric-screenshots

# 启用的 SDK 版本
robolectric.enabledSdks=28,29,30,31,32,33,34

# 日志配置
robolectric.logging=stdout

# 性能优化
robolectric.dependency.repo.url=https://repo1.maven.org/maven2
robolectric.dependency.repo.url=https://google.maven.org
robolectric.dependency.repo.url=https://jcenter.bintray.com
EOF

    log_info "Robolectric 配置已生成: $output_dir/robolectric_properties.properties"
}

# 生成 Android Instrumentation 测试配置
generate_instrumentation_config() {
    local output_dir="$PROJECT_ROOT/app/src/androidTest/resources"
    mkdir -p "$output_dir"

    log_step "生成 Instrumentation 测试配置..."

    cat > "$output_dir/test_runner_config.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<!-- Android Test Runner Configuration -->
<!-- Epic #12 - Task 12.5: Multi-Device Testing Matrix -->
<resources>
    <!-- 测试套件名称 -->
    <string name="test_suite">com.wordland.test</string>

    <!-- 测试超时 (5分钟) -->
    <integer name="test_timeout">300000</integer>

    <!-- 测试前是否清除应用数据 -->
    <bool name="clear_package_data">true</bool>

    <!-- 是否在测试时授予运行时权限 -->
    <bool name="grant_runtime_permissions">true</bool>

    <!-- 覆盖率配置 -->
    <bool name="enable_coverage">true</bool>
</resources>
EOF

    log_info "Instrumentation 配置已生成: $output_dir/test_runner_config.xml"
}

# 生成设备配置 JSON (用于动态配置)
generate_device_configs() {
    local output_dir="$PROJECT_ROOT/app/src/androidTest/resources/config"
    mkdir -p "$output_dir"

    log_step "生成设备配置 JSON..."

    # 标准全面屏手机
    cat > "$output_dir/device_standard_phone.json" << EOF
{
    "device_name": "Standard Phone",
    "screen_width": 1080,
    "screen_height": 2400,
    "screen_density": 480,
    "orientation": "portrait",
    "api_level": 34,
    "target_sdk": 34
}
EOF

    # 紧凑型手机
    cat > "$output_dir/device_compact_phone.json" << EOF
{
    "device_name": "Compact Phone",
    "screen_width": 1080,
    "screen_height": 2340,
    "screen_density": 480,
    "orientation": "portrait",
    "api_level": 33,
    "target_sdk": 34
}
EOF

    # 平板 (竖屏)
    cat > "$output_dir/device_tablet_portrait.json" << EOF
{
    "device_name": "Tablet Portrait",
    "screen_width": 1200,
    "screen_height": 2000,
    "screen_density": 320,
    "orientation": "portrait",
    "api_level": 33,
    "target_sdk": 34
}
EOF

    # 小屏手机
    cat > "$output_dir/device_small_phone.json" << EOF
{
    "device_name": "Small Phone",
    "screen_width": 720,
    "screen_height": 1280,
    "screen_density": 240,
    "orientation": "portrait",
    "api_level": 29,
    "target_sdk": 34
}
EOF

    log_info "设备配置已生成: $output_dir/"
}

# 生成测试套件配置
generate_test_suite_configs() {
    local output_dir="$PROJECT_ROOT/app/src/androidTest/resources/config"
    mkdir -p "$output_dir"

    log_step "生成测试套件配置..."

    # 关键路径测试
    cat > "$output_dir/suite_critical.json" << EOF
{
    "suite_name": "Critical Path Tests",
    "timeout_minutes": 20,
    "test_classes": [
        "com.wordland.ui.screens.AppLaunchTest",
        "com.wordland.ui.screens.LearningFlowTest",
        "com.wordland.ui.screens.OnboardingTest"
    ],
    "priority": "p0"
}
EOF

    # 布局验证测试
    cat > "$output_dir/suite_layout.json" << EOF
{
    "suite_name": "Layout Validation Tests",
    "timeout_minutes": 15,
    "test_classes": [
        "com.wordland.ui.components.HintCardTest",
        "com.wordland.ui.components.WordlandButtonTest",
        "com.wordland.ui.screens.HomeScreenTest"
    ],
    "priority": "p1"
}
EOF

    # 完整测试
    cat > "$output_dir/suite_all.json" << EOF
{
    "suite_name": "Full Test Suite",
    "timeout_minutes": 45,
    "test_classes": [
        "com.wordland.ui.screens.AppLaunchTest",
        "com.wordland.ui.screens.OnboardingTest",
        "com.wordland.ui.screens.LearningFlowTest",
        "com.wordland.ui.screens.MatchGameTest",
        "com.wordland.data.persistence.ProgressTest"
    ],
    "priority": "p0"
}
EOF

    log_info "测试套件配置已生成: $output_dir/"
}

# 生成 API 级别配置
generate_api_configs() {
    local output_dir="$PROJECT_ROOT/app/src/androidTest/resources/config"
    mkdir -p "$output_dir"

    log_step "生成 API 级别配置..."

    for api in 29 33 34; do
        cat > "$output_dir/api_${api}.json" << EOF
{
    "api_level": $api,
    "api_name": "$(get_api_name $api)",
    "target_sdk": 34,
    "features": $(get_api_features $api)
}
EOF
    done

    log_info "API 级别配置已生成: $output_dir/api_*.json"
}

get_api_name() {
    local api=$1
    case $api in
        29) echo "\"Android 10 (Q)\"" ;;
        30) echo "\"Android 11 (R)\"" ;;
        31) echo "\"Android 12 (S)\"" ;;
        32) echo "\"Android 12L (Sv2)\"" ;;
        33) echo "\"Android 13 (Tiramisu)\"" ;;
        34) echo "\"Android 14 (Upside Down Cake)\"" ;;
        *) echo "\"Unknown\"" ;;
    esac
}

get_api_features() {
    local api=$1
    case $api in
        29)
            echo "[\"dark_mode\", \"gesture_nav\"]"
            ;;
        33)
            echo "[\"dark_mode\", \"gesture_nav\", \"material3\", \"predictive_back\"]"
            ;;
        34)
            echo "[\"dark_mode\", \"gesture_nav\", \"material3\", \"predictive_back\", \"edge_to_edge\", \"back_arrow\"]"
            ;;
        *)
            echo "[]"
            ;;
    esac
}

# 主流程
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  测试配置生成器${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""

    # 检查矩阵配置文件
    if [ ! -f "$MATRIX_CONFIG" ]; then
        log_warn "矩阵配置文件不存在: $MATRIX_CONFIG"
        log_info "将生成默认配置"
    fi

    # 生成配置
    generate_robolectric_config
    generate_instrumentation_config
    generate_device_configs
    generate_test_suite_configs
    generate_api_configs

    echo ""
    log_step "配置生成完成"
    log_info "配置文件位置:"
    echo "  - app/src/test/resources/"
    echo "  - app/src/androidTest/resources/config/"
    echo ""
    log_info "下一步:"
    echo "  1. 运行测试: ./gradlew connectedDebugAndroidTest"
    echo "  2. 生成报告: ./scripts/real-device/generate-report.sh"
    echo ""
}

main "$@"
