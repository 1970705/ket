#!/bin/bash
# Epic #8 真机测试结果收集脚本
# 用于自动收集测试完成后的日志和截图

DEVICE="5369b23a"  # Xiaomi 24031PN0DC
SCREENSHOT_DIR="docs/screenshots/epic8"
LOG_DIR="docs/reports/testing"

mkdir -p "$SCREENSHOT_DIR"
mkdir -p "$LOG_DIR"

echo "=========================================="
echo "Epic #8 真机测试结果收集"
echo "=========================================="
echo ""

# 函数: 收集单个场景的结果
collect_scenario() {
    local scenario_num=$1
    local scenario_name=$2

    echo "正在收集 Scenario $scenario_num: $scenario_name ..."

    # 截图
    local screenshot_file="$SCREENSHOT_DIR/scenario${scenario_num}_${scenario_name}.png"
    adb -s $DEVICE shell screencap -p > "$screenshot_file"

    if [ -f "$screenshot_file" ]; then
        local size=$(ls -lh "$screenshot_file" | awk '{print $5}')
        echo "  ✅ 截图已保存: $screenshot_file ($size)"
    else
        echo "  ❌ 截图保存失败"
    fi

    # 保存日志
    local log_file="$LOG_DIR/epic8_scenario${scenario_num}.log"
    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$log_file"

    if [ -s "$log_file" ]; then
        local lines=$(wc -l < "$log_file")
        echo "  ✅ 日志已保存: $log_file ($lines 行)"
    else
        echo "  ⚠️  日志为空或未找到相关内容"
    fi

    # 清空logcat为下一个场景做准备
    adb -s $DEVICE logcat -c

    echo ""
}

# 主菜单
echo "选择要收集的场景:"
echo "1) Scenario 1: Perfect Performance"
echo "2) Scenario 2: All Hints"
echo "3) Scenario 3: Mixed Accuracy"
echo "4) Scenario 4: Guessing"
echo "5) Scenario 5: High Combo"
echo "6) Scenario 6: Slow"
echo "7) Scenario 7: One Wrong"
echo "8) Scenario 8: Multiple Wrong"
echo "a) 收集所有场景"
echo "q) 退出"
echo ""
read -p "请选择 (1-8/a/q): " choice

case $choice in
    1) collect_scenario 1 "perfect_3stars" ;;
    2) collect_scenario 2 "hints_2stars" ;;
    3) collect_scenario 3 "mixed_2stars" ;;
    4) collect_scenario 4 "guessing_1star" ;;
    5) collect_scenario 5 "combo_3stars" ;;
    6) collect_scenario 6 "slow_3stars" ;;
    7) collect_scenario 7 "onewrong_2stars" ;;
    8) collect_scenario 8 "multiplewrong_1star" ;;
    a)
        collect_scenario 1 "perfect_3stars"
        collect_scenario 2 "hints_2stars"
        collect_scenario 3 "mixed_2stars"
        collect_scenario 4 "guessing_1star"
        collect_scenario 5 "combo_3stars"
        collect_scenario 6 "slow_3stars"
        collect_scenario 7 "onewrong_2stars"
        collect_scenario 8 "multiplewrong_1star"
        ;;
    q) echo "退出..."; exit 0 ;;
    *) echo "无效选择" ;;
esac

echo "=========================================="
echo "结果收集完成！"
echo "=========================================="
echo ""
echo "截图目录: $SCREENSHOT_DIR"
echo "日志目录: $LOG_DIR"
echo ""
echo "查看日志中的星级计算结果:"
echo "  cat $LOG_DIR/epic8_scenario*.log | grep 'calculateStars'"
