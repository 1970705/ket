package com.wordland.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.wordland.domain.model.Pet
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PetRepository
 */
class PetRepositoryTest {
    private lateinit var mockContext: Context
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mockk()
        mockSharedPreferences = mockk()
        mockEditor = mockk()

        every { mockContext.applicationContext } returns mockContext
        every { mockContext.getSharedPreferences("pet_preferences", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit
    }

    @Test
    fun `getAllPets returns all 3 pets`() {
        // Given
        every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
        val repository = PetRepositoryImpl(mockContext)

        // When
        val pets = repository.getAllPets()

        // Then
        assertEquals(3, pets.size)
        assert(pets.contains(Pet.CAT))
        assert(pets.contains(Pet.DOG))
        assert(pets.contains(Pet.BIRD))
    }

    @Test
    fun `getSelectedPet returns null when no pet selected`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
            val repository = PetRepositoryImpl(mockContext)

            // When
            val selectedPet = repository.getSelectedPet()

            // Then
            assertNull(selectedPet)
        }

    @Test
    fun `getSelectedPet returns cat when cat is selected`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns "pet_cat"
            val repository = PetRepositoryImpl(mockContext)

            // When
            val selectedPet = repository.getSelectedPet()

            // Then
            assertNotNull(selectedPet)
            assertEquals(Pet.CAT, selectedPet)
        }

    @Test
    fun `getSelectedPet returns dog when dog is selected`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns "pet_dog"
            val repository = PetRepositoryImpl(mockContext)

            // When
            val selectedPet = repository.getSelectedPet()

            // Then
            assertEquals(Pet.DOG, selectedPet)
        }

    @Test
    fun `getSelectedPet returns bird when bird is selected`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns "pet_bird"
            val repository = PetRepositoryImpl(mockContext)

            // When
            val selectedPet = repository.getSelectedPet()

            // Then
            assertEquals(Pet.BIRD, selectedPet)
        }

    @Test
    fun `saveSelectedPet saves cat ID to SharedPreferences`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
            val repository = PetRepositoryImpl(mockContext)

            // When
            repository.saveSelectedPet("pet_cat")

            // Then
            verify { mockEditor.putString("selected_pet_id", "pet_cat") }
            verify { mockEditor.apply() }
        }

    @Test
    fun `saveSelectedPet saves dog ID to SharedPreferences`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
            val repository = PetRepositoryImpl(mockContext)

            // When
            repository.saveSelectedPet("pet_dog")

            // Then
            verify { mockEditor.putString("selected_pet_id", "pet_dog") }
            verify { mockEditor.apply() }
        }

    @Test
    fun `saveSelectedPet throws exception for invalid ID`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
            val repository = PetRepositoryImpl(mockContext)

            // When & Then
            var exceptionThrown = false
            try {
                repository.saveSelectedPet("invalid_id")
            } catch (e: IllegalArgumentException) {
                exceptionThrown = true
                assertEquals("Invalid pet ID: invalid_id", e.message)
            }

            assert(exceptionThrown)
        }

    @Test
    fun `clearPetSelection removes pet ID from SharedPreferences`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns "pet_cat"
            val repository = PetRepositoryImpl(mockContext)

            // When
            repository.clearPetSelection()

            // Then
            verify { mockEditor.remove("selected_pet_id") }
            verify { mockEditor.apply() }
        }

    @Test
    fun `getSelectedPetFlow emits null initially when no pet selected`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns null
            val repository = PetRepositoryImpl(mockContext)

            // When
            val flowValue = repository.getSelectedPetFlow().first()

            // Then
            assertNull(flowValue)
        }

    @Test
    fun `getSelectedPetFlow emits selected pet`() =
        runTest {
            // Given
            every { mockSharedPreferences.getString("selected_pet_id", null) } returns "pet_dog"
            val repository = PetRepositoryImpl(mockContext)

            // When
            val flowValue = repository.getSelectedPetFlow().first()

            // Then
            assertEquals(Pet.DOG, flowValue)
        }
}
