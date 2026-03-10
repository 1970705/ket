package com.wordland.domain.model

import androidx.compose.runtime.Immutable

/**
 * Pet Entity
 * Represents a virtual pet companion for the learning journey
 *
 * This class is immutable - all properties are val and never change after creation.
 * Annotated with @Immutable to help Compose compiler optimize recomposition.
 */
@Immutable
data class Pet(
    val id: String, // "pet_cat", "pet_dog", "pet_bird"
    val name: String, // Chinese name: "猫", "狗", "鸟"
    val emoji: String, // Emoji representation: "🐱", "🐶", "🐦"
    val description: String, // Short description
) {
    companion object {
        // Predefined pets
        val CAT =
            Pet(
                id = "pet_cat",
                name = "猫",
                emoji = "🐱",
                description = "可爱陪伴",
            )

        val DOG =
            Pet(
                id = "pet_dog",
                name = "狗",
                emoji = "🐶",
                description = "忠实伙伴",
            )

        val BIRD =
            Pet(
                id = "pet_bird",
                name = "鸟",
                emoji = "🐦",
                description = "自由飞翔",
            )

        val ALL_PETS = listOf(CAT, DOG, BIRD)

        /**
         * Find a pet by ID
         * @return The pet with the given ID, or null if not found
         */
        fun findById(id: String): Pet? {
            return ALL_PETS.find { it.id == id }
        }

        /**
         * Get the default pet (Cat)
         */
        fun getDefault(): Pet = CAT
    }
}
