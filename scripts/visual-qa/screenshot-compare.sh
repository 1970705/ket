#!/bin/bash
#
# screenshot-compare.sh
#
# Compares current screenshots with baseline for visual regression testing.
#
# Part of Epic #12 Task 12.6 - Visual QA Automation
#
# Usage:
#   ./scripts/visual-qa/screenshot-compare.sh <baseline_date> [device_name]
#
# Arguments:
#   baseline_date - Date of baseline to compare (format: YYYY-MM-DD)
#   device_name   - Optional device identifier (default: auto-detect)
#
# Output:
#   Comparison report and diff images in docs/screenshots/comparison/
#
# Requirements:
#   - ADB device connected
#   - App installed on device
#   - ImageMagick (for image comparison)

set -e

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Configuration
BASELINE_DIR="$PROJECT_ROOT/docs/screenshots/baseline"
CURRENT_DIR="$PROJECT_ROOT/docs/screenshots/current"
COMPARISON_DIR="$PROJECT_ROOT/docs/screenshots/comparison"
REPORT_FILE="$PROJECT_ROOT/docs/reports/testing/VISUAL_QA_REPORT_$(date +%Y%m%d).md"

# Input arguments
BASELINE_DATE=${1:-}
DEVICE_ID=${2:-}

# Thresholds
DIFF_THRESHOLD=5  # Maximum acceptable difference percentage (0-100)
PIXEL_THRESHOLD=10  # Per-pixel difference threshold (0-255)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Statistics
TOTAL_COMPARISONS=0
PASSED_COMPARISONS=0
FAILED_COMPARISONS=0

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

log_comparison() {
    echo -e "${BLUE}[COMPARE]${NC} $1"
}

# Check dependencies
check_dependencies() {
    log_info "Checking dependencies..."

    if ! command -v adb &> /dev/null; then
        log_error "ADB not found. Please install Android SDK platform-tools."
        exit 1
    fi

    if ! command -v magick &> /dev/null && ! command -v compare &> /dev/null; then
        log_warn "ImageMagick not found. Comparison will use file size only."
        log_warn "Install ImageMagick for pixel-level comparison: brew install imagemagick"
    fi
}

# Check baseline exists
check_baseline() {
    if [ -z "$BASELINE_DATE" ]; then
        log_error "Baseline date required. Usage: $0 <baseline_date>"
        log_info "Available baselines:"
        ls -1 "$BASELINE_DIR" 2>/dev/null || echo "  No baselines found"
        exit 1
    fi

    local baseline_path="$BASELINE_DIR/$BASELINE_DATE"

    if [ ! -d "$baseline_path" ]; then
        log_error "Baseline not found: $baseline_path"
        log_info "Available baselines:"
        ls -1 "$BASELINE_DIR" 2>/dev/null || echo "  No baselines found"
        exit 1
    fi

    log_info "Using baseline: $baseline_path"
}

# Create current screenshot directory
create_current_dir() {
    log_info "Creating current screenshot directory: $CURRENT_DIR"
    rm -rf "$CURRENT_DIR"
    mkdir -p "$CURRENT_DIR"
}

# Create comparison directory
create_comparison_dir() {
    log_info "Creating comparison directory: $COMPARISON_DIR"
    rm -rf "$COMPARISON_DIR"
    mkdir -p "$COMPARISON_DIR"
}

# Get device info
check_device() {
    log_info "Checking device connection..."

    if [ -n "$DEVICE_ID" ]; then
        DEVICES="$DEVICE_ID"
    else
        DEVICES=$(adb devices | grep -w "device" | awk '{print $1}')
    fi

    if [ -z "$DEVICES" ]; then
        log_error "No ADB device connected."
        exit 1
    fi

    DEVICE_ID=$(echo "$DEVICES" | head -n1)

    DEVICE_MANUFACTURER=$(adb -s "$DEVICE_ID" shell getprop ro.product.manufacturer 2>/dev/null | tr -d '\r')
    DEVICE_MODEL=$(adb -s "$DEVICE_ID" shell getprop ro.product.model 2>/dev/null | tr -d '\r')
    DEVICE_INFO="${DEVICE_MANUFACTURER}_${DEVICE_MODEL}"

    log_info "Device: $DEVICE_INFO (ID: $DEVICE_ID)"
}

