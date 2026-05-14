package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.services.notifications.NotificationService
import com.example.tastetoshare.data.NotificationType
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.entities.Notification
import com.example.tastetoshare.data.entities.Recipe
import com.example.tastetoshare.data.entities.Review
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIReview
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.navigation.RecipeDetailsRoute
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.NotificationRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.TagRepository
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.map

class RecipeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationService: NotificationService
) : ViewModel() {


    private val _uiState = MutableStateFlow(RecipeDetailsUIState())
    val uiState = _uiState.asStateFlow()

    private val args = savedStateHandle.toRoute<RecipeDetailsRoute>()

    val focusReviewId: String? = args.focusReviewId
    val recipe = recipeRepository
        .getRecipeById(args.recipeId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Recipe()
        )

    fun showIngredients(isShowingIngredients: Boolean) {
        _uiState.update { it.copy(isShowingIngredients = isShowingIngredients) }
    }

    private val _selectedServings = MutableStateFlow<Int?>(null)
    val selectedServings: StateFlow<Int?> = _selectedServings.asStateFlow()

    fun increaseServings() {
        val currentServings = _selectedServings.value ?: recipe.value.servings
        if (currentServings < Int.MAX_VALUE)
            _selectedServings.value = currentServings + 1
    }

    fun decreaseServings() {
        val currentServings = _selectedServings.value ?: recipe.value.servings
        if (currentServings > 1)
            _selectedServings.value = currentServings - 1
    }

    fun deleteRecipe() {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe.value.recipeId)
        }
        hideDeleteRecipePopUp()
    }

    fun toggleRecipeTried() {
        viewModelScope.launch {
            if(isTried.value) {
                userRepository.deleteTriedRecipe(SessionManager.CURRENT_LOGGED_IN_USER_ID, recipe.value.recipeId)
            } else {
                userRepository.addTriedRecipe(SessionManager.CURRENT_LOGGED_IN_USER_ID, recipe.value.recipeId)
            }
        }
    }

    fun toggleRecipeSaved() {
        viewModelScope.launch {
            if(isSaved.value) {
                userRepository.deleteSavedRecipe(SessionManager.CURRENT_LOGGED_IN_USER_ID, recipe.value.recipeId)
            } else {
                userRepository.addSavedRecipe(SessionManager.CURRENT_LOGGED_IN_USER_ID, recipe.value.recipeId)
            }
        }
    }

    fun hideDeleteRecipePopUp() {
        _uiState.update { it.copy(isShowingDeleteRecipePopUp = false) }
    }

    fun showDeleteRecipePopUp() {
        _uiState.update { it.copy(isShowingDeleteRecipePopUp = true) }
    }

    val adjustedIngredients = recipe.combine(selectedServings) { recipe, servings ->
        val actualServings = servings ?: recipe.servings
        val factor = actualServings.toDouble() / recipe.servings.toDouble()

        recipe.ingredients.map {
            it.copy(quantity = (it.quantity * factor).toFloat())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val author: StateFlow<User> = recipe.flatMapLatest { recipe ->
        userRepository.getUser(recipe.authorId)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            User()
        )

    //the flatmap latest is a transformation (see slide 11 of flows)
    //That allows to get the latest element in the flow and update when the value changes
    @OptIn(ExperimentalCoroutinesApi::class)
    val loggedInUser: StateFlow<User> = recipe.flatMapLatest { recipe ->
        userRepository.getUser(SessionManager.CURRENT_LOGGED_IN_USER_ID)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            User()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val originalRecipe: StateFlow<Recipe> = recipe.flatMapLatest { recipe ->
        val id = recipe.originalRecipeId
        if (id != null) {
            recipeRepository.getRecipeById(id)
        } else {
            flowOf(Recipe())
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            Recipe()
        )

    val isTried = recipe.combine(loggedInUser) { recipe, loggedInUser ->
        recipe.recipeId in loggedInUser.triedRecipeIds
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isSaved = recipe.combine(loggedInUser) { recipe, loggedInUser ->
        recipe.recipeId in loggedInUser.savedRecipeIds
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews: StateFlow<List<UIReview>> =
        recipe
            .flatMapLatest { recipe ->
                if (recipe.reviews.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        recipe.reviews.map { review ->      //TODO move the mapping to a different file
                            userRepository.getUser(review.userId).map { user ->
                                UIReview(
                                    reviewId = review.reviewId,
                                    photosUrl = review.photosUrl,
                                    body = review.body,
                                    rating = review.rating,
                                    user = user
                                )
                            }
                        }
                    ) { it.toList() }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val userReview: StateFlow<UIReview?> = reviews.map { reviewList ->
        reviewList.find { it.user.userId == SessionManager.CURRENT_LOGGED_IN_USER_ID }
    }
        .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun onRatingChanged(rating: Int) {
        _uiState.update { it.copy(selectedRating = rating.toUInt()) }
    }

    fun onReviewTextChanged(text: String) {
        _uiState.update { it.copy(reviewText = text) }
    }

    fun addReview() {
        val currentRecipe = recipe.value
        val currentUiState = _uiState.value

        // rating mandatory, textual review optional
        if (currentUiState.selectedRating == 0U) return

        val review = Review(
            body = currentUiState.reviewText,
            rating = currentUiState.selectedRating.toInt(),
            userId = SessionManager.CURRENT_LOGGED_IN_USER_ID,
            photosUrl = if (currentUiState.reviewPhotoURL.isNotBlank()) listOf(currentUiState.reviewPhotoURL)
                        else emptyList()
        )

        viewModelScope.launch {
            recipeRepository.addReview(currentRecipe.recipeId, review)
            // To change by a firebase script

            // remove this if for testing on the logged account
            // (while we don't have firebase repository for notifications and can't test on 2 devices simultaneously)
            if (currentRecipe.authorId != SessionManager.CURRENT_LOGGED_IN_USER_ID)
            {
                val notification = Notification(
                    userId = currentRecipe.authorId,
                    recipeId = currentRecipe.recipeId,
                    type = NotificationType.NEW_REVIEW,
                    title = "New review on your recipe!",
                    message = "Someone left a review on your ${currentRecipe.title} recipe.",
                    targetId = review.reviewId
                )

                notificationRepository.addNotification(notification)
                notificationService.dispatchNotification(notification.toUi(currentRecipe))
            }
        }



        _uiState.update { it.copy(reviewText = "", selectedRating = 0U, reviewPhotoURL = "") }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            recipeRepository.deleteReview(recipe.value.recipeId, reviewId)
        }
        hideDeleteReviewPopUp()
    }

    fun hideDeleteReviewPopUp() {
        _uiState.update { it.copy(isShowingDeleteReviewPopUp = false, reviewIdDelete = "") }
    }

    fun showDeleteReviewPopUp(reviewId: String) {
        _uiState.update { it.copy(isShowingDeleteReviewPopUp = true, reviewIdDelete = reviewId) }
    }

    fun onReviewPhotoChanged(url: String) {
        _uiState.update { it.copy(reviewPhotoURL = url) }
    }

    fun setPhotoTarget(target: PhotoReviewTarget) {
        _uiState.update { it.copy(currentPhotoReviewTarget = target) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {

                    val application =
                        checkNotNull(extras[APPLICATION_KEY]) as TasteToShareApp

                    return RecipeDetailsViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        userRepository = application.container.userRepository,
                        notificationRepository = application.container.notificationRepository,
                        notificationService = NotificationService(application)
                    ) as T
                }
            }

    }
}

data class RecipeDetailsUIState(
    val isShowingIngredients: Boolean = true,
    val isShowingDeleteRecipePopUp: Boolean = false,
    val selectedRating: UInt = 0U,
    val reviewText: String = "",
    val isShowingDeleteReviewPopUp: Boolean = false,
    val reviewIdDelete: String = "",
    val reviewPhotoURL: String = "",
    val currentPhotoReviewTarget: PhotoReviewTarget = PhotoReviewTarget.REVIEW_PHOTO
)

enum class PhotoReviewTarget {
    REVIEW_PHOTO
}