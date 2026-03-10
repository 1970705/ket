#!/bin/bash

# Automated Hint System Test Script
# Uses ADB to simulate UI interactions

DEVICE_ID="5369b23a"
PACKAGE="com.wordland"

echo "=========================================="
echo "Automated Hint System Test"
echo "=========================================="

# Function to tap screen coordinates
tap_screen() {
    local x=$1
    local y=$2
    echo "Tapping at ($x, $y)"
    adb -s $DEVICE_ID shell input tap $x $y
    sleep 2
}

# Function to take screenshot
take_screenshot() {
    local filename=$1
    adb -s $DEVICE_ID shell screencap -p /sdcard/$filename
    adb -s $DEVICE_ID pull /sdcard/$filename docs/reports/testing/screenshots/$filename
}

# Create screenshots directory
mkdir -p docs/reports/testing/screenshots

echo ""
echo "[Step 1] Navigate to Island Map"
echo "-------------------------------"
# Tap "开始冒险" button (based on UI dump: bounds="[63,1117][1017,1642]")
# Center of button: (540, 1380)
tap_screen 540 1380
take_screenshot "step1_island_map.png"

echo ""
echo "[Step 2] Select Look Island"
echo "--------------------------"
# Assuming Look Island is first island
# This would need visual verification
sleep 3
take_screenshot "step2_look_island_selected.png"

echo ""
echo "[Step 3] Select Level 1"
echo "----------------------"
# Assuming Level 1 is first level
tap_screen 540 1000
sleep 2
take_screenshot "step3_level1_selected.png"

echo ""
echo "[Step 4] Start Learning Session"
echo "------------------------------"
# Tap "开始学习" button
tap_screen 540 1800
sleep 3
take_screenshot "step4_learning_screen.png"

echo ""
echo "[Step 5] Test Hint Button - First Click"
echo "---------------------------------------"
# Tap Hint button (would need coordinates from UI dump)
# For now, let's assume it's at bottom right
tap_screen 900 2000
sleep 2
take_screenshot "step5_hint_level1.png"

echo ""
echo "[Step 6] Check logcat for hint system activity"
echo "----------------------------------------------"
adb -s $DEVICE_ID logcat -d | grep -E "(HintManager|HintGenerator|UseHintUseCase)" | tail -20

echo ""
echo "=========================================="
echo "Automated Test Complete"
echo "=========================================="
echo ""
echo "Screenshots saved to: docs/reports/testing/screenshots/"
echo "Please verify:"
echo "1. Island map displayed correctly"
echo "2. Look Island and Level 1 accessible"
echo "3. Learning screen loaded"
echo "4. Hint button clickable and shows hint"
echo ""
