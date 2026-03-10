package com.wordland.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.wordland.domain.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository interface for pet selection operations
 */
interface PetRepository {
    /**
     * Get all available pets
     */
    fun getAllPets(): List<Pet>

    /**
     * Get the user's selected pet
     * @return The selected pet, or null if none selected yet
     */
    suspend fun getSelectedPet(): Pet?

    /**
     * Get the user's selected pet as a Flow for reactive updates
     */
    fun getSelectedPetFlow(): Flow<Pet?>

    /**
     * Save the user's pet selection
     * @param petId The ID of the pet to select
     */
    suspend fun saveSelectedPet(petId: String)

    /**
     * Clear the user's pet selection
     */
    suspend fun clearPetSelection()
}

/**
 * Implementation of PetRepository using SharedPreferences for persistence
 */
class PetRepositoryImpl(context: Context) : PetRepository {
    companion object {
        private const val PREFS_NAME = "pet_preferences"
        private const val KEY_SELECTED_PET_ID = "selected_pet_id"
    }

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // In-memory cache for the selected pet to enable Flow-based observation
    private val _selectedPetFlow = MutableStateFlow<Pet?>(null)

    init {
        // Load the saved pet on initialization
        _selectedPetFlow.value = loadSelectedPetFromPrefs()
    }

    override fun getAllPets(): List<Pet> {
        return Pet.ALL_PETS
    }

    override suspend fun getSelectedPet(): Pet? {
        return _selectedPetFlow.value ?: loadSelectedPetFromPrefs()
    }

    override fun getSelectedPetFlow(): Flow<Pet?> {
        return _selectedPetFlow.asStateFlow()
    }

    override suspend fun saveSelectedPet(petId: String) {
        // Validate the pet ID
        val pet = Pet.findById(petId)
        if (pet != null) {
            sharedPreferences.edit()
                .putString(KEY_SELECTED_PET_ID, petId)
                .apply()

            // Update the flow
            _selectedPetFlow.value = pet
        } else {
            throw IllegalArgumentException("Invalid pet ID: $petId")
        }
    }

    override suspend fun clearPetSelection() {
        sharedPreferences.edit()
            .remove(KEY_SELECTED_PET_ID)
            .apply()

        // Update the flow
        _selectedPetFlow.value = null
    }

    private fun loadSelectedPetFromPrefs(): Pet? {
        val petId = sharedPreferences.getString(KEY_SELECTED_PET_ID, null)
        return if (petId != null) {
            Pet.findById(petId) ?: Pet.getDefault()
        } else {
            null
        }
    }
}
