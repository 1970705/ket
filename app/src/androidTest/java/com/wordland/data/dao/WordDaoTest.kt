package com.wordland.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.data.database.WordDatabase
import com.wordland.domain.model.Word
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for WordDao
 * Uses Room In-Memory database for testing
 */
@RunWith(AndroidJUnit4::class)
class WordDaoTest {
    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao

    // Test data
    private val testWord1 =
        Word(
            id = "word1",
            word = "look",
            translation = "观看",
            pronunciation = "/lʊk/",
            audioPath = null,
            partOfSpeech = "verb",
            difficulty = 1,
            frequency = 80,
            theme = "look",
            islandId = "look_island",
            levelId = "look_island_level_01",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    private val testWord2 =
        Word(
            id = "word2",
            word = "watch",
            translation = "观看",
            pronunciation = "/wɒtʃ/",
            audioPath = null,
            partOfSpeech = "verb",
            difficulty = 2,
            frequency = 70,
            theme = "look",
            islandId = "look_island",
            levelId = "look_island_level_01",
            order = 2,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    @Before
    fun setup() {
        // Create in-memory database
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                WordDatabase::class.java,
            ).build()

        wordDao = database.wordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveWordById() =
        runTest {
            // Given
            wordDao.insertWord(testWord1)

            // When
            val retrieved = wordDao.getWordById("word1")

            // Then
            assertNotNull(retrieved)
            assertEquals("word1", retrieved?.id)
            assertEquals("look", retrieved?.word)
            assertEquals("观看", retrieved?.translation)
        }

    @Test
    fun insertAndRetrieveWordsByLevel() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))

            // When
            val words = wordDao.getWordsByLevel("look_island_level_01")

            // Then
            assertEquals(2, words.size)
            assertEquals("word1", words[0].id)
            assertEquals("word2", words[1].id)
        }

    @Test
    fun insertAndRetrieveWordsByIsland() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))

            // When
            val words = wordDao.getWordsByIsland("look_island")

            // Then
            assertEquals(2, words.size)
        }

    @Test
    fun updateExistingWord() =
        runTest {
            // Given
            wordDao.insertWord(testWord1)

            // When
            val updatedWord = testWord1.copy(translation = "观看（已更新）")
            wordDao.updateWord(updatedWord)

            // Then
            val retrieved = wordDao.getWordById("word1")
            assertEquals("观看（已更新）", retrieved?.translation)
        }

    @Test
    fun deleteWord() =
        runTest {
            // Given
            wordDao.insertWord(testWord1)
            assertEquals(1, wordDao.getWordCount())

            // When
            wordDao.deleteWord(testWord1)

            // Then
            val retrieved = wordDao.getWordById("word1")
            assertNull(retrieved)
            assertEquals(0, wordDao.getWordCount())
        }

    @Test
    fun getWordCount() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))

            // When
            val count = wordDao.getWordCount()

            // Then
            assertEquals(2, count)
        }

    @Test
    fun getKETWordCount() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))

            // When
            val count = wordDao.getKETWordCount()

            // Then
            assertEquals(2, count) // Both test words are KET level
        }

    @Test
    fun getPETWordCount() =
        runTest {
            // Given
            val petWord =
                testWord1.copy(
                    id = "word3",
                    ketLevel = false,
                    petLevel = true,
                )
            wordDao.insertWord(petWord)

            // When
            val count = wordDao.getPETWordCount()

            // Then
            assertEquals(1, count)
        }

    @Test
    fun getRandomKETWords() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))

            // When
            val words = wordDao.getRandomKETWords(2)

            // Then
            assertEquals(2, words.size)
        }

    @Test
    fun getWordByIdAsFlow() =
        runTest {
            // Given
            wordDao.insertWord(testWord1)

            // When
            val flow = wordDao.getWordByIdFlow("word1")
            val word = flow.first()

            // Then
            assertNotNull(word)
            assertEquals("word1", word?.id)
        }

    @Test
    fun deleteAllWords() =
        runTest {
            // Given
            wordDao.insertWords(listOf(testWord1, testWord2))
            assertEquals(2, wordDao.getWordCount())

            // When
            wordDao.deleteAllWords()

            // Then
            assertEquals(0, wordDao.getWordCount())
        }

    @Test
    fun insertWordsWithReplaceStrategy() =
        runTest {
            // Given
            wordDao.insertWord(testWord1)

            // When - Insert same ID with different data
            val updatedWord = testWord1.copy(translation = "新的翻译")
            wordDao.insertWord(updatedWord)

            // Then - Should replace existing
            val retrieved = wordDao.getWordById("word1")
            assertEquals("新的翻译", retrieved?.translation)
            assertEquals(1, wordDao.getWordCount()) // Still only 1 word
        }
}
