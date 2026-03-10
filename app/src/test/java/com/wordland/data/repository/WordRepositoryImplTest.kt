package com.wordland.data.repository

import com.wordland.data.dao.WordDao
import com.wordland.domain.model.Word
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WordRepositoryImpl
 * Tests the repository layer with mocked DAO
 */
class WordRepositoryImplTest {
    private lateinit var repository: WordRepositoryImpl
    private lateinit var wordDao: WordDao

    private val testWordId = "test_word_001"
    private val testIslandId = "look_island"
    private val testLevelId = "look_level_1"

    private val testWord =
        Word(
            id = testWordId,
            word = "test",
            translation = "测试",
            pronunciation = "/test/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 1,
            frequency = 100,
            theme = "test",
            islandId = testIslandId,
            levelId = testLevelId,
            order = 1,
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
        wordDao = mockk(relaxed = true)
        repository = WordRepositoryImpl(wordDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // === Basic CRUD Tests ===

    @Test
    fun `getWordById returns null when not found`() =
        runTest {
            coEvery { wordDao.getWordById(any()) } returns null

            val result = repository.getWordById(testWordId)

            assertNull(result)
            coVerify { wordDao.getWordById(testWordId) }
        }

    @Test
    fun `getWordById returns word when exists`() =
        runTest {
            coEvery { wordDao.getWordById(testWordId) } returns testWord

            val result = repository.getWordById(testWordId)

            assertNotNull(result)
            assertEquals(testWordId, result!!.id)
            assertEquals("test", result.word)
            assertEquals("测试", result.translation)
        }

    @Test
    fun `getWordByIdFlow returns flow from DAO`() =
        runTest {
            coEvery { wordDao.getWordByIdFlow(testWordId) } returns flowOf(testWord)

            val result = repository.getWordByIdFlow(testWordId).first()

            assertNotNull(result)
            assertEquals(testWordId, result!!.id)
        }

    @Test
    fun `getWordsByIsland returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    testWord,
                    testWord.copy(id = "word_002", word = "example", translation = "例子"),
                )
            coEvery { wordDao.getWordsByIsland(testIslandId) } returns expectedList

            val result = repository.getWordsByIsland(testIslandId)

            assertEquals(2, result.size)
            assertEquals(testWordId, result[0].id)
            assertEquals("word_002", result[1].id)
        }

    @Test
    fun `getWordsByIsland returns empty list when no words`() =
        runTest {
            coEvery { wordDao.getWordsByIsland(any()) } returns emptyList()

            val result = repository.getWordsByIsland("unknown_island")

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getWordsByLevel returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    testWord,
                    testWord.copy(id = "word_003", word = "sample", translation = "样本"),
                )
            coEvery { wordDao.getWordsByLevel(testLevelId) } returns expectedList

            val result = repository.getWordsByLevel(testLevelId)

            assertEquals(2, result.size)
            assertEquals(testLevelId, result[0].levelId)
            assertEquals(testLevelId, result[1].levelId)
        }

    @Test
    fun `getRandomKETWords returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    testWord.copy(ketLevel = true, petLevel = false),
                    testWord.copy(id = "word_004", ketLevel = true, petLevel = false),
                )
            coEvery { wordDao.getRandomKETWords(5) } returns expectedList

            val result = repository.getRandomKETWords(5)

            assertEquals(2, result.size)
            assertTrue(result.all { it.ketLevel })
        }

    @Test
    fun `getRandomPETWords returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    testWord.copy(ketLevel = false, petLevel = true),
                    testWord.copy(id = "word_005", ketLevel = false, petLevel = true),
                )
            coEvery { wordDao.getRandomPETWords(5) } returns expectedList

            val result = repository.getRandomPETWords(5)

            assertEquals(2, result.size)
            assertTrue(result.all { it.petLevel })
        }

    @Test
    fun `getWordsByPartOfSpeech returns list from DAO`() =
        runTest {
            val expectedList =
                listOf(
                    testWord.copy(partOfSpeech = "verb"),
                    testWord.copy(id = "word_006", partOfSpeech = "verb"),
                )
            coEvery { wordDao.getWordsByPartOfSpeech("verb", 10) } returns expectedList

            val result = repository.getWordsByPartOfSpeech("verb", 10)

            assertEquals(2, result.size)
            assertTrue(result.all { it.partOfSpeech == "verb" })
        }

    @Test
    fun `insertWord calls DAO`() =
        runTest {
            coEvery { wordDao.insertWord(any()) } just Runs

            repository.insertWord(testWord)

            coVerify { wordDao.insertWord(testWord) }
        }

    @Test
    fun `insertWords calls DAO with list`() =
        runTest {
            val words =
                listOf(
                    testWord,
                    testWord.copy(id = "word_007"),
                    testWord.copy(id = "word_008"),
                )
            coEvery { wordDao.insertWords(any()) } just Runs

            repository.insertWords(words)

            coVerify { wordDao.insertWords(words) }
        }

    @Test
    fun `updateWord calls DAO`() =
        runTest {
            val updatedWord = testWord.copy(translation = "更新后的测试")
            coEvery { wordDao.updateWord(any()) } just Runs

            repository.updateWord(updatedWord)

            coVerify { wordDao.updateWord(updatedWord) }
        }

    @Test
    fun `deleteWord calls DAO`() =
        runTest {
            coEvery { wordDao.deleteWord(any()) } just Runs

            repository.deleteWord(testWord)

            coVerify { wordDao.deleteWord(testWord) }
        }

    @Test
    fun `getWordCount returns count from DAO`() =
        runTest {
            coEvery { wordDao.getWordCount() } returns 100

            val result = repository.getWordCount()

            assertEquals(100, result)
        }

    @Test
    fun `getKETWordCount returns KET count from DAO`() =
        runTest {
            coEvery { wordDao.getKETWordCount() } returns 60

            val result = repository.getKETWordCount()

            assertEquals(60, result)
        }

    @Test
    fun `getPETWordCount returns PET count from DAO`() =
        runTest {
            coEvery { wordDao.getPETWordCount() } returns 40

            val result = repository.getPETWordCount()

            assertEquals(40, result)
        }

    // === Edge Cases ===

    @Test
    fun `getRandomKETWords returns empty list when no KET words`() =
        runTest {
            coEvery { wordDao.getRandomKETWords(any()) } returns emptyList()

            val result = repository.getRandomKETWords(10)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getRandomPETWords returns empty list when no PET words`() =
        runTest {
            coEvery { wordDao.getRandomPETWords(any()) } returns emptyList()

            val result = repository.getRandomPETWords(10)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getWordsByPartOfSpeech returns empty list when no matches`() =
        runTest {
            coEvery { wordDao.getWordsByPartOfSpeech(any(), any()) } returns emptyList()

            val result = repository.getWordsByPartOfSpeech("conjunction", 10)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `insertWords with empty list calls DAO`() =
        runTest {
            coEvery { wordDao.insertWords(any()) } just Runs

            repository.insertWords(emptyList())

            coVerify { wordDao.insertWords(emptyList()) }
        }

    @Test
    fun `getWordsByLevel with unknown level returns empty list`() =
        runTest {
            coEvery { wordDao.getWordsByLevel(any()) } returns emptyList()

            val result = repository.getWordsByLevel("unknown_level")

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getWordById with empty string returns null`() =
        runTest {
            coEvery { wordDao.getWordById(any()) } returns null

            val result = repository.getWordById("")

            assertNull(result)
        }
}
