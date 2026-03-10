package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import javax.inject.Inject

/**
 * UseCase for getting the next word to learn
 * Prioritizes:
 * 1. Words not yet learned
 * 2. Words due for review (nextReviewTime <= now)
 * 3. Words with lowest memory strength
 */
class GetNextWordUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
        private val progressRepository: ProgressRepository,
    ) {
        /**
         * Get the next word to learn
         * @param userId User ID
         * @param levelId Level ID
         * @param currentWordId Current word ID (to skip)
         * @return Result containing next word or null if no more words
         */
        suspend operator fun invoke(
            userId: String,
            levelId: String,
            currentWordId: String? = null,
        ): Result<Word?> {
            return try {
                // 1. Get all words in the level
                val allWords = wordRepository.getWordsByLevel(levelId)

                if (allWords.isEmpty()) {
                    return Result.Error(NoSuchElementException("No words found for level: $levelId"))
                }

                // 2. Filter out current word
                val availableWords = allWords.filter { it.id != currentWordId }

                if (availableWords.isEmpty()) {
                    return Result.Success(null) // No more words
                }

                // 3. Get progress and determine priority
                val now = System.currentTimeMillis()
                var nextWord: Word? = null
                var highestPriority = -1

                for (word in availableWords) {
                    val progress = progressRepository.getWordProgress(userId, word.id)
                    var priority = 0

                    when {
                        // Priority 1: Never learned words (highest)
                        progress == null -> {
                            priority = 100
                        }
                        // Priority 2: Due for review
                        progress.nextReviewTime != null &&
                            progress.nextReviewTime!! <= now -> {
                            priority = 80
                        }
                        // Priority 3: Low memory strength
                        else -> {
                            priority = 100 - progress.memoryStrength
                        }
                    }

                    if (priority > highestPriority) {
                        highestPriority = priority
                        nextWord = word
                    }
                }

                Result.Success(nextWord)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
