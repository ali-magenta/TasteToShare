package com.example.tastetoshare.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.R
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.screens.components.RecipeCard
import com.example.tastetoshare.screens.components.RecipePreviewCardVertical
import com.example.tastetoshare.viewmodels.HomepageUIState
import com.example.tastetoshare.viewmodels.HomepageViewModel

@Composable
fun HomepageDestination(
    viewModel: HomepageViewModel = viewModel(factory = HomepageViewModel.Factory),
    onNavigateToNotifications: () -> Unit,
    onNavigateToRecipeProposal: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    onAuthRequired: () -> Unit,
    bottomPadding: Dp = 0.dp
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topWeeklyRecipes by viewModel.topWeeklyRecipes.collectAsStateWithLifecycle()
    val topMonthlyRecipes by viewModel.topMonthlyRecipes.collectAsStateWithLifecycle()
    val yourTopRecipes by viewModel.yourTopRecipes.collectAsStateWithLifecycle()

    HomepageScreen(
        uiState = uiState,
        topWeeklyRecipes,
        topMonthlyRecipes,
        yourTopRecipes,
        onSearchSubmit,
        viewModel::onSearchTextChanged,
        onNavigateToNotifications,
        onNavigateToMap = onNavigateToMap,
        onNavigateToRecipeProposal,
        viewModel::onToggleSaved,
        onAuthRequired,
        bottomPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomepageScreen(
    uiState: HomepageUIState,
    topWeeklyRecipes: List<UIRecipePreview>,
    topMonthlyRecipes: List<UIRecipePreview>,
    yourTopRecipes: List<UIRecipePreview>,
    onSearchSubmit: (String) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToRecipeProposal: (String) -> Unit,
    onSaveClick: (String, () -> Unit) -> Unit,
    onAuthRequired: () -> Unit,
    bottomPadding: Dp = 0.dp
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp, start = 17.dp, end = 17.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(4f)) {
                    SearchRecipeBar(uiState.searchText, onSearchTextChanged, onSearchSubmit)
                }

                IconButton(
                    onClick = {onNavigateToMap()},
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        Icons.Filled.Map,
                        contentDescription = "Map Button",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        item {
            Text(
                text = "Recommended Recipes",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(yourTopRecipes) { recipe ->
                    RecipePreviewCardVertical(
                        recipe,
                        onNavigateToRecipeProposal,
                        onSaveClick,
                        onAuthRequired,
                        Modifier.width(220.dp)
                    )
                }
            }
        }
        item {
            Text(
                text = "Top Weekly Recipes",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        items(topWeeklyRecipes) { recipe ->
            RecipeCard(recipe, onNavigateToRecipeProposal, onSaveClick, onAuthRequired)
        }
    }
}