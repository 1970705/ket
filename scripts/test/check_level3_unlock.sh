#!/bin/bash

# 检查 Level 3 解锁状态

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🔄 检查 Level 3 解锁状态"
echo "================================"
echo ""

echo "📍 步骤 1: 点击返回按钮"
echo "   坐标: (540, 1800)"
$ADB shell input tap 540 1800
sleep 3

echo "📸 截图: 关卡选择界面"
echo "   预期: Level 1 和 Level 2 都已完成，Level 3 已解锁"
$ADB shell screencap -p /sdcard/level3_unlock.png
$ADB pull /sdcard/level3_unlock.png /tmp/level3_unlock.png > /dev/null 2>&1
echo "✅ 截图已保存"
echo ""

echo "📍 步骤 2: 尝试进入 Level 3"
echo "   坐标: (540, 1300) - Level 3 位置"
$ADB shell input tap 540 1300
sleep 3

echo "📸 截图: Level 3 起始界面"
$ADB shell screencap -p /sdcard/level3_start.png
$ADB pull /sdcard/level3_start.png /tmp/level3_start.png > /dev/null 2>&1
echo "✅ 截图已保存"
echo ""

echo "✅ 检查完成！"
echo ""
echo "📸 查看截图:"
echo "  /tmp/level3_unlock.png - 关卡选择（应显示 Level 3 已解锁）"
echo "  /tmp/level3_start.png - Level 3 起始界面"
echo ""
echo "💡 查看截图:"
echo "  open /tmp/level3_unlock.png"
echo "  open /tmp/level3_start.png"
echo ""
