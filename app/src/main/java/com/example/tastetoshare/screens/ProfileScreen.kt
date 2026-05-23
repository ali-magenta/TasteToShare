package com.example.tastetoshare.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.screens.components.ProfileHeader
import com.example.tastetoshare.screens.components.ProfileInfo
import com.example.tastetoshare.screens.components.RecipeCard
import com.example.tastetoshare.screens.components.TagsRow
import com.example.tastetoshare.viewmodels.ProfileUIState
import com.example.tastetoshare.viewmodels.ProfileViewModel

@Composable
fun ProfileDestination(
    onViewAllRecipes: (String) -> Unit,
    onNavigateToRecipeProposal: (String) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {

    val user by viewModel.user.collectAsStateWithLifecycle()
    val userRecipes by viewModel.userRecipes.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
            user = user,
            recipes = userRecipes,
            uiState = uiState,
            onNavigateToRecipeProposal = onNavigateToRecipeProposal,
            isOwnProfile = user.userId == SessionManagerFacade.currentUserId,
            edit = onNavigateToEditProfile,
            toggleMyRecipesAnalyticsTab = viewModel::toggleMyRecipesAnalyticsTab,
            onViewAllRecipes = {onViewAllRecipes(user.userId)},
            onToggleSaved = viewModel::onToggleSaved
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    recipes: List<UIRecipePreview>,
    uiState: ProfileUIState,
    onNavigateToRecipeProposal: (String) -> Unit,
    isOwnProfile: Boolean,
    edit: () -> Unit,
    toggleMyRecipesAnalyticsTab: (Boolean) -> Unit,
    onViewAllRecipes: () -> Unit,
    onToggleSaved: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isVertical = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 17.dp, end = 17.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item{
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (!isVertical){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileHeader(user, Modifier.size(160.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        ProfileInfo(user = user, alignment = Alignment.Start)
                    }
                } else {
                    ProfileHeader(user, Modifier.size(110.dp))
                    ProfileInfo(user)
                }
                TagsRow(user.preferences, MaterialTheme.typography.titleMedium)
                if (isOwnProfile)
                {
                    EditProfileButton(user, edit)
                }
            }
        }

        item{MyRecipesAnalyticsTab(uiState.isShowingMyRecipes, isOwnProfile, toggleMyRecipesAnalyticsTab)}
        if (isOwnProfile) {
            val selectedTabIndex = if (uiState.isShowingMyRecipes) 0 else 1

            when (selectedTabIndex) {
                0 -> {
                    // My recipes tab
                    if (recipes.isEmpty()) {
                        item{NoRecipes()}
                    } else {
                        item{
                            Row(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Text(
                                    modifier = Modifier.padding(0.dp).clickable {
                                        onViewAllRecipes()
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    text = "View All",
                                )
                            }
                        }
                        items(recipes.size) { index ->
                            RecipeCard(recipes[index], onNavigateToRecipeProposal, onToggleSaved)
                        }
                    }
                }

                1 -> {
                    // Analytics tab
                    item {
                        ProfileAnalytics(user)
                    }
                }
            }
        }
        else {
            if (recipes.isEmpty()) {
                item { NoRecipes() }
            } else {
                item{
                    Row(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier.padding(0.dp).clickable {
                                onViewAllRecipes()
                            },
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            text = "View All",
                        )
                    }
                }
                items(recipes.size) { index ->
                    RecipeCard(recipe= recipes[index], onNavigateToDetails= onNavigateToRecipeProposal, onToggleSaved)
                }
            }
        }
    }
}

@Composable
fun ProfileAnalytics(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row() {

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard(
                label = "Tried by Others",
                value = "${user.numberOfRecipeTriedByOthers}",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                label = "Average Rating",
                value = "${user.averageRating}",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                label = "Total Reviews",
                value = "${user.numberOfReviews}",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AnalyticsCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onTertiary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MyRecipesAnalyticsTab(isShowingMyRecipes: Boolean, isOwnProfile: Boolean, toggleMyRecipesAnalyticsTab: (Boolean) -> Unit){
    val tabs = if (isOwnProfile) {
        listOf("Recent Recipes", "Analytics")
    } else {
        listOf("Recent Recipes")
    }

    val selectedTabIndex = if (isOwnProfile) {
        if (isShowingMyRecipes) 0 else 1
    } else {
        0
    }
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                onClick = {
                    if (isOwnProfile)
                    {
                        toggleMyRecipesAnalyticsTab(index == 0)
                    }
                },
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

@Composable
fun EditProfileButton(x0: User, edit: () -> Unit) {
    Button(
        onClick = edit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Edit profile",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun NoRecipes() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = "You didn't create recipes yet...",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}