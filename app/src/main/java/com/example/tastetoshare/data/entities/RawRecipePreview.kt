package com.example.tastetoshare.data.entities

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// similar to recipe, but keep only information for preview and filtering
data class RawRecipePreview @OptIn(ExperimentalUuidApi::class) constructor(
    val recipeId: String = Uuid.random().toString(),
    val creationDate: Long = System.currentTimeMillis(),
    val originalRecipeId: String? = null,
    val authorId: String = "",
    val title: String = "",
    val photoURL: String = "",
    val preparationTime: Int = 0,
    val rating : Float = 0F,
    val numberOfRatings : Long = -1,
    val difficulty : Difficulty = Difficulty.EASY,
    val cost: Cost = Cost.CHEAP,
    val calories: Int? = null,
    val ingredients : List<RecipeIngredient> = emptyList(),
    val tags: List<Tag> = emptyList()
)
