package com.wordland.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wordland.di.AppServiceLocator
import com.wordland.navigation.NavRoute
import com.wordland.navigation.SetupNavGraph
import com.wordland.performance.PerformanceMonitor
import com.wordland.performance.StartupPerformanceTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main Activity for Wordland app
 * Entry point for the application, sets up navigation
 *
 * Performance Optimizations:
 * - Startup time tracking
 * - First frame monitoring
 * - Composition tracking
 *
 * First-Time User Flow (Epic #10 Onboarding):
 * - Checks if user has completed onboarding
 * - If not completed: Navigate to ONBOARDING_WELCOME
 * - If completed: Navigate to HomeScreen
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        StartupPerformanceTracker.onActivityCreate(this)
        super.onCreate(savedInstanceState)

        PerformanceMonitor.startOperation("MainActivity_SetContent")

        setContent {
            WordlandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf(NavRoute.HOME) }
                    var hasCheckedOnboarding by remember { mutableStateOf(false) }

                    // Check if user has completed onboarding on first launch
                    LaunchedEffect(Unit) {
                        if (!hasCheckedOnboarding) {
                            Log.d("MainActivity", "Checking onboarding state...")
                            val onboardingState =
                                withContext(Dispatchers.IO) {
                                    AppServiceLocator.onboardingRepository.getOnboardingState()
                                }

                            Log.d("MainActivity", "Onboarding state: $onboardingState")

                            startDestination =
                                if (onboardingState == null ||
                                    onboardingState.currentPhase !=
                                    com.wordland.domain.model.OnboardingPhase.COMPLETED
                                ) {
                                    Log.d("MainActivity", "Starting at ONBOARDING_WELCOME")
                                    NavRoute.ONBOARDING_WELCOME
                                } else {
                                    Log.d("MainActivity", "Starting at HOME")
                                    NavRoute.HOME
                                }
                            hasCheckedOnboarding = true
                            Log.d("MainActivity", "Final startDestination: $startDestination")
                        }
                    }

                    if (hasCheckedOnboarding) {
                        SetupNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                        )
                    }
                }
            }
        }

        PerformanceMonitor.endOperation("MainActivity_SetContent")

        // Track first frame rendering
        window.decorView.postOnAnimation {
            PerformanceMonitor.endOperation("First_Frame_Rendering")
        }
    }
}

@Composable
fun WordlandTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content,
    )
}
