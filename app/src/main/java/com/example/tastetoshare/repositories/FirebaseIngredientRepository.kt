package com.example.tastetoshare.repositories

import android.util.Log
import androidx.core.net.toUri
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.map
import kotlin.collections.plus

class FirebaseIngredientRepository(
    private val firestore: FirebaseFirestore,
    storage: FirebaseStorage
): IngredientRepository {

    private val storageRef = storage.reference
    private val ingredientCollection = firestore.collection(FirebaseCollections.INGREDIENTS)

    suspend fun initializeData(ingredients: List<Ingredient>) {
        val existing = ingredientCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                ingredients.forEach { ingredient ->
                    val docRef = ingredientCollection.document(ingredient.ingredientId)
                    batch.set(docRef, ingredient)
                }
            }.await()
        }
    }


    override fun getAppIngredientsSortedByName(searchString: String): Flow<List<Ingredient>> {
        //return catsCollection.snapshots().toObjects<Cat>()
        return ingredientCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Ingredient::class.java)
            }.map { ingredients ->
                ingredients
                    .filter { ingredient -> !ingredient.isUserCreated }
                    .filter { ingredient -> ingredient.name.startsWith(searchString, ignoreCase = true) }
                    .sortedBy { ingredient -> ingredient.name }.toList()
            }
    }


    override fun getIngredientById(ingredientId: String): Flow<Ingredient> {
        return ingredientCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Ingredient::class.java)
            }.map { ingredients ->  ingredients.find { it.ingredientId == ingredientId }?:  Ingredient()  }
    }

    override fun getIngredientByName(name: String): Flow<Ingredient?> {
        return ingredientCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Ingredient::class.java)
            }.map { ingredients ->  ingredients.filter { !it.isUserCreated }.find { it.name == name }}
    }


    override suspend fun addOrOverrideIngredient(ingredient: Ingredient) {
        try {
            val photoURL = if (ingredient.photoURL != null && ingredient.photoURL.startsWith("content://")) {
                uploadIngredientImage(ingredient.ingredientId, ingredient.photoURL)
            } else {
                ingredient.photoURL
            }
            val updatedIngredient = ingredient.copy(
                photoURL = photoURL
            )
            val ref = ingredientCollection.document(updatedIngredient.ingredientId)
            firestore.runTransaction { transaction -> //Transactions in Firestore are designed to ensure data consistency by communicating with the server. Because of this, they require an active network connection and will fail immediately if the device is offline (e.g., in airplane mode). If this failure isn't caught, it causes the app to crash.
                transaction.set(ref, updatedIngredient)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addOrOverrideIngredient", e)
        }
    }

    suspend fun uploadIngredientImage(ingredientId: String, uri: String): String {
        val imageRef = storageRef.child("ingredients/$ingredientId/ingredient.jpg")
        val fileUri = uri.toUri()
        imageRef.putFile(fileUri).await()
        return imageRef.downloadUrl.await().toString()
    }

    override suspend fun deleteIngredient(ingredientId: String) {
        ingredientCollection.document(ingredientId)
            .delete()
            .addOnSuccessListener { Log.d("delete", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("delete", "Error deleting document", e) }

    }
}



