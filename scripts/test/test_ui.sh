#!/bin/bash

# Wordland UI 自动化测试脚本
# 使用 adb 模拟用户操作

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🎮 Wordland UI 自动化测试"
echo "================================"

# 等待函数
wait_seconds() {
    echo "⏳ 等待 $1 秒..."
    sleep $1
}

# 截图函数
take_screenshot() {
    local name=$1
    echo "📸 截图: $name"
    $ADB shell screencap -p /sdcard/test_${name}.png
    $ADB pull /sdcard/test_${name}.png /tmp/test_${name}.png > /dev/null 2>&1
    open /tmp/test_${name}.png
    wait_seconds 2
}

# 点击函数（需要根据实际屏幕调整坐标）
tap() {
    local x=$1
    local y=$2
    local desc=$3
    echo "👆 点击: $desc ($x, $y)"
    $ADB shell input tap $x $y
    wait_seconds 1
}

# 启动应用
echo "🚀 启动 Wordland 应用..."
$ADB shell am start -n com.wordland/.ui.MainActivity
wait_seconds 3

take_screenshot "01_home"

# 测试导航
echo ""
echo "📍 测试 1: 主界面 → 岛屿地图"
tap 540 1000 "岛屿地图按钮"
wait_seconds 2

take_screenshot "02_island_map"

echo ""
echo "📍 测试 2: 岛屿地图 → Look Island"
tap 540 1200 "Look Island 卡片"
wait_seconds 2

take_screenshot "03_level_select"

echo ""
echo "📍 测试 3: 关卡选择 → Level 1"
tap 540 1000 "Level 1"
wait_seconds 2

take_screenshot "04_learning_screen"

echo ""
echo "⌨️ 测试 4: 拼写战斗游戏"
echo "请手动输入: L-O-O-K"
echo "坐标参考:"
echo "  - Q: (200, 1800)"
echo "  - W: (300, 1800)"
echo "  - E: (400, 1800)"
echo "  - R: (500, 1800)"
echo "  - T: (600, 1800)"
echo "  - Y: (700, 1800)"
echo "  - ..."
echo ""
echo "第二行键盘 (y=1900):"
echo "  - A: (250, 1900)"
echo "  - S: (350, 1900)"
echo "  - D: (450, 1900)"
echo "  - F: (550, 1900)"
echo "  - ..."
echo ""
echo "第三行键盘 (y=2000):"
echo "  - Z: (350, 2000)"
echo "  - L: (750, 2000)"
echo "  - K: (850, 2000)"
echo "  - 退格: (950, 2000)"

# 等待用户手动测试
echo ""
echo "⏸️ 脚本暂停，请手动测试以下功能:"
echo "  1. 使用虚拟键盘拼写 'look'"
echo "  2. 点击提交按钮"
echo "  3. 完成剩余 5 个词"
echo "  4. 查看关卡完成界面"
echo ""
read -p "按回车键继续查看最终截图..."

take_screenshot "05_final"

echo ""
echo "✅ 测试完成！"
echo ""
echo "📋 测试截图保存在 /tmp/ 目录:"
echo "  - test_01_home.png (主界面)"
echo "  - test_02_island_map.png (岛屿地图)"
echo "  - test_03_level_select.png (关卡选择)"
echo "  - test_04_learning_screen.png (学习界面)"
echo "  - test_05_final.png (最终状态)"
echo ""
echo "📊 查看应用日志:"
echo "  $ADB logcat | grep Wordland"
