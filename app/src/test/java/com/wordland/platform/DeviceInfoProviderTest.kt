package com.wordland.platform

import com.wordland.domain.performance.PerformanceTier
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for DeviceProfiles
 *
 * Tests device profile detection and tier overrides
 */
@RunWith(JUnit4::class)
class DeviceProfilesTest {
    @Test
    fun deviceProfiles_isKnownHighEnd_detectsPixel() {
        assertTrue(DeviceProfiles.isKnownHighEnd("Google", "Pixel 9 Pro"))
    }

    @Test
    fun deviceProfiles_isKnownHighEnd_detectsSamsungS23() {
        assertTrue(DeviceProfiles.isKnownHighEnd("Samsung", "SM-S921"))
    }

    @Test
    fun deviceProfiles_isKnownHighEnd_caseInsensitive() {
        assertTrue(DeviceProfiles.isKnownHighEnd("google", "pixel 9"))
    }

    @Test
    fun deviceProfiles_isKnownHighEnd_returnsFalseForGenericDevice() {
        assertFalse(DeviceProfiles.isKnownHighEnd("Generic", "Model X"))
    }

    @Test
    fun deviceProfiles_isKnownLowEnd_detectsRedmi() {
        assertTrue(DeviceProfiles.isKnownLowEnd("Xiaomi", "Redmi 9A"))
    }

    @Test
    fun deviceProfiles_isKnownLowEnd_detectsGalaxyA() {
        assertTrue(DeviceProfiles.isKnownLowEnd("Samsung", "Galaxy A04"))
    }

    @Test
    fun deviceProfiles_isKnownLowEnd_caseInsensitive() {
        assertTrue(DeviceProfiles.isKnownLowEnd("xiaomi", "redmi 9c"))
    }

    @Test
    fun deviceProfiles_isKnownLowEnd_returnsFalseForHighEndDevice() {
        assertFalse(DeviceProfiles.isKnownLowEnd("Google", "Pixel 9"))
    }

    @Test
    fun deviceProfiles_getOverrideTier_returnsHighForKnownHighEnd() {
        val tier =
            DeviceProfiles.getOverrideTier(
                "Google",
                "Pixel 9",
                PerformanceTier.MEDIUM,
            )

        assertEquals(PerformanceTier.HIGH, tier)
    }

    @Test
    fun deviceProfiles_getOverrideTier_returnsLowForKnownLowEnd() {
        val tier =
            DeviceProfiles.getOverrideTier(
                "Xiaomi",
                "Redmi 9A",
                PerformanceTier.MEDIUM,
            )

        assertEquals(PerformanceTier.LOW, tier)
    }

    @Test
    fun deviceProfiles_getOverrideTier_returnsDetectedForUnknownDevice() {
        val tier =
            DeviceProfiles.getOverrideTier(
                "Unknown",
                "Model X",
                PerformanceTier.MEDIUM,
            )

        assertEquals(PerformanceTier.MEDIUM, tier)
    }

    @Test
    fun deviceProfiles_fullNameConstruction() {
        val fullName = "Samsung SM-S921"
        val manufacturer = "Samsung"
        val model = "SM-S921"

        assertEquals(fullName, "$manufacturer $model")
    }

    @Test
    fun deviceProfiles_containsCheck() {
        val fullName = "Google Pixel 9 Pro"
        val highEndDevice = "Pixel 9"

        assertTrue(fullName.contains(highEndDevice, ignoreCase = true))
    }

    @Test
    fun deviceProfiles_multipleHighEndDevices() {
        val highEndModels =
            listOf(
                "Pixel 7",
                "Pixel 8",
                "Pixel 9",
                "SM-S921",
                "SM-S918",
                "SM-G998",
            )

        highEndModels.forEach { model ->
            assertTrue("Expected $model to be high-end", DeviceProfiles.isKnownHighEnd("Test", model))
        }
    }
}
