#!/bin/bash
# Real Device Clean Install Test
# This test verifies that the app works correctly on real device with clean install
#
# Usage: ./scripts/test/test_real_device_clean_install.sh
#
# This test is CRITICAL and MUST PASS before any release
# Reference: docs/reports/issues/FIRST_LAUNCH_BUG_ROOT_CAUSE_ANALYSIS.md

set -e

echo "📱 Real Device Clean Install Test (CRITICAL)"
echo "============================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $2"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}❌ FAIL${NC}: $2"
        TESTS_FAILED=$((TESTS_FAILED + 1))
    fi
}

# Step 1: Check for real device
echo "Step 1: Checking for real device connection..."
DEVICE_ID=$(adb devices | grep -v "List" | awk '{print $1}' | head -1)

if [ -z "$DEVICE_ID" ]; then
    echo -e "${RED}❌ FAIL${NC}: No device found"
    echo "Please connect a real device"
    exit 1
fi

# Check if it's a real device (not emulator)
DEVICE_INFO=$(adb -s $DEVICE_ID shell getprop ro.build.characteristics | grep "default" || echo "")
if [ -n "$DEVICE_INFO" ]; then
    echo -e "${YELLOW}⚠️  WARNING${NC}: This might be an emulator"
    echo "For real device testing, please use a physical device"
fi

echo "Using device: $DEVICE_ID"
print_result 0 "Real device connected"
echo ""

# Step 2: Uninstall existing app (simulate clean install)
echo "Step 2: Uninstalling existing app..."
adb -s $DEVICE_ID uninstall com.wordland || true
sleep 2
print_result 0 "App uninstalled"
echo ""

# Step 3: Install APK
echo "Step 3: Installing APK..."
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ FAIL${NC}: APK not found at $APK_PATH"
    echo "Please build the APK first: ./gradlew assembleDebug"
    exit 1
fi

adb -s $DEVICE_ID install -r "$APK_PATH"
INSTALL_RESULT=$?
print_result $INSTALL_RESULT "APK installed"
echo ""

if [ $INSTALL_RESULT -ne 0 ]; then
    echo -e "${RED}❌ APK installation failed${NC}"
    exit 1
fi

# Step 4: Launch app
echo "Step 4: Launching app..."
adb -s $DEVICE_ID shell am start -n com.wordland.ui/.MainActivity
sleep 5  # Wait for initialization
print_result 0 "App launched"
echo ""

# Step 5: Verify database is created
echo "Step 5: Verifying database creation..."
adb -s $DEVICE_ID shell "run-as com.wordland ls -l /data/data/com.wordland/databases/" > /dev/null 2>&1
DB_EXISTS=$?
print_result $DB_EXISTS "Database file created"
echo ""

# Step 6: Verify database has data
echo "Step 6: Verifying database has data..."
WORD_COUNT=$(adb -s $DEVICE_ID shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT COUNT(*) FROM words' 2>/dev/null" || echo "0")
echo "Words in database: $WORD_COUNT"

if [ "$WORD_COUNT" -gt 0 ]; then
    print_result 0 "Database contains data ($WORD_COUNT words)"
else
    print_result 1 "Database is empty - initialization failed"
fi
echo ""

# Step 7: Verify Level 1 is unlocked
echo "Step 7: Verifying Level 1 status..."
LEVEL1_STATUS=$(adb -s $DEVICE_ID shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT status FROM level_progress WHERE level_id=\"look_island_level_01\"' 2>/dev/null" || echo "")
echo "Level 1 status: $LEVEL1_STATUS"

if [ "$LEVEL1_STATUS" = "unlocked" ]; then
    print_result 0 "Level 1 is UNLOCKED"
else
    print_result 1 "Level 1 is $LEVEL1_STATUS (should be unlocked)"
fi
echo ""

# Step 8: Check for initialization in logcat
echo "Step 8: Checking initialization logcat..."
LOGCAT_INIT=$(adb -s $DEVICE_ID logcat -d | grep -i "WordlandApplication.*App data initialized" || echo "")
if [ -n "$LOGCAT_INIT" ]; then
    print_result 0 "Initialization log found in logcat"
else
    print_result 1 "No initialization log found"
fi
echo ""

# Step 9: Check for errors in logcat
echo "Step 9: Checking for errors in logcat..."
ERRORS=$(adb -s $DEVICE_ID logcat -d | grep -i "wordland" | grep -i "error\|crash\|fatal" || echo "")
if [ -z "$ERRORS" ]; then
    print_result 0 "No errors in logcat"
else
    print_result 1 "Errors found in logcat"
    echo "Errors:"
    echo "$ERRORS"
fi
echo ""

# Step 10: Manual verification prompt
echo "Step 10: Manual verification"
echo "=============================="
echo "Please manually verify the following on the device:"
echo ""
echo "1. App launches without crash"
echo "2. You can see the home screen"
echo "3. Click '开始冒险' (Start Adventure)"
echo "4. You can see the island list (not empty)"
echo "5. Level 1 shows as unlocked"
echo "6. You can click on Level 1"
echo "7. The game starts successfully"
echo ""
read -p "Press Enter after manual verification..."
echo ""
print_result 0 "Manual verification completed"
echo ""

# Final summary
echo "============================================"
echo "Test Summary"
echo "============================================"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED - Real device clean install works correctly${NC}"
    echo ""
    echo "The app is ready for release!"
    exit 0
else
    echo -e "${RED}❌ TESTS FAILED - Real device clean install has issues${NC}"
    echo ""
    echo "Please review the following:"
    echo "1. Check if WordlandApplication.onCreate() calls initializeAppData()"
    echo "2. Check if AppDataInitializer.initializeAllData() completes"
    echo "3. Check if Level 1 is set to UNLOCKED (not LOCKED)"
    echo "4. Check logcat for errors: adb logcat | grep wordland"
    echo "5. Verify the device has sufficient storage"
    echo "6. Try uninstalling and reinstalling manually"
    exit 1
fi
