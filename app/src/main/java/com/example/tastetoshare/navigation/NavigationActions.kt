package com.example.tastetoshare.navigation

import androidx.navigation.NavController
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.auth.SessionManagerFacade
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
    fun navigateToCreateRecipe(cloneId: String?, editId: String?) {
        if (SessionManagerFacade.isLoggedIn) {
            navCtrl.navigate(CreateRecipeGraphRoute(cloneId, editId))
        } else navCtrl.navigate(WelcomeRoute)
    }
    fun navigateToRecipeDetails(id: String) = navCtrl.navigate(RecipeDetailsRoute(id))

    fun navigateToRecipeDetailsWithReviewScroll(id: String, reviewId: String) = navCtrl.navigate(RecipeDetailsRoute(id, reviewId))
    fun navigateToHomePage() = navCtrl.navigate(HomeRoute)
    fun navigateToMap() = navCtrl.navigate(MapRoute)
    fun navigateBack() = navCtrl.popBackStack()
    fun navigateToNotifications() {
        if(SessionManagerFacade.isLoggedIn) {
        navCtrl.navigate(NotificationRoute)
        }
        else navCtrl.navigate(WelcomeRoute)
    }
    fun navigateToShoppingList() {
        if(SessionManagerFacade.isLoggedIn) {
        navCtrl.navigate(ShoppingListRoute)
        }
        else navCtrl.navigate(WelcomeRoute)
    }
    fun navigateToSavedRecipes() {
        if(SessionManagerFacade.isLoggedIn) {
            navCtrl.navigate(SavedRecipeRoute)
        }
        else navCtrl.navigate(WelcomeRoute)
    }
    fun navigateToRegister(googleEmail: String?) = navCtrl.navigate(RegisterRoute(googleEmail))
    //fun navigateToLogin() = navCtrl.navigate(LoginRoute)
    fun navigateToPictureScreen(isFrontCamera: Boolean) = navCtrl.navigate(PictureScreenRoute(isFrontCamera))
    fun navigateToPictureDialog(isFrontCamera: Boolean) = navCtrl.navigate(PictureDialogRoute(isFrontCamera))
    fun navigateToHomeFromAuth() = navCtrl.navigate(HomeRoute) {
        popUpTo<AuthGraph> { inclusive = true }
    }
    fun navigateToLogin() = navCtrl.navigate(WelcomeRoute)
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