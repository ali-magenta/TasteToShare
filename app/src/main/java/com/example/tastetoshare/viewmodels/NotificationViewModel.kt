package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.NotificationType
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.Notification
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.repositories.NotificationRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.services.notifications.NotificationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    savedStateHandle: SavedStateHandle,
    private val notificationRepository: NotificationRepository,
    private val recipeRepository: RecipeRepository,
    private val notificationService: NotificationService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUIState())
    val uiState: StateFlow<NotificationUIState> = _uiState.asStateFlow()

    private val currentUserId: StateFlow<String?> = SessionManagerFacade.currentUserStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionManagerFacade.currentUserStateFlow.value
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notifications: StateFlow<List<UINotification>> =
        currentUserId
            .flatMapLatest { userId ->
                if (userId != null) notificationRepository.getNotifications(userId = userId)
                else flowOf(emptyList())
            }
            .flatMapLatest { notifications ->
                val flows = notifications.map { notification ->
                    recipeRepository.getRecipeById(notification.recipeId)
                        .map { recipe ->
                            notification.toUi(recipe)
                        }
                }
                combine(flows) {    it.toList() }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun sendRecommendationNotification() {
        val userId = SessionManagerFacade.currentUserStateFlow.value
        if (userId.isNullOrBlank()) return
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById("100").first()
            val notification = Notification(
                notificationId = "fakeRecommendation",
                creationDate = System.currentTimeMillis(),
                userId = userId,
                recipeId = recipe.recipeId,
                type = NotificationType.RECOMMENDED_RECIPE,
                title = "Some recommendation for you!",
                message = "Based on your preferences, you might love ${recipe.title}!",
            )
            notificationService.dispatchNotification(notification.toUi(recipe))
        }
    }

    fun sendReviewNotification() {
        val userId = SessionManagerFacade.currentUserStateFlow.value
        if (userId.isNullOrBlank()) return
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById("100").first()
            val notification = Notification(
                notificationId = "fakeReview",
                creationDate = System.currentTimeMillis(),
                userId = userId,
                recipeId = recipe.recipeId,
                reviewId = recipe.reviews.first().reviewId,
                type = NotificationType.NEW_REVIEW,
                title = "New Review on your recipe!",
                message = "Someone left a review on your ${recipe.title}."
            )

            notificationService.dispatchNotification(notification.toUi(recipe))
        }
    }

    fun sendDuplicateNotification() {
        val userId = SessionManagerFacade.currentUserStateFlow.value
        if (userId.isNullOrBlank()) return
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById("101").first()
            val notification = Notification(
                notificationId = "fakeDuplication",
                creationDate = System.currentTimeMillis(),
                userId = userId,
                recipeId = recipe.recipeId,
                type = NotificationType.DUPLICATED_RECIPE,
                title = "Your recipe was duplicated!",
                message = "A chef was inspired by your ${recipe.title} and created their own version."
            )

            notificationService.dispatchNotification(notification.toUi(recipe))
        }
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

                    return NotificationViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        notificationRepository = application.container.notificationRepository,
                        notificationService = NotificationService(application)
                    ) as T
                }
            }

    }
}

data class NotificationUIState(
    val temp: String = ""
)