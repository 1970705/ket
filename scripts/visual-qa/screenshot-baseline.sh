#!/bin/bash
#
# screenshot-baseline.sh
#
# Creates baseline screenshots for Visual QA.
#
# Part of Epic #12 Task 12.6 - Visual QA Automation
#
# Usage:
#   ./scripts/visual-qa/screenshot-baseline.sh [device_name]
#
# Arguments:
#   device_name - Optional device identifier (default: auto-detect)
#
# Output:
#   Screenshots saved to docs/screenshots/baseline/YYYY-MM-DD/
#
# Requirements:
#   - ADB device connected
#   - App installed on device
#   - ImageMagick (for screenshot processing)

set -e

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Configuration
BASELINE_DIR="$PROJECT_ROOT/docs/screenshots/baseline"
DATE=$(date +%Y-%m-%d)
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SCREENSHOT_DIR="$BASELINE_DIR/$DATE"

# Device info
DEVICE_ID=${1:-}
DEVICE_MODEL=""
DEVICE_INFO=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Create screenshot directory
create_screenshot_dir() {
    log_info "Creating screenshot directory: $SCREENSHOT_DIR"
    mkdir -p "$SCREENSHOT_DIR"
}

# Check ADB connection
check_adb() {
    log_info "Checking ADB connection..."

    if ! command -v adb &> /dev/null; then
        log_error "ADB not found. Please install Android SDK platform-tools."
        exit 1
    fi

    # Get device list
    if [ -n "$DEVICE_ID" ]; then
        DEVICES="$DEVICE_ID"
    else
        DEVICES=$(adb devices | grep -w "device" | awk '{print $1}')
    fi

    if [ -z "$DEVICES" ]; then
        log_error "No ADB device connected. Please connect a device."
        exit 1
    fi

    # Use first device if multiple
    DEVICE_ID=$(echo "$DEVICES" | head -n1)

    # Get device info
    DEVICE_MANUFACTURER=$(adb -s "$DEVICE_ID" shell getprop ro.product.manufacturer)
    DEVICE_MODEL=$(adb -s "$DEVICE_ID" shell getprop ro.product.model)
    DEVICE_ANDROID_VERSION=$(adb -s "$DEVICE_ID" shell getprop ro.build.version.release)
    DEVICE_API_LEVEL=$(adb -s "$DEVICE_ID" shell getprop ro.build.version.sdk)

    DEVICE_INFO="${DEVICE_MANUFACTURER}_${DEVICE_MODEL}_Android${DEVICE_ANDROID_VERSION}"

    log_info "Device: $DEVICE_INFO"
    log_info "Device ID: $DEVICE_ID"
}

# Screenshot function
screenshot() {
    local name=$1
    local wait_seconds=${2:-2}
    local description=$3

    log_info "Capturing: $name - $description"
    sleep "$wait_seconds"

    local filename="${TIMESTAMP}_${name}.png"
    local filepath="$SCREENSHOT_DIR/$filename"

    # Capture screenshot
    adb -s "$DEVICE_ID" shell screencap -p "/sdcard/$filename"
    adb -s "$DEVICE_ID" pull "/sdcard/$filename" "$filepath" > /dev/null
    adb -s "$DEVICE_ID" shell rm "/sdcard/$filename"

    log_info "Saved: $filename"

    # Add metadata to screenshot (using ImageMagick if available)
    if command -v magick &> /dev/null; then
        # Add small label with component name
        # This is optional and helps identify screenshots
        :
    fi
}

# Navigate to screen
navigate_to_screen() {
    local screen=$1
    log_info "Navigating to: $screen"
    sleep 1
}

# Clear app data for fresh start
clear_app_data() {
    log_info "Clearing app data..."
    adb -s "$DEVICE_ID" shell pm clear com.wordland > /dev/null 2>&1 || true
    sleep 1
}

# Launch app
launch_app() {
    log_info "Launching Wordland app..."
    adb -s "$DEVICE_ID" shell am start -n com.wordland/.ui.MainActivity
    sleep 3  # Wait for app to fully load
}

