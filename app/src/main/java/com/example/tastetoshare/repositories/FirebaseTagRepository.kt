package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Tag
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FirebaseTagRepository(
    private val firestore: FirebaseFirestore
): TagRepository {

    suspend fun initializeData(tags: List<Tag>) {

    }

    override fun getTags(
        searchString: String?,
        type: String?,
        sortByName: Boolean
    ): Flow<List<Tag>> {
        TODO("Not yet implemented")
    }

    override fun getTagById(tagId: String): Flow<Tag> {
        TODO("Not yet implemented")
    }

    override suspend fun addTag(tag: Tag) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTag(tagId: String) {
        TODO("Not yet implemented")
    }
}