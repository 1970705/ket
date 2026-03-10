#!/bin/bash

# 测试返回功能和进度保存

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🔄 测试返回功能和进度保存"
echo "================================"
echo ""

echo "📍 步骤 1: 点击返回按钮"
echo "   坐标: (540, 1800)"
$ADB shell input tap 540 1800
sleep 3

echo "📸 截图: 关卡选择界面（应该显示 Level 1 已完成）"
$ADB shell screencap -p /sdcard/after_level.png
$ADB pull /sdcard/after_level.png /tmp/after_level.png > /dev/null 2>&1
sleep 2

echo ""
echo "📍 步骤 2: 返回主界面"
echo "   坐标: (100, 150)"
$ADB shell input tap 100 150
sleep 3

echo "📸 截图: 主界面"
$ADB shell screencap -p /sdcard/back_home.png
$ADB pull /sdcard/back_home.png /tmp/back_home.png > /dev/null 2>&1
sleep 2

echo ""
echo "✅ 返回测试完成！"
echo ""
echo "📸 测试截图:"
echo "  /tmp/after_level.png - Level 1 应该显示已完成状态"
echo "  /tmp/back_home.png - 返回到主界面"
echo ""
echo "💡 查看截图:"
echo "  open /tmp/after_level.png"
echo "  open /tmp/back_home.png"
echo ""
