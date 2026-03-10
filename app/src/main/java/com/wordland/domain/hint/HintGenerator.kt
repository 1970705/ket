package com.wordland.domain.hint

/**
 * Generates hints for words based on hint level
 * Provides progressive hints to help learners without giving away the answer
 */
class HintGenerator {
    /**
     * Generate hint for a word at specified level
     * @param word The target word
     * @param level The hint level (1-3)
     * @return The generated hint string
     */
    fun generateHint(
        word: String,
        level: Int,
    ): String {
        return when (level) {
            1 -> generateLevel1Hint(word)
            2 -> generateLevel2Hint(word)
            3 -> generateLevel3Hint(word)
            else -> ""
        }
    }

    /**
     * Level 1: Show first letter
     * Gives the user a starting point without revealing too much
     */
    private fun generateLevel1Hint(word: String): String {
        if (word.isEmpty()) return ""
        return "首字母: ${word.first().uppercaseChar()}"
    }

    /**
     * Level 2: Show first half of the word
     * Reveals more information but still requires effort
     * For odd-length words, rounds up to reveal more
     */
    private fun generateLevel2Hint(word: String): String {
        if (word.isEmpty()) return ""

        val revealCount = (word.length + 1) / 2
        val revealedPart = word.take(revealCount)
        val maskedLength = word.length - revealCount
        val maskedPart =
            if (maskedLength > 0) {
                "_".repeat(maskedLength)
            } else {
                ""
            }

        return "前半部分: $revealedPart$maskedPart"
    }

    /**
     * Level 3: Show full word with vowels masked
     * Reveals most information but still requires recalling exact spelling
     * Vowels are masked as they are easier to guess
     */
    private fun generateLevel3Hint(word: String): String {
        if (word.isEmpty()) return ""

        val vowels = setOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')

        val maskedWord =
            word.map { char ->
                if (char in vowels) '_' else char
            }.joinToString("")

        return "完整单词（元音隐藏）: $maskedWord"
    }

    /**
     * Generate hint based on word length
     * Optimizes hint strategy for different word lengths
     */
    fun generateAdaptiveHint(
        word: String,
        hintCount: Int,
    ): String {
        return when {
            word.length <= 3 -> {
                // Short words: jump to level 2 or 3
                if (hintCount == 1) {
                    generateLevel2Hint(word)
                } else {
                    generateLevel3Hint(word)
                }
            }
            word.length <= 6 -> {
                // Medium words: normal progression
                generateHint(word, hintCount.coerceIn(1, 3))
            }
            else -> {
                // Long words: add intermediate level
                when (hintCount) {
                    1 -> generateLevel1Hint(word)
                    2 -> {
                        // Reveal 40% instead of 50%
                        val revealCount = (word.length * 0.4).toInt()
                        val revealedPart = word.take(revealCount)
                        val maskedPart = "_".repeat(word.length - revealCount)
                        "部分提示: $revealedPart$maskedPart"
                    }
                    else -> generateLevel3Hint(word)
                }
            }
        }
    }

    /**
     * Generate hint with custom reveal percentage
     * @param word The target word
     * @param percentage How much to reveal (0.0 to 1.0)
     * @return The hint with masked portions
     */
    fun generatePartialHint(
        word: String,
        percentage: Float,
    ): String {
        if (word.isEmpty() || percentage <= 0f) return ""

        val revealCount = (word.length * percentage.coerceIn(0f, 1f)).toInt()
        val revealedPart = word.take(revealCount)
        val maskedPart = "_".repeat(word.length - revealCount)

        return "提示: $revealedPart$maskedPart"
    }

    /**
     * Generate hint that shows letter positions
     * Useful for spelling practice
     */
    fun generateLetterCountHint(word: String): String {
        return "字母数量: ${word.length}"
    }

    /**
     * Generate hint for specific letter positions
     * @param word The target word
     * @param positions List of 0-based positions to reveal
     * @return The hint with selected letters revealed
     */
    fun generatePositionHint(
        word: String,
        positions: List<Int>,
    ): String {
        if (word.isEmpty()) return ""

        val hintArray = word.map { '_' }.toMutableList()
        positions.forEach { pos ->
            if (pos in word.indices) {
                hintArray[pos] = word[pos]
            }
        }

        return "位置提示: ${hintArray.joinToString(" ")}"
    }
}
