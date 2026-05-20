package com.example.tastetoshare.repositories

import android.util.Log
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.plus

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore
): UserRepository {

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
        }
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

    override suspend fun updateUser(
        user: User
    ) {
        try {
            val ref = userCollection.document(user.userId)
            firestore.runTransaction { transaction -> //Transactions in Firestore are designed to ensure data consistency by communicating with the server. Because of this, they require an active network connection and will fail immediately if the device is offline (e.g., in airplane mode). If this failure isn't caught, it causes the app to crash.
                transaction.set(ref, user)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addOrOverrideRecipe", e)
        }
    }

    override suspend fun addUser(user: User) {
        userCollection.document(user.userId).set( user )
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "Success addUser")
            }
            .addOnFailureListener{ e->
                Log.e("FirebaseRepository", "Error addUser", e)
            }
    }

    override suspend fun addSavedRecipe(userId: String, recipeId: String) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
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
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::triedRecipeIds.name, user.triedRecipeIds + recipeId).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addTriedRecipe", e)
        }
    }

    override suspend fun deleteSavedRecipe(userId: String, recipeId: String) {
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
            val user= snapshot.toObject(User::class.java)
            if(user != null){
                ref.update(User::savedRecipeIds.name, user.savedRecipeIds.filter { it != recipeId }).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleteSavedRecipe", e)
        }
    }


    override suspend fun deleteTriedRecipe(userId: String, recipeId: String){
        try {
            val ref = userCollection.document(userId)
            val snapshot = ref.get()
                .await() // a simpler get().await() and update().await() combination. Unlike transactions, these operations are automatically queued by Firestore and synced to the server once the connection is restored, allowing them to work offline.
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
}