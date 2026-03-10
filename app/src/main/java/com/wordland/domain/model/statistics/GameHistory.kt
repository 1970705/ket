package com.wordland.domain.model.statistics

/**
 * Domain model for a single game history record
 * Contains detailed statistics from one completed game session
 *
 * @property gameId Unique identifier (UUID)
 * @property userId User who played the game
 * @property levelId Level identifier (e.g., "look_island_level_01")
 * @property islandId Island identifier (e.g., "look_island")
 * @property gameMode Game mode played
 * @property startTime Game start timestamp (ms)
 * @property endTime Game end timestamp (ms)
 * @property duration Game duration (ms)
 * @property score Total score earned
 * @property stars Star rating (0-3)
 * @property totalQuestions Total questions in the game
 * @property correctAnswers Number of correct answers
 * @property accuracy Accuracy rate (0.0-1.0)
 * @property maxCombo Maximum combo achieved
 * @property hintsUsed Number of hints used
 * @property wrongAnswers Number of wrong answers
 * @property avgResponseTime Average response time (ms)
 * @property fastestAnswer Fastest answer time (ms), null if none
 * @property slowestAnswer Slowest answer time (ms), null if none
 * @property difficulty Difficulty setting ("easy", "normal", "hard")
 * @property createdAt Record creation timestamp (ms)
 */
data class GameHistory(
    val gameId: String,
    val userId: String,
    val levelId: String,
    val islandId: String,
    val gameMode: GameMode,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val score: Int,
    val stars: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val maxCombo: Int,
    val hintsUsed: Int,
    val wrongAnswers: Int,
    val avgResponseTime: Long,
    val fastestAnswer: Long?,
    val slowestAnswer: Long?,
    val difficulty: String,
    val createdAt: Long,
) {
    /**
     * Check if this was a perfect game (3 stars)
     */
    val isPerfect: Boolean
        get() = stars == 3

    /**
     * Check if this was a completed game (not abandoned)
     */
    val isCompleted: Boolean
        get() = totalQuestions > 0 && (correctAnswers + wrongAnswers) >= totalQuestions

    /**
     * Get completion percentage
     */
    val completionPercentage: Int
        get() =
            if (totalQuestions > 0) {
                ((correctAnswers + wrongAnswers) * 100 / totalQuestions)
            } else {
                0
            }
}

/**
 * Enum representing different game modes
 */
enum class GameMode {
    /** Spell Battle - spell the word from translation */
    SPELL_BATTLE,

    /** Quick Judge - judge if translation is correct */
    QUICK_JUDGE,

    /** Listen Find - listen and find the word */
    LISTEN_FIND,

    /** Sentence Match - match words to context */
    SENTENCE_MATCH,
    ;

    /**
     * Get display name for the game mode
     */
    fun getDisplayName(): String =
        when (this) {
            SPELL_BATTLE -> "拼写对战"
            QUICK_JUDGE -> "快速判断"
            LISTEN_FIND -> "听力寻词"
            SENTENCE_MATCH -> "句子配对"
        }

    /**
     * Get icon resource name for the game mode
     */
    fun getIconName(): String =
        when (this) {
            SPELL_BATTLE -> "ic_spell_battle"
            QUICK_JUDGE -> "ic_quick_judge"
            LISTEN_FIND -> "ic_listen_find"
            SENTENCE_MATCH -> "ic_sentence_match"
        }
}
