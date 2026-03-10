#!/bin/bash

# Wordland Level 2 完整测试
# 主题：颜色和明暗

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🎮 Wordland Level 2 完整测试"
echo "================================"
echo "主题: 颜色和明暗 (6个词)"
echo ""

# 按键坐标 (与之前相同)
declare -A KX
KX[Q]=180; KX[W]=280; KX[E]=380; KX[R]=480; KX[T]=580
KX[Y]=680; KX[U]=780; KX[I]=880; KX[O]=980; KX[P]=1080
KX[A]=230; KX[S]=330; KX[D]=430; KX[F]=530; KX[G]=630
KX[H]=730; KX[J]=830; KX[K]=930; KX[L]=1030
KX[Z]=330; KX[X]=430; KX[C]=530; KX[V]=630
KX[B]=730; KX[N]=830; KX[M]=930

tap() {
    local key=$1
    local x=${KX[$key]}
    local y=1950
    [[ $key =~ [QWERTYUIOP] ]] && y=1850
    [[ $key =~ [ZXCVBNM] ]] && y=2050
    $ADB shell input tap $x $y
    sleep 0.3
}

spell() {
    local word=$1
    echo "  🔤 拼写: $word"
    word=$(echo "$word" | tr '[:lower:]' '[:upper:]')
    for (( i=0; i<${#word}; i++ )); do
        tap "${word:$i:1}"
    done
}

submit() {
    echo "  ✅ 提交..."
    $ADB shell input tap 540 2200
    sleep 3
}

next_word() {
    echo "  ➡️ 继续..."
    $ADB shell input tap 540 1400
    sleep 2
}

capture() {
    local name=$1
    $ADB shell screencap -p /sdcard/l2_$name.png
    $ADB pull /sdcard/l2_$name.png /tmp/l2_$name.png > /dev/null 2>&1
}

echo "📍 当前应该在 Level 2 起始界面"
capture "00_start"
echo ""

# 词 1: COLOR
echo "🎯 词 1/6: color (颜色)"
spell "COLOR"
submit
capture "01_word1"
next_word
echo "✅ 完成"
echo ""

# 词 2: RED
echo "🎯 词 2/6: red (红色)"
spell "RED"
submit
capture "02_word2"
next_word
echo "✅ 完成"
echo ""

# 词 3: BLUE
echo "🎯 词 3/6: blue (蓝色)"
spell "BLUE"
submit
capture "03_word3"
next_word
echo "✅ 完成"
echo ""

# 词 4: DARK
echo "🎯 词 4/6: dark (黑暗的，深色的)"
spell "DARK"
submit
capture "04_word4"
next_word
echo "✅ 完成"
echo ""

# 词 5: LIGHT
echo "🎯 词 5/6: light (明亮的，浅色的)"
spell "LIGHT"
submit
capture "05_word5"
next_word
echo "✅ 完成"
echo ""

# 词 6: BRIGHT
echo "🎯 词 6/6: bright (明亮的)"
spell "BRIGHT"
submit
capture "06_word6"
echo "✅ 完成"
echo ""

echo "⏳ 等待关卡完成..."
sleep 3
capture "07_level_complete"

echo ""
echo "🎉 Level 2 完成！"
echo ""
echo "📸 测试截图:"
echo "  /tmp/l2_00_start.png - 起始界面"
echo "  /tmp/l2_01_word1.png - color"
echo "  /tmp/l2_02_word2.png - red"
echo "  /tmp/l2_03_word3.png - blue"
echo "  /tmp/l2_04_word4.png - dark"
echo "  /tmp/l2_05_word5.png - light"
echo "  /tmp/l2_06_word6.png - bright"
echo "  /tmp/l2_07_level_complete.png - 关卡完成"
echo ""
echo "💡 查看关卡完成界面:"
echo "  open /tmp/l2_07_level_complete.png"
echo ""
