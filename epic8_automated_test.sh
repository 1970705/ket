#!/bin/bash
# Epic #8 Task #8.2 - Automated Test Script
# Uses adb shell input to simulate user interaction

DEVICE="emulator-5554"
PACKAGE="com.wordland"
SCREENSHOT_DIR="docs/screenshots/epic8"
LOG_DIR="docs/reports/testing"

# Create directories
mkdir -p "$SCREENSHOT_DIR"
mkdir -p "$LOG_DIR"

# Helper function to tap screen coordinates
tap() {
    local x=$1
    local y=$2
    adb -s $DEVICE shell input tap $x $y
    sleep 0.5
}

# Helper function to type text
type_text() {
    local text=$1
    adb -s $DEVICE shell input text "$text"
    sleep 0.3
}

# Helper function to press enter
press_enter() {
    adb -s $DEVICE shell input keyevent KEYCODE_ENTER
    sleep 0.5
}

# Helper function to press back
press_back() {
    adb -s $DEVICE shell input keyevent KEYCODE_BACK
    sleep 0.5
}

# Helper function to clear and launch app
reset_app() {
    echo "Resetting app..."
    adb -s $DEVICE shell pm clear $PACKAGE
    sleep 2
    adb -s $DEVICE shell am start -n $PACKAGE/.ui.MainActivity
    sleep 3
}

# Helper function to navigate to Level 1
navigate_to_level1() {
    echo "Navigating to Level 1..."
    # These coordinates need to be adjusted based on actual screen
    # Home screen -> Island Map
    tap 500 800  # Approximate position for "岛屿地图"
    sleep 1
    tap 500 600  # Approximate position for "Look Island"
    sleep 1
    tap 500 700  # Approximate position for "Level 1"
    sleep 2
}

