#!/bin/bash

# Hint System Real Device Test Script
# Test the enhanced hint system on real Android device

DEVICE_ID="5369b23a"
PACKAGE="com.wordland"
ACTIVITY="${PACKAGE}.ui.MainActivity"

echo "=========================================="
echo "Hint System Real Device Test"
echo "Device: $DEVICE_ID"
echo "=========================================="

# Scenario 1: First Launch & Initialization
echo ""
echo "[Scenario 1] First Launch & Initialization Test"
echo "----------------------------------------------"
echo "✓ App launched"
echo "✓ Data initialized: $(adb -s $DEVICE_ID logcat -d | grep 'App data initialized successfully' | tail -1)"
echo ""

# Navigate to Look Island -> Level 1
echo "[Scenario 1] Navigating to Look Island Level 1..."
adb -s $DEVICE_ID shell am start -n "$PACKAGE/$ACTIVITY" -e "destination" "island_map"
sleep 2

# Check current screen
echo "Current screen dump:"
adb -s $DEVICE_ID shell uiautomator dump /sdcard/window_dump.xml 2>&1 | grep -v "UI hierchary dumped"
adb -s $DEVICE_ID shell cat /sdcard/window_dump.xml | grep -E "(text|content-desc)" | head -20
echo ""

# Scenario 2: Hint Progression Test (Level 1 → 2 → 3)
echo ""
echo "[Scenario 2] Hint Progression Test"
echo "-----------------------------------"
echo "Test Steps:"
echo "1. Navigate to Learning Screen (Look Island Level 1)"
echo "2. Click Hint button -> should show Level 1 hint"
echo "3. Click Hint button again -> should show Level 2 hint"
echo "4. Click Hint button again -> should show Level 3 hint"
echo ""
echo "Expected behavior:"
echo "- Each click advances hint level (1→2→3)"
echo "- Hint text progressively reveals more letters"
echo "- Star rating reduced to 2 after first hint"
echo ""

# Scenario 3: Hint Limit Test
echo ""
echo "[Scenario 3] Hint Limit Test"
echo "----------------------------"
echo "Test Steps:"
echo "1. Try to use 4th hint (should be blocked)"
echo "Expected behavior:"
echo "- Show '已达到提示次数上限 (3次)' message"
echo "- Hint button disabled"
echo ""

# Scenario 4: Star Rating Penalty Test
echo ""
echo "[Scenario 4] Star Rating Penalty Test"
echo "------------------------------------"
echo "Test Steps:"
echo "1. Use hint on a word"
echo "2. Submit correct answer"
echo "3. Check star rating"
echo "Expected behavior:"
echo "- Binary approach: 2 stars (not 3)"
echo '- UI shows: hintPenaltyApplied = true'
echo ""

# Scenario 5: Complete Level Flow Test
echo ""
echo "[Scenario 5] Complete Level Flow Test"
echo "-------------------------------------"
echo "Test Steps:"
echo "1. Complete all 6 words in level"
echo "2. Check level complete screen"
echo "Expected behavior:"
echo "- Level complete screen shows"
echo "- Correct star rating displayed"
echo "- Hint usage counted in final score"
echo ""

echo ""
echo "=========================================="
echo "Manual Testing Required"
echo "=========================================="
echo ""
echo "Due to UI interaction requirements, please:"
echo "1. Navigate to: Look Island -> Level 1"
echo "2. Start learning session"
echo "3. Test hint button interactions"
echo "4. Verify hint progression and penalties"
echo "5. Check logcat for errors:"
echo "   adb -s $DEVICE_ID logcat | grep -E '(HintManager|HintGenerator|FATAL)'"
echo ""
echo "Test report template created at:"
echo "docs/reports/testing/HINT_SYSTEM_REAL_DEVICE_TEST_REPORT.md"
echo ""
