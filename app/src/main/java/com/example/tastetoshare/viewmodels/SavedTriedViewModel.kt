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
import com.example.tastetoshare.data.entities.RawRecipePreview
import com.example.tastetoshare.data.entities.User
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedTriedViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedTriedUIState())
    val uiState: StateFlow<SavedTriedUIState> = _uiState.asStateFlow()

    private val currentUserId: StateFlow<String?> = SessionManagerFacade.currentUserStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionManagerFacade.currentUserStateFlow.value
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val loggedInUser: StateFlow<User> =
        currentUserId.flatMapLatest{ userId ->
            if (userId != null) userRepository.getUser(userId)
            else flowOf(User())
        }
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
        if (!SessionManagerFacade.isLoggedIn) {
            onAuthRequired()
            return
        }
        val currentlySaved = recipeId in loggedInUser.value.savedRecipeIds
        viewModelScope.launch {
            if (currentlySaved) {
                userRepository.deleteSavedRecipe(currentUserId.value ?: "", recipeId)
            } else {
                userRepository.addSavedRecipe(currentUserId.value ?: "", recipeId)
            }
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