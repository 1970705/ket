package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core Word Entity
 * Represents a single vocabulary word with all its metadata
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
@Entity(tableName = "words")
data class Word(
    @PrimaryKey
    val id: String, // "word_001", "word_002", etc.
    val word: String, // "watch", "look", "see"
    val translation: String, // "观看", "看", "看见"
    val pronunciation: String?, // "/wɒtʃ/", null if not available
    val audioPath: String?, // "assets/audio/watch.mp3", null if not available
    val partOfSpeech: String?, // "verb", "noun", etc.
    val difficulty: Int, // 1 (easiest) to 5 (hardest)
    val frequency: Int, // 1 (rare) to 100 (common)
    val theme: String, // "look", "move", "say", etc.
    val islandId: String?, // "look_island", etc.
    val levelId: String?, // "look_island_level_01", etc.
    val order: Int = 0, // Order within level
    val ketLevel: Boolean, // true if in KET syllabus
    val petLevel: Boolean, // true if in PET syllabus
    // Example sentences (JSON string)
    val exampleSentences: String?, // [{"sentence": "I watch TV every evening.", "translation": "我每天晚上看电视。"}]
    // Related words (JSON string) - for future expansion
    val relatedWords: String?, // ["see", "look at", "look for"]
    // Root word parts (for Phase 3: Root System)
    val root: String?, // "spect", "port", etc.
    val prefix: String?, // "re-", "pre-"
    val suffix: String?, // "-ly", "-ful"
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    // Helper to check if this word is relevant to a specific level
    fun belongsToTheme(theme: String): Boolean {
        return this.theme == theme
    }
}
