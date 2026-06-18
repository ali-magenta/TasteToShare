package com.example.tastetoshare.screens

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.viewmodels.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
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
    recipes: List<UIRecipePreview>,
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
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val scope = rememberCoroutineScope()

    val fallbackPosition = LatLng(48.0, 14.0) // Center of europe
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackPosition, 4.5f)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        Log.d("MapScreen", "User location : ${userLatLng}")
                        scope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLatLng, 4.5f)
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapScreen", "Security error : ", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
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
        val loadedBitmaps = urls.map { url ->
            async {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(128, 128)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                (result as? BitmapDrawable)?.bitmap?.asImageBitmap()
            }
        }.awaitAll().filterNotNull()

        bitmaps = loadedBitmaps
    }


    val targetSize = 48.dp

    val density = LocalDensity.current
    val maxNbOfRecipesShownPerRow by remember {
        derivedStateOf {
            // compute on change of these states
            val currentZoom = cameraPositionState.position.zoom.toDouble()
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

            val dpMetersRatio = distanceKm/(distanceDp/1.dp).coerceAtLeast(0.01f)
            val proposedNbPerRow = ((countrySize*2)/((targetSize/1.dp)*dpMetersRatio)).toInt()

            var sizeSqrt = sqrt(urls.size.toDouble()).toInt()
            if(urls.size != sizeSqrt * sizeSqrt){
                sizeSqrt = sizeSqrt + 1
            }
            min(min(proposedNbPerRow, sizeSqrt), 5) // UI get updated only when this value change
        }
    }

    if(maxNbOfRecipesShownPerRow > 0 && bitmaps.isNotEmpty()){
        MarkerComposable(
            state = markerState,
            keys = arrayOf(bitmaps, maxNbOfRecipesShownPerRow),
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