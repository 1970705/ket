package com.wordland.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.wordland.WordlandApplication
import com.wordland.data.assets.AudioAssetManager
import com.wordland.data.repository.*
import com.wordland.data.seed.AchievementSeeder
import com.wordland.domain.achievement.AchievementTracker
import com.wordland.domain.behavior.BehaviorAnalyzer
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.hint.HintManager
import com.wordland.domain.usecase.usecases.*
import com.wordland.media.SoundManager
import com.wordland.media.TTSController
import com.wordland.ui.viewmodel.AudioSettingsViewModel
import com.wordland.ui.viewmodel.LearningViewModel
import com.wordland.ui.viewmodel.MatchGameViewModel
import com.wordland.ui.viewmodel.OnboardingViewModel
import com.wordland.ui.viewmodel.QuickJudgeViewModel

/**
 * Simple Service Locator for providing ViewModels
 * This replaces Hilt's @AndroidEntryPoint for Activity-level injection
 */
object AppServiceLocator {
    private val database: com.wordland.data.database.WordDatabase by lazy {
        com.wordland.data.database.WordDatabase.getInstance(WordlandApplication.instance)
    }

    // Lazy init repositories
    private val wordRepository: WordRepository by lazy {
        WordRepositoryImpl(wordDao = database.wordDao())
    }

