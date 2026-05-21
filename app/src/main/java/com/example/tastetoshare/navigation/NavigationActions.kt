package com.example.tastetoshare.navigation

import androidx.navigation.NavController
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppNavigationActions(private val navCtrl: NavController) {
    fun navigateToRecipeList(
        userId: String? = null,
        searchString: String? = null,
        difficultyRange: Difficulty? = null,
        costRange: Cost? = null,
        ingredientsId: List<String>? = null,
        tagsId: List<String>? = null
    ) = navCtrl.navigate(RecipeListRoute(userId, searchString, difficultyRange, costRange, ingredientsId, tagsId))
    fun navigateToProfile(userId: String) = navCtrl.navigate(ProfileRoute(userId))
    fun navigateToEditProfile(userId: String) = navCtrl.navigate(EditProfileRoute(userId))
    fun navigateToCreateRecipe(cloneId: String?, editId: String?) =
        navCtrl.navigate(CreateRecipeGraphRoute(cloneId, editId))
    fun navigateToRecipeDetails(id: String) = navCtrl.navigate(RecipeDetailsRoute(id))

    fun navigateToRecipeDetailsWithReviewScroll(id: String, reviewId: String) = navCtrl.navigate(RecipeDetailsRoute(id, reviewId))
    fun navigateToHomePage() = navCtrl.navigate(HomeRoute)
    fun navigateBack() = navCtrl.popBackStack()
    fun navigateToNotifications() = navCtrl.navigate(NotificationRoute)
    fun navigateToShoppingList() = navCtrl.navigate(ShoppingListRoute)
    fun navigateToSavedRecipes() = navCtrl.navigate(SavedRecipeRoute)
    fun navigateToRegister() = navCtrl.navigate(RegisterRoute)
    fun navigateToLogin() = navCtrl.navigate(LoginRoute)
    fun navigateToPictureScreen(isFrontCamera: Boolean) = navCtrl.navigate(PictureScreenRoute(isFrontCamera))
    fun navigateToPictureDialog(isFrontCamera: Boolean) = navCtrl.navigate(PictureDialogRoute(isFrontCamera))
    fun navigateToHomeFromAuth() = navCtrl.navigate(HomeRoute) {
        popUpTo<AuthGraph> { inclusive = true }
    }
    fun navigateToLoginDialog() = navCtrl.navigate(LoginDialogRoute)
    fun returnUri(uri: String) {
        navCtrl.previousBackStackEntry
            ?.savedStateHandle
            ?.set("returned_uri", uri)
        navCtrl.popBackStack()
    }
    fun getReturnedUriFlow(): StateFlow<String?> { // this is to maje the uri observable in the composable
        return navCtrl.currentBackStackEntry?.savedStateHandle
            ?.getStateFlow<String?>("returned_uri", null)
            ?: MutableStateFlow(null)
    }
}