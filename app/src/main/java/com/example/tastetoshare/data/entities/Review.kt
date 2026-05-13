package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Review @OptIn(ExperimentalUuidApi::class) constructor(
    val reviewId: String = Uuid.random().toString(),
    val creationDate: Long = System.currentTimeMillis(),
    val photosUrl: List<String> = emptyList(),
    val body: String? = null,
    val rating: Int? = null,
    val userId: String = Uuid.random().toString()
)
