package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.repositories.RecipeRepository
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

class HomepageViewModel (
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomepageUIState())
    val uiState: StateFlow<HomepageUIState> = _uiState.asStateFlow()

    fun onSearchTextChanged(search: String) {
        _uiState.update { it.copy(searchText = search) }
    }

    private val currentUserId: StateFlow<String?> = SessionManagerFacade.currentUserStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionManagerFacade.currentUserStateFlow.value
        )

    private val isLoggedIn: StateFlow<Boolean> = SessionManagerFacade.currentUserStateFlow
        .map {it != null}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SessionManagerFacade.currentUserStateFlow.value != null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val loggedInUser: StateFlow<User> =
        currentUserId.flatMapLatest{ userId ->
            if (userId != null) userRepository.getUser(userId)
            else flowOf(User())
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = User()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val topWeeklyRecipes: StateFlow<List<UIRecipePreview>> =
        recipeRepository.getTopWeeklyRecipesPreview()
            .combine(loggedInUser) {
                recipes, loggedInUser ->
                    recipes.map { it.toUi(loggedInUser) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val topMonthlyRecipes: StateFlow<List<UIRecipePreview>> =
        recipeRepository.getTopMonthlyRecipesPreview()
            .combine(loggedInUser) {
                recipes, loggedInUser ->
                    recipes.map { it.toUi(loggedInUser) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val yourTopRecipes: StateFlow<List<UIRecipePreview>> =
        loggedInUser
            .flatMapLatest { user ->
                recipeRepository.getFilteredTopWeeklyRecipesPreview(user.preferences, loggedInUser.value.userId)
                    .map { recipes ->
                        recipes.map { it.toUi(user) }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun onToggleSaved(recipeId: String, onAuthRequired: () -> Unit) {
        if(isLoggedIn.value) {
            viewModelScope.launch {
                val userId = SessionManagerFacade.currentUserStateFlow.value ?: return@launch
                val currentlySaved = recipeId in loggedInUser.value.savedRecipeIds
                if (currentlySaved) {
                    userRepository.deleteSavedRecipe(userId, recipeId)
                } else {
                    userRepository.addSavedRecipe(userId, recipeId)
                }

            }
        }
        else onAuthRequired()
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

                    return HomepageViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        userRepository = application.container.userRepository
                    ) as T
                }
            }

    }
}

data class HomepageUIState(
    val searchText: String = ""
)