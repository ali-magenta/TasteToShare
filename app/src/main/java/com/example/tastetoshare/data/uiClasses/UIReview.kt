package com.example.tastetoshare.data.uiClasses

import com.example.tastetoshare.data.entities.User
import kotlin.uuid.ExperimentalUuidApi

data class UIReview @OptIn(ExperimentalUuidApi::class) constructor(
    val reviewId: String,
    val photosUrl: List<String>,
    val body: String? = null,
    val rating: Int? = null,
    val user: User
)
