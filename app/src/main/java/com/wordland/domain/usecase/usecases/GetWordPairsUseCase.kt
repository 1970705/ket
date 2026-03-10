package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchGameConfig
import com.wordland.domain.model.Result
import com.wordland.domain.model.Word
import javax.inject.Inject

class GetWordPairsUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
    ) {
        suspend operator fun invoke(config: MatchGameConfig): Result<List<BubbleState>> {
            return try {
                val words = getWords(config)

                if (words.size < config.wordPairs) {
                    return Result.Error(
                        IllegalStateException(
                            "Not enough words. Required: ${config.wordPairs}, Available: ${words.size}",
                        ),
                    )
                }

                val selectedWords = words.shuffled().take(config.wordPairs)
                val bubbles = selectedWords.flatMap { createBubblePair(it) }
                val shuffledBubbles = bubbles.shuffled()

                Result.Success(shuffledBubbles)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        private suspend fun getWords(config: MatchGameConfig): List<Word> {
            return when {
                config.levelId != null -> {
                    wordRepository.getWordsByLevel(config.levelId)
                }
                config.islandId != null -> {
                    wordRepository.getWordsByIsland(config.islandId)
                }
                else -> {
                    wordRepository.getRandomKETWords(Int.MAX_VALUE)
                }
            }
        }

        private fun createBubblePair(word: Word): List<BubbleState> {
            val pairId = "pair_${word.id}"

            return listOf(
                BubbleState(
                    id = "${pairId}_en",
                    word = word.word,
                    pairId = pairId,
                    color = BubbleColor.random(),
                ),
                BubbleState(
                    id = "${pairId}_zh",
                    word = word.translation,
                    pairId = pairId,
                    color = BubbleColor.random(),
                ),
            )
        }
    }
