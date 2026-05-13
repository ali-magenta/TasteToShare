package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Ingredient @OptIn(ExperimentalUuidApi::class) constructor(
    val ingredientId: String = Uuid.random().toString(),
    val name: String = "",
    val unitOfMeasurementToCaloryPerUnit: Map<String, Int> = emptyMap(),
    val photoURL: String? = null,
    val isUserCreated: Boolean = false,
    val userId: String? = null
)
