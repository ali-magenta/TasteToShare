package com.example.tastetoshare.screens

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Country
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.viewmodels.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun MapDestination(
    onNavigateToDetails: (recipeId: String) -> Unit,
    onAuthRequired: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
    bottomPadding: Dp = 0.dp
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
        selectedCountry = uiState.selectedCountry,
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
        openBottomSheet = viewModel::openBottomSheet,
        topRecipesURLsByCountries = topRecipesURLsByCountries,
        bottomPadding = bottomPadding
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
    selectedCountry: Tag?,
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
    topRecipesURLsByCountries: Map<Pair<Tag, Country?>, List<String>>,
    openBottomSheet: (Tag) -> Unit,
    bottomPadding: Dp = 0.dp
){
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

        topRecipesURLsByCountries.entries.forEach {
            if(it.key.second != null){
                RecipesMarker(
                    LatLng(it.key.second?.lat?:0.0,
                        it.key.second?.lng?:0.0),
                    it.key.second?.size?:0.1,
                    it.value,
                    cameraPositionState,
                ) { openBottomSheet(it.key.first) }
            }
        }
    }
    if(showBottomSheet){
        ModalBottomSheet(onDismissRequest = { hideBottomSheet() }) {
            Text(
                text ="Recipes from ${selectedCountry?.name?:""}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
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
                onAuthRequired = onAuthRequired,
                bottomPadding = bottomPadding
            )
        }
    }
}


@OptIn(ExperimentalGridApi::class)
@Composable
fun RecipesMarker(
    location: LatLng,
    countrySize: Double,
    urls: List<String>,
    cameraPositionState: CameraPositionState,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val markerState = remember { MarkerState(position = location) }
    var bitmaps by remember { mutableStateOf<List<ImageBitmap>>(emptyList()) }

    LaunchedEffect(urls) {
        val loader = ImageLoader(context)
        urls.forEach {
            val request = ImageRequest.Builder(context)
                .data(it)
                .size(128, 128)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap?.asImageBitmap()
            if(bitmap != null){
                bitmaps = bitmaps + bitmap
            }
        }
    }

    val currentZoom:Double = cameraPositionState.position.zoom.toDouble()

    val targetSize = when {
        currentZoom >= 15f -> 72.dp
        currentZoom >= 12f -> 56.dp
        currentZoom >= 10f -> 40.dp
        currentZoom >= 5f -> 50.dp
        currentZoom >= 4f -> 40.dp
        currentZoom >= 3f -> 30.dp
        else        -> 10.dp
    }



    val density = LocalDensity.current
    val visibleRegion = cameraPositionState.projection?.visibleRegion

    var distanceDp = 1.dp
    var distanceKm = 1.0

    if(visibleRegion != null){

        distanceKm = (4000/2.0.pow(currentZoom))*10.0

        val farLeftPoint = cameraPositionState.projection?.toScreenLocation(visibleRegion.farLeft)
        val centerPoint = cameraPositionState.projection?.toScreenLocation(cameraPositionState.position.target)

        if (farLeftPoint != null && centerPoint != null){
            val dx = (farLeftPoint.x - centerPoint.x).toDouble()
            val dy = (farLeftPoint.y - centerPoint.y).toDouble()
            val distancePx = sqrt(dx * dx + dy * dy)
            distanceDp = with(density) { distancePx.toFloat().toDp() }
        }
    }

    val dpMetersRatio = distanceKm/(distanceDp/1.dp)
    val proposedNbPerRow = ((countrySize*2)/((targetSize/1.dp)*dpMetersRatio)).toInt()

    var sizeSqrt = sqrt(urls.size.toDouble()).toInt()
    if(urls.size != sizeSqrt * sizeSqrt){
        sizeSqrt = sizeSqrt + 1
    }


    val maxNbOfRecipesShownPerRow = min(min(proposedNbPerRow, sizeSqrt), 5)
    if(maxNbOfRecipesShownPerRow > 0){
        MarkerComposable(
            state = markerState,
            keys = arrayOf(bitmaps, targetSize, maxNbOfRecipesShownPerRow),
            anchor = Offset(0.5f, 0.5f),
            onClick = { onClick()
                true }
        ) {
            Grid(
                config = {
                    repeat(maxNbOfRecipesShownPerRow) {
                        column(targetSize)
                    }
                    repeat(maxNbOfRecipesShownPerRow) {
                        row(targetSize)
                    }
                }
            ) {
                bitmaps.forEachIndexed { index, bitmap ->
                    if (index < maxNbOfRecipesShownPerRow*maxNbOfRecipesShownPerRow){
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}