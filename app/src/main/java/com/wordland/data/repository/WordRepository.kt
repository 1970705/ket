package com.wordland.data.repository

import com.wordland.data.dao.WordDao
import com.wordland.domain.model.Word
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Word data operations
 */
interface WordRepository {
    suspend fun getWordById(wordId: String): Word?

    fun getWordByIdFlow(wordId: String): Flow<Word?>

    suspend fun getWordsByIsland(islandId: String): List<Word>

    suspend fun getWordsByLevel(levelId: String): List<Word>

    suspend fun getRandomKETWords(limit: Int): List<Word>

    suspend fun getRandomPETWords(limit: Int): List<Word>

    suspend fun getWordsByPartOfSpeech(
        partOfSpeech: String,
        limit: Int,
    ): List<Word>

    suspend fun insertWord(word: Word)

    suspend fun insertWords(words: List<Word>)

    suspend fun updateWord(word: Word)

    suspend fun deleteWord(word: Word)

    suspend fun getWordCount(): Int

    suspend fun getKETWordCount(): Int

    suspend fun getPETWordCount(): Int
}

/**
 * Implementation of WordRepository using Room database
 */
class WordRepositoryImpl(
    private val wordDao: WordDao,
) : WordRepository {
    override suspend fun getWordById(wordId: String): Word? {
        return wordDao.getWordById(wordId)
    }

    override fun getWordByIdFlow(wordId: String): Flow<Word?> {
        return wordDao.getWordByIdFlow(wordId)
    }

    override suspend fun getWordsByIsland(islandId: String): List<Word> {
        return wordDao.getWordsByIsland(islandId)
    }

    override suspend fun getWordsByLevel(levelId: String): List<Word> {
        return wordDao.getWordsByLevel(levelId)
    }

    override suspend fun getRandomKETWords(limit: Int): List<Word> {
        return wordDao.getRandomKETWords(limit)
    }

    override suspend fun getRandomPETWords(limit: Int): List<Word> {
        return wordDao.getRandomPETWords(limit)
    }

    override suspend fun getWordsByPartOfSpeech(
        partOfSpeech: String,
        limit: Int,
    ): List<Word> {
        return wordDao.getWordsByPartOfSpeech(partOfSpeech, limit)
    }

    override suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    override suspend fun insertWords(words: List<Word>) {
        wordDao.insertWords(words)
    }

    override suspend fun updateWord(word: Word) {
        wordDao.updateWord(word)
    }

    override suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word)
    }

    override suspend fun getWordCount(): Int {
        return wordDao.getWordCount()
    }

    override suspend fun getKETWordCount(): Int {
        return wordDao.getKETWordCount()
    }

    override suspend fun getPETWordCount(): Int {
        return wordDao.getPETWordCount()
    }
}
