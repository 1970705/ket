package com.wordland.media

/**
 * Sound effect types for game events
 *
 * Each effect corresponds to an audio file in assets/audio/sfx/
 */
enum class SoundEffect(val fileName: String) {
    /** Correct answer feedback */
    CORRECT("correct.mp3"),

    /** Wrong answer feedback */
    WRONG("wrong.mp3"),

    /** Combo milestone reached */
    COMBO("combo.mp3"),

    /** 3-combo milestone */
    COMBO_3("combo.mp3"),

    /** 5-combo milestone */
    COMBO_5("combo.mp3"),

    /** 10-combo milestone */
    COMBO_10("combo.mp3"),

    /** Level completion celebration */
    LEVEL_COMPLETE("level_complete.mp3"),

    /** Button click feedback */
    BUTTON_CLICK("button_click.mp3"),

    /** Star earned */
    STAR_EARNED("star_earned.mp3"),

    /** Achievement unlocked */
    ACHIEVEMENT_UNLOCK("achievement_unlock.mp3"),

    /** Island unlocked */
    ISLAND_UNLOCK("island_unlock.mp3"),

    /** Chest open */
    CHEST_OPEN("chest_open.mp3"),

    /** Hint used */
    HINT("hint.mp3"),
}