# Capture current screenshots
capture_current_screenshots() {
    log_info "Capturing current screenshots..."

    local timestamp=$(date +%Y%m%d_%H%M%S)

    # Launch app fresh
    adb -s "$DEVICE_ID" shell pm clear com.wordland > /dev/null 2>&1 || true
    sleep 1
    adb -s "$DEVICE_ID" shell am start -n com.wordland/.ui.MainActivity
    sleep 3

    # Capture same screens as baseline
    local screens=(
        "01_home_screen:3:Home screen main menu"
        "02_island_map:2:Island selection screen"
        "03_level_select:2:Level selection screen"
        "04_learning_screen_initial:2:Learning screen initial state"
        "05_learning_screen_with_hint:2:Learning screen with hint shown"
        "06_learning_screen_with_answer:2:Learning screen with answer entered"
        "07_review_screen:2:Review screen"
        "08_progress_screen:2:Progress screen"
    )

    for screen_spec in "${screens[@]}"; do
        IFS=':' read -r name wait desc <<< "$screen_spec"

        log_info "Capturing: $name - $desc"

        # Navigate between screens
        case "$name" in
            "02_island_map")
                adb -s "$DEVICE_ID" shell input tap 540 800
                sleep 2
                ;;
            "03_level_select")
                adb -s "$DEVICE_ID" shell input tap 540 900
                sleep 2
                ;;
            "04_learning_screen_initial")
                adb -s "$DEVICE_ID" shell input tap 540 700
                sleep 2
                ;;
            "05_learning_screen_with_hint")
                adb -s "$DEVICE_ID" shell input tap 900 400
                sleep 1
                ;;
            "06_learning_screen_with_answer")
                adb -s "$DEVICE_ID" shell input text "look"
                sleep 1
                ;;
            "07_review_screen")
                adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
                sleep 1
                adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
                sleep 1
                adb -s "$DEVICE_ID" shell input tap 540 1100
                sleep 2
                ;;
            "08_progress_screen")
                adb -s "$DEVICE_ID" shell input keyevent KEYCODE_BACK
                sleep 1
                adb -s "$DEVICE_ID" shell input tap 540 1200
                sleep 2
                ;;
        esac

        # Capture screenshot
        local filename="${timestamp}_${name}.png"
        local filepath="$CURRENT_DIR/$filename"

        adb -s "$DEVICE_ID" shell screencap -p "/sdcard/$filename"
        adb -s "$DEVICE_ID" pull "/sdcard/$filename" "$filepath" > /dev/null
        adb -s "$DEVICE_ID" shell rm "/sdcard/$filename"

        log_info "Saved: $filename"
    done
}

