package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Spell Battle Game Question
 * User sees a word's translation and must spell the English word
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class SpellBattleQuestion(
    val wordId: String,
    val translation: String, // Chinese translation shown to user
    val targetWord: String, // The English word to spell
    val hint: String?, // Optional hint (first letter)
    val difficulty: Int = 1, // 1-5
) {
    /**
     * Check if user's answer is correct
     */
    fun isCorrect(userAnswer: String): Boolean {
        return userAnswer.trim().equals(targetWord, ignoreCase = true)
    }

    /**
     * Get hint (first letter)
     */
    fun getHintLetter(): String? {
        return if (targetWord.isNotEmpty()) {
            targetWord.first().uppercaseChar().toString()
        } else {
            null
        }
    }

    /**
     * Compare answer and return mismatched positions
     * @return List of indices where characters don't match
     */
    fun getWrongPositions(userAnswer: String): List<Int> {
        val wrongPositions = mutableListOf<Int>()
        val user = userAnswer.trim().lowercase()
        val target = targetWord.lowercase()

        for (i in user.indices) {
            if (i >= target.length || user[i] != target[i]) {
                wrongPositions.add(i)
            }
        }

        // If answer is shorter, remaining correct positions are also wrong
        if (user.length < target.length) {
            for (i in user.length until target.length) {
                wrongPositions.add(i)
            }
        }

        return wrongPositions
    }

    /**
     * Get answer progress for UI display
     * @return Pair of (correctCount, totalLength)
     */
    fun getAnswerProgress(userAnswer: String): Pair<Int, Int> {
        val user = userAnswer.trim()
        var correctCount = 0

        for (i in user.indices) {
            if (i < targetWord.length && user[i].equals(targetWord[i], ignoreCase = true)) {
                correctCount++
            }
        }

        return Pair(correctCount, targetWord.length)
    }
}