# Helper function to complete a word
complete_word() {
    local word=$1
    local wait_time=$2

    echo "Typing word: $word"
    # Type each letter
    for (( i=0; i<${#word}; i++ )); do
        local char="${word:$i:1}"
        adb -s $DEVICE shell input text "$char"
        sleep 0.2
    done

    # Wait specified time
    sleep $wait_time

    # Press submit button (need to find exact coordinates)
    # For now, use Enter key
    press_enter
    sleep 2
}

# Helper function to use hint and complete word
complete_with_hint() {
    local word=$1
    echo "Using hint for: $word"
    # Tap hint button (coordinates needed)
    tap 900 1600  # Approximate hint button position
    sleep 2
    complete_word "$word" 1
}

# ========================================
# SCENARIO 1: Perfect Performance (★★★ expected)
# ========================================
run_scenario1() {
    echo "=========================================="
    echo "Scenario 1: Perfect Performance"
    echo "=========================================="

    reset_app

    # Manual navigation required - coordinates vary by screen size
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # Complete all words correctly, ~4s per word
    complete_word "look" 4
    complete_word "see" 4
    complete_word "watch" 4
    complete_word "eye" 4
    complete_word "glass" 4
    complete_word "find" 4

    # Wait for level complete screen
    sleep 3

    # Screenshot
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario1_perfect_3stars.png"
    echo "Screenshot saved: scenario1_perfect_3stars.png"

    # Save logs
    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario1.log"
    echo "Logs saved: epic8_scenario1.log"
}

# ========================================
# SCENARIO 2: All With Hints (★★ expected)
# ========================================
run_scenario2() {
    echo "=========================================="
    echo "Scenario 2: All With Hints"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # Use hint for each word
    complete_with_hint "look"
    complete_with_hint "see"
    complete_with_hint "watch"
    complete_with_hint "eye"
    complete_with_hint "glass"
    complete_with_hint "find"

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario2_hints_2stars.png"
    echo "Screenshot saved: scenario2_hints_2stars.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario2.log"
    echo "Logs saved: epic8_scenario2.log"
}

# ========================================
# SCENARIO 3: Mixed Accuracy (★★ expected)
# ========================================
run_scenario3() {
    echo "=========================================="
    echo "Scenario 3: Mixed Accuracy (4/6 correct)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # 4 correct
    complete_word "look" 4
    complete_word "see" 4
    complete_word "watch" 4
    complete_word "eye" 4

    # 2 wrong (incomplete words)
    type_text "gla"
    press_enter
    sleep 2

    type_text "fi"
    press_enter
    sleep 2

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario3_mixed_2stars.png"
    echo "Screenshot saved: scenario3_mixed_2stars.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario3.log"
    echo "Logs saved: epic8_scenario3.log"
}

# ========================================
# SCENARIO 4: Guessing Detected (★ expected)
# ========================================
run_scenario4() {
    echo "=========================================="
    echo "Scenario 4: Guessing Detected (FAST!)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # Complete all words very fast (<1.5s each)
    complete_word "look" 0
    complete_word "see" 0
    complete_word "watch" 0
    complete_word "eye" 0
    complete_word "glass" 0
    complete_word "find" 0

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario4_guessing_1star.png"
    echo "Screenshot saved: scenario4_guessing_1star.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario4.log"
    echo "Logs saved: epic8_scenario4.log"
}

# ========================================
# SCENARIO 5: High Combo (★★★ expected)
# ========================================
run_scenario5() {
    echo "=========================================="
    echo "Scenario 5: High Combo (5-combo streak)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # Build 5-combo
    complete_word "look" 5
    complete_word "see" 5
    complete_word "watch" 5
    complete_word "eye" 5
    complete_word "glass" 5

    # Break combo with wrong answer
    type_text "fi"
    press_enter
    sleep 2

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario5_combo_3stars.png"
    echo "Screenshot saved: scenario5_combo_3stars.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario5.log"
    echo "Logs saved: epic8_scenario5.log"
}

# ========================================
# SCENARIO 6: Slow Performance (★★★ expected)
# ========================================
run_scenario6() {
    echo "=========================================="
    echo "Scenario 6: Slow Performance (20s per word)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # Complete all words very slowly (20s wait before each)
    for word in look see watch eye glass find; do
        echo "Waiting 20s before typing: $word"
        sleep 20
        complete_word "$word" 0
    done

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario6_slow_3stars.png"
    echo "Screenshot saved: scenario6_slow_3stars.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario6.log"
    echo "Logs saved: epic8_scenario6.log"
}

# ========================================
# SCENARIO 7: One Wrong (★★ expected)
# ========================================
run_scenario7() {
    echo "=========================================="
    echo "Scenario 7: One Wrong (5/6 correct)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # 5 correct
    complete_word "look" 4
    complete_word "see" 4
    complete_word "watch" 4
    complete_word "eye" 4
    complete_word "glass" 4

    # 1 wrong
    type_text "finx"
    press_enter
    sleep 2

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario7_onewrong_2stars.png"
    echo "Screenshot saved: scenario7_onewrong_2stars.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario7.log"
    echo "Logs saved: epic8_scenario7.log"
}

# ========================================
# SCENARIO 8: Multiple Wrong (★ expected)
# ========================================
run_scenario8() {
    echo "=========================================="
    echo "Scenario 8: Multiple Wrong (3/6 correct)"
    echo "=========================================="

    reset_app
    echo "Please manually navigate to Level 1"
    echo "Then press Enter to continue..."
    read

    # 3 correct, 3 wrong
    complete_word "look" 4

    type_text "se"
    press_enter
    sleep 2

    complete_word "watch" 4

    type_text "ey"
    press_enter
    sleep 2

    complete_word "glass" 4

    type_text "fin"
    press_enter
    sleep 2

    sleep 3
    adb -s $DEVICE shell screencap -p > "$SCREENSHOT_DIR/scenario8_multiplewrong_1star.png"
    echo "Screenshot saved: scenario8_multiplewrong_1star.png"

    adb -s $DEVICE logcat -d | grep -E "StarRatingCalculator|LearningViewModel|SubmitAnswerUseCase|GuessingDetector|ComboManager" > "$LOG_DIR/epic8_scenario8.log"
    echo "Logs saved: epic8_scenario8.log"
}

# ========================================
# MAIN MENU
# ========================================
echo "=========================================="
echo "Epic #8 Automated Test Script"
echo "=========================================="
echo ""
echo "Select scenario to run:"
echo "1) Scenario 1: Perfect Performance"
echo "2) Scenario 2: All With Hints"
echo "3) Scenario 3: Mixed Accuracy"
echo "4) Scenario 4: Guessing Detected"
echo "5) Scenario 5: High Combo"
echo "6) Scenario 6: Slow Performance"
echo "7) Scenario 7: One Wrong"
echo "8) Scenario 8: Multiple Wrong"
echo "a) Run all scenarios"
echo "q) Quit"
echo ""
read -p "Enter choice: " choice

case $choice in
    1) run_scenario1 ;;
    2) run_scenario2 ;;
    3) run_scenario3 ;;
    4) run_scenario4 ;;
    5) run_scenario5 ;;
    6) run_scenario6 ;;
    7) run_scenario7 ;;
    8) run_scenario8 ;;
    a)
        run_scenario1
        run_scenario2
        run_scenario3
        run_scenario4
        run_scenario5
        run_scenario6
        run_scenario7
        run_scenario8
        ;;
    q) echo "Exiting..." ;;
    *) echo "Invalid choice" ;;
esac

echo ""
echo "=========================================="
echo "Test execution complete!"
echo "=========================================="
echo "Screenshots: $SCREENSHOT_DIR"
echo "Logs: $LOG_DIR"
