package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class FirebaseIngredientRepository(
    private val firestore: FirebaseFirestore
): IngredientRepository {

    suspend fun initializeData(ingredients: List<Ingredient>) {

    }

    override fun getIngredientById(ingredientId: String): Flow<Ingredient> {
        TODO("Not yet implemented")
    }

    override fun getIngredientByName(name: String): Flow<Ingredient?> {
        TODO("Not yet implemented")
    }

    override fun getAppIngredientsSortedByName(searchString: String): Flow<List<Ingredient>> {
        TODO("Not yet implemented")
    }

    override suspend fun addOrOverrideIngredient(ingredient: Ingredient) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteIngredient(ingredientId: String) {
        TODO("Not yet implemented")
    }
}