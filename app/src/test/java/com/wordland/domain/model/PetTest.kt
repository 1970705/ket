package com.wordland.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for Pet model
 */
class PetTest {
    @Test
    fun `pet IDs are correct`() {
        assertEquals("pet_cat", Pet.CAT.id)
        assertEquals("pet_dog", Pet.DOG.id)
        assertEquals("pet_bird", Pet.BIRD.id)
    }

    @Test
    fun `pet names are in Chinese`() {
        assertEquals("猫", Pet.CAT.name)
        assertEquals("狗", Pet.DOG.name)
        assertEquals("鸟", Pet.BIRD.name)
    }

    @Test
    fun `pet emojis are correct`() {
        assertEquals("🐱", Pet.CAT.emoji)
        assertEquals("🐶", Pet.DOG.emoji)
        assertEquals("🐦", Pet.BIRD.emoji)
    }

    @Test
    fun `pet descriptions are correct`() {
        assertEquals("可爱陪伴", Pet.CAT.description)
        assertEquals("忠实伙伴", Pet.DOG.description)
        assertEquals("自由飞翔", Pet.BIRD.description)
    }

    @Test
    fun `ALL_PETS contains exactly 3 pets`() {
        assertEquals(3, Pet.ALL_PETS.size)
    }

    @Test
    fun `ALL_PETS contains cat, dog, and bird`() {
        assert(Pet.ALL_PETS.contains(Pet.CAT))
        assert(Pet.ALL_PETS.contains(Pet.DOG))
        assert(Pet.ALL_PETS.contains(Pet.BIRD))
    }

    @Test
    fun `findById returns correct pet`() {
        assertEquals(Pet.CAT, Pet.findById("pet_cat"))
        assertEquals(Pet.DOG, Pet.findById("pet_dog"))
        assertEquals(Pet.BIRD, Pet.findById("pet_bird"))
    }

    @Test
    fun `findById returns null for invalid ID`() {
        assertNull(Pet.findById("invalid_id"))
        assertNull(Pet.findById(""))
        assertNull(Pet.findById("pet_fish"))
    }

    @Test
    fun `getDefault returns cat`() {
        assertEquals(Pet.CAT, Pet.getDefault())
    }

    @Test
    fun `Pet is immutable data class`() {
        val cat = Pet.CAT
        val sameCat = Pet.CAT
        assertEquals(cat, sameCat)

        val catCopy = cat.copy()
        assertEquals(cat, catCopy)
    }
}
