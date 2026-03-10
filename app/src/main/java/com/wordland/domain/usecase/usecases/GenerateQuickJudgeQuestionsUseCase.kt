package com.wordland.domain.usecase.usecases

import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.QuickJudgeQuestion
import com.wordland.domain.model.Result
import javax.inject.Inject

/**
 * UseCase for generating Quick Judge questions
 *
 * Quick Judge shows a word with a translation - the user must decide if it's correct.
 * This use case generates both correct translations and distractors (wrong translations).
 */
class GenerateQuickJudgeQuestionsUseCase
    @Inject
    constructor(
        private val wordRepository: WordRepository,
    ) {
        companion object {
            // Ratio of correct to incorrect questions (60% correct to build confidence)
            private const val CORRECT_RATIO = 0.6f

            // Minimum and maximum questions per level
            private const val MIN_QUESTIONS = 4
            private const val MAX_QUESTIONS = 8
        }

        /**
         * Generate Quick Judge questions for a level
         * @param levelId Level ID
         * @param count Number of questions to generate (default: based on level word count)
         * @param timeLimit Time limit per question in seconds
         * @return Result containing list of QuickJudgeQuestion
         */
        suspend operator fun invoke(
            levelId: String,
            count: Int? = null,
            timeLimit: Int = 5,
        ): Result<List<QuickJudgeQuestion>> {
            return try {
                // 1. Get all words for the level
                val wordsResult = wordRepository.getWordsByLevel(levelId)
                if (wordsResult.isEmpty()) {
                    return Result.Error(IllegalStateException("No words found for level: $levelId"))
                }

                // 2. Determine question count
                val questionCount =
                    count?.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
                        ?: wordsResult.size.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)

                // 3. Calculate how many should be correct
                val correctCount = (questionCount * CORRECT_RATIO).toInt().coerceAtLeast(1)
                val incorrectCount = questionCount - correctCount

                // 4. Shuffle words for randomness
                val shuffledWords = wordsResult.shuffled()

                // 5. Generate questions
                val questions = mutableListOf<QuickJudgeQuestion>()

                // Add correct questions (word with its actual translation)
                shuffledWords.take(correctCount).forEach { word ->
                    questions.add(
                        QuickJudgeQuestion(
                            wordId = word.id,
                            word = word.word,
                            translation = word.translation,
                            isCorrect = true,
                            timeLimit = timeLimit,
                            difficulty = word.difficulty,
                        ),
                    )
                }

                // Add incorrect questions (word with wrong translation)
                val remainingWords = shuffledWords.drop(correctCount)
                repeat(incorrectCount) { index ->
                    if (remainingWords.size < 2) return@repeat

                    // Get the word for this question
                    val targetWord = remainingWords[index % remainingWords.size]

                    // Find a wrong translation (from a different word)
                    val wrongTranslation = findWrongTranslation(targetWord, remainingWords)

                    questions.add(
                        QuickJudgeQuestion(
                            wordId = targetWord.id,
                            word = targetWord.word,
                            translation = wrongTranslation,
                            isCorrect = false,
                            timeLimit = timeLimit,
                            difficulty = targetWord.difficulty,
                        ),
                    )
                }

                // 6. Shuffle final question order
                Result.Success(questions.shuffled())
            } catch (e: Exception) {
                Result.Error(e)
            }
        }

        /**
         * Find a wrong translation for a word from other words in the list
         * @param targetWord The word to find a wrong translation for
         * @param allWords List of words to choose from
         * @return A wrong translation (from a different word)
         */
        private fun findWrongTranslation(
            targetWord: com.wordland.domain.model.Word,
            allWords: List<com.wordland.domain.model.Word>,
        ): String {
            // Filter out the target word itself
            val otherWords = allWords.filter { it.id != targetWord.id }

            // Return a translation from another word, or a generic wrong translation
            return otherWords.randomOrNull()?.translation ?: "其他意思"
        }

        /**
         * Generate a single Quick Judge question for practice mode
         * @param wordId The word to generate a question for
         * @param forceCorrect If true, always use correct translation
         * @return Result containing a QuickJudgeQuestion
         */
        suspend fun generateSingleQuestion(
            wordId: String,
            forceCorrect: Boolean = false,
            timeLimit: Int = 5,
        ): Result<QuickJudgeQuestion> {
            return try {
                val word =
                    wordRepository.getWordById(wordId)
                        ?: return Result.Error(IllegalStateException("Word not found: $wordId"))

                // Get all words from the same level for distractors
                val wordLevelId = word.levelId ?: "look_island_level_01"
                val levelWords = wordRepository.getWordsByLevel(wordLevelId)

                val translation =
                    if (forceCorrect) {
                        word.translation
                    } else {
                        // 70% chance of correct translation for practice
                        if (listOf(true, false, true, true, true, true, true).random()) {
                            word.translation
                        } else {
                            findWrongTranslation(word, levelWords)
                        }
                    }

                Result.Success(
                    QuickJudgeQuestion(
                        wordId = word.id,
                        word = word.word,
                        translation = translation,
                        isCorrect = translation == word.translation,
                        timeLimit = timeLimit,
                        difficulty = word.difficulty,
                    ),
                )
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
