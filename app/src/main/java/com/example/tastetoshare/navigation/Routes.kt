package com.example.tastetoshare.navigation

import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class PictureScreenRoute(
    val isFrontCamera: Boolean
)

@Serializable
data object NotificationRoute

@Serializable
data object SavedRecipeRoute


@Serializable
data object ShoppingListRoute {

}

@Serializable
data class ProfileRoute(
    val userId: String,
)

@Serializable
data class EditProfileRoute(val userId: String)

@Serializable
data class RecipeDetailsRoute(
    val recipeId: String,
    val focusReviewId: String? = null
)

@Serializable
data class RecipeListRoute(
    val userId: String?,
    val searchString: String?,
    val difficultyRange: Difficulty?,
    val costRange: Cost?,
    val ingredientsId: List<String>?,
    val tagsId: List<String>?,
)

// Authentification nested graph
@Serializable
object AuthGraph

@Serializable
object LoginRoute

@Serializable
object WelcomeRoute

@Serializable
data class RegisterRoute(val googleEmail: String? = null)

// Recipe nested graph
@Serializable
data class CreateRecipeGraphRoute(val parentRecipeId: String? = null, val editedRecipeId: String? = null)

@Serializable object BasicInfoStepRoute
@Serializable object TagsStepRoute
@Serializable object IngredientsStepRoute
@Serializable object InstructionsStepRoute

@Serializable
data class PictureDialogRoute(
    val isFrontCamera: Boolean
)