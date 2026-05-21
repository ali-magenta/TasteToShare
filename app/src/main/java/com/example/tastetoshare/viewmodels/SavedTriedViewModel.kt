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
import androidx.navigation.toRoute
import com.example.tastetoshare.SessionManager
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.navigation.SavedRecipeRoute
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.filter

class SavedTriedViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
    private val loggedUserId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedTriedUIState())
    val uiState: StateFlow<SavedTriedUIState> = _uiState.asStateFlow()

    private val loggedInUser: StateFlow<User> =
        userRepository.getUser(loggedUserId ?: "")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = User()
            )


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    val rawRecipes: StateFlow<List<RawRecipePreview>> =
        uiState
            .debounce(300)
            .flatMapLatest { state ->
                recipeRepository.getRecipesPreviewBySearch(
                    searchString = state.searchText,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<RawRecipePreview>()
            )

    val savedRecipes: StateFlow<List<RawRecipePreview>> = combine(
        rawRecipes, loggedInUser
    ) { recipes, user ->
        recipes
            .filter { it.recipeId in user.savedRecipeIds }
            .map { it.copy(isSaved = true) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<RawRecipePreview>()
        )

    val triedRecipes: StateFlow<List<RawRecipePreview>> = combine(
        rawRecipes, loggedInUser
    ) { recipes, user ->
        recipes
            .filter { it.recipeId in user.triedRecipeIds }
            .map { it.copy(isSaved = it.recipeId in user.savedRecipeIds) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<RawRecipePreview>()
        )

    fun onSearchTextChanged(search: String) {
        _uiState.update { it.copy(searchText = search) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onToggleSaved(recipeId: String, onAuthRequired: () -> Unit) {
        val currentlySaved = recipeId in loggedInUser.value.savedRecipeIds
        if (SessionManagerFacade.isLoggedIn) {
            viewModelScope.launch {
                if (currentlySaved) {
                    userRepository.deleteSavedRecipe(loggedUserId ?: "", recipeId)
                } else {
                    userRepository.addSavedRecipe(loggedUserId ?: "", recipeId)
                }
            }
        }
        else {
            onAuthRequired()
        }
        if (currentlySaved) hideUnsavePopUp()
    }

    fun hideUnsavePopUp() {
        _uiState.update {
            it.copy(
                isShowingUnsavePopUp = false,
                recipeToUnsave = ""
            )
        }
    }

    fun showUnsavePopUp(recipeId: String) {
        _uiState.update {
            it.copy(
                isShowingUnsavePopUp = true,
                recipeToUnsave = recipeId
            )
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
                    val application = checkNotNull(extras[APPLICATION_KEY]) as TasteToShareApp

                    return SavedTriedViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        userRepository = application.container.userRepository,
                        loggedUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID
                    ) as T
                }
            }
    }

}

data class SavedTriedUIState(
    val searchText: String = "",
    val selectedTabIndex: Int = 0,
    val isShowingUnsavePopUp: Boolean = false,
    val recipeToUnsave: String = ""
)