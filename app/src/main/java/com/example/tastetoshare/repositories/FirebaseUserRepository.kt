package com.example.tastetoshare.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.collections.plus
import androidx.core.net.toUri
import com.example.tastetoshare.data.entities.RecipeIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
): UserRepository {

    private val storageRef = storage.reference
    private val userCollection = firestore.collection(FirebaseCollections.USERS)


    suspend fun initializeData(users: List<User>) {
        val existing = userCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                users.forEach { user ->
                    val docRef = userCollection.document(user.userId)
                    batch.set(docRef, user)
                }
            }.await()

            users.forEach { user ->
                if (!user.photoURI.isNullOrBlank() && user.photoURI.startsWith("http")) {
                    val downloadedUrl = uploadExternalImageToFirebase(
                        user.photoURI,
                        "users/${user.userId}/profile_${System.currentTimeMillis()}.jpg"
                    )
                    userCollection.document(user.userId)
                        .update("photoURI", downloadedUrl)
                        .await()
                }
            }
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

    override fun getUser(userId: String): Flow<User> {
        return userCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
            }.map { users ->  users.find { it.userId == userId }?:  User() }
    }

    override fun getSavedRecipes(userId: String): Flow<Set<String>> {
        return userCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
            }.map { users ->
            users.find { it.userId == userId }?.savedRecipeIds?.toSet() ?: emptySet()
        }
    }

    override fun getShoppingListByUserId(userId: String): Flow<List<RecipeIngredient>> {
        return userCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
            }.map { users ->
                users.find{ it.userId == userId }?.shoppingList?.sortedBy { it.ingredient.name } ?: emptyList()
            }
    }

    override suspend fun addIngredientToShoppingList(userId: String, recipeIngredient: RecipeIngredient) {
        addIngredientListToShoppingList(userId, listOf(recipeIngredient))
    }

    override suspend fun addIngredientListToShoppingList(userId: String, recipeIngredientList: List<RecipeIngredient>) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val user= snapshot.toObject(User::class.java)
            if(user != null) {
                val updatedList = user.shoppingList.toMutableList()

                for (ingredient in recipeIngredientList) {
                    val existIndex = updatedList.indexOfFirst {
                        it.ingredient.ingredientId == ingredient.ingredient.ingredientId &&
                        it.customUnitOfMeasurement.equals(ingredient.customUnitOfMeasurement, ignoreCase = true)
                    }

                    if (existIndex != -1) {
                        val existIngredient = updatedList[existIndex]
                        updatedList[existIndex] = existIngredient.copy(quantity = existIngredient.quantity + ingredient.quantity)
                    }
                    else updatedList.add(ingredient)
                }

                ref.update(User::shoppingList.name, updatedList).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addIngredientListToShoppingList", e)
        }
    }

    override suspend fun updateIngredientFromShoppingList(userId: String, ingredientId: String, oldUnitOfMeasurement: String, quantity: Float?, unitOfMeasurement: String?) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val user = snapshot.toObject(User::class.java)

            if (user != null) {
                val targetUnit = unitOfMeasurement ?: oldUnitOfMeasurement
                var ingToUpdate: RecipeIngredient? = null
                val otherIngs = mutableListOf<RecipeIngredient>()

                for (ing in user.shoppingList) {
                    if (ing.ingredient.ingredientId == ingredientId &&
                        ing.customUnitOfMeasurement.equals(oldUnitOfMeasurement, ignoreCase = true)
                    ) {
                        ingToUpdate = ing
                    } else {
                        otherIngs.add(ing)
                    }
                }
                if (ingToUpdate != null) {
                    val modifiedIng = ingToUpdate.copy(
                        quantity = quantity ?: ingToUpdate.quantity,
                        customUnitOfMeasurement = targetUnit
                    )

                    val existingIndex = otherIngs.indexOfFirst {
                        it.ingredient.ingredientId == ingredientId && it.customUnitOfMeasurement.equals(
                            targetUnit,
                            ignoreCase = true
                        )
                    }

                    val finalShoppingList = if (existingIndex != -1) {
                        val existingIng = otherIngs[existingIndex]
                        val mergedIng = existingIng.copy(
                            quantity = existingIng.quantity + modifiedIng.quantity
                        )
                        otherIngs[existingIndex] = mergedIng
                        otherIngs
                    } else {
                        otherIngs.add(modifiedIng)
                        otherIngs
                    }

                    ref.update(User::shoppingList.name, finalShoppingList).await()
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updateIngredientFromShoppingList", e)
        }
    }

    override suspend fun deleteIngredientFromShoppingList(userId: String, ingredientId: String, unitOfMeasurement: String?){
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                val filteredList = user.shoppingList.filterNot {
                    it.ingredient.ingredientId == ingredientId &&
                    it.customUnitOfMeasurement.equals(unitOfMeasurement, ignoreCase = true)
                }
                ref.update(User::shoppingList.name, filteredList).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleteTriedRecipe", e)
        }
    }

    override suspend fun updateUser(user: User) {
        try {
            val photoURL = if (user.photoURI?.startsWith("content://") ?: false) {
                uploadProfileImage(user.userId, user.photoURI)
            } else {
                user.photoURI
            }
            val userWithPhotoUrl = user.copy(
                photoURI = photoURL
            )

            val currentUser = userCollection.document(user.userId).get().await().toObject(User::class.java)

            userCollection.document(userWithPhotoUrl.userId).set(userWithPhotoUrl).await()
            if (currentUser?.photoURI != userWithPhotoUrl.photoURI && currentUser?.photoURI != null){
                deleteProfileImage(currentUser.photoURI)
                Log.e("FirebaseRepository", "Photo profile cleanup after update")
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updateUser", e)
            //throw e
        }
    }

    override suspend fun addUser(user: User) {
        var uploadedPhotoUrl: String? = null
        try {
            uploadedPhotoUrl = if (user.photoURI?.startsWith("content://") == true) {
                uploadProfileImage(user.userId, user.photoURI)
            } else {
                user.photoURI
            }
            val userWithPhoto = user.copy(photoURI = uploadedPhotoUrl)

            userCollection.document(userWithPhoto.userId).set(userWithPhoto).await()
            Log.d("FirebaseRepository", "Success addUser")
        } catch (e: Exception){
            if (!uploadedPhotoUrl.isNullOrBlank()) {
                deleteProfileImage(uploadedPhotoUrl)
                Log.e("Firebase Storage", "Profile Photo Cleanup")
            }
            Log.e("FirebaseRepository", "Error addUser", e)
            //throw e
        }
    }

    suspend fun uploadProfileImage(userId: String, uri: String): String? {
        if (uri == "") return null

        val imageRef = storageRef.child("users/$userId/profile_${System.currentTimeMillis()}.jpg")
        val fileUri = uri.toUri()
        imageRef.putFile(fileUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun deleteProfileImage(photoUrl: String?) {
        if (photoUrl.isNullOrBlank()) return
        try {
            val imageRef = storage.getReferenceFromUrl(photoUrl)
            imageRef.delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addSavedRecipe(userId: String, recipeId: String) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await()
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::savedRecipeIds.name, user.savedRecipeIds + recipeId).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addSavedRecipe", e)
        }
    }


    override suspend fun addTriedRecipe(userId: String, recipeId: String) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await()
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::triedRecipeIds.name, user.triedRecipeIds + recipeId).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addTriedRecipe", e)
        }
    }

    override suspend fun deleteSavedRecipe(userId: String, recipeId: String){
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await()
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::savedRecipeIds.name, user.savedRecipeIds.filter { it != recipeId }).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleteTriedRecipe", e)
        }
    }



    override suspend fun deleteTriedRecipe(userId: String, recipeId: String){
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await()
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::triedRecipeIds.name, user.triedRecipeIds.filter { it != recipeId }).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleteTriedRecipe", e)
        }
    }

    override suspend fun syncFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCM_DEBUG", "Device token: $token")

            firestore.collection("Users").document(userId)
                .update("fcmToken", token)
                .await()
            Log.d("FCM_DEBUG", "Token sync for $userId")
        } catch (e: Exception) {
            Log.e("FCM_DEBUG", "Token sync error", e)
        }
    }

    override suspend fun userExists(userId: String): Boolean {
        return try {
            userCollection.document(userId).get().await().exists()
        } catch (e: Exception) {
            false
        }
    }
}