# Compare two images
compare_images() {
    local baseline_file=$1
    local current_file=$2
    local screen_name=$3

    TOTAL_COMPARISONS=$((TOTAL_COMPARISONS + 1))

    local baseline_filename=$(basename "$baseline_file")
    local current_filename=$(basename "$current_file")

    if [ ! -f "$baseline_file" ]; then
        log_warn "Baseline not found: $baseline_filename"
        return 1
    fi

    if [ ! -f "$current_file" ]; then
        log_warn "Current screenshot not found: $current_filename"
        return 1
    fi

    # Check if ImageMagick is available
    if command -v compare &> /dev/null; then
        # Use ImageMagick for pixel-level comparison
        local diff_file="$COMPARISON_DIR/diff_${screen_name}.png"
        local metric_output=$(mktemp)

        # Run comparison
        compare -metric RMSE -fuzz "5%" \
            "$baseline_file" "$current_file" \
            "$diff_file" 2> "$metric_output" || true

        # Parse result (ImageMagick returns non-zero even for small differences)
        local diff_value=$(cat "$metric_output" | grep -oE '[0-9]+\.[0-9]+' | head -1)

        # Convert to percentage (rough approximation)
        local diff_percent=$(echo "$diff_value * 100" | bc 2>/dev/null || echo "0")

        rm -f "$metric_output"

        # Check against threshold
        local comparison_result
        local status_icon

        if (( $(echo "$diff_percent <= $DIFF_THRESHOLD" | bc -l 2>/dev/null || echo "0") )); then
            PASSED_COMPARISONS=$((PASSED_COMPARISONS + 1))
            status_icon="✅"
            comparison_result="PASS"
        else
            FAILED_COMPARISONS=$((FAILED_COMPARISONS + 1))
            status_icon="❌"
            comparison_result="FAIL"
        fi

        log_comparison "$status_icon $screen_name: ${diff_percent}% difference (threshold: ${DIFF_THRESHOLD}%)"

        # Return result
        [ "$comparison_result" = "PASS" ]

    else
        # Fallback: Compare file sizes
        local baseline_size=$(stat -f%z "$baseline_file" 2>/dev/null || stat -c%s "$baseline_file")
        local current_size=$(stat -f%z "$current_file" 2>/dev/null || stat -c%s "$current_file")

        local size_diff=$((baseline_size - current_size))
        local size_diff_percent=$((size_diff * 100 / baseline_size))
        local size_diff_abs=${size_diff#-}  # Absolute value

        if [ $size_diff_abs -lt 500 ]; then  # Less than 500 bytes difference
            PASSED_COMPARISONS=$((PASSED_COMPARISONS + 1))
            log_comparison "✅ $screen_name: Size match (baseline: $baseline_size, current: $current_size)"
            return 0
        else
            FAILED_COMPARISONS=$((FAILED_COMPARISONS + 1))
            log_comparison "❌ $screen_name: Size differs by ${size_diff_abs} bytes (${size_diff_percent}%)"
            return 1
        fi
    fi
}

# Run all comparisons
run_comparisons() {
    log_info "Running comparisons..."

    for baseline_file in "$BASELINE_DIR/$BASELINE_DATE"/*.png; do
        if [ -f "$baseline_file" ]; then
            local filename=$(basename "$baseline_file")
            local current_file="$CURRENT_DIR/$filename"

            if [ -f "$current_file" ]; then
                local screen_name=$(echo "$filename" | sed 's/[0-9]*_//' | sed 's/.png//')
                compare_images "$baseline_file" "$current_file" "$screen_name"
            else
                log_warn "No current screenshot for: $filename"
            fi
        fi
    done
}

# Generate comparison report
generate_report() {
    log_info "Generating comparison report..."

    local report_date=$(date +%Y-%m-%d)
    local report_time=$(date +%H:%M:%S)

    cat > "$REPORT_FILE" << 'EOF'
# Visual QA Comparison Report

**Date`: REPORT_DATE`
**Time`: REPORT_TIME`
**Baseline`: BASELINE_DATE`
**Device`: DEVICE_INFO`

---

## Summary

| Metric | Count |
|--------|-------|
| Total Comparisons | TOTAL |
| Passed ✅ | PASSED |
| Failed ❌ | FAILED |
| Pass Rate | RATE% |

---

## Results by Screen

EOF

    # Replace placeholders
    sed -i '' "s/REPORT_DATE/$report_date/" "$REPORT_FILE"
    sed -i '' "s/REPORT_TIME/$report_time/" "$REPORT_FILE"
    sed -i '' "s/BASELINE_DATE/$BASELINE_DATE/" "$REPORT_FILE"
    sed -i '' "s/DEVICE_INFO/$DEVICE_INFO/" "$REPORT_FILE"
    sed -i '' "s/TOTAL/$TOTAL_COMPARISONS/" "$REPORT_FILE"
    sed -i '' "s/PASSED/$PASSED_COMPARISONS/" "$REPORT_FILE"
    sed -i '' "s/FAILED/$FAILED_COMPARISONS/" "$REPORT_FILE"

    local pass_rate=0
    if [ $TOTAL_COMPARISONS -gt 0 ]; then
        pass_rate=$((PASSED_COMPARISONS * 100 / TOTAL_COMPARISONS))
    fi
    sed -i '' "s/RATE/$pass_rate/" "$REPORT_FILE"

    # Add detailed results
    echo "" >> "$REPORT_FILE"
    echo "### ✅ Passed Screens" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"

    # List passed screenshots (if we tracked them)
    echo "Screens that matched baseline within threshold." >> "$REPORT_FILE"

    if [ $FAILED_COMPARISONS -gt 0 ]; then
        echo "" >> "$REPORT_FILE"
        echo "### ❌ Failed Screens" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "The following screens exceeded the difference threshold:" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo "Check diff images in: \`docs/screenshots/comparison/\`" >> "$REPORT_FILE"
    fi

    # Add footer
    cat >> "$REPORT_FILE" << 'EOF'

---

## Next Steps

- Review failed screens for visual regressions
- Update baseline if changes are intentional: \`./scripts/visual-qa/screenshot-baseline.sh\`
- Fix UI issues if regressions found

---

**Generated by**: Epic #12 Task 12.6 - Visual QA Automation
EOF

    log_info "Report saved to: $REPORT_FILE"
}

# Print summary
print_summary() {
    log_info "================================"
    log_info "Comparison Complete"
    log_info "================================"
    log_info "Total comparisons: $TOTAL_COMPARISONS"
    log_info "Passed: $PASSED_COMPARISONS ✅"
    log_info "Failed: $FAILED_COMPARISONS ❌"

    local pass_rate=0
    if [ $TOTAL_COMPARISONS -gt 0 ]; then
        pass_rate=$((PASSED_COMPARISONS * 100 / TOTAL_COMPARISONS))
    fi

    log_info "Pass rate: ${pass_rate}%"
    log_info ""
    log_info "Diff images: $COMPARISON_DIR"
    log_info "Report: $REPORT_FILE"

    if [ $FAILED_COMPARISONS -gt 0 ]; then
        log_warn "Some screens failed comparison. Review diff images for visual regressions."
        exit 1
    else
        log_info "All screens passed! ✅"
    fi
}

# Main execution
main() {
    log_info "================================"
    log_info "Visual QA Screenshot Comparison"
    log_info "================================"

    check_dependencies
    check_baseline
    create_current_dir
    create_comparison_dir
    check_device
    capture_current_screenshots
    run_comparisons
    generate_report
    print_summary
}

# Run main function
main "$@"
