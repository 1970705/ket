package com.wordland.platform

import android.app.ActivityManager
import android.content.Context
import com.wordland.domain.performance.DeviceCapabilities
import com.wordland.domain.performance.DeviceInfo
import com.wordland.domain.performance.PerformanceTier

/**
 * Platform-specific device info provider for Android
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * This class provides Android-specific hardware information:
 * - RAM (ActivityManager.MemoryInfo)
 * - CPU cores (Runtime.availableProcessors)
 * - GPU (OpenGL ES version from ConfigurationInfo)
 * - Device classification
 *
 * Architecture Notes:
 * - Platform layer (outside domain layer)
 * - Used to gather hardware info for domain layer detection
 * - Follows Clean Architecture (domain layer depends on abstraction)
 */
class DeviceInfoProvider(private val context: Context) {
    private val activityManager: ActivityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    /**
     * Get device information
     *
     * @return DeviceInfo containing hardware capabilities
     */
    fun getDeviceInfo(): DeviceInfo {
        val memoryInfo = getMemoryInfo()
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val glEsVersion = getGlEsVersion()
        val apiLevel = android.os.Build.VERSION.SDK_INT
        val isLowRam = isLowRamDevice()
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL

        return DeviceInfo.from(
            totalRamBytes = memoryInfo.totalMem,
            cpuCores = cpuCores,
            glEsVersion = glEsVersion,
            apiLevel = apiLevel,
            isLowRamFlag = isLowRam,
            manufacturer = manufacturer,
            model = model,
        )
    }

    /**
     * Get performance tier
     *
     * @return Detected performance tier
     */
    fun getPerformanceTier(): PerformanceTier {
        return DeviceCapabilities.detectPerformanceTier(getDeviceInfo())
    }

    /**
     * Get memory information
     */
    private fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    /**
     * Get OpenGL ES version
     *
     * @return OpenGL ES version as integer (e.g., 0x30000 for ES 3.0)
     */
    private fun getGlEsVersion(): Int {
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion
    }

    /**
     * Check if device is a low RAM device
     *
     * @return true if device is memory-constrained
     */
    private fun isLowRamDevice(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            activityManager.isLowRamDevice
        } else {
            false
        }
    }

    /**
     * Get available memory in bytes
     *
     * @return Available memory in bytes
     */
    fun getAvailableMemoryBytes(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    /**
     * Get total memory in bytes
     *
     * @return Total memory in bytes
     */
    fun getTotalMemoryBytes(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    /**
     * Get memory usage percentage
     *
     * @return Memory usage as percentage (0-100)
     */
    fun getMemoryUsagePercentage(): Float {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return if (memoryInfo.totalMem > 0) {
            ((memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()) * 100f
        } else {
            0f
        }
    }

    companion object {
        /**
         * Singleton instance for easy access
         * Lazy initialized on first access
         */
        @Volatile
        private var instance: DeviceInfoProvider? = null

        fun getInstance(context: Context): DeviceInfoProvider {
            return instance ?: synchronized(this) {
                instance ?: DeviceInfoProvider(context.applicationContext).also { instance = it }
            }
        }

        /**
         * Quick check for device performance tier
         * Uses cached instance if available
         */
        fun quickDetectTier(context: Context): PerformanceTier {
            return getInstance(context).getPerformanceTier()
        }
    }
}

/**
 * Device info cache
 *
 * Caches device info to avoid repeated system calls
 */
class DeviceInfoCache(private val provider: DeviceInfoProvider) {
    private var cachedDeviceInfo: DeviceInfo? = null
    private var cachedTier: PerformanceTier? = null
    private var cacheTime: Long = 0

    private val cacheValidityMs: Long = 5 * 60 * 1000 // 5 minutes

    /**
     * Get cached device info
     *
     * @return Cached device info, or fetches fresh if cache is stale
     */
    fun getDeviceInfo(forceRefresh: Boolean = false): DeviceInfo {
        val now = System.currentTimeMillis()

        if (forceRefresh || cachedDeviceInfo == null || (now - cacheTime) > cacheValidityMs) {
            cachedDeviceInfo = provider.getDeviceInfo()
            cacheTime = now
        }

        return cachedDeviceInfo!!
    }

    /**
     * Get cached performance tier
     *
     * @return Cached performance tier
     */
    fun getPerformanceTier(forceRefresh: Boolean = false): PerformanceTier {
        if (forceRefresh || cachedTier == null) {
            cachedTier = DeviceCapabilities.detectPerformanceTier(getDeviceInfo(forceRefresh))
        }
        return cachedTier!!
    }

    /**
     * Invalidate cache
     */
    fun invalidate() {
        cachedDeviceInfo = null
        cachedTier = null
        cacheTime = 0
    }
}

/**
 * Predefined device profiles for known devices
 *
 * Allows manual quality adjustment for specific device models
 * that may not auto-detect correctly
 */
object DeviceProfiles {
    private val highEndDevices =
        setOf(
            "Pixel 7", "Pixel 7 Pro", "Pixel 8", "Pixel 8 Pro", "Pixel 9", "Pixel 9 Pro",
            "SM-S911", "SM-S916", "SM-S918", "SM-S921", "SM-S928", // Samsung S23 series
            "SM-S901", "SM-S906", "SM-S908", // Samsung S22 series
            "SM-G991", "SM-G996", "SM-G998", // Samsung S21 series
            "iPhone14", "iPhone15", "iPhone16", // iPhone (future)
        )

    private val lowEndDevices =
        setOf(
            "Redmi 9A",
            "Redmi 9C",
            "Redmi 10A",
            "Galaxy A02",
            "Galaxy A03",
            "Galaxy A04",
        )

    /**
     * Check if device is known high-end
     *
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @return true if device is known high-end
     */
    fun isKnownHighEnd(
        manufacturer: String,
        model: String,
    ): Boolean {
        val fullName = "$manufacturer $model"
        return highEndDevices.any { fullName.contains(it, ignoreCase = true) }
    }

    /**
     * Check if device is known low-end
     *
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @return true if device is known low-end
     */
    fun isKnownLowEnd(
        manufacturer: String,
        model: String,
    ): Boolean {
        val fullName = "$manufacturer $model"
        return lowEndDevices.any { fullName.contains(it, ignoreCase = true) }
    }

    /**
     * Get override tier for known devices
     *
     * @param manufacturer Device manufacturer
     * @param model Device model
     * @param detectedTier Auto-detected tier
     * @return Override tier if device is known, otherwise detected tier
     */
    fun getOverrideTier(
        manufacturer: String,
        model: String,
        detectedTier: PerformanceTier,
    ): PerformanceTier {
        return when {
            isKnownHighEnd(manufacturer, model) -> PerformanceTier.HIGH
            isKnownLowEnd(manufacturer, model) -> PerformanceTier.LOW
            else -> detectedTier
        }
    }
}
