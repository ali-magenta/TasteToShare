package com.example.tastetoshare.data.uiClasses

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.RecipeIngredient
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.entities.RawRecipePreview
import java.lang.System
import kotlin.Long
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class UIRecipePreview @OptIn(ExperimentalUuidApi::class) constructor(
    val recipeId: String = Uuid.random().toString(),
    val creationDate: Long = System.currentTimeMillis(),
    val originalRecipeId: String? = null,
    val authorId: String? = null,
    val title: String = "",
    val photoURL: String = "",
    val preparationTime: Int = 0,
    val servings : Int = 0,
    val rating : Float = 0F,
    val numberOfRatings : Long = 0,
    val difficulty : Difficulty = Difficulty.EASY,
    val cost : Cost = Cost.CHEAP,
    val calories: Int? = 0,
    val ingredients : List<RecipeIngredient> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val isSaved: Boolean = false,
    val isTried: Boolean = false
)


fun RawRecipePreview.toUi(user: User): UIRecipePreview {
    return UIRecipePreview(
        recipeId = recipeId,
        creationDate = creationDate,
        originalRecipeId = originalRecipeId,
        authorId = authorId,
        title = title,
        photoURL = photoURL,
        preparationTime = preparationTime,
        rating = rating,
        numberOfRatings = numberOfRatings,
        difficulty = difficulty,
        cost = cost,
        calories = calories,
        ingredients = ingredients,
        tags = tags,
        isSaved = recipeId in user.savedRecipeIds,
        isTried = recipeId in user.triedRecipeIds
    )
}