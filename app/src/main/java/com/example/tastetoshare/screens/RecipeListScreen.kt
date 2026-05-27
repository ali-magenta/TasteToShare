package com.example.tastetoshare.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.screens.recipeListComponents.FilterChipDropdown
import com.example.tastetoshare.screens.recipeListComponents.FilterChipIngredients
import com.example.tastetoshare.screens.recipeListComponents.RecipeListCard
import com.example.tastetoshare.viewmodels.RecipeListUIState
import com.example.tastetoshare.viewmodels.RecipeListViewModel

@Composable
fun RecipeListDestination(
    onNavigateToDetails: (recipeId: String) -> Unit,
    onAuthRequired: () -> Unit,
    viewModel: RecipeListViewModel = viewModel(factory = RecipeListViewModel.Factory)
) {

    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val cuisineTags by viewModel.cuisineTags.collectAsStateWithLifecycle()
    val dietaryTags by viewModel.dietaryTags.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
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
    onAuthRequired: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 17.dp)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SearchRecipeBar(searchText, onSearchTextChanged)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        FilterRow(
                            selectedDifficulty = selectedDifficulty,
                            onDifficultySelected = onDifficultySelected,
                            selectedCost = selectedCost,
                            onCostSelected = onCostSelected,
                            ingredients = ingredients,
                            searchIngredient = searchIngredient,
                            selectedIngredients = selectedIngredients,
                            onSearchIngredientChanged = onSearchIngredientChanged,
                            onIngredientsSelected = onIngredientsSelected,
                            cuisineTags = cuisineTags,
                            dietaryTags = dietaryTags,
                            onCuisineSelected = onCuisineSelected,
                            onDietarySelected = onDietarySelected,
                            selectedCuisine = selectedCuisine,
                            selectedDietary = selectedDietary
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (recipes.size == 1) "1 recipe found"
                            else "${recipes.size} recipes found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(recipes) { recipe ->
                Box(
                    Modifier
                        .padding(start = 17.dp, end = 16.dp)
                        .clickable { onNavigateToDetails(recipe.recipeId) }
                        .animateItem()
                ) {
                    RecipeListCard(recipe, onToggleSaved, null, onAuthRequired)
                }
            }

            // extra spacer to avoid cutting the last element
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipeBar(searchText: String, onSearchRecipe: (String) -> Unit, onSearchSubmit: ((String) -> Unit)? = null) {

    val focusRequester = remember { FocusRequester() }
    var textFieldLoaded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchRecipe,
        label = { Text("Search recipes") },
        shape = RoundedCornerShape(50),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = { onSearchRecipe("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (onSearchSubmit != null) {
                onSearchSubmit(searchText)
                focusManager.clearFocus()
            } else {
                onSearchRecipe(searchText)
                focusManager.clearFocus()
            }
        }),
        enabled = onSearchSubmit == null,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth().padding(10.dp)
            .focusRequester(focusRequester)
            .onGloballyPositioned {
                if (!textFieldLoaded) {
                    focusRequester.requestFocus()
                    textFieldLoaded = true
                }
            }
            .clickable{
            if (onSearchSubmit != null) {
                onSearchSubmit(searchText)
            }
        }
        )
}

@Composable
fun FilterRow(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    selectedCost: Cost?,
    onCostSelected: (Cost?) -> Unit,
    ingredients: List<Ingredient>,
    searchIngredient: String,
    selectedIngredients: List<Ingredient>,
    onSearchIngredientChanged: (String) -> Unit,
    onIngredientsSelected: (List<Ingredient>) -> Unit,
    cuisineTags: List<Tag>,
    dietaryTags: List<Tag>,
    onCuisineSelected: (Tag?) -> Unit,
    onDietarySelected: (Tag?) -> Unit,
    selectedCuisine: Tag?,
    selectedDietary: Tag?
) {
    // ingredients chip
    FilterChipIngredients(
        ingredients = ingredients,
        searchIngredient = searchIngredient,
        selectedIngredients = selectedIngredients,
        onSearchIngredientChanged = onSearchIngredientChanged,
        onIngredientsSelected = onIngredientsSelected
    )

    // difficulty chip
    FilterChipDropdown(
        label = "Difficulty",
        selectedOption = selectedDifficulty,
        options = Difficulty.entries,
        optionLabel = { it.displayName},
        onOptionSelected = onDifficultySelected
    )

    // cost range chip
    FilterChipDropdown(
        label = "Cost",
        selectedOption = selectedCost,
        options = Cost.entries,
        optionLabel = { it.displayName},
        onOptionSelected = onCostSelected
    )

    // cuisine chip
    FilterChipDropdown(
        label = "Cuisine",
        selectedOption = selectedCuisine,
        options = cuisineTags,
        optionLabel = { it.name},
        onOptionSelected = onCuisineSelected
    )

    // dietary restrictions chip
    FilterChipDropdown(
        label = "Dietary Restrictions",
        selectedOption = selectedDietary,
        options = dietaryTags,
        optionLabel = { it.name},
        onOptionSelected = onDietarySelected
    )
}