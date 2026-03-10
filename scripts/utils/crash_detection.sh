#!/bin/bash
# Crash Detection Helper Functions
# These functions help detect if the app has crashed

# Check if Wordland app process is still running
check_app_running() {
    local package_name="com.wordland"
    local device_id=${1:-""}

    local adb_cmd="adb"
    if [ -n "$device_id" ]; then
        adb_cmd="adb -s $device_id"
    fi

    local pid=$($adb_cmd shell pidof $package_name 2>/dev/null || echo "")

    if [ -z "$pid" ]; then
        echo "❌ CRASH DETECTED: App process not running"
        return 1
    fi

    return 0
}

# Check logcat for fatal exceptions
check_logcat_crashes() {
    local device_id=${1:-""}
    local lines=${2:-20}

    local adb_cmd="adb"
    if [ -n "$device_id" ]; then
        adb_cmd="adb -s $device_id"
    fi

    local crashes=$($adb_cmd logcat -d | grep "FATAL EXCEPTION" | tail -$lines 2>/dev/null || echo "")

    if [ -n "$crashes" ]; then
        echo "❌ CRASH DETECTED in logcat:"
        echo "$crashes"
        return 1
    fi

    return 0
}

# Check for any errors in logcat
check_logcat_errors() {
    local device_id=${1:-""}
    local package_name="com.wordland"
    local lines=${2:-10}

    local adb_cmd="adb"
    if [ -n "$device_id" ]; then
        adb_cmd="adb -s $device_id"
    fi

    local errors=$($adb_cmd logcat -d | grep -i "$package_name" | grep -i "error\|exception" | tail -$lines 2>/dev/null || echo "")

    if [ -n "$errors" ]; then
        echo "⚠️  ERRORS found in logcat:"
        echo "$errors"
        # Return warning, not failure
        return 2
    fi

    return 0
}

# Comprehensive crash check (process + logcat)
check_app_crashed() {
    local device_id=${1:-""}
    local failed=0

    echo "🔍 Checking for crashes..."

    # Check if process is running
    if ! check_app_running $device_id; then
        failed=1
    fi

    # Check logcat for crashes
    if ! check_logcat_crashes $device_id 5; then
        failed=1
    fi

    if [ $failed -eq 1 ]; then
        echo "❌ CRASH CONFIRMED"
        return 1
    fi

    echo "✅ No crashes detected"
    return 0
}

# Verify current activity/screen
verify_current_activity() {
    local expected_pattern=$1
    local device_id=${2:-""}

    local adb_cmd="adb"
    if [ -n "$device_id" ]; then
        adb_cmd="adb -s $device_id"
    fi

    local current=$($adb_cmd shell dumpsys window windows | grep -E "mCurrentFocus" | tail -1)

    if [[ ! "$current" =~ "$expected_pattern" ]]; then
        echo "❌ ACTIVITY MISMATCH"
        echo "   Expected: $expected_pattern"
        echo "   Got: $current"
        return 1
    fi

    echo "✅ Activity verified: $expected_pattern"
    return 0
}

# Export functions for use in other scripts
export -f check_app_running
export -f check_logcat_crashes
export -f check_logcat_errors
export -f check_app_crashed
export -f verify_current_activity
