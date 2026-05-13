package com.example.tastetoshare.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tastetoshare.data.Screen

class ScreenViewModel: ViewModel() {
    var currentScreen by mutableStateOf<Screen>(Screen.HomeScreen)
    var passedRecipeIdForClone by mutableStateOf<String?>(null)
    var passedRecipeIdForEdit by mutableStateOf<String?>(null)

    fun navigateToCreateRecipePage(recipeIdForClone: String?, recipeIdForEdit: String?) {
        passedRecipeIdForEdit = recipeIdForEdit
        passedRecipeIdForClone = recipeIdForClone
        currentScreen = Screen.CreateRecipeScreen
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }
}