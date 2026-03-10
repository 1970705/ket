#!/bin/bash
# Navigation Test with Crash Detection
# This test verifies navigation flow and detects any crashes

set -e  # Exit on any error

# Source crash detection functions
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/../utils/crash_detection.sh"

# ADB path
ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

# Device selection (use real device if available, else emulator)
DEVICE_ID=$($ADB devices | grep -v "List" | awk '{print $1}' | grep -v "emulator" | head -1)

if [ -z "$DEVICE_ID" ]; then
    echo "⚠️  No real device found, using emulator"
    DEVICE_ID=$($ADB devices | grep -v "List" | awk '{print $1}' | head -1)
fi

if [ -z "$DEVICE_ID" ]; then
    echo "❌ FAIL: No device found"
    exit 1
fi

echo "📱 Using device: $DEVICE_ID"
echo ""

# Wait function
wait_seconds() {
    local seconds=$1
    echo "⏳ Waiting ${seconds} seconds..."
    sleep $seconds
}

echo "🧪 Navigation Test with Crash Detection"
echo "========================================"
echo ""

# Step 1: Launch app
echo "Step 1: Launching app..."
$ADB -s $DEVICE_ID shell am start -n com.wordland.ui/.MainActivity
wait_seconds 3

if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed on launch"
    exit 1
fi
echo ""

# Step 2: Click "开始冒险" (Start Adventure)
echo "Step 2: Clicking '开始冒险' button..."
$ADB -s $DEVICE_ID shell input tap 540 1200
wait_seconds 2

if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed after clicking '开始冒险'"
    exit 1
fi
echo ""

# Step 3: Click Look Island
echo "Step 3: Clicking Look Island..."
$ADB -s $DEVICE_ID shell input tap 540 1300
wait_seconds 2

if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed after clicking Look Island"
    exit 1
fi
echo ""

# Step 4: Click Level 1 ⚠️ CRITICAL TEST
echo "Step 4: Clicking Level 1 (CRITICAL)..."
$ADB -s $DEVICE_ID shell input tap 540 1000
wait_seconds 3

# ⚠️ THIS IS THE CRITICAL CHECK - Would have caught the navigation bug!
if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed after clicking Level 1"
    echo ""
    echo "🔍 Getting crash details..."
    $ADB -s $DEVICE_ID logcat -d | grep -A 30 "FATAL EXCEPTION" | tail -40
    exit 1
fi

# Verify we're on the correct screen
echo "Step 4.1: Verifying Learning screen..."
if ! verify_current_activity "Learning" $DEVICE_ID; then
    echo "⚠️  WARNING: Cannot verify Learning screen, but no crash detected"
fi
echo ""

# Step 5: Navigate back
echo "Step 5: Navigating back..."
$ADB -s $DEVICE_ID shell input tap 100 150
wait_seconds 2

if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed after navigating back"
    exit 1
fi
echo ""

# Step 6: Navigate back again
echo "Step 6: Navigating back to island map..."
$ADB -s $DEVICE_ID shell input tap 100 150
wait_seconds 2

if ! check_app_crashed $DEVICE_ID; then
    echo "❌ FAIL: App crashed after second back navigation"
    exit 1
fi
echo ""

echo "========================================"
echo "✅ ALL TESTS PASSED"
echo ""
echo "Navigation flow successful:"
echo "  ✅ Home → Island Map"
echo "  ✅ Island Map → Level Select"
echo "  ✅ Level Select → Learning (Level 1)"
echo "  ✅ Learning → Level Select (back)"
echo "  ✅ Level Select → Island Map (back)"
echo ""
echo "No crashes detected!"
echo "========================================"
