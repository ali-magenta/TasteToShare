package com.example.tastetoshare.data

sealed class Screen {
    object HomeScreen : Screen()
    object PictureScreen : Screen()
    object CreateRecipeScreen : Screen()
    data class RecipeListScreen(val userId: String? = null) : Screen()
    data class ProfileScreen(val userId: String) : Screen()
    data class RecipeScreen(val recipeId: String) : Screen()
}