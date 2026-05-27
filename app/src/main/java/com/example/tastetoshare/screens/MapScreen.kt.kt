package com.example.tastetoshare.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.viewmodels.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDestination(
    onNavigateToDetails: (recipeId: String) -> Unit,
    onAuthRequired: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
) {

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val cuisineTags by viewModel.cuisineTags.collectAsStateWithLifecycle()
    val dietaryTags by viewModel.dietaryTags.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }
    if(uiState.showBottomSheet){
        ModalBottomSheet(onDismissRequest = { viewModel.hideBottomSheet() }) {
            RecipeListScreen(
                recipes = recipes,
                ingredients = ingredients,
                cuisineTags = cuisineTags,
                dietaryTags = dietaryTags,
                searchText = uiState.searchText,
                selectedDifficulty = uiState.selectedDifficulty,
                selectedCost = uiState.selectedCost,
                searchIngredient = uiState.searchIngredient,
                selectedIngredients = uiState.selectedIngredients,
                selectedCuisine = uiState.selectedCuisine,
                selectedDietary = uiState.selectedDietary,
                onSearchTextChanged = viewModel::onSearchTextChanged,
                onDifficultySelected = viewModel::onDifficultySelected,
                onCostSelected = viewModel::onCostSelected,
                onSearchIngredientChanged = viewModel::onSearchIngredientChanged,
                onIngredientsSelected = viewModel::onIngredientsSelected,
                onCuisineSelected = viewModel::onCuisineSelected,
                onDietarySelected = viewModel::onDietarySelected,
                onNavigateToDetails = onNavigateToDetails,
                onToggleSaved = viewModel::onToggleSaved,
                onAuthRequired = onAuthRequired
            )
        }
    }


}
