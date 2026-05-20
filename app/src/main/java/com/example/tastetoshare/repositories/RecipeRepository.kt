package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.RawRecipePreview
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipeById(recipeId: String): Flow<Recipe>
    fun getRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>>
    fun getRecentRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>>
    fun getRecipesPreviewBySearch(
        restrictedRecipeIdList : List<String>? = null,
        searchString: String? = null,
        userId: String? = null,
        ingredients : List<Ingredient>? = null,
        costRange : Cost? = null,
        tags : List<Tag>? = null,
        preparationTimeRange : Pair<Int, Int>? = null,
        ratingsRange : Pair<Float, Float>? = null,
        difficultyRange : Difficulty? = null,
        caloriesPerServingRange : Pair<Int, Int>? = null,
        orderByRating : Boolean = false
    ): Flow<List<RawRecipePreview>>
    fun getTopWeeklyRecipesPreview(): Flow<List<RawRecipePreview>>
    fun getTopMonthlyRecipesPreview(): Flow<List<RawRecipePreview>>
    fun getFilteredTopWeeklyRecipesPreview(tags : List<Tag>? = null): Flow<List<RawRecipePreview>>
    suspend fun addOrOverrideRecipe(recipe: Recipe): Int
    suspend fun deleteRecipe(recipeId : String)
    suspend fun addReview(recipeId : String, review : Review)
    suspend fun deleteReview(recipeId : String, reviewId : String)
}