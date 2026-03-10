package com.wordland.domain.model

/**
 * Domain model for Fill Blank game mode questions.
 *
 * In Fill Blank mode, users see the translation and must spell the word
 * by selecting letters from a scrambled letter pool.
 *
 * @property wordId Unique identifier for the word
 * @property translation Chinese translation to display to user
 * @property targetWord The correct English word the user must spell
 * @property firstLetterHint First letter of targetWord as a hint
 * @property letterPool Scrambled letters including all letters from targetWord
 * @property difficulty Level of difficulty (1-5, default 1)
 * @property emoji Optional emoji for visual engagement
 */
data class FillBlankQuestion(
    val wordId: String,
    val translation: String,
    val targetWord: String,
    val firstLetterHint: String,
    val letterPool: List<Char>,
    val difficulty: Int = 1,
    val emoji: String? = null,
) {
    /**
     * Checks if the user's answer matches the target word.
     * Comparison is case-insensitive.
     *
     * @param userAnswer The answer provided by the user
     * @return true if the answer matches, false otherwise
     */
    fun isCorrect(userAnswer: String): Boolean {
        return userAnswer.equals(targetWord, ignoreCase = true)
    }

    /**
     * Checks if a given letter is correct at a specific position.
     * Comparison is case-insensitive.
     *
     * @param letter The letter to check
     * @param position The position in the word (0-indexed)
     * @return true if the letter matches at that position, false otherwise
     */
    fun isLetterCorrect(
        letter: Char,
        position: Int,
    ): Boolean {
        if (position < 0 || position >= targetWord.length) {
            return false
        }
        return letter.equals(targetWord[position], ignoreCase = true)
    }

    /**
     * Calculates the user's progress on the answer.
     *
     * @param userAnswer The current answer provided by the user
     * @return A Pair where first is the count of correct letters in correct positions,
     *         and second is the total length of the target word
     */
    fun getAnswerProgress(userAnswer: String): Pair<Int, Int> {
        val correctCount =
            userAnswer.indices.count { index ->
                index < targetWord.length &&
                    userAnswer[index].equals(targetWord[index], ignoreCase = true)
            }
        return Pair(correctCount, targetWord.length)
    }

    /**
     * Returns the first letter hint for the target word.
     *
     * @return A string containing the first letter hint
     */
    fun getHint(): String {
        return "首字母: $firstLetterHint"
    }

    /**
     * Validates that this question is properly configured.
     * Useful for debugging and data validation.
     *
     * @return true if the question is valid, false otherwise
     */
    fun isValid(): Boolean {
        return wordId.isNotEmpty() &&
            translation.isNotEmpty() &&
            targetWord.isNotEmpty() &&
            firstLetterHint.isNotEmpty() &&
            letterPool.isNotEmpty() &&
            difficulty in 1..5 &&
            hasAllRequiredLetters()
    }

    /**
     * Checks if the letter pool contains all required letters to spell the target word.
     *
     * @return true if all letters from targetWord are present in letterPool (case-insensitive)
     */
    private fun hasAllRequiredLetters(): Boolean {
        val targetLetters = targetWord.lowercase().toList()
        val poolLetters = letterPool.map { it.lowercaseChar() }

        return targetLetters.all { targetLetter ->
            poolLetters.count { it == targetLetter } >=
                targetLetters.count { it == targetLetter }
        }
    }
}

/**
 * Represents a single letter slot in the Fill Blank answer area.
 *
 * @property letter The letter in this slot (null if empty)
 * @property isCorrect Whether the letter is correct (null if not yet checked)
 * @property position The position of this slot in the answer
 */
data class LetterSlot(
    val letter: Char?,
    val isCorrect: Boolean?,
    val position: Int,
) {
    /**
     * Checks if this slot is empty.
     */
    fun isEmpty(): Boolean = letter == null

    /**
     * Checks if this slot has been filled and checked.
     */
    fun isFilledAndChecked(): Boolean = letter != null && isCorrect != null

    companion object {
        /**
         * Creates a list of empty LetterSlots for a given word length.
         */
        fun createEmptySlots(count: Int): List<LetterSlot> {
            return List(count) { index ->
                LetterSlot(null, null, index)
            }
        }
    }
}
