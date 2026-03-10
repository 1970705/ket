#!/bin/bash
# Epic #8.2 快速场景执行脚本
# 使用方法: ./epic8_quick_execute.sh <场景编号>

DEVICE="5369b23a"
SCREENSHOT_DIR="docs/screenshots/epic8"

show_help() {
    echo "Epic #8.2 快速场景执行脚本"
    echo ""
    echo "使用方法: ./epic8_quick_execute.sh <场景编号>"
    echo ""
    echo "场景说明:"
    echo "  1 - Perfect (★★★)     : 6/6正确, ~4秒/词, 无提示"
    echo "  2 - All Hints (★★)     : 6/6正确, 每个词用提示"
    echo "  3 - Mixed (★★)         : 4/6正确, 2错误"
    echo "  4 - Guessing (★)       : 6/6正确, <1.5秒/词"
    echo "  5 - High Combo (★★★)   : 5/6正确, combo=5"
    echo "  6 - Slow (★★★)         : 6/6正确, 20秒/词"
    echo "  7 - One Wrong (★★)     : 5/6正确, 1错误"
    echo "  8 - Multiple Wrong (★) : 3/6正确, 3错误"
    echo ""
    echo "完整测试: ./epic8_quick_execute.sh all"
}

prepare_scenario() {
    local num=$1
    echo "=========================================="
    echo "准备场景 $num"
    echo "=========================================="

    adb -s $DEVICE shell pm clear com.wordland
    sleep 2
    adb -s $DEVICE shell am start -n com.wordland/.ui.MainActivity
    sleep 1

    echo "✓ App已启动"
    echo ""
    echo "请在设备上执行场景 $num 测试"
    echo "导航: 主页 → 岫屿地图 → Look Island → Level 1"
    echo ""
    echo "按 Enter 键捕获截图..."
    read

    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario${num}_complete.png
    echo "✓ 截图已保存: $SCREENSHOT_DIR/scenario${num}_complete.png"
    echo ""
}

# 主逻辑
case "$1" in
    1|2|3|4|5|6|7|8)
        prepare_scenario $1
        ;;
    all)
        echo "执行全部8个场景..."
        for i in {1..8}; do
            prepare_scenario $i
        done
        echo "全部场景完成！"
        echo "截图目录: $SCREENSHOT_DIR"
        ls -lh $SCREENSHOT_DIR/scenario*_complete.png
        ;;
    *)
        show_help
        ;;
esac
