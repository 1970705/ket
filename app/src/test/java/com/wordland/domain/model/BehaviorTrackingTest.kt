package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for BehaviorTracking
 */
class BehaviorTrackingTest {
    @Test
    fun `BehaviorTracking creates with correct values for answer action`() {
        // Given
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = "level_01",
                action = "answer",
                isCorrect = true,
                responseTime = 3000L,
                difficulty = 2,
                hintUsed = false,
                isNewWord = true,
                timestamp = System.currentTimeMillis(),
            )

        // Then
        assertEquals("user_001", tracking.userId)
        assertEquals("word_1", tracking.wordId)
        assertEquals("level_01", tracking.sceneId)
        assertEquals("answer", tracking.action)
        assertTrue(tracking.isCorrect!!)
        assertEquals(3000L, tracking.responseTime)
        assertEquals(2, tracking.difficulty)
        assertFalse(tracking.hintUsed!!)
        assertTrue(tracking.isNewWord!!)
    }

    @Test
    fun `BehaviorTracking creates with correct values for hint action`() {
        // Given
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = "level_01",
                action = "hint_used",
                isCorrect = null,
                responseTime = null,
                difficulty = null,
                hintUsed = null,
                isNewWord = null,
            )

        // Then
        assertEquals("hint_used", tracking.action)
        assertNull(tracking.isCorrect)
        assertNull(tracking.responseTime)
        assertNull(tracking.difficulty)
        assertNull(tracking.hintUsed)
        assertNull(tracking.isNewWord)
    }

    @Test
    fun `BehaviorTracking creates with default timestamp`() {
        // Given
        val beforeTime = System.currentTimeMillis()
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = null,
                action = "cross_scene_answer",
                isCorrect = false,
                responseTime = 5000L,
                difficulty = 3,
                hintUsed = true,
                isNewWord = false,
            )
        val afterTime = System.currentTimeMillis()

        // Then
        assertNotNull(tracking.timestamp)
        assertTrue(tracking.timestamp >= beforeTime)
        assertTrue(tracking.timestamp <= afterTime)
    }

    @Test
    fun `BehaviorTracking creates with custom timestamp`() {
        // Given
        val customTimestamp = 1234567890L
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = "review_scene",
                action = "cross_scene_answer",
                isCorrect = true,
                responseTime = 2000L,
                difficulty = 4,
                hintUsed = false,
                isNewWord = false,
                timestamp = customTimestamp,
            )

        // Then
        assertEquals(customTimestamp, tracking.timestamp)
    }

    @Test
    fun `BehaviorTracking allows null sceneId`() {
        // Given
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = null,
                action = "answer",
                isCorrect = true,
                responseTime = 3000L,
                difficulty = null,
                hintUsed = null,
                isNewWord = null,
            )

        // Then
        assertNull(tracking.sceneId)
    }

    @Test
    fun `BehaviorTracking handles different difficulty levels`() {
        // Test all difficulty levels 1-5
        for (difficulty in 1..5) {
            val tracking =
                BehaviorTracking(
                    userId = "user_001",
                    wordId = "word_1",
                    sceneId = "level_01",
                    action = "answer",
                    isCorrect = true,
                    responseTime = 3000L,
                    difficulty = difficulty,
                    hintUsed = false,
                    isNewWord = true,
                )

            assertEquals(difficulty, tracking.difficulty)
        }
    }

    @Test
    fun `BehaviorTracking creates with auto-generated id`() {
        // Given
        val tracking1 =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = null,
                action = "answer",
                isCorrect = null,
                responseTime = null,
                difficulty = null,
                hintUsed = null,
                isNewWord = null,
            )

        val tracking2 =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_2",
                sceneId = null,
                action = "answer",
                isCorrect = null,
                responseTime = null,
                difficulty = null,
                hintUsed = null,
                isNewWord = null,
            )

        // Then - default id should be 0 for both (Room generates actual IDs)
        assertEquals(0L, tracking1.id)
        assertEquals(0L, tracking2.id)
    }

    @Test
    fun `BehaviorTracking tracks cross scene answers`() {
        // Given
        val tracking =
            BehaviorTracking(
                userId = "user_001",
                wordId = "word_1",
                sceneId = "level_05",
                action = "cross_scene_answer",
                isCorrect = true,
                responseTime = 2500L,
                difficulty = 3,
                hintUsed = false,
                isNewWord = false,
            )

        // Then
        assertEquals("cross_scene_answer", tracking.action)
        assertEquals("level_05", tracking.sceneId)
        assertTrue(tracking.isCorrect!!)
        assertFalse(tracking.isNewWord!!)
    }
}
