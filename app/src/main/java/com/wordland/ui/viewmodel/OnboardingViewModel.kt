package com.wordland.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.domain.model.OnboardingPhase
import com.wordland.domain.model.OnboardingState
import com.wordland.domain.model.OnboardingUiState
import com.wordland.domain.model.PetType
import com.wordland.domain.model.TutorialQuestion
import com.wordland.domain.model.TutorialWordConfig
import com.wordland.domain.usecase.usecases.CompleteOnboardingUseCase
import com.wordland.domain.usecase.usecases.CompleteTutorialWordUseCase
import com.wordland.domain.usecase.usecases.GetTutorialWordsUseCase
import com.wordland.domain.usecase.usecases.OpenFirstChestUseCase
import com.wordland.domain.usecase.usecases.SelectPetUseCase
import com.wordland.domain.usecase.usecases.StartOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Onboarding ViewModel
 *
 * Manages the Onboarding flow state and business logic
 *
 * @property startOnboardingUseCase UseCase to start onboarding
 * @property selectPetUseCase UseCase to select pet
 * @property getTutorialWordsUseCase UseCase to get tutorial words
 * @property completeTutorialWordUseCase UseCase to complete a word
 * @property openFirstChestUseCase UseCase to open first chest
 * @property completeOnboardingUseCase UseCase to complete onboarding
 */
