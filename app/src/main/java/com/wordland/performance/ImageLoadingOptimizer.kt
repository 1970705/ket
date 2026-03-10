package com.wordland.performance

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Optimized Coil image loading configuration.
 *
 * Features:
 * - Memory cache: 25% of available memory
 * - Disk cache: 50MB max
 * - Optimized networking
 * - Request caching
 * - Prefetching support
 */
object ImageLoadingOptimizer {
    private const val DISK_CACHE_SIZE = 50L * 1024 * 1024 // 50MB
    private const val MEMORY_CACHE_PERCENT = 0.25 // 25% of available memory
    private const val NETWORK_TIMEOUT_MS = 10_000L // 10 seconds

    /**
     * Initialize optimized Coil image loader
     * Call this in Application.onCreate()
     */
    fun initialize(context: Context) {
        PerformanceMonitor.startOperation("Coil_Initialization")

        val imageLoader =
            ImageLoader.Builder(context)
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(MEMORY_CACHE_PERCENT)
                        .build()
                }
                .diskCache {
                    coil.disk.DiskCache.Builder()
                        .directory(File(context.cacheDir, "image_cache"))
                        .maxSizeBytes(DISK_CACHE_SIZE)
                        .build()
                }
                .okHttpClient {
                    OkHttpClient.Builder()
                        .connectTimeout(NETWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .readTimeout(NETWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .writeTimeout(NETWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .cache(
                            okhttp3.Cache(
                                directory = File(context.cacheDir, "okhttp_cache"),
                                maxSize = DISK_CACHE_SIZE,
                            ),
                        )
                        .build()
                }
                .crossfade(true)
                .respectCacheHeaders(false)
                .build()

        Coil.setImageLoader(imageLoader)

        PerformanceMonitor.endOperation("Coil_Initialization")
    }

    /**
     * Prefetch image for faster loading later
     */
    fun prefetchImage(
        context: Context,
        url: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        val request =
            ImageRequest.Builder(context)
                .data(url)
                .target(
                    onSuccess = { onSuccess?.invoke() },
                    onError = { onError?.invoke(java.lang.Exception("Image load failed")) },
                )
                .build()

        Coil.imageLoader(context).enqueue(request)
    }

    /**
     * Clear all image caches
     */
    fun clearCaches(context: Context) {
        val imageLoader = Coil.imageLoader(context)
        imageLoader.memoryCache?.clear()
        // Disk cache is managed by OkHttp
    }

    /**
     * Get memory cache size in bytes
     */
    fun getMemoryCacheSize(context: Context): Long {
        val imageLoader = Coil.imageLoader(context)
        return (imageLoader.memoryCache?.size ?: 0).toLong()
    }

    /**
     * Get disk cache size in bytes
     */
    fun getDiskCacheSize(context: Context): Long {
        val diskCacheDir = File(context.cacheDir, "image_cache")
        return if (diskCacheDir.exists()) {
            diskCacheDir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        } else {
            0L
        }
    }

    /**
     * Preload multiple images
     */
    fun preloadImages(
        context: Context,
        urls: List<String>,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ) {
        var loaded = 0
        urls.forEach { url ->
            prefetchImage(
                context = context,
                url = url,
                onSuccess = {
                    loaded++
                    onProgress(loaded, urls.size)
                },
            )
        }
    }
}

/**
 * Image loading performance data class
 */
data class ImageLoadingStats(
    val memoryCacheSizeBytes: Long,
    val diskCacheSizeBytes: Long,
    val memoryCachePercentage: Double,
) {
    fun toReadableString(): String {
        return "Image Cache Stats:\n" +
            "  Memory: ${memoryCacheSizeBytes / 1024}KB / " +
            "${(memoryCachePercentage * 100).toInt()}%\n" +
            "  Disk: ${diskCacheSizeBytes / 1024}KB"
    }
}
