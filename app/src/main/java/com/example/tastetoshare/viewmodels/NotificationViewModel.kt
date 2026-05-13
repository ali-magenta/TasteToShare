package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.uiClasses.UINotification
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.repositories.NotificationRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotificationViewModel(
    savedStateHandle: SavedStateHandle,
    private val notificationRepository: NotificationRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUIState())
    val uiState: StateFlow<NotificationUIState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(SessionManager.CURRENT_LOGGED_IN_USER_ID)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val notifications: StateFlow<List<UINotification>> =
        _userId
            .flatMapLatest { userId ->
                notificationRepository.getNotifications(userId = userId)
            }
            .flatMapLatest { notifications ->
                val flows = notifications.map { notification ->
                    recipeRepository.getRecipeById(notification.recipeId)
                        .map { recipe ->
                            notification.toUi(recipe)
                        }
                }
                combine(flows) { it.toList() }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

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
                        notificationRepository = application.container.notificationRepository
                    ) as T
                }
            }

    }
}

data class NotificationUIState(
    val temp: String = ""
)