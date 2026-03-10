package com.wordland.data.dao

import androidx.room.*
import com.wordland.domain.model.Word
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Word entities
 */
@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWordById(wordId: String): Word?

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun getWordByIdFlow(wordId: String): Flow<Word?>

    @Query("SELECT * FROM words WHERE islandId = :islandId ORDER BY difficulty ASC")
    suspend fun getWordsByIsland(islandId: String?): List<Word>

    @Query("SELECT * FROM words WHERE levelId = :levelId ORDER BY `order` ASC")
    suspend fun getWordsByLevel(levelId: String?): List<Word>

    @Query("SELECT * FROM words WHERE ketLevel = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomKETWords(limit: Int = 10): List<Word>

    @Query("SELECT * FROM words WHERE petLevel = 1 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPETWords(limit: Int = 10): List<Word>

    @Query("SELECT * FROM words WHERE partOfSpeech = :pos ORDER BY RANDOM() LIMIT :limit")
    suspend fun getWordsByPartOfSpeech(
        pos: String,
        limit: Int = 5,
    ): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("DELETE FROM words WHERE islandId = :islandId")
    suspend fun deleteWordsByIsland(islandId: String)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE ketLevel = 1")
    suspend fun getKETWordCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE petLevel = 1")
    suspend fun getPETWordCount(): Int
}
