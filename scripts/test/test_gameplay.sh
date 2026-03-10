#!/bin/bash

# Wordland 游戏玩法测试脚本
# 测试拼写战斗功能

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🎮 Wordland 游戏玩法测试"
echo "================================"
echo ""

# 键盘布局坐标 (1080x2400 屏幕)
# 第一行: Q W E R T Y U I O P (y ≈ 1850)
# 第二行: A S D F G H J K L (y ≈ 1950)
# 第三行: Z X C V B N M (y ≈ 2050)

# 定义按键坐标
declare -A KEY_X
KEY_X[Q]=200; KEY_X[W]=300; KEY_X[E]=400; KEY_X[R]=500; KEY_X[T]=600
KEY_X[Y]=700; KEY_X[U]=800; KEY_X[I]=900; KEY_X[O]=1000; KEY_X[P]=1100

KEY_X[A]=250; KEY_X[S]=350; KEY_X[D]=450; KEY_X[F]=550; KEY_X[G]=650
KEY_X[H]=750; KEY_X[J]=850; KEY_X[K]=950; KEY_X[L]=1050

KEY_X[Z]=350; KEY_X[X]=450; KEY_X[C]=550; KEY_X[V]=650
KEY_X[B]=750; KEY_X[N]=850; KEY_X[M]=950

# 点击函数
tap_key() {
    local key=$1
    local x=${KEY_X[$key]}
    local y=1950  # 默认第二行
    [[ $key =~ [QWERTYUIOP] ]] && y=1850  # 第一行
    [[ $key =~ [ZXCVBNM] ]] && y=2050  # 第三行

    echo "  按键: $key ($x, $y)"
    $ADB shell input tap $x $y
    sleep 0.3
}

# 输入单词
type_word() {
    local word=$1
    echo "  拼写: $word"
    for (( i=0; i<${#word}; i++ )); do
        local char="${word:$i:1}"
        tap_key "$char"
    done
}

# 截图函数
capture() {
    local name=$1
    echo "📸 截图: $name"
    $ADB shell screencap -p /sdcard/game_${name}.png
    $ADB pull /sdcard/game_${name}.png /tmp/game_${name}.png > /dev/null 2>&1
    sleep 2
}

# 启动到游戏界面
echo "🚀 启动应用并导航到游戏"
$ADB shell am start -n com.wordland/.ui.MainActivity
sleep 3
capture "00_home"

echo "点击岛屿地图..."
$ADB shell input tap 540 1100
sleep 2
capture "01_island"

echo "点击 Look Island..."
$ADB shell input tap 540 1300
sleep 2
capture "02_levels"

echo "点击 Level 1..."
$ADB shell input tap 540 1000
sleep 2
capture "03_learning_screen"

# 测试第一个词
echo ""
echo "🎯 测试词 1: look"
type_word "LOOK"
echo "  点击提交..."
$ADB shell input tap 540 2150  # 提交按钮位置
sleep 3
capture "04_word1_complete"

echo "点击继续..."
$ADB shell input tap 540 1500  # 继续按钮位置
sleep 2
capture "05_word2_start"

echo ""
echo "⏸️ 自动化测试暂停"
echo ""
echo "✅ 已完成的测试:"
echo "  1. 应用启动"
echo "  2. 导航到游戏界面"
echo "  3. 拼写第一个词: LOOK"
echo ""
echo "📋 请手动完成剩余测试:"
echo "  - 继续拼写剩余5个词"
echo "  - 完成关卡"
echo "  - 查看星星评分"
echo "  - 返回主界面"
echo ""
echo "📸 测试截图:"
echo "  /tmp/game_00_home.png - 主界面"
echo "  /tmp/game_01_island.png - 岛屿地图"
echo "  /tmp/game_02_levels.png - 关卡选择"
echo "  /tmp/game_03_learning_screen.png - 游戏界面"
echo "  /tmp/game_04_word1_complete.png - 第一个词完成"
echo "  /tmp/game_05_word2_start.png - 第二个词开始"
echo ""
echo "💡 查看所有截图:"
echo "  open /tmp/game_*.png"
