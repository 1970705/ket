package com.wordland

import android.app.Application

/**
 * Test Application for Robolectric.
 *
 * Provides the necessary Android context for Compose testing.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Set up test environment
    }
}
