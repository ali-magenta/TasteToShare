package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class User @OptIn(ExperimentalUuidApi::class) constructor(
    val userId : String = Uuid.random().toString(),
    val fullName: String = "",
    val userName: String = "",
    val email: String = "",
    val role: String = "",
    val description: String = "",
    val photoURI: String? = null,
    val preferences: List<Tag> = emptyList(),
    val savedRecipeIds: List<String> = emptyList(),
    val triedRecipeIds: List<String> = emptyList(),
    val averageRating: Float = 0.0f,
    val numberOfRatings: Int = 0,
    val numberOfRecipeTriedByOthers: Int = 0,
    val numberOfReviews: Int = 0
)
