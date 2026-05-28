package com.example.tastetoshare.screens

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.viewmodels.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState



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
    val topRecipesURLsByCountries by viewModel.topRecipesURLsByCountries.collectAsStateWithLifecycle()


    MapScreen(
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
        onAuthRequired = onAuthRequired,
        showBottomSheet = uiState.showBottomSheet,
        hideBottomSheet = viewModel::hideBottomSheet,
        topRecipesURLsByCountries = topRecipesURLsByCountries
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    recipes: List<RawRecipePreview>,
    ingredients: List<Ingredient>,
    cuisineTags: List<Tag>,
    dietaryTags: List<Tag>,
    searchText: String,
    selectedDifficulty: Difficulty?,
    selectedCost: Cost?,
    searchIngredient: String,
    selectedIngredients: List<Ingredient>,
    selectedCuisine: Tag?,
    selectedDietary: Tag?,
    onSearchTextChanged: (String) -> Unit,
    onDifficultySelected: (Difficulty?) -> Unit,
    onCostSelected: (Cost?) -> Unit,
    onSearchIngredientChanged: (String) -> Unit,
    onIngredientsSelected: (List<Ingredient>) -> Unit,
    onCuisineSelected: (Tag?) -> Unit,
    onDietarySelected: (Tag?) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onToggleSaved: (String, () -> Unit) -> Unit,
    onAuthRequired: () -> Unit,
    showBottomSheet: Boolean,
    hideBottomSheet: () -> Unit,
    topRecipesURLsByCountries: Map<Tag, List<String>>
){
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        RecipesMarker(singapore, topRecipesURLsByCountries.values.firstOrNull()?.firstOrNull()?: "")
        Log.d("DEBUG", "$topRecipesURLsByCountries")
    }
    if(showBottomSheet){
        ModalBottomSheet(onDismissRequest = { hideBottomSheet() }) {
            RecipeListScreen(
                recipes = recipes,
                ingredients = ingredients,
                cuisineTags = cuisineTags,
                dietaryTags = dietaryTags,
                searchText = searchText,
                selectedDifficulty = selectedDifficulty,
                selectedCost = selectedCost,
                searchIngredient = searchIngredient,
                selectedIngredients = selectedIngredients,
                selectedCuisine = selectedCuisine,
                selectedDietary = selectedDietary,
                onSearchTextChanged = onSearchTextChanged,
                onDifficultySelected = onDifficultySelected,
                onCostSelected = onCostSelected,
                onSearchIngredientChanged = onSearchIngredientChanged,
                onIngredientsSelected = onIngredientsSelected,
                onCuisineSelected = onCuisineSelected,
                onDietarySelected = onDietarySelected,
                onNavigateToDetails = onNavigateToDetails,
                onToggleSaved = onToggleSaved,
                onAuthRequired = onAuthRequired
            )
        }
    }
}


@Composable
fun RecipesMarker(
    location: LatLng,
    urls: List<String>,
) {
    val context = LocalContext.current
    val markerState = remember { MarkerState(position = location) }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(urls) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(urls)
            .size(128, 128)
            .allowHardware(false)
            .build()

        val result = (loader.execute(request) as? SuccessResult)?.drawable
        bitmap = (result as? BitmapDrawable)?.bitmap?.asImageBitmap()
    }

    bitmap?.let { bmp ->
        MarkerComposable(
            state = markerState,
            keys = arrayOf(bmp),
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}