# Take screenshots of all key screens
take_all_screenshots() {
    log_info "Starting screenshot capture..."

    # 1. HomeScreen
    screenshot "01_home_screen" "3" "Home screen main menu"

    # 2. IslandMapScreen - Navigate to island map
    log_info "Navigating to Island Map..."
    adb -s "$DEVICE_ID" shell input tap 540 800  # Approximate center tap
    sleep 2
    screenshot "02_island_map" "2" "Island selection screen"

    # 3. LevelSelectScreen - Navigate to level select
    log_info "Navigating to Level Select..."
    adb -s "$DEVICE_ID" shell input tap 540 900  # Tap on Look Island
    sleep 2
    screenshot "03_level_select" "2" "Level selection screen"

    # 4. LearningScreen - Navigate to learning
    log_info "Navigating to Learning Screen..."
    adb -s "$DEVICE_ID" shell input tap 540 700  # Tap on Level 1
    sleep 2
    screenshot "04_learning_screen_initial" "2" "Learning screen initial state"

    # 5. LearningScreen - With hint
    log_info "Using hint..."
    adb -s "$DEVICE_ID" shell input tap 900 400  # Tap hint button
    sleep 1
    screenshot "05_learning_screen_with_hint" "2" "Learning screen with hint shown"

    # 6. LearningScreen - With answer
    log_info "Entering answer..."
    adb -s "$DEVICE_ID" shell input text "look"
    sleep 1
    screenshot "06_learning_screen_with_answer" "2" "Learning screen with answer entered"

    # 7. ReviewScreen - Navigate via home
    log_info "Navigating to Review Screen..."
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
    sleep 1
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
    sleep 1
    adb -s "$DEVICE_ID" shell input tap 540 1100  # Tap review button
    sleep 2
    screenshot "07_review_screen" "2" "Review screen"

    # 8. ProgressScreen
    log_info "Navigating to Progress Screen..."
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
    sleep 1
    adb -s "$DEVICE_ID" shell input tap 540 1200  # Tap progress button
    sleep 2
    screenshot "08_progress_screen" "2" "Progress screen"
}

# Generate manifest
generate_manifest() {
    local manifest_file="$SCREENSHOT_DIR/manifest.txt"

    log_info "Generating manifest..."

    cat > "$manifest_file" << EOF
# Visual QA Baseline Manifest

**Date**: $DATE
**Timestamp**: $TIMESTAMP
**Device**: $DEVICE_INFO
**Device ID**: $DEVICE_ID
**Android Version**: $DEVICE_ANDROID_VERSION (API $DEVICE_API_LEVEL)

## Screenshots

| Filename | Screen | Description |
|----------|--------|-------------|
EOF

    # List all screenshots
    for file in "$SCREENSHOT_DIR"/*.png; do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            # Extract screen name from filename
            screen_name=$(echo "$filename" | sed 's/'"$TIMESTAMP"'_//' | sed 's/.png//')
            echo "| $filename | $screen_name | Screenshot |" >> "$manifest_file"
        fi
    done

    cat >> "$manifest_file" << EOF

## Usage

This baseline is used for visual regression testing.
Compare future screenshots against this baseline using:
\`./scripts/visual-qa/screenshot-compare.sh $DATE\`
EOF

    log_info "Manifest saved to: $manifest_file"
}

# Print summary
print_summary() {
    log_info "================================"
    log_info "Baseline Creation Complete"
    log_info "================================"
    log_info "Screenshots: $SCREENSHOT_DIR"
    log_info "Total screenshots: $(ls -1 "$SCREENSHOT_DIR"/*.png 2>/dev/null | wc -l)"
    log_info ""
    log_info "To compare with future screenshots:"
    log_info "  ./scripts/visual-qa/screenshot-compare.sh $DATE"
}

# Main execution
main() {
    log_info "================================"
    log_info "Visual QA Baseline Screenshot Tool"
    log_info "================================"
    log_info "Date: $DATE"

    check_adb
    create_screenshot_dir
    clear_app_data
    launch_app
    take_all_screenshots
    generate_manifest
    print_summary
}

# Run main function
main "$@"
