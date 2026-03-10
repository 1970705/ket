#!/bin/bash

# 测试进度持久化 - 验证数据保存

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🔄 测试进度持久化"
echo "================================"
echo ""

echo "📍 步骤 1: 记录当前状态"
echo "   当前应该显示: Level 1 已完成，Level 2 已解锁"
$ADB shell screencap -p /sdcard/before_restart.png
$ADB pull /sdcard/before_restart.png /tmp/before_restart.png > /dev/null 2>&1
echo "📸 截图已保存: before_restart.png"
echo ""

echo "📍 步骤 2: 完全关闭应用"
echo "   命令: force-stop"
$ADB shell am force-stop com.wordland
sleep 3
echo "✅ 应用已完全关闭"
echo ""

echo "📍 步骤 3: 重新启动应用"
echo "   命令: start MainActivity"
$ADB shell am start -n com.wordland/.ui.MainActivity
sleep 5
echo "✅ 应用已重新启动"
echo ""

echo "📍 步骤 4: 导航到关卡选择"
echo "   点击: 岛屿地图 → Look Island"
$ADB shell input tap 540 1100  # 岛屿地图
sleep 2
$ADB shell input tap 540 1300  # Look Island
sleep 2
echo "✅ 已进入关卡选择界面"
echo ""

echo "📍 步骤 5: 验证进度保存"
echo "   预期: Level 1 仍显示已完成，Level 2 仍显示已解锁"
$ADB shell screencap -p /sdcard/after_restart.png
$ADB pull /sdcard/after_restart.png /tmp/after_restart.png > /dev/null 2>&1
echo "📸 截图已保存: after_restart.png"
echo ""

echo "📍 步骤 6: 尝试进入 Level 2"
echo "   点击: Level 2"
$ADB shell input tap 540 1150  # Level 2
sleep 3
$ADB shell screencap -p /sdcard/level2_start.png
$ADB pull /sdcard/level2_start.png /tmp/level2_start.png > /dev/null 2>&1
echo "📸 截图已保存: level2_start.png"
echo ""

echo "✅ 进度持久化测试完成！"
echo ""
echo "📋 测试结果验证:"
echo "  对比以下两张截图:"
echo "    1. /tmp/before_restart.png (重启前)"
echo "    2. /tmp/after_restart.png (重启后)"
echo ""
echo "  应该看到:"
echo "    - Level 1 状态保持不变（已完成）"
echo "    - Level 2 状态保持不变（已解锁）"
echo ""
echo "  查看 Level 2 起始界面:"
echo "    /tmp/level2_start.png"
echo ""
echo "💡 查看所有截图:"
echo "  open /tmp/before_restart.png"
echo "  open /tmp/after_restart.png"
echo "  open /tmp/level2_start.png"
echo ""
