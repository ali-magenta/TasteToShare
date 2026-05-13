package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getIngredientById(ingredientId: String): Flow<Ingredient>
    fun getIngredientByName(name: String): Flow<Ingredient?>
    fun getAppIngredientsSortedByName(searchString: String): Flow<List<Ingredient>>

    suspend fun addOrOverrideIngredient(ingredient : Ingredient)
    suspend fun deleteIngredient(ingredientId : String)
}