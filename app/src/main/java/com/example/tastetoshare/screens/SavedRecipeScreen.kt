package com.example.tastetoshare.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.screens.recipeListComponents.RecipeListCard
import com.example.tastetoshare.ui.theme.DangerRed
import com.example.tastetoshare.viewmodels.SavedTriedUIState
import com.example.tastetoshare.viewmodels.SavedTriedViewModel

@Composable
fun SavedRecipeDestination(
    onNavigateToRecipeDetails: (recipeId: String) -> Unit,
    onAuthRequired: () -> Unit,
    viewModel: SavedTriedViewModel = viewModel(factory = SavedTriedViewModel.Factory),
    bottomPadding: Dp = 0.dp
) {

    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()
    val triedRecipes by viewModel.triedRecipes.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedTriedScreen(
        savedRecipes = savedRecipes,
        triedRecipes = triedRecipes,
        uiState = uiState,
        onSearchTextChanged = viewModel::onSearchTextChanged,
        onTabSelected = viewModel::onTabSelected,
        onNavigateToDetails = onNavigateToRecipeDetails,
        onToggleSaved = viewModel::onToggleSaved,
        hideUnsavePopUp = viewModel::hideUnsavePopUp,
        showUnsavePopUp = viewModel::showUnsavePopUp,
        onAuthRequired = onAuthRequired,
        bottomPadding = bottomPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTriedScreen(
    savedRecipes: List<RawRecipePreview>,
    triedRecipes: List<RawRecipePreview>,
    uiState: SavedTriedUIState,
    onSearchTextChanged: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onToggleSaved: (String, () -> Unit) -> Unit,
    onTabSelected: (Int) -> Unit,
    hideUnsavePopUp: () -> Unit,
    showUnsavePopUp: (String) -> Unit,
    onAuthRequired: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    val tabs = listOf("Saved", "Tried")
    val currentRecipes = if (uiState.selectedTabIndex == 0) savedRecipes else triedRecipes

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
        ) {
            item {
                TabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = uiState.selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { onTabSelected(index) },
                            text = {
                                Text(
                                    text = title,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 17.dp)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SearchSavedRecipeBar(uiState.searchText, onSearchTextChanged)
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (currentRecipes.size == 1) "1 recipe found"
                                else "${currentRecipes.size} recipes found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (currentRecipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = if (uiState.selectedTabIndex == 0) "No saved recipes yet..."
                            else "No tried recipes yet...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {

                items(currentRecipes) { recipe ->
                    Box(
                        Modifier
                            .padding(start = 17.dp, end = 16.dp)
                            .clickable { onNavigateToDetails(recipe.recipeId) }
                            .animateItem()
                    ) {
                        RecipeListCard(recipe, onToggleSaved, {showUnsavePopUp(recipe.recipeId)}, onAuthRequired)
                    }
                }
            }
        }

        if (uiState.isShowingUnsavePopUp) {
            UnsavePopup(onToggleSaved, hideUnsavePopUp, uiState.recipeToUnsave, onAuthRequired)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSavedRecipeBar(searchText: String, onSearchRecipe: (String) -> Unit) {
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
        keyboardActions = KeyboardActions(onSearch = { onSearchRecipe(searchText)}),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
}

@Composable
fun UnsavePopup(
    unsaveRecipe: (String, () -> Unit) -> Unit,
    hideUnsavePopUp: () -> Unit,
    recipeId: String,
    onAuthRequired: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center

    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
        )

        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp)

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally ,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
                //.fillMaxSize()
            ) {
                Text(
                    text = "Are you sure you want to remove this recipe from your saved?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1F),
                        onClick = {
                            hideUnsavePopUp()
                        },
                    ) {
                        Text("No")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1F),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = {
                            unsaveRecipe(recipeId, onAuthRequired)
                        },
                    ) {
                        Text("Yes")
                    }
                }

            }
        }
    }

}