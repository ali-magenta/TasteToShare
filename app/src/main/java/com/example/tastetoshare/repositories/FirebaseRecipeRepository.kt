package com.example.tastetoshare.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.net.toUri
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.collections.plus

class FirebaseRecipeRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
): RecipeRepository {

    private val storageRef = storage.reference
    private val recipeCollection = firestore.collection(FirebaseCollections.RECIPES)

    suspend fun initializeData(recipes: List<Recipe>) {
        val existing = recipeCollection.limit(1).get().await()
        if (existing.isEmpty) {
            val updatedRecipes = recipes.map { recipe ->
                val uploadedRecipePhoto = if (recipe.photoURL.startsWith("http")) {
                    uploadExternalImageToFirebase(
                        recipe.photoURL,
                        "recipes/${recipe.recipeId}/recipe_${System.currentTimeMillis()}.jpg"
                    )
                } else {
                    recipe.photoURL
                }

                val updatedSteps = recipe.steps.map { step ->
                    val uploadedStepPhoto = step.photoURL?.let { url ->
                        if (url.startsWith("http")) {
                            uploadExternalImageToFirebase(
                                url,
                                "recipes/${recipe.recipeId}/steps/${step.stepId}.jpg"
                            )
                        } else {
                            url
                        }
                    }
                    step.copy(photoURL = uploadedStepPhoto)
                }

                val updatedReviews = recipe.reviews.map { review ->
                    val uploadedPhotos = review.photosUrl.mapIndexed { index, photoUrl ->
                        if (photoUrl.startsWith("http")) {
                            uploadExternalImageToFirebase(
                                photoUrl,
                                "reviews/${review.reviewId}/$index.jpg"
                            )
                        } else {
                            photoUrl
                        }
                    }
                    review.copy(photosUrl = uploadedPhotos)
                }

                recipe.copy(
                    photoURL = uploadedRecipePhoto,
                    steps = updatedSteps,
                    reviews = updatedReviews
                )
            }

            firestore.runBatch { batch ->
                updatedRecipes.forEach { recipe ->
                    val docRef = recipeCollection.document(recipe.recipeId)
                    batch.set(docRef, recipe)
                }
            }.await()
        }
    }

    private suspend fun uploadExternalImageToFirebase(
        imageUrl: String,
        storagePath: String
    ): String = withContext(Dispatchers.IO) {

        val url = URL(imageUrl)
        val connection = url.openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        if (bitmap == null) {
            throw Exception("Failed to decode image")
        }
        inputStream.close()

        //compress the image
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            85,
            outputStream
        )
        val compressedBytes = outputStream.toByteArray()

        val storageRef = storage.reference.child(storagePath)
        storageRef.putBytes(compressedBytes).await()
        storageRef.downloadUrl.await().toString()
    }

    override fun getRecipeById(recipeId: String): Flow<Recipe> {

        val recipe =  recipeCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Recipe::class.java)
            }.map { recipes ->
                Log.d("DEBUG", "from getRecipeById : ${recipes.find { it.recipeId == recipeId }}")
            recipes.find { it.recipeId == recipeId }
                ?: Recipe()
        }

        return recipe
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

    override suspend fun addOrOverrideRecipe(recipe: Recipe) = withContext(Dispatchers.IO) {
        Log.d("CLONED FROM", "${recipe.originalRecipeId}")
        var uploadedRecipeImageUrl: String? = null
        val uploadedStepImages = mutableListOf<Pair<String, String>>()
        try {

            val currentRecipe = recipeCollection.document(recipe.recipeId).get().await().toObject(Recipe::class.java)
            val photoUrl: String = when {
                recipe.photoURL.startsWith("content://") -> {
                    uploadRecipeImage(recipe.recipeId, recipe.photoURL).also {
                        uploadedRecipeImageUrl = it
                    }
                }

                recipe.photoURL.startsWith("http") && currentRecipe == null  -> {
                    val oldRef = storage.getReferenceFromUrl(recipe.photoURL)
                    val newRef = storage.reference.child(
                        "recipes/${recipe.recipeId}/recipe_${System.currentTimeMillis()}.jpg"
                    )

                    val stream = oldRef.stream.await().stream
                    newRef.putStream(stream).await()

                    newRef.downloadUrl.await().toString()
                }

                else -> recipe.photoURL
            }

            val updatedSteps = recipe.steps.map { step ->
                val updatedPhotoURL = step.photoURL?.let { url ->
                    when {
                        url.startsWith("content://") -> {
                            uploadStepImage(recipe.recipeId, step.stepId, url).also { uploadedUrl ->
                                uploadedStepImages.add(step.stepId to uploadedUrl)
                            }
                        }

                        url.startsWith("http") && currentRecipe == null -> {
                            val oldRef = storage.getReferenceFromUrl(url)
                            val newRef = storage.reference.child(
                                "recipes/${recipe.recipeId}/steps/${step.stepId}.jpg"
                            )

                            val stream = oldRef.stream.await().stream
                            newRef.putStream(stream).await()

                            newRef.downloadUrl.await().toString()
                        }

                        else -> url
                    }
                }

                step.copy(photoURL = updatedPhotoURL)
            }

            val updatedRecipe = recipe.copy(
                photoURL = photoUrl,
                steps = updatedSteps,
                originalRecipeId = recipe.originalRecipeId ?: currentRecipe?.originalRecipeId
            )
            recipeCollection.document(updatedRecipe.recipeId).set(updatedRecipe).await()

            // clean up old photo if photo has changed
            if (currentRecipe?.photoURL != null &&
                currentRecipe.photoURL != updatedRecipe.photoURL
            ) {
                deleteRecipeImage(currentRecipe.photoURL)
            }

            // clean up old steps photos if they have changed
            currentRecipe?.steps?.forEach { oldStep ->
                val newStep = updatedRecipe.steps.find { it.stepId == oldStep.stepId }
                val oldUrl = oldStep.photoURL
                val newUrl = newStep?.photoURL

                if (!oldUrl.isNullOrBlank() && oldUrl != newUrl) {
                    deleteRecipeImage(oldUrl)
                }
            }


            Log.d("FirebaseRepository", "Success addOrOverrideRecipe")

        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addOrOverrideRecipe", e)
            try {
                if (uploadedRecipeImageUrl != null) deleteRecipeImage(uploadedRecipeImageUrl)

                uploadedStepImages.forEach { (_, url) ->
                    deleteRecipeImage(url)
                }
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Cleanup failed", e)
            }
            //throw e
        }
    }

    suspend fun deleteRecipeImage(photoUrl: String?) {
        if (photoUrl.isNullOrBlank()) return
        try {
            val imageRef = storage.getReferenceFromUrl(photoUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Failed to delete image", e)
            //throw e
        }
    }

    suspend fun uploadRecipeImage(recipeId: String, uri: String): String {
        val imageRef = storageRef.child("recipes/$recipeId/recipe${System.currentTimeMillis()}.jpg")
        val fileUri = uri.toUri()
        imageRef.putFile(fileUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun uploadStepImage(recipeId: String, stepId: String, uri: String): String {
        val imageRef = storageRef.child("recipes/$recipeId/steps/$stepId.jpg")
        val fileUri = uri.toUri()
        imageRef.putFile(fileUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    override suspend fun deleteRecipe(recipeId: String) {
        try {
            val recipe = recipeCollection.document(recipeId).get().await().toObject(Recipe::class.java)

            //delete recipe photo
            if (recipe?.photoURL != null) deleteRecipeImage(recipe.photoURL)

            //delete steps photos
            recipe?.steps?.forEach { step ->
                step.photoURL?.let { url ->
                    deleteRecipeImage(url)
                }
            }

            recipeCollection.document(recipeId).delete().await()

            Log.d("deleteRecipe", "Recipe and images successfully deleted")

        } catch (e: Exception) {
            Log.e("deleteRecipe", "Error deleting recipe", e)
            //throw e
        }
    }

    override suspend fun addReview(
        recipeId: String,
        review: Review
    ) {
        try {
            val updatedReview = review.copy(
                photosUrl = uploadReviewImages(review.reviewId, review.photosUrl)
            )
            val ref = recipeCollection.document(recipeId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val recipe= snapshot.toObject(Recipe::class.java)
            if(recipe != null){
                ref.update(Recipe::reviews.name, recipe.reviews + updatedReview).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseCatRepository", "Error toggling favorite", e)
        }
    }

    private suspend fun uploadReviewImages(reviewId: String, photos: List<String>): List<String> {
        return photos.mapIndexed { index, uri ->
            if (uri.startsWith("content://")) {
                val imageRef = storageRef.child("reviews/$reviewId/$index.jpg")
                imageRef.putFile(uri.toUri()).await()
                imageRef.downloadUrl.await().toString()
            } else {
                uri
            }
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

                val folderRef = storageRef.child("reviews/$reviewId")
                val files = folderRef.listAll().await()

                try {
                    files.items.forEach { fileRef ->
                        fileRef.delete().await()
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseStorage", "Failed deleting review images", e)
                }

            }
        } catch (e: Exception) {
            Log.e("FirebaseCatRepository", "Error toggling favorite", e)
        }
    }
}