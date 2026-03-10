package com.wordland.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.PetRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.algorithm.GuessingDetector
import com.wordland.domain.algorithm.StarRatingCalculator
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.Pet
import com.wordland.domain.model.Result
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.ExploreRegionUseCase
import com.wordland.domain.usecase.usecases.LoadLevelWordsUseCase
import com.wordland.domain.usecase.usecases.SubmitAnswerUseCase
import com.wordland.domain.usecase.usecases.UnlockNextLevelUseCase
import com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced
import com.wordland.media.SoundManager
import com.wordland.media.TTSController
import com.wordland.ui.components.PetAnimationState
import com.wordland.ui.uistate.LearningUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for learning screen
 * Refactored to follow Clean Architecture:
 * - Uses UseCases for business logic
 * - No business logic in ViewModel
 * - Only manages UI state
 *
 * Dynamic Star Rating Algorithm (v1.2):
 * - Uses StarRatingCalculator for level completion rating
 * - Tracks: correct answers, hints used, total time, wrong answers
 * - Child-friendly: minimum 1 star if any correct answer
 */
@HiltViewModel
class LearningViewModel
    @Inject
    constructor(
        private val loadLevelWords: LoadLevelWordsUseCase,
        private val submitAnswer: SubmitAnswerUseCase,
        private val useHint: UseHintUseCaseEnhanced,
        private val unlockNextLevel: UnlockNextLevelUseCase,
        private val exploreRegion: ExploreRegionUseCase,
        private val islandMasteryRepository: IslandMasteryRepository,
        private val progressRepository: ProgressRepository,
        private val petRepository: PetRepository,
        savedStateHandle: SavedStateHandle,
        private val ttsController: TTSController,
        private val soundManager: SoundManager,
    ) : ViewModel() {
        companion object {
            private const val TAG = "LearningViewModel"
        }

        private val _uiState = MutableStateFlow<LearningUiState>(LearningUiState.Loading)
        val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

        private val _currentWord = MutableStateFlow<Word?>(null)
        val currentWord: StateFlow<Word?> = _currentWord.asStateFlow()

        // Pet state for animation control
        private val _selectedPet = MutableStateFlow<Pet?>(null)
        val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

        private val _petAnimationState = MutableStateFlow(PetAnimationState.IDLE)
        val petAnimationState: StateFlow<PetAnimationState> = _petAnimationState.asStateFlow()

        private val _responseStartTime = MutableStateFlow(0L)
        val responseStartTime: Long = _responseStartTime.value

        // Star breakdown screen visibility
        private val _showStarBreakdown = MutableStateFlow(false)
        val showStarBreakdown: StateFlow<Boolean> = _showStarBreakdown.asStateFlow()

        // Navigation params
        private val currentLevelId: String = savedStateHandle.get<String>("levelId") ?: ""
        private val currentIslandId: String = savedStateHandle.get<String>("islandId") ?: ""
        private val userId: String = "user_001" // TODO: Get from user session

        // Level state
        private var levelWords: List<Word> = emptyList()
        private var currentWordIndex = 0
        private val starsEarnedInLevel = mutableListOf<Int>() // Track stars for each word

        // Dynamic star rating tracking
        private var levelStartTime = 0L
        private var totalHintsUsedInLevel = 0
        private var totalWrongAnswersInLevel = 0
        private var correctAnswersInLevel = 0

        // Combo state (tracked per level, resets on wrong answer)
        private var currentComboState = ComboState()
        private var maxComboInLevel = 0

        // Response pattern tracking for unified guessing detection
        private val responsePatterns = mutableListOf<GuessingDetector.ResponsePattern>()

        init {
            if (currentLevelId.isNotEmpty()) {
                loadLevel(currentLevelId, currentIslandId)
            }
            // Load selected pet
            loadSelectedPet()
        }

        /**
         * Load the user's selected pet from PetRepository
         */
        private fun loadSelectedPet() {
            viewModelScope.launch {
                val pet = petRepository.getSelectedPet()
                _selectedPet.value = pet ?: Pet.getDefault()
            }
        }

        /**
         * Trigger a pet animation state
         * Automatically resets to IDLE after a delay
         */
        private fun triggerPetAnimation(state: PetAnimationState) {
            _petAnimationState.value = state
            // Reset to IDLE after animation duration
            viewModelScope.launch {
                kotlinx.coroutines.delay(
                    when (state) {
                        PetAnimationState.HAPPY -> 1500L
                        PetAnimationState.EXCITED -> 2000L
                        PetAnimationState.CELEBRATE -> 3000L
                        PetAnimationState.IDLE -> 0L
                    },
                )
                _petAnimationState.value = PetAnimationState.IDLE
            }
        }

        /**
         * Load level words from UseCase
         * @param levelId The level ID to load
         * @param islandId The island ID (reserved for future island-specific logic)
         */
        @Suppress("UNUSED_PARAMETER")
        fun loadLevel(
            levelId: String,
            islandId: String,
        ) {
            viewModelScope.launch {
                _uiState.value = LearningUiState.Loading

                when (val result = loadLevelWords(levelId)) {
                    is Result.Success -> {
                        levelWords = result.data
                        currentWordIndex = 0
                        showFirstWord()
                    }
                    is Result.Error -> {
                        _uiState.value =
                            LearningUiState.Error(
                                message = result.exception.message ?: "Failed to load level",
                            )
                    }
                    is Result.Loading -> {
                        // Already loading
                    }
                }
            }
        }

        /**
         * Show the first word in the level
         * Resets all level tracking including dynamic star rating data
         */
        private fun showFirstWord() {
            if (levelWords.isNotEmpty()) {
                val firstWord = levelWords[0]
                _currentWord.value = firstWord
                // Reset hints for new word
                useHint.resetHints(firstWord.id)
                // Reset level stars and combo
                starsEarnedInLevel.clear()
                currentComboState = ComboState()
                maxComboInLevel = 0

                // Reset dynamic star rating tracking
                levelStartTime = System.currentTimeMillis()
                totalHintsUsedInLevel = 0
                totalWrongAnswersInLevel = 0
                correctAnswersInLevel = 0

                // Reset response patterns for guessing detection
                responsePatterns.clear()

                _uiState.value =
                    LearningUiState.Ready(
                        question = generateQuestion(firstWord),
                        hintAvailable = true,
                        hintShown = false,
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = useHint.getHintStats(firstWord.id)?.hintsRemaining ?: 3,
                        hintPenaltyApplied = false,
                        comboState = currentComboState,
                        currentWordIndex = 0,
                        totalWords = levelWords.size,
                    )
            }
        }

        /**
         * Submit user's answer
         * Tracks correct/wrong answers for dynamic star rating
         * Tracks response patterns for unified guessing detection
         */
        fun submitAnswer(
            userAnswer: String,
            responseTime: Long,
            hintUsed: Boolean,
        ) {
            viewModelScope.launch {
                val currentWord = _currentWord.value ?: return@launch

                when (
                    val result =
                        submitAnswer(
                            userId = userId,
                            wordId = currentWord.id,
                            userAnswer = userAnswer,
                            responseTime = responseTime,
                            hintUsed = hintUsed,
                            levelId = currentLevelId,
                            previousComboState = currentComboState,
                        )
                ) {
                    is Result.Success -> {
                        // Update combo state
                        currentComboState = result.data.comboState
                        if (currentComboState.consecutiveCorrect > maxComboInLevel) {
                            maxComboInLevel = currentComboState.consecutiveCorrect
                        }

                        // Track answers for dynamic star rating
                        val isCorrect = result.data.isCorrect
                        if (isCorrect) {
                            correctAnswersInLevel++
                            playCorrectSound() // Play correct answer sound
                            triggerPetAnimation(PetAnimationState.HAPPY)
                            // Trigger excited animation and combo sound on significant combo
                            if (currentComboState.consecutiveCorrect >= 3) {
                                playComboSound(currentComboState.consecutiveCorrect) // Play combo sound
                                triggerPetAnimation(PetAnimationState.EXCITED)
                            }
                        } else {
                            totalWrongAnswersInLevel++
                            playWrongSound() // Play wrong answer sound
                        }

                        // NOTE: Hint usage is tracked in useHint() function, not here
                        // The hintUsed flag is used for per-word star calculation only

                        // Track response pattern for guessing detection (unified approach)
                        responsePatterns.add(
                            GuessingDetector.ResponsePattern(
                                timestamp = System.currentTimeMillis(),
                                responseTime = responseTime,
                                isCorrect = isCorrect,
                                hintUsed = hintUsed,
                            ),
                        )

                        // Record stars earned for this word
                        starsEarnedInLevel.add(result.data.starsEarned)
                        _uiState.value =
                            LearningUiState.Feedback(
                                result = result.data.toLearnWordResult(),
                                stars = result.data.starsEarned,
                                progress = calculateProgress(),
                                comboState = currentComboState,
                            )
                    }
                    is Result.Error -> {
                        _uiState.value =
                            LearningUiState.Error(
                                message = result.exception.message ?: "Failed to submit answer",
                            )
                    }
                    is Result.Loading -> {
                        // No loading state for submit
                    }
                }
            }
        }

        /**
         * Use hint
         * Calls the enhanced hint use case to get progressive hint content
         * Tracks hint usage for dynamic star rating
         */
        fun useHint() {
            viewModelScope.launch {
                val currentWord = _currentWord.value ?: return@launch

                when (
                    val result =
                        useHint(
                            userId = userId,
                            wordId = currentWord.id,
                            levelId = currentLevelId,
                        )
                ) {
                    is com.wordland.domain.model.Result.Success -> {
                        val hintResult = result.data
                        // Track hint usage for star rating
                        if (hintResult.hintLevel > 0) {
                            totalHintsUsedInLevel++
                        }
                        // Update UI state with hint information
                        val currentState = _uiState.value
                        if (currentState is LearningUiState.Ready) {
                            _uiState.value =
                                currentState.copy(
                                    hintShown = true,
                                    hintText = hintResult.hintText,
                                    hintLevel = hintResult.hintLevel,
                                    hintsRemaining = hintResult.hintsRemaining,
                                    hintPenaltyApplied = hintResult.shouldApplyPenalty,
                                )
                        }
                    }
                    is com.wordland.domain.model.Result.Error -> {
                        // Handle hint limit exceeded or other errors
                        val currentState = _uiState.value
                        if (currentState is LearningUiState.Ready) {
                            _uiState.value =
                                currentState.copy(
                                    hintAvailable = false,
                                    hintText = result.exception.message,
                                    hintsRemaining = 0, // Ensure UI shows disabled state
                                )
                        }
                    }
                    is com.wordland.domain.model.Result.Loading -> {
                        // No loading state for hints
                    }
                }
            }
        }

        /**
         * Speak a word using TTS
         */
        fun speakWord(word: String) {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Speaking word: $word")
                    ttsController.speak(word)
                } catch (e: Exception) {
                    Log.e(TAG, "TTS error: ${e.message}", e)
                }
            }
        }

        /**
         * Play correct answer sound effect
         */
        private fun playCorrectSound() {
            viewModelScope.launch {
                try {
                    soundManager.playCorrectAnswer()
                    Log.d(TAG, "Played correct answer sound")
                } catch (e: Exception) {
                    Log.e(TAG, "Sound error: ${e.message}", e)
                }
            }
        }

        /**
         * Play wrong answer sound effect
         */
        private fun playWrongSound() {
            viewModelScope.launch {
                try {
                    soundManager.playWrongAnswer()
                    Log.d(TAG, "Played wrong answer sound")
                } catch (e: Exception) {
                    Log.e(TAG, "Sound error: ${e.message}", e)
                }
            }
        }

        /**
         * Play level complete sound effect
         */
        private fun playLevelCompleteSound() {
            viewModelScope.launch {
                try {
                    soundManager.playLevelComplete()
                    Log.d(TAG, "Played level complete sound")
                } catch (e: Exception) {
                    Log.e(TAG, "Sound error: ${e.message}", e)
                }
            }
        }

        /**
         * Play combo sound effect
         */
        private fun playComboSound(level: Int) {
            viewModelScope.launch {
                try {
                    soundManager.playCombo(level)
                    Log.d(TAG, "Played combo sound: level $level")
                } catch (e: Exception) {
                    Log.e(TAG, "Sound error: ${e.message}", e)
                }
            }
        }

        /**
         * Move to next word or complete level
         */
        fun onNextWord() {
            currentWordIndex++
            if (currentWordIndex < levelWords.size) {
                val nextWord = levelWords[currentWordIndex]
                _currentWord.value = nextWord
                // Reset hints for new word
                useHint.resetHints(nextWord.id)
                _uiState.value =
                    LearningUiState.Ready(
                        question = generateQuestion(nextWord),
                        hintAvailable = true,
                        hintShown = false,
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = useHint.getHintStats(nextWord.id)?.hintsRemaining ?: 3,
                        hintPenaltyApplied = false,
                        comboState = currentComboState,
                        currentWordIndex = currentWordIndex,
                        totalWords = levelWords.size,
                    )
            } else {
                // Level complete - unlock next level and update island mastery
                viewModelScope.launch {
                    try {
                        unlockNextLevel(userId, currentLevelId)

                        // Mark region as explored for world map
                        val regionId = getRegionIdForLevel(currentLevelId)
                        exploreRegion(userId, regionId)

                        // Update island mastery progress
                        updateIslandMastery()

                        // Get mastery percentage from database
                        val mastery = islandMasteryRepository.getIslandMastery(userId, currentIslandId)
                        val masteryPercentage = mastery?.masteryPercentage ?: 0.0

                        // Calculate performance breakdown data
                        val totalTimeMs =
                            if (levelStartTime > 0) {
                                System.currentTimeMillis() - levelStartTime
                            } else {
                                0L
                            }
                        val accuracy =
                            if (levelWords.isNotEmpty()) {
                                (correctAnswersInLevel.toFloat() / levelWords.size * 100).toInt()
                            } else {
                                0
                            }

                        // Trigger celebrate animation
                        triggerPetAnimation(PetAnimationState.CELEBRATE)

                        // Play level complete sound
                        playLevelCompleteSound()

                        _uiState.value =
                            LearningUiState.LevelComplete(
                                stars = calculateLevelStars(),
                                score = calculateLevelScore(),
                                isNextIslandUnlocked = false,
                                islandMasteryPercentage = masteryPercentage,
                                maxCombo = maxComboInLevel,
                                accuracy = accuracy,
                                hintsUsed = totalHintsUsedInLevel,
                                timeTaken = (totalTimeMs / 1000).toInt(),
                                errorCount = totalWrongAnswersInLevel,
                            )
                    } catch (e: Exception) {
                        // Log error but don't crash level completion
                        e.printStackTrace()

                        // Calculate performance breakdown data (even in error case)
                        val totalTimeMs =
                            if (levelStartTime > 0) {
                                System.currentTimeMillis() - levelStartTime
                            } else {
                                0L
                            }
                        val accuracy =
                            if (levelWords.isNotEmpty()) {
                                (correctAnswersInLevel.toFloat() / levelWords.size * 100).toInt()
                            } else {
                                0
                            }

                        // Play level complete sound
                        playLevelCompleteSound()

                        _uiState.value =
                            LearningUiState.LevelComplete(
                                stars = calculateLevelStars(),
                                score = calculateLevelScore(),
                                isNextIslandUnlocked = false,
                                islandMasteryPercentage = 0.0,
                                maxCombo = maxComboInLevel,
                                accuracy = accuracy,
                                hintsUsed = totalHintsUsedInLevel,
                                timeTaken = (totalTimeMs / 1000).toInt(),
                                errorCount = totalWrongAnswersInLevel,
                            )
                    }
                }
            }
        }

        /**
         * Start response timer
         */
        fun startResponseTimer() {
            _responseStartTime.value = System.currentTimeMillis()
        }

        /**
         * Calculate current progress
         */
        private fun calculateProgress(): Float {
            return if (levelWords.isNotEmpty()) {
                (currentWordIndex + 1).toFloat() / levelWords.size
            } else {
                0f
            }
        }

        /**
         * Calculate level stars using the dynamic star rating algorithm
         *
         * Uses StarRatingCalculator which considers:
         * - Accuracy (correct / total words)
         * - Hints used (penalty)
         * - Time taken (fast completion bonus)
         * - Wrong answers (penalty)
         * - Combo (bonus for consecutive correct)
         * - Guessing (penalty from unified GuessingDetector)
         *
         * Child-friendly: At least 1 star if any correct answer
         */
        private fun calculateLevelStars(): Int {
            if (levelWords.isEmpty()) return 0

            // Calculate total time for the level
            val totalTimeMs =
                if (levelStartTime > 0) {
                    System.currentTimeMillis() - levelStartTime
                } else {
                    0L
                }

            // Use unified guessing detection from GuessingDetector
            // This provides a single source of truth for guessing behavior
            val isGuessing = GuessingDetector.detectGuessing(responsePatterns)

            // Create performance data for the calculator
            val performanceData =
                StarRatingCalculator.PerformanceData(
                    totalWords = levelWords.size,
                    correctAnswers = correctAnswersInLevel,
                    hintsUsed = totalHintsUsedInLevel,
                    totalTimeMs = totalTimeMs,
                    wrongAnswers = totalWrongAnswersInLevel,
                    maxCombo = maxComboInLevel,
                    isGuessing = isGuessing,
                )

            return StarRatingCalculator.calculateStars(performanceData)
        }

        /**
         * Calculate level score based on stars earned
         * Each star = 10 points, max 60 points for 6 words (all 3 stars)
         */
        private fun calculateLevelScore(): Int {
            val totalStars = starsEarnedInLevel.sum()
            return totalStars * 10
        }

        /**
         * Update island mastery progress after completing a level
         * Calculates mastered words, completed levels, and updates IslandMastery record
         */
        private suspend fun updateIslandMastery() {
            try {
                // Get all word progress for this user
                val allProgress = progressRepository.getAllWordProgress(userId)

                // Filter words belonging to current island (look_island)
                val islandWords = allProgress.filter { it.wordId.startsWith("look_") }

                // Count mastered words (correctAttempts >= 1 means word is mastered)
                val masteredWords = islandWords.count { it.correctAttempts >= 1 }

                // Get completed levels from database (count unlocked levels)
                val completedLevels = 1 // Current level just completed

                // Calculate cross-scene score (simplified: based on average stars)
                val crossSceneScore =
                    if (starsEarnedInLevel.isNotEmpty()) {
                        starsEarnedInLevel.average().toDouble() / 3.0 // Normalize to 0-1
                    } else {
                        0.0
                    }

                // Update mastery in repository
                islandMasteryRepository.calculateMastery(
                    userId = userId,
                    islandId = currentIslandId,
                    masteredWords = masteredWords,
                    completedLevels = completedLevels,
                    crossSceneScore = crossSceneScore,
                )
            } catch (e: Exception) {
                // Log error but don't crash level completion
                e.printStackTrace()
            }
        }

        /**
         * Generate question from word
         * Creates a SpellBattleQuestion for the word
         */
        private fun generateQuestion(word: Word): SpellBattleQuestion {
            return SpellBattleQuestion(
                wordId = word.id,
                translation = word.translation,
                targetWord = word.word,
                hint = word.pronunciation, // Use pronunciation as hint
                difficulty = word.difficulty,
            )
        }

        /**
         * Map level IDs to region IDs for world map exploration
         * Used to track which regions have been explored by the player
         */
        private fun getRegionIdForLevel(levelId: String): String {
            return when {
                levelId.startsWith("look_") -> "look_peninsula"
                levelId.startsWith("make_") -> "make_lake"
                else -> "unknown_region"
            }
        }

        /**
         * Show star breakdown screen
         * Displays detailed performance breakdown after level completion
         */
        fun showStarBreakdown() {
            _showStarBreakdown.value = true
        }

        /**
         * Hide star breakdown screen
         * Returns to level complete screen
         */
        fun hideStarBreakdown() {
            _showStarBreakdown.value = false
        }
    }
