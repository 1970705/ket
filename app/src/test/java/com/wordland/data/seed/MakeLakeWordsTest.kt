package com.wordland.data.seed

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MakeLakeWords
 * Validates 60 words across 10 levels
 */
class MakeLakeWordsTest {
    @Test
    fun `getAllWords returns non-empty list`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then
        assertNotNull(words)
        assertTrue(words.isNotEmpty())
    }

    @Test
    fun `getAllWords returns exactly 60 words`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then - Should have exactly 60 words (10 levels x 6 words)
        assertEquals(60, words.size)
    }

    @Test
    fun `getAllWords contains words with make theme`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then
        assertTrue(words.any { it.theme == "make" })
        assertTrue(words.all { it.islandId == "make_lake" })
    }

    @Test
    fun `getKETWordCount returns 56`() {
        // When
        val count = MakeLakeWords.getKETWordCount()

        // Then - 56 KET words in Make Lake
        assertEquals(56, count)
    }

    @Test
    fun `getKETWordCount counts only KET words`() {
        // When
        val words = MakeLakeWords.getAllWords()
        val ketCount = MakeLakeWords.getKETWordCount()

        // Then
        val actualKetCount = words.count { it.ketLevel }
        assertEquals(actualKetCount, ketCount)
    }

    @Test
    fun `getPETWordCount returns 4`() {
        // When
        val count = MakeLakeWords.getPETWordCount()

        // Then - 4 PET words (architect, innovation, craftsmanship, workmanship in Level 10)
        assertEquals(4, count)
    }

    @Test
    fun `getPETWordCount counts only PET words`() {
        // When
        val words = MakeLakeWords.getAllWords()
        val petCount = MakeLakeWords.getPETWordCount()

        // Then
        val actualPetCount = words.count { it.petLevel }
        assertEquals(actualPetCount, petCount)
    }

    @Test
    fun `getLevelIdForWord returns correct level for valid word`() {
        // When
        val levelId = MakeLakeWords.getLevelIdForWord("make_001")

        // Then
        assertEquals("make_lake_level_01", levelId)
    }

    @Test
    fun `getLevelIdForWord returns default level for invalid word`() {
        // When
        val levelId = MakeLakeWords.getLevelIdForWord("invalid_word")

        // Then
        assertEquals("make_lake_level_01", levelId)
    }

    @Test
    fun `getLevelIdForWord returns correct level for second level words`() {
        // When
        val levelId = MakeLakeWords.getLevelIdForWord("make_007")

        // Then
        assertEquals("make_lake_level_02", levelId)
    }

    @Test
    fun `words have correct island ID`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then
        assertTrue(words.all { it.islandId == "make_lake" })
    }

    @Test
    fun `level 1 words are ordered correctly`() {
        // When
        val words = MakeLakeWords.getAllWords()
        val level1Words = words.filter { it.levelId == "make_lake_level_01" }

        // Then
        assertEquals(6, level1Words.size)
        assertEquals(1, level1Words[0].order)
        assertEquals(6, level1Words[5].order)
    }

    @Test
    fun `words contain required fields`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then - All words should have essential fields
        assertTrue(words.all { it.id.isNotEmpty() })
        assertTrue(words.all { it.word.isNotEmpty() })
        assertTrue(words.all { it.translation.isNotEmpty() })
        assertTrue(words.all { it.difficulty in 1..5 })
        assertTrue(words.all { it.frequency in 1..100 })
    }

    @Test
    fun `getLevelIdForWord handles all level 1 words`() {
        // Given
        val level1Ids = listOf("make_001", "make_002", "make_003", "make_004", "make_005", "make_006")

        // When & Then
        level1Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_01", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 2 words`() {
        // Given
        val level2Ids = listOf("make_007", "make_008", "make_009", "make_010", "make_011", "make_012")

        // When & Then
        level2Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_02", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 3 words`() {
        // Given
        val level3Ids = listOf("make_013", "make_014", "make_015", "make_016", "make_017", "make_018")

        // When & Then
        level3Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_03", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 4 words`() {
        // Given
        val level4Ids = listOf("make_019", "make_020", "make_021", "make_022", "make_023", "make_024")

        // When & Then
        level4Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_04", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 5 words`() {
        // Given
        val level5Ids = listOf("make_025", "make_026", "make_027", "make_028", "make_029", "make_030")

        // When & Then
        level5Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_05", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 6 words`() {
        // Given
        val level6Ids = listOf("make_031", "make_032", "make_033", "make_034", "make_035", "make_036")

        // When & Then
        level6Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_06", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 7 words`() {
        // Given
        val level7Ids = listOf("make_037", "make_038", "make_039", "make_040", "make_041", "make_042")

        // When & Then
        level7Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_07", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 8 words`() {
        // Given
        val level8Ids = listOf("make_043", "make_044", "make_045", "make_046", "make_047", "make_048")

        // When & Then
        level8Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_08", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 9 words`() {
        // Given
        val level9Ids = listOf("make_049", "make_050", "make_051", "make_052", "make_053", "make_054")

        // When & Then
        level9Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_09", levelId)
        }
    }

    @Test
    fun `getLevelIdForWord handles all level 10 words`() {
        // Given
        val level10Ids = listOf("make_055", "make_056", "make_057", "make_058", "make_059", "make_060")

        // When & Then
        level10Ids.forEach { id ->
            val levelId = MakeLakeWords.getLevelIdForWord(id)
            assertEquals("make_lake_level_10", levelId)
        }
    }

    @Test
    fun `getAllLevelIds returns 10 levels`() {
        // When
        val levelIds = MakeLakeWords.getAllLevelIds()

        // Then
        assertEquals(10, levelIds.size)
        assertEquals("make_lake_level_01", levelIds[0])
        assertEquals("make_lake_level_10", levelIds[9])
    }

    @Test
    fun `getWordsForLevel returns 6 words for each level`() {
        // Given
        val allLevels = MakeLakeWords.getAllLevelIds()

        // When & Then
        allLevels.forEach { levelId ->
            val words = MakeLakeWords.getWordsForLevel(levelId)
            assertEquals("Level $levelId should have 6 words", 6, words.size)
        }
    }

    @Test
    fun `getWordCountForLevel returns correct count`() {
        // Given
        val allLevels = MakeLakeWords.getAllLevelIds()

        // When & Then
        allLevels.forEach { levelId ->
            val count = MakeLakeWords.getWordCountForLevel(levelId)
            assertEquals("Level $levelId should have 6 words", 6, count)
        }
    }

    @Test
    fun `level 1 contains basic making verbs`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_01")
        val wordStrings = words.map { it.word }

        // Then - Should contain basic making verbs
        assertTrue(wordStrings.contains("make"))
        assertTrue(wordStrings.contains("build"))
        assertTrue(wordStrings.contains("draw"))
        assertTrue(wordStrings.contains("paint"))
        assertTrue(wordStrings.contains("write"))
        assertTrue(wordStrings.contains("create"))
    }

    @Test
    fun `level 2 contains cooking and food preparation words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_02")
        val wordStrings = words.map { it.word }

        // Then - Should contain cooking words
        assertTrue(wordStrings.contains("cook"))
        assertTrue(wordStrings.contains("bake"))
        assertTrue(wordStrings.contains("mix"))
        assertTrue(wordStrings.contains("cut"))
        assertTrue(wordStrings.contains("fix"))
        assertTrue(wordStrings.contains("clean"))
    }

    @Test
    fun `level 3 contains material words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_03")
        val wordStrings = words.map { it.word }

        // Then - Should contain material words
        assertTrue(wordStrings.contains("wood"))
        assertTrue(wordStrings.contains("metal"))
        assertTrue(wordStrings.contains("plastic"))
        assertTrue(wordStrings.contains("paper"))
        assertTrue(wordStrings.contains("glass"))
        assertTrue(wordStrings.contains("stone"))
    }

    @Test
    fun `level 4 contains tool words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_04")
        val wordStrings = words.map { it.word }

        // Then - Should contain tool words
        assertTrue(wordStrings.contains("hammer"))
        assertTrue(wordStrings.contains("saw"))
        assertTrue(wordStrings.contains("glue"))
        assertTrue(wordStrings.contains("scissors"))
        assertTrue(wordStrings.contains("brush"))
        assertTrue(wordStrings.contains("tool"))
    }

    @Test
    fun `level 5 contains construction words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_05")
        val wordStrings = words.map { it.word }

        // Then - Should contain construction words
        assertTrue(wordStrings.contains("house"))
        assertTrue(wordStrings.contains("wall"))
        assertTrue(wordStrings.contains("floor"))
        assertTrue(wordStrings.contains("roof"))
        assertTrue(wordStrings.contains("door"))
        assertTrue(wordStrings.contains("window"))
    }

    @Test
    fun `level 6 contains arts and crafts words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_06")
        val wordStrings = words.map { it.word }

        // Then - Should contain arts and crafts words
        assertTrue(wordStrings.contains("art"))
        assertTrue(wordStrings.contains("craft"))
        assertTrue(wordStrings.contains("design"))
        assertTrue(wordStrings.contains("shape"))
        assertTrue(wordStrings.contains("color"))
        assertTrue(wordStrings.contains("model"))
    }

    @Test
    fun `level 7 contains technology words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_07")
        val wordStrings = words.map { it.word }

        // Then - Should contain technology words
        assertTrue(wordStrings.contains("machine"))
        assertTrue(wordStrings.contains("robot"))
        assertTrue(wordStrings.contains("computer"))
        assertTrue(wordStrings.contains("invent"))
        assertTrue(wordStrings.contains("electric"))
        assertTrue(wordStrings.contains("power"))
    }

    @Test
    fun `level 8 contains agriculture words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_08")
        val wordStrings = words.map { it.word }

        // Then - Should contain agriculture words
        assertTrue(wordStrings.contains("grow"))
        assertTrue(wordStrings.contains("plant"))
        assertTrue(wordStrings.contains("farm"))
        assertTrue(wordStrings.contains("garden"))
        assertTrue(wordStrings.contains("harvest"))
        assertTrue(wordStrings.contains("seed"))
    }

    @Test
    fun `level 9 contains manufacturing words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_09")
        val wordStrings = words.map { it.word }

        // Then - Should contain manufacturing words
        assertTrue(wordStrings.contains("factory"))
        assertTrue(wordStrings.contains("product"))
        assertTrue(wordStrings.contains("produce"))
        assertTrue(wordStrings.contains("manufacture"))
        assertTrue(wordStrings.contains("assemble"))
        assertTrue(wordStrings.contains("construction"))
    }

    @Test
    fun `level 10 contains expert creation words`() {
        // When
        val words = MakeLakeWords.getWordsForLevel("make_lake_level_10")
        val wordStrings = words.map { it.word }

        // Then - Should contain expert creation words
        assertTrue(wordStrings.contains("engineer"))
        assertTrue(wordStrings.contains("architect"))
        assertTrue(wordStrings.contains("invention"))
        assertTrue(wordStrings.contains("innovation"))
        assertTrue(wordStrings.contains("craftsmanship"))
        assertTrue(wordStrings.contains("workmanship"))
    }

    @Test
    fun `all word IDs are unique`() {
        // When
        val words = MakeLakeWords.getAllWords()
        val ids = words.map { it.id }

        // Then - All IDs should be unique
        val uniqueIds = ids.toSet()
        assertEquals(ids.size, uniqueIds.size)
    }

    @Test
    fun `all words have audio paths configured`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then - All words should have audio paths
        assertTrue(words.all { !it.audioPath.isNullOrEmpty() })
    }

    @Test
    fun `all words have pronunciations`() {
        // When
        val words = MakeLakeWords.getAllWords()

        // Then - All words should have pronunciations
        assertTrue(words.all { !it.pronunciation.isNullOrEmpty() })
    }

    @Test
    fun `difficulty increases in higher levels`() {
        // When
        val level1Words = MakeLakeWords.getWordsForLevel("make_lake_level_01")
        val level10Words = MakeLakeWords.getWordsForLevel("make_lake_level_10")

        // Then - Level 1 should have lower average difficulty than Level 10
        val avgDifficulty1 = level1Words.map { it.difficulty }.average()
        val avgDifficulty10 = level10Words.map { it.difficulty }.average()

        assertTrue(avgDifficulty10 >= avgDifficulty1)
    }
}
