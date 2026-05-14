package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.Tag
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class FirebaseRecipeRepository(
    private val firestore: FirebaseFirestore
): RecipeRepository {

    suspend fun initializeData(recipes: List<Recipe>) {

    }

    override fun getRecipeById(recipeId: String): Flow<Recipe> {
        TODO("Not yet implemented")
    }

    override fun getRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>> {
        TODO("Not yet implemented")
    }

    override fun getRecentRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>> {
        TODO("Not yet implemented")
    }

    override fun getRecipesPreviewBySearch(
        restrictedRecipeIdList: List<String>?,
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
        TODO("Not yet implemented")
    }

    override fun getTopWeeklyRecipesPreview(): Flow<List<RawRecipePreview>> {
        TODO("Not yet implemented")
    }

    override fun getTopMonthlyRecipesPreview(): Flow<List<RawRecipePreview>> {
        TODO("Not yet implemented")
    }

    override fun getFilteredTopWeeklyRecipesPreview(tags: List<Tag>?): Flow<List<RawRecipePreview>> {
        TODO("Not yet implemented")
    }

    override suspend fun addOrOverrideRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipe(recipeId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun addReview(
        recipeId: String,
        review: Review
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReview(recipeId: String, reviewId: String) {
        TODO("Not yet implemented")
    }
}