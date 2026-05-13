package com.example.tastetoshare.navigation

import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.screens.EditProfileDestination
import com.example.tastetoshare.screens.HomeScreen
import com.example.tastetoshare.screens.HomepageDestination
import com.example.tastetoshare.screens.NotificationDestination
import com.example.tastetoshare.screens.PictureDialogDestination
import com.example.tastetoshare.screens.ProfileDestination
import com.example.tastetoshare.screens.RecipeDetailsDestination
import com.example.tastetoshare.screens.RecipeListDestination
import com.example.tastetoshare.screens.SavedRecipeDestination
import com.example.tastetoshare.screens.ShoppingListDestination
import com.example.tastetoshare.screens.components.TasteToShareBottomBar
import com.example.tastetoshare.ui.screens.PictureScreenDestination
import com.example.tastetoshare.viewmodels.RecipeDetailsViewModel

@Composable
fun AppNavigation() {
    val navCtrl = rememberNavController()
    val navActions = remember(navCtrl) { AppNavigationActions(navCtrl) }

    val navBackStackEntry by navCtrl.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAuthScreen = currentDestination?.hasRoute<LoginRoute>() == true ||
            currentDestination?.hasRoute<RegisterRoute>() == true ||
            currentDestination?.hasRoute<WelcomeRoute>() == true ||
            currentDestination?.hasRoute<AuthGraph>() == true

    val isCreateRecipeScreen = currentDestination?.hierarchy?.any {
        it.hasRoute<CreateRecipeGraphRoute>()
    } == true

    val isPictureScreen = currentDestination?.hierarchy?.any {
        it.hasRoute<PictureScreenRoute>()
    } == true

    val isPictureDialog = currentDestination?.hierarchy?.any {
        it.hasRoute<PictureDialogRoute>()
    } == true


    Scaffold(
        bottomBar = {
            if (!isCreateRecipeScreen && !isAuthScreen && !isPictureScreen && !isPictureDialog && (SessionManager.CURRENT_LOGGED_IN_USER_ID != null)) {
                TasteToShareBottomBar(
                    currentDestination = currentDestination,
                    onNavigateHome = { navActions.navigateToHomePage() },
                    onNavigateShopping = { navActions.navigateToShoppingList() },
                    onNavigateSaved = { navActions.navigateToSavedRecipes() },
                    onNavigateProfile = { navActions.navigateToProfile(SessionManager.CURRENT_LOGGED_IN_USER_ID) },
                    onCreateRecipe = {
                        navActions.navigateToCreateRecipe(null, null)
                    }
                )
            }
        },
    )
    {  globalPadding ->
        NavHost(
            navController = navCtrl,
            startDestination = AuthGraph,
            modifier = Modifier.padding(bottom = globalPadding.calculateBottomPadding() - 24.dp)
        ) {
            // Nested graph for login
            authGraph(navActions, navCtrl = navCtrl)

            composable<HomeRoute>
            {
                HomepageDestination(
                    onNavigateToRecipeProposal = { recipeId -> navActions.navigateToRecipeDetails(recipeId) },
                    onNavigateToNotifications = { navActions.navigateToNotifications() },
                    onSearchSubmit = { search -> navActions.navigateToRecipeList(searchString = search) }
                )
                //HomeScreen(
                //    onNavigateToRecipeList = { navActions.navigateToRecipeList() },
                //    onNavigateToProfile = { userId -> navActions.navigateToProfile(userId) },
                //    onNavigateToCreateRecipe = { cloneId, editId -> navActions.navigateToCreateRecipe(cloneId, editId) },
                //    onNavigateToRecipeProposal = { recipeId -> navActions.navigateToRecipeDetails(recipeId) },
//
                //    // for final HomeScreen (not useful yet)
                //    onNavigateToNotifications = { navActions.navigateToNotifications() }
                //)
            }

            composable<ProfileRoute>
            { backStackEntry ->
                val args = backStackEntry.toRoute<ProfileRoute>()
                ProfileDestination(
                    onViewAllRecipes = { userId -> navActions.navigateToRecipeList(userId) },
                    onNavigateToRecipeProposal = { recipeId -> navActions.navigateToRecipeDetails(recipeId) },
                    onNavigateToEditProfile = {navActions.navigateToEditProfile(args.userId) },
                )
            }

            composable<EditProfileRoute> {
                EditProfileDestination(
                    onNavigateBack = { navActions.navigateBack()},
                    onNavigateToPictureDialog = {isFrontCamera -> navActions.navigateToPictureDialog(isFrontCamera)},
                    getReturnedUri = navActions::getReturnedUriFlow
                )
            }

            composable<RecipeDetailsRoute>
            { backStackEntry ->
                val viewModel: RecipeDetailsViewModel = viewModel(
                    backStackEntry,
                    factory = RecipeDetailsViewModel.Factory
                )

                val returnedUri by backStackEntry.savedStateHandle
                    .getStateFlow<String>("returned_uri", "")
                    .collectAsStateWithLifecycle()

                LaunchedEffect(returnedUri) {
                    val uri = returnedUri
                    if (uri != null) {
                        viewModel.onReviewPhotoChanged(uri)
                        backStackEntry.savedStateHandle["returned_uri"] = null
                    }
                }

                RecipeDetailsDestination(
                    onNavigateToProfile = { userId -> navActions.navigateToProfile(userId) },
                    onNavigateToRecipeDetails = { recipeId ->
                        navActions.navigateToRecipeDetails(recipeId)
                    },
                    onNavigateToCreateRecipePage = { cloneId, editId ->
                        navActions.navigateToCreateRecipe(cloneId, editId)
                    },
                    onNavigateToHome = { navActions.navigateToHomePage() },
                    onNavigateToPictureDialog = {isFrontCamera -> navActions.navigateToPictureDialog(isFrontCamera)},
                    viewModel = viewModel
                    // for final RecipeDetails (idk if we want to access shopping list when adding ingredients to it or not)
                    // onNavigateToShoppingList = {navActions.navigateToShoppingList()}
                )
            }

            composable<RecipeListRoute>
            {
                RecipeListDestination(
                    onNavigateToDetails = { recipeId ->
                        navActions.navigateToRecipeDetails(recipeId)
                    },
                )
            }

            createRecipeGraph(
                navCtrl = navCtrl,
                onRecipeCreated = { recipeId -> navActions.navigateToRecipeDetails(recipeId)},
                onNavigateToPictureDialog = {isFrontCamera -> navActions.navigateToPictureDialog(isFrontCamera) },
            )

            composable<PictureScreenRoute>
            {
                PictureScreenDestination(
                    returnUri = navActions::returnUri
                )
            }

            dialog<PictureDialogRoute> {
                PictureDialogDestination(
                    onDismiss = { navActions.navigateBack() },
                    returnUri = navActions::returnUri,
                    onNavigateToPictureScreen = {isFrontCamera -> navActions.navigateToPictureScreen(isFrontCamera) },
                )
            }

            composable<NotificationRoute>
            {
                NotificationDestination(
                    onNavigateToProfile = { userId -> navActions.navigateToProfile(userId) },
                    onNavigateToRecipeDetails = { recipeId ->
                        navActions.navigateToRecipeDetails(recipeId)
                    },
                )
            }

            composable<ShoppingListRoute>
            {
                ShoppingListDestination()
            }

            composable<SavedRecipeRoute>
            {
                SavedRecipeDestination(
                    onNavigateToRecipeDetails = { recipeId ->
                        navActions.navigateToRecipeDetails(recipeId)
                    },
                )
            }
        }
    }
}