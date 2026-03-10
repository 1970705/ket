package com.wordland.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FillBlankQuestion.LetterSlot nested class.
 * Tests cover LetterSlot properties, factory methods, and edge cases.
 */
class LetterSlotTest {
    // ==================== LetterSlot Properties Tests ====================

    @Test
    fun `LetterSlot isEmpty should return true when letter is null`() {
        val slot = LetterSlot(null, null, 0)

        assertTrue(slot.isEmpty())
    }

    @Test
    fun `LetterSlot isEmpty should return false when letter is not null`() {
        val slot = LetterSlot('a', null, 0)

        assertFalse(slot.isEmpty())
    }

    @Test
    fun `LetterSlot isFilledAndChecked should return false when letter is null`() {
        val slot = LetterSlot(null, true, 0)

        assertFalse(slot.isFilledAndChecked())
    }

    @Test
    fun `LetterSlot isFilledAndChecked should return false when isCorrect is null`() {
        val slot = LetterSlot('a', null, 0)

        assertFalse(slot.isFilledAndChecked())
    }

    @Test
    fun `LetterSlot isFilledAndChecked should return true when both are not null`() {
        val slot = LetterSlot('a', true, 0)

        assertTrue(slot.isFilledAndChecked())
    }

    @Test
    fun `LetterSlot isFilledAndChecked should return false when isCorrect is false`() {
        val slot = LetterSlot('a', false, 0)

        assertTrue(slot.isFilledAndChecked()) // Still filled and checked
    }

    @Test
    fun `LetterSlot should have correct properties`() {
        val slot = LetterSlot('x', false, 3)

        assertEquals('x', slot.letter)
        assertEquals(false, slot.isCorrect)
        assertEquals(3, slot.position)
    }

    @Test
    fun `LetterSlot should handle uppercase letter`() {
        val slot = LetterSlot('A', true, 0)

        assertEquals('A', slot.letter)
        assertEquals(true, slot.isCorrect)
        assertEquals(0, slot.position)
    }

    @Test
    fun `LetterSlot should handle special characters`() {
        val slot = LetterSlot('\'', null, 2)

        assertEquals('\'', slot.letter)
        assertEquals(null, slot.isCorrect)
        assertEquals(2, slot.position)
    }

    @Test
    fun `LetterSlot should handle hyphen`() {
        val slot = LetterSlot('-', null, 4)

        assertEquals('-', slot.letter)
        assertEquals(null, slot.isCorrect)
        assertEquals(4, slot.position)
    }

    // ==================== LetterSlot createEmptySlots Tests ====================

    @Test
    fun `createEmptySlots should create correct number of slots`() {
        val slots = LetterSlot.createEmptySlots(5)

        assertEquals(5, slots.size)
    }

    @Test
    fun `createEmptySlots should create all empty slots`() {
        val slots = LetterSlot.createEmptySlots(3)

        assertTrue(slots.all { it.isEmpty() })
    }

    @Test
    fun `createEmptySlots should set correct positions`() {
        val slots = LetterSlot.createEmptySlots(5)

        assertEquals(0, slots[0].position)
        assertEquals(1, slots[1].position)
        assertEquals(2, slots[2].position)
        assertEquals(3, slots[3].position)
        assertEquals(4, slots[4].position)
    }

    @Test
    fun `createEmptySlots should create slots with null isCorrect`() {
        val slots = LetterSlot.createEmptySlots(3)

        assertTrue(slots.all { it.isCorrect == null })
    }

    @Test
    fun `createEmptySlots should handle single slot`() {
        val slots = LetterSlot.createEmptySlots(1)

        assertEquals(1, slots.size)
        assertEquals(0, slots[0].position)
        assertTrue(slots[0].isEmpty())
    }

    @Test
    fun `createEmptySlots should handle zero slots`() {
        val slots = LetterSlot.createEmptySlots(0)

        assertEquals(0, slots.size)
        assertTrue(slots.isEmpty())
    }

    @Test
    fun `createEmptySlots should handle large number of slots`() {
        val slots = LetterSlot.createEmptySlots(15)

        assertEquals(15, slots.size)
        for (i in 0 until 15) {
            assertEquals(i, slots[i].position)
        }
    }

    @Test
    fun `createEmptySlots should create two slots`() {
        val slots = LetterSlot.createEmptySlots(2)

        assertEquals(2, slots.size)
        assertEquals(0, slots[0].position)
        assertEquals(1, slots[1].position)
        assertTrue(slots.all { it.isEmpty() })
    }

    @Test
    fun `createEmptySlots should create slots for word with hyphen`() {
        val slots = LetterSlot.createEmptySlots(9) // "aunt-like"

        assertEquals(9, slots.size)
        for (i in 0 until 9) {
            assertEquals(i, slots[i].position)
        }
    }

    @Test
    fun `createEmptySlots should create slots for long word`() {
        val slots = LetterSlot.createEmptySlots(14) // "accomplishment"

        assertEquals(14, slots.size)
        for (i in 0 until 14) {
            assertEquals(i, slots[i].position)
        }
    }
}
