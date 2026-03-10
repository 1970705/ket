# Epic #12 Technical Debt Documentation

**Date**: 2026-03-08
**Epic**: #12 - Real Device UI Automation Testing
**Task**: 12.1 - Paparazzi Integration / Robolectric Upgrade

---

## Paparazzi Screenshot Testing - Technical Debt

### Issue Summary
Paparazzi (v1.3.3) was evaluated as the primary screenshot testing tool for Epic #12. After extensive research and testing, it was determined to be incompatible with the current project setup.

### Root Causes

1. **JDK 17 Incompatibility**
   - Error: `java.lang.UnsupportedOperationException: class redefinition failed`
   - Location: `java.lang.instrument.InstrumentationImpl.retransformClasses0(Native Method)`
   - Cause: Java bytecode instrumentation conflict between Paparazzi and JDK 17

2. **Resource Loading Failure**
   - Error: `NoClassDefFoundError: Could not initialize class com.android.resources.ResourceType`
   - Location: `app.cash.paparazzi.internal.DynamicResourceIdManager`
   - Cause: Paparazzi requires Android platform resources not available in unit test environment

3. **JaCoCo Coverage Tool Conflict**
   - Issue: JaCoCo and Paparazzi both use Java instrumentation
   - Result: Cannot run screenshot tests with code coverage enabled

### Research Sources
- [Paparazzi Issue #384](https://github.com/cashapp/paparazzi/issues/384) - Java 17 incompatibility
- [Paparazzi Issue #1030](https://github.com/cashapp/paparazzi/issues/1030) - JaCoCo incompatibility
- [Paparazzi Issue #1124](https://github.com/cashapp/paparazzi/issues/1124) - Exception thrown by Paparazzi
- [Paparazzi Releases](https://github.com/cashapp/paparazzi/releases) - v1.3.3 with JDK 17 support (partial fix)

### Attempted Solutions

| Solution | Result | Notes |
|----------|--------|-------|
| Upgrade Paparazzi to v1.3.3 | Failed | Still has resource loading issues |
| Configure Jetifier ignore list | Partial | Fixed build, runtime issues remain |
| Disable JaCoCo for screenshot tests | Not viable | Would require separate test task configuration |
| Switch to Roborazzi | Not attempted | Similar issues expected |

### Decision
**Paparazzi will NOT be used for Epic #12 screenshot testing.**

### Alternative Approach
- **Primary**: Compose Testing (existing `createComposeRule()`)
- **Screenshot**: ADB shell screencap scripts (Task 12.3)
- **Validation**: Manual visual QA on real devices

---

## Robolectric 4.13 Upgrade - Partial Success

### Changes Made
- Upgraded Robolectric from 4.12.2 to 4.13 in `app/build.gradle.kts`
- Added Jetifier ignore list for external libraries (BouncyCastle, Android tools)
- Created `app/src/test/AndroidManifest.xml` for Robolectric

### Status

**Working**: Regular JUnit tests with Robolectric
**Not Working**: Compose tests using `createComposeRule()`

### Known Issue: ComponentActivity Resolution

**Error**:
```
java.lang.RuntimeException: Unable to resolve activity for Intent { act=android.intent.action.MAIN
cat=[android.intent.category.LAUNCHER] cmp=org.robolectric.default/androidx.activity.ComponentActivity }
```

**Root Cause**: Robolectric 4.13 + ActivityScenario + ComponentActivity incompatibility

**Reference**: [Robolectric PR #4736](https://github.com/robolectric/robolectric/pull/4736)

### Impact
- 15 Compose integration tests (`Epic1IntegrationTestRobolectric`, `Epic2IntegrationTestRobolectric`) are currently failing
- 2,300+ regular unit tests pass successfully
- Test coverage reporting (JaCoCo) works correctly

### Next Steps (Future Work)

1. **Short-term**: Use Compose testing without ActivityScenario (direct component rendering)
2. **Medium-term**: Re-evaluate when Robolectric 4.14+ releases with ComponentActivity fixes
3. **Long-term**: Consider migrating screenshot tests to AndroidTest (instrumented) with emulator

---

## Jetifier Configuration - Added to gradle.properties

```properties
# Jetifier ignore list for Epic #12 - External libraries already using AndroidX or newer Java versions
android.jetifier.ignorelist=bcpkix-jdk18on,bcprov-jdk18on,bcutil-jdk18on,common-31.2.2,common-31.0.2,sdk-common-31.2.2,sdk-common-31.0.2
```

**Reason**: External BouncyCastle libraries (for JDK 18+) and Android tools use Java 21 bytecode (class version 65) which Jetifier cannot process.

---

## Summary

| Component | Status | Notes |
|-----------|--------|-------|
| Paparazzi Integration | ❌ Abandoned | JDK 17 + resource loading incompatibility |
| Robolectric 4.13 Upgrade | ⚠️ Partial | Regular tests pass, Compose tests blocked |
| Jetifier Configuration | ✅ Complete | Handles external libraries correctly |
| Test Manifest | ✅ Created | `app/src/test/AndroidManifest.xml` |

**Technical Debt Category**: Toolchain incompatibility
**Priority**: P2 (blocks full Compose UI testing)
**Estimated Resolution Effort**: 4-6 hours

---

**Last Updated**: 2026-03-08
**Author**: android-test-engineer
**Reviewer**: android-architect (pending)
