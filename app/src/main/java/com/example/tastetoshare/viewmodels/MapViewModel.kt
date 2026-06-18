package com.example.tastetoshare.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.tastetoshare.TasteToShareApp
import com.example.tastetoshare.data.Cost
import com.example.tastetoshare.data.Difficulty
import com.example.tastetoshare.data.auth.SessionManagerFacade
import com.example.tastetoshare.data.entities.Country
import com.example.tastetoshare.data.entities.Ingredient
import com.example.tastetoshare.data.entities.Tag
import com.example.tastetoshare.data.entities.User
import com.example.tastetoshare.data.uiClasses.UIRecipePreview
import com.example.tastetoshare.data.uiClasses.toUi
import com.example.tastetoshare.repositories.CountryRepository
import com.example.tastetoshare.repositories.IngredientRepository
import com.example.tastetoshare.repositories.RecipeRepository
import com.example.tastetoshare.repositories.TagRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository,
    private val userRepository: UserRepository,
    private val countryRepository: CountryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUIState())

    val uiState: StateFlow<MapUIState> = _uiState.asStateFlow()

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

    private val topRecipeByCountryTags: StateFlow<Map<Tag, List<String>>> = recipeRepository.getTopRecipesURLsByCountries()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
        )

    private val countries: StateFlow<List<Country>> = countryRepository.getCountries()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
        )

    val topRecipesURLsByCountries: StateFlow<Map<Pair<Tag,Country?>, List<String>>> = combine(topRecipeByCountryTags, countries){
        top, countryList -> top.mapKeys {entry -> Pair(entry.key, countryList.find { it.name == entry.key.name }) }
    }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
    val recipes: StateFlow<List<UIRecipePreview>> =
        uiState
            .debounce(300)
            .flatMapLatest { state ->
                recipeRepository.getRecipesPreviewBySearch(
                    userId = state.userId,
                    searchString = state.searchText,
                    difficultyRange = state.selectedDifficulty,
                    costRange = state.selectedCost,
                    ingredients = state.selectedIngredients.ifEmpty { null },
                    tags = buildList {
                        state.selectedCuisine?.let{add(it)}
                        state.selectedDietary?.let{add(it)}
                        state.selectedCountry?.let{add(it)}
                    }.ifEmpty { null }
                )
            }.combine(loggedInUser) {
                    recipes, loggedInUser ->
                recipes.map { it.toUi(loggedInUser) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val ingredients: StateFlow<List<Ingredient>> =
        uiState
            .debounce(300)
            .flatMapLatest { state ->
                ingredientRepository.getAppIngredientsSortedByName(searchString = state.searchIngredient)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<Ingredient>()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val cuisineTags: StateFlow<List<Tag>> =
        tagRepository.getTags(
            type = "Cuisine Type", sortByName = true,
            searchString = ""
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<Tag>()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val dietaryTags: StateFlow<List<Tag>> =
        tagRepository.getTags(
            type = "Dietary Restrictions", sortByName = true,
            searchString = ""
        )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList<Tag>()
            )

    fun onSearchTextChanged(search: String) {
        _uiState.update { it.copy(searchText = search) }
    }

    fun onDifficultySelected(difficulty: Difficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty)}
    }

    fun onCostSelected(cost: Cost?) {
        _uiState.update { it.copy(selectedCost = cost)}
    }

    fun onIngredientsSelected(ingredients: List<Ingredient>) {
        _uiState.update { it.copy(selectedIngredients = ingredients) }
    }

    fun onSearchIngredientChanged(search: String) {
        _uiState.update { it.copy(searchIngredient = search)}
    }

    fun onCuisineSelected(cuisine: Tag?) {
        _uiState.update { it.copy(selectedCuisine = cuisine)}
    }

    fun onDietarySelected(dietary: Tag?) {
        _uiState.update { it.copy(selectedDietary = dietary)}
    }

    fun onCountrySelected(country: Tag?) {
        _uiState.update { it.copy(selectedCountry = country)}
    }

    fun hideBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false)}
    }

    fun openBottomSheet(country: Tag){
        _uiState.update { it.copy(
            selectedCountry = country,
            showBottomSheet = true
        )}
    }


    fun onToggleSaved(recipeId: String, onAuthRequired: () -> Unit) {
        if (isLoggedIn.value) {
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
        else {
            onAuthRequired()
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

                    return MapViewModel(
                        savedStateHandle = extras.createSavedStateHandle(),
                        recipeRepository = application.container.recipeRepository,
                        ingredientRepository = application.container.ingredientRepository,
                        tagRepository = application.container.tagRepository,
                        userRepository = application.container.userRepository,
                        countryRepository = application.container.countryRepository
                    ) as T
                }
            }

    }
}

data class MapUIState(
    val selectedDifficulty: Difficulty? = null,
    val selectedCost: Cost? = null,
    val searchText: String = "",
    val selectedIngredients: List<Ingredient> = emptyList(),
    val searchIngredient: String = "",
    val selectedCuisine: Tag? = null,
    val selectedDietary: Tag? = null,
    val selectedCountry: Tag? = null,
    val userId: String? = null,
    val showBottomSheet: Boolean = false
)