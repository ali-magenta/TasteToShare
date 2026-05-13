package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PaperDbIngredientRepository: IngredientRepository {

    private val bookName = "ingredients"

    private val _ingredients = MutableStateFlow<List<Ingredient>>(
        Paper.book(bookName).allKeys.mapNotNull { key ->
            Paper.book(bookName).read<Ingredient>(key)
        }
    )

    override fun getIngredientById(ingredientId: String): Flow<Ingredient> {
        return _ingredients.map { ingredients ->  ingredients.find { it.ingredientId == ingredientId }?:  Ingredient()  }
    }

    override fun getIngredientByName(name: String): Flow<Ingredient?> {
        return _ingredients.map { ingredients ->  ingredients.filter { !it.isUserCreated }.find { it.name == name }}
    }

    override fun getAppIngredientsSortedByName(searchString: String): Flow<List<Ingredient>> {
        return _ingredients.map { ingredients ->
            ingredients
                .filter { ingredient -> !ingredient.isUserCreated }
                .filter { ingredient -> ingredient.name.startsWith(searchString, ignoreCase = true) }
                .sortedBy { ingredient -> ingredient.name }.toList()
        }
    }

    override suspend fun addOrOverrideIngredient(ingredient: Ingredient) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).write(ingredient.ingredientId, ingredient)
            _ingredients.update { currentList-> if (ingredient.ingredientId in currentList.map{it.ingredientId})
                currentList.filter { it.ingredientId != ingredient.ingredientId } + ingredient
                else  currentList + ingredient
            }
        }
    }

    override suspend fun deleteIngredient(ingredientId: String) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).delete(ingredientId)
            _ingredients.update { currentList-> currentList.filter { it.ingredientId != ingredientId }}
        }
    }
}