package com.example.tastetoshare.repositories

import com.example.tastetoshare.data.entities.Tag
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PaperDbTagRepository: TagRepository {

    private val bookName = "tags"

    private val _tags = MutableStateFlow<List<Tag>>(
        Paper.book(bookName).allKeys.mapNotNull { key ->
            Paper.book(bookName).read<Tag>(key)
        }
    )

    override fun getTags(
        searchString: String?,
        type: String?,
        sortByName: Boolean
    ): Flow<List<Tag>> {
        return _tags.map { tags ->
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
        return _tags.map { tags ->  tags.find { it.tagId == tagId }?:  Tag()  }
    }

    override suspend fun addTag(tag: Tag) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).write(tag.tagId, tag)
            _tags.update { currentList-> currentList + tag}
        }
    }

    override suspend fun deleteTag(tagId: String) {
        withContext(Dispatchers.IO){
            Paper.book(bookName).delete(tagId)
            _tags.update { currentList-> currentList.filter { it.tagId != tagId }}
        }
    }
}