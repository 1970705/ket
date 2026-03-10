package com.wordland.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.data.dao.WordDao
import com.wordland.data.database.WordDatabase
import com.wordland.domain.model.Word
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for WordRepository
 * Tests Repository + DAO integration with Room database
 */
@RunWith(AndroidJUnit4::class)
class WordRepositoryTest {
    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao
    private lateinit var repository: WordRepository

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
        repository = WordRepositoryImpl(wordDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getWordByIdReturnsCorrectWord() =
        runTest {
            // Given
            repository.insertWord(testWord1)

            // When
            val retrieved = repository.getWordById("word1")

            // Then
            assertNotNull(retrieved)
            assertEquals("word1", retrieved?.id)
            assertEquals("look", retrieved?.word)
        }

    @Test
    fun getWordByIdFlowEmitsWordUpdates() =
        runTest {
            // Given
            repository.insertWord(testWord1)

            // When
            val flow = repository.getWordByIdFlow("word1")
            val word = flow.first()

            // Then
            assertNotNull(word)
            assertEquals("word1", word?.id)
        }

    @Test
    fun getWordsByLevelReturnsAllWordsInLevel() =
        runTest {
            // Given
            repository.insertWords(listOf(testWord1, testWord2))

            // When
            val words = repository.getWordsByLevel("look_island_level_01")

            // Then
            assertEquals(2, words.size)
            assertEquals("word1", words[0].id)
            assertEquals("word2", words[1].id)
        }

    @Test
    fun getWordsByIslandReturnsAllWordsInIsland() =
        runTest {
            // Given
            repository.insertWords(listOf(testWord1, testWord2))

            // When
            val words = repository.getWordsByIsland("look_island")

            // Then
            assertEquals(2, words.size)
        }

    @Test
    fun insertWordAddsWordToDatabase() =
        runTest {
            // When
            repository.insertWord(testWord1)

            // Then
            val count = repository.getWordCount()
            assertEquals(1, count)
        }

    @Test
    fun insertWordsAddsMultipleWordsToDatabase() =
        runTest {
            // When
            repository.insertWords(listOf(testWord1, testWord2))

            // Then
            val count = repository.getWordCount()
            assertEquals(2, count)
        }

    @Test
    fun updateWordModifiesExistingWord() =
        runTest {
            // Given
            repository.insertWord(testWord1)

            // When
            val updatedWord = testWord1.copy(translation = "更新的翻译")
            repository.updateWord(updatedWord)

            // Then
            val retrieved = repository.getWordById("word1")
            assertEquals("更新的翻译", retrieved?.translation)
        }

    @Test
    fun deleteWordRemovesWordFromDatabase() =
        runTest {
            // Given
            repository.insertWord(testWord1)

            // When
            repository.deleteWord(testWord1)

            // Then
            val retrieved = repository.getWordById("word1")
            assertNull(retrieved)
        }

    @Test
    fun getWordCountReturnsTotalWordCount() =
        runTest {
            // Given
            repository.insertWords(listOf(testWord1, testWord2))

            // When
            val count = repository.getWordCount()

            // Then
            assertEquals(2, count)
        }

    @Test
    fun getKETWordCountReturnsKETWordCount() =
        runTest {
            // Given
            repository.insertWords(listOf(testWord1, testWord2))

            // When
            val count = repository.getKETWordCount()

            // Then
            assertEquals(2, count)
        }

    @Test
    fun getRandomKETWordsReturnsRandomWords() =
        runTest {
            // Given
            repository.insertWords(listOf(testWord1, testWord2))

            // When
            val words = repository.getRandomKETWords(2)

            // Then
            assertEquals(2, words.size)
            assertTrue(words.all { it.ketLevel })
        }

    @Test
    fun repositoryIntegrationWithDAOWorksEndToEnd() =
        runTest {
            // Given - Insert words
            repository.insertWords(listOf(testWord1, testWord2))

            // When - Perform multiple operations
            val wordsByLevel = repository.getWordsByLevel("look_island_level_01")
            val wordsByIsland = repository.getWordsByIsland("look_island")
            val count = repository.getWordCount()

            // Then - All operations should work correctly
            assertEquals(2, wordsByLevel.size)
            assertEquals(2, wordsByIsland.size)
            assertEquals(2, count)
        }
}
