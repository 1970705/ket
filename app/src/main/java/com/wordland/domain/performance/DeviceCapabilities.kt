package com.wordland.domain.performance

/**
 * Device capabilities detection for quality settings
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * This class provides device capability detection based on:
 * - Available RAM (ActivityManager.MemoryInfo)
 * - CPU cores (Runtime.availableProcessors)
 * - GPU capabilities (OpenGL ES version)
 * - Device tier classification (HIGH/MEDIUM/LOW)
 *
 * Architecture Notes:
 * - Pure domain logic (no Android dependencies in detection logic)
 * - Platform-specific detection abstracted via DeviceInfoProvider interface
 * - Follows existing algorithm pattern (MemoryStrengthAlgorithm, Sm2Algorithm)
 */
object DeviceCapabilities {
    /**
     * Detect device performance tier based on hardware capabilities
     *
     * Performance Tier Criteria:
     * - HIGH: 8GB+ RAM, OpenGL ES 3.0+, 8+ CPU cores, API 30+
     * - MEDIUM: 3GB+ RAM, OpenGL ES 2.0+, 4+ CPU cores
     * - LOW: Below MEDIUM thresholds
     *
     * Note: 6GB devices with otherwise high specs (like Samsung A54)
     * are classified as MEDIUM to leave headroom for true flagships.
     *
     * @param deviceInfo Device hardware information
     * @return Detected performance tier
     */
    fun detectPerformanceTier(deviceInfo: DeviceInfo): PerformanceTier {
        // Direct classification for predictable results
        val isHighEnd =
            deviceInfo.totalRamBytes >= 8_000_000_000L &&
                deviceInfo.cpuCores >= 8 &&
                deviceInfo.glEsVersion >= 0x30000 &&
                deviceInfo.apiLevel >= 30

        val isMedium =
            deviceInfo.totalRamBytes >= 3_000_000_000L &&
                deviceInfo.cpuCores >= 4 &&
                deviceInfo.glEsVersion >= 0x20000

        return when {
            isHighEnd -> PerformanceTier.HIGH
            isMedium -> PerformanceTier.MEDIUM
            else -> PerformanceTier.LOW
        }
    }

    /**
     * Check if device should use low memory mode
     *
     * @param deviceInfo Device hardware information
     * @return True if device is memory-constrained
     */
    fun isLowRamDevice(deviceInfo: DeviceInfo): Boolean {
        return deviceInfo.totalRamBytes < 3_000_000_000L || // < 3GB
            deviceInfo.isLowRamFlag
    }

    /**
     * Get recommended texture resolution based on device capabilities
     *
     * @param tier Performance tier
     * @return Recommended texture resolution (scale factor)
     */
    fun getTextureResolution(tier: PerformanceTier): Float {
        return when (tier) {
            PerformanceTier.HIGH -> 1.0f // Full resolution
            PerformanceTier.MEDIUM -> 0.75f // 3/4 resolution
            PerformanceTier.LOW -> 0.5f // Half resolution
        }
    }

    /**
     * Get maximum concurrent animations based on device tier
     *
     * @param tier Performance tier
     * @return Maximum number of concurrent animations
     */
    fun getMaxConcurrentAnimations(tier: PerformanceTier): Int {
        return when (tier) {
            PerformanceTier.HIGH -> 5
            PerformanceTier.MEDIUM -> 3
            PerformanceTier.LOW -> 1
        }
    }

    /**
     * Get target frame rate based on device tier
     *
     * @param tier Performance tier
     * @return Target FPS
     */
    fun getTargetFps(tier: PerformanceTier): Int {
        return when (tier) {
            PerformanceTier.HIGH -> 60
            PerformanceTier.MEDIUM -> 30
            PerformanceTier.LOW -> 30
        }
    }
}

/**
 * Device performance tier classification
 *
 * Maps to AnimationQuality in domain/model/AnimationQuality.kt
 */
