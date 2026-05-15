package com.example.tastetoshare.repositories

import android.util.Log
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.Tag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.collections.plus

class FirebaseRecipeRepository(
    private val firestore: FirebaseFirestore
): RecipeRepository {

    private val recipeCollection = firestore.collection(FirebaseCollections.RECIPES)

    suspend fun initializeData(recipes: List<Recipe>) {
        val existing = recipeCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                recipes.forEach { recipe ->
                    Log.d("DEBUG", "$recipe")
                    val docRef = recipeCollection.document(recipe.recipeId)
                    batch.set(docRef, recipe)
                }
            }.await()
        }
    }

    override fun getRecipeById(recipeId: String): Flow<Recipe> {
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
            recipes.find { it.recipeId == recipeId }
                ?: Recipe()
        }
    }

    override fun getRecipesPreviewByUserId(userId: String): Flow<List<RawRecipePreview>> {
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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
        return recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
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

    override suspend fun addOrOverrideRecipe(recipe: Recipe) {
        recipeCollection.document(recipe.recipeId).set( recipe )
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "Success addOrOverrideRecipe")
            }
            .addOnFailureListener{ e->
                Log.e("FirebaseRepository", "Error addOrOverrideRecipe", e)
            }
    }

    override suspend fun deleteRecipe(recipeId: String) {
        recipeCollection.document(recipeId)
            .delete()
            .addOnSuccessListener { Log.d("delete", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("delete", "Error deleting document", e) }
    }

    override suspend fun addReview(
        recipeId: String,
        review: Review
    ) {
        try {
            val ref = recipeCollection.document(recipeId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val recipe= snapshot.toObject(Recipe::class.java)
            if(recipe != null){
                ref.update(Recipe::reviews.name, recipe.reviews + review).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseCatRepository", "Error toggling favorite", e)
        }
    }

    override suspend fun deleteReview(recipeId: String, reviewId: String) {
        try {
            val ref = recipeCollection.document(recipeId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val recipe= snapshot.toObject(Recipe::class.java)
            if(recipe != null){
                ref.update(Recipe::reviews.name, recipe.reviews.filter { it.reviewId != reviewId }).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseCatRepository", "Error toggling favorite", e)
        }
    }
}