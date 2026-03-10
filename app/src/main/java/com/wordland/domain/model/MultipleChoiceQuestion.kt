package com.wordland.domain.model

/**
 * Multiple choice question for onboarding
 * Designed for 100% success rate to build confidence
 *
 * @property wordId The word ID being tested
 * @property translation Chinese translation (question)
 * @property targetWord Correct English answer
 * @property options List of 4 options (1 correct, 3 distractors)
 * @property difficulty Word difficulty (1-5)
 * @property emoji Optional emoji for visual appeal
 */
data class MultipleChoiceQuestion(
    val wordId: String,
    val translation: String,
    val targetWord: String,
    val options: List<String>,
    val difficulty: Int = 1,
    val emoji: String? = null,
) {
    init {
        require(options.size == 4) { "Multiple choice must have exactly 4 options" }
        require(options.contains(targetWord)) { "Options must contain the target word" }
    }

    /**
     * Check if user answer is correct
     */
    fun isCorrect(userAnswer: String): Boolean {
        return userAnswer.equals(targetWord, ignoreCase = true)
    }

    /**
     * Get the correct answer index (0-3)
     */
    fun getCorrectIndex(): Int {
        return options.indexOf(targetWord)
    }

    /**
     * Get answer progress feedback
     * @return Pair of (correct, selectedIndex)
     */
    fun getAnswerFeedback(userAnswer: String): AnswerFeedback {
        val selectedIndex =
            options.indexOfFirst {
                it.equals(userAnswer, ignoreCase = true)
            }

        return AnswerFeedback(
            isCorrect = isCorrect(userAnswer),
            correctIndex = getCorrectIndex(),
            selectedIndex = if (selectedIndex >= 0) selectedIndex else null,
            correctAnswer = targetWord,
        )
    }
}

/**
 * Feedback for multiple choice answer
 */
data class AnswerFeedback(
    val isCorrect: Boolean,
    val correctIndex: Int,
    val selectedIndex: Int?,
    val correctAnswer: String,
)
