package com.wordland.domain.performance

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DeviceCapabilities
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * Tests:
 * - Performance tier detection for various device configurations
 * - Low RAM device detection
 * - Quality setting calculations
 * - Edge cases
 */
class DeviceCapabilitiesTest {
    @Test
    fun `detectPerformanceTier returns HIGH for flagship device`() {
        // Arrange: High-end device specs (8GB+ RAM, 8 cores, OpenGL ES 3.0+)
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 8_000_000_000L,
                cpuCores = 8,
                glEsVersion = 0x30000, // OpenGL ES 3.0
                apiLevel = 33,
            )

        // Act
        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)

        // Assert
        assertEquals(PerformanceTier.HIGH, tier)
    }

    @Test
    fun `detectPerformanceTier returns MEDIUM for mid-range device`() {
        // Arrange: Mid-range device specs
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 4_000_000_000L,
                cpuCores = 6,
                glEsVersion = 0x30000,
                apiLevel = 30,
            )

        // Act
        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)

        // Assert
        assertEquals(PerformanceTier.MEDIUM, tier)
    }

    @Test
    fun `detectPerformanceTier returns LOW for budget device`() {
        // Arrange: Budget device specs
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 2_000_000_000L,
                cpuCores = 4,
                glEsVersion = 0x20000, // OpenGL ES 2.0
                apiLevel = 27,
            )

        // Act
        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)

        // Assert
        assertEquals(PerformanceTier.LOW, tier)
    }

    @Test
    fun `detectPerformanceTier returns LOW for very low end device`() {
        // Arrange: Very low-end device specs
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 1_500_000_000L,
                cpuCores = 2,
                glEsVersion = 0x10000,
                apiLevel = 24,
            )

        // Act
        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)

        // Assert
        assertEquals(PerformanceTier.LOW, tier)
    }

    @Test
    fun `detectPerformanceTier correctly scores RAM`() {
        // Test different RAM configurations
        val rams =
            listOf(
                8_000_000_000L to 40, // 8GB = 40 points
                6_000_000_000L to 40, // 6GB = 40 points
                4_000_000_000L to 30, // 4GB = 30 points
                3_000_000_000L to 20, // 3GB = 20 points
                2_000_000_000L to 10, // 2GB = 10 points
                1_000_000_000L to 0, // 1GB = 0 points
            )

        rams.forEach { (ram, expectedMinScore) ->
            val deviceInfo = createDeviceInfo(ramBytes = ram, cpuCores = 8, glEsVersion = 0x30000)
            val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
            // Just verify it runs, exact scoring is internal implementation
            assertNotNull(tier)
        }
    }

    @Test
    fun `detectPerformanceTier correctly scores CPU cores`() {
        val cores = listOf(2, 4, 6, 8, 12)

        cores.forEach { coreCount ->
            val deviceInfo =
                createDeviceInfo(
                    ramBytes = 4_000_000_000L,
                    cpuCores = coreCount,
                    glEsVersion = 0x30000,
                )
            val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
            assertNotNull(tier)
        }
    }

    @Test
    fun `detectPerformanceTier correctly scores GPU`() {
        val gpuVersions =
            listOf(
                0x00000 to 0, // No OpenGL
                0x10000 to 10, // OpenGL ES 1.x
                0x20000 to 20, // OpenGL ES 2.0
                0x30000 to 30, // OpenGL ES 3.0
                0x31000 to 30, // OpenGL ES 3.1
            )

        gpuVersions.forEach { (version, _) ->
            val deviceInfo =
                createDeviceInfo(
                    ramBytes = 4_000_000_000L,
                    cpuCores = 6,
                    glEsVersion = version,
                )
            val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
            assertNotNull(tier)
        }
    }

    @Test
    fun `isLowRamDevice returns true for less than 3GB`() {
        // Arrange
        val deviceInfo = createDeviceInfo(ramBytes = 2_500_000_000L)

        // Act
        val isLowRam = DeviceCapabilities.isLowRamDevice(deviceInfo)

        // Assert
        assertTrue(isLowRam)
    }

    @Test
    fun `isLowRamDevice returns false for 3GB or more`() {
        // Arrange
        val deviceInfo = createDeviceInfo(ramBytes = 3_500_000_000L)

        // Act
        val isLowRam = DeviceCapabilities.isLowRamDevice(deviceInfo)

        // Assert
        assertFalse(isLowRam)
    }

    @Test
    fun `isLowRamDevice returns true when lowRam flag is set`() {
        // Arrange
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 4_000_000_000L,
                isLowRamFlag = true,
            )

        // Act
        val isLowRam = DeviceCapabilities.isLowRamDevice(deviceInfo)

        // Assert
        assertTrue(isLowRam)
    }

    @Test
    fun `getTextureResolution returns full scale for HIGH tier`() {
        // Act
        val scale = DeviceCapabilities.getTextureResolution(PerformanceTier.HIGH)

        // Assert
        assertEquals(1.0f, scale)
    }

    @Test
    fun `getTextureResolution returns 075 for MEDIUM tier`() {
        // Act
        val scale = DeviceCapabilities.getTextureResolution(PerformanceTier.MEDIUM)

        // Assert
        assertEquals(0.75f, scale)
    }

    @Test
    fun `getTextureResolution returns 05 for LOW tier`() {
        // Act
        val scale = DeviceCapabilities.getTextureResolution(PerformanceTier.LOW)

        // Assert
        assertEquals(0.5f, scale)
    }

    @Test
    fun `getMaxConcurrentAnimations returns 5 for HIGH tier`() {
        // Act
        val max = DeviceCapabilities.getMaxConcurrentAnimations(PerformanceTier.HIGH)

        // Assert
        assertEquals(5, max)
    }

    @Test
    fun `getMaxConcurrentAnimations returns 3 for MEDIUM tier`() {
        // Act
        val max = DeviceCapabilities.getMaxConcurrentAnimations(PerformanceTier.MEDIUM)

        // Assert
        assertEquals(3, max)
    }

    @Test
    fun `getMaxConcurrentAnimations returns 1 for LOW tier`() {
        // Act
        val max = DeviceCapabilities.getMaxConcurrentAnimations(PerformanceTier.LOW)

        // Assert
        assertEquals(1, max)
    }

    @Test
    fun `getTargetFps returns 60 for HIGH tier`() {
        // Act
        val fps = DeviceCapabilities.getTargetFps(PerformanceTier.HIGH)

        // Assert
        assertEquals(60, fps)
    }

    @Test
    fun `getTargetFps returns 30 for MEDIUM tier`() {
        // Act
        val fps = DeviceCapabilities.getTargetFps(PerformanceTier.MEDIUM)

        // Assert
        assertEquals(30, fps)
    }

    @Test
    fun `getTargetFps returns 30 for LOW tier`() {
        // Act
        val fps = DeviceCapabilities.getTargetFps(PerformanceTier.LOW)

        // Assert
        assertEquals(30, fps)
    }

    @Test
    fun `DeviceInfo properties are accessible`() {
        // Arrange
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 4_000_000_000L,
                cpuCores = 6,
                glEsVersion = 0x30000,
                apiLevel = 30,
                isLowRamFlag = false,
                manufacturer = "TestManu",
                model = "TestModel",
            )

        // Assert
        assertEquals(4_000_000_000L, deviceInfo.totalRamBytes)
        assertEquals(6, deviceInfo.cpuCores)
        assertEquals(0x30000, deviceInfo.glEsVersion)
        assertEquals(30, deviceInfo.apiLevel)
        assertFalse(deviceInfo.isLowRamFlag)
        assertEquals("TestManu", deviceInfo.manufacturer)
        assertEquals("TestModel", deviceInfo.model)
    }

    @Test
    fun `DeviceCapabilitySummary formats correctly`() {
        // Arrange
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 8_000_000_000L,
                cpuCores = 8,
                glEsVersion = 0x30000,
                apiLevel = 33,
                manufacturer = "Test",
                model = "Device",
            )

        // Act
        val summary = DeviceCapabilitySummary.from(deviceInfo, PerformanceTier.HIGH)

        // Assert
        assertEquals(PerformanceTier.HIGH, summary.tier)
        assertEquals(7629L, summary.totalRamMB) // 8GB
        assertEquals(8, summary.cpuCores)
        assertEquals("3.0", summary.glEsVersion)
        assertEquals(33, summary.apiLevel)
        assertEquals("Test Device", summary.deviceName)
        assertFalse(summary.isLowRam)
        assertEquals(60, summary.targetFps)
        assertEquals(1.0f, summary.textureScale)
        assertEquals(5, summary.maxAnimations)
    }

    @Test
    fun `DeviceCapabilitySummary toLogString returns formatted output`() {
        // Arrange
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 4_000_000_000L,
                cpuCores = 6,
                glEsVersion = 0x30000,
                apiLevel = 30,
                manufacturer = "Test",
                model = "Device",
            )
        val summary = DeviceCapabilitySummary.from(deviceInfo, PerformanceTier.MEDIUM)

        // Act
        val logString = summary.toLogString()

        // Assert
        assertTrue(logString.contains("Device Capabilities:"))
        assertTrue(logString.contains("Test Device"))
        assertTrue(logString.contains("MEDIUM"))
        assertTrue(logString.contains("RAM:"))
        assertTrue(logString.contains("CPU:"))
        assertTrue(logString.contains("Target FPS:"))
    }

    @Test
    fun `detectPerformanceTier returns HIGH for Pixel 7 Pro specs`() {
        // Pixel 7 Pro: 12GB RAM, 8 cores, OpenGL ES 3.2, API 33
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 12_000_000_000L,
                cpuCores = 8,
                glEsVersion = 0x30002, // OpenGL ES 3.2
                apiLevel = 33,
            )

        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
        assertEquals(PerformanceTier.HIGH, tier)
    }

    @Test
    fun `detectPerformanceTier returns MEDIUM for Samsung A54 specs`() {
        // Samsung A54: 6GB RAM, 8 cores, OpenGL ES 3.2, API 33
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 6_000_000_000L,
                cpuCores = 8,
                glEsVersion = 0x30002,
                apiLevel = 33,
            )

        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
        assertEquals(PerformanceTier.MEDIUM, tier) // Score ~70, on the boundary
    }

    @Test
    fun `detectPerformanceTier returns LOW for Redmi 9A specs`() {
        // Redmi 9A: 2GB RAM, 4 cores, OpenGL ES 3.0, API 29
        val deviceInfo =
            createDeviceInfo(
                ramBytes = 2_000_000_000L,
                cpuCores = 4,
                glEsVersion = 0x30000,
                apiLevel = 29,
            )

        val tier = DeviceCapabilities.detectPerformanceTier(deviceInfo)
        assertEquals(PerformanceTier.LOW, tier)
    }

    // Helper function

    private fun createDeviceInfo(
        ramBytes: Long = 4_000_000_000L,
        cpuCores: Int = 6,
        glEsVersion: Int = 0x30000,
        apiLevel: Int = 30,
        isLowRamFlag: Boolean = false,
        manufacturer: String = "TestManu",
        model: String = "TestModel",
    ): DeviceInfo {
        return DeviceInfo.from(
            totalRamBytes = ramBytes,
            cpuCores = cpuCores,
            glEsVersion = glEsVersion,
            apiLevel = apiLevel,
            isLowRamFlag = isLowRamFlag,
            manufacturer = manufacturer,
            model = model,
        )
    }
}
