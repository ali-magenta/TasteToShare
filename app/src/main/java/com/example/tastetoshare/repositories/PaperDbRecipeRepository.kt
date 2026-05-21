package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.RawRecipePreview
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PaperDbRecipeRepository: RecipeRepository {

    private val bookName = "recipes"

    private val _recipes = MutableStateFlow<List<Recipe>>(
        Paper.book(bookName).allKeys.mapNotNull { key ->
            Paper.book(bookName).read<Recipe>(key)
        }
    )

    override fun getRecipeById(recipeId: String): Flow<Recipe> {
        return _recipes.map { recipes ->
            recipes.find { it.recipeId == recipeId }
                ?: Recipe()
        }
    }

    override fun getRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            recipes
                .filter { it.authorId == userId }
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
        }
    }

    override fun getRecentRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            recipes
                .filter { it.authorId == userId }
                .take(5)    //naive implementation, should add a created at field in the future
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
        }
    }

    override fun getRecipesPreviewBySearch(
        restrictedRecipeIdList : List<String>?,
        searchString: String?,
        userId: String?,
        ingredients: List<Ingredient>?,
        costRange: Cost?,
        tags: List<Tag>?,
        preparationTimeRange: Pair<Int, Int>?,
        ratingsRange: Pair<Float, Float>?,
        difficultyRange: Difficulty?,
        caloriesPerServingRange: Pair<Int, Int>?,
        orderByRating: Boolean
    ): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            recipes
                .asSequence()
                .filter { restrictedRecipeIdList == null || it.recipeId in restrictedRecipeIdList }
                .filter { userId == null || it.authorId == userId }
                .filter { recipe -> ingredients == null || ingredients.any { searchedIngredient -> searchedIngredient in recipe.ingredients.map{it.ingredient} } }
                .filter { recipe -> costRange == null || recipe.cost == costRange }
                .filter { recipe -> tags == null || tags.all {searchedTags -> searchedTags in recipe.tags }}
                .filter { recipe -> preparationTimeRange == null || (recipe.preparationTime >= preparationTimeRange.first && recipe.preparationTime <= preparationTimeRange.second)}
                .filter { recipe -> ratingsRange == null || (recipe.rating >= ratingsRange.first && recipe.rating <= ratingsRange.second)}
                .filter { recipe -> difficultyRange == null || recipe.difficulty == difficultyRange}
                .filter { recipe -> caloriesPerServingRange == null || (recipe.calories != null && recipe.calories >= caloriesPerServingRange.first && recipe.calories <= caloriesPerServingRange.second)}
                .filter { recipe -> searchString == null || recipe.title.contains(searchString, ignoreCase = true)} //search by name and case-insensitive
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
                .let { sequence ->
                    if (orderByRating) sequence.sortedBy { it.rating }.toList()
                    else sequence.toList()
                }

        }
    }

    override fun getTopWeeklyRecipesPreview(): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

            recipes
                .filter { it.creationDate >= oneWeekAgo }
                .sortedWith(
                    compareByDescending<Recipe> { it.rating }
                        .thenByDescending { it.numberOfRatings }
                )
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
        }
    }

    override fun getTopMonthlyRecipesPreview(): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            val oneMonthAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)

            recipes
                .filter { it.creationDate >= oneMonthAgo }
                .sortedWith(
                    compareByDescending<Recipe> { it.rating }
                        .thenByDescending { it.numberOfRatings }
                )
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
        }
    }

    override fun getFilteredTopWeeklyRecipesPreview(
        tags: List<Tag>?
    ): Flow<List<RawRecipePreview>> {
        return _recipes.map { recipes ->
            val oneWeekAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)

            recipes
                .filter { it.creationDate >= oneWeekAgo }
                .filter { recipe ->
                    //TODO decide what to do when user has no preferences so the tag list passed in empty
                    // for now it retrieves all recipes (same as doing getTopWeekly)
                    tags.isNullOrEmpty() || recipe.tags.any { it in tags }
                }
                .sortedWith(
                    compareByDescending<Recipe> { it.rating }
                        .thenByDescending { it.numberOfRatings }
                )
                .map { recipe ->
                    RawRecipePreview(
                        recipeId = recipe.recipeId,
                        creationDate = recipe.creationDate,
                        originalRecipeId = recipe.originalRecipeId,
                        authorId = recipe.authorId,
                        title = recipe.title,
                        photoURL = recipe.photoURL,
                        preparationTime = recipe.preparationTime,
                        rating = recipe.rating,
                        numberOfRatings = recipe.numberOfRatings,
                        difficulty = recipe.difficulty,
                        cost = recipe.cost,
                        calories = recipe.calories,
                        ingredients = recipe.ingredients,
                        tags = recipe.tags
                    )
                }
        }
    }

    override suspend fun addOrOverrideRecipe(recipe: Recipe): Int {
        withContext(Dispatchers.IO){
            Paper.book(bookName).write(recipe.recipeId, recipe)
            _recipes.update {currentList-> if (recipe.recipeId in currentList.map{it.recipeId}) currentList.filter{it.recipeId != recipe.recipeId} + recipe else  currentList + recipe}
        }
        return 0
    }

    override suspend fun deleteRecipe(recipeId: String) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).delete(recipeId)
            _recipes.update { currentList-> currentList.filter { it.recipeId != recipeId }}
        }
    }

    override suspend fun addReview(
        recipeId: String,
        review: Review
    ) {
        withContext(Dispatchers.IO){
            val recipe = Paper.book(bookName).read<Recipe>(recipeId)
            if(recipe != null){
                val updatedRecipe = recipe.copy(
                    reviews =  recipe.reviews + review
                )
                Paper.book(bookName).write(recipeId, updatedRecipe)
                _recipes.update { recipes ->
                    recipes.map {
                        if (it.recipeId == recipeId) updatedRecipe else it
                    }
                }
            }
        }
    }

    override suspend fun deleteReview(recipeId: String, reviewId: String) {
        withContext(Dispatchers.IO){
            val recipe = Paper.book(bookName).read<Recipe>(recipeId)
            if(recipe != null){
                val updatedRecipe = recipe.copy(
                    reviews =  recipe.reviews.filter { it.reviewId != reviewId }
                )
                Paper.book(bookName).write(recipeId, updatedRecipe)
                _recipes.update { recipes ->
                    recipes.map {
                        if (it.recipeId == recipeId) updatedRecipe else it
                    }
                }
            }
        }
    }
}