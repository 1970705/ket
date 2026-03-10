package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.Result
import com.wordland.domain.model.ReviewWordItem
import javax.inject.Inject

/**
 * UseCase for getting words that need review
 * Words need review if:
 * 1. nextReviewTime <= now (due for review)
 * 2. memoryStrength < 60 (not well learned)
 */
class GetReviewWordsUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
        private val progressRepository: ProgressRepository,
    ) {
        /**
         * Get words that need review
         * @param userId User ID
         * @param limit Maximum number of words to return
         * @return Result containing list of review words
         */
        suspend operator fun invoke(
            userId: String,
            limit: Int = 20,
        ): Result<List<ReviewWordItem>> {
            return try {
                val now = System.currentTimeMillis()

                // 1. Get words due for review
                val dueForReview =
                    progressRepository.getWordsDueForReview(
                        userId = userId,
                        currentTime = now,
                        limit = limit,
                    )

                // 2. Get words with low memory strength
                val lowStrengthWords =
                    progressRepository.getAllWordProgress(userId)
                        .filter { it.memoryStrength < 60 && it.memoryStrength > 0 }
                        .take(limit)

                // 3. Combine and deduplicate
                val combinedProgress =
                    (dueForReview + lowStrengthWords)
                        .distinctBy { it.wordId }
                        .take(limit)

                if (combinedProgress.isEmpty()) {
                    return Result.Success(emptyList())
                }

                // 4. Get word details
                val reviewWords =
                    combinedProgress.map { progress ->
                        val word = wordRepository.getWordById(progress.wordId)!!

                        // Calculate correct rate
                        val correctRate =
                            if (progress.totalAttempts > 0) {
                                progress.correctAttempts.toFloat() / progress.totalAttempts
                            } else {
                                0f
                            }

                        ReviewWordItem(
                            word = word,
                            memoryStrength = progress.memoryStrength,
                            lastReviewTime = progress.lastReviewTime,
                            correctRate = correctRate,
                        )
                    }

                // 5. Sort by memory strength (lowest first) and correct rate
                Result.Success(
                    reviewWords.sortedWith(
                        compareBy({ it.memoryStrength }, { -it.correctRate }),
                    ),
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