enum class PerformanceTier {
    /**
     * High performance - All features, 60fps target
     * Devices: Flagship phones (e.g., Samsung S23+, Pixel 7+)
     */
    HIGH,

    /**
     * Medium performance - Reduced animations, 30fps target
     * Devices: Mid-range phones (e.g., Samsung A-series, older flagships)
     */
    MEDIUM,

    /**
     * Low performance - Static rendering, minimal animations
     * Devices: Budget phones, very old devices
     */
    LOW,
}

/**
 * Device hardware information
 *
 * Data class representing device hardware capabilities.
 * Populated by platform-specific code (Android ActivityManager).
 *
 * Note: Using a regular data class instead of @JvmInline value class
 * because JaCoCo cannot instrument value classes with nested classes.
 */
data class DeviceInfo(
    val totalRamBytes: Long,
    val cpuCores: Int,
    val glEsVersion: Int,
    val apiLevel: Int,
    val isLowRamFlag: Boolean,
    val manufacturer: String,
    val model: String,
) {
    companion object {
        /**
         * Create DeviceInfo from raw values
         */
        fun from(
            totalRamBytes: Long,
            cpuCores: Int,
            glEsVersion: Int,
            apiLevel: Int,
            isLowRamFlag: Boolean,
            manufacturer: String,
            model: String,
        ): DeviceInfo {
            return DeviceInfo(
                totalRamBytes = totalRamBytes,
                cpuCores = cpuCores,
                glEsVersion = glEsVersion,
                apiLevel = apiLevel,
                isLowRamFlag = isLowRamFlag,
                manufacturer = manufacturer,
                model = model,
            )
        }
    }
}

/**
 * Device capability summary
 *
 * Human-readable summary of device capabilities for debugging
 */
data class DeviceCapabilitySummary(
    val tier: PerformanceTier,
    val totalRamMB: Long,
    val cpuCores: Int,
    val glEsVersion: String,
    val apiLevel: Int,
    val deviceName: String,
    val isLowRam: Boolean,
    val targetFps: Int,
    val textureScale: Float,
    val maxAnimations: Int,
) {
    companion object {
        /**
         * Generate summary from device info and detected tier
         */
        fun from(
            deviceInfo: DeviceInfo,
            tier: PerformanceTier,
        ): DeviceCapabilitySummary {
            return DeviceCapabilitySummary(
                tier = tier,
                totalRamMB = deviceInfo.totalRamBytes / (1024 * 1024),
                cpuCores = deviceInfo.cpuCores,
                glEsVersion = formatGlEsVersion(deviceInfo.glEsVersion),
                apiLevel = deviceInfo.apiLevel,
                deviceName = "${deviceInfo.manufacturer} ${deviceInfo.model}",
                isLowRam = DeviceCapabilities.isLowRamDevice(deviceInfo),
                targetFps = DeviceCapabilities.getTargetFps(tier),
                textureScale = DeviceCapabilities.getTextureResolution(tier),
                maxAnimations = DeviceCapabilities.getMaxConcurrentAnimations(tier),
            )
        }

        private fun formatGlEsVersion(version: Int): String {
            val major = (version shr 16) and 0xFF
            val minor = version and 0xFF
            return "$major.$minor"
        }
    }

    /**
     * Get summary as log-friendly string
     */
    fun toLogString(): String {
        return buildString {
            appendLine("Device Capabilities:")
            appendLine("  Device: $deviceName")
            appendLine("  Tier: $tier")
            appendLine("  RAM: ${totalRamMB}MB")
            appendLine("  CPU: $cpuCores cores")
            appendLine("  GPU: OpenGL ES $glEsVersion")
            appendLine("  Android: API $apiLevel")
            appendLine("  Low RAM: $isLowRam")
            appendLine("  Target FPS: $targetFps")
            appendLine("  Texture Scale: ${textureScale}x")
            appendLine("  Max Animations: $maxAnimations")
        }
    }
}
