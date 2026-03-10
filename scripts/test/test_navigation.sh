#!/bin/bash

# Wordland 导航自动化测试
# 使用 adb shell input 命令模拟用户点击

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"
SCREEN_WIDTH=1080
SCREEN_HEIGHT=2400

echo "🎮 Wordland 导航自动化测试"
echo "================================"
echo "屏幕尺寸: ${SCREEN_WIDTH}x${SCREEN_HEIGHT}"
echo ""

# 等待函数
wait_and_capture() {
    local label=$1
    echo "⏳ 等待 2 秒..."
    sleep 2
    echo "📸 截图: $label"
    $ADB shell screencap -p /sdcard/nav_${label}.png
    $ADB pull /sdcard/nav_${label}.png /tmp/nav_${label}.png > /dev/null 2>&1
}

# 启动应用
echo "🚀 步骤 1: 启动应用"
$ADB shell am start -n com.wordland/.ui.MainActivity
wait_and_capture "01_home"
echo "✅ 应用已启动 - 查看 /tmp/nav_01_home.png"
echo ""

# 测试导航到岛屿地图
echo "🚀 步骤 2: 点击 '岛屿地图' 按钮"
echo "   坐标: (540, 1100)"
$ADB shell input tap 540 1100
wait_and_capture "02_island_map"
echo "✅ 应该显示岛屿地图界面 - 查看 /tmp/nav_02_island_map.png"
echo ""

# 测试点击 Look Island
echo "🚀 步骤 3: 点击 'Look Island' 卡片"
echo "   坐标: (540, 1300)"
$ADB shell input tap 540 1300
wait_and_capture "03_level_select"
echo "✅ 应该显示关卡选择界面（5个关卡） - 查看 /tmp/nav_03_level_select.png"
echo ""

# 测试点击 Level 1
echo "🚀 步骤 4: 点击 'Level 1'"
echo "   坐标: (540, 1000)"
$ADB shell input tap 540 1000
wait_and_capture "04_learning_screen"
echo "✅ 应该显示学习界面（拼写战斗游戏） - 查看 /tmp/nav_04_learning_screen.png"
echo ""

# 返回
echo "🚀 步骤 5: 点击返回按钮"
echo "   坐标: (100, 150)"
$ADB shell input tap 100 150
wait_and_capture "05_back_to_levels"
echo "✅ 应该返回关卡选择界面 - 查看 /tmp/nav_05_back_to_levels.png"
echo ""

echo "🎉 导航测试完成！"
echo ""
echo "📋 测试截图:"
echo "  /tmp/nav_01_home.png - 主界面"
echo "  /tmp/nav_02_island_map.png - 岛屿地图"
echo "  /tmp/nav_03_level_select.png - 关卡选择"
echo "  /tmp/nav_04_learning_screen.png - 学习界面"
echo "  /tmp/nav_05_back_to_levels.png - 返回关卡"
echo ""
echo "💡 查看所有截图:"
echo "  open /tmp/nav_*.png"
