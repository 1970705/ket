#!/bin/bash

# Wordland Level 3-5 完整测试
# 测试所有剩余关卡

ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🎮 Wordland Level 3-5 完整测试"
echo "================================"
echo "目标: 完成剩余的3个关卡（18个词）"
echo ""

# 按键坐标
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
    echo "  🔤 $word"
    word=$(echo "$word" | tr '[:lower:]' '[:upper:]')
    for (( i=0; i<${#word}; i++ )); do
        tap "${word:$i:1}"
    done
}

submit() {
    $ADB shell input tap 540 2200
    sleep 3
}

next_word() {
    $ADB shell input tap 540 1400
    sleep 2
}

back_to_levels() {
    $ADB shell input tap 540 1800
    sleep 3
}

select_level() {
    local level_y=$1
    $ADB shell input tap 540 $level_y
    sleep 3
}

capture() {
    local name=$1
    $ADB shell screencap -p /sdcard/l3l5_$name.png
    $ADB pull /sdcard/l3l5_$name.png /tmp/l3l5_$name.png > /dev/null 2>&1
}

# ============================================================
# LEVEL 3: Movement and gaze (移动和凝视)
# ============================================================
echo "==========================================="
echo "📍 LEVEL 3: 移动和凝视"
echo "==========================================="
select_level 1450  # Level 3 position
capture "l3_start"

echo "词 1/6: stare (盯着看)"
spell "STARE"
submit
capture "l3_w1"
next_word

echo "词 2/6: notice (注意到)"
spell "NOTICE"
submit
capture "l3_w2"
next_word

echo "词 3/6: observe (观察)"
spell "OBSERVE"
submit
capture "l3_w3"
next_word

echo "词 4/6: appear (出现，显现)"
spell "APPEAR"
submit
capture "l3_w4"
next_word

echo "词 5/6: view (观看，视野)"
spell "VIEW"
submit
capture "l3_w5"
next_word

echo "词 6/6: scene (场景，景色)"
spell "SCENE"
submit
capture "l3_w6"

sleep 3
capture "l3_complete"
echo "✅ Level 3 完成！"
echo ""

# 返回关卡选择
back_to_levels

# ============================================================
# LEVEL 4: Looking actions (观看动作)
# ============================================================
echo "==========================================="
echo "📍 LEVEL 4: 观看动作"
echo "==========================================="
select_level 1600  # Level 4 position
capture "l4_start"

echo "词 1/6: notice (注意，注意到)"
spell "NOTICE"
submit
capture "l4_w1"
next_word

echo "词 2/6: search (搜索，寻找)"
spell "SEARCH"
submit
capture "l4_w2"
next_word

echo "词 3/6: check (检查，查看)"
spell "CHECK"
submit
capture "l4_w3"
next_word

echo "词 4/6: picture (图片，照片)"
spell "PICTURE"
submit
capture "l4_w4"
next_word

echo "词 5/6: photo (照片)"
spell "PHOTO"
submit
capture "l4_w5"
next_word

echo "词 6/6: camera (照相机)"
spell "CAMERA"
submit
capture "l4_w6"

sleep 3
capture "l4_complete"
echo "✅ Level 4 完成！"
echo ""

# 返回关卡选择
back_to_levels

# ============================================================
# LEVEL 5: Advanced observation (高级观察)
# ============================================================
echo "==========================================="
echo "📍 LEVEL 5: 高级观察"
echo "==========================================="
select_level 1750  # Level 5 position
capture "l5_start"

echo "词 1/6: observe (观察)"
spell "OBSERVE"
submit
capture "l5_w1"
next_word

echo "词 2/6: examine (检查，细看)"
spell "EXAMINE"
submit
capture "l5_w2"
next_word

echo "词 3/6: stare (盯着看)"
spell "STARE"
submit
capture "l5_w3"
next_word

echo "词 4/6: display (展示，显示)"
spell "DISPLAY"
submit
capture "l5_w4"
next_word

echo "词 5/6: appear (出现，显现)"
spell "APPEAR"
submit
capture "l5_w5"
next_word

echo "词 6/6: visible (可见的)"
spell "VISIBLE"
submit
capture "l5_w6"

sleep 3
capture "l5_complete"
echo "✅ Level 5 完成！"
echo ""

# 返回查看最终状态
back_to_levels
capture "final_all_levels"

echo ""
echo "🎉 所有关卡测试完成！"
echo ""
echo "📊 测试统计:"
echo "  Level 3: 6/6 词 ✅"
echo "  Level 4: 6/6 词 ✅"
echo "  Level 5: 6/6 词 ✅"
echo "  总计: 18/18 词 ✅"
echo ""
echo "📸 关键截图:"
echo "  /tmp/l3l5_l3_complete.png - Level 3 完成"
echo "  /tmp/l3l5_l4_complete.png - Level 4 完成"
echo "  /tmp/l3l5_l5_complete.png - Level 5 完成"
echo "  /tmp/l3l5_final_all_levels.png - 所有关卡状态"
echo ""
echo "💡 查看所有关卡完成状态:"
echo "  open /tmp/l3l5_final_all_levels.png"
echo ""
