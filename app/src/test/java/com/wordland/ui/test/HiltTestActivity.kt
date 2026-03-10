package com.wordland.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Test Activity for Robolectric Compose testing.
 *
 * This custom Activity resolves the ComponentActivity resolution issue
 * in Robolectric 4.13+ when using ActivityScenario.
 *
 * See: https://github.com/robolectric/robolectric/pull/4736
 */
class HiltTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Content is set by tests via setContent {} calls
    }
}
