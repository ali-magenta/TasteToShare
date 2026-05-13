package com.example.tastetoshare.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastetoshare.screens.CreateRecipeSharedLayout
import com.example.tastetoshare.screens.components.BasicInfoStep
import com.example.tastetoshare.screens.components.IngredientsStep
import com.example.tastetoshare.screens.components.InstructionsStep
import com.example.tastetoshare.screens.components.TagsStep
import com.example.tastetoshare.viewmodels.CreateRecipeViewModel

fun NavGraphBuilder.createRecipeGraph(
    navCtrl: NavController,
    onRecipeCreated: (String) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit
) {
    navigation<CreateRecipeGraphRoute>(startDestination = BasicInfoStepRoute) {

        composable<BasicInfoStepRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navCtrl.getBackStackEntry<CreateRecipeGraphRoute>()
            }
            val viewModel: CreateRecipeViewModel = viewModel(
                parentEntry,
                factory = CreateRecipeViewModel.Factory
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val returnedUri = navCtrl.currentBackStackEntry?.savedStateHandle?.get<String>("returned_uri")
            LaunchedEffect(returnedUri) {
                if (returnedUri != null) {
                    viewModel.handlePhotoUri(returnedUri)
                    backStackEntry.savedStateHandle["returned_uri"] = null
                }
            }

            CreateRecipeSharedLayout(currentStep = 1, uiState.recipeIdFromEdit != null) {
                BasicInfoStep(
                    uiState = uiState,
                    onTitleChange = { viewModel.setTitle(it) },
                    onDescriptionChange = { viewModel.setDescription(it) },
                    onPrepTimeChange = { viewModel.setPreparationTime(it) },
                    onCostChange = { viewModel.setCost(it) },
                    onDifficultyChange = { viewModel.setDifficulty(it) },
                    onServingsChange = { viewModel.setServings(it) },
                    onNextClick = {
                        if (viewModel.validateBasicInfoStep())
                        {
                            navCtrl.navigate(TagsStepRoute)
                        }
                    },
                    onSetPhotoTarget = viewModel::setPhotoTarget,
                    onNavigateToPictureDialog = {isFrontCamera -> onNavigateToPictureDialog(isFrontCamera)}
                )
            }
        }

        composable<TagsStepRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navCtrl.getBackStackEntry<CreateRecipeGraphRoute>() }
            val viewModel: CreateRecipeViewModel = viewModel(parentEntry, factory = CreateRecipeViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CreateRecipeSharedLayout(currentStep = 2, uiState.recipeIdFromEdit != null) {
                TagsStep(
                    uiState = uiState,
                    onNextClick = {
                        if (viewModel.validateTagsStep())
                        {
                            navCtrl.navigate(IngredientsStepRoute)
                        }
                    },
                    onCuisineTagClick = { viewModel.toggleSelectedCuisineTag(it) },
                    onDietaryTagClick= { viewModel.toggleSelectedDietaryTag(it)},
                    onOriginTagClick= { viewModel.toggleSelectedCountryTag(it)},
                    onOtherTagClick = { viewModel.toggleSelectedOtherTag(it)},
                    onCuisineSearchChange = { viewModel.setFieldSearchCuisineTypeTags(it)},
                    onDietarySearchChange = { viewModel.setFieldSearchDietaryRestrictionTags(it)},
                    onOriginSearchChange = { viewModel.setFieldSearchCountryOfOriginTag(it)},
                    onOtherSearchChange = { viewModel.setFieldSearchOtherTags(it)},
                )
            }
        }

        composable<IngredientsStepRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navCtrl.getBackStackEntry<CreateRecipeGraphRoute>() }
            val viewModel: CreateRecipeViewModel = viewModel(parentEntry, factory = CreateRecipeViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CreateRecipeSharedLayout(currentStep = 3, uiState.recipeIdFromEdit != null) {
                IngredientsStep(
                    uiState = uiState,
                    onNextClick = {
                        if (viewModel.validateIngredientStep())
                        {
                            navCtrl.navigate(InstructionsStepRoute)
                        }
                    },
                    onDeleteClick = { viewModel.removeIngredient(it)},
                    onIngredientSearchChange = { viewModel.setFieldSearchIngredient(it)},
                    onIngredientClick = { viewModel.setSelectedIngredient(it)},
                    onAddIngredientClick = { viewModel.addIngredient()},
                    onQuantityChange = { viewModel.setFieldIngredientQuantity(it)},
                    onUnitChange = { viewModel.setFieldIngredientUnit(it)}
                )
            }
        }

        composable<InstructionsStepRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) { navCtrl.getBackStackEntry<CreateRecipeGraphRoute>() }
            val viewModel: CreateRecipeViewModel = viewModel(parentEntry, factory = CreateRecipeViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.savedRecipeId) {
                val recipeId = uiState.savedRecipeId
                if (recipeId != null) {
                    onRecipeCreated(recipeId)
                    viewModel.navigationHandled()
                }
            }
            val returnedUri by backStackEntry.savedStateHandle.getStateFlow<String?>("returned_uri", null).collectAsStateWithLifecycle()
            LaunchedEffect(returnedUri) {
                val uri = returnedUri
                if (uri != null) {
                    viewModel.handlePhotoUri(uri)
                    backStackEntry.savedStateHandle["returned_uri"] = null
                }
            }

            CreateRecipeSharedLayout(currentStep = 4, uiState.recipeIdFromEdit != null) {
                InstructionsStep(
                    uiState = uiState,
                    onNewStepDescriptionChange = { viewModel.setNewStepDescription(it) },
                    onAddStepClick = { viewModel.addStep() },
                    onRemoveStepClick = { viewModel.removeStep(it) },
                    onNavigateToPictureDialog = {isFrontCamera -> onNavigateToPictureDialog(isFrontCamera)},
                    onSetPhotoTarget = viewModel::setPhotoTarget,
                    onNextClick = {
                        if (viewModel.validateTagsStep())
                        {
                            viewModel.saveRecipe()
                        }
                    },
                )
            }
        }
    }
}