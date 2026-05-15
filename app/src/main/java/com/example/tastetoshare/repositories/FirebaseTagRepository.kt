package com.example.tastetoshare.repositories

import android.util.Log
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Ingredient
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
import kotlin.collections.plus

class FirebaseTagRepository(
    private val firestore: FirebaseFirestore
): TagRepository {

    private val tagCollection = firestore.collection(FirebaseCollections.TAGS)

    suspend fun initializeData(tags: List<Tag>) {
        val existing = tagCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                tags.forEach { tag ->
                    val docRef = tagCollection.document(tag.tagId)
                    batch.set(docRef, tag)
                }
            }.await()
        }
    }


    override fun getTags(
        searchString: String?,
        type: String?,
        sortByName: Boolean
    ): Flow<List<Tag>> {
        return tagCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Tag::class.java)
            }.map { tags ->
            val filteredTags = tags
                .filter { tag -> type == null || tag.type == type }
                .filter { tag -> searchString == null || tag.name.startsWith(searchString, ignoreCase = true) }

            if (sortByName) {
                filteredTags.sortedBy { it.name }
            } else {
                filteredTags
            }
        }
    }

    override fun getTagById(tagId: String): Flow<Tag> {
        return tagCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Tag::class.java)
            }.map { tags ->  tags.find { it.tagId == tagId }?:  Tag()  }
    }

    override suspend fun addTag(tag: Tag) {
        try {
            val ref = tagCollection.document(tag.tagId)
            firestore.runTransaction { transaction -> //Transactions in Firestore are designed to ensure data consistency by communicating with the server. Because of this, they require an active network connection and will fail immediately if the device is offline (e.g., in airplane mode). If this failure isn't caught, it causes the app to crash.
                transaction.set(ref, tag)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addTag", e)
        }
    }

    override suspend fun deleteTag(tagId: String) {
        tagCollection.document(tagId)
            .delete()
            .addOnSuccessListener { Log.d("delete", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("delete", "Error deleting document", e) }
    }
}