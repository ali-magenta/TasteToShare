package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTags(searchString: String?, type : String?, sortByName: Boolean = false): Flow<List<Tag>>
    fun getTagById(tagId: String): Flow<Tag>
    suspend fun addTag(tag : Tag)
    suspend fun deleteTag(tagId : String)
}