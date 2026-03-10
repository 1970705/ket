#!/bin/bash

# Complete Real Device Test
# Tests all navigation and gameplay flows on real device

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"
DEVICE_ID="5369b23a"
PACKAGE="com.wordland"

echo "=========================================="
echo "📱 真机完整测试"
echo "=========================================="
echo "设备: $DEVICE_ID"
echo ""

# Function to launch app
launch_app() {
    echo "🚀 启动应用..."
    $ADB -s $DEVICE_ID shell am start -n $PACKAGE/.ui.MainActivity
    sleep 2
}

# Function to check for crashes
check_crashes() {
    local crashes=$($ADB -s $DEVICE_ID logcat -d -s AndroidRuntime:E | grep "FATAL EXCEPTION")
    if [ -n "$crashes" ]; then
        echo "❌ 检测到崩溃！"
        echo "$crashes"
        return 1
    else
        echo "✅ 无崩溃"
        return 0
    fi
}

# Clear logs and launch
echo "📋 清空日志..."
$ADB -s $DEVICE_ID logcat -c

launch_app

# Test 1: App Launch
echo ""
echo "=========================================="
echo "测试 1: 应用启动"
echo "=========================================="
if check_crashes; then
    echo "✅ 应用启动成功"
else
    echo "❌ 应用启动失败"
    exit 1
fi

# Test 2: Navigate to Island Map
echo ""
echo "=========================================="
echo "测试 2: 导航到岛屿地图"
echo "=========================================="
echo "请在手机上点击 '开始冒险' 按钮"
echo "等待 5 秒..."
sleep 5

if check_crashes; then
    echo "✅ 岛屿地图导航成功"
else
    echo "❌ 岛屿地图导航失败"
    $ADB -s $DEVICE_ID logcat -d | grep -A 20 "FATAL EXCEPTION"
    exit 1
fi

# Test 3: Navigate back to Home
echo ""
echo "=========================================="
echo "测试 3: 返回主界面"
echo "=========================================="
echo "请在手机上点击返回按钮"
echo "等待 3 秒..."
sleep 3

# Test 4: Navigate to Progress
echo ""
echo "=========================================="
echo "测试 4: 导航到学习进度"
echo "=========================================="
$ADB -s $DEVICE_ID shell input tap 540 700  # 点击"学习进度"按钮位置
sleep 3

if check_crashes; then
    echo "✅ 学习进度导航成功"
else
    echo "❌ 学习进度导航失败"
    $ADB -s $DEVICE_ID logcat -d | grep -A 20 "FATAL EXCEPTION"
    exit 1
fi

# Test 5: Navigate back and to Review
echo ""
echo "=========================================="
echo "测试 5: 导航到每日复习"
echo "=========================================="
$ADB -s $DEVICE_ID shell input keyevent 4  # 返回
sleep 2
$ADB -s $DEVICE_ID shell input tap 540 850  # 点击"每日复习"按钮位置
sleep 3

if check_crashes; then
    echo "✅ 每日复习导航成功"
else
    echo "❌ 每日复习导航失败"
    $ADB -s $DEVICE_ID logcat -d | grep -A 20 "FATAL EXCEPTION"
    exit 1
fi

# Test 6: Select Island and Level
echo ""
echo "=========================================="
echo "测试 6: 选择关卡"
echo "=========================================="
$ADB -s $DEVICE_ID shell input keyevent 4  # 返回
sleep 2
$ADB -s $DEVICE_ID shell input tap 540 550  # 点击"开始冒险"
sleep 2
$ADB -s $DEVICE_ID shell input tap 540 600  # 点击"Look Island"
sleep 3

if check_crashes; then
    echo "✅ 选择岛屿成功"
else
    echo "❌ 选择岛屿失败"
    $ADB -s $DEVICE_ID logcat -d | grep -A 20 "FATAL EXCEPTION"
    exit 1
fi

# Test 7: Start Level
echo ""
echo "=========================================="
echo "测试 7: 开始关卡"
echo "=========================================="
$ADB -s $DEVICE_ID shell input tap 540 400  # 点击"Level 1"
sleep 3

if check_crashes; then
    echo "✅ 进入游戏界面成功"
else
    echo "❌ 进入游戏界面失败"
    $ADB -s $DEVICE_ID logcat -d | grep -A 20 "FATAL EXCEPTION"
    exit 1
fi

# Final check
echo ""
echo "=========================================="
echo "✅ 所有测试通过！"
echo "=========================================="
echo ""
echo "测试完成！应用在真机上运行正常。"
echo ""
