package com.wordland

import android.app.Application
import android.util.Log
import com.wordland.data.database.WordDatabase
import com.wordland.data.repository.IslandMasteryRepositoryImpl
import com.wordland.data.repository.ProgressRepositoryImpl
import com.wordland.data.repository.WordRepositoryImpl
import com.wordland.data.seed.AppDataInitializer
import com.wordland.data.seed.LevelDataSeeder
import com.wordland.data.seed.MakeLakeSeeder
import com.wordland.performance.ImageLoadingOptimizer
import com.wordland.performance.PerformanceMonitor
import com.wordland.performance.StartupPerformanceTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main Application class for Wordland
 *
 * Performance Optimizations:
 * - Startup time tracking
 * - Lazy data initialization
 * - Performance monitoring
 * - Optimized image loading
 */
class WordlandApplication : Application() {
    companion object {
        const val USER_ID = "user_001"

        lateinit var instance: WordlandApplication
            private set
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        StartupPerformanceTracker.onApplicationCreate()
        super.onCreate()
        instance = this

        // Initialize LeakCanary for memory leak detection (debug builds only)
        // LeakCanary is automatically installed via debugImplementation dependency
        // Ref: https://square.github.io/leakcanary/
        initializeLeakCanary()
        StartupPerformanceTracker.recordPhase("LeakCanary_initialized")

        // Initialize performance monitoring
        PerformanceMonitor.initialize()
        StartupPerformanceTracker.recordPhase("PerformanceMonitor_initialized")

        // Initialize optimized image loading
        ImageLoadingOptimizer.initialize(this)
        StartupPerformanceTracker.recordPhase("ImageLoader_initialized")

        // Initialize app data on first launch (lazy, in background)
        initializeAppData()
    }

    /**
     * Initialize LeakCanary for memory leak detection.
     * Only available in debug builds via debugImplementation dependency.
     */
    private fun initializeLeakCanary() {
        // LeakCanary 2.x auto-initializes via ContentProvider
        // No manual installation needed, but we can detect if it's available
        try {
            val leakCanaryClass = Class.forName("com.squareup.leakcanary.LeakCanary")
            val isInAnalyzerProcessMethod = leakCanaryClass.getMethod("isInAnalyzerProcess", Application::class.java)
            val isInAnalyzer = isInAnalyzerProcessMethod.invoke(null, this) as? Boolean ?: false

            if (isInAnalyzer) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                Log.d("WordlandApplication", "Running in LeakCanary analyzer process")
            } else {
                Log.d("WordlandApplication", "LeakCanary installed and monitoring for leaks")
            }
        } catch (e: ClassNotFoundException) {
            // LeakCanary not available in this build variant (release)
            Log.d("WordlandApplication", "LeakCanary not available in this build")
        }
    }

    /**
     * Initialize application data (optimized)
     * Creates repositories and calls AppDataInitializer
     * Moved to background to avoid blocking startup
     */
    private fun initializeAppData() {
        applicationScope.launch {
            PerformanceMonitor.startOperation("Data_Initialization")

            try {
                val database = WordDatabase.getInstance(applicationContext)

                // Create repositories
                val wordDao = database.wordDao()
                val progressDao = database.progressDao()
                val islandMasteryDao = database.islandMasteryDao()

                val wordRepository = WordRepositoryImpl(wordDao)
                val progressRepository = ProgressRepositoryImpl(progressDao)
                val islandMasteryRepository = IslandMasteryRepositoryImpl(islandMasteryDao, progressDao)
                val levelDataSeeder = LevelDataSeeder(wordRepository, progressRepository)
                val makeLakeSeeder = MakeLakeSeeder(wordDao, progressDao, islandMasteryDao, database.trackingDao())

                // Create data initializer
                val appDataInitializer =
                    AppDataInitializer(
                        levelDataSeeder,
                        makeLakeSeeder,
                        islandMasteryRepository,
                        progressRepository,
                        wordDao,
                    )

                // Initialize all data
                appDataInitializer.initializeAllData()

                PerformanceMonitor.endOperation("Data_Initialization")
                StartupPerformanceTracker.onDataInitComplete()

                Log.d("WordlandApplication", "App data initialized successfully")
            } catch (e: Exception) {
                PerformanceMonitor.endOperation("Data_Initialization")
                Log.e("WordlandApplication", "Failed to initialize app data", e)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w("WordlandApplication", "Low memory warning received")
        PerformanceMonitor.logReport()

        // Clear image caches
        ImageLoadingOptimizer.clearCaches(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN, TRIM_MEMORY_BACKGROUND -> {
                // Clear image caches when UI is hidden
                ImageLoadingOptimizer.clearCaches(this)
            }
            TRIM_MEMORY_COMPLETE, TRIM_MEMORY_MODERATE, TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Aggressive memory cleanup
                ImageLoadingOptimizer.clearCaches(this)
                PerformanceMonitor.reset()
            }
        }
    }
}
