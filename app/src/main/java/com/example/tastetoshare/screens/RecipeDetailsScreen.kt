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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastetoshare.R
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.RecipeIngredient
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIReview
import com.example.tastetoshare.screens.components.AuthorInfo
import com.example.tastetoshare.screens.components.CopiedImageIcon
import com.example.tastetoshare.screens.components.DeletePopup
import com.example.tastetoshare.screens.components.DeleteReviewPopup
import com.example.tastetoshare.screens.components.IngredientRow
import com.example.tastetoshare.screens.components.RecipeHeader
import com.example.tastetoshare.screens.components.RecipeInfoCard
import com.example.tastetoshare.screens.components.Reviews
import com.example.tastetoshare.screens.components.ServingsField
import com.example.tastetoshare.screens.components.Step
import com.example.tastetoshare.screens.components.TagsRow
import com.example.tastetoshare.ui.theme.DangerRed
import com.example.tastetoshare.viewmodels.RecipeDetailsUIState
import com.example.tastetoshare.viewmodels.RecipeDetailsViewModel
import com.example.tastetoshare.viewmodels.PhotoReviewTarget
import kotlin.String
import kotlin.math.roundToInt


@Composable
fun RecipeDetailsDestination(
    onNavigateToProfile: (userId: String) -> Unit,
    onNavigateToRecipeDetails: (recipeId: String) -> Unit,
    onNavigateToCreateRecipePage: (recipeIdForClone: String?, recipeIdForEdit: String?) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    onAuthRequired: () -> Unit,
    // onNavigateToShoppingList: () -> Unit,
    viewModel: RecipeDetailsViewModel = viewModel(factory = RecipeDetailsViewModel.Factory),
    bottomPadding: Dp = 0.dp
)
{


    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val originalRecipe by viewModel.originalRecipe.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val isTried by viewModel.isTried.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val adjustedIngredients by viewModel.adjustedIngredients.collectAsStateWithLifecycle()
    val selectedServings by viewModel.selectedServings.collectAsStateWithLifecycle()
    val author by viewModel.author.collectAsStateWithLifecycle()
    val userReview by viewModel.userReview.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusReviewId = viewModel.focusReviewId

    RecipeDetailsScreen(
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToRecipeDetails = onNavigateToRecipeDetails,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCreateRecipePage = onNavigateToCreateRecipePage,
        recipe = recipe,
        reviews = reviews,
        originalRecipe = originalRecipe,
        isTried = isTried,
        isSaved = isSaved,
        adjustedIngredients = adjustedIngredients,
        selectedServings = selectedServings,
        author = author,
        uiState = uiState,
        onShowIngredients = viewModel::showIngredients,
        onIncreaseServingClicked = viewModel::increaseServings,
        onDecreaseServingClicked = viewModel::decreaseServings,
        deleteRecipe = viewModel::deleteRecipe,
        toggleTriedRecipe = viewModel::toggleRecipeTried,
        toggleSavedRecipe = viewModel::toggleRecipeSaved,
        hideDeleteRecipePopUp = viewModel::hideDeleteRecipePopUp,
        showDeleteRecipePopUp = viewModel::showDeleteRecipePopUp,
        onRatingChanged = viewModel::onRatingChanged,
        onReviewTextChanged = viewModel::onReviewTextChanged,
        onAddReview = viewModel::addReview,
        userReview = userReview,
        onDeleteReview = viewModel::deleteReview,
        hideDeleteReviewPopup = viewModel::hideDeleteReviewPopUp,
        showDeleteReviewPopup = viewModel::showDeleteReviewPopUp,
        onSetPhotoTarget = viewModel::setPhotoTarget,
        onNavigateToPictureDialog = onNavigateToPictureDialog,
        onAuthRequired = onAuthRequired,
        focusReviewId = focusReviewId,
        bottomPadding = bottomPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    onNavigateToProfile: (userId: String) -> Unit,
    onNavigateToRecipeDetails: (recipeId: String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCreateRecipePage: (recipeIdForClone: String?, recipeIdForEdit: String?) -> Unit,
    recipe: Recipe,
    reviews: List<UIReview>,
    originalRecipe: Recipe,
    isTried: Boolean,
    isSaved: Boolean,
    adjustedIngredients: List<RecipeIngredient>,
    selectedServings: Int?,
    author: User,
    uiState: RecipeDetailsUIState,
    onShowIngredients: (Boolean) -> Unit,
    onIncreaseServingClicked: () -> Unit,
    onDecreaseServingClicked: () -> Unit,
    deleteRecipe: () -> Unit,
    toggleTriedRecipe: (() -> Unit) -> Unit,
    toggleSavedRecipe: (() -> Unit) -> Unit,
    hideDeleteRecipePopUp: () -> Unit,
    showDeleteRecipePopUp: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    onReviewTextChanged: (String) -> Unit,
    onAddReview: (() -> Unit) -> Unit,
    userReview: UIReview?,
    onDeleteReview: (String) -> Unit,
    hideDeleteReviewPopup: () -> Unit,
    showDeleteReviewPopup: (String) -> Unit,
    onSetPhotoTarget: (PhotoReviewTarget) -> Unit,
    onNavigateToPictureDialog: (Boolean) -> Unit,
    onAuthRequired: () -> Unit,
    focusReviewId: String? = null,
    bottomPadding: Dp = 0.dp
) {
    val verticalScrollState = rememberScrollState()
    var reviewsSectionY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(focusReviewId, reviewsSectionY) {
        if (focusReviewId != null && reviewsSectionY > 0f) {
            verticalScrollState.animateScrollTo(reviewsSectionY.roundToInt())
        }
    }

    Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecipeHeader(recipe = recipe, isSaved, toggleSavedRecipe, onAuthRequired)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AuthorInfo(
                    author = author, modifier = Modifier.clickable(onClick = {
                        onNavigateToProfile(author.userId)
                    })
                )
                if (recipe.originalRecipeId != null) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable(onClick = {
                            onNavigateToRecipeDetails(originalRecipe.recipeId)
                        })
                    ) {
                        Text(
                            text = "Cloned From",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        CopiedImageIcon(originalRecipe.photoURL, Modifier.size(56.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button( //Duplicate button
                    onClick = {
                        onNavigateToCreateRecipePage(recipe.recipeId, null)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.duplicate),
                        contentDescription = "Clone icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Clone",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                if (uiState.currentUserId != recipe.authorId) {
                    Button( //Tried button
                        onClick = {
                            toggleTriedRecipe(onAuthRequired)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTried)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.primary,
                            contentColor = if (isTried)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = if (!isTried) "Mark as tried" else "Already tried!",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    Button( //Edit button
                        onClick = {
                            onNavigateToCreateRecipePage(null, recipe.recipeId)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button( //Delete button
                        onClick = {
                            showDeleteRecipePopUp()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RecipeInfoCard(
                    icon = R.drawable.difficulty,
                    label = "Difficulty",
                    value = recipe.difficulty.displayName,
                    modifier = Modifier.weight(1f)
                )
                var time = ""
                val initialHour = recipe.preparationTime.div(60) ?: 0
                val initialMinute = recipe.preparationTime.rem(60) ?: 0
                if (initialHour > 0) {
                    time += "${initialHour}h "
                }
                if (initialMinute > 0) {
                    time += "${initialMinute}min"
                }
                RecipeInfoCard(
                    icon = R.drawable.time,
                    label = "Time",
                    value = time,
                    modifier = Modifier.weight(1f)
                )
                RecipeInfoCard(
                    icon = R.drawable.cost,
                    label = "Cost",
                    value = recipe.cost.displayName,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            TagsRow(recipe.tags, MaterialTheme.typography.titleMedium)


            val selectedTabIndex = if (uiState.isShowingIngredients) 0 else 1
            val tabs = listOf("Ingredients", "Steps")

            Column(modifier = Modifier.fillMaxSize()) {
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
                            onClick = { onShowIngredients(index == 0) },
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
                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> {
                        Column() {
                            ServingsField(
                                servings = recipe.servings,
                                selectedServings = selectedServings,
                                recipe.calories,
                                onIncreaseServingClicked = onIncreaseServingClicked,
                                onDecreaseServingClicked = onDecreaseServingClicked
                            )
                            adjustedIngredients.forEach { ingredient ->
                                Spacer(modifier = Modifier.height(5.dp))
                                IngredientRow(ingredient)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    //TODO add ingredients to shopping list
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cart),
                                        contentDescription = "Shopping icon",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Add all to Shopping List",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        Column() {
                            recipe.steps.forEach { step ->
                                Spacer(modifier = Modifier.height(5.dp))
                                Step(step)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    reviewsSectionY = coordinates.positionInParent().y
                }
        ) {
            Reviews(
                reviews = reviews,
                rating = uiState.selectedRating,
                reviewText = uiState.reviewText,
                onRatingChanged = onRatingChanged,
                onReviewTextChanged = onReviewTextChanged,
                onAddReview = onAddReview,
                userReview = userReview,
                showDeleteReviewPopup = showDeleteReviewPopup,
                reviewPhotoUrl = uiState.reviewPhotoURL,
                onSetPhotoReviewTarget = onSetPhotoTarget,
                onNavigateToPictureDialog = onNavigateToPictureDialog,
                focusReviewId = focusReviewId,
                isSavingReview = uiState.isSavingReview,
                onAuthRequired = onAuthRequired,
                loggedUserId = uiState.currentUserId
            )
        }

        Spacer(modifier = Modifier.height(bottomPadding + 16.dp))
    }

    if (uiState.isShowingDeleteRecipePopUp) {
        DeletePopup("Recipe", onNavigateToHome, deleteRecipe, hideDeleteRecipePopUp)
    }

    if(uiState.isShowingDeleteReviewPopUp) {
        DeleteReviewPopup({onDeleteReview(uiState.reviewIdDelete)}, hideDeleteReviewPopup)
    }

    if (uiState.isSavingReview) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Saving review...")
                }
            }
        }
    }
}