class OnboardingViewModel(
    private val startOnboardingUseCase: StartOnboardingUseCase,
    private val selectPetUseCase: SelectPetUseCase,
    private val getTutorialWordsUseCase: GetTutorialWordsUseCase,
    private val completeTutorialWordUseCase: CompleteTutorialWordUseCase,
    private val openFirstChestUseCase: OpenFirstChestUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _onboardingState = MutableStateFlow<OnboardingState?>(null)
    val onboardingState: StateFlow<OnboardingState?> = _onboardingState.asStateFlow()

    private val tutorialWords = mutableListOf<TutorialWordConfig>()

    /**
     * Start the onboarding flow
     */
    fun startOnboarding() {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            try {
                val state = startOnboardingUseCase()
                _onboardingState.value = state

                when (state.currentPhase) {
                    OnboardingPhase.NOT_STARTED,
                    OnboardingPhase.WELCOME,
                    -> {
                        _uiState.value = OnboardingUiState.Welcome()
                    }
                    OnboardingPhase.PET_SELECTION -> {
                        _uiState.value = OnboardingUiState.PetSelection()
                    }
                    OnboardingPhase.TUTORIAL -> {
                        loadTutorialWords()
                    }
                    OnboardingPhase.FIRST_CHEST -> {
                        // Need to open chest, not complete yet
                        openChest()
                    }
                    OnboardingPhase.COMPLETED -> {
                        _uiState.value =
                            OnboardingUiState.Completed(
                                pet = state.selectedPet ?: PetType.DOLPHIN,
                                wordsLearned = state.completedTutorialWords,
                                stars = state.totalStars,
                            )
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "启动失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Select a pet by String ID (from UI)
     * Maps String ID to PetType enum
     */
    fun selectPet(petId: String) {
        Log.d("OnboardingViewModel", "selectPet called with petId: $petId")
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Converting petId to PetType")
                val petType = petIdToPetType(petId)
                Log.d("OnboardingViewModel", "Calling selectPetUseCase with petType: $petType")
                val state = selectPetUseCase(petType)
                Log.d("OnboardingViewModel", "SelectPetUseCase returned state: $state")
                _onboardingState.value = state
                Log.d("OnboardingViewModel", "Loading tutorial words")
                loadTutorialWords()
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Error selecting pet", e)
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "选择宠物失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Select a pet by PetType enum
     */
    fun selectPetByType(pet: PetType) {
        viewModelScope.launch {
            try {
                val state = selectPetUseCase(pet)
                _onboardingState.value = state
                loadTutorialWords()
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "选择宠物失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Load tutorial words
     */
    private fun loadTutorialWords() {
        viewModelScope.launch {
            try {
                _uiState.value = OnboardingUiState.Loading
                val words = getTutorialWordsUseCase()
                tutorialWords.clear()
                tutorialWords.addAll(words)
                showCurrentWord()
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "加载单词失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Show current tutorial word
     */
    private fun showCurrentWord() {
        val state = _onboardingState.value ?: return

        Log.d(
            "OnboardingViewModel",
            "showCurrentWord called, completedWords: ${state.completedTutorialWords}, phase: ${state.currentPhase}",
        )

        // Check if user has already completed 5 words
        if (state.completedTutorialWords >= CompleteTutorialWordUseCase.REQUIRED_WORDS_FOR_CHEST) {
            // Already completed required words, go to chest
            Log.d("OnboardingViewModel", "Already completed 5 words, opening chest")
            openChest()
            return
        }

        val currentWordIndex = state.completedTutorialWords.coerceAtMost(tutorialWords.size - 1)
        val currentWord = tutorialWords.getOrNull(currentWordIndex)

        Log.d("OnboardingViewModel", "Current word index: $currentWordIndex, tutorialWords size: ${tutorialWords.size}")

        if (currentWord != null) {
            val question =
                TutorialQuestion(
                    word = currentWord.word,
                    translation = currentWord.translation,
                    preFilledLetters = currentWord.generatePreFilledIndices(),
                    hintsRemaining = currentWord.hintsAllowed,
                )

            Log.d("OnboardingViewModel", "Setting uiState to Tutorial with word: ${currentWord.word}")
            _uiState.value =
                OnboardingUiState.Tutorial(
                    currentWordIndex = currentWordIndex,
                    totalWords = tutorialWords.size,
                    question = question,
                    progress = currentWordIndex.toFloat() / tutorialWords.size,
                )
        } else {
            // All words completed, go to chest
            Log.d("OnboardingViewModel", "No current word, opening chest")
            openChest()
        }
    }

    /**
     * Update current answer
     */
    fun updateAnswer(answer: String) {
        val currentState = _uiState.value
        if (currentState is OnboardingUiState.Tutorial) {
            val updatedQuestion =
                currentState.question.copy(
                    currentAnswer = answer,
                )
            _uiState.value =
                currentState.copy(
                    question = updatedQuestion,
                )
        }
    }

    /**
     * Submit answer for current word
     */
    fun submitAnswer(answer: String) {
        viewModelScope.launch {
            try {
                val state = _onboardingState.value ?: return@launch

                // Ensure tutorial words are loaded
                if (tutorialWords.isEmpty()) {
                    val words = getTutorialWordsUseCase()
                    tutorialWords.clear()
                    tutorialWords.addAll(words)
                }

                val currentWord =
                    tutorialWords.getOrNull(state.completedTutorialWords)
                        ?: return@launch

                val isCorrect = currentWord.isCorrect(answer)
                val stars = if (isCorrect) 3 else 1

                val updatedState = completeTutorialWordUseCase(stars)
                _onboardingState.value = updatedState

                when {
                    updatedState.completedTutorialWords >= CompleteTutorialWordUseCase.REQUIRED_WORDS_FOR_CHEST &&
                        updatedState.currentPhase == OnboardingPhase.FIRST_CHEST -> {
                        openChest()
                    }
                    else -> {
                        showCurrentWord()
                    }
                }
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "提交答案失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Skip current word
     */
    fun skipWord() {
        viewModelScope.launch {
            try {
                // Ensure tutorial words are loaded
                if (tutorialWords.isEmpty()) {
                    val words = getTutorialWordsUseCase()
                    tutorialWords.clear()
                    tutorialWords.addAll(words)
                }

                val updatedState = completeTutorialWordUseCase(0)
                _onboardingState.value = updatedState

                if (updatedState.completedTutorialWords >= CompleteTutorialWordUseCase.REQUIRED_WORDS_FOR_CHEST) {
                    openChest()
                } else {
                    showCurrentWord()
                }
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "跳过失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Open the first chest
     */
    fun openChest() {
        viewModelScope.launch {
            try {
                _uiState.value = OnboardingUiState.Loading
                val reward = openFirstChestUseCase()
                _uiState.value = OnboardingUiState.OpeningChest(reward)
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "开宝箱失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Dismiss chest and complete onboarding
     */
    fun dismissChest() {
        viewModelScope.launch {
            try {
                // Mark onboarding as completed
                completeOnboardingUseCase()

                val state = _onboardingState.value
                _uiState.value =
                    OnboardingUiState.Completed(
                        pet = state?.selectedPet ?: PetType.DOLPHIN,
                        wordsLearned = state?.completedTutorialWords ?: 0,
                        stars = state?.totalStars ?: 0,
                    )
            } catch (e: Exception) {
                _uiState.value =
                    OnboardingUiState.Error(
                        message = e.message ?: "完成失败",
                        recoverable = true,
                    )
            }
        }
    }

    /**
     * Map pet ID string to PetType enum
     */
    private fun petIdToPetType(petId: String): PetType {
        return when (petId) {
            "pet_dolphin" -> PetType.DOLPHIN
            "pet_cat" -> PetType.CAT
            "pet_dog" -> PetType.DOG
            "pet_fox" -> PetType.FOX
            else -> PetType.DOLPHIN
        }
    }

    /**
     * Get pet ID string from PetType enum
     */
    fun petTypeToId(pet: PetType): String {
        return when (pet) {
            PetType.DOLPHIN -> "pet_dolphin"
            PetType.CAT -> "pet_cat"
            PetType.DOG -> "pet_dog"
            PetType.FOX -> "pet_fox"
        }
    }

    companion object {
        const val REQUIRED_WORDS_COUNT = 5
    }
}
