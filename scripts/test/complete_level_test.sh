#!/bin/bash

# Wordland 完整关卡测试
# 自动完成 Level 1 的所有6个词

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🎮 Wordland 完整关卡自动化测试"
echo "================================"
echo "目标: 完成 Level 1 的所有6个词"
echo ""

# 按键坐标定义 (QWERTY 键盘)
# 第一行: Q(180) W(280) E(380) R(480) T(580) Y(680) U(780) I(880) O(980) P(1080) - y=1850
# 第二行: A(230) S(330) D(430) F(530) G(630) H(730) J(830) K(930) L(1030) - y=1950
# 第三行: Z(330) X(430) C(530) V(630) B(730) N(830) M(930) - y=2050

# 定义按键坐标
declare -A KX
KX[Q]=180; KX[W]=280; KX[E]=380; KX[R]=480; KX[T]=580
KX[Y]=680; KX[U]=780; KX[I]=880; KX[O]=980; KX[P]=1080

KX[A]=230; KX[S]=330; KX[D]=430; KX[F]=530; KX[G]=630
KX[H]=730; KX[J]=830; KX[K]=930; KX[L]=1030

KX[Z]=330; KX[X]=430; KX[C]=530; KX[V]=630
KX[B]=730; KX[N]=830; KX[M]=930

# 按键函数
tap() {
    local key=$1
    local x=${KX[$key]}
    local y=1950
    [[ $key =~ [QWERTYUIOP] ]] && y=1850
    [[ $key =~ [ZXCVBNM] ]] && y=2050
    $ADB shell input tap $x $y
    sleep 0.3
}

# 拼写单词
spell() {
    local word=$1
    echo "  🔤 拼写: $word"
    word=$(echo "$word" | tr '[:lower:]' '[:upper:]')
    for (( i=0; i<${#word}; i++ )); do
        tap "${word:$i:1}"
    done
}

# 提交答案
submit() {
    echo "  ✅ 提交答案..."
    $ADB shell input tap 540 2200  # 提交按钮
    sleep 3
}

# 继续下一个
next_word() {
    echo "  ➡️ 继续下一个..."
    $ADB shell input tap 540 1400  # 继续按钮
    sleep 2
}

# 截图
capture() {
    local name=$1
    $ADB shell screencap -p /sdcard/level_${name}.png
    $ADB pull /sdcard/level_${name}.png /tmp/level_${name}.png > /dev/null 2>&1
}

echo "📍 当前状态: 第二个词 'see' 开始"
echo ""

# 词 2: SEE
echo "🎯 词 2/6: see (看见，看到)"
spell "SEE"
submit
capture "02_word2_complete"
next_word
echo "✅ 词 2 完成"
echo ""

# 词 3: WATCH
echo "🎯 词 3/6: watch (观看，注视)"
spell "WATCH"
submit
capture "03_word3_complete"
next_word
echo "✅ 词 3 完成"
echo ""

# 词 4: EYE
echo "🎯 词 4/6: eye (眼睛)"
spell "EYE"
submit
capture "04_word4_complete"
next_word
echo "✅ 词 4 完成"
echo ""

# 词 5: GLASS
echo "🎯 词 5/6: glass (玻璃，眼镜)"
spell "GLASS"
submit
capture "05_word5_complete"
next_word
echo "✅ 词 5 完成"
echo ""

# 词 6: FIND
echo "🎯 词 6/6: find (发现，找到)"
spell "FIND"
submit
capture "06_word6_complete"
echo "✅ 词 6 完成"
echo ""

echo "⏳ 等待关卡完成界面..."
sleep 3
capture "07_level_complete"

echo ""
echo "🎉 关卡完成！"
echo ""
echo "📸 测试截图已保存:"
echo "  /tmp/level_02_word2_complete.png"
echo "  /tmp/level_03_word3_complete.png"
echo "  /tmp/level_04_word4_complete.png"
echo "  /tmp/level_05_word5_complete.png"
echo "  /tmp/level_06_word6_complete.png"
echo "  /tmp/level_07_level_complete.png"
echo ""
echo "💡 查看关卡完成界面:"
echo "  open /tmp/level_07_level_complete.png"
echo ""
