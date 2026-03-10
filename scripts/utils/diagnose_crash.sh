#!/bin/bash

# Wordland 崩溃诊断脚本

echo "🔍 Wordland 真机崩溃诊断"
echo "================================"
echo ""

ADB="adb"

# 检查设备连接
echo "📱 步骤 1: 检查设备连接"
$ADB devices
echo ""

# 清空日志
echo "🧹 步骤 2: 清空旧日志"
$ADB logcat -c
echo "✅ 日志已清空"
echo ""

# 启动应用
echo "🚀 步骤 3: 启动应用"
$ADB shell am start -n com.wordland/.ui.MainActivity
echo "✅ 应用已启动"
echo ""

# 等待几秒
echo "⏳ 等待 3 秒..."
sleep 3
echo ""

# 查看崩溃日志
echo "💥 步骤 4: 查找崩溃日志"
echo "================================"
echo ""
echo "📋 FATAL EXCEPTION (崩溃信息):"
echo "----------------------------------------"
$ADB logcat -d -s AndroidRuntime:E | grep -A 30 "FATAL" | tail -50
echo ""

echo "📋 Wordland 应用日志:"
echo "----------------------------------------"
$ADB logcat -d | grep -i "wordland" | tail -30
echo ""

echo "📋 最近错误日志:"
echo "----------------------------------------"
$ADB logcat -d *:E | tail -20
echo ""

# 保存完整日志
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
LOG_FILE="crash_log_$TIMESTAMP.txt"

echo "💾 步骤 5: 保存完整日志"
$ADB logcat -d > $LOG_FILE
echo "✅ 完整日志已保存: $LOG_FILE"
echo ""

echo "================================"
echo "✅ 诊断完成！"
echo ""
echo "📝 请将上面的崩溃信息（FATAL EXCEPTION 部分）复制分享"
echo "   或者查看完整日志文件: cat $LOG_FILE"
echo ""
