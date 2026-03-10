#!/bin/bash
# First Launch Test - CRITICAL TEST FOR CLEAN INSTALL
# This test verifies that the app works correctly on first launch (empty database)
#
# Usage: ./scripts/test/test_first_launch.sh
#
# This test is CRITICAL and MUST PASS before any release
# Reference: docs/reports/issues/FIRST_LAUNCH_BUG_ROOT_CAUSE_ANALYSIS.md

set -e

# ADB path
ADB="/Users/panshan/Library/Android/sdk/platform-tools/adb"

echo "🧪 First Launch Test (CRITICAL)"
echo "================================"
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

# Step 1: Check device connection
echo "Step 1: Checking device connection..."
DEVICE_COUNT=$($ADB devices | grep -v "List" | wc -l)
if [ "$DEVICE_COUNT" -lt 1 ]; then
    echo -e "${RED}❌ FAIL${NC}: No device found"
    echo "Please connect a device or start an emulator"
    exit 1
fi
echo -e "${GREEN}✅ Device connected${NC}"
echo ""

# Step 2: Clear all app data (simulate clean install)
echo "Step 2: Clearing app data (simulating clean install)..."
$ADB shell pm clear com.wordland || true
sleep 2
print_result 0 "App data cleared"
echo ""

# Step 3: Launch app
echo "Step 3: Launching app..."
$ADB shell am start -n com.wordland.ui/.MainActivity
sleep 5  # Wait for initialization to complete
print_result 0 "App launched"
echo ""

# Step 4: Verify database is created
echo "Step 4: Verifying database creation..."
$ADB shell "run-as com.wordland ls -l /data/data/com.wordland/databases/" > /dev/null 2>&1
DB_EXISTS=$?
print_result $DB_EXISTS "Database file created"
echo ""

# Step 5: Verify database has data
echo "Step 5: Verifying database has data..."
WORD_COUNT=$($ADB shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT COUNT(*) FROM words' 2>/dev/null" || echo "0")
echo "Words in database: $WORD_COUNT"

if [ "$WORD_COUNT" -gt 0 ]; then
    print_result 0 "Database contains data ($WORD_COUNT words)"
else
    print_result 1 "Database is empty - initialization failed"
fi
echo ""

# Step 6: Verify Level 1 is unlocked
echo "Step 6: Verifying Level 1 status..."
LEVEL1_STATUS=$($ADB shell "run-as com.wordland sqlite3 /data/data/com.wordland/databases/wordland_database 'SELECT status FROM level_progress WHERE level_id=\"look_island_level_01\"' 2>/dev/null" || echo "")
echo "Level 1 status: $LEVEL1_STATUS"

if [ "$LEVEL1_STATUS" = "unlocked" ]; then
    print_result 0 "Level 1 is UNLOCKED"
else
    print_result 1 "Level 1 is $LEVEL1_STATUS (should be unlocked)"
fi
echo ""

# Step 7: Check for initialization in logcat
echo "Step 7: Checking initialization logcat..."
LOGCAT_INIT=$($ADB logcat -d | grep -i "WordlandApplication.*App data initialized" || echo "")
if [ -n "$LOGCAT_INIT" ]; then
    print_result 0 "Initialization log found in logcat"
else
    print_result 1 "No initialization log found"
fi
echo ""

# Step 8: Check for errors in logcat
echo "Step 8: Checking for errors in logcat..."
ERRORS=$($ADB logcat -d | grep -i "wordland" | grep -i "error\|crash\|fatal" || echo "")
if [ -z "$ERRORS" ]; then
    print_result 0 "No errors in logcat"
else
    print_result 1 "Errors found in logcat"
    echo "Errors:"
    echo "$ERRORS"
fi
echo ""

# Final summary
echo "================================"
echo "Test Summary"
echo "================================"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED - First launch is working correctly${NC}"
    exit 0
else
    echo -e "${RED}❌ TESTS FAILED - First launch has issues${NC}"
    echo ""
    echo "Please review the following:"
    echo "1. Check if WordlandApplication.onCreate() calls initializeAppData()"
    echo "2. Check if AppDataInitializer.initializeAllData() completes"
    echo "3. Check if Level 1 is set to UNLOCKED (not LOCKED)"
    echo "4. Check logcat for errors: $ADB logcat | grep wordland"
    exit 1
fi
