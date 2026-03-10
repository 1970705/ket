#!/bin/bash
# Epic #8.2 半自动化测试脚本
# 说明: 此脚本自动化文本输入，但用户仍需手动点击屏幕位置
# 使用方法:
# 1. 运行此脚本启动场景
# 2. 在设备上导航到游戏界面
# 3. 按Enter键，脚本将自动输入文本
# 4. 用户点击提交

DEVICE="5369b23a"
SCREENSHOT_DIR="docs/screenshots/epic8"

# 清除并启动App
prepare_scenario() {
    local num=$1
    local name=$2
    echo "=========================================="
    echo "场景 $num: $name"
    echo "=========================================="
    adb -s $DEVICE shell pm clear com.wordland >/dev/null 2>&1
    sleep 2
    adb -s $DEVICE shell am start -n com.wordland/.ui.MainActivity >/dev/null 2>&1
    sleep 2
    echo "✓ App已启动"
    echo "导航: 主页 → 岛屿地图 → Look Island → Level 1"
    echo "到达游戏界面后按 Enter 键继续..."
    read
}

# 输入单词
type_word() {
    local word=$1
    local wait_time=${2:-3}
    echo "输入: $word"
    adb -s $DEVICE shell input text "$word"
    sleep $wait_time
    # 模拟点击提交 (需要用户手动点击或提供坐标)
    echo "请点击提交按钮 (或按Enter继续)..."
    read
}

# 场景1: Perfect Performance
scenario1() {
    prepare_scenario 1 "Perfect Performance (预期★★★)"
    echo "开始自动输入单词..."
    type_word "look" 4
    type_word "see" 4
    type_word "watch" 4
    type_word "eye" 4
    type_word "glass" 4
    type_word "find" 4
    echo "场景1完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario1_perfect.png
}

# 场景2: All Hints
scenario2() {
    prepare_scenario 2 "All Hints (预期★★)"
    echo "对于每个单词:"
    echo "1. 点击提示按钮 (您需要手动点击)"
    echo "2. 按Enter继续自动输入"
    for word in look see watch eye glass find; do
        echo "=== 单词: $word ==="
        echo "请点击提示按钮，然后按Enter..."
        read
        adb -s $DEVICE shell input text "$word"
        echo "请点击提交，然后按Enter..."
        read
    done
    echo "场景2完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario2_hints.png
}

# 场景3: Mixed Accuracy (4正确, 2错误)
scenario3() {
    prepare_scenario 3 "Mixed Accuracy (预期★★)"
    echo "4个正确, 2个错误"
    type_word "look" 3
    type_word "see" 3
    type_word "watch" 3
    type_word "eye" 3
    echo "=== 故意错误: glas ==="
    adb -s $DEVICE shell input text "glas"
    echo "请点击提交 (应该显示错误)，然后按Enter..."
    read
    echo "=== 故意错误: fin ==="
    adb -s $DEVICE shell input text "fin"
    echo "请点击提交，然后按Enter..."
    read
    echo "场景3完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario3_mixed.png
}

# 场景4: Guessing (快速输入)
scenario4() {
    prepare_scenario 4 "Guessing (预期★)"
    echo "快速输入 (<1.5秒/词)"
    for word in look see watch eye glass find; do
        echo "=== $word ==="
        adb -s $DEVICE shell input text "$word"
        sleep 0.5
        echo "请快速点击提交！"
        sleep 1
    done
    echo "场景4完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario4_guessing.png
}

# 场景5: High Combo
scenario5() {
    prepare_scenario 5 "High Combo (预期★★★)"
    echo "建立5连击，然后故意错误"
    type_word "look" 4
    type_word "see" 4
    type_word "watch" 4
    type_word "eye" 4
    type_word "glass" 4
    echo "=== 故意错误: fin (重置combo) ==="
    adb -s $DEVICE shell input text "fin"
    echo "请点击提交，然后按Enter..."
    read
    echo "场景5完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario5_combo.png
}

# 场景6: Slow (每个词等20秒)
scenario6() {
    prepare_scenario 6 "Slow Performance (预期★★★)"
    echo "每个单词等待20秒..."
    for word in look see watch eye glass find; do
        echo "=== $word ==="
        echo "等待20秒..."
        for i in {20..1}; do
            echo -ne "$i秒\r"
            sleep 1
        done
        echo ""
        adb -s $DEVICE shell input text "$word"
        echo "请点击提交，然后按Enter..."
        read
    done
    echo "场景6完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario6_slow.png
}

# 场景7: One Wrong
scenario7() {
    prepare_scenario 7 "One Wrong (预期★★)"
    type_word "look" 3
    type_word "see" 3
    type_word "watch" 3
    type_word "eye" 3
    type_word "glass" 3
    echo "=== 故意错误: finx ==="
    adb -s $DEVICE shell input text "finx"
    echo "请点击提交，然后按Enter..."
    read
    echo "场景7完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario7_onewrong.png
}

# 场景8: Multiple Wrong
scenario8() {
    prepare_scenario 8 "Multiple Wrong (预期★)"
    echo "3个正确, 3个错误"
    type_word "look" 3
    echo "=== 错误: se ==="
    adb -s $DEVICE shell input text "se"
    echo "请点击提交，然后按Enter..."
    read
    type_word "watch" 3
    echo "=== 错误: ey ==="
    adb -s $DEVICE shell input text "ey"
    echo "请点击提交，然后按Enter..."
    read
    type_word "find" 3
    echo "场景8完成！捕获截图..."
    adb -s $DEVICE shell screencap -p > $SCREENSHOT_DIR/scenario8_multiplewrong.png
}

# 主菜单
case "$1" in
    1) scenario1 ;;
    2) scenario2 ;;
    3) scenario3 ;;
    4) scenario4 ;;
    5) scenario5 ;;
    6) scenario6 ;;
    7) scenario7 ;;
    8) scenario8 ;;
    all)
        scenario1
        scenario2
        scenario3
        scenario4
        scenario5
        scenario6
        scenario7
        scenario8
        ;;
    *)
        echo "Epic #8.2 半自动化测试脚本"
        echo ""
        echo "使用方法: $0 <场景编号|all>"
        echo ""
        echo "场景:"
        echo "  1 - Perfect Performance (预期★★★)"
        echo "  2 - All With Hints (预期★★)"
        echo "  3 - Mixed Accuracy (预期★★)"
        echo "  4 - Guessing (预期★)"
        echo "  5 - High Combo (预期★★★)"
        echo "  6 - Slow Performance (预期★★★)"
        echo "  7 - One Wrong (预期★★)"
        echo "  8 - Multiple Wrong (预期★)"
        echo "  all - 执行全部场景"
        echo ""
        echo "注意: 此脚本自动化文本输入，但您仍需手动点击提交按钮"
        ;;
esac

echo ""
echo "=========================================="
echo "测试完成！"
echo "截图目录: $SCREENSHOT_DIR"
ls -lh $SCREENSHOT_DIR/scenario*.png 2>/dev/null || echo "未找到截图"
