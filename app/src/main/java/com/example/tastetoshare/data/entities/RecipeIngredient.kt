package com.example.tastetoshare.data.entities

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class RecipeIngredient @OptIn(ExperimentalUuidApi::class) constructor(
    val recipeIngredientId: String = Uuid.random().toString(),
    val ingredient: Ingredient = Ingredient(),
    val quantity: Float = 0F,
    val customUnitOfMeasurement: String = ""
)