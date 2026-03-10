package com.wordland.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wordland.di.AppServiceLocator
import com.wordland.ui.screens.*

/**
 * Navigation Graph Setup
 * Defines all screens and navigation routes
 *
 * @param navController The navigation controller
 * @param startDestination The starting route (defaults to HOME, can be PET_SELECTION for first-time users)
 */
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String = NavRoute.HOME,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInRight },
        exitTransition = { fadeOut },
        popEnterTransition = { slideInLeft },
        popExitTransition = { slideOutRight },
    ) {
        // Onboarding Welcome Screen
        composable(route = NavRoute.ONBOARDING_WELCOME) {
            val viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
                viewModel(
                    factory = AppServiceLocator.provideFactory(),
                )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.startOnboarding()
            }

            OnboardingWelcomeScreen(
                onStartClick = {
                    android.util.Log.d("SetupNavGraph", "Start button clicked, navigating to PET_SELECTION")
                    navController.navigate(NavRoute.ONBOARDING_PET_SELECTION)
                },
                onLearnMoreClick = null,
            )
        }

        // Onboarding Pet Selection Screen
        composable(route = NavRoute.ONBOARDING_PET_SELECTION) {
            val viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
                viewModel(
                    factory = AppServiceLocator.provideFactory(),
                )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            OnboardingPetSelectionScreen(
                onPetSelected = { petId ->
                    android.util.Log.d("SetupNavGraph", "onPetSelected called with petId: $petId")
                    viewModel.selectPet(petId)
                    // Navigation will happen when uiState changes to Tutorial
                },
            )

            // Navigate to Tutorial or Chest when state changes
            LaunchedEffect(uiState) {
                when (uiState) {
                    is com.wordland.domain.model.OnboardingUiState.Tutorial -> {
                        navController.navigate(NavRoute.ONBOARDING_TUTORIAL)
                    }
                    is com.wordland.domain.model.OnboardingUiState.OpeningChest -> {
                        // Skip tutorial, go directly to chest
                        navController.navigate(NavRoute.ONBOARDING_CHEST) {
                            // Pop up to PET_SELECTION to avoid going back
                            popUpTo(NavRoute.ONBOARDING_PET_SELECTION) { inclusive = true }
                        }
                    }
                    else -> {
                        // Other states: Loading, Error, Welcome - do nothing
                    }
                }
            }
        }

        // Onboarding Tutorial Screen
        composable(route = NavRoute.ONBOARDING_TUTORIAL) {
            val viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
                viewModel(
                    factory = AppServiceLocator.provideFactory(),
                )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            OnboardingTutorialScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )

            // Navigate to Chest when tutorial completes
            LaunchedEffect(uiState) {
                if (uiState is com.wordland.domain.model.OnboardingUiState.OpeningChest) {
                    navController.navigate(NavRoute.ONBOARDING_CHEST)
                }
            }
        }

        // Onboarding Chest Screen
        composable(route = NavRoute.ONBOARDING_CHEST) {
            val viewModel: com.wordland.ui.viewmodel.OnboardingViewModel =
                viewModel(
                    factory = AppServiceLocator.provideFactory(),
                )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            OnboardingChestScreen(
                viewModel = viewModel,
                onDismiss = {
                    navController.navigate(NavRoute.HOME) {
                        popUpTo(NavRoute.ONBOARDING_WELCOME) { inclusive = true }
                    }
                },
            )
        }

        // Pet Selection Screen (for onboarding)
        composable(route = NavRoute.PET_SELECTION) {
            PetSelectionScreen(
                onPetSelected = { petId ->
                    // Navigate to Home after pet selection
                    navController.navigate(NavRoute.HOME) {
                        // Pop the pet selection screen from back stack
                        popUpTo(NavRoute.PET_SELECTION) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Home Screen
        composable(route = NavRoute.HOME) {
            HomeScreen(
                onNavigateToIslandMap = {
                    navController.navigate(NavRoute.ISLAND_MAP)
                },
                onNavigateToReview = {
                    navController.navigate(NavRoute.REVIEW)
                },
                onNavigateToProgress = {
                    navController.navigate(NavRoute.PROGRESS)
                },
                onNavigateToMultipleChoice = {
                    navController.navigate(NavRoute.multipleChoice("look_island_level_01", "look_island"))
                },
                onNavigateToFillBlank = {
                    navController.navigate(NavRoute.fillBlank("look_island_level_01", "look_island"))
                },
                onNavigateToMatchGame = {
                    navController.navigate(NavRoute.matchGame("look_island_level_01", "look_island"))
                },
            )
        }

        // Island Map Screen
        composable(route = NavRoute.ISLAND_MAP) {
            WorldMapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLevelSelect = { islandId ->
                    navController.navigate("${NavRoute.LEVEL_SELECT}/$islandId")
                },
            )
        }

        // Level Select Screen
        composable(
            route = "${NavRoute.LEVEL_SELECT}/{islandId}",
            arguments =
                listOf(
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val islandId = backStackEntry.arguments?.getString("islandId") ?: "look_island"
            LevelSelectScreen(
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartLevel = { levelId ->
                    navController.navigate(NavRoute.learning(levelId, islandId))
                },
                onStartSpellBattle = { levelId, _ ->
                    navController.navigate(NavRoute.learning(levelId, islandId))
                },
                onStartQuickJudge = { levelId, _ ->
                    navController.navigate(NavRoute.quickJudge(levelId, islandId))
                },
            )
        }

        // Learning Screen
        composable(
            route = NavRoute.LEARNING,
            arguments =
                listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
            LearningScreen(
                levelId = levelId,
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewStarBreakdown = { stars, accuracy, hintsUsed, timeTaken, errorCount ->
                    navController.navigate(
                        NavRoute.starBreakdown(
                            stars = stars,
                            accuracy = accuracy,
                            hintsUsed = hintsUsed,
                            timeTaken = timeTaken,
                            errorCount = errorCount,
                            islandId = islandId,
                        ),
                    )
                },
            )
        }

        // Match Game Screen
        composable(
            route = NavRoute.MATCH_GAME,
            arguments =
                listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
            MatchGameScreen(
                levelId = levelId,
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Multiple Choice Screen (for onboarding)
        composable(
            route = NavRoute.MULTIPLE_CHOICE,
            arguments =
                listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
            MultipleChoiceScreen(
                levelId = levelId,
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Fill Blank Screen (for onboarding)
        composable(
            route = NavRoute.FILL_BLANK,
            arguments =
                listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
            FillBlankScreen(
                levelId = levelId,
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Quick Judge Screen
        composable(
            route = NavRoute.QUICK_JUDGE,
            arguments =
                listOf(
                    navArgument("levelId") { type = NavType.StringType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""
            QuickJudgeScreen(
                levelId = levelId,
                islandId = islandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLevelComplete = {
                    // Navigate back to level select after completion
                    navController.popBackStack()
                },
            )
        }

        // Review Screen
        composable(route = NavRoute.REVIEW) {
            ReviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartReview = { wordId ->
                    // Navigate to specific word review
                    // TODO: Implement word detail screen
                },
            )
        }

        // Progress Screen
        composable(route = NavRoute.PROGRESS) {
            ProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // Star Breakdown Screen
        composable(
            route = NavRoute.STAR_BREAKDOWN,
            arguments =
                listOf(
                    navArgument("stars") { type = NavType.IntType },
                    navArgument("accuracy") { type = NavType.IntType },
                    navArgument("hintsUsed") { type = NavType.IntType },
                    navArgument("timeTaken") { type = NavType.IntType },
                    navArgument("errorCount") { type = NavType.IntType },
                    navArgument("islandId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val stars = backStackEntry.arguments?.getInt("stars") ?: 0
            val accuracy = backStackEntry.arguments?.getInt("accuracy") ?: 0
            val hintsUsed = backStackEntry.arguments?.getInt("hintsUsed") ?: 0
            val timeTaken = backStackEntry.arguments?.getInt("timeTaken") ?: 0
            val errorCount = backStackEntry.arguments?.getInt("errorCount") ?: 0
            val islandId = backStackEntry.arguments?.getString("islandId") ?: ""

            StarBreakdownScreen(
                starRating = stars,
                accuracy = accuracy,
                hintsUsed = hintsUsed,
                timeTaken = timeTaken,
                errorCount = errorCount,
                onClose = {
                    // Navigate back to level select screen
                    navController.navigate("${NavRoute.LEVEL_SELECT}/$islandId") {
                        // Pop up to level select to avoid back stack issues
                        popUpTo("${NavRoute.LEVEL_SELECT}/$islandId") {
                            inclusive = true
                        }
                    }
                },
            )
        }

        // Audio Settings Screen
        composable(route = NavRoute.AUDIO_SETTINGS) {
            AudioSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}

/**
 * Slide in from right transition (forward navigation)
 */
private val slideInRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) +
        fadeIn(
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )

/**
 * Slide out to right transition (back navigation)
 */
private val slideOutRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) +
        fadeOut(
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )

/**
 * Slide in from left transition (back navigation)
 */
private val slideInLeft: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) +
        fadeIn(
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )

/**
 * Slide out to left transition (forward navigation)
 */
private val slideOutLeft: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec =
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing,
            ),
    ) +
        fadeOut(
            animationSpec =
                tween(
                    durationMillis = 200,
                    easing = LinearEasing,
                ),
        )

/**
 * Fade out transition
 */
private val fadeOut: ExitTransition =
    fadeOut(
        animationSpec =
            tween(
                durationMillis = 200,
                easing = LinearEasing,
            ),
    )
