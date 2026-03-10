#!/bin/bash

# Performance Testing Script for Wordland
# Measures startup time, frame rate, and memory usage

set -e

echo "======================================"
echo "Wordland Performance Testing Suite"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if device is connected
check_device() {
    if ! adb devices | grep -q "device$"; then
        print_status "$RED" "❌ No device connected. Please connect a device or start an emulator."
        exit 1
    fi
    print_status "$GREEN" "✓ Device connected"
}

# Function to run macrobenchmark
run_macrobenchmark() {
    print_status "$YELLOW" "🚀 Running Macrobenchmarks..."

    ./gradlew :benchmark:connectedCheck

    if [ $? -eq 0 ]; then
        print_status "$GREEN" "✓ Macrobenchmarks completed successfully"
    else
        print_status "$RED" "❌ Macrobenchmarks failed"
        exit 1
    fi
}

# Function to run microbenchmark
run_microbenchmark() {
    print_status "$YELLOW" "🔬 Running Microbenchmarks..."

    ./gradlew :microbenchmark:connectedCheck

    if [ $? -eq 0 ]; then
        print_status "$GREEN" "✓ Microbenchmarks completed successfully"
    else
        print_status "$RED" "❌ Microbenchmarks failed"
        exit 1
    fi
}

# Function to build release APK for testing
build_apk() {
    print_status "$YELLOW" "🔨 Building release APK..."

    ./gradlew assembleRelease

    if [ $? -eq 0 ]; then
        print_status "$GREEN" "✓ Release APK built successfully"
    else
        print_status "$RED" "❌ Release APK build failed"
        exit 1
    fi
}

# Function to install APK
install_apk() {
    print_status "$YELLOW" "📲 Installing APK..."

    adb install -r app/build/outputs/apk/release/app-release.apk

    if [ $? -eq 0 ]; then
        print_status "$GREEN" "✓ APK installed successfully"
    else
        print_status "$RED" "❌ APK installation failed"
        exit 1
    fi
}

# Function to clear app data
clear_data() {
    print_status "$YELLOW" "🗑️  Clearing app data..."

    adb shell pm clear com.wordland

    print_status "$GREEN" "✓ App data cleared"
}

# Function to measure cold startup time
measure_startup() {
    print_status "$YELLOW" "⏱️  Measuring cold startup time..."

    clear_data

    # Force stop app
    adb shell am force-stop com.wordland

    # Measure startup time
    local start_time=$(date +%s%3N)
    adb shell am start -W -n com.wordland/.ui.MainActivity | grep "TotalTime"
    local end_time=$(date +%s%3N)

    local duration=$((end_time - start_time))
    print_status "$GREEN" "✓ Cold startup time: ${duration}ms"

    if [ $duration -lt 3000 ]; then
        print_status "$GREEN" "✓ Startup time within 3s target"
    else
        print_status "$RED" "⚠️  Startup time exceeds 3s target"
    fi
}

# Function to measure memory usage
measure_memory() {
    print_status "$YELLOW" "💾 Measuring memory usage..."

    # Launch app
    adb shell am start -n com.wordland/.ui.MainActivity
    sleep 3

    # Get memory stats
    local pid=$(adb shell pidof com.wordland)
    local memory=$(adb shell dumpsys meminfo $pid | grep "TOTAL:" | awk '{print $2}')

    print_status "$GREEN" "✓ Memory usage: ${memory}KB"

    # Check if memory usage is reasonable (< 150MB)
    if [ ${memory%KB} -lt 153600 ]; then
        print_status "$GREEN" "✓ Memory usage within acceptable range"
    else
        print_status "$RED" "⚠️  Memory usage exceeds 150MB"
    fi
}

# Function to profile with Android Profiler
profile_gpu() {
    print_status "$YELLOW" "🎨 Profile GPU rendering..."

    print_status "$YELLOW" "Please manually profile GPU rendering using Android Studio:"
    echo "  1. Open Android Studio"
    echo "  2. View > Tool Windows > Profiler"
    echo "  3. Select Wordland process"
    echo "  4. Click GPU section"
    echo "  5. Navigate through the app"
    echo "  6. Check for:
    echo "     - Green bars (good, < 16ms per frame)"
    echo "     - Yellow/red bars (jank, > 16ms per frame)"
}

# Main test execution
main() {
    print_status "$YELLOW" "Starting performance tests..."
    echo ""

    check_device
    echo ""

    # Build and install
    build_apk
    echo ""
    install_apk
    echo ""

    # Run benchmarks
    run_macrobenchmark
    echo ""
    run_microbenchmark
    echo ""

    # Manual measurements
    measure_startup
    echo ""
    measure_memory
    echo ""

    # Profile instructions
    profile_gpu
    echo ""

    print_status "$GREEN" "======================================"
    print_status "$GREEN" "Performance tests completed!"
    print_status "$GREEN" "======================================"
    echo ""

    print_status "$YELLOW" "Results:"
    echo "  - Macrobenchmark results: benchmark/build/reports/"
    echo "  - Microbenchmark results: microbenchmark/build/reports/"
    echo ""
}

# Run main function
main "$@"
