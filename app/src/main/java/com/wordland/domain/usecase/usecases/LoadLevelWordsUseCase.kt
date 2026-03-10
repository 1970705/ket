package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import javax.inject.Inject

/**
 * UseCase for loading all words in a level
 * Returns words sorted by learning order
 */
class LoadLevelWordsUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
    ) {
        /**
         * Load words for a specific level
         * @param levelId The level ID to load words for
         * @return Result containing list of words sorted by order, or error if none found
         */
        suspend operator fun invoke(levelId: String): Result<List<Word>> {
            return try {
                val words = wordRepository.getWordsByLevel(levelId)
                if (words.isEmpty()) {
                    Result.Error(NoSuchElementException("No words found for level: $levelId"))
                } else {
                    Result.Success(words.sortedBy { it.order })
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
