# Epic #12 Troubleshooting Guide

**Real Device UI Automation Testing**

**Version**: 1.0
**Last Updated**: 2026-03-08
**Owner**: android-test-engineer

---

## Table of Contents

1. [ADB Issues](#adb-issues)
2. [Device Connection Issues](#device-connection-issues)
3. [Screenshot Issues](#screenshot-issues)
4. [Test Execution Issues](#test-execution-issues)
5. [CI/CD Issues](#cicd-issues)
6. [Visual Comparison Issues](#visual-comparison-issues)
7. [Performance Issues](#performance-issues)

---

## ADB Issues

### Issue: `adb: command not found`

**Symptoms**:
```bash
adb: command not found
```

**Causes**:
- Android SDK not installed
- PATH not configured correctly

**Solutions**:

1. **Install Android SDK**:
   ```bash
   # macOS
   brew install --cask android-platform-tools

   # Linux
   sudo apt-get install android-tools-adb
   ```

2. **Configure PATH**:
   ```bash
   # Add to ~/.zshrc or ~/.bashrc
   export ANDROID_HOME=~/Library/Android/sdk
   export PATH="$ANDROID_HOME/platform-tools:$PATH"

   # Reload shell
   source ~/.zshrc
   ```

3. **Verify Installation**:
   ```bash
   adb version
   ```

---

### Issue: `adb: device offline`

**Symptoms**:
```bash
adb devices
# List of devices attached
# <device_id> offline
```

**Causes**:
- ADB server version mismatch
- Device connection issue

**Solutions**:

1. **Restart ADB Server**:
   ```bash
   adb kill-server
   adb start-server
   ```

2. **Reconnect Device**:
   ```bash
   # Disconnect and reconnect USB cable
   # Re-authorize on device
   ```

3. **Restart Device**:
   ```bash
   adb reboot
   ```

---

### Issue: `adb: device unauthorized`

**Symptoms**:
```bash
adb devices
# List of devices attached
# <device_id> unauthorized
```

**Causes**:
- USB debugging not authorized on device
- RSA key mismatch

**Solutions**:

1. **Revoke Authorization on Device**:
   - Settings → Developer Options
   - Revoke USB debugging authorization
   - Re-connect and accept prompt

2. **Clear ADB Keys**:
   ```bash
   rm -rf ~/.android/adbkey*
   adb kill-server
   adb start-server
   ```

3. **Accept Prompt on Device**:
   - Check "Always allow from this computer"
   - Click OK

---

## Device Connection Issues

### Issue: Device not detected

**Symptoms**:
```bash
adb devices
# List of devices attached
# (empty)
```

**Causes**:
- USB cable not connected
- USB debugging disabled
- Wrong USB mode (charging only)

**Solutions**:

1. **Check Physical Connection**:
   - Try different USB cable
   - Try different USB port
   - Try USB hub vs direct connection

2. **Enable USB Debugging**:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Developer Options → USB Debugging → ON

3. **Check USB Mode**:
   - Swipe down from notification shade
   - Tap "Charging via USB"
   - Change to "File Transfer" or "MTP"

4. **Verify with lsusb** (Linux):
   ```bash
   lsusb | grep -i android
   ```

---

### Issue: Multiple devices connected

**Symptoms**:
```bash
adb devices
# List of devices attached
# device1   device
# device2   device
```

**Problem**: Commands target all devices

**Solutions**:

1. **Specify Device ID**:
   ```bash
   adb -s <device_id> shell <command>
   ./run-device-tests.sh <device_id>
   ```

2. **Set TCP/IP for One Device**:
   ```bash
   # Connect one device via USB, another via WiFi
   adb -s <device_id> tcpip 5555
   adb connect <device_ip>:5555
   ```

---

## Screenshot Issues

### Issue: `screencap: permission denied`

**Symptoms**:
```bash
adb shell screencap -p /sdcard/screen.png
# Permission denied
```

**Causes**:
- Device storage permission
- Screen locked

**Solutions**:

1. **Unlock Device**:
   ```bash
   # Wake up device
   adb shell input keyevent KEYCODE_WAKEUP

   # Unlock (if no PIN/password)
   adb shell input keyevent 82
   ```

2. **Check Storage Permission**:
   ```bash
   adb shell pm grant com.wordland android.permission.WRITE_EXTERNAL_STORAGE
   ```

3. **Use Different Location**:
   ```bash
   adb shell screencap -p /data/local/tmp/screen.png
   adb pull /data/local/tmp/screen.png
   ```

---

### Issue: Screenshots are black

**Symptoms**:
- Captured screenshots show black screen

**Causes**:
- Screen is off
- Secure content (DRM)

**Solutions**:

1. **Wake Up Screen**:
   ```bash
   adb shell input keyevent KEYCODE_WAKEUP
   sleep 1
   adb shell screencap -p /sdcard/screen.png
   ```

2. **Disable Keyguard** (rooted):
   ```bash
   adb shell wm dismiss-keyguard
   ```

3. **Check Display State**:
   ```bash
   adb shell dumpsys power | grep 'Display Power'
   ```

---

### Issue: Screenshots have different sizes

**Symptoms**:
- Visual comparison fails due to size mismatch

**Causes**:
- Device orientation changed
- Different screen sizes

**Solutions**:

1. **Force Orientation**:
   ```bash
   # Portrait
   adb shell settings put system user_rotation 0

   # Landscape
   adb shell settings put system user_rotation 1
   ```

2. **Resize for Comparison**:
   ```bash
   # Using ImageMagick
   convert baseline.png -resize 1080x2400! baseline_resized.png
   compare baseline_resized.png current.png diff.png
   ```

---

## Test Execution Issues

### Issue: `install: FAILED_INSUFFICIENT_STORAGE`

**Symptoms**:
```bash
adb install app-debug.apk
# Performing Push Install
# FAILURE: Failed to install APK
```

**Causes**:
- Device storage full

**Solutions**:

1. **Check Available Storage**:
   ```bash
   adb shell df -h
   ```

2. **Clear App Data**:
   ```bash
   adb shell pm clear com.wordland
   ```

3. **Uninstall Old Version**:
   ```bash
   adb uninstall com.wordland
   adb install app-debug.apk
   ```

4. **Free Up Space**:
   - Clear app cache on device
   - Delete unnecessary files

---

### Issue: Tests timeout

**Symptoms**:
- Test script hangs
- Timeout errors in logs

**Causes**:
- Device too slow
- Emulator performance
- Network delays

**Solutions**:

1. **Increase Timeout**:
   ```bash
   # In run-device-tests.sh
   TEST_TIMEOUT=120 ./run-device-tests.sh
   ```

2. **Disable Animations**:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

3. **Use Real Device Instead of Emulator**:
   - Real devices are faster for UI tests
   - Emulators have overhead

---

### Issue: APK install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

**Symptoms**:
```bash
adb install -r app-debug.apk
# Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE]
```

**Causes**:
- Signature mismatch (debug vs release)
- Downgrade not allowed

**Solutions**:

1. **Uninstall First**:
   ```bash
   adb uninstall com.wordland
   adb install app-debug.apk
   ```

2. **Use -r Flag** (replace):
   ```bash
   adb install -r app-debug.apk
   ```

3. **Check Signature**:
   ```bash
   adb shell dumpsys package com.wordland | grep signatures
   ```

---

## CI/CD Issues

### Issue: GitHub Actions workflow not triggering

**Symptoms**:
- Workflow doesn't run on push
- Manual trigger doesn't work

**Causes**:
- Workflow file syntax error
- Branch filter mismatch

**Solutions**:

1. **Check Workflow Syntax**:
   ```bash
   # Install act (local GitHub Actions runner)
   brew install act

   # Test workflow locally
   act -l
   act push
   ```

2. **Verify Branch Names**:
   ```yaml
   on:
     push:
       branches: [ main, develop ]  # Must match your branch
   ```

3. **Check Workflow File Location**:
   ```
   .github/workflows/ci.yml  # Must be in .github/workflows/
   ```

---

### Issue: UI tests skipped in CI

**Symptoms**:
- CI runs but UI tests don't execute

**Causes**:
- Condition not met
- Input not provided

**Solutions**:

1. **Check Condition**:
   ```yaml
   if: github.event_name == 'workflow_dispatch' && inputs.run-ui-tests == true
        || contains(github.event.head_commit.message, '[ui-test]')
   ```

2. **Use Commit Message Trigger**:
   ```bash
   git commit -m "feat: add feature [ui-test]"
   git push
   ```

3. **Manual Trigger**:
   ```bash
   gh workflow run ci.yml -f run-ui-tests=true
   ```

---

### Issue: AVD creation takes too long

**Symptoms**:
- CI job times out during AVD creation

**Causes**:
- AVD not cached
- Network issues

**Solutions**:

1. **Enable Caching**:
   ```yaml
   - name: AVD cache
     uses: actions/cache@v3
     with:
       path: |
         ~/.android/avd/*
         ~/.android/adb*
       key: avd-34
   ```

2. **Use Faster Options**:
   ```yaml
   emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
   ```

3. **Increase Timeout**:
   ```yaml
   timeout-minutes: 60
   ```

---

## Visual Comparison Issues

### Issue: False positives in screenshot diff

**Symptoms**:
- Comparison shows differences when none exist
- Anti-aliasing artifacts

**Causes**:
- Rendering differences
- Compression artifacts
- Font rendering

**Solutions**:

1. **Increase Fuzz Threshold**:
   ```bash
   FUZZ_THRESHOLD=15 ./compare-screenshots.sh compare baseline.png current.png diff.png
   ```

2. **Ignore Anti-Aliasing**:
   ```bash
   # Using ImageMagick with similar image detection
   compare -metric AE -fuzz 10% baseline.png current.png diff.png
   ```

3. **Normalize Images**:
   ```bash
   # Resize to same dimensions
   convert baseline.png -resize 1080x2400! baseline_norm.png
   convert current.png -resize 1080x2400! current_norm.png
   ```

---

### Issue: Animations cause inconsistent screenshots

**Symptoms**:
- Screenshots show different animation frames

**Causes**:
- Transitions not disabled
- Loading states

**Solutions**:

1. **Disable Animations**:
   ```bash
   adb shell settings put global window_animation_scale 0
   adb shell settings put global transition_animation_scale 0
   adb shell settings put global animator_duration_scale 0
   ```

2. **Add Delays**:
   ```bash
   # In capture-screenshots.sh
   sleep 2  # Wait for animations to complete
   adb exec-out screencap -p > screenshot.png
   ```

3. **Use uiMode Flag** (Compose):
   ```kotlin
   @Composable
   fun TestScreen() {
       // Disable animations in tests
       LocalViewMode.current
   }
   ```

---

## Performance Issues

### Issue: Tests run too slowly

**Symptoms**:
- Full test suite takes >45 minutes

**Causes**:
- Too many scenarios
- Emulator overhead
- No parallelization

**Solutions**:

1. **Run Critical Path Only**:
   ```bash
   SCENARIO=critical ./run-device-tests.sh
   ```

2. **Use Real Device**:
   - Real devices are 2-3x faster than emulators

3. **Parallel Execution**:
   ```bash
   # Run on multiple devices simultaneously
   ./run-device-tests.sh device1 &
   ./run-device-tests.sh device2 &
   wait
   ```

4. **Disable Unnecessary Checks**:
   ```bash
   SKIP_CLEANUP=true ./run-device-tests.sh  # Keep APK installed
   SKIP_LOGCAT=true ./run-device-tests.sh   # Skip log collection
   ```

---

### Issue: Device battery drains quickly

**Symptoms**:
- Battery dies during long test runs

**Solutions**:

1. **Keep Device Plugged In**:
   - Use USB cable during testing

2. **Reduce Screen Brightness**:
   ```bash
   adb shell settings put system screen_brightness 50
   ```

3. **Enable Airplane Mode**:
   ```bash
   adb shell settings put global airplane_mode_on 1
   adb shell am broadcast -a android.intent.action.AIRPLANE_MODE
   ```

---

## Quick Reference

### Essential Commands

```bash
# Device connection
adb devices                           # List connected devices
adb kill-server && adb start-server  # Restart ADB
adb -s <device_id> shell <cmd>       # Target specific device

# Screen control
adb shell input keyevent KEYCODE_WAKEUP  # Wake screen
adb shell input keyevent 82              # Unlock (no PIN)
adb shell settings put global window_animation_scale 0  # Disable animations

# App management
adb install app-debug.apk            # Install APK
adb install -r app-debug.apk          # Replace existing
adb uninstall com.wordland            # Uninstall
adb shell pm clear com.wordland       # Clear data

# Screenshots
adb exec-out screencap -p > screen.png  # Capture to local
adb shell screencap -p /sdcard/s.png    # Capture to device
adb pull /sdcard/s.png                  # Pull from device

# Logs
adb logcat -c                          # Clear logcat
adb logcat | grep Wordland             # Filter logs
adb bugreport > bugreport.txt          # Full bug report
```

### Getting Help

- **Documentation**: See [SCREENSHOT_TESTING_GUIDE.md](./SCREENSHOT_TESTING_GUIDE.md)
- **Team**: Contact android-test-engineer
- **Issues**: Create GitHub issue with label `epic-12`

---

**Document Status**: ✅ Complete
**Last Updated**: 2026-03-08