    private val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(progressDao = database.progressDao())
    }

    private val islandRepository: IslandMasteryRepository by lazy {
        IslandMasteryRepositoryImpl(
            islandMasteryDao = database.islandMasteryDao(),
            progressDao = database.progressDao(),
        )
    }

    private val trackingRepository: TrackingRepository by lazy {
        TrackingRepositoryImpl(trackingDao = database.trackingDao())
    }

    // World Map repository
    private val worldMapRepository: WorldMapRepository by lazy {
        WorldMapRepositoryImpl(worldMapDao = database.worldMapDao())
    }

    // Pet repository (uses SharedPreferences, not database)
    val petRepository: PetRepository by lazy {
        PetRepositoryImpl(context = WordlandApplication.instance)
    }

    // Achievement repository
    private val achievementRepository: AchievementRepository by lazy {
        AchievementRepository(
            achievementDao = database.achievementDao(),
            progressRepository = progressRepository,
        )
    }

    // Achievement seeder
    val achievementSeeder: AchievementSeeder by lazy {
        AchievementSeeder(achievementRepository)
    }

    // Achievement tracker
    private val achievementTracker: AchievementTracker by lazy {
        AchievementTracker(progressRepository)
    }

    // Grant reward use case
    private val grantRewardUseCase: GrantRewardUseCase by lazy {
        GrantRewardUseCase()
    }

    // Audio manager for word pronunciation
    val audioManager: AudioAssetManager by lazy {
        AudioAssetManager(WordlandApplication.instance)
    }

    // TTS Controller for text-to-speech pronunciation
    val ttsController: TTSController by lazy {
        TTSController(WordlandApplication.instance)
    }

    // Sound Manager for sound effects and background music
    val soundManager: SoundManager by lazy {
        SoundManager(WordlandApplication.instance)
    }

    // Lazy init use cases
    private val getIslandsUseCase: GetIslandsUseCase by lazy {
        GetIslandsUseCase(islandRepository)
    }

    private val getLevelsUseCase: GetLevelsUseCase by lazy {
        GetLevelsUseCase(progressRepository)
    }

    private val getUserProgressUseCase: GetUserProgressUseCase by lazy {
        GetUserProgressUseCase(progressRepository)
    }

    private val getUserStatsUseCase: GetUserStatsUseCase by lazy {
        GetUserStatsUseCase(
            progressRepository,
            islandRepository,
            wordRepository,
        )
    }

    private val getReviewWordsUseCase: GetReviewWordsUseCase by lazy {
        GetReviewWordsUseCase(
            wordRepository,
            progressRepository,
        )
    }

    private val loadLevelWordsUseCase: LoadLevelWordsUseCase by lazy {
        LoadLevelWordsUseCase(wordRepository)
    }

    // Combo system is now an object (ComboManager)

    private val submitAnswerUseCase: SubmitAnswerUseCase by lazy {
        SubmitAnswerUseCase(
            wordRepository,
            progressRepository,
            trackingRepository,
        )
    }

    private val unlockNextLevelUseCase: UnlockNextLevelUseCase by lazy {
        UnlockNextLevelUseCase(progressRepository)
    }

    // Enhanced hint system components
    private val hintGenerator: HintGenerator by lazy { HintGenerator() }

    private val hintManager: HintManager by lazy { HintManager() }

    private val behaviorAnalyzer: BehaviorAnalyzer by lazy {
        BehaviorAnalyzer(trackingRepository)
    }

    private val useHintUseCaseEnhanced: UseHintUseCaseEnhanced by lazy {
        UseHintUseCaseEnhanced(
            trackingRepository = trackingRepository,
            wordRepository = wordRepository,
            hintGenerator = hintGenerator,
            hintManager = hintManager,
            behaviorAnalyzer = behaviorAnalyzer,
        )
    }

    // World Map use cases
    private val getWorldMapStateUseCase: GetWorldMapStateUseCase by lazy {
        GetWorldMapStateUseCase(worldMapRepository)
    }

    private val exploreRegionUseCase: ExploreRegionUseCase by lazy {
        ExploreRegionUseCase(worldMapRepository)
    }

    private val toggleMapViewModeUseCase: ToggleMapViewModeUseCase by lazy {
        ToggleMapViewModeUseCase()
    }

    // Quick Judge use cases
    private val generateQuickJudgeQuestionsUseCase: GenerateQuickJudgeQuestionsUseCase by lazy {
        GenerateQuickJudgeQuestionsUseCase(wordRepository)
    }

    private val submitQuickJudgeAnswerUseCase: SubmitQuickJudgeAnswerUseCase by lazy {
        SubmitQuickJudgeAnswerUseCase(
            wordRepository,
            progressRepository,
            trackingRepository,
        )
    }

    // Match Game use cases
    private val getWordPairsUseCase: GetWordPairsUseCase by lazy {
        GetWordPairsUseCase(wordRepository)
    }

    private val checkMatchUseCase: CheckMatchUseCase by lazy {
        CheckMatchUseCase()
    }

    private val updateGameStateUseCase: UpdateGameStateUseCase by lazy {
        UpdateGameStateUseCase(checkMatchUseCase)
    }

    // Onboarding repository
    val onboardingRepository by lazy {
        com.wordland.data.repository.OnboardingRepositoryImpl(
            dao = database.onboardingStateDao(),
        )
    }

    // Onboarding use cases
    private val startOnboardingUseCase by lazy {
        StartOnboardingUseCase(onboardingRepository)
    }

    private val selectPetUseCase by lazy {
        SelectPetUseCase(onboardingRepository)
    }

    private val getTutorialWordsUseCase by lazy {
        GetTutorialWordsUseCase()
    }

    private val completeTutorialWordUseCase by lazy {
        CompleteTutorialWordUseCase(onboardingRepository)
    }

    private val openFirstChestUseCase by lazy {
        OpenFirstChestUseCase(onboardingRepository)
    }

    private val completeOnboardingUseCase by lazy {
        CompleteOnboardingUseCase(onboardingRepository)
    }

    // Achievement use cases
    val checkAchievementsUseCase: CheckAchievementsUseCase by lazy {
        CheckAchievementsUseCase(
            achievementRepository = achievementRepository,
            achievementTracker = achievementTracker,
            grantRewardUseCase = grantRewardUseCase,
        )
    }

    val getAchievementsUseCase: GetAchievementsUseCase by lazy {
        GetAchievementsUseCase(achievementRepository)
    }

    /**
     * Factory for creating ViewModels
     */
    fun provideFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                return when (modelClass) {
                    com.wordland.ui.viewmodel.IslandMapViewModel::class.java ->
                        com.wordland.ui.viewmodel.IslandMapViewModel(getIslandsUseCase) as T

                    com.wordland.ui.viewmodel.LevelSelectViewModel::class.java ->
                        com.wordland.ui.viewmodel.LevelSelectViewModel(getLevelsUseCase) as T

                    com.wordland.ui.viewmodel.ProgressViewModel::class.java ->
                        com.wordland.ui.viewmodel.ProgressViewModel(getUserProgressUseCase) as T

                    com.wordland.ui.viewmodel.HomeViewModel::class.java ->
                        com.wordland.ui.viewmodel.HomeViewModel(
                            getUserStatsUseCase,
                            getIslandsUseCase,
                        ) as T

                    com.wordland.ui.viewmodel.ReviewViewModel::class.java ->
                        com.wordland.ui.viewmodel.ReviewViewModel(getReviewWordsUseCase) as T

                    com.wordland.ui.viewmodel.WorldMapViewModel::class.java ->
                        com.wordland.ui.viewmodel.WorldMapViewModel(
                            getWorldMapStateUseCase,
                            exploreRegionUseCase,
                            toggleMapViewModeUseCase,
                        ) as T

                    LearningViewModel::class.java -> {
                        val savedStateHandle = extras.createSavedStateHandle()
                        LearningViewModel(
                            loadLevelWordsUseCase,
                            submitAnswerUseCase,
                            useHintUseCaseEnhanced,
                            unlockNextLevelUseCase,
                            exploreRegionUseCase,
                            islandRepository,
                            progressRepository,
                            petRepository,
                            savedStateHandle,
                            ttsController,
                            soundManager,
                        ) as T
                    }

                    QuickJudgeViewModel::class.java -> {
                        val savedStateHandle = extras.createSavedStateHandle()
                        QuickJudgeViewModel(
                            generateQuickJudgeQuestionsUseCase,
                            submitQuickJudgeAnswerUseCase,
                            unlockNextLevelUseCase,
                            exploreRegionUseCase,
                            islandRepository,
                            progressRepository,
                            petRepository,
                            savedStateHandle,
                            soundManager,
                        ) as T
                    }

                    MatchGameViewModel::class.java -> {
                        MatchGameViewModel(
                            getWordPairsUseCase,
                            checkMatchUseCase,
                        ) as T
                    }

                    OnboardingViewModel::class.java -> {
                        OnboardingViewModel(
                            startOnboardingUseCase = startOnboardingUseCase,
                            selectPetUseCase = selectPetUseCase,
                            getTutorialWordsUseCase = getTutorialWordsUseCase,
                            completeTutorialWordUseCase = completeTutorialWordUseCase,
                            openFirstChestUseCase = openFirstChestUseCase,
                            completeOnboardingUseCase = completeOnboardingUseCase,
                        ) as T
                    }

                    AudioSettingsViewModel::class.java -> {
                        AudioSettingsViewModel(
                            sharedPreferences =
                                WordlandApplication.instance.getSharedPreferences(
                                    "audio_settings",
                                    android.content.Context.MODE_PRIVATE,
                                ),
                        ) as T
                    }

                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
}
